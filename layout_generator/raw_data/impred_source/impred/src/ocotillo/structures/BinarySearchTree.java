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
import java.util.List;
import java.util.Set;

/**
 * Defines a binary search tree.
 *
 * @param <T> the type of data contained in the tree.
 * @param <K> the type of key values associated to the nodes.
 */
public interface BinarySearchTree<T extends BinarySearchTree.Data<K>, K extends Comparable<K>> extends Iterable<T> {

    /**
     * Gets a single data element that match the query key.
     *
     * @param queryKey the key to search.
     * @return a random data matching the query.
     */
    public T get(K queryKey);

    /**
     * Gets all the data that match the query key.
     *
     * @param queryKey the key to search.
     * @return the list of all data matching the query.
     */
    public Set<T> getAll(K queryKey);

    /**
     * Inserts the given data into the search tree.
     *
     * @param data the data to insert.
     */
    public void insert(T data);

    /**
     * Inserts all the given data into the search tree.
     *
     * @param dataSet the data to insert.
     */
    public void insertAll(Collection<T> dataSet);

    /**
     * Deletes the given data from the search tree.
     *
     * @param data the data to delete.
     */
    public void delete(T data);

    /**
     * Deletes all the given data in the search tree.
     *
     * @param dataSet the data to delete.
     */
    public void deleteAll(Collection<T> dataSet);

    /**
     * Deletes all nodes that match the given key.
     *
     * @param key the key to delete.
     */
    public void deleteKey(K key);

    /**
     * Checks is the tree contains the given data.
     *
     * @param data the data to find.
     * @return true if the data is contained in the tree.
     */
    public boolean contains(T data);

    /**
     * Checks is the tree contains the given key.
     *
     * @param key the key to find.
     * @return true if a node with such key exists.
     */
    public boolean containsKey(K key);

    /**
     * Returns the size of the search tree.
     *
     * @return the size.
     */
    public int size();

    /**
     * Checks if the tree is empty.
     *
     * @return true if the tree is empty.
     */
    public boolean isEmpty();

    /**
     * Clears the search tree.
     */
    public void clear();

    /**
     * Returns the list of nodes visited according to an in-order traversal.
     *
     * @return the in-order list of nodes.
     */
    List<T> inOrderTraversal();

    /**
     * Defines the data contained in the search tree.
     *
     * @param <K> the type of key value associated to the node.
     */
    public static interface Data<K extends Comparable<K>> {

        public K bstKey();
    }
}
