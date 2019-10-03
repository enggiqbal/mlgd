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

import lombok.EqualsAndHashCode;

/**
 * The edge of a graph.
 */
@EqualsAndHashCode(callSuper = true)
public final class Edge extends Element {

    private final Node source;
    private final Node target;

    /**
     * Constructs an edge.
     *
     * @param id the edge id.
     * @param source the edge source.
     * @param target the edge target.
     */
    public Edge(String id, Node source, Node target) {
        super(id);
        this.source = source;
        this.target = target;
    }

    /**
     * Gets the source of an edge.
     *
     * @return the edge source.
     */
    public Node source() {
        return source;
    }

    /**
     * Gets the target of an edge.
     *
     * @return the edge target.
     */
    public Node target() {
        return target;
    }

    /**
     * Gets the other ending point of and edge.
     *
     * @param node the ending point of an edge.
     * @return the other ending point.
     */
    public Node otherEnd(Node node) {
        if (node.equals(source)) {
            return target;
        }
        if (node.equals(target)) {
            return source;
        }
        return null;
    }

    /**
     * Checks if a node is an extremity of the edge.
     *
     * @param node the node.
     * @return true if the node is the edge source or target, false otherwise.
     */
    public boolean isNodeExtremity(Node node) {
        return source.equals(node) || target.equals(node);
    }

}
