/**
 * Copyright © 2014-2016 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.graph.layout.fdl.modular;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.GeomE;
import ocotillo.geometry.GeomE.PointRelation;
import ocotillo.geometry.GeomNumeric;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;

/**
 * Constrain for the ModularFdl algorithm.
 */
public abstract class ModularConstraint extends ModularElement {

    /**
     * Computes the movement constraints for all the nodes in the graph.
     *
     * @return the computed constraints.
     */
    protected abstract NodeAttribute<Double> computeConstraints();

    /**
     * Constraint that gradually decreases the global max movement for that the
     * nodes can perform at each iteration.
     */
    public static class DecreasingMaxMovement extends ModularConstraint {

        /**
         * The global max movement at the beginning of the computation.
         */
        protected double initialMaxMovement;

        /**
         * Constructs a decreasing max movement constraint.
         *
         * @param initialMaxMovement the max movement at the beginning of the
         * computation.
         */
        public DecreasingMaxMovement(double initialMaxMovement) {
            this.initialMaxMovement = initialMaxMovement;
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            return new NodeAttribute<>(initialMaxMovement * temperature());
        }
    }

    /**
     * Constraint that consents large movements only if the force direction is
     * consistent in successive iterations.
     */
    public static class MovementAcceleration extends ModularConstraint {

        /**
         * The max movement allowed.
         */
        protected double maxMovement;
        /**
         * The Euclidean geometry to use in the computation.
         */
        protected GeomE geometry = Geom.e2D;
        protected Map<Node, Coordinates> previousMovements = new HashMap<>();

        /**
         * Constructs a constraint that limits the current node movement when
         * the angle with the movement at previous steps is large. This
         * constraint increases the layout stability and drastically reduces
         * node oscillations.
         *
         * @param maxMovement the max movement.
         * @param geometry the geometry to use in the computation.
         */
        public MovementAcceleration(double maxMovement, GeomE geometry) {
            this.maxMovement = maxMovement;
            this.geometry = geometry;
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            NodeAttribute<Double> constraints = new NodeAttribute<>(Double.POSITIVE_INFINITY);
            for (Node node : mirrorGraph().nodes()) {

                Coordinates currentForce = forces().get(node);

                if (Geom.e2D.almostZero(currentForce)) {
                    previousMovements.remove(node);
                    continue;
                }

                double currentLimit;
                if (!previousMovements.containsKey(node)) {
                    currentLimit = maxMovement / 5;
                } else {
                    Coordinates previousMovement = previousMovements.get(node);
                    double angleDiff = geometry.betweenAngle(currentForce, previousMovement);
                    double previousMagnitude = geometry.magnitude(previousMovement);
                    if (angleDiff < Math.PI / 3) {
                        currentLimit = Math.min(previousMagnitude * (1 + 2 * (1 - angleDiff / (Math.PI / 3))), maxMovement);
                    } else if (angleDiff < Math.PI / 2) {
                        currentLimit = previousMagnitude;
                    } else {
                        currentLimit = previousMagnitude / (1 + 4 * (angleDiff / (Math.PI / 2) - 1));
                    }
                }
                constraints.set(node, currentLimit);
                previousMovements.put(node, geometry.unitVector(currentForce).timesIP(currentLimit));
            }
            return constraints;
        }
    }

    /**
     * Constraint that limit the node movement so that each node does not cross
     * its surrounding edges. It only operates in 2D.
     */
    public static class SurroundingEdges extends ModularConstraint {

        /**
         * The surrounding edges for each node.
         */
        protected NodeAttribute<Collection<Edge>> surroundingEdges;

        /**
         * Construct a surrounding edges constraint.
         *
         * @param surroundingEdges the surrounding edges.
         */
        public SurroundingEdges(NodeAttribute<Collection<Edge>> surroundingEdges) {
            this.surroundingEdges = surroundingEdges;
        }

        /**
         * Returns the affected nodes.
         *
         * @return the affected nodes.
         */
        protected Collection<Node> nodes() {
            return mirrorGraph().nodes();
        }

        /**
         * Returns the edges to involve in the computation for a given node.
         *
         * @param node the given node.
         * @return the edges to check.
         */
        protected Collection<Edge> edges(Node node) {
            Collection<Edge> edges = new HashSet<>();
            for (Edge originalSurroundingEdge : surroundingEdges.get(node)) {
                edges.addAll(synchronizer().getMirrorEdge(originalSurroundingEdge).segments());
            }
            pruneDistantEdges(edges, node);
            edges.removeAll(mirrorGraph().inEdges(node));
            edges.removeAll(mirrorGraph().outEdges(node));
            return edges;
        }

        /**
         * Removes from a edge collection the edges that are too far from the
         * given node.
         *
         * @param edges the edge collection to be pruned.
         * @param node the given node.
         */
        protected void pruneDistantEdges(Collection<Edge> edges, Node node) {
            if (constraints().getDefault() != Double.POSITIVE_INFINITY) {
                double distanceToConsider = 3 * constraints().getDefault();
                edges.retainAll(locator().getCloseEdges(node, distanceToConsider));
            }
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            NodeAttribute<Double> maxMovement = new NodeAttribute<>(Double.POSITIVE_INFINITY);
            for (Node node : nodes()) {
                for (Edge edge : edges(node)) {
                    if (!edge.isNodeExtremity(node)) {
                        Coordinates nPos = mirrorPositions().get(node);
                        Coordinates sPos = mirrorPositions().get(edge.source());
                        Coordinates tPos = mirrorPositions().get(edge.target());
                        PointRelation relation = Geom.e2D.pointSegmentRelation(nPos, sPos, tPos);
                        if (relation.isProjectionIncluded()) {
                            computeConstraintsProjInside(node, edge, relation.projection(), maxMovement);
                        } else {
                            computeConstraintsProjOutside(node, edge, maxMovement);
                        }
                    }
                }
            }
            return maxMovement;
        }

        /**
         * Computes the constraints when the node projection is inside the edge.
         *
         * @param node the node.
         * @param edge the edge.
         * @param projection the node projection on the edge.
         * @param maxMovement the max movements.
         */
        protected void computeConstraintsProjInside(Node node, Edge edge, Coordinates projection, NodeAttribute<Double> maxMovement) {
            Coordinates pn = projection.minus(mirrorPositions().get(node));
            double nodeCollisionAngle = Geom.e2D.angle(pn);
            double edgeCollisionAngle = nodeCollisionAngle + Math.PI;
            double collisionDistance = Geom.e2D.magnitude(pn) / 2;
            reduceMovement(node, nodeCollisionAngle, collisionDistance, maxMovement);
            reduceMovement(edge.source(), edgeCollisionAngle, collisionDistance, maxMovement);
            reduceMovement(edge.target(), edgeCollisionAngle, collisionDistance, maxMovement);
        }

        /**
         * Computes the constraints when the node projection is outside the
         * edge.
         *
         * @param node the node.
         * @param edge the edge.
         * @param maxMovement the max movements.
         */
        protected void computeConstraintsProjOutside(Node node, Edge edge, NodeAttribute<Double> maxMovement) {
            Coordinates nodePos = mirrorPositions().get(node);
            Coordinates sourcePos = mirrorPositions().get(edge.source());
            Coordinates targetPos = mirrorPositions().get(edge.target());

            Node closeExtremity, farExtremity;
            if (Geom.e2D.magnitude(sourcePos.minus(nodePos)) < Geom.e2D.magnitude(targetPos.minus(nodePos))) {
                closeExtremity = edge.source();
                farExtremity = edge.target();
            } else {
                closeExtremity = edge.target();
                farExtremity = edge.source();
            }

            Coordinates closePos = mirrorPositions().get(closeExtremity);
            Coordinates farPos = mirrorPositions().get(farExtremity);
            double nodeAngle = Geom.e2D.angle(closePos.minus(nodePos));
            double edgeAngle = GeomNumeric.posNormalizeRadiansAngle(nodeAngle + Math.PI);

            double axisAngle = nodeAngle + Math.PI / 2;
            Coordinates axisPointA = nodePos.plus(closePos.minus(nodePos).divide(2));
            Coordinates axisPointB = Geom.e2D.unitVector(axisAngle).plusIP(axisPointA);

            double nodeCollDist = Geom.e2D.magnitude(closePos.minus(nodePos)) / 2;
            double closeExtrCollDist = nodeCollDist;
            double farExtrCollDist = Geom.e2D.pointLineRelation(farPos, axisPointA, axisPointB).distance();

            reduceMovement(node, nodeAngle, nodeCollDist, maxMovement);
            reduceMovement(closeExtremity, edgeAngle, closeExtrCollDist, maxMovement);
            reduceMovement(farExtremity, edgeAngle, farExtrCollDist, maxMovement);
        }

        /**
         * Reduces the node max movement.
         *
         * @param node the node.
         * @param collisionAngle the angle of most direct collision.
         * @param collisionDistance the collision distance when moving on the
         * collision angle.
         * @param maxMovement the max movements.
         */
        protected void reduceMovement(Node node, double collisionAngle, double collisionDistance, NodeAttribute<Double> maxMovement) {
            double forceAngle = Geom.e2D.angle(forces().get(node));
            double forceCollAngle = Math.abs(GeomNumeric.normalizeRadiansAngle(forceAngle - collisionAngle));
            if (forceCollAngle < Math.PI / 2.0) {
                maxMovement.set(node, Math.min(maxMovement.get(node), collisionDistance / Math.cos(forceCollAngle)));
            }
        }
    }

    /**
     * Constraint that forbids certain nodes to move.
     */
    public static class PinnedNodes extends ModularConstraint {

        /**
         * The pinned nodes.
         */
        protected NodeAttribute<Boolean> pinnedNodes;

        /**
         * Constructs a pinned nodes constraint.
         *
         * @param pinnedNodes the pinned nodes.
         */
        public PinnedNodes(NodeAttribute<Boolean> pinnedNodes) {
            this.pinnedNodes = pinnedNodes;
        }

        /**
         * Constructs a pinned nodes constraint.
         *
         * @param pinnedNodes the pinned nodes.
         */
        public PinnedNodes(Collection<Node> pinnedNodes) {
            this.pinnedNodes = new NodeAttribute<>(false);
            for (Node node : pinnedNodes) {
                this.pinnedNodes.set(node, true);
            }
        }

        @Override
        protected NodeAttribute<Double> computeConstraints() {
            NodeAttribute<Double> maxMovement = new NodeAttribute<>(Double.POSITIVE_INFINITY);
            for (Node node : mirrorGraph().nodes()) {
                if (pinnedNodes.get(node)) {
                    maxMovement.set(node, 0.0);
                }
            }
            return maxMovement;
        }
    }
}
