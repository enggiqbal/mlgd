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
package ocotillo.structures;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import lombok.EqualsAndHashCode;
import ocotillo.geometry.Interval;
import ocotillo.geometry.IntervalBox;

/**
 * Multidimensional interval tree data structure.
 *
 * @param <T> the type of data contained in the tree.
 */
public class MultidimIntervalTree<T extends MultidimIntervalTree.Data> {

    /**
     * Defines data related to an interval for each dimension.
     */
    public static interface Data {

        /**
         * Gets the interval box associated with the object.
         *
         * @return the interval box.
         */
        public IntervalBox intervalBox();
    }

    private final IntervalTree<?> rootTree;
    private final int dimensions;
    private int size;

    /**
     * Builds a multidimensional interval tree.
     *
     * @param dimensions the number of dimensions for this tree.
     */
    public MultidimIntervalTree(int dimensions) {
        this.rootTree = new IntervalTree<>();
        this.dimensions = dimensions;
    }

    /**
     * Gets all the elements whose box is equal to the query one.
     *
     * @param queryBox the query box.
     * @return all the elements whose box is equal to the given one.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<T> getAllEqual(IntervalBox queryBox) {
        Set results = new HashSet();
        results.add(rootTree);
        for (int i = dimensions - 1; i >= 0; i--) {
            Set newResults = new HashSet();
            for (Object tree : results) {
                newResults.addAll(((IntervalTree) tree).getAllEqual(queryBox.interval(i)));
            }
            results = newResults;
        }
        return (Set<T>) unwrapData(results);
    }

    /**
     * Gets all the elements whose box overlap the query one.
     *
     * @param queryBox the query box.
     * @return all the elements whose box overlap the given one.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<T> getAllOverlapping(IntervalBox queryBox) {
        Set results = new HashSet();
        results.add(rootTree);
        for (int i = dimensions - 1; i >= 0; i--) {
            Set newResults = new HashSet();
            for (Object tree : results) {
                newResults.addAll(((IntervalTree) tree).getAllOverlapping(queryBox.interval(i)));
            }
            results = newResults;
        }
        return (Set<T>) unwrapData(results);
    }

    /**
     * Gets all the elements whose box contain in the query one.
     *
     * @param queryBox the query box.
     * @return all the elements whose box contain the given one.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<T> getAllContaining(IntervalBox queryBox) {
        Set results = new HashSet();
        results.add(rootTree);
        for (int i = dimensions - 1; i >= 0; i--) {
            Set newResults = new HashSet();
            for (Object tree : results) {
                newResults.addAll(((IntervalTree) tree).getAllContaining(queryBox.interval(i)));
            }
            results = newResults;
        }
        return (Set<T>) unwrapData(results);
    }

    /**
     * Gets all the elements whose box is contained in the query one.
     *
     * @param queryBox the query box.
     * @return all the elements whose box is contained in the given one.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<T> getAllContainedIn(IntervalBox queryBox) {
        Set results = new HashSet();
        results.add(rootTree);
        for (int i = dimensions - 1; i >= 0; i--) {
            Set newResults = new HashSet();
            for (Object tree : results) {
                newResults.addAll(((IntervalTree) tree).getAllContainedIn(queryBox.interval(i)));
            }
            results = newResults;
        }
        return (Set<T>) unwrapData(results);
    }

    /**
     * Inserts an element in the interval tree.
     *
     * @param element the element to add.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void insert(T element) {
        DataContainer<T> wrappedElement = new DataContainer<>(element);
        int currentDim = dimensions - 1;
        IntervalTree currentTree = rootTree;
        while (currentDim > 0) {
            Interval dimInterval = wrappedElement.interval(currentDim);
            InnerLevelTree nextTree = (InnerLevelTree) currentTree.getAnyEqual(dimInterval);
            if (nextTree == null) {
                nextTree = new InnerLevelTree(dimInterval);
                currentTree.insert(nextTree);
            }
            currentTree = nextTree;
            currentDim--;
        }
        int sizeBeforeInsert = currentTree.size();
        currentTree.insert(wrappedElement);
        if (currentTree.size() == sizeBeforeInsert + 1) {
            size++;
        }
    }

    /**
     * Inserts all given intervals in the interval tree.
     *
     * @param intervalSet the collection of intervals.
     */
    public void insertAll(Collection<T> intervalSet) {
        for (T data : intervalSet) {
            insert(data);
        }
    }

    /**
     * Removes an element from the interval tree.
     *
     * @param element the element to delete.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void delete(T element) {
        DataContainer<T> wrappedElement = new DataContainer<>(element);
        int currentDim = dimensions - 1;
        IntervalTree currentTree = rootTree;
        Stack<InnerLevelTree> subtreeStack = new Stack<>();
        while (currentDim > 0) {
            Interval dimInterval = wrappedElement.interval(currentDim);
            InnerLevelTree nextTree = (InnerLevelTree) currentTree.getAnyEqual(dimInterval);
            if (nextTree == null) {
                return;
            }
            subtreeStack.push(nextTree);
            currentTree = nextTree;
            currentDim--;
        }
        int sizeBeforeDelete = currentTree.size();
        currentTree.delete(wrappedElement);
        if (currentTree.size() == sizeBeforeDelete - 1) {
            size--;
        }
        while (!subtreeStack.isEmpty()) {
            InnerLevelTree tree = subtreeStack.pop();
            if (tree.isEmpty()) {
                IntervalTree father = subtreeStack.isEmpty() ? rootTree : subtreeStack.peek();
                father.delete(tree);
            }
        }
    }

    /**
     * Deletes all given elements from the interval tree.
     *
     * @param elementSet the collection of elements.
     */
    public void deleteAll(Collection<T> elementSet) {
        for (T data : elementSet) {
            delete(data);
        }
    }

    /**
     * Verifies if an element is contained in the tree.
     *
     * @param element the element to check.
     * @return true if the element is contained.
     */
    public boolean contains(T element) {
        return getAllEqual(element.intervalBox()).contains(element);
    }

    /**
     * Returns the number of elements contained in this multilevel interval
     * tree.
     *
     * @return the size of the tree.
     */
    public int size() {
        return size;
    }

    /**
     * Clears the tree.
     */
    public void clear() {
        rootTree.clear();
        size = 0;
    }

    /**
     * Checks if the tree is empty.
     *
     * @return true if the tree is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Unwraps data out of their containers.
     *
     * @param containersSet the set of containers.
     * @return the data set.
     */
    private Set<T> unwrapData(Set<DataContainer<T>> containersSet) {
        Set<T> result = new HashSet<>();
        for (DataContainer<T> container : containersSet) {
            result.add(container.originalObject);
        }
        return result;
    }

    /**
     * Data wrapper that combine the requirements of IntervalTree and
     * MultilevelIntervalTree data.
     *
     * @param <T> the type of data contained.
     */
    @EqualsAndHashCode(of = "originalObject")
    private static class DataContainer<T extends Data> implements Data, IntervalTree.Data {

        public final T originalObject;

        public DataContainer(T originalData) {
            this.originalObject = originalData;
        }

        @Override
        public IntervalBox intervalBox() {
            return originalObject.intervalBox();
        }

        @Override
        public Interval interval() {
            return originalObject.intervalBox().interval(0);
        }

        public Interval interval(int dimension) {
            return originalObject.intervalBox().interval(dimension);
        }
    }

    /**
     * Interval tree wrapper that allows to store IntervalTrees as elements of
     * other interval trees.
     */
    @SuppressWarnings("rawtypes")
    protected static class InnerLevelTree extends IntervalTree implements IntervalTree.Data {

        private final Interval interval;

        public InnerLevelTree(Interval interval) {
            this.interval = interval;
        }

        @Override
        public Interval interval() {
            return interval;
        }
    }
}
