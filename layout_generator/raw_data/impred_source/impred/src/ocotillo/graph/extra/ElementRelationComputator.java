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

import ocotillo.graph.Edge;
import ocotillo.graph.Node;

/**
 * A class that computes a result which is based on the relations between two
 * elements.
 *
 * @param <E> the type of the computed result.
 */
public interface ElementRelationComputator<E> {

    /**
     * Compute a relation between two nodes.
     *
     * @param nodeA the first node.
     * @param nodeB the second node.
     * @return the relation between the two.
     */
    public E compute(Node nodeA, Node nodeB);

    /**
     * Compute a relation between two edges.
     *
     * @param edgeA the first edge.
     * @param edgeB the second edge.
     * @return the relation between the two.
     */
    public E compute(Edge edgeA, Edge edgeB);

    /**
     * Compute a relation between a node and an edge.
     *
     * @param node the node.
     * @param edge the edge.
     * @return the relation between the two.
     */
    public E compute(Node node, Edge edge);

    /**
     * Hook for the classes that need to perform some operations before shutting
     * down.
     */
    public void close();

}
