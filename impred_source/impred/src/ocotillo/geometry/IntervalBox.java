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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * A box.
 */
@EqualsAndHashCode(callSuper = true)
public class IntervalBox extends Box {

    private final boolean[] leftClosed;
    private final boolean[] rightClosed;

    /**
     * Builds an interval box of given dimension.
     *
     * @param leftBounds the left bounds.
     * @param rightBounds the right bounds.
     * @param leftClosed the information on the left bounds.
     * @param rightClosed the information on the right bounds.
     */
    private IntervalBox(double[] leftBounds, double[] rightBounds, boolean[] leftClosed, boolean[] rightClosed) {
        super(leftBounds, rightBounds);
        assert (leftBounds.length == leftClosed.length && leftBounds.length == rightClosed.length) : "Bounds of different dimension";
        this.leftClosed = leftClosed;
        this.rightClosed = rightClosed;
    }

    /**
     * Creates a new interval box considering a standard box with closed
     * boundaries.
     *
     * @param box the standard box.
     * @return the interval box.
     */
    public static IntervalBox newInstance(Box box) {
        double[] leftBounds = new double[box.dimensions()];
        double[] rightBounds = new double[box.dimensions()];
        boolean[] leftClosed = new boolean[box.dimensions()];
        boolean[] rightClosed = new boolean[box.dimensions()];
        for (int i = 0; i < box.dimensions(); i++) {
            leftBounds[i] = box.leftBound(i);
            rightBounds[i] = box.rightBound(i);
            leftClosed[i] = true;
            rightClosed[i] = true;
        }
        return new IntervalBox(leftBounds, rightBounds, leftClosed, rightClosed);
    }

    /**
     * Creates a new interval box.
     *
     * @param intervals the intervals.
     * @return the interval box.
     */
    public static IntervalBox newInstance(Interval... intervals) {
        return newInstance(Arrays.asList(intervals));
    }

    /**
     * Creates a new interval box.
     *
     * @param intervals the intervals.
     * @return the interval box.
     */
    public static IntervalBox newInstance(List<Interval> intervals) {
        double[] leftBounds = new double[intervals.size()];
        double[] rightBounds = new double[intervals.size()];
        boolean[] leftClosed = new boolean[intervals.size()];
        boolean[] rightClosed = new boolean[intervals.size()];
        for (int i = 0; i < intervals.size(); i++) {
            Interval interval = intervals.get(i);
            if (interval == null) {
                return null;
            }
            leftBounds[i] = interval.leftBound();
            rightBounds[i] = interval.rightBound();
            leftClosed[i] = interval.isLeftClosed();
            rightClosed[i] = interval.isRightClosed();
        }
        return new IntervalBox(leftBounds, rightBounds, leftClosed, rightClosed);
    }

    /**
     * Checks if the left bounds are smaller or equal to the corresponding right
     * ones.
     *
     * @return true if the bounds are valid, false otherwise.
     */
    @Override
    public final boolean isValid() {
        for (int i = 0; i < dimensions(); i++) {
            if (interval(i) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the state of the left bound.
     *
     * @return the left bound state.
     */
    public boolean leftClosed() {
        if (dimensions() > 0) {
            return leftClosed[0];
        }
        throw new IllegalStateException("The dimension idx required (" + 0 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the state of the right bound.
     *
     * @return the right bound state.
     */
    public boolean rightClosed() {
        if (dimensions() > 0) {
            return rightClosed[0];
        }
        throw new IllegalStateException("The dimension idx required (" + 0 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the state of the bottom bound.
     *
     * @return the bottom bound state.
     */
    public boolean bottomClosed() {
        if (dimensions() > 1) {
            return leftClosed[1];
        }
        throw new IllegalStateException("The dimension idx required (" + 1 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the state of the top bound.
     *
     * @return the top bound state.
     */
    public boolean topClosed() {
        if (dimensions() > 1) {
            return rightClosed[1];
        }
        throw new IllegalStateException("The dimension idx required (" + 1 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the state of the near bound.
     *
     * @return the near bound state.
     */
    public boolean nearClosed() {
        if (dimensions() > 2) {
            return leftClosed[2];
        }
        throw new IllegalStateException("The dimension idx required (" + 2 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the state of the far bound.
     *
     * @return the far bound state.
     */
    public boolean farClosed() {
        if (dimensions() > 2) {
            return rightClosed[2];
        }
        throw new IllegalStateException("The dimension idx required (" + 2 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Gets the left bound status for the given dimension.
     *
     * @param dimension the dimension index.
     * @return true if the interval is left closed for that dimension.
     */
    public boolean leftStatus(int dimension) {
        if (dimensions() > dimension) {
            return leftClosed[dimension];
        }
        throw new IllegalStateException("The dimension idx required (" + dimension + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Gets the right bound status for the given dimension.
     *
     * @param dimension the dimension index.
     * @return true if the interval is right closed for that dimension.
     */
    public boolean rightStatus(int dimension) {
        if (dimensions() > dimension) {
            return rightClosed[dimension];
        }
        throw new IllegalStateException("The dimension idx required (" + dimension + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the interval relative to the given dimension.
     *
     * @param i the dimension index.
     * @return the interval relative to this dimension.
     */
    public Interval interval(int i) {
        return Interval.newCustom(leftBound(i), rightBound(i), leftStatus(i), rightStatus(i));
    }

    /**
     * Combines this box with another one.
     *
     * @param box the other box.
     * @return the combination of the boxes.
     */
    public IntervalBox combine(IntervalBox box) {
        return combineInterval(Arrays.asList(this, box));
    }

    /**
     * Computes the intersection of this box with another one.
     *
     * @param box the other box.
     * @return null if they do not intersect, the intersection box otherwise.
     */
    public IntervalBox intersect(IntervalBox box) {
        return intersectInterval(Arrays.asList(this, box));
    }

    @Override
    public String toString() {
        String result = "<";
        for (int i = 0; i < dimensions(); i++) {
            result += i + "=" + interval(i) + " ";
        }
        return result.substring(0, result.length() - 1) + ">";
    }

    /**
     * Verifies if a point is inside a box.
     *
     * @param point the point.
     * @return true if the point is in the box, false otherwise.
     */
    @Override
    public boolean contains(Coordinates point) {
        for (int i = 0; i < dimensions(); i++) {
            if (!interval(i).contains(point.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Combine multiple boxes into one.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static IntervalBox combine(IntervalBox... boxes) {
        return combineInterval(Arrays.asList(boxes));
    }

    /**
     * Combine multiple boxes into one.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static IntervalBox combineInterval(Collection<IntervalBox> boxes) {
        List<Interval> intervals = new ArrayList<>();
        for (IntervalBox box : boxes) {
            if (box != null) {
                for (int i = 0; i < box.dimensions(); i++) {
                    if (intervals.size() <= i) {
                        intervals.set(i, box.interval(i));
                    } else {
                        intervals.set(i, intervals.get(i).fusion(box.interval(i)));
                    }
                }
            }
        }
        return newInstance(intervals);
    }

    /**
     * Computes the intersection of multiple boxes.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static IntervalBox intersectInterval(IntervalBox... boxes) {
        return intersectInterval(Arrays.asList(boxes));
    }

    /**
     * Computes the intersection of multiple boxes.
     *
     * @param boxes the boxes.
     * @return null if the boxes do not have a region shared by all of them, the
     * intersection box otherwise.
     */
    public static IntervalBox intersectInterval(Collection<IntervalBox> boxes) {
        List<Interval> intervals = new ArrayList<>();
        for (IntervalBox box : boxes) {
            if (box != null) {
                for (int i = 0; i < box.dimensions(); i++) {
                    if (intervals.size() <= i) {
                        intervals.set(i, box.interval(i));
                    } else {
                        intervals.set(i, intervals.get(i).intersection(box.interval(i)));
                    }
                }
            }
        }
        return newInstance(intervals);
    }
}
