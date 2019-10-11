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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Graph with hierarchical structure and inheritable attributes.
 *
 * @param <G> the specific implementation of the hierarchical graph.
 * @param <U> the kind of graph attribute used.
 * @param <V> the kind of node attribute used.
 * @param <Z> the kind of edge attribute used.
 */
public abstract class GraphWithAttributes<G extends GraphWithAttributes<G, U, V, Z>, U extends GraphAttribute<?>, V extends NodeAttribute<?>, Z extends EdgeAttribute<?>>
        extends GraphWithHierarchy<G> {

    private final Map<String, U> graphAttributeMap = new HashMap<>();
    private final Map<String, V> nodeAttributeMap = new HashMap<>();
    private final Map<String, Z> edgeAttributeMap = new HashMap<>();

    private final Set<Observer.LocalAttributeList> attributesObservers = new HashSet<>();
    private final Set<Attribute<?>> changedAttributes = new HashSet<>();

    // ======================================================================
    // ======== Attribute access ============================================
    // ======================================================================
    //
    /**
     * Returns the attribute with given id. It returns the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    public Attribute<?> attribute(Attribute.Type type, String attrId) {
        return retrieveExistingAttribute(type, attrId);
    }

    /**
     * Returns the graph attribute with given id. It returns the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If called on a standard attribute that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of value handled by the attribute.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    public <T> Attribute<?> graphAttribute(String attrId) {
        if (!hasGraphAttribute(attrId)) {
            StdAttribute.createStdGraphAttribute(this, attrId);
        }
        return attribute(Attribute.Type.graph, attrId);
    }

    /**
     * Returns the node attribute with given id. It returns the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If called on a standard attribute that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of value handled by the attribute.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    public <T> Attribute<?> nodeAttribute(String attrId) {
        if (!hasNodeAttribute(attrId)) {
            StdAttribute.createStdNodeAttribute(this, attrId);
        }
        return attribute(Attribute.Type.node, attrId);
    }

    /**
     * Returns the edge attribute with given id. It returns the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If called on a standard attribute that does not exists, it
     * creates the attribute rather than throwing an exception.
     *
     * @param <T> the type of value handled by the attribute.
     * @param attrId the attribute id.
     * @return the attribute.
     */
    public <T> Attribute<?> edgeAttribute(String attrId) {
        if (!hasEdgeAttribute(attrId)) {
            StdAttribute.createStdEdgeAttribute(this, attrId);
        }
        return attribute(Attribute.Type.edge, attrId);
    }

    // ======================================================================
    // ======== New attribute creation ======================================
    // ======================================================================
    //
    /**
     * Creates and inserts a graph attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of value inserted.
     * @param attrId the attribute id.
     * @param value the value to be assigned to the attribute.
     * @return the new attribute.
     */
    public abstract <T> U newGraphAttribute(String attrId, T value);

    /**
     * Creates and inserts a node attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public abstract <T> V newNodeAttribute(String attrId, T defaultValue);

    /**
     * Creates and inserts an edge attribute in the root graph. If an attribute
     * with the same id already exists in the hierarchy, throws and exception.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public abstract <T> Z newEdgeAttribute(String attrId, T defaultValue);

    // ======================================================================
    // ======== New local attribute creation ================================
    // ======================================================================
    //
    /**
     * Creates and inserts a graph attribute in this graph. Whenever an
     * attribute already exists at this level, and the value to be assigned has
     * a different type than before, throws and exception. Attributes created
     * locally are inaccessible at higher levels of the hierarchy and override
     * higher level attributes with the same id.
     *
     * @param <T> the type of value inserted.
     * @param attrId the attribute id.
     * @param value the value to be assigned.
     * @return the new attribute.
     */
    public abstract <T> U newLocalGraphAttribute(String attrId, T value);

    /**
     * Creates and insert a node attribute in this graph. If an attribute with
     * the same id already exists in this graph, throws and exception.
     * Attributes created locally are inaccessible at higher levels of the
     * hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public abstract <T> V newLocalNodeAttribute(String attrId, T defaultValue);

    /**
     * Creates and insert a edge attribute in this graph. If an attribute with
     * the same id already exists in this graph, throws and exception.
     * Attributes created locally are inaccessible at higher levels of the
     * hierarchy and override higher level attributes with the same id.
     *
     * @param <T> the type of values accepted in the attribute.
     * @param attrId the attribute id.
     * @param defaultValue the default value.
     * @return the new attribute.
     */
    public abstract <T> Z newLocalEdgeAttribute(String attrId, T defaultValue);

    // ======================================================================
    // ======== Access to the attribute collections =========================
    // ======================================================================
    //
    /**
     * Returns all the attributes of a given type in the graph hierarchy.
     *
     * @param <T> the type of attribute returned.
     * @param type the attribute type to be retrieved.
     * @return the local attributes.
     */
    public <T> Map<String, T> attributes(Attribute.Type type) {
        Map<String, T> results = localAttributes(type);
        if (parentGraph() != null) {
            results.putAll(parentGraph().<T>attributes(type));
        }
        return results;
    }

    /**
     * Returns all the graph attributes in the graph hierarchy.
     *
     * @return the graph attributes.
     */
    public Map<String, GraphAttribute<?>> graphAttributes() {
        return attributes(Attribute.Type.graph);
    }

    /**
     * Returns all the node attributes in the graph hierarchy.
     *
     * @return the node attributes.
     */
    public Map<String, NodeAttribute<?>> nodeAttributes() {
        return attributes(Attribute.Type.node);
    }

    /**
     * Returns all the edge attributes in the graph hierarchy.
     *
     * @return the edge attributes.
     */
    public Map<String, EdgeAttribute<?>> edgeAttributes() {
        return attributes(Attribute.Type.edge);
    }

    /**
     * Returns all the local attributes of a given type.
     *
     * @param <T> the type of attribute returned.
     * @param type the attribute type to be retrieved.
     * @return the local attributes.
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> localAttributes(Attribute.Type type) {
        return new HashMap<>((Map<String, T>) getAttributeMap(type));
    }

    /**
     * Returns all the local graph attributes.
     *
     * @return the local graph attributes.
     */
    public Map<String, GraphAttribute<?>> localGraphAttributes() {
        return localAttributes(Attribute.Type.graph);
    }

    /**
     * Returns all the local node attributes.
     *
     * @return the local node attributes.
     */
    public Map<String, NodeAttribute<?>> localNodeAttributes() {
        return localAttributes(Attribute.Type.node);
    }

    /**
     * Returns all the local edge attributes.
     *
     * @return the local edge attributes.
     */
    public Map<String, EdgeAttribute<?>> localEdgeAttributes() {
        return localAttributes(Attribute.Type.edge);
    }

    // ======================================================================
    // ======== Tests to check attribute presence ===========================
    // ======================================================================
    //
    /**
     * Checks if the graph hierarchy has an attribute with given id.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasAttribute(Attribute.Type type, String attrId) {
        return retrieveAttribute(type, attrId) != null;
    }

    /**
     * Checks if the graph hierarchy has a graph attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasGraphAttribute(String attrId) {
        return hasAttribute(Attribute.Type.graph, attrId);
    }

    /**
     * Checks if the graph hierarchy has a standard graph attribute defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasGraphAttribute(StdAttribute attribute) {
        return hasGraphAttribute(attribute.name());
    }

    /**
     * Checks if the graph hierarchy has a node attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasNodeAttribute(String attrId) {
        return hasAttribute(Attribute.Type.node, attrId);
    }

    /**
     * Checks if the graph hierarchy has a standard node attribute defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasNodeAttribute(StdAttribute attribute) {
        return hasNodeAttribute(attribute.name());
    }

    /**
     * Checks if the graph hierarchy has an edge attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasEdgeAttribute(String attrId) {
        return hasAttribute(Attribute.Type.edge, attrId);
    }

    /**
     * Checks if the graph hierarchy has a standard edge attribute defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasEdgeAttribute(StdAttribute attribute) {
        return hasEdgeAttribute(attribute.name());
    }

    // ======================================================================
    // ======== Tests to check local attribute presence =====================
    // ======================================================================
    //
    /**
     * Checks if the graph has an attribute with given id.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalAttribute(Attribute.Type type, String attrId) {
        return getAttributeMap(type).containsKey(attrId);
    }

    /**
     * Checks if this graph has a local graph attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalGraphAttribute(String attrId) {
        return hasLocalAttribute(Attribute.Type.graph, attrId);
    }

    /**
     * Checks if the graph hierarchy has a local standard graph attribute
     * defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalGraphAttribute(StdAttribute attribute) {
        return hasLocalGraphAttribute(attribute.name());
    }

    /**
     * Checks if this graph has a local node attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalNodeAttribute(String attrId) {
        return hasLocalAttribute(Attribute.Type.node, attrId);
    }

    /**
     * Checks if the graph hierarchy has a local standard node attribute
     * defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalNodeAttribute(StdAttribute attribute) {
        return hasLocalNodeAttribute(attribute.name());
    }

    /**
     * Checks if this graph has a local edge attribute with given id.
     *
     * @param attrId the attribute id.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalEdgeAttribute(String attrId) {
        return hasLocalAttribute(Attribute.Type.edge, attrId);
    }

    /**
     * Checks if the graph hierarchy has a local standard edge attribute
     * defined.
     *
     * @param attribute the standard attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public boolean hasLocalEdgeAttribute(StdAttribute attribute) {
        return hasLocalEdgeAttribute(attribute.name());
    }

    // ======================================================================
    // ======== Attribute removal ===========================================
    // ======================================================================
    //
    /**
     * Removes an attribute with given id. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param type the type of attribute.
     * @param attrId the attribute id.
     */
    public void removeAttribute(Attribute.Type type, String attrId) {
        if (hasLocalAttribute(type, attrId)) {
            Map<String, ? extends Attribute<?>> attributeMap = getAttributeMap(type);
            Attribute<?> attributeRemoved = attributeMap.remove(attrId);
            changedAttributes.add(attributeRemoved);
            notifyObservers();
        } else if (parentGraph() != null) {
            parentGraph().removeAttribute(type, attrId);
        } else {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" does not exist");
        }
    }

    /**
     * Removes a graph attribute with given id. It removes the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If such attribute does not exist, throws an exception.
     *
     * @param attrId the attribute id.
     */
    public void removeGraphAttribute(String attrId) {
        removeAttribute(Attribute.Type.graph, attrId);
    }

    /**
     * Removes a standard graph attribute. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param attribute the standard attribute.
     */
    public void removeGraphAttribute(StdAttribute attribute) {
        removeGraphAttribute(attribute.name());
    }

    /**
     * Removes a node attribute with given id. It removes the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If such attribute does not exist, throws an exception.
     *
     * @param attrId the attribute id.
     */
    public void removeNodeAttribute(String attrId) {
        removeAttribute(Attribute.Type.node, attrId);
    }

    /**
     * Removes a standard node attribute. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param attribute the standard attribute.
     */
    public void removeNodeAttribute(StdAttribute attribute) {
        removeNodeAttribute(attribute.name());
    }

    /**
     * Removes an edge attribute with given id. It removes the first attribute
     * with given id in the path from this graph and its root in the graph
     * hierarchy. If such attribute does not exist, throws an exception.
     *
     * @param attrId the attribute id.
     */
    public void removeEdgeAttribute(String attrId) {
        removeAttribute(Attribute.Type.edge, attrId);
    }

    /**
     * Removes a standard edge attribute. It removes the first attribute with
     * given id in the path from this graph and its root in the graph hierarchy.
     * If such attribute does not exist, throws an exception.
     *
     * @param attribute the standard attribute.
     */
    public void removeEdgeAttribute(StdAttribute attribute) {
        removeEdgeAttribute(attribute.name());
    }

    // ======================================================================
    // ======== Attribute inception =========================================
    // ======================================================================
    //
    /**
     * Creates and insert an attribute in the root graph. If an attribute with
     * the same id already exists in the hierarchy, throws and exception.
     *
     * @param type the attribute type.
     * @param attrId the attribute id.
     * @param attribute the attribute to be assigned.
     */
    public void setAttribute(Attribute.Type type, String attrId, Attribute<?> attribute) {
        if (hasAttribute(type, attrId)) {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" already exists");
        }
        rootGraph().setLocalAttribute(type, attrId, attribute);
    }

    /**
     * Sets an attribute in this graph. If an attribute with the same id already
     * exists in this graph, throws and exception. Attributes created locally
     * are inaccessible at higher levels of the hierarchy and override higher
     * level attributes with the same id.
     *
     * @param type the attribute type.
     * @param attrId the attribute id.
     * @param attribute the attribute to be inserted.
     */
    @SuppressWarnings("unchecked")
    public void setLocalAttribute(Attribute.Type type, String attrId, Attribute<?> attribute) {
        Rules.checkId(attrId);
        Map<String, Attribute<?>> attributeMap = (Map<String, Attribute<?>>) getAttributeMap(type);
        if (attributeMap.containsKey(attrId)) {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" already exists");
        }
        StdAttribute.checkStdAttributeCompatibility(this, attrId, attribute);
        attributeMap.put(attrId, attribute);
        changedAttributes.add(attribute);
        notifyObservers();
    }

    // ======================================================================
    // ======== Service methods =============================================
    // ======================================================================
    //
    /**
     * Returns the attribute map related to the specified type.
     *
     * @param type the attribute type.
     * @return the related map.
     */
    private Map<String, ? extends Attribute<?>> getAttributeMap(Attribute.Type type) {
        switch (type) {
            case graph:
                return graphAttributeMap;
            case node:
                return nodeAttributeMap;
            case edge:
                return edgeAttributeMap;
            default:
                throw new UnsupportedOperationException("The attribute type " + type.name() + " is not supported");
        }
    }

    /**
     * Retrieve an attribute from a graph hierarchy.
     *
     * @param type the type of attribute to be retrieved.
     * @param attrId the attribute id.
     * @return the desired attribute, or null if an attribute with given id does
     * not exist.
     */
    Attribute<?> retrieveAttribute(Attribute.Type type, String attrId) {
        Map<String, ?> attributeMap = getAttributeMap(type);
        if (attributeMap.containsKey(attrId)) {
            return (Attribute) attributeMap.get(attrId);
        } else {
            return parentGraph() != null ? parentGraph().retrieveAttribute(type, attrId) : null;
        }
    }

    /**
     * Retrieve an existing attribute from a graph hierarchy. Throws exception
     * if the attribute does not exist.
     *
     * @param type the type of attribute to be retrieved.
     * @param attrId the attribute id.
     * @return the desired attribute.
     */
    private Attribute<?> retrieveExistingAttribute(Attribute.Type type, String attrId) {
        Attribute<?> attribute = retrieveAttribute(type, attrId);
        if (attribute == null) {
            throw new IllegalArgumentException("The attribute \"" + attrId + "\" does not exist");
        }
        return attribute;
    }

    // ======================================================================
    // ======== Observers =============================================
    // ======================================================================
    //
    /**
     * Register a graph attribute observer.
     *
     * @param observer the attribute observer.
     */
    public void registerObserver(Observer.LocalAttributeList observer) {
        attributesObservers.add(observer);
    }

    /**
     * Unregister a graph attribute observer.
     *
     * @param observer the attribute observer.
     */
    public void unregisterObserver(Observer.LocalAttributeList observer) {
        attributesObservers.remove(observer);
    }

    @Override
    protected void notifyObservers() {
        if (bulkNotify) {
            return;
        }
        super.notifyObservers();
        for (Observer.LocalAttributeList observer : attributesObservers) {
            if (!changedAttributes.isEmpty()) {
                observer.theseLocalAttributesAddedOrRemoved(Collections.unmodifiableCollection(changedAttributes));
            }
        }
        changedAttributes.clear();
    }
}
