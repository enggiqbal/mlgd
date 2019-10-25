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
package ocotillo.graph.extra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Element;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.Observer;

/**
 * Decorator for ElementRelationComputator that stores the results of the
 * computation in a lookup table to avoid re-computing them. The lookup table
 * can observe changes in graphs or attributes and automatically erase the
 * lookup value for nodes and edges that changed.
 *
 * @param <E> the type of the computed result.
 */
public class ElementRelationLookup<E> implements ElementRelationComputator<E> {

    private final ElementRelationComputator<E> computator;
    private final SymmetricLookupTable<Node, Node, E> nodeNodeLookup = new SymmetricLookupTable<>();
    private final SymmetricLookupTable<Node, Edge, E> nodeEdgeLookup = new SymmetricLookupTable<>();
    private final SymmetricLookupTable<Edge, Edge, E> edgeEdgeLookup = new SymmetricLookupTable<>();

    private Graph graph;
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Constructs a lookup decorator for an ElementRelationComputator.
     *
     * @param computator the class that actually perform the computation.
     */
    public ElementRelationLookup(ElementRelationComputator<E> computator) {
        this.computator = computator;
    }

    /**
     * Instructs the lookup to observe graph changes to delete lookup entries no
     * more relevant. It is necessary to observe a graph before observing any
     * element attribute.
     *
     * @param graph the graph to observe.
     */
    public void observe(Graph graph) {
        assert (this.graph == null) : "The graph to observe cannot be set twice";

        this.graph = graph;
        observers.add(new Observer.GraphElements(graph) {

            @Override
            public void theseElementsChanged(Collection<Element> changedElements) {
                for (Element element : changedElements) {
                    nodeNodeLookup.erase(element);
                    nodeEdgeLookup.erase(element);
                    edgeEdgeLookup.erase(element);
                }
            }
        });
    }

    /**
     * Instructs the lookup to observe node attribute changes to delete lookup
     * entries that need to be updated. It is necessary to observe a graph
     * before observing any element attribute.
     *
     * @param attribute the attribute to observe.
     * @param affectIncidentEdges indicates whether a change in a node attribute
     * affects the stored values of incident edges. (e.g. a change in node
     * positions affect the position of incident edges)
     */
    public void observe(NodeAttribute<?> attribute, final boolean affectIncidentEdges) {
        assert (graph != null) : "If you choose to observe an attribute, than you must first observe a graph";

        observers.add(new Observer.ElementAttributeChanges<Node>(attribute) {

            @Override
            public void update(Collection<Node> changedElements) {
                for (Node node : changedElements) {
                    nodeNodeLookup.erase(node);
                    nodeEdgeLookup.erase(node);
                    if (affectIncidentEdges) {
                        for (Edge edge : graph.inOutEdges(node)) {
                            edgeEdgeLookup.erase(edge);
                            nodeEdgeLookup.erase(edge);
                        }
                    }
                }
            }

            @Override
            public void updateAll() {
                nodeNodeLookup.clear();
                nodeEdgeLookup.clear();
                if (affectIncidentEdges) {
                    edgeEdgeLookup.clear();
                }
            }
        });
    }

    /**
     * Instructs the lookup to observe edge attribute changes to delete lookup
     * entries that need to be updated. It is necessary to observe a graph
     * before observing any element attribute.
     *
     * @param attribute the attribute to observe.
     * @param affectIncidentNodes indicates whether a change in an edge
     * attribute affects the stored values of incident nodes. (e.g. a change in
     * an edge weight might effect the metric of the incident nodes)
     */
    public void observe(EdgeAttribute<?> attribute, final boolean affectIncidentNodes) {
        assert (graph != null) : "If you choose to observe an attribute, than you must first observe a graph";

        observers.add(new Observer.ElementAttributeChanges<Edge>(attribute) {

            @Override
            public void update(Collection<Edge> changedElements) {
                for (Edge edge : changedElements) {
                    edgeEdgeLookup.erase(edge);
                    nodeEdgeLookup.erase(edge);
                    if (affectIncidentNodes) {
                        nodeNodeLookup.erase(edge.source());
                        nodeNodeLookup.erase(edge.target());
                        nodeEdgeLookup.erase(edge.source());
                        nodeEdgeLookup.erase(edge.target());
                    }
                }
            }

            @Override
            public void updateAll() {
                edgeEdgeLookup.clear();
                nodeEdgeLookup.clear();
                if (affectIncidentNodes) {
                    nodeNodeLookup.clear();
                }
            }
        });
    }

    /**
     * Clears all the values currently stored in the lookup.
     */
    public void clear() {
        nodeNodeLookup.clear();
        nodeEdgeLookup.clear();
        edgeEdgeLookup.clear();
    }

    @Override
    public E compute(Node nodeA, Node nodeB) {
        E storedValue = nodeNodeLookup.get(nodeA, nodeB);
        if (storedValue != null) {
            return storedValue;
        } else {
            E result = computator.compute(nodeA, nodeB);
            nodeNodeLookup.set(nodeA, nodeB, result);
            return result;
        }
    }

    @Override
    public E compute(Edge edgeA, Edge edgeB) {
        E storedValue = edgeEdgeLookup.get(edgeA, edgeB);
        if (storedValue != null) {
            return storedValue;
        } else {
            E result = computator.compute(edgeA, edgeB);
            edgeEdgeLookup.set(edgeA, edgeB, result);
            return result;
        }
    }

    @Override
    public E compute(Node node, Edge edge) {
        E storedValue = nodeEdgeLookup.get(node, edge);
        if (storedValue != null) {
            return storedValue;
        } else {
            E result = computator.compute(node, edge);
            nodeEdgeLookup.set(node, edge, result);
            return result;
        }
    }

    @Override
    public void close() {
        for (Observer observer : observers) {
            observer.unregister();
        }
        computator.close();
    }

}
