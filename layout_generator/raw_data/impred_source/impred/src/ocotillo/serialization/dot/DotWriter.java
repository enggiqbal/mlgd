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

import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.serialization.dot.DotTools.DotAttributes;
import ocotillo.serialization.dot.ConversionSettings.AttributeConvSettings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DotWriter {

    private final ConversionSettings graphAttrSettings;
    private final ConversionSettings nodeAttrSettings;
    private final ConversionSettings edgeAttrSettings;
    private final ConversionSettings clusterToNodeAttrSettings;
    private final DefaultDotConverters defaultConverters;

    private final DotAttributes graphAttributes = new DotAttributes();
    private final DotAttributes defaultNodeAttributes = new DotAttributes();
    private final DotAttributes defaultEdgeAttributes = new DotAttributes();
    private final Map<String, DotAttributes> nodeAttributes = new HashMap<>();
    private final List<DotAttributes> edgeAttributes = new ArrayList<>();

    /**
     * Builder for dot writers.
     */
    public static class DotWriterBuilder {

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
         * The conversion settings for the cluster attributes to be inserted as
         * node ones.
         */
        public final ConversionSettings.AllOperations clusterToNodeAttributes = new ConversionSettings.AllOperations();
        /**
         * The default converters.
         */
        public final DefaultDotConverters defaultConverters = new DefaultDotConverters();

        /**
         * Constructs a dot writer.
         *
         * @return
         */
        public DotWriter build() {
            return new DotWriter(graphAttributes, nodeAttributes, edgeAttributes, clusterToNodeAttributes, defaultConverters);
        }
    }

    public DotWriter(ConversionSettings graphAttrSettings, ConversionSettings nodeAttrSettings, ConversionSettings edgeAttrSettings, ConversionSettings clusterToNodeAttrSettings, DefaultDotConverters defaultConverters) {
        this.graphAttrSettings = graphAttrSettings;
        this.nodeAttrSettings = nodeAttrSettings;
        this.edgeAttrSettings = edgeAttrSettings;
        this.clusterToNodeAttrSettings = clusterToNodeAttrSettings;
        this.defaultConverters = defaultConverters;
    }

    /**
     * Writes the graph in dot format on the given file.
     *
     * @param graph the graph.
     * @param file the output file.
     */
    public void writeGraph(Graph graph, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            List<String> lines = writeGraph(graph);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("The file " + file.getName() + "is not writable.");
        }
    }

    /**
     * Writes the graph in dot format.
     *
     * @param graph the graph.
     * @return the dot lines.
     */
    public List<String> writeGraph(Graph graph) {
        graphAttributes.clear();
        defaultNodeAttributes.clear();
        defaultEdgeAttributes.clear();
        nodeAttributes.clear();
        edgeAttributes.clear();

        (new GraphGlobalAS(graph)).fillDotAttributes();
        (new NodeGlobalAS(graph)).fillDotAttributes();
        (new EdgeGlobalAS(graph)).fillDotAttributes();

        extractNodeAttributes(graph);
        extractEdgeAttributes(graph);
        extractClusterAttributes(graph);

        return writeDotLines();
    }

    /**
     * Extracts the node attributes from a graph and inserts them in the dot
     * attribute maps.
     *
     * @param graph the graph.
     */
    private void extractNodeAttributes(Graph graph) {
        for (Node node : graph.nodes()) {
            DotAttributes attributes = new DotAttributes();
            (new NodeAS(graph, node, attributes)).fillDotAttributes();
            nodeAttributes.put(node.id(), attributes);
        }
    }

    /**
     * Extracts the edge attributes from a graph and inserts them in the dot
     * attribute maps.
     *
     * @param graph the graph.
     */
    private void extractEdgeAttributes(Graph graph) {
        List<Edge> orderedEdges = new ArrayList<>(graph.edges());
        Collections.sort(orderedEdges);
        for (Edge edge : orderedEdges) {
            DotAttributes attributes = new DotAttributes();
            attributes.put(DotTools.edgeSourceAttr, edge.source().id());
            attributes.put(DotTools.edgeTargetAttr, edge.target().id());
            if (graph.hasEdgeAttribute(DotTools.directedAttr)
                    && graph.<Boolean>edgeAttribute(DotTools.directedAttr).get(edge)) {
                attributes.put(DotTools.directedAttr, "true");
            }
            (new EdgeAS(graph, edge, attributes)).fillDotAttributes();
            edgeAttributes.add(attributes);
        }
    }

    /**
     * Extracts the cluster attributes from a graph and inserts them in the dot
     * attribute maps as node attributes.
     *
     * @param graph the graph.
     */
    private void extractClusterAttributes(Graph graph) {
        for (Graph cluster : graph.subGraphs()) {
            DotAttributes clusterAttr = new DotAttributes();
            (new ClusterAS(cluster, clusterAttr)).fillDotAttributes();
            for (Node node : cluster.nodes()) {
                nodeAttributes.get(node.id()).putAll(clusterAttr);
            }
        }
    }

    /**
     * Writes the attributes in the maps as dot lines.
     *
     * @return the dot lines.
     */
    private List<String> writeDotLines() {
        List<String> dotLines = new ArrayList<>();
        dotLines.add(writeOpeningLine());
        dotLines.add(writeGraphGlobalLine());
        dotLines.add(writeNodeGlobalLine());
        dotLines.add(writeEdgeGlobalLine());

        List<String> orderedNodeIds = new ArrayList<>(nodeAttributes.keySet());
        Collections.sort(orderedNodeIds);
        for (String nodeId : orderedNodeIds) {
            dotLines.add(writeNodeLine(nodeId, nodeAttributes.get(nodeId)));
        }

        for (DotAttributes entry : edgeAttributes) {
            dotLines.add(writeEdgeLine(entry));
        }

        dotLines.add("}");
        return dotLines;
    }

    /**
     * Writes the opening line of a dot file.
     *
     * @return the opening line.
     */
    private String writeOpeningLine() {
        String line = "";
        if (graphAttributes.containsKey(DotTools.strictAttr)
                && graphAttributes.get(DotTools.strictAttr).equals("true")) {
            line += "strict ";
        }
        if (graphAttributes.containsKey(DotTools.directedAttr)
                && graphAttributes.get(DotTools.directedAttr).equals("true")) {
            line += "digraph ";
        } else {
            line += "graph ";
        }
        if (graphAttributes.containsKey(DotTools.graphNameAttr)) {
            line += graphAttributes.get(DotTools.graphNameAttr) + " ";
        }
        return line + "{";
    }

    /**
     * Writes the line of the graph attributes.
     *
     * @return the graph attribute line.
     */
    private String writeGraphGlobalLine() {
        DotAttributes attributes = new DotAttributes(graphAttributes);
        attributes.remove(DotTools.strictAttr);
        attributes.remove(DotTools.directedAttr);
        attributes.remove(DotTools.graphNameAttr);
        return ""; //TODO:// "\tgraph [" + writeAttributes(attributes) + "];";
    }

    /**
     * Writes the line of the default node attributes.
     *
     * @return the default node attribute line.
     */
    private String writeNodeGlobalLine() {
        DotAttributes attributes = new DotAttributes(defaultNodeAttributes);
        return ""; //TODO: changed here //"\tnode [" + writeAttributes(attributes) + "];";
    }

    /**
     * Writes the line of the default edge attributes.
     *
     * @return the default edge attribute line.
     */
    private String writeEdgeGlobalLine() {
        DotAttributes attributes = new DotAttributes(defaultEdgeAttributes);
        attributes.remove(DotTools.edgeSourceAttr);
        attributes.remove(DotTools.edgeTargetAttr);
        attributes.remove(DotTools.directedAttr);
        return ""; //TODO://"\tedge [" + writeAttributes(attributes) + "];";
    }

    /**
     * Writes the line of a node.
     *
     * @return the node line.
     */
    private String writeNodeLine(String nodeId, DotAttributes nodeAttributes) {
        DotAttributes attributes = new DotAttributes(nodeAttributes);
        return "\t" + nodeId + " [" + writeAttributes(attributes) + "];";
    }

    /**
     * Writes the line of an edge.
     *
     * @return the edge line.
     */
    private String writeEdgeLine(DotAttributes nodeAttributes) {
        DotAttributes attributes = new DotAttributes(nodeAttributes);
        String edgeSource = attributes.get(DotTools.edgeSourceAttr);
        String edgeTarget = attributes.get(DotTools.edgeTargetAttr);
        String edge;
        if (attributes.containsKey(DotTools.directedAttr)
                && attributes.get(DotTools.directedAttr).equals("true")) {
            edge = " -> ";
        } else {
            edge = " -- ";
        }

        attributes.remove(DotTools.edgeSourceAttr);
        attributes.remove(DotTools.edgeTargetAttr);
        attributes.remove(DotTools.directedAttr);
        return "\t" + edgeSource + edge + edgeTarget + " [" + writeAttributes(attributes) + "];";
    }

    /**
     * Write a sequence of attributes in the dot format.
     *
     * @param attributes the attributes.
     * @return the attribute line.
     */
    private String writeAttributes(DotAttributes attributes) {
        if (attributes.isEmpty()) {
            return " ";
        }
        String line = "";
        List<Map.Entry<String, String>> orderedAttributes = new ArrayList<>(attributes.entrySet());
        Collections.sort(orderedAttributes, (Map.Entry<String, String> a, Map.Entry<String, String> b)
                -> a.getKey().compareTo(b.getKey()));
        for (Map.Entry<String, String> entry : orderedAttributes) {
            line += entry.getKey() + "=\"" + entry.getValue() + "\", ";
        }
        return line.substring(0, line.length() - 2);
    }

    /**
     * AttributeSetter for DotWriter. Handles the conversion from graph to dot
     * attributes according to the specified settings. This generic class
     * contains the methods that are invariant from the type of attribute
     * (graph, node, edge) as well as with respect to the attribute level
     * (global, local).
     */
    private abstract class DotWriterAttributeSetter {

        protected Graph graph;
        protected DotAttributes dotAttributes;
        protected ConversionSettings convSettings;

        /**
         * Extract graph library attributes and fills the map on dot attributes.
         */
        protected void fillDotAttributes() {
            for (AttributeConvSettings attributeSettings : convSettings.toConvert) {
                convertValue(attributeSettings);
            }
            if (convSettings.saveUnspecified) {
                Set<String> unspecifiedToconvert = getAllAttributeIds();
                unspecifiedToconvert.removeAll(specifiedAttributes(convSettings.toConvert));
                unspecifiedToconvert.removeAll(convSettings.toIgnore);
                for (String attrId : unspecifiedToconvert) {
                    convertValue(new AttributeConvSettings(attrId, attrId));
                }
            }
        }

        /**
         * Performs the conversion of a given attribute,
         *
         * @param attributeSettings the attribute settings.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        protected void convertValue(AttributeConvSettings attributeSettings) {
            if (!isValueDefault(attributeSettings.sourceAttrId)) {
                Object value = retrieveAttributeValue(attributeSettings.sourceAttrId);
                DotValueConverter converter = getConverter(attributeSettings, value);
                dotAttributes.put(attributeSettings.destAttrId, converter.graphLibToDot(value));
            }
        }

        /**
         * Retrieves the converter to be used.
         *
         * @param attributeSettings the attribute settings.
         */
        protected DotValueConverter<?> getConverter(AttributeConvSettings attributeSettings, Object value) {
            if (attributeSettings.converter != null) {
                return attributeSettings.converter;
            } else if (attributeSettings.type != null && defaultConverters.contains(attributeSettings.type)) {
                return defaultConverters.get(attributeSettings.type);
            } else if (defaultConverters.contains(value.getClass())) {
                return defaultConverters.get(value.getClass());
            } else {
                throw new IllegalStateException("The default convertes do not support the type " + attributeSettings.type + " or " + value.getClass());
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
         * Retrieves all the existing attributes (of a particular type) in the
         * graph.
         *
         * @return the set of existing attributes IDs.
         */
        protected abstract Set<String> getAllAttributeIds();

        /**
         * Retrieve the value for an attribute with given id.
         *
         * @param attributeId the attribute id.
         * @return the value.
         */
        protected abstract Object retrieveAttributeValue(String attributeId);

        /**
         * Checks if the value for this attribute is default.
         *
         * @param attributeId the attribute id.
         * @return true if the value is default, false otherwise.
         */
        protected abstract boolean isValueDefault(String attributeId);
    }

    /**
     * Attribute setter for global graph attributes.
     */
    private class GraphGlobalAS extends DotWriterAttributeSetter {

        protected GraphGlobalAS(Graph graph) {
            this.graph = graph;
            this.convSettings = graphAttrSettings;
            this.dotAttributes = graphAttributes;
        }

        @Override
        protected Set<String> getAllAttributeIds() {
            return graph.graphAttributes().keySet();
        }

        @Override
        protected Object retrieveAttributeValue(String attributeId) {
            return graph.graphAttribute(attributeId).get();
        }

        @Override
        protected boolean isValueDefault(String attributeId) {
            return false;
        }
    }

    /**
     * Attribute setter for global node attributes.
     */
    private class NodeGlobalAS extends DotWriterAttributeSetter {

        protected NodeGlobalAS(Graph graph) {
            this.graph = graph;
            this.convSettings = nodeAttrSettings;
            this.dotAttributes = defaultNodeAttributes;
        }

        @Override
        protected Set<String> getAllAttributeIds() {
            return graph.nodeAttributes().keySet();
        }

        @Override
        protected Object retrieveAttributeValue(String attributeId) {
            return graph.nodeAttribute(attributeId).getDefault();
        }

        @Override
        protected boolean isValueDefault(String attributeId) {
            return false;
        }
    }

    /**
     * Attribute setter for global edge attributes.
     */
    private class EdgeGlobalAS extends DotWriterAttributeSetter {

        protected EdgeGlobalAS(Graph graph) {
            this.graph = graph;
            this.convSettings = edgeAttrSettings;
            this.dotAttributes = defaultEdgeAttributes;
        }

        @Override
        protected Set<String> getAllAttributeIds() {
            return graph.edgeAttributes().keySet();
        }

        @Override
        protected Object retrieveAttributeValue(String attributeId) {
            return graph.edgeAttribute(attributeId).getDefault();
        }

        @Override
        protected boolean isValueDefault(String attributeId) {
            return false;
        }
    }

    /**
     * Attribute setter for node attributes.
     */
    private class NodeAS extends DotWriterAttributeSetter {

        private final Node node;

        protected NodeAS(Graph graph, Node node, DotAttributes attributes) {
            this.graph = graph;
            this.node = node;
            this.convSettings = nodeAttrSettings;
            this.dotAttributes = attributes;
        }

        @Override
        protected Set<String> getAllAttributeIds() {
            return graph.nodeAttributes().keySet();
        }

        @Override
        protected Object retrieveAttributeValue(String attributeId) {
            return graph.nodeAttribute(attributeId).get(node);
        }

        @Override
        protected boolean isValueDefault(String attributeId) {
            return graph.nodeAttribute(attributeId).isDefault(node);
        }
    }

    /**
     * Attribute setter for edge attributes.
     */
    private class EdgeAS extends DotWriterAttributeSetter {

        private final Edge edge;

        protected EdgeAS(Graph graph, Edge edge, DotAttributes attributes) {
            this.graph = graph;
            this.edge = edge;
            this.convSettings = edgeAttrSettings;
            this.dotAttributes = attributes;
        }

        @Override
        protected Set<String> getAllAttributeIds() {
            return graph.edgeAttributes().keySet();
        }

        @Override
        protected Object retrieveAttributeValue(String attributeId) {
            return graph.edgeAttribute(attributeId).get(edge);
        }

        @Override
        protected boolean isValueDefault(String attributeId) {
            return graph.edgeAttribute(attributeId).isDefault(edge);
        }
    }

    /**
     * Attribute setter for cluster attributes to be saved as node ones.
     */
    private class ClusterAS extends DotWriterAttributeSetter {

        protected ClusterAS(Graph cluster, DotAttributes attributes) {
            this.graph = cluster;
            this.convSettings = clusterToNodeAttrSettings;
            this.dotAttributes = attributes;
        }

        @Override
        protected Set<String> getAllAttributeIds() {
            return graph.localGraphAttributes().keySet();
        }

        @Override
        protected Object retrieveAttributeValue(String attributeId) {
            return graph.graphAttribute(attributeId).get();
        }

        @Override
        protected boolean isValueDefault(String attributeId) {
            return false;
        }
    }
}
