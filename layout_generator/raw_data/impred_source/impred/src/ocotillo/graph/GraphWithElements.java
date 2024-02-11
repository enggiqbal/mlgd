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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A basic graph that only handles nodes and edges, with no attribute or graph
 * hierarchy.
 */
public class GraphWithElements {

    private final Map<String, Node> nodeMap = new HashMap<>();
    private final Map<String, Edge> edgeMap = new HashMap<>();

    private final Map<Node, Set<Edge>> incomingMap = new HashMap<>();
    private final Map<Node, Set<Edge>> outgoingMap = new HashMap<>();

    private final Set<Element> changedElements = new HashSet<>();
    private final Set<Observer.GraphElements> elementObservers = new HashSet<>();

    static long nodeIdIndex = 0;
    static long edgeIdIndex = 0;

    boolean bulkNotify = false;

    /**
     * Returns the graph nodes.
     *
     * @return the graph nodes.
     */
    public Collection<Node> nodes() {
        return Collections.unmodifiableCollection(nodeMap.values());
    }

    /**
     * Creates and inserts a new node.
     *
     * @return the new node.
     */
    public Node newNode() {
        return newNode(null);
    }

    /**
     * Creates and inserts a new node.
     *
     * @param id the node id.
     * @return the new node.
     */
    public Node newNode(String id) {
        if (id == null) {
            do {
                id = (++nodeIdIndex) + "n";
            } while (hasNode(id));
        }

        Node node = new Node(id);
        add(node);
        return node;
    }

    /**
     * Checks if a node with given id is part of the graph.
     *
     * @param id the node id.
     * @return true if the node is contained in the graph, false otherwise.
     */
    public boolean hasNode(String id) {
        return nodeMap.containsKey(id);
    }

    /**
     * Returns a node with given id.
     *
     * @param id the node id.
     * @return the node having given id.
     */
    public Node getNode(String id) {
        return nodeMap.get(id);
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the number of graph nodes.
     */
    public int nodeCount() {
        return nodeMap.size();
    }

    /**
     * Returns the graph edges.
     *
     * @return the graph edges.
     */
    public Collection<Edge> edges() {
        return Collections.unmodifiableCollection(edgeMap.values());
    }

    /**
     * Creates and inserts a new edge.
     *
     * @param source the edge source.
     * @param target the edge target.
     * @return the new edge.
     */
    public Edge newEdge(Node source, Node target) {
        return newEdge(null, source, target);
    }

    /**
     * Creates and inserts a new edge.
     *
     * @param id the edge id.
     * @param source the edge source.
     * @param target the edge target.
     * @return the new edge.
     */
    public Edge newEdge(String id, Node source, Node target) {
        if (id == null) {
            do {
                id = (++edgeIdIndex) + "e";
            } while (hasEdge(id));
        }

        Edge edge = new Edge(id, source, target);
        add(edge);
        return edge;
    }

    /**
     * Checks if an edge with given id is part of the graph.
     *
     * @param id the edge id.
     * @return true if the edge is contained in the graph, false otherwise.
     */
    public boolean hasEdge(String id) {
        return edgeMap.containsKey(id);
    }

    /**
     * Returns an edge with given id.
     *
     * @param id the edge id.
     * @return the edge having given id.
     */
    public Edge getEdge(String id) {
        return edgeMap.get(id);
    }

    /**
     * Returns the number of edges in the graph.
     *
     * @return the number of graph edges.
     */
    public int edgeCount() {
        return edgeMap.size();
    }

    /**
     * Checks if an element is contained in the graph.
     *
     * @param element the element.
     * @return true if the element is contained in the graph, false otherwise.
     */
    public boolean has(Element element) {
        if (element instanceof Node) {
            return hasNode(element.id());
        }
        if (element instanceof Edge) {
            return hasEdge(element.id());
        }
        return false;
    }

    /**
     * Adds an already created element to a graph. When adding an element that
     * already exists, or when inserting an edge that does not have both
     * extremities in the graph, throws an exception.
     *
     * @param element the element to be added.
     */
    public void add(Element element) {
        addImplementation(element, false);
    }

    /**
     * Adds an already created element to a graph. When adding an element that
     * already exists, does nothing. When inserting an edge that does not have
     * both extremities in the graph, inserts these nodes in the graph.
     *
     * @param element the element to be added.
     */
    public void forcedAdd(Element element) {
        addImplementation(element, true);
    }

    /**
     * Private method that actually performs the operation add.
     *
     * @param element the element to be added.
     * @param forced whether to force or not the operation.
     */
    void addImplementation(Element element, Boolean forced) {
        if (!shouldAddBePerformed(element, forced)) {
            return;
        }

        if (element instanceof Node) {
            Node node = (Node) element;
            nodeMap.put(node.id(), node);
            incomingMap.put(node, new HashSet<>());
            outgoingMap.put(node, new HashSet<>());
        }

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            addImplementation(edge.source(), true);
            addImplementation(edge.target(), true);

            edgeMap.put(edge.id(), edge);
            outgoingMap.get(edge.source()).add(edge);
            incomingMap.get(edge.target()).add(edge);
        }

        changedElements.add(element);
        notifyObservers();
    }

    /**
     * Checks whether the add operation should be performed.
     *
     * @param element the element to be added.
     * @param forced whether to force or not the operation.
     * @return true if the operation should be performed, false or exception
     * otherwise.
     */
    boolean shouldAddBePerformed(Element element, Boolean forced) {
        if (has(element)) {
            if (forced) {
                return false;
            } else {
                throw new IllegalArgumentException("Adding an element that is already in the graph");
            }
        }

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            if (!forced && (!has(edge.source()) || !has(edge.target()))) {
                throw new IllegalArgumentException("Adding an edge whose extremities are not in the graph: (" + edge.source() + ",  " + edge.target() + ") element: " + element);
            }
        }

        return true;
    }

    /**
     * Removes an element from a graph. When removing an element that is not in
     * the graph, or when removing a node that still has incident edges, throws
     * an exception.
     *
     * @param element the element to be removed.
     */
    public void remove(Element element) {
        removeImplementation(element, false);
    }

    /**
     * Removes an element from a graph. When removing an element that is not in
     * the graph, does nothing. When removing a node that still has incident
     * edges, removes all of them as well.
     *
     * @param element the element to be removed.
     */
    public void forcedRemove(Element element) {
        removeImplementation(element, true);
    }

    /**
     * Private method that actually performs the operation remove.
     *
     * @param element the element to be removed.
     * @param forced whether to force or not the operation.
     */
    void removeImplementation(Element element, Boolean forced) {
        if (!shouldRemoveBePerformed(element, forced)) {
            return;
        }

        if (element instanceof Node) {
            Node node = (Node) element;
            for (Edge edge : incomingMap.get(node)) {
                removeImplementation(edge, true);
            }
            for (Edge edge : outgoingMap.get(node)) {
                removeImplementation(edge, true);
            }
            nodeMap.remove(node.id());
        }

        if (element instanceof Edge) {
            Edge edge = (Edge) element;
            edgeMap.remove(edge.id());
            outgoingMap.get(edge.source()).remove(edge);
            incomingMap.get(edge.target()).remove(edge);
        }

        changedElements.add(element);
        notifyObservers();
    }

    /**
     * Checks whether the remove operation should be performed.
     *
     * @param element the element to be added.
     * @param forced whether to force or not the operation.
     * @return true if the operation should be performed, false or exception
     * otherwise.
     */
    private boolean shouldRemoveBePerformed(Element element, Boolean forced) {
        if (!has(element)) {
            if (forced) {
                return false;
            } else {
                throw new IllegalArgumentException("Removing an element that is not in the graph.");
            }
        }

        if (element instanceof Node) {
            Node node = (Node) element;
            if (!forced && degree(node) > 0) {
                throw new IllegalArgumentException("Removing a node that still has incident edges.");
            }
        }

        return true;
    }

    /**
     * Returns the number of incoming edges on a given node.
     *
     * @param node a node.
     * @return the incoming degree of the node.
     */
    public int inDegree(Node node) {
        return incomingMap.get(node).size();
    }

    /**
     * Returns the number of outgoing edges from a given node.
     *
     * @param node a node.
     * @return the outgoing degree of the node.
     */
    public int outDegree(Node node) {
        return outgoingMap.get(node).size();
    }

    /**
     * Returns the number of incoming and outgoing edges from a given node.
     *
     * @param node a node.
     * @return the total degree of the node.
     */
    public int degree(Node node) {
        return inDegree(node) + outDegree(node);
    }

    /**
     * Returns the incoming edges into a node.
     *
     * @param node a node.
     * @return the incoming edges into a node.
     */
    public Collection<Edge> inEdges(Node node) {
        return Collections.unmodifiableCollection(incomingMap.get(node));
    }

    /**
     * Returns the outgoing edges from a node.
     *
     * @param node a node.
     * @return the outgoing edges from a node
     */
    public Collection<Edge> outEdges(Node node) {
        return Collections.unmodifiableCollection(outgoingMap.get(node));
    }

    /**
     * Returns the incoming and outgoing edges from a node.
     *
     * @param node a node.
     * @return the incoming and outgoing edges from a node.
     */
    public Collection<Edge> inOutEdges(Node node) {
        Set<Edge> edges = new HashSet<>();
        edges.addAll(inEdges(node));
        edges.addAll(outEdges(node));
        return edges;
    }

    /**
     * Returns the edges leaving from an node and incoming into another.
     *
     * @param source the source of the edges.
     * @param target the target of the edges.
     * @return all the edges from source to target.
     */
    public Collection<Edge> fromToEdges(Node source, Node target) {
        Set<Edge> edges = new HashSet<>(outEdges(source));
        edges.retainAll(inEdges(target));
        return edges;
    }

    /**
     * Returns a random edge leaving one node and incoming into another, if it
     * exists. Return null if there is not such edge.
     *
     * @param source the source of the edges.
     * @param target the target of the edges.
     * @return an edge from source to target, of null if there is no such edge.
     */
    public Edge fromToEdge(Node source, Node target) {
        Collection<Edge> edges = fromToEdges(source, target);
        if (edges.isEmpty()) {
            return null;
        } else {
            return edges.iterator().next();
        }
    }

    /**
     * Returns all edges between the nodes.
     *
     * @param first the first node;
     * @param second the second node;
     * @return all edges between the nodes.
     */
    public Collection<Edge> betweenEdges(Node first, Node second) {
        Set<Edge> firstToSecond = new HashSet<>(outEdges(first));
        firstToSecond.retainAll(inEdges(second));
        Set<Edge> secondToFirst = new HashSet<>(outEdges(second));
        secondToFirst.retainAll(inEdges(first));
        firstToSecond.addAll(secondToFirst);
        return firstToSecond;
    }

    /**
     * Returns a random edge between the two nodes, if it exists. Return null if
     * there is not such edge.
     *
     * @param first the first node;
     * @param second the second node;
     * @return an edge between the nodes, of null if there is no such edge.
     */
    public Edge betweenEdge(Node first, Node second) {
        Collection<Edge> edges = betweenEdges(first, second);
        if (edges.isEmpty()) {
            return null;
        } else {
            return edges.iterator().next();
        }
    }

    /**
     * Register a graph element observer.
     *
     * @param observer the element observer.
     */
    public void registerObserver(Observer.GraphElements observer) {
        elementObservers.add(observer);
    }

    /**
     * Unregister a graph element observer.
     *
     * @param observer the element observer.
     */
    public void unregisterObserver(Observer.GraphElements observer) {
        elementObservers.remove(observer);
    }

    /**
     * Starts a bulk notification. All notifications are suspended until the
     * bulk notification end command, at which point all notifications are
     * transmitted in block.
     */
    public void startBulkNotification() {
        bulkNotify = true;
    }

    /**
     * Ends a bulk notification. All notifications stacked up during the bulk
     * notification interval are transmitted in block.
     */
    public void stopBulkNotification() {
        bulkNotify = false;
        notifyObservers();
    }

    /**
     * Notifies the graph observers.
     */
    protected void notifyObservers() {
        if (bulkNotify) {
            return;
        }
        for (Observer.GraphElements observer : elementObservers) {
            if (!changedElements.isEmpty()) {
                observer.theseElementsChanged(Collections.unmodifiableCollection(changedElements));
            }
        }
        changedElements.clear();
    }
}
