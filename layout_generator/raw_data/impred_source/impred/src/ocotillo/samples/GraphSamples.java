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
package ocotillo.samples;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

/**
 * Collection of GraphSamples.
 */
public class GraphSamples {

    /**
     * A graph with three node on the positive axe of each axis.
     *
     * @return the graph.
     */
    public static Graph nodesOnAxes() {
        Graph graph = new Graph();
        Node o = graph.newNode();
        Node ax = graph.newNode();
        Node bx = graph.newNode();
        Node cx = graph.newNode();
        Node ay = graph.newNode();
        Node by = graph.newNode();
        Node cy = graph.newNode();
        Node az = graph.newNode();
        Node bz = graph.newNode();
        Node cz = graph.newNode();

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        positions.set(ax, new Coordinates(3, 0, 0));
        positions.set(bx, new Coordinates(6, 0, 0));
        positions.set(cx, new Coordinates(9, 0, 0));
        positions.set(ay, new Coordinates(0, 3, 0));
        positions.set(by, new Coordinates(0, 6, 0));
        positions.set(cy, new Coordinates(0, 9, 0));
        positions.set(az, new Coordinates(0, 0, 3));
        positions.set(bz, new Coordinates(0, 0, 6));
        positions.set(cz, new Coordinates(0, 0, 9));

        NodeAttribute<String> labels = graph.nodeAttribute(StdAttribute.label);
        labels.set(ax, "x");
        labels.set(bx, "x");
        labels.set(cx, "x");
        labels.set(ay, "y");
        labels.set(by, "y");
        labels.set(cy, "y");
        labels.set(az, "z");
        labels.set(bz, "z");
        labels.set(cz, "z");

        return graph;
    }

}
