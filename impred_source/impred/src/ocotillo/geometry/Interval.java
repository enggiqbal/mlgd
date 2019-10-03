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
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * An Interval.
 */
@EqualsAndHashCode
public class Interval {

    /**
     * A global interval.
     */
    static public final Interval global = Interval.newOpen(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    private final double leftBound;
    private final double rightBound;
    private final boolean leftClosed;
    private final boolean rightClosed;

    /**
     * Builds a new open interval.
     *
     * @param leftBound the beginning of the interval.
     * @param rightBound the end of an interval.
     * @return the new interval.
     */
    public static Interval newOpen(double leftBound, double rightBound) {
        return Interval.newCustom(leftBound, rightBound, false, false);
    }

    /**
     * Builds a new left-closed interval.
     *
     * @param leftBound the beginning of the interval.
     * @param rightBound the end of an interval.
     * @return the new interval.
     */
    public static Interval newLeftClosed(double leftBound, double rightBound) {
        return Interval.newCustom(leftBound, rightBound, true, false);
    }

    /**
     * Builds a new right-closed interval.
     *
     * @param leftBound the beginning of the interval.
     * @param rightBound the end of an interval.
     * @return the new interval.
     */
    public static Interval newRightClosed(double leftBound, double rightBound) {
        return Interval.newCustom(leftBound, rightBound, false, true);
    }

    /**
     * Builds a new closed interval.
     *
     * @param leftBound the beginning of the interval.
     * @param rightBound the end of an interval.
     * @return the new interval.
     */
    public static Interval newClosed(double leftBound, double rightBound) {
        return Interval.newCustom(leftBound, rightBound, true, true);
    }

    /**
     * Builds a interval with custom bound definitions.
     *
     * @param leftBound the beginning of the interval.
     * @param rightBound the end of an interval.
     * @param leftClosed indicates if the interval is left closed.
     * @param rightClosed indicates if the interval is right closed.
     * @return the new interval.
     */
    public static Interval newCustom(double leftBound, double rightBound, boolean leftClosed, boolean rightClosed) {
        if (rightBound < leftBound
                || (leftBound == rightBound && (!leftClosed || !rightClosed))) {
            return null;
        } else {
            return new Interval(leftBound, rightBound, leftClosed, rightClosed);
        }
    }

    /**
     * Builds an interval.
     *
     * @param leftBound the beginning of the interval.
     * @param rightBound the end of an interval.
     * @param leftClosed true if the interval is closed to the left.
     * @param rightClosed true if the interval is closed to the right.
     */
    private Interval(double leftBound, double rightBound, boolean leftClosed, boolean rightClosed) {
        assert (leftBound <= rightBound) : "Interval with left bound bigger than right bound: "
                + (leftClosed ? "[" : "(") + leftBound + "," + rightBound + (rightClosed ? "]" : ")");
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        if (leftBound == Double.NEGATIVE_INFINITY) {
            this.leftClosed = false;
        } else {
            this.leftClosed = leftClosed;
        }
        if (rightBound == Double.POSITIVE_INFINITY) {
            this.rightClosed = false;
        } else {
            this.rightClosed = rightClosed;
        }
    }

    /**
     * Gets the left bound of the interval.
     *
     * @return the left bound.
     */
    public double leftBound() {
        return leftBound;
    }

    /**
     * Gets the right bound of the interval.
     *
     * @return the right bound.
     */
    public double rightBound() {
        return rightBound;
    }

    /**
     * Checks if the interval is closed to the left.
     *
     * @return true if the interval is closed to the left, false otherwise.
     */
    public boolean isLeftClosed() {
        return leftClosed;
    }

    /**
     * Checks if the interval is closed to the right.
     *
     * @return true if the interval is closed to the right, false otherwise.
     */
    public boolean isRightClosed() {
        return rightClosed;
    }

    /**
     * Checks if a point is included in the interval.
     *
     * @param point the point to check.
     * @return true if the point is in the interval, false otherwise.
     */
    public boolean contains(double point) {
        if (leftBound < point && point < rightBound) {
            return true;
        } else {
            return (leftClosed && Geom.eXD.almostEqual(leftBound, point))
                    || (rightClosed && Geom.eXD.almostEqual(rightBound, point));
        }
    }

    /**
     * Computes the intersection with another interval.
     *
     * @param other the other interval.
     * @return their intersection.
     */
    public Interval intersection(Interval other) {
        double[] rangeInt = Geom.e1D.rangesIntersection(leftBound, rightBound, other.leftBound, other.rightBound);
        if (rangeInt != null) {

            boolean intersectionLeftClosed = true;
            if (Geom.eXD.almostEqual(leftBound, rangeInt[0]) && !leftClosed) {
                intersectionLeftClosed = false;
            }
            if (Geom.eXD.almostEqual(other.leftBound, rangeInt[0]) && !other.leftClosed) {
                intersectionLeftClosed = false;
            }

            boolean intersectionRightClosed = true;
            if (Geom.eXD.almostEqual(rightBound, rangeInt[1]) && !rightClosed) {
                intersectionRightClosed = false;
            }
            if (Geom.eXD.almostEqual(other.rightBound, rangeInt[1]) && !other.rightClosed) {
                intersectionRightClosed = false;
            }

            if (!Geom.eXD.almostEqual(rangeInt[0], rangeInt[1])
                    || intersectionLeftClosed || intersectionRightClosed) {
                return Interval.newCustom(rangeInt[0], rangeInt[1], intersectionLeftClosed, intersectionRightClosed);
            }
        }
        return null;
    }

    /**
     * Computes the fusion between two intervals. If the intervals are not
     * overlapping or contiguous, it might include values that are not contained
     * in a proper set union.
     *
     * @param other the other interval.
     * @return the interval fusion.
     */
    public Interval fusion(Interval other) {
        double newLeftBound = Math.min(leftBound, other.leftBound);
        double newRightBound = Math.max(rightBound, other.rightBound);

        boolean newLeftClosed = false;
        if (Geom.eXD.almostEqual(leftBound, newLeftBound)) {
            newLeftClosed |= leftClosed;
        }
        if (Geom.eXD.almostEqual(other.leftBound, newLeftBound)) {
            newLeftClosed |= other.leftClosed;
        }

        boolean newRightClosed = false;
        if (Geom.eXD.almostEqual(rightBound, newRightBound)) {
            newRightClosed |= rightClosed;
        }
        if (Geom.eXD.almostEqual(other.rightBound, newRightBound)) {
            newRightClosed |= other.rightClosed;
        }

        return newCustom(newLeftBound, newRightBound, newLeftClosed, newRightClosed);
    }

    /**
     * Checks if the interval overlaps with another one.
     *
     * @param other the other interval.
     * @return true if they overlap, false otherwise.
     */
    public boolean overlapsWith(Interval other) {
        return intersection(other) != null;
    }

    /**
     * Checks if the interval fully contains another one.
     *
     * @param other the other interval.
     * @return true if it contains the other, false otherwise.
     */
    public boolean contains(Interval other) {
        return other.equals(intersection(other));
    }

    /**
     * Checks if the interval is fully contained in another one.
     *
     * @param other the other interval.
     * @return true if it is contained in the other, false otherwise.
     */
    public boolean isContainedIn(Interval other) {
        return this.equals(intersection(other));
    }

    /**
     * Returns the width of the interval.
     *
     * @return the interval width.
     */
    public double width() {
        return rightBound - leftBound;
    }

    @Override
    public String toString() {
        String leftBracket = isLeftClosed() ? "[" : "(";
        String rightBracket = isRightClosed() ? "]" : ")";
        return leftBracket + leftBound + ", " + rightBound + rightBracket;
    }

    /**
     * Parses a string describing an interval.
     *
     * @param stringDescription the interval in string form.
     * @return the parsed interval.
     */
    public static Interval parse(String stringDescription) {
        String cleanedDescription = stringDescription.replaceAll(" ", "");

        boolean leftClosed, righClosed;
        if (cleanedDescription.startsWith("[")) {
            leftClosed = true;
        } else if (cleanedDescription.startsWith("(")) {
            leftClosed = false;
        } else {
            throw new IllegalArgumentException("The string cannot be parsed as an interval: " + stringDescription);
        }

        if (cleanedDescription.endsWith("]")) {
            righClosed = true;
        } else if (cleanedDescription.endsWith(")")) {
            righClosed = false;
        } else {
            throw new IllegalArgumentException("The string cannot be parsed as an interval: " + stringDescription);
        }

        String[] values = cleanedDescription.substring(1, cleanedDescription.length() - 1).split(",");
        double leftBound = Double.parseDouble(values[0]);
        double rightBound = Double.parseDouble(values[1]);
        return Interval.newCustom(leftBound, rightBound, leftClosed, righClosed);
    }

    /**
     * Returns a given number of equally spaced samples for the interval.
     * Samples include the interval bounds if the interval is closed on a given
     * side.
     *
     * @param numberOfSamples the number of samples to produce.
     * @return the samples.
     */
    public List<Double> sample(int numberOfSamples) {
        List<Double> samples = new ArrayList<>();
        int divisions = numberOfSamples - 1;
        int startingPoint = 0;
        if (!isLeftClosed()) {
            divisions++;
            startingPoint = 1;
        }
        if (!isRightClosed()) {
            divisions++;
        }
        double divisionSize = width() / divisions;
        for (int i = startingPoint; i < numberOfSamples; i++) {
            samples.add(leftBound() + i * divisionSize);
        }
        return samples;
    }
}
