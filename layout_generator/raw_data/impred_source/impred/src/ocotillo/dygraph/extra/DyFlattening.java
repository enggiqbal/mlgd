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
package ocotillo.dygraph.extra;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.GeomNumeric;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

/**
 * Class that flattens a dynamic graph into a static graph.
 */
public class DyFlattening {

    /**
     * Flattens a graph into a static graph.
     *
     * @param dyGraph the dynamic graph.
     * @param atTime the central time of the flattened snapshot.
     * @param sigma the standard deviation used to flatten events at a distance
     * form the cental time.
     * @return the flattened graph.
     */
    public static Graph flatten(DyGraph dyGraph, double atTime, double sigma) {

        Map<Node, Interval> nodeIntervals = new HashMap<>();
        for (Node node : dyGraph.nodes()) {
            nodeIntervals.put(node, Interval.global);
        }

        Map<Edge, Interval> edgeIntervals = new HashMap<>();
        for (Edge edge : dyGraph.edges()) {
            edgeIntervals.put(edge, Interval.global);
        }

        return flatten(dyGraph, nodeIntervals, edgeIntervals, atTime, sigma);
    }

    /**
     * Flattens a graph into a static graph.
     *
     * @param dyGraph the dynamic graph.
     * @param nodeIntervals the nodes to consider and their interval.
     * @param edgeIntervals the edges to consider and their interval.
     * @param atTime the central time of the flattened snapshot.
     * @param sigma the standard deviation used to flatten events at a distance
     * form the cental time.
     * @return the flattened graph.
     */
    public static Graph flatten(DyGraph dyGraph, Map<Node, Interval> nodeIntervals,
            Map<Edge, Interval> edgeIntervals, double atTime, double sigma) {

        DyNodeAttribute<Coordinates> dyPositions = dyGraph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Coordinates> dyNodeSizes = dyGraph.nodeAttribute(StdAttribute.nodeSize);
        DyNodeAttribute<String> dyLabels = dyGraph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Color> dyNodeColors = dyGraph.nodeAttribute(StdAttribute.color);
        DyEdgeAttribute<Color> dyEdgeColors = dyGraph.edgeAttribute(StdAttribute.color);
        DyNodeAttribute<Boolean> dyNodePresence = dyGraph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> dyEdgePresence = dyGraph.edgeAttribute(StdAttribute.dyPresence);

        Graph graph = new Graph();
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> nodeSizes = graph.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<String> labels = graph.nodeAttribute(StdAttribute.label);
        NodeAttribute<Color> nodeColors = graph.nodeAttribute(StdAttribute.color);
        EdgeAttribute<Color> edgeColors = graph.edgeAttribute(StdAttribute.color);

        for (Node node : nodeIntervals.keySet()) {
            graph.add(node);
            positions.set(node, dyPositions.get(node).valueAt(atTime));
            nodeSizes.set(node, dyNodeSizes.get(node).valueAt(atTime));
            labels.set(node, dyLabels.get(node).valueAt(atTime));
            Color solidColor = dyNodeColors.get(node).valueAt(atTime);
            int alpha = computeTransparency(dyNodePresence.get(node), atTime, sigma);
            Color transparentColor = new Color(solidColor.getRed(),
                    solidColor.getGreen(), solidColor.getBlue(), alpha);
            nodeColors.set(node, transparentColor);
        }
        for (Edge edge : edgeIntervals.keySet()) {
            if (graph.has(edge.source()) && graph.has(edge.target())) {
                graph.add(edge);
                Color solidColor = dyEdgeColors.get(edge).valueAt(atTime);
                int alpha = computeTransparency(dyEdgePresence.get(edge), atTime, sigma);
                Color transparentColor = new Color(solidColor.getRed(),
                        solidColor.getGreen(), solidColor.getBlue(), alpha);
                edgeColors.set(edge, transparentColor);
            }
        }
        return graph;
    }

    /**
     * Computes the transparency value for a graph element.
     *
     * @param presence the element presence.
     * @param atTime the central time of the flattened snapshot.
     * @param sigma the standard deviation used to flatten events at a distance
     * form the cental time.
     * @return
     */
    private static int computeTransparency(Evolution<Boolean> presence, double atTime, double sigma) {
        double value = 0;
        List<Interval> intervals = EvolutionAnalyser.getIntervalsWithValue(presence, true);
        for (Interval interval : intervals) {
            value += GeomNumeric.cdf(interval.rightBound(), atTime, sigma)
                    - GeomNumeric.cdf(interval.leftBound(), atTime, sigma);
        }
        return Math.max(0, Math.min(255, (int) Math.round(value * 255)));
    }
}
