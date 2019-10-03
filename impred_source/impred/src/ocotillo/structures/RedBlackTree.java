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
import java.util.Objects;
import java.util.Set;

/**
 * The Red-Black data structure. It implements a self-balancing binary search
 * tree.
 *
 * @param <T> the type of data contained in the tree.
 * @param <K> the type of key values associated to the nodes.
 */
public class RedBlackTree<T extends BinarySearchTree.Data<K>, K extends Comparable<K>> implements BinarySearchTree<T, K> {

    protected RbNode<T, K> root;
    protected int size = 0;

    @Override
    public T get(K queryKey) {
        Set<T> results = getAll(queryKey);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.iterator().next();
        }
    }

    @Override
    public Set<T> getAll(K queryKey) {
        Set<T> results = new HashSet<>();
        RbNode<T, K> interestedNode = findNode(queryKey);
        if (interestedNode != null) {
            results.addAll(interestedNode.dataSet);
        }
        return results;
    }

    @Override
    public void insert(T data) {
        RbNode<T, K> potentialParent = findLastNode(data.bstKey());
        RbNode<T, K> newNode = createNewNode(data);
        newNode.setRed();
        if (potentialParent == null) {
            insert_case1(newNode);
            onNodeInsertion(newNode);
        } else {
            newNode.parent = potentialParent;
            switch (data.bstKey().compareTo(potentialParent.bstKey())) {
                case -1:
                    potentialParent.leftChild = newNode;
                    onNodeInsertion(newNode);
                    insert_case2(newNode);
                    break;
                case 0:
                    // Parent contains the same key, so we insert the abstract value
                    // here and we forget the newly created RbNode.
                    if (potentialParent.dataSet.contains(data)) {
                        return;
                    } else {
                        insertDataInNode(data, potentialParent);
                    }
                    break;
                case 1:
                    potentialParent.rightChild = newNode;
                    onNodeInsertion(newNode);
                    insert_case2(newNode);
                    break;
                default:
                    throw new IllegalStateException("CompareTo has returned a different value than -1, 0 or 1.");
            }
        }
        size++;
    }

    /**
     * Deals with the first special case in node insertion. Handles current node
     * is the root of a tree.
     *
     * @param currentNode the current node.
     */
    private void insert_case1(RbNode<T, K> currentNode) {
        if (currentNode.parent == null) {
            currentNode.setBlack();
            root = currentNode;
        } else {
            insert_case2(currentNode);
        }
    }

    /**
     * Deals with the second special case in node insertion. Handles current
     * node's parent is black.
     *
     * @param currentNode the current node.
     */
    private void insert_case2(RbNode<T, K> currentNode) {
        if (currentNode.parent.isRed()) {
            insert_case3(currentNode);
        }
    }

    /**
     * Deals with the third special case in node insertion. Handles current
     * node's parent and uncle are red.
     *
     * @param currentNode the current node.
     */
    private void insert_case3(RbNode<T, K> currentNode) {
        RbNode<T, K> uncle = currentNode.uncle();
        if (uncle != null && uncle.isRed()) {
            currentNode.parent.setBlack();
            uncle.setBlack();
            RbNode<T, K> grandparent = currentNode.grandparent();
            grandparent.setRed();
            insert_case1(grandparent);
        } else {
            insert_case4(currentNode);
        }
    }

    /**
     * Deals with the fourth special case in node insertion. Handles current
     * node's parent is red and uncle is black.
     *
     * @param currentNode the current node.
     */
    private void insert_case4(RbNode<T, K> currentNode) {
        if (currentNode.isRightChild() && currentNode.parent.isLeftChild()) {
            rotateLeft(currentNode);
            currentNode = currentNode.leftChild;
        } else if (currentNode.isLeftChild() && currentNode.parent.isRightChild()) {
            rotateRight(currentNode);
            currentNode = currentNode.rightChild;
        }
        insert_case5(currentNode);
    }

    /**
     * Deals with the fifth special case in node insertion. Handles current
     * node's parent is red, uncle is black, and current node is left child of
     * left child or right child of right child.
     *
     * @param currentNode the current node.
     */
    private void insert_case5(RbNode<T, K> currentNode) {
        currentNode.parent.setBlack();
        currentNode.grandparent().setRed();
        if (currentNode.isLeftChild()) {
            rotateRight(currentNode.parent);
        } else {
            rotateLeft(currentNode.parent);
        }
    }

    @Override
    public void insertAll(Collection<T> dataSet) {
        for (T data : dataSet) {
            insert(data);
        }
    }

    @Override
    public void delete(T data) {
        RbNode<T, K> nodeToDelete = findNode(data.bstKey());
        if (nodeToDelete == null || !nodeToDelete.dataSet.contains(data)) {
            return;
        } else if (nodeToDelete.dataSet.size() > 1) {
            deleteDataFromNode(data, nodeToDelete);
        } else {
            RbNode<T, K> nodeToDeleteParent;
            if (nodeToDelete.leftChild != null && nodeToDelete.rightChild != null) {
                RbNode<T, K> nextInOrder = findNextInOrder(nodeToDelete.rightChild);
                nodeToDelete.dataSet = nextInOrder.dataSet;
                nodeToDeleteParent = nextInOrder.parent;
                eliminateNode(nextInOrder.bstKey(), nodeToDelete.rightChild);
            } else {
                eliminateNode(nodeToDelete.bstKey(), nodeToDelete);
                nodeToDeleteParent = nodeToDelete.parent;
            }
            if (nodeToDeleteParent != null) {
                onNodeDeletion(nodeToDeleteParent);
            }
        }
        size--;
    }

    /**
     * Eliminates a node from a subtree. At this point, we should have already
     * checked that the node must be eliminated from the tree as its last
     * element have just been removed.
     *
     * @param key the key of the node to eliminate.
     * @param currentRoot the root of the subtree interested by the operation.
     */
    private void eliminateNode(K key, RbNode<T, K> currentRoot) {
        RbNode<T, K> nodeToEliminate = findNodeInSubTree(key, currentRoot);
        RbNode<T, K> child = nodeToEliminate.leftChild != null
                ? nodeToEliminate.leftChild : nodeToEliminate.rightChild;
        if (nodeToEliminate.isRed()) {
            removeAndReconnect(nodeToEliminate, child);
        } else if (child != null && child.isRed()) {
            removeAndReconnect(nodeToEliminate, child);
            child.setBlack();
        } else {
            eliminate_case1(nodeToEliminate);
            removeAndReconnect(nodeToEliminate, null);
        }
    }

    /**
     * Removes a node that have one or zero children are reconnects the eventual
     * two components of the tree.
     *
     * @param nodeToEliminate the node to be eliminated.
     * @param child the child of such node, which might be null if the node to
     * eliminate has zero children.
     */
    private void removeAndReconnect(RbNode<T, K> nodeToEliminate, RbNode<T, K> child) {
        if (nodeToEliminate.parent != null) {
            if (nodeToEliminate.isLeftChild()) {
                nodeToEliminate.parent.leftChild = child;
            } else {
                nodeToEliminate.parent.rightChild = child;
            }
        } else {
            root = child;
        }
        if (child != null) {
            child.parent = nodeToEliminate.parent;
        }
    }

    /**
     * Deals with the first special case of node elimination. Handles current
     * node is the new root.
     *
     * @param currentNode the current node.
     */
    private void eliminate_case1(RbNode<T, K> currentNode) {
        if (currentNode.parent != null) {
            eliminate_case2(currentNode);
        }
    }

    /**
     * Deals with the second special case of node elimination. Handles sibling
     * is red.
     *
     * @param currentNode the current node.
     */
    private void eliminate_case2(RbNode<T, K> currentNode) {
        RbNode<T, K> sibling = currentNode.sibling();
        if (sibling.isRed()) {
            currentNode.parent.setRed();
            sibling.setBlack();
            if (currentNode.isLeftChild()) {
                rotateLeft(sibling);
            } else {
                rotateRight(sibling);
            }
        }
        eliminate_case3(currentNode);
    }

    /**
     * Deals with the third special case of node elimination. Handles parent,
     * sibling and sibling's children are all black.
     *
     * @param currentNode the current node.
     */
    private void eliminate_case3(RbNode<T, K> currentNode) {
        RbNode<T, K> sibling = currentNode.sibling();
        if (currentNode.parent.isBlack() && sibling.isBlack()
                && isNullOrBlack(sibling.leftChild) && isNullOrBlack(sibling.rightChild)) {
            sibling.setRed();
            eliminate_case1(currentNode.parent);
        } else {
            eliminate_case4(currentNode);
        }
    }

    /**
     * Deals with the forth special case of node elimination. Handles siblings
     * and sibling's children are black, but parent is red.
     *
     * @param currentNode the current node.
     */
    private void eliminate_case4(RbNode<T, K> currentNode) {
        RbNode<T, K> sibling = currentNode.sibling();
        if (currentNode.parent.isRed() && sibling.isBlack()
                && isNullOrBlack(sibling.leftChild) && isNullOrBlack(sibling.rightChild)) {
            sibling.setRed();
            currentNode.parent.setBlack();
        } else {
            eliminate_case5(currentNode);
        }
    }

    /**
     * Deals with the fifth special case of node elimination. Handles sibling is
     * black, the sibling's child toward current node is red, the other
     * sibling's child is black.
     *
     * @param currentNode the current node.
     */
    private void eliminate_case5(RbNode<T, K> currentNode) {
        RbNode<T, K> sibling = currentNode.sibling();
        if (currentNode.isLeftChild() && isNullOrBlack(sibling.rightChild)) {
            sibling.setRed();
            sibling.leftChild.setBlack();
            rotateRight(sibling.leftChild);
        } else if (currentNode.isRightChild() && isNullOrBlack(sibling.leftChild)) {
            sibling.setRed();
            sibling.rightChild.setBlack();
            rotateLeft(sibling.rightChild);
        }
        eliminate_case6(currentNode);
    }

    /**
     * Deals with the sixth special case of node elimination. Handles sibling is
     * black and the sibling's child which is not toward current node is red.
     *
     * @param currentNode the current node.
     */
    private void eliminate_case6(RbNode<T, K> currentNode) {
        RbNode<T, K> sibling = currentNode.sibling();
        sibling.isBlack = currentNode.parent.isBlack;
        currentNode.parent.setBlack();
        if (currentNode.isLeftChild()) {
            sibling.rightChild.setBlack();
            rotateLeft(sibling);
        } else {
            sibling.leftChild.setBlack();
            rotateRight(sibling);
        }
    }

    @Override
    public void deleteAll(Collection<T> dataSet) {
        for (T data : dataSet) {
            delete(data);
        }
    }

    @Override
    public void deleteKey(K key) {
        Set<T> results = getAll(key);
        for (T result : results) {
            delete(result);
        }
    }

    @Override
    public boolean contains(T data) {
        RbNode<T, K> dataNode = findNode(data.bstKey());
        if (dataNode != null) {
            return dataNode.dataSet.contains(data);
        } else {
            return false;
        }
    }

    @Override
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public List<T> inOrderTraversal() {
        List<T> results = new ArrayList<>(size());
        inOrderTraversalRecursion(results, root);
        return results;
    }

    /**
     * Performs the recursion that allow to collect the tree elements according
     * to a in-order traversal.
     *
     * @param results the list of results.
     * @param currentRoot the current root of the tree.
     */
    private void inOrderTraversalRecursion(List<T> results, RbNode<T, K> currentRoot) {
        if (currentRoot == null) {
            return;
        }
        inOrderTraversalRecursion(results, currentRoot.leftChild);
        results.addAll(currentRoot.dataSet);
        inOrderTraversalRecursion(results, currentRoot.rightChild);
    }

    @Override
    public Iterator<T> iterator() {
        return inOrderTraversal().iterator();
    }

    @Override
    public String toString() {
        String output = "";
        List<String> lines = new ArrayList<>();
        toStringRecursion("", root, lines);
        for (String line : lines) {
            output = output + line + "\n";
        }
        return output;
    }

    /**
     * Performs the recursion that allows to print the tree (sideways) in a text
     * output.
     *
     * @param prefix the current prefix for the subtree.
     * @param currentRoot the current root of the subtree.
     * @param lines the collection of output lines.
     */
    private void toStringRecursion(String prefix, RbNode<T, K> currentRoot, List<String> lines) {
        if (currentRoot == null) {
            lines.add("--null tree--");
        } else {
            if (currentRoot.rightChild != null) {
                toStringRecursion(prefix + "   ", currentRoot.rightChild, lines);
            }
            lines.add(prefix + currentRoot.toString());
            if (currentRoot.leftChild != null) {
                toStringRecursion(prefix + "   ", currentRoot.leftChild, lines);
            }
        }
    }

    /**
     * Creates a new red-black node containing the given data.
     *
     * @param data the data
     * @return the red-black node.
     */
    protected RbNode<T, K> createNewNode(T data) {
        return new RbNode<>(data);
    }

    /**
     * Inserts data in a node dataset.
     *
     * @param data the data.
     * @param node the node in which the data should be inserted.
     */
    protected void insertDataInNode(T data, RbNode<T, K> node) {
        node.dataSet.add(data);
    }

    /**
     * Deletes data from a node dataset.
     *
     * @param data the data.
     * @param node the node from which the data should be deleted.
     */
    protected void deleteDataFromNode(T data, RbNode<T, K> node) {
        node.dataSet.remove(data);
    }

    /**
     * Retrieves the node in the tree associated to the given key.
     *
     * @param queryKey the key value to find.
     * @return the red-black node associated to the key, or null if it does not
     * exist.
     */
    protected RbNode<T, K> findNode(K queryKey) {
        return findNodeInSubTree(queryKey, root);
    }

    /**
     * Retrieves the node in the given subtree associated to the given key.
     *
     * @param queryKey the key value to find.
     * @param currentRoot the root of the subtree to explore.
     * @return the red-black node associated to the key, or null if it does not
     * exist.
     */
    protected RbNode<T, K> findNodeInSubTree(K queryKey, RbNode<T, K> currentRoot) {
        if (currentRoot == null) {
            return currentRoot;
        }
        switch (queryKey.compareTo(currentRoot.bstKey())) {
            case -1:
                return findNodeInSubTree(queryKey, currentRoot.leftChild);
            case 0:
                return currentRoot;
            case 1:
                return findNodeInSubTree(queryKey, currentRoot.rightChild);
            default:
                throw new IllegalStateException("CompareTo has returned a different value than -1, 0 or 1.");
        }
    }

    /**
     * Finds the last node in the tree which have been visited when searching
     * for a given key.
     *
     * @param queryKey the key value to find.
     * @return the node associated with a given key, or the last visited node if
     * a node with such key does not exists, or null if the tree is empty.
     */
    protected RbNode<T, K> findLastNode(K queryKey) {
        return findLastNodeInSubTree(queryKey, root);
    }

    /**
     * Finds the last node in a given subtree which have been visited when
     * searching for a given key.
     *
     * @param queryKey the key value to find.
     * @param currentRoot the root of the subtree to explore.
     * @return the node associated with a given key, or the last visited node if
     * a node with such key does not exists, or null if the subtree is empty.
     */
    protected RbNode<T, K> findLastNodeInSubTree(K queryKey, RbNode<T, K> currentRoot) {
        if (currentRoot == null) {
            return null;
        }
        switch (queryKey.compareTo(currentRoot.bstKey())) {
            case -1:
                if (currentRoot.leftChild == null) {
                    return currentRoot;
                } else {
                    return findLastNodeInSubTree(queryKey, currentRoot.leftChild);
                }
            case 0:
                return currentRoot;
            case 1:
                if (currentRoot.rightChild == null) {
                    return currentRoot;
                } else {
                    return findLastNodeInSubTree(queryKey, currentRoot.rightChild);
                }
            default:
                throw new IllegalStateException("CompareTo has returned a different value than -1, 0 or 1.");
        }
    }

    /**
     * Hook for operations to perform on node insertion.
     *
     * @param insertedNode the inserted node.
     */
    protected void onNodeInsertion(RbNode<T, K> insertedNode) {
    }

    /**
     * Hook for operations to perform on node deletion.
     *
     * @param deletedNodeParent the parent of the node that has just been
     * deleted.
     */
    protected void onNodeDeletion(RbNode<T, K> deletedNodeParent) {
    }

    /**
     * Finds the next node in an in-order scan of a subtree.
     *
     * @param currentRoot the current root of the subtree.
     * @return the next in-order node.
     */
    private RbNode<T, K> findNextInOrder(RbNode<T, K> currentRoot) {
        if (currentRoot.leftChild != null) {
            return findNextInOrder(currentRoot.leftChild);
        } else {
            return currentRoot;
        }
    }

    /**
     * Performs a left rotation which moves current node in the place of its
     * parent.
     *
     * @param currentNode the current node.
     */
    protected void rotateLeft(RbNode<T, K> currentNode) {
        RbNode<T, K> grandparent = currentNode.grandparent();
        RbNode<T, K> parent = currentNode.parent;
        RbNode<T, K> leftChild = currentNode.leftChild;
        if (grandparent == null) {
            root = currentNode;
        } else if (parent.isLeftChild()) {
            grandparent.leftChild = currentNode;
        } else {
            grandparent.rightChild = currentNode;
        }
        currentNode.parent = grandparent;
        currentNode.leftChild = parent;
        parent.parent = currentNode;
        parent.rightChild = leftChild;
        if (leftChild != null) {
            leftChild.parent = parent;
        }
    }

    /**
     * Performs a right rotation which moves current node in the place of its
     * parent.
     *
     * @param currentNode the current node.
     */
    protected void rotateRight(RbNode<T, K> currentNode) {
        RbNode<T, K> grandparent = currentNode.grandparent();
        RbNode<T, K> parent = currentNode.parent;
        RbNode<T, K> rightChild = currentNode.rightChild;
        if (grandparent == null) {
            root = currentNode;
        } else if (parent.isLeftChild()) {
            grandparent.leftChild = currentNode;
        } else {
            grandparent.rightChild = currentNode;
        }
        currentNode.parent = grandparent;
        currentNode.rightChild = parent;
        parent.parent = currentNode;
        parent.leftChild = rightChild;
        if (rightChild != null) {
            rightChild.parent = parent;
        }
    }

    /**
     * Checks if a node is black or if it is null, which is black by definition.
     *
     * @param node the node to check.
     * @return true if the node is null or black.
     */
    private boolean isNullOrBlack(RbNode<T, K> node) {
        return node == null || node.isBlack();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(new HashSet<>(inOrderTraversal()));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RedBlackTree<?, ?> other = (RedBlackTree<?, ?>) obj;
        return Objects.equals(new HashSet<>(inOrderTraversal()),
                new HashSet<>(other.inOrderTraversal()));
    }

    /**
     * A red-black tree node.
     *
     * @param <T> the type of data contained in this tree node.
     * @param <K> the type of key values associated to the nodes.
     */
    public static class RbNode<T extends BinarySearchTree.Data<K>, K extends Comparable<K>> implements BinarySearchTree.Data<K> {

        protected Set<T> dataSet = new HashSet<>();
        private boolean isBlack = true;
        protected RbNode<T, K> parent;
        protected RbNode<T, K> leftChild;
        protected RbNode<T, K> rightChild;

        /**
         * Creates a red-black node that contains the given abstract one.
         *
         * @param data the abstract node that initialise this red-black node.
         */
        public RbNode(T data) {
            dataSet.add(data);
        }

        /**
         * Checks if the node is black.
         *
         * @return true if the node is black.
         */
        public boolean isBlack() {
            return isBlack;
        }

        /**
         * Checks if the node is red.
         *
         * @return true if the node is red.
         */
        public boolean isRed() {
            return !isBlack;
        }

        /**
         * Sets the node colour to black.
         */
        public void setBlack() {
            isBlack = true;
        }

        /**
         * Sets the node colour to red.
         */
        public void setRed() {
            isBlack = false;
        }

        /**
         * Retrieves the grandparent of a node.
         *
         * @return the node's grandparent.
         */
        public RbNode<T, K> grandparent() {
            if (parent == null) {
                return null;
            } else {
                return parent.parent;
            }
        }

        /**
         * Retrieves the uncle of a node.
         *
         * @return the node's uncle.
         */
        public RbNode<T, K> uncle() {
            RbNode<T, K> grandparent = grandparent();
            if (grandparent == null) {
                return null;
            } else if (parent.isLeftChild()) {
                return grandparent.rightChild;
            } else {
                return grandparent.leftChild;
            }
        }

        /**
         * Retrieves the sibling of a node.
         *
         * @return the node's sibling.
         */
        public RbNode<T, K> sibling() {
            if (parent == null) {
                return null;
            }
            if (isLeftChild()) {
                return parent.rightChild;
            } else {
                return parent.leftChild;
            }
        }

        /**
         * Checks is a node is a left child.
         *
         * @return true if the node is a left child.
         */
        public boolean isLeftChild() {
            return this == parent.leftChild;
        }

        /**
         * Checks if a node is a right child.
         *
         * @return true if the node is a right child.
         */
        public boolean isRightChild() {
            return this == parent.rightChild;
        }

        @Override
        public K bstKey() {
            assert (dataSet.size() > 0) : "Asking for the key of a RbNode with no abstract nodes.";
            return dataSet.iterator().next().bstKey();
        }

        @Override
        public String toString() {
            String decorator = isBlack() ? "_" : "^";
            return decorator + bstKey().toString() + decorator;
        }
    }
}
