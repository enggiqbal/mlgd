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
package ocotillo.graph.layout.locator.intervaltree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import ocotillo.geometry.Box;
import ocotillo.geometry.Geom;
import ocotillo.geometry.GeomE;
import ocotillo.geometry.IntervalBox;
import ocotillo.graph.Edge;
import ocotillo.graph.Element;
import ocotillo.graph.ElementAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.GraphWithElements;
import ocotillo.graph.Node;
import ocotillo.graph.Observer;
import ocotillo.graph.layout.locator.ElementLocatorAbst;
import ocotillo.structures.MultidimIntervalTree;

/**
 * Locator based on multilevel interval trees.
 */
public class IntervalTreeLocator extends ElementLocatorAbst {

    private final MultidimIntervalTree<Boxed<Node>> nodeTree;
    private final MultidimIntervalTree<Boxed<Edge>> edgeTree;
    private final boolean autoSync;

    private Observer.GraphElements elementObserver;
    private Observer.ElementAttributeChanges<Node> nodePositionObserver;
    private Observer.ElementAttributeChanges<Node> nodeSizeObserver;
    private Observer.ElementAttributeChanges<Edge> edgePointsObserver;
    private Observer.ElementAttributeChanges<Edge> edgeWitdhObserver;

    /**
     * Builder for interval tree locator.
     */
    public static class ItlBuilder {

        private final Graph graph;
        private final NodePolicy nodePolicy;
        private final EdgePolicy edgePolicy;
        private GeomE geometry = Geom.e2D;
        private boolean autoSync = true;

        /**
         * Construct an interval tree locator builder.
         *
         * @param graph the graph.
         * @param nodePolicy the node policy.
         * @param edgePolicy the edge policy.
         */
        public ItlBuilder(Graph graph, NodePolicy nodePolicy, EdgePolicy edgePolicy) {
            this.graph = graph;
            this.nodePolicy = nodePolicy;
            this.edgePolicy = edgePolicy;
        }

        /**
         * Indicates the Euclidean geometry to be used.
         *
         * @param geometry the geometry.
         * @return this builder.
         */
        public ItlBuilder withGeometry(GeomE geometry) {
            this.geometry = geometry;
            return this;
        }

        /**
         * Enables automatic synchronisation.
         *
         * @return this builder.
         */
        public ItlBuilder enableAutoSync() {
            this.autoSync = true;
            return this;
        }

        /**
         * Disables automatic synchronisation. The structure must be updated
         * using rebuild.
         *
         * @return this builder.
         */
        public ItlBuilder disableAutoSync() {
            this.autoSync = false;
            return this;
        }

        /**
         * Generates the interval tree locator.
         *
         * @return the locator.
         */
        public IntervalTreeLocator build() {
            return new IntervalTreeLocator(graph, geometry, nodePolicy, edgePolicy, autoSync);
        }
    }

    /**
     * Builds an interval tree locator.
     *
     * @param graph the graph.
     * @param geometry the geometry to be used.
     * @param nodePolicy the node policy.
     * @param edgePolicy the edge policy.
     * @param autoSync the autoSync status.
     */
    private IntervalTreeLocator(Graph graph, GeomE geometry, NodePolicy nodePolicy, EdgePolicy edgePolicy, boolean autoSync) {
        super(graph, geometry, nodePolicy, edgePolicy);
        this.nodeTree = new MultidimIntervalTree<>(geomDim);
        this.edgeTree = new MultidimIntervalTree<>(geomDim);
        this.autoSync = autoSync;
        build();

        if (autoSync) {
            elementObserver = new ElementObserver(graph);
            nodePositionObserver = new NodePositionObserver(nodePositions);
            nodeSizeObserver = new NodeSizeObserver(nodeSizes);
            edgePointsObserver = new EdgeAttributeObserver(edgePoints);
            edgeWitdhObserver = new EdgeAttributeObserver(edgeWidths);
        }
    }

    /**
     * Initialises the structure by adding all nodes and edges boxes.
     */
    private void build() {
        nodeTree.clear();
        edgeTree.clear();
        nodeBoxes.clear();
        edgeBoxes.clear();

        if (nodePolicy != NodePolicy.ignoreNodes) {
            for (Node node : graph.nodes()) {
                updateBox(node);
            }
        }
        if (edgePolicy != EdgePolicy.ignoreEdges) {
            for (Edge edge : graph.edges()) {
                updateBox(edge);
            }
        }
    }

    @Override
    protected Box computeBox(Node node) {
        Box standardBox = super.computeBox(node);
        return new Boxed<>(node, IntervalBox.newInstance(standardBox));
    }

    @Override
    protected Box computeBox(Edge edge) {
        Box standardBox = super.computeBox(edge);
        return new Boxed<>(edge, IntervalBox.newInstance(standardBox));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateBox(Node node) {
        if (nodeBoxes.containsKey(node)) {
            Box nodeBox = nodeBoxes.get(node);
            nodeTree.delete((Boxed<Node>) nodeBox);
        }
        if (graph.has(node)) {
            super.updateBox(node);
            nodeTree.insert((Boxed<Node>) getBox(node));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateBox(Edge edge) {
        if (edgeBoxes.containsKey(edge)) {
            Box edgeBox = edgeBoxes.get(edge);
            edgeTree.delete((Boxed<Edge>) edgeBox);
        }
        if (graph.has(edge)) {
            super.updateBox(edge);
            edgeTree.insert((Boxed<Edge>) getBox(edge));
        }
    }

    @Override
    public void rebuild() {
        if (!autoSync) {
            build();
        }
    }

    @Override
    public Collection<Node> getNodesPartiallyInBox(Box box) {
        return unwrap(nodeTree.getAllOverlapping(IntervalBox.newInstance(box)));
    }

    @Override
    public Collection<Node> getNodesFullyInBox(Box box) {
        return unwrap(nodeTree.getAllContainedIn(IntervalBox.newInstance(box)));
    }

    @Override
    public Collection<Edge> getEdgesPartiallyInBox(Box box) {
        return unwrap(edgeTree.getAllOverlapping(IntervalBox.newInstance(box)));
    }

    @Override
    public Collection<Edge> getEdgesFullyInBox(Box box) {
        return unwrap(edgeTree.getAllContainedIn(IntervalBox.newInstance(box)));
    }

    @Override
    public void close() {
        if (autoSync) {
            elementObserver.unregister();
            nodePositionObserver.unregister();
            nodeSizeObserver.unregister();
            edgePointsObserver.unregister();
            edgeWitdhObserver.unregister();
        }
    }

    /**
     * Unwraps elements from their Boxed container.
     *
     * @param <T> the type of element handled.
     * @param wrappedCollection the wrapped collection.
     * @return the unwrapped collection.
     */
    private <T extends Element> Collection<T> unwrap(Collection<Boxed<T>> wrappedCollection) {
        Collection<T> result = new HashSet<>(wrappedCollection.size());
        for (Boxed<T> wrappedElement : wrappedCollection) {
            result.add(wrappedElement.element);
        }
        return result;
    }

    /**
     * Wrapper for graph elements that allow them to be inserted in
     * multidimensional interval trees.
     *
     * @param <T> the type of element handled.
     */
    private static class Boxed<T extends Element> extends Box implements MultidimIntervalTree.Data {

        private final T element;
        private final IntervalBox box;

        /**
         * Builds the wrapper.
         *
         * @param node the node.
         * @param box its box.
         */
        private Boxed(T node, IntervalBox box) {
            super(box);
            this.element = node;
            this.box = box;
        }

        /**
         * Returns the element.
         *
         * @return the element.
         */
        public T element() {
            return element;
        }

        @Override
        public IntervalBox intervalBox() {
            return box;
        }
    }

    /**
     * Observer for element insertion or removal.
     */
    private class ElementObserver extends Observer.GraphElements {

        public ElementObserver(GraphWithElements observedGraph) {
            super(observedGraph);
        }

        @Override
        public void theseElementsChanged(Collection<Element> changedElements) {
            if (changedElements.size() > (nodeTree.size() + edgeTree.size()) * 0.5) {
                build();
            } else {
                for (Element element : changedElements) {
                    if (element instanceof Node) {
                        updateBox((Node) element);
                    } else if (element instanceof Edge) {
                        updateBox((Edge) element);
                    }
                }
            }
        }
    }

    /**
     * Observer for changes in node size attribute.
     */
    private class NodePositionObserver extends Observer.ElementAttributeChanges<Node> {

        public NodePositionObserver(ElementAttribute<Node, ?> attributeObserved) {
            super(attributeObserved);
        }

        @Override
        public void update(Collection<Node> changedElements) {
            if (changedElements.size() > (nodeTree.size() + edgeTree.size()) * 0.4) {
                build();
            } else {
                Set<Edge> changedEdges = new HashSet<>();
                for (Node node : changedElements) {
                    updateBox(node);
                    changedEdges.addAll(graph.inOutEdges(node));
                }
                for (Edge edge : changedEdges) {
                    updateBox(edge);
                }
            }
        }

        @Override
        public void updateAll() {
            build();
        }
    }

    /**
     * Observer for changes in node size attribute.
     */
    private class NodeSizeObserver extends Observer.ElementAttributeChanges<Node> {

        public NodeSizeObserver(ElementAttribute<Node, ?> attributeObserved) {
            super(attributeObserved);
        }

        @Override
        public void update(Collection<Node> changedElements) {
            if (changedElements.size() > (nodeTree.size() + edgeTree.size()) * 0.6) {
                build();
            } else {
                for (Node node : changedElements) {
                    updateBox(node);
                }
            }
        }

        @Override
        public void updateAll() {
            build();
        }
    }

    /**
     * Observer for changes in relevant edge attributes.
     */
    private class EdgeAttributeObserver extends Observer.ElementAttributeChanges<Edge> {

        public EdgeAttributeObserver(ElementAttribute<Edge, ?> attributeObserved) {
            super(attributeObserved);
        }

        @Override
        public void update(Collection<Edge> changedElements) {
            if (changedElements.size() > (edgeTree.size() + edgeTree.size()) * 0.6) {
                build();
            } else {
                for (Edge edge : changedElements) {
                    updateBox(edge);
                }
            }
        }

        @Override
        public void updateAll() {
            build();
        }
    }
}
