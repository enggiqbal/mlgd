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

/**
 * Collects the geometry methods that operates on one dimensional vectorial
 * spaces. Unless otherwise specified, it should be assumed that any dimension
 * exceeding the first two is ignored by the methods of this class and dropped
 * from the returned results.
 */
public class Geom1D extends GeomXD {

    @Override
    public int geomDim() {
        return 1;
    }

    /**
     * Computes the overlap between two integer ranges.
     *
     * @param firstLeft the smaller extreme of the first range.
     * @param firstRight the bigger extreme of the first range.
     * @param secondLeft the smaller extreme of the second range.
     * @param secondRight the bigger extreme of the second range.
     * @return Null if the ranges do not overlap, the intersection range
     * otherwise.
     */
    public int[] rangesIntersection(int firstLeft, int firstRight, int secondLeft, int secondRight) {
        assert (firstLeft <= firstRight && secondLeft <= secondRight) : "The left indexes must be smaller or equal to the right ones";
        int left = Math.max(firstLeft, secondLeft);
        int right = Math.min(firstRight, secondRight);
        if (left <= right) {
            return new int[]{left, right};
        } else {
            return null;
        }
    }

    /**
     * Computes the overlap between two double ranges.
     *
     * @param firstLeft the smaller extreme of the first range.
     * @param firstRight the bigger extreme of the first range.
     * @param secondLeft the smaller extreme of the second range.
     * @param secondRight the bigger extreme of the second range.
     * @return Null if the ranges do not overlap, the intersection range
     * otherwise.
     */
    public double[] rangesIntersection(double firstLeft, double firstRight, double secondLeft, double secondRight) {
        assert (firstLeft <= firstRight && secondLeft <= secondRight) : "The left indexes must be smaller or equal to the right ones";
        double left = Math.max(firstLeft, secondLeft);
        double right = Math.min(firstRight, secondRight);
        if (left <= right) {
            return new double[]{left, right};
        } else {
            return null;
        }
    }
}
