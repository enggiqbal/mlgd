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

import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.Layout2D;

/**
 * A class implementing the placement strategy for cluster nodes in the
 * defragmentation process.
 */
public abstract class NodePlacer {

    /**
     * Computes a placement for the cluster nodes of a fragmented graph.
     *
     * @param originalGraph the original graph.
     * @param clusterPlacement the computer cluster placement.
     * @return the initial positions of the cluster nodes.
     */
    public abstract NodeAttribute<Coordinates> computePlacing(Graph originalGraph, Graph clusterPlacement);

    /**
     * Computes the placement of the cluster nodes according to the original
     * layout of the graph. Each node is placed in the area reserved to the
     * cluster by scaling and repositioning the subgraph that correspond to each
     * cluster.
     */
    public static class OriginalLayoutNodePlacer extends NodePlacer {

        @Override
        public NodeAttribute<Coordinates> computePlacing(Graph originalGraph, Graph clusterPlacement) {
            NodeAttribute<Coordinates> nodePlacing = new NodeAttribute<>(new Coordinates(0, 0));

            NodeAttribute<Coordinates> originalPositions = originalGraph.nodeAttribute(StdAttribute.nodePosition);
            NodeAttribute<Coordinates> clusterPositions = clusterPlacement.nodeAttribute(StdAttribute.nodePosition);
            NodeAttribute<Coordinates> clusterSizes = clusterPlacement.nodeAttribute(StdAttribute.nodeSize);

            for (Graph cluster : originalGraph.subGraphs()) {
                String clusterLabel = cluster.<String>graphAttribute(StdAttribute.label).get();
                Box initialClusterBox = Layout2D.graphBox(cluster, originalPositions, null, null, null);
                Coordinates initialClusterCenter = initialClusterBox.center();

                Node clusterPlaceholder = clusterPlacement.getNode(clusterLabel);
                Coordinates finalClusterCenter = clusterPositions.get(clusterPlaceholder);
                Coordinates finalClusterSize = clusterSizes.get(clusterPlaceholder).times(0.8);
                double xScaling = initialClusterBox.width() > 0 ? finalClusterSize.x() / initialClusterBox.width() : 1;
                double yScaling = initialClusterBox.height() > 0 ? finalClusterSize.y() / initialClusterBox.height() : 1;

                for (Node clusterNode : cluster.nodes()) {
                    Coordinates originalPosition = originalPositions.get(clusterNode);
                    Coordinates offset = originalPosition.restrMinus(initialClusterCenter, 2);
                    offset.setX(offset.x() * xScaling);
                    offset.setY(offset.y() * yScaling);
                    nodePlacing.set(clusterNode, offset.restrPlusIP(finalClusterCenter, 2));
                }
            }
            return nodePlacing;
        }
    }
}
