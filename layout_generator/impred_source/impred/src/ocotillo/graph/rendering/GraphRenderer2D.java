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
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import ocotillo.geometry.Box;
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
import ocotillo.graph.layout.Layout2D;
import ocotillo.graph.rendering.svg.SvgElement;

/**
 * Renderer for 2D graphs into graphics objects.
 */
public class GraphRenderer2D extends GraphRenderer {

    private final Graph graph;
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

    /**
     * Constructs a rendered for a given graph.
     *
     * @param graph the graph.
     */
    public GraphRenderer2D(Graph graph) {
        this.graph = graph;

        this.nodePositions = graph.nodeAttribute(StdAttribute.nodePosition);
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
    }

    @Override
    public void draw(Graphics2D graphics) {
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
        for (Node node : new ArrayList<>(graph.nodes())) {
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
        Coordinates labelPosition = position.plus(labelOffset);
        ComponentDrawer.drawText(graphics2D, label, labelPosition, dimension, color);
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

            switch (shape) {
                case polyline:
                    ComponentDrawer.drawPolyline(graphics2D, startingPoint, endingPoint, controlPoints, width, color);
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
            Image heatMapImage = heatMap.getImage(graph);
            Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();
            Box imageBox = heatMap.getImageBox();
            int x = (int) (imageBox.left() * scaling);
            int y = (int) (-imageBox.top() * scaling);
            int width = (int) (imageBox.width() * scaling);
            int height = (int) (imageBox.height() * scaling);
            graphicsCopy.drawImage(heatMapImage, x, y, width, height, null);
            graphicsCopy.dispose();
        }
    }

    /**
     * Draws the graphics of the given graph.
     *
     * @param graphics2D the 2D graphics.
     * @param graph the graph.
     */
    private void drawGraphics(Graphics2D graphics2D, Graph graph) {
        if (graph.hasLocalGraphAttribute(StdAttribute.graphics)) {
            String graphicsString = graph.<String>graphAttribute(StdAttribute.graphics).get();
            List<SvgElement> svgElements = SvgElement.parseSvg(graphicsString);
            for (SvgElement svgElement : svgElements) {
                svgElement.drawYourself(graphics2D);
            }
        }

        for (Graph subGraph : graph.subGraphs()) {
            drawGraphics(graphics2D, subGraph);
        }
    }

    @Override
    public Box computeBox() {
        Box graphBox = Layout2D.graphBox(graph);
        Box labelBox = labelBox(graph);
        Box graphicsBox = graphicsBox(graph, null);
        return Box.combine(graphBox, labelBox, graphicsBox).scale(scaling);
    }

    /**
     * Computes the box including the nodes labels.
     *
     * @param graph the graph.
     * @return the node label box.
     */
    private Box labelBox(Graph graph) {
        Box labelBox = null;
        NodeAttribute<Double> nodeLabelFontScaling = graph.nodeAttribute(StdAttribute.labelScaling);
        for (Node node : graph.nodes()) {
            String label = nodeLabels.get(node);
            Coordinates center = nodePositions.get(node).plus(nodeLabelOffset.get(node));
            double fontScaling = nodeLabelFontScaling.get(node);
            if (labelBox == null) {
                labelBox = RenderingTools.textBox(label, center, fontScaling);
            } else {
                labelBox = labelBox.combine(RenderingTools.textBox(label, center, fontScaling));
            }
        }
        return labelBox;
    }

    /**
     * Combine a given box with the graphic box of a given graph.
     *
     * @param graph the graph.
     * @param currentBox the current box.
     * @return the combined box.
     */
    private Box graphicsBox(Graph graph, Box currentBox) {
        String graphics = RenderingTools.Graphics.getLocalGraphics(graph).get();
        List<SvgElement> elements = SvgElement.parseSvg(graphics);
        for (SvgElement element : elements) {
            if (currentBox == null) {
                currentBox = element.box();
            } else {
                currentBox = currentBox.combine(element.box());
            }
        }

        for (Graph subgraph : graph.subGraphs()) {
            currentBox = graphicsBox(subgraph, currentBox);
        }
        return currentBox;
    }

    /**
     * Resizes a node to make it sufficient to contain its label. Uses the
     * default margin of 0.25.
     *
     * @param node the node.
     * @param graph the graph.
     */
    public static void resizeGlyphToFitLabel(Node node, Graph graph) {
        resizeGlyphToFitLabel(node, graph, 0.25);
    }

    /**
     * Resizes a node to make it sufficient to contain its label.
     *
     * @param node the node.
     * @param graph the graph.
     * @param margin the margin to be used.
     */
    public static void resizeGlyphToFitLabel(Node node, Graph graph, double margin) {
        NodeAttribute<Coordinates> nodeSizes = graph.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<String> nodeLabels = graph.nodeAttribute(StdAttribute.label);
        NodeAttribute<Double> nodeLabelFontScaling = graph.nodeAttribute(StdAttribute.labelScaling);
        resizeGlyphToFitLabel(node, nodeSizes, nodeLabels, nodeLabelFontScaling, margin);
    }

    /**
     * Resizes a node to make it sufficient to contain its label. Uses the
     * default margin of 0.25.
     *
     * @param node the node.
     * @param nodeSizes the node sizes.
     * @param nodeLabels the node labels.
     * @param nodeLabelFontScaling the node font scalings.
     */
    public static void resizeGlyphToFitLabel(Node node, NodeAttribute<Coordinates> nodeSizes, NodeAttribute<String> nodeLabels, NodeAttribute<Double> nodeLabelFontScaling) {
        resizeGlyphToFitLabel(node, nodeSizes, nodeLabels, nodeLabelFontScaling, 0.25);
    }

    /**
     * Resizes a node to make it sufficient to contain its label.
     *
     * @param node the node.
     * @param nodeSizes the node sizes.
     * @param nodeLabels the node labels.
     * @param nodeLabelFontScaling the node font scalings.
     * @param margin the margin to be used.
     */
    public static void resizeGlyphToFitLabel(Node node, NodeAttribute<Coordinates> nodeSizes, NodeAttribute<String> nodeLabels, NodeAttribute<Double> nodeLabelFontScaling, double margin) {
        String label = nodeLabels.get(node);
        double fontScaling = nodeLabelFontScaling.get(node);
        Coordinates textSize = RenderingTools.textSize(label, fontScaling);
        nodeSizes.set(node, textSize.plusIP(new Coordinates(2 * margin, 2 * margin)));
    }

    /**
     * Resizes a node label to make it fit in its glyph. Uses the default margin
     * of 0.25.
     *
     * @param node the node.
     * @param graph the graph.
     */
    public static void resizeLabelToFitGlyph(Node node, Graph graph) {
        resizeLabelToFitGlyph(node, graph, 0.25);
    }

    /**
     * Resizes a node label to make it fit in its glyph.
     *
     * @param node the node.
     * @param graph the graph.
     * @param margin the margin to be used.
     */
    public static void resizeLabelToFitGlyph(Node node, Graph graph, double margin) {
        NodeAttribute<Coordinates> nodeSizes = graph.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<String> nodeLabels = graph.nodeAttribute(StdAttribute.label);
        NodeAttribute<Double> nodeLabelFontScaling = graph.nodeAttribute(StdAttribute.labelScaling);
        resizeLabelToFitGlyph(node, nodeSizes, nodeLabels, nodeLabelFontScaling, margin);
    }

    /**
     * Resizes a node label to make it fit in its glyph. Uses the default margin
     * of 0.25.
     *
     * @param node the node.
     * @param nodeSizes the node sizes.
     * @param nodeLabels the node labels.
     * @param nodeLabelFontScaling the node font scalings.
     */
    public static void resizeLabelToFitGlyph(Node node, NodeAttribute<Coordinates> nodeSizes, NodeAttribute<String> nodeLabels, NodeAttribute<Double> nodeLabelFontScaling) {
        resizeLabelToFitGlyph(node, nodeSizes, nodeLabels, nodeLabelFontScaling, 0.25);
    }

    /**
     * Resizes a node label to make it fit in its glyph.
     *
     * @param node the node.
     * @param nodeSizes the node sizes.
     * @param nodeLabels the node labels.
     * @param nodeLabelFontScaling the node font scalings.
     * @param margin the margin to be used.
     */
    public static void resizeLabelToFitGlyph(Node node, NodeAttribute<Coordinates> nodeSizes, NodeAttribute<String> nodeLabels, NodeAttribute<Double> nodeLabelFontScaling, double margin) {
        String label = nodeLabels.get(node);
        Coordinates nodeSize = nodeSizes.get(node);
        double fontScaling = 1.0 / scaling;
        double basicIncrement = 1.1;
        Coordinates labelSize;
        do {
            fontScaling *= basicIncrement;
            labelSize = RenderingTools.textSize(label, fontScaling);
        } while (labelSize.x() < nodeSize.x() - 2 * margin && labelSize.y() < nodeSize.y() - 2 * margin);
        nodeLabelFontScaling.set(node, fontScaling / basicIncrement);
    }
}
