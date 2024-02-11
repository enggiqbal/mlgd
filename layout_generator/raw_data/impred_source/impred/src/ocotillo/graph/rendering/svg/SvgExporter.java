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
package ocotillo.graph.rendering.svg;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
import ocotillo.graph.rendering.GraphRenderer2D;
import ocotillo.graph.rendering.svg.SvgElement.SvgEllipse;
import ocotillo.graph.rendering.svg.SvgElement.SvgPolygon;
import ocotillo.graph.rendering.svg.SvgElement.SvgPolyline;
import ocotillo.graph.rendering.svg.SvgElement.SvgRectangle;
import ocotillo.graph.rendering.svg.SvgElement.SvgText;

/**
 * Export graphs as SVG images. Currently, only a 2D rendering of the graph is
 * supported.
 */
public class SvgExporter {

    private final Graph graph;
    private final Box graphBox;

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

    /**
     * Saves an SVG file with the graph layout.
     *
     * @param graph the graph.
     * @param destinationFile the destination file.
     */
    public static void saveSvg(Graph graph, File destinationFile) {
        try (PrintWriter writer = new PrintWriter(destinationFile)) {
            writer.write(makeSvg(graph));
        } catch (FileNotFoundException ex) {
            throw new IllegalStateException("Impossible to write on the destination file " + destinationFile.getName());
        }
    }

    /**
     * Computes the content of the SVG file.
     *
     * @param graph the graph.
     * @return the SVG content.
     */
    public static String makeSvg(Graph graph) {
        SvgExporter exporter = new SvgExporter(graph);
        return exporter.buildSvg();
    }

    /**
     * Constructs an SVG exporter.
     *
     * @param graph the graph.
     */
    private SvgExporter(Graph graph) {
        this.graph = graph;
        this.graphBox = new GraphRenderer2D(graph).computeBox();

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
    }

    /**
     * Builds the SVG document.
     *
     * @return the document text.
     */
    private String buildSvg() {
        SvgDocument svgDocument = new SvgDocument(graphBox.width(), graphBox.height());
        buildGraphics(svgDocument, graph);
        buildEdges(svgDocument);
        buildNodes(svgDocument);
        return svgDocument.close();
    }

    /**
     * Inserts the graph nodes.
     *
     * @param svgDocument the SVG document.
     */
    private void buildNodes(SvgDocument svgDocument) {
        for (Node node : new ArrayList<>(graph.nodes())) {
            buildNodeGlyph(svgDocument, node);
            buildNodeLabel(svgDocument, node);
        }
    }

    /**
     * Inserts the glyph of the nodes.
     *
     * @param svgDocument the SVG document.
     * @param node the node for which a glyph must be added.
     */
    private void buildNodeGlyph(SvgDocument svgDocument, Node node) {
        Color color = nodeColors.get(node);
        if (color.getAlpha() == 0) {
            return;
        }

        Coordinates size = nodeSizes.get(node);
        StdAttribute.NodeShape shape = nodeShapes.get(node);
        Coordinates position = graphToCanvasPosition(nodePositions.get(node));
        switch (shape) {
            case spheroid:
                svgDocument.addElement(new SvgEllipse(node.id(), position, size, color));
                break;
            case cuboid:
                svgDocument.addElement(new SvgRectangle(node.id(), position, size, color));
                break;
            default:
                throw new UnsupportedOperationException("The shape " + shape.name() + " is not supported");
        }
    }

    /**
     * Draws the label of a node.
     *
     * @param svgDocument the SVG document.
     * @param node the node.
     */
    private void buildNodeLabel(SvgDocument svgDocument, Node node) {
        String label = nodeLabels.get(node);
        Color color = nodeLabelColors.get(node);
        if (label.isEmpty() || color.getAlpha() == 0) {
            return;
        }

        double labelFontSize = nodeLabelScaling.get(node);
        Color labelColor = nodeLabelColors.get(node);
        Coordinates position = graphToCanvasPosition(nodePositions.get(node).plus(nodeLabelOffset.get(node)));
        svgDocument.addElement(new SvgText(node.id() + "_label", label, position, labelFontSize, labelColor));
    }

    /**
     * Inserts the graph edges.
     *
     * @param svgDocument the SVG document.
     */
    private void buildEdges(SvgDocument svgDocument) {
        for (Edge edge : new ArrayList<>(graph.edges())) {
            Color color = edgeColors.get(edge);
            if (color.getAlpha() < 10) {
                continue;
            }

            double width = edgeWidths.get(edge);
            StdAttribute.EdgeShape shape = edgeShapes.get(edge);
            StdAttribute.ControlPoints controlPoints = edgePoints.get(edge);
            switch (shape) {
                case polyline:
                    List<Coordinates> points = new ArrayList<>();
                    points.add(graphToCanvasPosition(nodePositions.get(edge.source())));
                    for (Coordinates bend : controlPoints) {
                        points.add(graphToCanvasPosition(bend));
                    }
                    points.add(graphToCanvasPosition(nodePositions.get(edge.target())));
                    svgDocument.addElement(new SvgPolyline(edge.id(), points, width, color));
                    break;
                default:
                    throw new UnsupportedOperationException("The shape " + shape.name() + " is not supported");
            }
        }
    }

    /**
     * Inserts the graphics into the SVG document.
     *
     * @param svgDocument the SVG document.
     * @param graph the graph.
     */
    private void buildGraphics(SvgDocument svgDocument, Graph graph) {
        if (graph.hasLocalGraphAttribute(StdAttribute.graphics)) {
            String graphicsString = graph.<String>graphAttribute(StdAttribute.graphics).get();
            List<SvgElement> svgElements = SvgElement.parseSvg(graphicsString);
            for (SvgElement svgElement : svgElements) {
                switch (svgElement.type) {
                    case "rect":
                        SvgRectangle rectangle = (SvgRectangle) svgElement;
                        svgDocument.addElement(new SvgRectangle(rectangle.id, graphToCanvasPosition(rectangle.center), rectangle.size, rectangle.fillColor));
                        break;
                    case "ellipse":
                        SvgEllipse ellipse = (SvgEllipse) svgElement;
                        svgDocument.addElement(new SvgEllipse(ellipse.id, graphToCanvasPosition(ellipse.center), ellipse.size, ellipse.fillColor));
                        break;
                    case "text":
                        SvgText text = (SvgText) svgElement;
                        svgDocument.addElement(new SvgText(text.id, text.text, graphToCanvasPosition(text.center), text.dimension, text.fillColor));
                        break;
                    case "polyline":
                        SvgPolyline polyline = (SvgPolyline) svgElement;
                        List<Coordinates> transformedPolyline = new ArrayList<>();
                        for (Coordinates point : polyline.points) {
                            transformedPolyline.add(graphToCanvasPosition(point));
                        }
                        svgDocument.addElement(new SvgPolyline(polyline.id, transformedPolyline, polyline.width, polyline.strokeColor));
                        break;
                    case "polygon":
                        SvgPolygon polygon = (SvgPolygon) svgElement;
                        List<Coordinates> transformedPolygon = new ArrayList<>();
                        for (Coordinates point : polygon.points) {
                            transformedPolygon.add(graphToCanvasPosition(point));
                        }
                        svgDocument.addElement(new SvgPolygon(polygon.id, transformedPolygon, polygon.fillColor, polygon.strokeWidth, polygon.strokeColor));
                        break;
                    default:
                        throw new UnsupportedOperationException("The svg element " + svgElement.type + " is not supported.");
                }
            }
        }

        for (Graph subGraph : graph.subGraphs()) {
            buildGraphics(svgDocument, subGraph);
        }
    }

    /**
     * Converts the position of a point in the graph to its positions of the
     * canvas.
     *
     * @param position the position of the point in the graph space.
     * @return the position of the point in the canvas space.
     */
    private Coordinates graphToCanvasPosition(Coordinates position) {
        return new Coordinates(position.x() - graphBox.left(), graphBox.top() - position.y());
    }
}
