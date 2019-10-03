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

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.or.repulsion.RepulsionOverlapRemover;

/**
 * A class implementing the placement strategy for clusters in the
 * defragmentation process.
 */
public abstract class ClusterPlacer {

    /**
     * Computes a placement for the clusters of a fragmented graph. The returned
     * graph must have one node for each cluster, so that: 1) the node has id
     * equal to the cluster label, 2) the node position reflects the desired
     * position of the cluster, 3) the glyph size covers an area somewhat
     * proportional to the number and size of cluster elements, 4) the nodes do
     * not overlap.
     *
     * @param originalGraph the original graph.
     * @return the graph encoding the cluster placement.
     */
    public abstract Graph computePlacing(Graph originalGraph);

    /**
     * Computes the placement of the clusters according to the original layout
     * of the graph. Each cluster is placed roughly in the barycentre of the
     * cluster nodes.
     */
    public static class OriginalLayoutClusterPlacer extends ClusterPlacer {

        private final double desiredNodeDistance;

        /**
         * Constructs a original layout cluster placer.
         *
         * @param desiredNodeDistance the desired distance between nodes.
         */
        public OriginalLayoutClusterPlacer(double desiredNodeDistance) {
            this.desiredNodeDistance = desiredNodeDistance;
        }

        @Override
        public Graph computePlacing(Graph originalGraph) {
            NodeAttribute<Coordinates> positions = originalGraph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);

            Graph clusterGraph = new Graph();
            NodeAttribute<Coordinates> clusterPositions = clusterGraph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
            NodeAttribute<Coordinates> clusterSizes = clusterGraph.<Coordinates>nodeAttribute(StdAttribute.nodeSize);
            NodeAttribute<String> clusterLabels = clusterGraph.<String>nodeAttribute(StdAttribute.label);

            for (Graph cluster : originalGraph.subGraphs()) {
                String clusterLabel = cluster.<String>graphAttribute(StdAttribute.label).get();
                Node clusterNode = clusterGraph.newNode(clusterLabel);
                Coordinates clusterBarycenter = new Coordinates(0, 0);
                for (Node node : cluster.nodes()) {
                    clusterBarycenter.restrPlusIP(positions.get(node), 2);
                }
                clusterPositions.set(clusterNode, clusterBarycenter.restrDivideIP(cluster.nodeCount(), 2));
                double clusterNodeSize = Math.sqrt(cluster.nodeCount() * desiredNodeDistance);
                clusterSizes.set(clusterNode, new Coordinates(clusterNodeSize, clusterNodeSize));
                clusterLabels.set(clusterNode, clusterLabel);
            }
            RepulsionOverlapRemover.run(clusterGraph, 1.0);
            return clusterGraph;
        }

    }

}
