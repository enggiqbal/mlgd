/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ocotillo.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A graph that can be inserted in a hierarchy.
 *
 * @param <T> the specific implementation of the hierarchical graph.
 */
public abstract class GraphWithHierarchy<T extends GraphWithHierarchy<?>> extends GraphWithElements {

    T parentGraph;
    private final Set<T> subGraphs = new HashSet<>();

    private final Set<Observer.GraphHierarchy> hierarchyObservers = new HashSet<>();
    private final Set<T> changedSubGraphs = new HashSet<>();

    /**
     * Generates a new empty graph.
     *
     * @return the subgraph.
     */
    protected abstract T createGraph();

    /**
     * Returns the parent graph.
     *
     * @return the parent graph, or null if the graph is the root.
     */
    public T parentGraph() {
        return parentGraph;
    }

    /**
     * Returns the root graph.
     *
     * @return the root graph, which is the graph itself if it is root.
     */
    @SuppressWarnings("unchecked")
    public T rootGraph() {
        if (parentGraph != null) {
            return (T) parentGraph.rootGraph();
        }
        return (T) this;
    }

    /**
     * Returns the subgraphs of this graph.
     *
     * @return the subgraphs of this graph.
     */
    public Collection<T> subGraphs() {
        return Collections.unmodifiableCollection(subGraphs);
    }

    /**
     * Sets the parent graph in a newly created subgraph. It is not supposed to
     * be used outside this class, but cannot be made private.
     *
     * @param parentGraph the parent graph.
     */
    @SuppressWarnings("unchecked")
    void setParentGraph(Object parentGraph) {
        this.parentGraph = (T) parentGraph;
    }

    /**
     * Creates and inserts a new subgraph.
     *
     * @return the new subgraph.
     */
    public T newSubGraph() {
        T subGraph = createGraph();
        subGraph.setParentGraph(this);
        subGraphs.add(subGraph);

        changedSubGraphs.add(subGraph);
        notifyObservers();
        return subGraph;
    }

    /**
     * Creates and inserts a new subgraph.
     *
     * @param nodes the subset of parent nodes to be added.
     * @param edges the subset of parent edges to be added.
     * @return the new subgraph.
     */
    public T newSubGraph(Collection<Node> nodes, Collection<Edge> edges) {
        T subGraph = createGraph();
        subGraph.setParentGraph(this);
        subGraphs.add(subGraph);

        for (Node node : nodes) {
            subGraph.add(node);
        }
        for (Edge edge : edges) {
            subGraph.add(edge);
        }

        changedSubGraphs.add(subGraph);
        notifyObservers();
        return subGraph;
    }

    /**
     * Creates and inserts a new induced subgraph. It inserts the given nodes,
     * and all the parent edges between them.
     *
     * @param nodes the subset of parent nodes to be added.
     * @return the new induced subgraph.
     */
    public T newInducedSubGraph(Collection<Node> nodes) {
        T subGraph = createGraph();
        subGraph.setParentGraph(this);
        subGraphs.add(subGraph);

        for (Node node : nodes) {
            subGraph.add(node);
        }
        for (Node node : nodes) {
            for (Edge edge : outEdges(node)) {
                if (subGraph.has(edge.target())) {
                    subGraph.add(edge);
                }
            }
        }
        changedSubGraphs.add(subGraph);
        notifyObservers();
        return subGraph;
    }

    /**
     * Removes a subgraph from this graph.
     *
     * @param subGraph the subgraph to be removed.
     */
    @SuppressWarnings("unchecked")
    public void removeSubGraph(T subGraph) {
        ((GraphWithHierarchy) subGraph).parentGraph = null;
        subGraphs.remove(subGraph);

        changedSubGraphs.add(subGraph);
        notifyObservers();
    }

    @Override
    public Node newNode(String id) {
        if (id == null) {
            do {
                id = (++nodeIdIndex) + "n";
            } while (rootGraph().hasNode(id));
        }
        return super.newNode(id);
    }

    @Override
    public Edge newEdge(String id, Node source, Node target) {
        if (id == null) {
            do {
                id = (++edgeIdIndex) + "e";
            } while (rootGraph().hasEdge(id));
        }
        return super.newEdge(id, source, target);
    }

    @Override
    void addImplementation(Element element, Boolean forced) {
        if (parentGraph != null) {
            parentGraph.addImplementation(element, true);
        }
        super.addImplementation(element, forced);
    }

    @Override
    boolean shouldAddBePerformed(Element element, Boolean forced) {
        if (rootGraph().has(element)) {
            String id = element.id();
            Element rootElem = element instanceof Node ? rootGraph().getNode(id) : rootGraph().getEdge(id);
            if (rootElem != element) {
                throw new IllegalArgumentException("Adding a different node or edge instance for the same ID");
            }
        }
        return super.shouldAddBePerformed(element, forced);
    }

    @Override
    void removeImplementation(Element element, Boolean forced) {
        for (T subGraph : subGraphs) {
            subGraph.removeImplementation(element, true);
        }
        super.removeImplementation(element, forced);
    }

    /**
     * Register a graph hierarchy observer.
     *
     * @param observer the hierarchy observer.
     */
    public void registerObserver(Observer.GraphHierarchy observer) {
        hierarchyObservers.add(observer);
    }

    /**
     * Unregister a graph hierarchy observer.
     *
     * @param observer the hierarchy observer.
     */
    public void unregisterObserver(Observer.GraphHierarchy observer) {
        hierarchyObservers.remove(observer);
    }

    @Override
    protected void notifyObservers() {
        if (bulkNotify) {
            return;
        }
        super.notifyObservers();
        for (Observer.GraphHierarchy observer : hierarchyObservers) {
            if (!changedSubGraphs.isEmpty()) {
                observer.theseSubGraphsChanged(Collections.unmodifiableCollection(changedSubGraphs));
            }
        }
        changedSubGraphs.clear();
    }
}
