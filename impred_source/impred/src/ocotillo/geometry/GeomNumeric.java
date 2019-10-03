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

import java.util.Random;

/**
 * Library of geometry related numeric functions.
 */
public class GeomNumeric {

    private static final Random randomGen = new Random();

    /**
     * Generates a new random value.
     *
     * @param maxValue the maximum value.
     * @return a random double.
     */
    public static double randomDouble(double maxValue) {
        return randomGen.nextDouble() * maxValue;
    }

    /**
     * Generates a new random value.
     *
     * @param minValue the minimum value.
     * @param maxValue the maximum value.
     * @return a random double.
     */
    public static double randomDouble(double minValue, double maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("The minimum value provided is greater than the maximum value.");
        }
        return randomDouble(maxValue - minValue) + minValue;
    }

    /**
     * Transforms an angle expressed in radians into degrees.
     *
     * @param radians the angle in radians to convert.
     * @return the angle in degrees.
     */
    public static double radiansToDegrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

    /**
     * Transforms an angle expressed in degrees into radians.
     *
     * @param degrees the angle in degrees to convert.
     * @return the angle in radians.
     */
    public static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    /**
     * Transforms an angle expressed in radians in the equivalent angle in the
     * interval [-PI, PI).
     *
     * @param angleInRadians the angle in radians.
     * @return the equivalent angle in radians.
     */
    public static double normalizeRadiansAngle(double angleInRadians) {
        double normalisedAngle = posNormalizeRadiansAngle(angleInRadians);
        if (normalisedAngle >= Math.PI) {
            normalisedAngle -= 2 * Math.PI;
        }
        return normalisedAngle;
    }

    /**
     * Transforms an angle expressed in degrees in the equivalent angle in the
     * interval [-180, 180).
     *
     * @param angleInDegrees the angle in degrees.
     * @return the equivalent angle in degrees.
     */
    public static double normalizeDegreesAngle(double angleInDegrees) {
        double normalisedAngle = posNormalizeDegreesAngle(angleInDegrees);
        if (normalisedAngle >= 180.0) {
            normalisedAngle -= 360.0;
        }
        return normalisedAngle;
    }

    /**
     * Transforms an angle expressed in radians in the equivalent angle in the
     * interval [0, 2PI).
     *
     * @param angleInRadians the angle in radians.
     * @return the equivalent angle in radians.
     */
    public static double posNormalizeRadiansAngle(double angleInRadians) {
        double completeRoundsToRemove = Math.floor(angleInRadians / (2.0 * Math.PI));
        return angleInRadians - 2.0 * Math.PI * completeRoundsToRemove;
    }

    /**
     * Transforms an angle expressed in degrees in the equivalent angle in the
     * interval [0, 360°).
     *
     * @param angleInDegrees The angle in degrees.
     * @return the equivalent angle in degrees.
     */
    public static double posNormalizeDegreesAngle(double angleInDegrees) {
        double completeRoundsToRemove = Math.floor(angleInDegrees / 360);
        return angleInDegrees - 360 * completeRoundsToRemove;
    }

    /**
     * Computes the difference between two angles in radians. The difference is
     * in the range [0,PI).
     *
     * @param firstAngleInRadians the first angle.
     * @param secondAngleInRadians the second angle.
     * @return the angle difference.
     */
    public static double angleDiff(double firstAngleInRadians, double secondAngleInRadians) {
        return Math.abs(normalizeRadiansAngle(firstAngleInRadians - secondAngleInRadians));
    }

    /**
     * Checks if a given direction is included in an angle.
     *
     * @param direction the direction, indicated as angle from the origin.
     * @param angleStart the direction at which the reference angle starts.
     * @param angleStop the direction at which the reference angle stops.
     * @return true if the direction is in between the angle start and stop,
     * false otherwise.
     */
    public static boolean isDirectionInAngle(double direction, double angleStart, double angleStop) {
        direction = posNormalizeRadiansAngle(direction);
        angleStart = posNormalizeRadiansAngle(angleStart);
        angleStop = posNormalizeRadiansAngle(angleStop);
        while (direction < angleStart) {
            direction += 2 * Math.PI;
        }
        while (angleStop < angleStart) {
            angleStop += 2 * Math.PI;
        }
        return angleStart <= direction && direction <= angleStop;
    }

    /**
     * Checks if a given angle is included in another angle.
     *
     * @param inAngleStart the direction at which the supposedly included angle
     * starts.
     * @param inAngleStop the direction at which the supposedly included angle
     * stops.
     * @param outAngleStart the direction at which the supposedly including
     * angle starts.
     * @param outAngleStop the direction at which the supposedly including angle
     * stops.
     * @return true if the direction is in between the angle start and stop,
     * false otherwise.
     */
    public static boolean isAngleInAngle(double inAngleStart, double inAngleStop,
            double outAngleStart, double outAngleStop) {
        inAngleStart = posNormalizeRadiansAngle(inAngleStart);
        inAngleStop = posNormalizeRadiansAngle(inAngleStop);
        outAngleStart = posNormalizeRadiansAngle(outAngleStart);
        outAngleStop = posNormalizeRadiansAngle(outAngleStop);
        while (inAngleStart < outAngleStart) {
            inAngleStart += 2 * Math.PI;
        }
        while (inAngleStop < inAngleStart) {
            inAngleStop += 2 * Math.PI;
        }
        while (outAngleStop < outAngleStart) {
            outAngleStop += 2 * Math.PI;
        }
        assert (inAngleStart <= inAngleStop && inAngleStart + 2 * Math.PI >= inAngleStop) : "The in angle is not expressed correctly.";
        assert (outAngleStart <= outAngleStop && outAngleStart + 2 * Math.PI >= outAngleStop) : "The out angle is not expressed correctly.";
        return outAngleStart <= inAngleStart && inAngleStop <= outAngleStop;
    }

    /**
     * Checks if an angle overlaps (totally or partially) with another one.
     *
     * @param firstAngleStart the direction at which the first angle starts.
     * @param firstAngleStop the direction at which the first angle stops.
     * @param secondAngleStart the direction at which the second angle starts.
     * @param secondAngleStop the direction at which the second angle stops.
     * @return true the angles partially or totally overlap, false otherwise.
     */
    public static boolean areAnglesOverlapping(double firstAngleStart, double firstAngleStop,
            double secondAngleStart, double secondAngleStop) {
        return isDirectionInAngle(firstAngleStart, secondAngleStart, secondAngleStop)
                || isDirectionInAngle(firstAngleStop, secondAngleStart, secondAngleStop)
                || isAngleInAngle(secondAngleStart, secondAngleStop, firstAngleStart, firstAngleStop);
    }

    /**
     * Provides a readable output for angles. Use for debugging only.
     *
     * @param angle the angle.
     * @return a readable angle description.
     */
    public static String printAngle(double angle) {
        return (int) Math.round(radiansToDegrees(angle)) + "°";
    }

    /**
     * Computes the cumulative distribution function for a given value of x.
     *
     * @param x the value of x.
     * @return the CDF for that value.
     */
    public static double cdf(double x) {
        return 0.5 * (1 + Math.signum(x) * Math.sqrt(1 - Math.exp(-2 * x * x / Math.PI)));
    }

    /**
     * Computes the cumulative distribution function for a given value of x and
     * for a Gaussian with given mu and variance.
     *
     * @param x the value of x.
     * @param mu the offset.
     * @param sigma the standard deviation.
     * @return the CDF for that value.
     */
    public static double cdf(double x, double mu, double sigma) {
        double t = (x - mu) / sigma;
        return cdf(t);
    }
}
