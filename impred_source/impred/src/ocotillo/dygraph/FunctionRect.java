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
package ocotillo.dygraph;

import lombok.EqualsAndHashCode;
import ocotillo.geometry.Geom;
import ocotillo.graph.rendering.YuvColor;
import ocotillo.geometry.Interval;

/**
 * A function defined on a single interval.
 *
 * @param <T> the type of data handled by the function.
 */
@EqualsAndHashCode
public abstract class FunctionRect<T> implements Function<T> {

    private final Interval interval;
    private final T leftValue;
    private final T rightValue;
    private final Interpolation interpolation;

    /**
     * Private interval function constructor.
     *
     * @param definitionInterval the definition interval.
     * @param leftValue the initial value of the function.
     * @param rightValue the final value of the function.
     * @param interpolation the interpolation method used to compute
     * intermediate values.
     */
    private FunctionRect(Interval definitionInterval, T leftValue, T rightValue, Interpolation interpolation) {
        this.interval = definitionInterval;
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.interpolation = interpolation;
    }

    @Override
    public Interval interval() {
        return interval;
    }

    @Override
    public T leftValue() {
        return leftValue;
    }

    @Override
    public T rightValue() {
        return rightValue;
    }

    public Interpolation interpolation() {
        return interpolation;
    }

    @Override
    public T valueAt(double point) {
        if (Geom.eXD.almostZero(interval.width())) {
            return leftValue;
        }
        double intDist = (point - interval.leftBound()) / interval.width();
        double interpIntDist = interpolation.valueAt(intDist);

        if (Geom.eXD.almostZero(interpIntDist)) {
            return leftValue;
        } else if (Geom.eXD.almostEqual(interpIntDist, 1.0)) {
            return rightValue;
        } else {
            return valueAtInterpolationComputation(interpIntDist);
        }
    }

    /**
     * Contains the function computation for a value that needs to be
     * interpolated.
     *
     * @param interpIntDist the interpolated interval distance.
     * @return the output at the interpolated interval distance.
     */
    protected T valueAtInterpolationComputation(double interpIntDist) {
        T outDist = subtract(rightValue, leftValue);
        T relativeOutDist = multiply(outDist, 1 - interpIntDist);
        return subtract(rightValue, relativeOutDist);
    }

    /**
     * Performs the subtraction operation between two values of the output type.
     *
     * @param a the first value;
     * @param b the second value;
     * @return the result of a-b.
     */
    protected abstract T subtract(T a, T b);

    /**
     * Performs the scalar multiplication of a value of the output type.
     *
     * @param a the value;
     * @param b the scalar factor;
     * @return the result of a*b.
     */
    protected abstract T multiply(T a, double b);

    /**
     * An interval function that handles double values.
     */
    public static class Double extends FunctionRect<java.lang.Double> {

        public Double(Interval definitionInterval, double initialValue, double finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
        }

        public Double(Interval definitionInterval, double initialValue, double finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
        }

        @Override
        protected java.lang.Double subtract(java.lang.Double a, java.lang.Double b) {
            return a - b;
        }

        @Override
        protected java.lang.Double multiply(java.lang.Double a, double b) {
            return a * b;
        }
    }

    /**
     * An interval function that handles integer values.
     */
    public static class Integer extends FunctionRect<java.lang.Integer> {

        private final Double delegateFunction;

        public Integer(Interval definitionInterval, java.lang.Integer leftValue, java.lang.Integer rightValue, Interpolation interpolation) {
            super(definitionInterval, leftValue, rightValue, interpolation);
            delegateFunction = new Double(definitionInterval, leftValue, rightValue, interpolation);
        }

        @Override
        protected java.lang.Integer valueAtInterpolationComputation(double interpIntDist) {
            return (int) Math.round(delegateFunction.valueAtInterpolationComputation(interpIntDist));
        }

        @Override
        protected java.lang.Integer subtract(java.lang.Integer a, java.lang.Integer b) {
            throw new IllegalStateException("Not used as the interpolation computation is overridden.");
        }

        @Override
        protected java.lang.Integer multiply(java.lang.Integer a, double b) {
            throw new IllegalStateException("Not used as the interpolation computation is overridden.");
        }
    }

    /**
     * An interval function that handles string values.
     */
    public static class String extends FunctionRect<java.lang.String> {

        public String(Interval definitionInterval, java.lang.String initialValue,
                java.lang.String finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
        }

        public String(Interval definitionInterval, java.lang.String initialValue,
                java.lang.String finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
        }

        @Override
        protected java.lang.String subtract(java.lang.String a, java.lang.String b) {
            throw new UnsupportedOperationException("Not supported for Strings. Use intepolation functions that only return 0 or 1.");
        }

        @Override
        protected java.lang.String multiply(java.lang.String a, double b) {
            throw new UnsupportedOperationException("Not supported for Strings. Use intepolation functions that only return 0 or 1.");
        }
    }

    /**
     * An interval function that handles boolean values.
     */
    public static class Boolean extends FunctionRect<java.lang.Boolean> {

        public Boolean(Interval definitionInterval, java.lang.Boolean initialValue,
                java.lang.Boolean finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
        }

        public Boolean(Interval definitionInterval, java.lang.Boolean initialValue,
                java.lang.Boolean finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
        }

        @Override
        protected java.lang.Boolean subtract(java.lang.Boolean a, java.lang.Boolean b) {
            throw new UnsupportedOperationException("Not supported for Booleans. Use intepolation functions that only return 0 or 1.");
        }

        @Override
        protected java.lang.Boolean multiply(java.lang.Boolean a, double b) {
            throw new UnsupportedOperationException("Not supported for Booleans. Use intepolation functions that only return 0 or 1.");
        }
    }

    /**
     * An interval function that handles string values.
     */
    public static class Coordinates extends FunctionRect<ocotillo.geometry.Coordinates> {

        public Coordinates(Interval definitionInterval, ocotillo.geometry.Coordinates initialValue,
                ocotillo.geometry.Coordinates finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
        }

        public Coordinates(Interval definitionInterval, ocotillo.geometry.Coordinates initialValue,
                ocotillo.geometry.Coordinates finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
        }

        @Override
        protected ocotillo.geometry.Coordinates subtract(ocotillo.geometry.Coordinates a, ocotillo.geometry.Coordinates b) {
            return a.minus(b);
        }

        @Override
        protected ocotillo.geometry.Coordinates multiply(ocotillo.geometry.Coordinates a, double b) {
            return a.times(b);
        }
    }

    /**
     * An interval function that handles colour values.
     */
    public static class Color extends FunctionRect<java.awt.Color> {

        private final YuvColor initialColor;
        private final YuvColor finalColor;
        private final YuvColor outDist;

        public Color(Interval definitionInterval, java.awt.Color initialValue, java.awt.Color finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
            initialColor = new YuvColor(initialValue);
            finalColor = new YuvColor(finalValue);
            outDist = finalColor.minus(initialColor);
        }

        public Color(Interval definitionInterval, java.awt.Color initialValue, java.awt.Color finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
            initialColor = new YuvColor(initialValue);
            finalColor = new YuvColor(finalValue);
            outDist = finalColor.minus(initialColor);
        }

        @Override
        protected java.awt.Color valueAtInterpolationComputation(double interpIntDist) {
            YuvColor relativeOutDist = outDist.times(1 - interpIntDist);
            return finalColor.minus(relativeOutDist).toRgb();
        }

        @Override
        protected java.awt.Color subtract(java.awt.Color a, java.awt.Color b) {
            throw new IllegalStateException("Not used as the interpolation computation is overridden.");
        }

        @Override
        protected java.awt.Color multiply(java.awt.Color a, double b) {
            throw new IllegalStateException("Not used as the interpolation computation is overridden.");
        }
    }

    /**
     * An interval function that handles node shape values.
     */
    public static class NodeShape extends FunctionRect<ocotillo.graph.StdAttribute.NodeShape> {

        public NodeShape(Interval definitionInterval, ocotillo.graph.StdAttribute.NodeShape initialValue,
                ocotillo.graph.StdAttribute.NodeShape finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
        }

        public NodeShape(Interval definitionInterval, ocotillo.graph.StdAttribute.NodeShape initialValue,
                ocotillo.graph.StdAttribute.NodeShape finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
        }

        @Override
        protected ocotillo.graph.StdAttribute.NodeShape subtract(ocotillo.graph.StdAttribute.NodeShape a,
                ocotillo.graph.StdAttribute.NodeShape b) {
            throw new UnsupportedOperationException("Not supported for node shapes. Use intepolation functions that only return 0 or 1.");
        }

        @Override
        protected ocotillo.graph.StdAttribute.NodeShape multiply(ocotillo.graph.StdAttribute.NodeShape a, double b) {
            throw new UnsupportedOperationException("Not supported for node shapes. Use intepolation functions that only return 0 or 1.");
        }
    }

    /**
     * An interval function that handles node shape values.
     */
    public static class EdgeShape extends FunctionRect<ocotillo.graph.StdAttribute.EdgeShape> {

        public EdgeShape(Interval definitionInterval, ocotillo.graph.StdAttribute.EdgeShape initialValue,
                ocotillo.graph.StdAttribute.EdgeShape finalValue, Interpolation interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation);
        }

        public EdgeShape(Interval definitionInterval, ocotillo.graph.StdAttribute.EdgeShape initialValue,
                ocotillo.graph.StdAttribute.EdgeShape finalValue, Interpolation.Std interpolation) {
            super(definitionInterval, initialValue, finalValue, interpolation.get());
        }

        @Override
        protected ocotillo.graph.StdAttribute.EdgeShape subtract(ocotillo.graph.StdAttribute.EdgeShape a,
                ocotillo.graph.StdAttribute.EdgeShape b) {
            throw new UnsupportedOperationException("Not supported for edge shapes. Use intepolation functions that only return 0 or 1.");
        }

        @Override
        protected ocotillo.graph.StdAttribute.EdgeShape multiply(ocotillo.graph.StdAttribute.EdgeShape a, double b) {
            throw new UnsupportedOperationException("Not supported for edge shapes. Use intepolation functions that only return 0 or 1.");
        }
    }
}
