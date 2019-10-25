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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.MirrorConnection;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.MirrorLine;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.structures.IntervalTree;
import ocotillo.various.ColorCollection;
import ocotillo.various.PointCluster;
import ocotillo.various.PointClustering;

/**
 * Clustering for a dynamic graph.
 */
public interface DyClustering {

    /**
     * Colours the graph according to the clusters.
     */
    public void colorGraph();

    /**
     * Flattens the clusters into static graphs.
     *
     * @return the list of flatten clusters.
     */
    public List<Graph> flattenClusters();

    /**
     * Dynamic graph performed in the space-time cube.
     */
    public abstract static class Stc implements DyClustering {

        protected final DyGraph graph;
        protected final double timeFactor;
        protected final double edgeSampling;
        protected final ColorCollection colors;
        protected final Interval intervalOfInterest;
        protected final SpaceTimeCubeSynchroniser synchroniser;

        /**
         * Builds a space-time cube clustering method.
         *
         * @param graph the dynamic graph.
         * @param timeFactor the conversion factor of time. This indicates how
         * many space units correspond to a time unit.
         * @param edgeSampling the distance between points inserted along
         * dynamic edge extremities when present.
         * @param colors the colour collection to use.
         */
        public Stc(DyGraph graph, double timeFactor, double edgeSampling, ColorCollection colors) {
            this(graph, timeFactor, edgeSampling, colors, Interval.global);
        }

        /**
         * Builds a space-time cube clustering method.
         *
         * @param graph the dynamic graph.
         * @param timeFactor the conversion factor of time. This indicates how
         * many space units correspond to a time unit.
         * @param edgeSampling the distance between points inserted along
         * dynamic edge extremities when present.
         * @param colors the colour collection to use.
         * @param intervalOfInterest the interval to consider.
         */
        public Stc(DyGraph graph, double timeFactor, double edgeSampling, ColorCollection colors, Interval intervalOfInterest) {
            this.graph = graph;
            this.timeFactor = timeFactor;
            this.edgeSampling = edgeSampling;
            this.colors = colors;
            this.intervalOfInterest = intervalOfInterest;
            this.synchroniser
                    = new SpaceTimeCubeSynchroniser.StcsBuilder(graph, timeFactor).build();
        }

        /**
         * Transforms a space-time cube coordinate in the space used by the
         * clustering algorithm.
         *
         * @param coordinates the space-time cube coordinates.
         * @return the coordinates in the clustering space.
         */
        public abstract Coordinates transform(Coordinates coordinates);

        /**
         * Extracts the points to cluster from the space-time cube.
         *
         * @return the points to cluster.
         */
        protected List<Coordinates> extractPoints() {
            List<Coordinates> points = new ArrayList<>();

            for (MirrorLine line : synchroniser.mirrorLines()) {
                for (Coordinates point : line.bendsAndExtremities()) {
                    if (intervalOfInterest.contains(synchroniser.spaceToTime(point.z()))) {
                        points.add(transform(point));
                    }
                }
            }
            for (MirrorConnection connection : synchroniser.mirrorConnections()) {
                MirrorLine source = connection.sourceMirrorLine();
                MirrorLine target = connection.targetMirrorLine();
                Interval mirrorInterval = connection.mirrorInterval();
                for (Coordinates point : sampleConnection(source, mirrorInterval, edgeSampling)) {
                    if (intervalOfInterest.contains(synchroniser.spaceToTime(point.z()))) {
                        points.add(transform(point));
                    }
                }
                for (Coordinates point : sampleConnection(target, mirrorInterval, edgeSampling)) {
                    if (intervalOfInterest.contains(synchroniser.spaceToTime(point.z()))) {
                        points.add(transform(point));
                    }
                }
            }
            return points;
        }

        /**
         * Samples a mirror line in correspondence to a connection,
         *
         * @param line the mirror line to sample.
         * @param mirrorInterval the mirror interval of the connection.
         * @param edgeSampling the length after which resampling the mirror
         * line.
         * @return the sample points of the connection.
         */
        protected List<Coordinates> sampleConnection(MirrorLine line, Interval mirrorInterval, double edgeSampling) {
            int numberOfPoints = (int) Math.floor(mirrorInterval.width() / edgeSampling);
            double gap = mirrorInterval.width() / numberOfPoints;
            List<Coordinates> edgePoints = new ArrayList<>();
            for (double time = mirrorInterval.leftBound(); time <= mirrorInterval.rightBound(); time = time + gap) {
                edgePoints.add(line.positionAtMirrorTime(time));
            }
            return edgePoints;
        }

        /**
         * k-means dynamic clustering on the space-time cube.
         */
        public static abstract class KMeans extends Stc {

            protected final int k;
            protected PointClustering.KMeans clustering;

            /**
             * Builds a k-means algorithm that operate in the space-time cube.
             *
             * @param graph the dynamic graph.
             * @param timeFactor the conversion factor of time. This indicates
             * how many space units correspond to a time unit.
             * @param edgeSampling the distance between points inserted along
             * dynamic edge extremities when present.
             * @param k the number of clusters.
             * @param colors the colour collection to use.
             */
            public KMeans(DyGraph graph, double timeFactor, double edgeSampling, int k, ColorCollection colors) {
                super(graph, timeFactor, edgeSampling, colors);
                this.k = k;
            }

            /**
             * Builds a k-means algorithm that operate in the space-time cube.
             *
             * @param graph the dynamic graph.
             * @param timeFactor the conversion factor of time. This indicates
             * how many space units correspond to a time unit.
             * @param edgeSampling the distance between points inserted along
             * dynamic edge extremities when present.
             * @param k the number of clusters.
             * @param colors the colour collection to use.
             * @param intervalOfInterest the interval to consider.
             */
            public KMeans(DyGraph graph, double timeFactor, double edgeSampling, int k, ColorCollection colors, Interval intervalOfInterest) {
                super(graph, timeFactor, edgeSampling, colors, intervalOfInterest);
                this.k = k;
            }

            @Override
            public void colorGraph() {
                doClustering();
                DyNodeAttribute<Color> nodeColors = graph.nodeAttribute(StdAttribute.color);
                for (Node node : graph.nodes()) {
                    nodeColors.set(node, new Evolution<>(Color.BLACK));
                }

                for (MirrorLine line : synchroniser.mirrorLines()) {
                    Evolution<Color> evolution = nodeColors.get(line.original());
                    List<Coordinates> sampledLine = sampleLine(line);
                    for (int i = 1; i < sampledLine.size(); i++) {
                        Coordinates a = sampledLine.get(i - 1);
                        Coordinates b = sampledLine.get(i);
                        Color aColor = colors.get(clustering.findCluster(transform(a)));
                        Color bColor = colors.get(clustering.findCluster(transform(b)));
                        Interval interval = Interval.newClosed(
                                synchroniser.spaceToTime(a.z()), synchroniser.spaceToTime(b.z()));
                        evolution.insert(new FunctionRect.Color(interval,
                                aColor, bColor, Interpolation.Std.linear));
                    }
                }
            }

            @Override
            public List<Graph> flattenClusters() {
                doClustering();
                List<Coordinates> means = clustering.computeMeans();

                List<Graph> flattenedClusters = new ArrayList<>();
                for (int i = 0; i < clustering.size(); i++) {

                    Map<Node, Interval> nodeIntervals = buildNodeIntervals(i);
                    Map<Edge, Interval> edgeIntervals = buildEdgeIntervals(i);

                    Coordinates mean = means.get(i);
                    double atTime = mean.dim() >= 3 ? mean.z() : mean.x();

                    double startTime = Double.POSITIVE_INFINITY;
                    double endTime = Double.NEGATIVE_INFINITY;
                    for (Interval interval : nodeIntervals.values()) {
                        startTime = Math.min(startTime, interval.leftBound());
                        endTime = Math.max(endTime, interval.rightBound());
                    }

                    Graph flattenedCluster = DyFlattening.flatten(graph,
                            nodeIntervals, edgeIntervals, synchroniser.spaceToTime(atTime),
                            (endTime - startTime) / 6.0);

                    colorAllNodesWithCluster(flattenedCluster, i);
                    interpolateDefaultNodePositions(flattenedCluster, i);
                    flattenedClusters.add(flattenedCluster);
                }
                return flattenedClusters;
            }

            /**
             * Performs the clustering.
             */
            private void doClustering() {
                if (clustering == null) {
                    List<Coordinates> points = extractPoints();
                    clustering = new PointClustering.KMeans(k, points);
                    Collections.sort(clustering, (PointCluster t, PointCluster t1) -> {
                        int dim = t.mean().dim();
                        return dim >= 3 ? Double.compare(t.mean().z(), t1.mean().z())
                                : Double.compare(t.mean().x(), t1.mean().x());
                    });
                }
            }

            /**
             * Builds the map of the interval of appearance of each node in a
             * given cluster.
             *
             * @param clusterIdx the index of the desired cluster.
             * @return the map.
             */
            private Map<Node, Interval> buildNodeIntervals(int clusterIdx) {
                Map<Node, Interval> nodeIntervals = new HashMap<>();
                for (MirrorLine line : synchroniser.mirrorLines()) {
                    double startTime = Double.POSITIVE_INFINITY;
                    double endTime = Double.NEGATIVE_INFINITY;
                    for (Coordinates sample : sampleLine(line)) {
                        if (clustering.findCluster(transform(sample)) == clusterIdx) {
                            startTime = Math.min(startTime, synchroniser.spaceToTime(sample.z()));
                            endTime = Math.max(endTime, synchroniser.spaceToTime(sample.z()));
                        }
                    }
                    if (startTime < endTime) {
                        nodeIntervals.put(line.original(), Interval.newClosed(startTime, endTime));
                    }
                }
                return nodeIntervals;
            }

            /**
             * Builds the map of the interval of appearance of each edge in a
             * given cluster.
             *
             * @param clusterIdx the index of the desired cluster.
             * @return the map.
             */
            private Map<Edge, Interval> buildEdgeIntervals(int clusterIdx) {
                Map<Edge, Interval> edgeIntervals = new HashMap<>();
                for (MirrorConnection connection : synchroniser.mirrorConnections()) {
                    double startTime = Double.POSITIVE_INFINITY;
                    double endTime = Double.NEGATIVE_INFINITY;
                    MirrorLine source = connection.sourceMirrorLine();
                    MirrorLine target = connection.targetMirrorLine();
                    Interval interval = connection.mirrorInterval();
                    List<Coordinates> sampledSource = sampleConnection(source, interval, edgeSampling / 10);
                    List<Coordinates> sampledTarget = sampleConnection(target, interval, edgeSampling / 10);
                    assert (sampledSource.size() == sampledTarget.size()) : "The samples have not the same size.";
                    for (int j = 0; j < sampledSource.size(); j++) {
                        int sourceCluster = clustering.findCluster(transform(sampledSource.get(j)));
                        int targetCluster = clustering.findCluster(transform(sampledTarget.get(j)));
                        if (clusterIdx == sourceCluster && clusterIdx == targetCluster) {
                            double currentTime = synchroniser.spaceToTime(sampledSource.get(j).z());
                            startTime = Math.min(startTime, currentTime);
                            endTime = Math.max(endTime, currentTime);
                        }
                    }
                    if (startTime < endTime) {
                        edgeIntervals.put(connection.original(), Interval.newClosed(startTime, endTime));
                    }
                }
                return edgeIntervals;
            }

            /**
             * Samples a mirror line for detecting better cluster intervals.
             *
             * @param line the mirror line.
             * @return the sampled mirror line.
             */
            private List<Coordinates> sampleLine(MirrorLine line) {
                List<Coordinates> bends = line.bendsAndExtremities();

                List<Coordinates> sampledLine = new ArrayList<>();
                for (int i = 1; i < bends.size(); i++) {
                    Coordinates a = bends.get(i - 1);
                    Coordinates b = bends.get(i);

                    sampledLine.add(a);
                    int sampleMagnitude = 1;
                    double ab = Geom.e3D.magnitude(b.minus(a));
                    Coordinates unit = Geom.e3D.unitVector(b.minus(a));
                    while (sampleMagnitude < ab) {
                        sampledLine.add(unit.times(sampleMagnitude).plus(a));
                        sampleMagnitude++;
                    }
                }
                sampledLine.add(bends.get(bends.size() - 1));
                return sampledLine;
            }

            /**
             * Colours all nodes of the same cluster with the cluster colour.
             *
             * @param flattenedCluster the flattened cluster graph.
             * @param i the index of the cluster.
             */
            private void colorAllNodesWithCluster(Graph flattenedCluster, int i) {
                NodeAttribute<Color> nodeColors = flattenedCluster.nodeAttribute(StdAttribute.color);
                for (Node node : flattenedCluster.nodes()) {
                    int alpha = nodeColors.get(node).getAlpha();
                    Color solid = colors.get(i);
                    nodeColors.set(node, new Color(solid.getRed(), solid.getGreen(),
                            solid.getBlue(), alpha));
                }
            }

            /**
             * Interpolates the node positions for nodes that are not present at
             * the time of the cluster mean.
             *
             * @param flattenedCluster the flattened cluster graph.
             * @param i the index of the cluster.
             */
            private void interpolateDefaultNodePositions(Graph flattenedCluster, int i) {
                Coordinates mean = clustering.get(i).mean();
                double atTime = mean.dim() >= 3 ? mean.z() : mean.x();

                NodeAttribute<Coordinates> positions = flattenedCluster.nodeAttribute(StdAttribute.nodePosition);
                for (Node node : flattenedCluster.nodes()) {
                    if (positions.get(node).equals(new Coordinates(0, 0))) {
                        IntervalTree<MirrorLine> lines = synchroniser.mirrorLines(node);
                        ArrayList<Coordinates> bends = new ArrayList<>();
                        for (MirrorLine line : lines) {
                            bends.addAll(line.bendsAndExtremities());
                        }
                        Coordinates extractedCoordinates;
                        if (atTime <= bends.get(0).z()) {
                            extractedCoordinates = bends.get(0);
                        } else if (atTime >= bends.get(bends.size() - 1).z()) {
                            extractedCoordinates = bends.get(bends.size() - 1);
                        } else {
                            int j = 0;
                            while (atTime < bends.get(j).z()) {
                                j++;
                            }
                            extractedCoordinates = bends.get(j - 1)
                                    .plus(bends.get(j)).divide(2.0);
                        }
                        positions.set(node, extractedCoordinates.restr(2));
                    }
                }
            }
        }

        /**
         * k-means dynamic clustering performed on 3D in the space-time cube.
         */
        public static class KMeans3D extends KMeans {

            /**
             * Builds a k-means algorithm that operate in 3D in the space-time
             * cube.
             *
             * @param graph the dynamic graph.
             * @param timeFactor the conversion factor of time. This indicates
             * how many space units correspond to a time unit.
             * @param edgeSampling the distance between points inserted along
             * dynamic edge extremities when present.
             * @param k the number of clusters.
             * @param colors the colour collection to use.
             */
            public KMeans3D(DyGraph graph, double timeFactor, double edgeSampling, int k, ColorCollection colors) {
                super(graph, timeFactor, edgeSampling, k, colors);
            }

            /**
             * Builds a k-means algorithm that operate in 3D in the space-time
             * cube.
             *
             * @param graph the dynamic graph.
             * @param timeFactor the conversion factor of time. This indicates
             * how many space units correspond to a time unit.
             * @param edgeSampling the distance between points inserted along
             * dynamic edge extremities when present.
             * @param k the number of clusters.
             * @param colors the colour collection to use.
             * @param intervalOfInterest the interval to consider.
             */
            public KMeans3D(DyGraph graph, double timeFactor, double edgeSampling, int k, ColorCollection colors, Interval intervalOfInterest) {
                super(graph, timeFactor, edgeSampling, k, colors, intervalOfInterest);
            }

            @Override
            public Coordinates transform(Coordinates coordinates) {
                return coordinates;
            }
        }

        /**
         * k-means dynamic clustering performed on the time dimension in the
         * space-time cube.
         */
        public static class KMeansTime extends KMeans {

            /**
             * Builds a k-means algorithm that operate in 3D in the space-time
             * cube.
             *
             * @param graph the dynamic graph.
             * @param timeFactor the conversion factor of time. This indicates
             * how many space units correspond to a time unit.
             * @param edgeSampling the distance between points inserted along
             * dynamic edge extremities when present.
             * @param k the number of clusters.
             * @param colors the colour collection to use.
             */
            public KMeansTime(DyGraph graph, double timeFactor, double edgeSampling, int k, ColorCollection colors) {
                super(graph, timeFactor, edgeSampling, k, colors);
            }

            /**
             * Builds a k-means algorithm that operate in 3D in the space-time
             * cube.
             *
             * @param graph the dynamic graph.
             * @param timeFactor the conversion factor of time. This indicates
             * how many space units correspond to a time unit.
             * @param edgeSampling the distance between points inserted along
             * dynamic edge extremities when present.
             * @param k the number of clusters.
             * @param colors the colour collection to use.
             * @param intervalOfInterest the interval to consider.
             */
            public KMeansTime(DyGraph graph, double timeFactor, double edgeSampling, int k, ColorCollection colors, Interval intervalOfInterest) {
                super(graph, timeFactor, edgeSampling, k, colors, intervalOfInterest);
            }

            @Override
            public Coordinates transform(Coordinates coordinates) {
                return new Coordinates(coordinates.z());
            }
        }
    }
}
