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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.GeomNumeric;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.NodeShape;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.MirrorEdge;
import ocotillo.graph.layout.Layout2D;

/**
 * Force for the ModularFdl algorithm.
 */
public abstract class ModularForce extends ModularElement {

    /**
     * Computes the forces for all the nodes in the graph.
     *
     * @return the computed forces.
     */
    protected abstract NodeAttribute<Coordinates> computeForces();

    /**
     * Returns the desired (or minimal, maximal) distance that this force aims
     * to achieve.
     *
     * @return the desired distance.
     */
    protected double desiredDistance() {
        return 0.0;
    }
    
    /**
     * Returns the level where the two vertices lie.
     *
     * @param a the first node.
     * @param b the second node.
     * @return the level.
     */
    protected int nodeLevel(Node a, Node b) {
    	
    	

    	NodeAttribute<Integer> mirrorLevels = mirrorGraph().nodeAttribute(StdAttribute.nodeLevel);
    	
    	int levelA = mirrorLevels.get(a);
    	int levelB = mirrorLevels.get(b);
    	
    	int level = Math.max(levelA, levelB);
    	    	
    	return Math.max(level, 1);
    }

    /**
     * Computes current and desired distances between two nodes.
     *
     * @param a the first node.
     * @param b the second node.
     * @param temperature the temperature.
     * @return the distances.
     */
    protected Distances computeDistances2D(Node a, Node b, double temperature) {
        NodeAttribute<Coordinates> mirrorSizes = mirrorGraph().nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<NodeShape> mirrorShapes = mirrorGraph().nodeAttribute(StdAttribute.nodeShape);
        Coordinates aPos = mirrorPositions().get(a);
        Coordinates bPos = mirrorPositions().get(b);
        
   //     System.out.println("computeDistances2D");
        
      //  System.out.println(a +":" + aPos + " " + b +":" + bPos);
        
        int level = nodeLevel(a, b);
        
        double distanceAtZeroSize = Geom.e2D.magnitude(bPos.restrMinus(aPos, 2));
        double distanceAtFullZize = Layout2D.nodeNodeGlyphDistance(mirrorGraph(), a, b, mirrorPositions(), mirrorSizes, mirrorShapes);
        
        double elemRadiusAtFullSize = distanceAtZeroSize - distanceAtFullZize;
        double currentElemRadius = elemRadiusAtFullSize * (1 - temperature);
        double currentDistance = distanceAtZeroSize - currentElemRadius;
        double desiredDistance = (desiredDistance()/level) + elemRadiusAtFullSize - currentElemRadius;
//        desiredDistance /= level;
        return new Distances(currentDistance, desiredDistance);
    }

    /**
     * Computes current and desired distances between a node and an edge.
     *
     * @param node the node.
     * @param edge the edge.
     * @param temperature the temperature.
     * @return the distances.
     */
    protected Distances computeDistances2D(Node node, Edge edge, double temperature) {
        NodeAttribute<Coordinates> mirrorSizes = mirrorGraph().nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<NodeShape> mirrorShapes = mirrorGraph().nodeAttribute(StdAttribute.nodeShape);
        EdgeAttribute<Double> mirrorEdgeWidths = mirrorGraph().edgeAttribute(StdAttribute.edgeWidth);
        Coordinates nPos = mirrorPositions().get(node);
        Coordinates ePos = Layout2D.closestEdgePoint(mirrorGraph(), nPos, edge, mirrorPositions(), null, null);
        double distanceAtZeroSize = Geom.e2D.magnitude(nPos.restrMinus(ePos, 2));
        double distanceAtFullZize = Layout2D.nodeEdgeGlyphDistance(mirrorGraph(), node, edge, mirrorPositions(), mirrorSizes, mirrorShapes, mirrorEdgeWidths, null, null);
        double elemRadiusAtFullSize = distanceAtZeroSize - distanceAtFullZize;
        double currentElemRadius = elemRadiusAtFullSize * (1 - temperature);
        double currentDistance = distanceAtZeroSize - currentElemRadius;
        double desiredDistance = desiredDistance() + elemRadiusAtFullSize;
        return new Distances(currentDistance, desiredDistance);
    }

    /**
     * Container for the current and desired distances between two elements.
     */
    protected static class Distances {

        /**
         * The minimal distance between two elements.
         */
        protected static final double minimal = 0.01;

        /**
         * The computed distance between two elements for the given temperature.
         * The glyph sizes are linearly interpolated from zero size at
         * temperature 1, to normal size at temperature 0. This value is limited
         * not to be lower of the minimal distance.
         */
        protected double currentDistance;
        /**
         * The desired distance between the center of two elements. The distance
         * is computed considering the elements at full size.
         */
        protected double desiredDistance;

        /**
         * Construct a distances object.
         *
         * @param currentDistance the current distance.
         * @param desiredDistance the desired distance.
         */
        public Distances(double currentDistance, double desiredDistance) {
            this.currentDistance = Math.max(minimal, currentDistance);
            this.desiredDistance = desiredDistance;
        }
    }

    /**
     * Force that attracts nodes connected with an edge.
     */
    public static class EdgeAttraction2D extends ModularForce {

        /**
         * The desired edge length.
         */
        protected double edgeLength;
        /**
         * The initial force exponent.
         */
        public double initialExponent = 1;
        /**
         * The final force exponent.
         */
        public double finalExponent = 1;

        /**
         * Constructs a force that attracts edge extremities farther than the
         * given distance.
         *
         * @param edgeLength the distance at which the force intensifies.
         */
        public EdgeAttraction2D(double edgeLength) {
            this.edgeLength = edgeLength;
        }

        @Override
        protected double desiredDistance() {
            return edgeLength;
        }

        /**
         * Selects the edges involved in the computation.
         *
         * @return the collection of edges.
         */
        protected Collection<Edge> edges() {
            return mirrorGraph().edges();
        }

        /**
         * Computes the exponent for the given force.
         *
         * @return the exponent that corresponds to that temperature.
         */
        protected double computeExponent() {
            return finalExponent + (initialExponent - finalExponent) * temperature();
        }

        /**
         * Computes the forces for the given edge and temperature. The force to
         * apply to the source will be contained in the position 0 of the
         * returned array, and the force to apply to the target in position 1.
         *
         * @param edge the edge.
         * @return the pair of forces to apply to the given nodes.
         */
        protected Coordinates[] computeForces(Edge edge) {
            Coordinates s = mirrorPositions().get(edge.source());
            Coordinates t = mirrorPositions().get(edge.target());
            Coordinates ts = Geom.e2D.unitVector(s.restrMinus(t, 2));
            Distances distances = computeDistances2D(edge.source(), edge.target(), temperature());
            Coordinates force = (ts).timesIP(Math.pow(distances.currentDistance / distances.desiredDistance, computeExponent()));
            return new Coordinates[]{force.minus(), force};
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            for (Edge edge : edges()) {
                Coordinates[] compForces = computeForces(edge);
                forces.set(edge.source(), compForces[0].plusIP(forces.get(edge.source())));
                forces.set(edge.target(), compForces[1].plusIP(forces.get(edge.target())));
            }
            return forces;
        }
    }

  
   

    
    
    /**
     * Force that repels nearby labels.
     */
    public static class LabelLabelRepulsion2D extends ModularForce {

        /**
         * The desired distance between two nodes.
         */
        protected double nodeNodeDistance;
        protected double delta;
        /**
         * The factor of the desired distance at witch two nodes are close
         * enough to be significantly effecting each other.
         */
        public double distanceActivityFactor = 3;
        /**
         * The initial force exponent.
         */
        public double initialExponent = 2;
        /**
         * The final force exponent.
         */
        public double finalExponent = 2;

        /**
         * Constructs a force that strongly repels nodes closer than the given
         * distance.
         *
         * @param nodeNodeDistance the distance at which the force intensifies.
         */
        public LabelLabelRepulsion2D(double delta) {
            this.nodeNodeDistance = delta;
            this.delta=delta;
            
        }

        @Override
        protected double desiredDistance() {
            return nodeNodeDistance;
        }

        /**
         * Selects the first nodes of the node-node pairs involved in the
         * computation.
         *
         * @return the collection of first nodes.
         */
        protected Collection<Node> firstLevelNodes() {
            return mirrorGraph().nodes();
        }

        /**
         * Selects the second nodes of the node-node pairs involved in the
         * computation.
         *
         * @param node the first node.
         * @return the collection of second nodes related to the given first
         * node.
         */
        protected Collection<Node> secondLevelNodes(Node node) {
            Collection<Node> nodes = new HashSet<>(mirrorGraph().nodes());
            pruneDistantNodes(nodes, node);
            pruneLowerIdNodes(nodes, node);
            return nodes;
        }

        /**
         * Removes from a node collection the nodes that are too far from the
         * given one.
         *
         * @param nodes the node collection to be pruned.
         * @param node the given node.
         */
        protected void pruneDistantNodes(Collection<Node> nodes, Node node) {
            double distanceToConsider = distanceActivityFactor * nodeNodeDistance;
            nodes.retainAll(locator().getCloseNodes(node, distanceToConsider));
        }

        /**
         * Removes from a node collection the nodes with lower ID than the given
         * one.
         *
         * @param nodes the node collection to be pruned.
         * @param node the given node.
         */
        protected void pruneLowerIdNodes(Collection<Node> nodes, Node node) {
            Set<Node> nodesToPrune = new HashSet<>();
            for (Node otherNode : nodes) {
                if (node.compareTo(otherNode) >= 0) {
                    nodesToPrune.add(otherNode);
                }
            }
            nodes.removeAll(nodesToPrune);
        }

        /**
         * Computes the exponent for the given force.
         *
         * @return the exponent that corresponds to that temperature.
         */
        protected double computeExponent() {
            return finalExponent + (initialExponent - finalExponent) * temperature();
        }

        /**
         * Computes the forces for the given pair of nodes and temperature. The
         * force to apply to the first node will be contained in the position 0
         * of the returned array, and the force to apply to the second node in
         * position 1.
         *
         * @param nodeA the first node.
         * @param nodeB the second node.
         * @return the pair of forces to apply to the given nodes.
         */
		protected Coordinates[] computeForces(Node nodeA, Node nodeB) {
			// System.out.println("iqtest");
			double lblshift = delta;
			Coordinates a = mirrorPositions().get(nodeA);
			Coordinates b = mirrorPositions().get(nodeB);

			// System.out.println(a + " " + b);
			//399 432
			int ax;
		//	if (nodeA.id() ==399 && nodeB.id()=="432" )
			//	  ax=0;
			
			if (nodeA.id()=="432" && nodeB.id()=="399" )
				  ax=0;
			
			
			NodeAttribute<Coordinates> mirrorSizes = mirrorGraph().nodeAttribute(StdAttribute.nodeSize);

			Coordinates sizeA = mirrorSizes.get(nodeA);
			Coordinates sizeB = mirrorSizes.get(nodeB);
			double w1 = sizeA.x() / 2, h1 = sizeA.y() / 2, w2 = sizeB.x() / 2, h2 = sizeB.y() / 2;
			double x1 = a.x(), y1 = a.y(), x2 = b.x(), y2 = b.y();

			double dx = w1 + w2 - Math.abs(x1 - x2);
			double dy = h1 + h2 - Math.abs(y1 - y2);
			if (dx < 0 || dy < 0) {
				// System.out.println("no overlap");
				return new Coordinates[] { new Coordinates(0, 0), new Coordinates(0, 0) };

			}

			double t = 0;

			if (dx > 0 && dy > 0) {

				t = (Math.min(x1 + w1, x2 + w2) - Math.max(x1 - w1, x2 - w2))
						* (Math.min(y1 + h1, y2 + h2) - Math.max(y1 - h1, y2 - h2));
				System.out.println(nodeA.id() + " " + nodeB.id() + "  overlap:" + t);

				
				double newx1 = x1, newx2 = x2, fy1 = y1, fy2 = y2, fx1 = lblshift, fx2 = lblshift;

				if (x1 < x2) {
					newx1 = x1 - lblshift;
					newx2 = x2 + lblshift;
					fx1 = -lblshift;
					fx2 = lblshift;
				}
				if (x1 > x2) {
					newx1 = x1 + lblshift;
					newx2 = x2 - lblshift;
					fx1 = lblshift;
					fx2 = -lblshift;
				}

				double slop = (y2 - y1) / (x2 - x1);
				// y=slop * (x-x2) + y1
				fy1 = slop * (newx1 - x1) + y1 - y1;
				fy2 = slop * (newx2 - x1) + y1 - y2;
				Coordinates aa = new Coordinates(fx1, fy1);
				Coordinates bb = new Coordinates(fx2, fy2);

				return new Coordinates[] { aa, bb };
			}
			return new Coordinates[] { new Coordinates(0, 0), new Coordinates(0, 0) };

		}

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            Collection<Node> n=firstLevelNodes();
            ArrayList <Node> bx= new ArrayList(n);
            
            for (int i =0 ; i< bx.size(); i++)
            {
            	for (int j=i+1; j< bx.size(); j++)
            	{
            		Node nodeA= bx.get(i);
            		Node nodeB= bx.get(j);
            		
                    Coordinates[] compForces = computeForces(nodeA, nodeB);
                    forces.set(nodeA, compForces[0].plusIP(forces.get(nodeA)));
                    forces.set(nodeB, compForces[1].plusIP(forces.get(nodeB)));
                    
            	}
            }
            /*
            for (Node nodeA : n) {
                for (Node nodeB : n) {
                	if (nodeA==nodeB) continue;
                	
                    Coordinates[] compForces = computeForces(nodeA, nodeB);
                    forces.set(nodeA, compForces[0].plusIP(forces.get(nodeA)));
                    forces.set(nodeB, compForces[1].plusIP(forces.get(nodeB)));
                }
            }
            */
            return forces;
        }
    }

    
    
    
    
    /**
     * Force that repels nearby nodes.
     */
    public static class NodeNodeRepulsion2D extends ModularForce {

        /**
         * The desired distance between two nodes.
         */
        protected double nodeNodeDistance;
        /**
         * The factor of the desired distance at witch two nodes are close
         * enough to be significantly effecting each other.
         */
        public double distanceActivityFactor = 3;
        /**
         * The initial force exponent.
         */
        public double initialExponent = 2;
        /**
         * The final force exponent.
         */
        public double finalExponent = 2;

        /**
         * Constructs a force that strongly repels nodes closer than the given
         * distance.
         *
         * @param nodeNodeDistance the distance at which the force intensifies.
         */
        public NodeNodeRepulsion2D(double nodeNodeDistance) {
            this.nodeNodeDistance = nodeNodeDistance;
        }

        @Override
        protected double desiredDistance() {
            return nodeNodeDistance;
        }

        /**
         * Selects the first nodes of the node-node pairs involved in the
         * computation.
         *
         * @return the collection of first nodes.
         */
        protected Collection<Node> firstLevelNodes() {
            return mirrorGraph().nodes();
        }

        /**
         * Selects the second nodes of the node-node pairs involved in the
         * computation.
         *
         * @param node the first node.
         * @return the collection of second nodes related to the given first
         * node.
         */
        protected Collection<Node> secondLevelNodes(Node node) {
            Collection<Node> nodes = new HashSet<>(mirrorGraph().nodes());
            pruneDistantNodes(nodes, node);
            pruneLowerIdNodes(nodes, node);
            return nodes;
        }

        /**
         * Removes from a node collection the nodes that are too far from the
         * given one.
         *
         * @param nodes the node collection to be pruned.
         * @param node the given node.
         */
        protected void pruneDistantNodes(Collection<Node> nodes, Node node) {
            double distanceToConsider = distanceActivityFactor * nodeNodeDistance;
            nodes.retainAll(locator().getCloseNodes(node, distanceToConsider));
        }

        /**
         * Removes from a node collection the nodes with lower ID than the given
         * one.
         *
         * @param nodes the node collection to be pruned.
         * @param node the given node.
         */
        protected void pruneLowerIdNodes(Collection<Node> nodes, Node node) {
            Set<Node> nodesToPrune = new HashSet<>();
            for (Node otherNode : nodes) {
                if (node.compareTo(otherNode) >= 0) {
                    nodesToPrune.add(otherNode);
                }
            }
            nodes.removeAll(nodesToPrune);
        }

        /**
         * Computes the exponent for the given force.
         *
         * @return the exponent that corresponds to that temperature.
         */
        protected double computeExponent() {
            return finalExponent + (initialExponent - finalExponent) * temperature();
        }

        /**
         * Computes the forces for the given pair of nodes and temperature. The
         * force to apply to the first node will be contained in the position 0
         * of the returned array, and the force to apply to the second node in
         * position 1.
         *
         * @param nodeA the first node.
         * @param nodeB the second node.
         * @return the pair of forces to apply to the given nodes.
         */
        protected Coordinates[] computeForces(Node nodeA, Node nodeB) {
        	

  
            
            Coordinates a = mirrorPositions().get(nodeA);
            Coordinates b = mirrorPositions().get(nodeB);
       //     System.out.println("iqtest");
         // 	System.out.println(nodeA.id() + " " + nodeB.id());
           // System.out.println(a + " " + b);
      
            
            Coordinates ab = Geom.e2D.unitVector(a.restrMinus(b, 2));
            Distances distances = computeDistances2D(nodeA, nodeB, temperature());
            Coordinates force = (ab).timesIP(Math.pow(distances.desiredDistance / distances.currentDistance, computeExponent()));
            return new Coordinates[]{force, force.minus()};
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            for (Node nodeA : firstLevelNodes()) {
                for (Node nodeB : secondLevelNodes(nodeA)) {
                    Coordinates[] compForces = computeForces(nodeA, nodeB);
                    forces.set(nodeA, compForces[0].plusIP(forces.get(nodeA)));
                    forces.set(nodeB, compForces[1].plusIP(forces.get(nodeB)));
                }
            }
            return forces;
        }
    }

    /**
     * Force that repels nearby nodes.
     */
    public static class SelectedNodeNodeRepulsion extends NodeNodeRepulsion2D {

        protected Collection<Node> firstLevelNodes;
        protected Collection<Node> secondLevelNodes;

        /**
         * Constructs a force that strongly repels nodes closer than the given
         * distance.
         *
         * @param nodeNodeDistance the distance at which the force intensifies.
         * @param selectedNodes the selected nodes.
         */
        public SelectedNodeNodeRepulsion(double nodeNodeDistance, Collection<Node> selectedNodes) {
            super(nodeNodeDistance);
            this.firstLevelNodes = selectedNodes;
        }

        /**
         * Constructs a force that strongly repels nodes closer than the given
         * distance.
         *
         * @param nodeNodeDistance the distance at which the force intensifies.
         * @param firstLevelNodes the first level nodes.
         * @param secondLevelNodes the second level nodes.
         */
        public SelectedNodeNodeRepulsion(double nodeNodeDistance, Collection<Node> firstLevelNodes, Collection<Node> secondLevelNodes) {
            super(nodeNodeDistance);
            this.firstLevelNodes = firstLevelNodes;
            this.secondLevelNodes = secondLevelNodes;
        }

        /**
         * Selects the first nodes of the node-node pairs involved in the
         * computation.
         *
         * @return the collection of first nodes.
         */
        @Override
        protected Collection<Node> firstLevelNodes() {
            return firstLevelNodes;
        }

        /**
         * Selects the second nodes of the node-node pairs involved in the
         * computation.
         *
         * @param node the first node.
         * @return the collection of second nodes related to the given first
         * node.
         */
        @Override
        protected Collection<Node> secondLevelNodes(Node node) {
            Collection<Node> nodes = new HashSet<>(secondLevelNodes != null ? secondLevelNodes : firstLevelNodes);
            pruneDistantNodes(nodes, node);
            pruneLowerIdNodes(nodes, node);
            return nodes;
        }
    }

    /**
     * Force that repels nearby edge-node pairs.
     */
    public static class EdgeNodeRepulsion2D extends ModularForce {

        /**
         * The desired node-edge distance.
         */
        protected double edgeNodeDistance;
        /**
         * The factor of the desired distance at witch two nodes are close
         * enough to be significantly effecting each other.
         */
        public double distanceActivityFactor = 3;
        /**
         * The initial force exponent.
         */
        public double initialExponent = 2;
        /**
         * The final force exponent.
         */
        public double finalExponent = 2;

        /**
         * Constructs a force that strongly repels nodes and edges closer than
         * the given distance.
         *
         * @param edgeNodeDistance the distance at which the force intensifies.
         */
        public EdgeNodeRepulsion2D(double edgeNodeDistance) {
            this.edgeNodeDistance = edgeNodeDistance;
        }

        @Override
        protected double desiredDistance() {
            return edgeNodeDistance;
        }

        /**
         * Selects the edges involved in the computation.
         *
         * @return the collection of edges.
         */
        protected Collection<Edge> edges() {
            return mirrorGraph().edges();
        }

        /**
         * Selects the nodes relative to a given edge involved in the
         * computation.
         *
         * @param edge the edge.
         * @return the collection of nodes to be considered for the edge.
         */
        protected Collection<Node> nodes(Edge edge) {
            Collection<Node> nodes = new HashSet<>(mirrorGraph().nodes());
            pruneDistantNodes(nodes, edge);
            nodes.remove(edge.source());
            nodes.remove(edge.target());
            return nodes;
        }

        /**
         * Removes from a node collection the nodes that are too far from the
         * given edge.
         *
         * @param nodes the node collection to be pruned.
         * @param edge the given edge.
         */
        protected void pruneDistantNodes(Collection<Node> nodes, Edge edge) {
            double distanceToConsider = distanceActivityFactor * edgeNodeDistance;
            nodes.retainAll(locator().getCloseNodes(edge, distanceToConsider));
        }

        /**
         * Computes the exponent for the given force.
         *
         * @return the exponent that corresponds to that temperature.
         */
        protected double computeExponent() {
            return finalExponent + (initialExponent - finalExponent) * temperature();
        }

        /**
         * Computes the forces for the given edge, node and temperature. The
         * force to apply to the source will be contained in the position 0 of
         * the returned array, the force to apply to the target in position 1,
         * the force to apply to the node in position 2.
         *
         * @param edge the edge.
         * @param node the node.
         * @return the pair of forces to apply to the given nodes.
         */
        protected Coordinates[] computeForces(Edge edge, Node node) {
            Coordinates s = mirrorPositions().get(edge.source());
            Coordinates t = mirrorPositions().get(edge.target());
            Coordinates n = mirrorPositions().get(node);
            Coordinates p = Geom.e2D.pointLineRelation(n, s, t).projection();

            if (Geom.e2D.almostEqual(n, p)) {
                return new Coordinates[]{new Coordinates(0, 0), new Coordinates(0, 0), new Coordinates(0, 0)};
            }

            if (Geom.e2D.isPointInSegment(p, s, t)) {
                Coordinates pn = Geom.e2D.unitVector(p.restrMinus(n, 2));
                Distances distances = computeDistances2D(node, edge, temperature());
                Coordinates force = (pn).timesIP(Math.pow(distances.desiredDistance / distances.currentDistance, computeExponent()));
                double balance = Geom.e2D.magnitude(p.restrMinus(t, 2)) / Geom.e2D.magnitude(s.restrMinus(t, 2));
                return new Coordinates[]{force.times(balance), force.times(1 - balance), force.minus()};
            } else {
                Coordinates ns = Geom.e2D.unitVector(n.restrMinus(s, 2));
                Distances nsDistances = computeDistances2D(node, edge.source(), temperature());
                Coordinates nsForce = (ns).timesIP(Math.pow(nsDistances.desiredDistance / nsDistances.currentDistance, computeExponent()));
                Coordinates nt = Geom.e2D.unitVector(n.restrMinus(t, 2));
                Distances ntDistances = computeDistances2D(node, edge.target(), temperature());
                Coordinates ntForce = (nt).timesIP(Math.pow(ntDistances.desiredDistance / ntDistances.currentDistance, computeExponent()));
                return new Coordinates[]{nsForce.minus(), ntForce.minus(), nsForce.plus(ntForce)};
            }
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            for (Edge edge : edges()) {
                for (Node node : nodes(edge)) {
                    Coordinates[] compForces = computeForces(edge, node);
                    forces.set(edge.source(), compForces[0].plusIP(forces.get(edge.source())));
                    forces.set(edge.target(), compForces[1].plusIP(forces.get(edge.target())));
                    forces.set(node, compForces[2].plusIP(forces.get(node)));
                }
            }
            return forces;
        }
    }

    /**
     * Edge-node repulsion force that acts on a specified sub-set of edges.
     */
    public static class SelectedEdgeNodeRepulsion2D extends EdgeNodeRepulsion2D {

        /**
         * The attribute indicating the edges to be considered.
         */
        protected Collection<Edge> selectedEdges;
        protected Collection<Node> selectedNodes;

        public SelectedEdgeNodeRepulsion2D(double nodeEdgeDistance, Collection<Edge> selectedEdges) {
            super(nodeEdgeDistance);
            this.selectedEdges = selectedEdges;
        }

        public SelectedEdgeNodeRepulsion2D(double nodeEdgeDistance, Collection<Edge> selectedEdges, Collection<Node> selectedNodes) {
            super(nodeEdgeDistance);
            this.selectedEdges = selectedEdges;
            this.selectedNodes = selectedNodes;
        }

        @Override
        protected Collection<Edge> edges() {
            Set<Edge> edges = new HashSet<>();
            for (Edge edge : selectedEdges) {
                edges.addAll(synchronizer().getMirrorEdge(edge).segments());
            }
            return edges;
        }

        @Override
        protected Collection<Node> nodes(Edge edge) {
            if (selectedNodes == null) {
                return super.nodes(edge);
            }

            Collection<Node> nodes = new HashSet<>(super.nodes(edge));
            nodes.retainAll(selectedNodes);
            return nodes;
        }
    }

    /**
     * Force that attracts nodes to a given point.
     */
    public static class NodeAttractionToPoint2D extends ModularForce {

        /**
         * The points of attraction for each node.
         */
        protected NodeAttribute<Coordinates> attractionPoints;
        /**
         * The initial force exponent.
         */
        public double initialExponent = 2;
        /**
         * The final force exponent.
         */
        public double finalExponent = 1;
        /**
         * Indicates whether to apply the force to nodes that have default
         * attraction point.
         */
        protected boolean affectDefault = true;

        /**
         * Constructs a force that attracts nodes to a given point in space.
         *
         * @param attractionPoints the points of attraction.
         * @param affectDefault apply to nodes with default attraction point.
         */
        public NodeAttractionToPoint2D(NodeAttribute<Coordinates> attractionPoints, boolean affectDefault) {
            this.attractionPoints = attractionPoints;
            this.affectDefault = affectDefault;
        }

        /**
         * Selects the nodes involved in the computation.
         *
         * @return the collection of nodes.
         */
        protected Collection<Node> nodes() {
            if (affectDefault) {
                return mirrorGraph().nodes();
            } else {
                Set<Node> nodes = new HashSet<>(mirrorGraph().nodes());
                nodes.retainAll(attractionPoints.nonDefaultElements());
                return nodes;
            }
        }

        /**
         * Computes the exponent for the given force.
         *
         * @return the exponent that corresponds to that temperature.
         */
        protected double computeExponent() {
            return finalExponent + (initialExponent - finalExponent) * temperature();
        }

        /**
         * Computes the forces for the given node and temperature.
         *
         * @param node the node.
         * @return the force to apply to the given node.
         */
        protected Coordinates computeForces(Node node) {
            Coordinates n = mirrorPositions().get(node);
            Coordinates a = attractionPoints.get(node);
            Coordinates an = a.restrMinus(n, 2);
            return (an).timesIP(Math.pow(Geom.e2D.magnitude(an) / 10, computeExponent()));    // TODO: change that 10
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            for (Node node : nodes()) {
                Coordinates compForce = computeForces(node);
                forces.set(node, compForce.plusIP(forces.get(node)));
            }
            return forces;
        }
    }

    /**
     * Force that smoothes a curve obtaining a more regular shape.
     */
    public static class CurveSmoothing extends ModularForce {

        protected List<List<Edge>> curves;

        /**
         * Constructs a curve smoothing force.
         *
         * @param curves the list of curves.
         */
        public CurveSmoothing(List<List<Edge>> curves) {
            this.curves = curves;
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            for (List<Edge> curve : curves) {
                computeCurveForces(curve, forces);
            }
            return forces;
        }

        /**
         * Computes the smoothing forces for a curve.
         *
         * @param curve the curve.
         * @param forces the computed forces.
         * @param temperature the current temperature.
         */
        private void computeCurveForces(List<Edge> curve, NodeAttribute<Coordinates> forces) {
            List<Edge> segmentList = getOrderedSegmentList(curve);
            Edge previousSegment = segmentList.get(segmentList.size() - 1);
            for (Edge currentSegment : segmentList) {
                Node nodeToSmooth = getCommonExtremity(currentSegment, previousSegment);
                Node previousNode = previousSegment.otherEnd(nodeToSmooth);
                Node nextNode = currentSegment.otherEnd(nodeToSmooth);
                Coordinates force = computeSmoothing(previousNode, nodeToSmooth, nextNode);
                forces.set(nodeToSmooth, force.plusIP(forces.get(nodeToSmooth)));
                previousSegment = currentSegment;
            }
        }

        /**
         * Gets the list of mirror segments of a curve in boundary chain
         * sequence.
         *
         * @param curve the curve.
         * @return the segments of the curve in a correct order.
         */
        private List<Edge> getOrderedSegmentList(List<Edge> curve) {
            Node startingNode = getCommonExtremity(curve.get(0), curve.get(curve.size() - 1));
            List<Edge> segmentList = new ArrayList<>();
            for (Edge originalEdge : curve) {
                MirrorEdge mirrorEdge = synchronizer().getMirrorEdge(originalEdge);
                if (mirrorEdge.source() == startingNode) {
                    segmentList.addAll(mirrorEdge.segments());
                    startingNode = mirrorEdge.target();
                } else {
                    List<Edge> reversedList = new ArrayList<>(mirrorEdge.segments());
                    Collections.reverse(reversedList);
                    segmentList.addAll(reversedList);
                    startingNode = mirrorEdge.source();
                }
            }
            return segmentList;
        }

        /**
         * Gets the common ending point for two edges. Throws an exception if
         * the edges do not share a node.
         *
         * @param a the first edge.
         * @param b the second edge.
         * @return the node in common.
         */
        private Node getCommonExtremity(Edge a, Edge b) {
            if (a.source() == b.source() || a.source() == b.target()) {
                return a.source();
            } else if (a.target() == b.source() || a.target() == b.target()) {
                return a.target();
            } else {
                throw new IllegalArgumentException("The edges have no extremities in common.");
            }
        }

        /**
         * Computes the smoothing force for a given point.
         *
         * @param previousNode the previous node in the boundary chain.
         * @param nodeToSmooth the node to smooth.
         * @param nextNode the next node in the boundary chain.
         * @return the force on the node.
         */
        private Coordinates computeSmoothing(Node previousNode, Node nodeToSmooth, Node nextNode) {
            Coordinates a = mirrorPositions().get(previousNode);
            Coordinates x = mirrorPositions().get(nodeToSmooth);
            Coordinates b = mirrorPositions().get(nextNode);
            Coordinates abMidpoint = Geom.eXD.midPoint(a, b);
            return (abMidpoint.minusIP(x)).timesIP(2.0 / 3.0);
        }
    }

    /**
     * Force that gets activated only when the temperature is lower than a given
     * threshold.
     */
    public static class LowTemperatureForce extends ModularForce {

        /**
         * The maximum magnitude for the random forces.
         */
        protected ModularForce force;

        /**
         * The temperature at which the force activates.
         */
        protected double activationTemperature;

        /**
         * Constructs a low temperature force.
         *
         * @param force the original force.
         * @param activationTemperature the temperature at which the force
         * activates.
         */
        public LowTemperatureForce(ModularForce force, double activationTemperature) {
            this.force = force;
            this.activationTemperature = activationTemperature;
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            if (temperature() > activationTemperature) {
                return new NodeAttribute<>(new Coordinates(0, 0));
            } else {
                NodeAttribute<Coordinates> forces = force.computeForces();
                double temperatureRatio = (activationTemperature - temperature()) / activationTemperature;
                for (Node node : forces.nonDefaultElements()) {
                    forces.set(node, forces.get(node).times(temperatureRatio));
                }
                return forces;
            }
        }

        @Override
        protected void attachTo(ModularFdl modularFdl) {
            super.attachTo(modularFdl);
            force.attachTo(modularFdl);
        }
    }

    /**
     * Force that attracts nodes to a given point.
     */
    public static class RandomForce2D extends ModularForce {

        /**
         * The maximum magnitude for the random forces.
         */
        protected double maxMagnitude;

        /**
         * Construct a random force generator.
         *
         * @param maxMagnitude the maximum magnitude for the random forces.
         */
        public RandomForce2D(double maxMagnitude) {
            this.maxMagnitude = maxMagnitude;
        }

        /**
         * Selects the nodes involved in the computation.
         *
         * @return the collection of nodes.
         */
        protected Collection<Node> nodes() {
            return mirrorGraph().nodes();
        }

        @Override
        protected NodeAttribute<Coordinates> computeForces() {
            NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
            for (Node node : nodes()) {
                double magnitude = GeomNumeric.randomDouble(maxMagnitude);
                double angle = GeomNumeric.randomDouble(2 * Math.PI);
                Coordinates compForce = Geom.e2D.unitVector(angle).timesIP(magnitude);
                forces.set(node, compForce.plusIP(forces.get(node)));
            }
            return forces;
        }
    }
}
