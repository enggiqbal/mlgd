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

import java.awt.Color;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.EvoBuilder;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.samples.parsers.Commons;
import ocotillo.samples.parsers.Commons.DyDataSet;

/**
 * Collection of GraphSamples.
 */
public class DyGraphSamples {

    /**
     * Graph showing tree nodes that collide at time 10.
     *
     * @return the graph.
     */
    public static DyGraph collidingNodes() {
        DyGraph graph = new DyGraph();

        Node a = graph.newNode();
        Node b = graph.newNode();
        Node c = graph.newNode();

        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        position.set(a, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 10), new Coordinates(10, 10), new Coordinates(0, 0), Interpolation.Std.mediumEndCharge)
                .build());
        position.set(b, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 10), new Coordinates(-10, 0), new Coordinates(0, 0), Interpolation.Std.linear)
                .build());
        position.set(c, EvoBuilder.defaultAt(new Coordinates(0, 0))
                .withRect(Interval.newClosed(0, 10), new Coordinates(0, 10), new Coordinates(0, 0), Interpolation.Std.largeGaussian)
                .build());

        DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
        color.set(c, EvoBuilder.defaultAt(Color.red)
                .withRect(Interval.newClosed(0, 10), Color.red, Color.yellow, Interpolation.Std.mediumGaussian)
                .build());

        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        label.set(a, EvoBuilder.defaultAt("a")
                .withRect(Interval.newClosed(0, 10), "a", "O", Interpolation.Std.step)
                .build());

        return graph;
    }

    public static DyDataSet discretisationExample() {
        DyGraph graph = new DyGraph();
        DyNodeAttribute<Boolean> nodePresence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);

        Node a = graph.newNode();
        Node b = graph.newNode();

        nodePresence.set(a, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 9), true)
                .withConst(Interval.newClosed(10, 19), true)
                .withConst(Interval.newClosed(20, 29), true)
                .withConst(Interval.newClosed(30, 39), true)
                .withConst(Interval.newClosed(40, 49), true)
                .withConst(Interval.newClosed(50, 59), true)
                .withConst(Interval.newClosed(60, 69), true)
                .withConst(Interval.newClosed(70, 79), true)
                .withConst(Interval.newClosed(80, 89), true)
                .withConst(Interval.newClosed(90, 100), true)
                .build());
        nodePresence.set(b, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(3, 6), true)
                .withConst(Interval.newClosed(13, 16), true)
                .withConst(Interval.newClosed(23, 26), true)
                .withConst(Interval.newClosed(33, 36), true)
                .withConst(Interval.newClosed(43, 46), true)
                .withConst(Interval.newClosed(53, 56), true)
                .withConst(Interval.newClosed(63, 66), true)
                .withConst(Interval.newClosed(73, 76), true)
                .withConst(Interval.newClosed(83, 86), true)
                .withConst(Interval.newClosed(93, 96), true)
                .build());

        Edge ab = graph.newEdge(a, b);

        edgePresence.set(ab, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(4, 5), true)
                .withConst(Interval.newClosed(14, 15), true)
                .withConst(Interval.newClosed(24, 25), true)
                .withConst(Interval.newClosed(34, 35), true)
                .withConst(Interval.newClosed(44, 45), true)
                .withConst(Interval.newClosed(54, 55), true)
                .withConst(Interval.newClosed(64, 65), true)
                .withConst(Interval.newClosed(74, 75), true)
                .withConst(Interval.newClosed(84, 85), true)
                .withConst(Interval.newClosed(94, 95), true)
                .build());

        Node c = graph.newNode();
        Node d = graph.newNode();
        Node e = graph.newNode();
        Node f = graph.newNode();
        Node g = graph.newNode();

        nodePresence.set(c, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 20), true)
                .withConst(Interval.newClosed(90, 120), true)
                .build());
        nodePresence.set(d, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(10, 40), true)
                .build());
        nodePresence.set(e, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(30, 60), true)
                .build());
        nodePresence.set(f, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(50, 80), true)
                .build());
        nodePresence.set(g, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(70, 100), true)
                .build());

        Edge cd = graph.newEdge(c, d);
        Edge de = graph.newEdge(d, e);
        Edge ef = graph.newEdge(e, f);
        Edge fg = graph.newEdge(f, g);
        Edge gc = graph.newEdge(g, c);

        edgePresence.set(cd, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(10, 20), true)
                .build());
        edgePresence.set(de, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(30, 40), true)
                .build());
        edgePresence.set(ef, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(50, 60), true)
                .build());
        edgePresence.set(fg, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(70, 80), true)
                .build());
        edgePresence.set(gc, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(90, 100), true)
                .build());

        Node h = graph.newNode();
        Node i = graph.newNode();
        Node j = graph.newNode();
        Node l = graph.newNode();
        Node m = graph.newNode();

        Node n = graph.newNode();
        Node o = graph.newNode();
        Node p = graph.newNode();
        Node q = graph.newNode();

        nodePresence.set(h, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(i, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(j, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(l, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(m, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(n, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(o, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(p, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());
        nodePresence.set(q, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 100), true)
                .build());

        Edge hi = graph.newEdge(h, i);
        Edge hj = graph.newEdge(h, j);
        Edge hl = graph.newEdge(h, l);
        Edge hm = graph.newEdge(h, m);
        Edge ij = graph.newEdge(i, j);
        Edge jl = graph.newEdge(j, l);
        Edge im = graph.newEdge(i, m);
        Edge mn = graph.newEdge(m, n);
        Edge ln = graph.newEdge(l, n);
        Edge lq = graph.newEdge(l, q);
        Edge jq = graph.newEdge(j, q);
        Edge no = graph.newEdge(n, o);
        Edge np = graph.newEdge(n, p);
        Edge nq = graph.newEdge(n, q);
        Edge oq = graph.newEdge(o, q);
        Edge op = graph.newEdge(o, p);

        edgePresence.set(hi, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 10), true)
                .withConst(Interval.newClosed(11, 16), true)
                .withConst(Interval.newClosed(21, 45), true)
                .withConst(Interval.newClosed(70, 97), true)
                .build());
        edgePresence.set(hj, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 6), true)
                .withConst(Interval.newClosed(8, 13), true)
                .withConst(Interval.newClosed(16, 18), true)
                .withConst(Interval.newClosed(24, 32), true)
                .withConst(Interval.newClosed(35, 42), true)
                .withConst(Interval.newClosed(73, 79), true)
                .withConst(Interval.newClosed(80, 85), true)
                .withConst(Interval.newClosed(87, 96), true)
                .build());
        edgePresence.set(hl, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(6, 23), true)
                .withConst(Interval.newClosed(30, 43), true)
                .withConst(Interval.newClosed(47, 56), true)
                .withConst(Interval.newClosed(79, 83), true)
                .withConst(Interval.newClosed(84, 99), true)
                .build());
        edgePresence.set(hm, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(1, 21), true)
                .withConst(Interval.newClosed(21, 28), true)
                .withConst(Interval.newClosed(32, 41), true)
                .withConst(Interval.newClosed(69, 85), true)
                .withConst(Interval.newClosed(87, 97), true)
                .build());
        edgePresence.set(ij, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 8), true)
                .withConst(Interval.newClosed(11, 33), true)
                .withConst(Interval.newClosed(34, 42), true)
                .withConst(Interval.newClosed(78, 88), true)
                .withConst(Interval.newClosed(89, 98), true)
                .build());
        edgePresence.set(jl, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(0, 16), true)
                .withConst(Interval.newClosed(18, 25), true)
                .withConst(Interval.newClosed(28, 42), true)
                .withConst(Interval.newClosed(69, 83), true)
                .withConst(Interval.newClosed(86, 98), true)
                .build());
        edgePresence.set(im, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(4, 7), true)
                .withConst(Interval.newClosed(10, 15), true)
                .withConst(Interval.newClosed(18, 28), true)
                .withConst(Interval.newClosed(33, 47), true)
                .withConst(Interval.newClosed(78, 79), true)
                .withConst(Interval.newClosed(80, 99), true)
                .build());
        edgePresence.set(mn, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(15, 18), true)
                .withConst(Interval.newClosed(45, 46), true)
                .withConst(Interval.newClosed(61, 62), true)
                .withConst(Interval.newClosed(93, 96), true)
                .build());
        edgePresence.set(ln, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(3, 5), true)
                .withConst(Interval.newClosed(35, 36), true)
                .withConst(Interval.newClosed(45, 48), true)
                .withConst(Interval.newClosed(68, 69), true)
                .withConst(Interval.newClosed(88, 90), true)
                .build());
        edgePresence.set(lq, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(11, 13), true)
                .withConst(Interval.newClosed(25, 28), true)
                .withConst(Interval.newClosed(40, 42), true)
                .withConst(Interval.newClosed(61, 62), true)
                .withConst(Interval.newClosed(80, 82), true)
                .build());
        edgePresence.set(jq, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(14, 16), true)
                .withConst(Interval.newClosed(36, 39), true)
                .withConst(Interval.newClosed(52, 53), true)
                .withConst(Interval.newClosed(87, 88), true)
                .build());
        edgePresence.set(no, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(4, 7), true)
                .withConst(Interval.newClosed(40, 51), true)
                .withConst(Interval.newClosed(53, 67), true)
                .withConst(Interval.newClosed(69, 76), true)
                .build());
        edgePresence.set(np, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(23, 25), true)
                .withConst(Interval.newClosed(39, 56), true)
                .withConst(Interval.newClosed(57, 69), true)
                .withConst(Interval.newClosed(72, 81), true)
                .build());
        edgePresence.set(nq, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(46, 53), true)
                .withConst(Interval.newClosed(54, 67), true)
                .withConst(Interval.newClosed(69, 73), true)
                .build());
        edgePresence.set(oq, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(35, 39), true)
                .withConst(Interval.newClosed(45, 48), true)
                .withConst(Interval.newClosed(50, 67), true)
                .withConst(Interval.newClosed(68, 75), true)
                .build());
        edgePresence.set(op, EvoBuilder.defaultAt(false)
                .withConst(Interval.newClosed(46, 53), true)
                .withConst(Interval.newClosed(56, 59), true)
                .withConst(Interval.newClosed(62, 69), true)
                .withConst(Interval.newClosed(72, 84), true)
                .build());

        Commons.scatterNodes(graph, 30);

        return new DyDataSet(graph, 0.1, Interval.newClosed(1, 99));
    }
}
