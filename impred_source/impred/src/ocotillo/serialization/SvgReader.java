package ocotillo.serialization;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Polygon;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.layout.Layout2D;
import ocotillo.graph.rendering.RenderingTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SvgReader {  // TODO: consolidate

    public Graph parseSetSimpleCurves(File svgFile) {
        SvgElements elements = read(svgFile);

        Graph graph = new Graph();
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> sizes = graph.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<Color> colors = graph.nodeAttribute(StdAttribute.color);
        EdgeAttribute<ControlPoints> bends = graph.edgeAttribute(StdAttribute.edgePoints);

        for (Box box : elements.squares) {
            Node node = graph.newNode();
            positions.set(node, box.center());
            sizes.set(node, box.size());
            colors.set(node, Color.RED);
        }

        for (List<Coordinates> path : elements.paths) {
            Node source = graph.newNode();
            Node target;
            if (path.get(0).equals(path.get(path.size() - 1))) {
                target = source;
            } else {
                target = graph.newNode();
            }
            positions.set(source, path.remove(0));
            positions.set(target, path.remove(path.size() - 1));
            colors.set(source, Color.BLUE);
            colors.set(target, Color.BLUE);
            Edge edge = graph.newEdge(source, target);
            bends.set(edge, new ControlPoints(path));
        }
        return graph;
    }

    public Graph parseGraph(File svgFile) {
        SvgElements elements = read(svgFile);

        Graph graph = new Graph();
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> sizes = graph.nodeAttribute(StdAttribute.nodeSize);
        NodeAttribute<String> labels = graph.nodeAttribute(StdAttribute.label);
        NodeAttribute<Color> colors = graph.nodeAttribute(StdAttribute.color);
        EdgeAttribute<ControlPoints> bends = graph.edgeAttribute(StdAttribute.edgePoints);

        Map<Character, Graph> subgraphMap = new HashMap<>();
        for (Label label : elements.labels) {
            if (!label.text.startsWith("~") && !label.text.startsWith("#")) {
                for (char clusterId : label.text.toCharArray()) {
                    if (!subgraphMap.containsKey(clusterId)) {
                        Graph subgraph = graph.newSubGraph();
                        subgraphMap.put(clusterId, subgraph);
                        for (Label label2 : elements.labels) {
                            if (label2.text.equals("~" + clusterId)) {
                                subgraph.newLocalGraphAttribute(StdAttribute.color, label2.color);
                                break;
                            }
                        }
                    }
                }
            }
        }

        for (Box box : elements.squares) {
            Node node = graph.newNode();
            positions.set(node, box.center());
            sizes.set(node, box.size());
            colors.set(node, Color.RED);
            Box nodeBox = Layout2D.nodeBox(node, graph);
            for (Label label : elements.labels) {
                if (label.text.startsWith("#") && nodeBox.contains(label.position)) {
                    labels.set(node, label.text.substring(1));
                }
            }
        }

        Map<Box, Node> boxMap = new HashMap<>();
        for (Box box : elements.junctions) {
            Node node = graph.newNode();
            positions.set(node, box.center());
            colors.set(node, Color.BLUE);
            boxMap.put(box, node);
        }

        for (List<Coordinates> path : elements.paths) {
            Node source = findNode(path.get(0), boxMap);
            if (source == null) {
                source = graph.newNode();
            }
            Node target;
            if (path.get(0).equals(path.get(path.size() - 1))) {
                target = source;
            } else {
                target = findNode(path.get(path.size() - 1), boxMap);
            }

            Edge edge = graph.newEdge(source, target);

            if (!elements.labels.isEmpty()) {
                String label = findLabel(path, elements.labels);
                for (char clusterId : label.toCharArray()) {
                    subgraphMap.get(clusterId).forcedAdd(edge);
                }
            }

            positions.set(source, path.remove(0));
            positions.set(target, path.remove(path.size() - 1));
            colors.set(source, Color.BLUE);
            colors.set(target, Color.BLUE);
            bends.set(edge, new ControlPoints(path));

        }

        for (Character character : subgraphMap.keySet()) {
            System.out.println("Extracting polygon " + character);
            Graph subgraph = subgraphMap.get(character);
            Polygon polygon = extractPolygon(subgraph);
            for (Node node : graph.nodes()) {
                if (Geom.e2D.isPointInPolygon(positions.get(node), polygon) && colors.get(node).equals(new Color(255, 0, 0))) {
                    subgraph.forcedAdd(node);
                }
            }
        }

        return graph;
    }

    private Node findNode(Coordinates position, Map<Box, Node> boxMap) {
        for (Box box : boxMap.keySet()) {
            if (box.contains(position)) {
                return boxMap.get(box);
            }
        }
        return null;
    }

    private String findLabel(List<Coordinates> path, List<Label> labels) {
        Label closestLabel = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Label label : labels) {
            for (int i = 0; i < path.size() - 1; ++i) {
                double labelDistance = Geom.e2D.pointSegmentRelation(label.position, path.get(i), path.get(i + 1)).distance();
                if (labelDistance < closestDistance) {
                    closestDistance = labelDistance;
                    closestLabel = label;
                }
            }
        }
        return closestLabel.text;
    }

    private static Polygon extractPolygon(Graph graph) {
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> bends = graph.edgeAttribute(StdAttribute.edgePoints);
        Polygon polygon = new Polygon();
        Edge currentEdge = graph.edges().iterator().next();
        Node startingNode = currentEdge.source();
        Node currentNode = startingNode;
        polygon.add(positions.get(currentNode));
        polygon.addAll(bends.get(currentEdge));
        while (currentEdge.otherEnd(currentNode) != startingNode) {
            currentNode = currentEdge.otherEnd(currentNode);
            List<Edge> inOutEdges = new ArrayList<>(graph.inOutEdges(currentNode));
            currentEdge = inOutEdges.get(0) != currentEdge ? inOutEdges.get(0) : inOutEdges.get(1);
            polygon.add(positions.get(currentNode));
            if (currentNode == currentEdge.source()) {
                polygon.addAll(bends.get(currentEdge));
            } else {
                List<Coordinates> edgeBends = new ArrayList<>(bends.get(currentEdge));
                Collections.reverse(edgeBends);
                polygon.addAll(edgeBends);
            }
        }
        return polygon;
    }

    private SvgElements read(File svgFile) {

        SvgElements elements = new SvgElements();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(svgFile);
            doc.getDocumentElement().normalize();

            parseLabels(elements, doc.getElementsByTagName("text"));
            parsePaths(elements, doc.getElementsByTagName("path"));
            parseSquares(elements, doc.getElementsByTagName("rect"));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Problems reading the svg file.");
        }

        return elements;
    }

    private void parseLabels(SvgElements svgElements, NodeList labels) {
        for (Element element : getElementList(labels)) {
            Label label = new Label();
            label.text = element.getTextContent();
            double x = Double.parseDouble(element.getAttribute("x"));
            double y = -Double.parseDouble(element.getAttribute("y"));
            label.position = new Coordinates(x, y);

            String style = element.getAttribute("style");
            String[] styleTokens = style.split(";");
            int r = 0;
            int g = 0;
            int b = 0;
            int a = 0;
            for (String token : styleTokens) {
                if (token.startsWith("fill:")) {
                    Color color = RenderingTools.colorHexReader(token.split(":")[1]);
                    r = color.getRed();
                    g = color.getGreen();
                    b = color.getBlue();
                }
                if (token.startsWith("fill-opacity:")) {
                    a = (int) (Double.parseDouble(token.split(":")[1]) * 255);
                }
            }
            label.color = new Color(r, g, b, a);

            svgElements.labels.add(label);
        }
    }

    private void parsePaths(SvgElements svgElements, NodeList paths) {
        for (Element element : getElementList(paths)) {
            List<Coordinates> coordinates = new ArrayList<>();

            boolean relativeMode = false;
            for (String coord : element.getAttribute("d").split(" ")) {
                if (coord.contains(",")) {
                    String[] xy = coord.split(",");
                    Coordinates currentCoord = new Coordinates(Double.parseDouble(xy[0]), -Double.parseDouble(xy[1]));
                    if (coordinates.isEmpty() || !relativeMode) {
                        coordinates.add(currentCoord);
                    } else {
                        coordinates.add(coordinates.get(coordinates.size() - 1).plus(currentCoord));
                    }
                } else if (coord.equals("z")) {
                    coordinates.add(coordinates.get(0));
                } else if (coord.equals("m") || coord.equals("l")) {
                    relativeMode = true;
                } else if (coord.equals("M") || coord.equals("L")) {
                    relativeMode = false;
                } else {
                    throw new IllegalArgumentException("Path token \"" + coord + "\" not recognized");
                }
            }
            svgElements.paths.add(coordinates);
        }
    }

    private void parseSquares(SvgElements svgElements, NodeList squares) {
        for (Element element : getElementList(squares)) {
            double width = Double.parseDouble(element.getAttribute("width"));
            double height = Double.parseDouble(element.getAttribute("height"));
            double x = Double.parseDouble(element.getAttribute("x"));
            double y = Double.parseDouble(element.getAttribute("y"));
            Box box = new Box.Box2D(x, x + width, -y - height, -y);
            if (element.getAttribute("style").contains("fill:#ff0000")) {
                svgElements.squares.add(box);
            } else if (element.getAttribute("style").contains("fill:#00ff00")) {
                svgElements.junctions.add(box);
            }
        }
    }

    private List<Element> getElementList(NodeList elementList) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < elementList.getLength(); i++) {
            org.w3c.dom.Node node = elementList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                elements.add((Element) node);
            }
        }
        return elements;
    }

    public static class SvgElements {

        public List<List<Coordinates>> paths = new ArrayList<>();
        public List<Box> squares = new ArrayList<>();
        public List<Box> junctions = new ArrayList<>();
        public List<Label> labels = new ArrayList<>();
    }

    public class Label {

        public String text;
        public Coordinates position;
        public Color color;
    }
}
