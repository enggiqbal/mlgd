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
package ocotillo.geometry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A polygon.
 */
public class Polygon extends ArrayList<Coordinates> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an empty polygon.
     */
    public Polygon() {
        super();
    }

    /**
     * Constructs a polygon with given points.
     *
     * @param points the polygon points.
     */
    public Polygon(Collection<? extends Coordinates> points) {
        super(points);
    }

    /**
     * Checks if the other polygon is cyclic equivalent with respect to this
     * one. Polygons are cyclic equivalent disregarding vertex order (clockwise
     * vs. anti-clockwise) or list rotations.
     *
     * @param other the other vector.
     * @return true if the other vector is cyclic equivalent to this.
     */
    public boolean cyclicEquivalent(Polygon other) {
        if (size() != other.size()) {
            return false;
        } else if (other.isEmpty()) {
            return true;
        } else if (!contains(other.get(0))) {
            return false;
        }
        assert (other.lastIndexOf(other.get(0)) == 0) : "The current implementation does not support coincident vertices.";

        Polygon twiceThis = new Polygon(this);
        twiceThis.addAll(this);
        int fromLeft = twiceThis.indexOf(other.get(0));
        int fromRight = twiceThis.lastIndexOf(other.get(0));
        boolean matchFromLeft = true;
        boolean matchFromRight = true;
        int i = 0;
        for (Coordinates point : other) {
            if (!twiceThis.get(fromLeft + i).equals(point)) {
                matchFromLeft = false;
            }
            if (!twiceThis.get(fromRight - i).equals(point)) {
                matchFromRight = false;
            }
            i++;
        }
        return matchFromLeft || matchFromRight;
    }
}
