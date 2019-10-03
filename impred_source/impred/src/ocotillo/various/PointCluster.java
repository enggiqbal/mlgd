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
import java.util.Iterator;
import java.util.List;

import ocotillo.geometry.Coordinates;

/**
 * A cluster of points.
 */
public class PointCluster implements Iterable<Coordinates> {

    private final List<Coordinates> points = new ArrayList<>();
    private Coordinates mean;

    /**
     * Adds a point to the cluster.
     *
     * @param point the point.
     * @return the result of the operation.
     */
    public boolean add(Coordinates point) {
        mean = null;
        return points.add(point);
    }

    /**
     * Compute the mean of the point cluster.
     *
     * @return the mean position.
     */
    public Coordinates mean() {
        assert (!points.isEmpty()) : "Cannot compute mean on empty cluster.";
        if (mean == null) {
            Coordinates sum = new Coordinates(0.0);
            for (Coordinates point : points) {
                sum.plusIP(point);
            }
            mean = sum.divide(points.size());
        }
        return mean;
    }

    @Override
    public Iterator<Coordinates> iterator() {
        return points.iterator();
    }

    /**
     * Returns the cluster size.
     *
     * @return
     */
    public int size() {
        return points.size();
    }
}
