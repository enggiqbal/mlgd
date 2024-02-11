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
package ocotillo.serialization.oco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ocotillo.dygraph.Evolution;
import ocotillo.graph.Attribute;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Element;
import ocotillo.graph.GraphWithAttributes;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

/**
 * Writer for oco files.
 */
public class OcoWriter {

    private final OcoConverterSet converters;
    private final String graphHeader;

    /**
     * Constructs an oco writer.
     *
     * @param converters the converters.
     * @param graphHeader provides the header for this type of graph.
     */
    protected OcoWriter(OcoConverterSet converters, String graphHeader) {
        this.converters = converters;
        this.graphHeader = graphHeader;
    }

    /**
     * Writes a graph in the oco format.
     *
     * @param graph the input graph.
     * @return the graph description in oco format.
     */
    protected List<String> write(GraphWithAttributes<?, ?, ?, ?> graph) {
        List<String> lines = new LinkedList<>();
        writeGraph(graph, 0, lines);
        return lines;
    }

    /**
     * Recursively write graphs in oco format.
     *
     * @param graph the graph.
     * @param graphLevel the current graph level.
     * @param lines the file lines.
     */
    private void writeGraph(GraphWithAttributes<?, ?, ?, ?> graph, int graphLevel, List<String> lines) {
        writeGraphHeader(graphLevel, lines);
        writeAttributesAndElements(graph, Attribute.Type.graph, lines);
        lines.add("");
        lines.add("#nodes");
        writeAttributesAndElements(graph, Attribute.Type.node, lines);
        lines.add("");
        lines.add("#edges");
        writeAttributesAndElements(graph, Attribute.Type.edge, lines);
        lines.add("");
        lines.add("");

        for (GraphWithAttributes<?, ?, ?, ?> subgraph : graph.subGraphs()) {
            writeGraph(subgraph, graphLevel + 1, lines);
        }
    }

    /**
     * Writes the graph header according to the current graph level.
     *
     * @param graphLevel the current graph level.
     * @param lines the file lines.
     */
    private void writeGraphHeader(int graphLevel, List<String> lines) {
        StringBuilder headerPrefix = new StringBuilder("#");
        for (int i = 0; i < graphLevel; i++) {
            headerPrefix.append("#");
        }
        lines.add(headerPrefix.append(graphHeader).toString());
    }

    /**
     * Write the attribute and the eventual elements of a oco file block.
     *
     * @param graph the graph.
     * @param generalAttrType the kind of block to written for the graph.
     * @param lines the file lines.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeAttributesAndElements(GraphWithAttributes<?, ?, ?, ?> graph, Attribute.Type generalAttrType, List<String> lines) {
        StringBuilder attributes = new StringBuilder("@attribute\t");
        StringBuilder types = new StringBuilder("@type\t");
        StringBuilder defaults = new StringBuilder("@default\t");

        boolean isRootGraph = (graph == graph.rootGraph());
        if (generalAttrType == Attribute.Type.edge && isRootGraph) {
            attributes.append("@from\t@to\t");
            types.append("\t\t");
            defaults.append("\t\t");
        }

        Map<Element, StringBuilder> elements = prepareElementsMap(graph, generalAttrType, isRootGraph);

        List<String> orderedAttributeList = new ArrayList<>(graph.localAttributes(generalAttrType).keySet());
        Collections.sort(orderedAttributeList);

        for (String attributeName : orderedAttributeList) {
            Attribute attribute = graph.attribute(generalAttrType, attributeName);
            if (!attribute.isSleeping()) {
                Object defaultValue = attribute.getDefault();
                OcoValueConverter converter = getConverter(attributeName, defaultValue);
                String attributeType = converter.typeName();
                String attributeDefault = converter.graphLibToOco(defaultValue);

                attributes.append(attributeName).append("\t");
                types.append(attributeType).append("\t");
                defaults.append(attributeDefault).append("\t");

                if (generalAttrType == Attribute.Type.node) {
                    for (Node node : graph.nodes()) {
                        NodeAttribute nodeAttribute = (NodeAttribute) attribute;
                        String value = nodeAttribute.isDefault(node)
                                ? "" : converter.graphLibToOco(nodeAttribute.get(node));
                        elements.get(node).append(value).append("\t");
                    }
                } else if (generalAttrType == Attribute.Type.edge) {
                    for (Edge edge : graph.edges()) {
                        EdgeAttribute edgeAttribute = (EdgeAttribute) attribute;
                        String value = converter.graphLibToOco(edgeAttribute.get(edge)) + "\t";
                        elements.get(edge).append(value);
                    }
                }
            }
        }
        lines.add(attributes.substring(0, attributes.length() - 1));
        lines.add(types.substring(0, types.length() - 1));
        lines.add(defaults.substring(0, defaults.length() - 1));
        writeElements(elements, lines);
    }

    /**
     * Gets a converter for the given attribute.
     *
     * @param attributeName the attribute name.
     * @param defaultValue the attribute default value.
     * @return the converter.
     */
    private OcoValueConverter<?> getConverter(String attributeName, Object defaultValue) {
        if (StdAttribute.isStandard(attributeName)) {
            Class<?> type = StdAttribute.get(attributeName).matchingClass;
            return converters.get(type);
        } else {
            if (defaultValue instanceof Evolution) {
                defaultValue = ((Evolution) defaultValue).getDefaultValue();
            }
            Class<?> type = defaultValue.getClass();
            if (converters.contains(type)) {
                return converters.get(type);
            } else {
                return converters.get(String.class);
            }
        }
    }

    /**
     * Prepares the map of elements for the block.
     *
     * @param graph the graph.
     * @param generalAttrType the kind of block to written for the graph.
     * @param isRootGraph indicates if the graph is the root one.
     * @return the element map.
     */
    private Map<Element, StringBuilder> prepareElementsMap(GraphWithAttributes<?, ?, ?, ?> graph, Attribute.Type generalAttrType, boolean isRootGraph) {
        Map<Element, StringBuilder> elements = new HashMap<>();
        if (generalAttrType == Attribute.Type.node) {
            for (Node node : graph.nodes()) {
                elements.put(node, new StringBuilder(node.id() + "\t"));
            }
        } else if (generalAttrType == Attribute.Type.edge) {
            for (Edge edge : graph.edges()) {
                StringBuilder edgeLine = new StringBuilder(edge.id() + "\t");
                if (isRootGraph) {
                    edgeLine.append(edge.source().id()).append("\t")
                            .append(edge.target().id()).append("\t");
                }
                elements.put(edge, edgeLine);
            }
        }
        return elements;
    }

    /**
     * Writes the elements in order by id.
     *
     * @param elements the element map.
     * @param lines the file lines.
     */
    private void writeElements(Map<Element, StringBuilder> elements, List<String> lines) {
        List<Element> elementList = new ArrayList<>(elements.keySet());
        Collections.sort(elementList, (Element a, Element b)
                -> a.id().compareTo(b.id()));

        for (Element element : elementList) {
            StringBuilder elementLine = elements.get(element);
            lines.add(elementLine.substring(0, elementLine.length() - 1));
        }
    }
}
