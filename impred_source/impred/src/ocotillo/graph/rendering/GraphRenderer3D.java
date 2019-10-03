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
package ocotillo.graph.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ocotillo.geometry.Box.Box2D;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;

/**
 * Renderer for 3D graphs into graphics objects. The rendering is a simplified
 * representation of the 3D graph as view from a certain angle.
 */
public class GraphRenderer3D extends GraphRenderer {

    private final Graph graph;
    private final NodeAttribute<Coordinates> originalPosition;
    private final NodeAttribute<Coordinates> nodePositions;
    private final NodeAttribute<Coordinates> nodeSizes;
    private final NodeAttribute<StdAttribute.NodeShape> nodeShapes;
    private final NodeAttribute<Color> nodeColors;
    private final NodeAttribute<String> nodeLabels;
    private final NodeAttribute<Color> nodeLabelColors;
    private final NodeAttribute<Double> nodeLabelScaling;
    private final NodeAttribute<Coordinates> nodeLabelOffset;
    private final EdgeAttribute<Double> edgeWidths;
    private final EdgeAttribute<StdAttribute.EdgeShape> edgeShapes;
    private final EdgeAttribute<StdAttribute.ControlPoints> edgePoints;
    private final EdgeAttribute<Color> edgeColors;
    private final HeatMap heatMap;
    private final ViewAngle viewAngle;

    /**
     * Constructs a renderer for a given graph.
     *
     * @param graph the graph.
     */
    public GraphRenderer3D(Graph graph) {
        this(graph, new ViewAngle());
    }

    /**
     * Constructs a renderer for a given graph and a given viewAngle.
     *
     * @param graph the graph.
     * @param viewAngle the viewAngle.
     */
    public GraphRenderer3D(Graph graph, ViewAngle viewAngle) {
        this.graph = graph;

        this.originalPosition = graph.nodeAttribute(StdAttribute.nodePosition);
        this.nodeSizes = graph.nodeAttribute(StdAttribute.nodeSize);
        this.nodeShapes = graph.nodeAttribute(StdAttribute.nodeShape);
        this.nodeColors = graph.nodeAttribute(StdAttribute.color);
        this.nodeLabels = graph.nodeAttribute(StdAttribute.label);
        this.nodeLabelColors = graph.nodeAttribute(StdAttribute.labelColor);
        this.nodeLabelScaling = graph.nodeAttribute(StdAttribute.labelScaling);
        this.nodeLabelOffset = graph.nodeAttribute(StdAttribute.labelOffset);

        this.edgeWidths = graph.edgeAttribute(StdAttribute.edgeWidth);
        this.edgeShapes = graph.edgeAttribute(StdAttribute.edgeShape);
        this.edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);
        this.edgeColors = graph.edgeAttribute(StdAttribute.color);

        this.heatMap = new HeatMap();
        this.viewAngle = viewAngle;

        this.nodePositions = new NodeAttribute<>(new Coordinates(0, 0));
    }

    @Override
    public void draw(Graphics2D graphics) {
        nodePositions.reset();
        for (Node node : graph.nodes()) {
            nodePositions.set(node, viewAngle.transformedPosition(originalPosition.get(node)));
        }

        drawGraphics(graphics, graph);
        drawHeatMap(graphics);
        drawEdges(graphics);
        drawNodes(graphics);
    }

    /**
     * Draws the graph nodes.
     *
     * @param graphics2D the 2D graphics.
     */
    private void drawNodes(Graphics2D graphics2D) {
        List<Node> orderedNodes = new ArrayList<>(graph.nodes());
        Collections.sort(orderedNodes, (Node t, Node t1)
                -> -Double.compare(nodePositions.get(t).z(), nodePositions.get(t1).z()));

        for (Node node : orderedNodes) {
            drawNodeGlyph(graphics2D, node);
            drawNodeLabel(graphics2D, node);
        }
    }

    /**
     * Draws the glyph of a node.
     *
     * @param graphics2D the 2D graphics.
     * @param node the node.
     */
    private void drawNodeGlyph(Graphics2D graphics2D, Node node) {
        Color fillColor = nodeColors.get(node);
        Coordinates size = nodeSizes.get(node);
        Coordinates center = nodePositions.get(node);
        NodeShape shape = nodeShapes.get(node);

        //TODO take in consideration z size and see how to make it more tri-dimensional.
        switch (shape) {
            case spheroid:
                ComponentDrawer.drawEllipse(graphics2D, center, size, fillColor);
                break;
            case cuboid:
                ComponentDrawer.drawRectangle(graphics2D, center, size, fillColor);
                break;
            default:
                throw new UnsupportedOperationException("The shape " + shape.name() + " is not supported");
        }
    }

    /**
     * Draws the label of a node.
     *
     * @param graphics2D the 2D graphics.
     * @param node the node.
     */
    private void drawNodeLabel(Graphics2D graphics2D, Node node) {
        String label = nodeLabels.get(node);
        Color color = nodeLabelColors.get(node);
        double dimension = nodeLabelScaling.get(node);
        Coordinates position = nodePositions.get(node);
        Coordinates labelOffset = nodeLabelOffset.get(node);
        Coordinates transformedLP = position.plus(labelOffset);
        ComponentDrawer.drawText(graphics2D, label, transformedLP, dimension, color);
    }

    /**
     * Draws the graph edges.
     *
     * @param graphics2D the 2D graphics.
     */
    private void drawEdges(Graphics2D graphics2D) {
        for (Edge edge : new ArrayList<>(graph.edges())) {
            Double width = edgeWidths.get(edge);
            EdgeShape shape = edgeShapes.get(edge);
            Color color = edgeColors.get(edge);
            Coordinates startingPoint = nodePositions.get(edge.source());
            Coordinates endingPoint = nodePositions.get(edge.target());
            ControlPoints controlPoints = edgePoints.get(edge);

            ControlPoints transformedCP = new ControlPoints();
            for (Coordinates point : controlPoints) {
                transformedCP.add(viewAngle.transformedPosition(point));
            }
            switch (shape) {
                case polyline:
                    ComponentDrawer.drawPolyline(graphics2D, startingPoint, endingPoint, transformedCP, width, color);
                    break;
                default:
                    throw new UnsupportedOperationException("The shape " + shape.name() + " is not supported");
            }
        }
    }

    /**
     * Draws the heat map.
     *
     * @param graphics2D the 2D graphics.
     */
    private void drawHeatMap(Graphics2D graphics2D) {
        if (!graph.nodeAttribute(StdAttribute.nodeHeat).isSleeping()) {
            throw new UnsupportedOperationException("Not yet coded");
        }
    }

    /**
     * Draws the graphics of the given graph.
     *
     * @param graphics2D the 2D graphics.
     * @param graph the graph.
     */
    private void drawGraphics(Graphics2D graphics2D, Graph graph) {
        if (!graph.graphAttribute(StdAttribute.graphics).isSleeping()) {
            throw new UnsupportedOperationException("Not yet coded");
        }

        for (Graph subGraph : graph.subGraphs()) {
            drawGraphics(graphics2D, subGraph);
        }
    }

    @Override
    public Box2D computeBox() {
        return new Box2D(-100, 100, -100, 100);   // TODO must be changed
    }

}
