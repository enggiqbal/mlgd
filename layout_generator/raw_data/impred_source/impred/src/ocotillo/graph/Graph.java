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

/**
 * A graph.
 */
public class Graph extends GraphWithAttributes<Graph, GraphAttribute<?>, NodeAttribute<?>, EdgeAttribute<?>> {

    @Override
    protected Graph createGraph() {
        return new Graph();
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
    public <T> GraphAttribute<T> graphAttribute(StdAttribute attribute) {
        return graphAttribute(attribute.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> GraphAttribute<T> graphAttribute(String attrId) {
        return (GraphAttribute<T>) super.graphAttribute(attrId);
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
    public <T> NodeAttribute<T> nodeAttribute(StdAttribute attribute) {
        return nodeAttribute(attribute.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> NodeAttribute<T> nodeAttribute(String attrId) {
        return (NodeAttribute<T>) super.nodeAttribute(attrId);
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
    public <T> EdgeAttribute<T> edgeAttribute(StdAttribute attribute) {
        return edgeAttribute(attribute.name());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> EdgeAttribute<T> edgeAttribute(String attrId) {
        return (EdgeAttribute<T>) super.edgeAttribute(attrId);
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
    public <T> GraphAttribute<T> newGraphAttribute(StdAttribute attribute, T value) {
        return newGraphAttribute(attribute.name(), value);
    }

    @Override
    public <T> GraphAttribute<T> newGraphAttribute(String attrId, T value) {
        GraphAttribute<T> attribute = new GraphAttribute<>(value);
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
    public <T> NodeAttribute<T> newNodeAttribute(StdAttribute attribute, T defaultValue) {
        return newNodeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> NodeAttribute<T> newNodeAttribute(String attrId, T defaultValue) {
        NodeAttribute<T> attribute = new NodeAttribute<>(defaultValue);
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
    public <T> EdgeAttribute<T> newEdgeAttribute(StdAttribute attribute, T defaultValue) {
        return newEdgeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> EdgeAttribute<T> newEdgeAttribute(String attrId, T defaultValue) {
        EdgeAttribute<T> attribute = new EdgeAttribute<>(defaultValue);
        setAttribute(Attribute.Type.edge, attrId, attribute);
        return attribute;
    }

    // ======================================================================
    // ======== New local attribute creation ================================
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
    public <T> GraphAttribute<T> newLocalGraphAttribute(StdAttribute attribute, T value) {
        return newLocalGraphAttribute(attribute.name(), value);
    }

    @Override
    public <T> GraphAttribute<T> newLocalGraphAttribute(String attrId, T value) {
        GraphAttribute<T> attribute = new GraphAttribute<>(value);
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
    public <T> NodeAttribute<T> newLocalNodeAttribute(StdAttribute attribute, T defaultValue) {
        return newLocalNodeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> NodeAttribute<T> newLocalNodeAttribute(String attrId, T defaultValue) {
        NodeAttribute<T> attribute = new NodeAttribute<>(defaultValue);
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
    public <T> EdgeAttribute<T> newLocalEdgeAttribute(StdAttribute attribute, T defaultValue) {
        return newLocalEdgeAttribute(attribute.name(), defaultValue);
    }

    @Override
    public <T> EdgeAttribute<T> newLocalEdgeAttribute(String attrId, T defaultValue) {
        EdgeAttribute<T> attribute = new EdgeAttribute<>(defaultValue);
        setLocalAttribute(Attribute.Type.edge, attrId, attribute);
        return attribute;
    }
}
