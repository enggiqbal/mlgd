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
import java.util.List;
import ocotillo.dygraph.DyGraph;
import ocotillo.geometry.Interval;
import ocotillo.graph.Graph;
import ocotillo.graph.extra.GraphMetric;

/**
 * Describes the computation of a dynamic graph metric.
 *
 * @param <T> the type of data output.
 */
public interface DyGraphMetric<T> {

    /**
     * Computes the metric for this dynamic graph.
     *
     * @param graph the input graph.
     * @return the metric for that graph.
     */
    public T computeMetric(DyGraph graph);

    /**
     * A metric that collects the computed values in a list.
     *
     * @param <T> the type of data output.
     */
    public static class DyLoggedMetric<T> implements DyGraphMetric<T> {

        private final List<T> values = new ArrayList<>();
        private final DyGraphMetric<T> metric;

        /**
         * Builds a logged metric.
         *
         * @param metric the metric.
         */
        public DyLoggedMetric(DyGraphMetric<T> metric) {
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
        public DyGraphMetric<T> getMetric() {
            return metric;
        }

        /**
         * Computes the metric value and store it in the list.
         *
         * @param graph the input graph.
         * @return the metric for that graph.
         */
        @Override
        public T computeMetric(DyGraph graph) {
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
     * Computes the average graph metric on a set of dynamic graph snapshots.
     */
    public static class AverageSnapshotMetricCalculation implements DyGraphMetric<Double> {

        private final GraphMetric<Double> metric;
        private final List<Double> snapshotTimes;

        /**
         * Builds a calculator for average snapshot metrics.
         *
         * @param metric the static graph metric.
         * @param interval the interval of activity of the dynamic graph.
         * @param numberOfSnapshots the number of snapshots to take.
         */
        public AverageSnapshotMetricCalculation(GraphMetric<Double> metric, Interval interval, int numberOfSnapshots) {
            this.metric = metric;
            this.snapshotTimes = interval.sample(numberOfSnapshots);
        }

        /**
         * Builds a calculator for average snapshot metrics.
         *
         * @param metric the static graph metric.
         * @param snapshotTimes the times to use to create the snapshots.
         */
        public AverageSnapshotMetricCalculation(GraphMetric<Double> metric, List<Double> snapshotTimes) {
            this.metric = metric;
            this.snapshotTimes = snapshotTimes;
        }

        @Override
        public Double computeMetric(DyGraph graph) {
            double result = 0;
            int samples = 0;
            for (double snapshotTime : snapshotTimes) {
                Graph staticGraph = graph.snapshotAt(snapshotTime);
                double metricValue = metric.computeMetric(staticGraph);
                if (!Double.isNaN(metricValue)) {
                    result += metricValue;
                    samples++;
                }
            }
            return result / samples;
        }
    }
}
