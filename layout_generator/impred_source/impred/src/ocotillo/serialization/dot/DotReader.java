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
package ocotillo.serialization.dot;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Polygon;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.rendering.RenderingTools;
import ocotillo.graph.rendering.svg.SvgElement.SvgPolygon;
import ocotillo.serialization.ParserTools;
import ocotillo.serialization.ParserTools.EscapedString;
import ocotillo.serialization.dot.ConversionSettings.AttributeConvSettings;
import ocotillo.serialization.dot.DotTools.DotAttributes;
import ocotillo.serialization.dot.DotTools.DotLineType;

/**
 * Reads and writes graphs in dot format.
 */
public class DotReader {

    private final ConversionSettings graphAttrSettings;
    private final ConversionSettings nodeAttrSettings;
    private final ConversionSettings edgeAttrSettings;
    private final DefaultDotConverters defaultConverters;
    private final String clusterBy;
    private final ConversionSettings nodeToClusterAttrSettings;
    private final boolean saveGmapPolygons;

    private final DotAttributes graphAttributes = new DotAttributes();
    private final DotAttributes defaultNodeAttributes = new DotAttributes();
    private final DotAttributes defaultEdgeAttributes = new DotAttributes();
    private final Map<String, DotAttributes> nodeAttributes = new HashMap<>();
    private final List<DotAttributes> edgeAttributes = new ArrayList<>();

    /**
     * Builder for dot reader.
     */
    public static class DotReaderBuilder {

        /**
         * The conversion settings for the graph attributes.
         */
        public final ConversionSettings.AllOperations graphAttributes = new ConversionSettings.AllOperations();
        /**
         * The conversion settings for the node attributes.
         */
        public final ConversionSettings.AllOperations nodeAttributes = new ConversionSettings.AllOperations();
        /**
         * The conversion settings for the edge attributes.
         */
        public final ConversionSettings.AllOperations edgeAttributes = new ConversionSettings.AllOperations();
        /**
         * The default converters.
         */
        public final DefaultDotConverters defaultConverters = new DefaultDotConverters();

        private String clusterBy;
        private ConversionSettings.ConvertOnly nodeToClusterAttrSettings;
        private boolean saveHarcodedAttributes = false;
        private boolean saveGmapPolygons = true;

        /**
         * Indicates to cluster by a given attributes and provides a structure
         * to indicate further cluster attributes to be saved.
         *
         * @param attrId the attribute to be used as cluster ID.
         * @return the structure that collects further attributes to be saved.
         */
        public ConversionSettings.ConvertOnly clusterBy(String attrId) {
            assert (attrId != null && !attrId.isEmpty()) : "The attribute id cannot be null or empty";
            clusterBy = attrId;
            nodeToClusterAttrSettings = new ConversionSettings.ConvertOnly();
            return nodeToClusterAttrSettings;
        }

        /**
         * Indicates whether or not to save hard-coded dot attributes.
         * Hard-coded attributes include among others the graph name and the
         * graph type (directed or not).
         *
         * @param activate whether or not to save hard-coded dot attributes.
         */
        public void saveHardcodedAttributes(boolean activate) {
            saveHarcodedAttributes = activate;
        }

        /**
         * Indicates whether or not to save GMap polygons.
         *
         * @param activate whether or not to save GMap polygons.
         */
        public void saveGmapPolygons(boolean activate) {
            saveGmapPolygons = activate;
        }

        /**
         * Constructs a dot reader.
         *
         * @return the dot reader.
         */
        public DotReader build() {
            edgeAttributes.ignore(DotTools.edgeSourceAttr);
            edgeAttributes.ignore(DotTools.edgeTargetAttr);

            if (saveHarcodedAttributes) {
                graphAttributes.convert(DotTools.strictAttr, DotTools.strictAttr, Boolean.class)
                        .convert(DotTools.directedAttr, DotTools.directedAttr, Boolean.class)
                        .convert(DotTools.graphNameAttr, DotTools.graphNameAttr, String.class);
            }

            return new DotReader(graphAttributes, nodeAttributes, edgeAttributes, defaultConverters, clusterBy, nodeToClusterAttrSettings, saveGmapPolygons);
        }
    }

    /**
     * Constructs a DotReader.
     */
    private DotReader(ConversionSettings graphAttrSettings, ConversionSettings nodeAttrSettings, ConversionSettings edgeAttrSettings, DefaultDotConverters defaultConverters, String clusterBy, ConversionSettings nodeToClusterAttrSettings, boolean saveGmapPolygons) {
        this.graphAttrSettings = graphAttrSettings;
        this.nodeAttrSettings = nodeAttrSettings;
        this.edgeAttrSettings = edgeAttrSettings;
        this.defaultConverters = defaultConverters;
        this.clusterBy = clusterBy;
        this.nodeToClusterAttrSettings = nodeToClusterAttrSettings;
        this.saveGmapPolygons = saveGmapPolygons;
    }

    /**
     * Parses a file and generates a graph.
     *
     * @param file the input dot file.
     * @return the generated graph.
     */
    public Graph parseFile(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("The file " + file.getName() + "is not readable.");
        }
        return parseFile(lines);
    }

    /**
     * Parses a collection of dot lines and generates a graph.
     *
     * @param lines the lines in dot format.
     * @return the generated graph.
     */
    public Graph parseFile(List<String> lines) {
        graphAttributes.clear();
        nodeAttributes.clear();
        edgeAttributes.clear();

        List<String> combinedLines = combineLines(lines);
        for (String line : combinedLines) {
            EscapedString escapedLine = escapeLine(line);
            parseLine(escapedLine);
        }

        return generateGraph();
    }

    /**
     * Combines the input lines so that the graph has a single line for global
     * properties, nodes and edges.
     *
     * @param lines the input lines.
     * @return the combined input lines.
     */
    private static List<String> combineLines(List<String> lines) {
        List<String> combinedLines = new ArrayList<>();
        String currentLine = "";
        for (String line : lines) {
            line = line.trim();

            if (!currentLine.isEmpty()) {
                currentLine += " ";
            }
            currentLine += line;

            if (line.endsWith(";") || line.endsWith("{") || line.endsWith("}")) {
                combinedLines.add(currentLine);
                currentLine = "";
            }
        }
        return combinedLines;
    }

    /**
     * Builds an EscapedString using dot conventions. Everything between double
     * quotes is considered as a single entity, and is not parsed according to
     * the dot syntax. These entities might contain a double quotes, but only if
     * escaped with backslash.
     *
     * @param line the input line.
     * @return its escaped string.
     */
    private static EscapedString escapeLine(String line) {
        // Regular expression with negative lookbehind to ignore escaped quotes.
        String notEscapedQuotes = "(?<!\\\\)\"";
        return new EscapedString(line, notEscapedQuotes);
    }

    /**
     * Parses a single dot line, provided as an escaped string.
     *
     * @param eString a dot line where the quoted portions have been substituted
     * by place holders.
     */
    private void parseLine(EscapedString eString) {
        List<String> tokens = splitLine(eString);
        switch (tokens.size()) {
            case 0:
                return;

            case 1:
            case 2:
                switch (DotLineType.detect(tokens.get(0))) {
                    case opening:
                        parseOpeningLine(graphAttributes, eString, tokens);
                        break;

                    case graphGlobal:
                        parseGlobalLine(graphAttributes, eString, tokens);
                        break;

                    case nodeGlobal:
                        parseGlobalLine(defaultNodeAttributes, eString, tokens);
                        break;

                    case edgeGlobal:
                        parseGlobalLine(defaultEdgeAttributes, eString, tokens);
                        break;

                    case node:
                        parseNodeLine(nodeAttributes, eString, tokens);
                        break;

                    case edge:
                        parseEdgeLine(edgeAttributes, eString, tokens);
                        break;
                }
                break;

            default:
                throw new UnsupportedOperationException("The line:\n" + eString.original + "\nhas an unsupported syntax.");
        }
    }

    /**
     * Splits an input line into the heading and the eventual attribute string.
     *
     * @param line the input line.
     * @return the line components.
     */
    private static List<String> splitLine(EscapedString line) {
        String[] tokens = line.withSubstitutions.split("[\\[\\];]+");
        List<String> answer = new ArrayList<>();
        for (String token : tokens) {
            String trimmedToken = token.trim();
            if (!trimmedToken.isEmpty()) {
                answer.add(trimmedToken);
            }
        }
        return answer;
    }

    /**
     * Parses the opening line of a dot file.
     *
     * @param graphAttributes the graph attributes.
     * @param line the line.
     * @param tokens the line components.
     */
    private static void parseOpeningLine(DotAttributes graphAttributes, EscapedString line, List<String> tokens) {
        String[] headerTokens = tokens.get(0).split("[ \t{}]+");
        for (String headerToken : headerTokens) {
            switch (headerToken) {
                case "strict":
                    graphAttributes.put(DotTools.strictAttr, "true");
                    break;
                case "digraph":
                    graphAttributes.put(DotTools.directedAttr, "true");
                    break;
                case "graph":
                    graphAttributes.put(DotTools.directedAttr, "false");
                    break;
                default:
                    graphAttributes.put(DotTools.graphNameAttr, line.revertSubst(headerToken));
            }
        }
    }

    /**
     * Parses a line with global graph, nodes or edges attributes.
     *
     * @param graphAttributes the global attributes.
     * @param line the line.
     * @param tokens the line components.
     */
    private static void parseGlobalLine(DotAttributes attributes, EscapedString line, List<String> tokens) {
        if (tokens.size() > 1) {
            attributes.putAll(extractAttributes(line, tokens.get(1)));
        }
    }

    /**
     * Parses a node line.
     *
     * @param nodeAttributes the node attributes.
     * @param line the line.
     * @param tokens the line components.
     */
    private static void parseNodeLine(Map<String, DotAttributes> nodeAttributes, EscapedString line, List<String> tokens) {
        if (tokens.get(0).trim().split(" ").length > 1) {
            throw new UnsupportedOperationException("The line:\n" + line.original + "\nhas an unsupported syntax.");
        }

        String nodeId = line.revertSubst(tokens.get(0).trim());
        //TODO: fixed here
        nodeId = nodeId.replace(" ", "_");//"\""+nodeId+"\"";
        if (!nodeAttributes.containsKey(nodeId)) {
            nodeAttributes.put(nodeId, new DotAttributes());
        }
        if (tokens.size() > 1) {
            DotAttributes attributes = nodeAttributes.get(nodeId);
            attributes.putAll(extractAttributes(line, tokens.get(1)));
        }
    }

    /**
     * Parses an edge line. Supports the definition of multiple edges in the
     * same line, as allowed by the dot format. Example a -- b -- c.
     *
     * @param edgeAttributes the edge attributes.
     * @param line the line.
     * @param tokens the line components.
     */
    private static void parseEdgeLine(List<DotAttributes> edgeAttributes, EscapedString line, List<String> tokens) {
        String[] multiEdgeDefinition = tokens.get(0).trim().split(String.format(ParserTools.SPLIT_KEEP_DELIMITERS, "(--|->)"));
        if (multiEdgeDefinition.length % 2 != 1) {
            throw new UnsupportedOperationException("The line:\n" + line.original + "\nhas an unsupported syntax.");
        }
        DotAttributes multiEdgeAttributes = new DotAttributes();
        if (tokens.size() > 1) {
            multiEdgeAttributes.putAll(extractAttributes(line, tokens.get(1)));
        }
        for (int i = 0; i < multiEdgeDefinition.length - 2; i = i + 2) {
        	//TODO: fixed here
            String source = line.revertSubst(multiEdgeDefinition[i]).trim();
            source = source.replace(" ", "_");//"\""+source+"\"";
            String target = line.revertSubst(multiEdgeDefinition[i + 2]).trim();
            target = target.replace(" ", "_");//"\""+target+"\"";
            String edge = line.revertSubst(multiEdgeDefinition[i + 1]);
            DotAttributes currentEdgeAttributes = new DotAttributes();
            currentEdgeAttributes.put(DotTools.edgeSourceAttr, source);
            currentEdgeAttributes.put(DotTools.edgeTargetAttr, target);
            currentEdgeAttributes.put(DotTools.directedAttr, edge.equals("->") ? "true" : "false");
            currentEdgeAttributes.putAll(multiEdgeAttributes);
            edgeAttributes.add(currentEdgeAttributes);
        }
    }

    /**
     * Extracts the dot attributes from a line.
     *
     * @param line the line.
     * @param attributeToken the attribute string of that line.
     * @return the collection of dot attributes.
     */
    private static DotAttributes extractAttributes(EscapedString line, String attributeToken) {
        DotAttributes attributes = new DotAttributes();
        String[] tokens = attributeToken.split("[,= \t]+");
        if (tokens.length % 2 != 0) {
            throw new UnsupportedOperationException("The attribute line:\n" + attributeToken + "\nhas an unsupported syntax.");
        }
        for (int i = 0; i < tokens.length; i++) {
            String attributeName = line.revertSubst(tokens[i]);
            String attributeValue = line.revertSubst(tokens[++i]);
            attributes.put(attributeName, attributeValue);
        }
        return attributes;
    }

    /**
     * Generates the graph specified by the dot file.
     *
     * @return the generated graph.
     */
    private Graph generateGraph() {
        Graph graph = new Graph();

        (new GraphGlobalAS(graph)).assignAttributes();
        (new NodeGlobalAS(graph)).assignAttributes();
        (new EdgeGlobalAS(graph)).assignAttributes();

        for (Map.Entry<String, DotAttributes> entry : nodeAttributes.entrySet()) {
            Node node = graph.newNode(entry.getKey());
            (new NodeAS(graph, node, entry.getValue())).assignAttributes();
        }

        for (DotAttributes entry : edgeAttributes) {
        	

            Node source = graph.getNode(entry.get(DotTools.edgeSourceAttr));
            Node target = graph.getNode(entry.get(DotTools.edgeTargetAttr));
            
        
            Edge edge = graph.newEdge(source, target);
            (new EdgeAS(graph, edge, entry)).assignAttributes();
        }

        if (clusterBy != null) {
            constructClusters(graph);
            assignClusterAttrInNodes(graph);
            if (saveGmapPolygons) {
                extractAndAssignPolygons(graph);
            }
        }

        return graph;
    }

    /**
     * Construct the clusters.
     *
     * @param graph the generated graph.
     */
    private Map<Graph, String> constructClusters(Graph graph) {
        Map<Graph, String> graphToClusterId = new HashMap<>();
        Map<String, List<String>> clusterMap = getClusterMap();
        for (String clusterId : clusterMap.keySet()) {

            Graph subGraph = graph.newSubGraph();
            subGraph.newLocalGraphAttribute(StdAttribute.label, clusterId);
            for (String nodeId : clusterMap.get(clusterId)) {
                subGraph.add(graph.getNode(nodeId));
            }
        }
        return graphToClusterId;
    }

    /**
     * Gets the map of the clusters. The map contains the cluster ID as entry
     * key, and the list of nodes id as entry value.
     *
     * @return the cluster map.
     */
    private Map<String, List<String>> getClusterMap() {
        Map<String, List<String>> clusters = new HashMap<>();

        for (Map.Entry<String, DotAttributes> entry : nodeAttributes.entrySet()) {
            String clusterId = entry.getValue().containsKey(clusterBy)
                    ? entry.getValue().get(clusterBy) : "Unknown";

            if (!clusters.containsKey(clusterId)) {
                clusters.put(clusterId, new ArrayList<>());
            }
            clusters.get(clusterId).add(entry.getKey());
        }
        return clusters;
    }

    /**
     * Extracts the polygons and assign them to the relative clusters.
     *
     * @param graph the generated graph.
     */
    private void extractAndAssignPolygons(Graph graph) {
        String polygonAttrId = "";    // TODO figure out why graphviz does not have a single attribute for this
        for (String possibleId : DotTools.polygonDotAttr) {
            if (graphAttributes.containsKey(possibleId)) {
                polygonAttrId = possibleId;
            }
        }                            // Hopefully remove this at some point

        if (graphAttributes.containsKey(polygonAttrId)) {
            String polygonString = graphAttributes.get(polygonAttrId);
            List<ColoredPolygon> polygons = extractPolygons(polygonString);
            cleanPolygons(polygons);
            assignPolygons(graph, polygons);
        }
    }

    /**
     * Extracts the polygons from the relative dot attribute value.
     *
     * @param polygonString the attribute value.
     * @return the extracted polygons.
     */
    private List<ColoredPolygon> extractPolygons(String polygonString) {
        DotValueConverter<Coordinates> converter = getPositionConverter();
        polygonString = polygonString.replace("\\", "");
        List<ColoredPolygon> polygons = new ArrayList<>();

        String[] tokens = polygonString.split("[ \t]+");
        Color polygonColor = Color.WHITE;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("-#") && tokens[i].length() > 2) {
                polygonColor = RenderingTools.colorHexReader(tokens[i].substring(1));
                continue;
            } else if (!tokens[i].equals("P")) {
                continue;
            }

            Polygon polygon = new Polygon();
            int numberOfPoints = Integer.parseInt(tokens[++i]);
            for (int j = 0; j < numberOfPoints; j++) {
                Coordinates point = converter.dotToGraphLib(tokens[++i] + ", " + tokens[++i]);
                polygon.add(point);
            }
            if (numberOfPoints > 0) {
                polygons.add(new ColoredPolygon(polygon, polygonColor));
            }
        }
        return polygons;
    }

    /**
     * Extracts the position converter to provide a correct conversion of the
     * polygon points into the graph space.
     *
     * @return the converter used for the node position.
     */
    @SuppressWarnings("unchecked")
    private DotValueConverter<Coordinates> getPositionConverter() {
        DotValueConverter<Coordinates> converter = new DotValueConverter.PositionConverter();
        for (AttributeConvSettings convSettings : nodeAttrSettings.toConvert) {
            if (convSettings.sourceAttrId.equals("pos")) {
                if (convSettings.converter != null) {
                    converter = (DotValueConverter<Coordinates>) convSettings.converter;
                } else {
                    converter = defaultConverters.get(Coordinates.class);
                }
            }
        }
        return converter;
    }

    /**
     * Cleans the polygons generated by GMap. The polygons often contains
     * portion of boundaries (such as ..AA.. or ..ABA..) that should be removed.
     *
     * @param polygons the GMap polygons.
     */
    private void cleanPolygons(List<ColoredPolygon> polygons) {
        for (ColoredPolygon coloredPolygon : polygons) {
            Polygon polygon = coloredPolygon.polygon;
            boolean modified = true;
            while (modified) {
                modified = false;
                for (int i = 0; i < polygon.size(); i++) {
                    int j = (i + 1) % polygon.size();
                    int k = (i + 2) % polygon.size();
                    if (Geom.e2D.almostEqual(polygon.get(i), polygon.get(j))
                            || Geom.e2D.almostEqual(polygon.get(i), polygon.get(k))) {
                        polygon.remove(j);
                        modified = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Assigns the cluster attributes that are stored as node attributes in the
     * dot file.
     *
     * @param graph the graph.
     */
    private void assignClusterAttrInNodes(Graph graph) {
        if (nodeToClusterAttrSettings.toConvert.isEmpty()) {
            return;
        }

        for (Graph cluster : graph.subGraphs()) {
            DotAttributes attributes = new DotAttributes();
            for (Node node : cluster.nodes()) {
                attributes.putAll(nodeAttributes.get(node.id()));
            }
            (new ClusterAS(cluster, attributes)).assignAttributes();
        }
    }

    /**
     * Associates the extracted polygons to their clusters.
     *
     * @param graph the graph.
     * @param polygons the extracted polygons.
     */
    private void assignPolygons(Graph graph, List<ColoredPolygon> polygons) {
        Collections.sort(polygons, (ColoredPolygon a, ColoredPolygon b)
                -> new Double(Geom.e2D.polygonArea(a.polygon)).compareTo(Geom.e2D.polygonArea(b.polygon)));

        Map<ColoredPolygon, Set<Node>> polygonToContainedNodes = new HashMap<>();
        Set<Node> unassignedNodes = new HashSet<>(graph.nodes());
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        for (ColoredPolygon coloredPolygon : polygons) {
            Polygon polygon = coloredPolygon.polygon;
            Set<Node> containedNodes = new HashSet<>();
            for (Node node : unassignedNodes) {
                if (Geom.e2D.isPointInPolygon(positions.get(node), polygon)) {
                    containedNodes.add(node);
                }
            }
            polygonToContainedNodes.put(coloredPolygon, containedNodes);
            unassignedNodes.removeAll(containedNodes);
        }

        for (Graph subgraph : graph.subGraphs()) {
            List<Polygon> subGraphPolygons = new ArrayList<>();
            subgraph.newLocalGraphAttribute(DotTools.polygonIdAttr, subGraphPolygons);

            for (ColoredPolygon coloredPolygon : polygonToContainedNodes.keySet()) {
                Polygon polygon = coloredPolygon.polygon;
                Set<Node> containedNodes = polygonToContainedNodes.get(coloredPolygon);
                if (!containedNodes.isEmpty() && subgraph.has(containedNodes.iterator().next())) {
                    subGraphPolygons.add(polygon);
                    addPolygonToGraphics(subgraph, coloredPolygon, subGraphPolygons.size());
                }
            }
        }
    }

    /**
     * Adds the given colored polygon to the graphics attribute of the subgraph.
     *
     * @param subgraph the subgraph.
     * @param coloredPolygon the colored polygon.
     * @param polygonNumber the index of the current polygon.
     */
    private void addPolygonToGraphics(Graph subgraph, ColoredPolygon coloredPolygon, int polygonNumber) {
        String clusterLabel = subgraph.<String>graphAttribute(StdAttribute.label).get();
        String polygonLabel = clusterLabel + "_GmapPolygon" + polygonNumber;
        SvgPolygon svgPolygon = new SvgPolygon(polygonLabel, coloredPolygon.polygon, coloredPolygon.fillColor, 0.1, Color.BLACK);
        RenderingTools.Graphics.addLocalGraphicLast(subgraph, svgPolygon);
    }

    /**
     * A class storing a polygon and its color.
     */
    protected class ColoredPolygon {

        protected Polygon polygon;
        protected Color fillColor;

        protected ColoredPolygon(Polygon polygon, Color fillColor) {
            this.polygon = polygon;
            this.fillColor = fillColor;
        }
    }

    /**
     * AttributeSetter for DotReader. Handles the conversion from dot to graph
     * attributes according to the specified settings. This generic class
     * contains the methods that are invariant from the type of attribute
     * (graph, node, edge) as well as with respect to the attribute level
     * (global, local).
     */
    private abstract class DotReaderAttributeSetter {

        protected Graph graph;
        protected DotAttributes dotAttributes;
        protected ConversionSettings convSettings;

        /**
         * Assigns all dot attributes to the related graph entity.
         */
        protected void assignAttributes() {
            for (AttributeConvSettings attributeSettings : convSettings.toConvert) {
                assignAttribute(attributeSettings);
            }
            if (convSettings.saveUnspecified) {
                Set<String> unspecifiedToconvert = dotAttributes.keySet();
                unspecifiedToconvert.removeAll(specifiedAttributes(convSettings.toConvert));
                unspecifiedToconvert.removeAll(convSettings.toIgnore);
                for (String attrId : unspecifiedToconvert) {
                    assignAttribute(new AttributeConvSettings(attrId, attrId));
                }
            }
        }

        /**
         * Assigns a single attribute to the related graph entity.
         *
         * @param attributeSettings the conversion settings for the attribute.
         */
        protected void assignAttribute(AttributeConvSettings attributeSettings) {
            String stringValue = getCombinedAttributeValue(attributeSettings.sourceAttrId, dotAttributes);
            if (stringValue != null) {
                DotValueConverter<?> converter = getConverter(attributeSettings);
                writeAttributeValue(attributeSettings.destAttrId, converter.dotToGraphLib(stringValue), converter.defaultValue());
            }
        }

        /**
         * Retrieves the converter to be used.
         *
         * @param attributeSettings the conversion settings for the attribute.
         */
        protected DotValueConverter<?> getConverter(AttributeConvSettings attributeSettings) {
            if (attributeSettings.converter != null) {
                return attributeSettings.converter;
            } else if (attributeSettings.type != null && defaultConverters.contains(attributeSettings.type)) {
                return defaultConverters.get(attributeSettings.type);
            } else {
                return new DotValueConverter.StringConverter();
            }
        }

        /**
         * Gets the list of all specified attributes, which are the attributes
         * for which conversion settings are explicitly defined.
         *
         * @param attributesToConvert the settings of the attributes to convert.
         * @return the list of specified attributes IDs.
         */
        private Set<String> specifiedAttributes(List<AttributeConvSettings> attributesToConvert) {
            Set<String> specifiedAttributes = new HashSet<>();
            for (AttributeConvSettings attributeSettings : attributesToConvert) {
                String[] singleAttributes = attributeSettings.sourceAttrId.split("[^a-zA-Z0-9]+");
                specifiedAttributes.addAll(Arrays.asList(singleAttributes));
            }
            return specifiedAttributes;
        }

        /**
         * Compose two attributes whenever a graph library attribute requires
         * parsing two or more dot attributes at the same time. The format of
         * the result is specified in the combinedDotId. Letters and numbers
         * identify attributes and are converted into their attribute values,
         * and other characters are kept in place. For example, if width=10 and
         * height=12, The string "(width|height)" will be converted into
         * "(10|12)".
         *
         * @param combinedDotId the string indicating which attribute to
         * consider and the format of the results.
         * @param attributes the dot attributes.
         * @return the result string.
         */
        protected String getCombinedAttributeValue(String combinedDotId, DotAttributes attributes) {
            String[] combineAttrTokens = combinedDotId.split(String.format(ParserTools.SPLIT_KEEP_DELIMITERS, "[^a-zA-Z0-9]+"));
            String value = "";
            for (String token : combineAttrTokens) {
                boolean isAttributeId = token.matches("[a-zA-Z0-9]+");
                if (isAttributeId) {
                    if (attributes.containsKey(token)) {
                        value += attributes.get(token);
                    } else {
                        return null;
                    }
                } else {
                    value += token;
                }
            }
            return value;
        }

        /**
         * Writes an attribute value to the graph.
         *
         * @param attrId the graph attribute id.
         * @param value the object built by the converter.
         * @param defaultValue a default value for the attribute, used if a new
         * element attribute needs to be built.
         */
        protected abstract void writeAttributeValue(String attrId, Object value, Object defaultValue);
    }

    /**
     * Assigns the global graph attributes.
     */
    private class GraphGlobalAS extends DotReaderAttributeSetter {

        private GraphGlobalAS(Graph graph) {
            this.graph = graph;
            this.convSettings = graphAttrSettings;
            this.dotAttributes = graphAttributes;
        }

        @Override
        protected void writeAttributeValue(String attrId, Object value, Object defaultValue) {
            graph.newGraphAttribute(attrId, value);
        }
    }

    /**
     * Assigns the global node attributes.
     */
    private class NodeGlobalAS extends DotReaderAttributeSetter {

        private NodeGlobalAS(Graph graph) {
            this.graph = graph;
            this.convSettings = nodeAttrSettings;
            this.dotAttributes = defaultNodeAttributes;
        }

        @Override
        protected void writeAttributeValue(String attrId, Object value, Object defaultValue) {
            graph.newNodeAttribute(attrId, value);
        }
    }

    /**
     * Assigns the global edge attributes.
     */
    private class EdgeGlobalAS extends DotReaderAttributeSetter {

        private EdgeGlobalAS(Graph graph) {
            this.graph = graph;
            this.convSettings = edgeAttrSettings;
            dotAttributes = defaultEdgeAttributes;
        }

        @Override
        protected void writeAttributeValue(String attrId, Object value, Object defaultValue) {
            graph.newEdgeAttribute(attrId, value);
        }
    }

    /**
     * Assigns the node attributes.
     */
    private class NodeAS extends DotReaderAttributeSetter {

        private final Node node;

        private NodeAS(Graph graph, Node node, DotAttributes attributes) {
            this.graph = graph;
            this.node = node;
            this.convSettings = nodeAttrSettings;
            this.dotAttributes = attributes;
        }

        @Override
        protected void writeAttributeValue(String attrId, Object value, Object defaultValue) {
            if (!graph.hasNodeAttribute(attrId)) {
                graph.newNodeAttribute(attrId, defaultValue);
            }
            graph.nodeAttribute(attrId).set(node, value);
        }
    }

    /**
     * Assigns the edge attributes.
     */
    private class EdgeAS extends DotReaderAttributeSetter {

        private final Edge edge;

        private EdgeAS(Graph graph, Edge edge, DotAttributes attributes) {
            this.graph = graph;
            this.edge = edge;
            this.convSettings = edgeAttrSettings;
            this.dotAttributes = attributes;
        }

        @Override
        protected void writeAttributeValue(String attrId, Object value, Object defaultValue) {
            if (!graph.hasEdgeAttribute(attrId)) {
                graph.newEdgeAttribute(attrId, defaultValue);
            }
            graph.edgeAttribute(attrId).set(edge, value);
        }
    }

    /**
     * Assigns the global graph attributes.
     */
    private class ClusterAS extends DotReaderAttributeSetter {

        private ClusterAS(Graph cluster, DotAttributes attributes) {
            this.graph = cluster;
            this.convSettings = nodeToClusterAttrSettings;
            this.dotAttributes = attributes;
        }

        @Override
        protected void writeAttributeValue(String attrId, Object value, Object defaultValue) {
            graph.newLocalGraphAttribute(attrId, value);
        }
    }

}
