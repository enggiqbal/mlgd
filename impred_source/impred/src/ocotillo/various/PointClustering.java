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
package ocotillo.various;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;

/**
 * A cluster techniques for a list of points.
 */
public abstract class PointClustering extends ArrayList<PointCluster> {

    private static final long serialVersionUID = 1L;

    /**
     * Finds the cluster for a given point.
     *
     * @param point the point.
     * @return the cluster that contain the point.
     */
    public abstract int findCluster(Coordinates point);

    /**
     * k-means clustering.
     */
    public static class KMeans extends PointClustering {

        private final int k;
        private final List<Coordinates> points;

        private static final long serialVersionUID = 1L;

        /**
         * Constructs a k-means clustering for the given list of points.
         *
         * @param k the number of clusters.
         * @param points the points to cluster.
         */
        public KMeans(int k, List<Coordinates> points) {
            this.k = k;
            this.points = points;

            List<Coordinates> means = initialiseMeans();
            List<Coordinates> newMeans = means;

            do {
                means = newMeans;
                computeClusters(means);
                newMeans = computeMeans();
            } while (!means.equals(newMeans));
        }

        /**
         * Initialises the list of means with random sample points.
         *
         * @return the random means.
         */
        private List<Coordinates> initialiseMeans() {
            Set<Coordinates> pointSet = new HashSet<>(points);
            if (pointSet.size() < k) {
                throw new IllegalStateException("The number of means is greater than the number of points.");
            }
            List<Coordinates> means = new ArrayList<>();
            for (Coordinates point : pointSet) {
                means.add(point);
                if (means.size() == k) {
                    break;
                }
            }
            return means;
        }

        /**
         * Computes the clusters using the parameter means.
         *
         * @param means the means.
         */
        private void computeClusters(List<Coordinates> means) {
            clear();
            for (int i = 0; i < k; i++) {
                add(new PointCluster());
            }

            for (Coordinates point : points) {
                int bestIndex = -1;
                double bestDistance = Double.POSITIVE_INFINITY;
                for (int i = 0; i < k; i++) {
                    Coordinates mean = means.get(i);
                    double distance = Geom.eXD.magnitude(mean.minus(point));
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestIndex = i;
                    }
                }
                get(bestIndex).add(point);
            }
        }

        /**
         * Compute the means from the given clusters.
         *
         * @return the means computed from the clusters.
         */
        public List<Coordinates> computeMeans() {
            List<Coordinates> means = new ArrayList<>();
            for (PointCluster cluster : this) {
                means.add(cluster.mean());
            }
            return means;
        }

        @Override
        public int findCluster(Coordinates point) {
            int bestIndex = -1;
            double bestDistance = Double.POSITIVE_INFINITY;
            for (int i = 0; i < k; i++) {
                Coordinates mean = get(i).mean();
                double distance = Geom.eXD.magnitude(mean.minus(point));
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }
            return bestIndex;
        }
    }
}
