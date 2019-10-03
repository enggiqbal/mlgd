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
package ocotillo.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ocotillo.dygraph.DyAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.geometry.Coordinates;

/**
 * Collects the standard, reserved attributes of a graph.
 */
public enum StdAttribute {

    nodePosition(Coordinates.class),
    nodeSize(Coordinates.class),
    nodeShape(NodeShape.class),
    nodeHeat(Double.class),
    nodeLevel(Integer.class), // The node level
    edgeWidth(Double.class),
    edgeShape(EdgeShape.class),
    edgePoints(ControlPoints.class),
    color(Color.class),
    label(String.class),
    labelColor(Color.class),
    labelScaling(Double.class),
    labelOffset(Coordinates.class),
    graphics(String.class),
    background(Color.class),
    dyPresence(Boolean.class);

    /**
     * Collects the attributes that affect the graph layout.
     */
    public static final StdAttributeSet thatAffectlayout = new StdAttributeSet(
            Arrays.<StdAttribute>asList(),
            Arrays.asList(nodePosition, nodeSize, nodeShape, nodeLevel),
            Arrays.asList(edgeWidth, edgeShape, edgePoints)
            );

    /**
     * Collects the attributes that affect the graph rendering.
     */
    public static final StdAttributeSet thatAffectRendering = new StdAttributeSet(
            Arrays.asList(graphics, background),
            Arrays.asList(nodePosition, nodeSize, nodeShape, nodeHeat, color, label, labelColor, labelScaling, labelOffset),
            Arrays.asList(edgeWidth, edgeShape, edgePoints, color));
    
    /**
     * Collects the custom attributes.
     */
    public static final Set<StdAttribute> nodeLevelAttributes =  new HashSet<>(Arrays.asList(nodeLevel));

    /**
     * Collects the attributes that are reserved for dynamic graphs.
     */
    public static final Set<StdAttribute> reservedForDynamic = new HashSet<>(Arrays.asList(dyPresence));

    /**
     * Indicates the type allowed for the given attribute.
     */
    public final Class<?> matchingClass;

    /**
     * Defines a standard attribute.
     *
     * @param matchingClass the type of data it refers to.
     */
    private StdAttribute(Class<?> matchingClass) {
        this.matchingClass = matchingClass;
    }

    /**
     * Returns the standard attribute with given name.
     *
     * @param attributeName the attribute name.
     * @return null if the attribute name is not standard, the standard
     * attribute otherwise.
     */
    public static StdAttribute get(String attributeName) {
        return attributeMap.get(attributeName);
    }

    /**
     * Checks if the given attribute is standard.
     *
     * @param attributeName the attribute name.
     * @return true if the attribute name corresponds to a standard one, false
     * otherwise.
     */
    public static boolean isStandard(String attributeName) {
        return attributeMap.containsKey(attributeName);
    }

    /**
     * Checks if the given attribute is reserved for dynamic graphs.
     *
     * @param attributeName the attribute name.
     * @return true if the attribute is reserved for dynamic graphs, false
     * otherwise.
     */
    public static boolean isReservedForDynamic(String attributeName) {
        return reservedForDynamic.contains(get(attributeName));
    }

    /**
     * Maps the standard attribute IDs to their object.
     */
    private static final Map<String, StdAttribute> attributeMap = new HashMap<>();

    static {
        for (StdAttribute attribute : StdAttribute.values()) {
            attributeMap.put(attribute.name(), attribute);
        }
    }

    /**
     * Creates a standard graph attribute and initialize it to its default
     * value.
     *
     * @param graph the graph.
     * @param attrId the attribute id.
     * @return null if the attribute is not standard (for graphs), the attribute
     * otherwise.
     */
    protected static GraphAttribute<?> createStdGraphAttribute(GraphWithAttributes<?, ?, ?, ?> graph, String attrId) {
        StdAttribute attribute = get(attrId);
        if (attribute == null) {
            // non-standard attribute id.
            return null;
        }

        GraphAttribute<?> createdAttribute;
        switch (attribute) {
            case label:
                createdAttribute = graph.newGraphAttribute(attrId, "");
                createdAttribute.setDescription("The graph label.");
                break;
            case graphics:
                createdAttribute = graph.newGraphAttribute(attrId, "");
                createdAttribute.setDescription("Graphics elements (in svg format) associated with the graph.");
                break;
            case background:
                createdAttribute = graph.newGraphAttribute(attrId, new Color(0, 0, 0, 0));
                createdAttribute.setDescription("Background color of the graph canvas.");
                break;
            default:
                return null;
        }
        createdAttribute.setSleeping();
        return createdAttribute;
    }

    /**
     * Creates a standard node attribute and initialize it to its default value.
     *
     * @param graph the graph.
     * @param attrId the attribute id.
     * @return null if the attribute is not standard (for nodes), the attribute
     * otherwise.
     */
    protected static NodeAttribute<?> createStdNodeAttribute(GraphWithAttributes<?, ?, ?, ?> graph, String attrId) {
        StdAttribute attribute = get(attrId);
        if (attribute == null) {
            // non-standard attribute id.
            return null;
        }

        NodeAttribute<?> createdAttribute;
        switch (attribute) {
            case nodePosition:
                createdAttribute = graph.newNodeAttribute(attrId, new Coordinates(0, 0));
                createdAttribute.setDescription("The position of the node centers in the space.");
                break;
            case nodeSize:
                createdAttribute = graph.newNodeAttribute(attrId, new Coordinates(1, 1));
                createdAttribute.setDescription("The size of the nodes.");
                break;
            case nodeShape:
                createdAttribute = graph.newNodeAttribute(attrId, NodeShape.cuboid);
                createdAttribute.setDescription("The shape of the nodes.");
                break;
            case nodeHeat:
                createdAttribute = graph.newNodeAttribute(attrId, 0.0);
                createdAttribute.setDescription("The heat assigned to each node, for heatMap construction.");
                break;
            case color:
                createdAttribute = graph.newNodeAttribute(attrId, new Color(141, 211, 199));
                createdAttribute.setDescription("The fill color of the nodes.");
                break;
            case label:
                createdAttribute = graph.newNodeAttribute(attrId, "");
                createdAttribute.setDescription("The node labels.");
                break;
            case nodeLevel:
                createdAttribute = graph.newNodeAttribute(attrId, 1);
                createdAttribute.setDescription("The node level.");
                break;
            case labelColor:
                createdAttribute = graph.newNodeAttribute(attrId, Color.BLACK);
                createdAttribute.setDescription("The color of the node labels.");
                break;
            case labelScaling:
                createdAttribute = graph.newNodeAttribute(attrId, 1.0);
                createdAttribute.setDescription("The scaling factor to assign to the node label.");
                break;
            case labelOffset:
                createdAttribute = graph.newNodeAttribute(attrId, new Coordinates(0, 0));
                createdAttribute.setDescription("The label offset with respect to the node center.");
                break;
            case dyPresence:
                createdAttribute = graph.newNodeAttribute(attrId, true);
                createdAttribute.setDescription("Dynamic presence in the graph.");
                break;
            default:
                return null;
        }
        createdAttribute.setSleeping();
        return createdAttribute;
    }

    /**
     * Creates a standard edge attribute and initialize it to its default value.
     *
     * @param graph the graph.
     * @param attrId the attribute id.
     * @return null if the attribute is not standard (for edges), the attribute
     * otherwise.
     */
    protected static EdgeAttribute<?> createStdEdgeAttribute(GraphWithAttributes<?, ?, ?, ?> graph, String attrId) {
        StdAttribute attribute = get(attrId);
        if (attribute == null) {
            // non-standard attribute id.
            return null;
        }

        EdgeAttribute<?> createdAttribute;
        switch (attribute) {
            case edgeWidth:
                createdAttribute = graph.newEdgeAttribute(attrId, 0.2);
                createdAttribute.setDescription("The width of the edges.");
                break;
            case edgeShape:
                createdAttribute = graph.newEdgeAttribute(attrId, EdgeShape.polyline);
                createdAttribute.setDescription("The type of the edges.");
                break;
            case edgePoints:
                createdAttribute = graph.newEdgeAttribute(attrId, new ControlPoints());
                createdAttribute.setDescription("The control points of the edge. The edge shape determines how the control points are used.");
                break;
            case color:
                createdAttribute = graph.newEdgeAttribute(attrId, Color.BLACK);
                createdAttribute.setDescription("The stroke color of the edges.");
                break;
            case label:
                createdAttribute = graph.newEdgeAttribute(attrId, "");
                createdAttribute.setDescription("The edge labels.");
                break;
            case dyPresence:
                createdAttribute = graph.newEdgeAttribute(attrId, true);
                createdAttribute.setDescription("Dynamic presence in the graph.");
                break;
            default:
                return null;
        }
        createdAttribute.setSleeping();
        return createdAttribute;
    }

    /**
     * Checks a newly created graph attribute. if the attribute id is reserved,
     * verifies that the type associated corresponds to the defined matching.
     *
     * @param graph the graph that will contain the attribute.
     * @param attrId the attribute id.
     * @param newAttribute the graph attribute created.
     */
    protected static void checkStdAttributeCompatibility(GraphWithAttributes<?, ?, ?, ?> graph, String attrId, Attribute<?> newAttribute) {
        StdAttribute attribute = get(attrId);
        if (attribute != null) {
            if (graph instanceof DyGraph) {
                try {
                    attribute.matchingClass.cast(((DyAttribute<?>) newAttribute).getDefault().getDefaultValue());
                } catch (ClassCastException e) {
                    throw new ClassCastException("The id \"" + attrId + "\" is that of a standard attribute, but the type assigned is incorrect");
                }
            } else {
                if (reservedForDynamic.contains(attribute)) {
                    throw new IllegalArgumentException("Using reserved dynamic attributes in non-dynamic graphs.");
                }
                try {
                    attribute.matchingClass.cast(newAttribute.getDefault());
                } catch (ClassCastException e) {
                    throw new ClassCastException("The id \"" + attrId + "\" is that of a standard attribute, but the type assigned is incorrect");
                }
            }
        }
    }

    /**
     * Supported node shapes.
     */
    public static enum NodeShape {

        cuboid,
        spheroid;
    }

    /**
     * Supported edge shapes.
     */
    public static enum EdgeShape {

        polyline;
    }

    /**
     * List of control points associated to an edge.
     */
    public static class ControlPoints extends ArrayList<Coordinates> {

        private static final long serialVersionUID = 1L;

        public ControlPoints(Coordinates... points) {
            super(Arrays.asList(points));
        }

        public ControlPoints(Collection<? extends Coordinates> points) {
            super(points);
        }
    }

    /**
     * Collects a group of standard attributes that are involved in the same
     * function.
     */
    public static class StdAttributeSet {

        List<StdAttribute> graphAttributes;
        List<StdAttribute> nodeAttributes;
        List<StdAttribute> edgeAttributes;

        /**
         * Constructs a set of standard attributes.
         *
         * @param graphAttributes the graph attributes.
         * @param nodeAttributes the node attributes.
         * @param edgeAttributes the edge attributes.
         */
        public StdAttributeSet(List<StdAttribute> graphAttributes, List<StdAttribute> nodeAttributes, List<StdAttribute> edgeAttributes) {
            this.graphAttributes = Collections.unmodifiableList(graphAttributes);
            this.nodeAttributes = Collections.unmodifiableList(nodeAttributes);
            this.edgeAttributes = Collections.unmodifiableList(edgeAttributes);
        }

        /**
         * Returns the graph attributes in this set for a given graph.
         *
         * @param graph the graph.
         * @return the standard graph attributes extracted for the graph.
         */
        public Collection<GraphAttribute<?>> graphAttributes(Graph graph) {
            List<GraphAttribute<?>> attributes = new ArrayList<>();
            for (StdAttribute stdAttributeId : graphAttributes) {
                attributes.add(graph.graphAttribute(stdAttributeId));
            }
            return attributes;
        }

        /**
         * Returns the node attributes in this set for a given graph.
         *
         * @param graph the graph.
         * @return the standard node attributes extracted for the graph.
         */
        public Collection<NodeAttribute<?>> nodeAttributes(Graph graph) {
            List<NodeAttribute<?>> attributes = new ArrayList<>();
            for (StdAttribute stdAttributeId : nodeAttributes) {
                attributes.add(graph.nodeAttribute(stdAttributeId));
            }
            return attributes;
        }

        /**
         * Returns the edge attributes in this set for a given graph.
         *
         * @param graph the graph.
         * @return the standard edge attributes extracted for the graph.
         */
        public Collection<EdgeAttribute<?>> edgeAttributes(Graph graph) {
            List<EdgeAttribute<?>> attributes = new ArrayList<>();
            for (StdAttribute stdAttributeId : edgeAttributes) {
                attributes.add(graph.edgeAttribute(stdAttributeId));
            }
            return attributes;
        }
    }
}
