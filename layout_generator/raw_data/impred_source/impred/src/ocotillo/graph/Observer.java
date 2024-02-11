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

/**
 * Observer interface.
 */
public interface Observer {

    /**
     * Unregister this observer from the observed subject.
     */
    public void unregister();

    /**
     * Observer for changes in the elements of a graph.
     */
    public abstract static class GraphElements implements Observer {

        private final GraphWithElements observedGraph;

        public GraphElements(GraphWithElements observedGraph) {
            this.observedGraph = observedGraph;
            observedGraph.registerObserver(this);
        }

        /**
         * Updates the elements recently been added or removed.
         *
         * @param changedElements the elements that changed.
         */
        public abstract void theseElementsChanged(Collection<Element> changedElements);

        @Override
        public void unregister() {
            observedGraph.unregisterObserver(this);
        }
    }

    /**
     * Observer for changes in the elements of a graph.
     */
    public abstract static class GraphHierarchy implements Observer {

        private final GraphWithHierarchy<?> observedGraph;

        public GraphHierarchy(GraphWithHierarchy<?> observedGraph) {
            this.observedGraph = observedGraph;
            observedGraph.registerObserver(this);
        }

        /**
         * Updates the sub-graphs that recently been added or removed.
         *
         * @param changedSubGraphs the sub-graphs that changed.
         */
        public abstract void theseSubGraphsChanged(Collection<GraphWithHierarchy<?>> changedSubGraphs);

        @Override
        public void unregister() {
            observedGraph.unregisterObserver(this);
        }
    }

    /**
     * Observer for changes in the elements of a graph.
     */
    public abstract static class LocalAttributeList implements Observer {

        private final Graph observedGraph;

        public LocalAttributeList(Graph observedGraph) {
            this.observedGraph = observedGraph;
            observedGraph.registerObserver(this);
        }

        /**
         * Updates the attributes that have recently been added or removed. The
         * observers will be notified only for the local attributes added or
         * removed, and not for changes in inherited attributes.
         *
         * @param changedAttributes the attributes that changed.
         */
        public abstract void theseLocalAttributesAddedOrRemoved(Collection<Attribute<?>> changedAttributes);

        @Override
        public void unregister() {
            observedGraph.unregisterObserver(this);
        }
    }

    /**
     * Observer for graph attribute modifications.
     */
    public abstract class GraphAttributeChanges implements Observer {

        private final GraphAttribute<?> attributeObserved;

        /**
         * Constructs an element attribute observer.
         *
         * @param attributeObserved the observed attribute.
         */
        @SuppressWarnings("LeakingThisInConstructor")
        public GraphAttributeChanges(GraphAttribute<?> attributeObserved) {
            this.attributeObserved = attributeObserved;
            attributeObserved.registerObserver(this);
        }

        /**
         * Updates the attribute to its current value.
         */
        public abstract void update();

        @Override
        public void unregister() {
            attributeObserved.unregisterObserver(this);
        }

    }

    /**
     * Observer for an element attribute.
     *
     * @param <K> The type of element handled.
     */
    public abstract static class ElementAttributeChanges<K extends Element> implements Observer {

        private final ElementAttribute<K, ?> attributeObserved;

        /**
         * Constructs an element attribute observer.
         *
         * @param attributeObserved the observed attribute.
         */
        @SuppressWarnings("LeakingThisInConstructor")
        public ElementAttributeChanges(ElementAttribute<K, ?> attributeObserved) {
            this.attributeObserved = attributeObserved;
            attributeObserved.registerObserver(this);
        }

        /**
         * Updates the elements whose attribute recently changed.
         *
         * @param changedElements the elements whose attributes changed.
         */
        public abstract void update(Collection<K> changedElements);

        /**
         * Updated all elements of the graph. This is used when the elements
         * that changed are not directly known by the attribute, such as when
         * the default value is changed.
         */
        public abstract void updateAll();

        @Override
        public void unregister() {
            attributeObserved.unregisterObserver(this);
        }
    }

}
