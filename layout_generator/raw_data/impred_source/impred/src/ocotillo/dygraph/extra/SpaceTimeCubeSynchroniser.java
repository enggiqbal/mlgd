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
package ocotillo.dygraph.extra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Element;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.structures.IntervalTree;

/**
 * A class that builds and synchronises a space-time-cube 3D graphs from a 2D
 * dynamic graph. Nodes of the dynamic graph will be represented by polylines in
 * the mirror graph defined by the node presence and coordinates. Dynamic edges
 * are not directly depicted.
 */
public class SpaceTimeCubeSynchroniser {

    private final DyGraph originalGraph;
    private final Graph mirrorGraph;
    private final double timeFactor;

    private final DyNodeAttribute<Boolean> dyNodePresence;
    private final DyEdgeAttribute<Boolean> dyEdgePresence;
    private final DyNodeAttribute<Coordinates> dyNodePositions;

    private final NodeAttribute<Coordinates> mirrorPositions;
    private final EdgeAttribute<ControlPoints> mirrorBends;

    private final Map<Node, IntervalTree<MirrorLine>> directNodeMap = new HashMap<>();
    private final Map<Edge, IntervalTree<MirrorConnection>> directEdgeMap = new HashMap<>();
    private final Map<Element, MirrorLine> reverseMap = new HashMap<>();

    private final List<MirrorLine> mirrorLineList = new ArrayList<>();
    private final List<MirrorConnection> mirrorConnectionList = new ArrayList<>();

    /**
     * Builder for space-time-cube synchronisers.
     */
    public static class StcsBuilder {

        private final DyGraph dyGraph;
        private final double timeFactor;

        /**
         * Constructs a space-time-cube synchroniser builder.
         *
         * @param dyGraph the dynamic graph.
         * @param timeFactor the conversion factor of time. This indicates how
         * many space units correspond to a time unit.
         */
        public StcsBuilder(DyGraph dyGraph, double timeFactor) {
            this.dyGraph = dyGraph;
            this.timeFactor = timeFactor;
        }

        /**
         * Builds the space-time cube synchroniser.
         *
         * @return the synchroniser instance.
         */
        public SpaceTimeCubeSynchroniser build() {
            return new SpaceTimeCubeSynchroniser(dyGraph, timeFactor);
        }
    }

    /**
     * Private constructor.
     *
     * @param dyGraph the dynamic graph.
     * @param timeFactor the conversion factor of time. This indicates how many
     * space units correspond to a time unit.
     */
    private SpaceTimeCubeSynchroniser(DyGraph dyGraph, double timeFactor) {
        this.originalGraph = dyGraph;
        this.timeFactor = timeFactor;

        this.dyNodePresence = dyGraph.nodeAttribute(StdAttribute.dyPresence);
        this.dyEdgePresence = dyGraph.edgeAttribute(StdAttribute.dyPresence);
        this.dyNodePositions = dyGraph.nodeAttribute(StdAttribute.nodePosition);

        this.mirrorGraph = new Graph();
        this.mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);
        this.mirrorBends = mirrorGraph.edgeAttribute(StdAttribute.edgePoints);
        buildMirror();
    }

    /**
     * Builds the mirror graph.
     */
    private void buildMirror() {
        for (Node node : originalGraph.nodes()) {
            Evolution<Boolean> presence = dyNodePresence.get(node);
            List<Interval> appearances = EvolutionAnalyser.getIntervalsWithValue(presence, true);
            IntervalTree<MirrorLine> mirrorEdges = new IntervalTree<>();
            for (Interval appearance : appearances) {
                MirrorLine mirrorEdge = new MirrorLine(node, appearance);
                mirrorEdges.insert(mirrorEdge);
                reverseMap.put(mirrorEdge.mirrorEdge, mirrorEdge);
                reverseMap.put(mirrorEdge.mirrorSource, mirrorEdge);
                reverseMap.put(mirrorEdge.mirrorTarget, mirrorEdge);
                mirrorLineList.add(mirrorEdge);
            }
            directNodeMap.put(node, mirrorEdges);
        }
        for (Edge edge : originalGraph.edges()) {
            Evolution<Boolean> presence = dyEdgePresence.get(edge);
            List<Interval> appearances = EvolutionAnalyser.getIntervalsWithValue(presence, true);
            IntervalTree<MirrorConnection> mirrorConnections = new IntervalTree<>();
            for (Interval appearance : appearances) {
                MirrorConnection mirrorConnection = new MirrorConnection(edge, appearance);
                mirrorConnections.insert(mirrorConnection);
                mirrorConnectionList.add(mirrorConnection);
            }
            directEdgeMap.put(edge, mirrorConnections);
        }
    }

    /**
     * Returns the original graph.
     *
     * @return the original graph.
     */
    public DyGraph originalGraph() {
        return originalGraph;
    }

    /**
     * Returns the mirror graph.
     *
     * @return the mirror graph.
     */
    public Graph mirrorGraph() {
        return mirrorGraph;
    }

    /**
     * Returns a list of all MirrorLines.
     *
     * @return the mirror line.
     */
    public List<MirrorLine> mirrorLines() {
        return mirrorLineList;
    }

    /**
     * Returns the mirror lines of a given original node.
     *
     * @param node the original node.
     * @return the mirror lines associated with this node.
     */
    public IntervalTree<MirrorLine> mirrorLines(Node node) {
        return directNodeMap.get(node);
    }

    /**
     * Return the original node associated with this mirror node or edge.
     *
     * @param mirrorElement the node or edge.
     * @return the original node associated with it.
     */
    public Node getOriginalNode(Element mirrorElement) {
        return reverseMap.get(mirrorElement).original();
    }

    /**
     * Returns a list of all MirrorConnections.
     *
     * @return the mirror connections.
     */
    public List<MirrorConnection> mirrorConnections() {
        return mirrorConnectionList;
    }

    /**
     * Updates the original graph.
     */
    public void updateOriginal() {
        originalGraph.startBulkNotification();
        for (Node node : originalGraph.nodes()) {
            Evolution<Coordinates> evolution = new Evolution<>(new Coordinates(0, 0));
            dyNodePositions.set(node, evolution);
            for (MirrorLine mirrorEdge : directNodeMap.get(node)) {
                for (Function<Coordinates> function : mirrorEdge.computeFunctions()) {
                    evolution.insert(function);
                }
            }
        }
        originalGraph.stopBulkNotification();
    }

    /**
     * Converts time values into space.
     *
     * @param time the time value to convert.
     * @return the converted space value.
     */
    protected double timeToSpace(double time) {
        return time * timeFactor;
    }

    /**
     * Converts space values into time.
     *
     * @param space the space value to convert.
     * @return the converted time value.
     */
    protected double spaceToTime(double space) {
        return space / timeFactor;
    }

    /**
     * The mirror entity of a node appearance in the dynamic graph. Such entity
     * is a line in the space-time cube, which is represented as an edge in the
     * mirror graph.
     */
    public class MirrorLine implements IntervalTree.Data {

        private final Node original;
        private final Interval interval;
        private final Interval mirrorInterval;
        private final Edge mirrorEdge;
        private final Node mirrorSource;
        private final Node mirrorTarget;

        /**
         * Builds a mirror line.
         *
         * @param original the node in the original dynamic graph.
         * @param appearance the appearance interval represented by this entity.
         */
        private MirrorLine(Node original, Interval appearance) {
            this.original = original;
            this.interval = appearance;
            double spaceStart = timeToSpace(appearance.leftBound());
            double spaceEnd = timeToSpace(appearance.rightBound());
            this.mirrorInterval = Interval.newCustom(spaceStart, spaceEnd,
                    appearance.isLeftClosed(), appearance.isRightClosed());

            mirrorSource = mirrorGraph.newNode();
            mirrorTarget = mirrorGraph.newNode();
            mirrorEdge = mirrorGraph.newEdge(mirrorSource, mirrorTarget);

            Coordinates startPos = dyNodePositions.get(original).valueAt(appearance.leftBound());
            mirrorPositions.set(mirrorSource, new Coordinates(startPos.x(), startPos.y(), timeToSpace(appearance.leftBound())));
            Coordinates endPos = dyNodePositions.get(original).valueAt(appearance.rightBound());
            mirrorPositions.set(mirrorTarget, new Coordinates(endPos.x(), endPos.y(), timeToSpace(appearance.rightBound())));

            List<Coordinates> bends = new ArrayList<>();
            for (Function<Coordinates> function : dyNodePositions.get(original)) {
                if (appearance.leftBound() < function.interval().rightBound() && function.interval().rightBound() < appearance.rightBound()) {
                    Coordinates originalPos = function.rightValue();
                    Coordinates transformedPos = new Coordinates(originalPos.x(), originalPos.y(), timeToSpace(function.interval().rightBound()));
                    bends.add(transformedPos);
                }
            }
            mirrorBends.set(mirrorEdge, new ControlPoints(bends));
        }

        /**
         * The original node.
         *
         * @return the original node.
         */
        public Node original() {
            return original;
        }

        /**
         * The interval of appearance.
         *
         * @return the appearance interval.
         */
        @Override
        public Interval interval() {
            return interval;
        }

        /**
         * The appearance interval in space coordinates.
         *
         * @return the mirror interval.
         */
        public Interval mirrorInterval() {
            return mirrorInterval;
        }

        /**
         * Returns the edge of the mirror graph that correspond to this line.
         *
         * @return the edge that represent the mirror line.
         */
        public Edge mirrorEdge() {
            return mirrorEdge;
        }

        /**
         * Returns the mirror edge source.
         *
         * @return the mirror edge source.
         */
        public Node mirrorSource() {
            return mirrorSource;
        }

        /**
         * Returns the mirror edge target.
         *
         * @return the mirror edge target.
         */
        public Node mirrorTarget() {
            return mirrorTarget;
        }

        /**
         * Gets the bends of this mirror line.
         *
         * @return the line bends.
         */
        public List<Coordinates> bends() {
            return new ArrayList<>(mirrorBends.get(mirrorEdge()));
        }

        /**
         * Gets the list of source, bends and target positions for this mirror
         * line.
         *
         * @return the list of positions.
         */
        public List<Coordinates> bendsAndExtremities() {
            List<Coordinates> result = new ArrayList<>();
            result.add(mirrorPositions.get(mirrorSource));
            result.addAll(mirrorBends.get(mirrorEdge()));
            result.add(mirrorPositions.get(mirrorTarget));
            return result;
        }

        /**
         * Gets the position of the line for the given converted time value.
         *
         * @param mirrorTime the time value converted to space unit.
         * @return the position of the line at this time.
         */
        public Coordinates positionAtMirrorTime(double mirrorTime) {
            assert (mirrorTime >= mirrorInterval.leftBound() && mirrorTime <= mirrorInterval.rightBound()) :
                    "The value of mirror time requested is not in the mirror interval of the line.";
            List<Coordinates> points = bendsAndExtremities();
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i).z() == mirrorTime) {
                    return new Coordinates(points.get(i));
                } else if (points.get(i).z() > mirrorTime) {
                    Coordinates a = points.get(i - 1);
                    Coordinates b = points.get(i);
                    double factor = (mirrorTime - a.z()) / (b.z() - a.z());
                    Coordinates offset = b.minus(a).times(factor);
                    return a.plus(offset);
                }
            }
            throw new IllegalStateException("The computation should not arrive here.");
        }

        /**
         * Returns the corresponding dynamic graph position function.
         *
         * @return the position function.
         */
        public List<Function<Coordinates>> computeFunctions() {
            List<Function<Coordinates>> result = new ArrayList<>();
            Coordinates previousPos = mirrorPositions.get(mirrorSource).restr(2);
            double previousTime = interval.leftBound();
            boolean leftClosed = interval.isLeftClosed();
            for (Coordinates bend : mirrorBends.get(mirrorEdge)) {
                double currentTime = spaceToTime(bend.z());
                Coordinates currentPos = bend.restr(2);
                result.add(new FunctionRect.Coordinates(
                        Interval.newCustom(previousTime, currentTime, leftClosed, true),
                        previousPos, currentPos, Interpolation.Std.linear));
                previousPos = currentPos;
                previousTime = currentTime;
                leftClosed = false;
            }
            Coordinates finalPos = mirrorPositions.get(mirrorTarget).restr(2);
            result.add(new FunctionRect.Coordinates(
                    Interval.newCustom(previousTime, interval.rightBound(), leftClosed, interval.isRightClosed()),
                    previousPos, finalPos, Interpolation.Std.linear));
            return result;
        }
    }

    /**
     * The mirror entity of an edge appearance in the dynamic graph. Such entity
     * is a trapezoid surface in the space-time cube, and it is currently not
     * physically represented in the mirror graph.
     */
    public class MirrorConnection implements IntervalTree.Data {

        private final Edge original;
        private final Interval interval;
        private final Interval mirrorInterval;
        private final MirrorLine sourceLine;
        private final MirrorLine targetLine;

        /**
         * Builds a mirror connection.
         *
         * @param original the edge in the original dynamic graph.
         * @param interval the appearance interval represented by this entity.
         */
        private MirrorConnection(Edge original, Interval interval) {
            this.original = original;
            this.interval = interval;
            double spaceStart = timeToSpace(interval.leftBound());
            double spaceEnd = timeToSpace(interval.rightBound());
            this.mirrorInterval = Interval.newCustom(spaceStart, spaceEnd,
                    interval.isLeftClosed(), interval.isRightClosed());
            double middlePoint = (interval.leftBound() + interval.rightBound()) / 2.0;
            this.sourceLine = directNodeMap.get(original.source()).getAnyContaining(middlePoint);
            this.targetLine = directNodeMap.get(original.target()).getAnyContaining(middlePoint);
            assert (sourceLine != null) : "Could not find the source line for "
                    + middlePoint + " in:\n" + directNodeMap.get(original.source());
            assert (targetLine != null) : "Could not find the target line for "
                    + middlePoint + " in:\n" + directNodeMap.get(original.target());
        }

        /**
         * The original node.
         *
         * @return the original node.
         */
        public Edge original() {
            return original;
        }

        /**
         * The interval of appearance.
         *
         * @return the appearance interval.
         */
        @Override
        public Interval interval() {
            return interval;
        }

        /**
         * The appearance interval in space coordinates.
         *
         * @return the mirror interval.
         */
        public Interval mirrorInterval() {
            return mirrorInterval;
        }

        /**
         * Returns the mirror line that corresponds to the connection source.
         *
         * @return the source mirror line.
         */
        public MirrorLine sourceMirrorLine() {
            return sourceLine;
        }

        /**
         * Returns the mirror line that corresponds to the connection target.
         *
         * @return the target mirror line.
         */
        public MirrorLine targetMirrorLine() {
            return targetLine;
        }

        @Override
        public String toString() {
            String result = "";
            result += "Connection for " + original + " at " + interval + "\n";
            result += "    source " + sourceLine + " target " + targetLine;
            return result;
        }
    }
}
