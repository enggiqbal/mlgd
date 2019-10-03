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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.MirrorLine;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

/**
 * Describes the computation of a space-time cube graph metric.
 *
 * @param <T> the type of data output.
 */
public interface StcGraphMetric<T> {

    /**
     * Computes the metric for this graph.
     *
     * @param syncroniser the space-time cube synchroniser.
     * @return the metric for that graph.
     */
    public T computeMetric(SpaceTimeCubeSynchroniser syncroniser);

    /**
     * Computes the average movements of the nodes in the space-time cube.
     */
    public static class AverageNodeMovement2D implements StcGraphMetric<Double> {

        @Override
        public Double computeMetric(SpaceTimeCubeSynchroniser syncroniser) {
            double result = 0;
            for (Node node : syncroniser.originalGraph().nodes()) {
                List<Coordinates> positions = new ArrayList<>();
                for (MirrorLine line : syncroniser.mirrorLines(node)) {
                    positions.addAll(line.bendsAndExtremities());
                }
                for (int i = 0; i < positions.size() - 1; i++) {
                    result += Geom.e2D.magnitude(positions.get(i + 1).minus(positions.get(i)));
                }
            }
            return result / syncroniser.originalGraph().nodeCount();
        }
    }

    /**
     * Computes the number of times two distinct nodes overlap while moving in
     * the space-time cube.
     */
    public static class Crowding implements StcGraphMetric<Integer> {

        private final Interval interval;
        private final int samples;

        /**
         * Crowding metric for the given interval and number of samples.
         *
         * @param interval the interval.
         * @param samples the number of samples.
         */
        public Crowding(Interval interval, int samples) {
            this.interval = interval;
            this.samples = samples;
        }

        @Override
        public Integer computeMetric(SpaceTimeCubeSynchroniser syncroniser) {
            Set<String> currentOverlaps = new HashSet<>();
            int crowdingCount = 0;
            for (double time : interval.sample(samples)) {
                Graph snapshot = syncroniser.originalGraph().snapshotAt(time);
                NodeAttribute<Coordinates> positions = snapshot.nodeAttribute(StdAttribute.nodePosition);
                NodeAttribute<Coordinates> sizes = snapshot.nodeAttribute(StdAttribute.nodeSize);

                for (String pair : new ArrayList<>(currentOverlaps)) {
                    String[] nodes = pair.split("\\|");
                    if (!snapshot.hasNode(nodes[0]) || !snapshot.hasNode(nodes[1])) {
                        currentOverlaps.remove(pair);
                    }
                }

                List<Node> nodes = new ArrayList<>(snapshot.nodes());
                for (int i = 0; i < nodes.size(); ++i) {
                    for (int j = i + 1; j < nodes.size(); ++j) {
                        Node a = nodes.get(i);
                        Node b = nodes.get(j);
                        Coordinates aPos = positions.get(a);
                        Coordinates bPos = positions.get(b);
                        Coordinates aSize = sizes.get(a);
                        Coordinates bSize = sizes.get(b);
                        String pair = a.id() + "|" + b.id();
                        double collisionDistance = (Math.max(aSize.x(), aSize.y())
                                + Math.max(bSize.x(), bSize.y())) / 2.0;
                        double actualDistance = Geom.e2D.magnitude(bPos.minus(aPos));
                        if (actualDistance < collisionDistance) {
                            if (!currentOverlaps.contains(pair)) {
                                currentOverlaps.add(pair);
                                crowdingCount++;
                            }
                        } else {
                            currentOverlaps.remove(pair);
                        }
                    }
                }
            }
            return crowdingCount;
        }
    }
}
