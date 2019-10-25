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
package ocotillo.graph.rendering;

import java.awt.Color;

/**
 * Color in the YUV space.
 */
public class YuvColor {

    private final double y;
    private final double u;
    private final double v;
    private final double alpha;

    /**
     * Builds a YUV color from its float components.
     *
     * @param y y, range [0..1].
     * @param u u, range [-1..1] * 0.436.
     * @param v v, range [-1..1] * 0.615.
     * @param alpha alpha, range [0..1].
     */
    public YuvColor(double y, double u, double v, double alpha) {
        this.y = y;
        this.u = u;
        this.v = v;
        this.alpha = alpha;
    }

    /**
     * Builds a YUV color from a RGB one.
     *
     * @param rgbColor the RGB color.
     */
    public YuvColor(Color rgbColor) {
        float[] rgbComponents = rgbColor.getRGBComponents(null);
        float r = rgbComponents[0];
        float g = rgbComponents[1];
        float b = rgbComponents[2];
        this.y = 0.299 * r + 0.587 * g + 0.114 * b;
        this.u = 0.492 * (b - y);
        this.v = 0.877 * (r - y);
        this.alpha = rgbComponents[3];
    }

    /**
     * Gets the float value of y.
     *
     * @return y, range [0..1].
     */
    public double yFloat() {
        return y;
    }

    /**
     * Gets the float value of u.
     *
     * @return y, range [-1..1] * 0.436.
     */
    public double uFloat() {
        return u;
    }

    /**
     * Gets the float value of v.
     *
     * @return v, range [-1..1] * 0.615.
     */
    public double vFloat() {
        return v;
    }

    /**
     * Gets the float value of alpha.
     *
     * @return alpha, range [0..1].
     */
    public double alphaFloat() {
        return alpha;
    }

    /**
     * Gets the integer value of y.
     *
     * @return y, range [0..255].
     */
    public int y() {
        return round(y * 255);
    }

    /**
     * Gets the integer value of u.
     *
     * @return u, range [0..255].
     */
    public int u() {
        return round((u / 0.872 + 0.5) * 255);
    }

    /**
     * Gets the integer value of v.
     *
     * @return v, range [0..255].
     */
    public int v() {
        return round((v / 1.23 + 0.5) * 255);
    }

    /**
     * Gets the integer value of alpha.
     *
     * @return alpha, range [0..255].
     */
    public int alpha() {
        return round(alpha * 255);
    }

    /**
     * Rounds and convert to integer a double value.
     *
     * @param value the value.
     * @return the rounded integer for the value.
     */
    private int round(double value) {
        return (int) Math.round(value);
    }

    /**
     * Adds two colors. Does not ensure the correctness of the obtained color as
     * it allows to exit the normal ranges.
     *
     * @param other the other color.
     * @return the color sum.
     */
    public YuvColor plus(YuvColor other) {
        return new YuvColor(y + other.y, u + other.u, v + other.v, alpha + other.alpha);
    }

    /**
     * Subtracts two colors. Does not ensure the correctness of the obtained
     * color as it allows to exit the normal ranges.
     *
     * @param other the other color.
     * @return the color difference.
     */
    public YuvColor minus(YuvColor other) {
        return new YuvColor(y - other.y, u - other.u, v - other.v, alpha - other.alpha);
    }

    /**
     * Multiplies a color by a scalar. Does not ensure the correctness of the
     * obtained color as it allows to exit the normal ranges.
     *
     * @param scalar the scalar.
     * @return the scaled color.
     */
    public YuvColor times(double scalar) {
        return new YuvColor(y * scalar, u * scalar, v * scalar, alpha * scalar);
    }

    /**
     * Divides a color by a scalar. Does not ensure the correctness of the
     * obtained color as it allows to exit the normal ranges.
     *
     * @param scalar the scalar.
     * @return the scaled color.
     */
    public YuvColor divided(double scalar) {
        return new YuvColor(y / scalar, u / scalar, v / scalar, alpha / scalar);
    }

    /**
     * Converts the color to RGB space.
     *
     * @return the RGB color.
     */
    public Color toRgb() {
        double red = y + 1.14 * v;
        double green = y - 0.395 * u - 0.581 * v;
        double blue = y + 2.033 * u;
        return new Color((float) red, (float) green, (float) blue, (float) alpha);
    }
}
