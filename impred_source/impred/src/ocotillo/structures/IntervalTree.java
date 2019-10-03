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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import ocotillo.geometry.Interval;

/**
 * Interval tree data structure.
 *
 * @param <T> the type of data contained in the tree.
 */
@EqualsAndHashCode
public class IntervalTree<T extends IntervalTree.Data> implements Iterable<T> {

    /**
     * Defines data related to an interval.
     */
    public static interface Data {

        /**
         * Gets the interval related to this object.
         *
         * @return the interval.
         */
        public Interval interval();
    }

    private final AugmentedTree tree = new AugmentedTree();

    /**
     * Gets any element whose interval is equal to the query one.
     *
     * @param queryInterval the query interval.
     * @return an element with the same interval as the query one.
     */
    public T getAnyEqual(Interval queryInterval) {
        RedBlackTree.RbNode<DataContainer<T>, Double> node = tree.findNode(queryInterval.leftBound());
        if (node != null) {
            for (DataContainer<T> container : node.dataSet) {
                if (container.interval.equals(queryInterval)) {
                    return container.originalObject;
                }
            }
        }
        return null;
    }

    /**
     * Gets all the elements whose interval is equal to the query one.
     *
     * @param queryInterval the query interval.
     * @return all elements with the same interval as the query one.
     */
    public Set<T> getAllEqual(Interval queryInterval) {
        Set<T> results = new HashSet<>();
        RedBlackTree.RbNode<DataContainer<T>, Double> node = tree.findNode(queryInterval.leftBound());
        if (node != null) {
            for (DataContainer<T> container : node.dataSet) {
                if (container.interval.equals(queryInterval)) {
                    results.add(container.originalObject);
                }
            }
        }
        return results;
    }

    /**
     * Gets any one element whose interval overlaps with the query interval.
     *
     * @param queryInterval the query interval.
     * @return an element whose interval overlaps with the query one.
     */
    public T getAnyOverlapping(Interval queryInterval) {
        return anyOverlappingRecursion(queryInterval, (ItNode) tree.root);
    }

    /**
     * Gets all the elements whose intervals overlap with the query interval.
     *
     * @param queryInterval the query interval.
     * @return all the elements whose intervals overlap the query one.
     */
    public Set<T> getAllOverlapping(Interval queryInterval) {
        Set<T> results = new HashSet<>();
        allOverlappingRecursion(queryInterval, (ItNode) tree.root, results);
        return results;
    }

    /**
     * Gets any one element whose interval contain the query point.
     *
     * @param queryPoint the query point.
     * @return an element whose interval contains the point.
     */
    public T getAnyContaining(double queryPoint) {
        return getAnyOverlapping(Interval.newClosed(queryPoint, queryPoint));
    }

    /**
     * Gets all the elements whose intervals contain the query point.
     *
     * @param queryPoint the query point.
     * @return all elements whose intervals contain the point.
     */
    public Set<T> getAllContaining(double queryPoint) {
        return getAllOverlapping(Interval.newClosed(queryPoint, queryPoint));
    }

    /**
     * Gets all the elements whose intervals fully contain the query interval.
     *
     * @param queryInterval the query interval.
     * @return all the elements whose intervals fully contain the query one.
     */
    public Set<T> getAllContaining(Interval queryInterval) {
        Set<T> results = new HashSet<>();
        for (T element : getAllOverlapping(queryInterval)) {
            if (element.interval().contains(queryInterval)) {
                results.add(element);
            }
        }
        return results;
    }

    /**
     * Gets all the elements whose intervals is fully contained in the query
     * interval.
     *
     * @param queryInterval the query interval.
     * @return all the elements whose intervals is fully contained in the query
     * one.
     */
    public Set<T> getAllContainedIn(Interval queryInterval) {
        Set<T> results = new HashSet<>();
        for (T element : getAllOverlapping(queryInterval)) {
            if (element.interval().isContainedIn(queryInterval)) {
                results.add(element);
            }
        }
        return results;
    }

    /**
     * Returns an element whose interval overlaps with the query one as soon as
     * it finds one.
     *
     * @param queryInterval the query interval.
     * @param currentRoot the current root of the subtree.
     * @return an element whose interval overlaps with the query one.
     */
    private T anyOverlappingRecursion(Interval queryInterval, ItNode currentRoot) {
        if (currentRoot == null || currentRoot.maxRightInSubTree < queryInterval.leftBound()) {
            return null;
        }
        T leftTreeResult = anyOverlappingRecursion(queryInterval, (ItNode) currentRoot.leftChild);
        if (leftTreeResult != null) {
            return leftTreeResult;
        }
        if (queryInterval.rightBound() < currentRoot.bstKey()) {
            return null;
        }
        for (DataContainer<T> dataContainer : currentRoot.dataSet) {
            if (dataContainer.interval.overlapsWith(queryInterval)) {
                return dataContainer.originalObject;
            }
        }
        return anyOverlappingRecursion(queryInterval, (ItNode) currentRoot.rightChild);
    }

    /**
     * Adds to the current results all the elements with intervals overlapping
     * the query one in the subtree with given root.
     *
     * @param queryInterval the query interval.
     * @param currentRoot the current root of the subtree.
     * @param results the results.
     */
    private void allOverlappingRecursion(Interval queryInterval, ItNode currentRoot, Set<T> results) {
        if (currentRoot == null || currentRoot.maxRightInSubTree < queryInterval.leftBound()) {
            return;
        }
        allOverlappingRecursion(queryInterval, (ItNode) currentRoot.leftChild, results);
        if (queryInterval.rightBound() < currentRoot.bstKey()) {
            return;
        }
        for (DataContainer<T> dataContainer : currentRoot.dataSet) {
            if (dataContainer.interval.overlapsWith(queryInterval)) {
                results.add(dataContainer.originalObject);
            }
        }
        allOverlappingRecursion(queryInterval, (ItNode) currentRoot.rightChild, results);
    }

    /**
     * Inserts an element in the interval tree.
     *
     * @param element the element to add.
     */
    public void insert(T element) {
        tree.insert(new DataContainer<>(element));
    }

    /**
     * Inserts all given elements in the interval tree.
     *
     * @param elementSet the collection of elements.
     */
    public void insertAll(Collection<T> elementSet) {
        for (T data : elementSet) {
            insert(data);
        }
    }

    /**
     * Removes an element from the interval tree.
     *
     * @param element the element to delete.
     */
    public void delete(T element) {
        tree.delete(new DataContainer<>(element));
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
     * Checks if the tree contains the given element.
     *
     * @param element the element.
     * @return true if the tree contains the element.
     */
    public boolean contains(T element) {
        return tree.contains(new DataContainer<>(element));
    }

    /**
     * Returns the number of elements contained in this interval tree.
     *
     * @return the size of the tree.
     */
    public int size() {
        return tree.size();
    }

    /**
     * Checks if the tree is empty.
     *
     * @return true if the tree is empty.
     */
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    /**
     * Clears the interval tree.
     */
    public void clear() {
        tree.clear();
    }

    /**
     * Returns the elements in order of increasing left bound.
     *
     * @return the in-order list of intervals.
     */
    public List<T> inOrderTraversal() {
        List<DataContainer<T>> containerList = tree.inOrderTraversal();
        List<T> objectList = new ArrayList<>(containerList.size());
        for (DataContainer<T> container : containerList) {
            objectList.add(container.originalObject);
        }
        return objectList;
    }

    @Override
    public Iterator<T> iterator() {
        return inOrderTraversal().iterator();
    }

    @Override
    public String toString() {
        return tree.toString();
    }

    /**
     * A container class for interval data that uses the interval left bound as
     * as binary search tree key.
     *
     * @param <T> the kind of data contained.
     */
    @EqualsAndHashCode(of = "originalObject")
    @SuppressWarnings("unchecked")
    private class DataContainer<T extends Data> implements BinarySearchTree.Data<Double> {

        public final Interval interval;
        public final T originalObject;

        private DataContainer(T originalObject) {
            this.originalObject = originalObject;
            this.interval = originalObject.interval();
        }

        @Override
        public Double bstKey() {
            return interval.leftBound();
        }
    }

    /**
     * A node of the IntervalTree. In contains the greatest right bound of the
     * contained intervals and of those contained in their subtree.
     */
    private class ItNode extends RedBlackTree.RbNode<DataContainer<T>, Double> {

        private double maxRightInSubTree;
        private double maxRightInNode;

        /**
         * Creates a new IntervalTree node.
         *
         * @param data the data container.
         */
        public ItNode(DataContainer<T> data) {
            super(data);
            maxRightInNode = data.interval.rightBound();
        }

        /**
         * Updates the max right bounds after an insertion in the node dataset.
         *
         * @param data the data inserted in this node.
         */
        private void updateAfterInsertion(DataContainer<T> data) {
            if (maxRightInNode < data.interval.rightBound()) {
                maxRightInNode = data.interval.rightBound();
                updateAndPropagate();
            }
        }

        /**
         * Updates the max right bounds after a deletion in the node dataset.
         *
         * @param data the data deleted in this node.
         */
        private void updateAfterDeletion(DataContainer<T> data) {
            if (maxRightInNode == data.interval.rightBound()) {
                maxRightInNode = computeMaxRightInNode();
                updateAndPropagate();
            }
        }

        /**
         * Computes the max right bound of the intervals contained in this node.
         *
         * @return the max right bound for the intervals in this node.
         */
        private double computeMaxRightInNode() {
            double value = Double.NEGATIVE_INFINITY;
            for (DataContainer<T> data : dataSet) {
                value = Math.max(value, data.interval.rightBound());
            }
            return value;
        }

        /**
         * Computes the max right bound of the intervals contained in the
         * subtree rooted in this node.
         *
         * @return the max right bound for this subtree,
         */
        private double computeMaxRightInSubTree() {
            double value = maxRightInNode;
            if (leftChild != null) {
                value = Math.max(value, ((ItNode) leftChild).maxRightInSubTree);
            }
            if (rightChild != null) {
                value = Math.max(value, ((ItNode) rightChild).maxRightInSubTree);
            }
            return value;
        }

        /**
         * Checks if an update is necessary and propagates it to the parent.
         */
        private void updateAndPropagate() {
            double newMaxRightInSubTree = computeMaxRightInSubTree();
            if (newMaxRightInSubTree != maxRightInSubTree) {
                maxRightInSubTree = newMaxRightInSubTree;
                if (parent != null) {
                    ((ItNode) parent).updateAndPropagate();
                }
            }
        }

        @Override
        public String toString() {
            return bstKey() + "(" + maxRightInSubTree + ")";
        }
    }

    /**
     * A binary search tree composed by ItNodes. The tree triggers the necessary
     * right-bound updates on ItNodes when intervals are added or removed from
     * the tree.
     */
    private class AugmentedTree extends RedBlackTree<DataContainer<T>, Double> {

        @Override
        protected RbNode<DataContainer<T>, Double> createNewNode(DataContainer<T> node) {
            return new ItNode(node);
        }

        @Override
        protected void insertDataInNode(DataContainer<T> data, RbNode<DataContainer<T>, Double> node) {
            super.insertDataInNode(data, node);
            ((ItNode) node).updateAfterInsertion(data);
        }

        @Override
        protected void deleteDataFromNode(DataContainer<T> data, RbNode<DataContainer<T>, Double> node) {
            super.deleteDataFromNode(data, node);
            ((ItNode) node).updateAfterDeletion(data);
        }

        @Override
        protected void onNodeInsertion(RbNode<DataContainer<T>, Double> insertedNode) {
            super.onNodeInsertion(insertedNode);
            ((ItNode) insertedNode).updateAndPropagate();
        }

        @Override
        protected void onNodeDeletion(RbNode<DataContainer<T>, Double> deletedNodeParent) {
            super.onNodeDeletion(deletedNodeParent);
            ((ItNode) deletedNodeParent).updateAndPropagate();
        }

        @Override
        protected void rotateLeft(RbNode<DataContainer<T>, Double> currentNode) {
            super.rotateLeft(currentNode);
            ((ItNode) currentNode.leftChild).updateAndPropagate();
            ((ItNode) currentNode).updateAndPropagate();
        }

        @Override
        protected void rotateRight(RbNode<DataContainer<T>, Double> currentNode) {
            super.rotateRight(currentNode);
            ((ItNode) currentNode.rightChild).updateAndPropagate();
            ((ItNode) currentNode).updateAndPropagate();
        }
    }
}
