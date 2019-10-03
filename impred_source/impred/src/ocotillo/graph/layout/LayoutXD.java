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
package ocotillo.graph.layout;

import java.util.ArrayList;
import java.util.List;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;

/**
 * Collects several methods relevant to a 2D layout of a graph.
 */
public class LayoutXD {

    /**
     * Returns the points associated to an edge. These points include the
     * position of the edge source and target. The method use the standard
     * attributes for the node positions and the edge control points.
     *
     * @param edge the edge.
     * @param graph the graph.
     * @return the edge points.
     */
    public static List<Coordinates> edgePoints(Edge edge, Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        return edgePoints(edge, positions, edgePoints);
    }

    /**
     * Returns the points associated to an edge. These points include the
     * position of the edge source and target.
     *
     * @param edge the edge.
     * @param positions the node positions.
     * @param edgePoints the edge control points. If null, all edges have no
     * bends.
     * @return the edge points.
     */
    public static List<Coordinates> edgePoints(Edge edge, NodeAttribute<Coordinates> positions, EdgeAttribute<ControlPoints> edgePoints) {
        List<Coordinates> result = new ArrayList<>();
        result.add(positions.get(edge.source()));
        if (edgePoints != null) {
            result.addAll(edgePoints.get(edge));
        }
        result.add(positions.get(edge.target()));
        return result;
    }

    /**
     * Returns the length of an edge. The method use the standard attributes for
     * the node positions, the edge control points and the edge shape.
     *
     * @param edge the edge.
     * @param graph the graph.
     * @return the edge length.
     */
    public static double edgeLength(Edge edge, Graph graph) {
        if (!graph.hasNodeAttribute(StdAttribute.nodePosition)) {
            throw new IllegalStateException("The node positions are not defined");
        }
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        EdgeAttribute<ControlPoints> edgePoints = graph.hasEdgeAttribute(StdAttribute.edgePoints) ? graph.<ControlPoints>edgeAttribute(StdAttribute.edgePoints) : null;
        EdgeAttribute<EdgeShape> edgeShapes = graph.hasEdgeAttribute(StdAttribute.edgeShape) ? graph.<EdgeShape>edgeAttribute(StdAttribute.edgeShape) : null;
        return edgeLength(edge, positions, edgePoints, edgeShapes);

    }

    /**
     * Returns the length of an edge.
     *
     * @param edge the edge.
     * @param positions the node positions.
     * @param edgePoints the edge control points. If null, all edges have no
     * bends.
     * @param edgeShapes the edge shape. If null, all edges are poly-line.
     * @return the edge points.
     */
    public static double edgeLength(Edge edge, NodeAttribute<Coordinates> positions, EdgeAttribute<ControlPoints> edgePoints, EdgeAttribute<EdgeShape> edgeShapes) {
        List<Coordinates> points = edgePoints(edge, positions, edgePoints);
        EdgeShape shape = edgeShapes != null ? edgeShapes.get(edge) : EdgeShape.polyline;
        switch (shape) {
            case polyline:
                double length = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    length += Geom.eXD.magnitude(points.get(i).minus(points.get(i + 1)));
                }
                return length;
            default:
                throw new UnsupportedOperationException("Lenght computation for this kind of edge is not supported yet.");
        }
    }

}
