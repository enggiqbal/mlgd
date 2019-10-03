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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.GeomE;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

/**
 * Describes the computation of a graph metric.
 *
 * @param <T> the type of data output.
 */
public interface GraphMetric<T> {

    /**
     * Computes the metric for this graph.
     *
     * @param graph the input graph.
     * @return the metric for that graph.
     */
    public T computeMetric(Graph graph);

    /**
     * A metric that collects the computed values in a list.
     *
     * @param <T> the type of data output.
     */
    public static class LoggedMetric<T> implements GraphMetric<T> {

        private final List<T> values = new ArrayList<>();
        private final GraphMetric<T> metric;

        /**
         * Builds a logged metric.
         *
         * @param metric the metric.
         */
        public LoggedMetric(GraphMetric<T> metric) {
            this.metric = metric;
        }

        /**
         * Gets the computed values.
         *
         * @return the values.
         */
        public List<T> getValues() {
            return values;
        }

        /**
         * Returns the actual metric.
         *
         * @return the metric.
         */
        public GraphMetric<T> getMetric() {
            return metric;
        }

        /**
         * Computes the metric value and store it in the list.
         *
         * @param graph the input graph.
         * @return the metric for that graph.
         */
        @Override
        public T computeMetric(Graph graph) {
            T value = metric.computeMetric(graph);
            values.add(value);
            return value;
        }

        /**
         * Clears the computed values.
         */
        public void clear() {
            values.clear();
        }
    }

    /**
     * Computes the shortest theoretical distance between every pair of nodes.
     */
    public static class NodeTheoreticalDistancesMetric implements GraphMetric<NodeDistances> {

        private final String weightAttributeId;
        private final EdgeAttribute<Double> weightAttribute;
        private final boolean asDirectedGraph;

        /**
         * Builder for node distances metric.
         */
        public static class Builder {

            private String weightAttributeId;
            private EdgeAttribute<Double> weightAttribute;
            private boolean asDirectedGraph = false;

            /**
             * Indicates an attribute id to use to extract edge weights,
             *
             * @param attributeId the id of the attribute.
             * @return the builder.
             */
            public Builder withWeight(String attributeId) {
                if (weightAttribute != null && attributeId != null) {
                    throw new IllegalStateException("Weight id and attribute are mutually exclusive.");
                }
                this.weightAttributeId = attributeId;
                return this;
            }

            /**
             * Indicates an attribute to use to extract edge weights,
             *
             * @param weightAttribute the edge weight attribute.
             * @return the builder.
             */
            public Builder withWeight(EdgeAttribute<Double> weightAttribute) {
                if (weightAttributeId != null && weightAttribute != null) {
                    throw new IllegalStateException("Weight id and attribute are mutually exclusive.");
                }
                this.weightAttribute = weightAttribute;
                return this;
            }

            /**
             * Indicates whether the edges should be considered directed or
             * undirected. Default is undirected.
             *
             * @param asDirectedGraph true if edges must be interpreted as
             * directed.
             * @return the builder.
             */
            public Builder withDirectedEdges(boolean asDirectedGraph) {
                this.asDirectedGraph = asDirectedGraph;
                return this;
            }

            /**
             * Builds the metrics.
             *
             * @return the metrics.
             */
            public NodeTheoreticalDistancesMetric build() {
                return new NodeTheoreticalDistancesMetric(weightAttributeId, weightAttribute, asDirectedGraph);
            }
        }

        /**
         * Create a metric that compute the shortest distances.
         *
         * @param weightAttributeId the id of the attribute containing the edge
         * weights.
         * @param weightAttribute the weight attribute.
         * @param asDirectedGraph true if the graph edges should be considered
         * as directed edges.
         */
        private NodeTheoreticalDistancesMetric(String weightAttributeId, EdgeAttribute<Double> weightAttribute, boolean asDirectedGraph) {
            this.weightAttributeId = weightAttributeId;
            this.weightAttribute = weightAttribute;
            this.asDirectedGraph = asDirectedGraph;
        }

        @Override
        public NodeDistances computeMetric(Graph graph) {
            NodeDistances result = new NodeDistances(graph);

            EdgeAttribute<Double> weight;
            if (weightAttribute != null) {
                weight = weightAttribute;
            } else if (weightAttributeId != null) {
                weight = graph.edgeAttribute(weightAttributeId);
            } else {
                weight = new EdgeAttribute<>(1.0);
            }

            for (Edge edge : graph.edges()) {
                int sourceIdx = result.directMap.get(edge.source());
                int targetIdx = result.directMap.get(edge.target());
                result.distances[sourceIdx][targetIdx] = weight.get(edge);
                if (!asDirectedGraph) {
                    result.distances[targetIdx][sourceIdx] = weight.get(edge);
                }
            }
            for (int i = 0; i < result.distances.length; i++) {
                for (int j = 0; j < result.distances.length; j++) {
                    for (int k = 0; k < result.distances.length; k++) {
                        result.distances[i][j] = Math.min(result.distances[i][j],
                                result.distances[i][k] + result.distances[k][j]);
                        if (!asDirectedGraph) {
                            result.distances[j][i] = result.distances[i][j];
                        }
                    }
                }
            }
            return result;
        }
    }

    /**
     * Computes the spacial distance between every pair of nodes.
     */
    public static class NodeSpacialDistancesMetric implements GraphMetric<NodeDistances> {

        private final GeomE geometry;

        /**
         * Computes the spacial distance between nodes in 2D.
         */
        public NodeSpacialDistancesMetric() {
            geometry = Geom.e2D;
        }

        /**
         * Computes the spacial distance between nodes in the given geometry.
         *
         * @param geometry the geometry.
         */
        public NodeSpacialDistancesMetric(GeomE geometry) {
            this.geometry = geometry;
        }

        @Override
        public NodeDistances computeMetric(Graph graph) {
            NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
            NodeDistances result = new NodeDistances(graph);
            for (int i = 0; i < result.distances.length; i++) {
                Node a = result.reverseMap[i];
                Coordinates aPos = positions.get(a);
                for (int j = i + 1; j < result.distances.length; j++) {
                    Node b = result.reverseMap[j];
                    Coordinates bPos = positions.get(b);
                    result.distances[i][j] = geometry.magnitude(bPos.minus(aPos));
                    result.distances[j][i] = result.distances[i][j];
                }
            }
            return result;
        }
    }

    /**
     * Stores distances between nodes.
     */
    public static class NodeDistances {

        private final Map<Node, Integer> directMap;
        private final Node[] reverseMap;
        private final double[][] distances;

        /**
         * Initialises the structure.
         *
         * @param graph the input graph.
         */
        private NodeDistances(Graph graph) {
            int nodeCount = graph.nodes().size();
            directMap = new HashMap<>();
            reverseMap = new Node[nodeCount];
            distances = new double[nodeCount][nodeCount];

            for (int i = 0; i < nodeCount; i++) {
                for (int j = 0; j < nodeCount; j++) {
                    distances[i][j] = Double.POSITIVE_INFINITY;
                }
            }

            int i = 0;
            for (Node node : graph.nodes()) {
                directMap.put(node, i);
                reverseMap[i] = node;
                distances[i][i] = 0;
                i++;
            }
        }

        /**
         * Gets the shortest distance between two nodes.
         *
         * @param a the first node.
         * @param b the second node.
         * @return their distance.
         */
        public double get(Node a, Node b) {
            return distances[directMap.get(a)][directMap.get(b)];
        }
    }

    /**
     * Computes the stress metric. The definition used is that of Brandes and
     * Maden, "A Quantitative Comparison of Stress-Minimization Approaches for
     * Offline Dynamic Graph Drawing". It corresponds to Sum((a-b)/a)^2, where a
     * is the theoretical distance and b the spacial distance between a couple
     * of nodes. The contribute of a disconnected pair of nodes is discarded.
     */
    public static class StressMetric implements GraphMetric<Double> {

        private final String weightAttributeId;
        private final EdgeAttribute<Double> weightAttribute;
        private final boolean asDirectedGraph;
        private final double scalingFactor;
        private final GeomE geometry;

        /**
         * Builder for the metric.
         */
        public static class Builder {

            private String weightAttributeId;
            private EdgeAttribute<Double> weightAttribute;
            private boolean asDirectedGraph = false;
            private double scalingFactor = 1.0;
            private GeomE geometry = Geom.e2D;

            /**
             * Indicates an attribute id to use to extract edge weights,
             *
             * @param attributeId the id of the attribute.
             * @return the builder.
             */
            public Builder withWeight(String attributeId) {
                if (weightAttribute != null && attributeId != null) {
                    throw new IllegalStateException("Weight id and attribute are mutually exclusive.");
                }
                this.weightAttributeId = attributeId;
                return this;
            }

            /**
             * Indicates an attribute to use to extract edge weights,
             *
             * @param weightAttribute the edge weight attribute.
             * @return the builder.
             */
            public Builder withWeight(EdgeAttribute<Double> weightAttribute) {
                if (weightAttributeId != null && weightAttribute != null) {
                    throw new IllegalStateException("Weight id and attribute are mutually exclusive.");
                }
                this.weightAttribute = weightAttribute;
                return this;
            }

            /**
             * Indicates whether the edges should be considered directed or
             * undirected. Default is undirected.
             *
             * @param asDirectedGraph true if edges must be interpreted as
             * directed.
             * @return the builder.
             */
            public Builder withDirectedEdges(boolean asDirectedGraph) {
                this.asDirectedGraph = asDirectedGraph;
                return this;
            }

            /**
             * The scaling to be used in the physical graph. The scaling factor
             * can be used to adjust the current space scale to the desired
             * space unit, for instance by indicating how many current space
             * units correspond to an edge of theoretical length one. Default is
             * 1.
             *
             * @param scalingFactor the scaling factor.
             * @return the builder.
             */
            public Builder withScaling(double scalingFactor) {
                this.scalingFactor = scalingFactor;
                return this;
            }

            /**
             * Indicates the geometry to be used to compute the spacial distance
             * between nodes. Default is Euclidean 2D.
             *
             * @param geometry the geometry to be used.
             * @return the builder.
             */
            public Builder withGeometry(GeomE geometry) {
                this.geometry = geometry;
                return this;
            }

            /**
             * Builds the metrics.
             *
             * @return the metrics.
             */
            public StressMetric build() {
                return new StressMetric(weightAttributeId, weightAttribute, asDirectedGraph, scalingFactor, geometry);
            }
        }

        /**
         * Create a metric that compute the graph stress.
         *
         * @param weightAttributeId the id of the attribute containing the edge
         * weights.
         * @param weightAttribute the weight attribute.
         * @param asDirectedGraph true if the graph edges should be considered
         * as directed edges.
         * @param scalingFactor the scaling factor.
         * @param geometry the geometry to be used.
         */
        private StressMetric(String weightAttributeId, EdgeAttribute<Double> weightAttribute, boolean asDirectedGraph, double scalingFactor, GeomE geometry) {
            this.weightAttributeId = weightAttributeId;
            this.weightAttribute = weightAttribute;
            this.asDirectedGraph = asDirectedGraph;
            this.scalingFactor = scalingFactor;
            this.geometry = geometry;
        }

        @Override
        public Double computeMetric(Graph graph) {
            if (graph.edgeCount() == 0) {
                return Double.NaN;
            }

            NodeTheoreticalDistancesMetric theoreticalDistMetric
                    = new NodeTheoreticalDistancesMetric.Builder()
                            .withWeight(weightAttribute)
                            .withWeight(weightAttributeId)
                            .withDirectedEdges(asDirectedGraph)
                            .build();
            NodeSpacialDistancesMetric spacialDistMetric
                    = new NodeSpacialDistancesMetric(geometry);

            NodeDistances theoreticalDist = theoreticalDistMetric.computeMetric(graph);
            NodeDistances spacialDist = spacialDistMetric.computeMetric(graph);

            double sum = 0;
            for (int i = 0; i < theoreticalDist.distances.length; i++) {
                Node first = theoreticalDist.reverseMap[i];
                for (int j = i + 1; j < theoreticalDist.distances.length; j++) {
                    Node second = theoreticalDist.reverseMap[j];
                    double a = theoreticalDist.distances[i][j];
                    double b = spacialDist.get(first, second) / scalingFactor;
                    if (a != Double.POSITIVE_INFINITY) {
                        sum += Math.pow((a - b) / a, 2.0);
                    }
                }
            }
            return sum;
        }
    }

    /**
     * Computes the scaling that gives the better stress metric. The contribute
     * of a disconnected pair of nodes is discarded.
     */
    public static class IdealStressScalingMetric implements GraphMetric<Double> {

        private final String weightAttributeId;
        private final EdgeAttribute<Double> weightAttribute;
        private final boolean asDirectedGraph;
        private final double scalingFactor;
        private final GeomE geometry;

        /**
         * Builder for the metric.
         */
        public static class Builder {

            private String weightAttributeId;
            private EdgeAttribute<Double> weightAttribute;
            private boolean asDirectedGraph = false;
            private double scalingFactor = 1.0;
            private GeomE geometry = Geom.e2D;

            /**
             * Indicates an attribute id to use to extract edge weights,
             *
             * @param attributeId the id of the attribute.
             * @return the builder.
             */
            public Builder withWeight(String attributeId) {
                if (weightAttribute != null && attributeId != null) {
                    throw new IllegalStateException("Weight id and attribute are mutually exclusive.");
                }
                this.weightAttributeId = attributeId;
                return this;
            }

            /**
             * Indicates an attribute to use to extract edge weights,
             *
             * @param weightAttribute the edge weight attribute.
             * @return the builder.
             */
            public Builder withWeight(EdgeAttribute<Double> weightAttribute) {
                if (weightAttributeId != null && weightAttribute != null) {
                    throw new IllegalStateException("Weight id and attribute are mutually exclusive.");
                }
                this.weightAttribute = weightAttribute;
                return this;
            }

            /**
             * Indicates whether the edges should be considered directed or
             * undirected. Default is undirected.
             *
             * @param asDirectedGraph true if edges must be interpreted as
             * directed.
             * @return the builder.
             */
            public Builder withDirectedEdges(boolean asDirectedGraph) {
                this.asDirectedGraph = asDirectedGraph;
                return this;
            }

            /**
             * The scaling to be used in the physical graph. The scaling factor
             * can be used to adjust the current space scale to the desired
             * space unit, for instance by indicating how many current space
             * units correspond to an edge of theoretical length one. Default is
             * 1.
             *
             * @param scalingFactor the scaling factor.
             * @return the builder.
             */
            public Builder withScaling(double scalingFactor) {
                this.scalingFactor = scalingFactor;
                return this;
            }

            /**
             * Indicates the geometry to be used to compute the spacial distance
             * between nodes. Default is Euclidean 2D.
             *
             * @param geometry the geometry to be used.
             * @return the builder.
             */
            public Builder withGeometry(GeomE geometry) {
                this.geometry = geometry;
                return this;
            }

            /**
             * Builds the metrics.
             *
             * @return the metrics.
             */
            public IdealStressScalingMetric build() {
                return new IdealStressScalingMetric(weightAttributeId, weightAttribute, asDirectedGraph, scalingFactor, geometry);
            }
        }

        /**
         * Create a metric that compute the ideal scaling for best graph stress.
         *
         * @param weightAttributeId the id of the attribute containing the edge
         * weights.
         * @param weightAttribute the weight attribute.
         * @param asDirectedGraph true if the graph edges should be considered
         * as directed edges.
         * @param scalingFactor the scaling factor.
         * @param geometry the geometry to be used.
         */
        private IdealStressScalingMetric(String weightAttributeId, EdgeAttribute<Double> weightAttribute, boolean asDirectedGraph, double scalingFactor, GeomE geometry) {
            this.weightAttributeId = weightAttributeId;
            this.weightAttribute = weightAttribute;
            this.asDirectedGraph = asDirectedGraph;
            this.scalingFactor = scalingFactor;
            this.geometry = geometry;
        }

        @Override
        public Double computeMetric(Graph graph) {
            NodeTheoreticalDistancesMetric theoreticalDistMetric
                    = new NodeTheoreticalDistancesMetric.Builder()
                            .withWeight(weightAttribute)
                            .withWeight(weightAttributeId)
                            .withDirectedEdges(asDirectedGraph)
                            .build();
            NodeSpacialDistancesMetric spacialDistMetric
                    = new NodeSpacialDistancesMetric(geometry);

            NodeDistances theoreticalDist = theoreticalDistMetric.computeMetric(graph);
            NodeDistances spacialDist = spacialDistMetric.computeMetric(graph);

            double num = 0;
            double den = 0;
            for (int i = 0; i < theoreticalDist.distances.length; i++) {
                Node first = theoreticalDist.reverseMap[i];
                for (int j = i + 1; j < theoreticalDist.distances.length; j++) {
                    Node second = theoreticalDist.reverseMap[j];
                    double a = theoreticalDist.distances[i][j];
                    double b = spacialDist.get(first, second);
                    if (a != Double.POSITIVE_INFINITY) {
                        num += b / a;
                        den += Math.sqrt(b / a);
                    }
                }
            }
            return num / den;
        }
    }
}
