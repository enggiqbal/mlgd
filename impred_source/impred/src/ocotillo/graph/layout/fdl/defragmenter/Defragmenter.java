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
package ocotillo.graph.layout.fdl.defragmenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.modular.ModularFdl;
import ocotillo.graph.layout.fdl.modular.ModularForce;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;

/**
 * Compacts the cluster nodes of a graph in a contiguous region.
 */
public class Defragmenter {

    private final ClusterPlacer clusterPlacer;
    private final NodePlacer nodePlacer;

    /**
     * Builder for Defragmenter.
     */
    public static class DefragmenterBuilder {

        private ClusterPlacer clusterPlacer;
        private NodePlacer nodePlacer;

        /**
         * Indicates the cluster placer to use in the defragmentation process.
         *
         * @param clusterPlacer the cluster placer.
         * @return the builder.
         */
        public DefragmenterBuilder withClusterPlacer(ClusterPlacer clusterPlacer) {
            this.clusterPlacer = clusterPlacer;
            return this;
        }

        /**
         * Indicates the node placer to use in the defragmentation process.
         *
         * @param nodePlacer the cluster node placer.
         * @return the builder.
         */
        public DefragmenterBuilder withNodePlacer(NodePlacer nodePlacer) {
            this.nodePlacer = nodePlacer;
            return this;
        }

        /**
         * Builds a defragmenter.
         *
         * @return the defragmenter.
         */
        public Defragmenter build() {
            clusterPlacer = clusterPlacer != null ? clusterPlacer : new ClusterPlacer.OriginalLayoutClusterPlacer(5.0);
            nodePlacer = nodePlacer != null ? nodePlacer : new NodePlacer.OriginalLayoutNodePlacer();
            return new Defragmenter(clusterPlacer, nodePlacer);
        }
    }

    /**
     * Constructor for a defragmenter.
     *
     * @param clusterPlacer the cluster placer to be used.
     */
    private Defragmenter(ClusterPlacer clusterPlacer, NodePlacer nodePlacer) {
        this.clusterPlacer = clusterPlacer;
        this.nodePlacer = nodePlacer;
    }

    /**
     * Performs the defragmentation of a graph.
     *
     * @param graph the graph.
     * @param elemDistance the desired element distance.
     * @param iterations the number of iterations to perform.
     */
    public void defragment(Graph graph, double elemDistance, int iterations) {
        Graph clusterPlacement = clusterPlacer.computePlacing(graph);
        NodeAttribute<Coordinates> initialNodePlacement = nodePlacer.computePlacing(graph, clusterPlacement);

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> originalPositions = new NodeAttribute<>(new Coordinates(0, 0));
        originalPositions.copy(positions);

        for (Node node : graph.nodes()) {
            positions.set(node, initialNodePlacement.get(node));
        }

        Boundaries boundaries = buildBoundaries(graph, clusterPlacement);

        ModularFdl modular = new ModularFdl.ModularFdlBuilder(graph)
                //.withForce(new ModularForce.EdgeAttraction(20))
                .withForce(new SetElementsRepulsion(elemDistance, boundaries))
                .withForce(new BoundaryEdgeAttraction(elemDistance / 2, boundaries))
                .withForce(new ModularForce.SelectedEdgeNodeRepulsion2D(elemDistance / 2, boundaries.edges))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(elemDistance))
                .withConstraint(new ModularConstraint.SurroundingEdges(boundaries.surroundingEdges))
                .withPostProcessing(new ModularPostProcessing.FlexibleEdges(boundaries.edges, elemDistance, 5 * elemDistance))
                .build();

        modular.iterate(iterations);
        modular.close();

        removeBoundaries(graph, boundaries);
    }

    /**
     * Builds the cluster boundaries.
     *
     * @param graph the graph.
     * @param clusterPlacement the graph indicating the cluster placement.
     * @return the boundaries.
     */
    private Boundaries buildBoundaries(Graph graph, Graph clusterPlacement) {
        Boundaries boundaries = new Boundaries();
        NodeAttribute<Coordinates> clusterPositions = clusterPlacement.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> clusterSizes = clusterPlacement.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);

        for (Graph cluster : graph.subGraphs()) {
            String clusterLabel = cluster.<String>graphAttribute(StdAttribute.label).get();
            Node clusterPlaceholder = clusterPlacement.getNode(clusterLabel);
            Coordinates clusterCenter = clusterPositions.get(clusterPlaceholder);
            Coordinates clusterSize = clusterSizes.get(clusterPlaceholder);
            Coordinates clusterBottomLeft = clusterCenter.minus(clusterSize.divide(2));

            Node boundaryNode = graph.newNode();
            Edge boundaryEdge = graph.newEdge(boundaryNode, boundaryNode);
            positions.set(boundaryNode, clusterBottomLeft);
            ControlPoints edgeBends = new ControlPoints();
            edgeBends.add(clusterBottomLeft.plus(new Coordinates(clusterSize.x(), 0)));
            edgeBends.add(clusterBottomLeft.plus(clusterSize));
            edgeBends.add(clusterBottomLeft.plus(new Coordinates(0, clusterSize.y())));
            edgePoints.set(boundaryEdge, edgeBends);

            boundaries.nodes.add(boundaryNode);
            boundaries.edges.add(boundaryEdge);
            Collection<Edge> surroundingEdge = new HashSet<>();
            surroundingEdge.add(boundaryEdge);
            for (Node node : cluster.nodes()) {
                boundaries.surroundingEdges.set(node, surroundingEdge);
            }
        }
        boundaries.surroundingEdges.setDefault(boundaries.edges);
        return boundaries;
    }

    /**
     * Removes the cluster boundaries.
     *
     * @param graph the graph.
     * @param boundaries the boundaries.
     */
    private void removeBoundaries(Graph graph, Boundaries boundaries) {
        for (Edge boundaryEdge : boundaries.edges) {
            graph.remove(boundaryEdge);
        }
        for (Node boundaryNode : boundaries.nodes) {
            graph.remove(boundaryNode);
        }
    }

    /**
     * Data structures containing the information about the boundaries.
     */
    private static class Boundaries {

        Set<Node> nodes = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        NodeAttribute<Collection<Edge>> surroundingEdges = new NodeAttribute<>((Collection<Edge>) new HashSet<Edge>());
    }

    /**
     * Repulsion force acting only on set elements.
     */
    private static class SetElementsRepulsion extends ModularForce.NodeNodeRepulsion2D {

        private final Boundaries boundaries;

        /**
         * Constructs a set element repulsion force.
         *
         * @param nodeNodeDistance the desired node-node distance.
         * @param boundaries the boundaries in the defragmenter.
         */
        public SetElementsRepulsion(double nodeNodeDistance, Boundaries boundaries) {
            super(nodeNodeDistance);
            this.boundaries = boundaries;
        }

        @Override
        protected Collection<Node> firstLevelNodes() {
            return keepOnlySetElement(super.firstLevelNodes());
        }

        @Override
        protected Collection<Node> secondLevelNodes(Node node) {
            return keepOnlySetElement(super.secondLevelNodes(node));
        }

        /**
         * Filters nodes that are not set elements from the force computation.
         *
         * @param nodes the nodes normally taken into consideration.
         * @return the set elements contained in the given parameter.
         */
        private Collection<Node> keepOnlySetElement(Collection<Node> nodes) {
            Set<Node> elements = new HashSet<>();
            for (Node node : nodes) {
                Edge originalEdge = synchronizer().getOriginalEdge(node);
                if ((originalEdge != null && !boundaries.edges.contains(originalEdge))
                        || !boundaries.nodes.contains(node)) {
                    elements.add(node);
                }
            }
            return elements;
        }

    }

    /**
     * Force that attracts the extremities of the boundary segments.
     */
    private static class BoundaryEdgeAttraction extends ModularForce.EdgeAttraction2D {

        private final Boundaries boundaries;

        /**
         * Constructs a boundary edge attraction force.
         *
         * @param edgeLength the desired edge length.
         * @param boundaries the boundaries in the defragmenter.
         */
        public BoundaryEdgeAttraction(double edgeLength, Boundaries boundaries) {
            super(edgeLength);
            this.boundaries = boundaries;
        }

        @Override
        protected Collection<Edge> edges() {
            List<Edge> boundaryEdges = new ArrayList<>();
            for (Edge edge : boundaries.edges) {
                boundaryEdges.addAll(synchronizer().getMirrorEdge(edge).segments());
            }
            return boundaryEdges;
        }

    }
}
