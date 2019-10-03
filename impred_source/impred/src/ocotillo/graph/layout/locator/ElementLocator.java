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
package ocotillo.graph.layout.locator;

import java.util.Collection;
import java.util.List;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;

/**
 * A structure that allows to quickly retrieve elements based on their position.
 * All the methods must ensure that all the required elements are returned, but
 * false positive are tolerated.
 */
public interface ElementLocator {

    /**
     * The policy to use with the graph nodes.
     */
    public enum NodePolicy {
        ignoreNodes,
        nodesAsPoints,
        nodesAsGlyphs;
    }

    /**
     * The policy to use with the graph edges.
     */
    public enum EdgePolicy {
        ignoreEdges,
        edgesAsLines,
        edgesAsGlyphs;
    }

    /**
     * Hook for the element locators that are not automatically updated and need
     * to be manually refreshed.
     */
    public void rebuild();

    /**
     * Hook for the element locators that need to perform some operations before
     * shutting down.
     */
    public void close();

    /**
     * Gets the node box.
     *
     * @param node the node.
     * @return its box.
     */
    public Box getBox(Node node);

    /**
     * Gets the edge box.
     *
     * @param edge the edge.
     * @return its box.
     */
    public Box getBox(Edge edge);

    /**
     * Gets the nodes fully contained in the given box.
     *
     * @param box the box.
     * @return the contained nodes.
     */
    public Collection<Node> getNodesPartiallyInBox(Box box);

    /**
     * Gets the nodes contained (even partially) in the given box.
     *
     * @param box the box.
     * @return the contained nodes.
     */
    public Collection<Node> getNodesFullyInBox(Box box);

    /**
     * Gets the edges contained (even partially) in the box.
     *
     * @param box the box.
     * @return the contained edges.
     */
    public Collection<Edge> getEdgesPartiallyInBox(Box box);

    /**
     * Gets the edges fully contained in the box.
     *
     * @param box the box.
     * @return the contained edges.
     */
    public Collection<Edge> getEdgesFullyInBox(Box box);

    /**
     * Get all the nodes closer than radius from the given point. Might return
     * other nodes as well.
     *
     * @param point the centre.
     * @param radius the desired radius.
     * @return the close nodes.
     */
    public Collection<Node> getCloseNodes(Coordinates point, double radius);

    /**
     * Get all the nodes closer than radius from the given polygonal chain.
     * Might return other nodes as well.
     *
     * @param polyline the polygonal chain.
     * @param radius the desired radius.
     * @return the close nodes.
     */
    public Collection<Node> getCloseNodes(List<Coordinates> polyline, double radius);

    /**
     * Get all the nodes closer than radius from the given node. Might return
     * other nodes as well. The node itself is not returned.
     *
     * @param node the central node.
     * @param radius the desired radius.
     * @return the close nodes.
     */
    public Collection<Node> getCloseNodes(Node node, double radius);

    /**
     * Get all the nodes closer than radius from the given edge. Might return
     * other nodes as well.
     *
     * @param edge the central node.
     * @param radius the desired radius.
     * @return the close nodes.
     */
    public Collection<Node> getCloseNodes(Edge edge, double radius);

    /**
     * Get all the edges closer than radius from the given point. Might return
     * other edges as well.
     *
     * @param point the centre.
     * @param radius the desired radius.
     * @return the close nodes.
     */
    public Collection<Edge> getCloseEdges(Coordinates point, double radius);

    /**
     * Get all the edges closer than radius from the given polygonal chain.
     * Might return other edges as well.
     *
     * @param polyline the polygonal chain.
     * @param radius the desired radius.
     * @return the close edges.
     */
    public Collection<Edge> getCloseEdges(List<Coordinates> polyline, double radius);

    /**
     * Get all the edges closer than radius from the given node. Might return
     * other edges as well.
     *
     * @param node the central node.
     * @param radius the desired radius.
     * @return the close edges.
     */
    public Collection<Edge> getCloseEdges(Node node, double radius);

    /**
     * Get all the edges closer than radius from the given edge. Might return
     * other edges as well. The edge itself is not returned.
     *
     * @param edge the central node.
     * @param radius the desired radius.
     * @return the close edges.
     */
    public Collection<Edge> getCloseEdges(Edge edge, double radius);

}
