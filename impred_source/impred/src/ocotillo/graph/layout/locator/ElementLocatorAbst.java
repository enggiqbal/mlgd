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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.GeomE;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;

public abstract class ElementLocatorAbst implements ElementLocator {

    protected final Graph graph;
    protected final GeomE geometry;
    protected final int geomDim;
    protected final NodePolicy nodePolicy;
    protected final EdgePolicy edgePolicy;
    protected final NodeAttribute<Coordinates> nodePositions;
    protected final NodeAttribute<Coordinates> nodeSizes;
    protected final EdgeAttribute<ControlPoints> edgePoints;
    protected final EdgeAttribute<Double> edgeWidths;
    protected final Map<Node, Box> nodeBoxes = new HashMap<>();
    protected final Map<Edge, Box> edgeBoxes = new HashMap<>();

    /**
     * Constructor for a standard element locator.
     *
     * @param graph the graph.
     * @param geometry the geometry to be used.
     * @param nodePolicy the node policy.
     * @param edgePolicy the edge policy.
     */
    protected ElementLocatorAbst(Graph graph, GeomE geometry, NodePolicy nodePolicy, EdgePolicy edgePolicy) {
        this.graph = graph;
        this.geometry = geometry;
        this.geomDim = geometry.geomDim();
        this.nodePolicy = nodePolicy;
        this.edgePolicy = edgePolicy;
        this.nodePositions = graph.nodeAttribute(StdAttribute.nodePosition);
        this.nodeSizes = graph.nodeAttribute(StdAttribute.nodeSize);
        this.edgePoints = graph.edgeAttribute(StdAttribute.edgePoints);
        this.edgeWidths = graph.edgeAttribute(StdAttribute.edgeWidth);
    }

    @Override
    public Box getBox(Node node) {
        if (!nodeBoxes.containsKey(node)) {
            updateBox(node);
        }
        return nodeBoxes.get(node);
    }

    /**
     * Updates the box for this node.
     *
     * @param node the node.
     */
    protected void updateBox(Node node) {
        nodeBoxes.put(node, computeBox(node));
    }

    /**
     * Computes the box for this node.
     *
     * @param node the node.
     * @return its box.
     */
    protected Box computeBox(Node node) {
        if (nodePolicy == NodePolicy.nodesAsGlyphs) {
            return Box.boundingBox(nodePositions.get(node).restr(geomDim))
                    .expand(nodeSizes.get(node).restr(geomDim));
        } else {
            return Box.boundingBox(nodePositions.get(node));
        }
    }

    @Override
    public Box getBox(Edge edge) {
        if (!edgeBoxes.containsKey(edge)) {
            updateBox(edge);
        }
        return edgeBoxes.get(edge);
    }

    /**
     * Updates the box for this edge.
     *
     * @param edge the edge.
     */
    protected void updateBox(Edge edge) {
        edgeBoxes.put(edge, computeBox(edge));
    }

    /**
     * Computes the box for this edge.
     *
     * @param edge the edge.
     * @return its box.
     */
    protected Box computeBox(Edge edge) {
        List<Coordinates> points = new ArrayList<>(edgePoints.get(edge));
        points.add(nodePositions.get(edge.source()));
        points.add(nodePositions.get(edge.target()));
        for (int i = 0; i < points.size(); i++) {
            points.set(i, points.get(i).restr(geomDim));
        }
        if (edgePolicy == EdgePolicy.edgesAsGlyphs) {
            return Box.boundingBox(points).expand(edgeWidths.get(edge));
        } else {
            return Box.boundingBox(points);
        }
    }

    @Override
    public Collection<Node> getCloseNodes(Coordinates point, double radius) {
        return getNodesPartiallyInBox(Box.boundingBox(point).expand(radius));
    }

    @Override
    public Collection<Node> getCloseNodes(List<Coordinates> polyline, double radius) {
        return getNodesPartiallyInBox(Box.boundingBox(polyline, radius));
    }

    @Override
    public Collection<Node> getCloseNodes(Node node, double radius) {
        Collection<Node> nodes = getNodesPartiallyInBox(getBox(node).expand(radius));
        nodes.remove(node);
        return nodes;
    }

    @Override
    public Collection<Node> getCloseNodes(Edge edge, double radius) {
        return getNodesPartiallyInBox(ElementLocatorAbst.this.getBox(edge).expand(radius));
    }

    @Override
    public Collection<Edge> getCloseEdges(Coordinates point, double radius) {
        return getEdgesPartiallyInBox(Box.boundingBox(point).expand(radius));
    }

    @Override
    public Collection<Edge> getCloseEdges(List<Coordinates> polyline, double radius) {
        return getEdgesPartiallyInBox(Box.boundingBox(polyline, radius));
    }

    @Override
    public Collection<Edge> getCloseEdges(Node node, double radius) {
        return getEdgesPartiallyInBox(getBox(node).expand(radius));
    }

    @Override
    public Collection<Edge> getCloseEdges(Edge edge, double radius) {
        Collection<Edge> edges = getEdgesPartiallyInBox(ElementLocatorAbst.this.getBox(edge).expand(radius));
        edges.remove(edge);
        return edges;
    }
}
