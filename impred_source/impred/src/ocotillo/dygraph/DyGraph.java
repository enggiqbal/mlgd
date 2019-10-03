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
package ocotillo.dygraph;

import java.util.Map;
import ocotillo.graph.Attribute;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.GraphWithAttributes;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;

/**
 * A dynamic graph.
 */
public class DyGraph extends GraphWithAttributes<DyGraph, DyGraphAttribute<?>, DyNodeAttribute<?>, DyEdgeAttribute<?>> {

    @Override
    protected DyGraph createGraph() {
        return new DyGraph();
    }

    // ======================================================================
    // ======== Attribute access ============================================
    // ======================================================================
    //
    /**
     * Returns a standard graph attribute. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If called on a standard attribute (for graphs) that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attribute the standard attribute.
     * @return the attribute.
     */
    public <T> DyGraphAttribute<T> graphAttribute(StdAttribute attribute) {
        return graphAttribute(attribute.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DyGraphAttribute<T> graphAttribute(String attrId) {
        return (DyGraphAttribute<T>) super.graphAttribute(attrId);
    }

    /**
     * Returns a standard node attribute. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If called on a standard attribute (for nodes) that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attribute the standard attribute.
     * @return the attribute.
     */
    public <T> DyNodeAttribute<T> nodeAttribute(StdAttribute attribute) {
        return nodeAttribute(attribute.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DyNodeAttribute<T> nodeAttribute(String attrId) {
        return (DyNodeAttribute<T>) super.nodeAttribute(attrId);
    }

    /**
     * Returns a standard edge attribute. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If called on a standard attribute (for edges) that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of the returned value.
     * @param attribute the standard attribute.
     * @return the attribute.
     */
    public <T> DyEdgeAttribute<T> edgeAttribute(StdAttribute attribute) {
        return edgeAttribute(attribute.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DyEdgeAttribute<T> edgeAttribute(String attrId) {
        return (DyEdgeAttribute<T>) super.edgeAttribute(attrId);
    }

    // ======================================================================
    // ======== New attribute creation ======================================
    // ======================================================================
    //
    /**
     * Creates and inserts a standard graph attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of value inserted.
     * @param attribute the standard attribute.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> DyGraphAttribute<T> newGraphAttribute(StdAttribute attribute, Evolution<T> value) {
        return newGraphAttribute(attribute.name(), value);
    }

    /**
     * Creates and inserts a graph attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of value inserted.
     * @param attrId the attribute id.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> DyGraphAttribute<T> newGraphAttribute(String attrId, Evolution<T> value) {
        DyGraphAttribute<T> attribute = new DyGraphAttribute<>(value);
        setAttribute(Attribute.Type.graph, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard node attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyNodeAttribute<T> newNodeAttribute(StdAttribute attribute, Evolution<T> defaultValue) {
        return newNodeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Creates and inserts a node attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyNodeAttribute<T> newNodeAttribute(String attrId, Evolution<T> defaultValue) {
        DyNodeAttribute<T> attribute = new DyNodeAttribute<>(defaultValue);
        setAttribute(Attribute.Type.node, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard edge attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyEdgeAttribute<T> newEdgeAttribute(StdAttribute attribute, Evolution<T> defaultValue) {
        return newEdgeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Creates and inserts an edge attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyEdgeAttribute<T> newEdgeAttribute(String attrId, Evolution<T> defaultValue) {
        DyEdgeAttribute<T> attribute = new DyEdgeAttribute<>(defaultValue);
        setAttribute(Attribute.Type.edge, attrId, attribute);
        return attribute;
    }

    // ======================================================================
    // ======== New attribute creation - w/o evolution ======================
    // ======================================================================
    //
    /**
     * Creates and inserts a standard graph attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of value inserted.
     * @param attribute the standard attribute.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> DyGraphAttribute<T> newGraphAttribute(StdAttribute attribute, T value) {
        return newGraphAttribute(attribute.name(), value);
    }

    @Override
    public <T> DyGraphAttribute<T> newGraphAttribute(String attrId, T value) {
        DyGraphAttribute<T> attribute = new DyGraphAttribute<>(value);
        setAttribute(Attribute.Type.graph, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard node attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyNodeAttribute<T> newNodeAttribute(StdAttribute attribute, T defaultValue) {
        return newNodeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> DyNodeAttribute<T> newNodeAttribute(String attrId, T defaultValue) {
        DyNodeAttribute<T> attribute = new DyNodeAttribute<>(defaultValue);
        setAttribute(Attribute.Type.node, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a standard edge attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyEdgeAttribute<T> newEdgeAttribute(StdAttribute attribute, T defaultValue) {
        return newEdgeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> DyEdgeAttribute<T> newEdgeAttribute(String attrId, T defaultValue) {
        DyEdgeAttribute<T> attribute = new DyEdgeAttribute<>(defaultValue);
        setAttribute(Attribute.Type.edge, attrId, attribute);
        return attribute;
    }

    // ======================================================================
    // ======== New local attribute creation ================================
    // ======================================================================
    //
    /**
     * Creates and inserts a local standard graph attribute in the root graph.
     * If an attribute with the same id already exists in the hierarchy, throws
     * and exception.
     *
     * @param <T> the type of value inserted.
     * @param attribute the standard attribute.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> DyGraphAttribute<T> newLocalGraphAttribute(StdAttribute attribute, Evolution<T> value) {
        return newLocalGraphAttribute(attribute.name(), value);
    }

    /**
     * Creates and inserts a local graph attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of value inserted.
     * @param attrId the attribute id.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public <T> DyGraphAttribute<T> newLocalGraphAttribute(String attrId, Evolution<T> value) {
        DyGraphAttribute<T> attribute = new DyGraphAttribute<>(value);
        setLocalAttribute(Attribute.Type.graph, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a local standard node attribute in the root graph. If
     * an attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyNodeAttribute<T> newLocalNodeAttribute(StdAttribute attribute, Evolution<T> defaultValue) {
        return newLocalNodeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Creates and inserts a local node attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyNodeAttribute<T> newLocalNodeAttribute(String attrId, Evolution<T> defaultValue) {
        DyNodeAttribute<T> attribute = new DyNodeAttribute<>(defaultValue);
        setLocalAttribute(Attribute.Type.node, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and inserts a local standard edge attribute in the root graph. If
     * an attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyEdgeAttribute<T> newLocalEdgeAttribute(StdAttribute attribute, Evolution<T> defaultValue) {
        return newLocalEdgeAttribute(attribute.name(), defaultValue);
    }

    /**
     * Creates and inserts a local edge attribute in the root graph. If an
     * attribute with the same id already exists in the hierarchy, throws and
     * exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyEdgeAttribute<T> newLocalEdgeAttribute(String attrId, Evolution<T> defaultValue) {
        DyEdgeAttribute<T> attribute = new DyEdgeAttribute<>(defaultValue);
        setLocalAttribute(Attribute.Type.edge, attrId, attribute);
        return attribute;
    }

    // ======================================================================
    // ======== New local attribute creation - w/o evolution ================
    // ======================================================================
    //
    /**
     * Creates and inserts a standard graph attribute in this graph. Whenever an
     * attribute already exists at this level, and the value to be assigned has
     * a different type than before, throws and exception. Attributes created
     * locally are inaccessible at higher levels of the hierarchy and override
     * higher level attributes with the same id.
     *
     * @param <T> the type of value inserted.
     * @param attribute the standard attribute.
     * @param value the value to be assigned.
     * @return the new attribute.
     */
    public <T> DyGraphAttribute<T> newLocalGraphAttribute(StdAttribute attribute, T value) {
        return newLocalGraphAttribute(attribute.name(), value);
    }

    @Override
    public <T> DyGraphAttribute<T> newLocalGraphAttribute(String attrId, T value) {
        DyGraphAttribute<T> attribute = new DyGraphAttribute<>(value);
        setLocalAttribute(Attribute.Type.graph, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and insert a standard node attribute in this graph. If an
     * attribute with the same id already exists in this graph, throws and
     * exception. Attributes created locally are inaccessible at higher levels
     * of the hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyNodeAttribute<T> newLocalNodeAttribute(StdAttribute attribute, T defaultValue) {
        return newLocalNodeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> DyNodeAttribute<T> newLocalNodeAttribute(String attrId, T defaultValue) {
        DyNodeAttribute<T> attribute = new DyNodeAttribute<>(defaultValue);
        setLocalAttribute(Attribute.Type.node, attrId, attribute);
        return attribute;
    }

    /**
     * Creates and insert a standard edge attribute in this graph. If an
     * attribute with the same id already exists in this graph, throws and
     * exception. Attributes created locally are inaccessible at higher levels
     * of the hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attribute the standard attribute.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public <T> DyEdgeAttribute<T> newLocalEdgeAttribute(StdAttribute attribute, T defaultValue) {
        return newLocalEdgeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> DyEdgeAttribute<T> newLocalEdgeAttribute(String attrId, T defaultValue) {
        DyEdgeAttribute<T> attribute = new DyEdgeAttribute<>(defaultValue);
        setLocalAttribute(Attribute.Type.edge, attrId, attribute);
        return attribute;
    }

    // ======================================================================
    // ======== Dynamic only-methods ========================================
    // ======================================================================
    //
    /**
     * Returns a static graph of this dynamic graph at a given time.
     *
     * @param time the time of the snapshot.
     * @return the static graph at that time.
     */
    public Graph snapshotAt(double time) {
        Graph snapshot = new Graph();
        for (Node node : nodes()) {
            if (this.<Boolean>nodeAttribute(StdAttribute.dyPresence).get(node).valueAt(time)) {
                snapshot.add(node);
            }
        }
        for (Edge edge : edges()) {
            if (snapshot.has(edge.source()) && snapshot.has(edge.target())
                    && this.<Boolean>edgeAttribute(StdAttribute.dyPresence).get(edge).valueAt(time)) {
                snapshot.add(edge);
            }
        }
        snapAttributes(snapshot, time, graphAttributes());
        snapAttributes(snapshot, time, nodeAttributes());
        snapAttributes(snapshot, time, edgeAttributes());
        return snapshot;
    }

    /**
     * Takes a snapshot of a map of dynamic attributes.
     *
     * @param snapshot the snapshot graph.
     * @param time the time of the snapshot.
     * @param attributeMap the map of attributes.
     */
    private void snapAttributes(Graph snapshot, double time, Map<String, ?> attributeMap) {
        for (Map.Entry<String, ?> entry : attributeMap.entrySet()) {
            String attrId = entry.getKey();
            if (!StdAttribute.isReservedForDynamic(attrId)) {
                DyAttribute<?> dyAttribute = (DyAttribute<?>) entry.getValue();
                Attribute<?> snapshotAttribute = dyAttribute.snapshotAt(time);
                snapshot.setAttribute(dyAttribute.getAttributeType(), attrId, snapshotAttribute);
            }
        }
    }
}
