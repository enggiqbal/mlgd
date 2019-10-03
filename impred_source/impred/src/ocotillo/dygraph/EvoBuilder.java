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

import java.awt.Color;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;
import ocotillo.geometry.Interval;

/**
 * Provides a quick way of building standard evolutions.
 */
public class EvoBuilder {

    /**
     * The builder class,
     *
     * @param <T> the type of data handled.
     */
    public static abstract class Builder<T> {

        public final Evolution<T> evolution;

        /**
         * Private constructor.
         *
         * @param evolution the evolution.
         */
        private Builder(Evolution<T> evolution) {
            this.evolution = evolution;
        }

        /**
         * Ends the construction and returns the evolution.
         *
         * @return the constructed evolution.
         */
        public Evolution<T> build() {
            return evolution;
        }

        /**
         * Adds a constant function to the evolution.
         *
         * @param interval the interval in which the function is defined.
         * @param value the value assumed by the function.
         * @return the builder.
         */
        public Builder<T> withConst(Interval interval, T value) {
            evolution.insert(new FunctionConst<>(interval, value));
            return this;
        }

        /**
         * Adds a function that computes values in the rectilinear line
         * connecting left and right value.
         *
         * @param interval the interval in which the function is defined.
         * @param leftValue the value at the left bound of the interval.
         * @param rightValue the value at the right bound of the interval.
         * @param interpolation the type of interpolation used.
         * @return the builder.
         */
        public abstract Builder<T> withRect(Interval interval, T leftValue, T rightValue, Interpolation interpolation);

        /**
         * Adds a function that computes values in the rectilinear line
         * connecting left and right value.
         *
         * @param interval the interval in which the function is defined.
         * @param leftValue the value at the left bound of the interval.
         * @param rightValue the value at the right bound of the interval.
         * @param interpolation the type of standard interpolation used.
         * @return the builder.
         */
        public Builder<T> withRect(Interval interval, T leftValue, T rightValue, Interpolation.Std interpolation) {
            return withRect(interval, leftValue, rightValue, interpolation.get());
        }
    }

    /**
     * Create a builder for double functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<Double> defaultAt(Double defaultValue) {
        return new DoubleBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for double values.
     */
    private static class DoubleBuilder extends Builder<Double> {

        private DoubleBuilder(Evolution<Double> evolution) {
            super(evolution);
        }

        @Override
        public Builder<Double> withRect(Interval interval, Double leftValue, Double rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.Double(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for integer functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<Integer> defaultAt(Integer defaultValue) {
        return new IntegerBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for integer values.
     */
    private static class IntegerBuilder extends Builder<Integer> {

        private IntegerBuilder(Evolution<Integer> evolution) {
            super(evolution);
        }

        @Override
        public Builder<Integer> withRect(Interval interval, Integer leftValue, Integer rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.Integer(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for string functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<String> defaultAt(String defaultValue) {
        return new StringBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for string values.
     */
    private static class StringBuilder extends Builder<String> {

        private StringBuilder(Evolution<String> evolution) {
            super(evolution);
        }

        @Override
        public Builder<String> withRect(Interval interval, String leftValue, String rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.String(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for boolean functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<Boolean> defaultAt(Boolean defaultValue) {
        return new BooleanBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for boolean values.
     */
    private static class BooleanBuilder extends Builder<Boolean> {

        public BooleanBuilder(Evolution<Boolean> evolution) {
            super(evolution);
        }

        @Override
        public Builder<Boolean> withRect(Interval interval, Boolean leftValue, Boolean rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.Boolean(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for coordinates functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<Coordinates> defaultAt(Coordinates defaultValue) {
        return new CoordinatesBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for coordinates values.
     */
    private static class CoordinatesBuilder extends Builder<Coordinates> {

        public CoordinatesBuilder(Evolution<Coordinates> evolution) {
            super(evolution);
        }

        @Override
        public Builder<Coordinates> withRect(Interval interval, Coordinates leftValue, Coordinates rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.Coordinates(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for colour functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<Color> defaultAt(Color defaultValue) {
        return new ColorBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for colour values.
     */
    private static class ColorBuilder extends Builder<Color> {

        public ColorBuilder(Evolution<Color> evolution) {
            super(evolution);
        }

        @Override
        public Builder<Color> withRect(Interval interval, Color leftValue, Color rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.Color(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for node shape functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<NodeShape> defaultAt(NodeShape defaultValue) {
        return new NodeShapeBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for node shape values.
     */
    private static class NodeShapeBuilder extends Builder<NodeShape> {

        public NodeShapeBuilder(Evolution<NodeShape> evolution) {
            super(evolution);
        }

        @Override
        public Builder<NodeShape> withRect(Interval interval, NodeShape leftValue, NodeShape rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.NodeShape(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }

    /**
     * Create a builder for edge shape functions.
     *
     * @param defaultValue the default value for the function.
     * @return the builder.
     */
    public static Builder<EdgeShape> defaultAt(EdgeShape defaultValue) {
        return new EdgeShapeBuilder(new Evolution<>(defaultValue));
    }

    /**
     * The builder for edge shape values.
     */
    private static class EdgeShapeBuilder extends Builder<EdgeShape> {

        public EdgeShapeBuilder(Evolution<EdgeShape> evolution) {
            super(evolution);
        }

        @Override
        public Builder<EdgeShape> withRect(Interval interval, EdgeShape leftValue, EdgeShape rightValue, Interpolation interpolation) {
            evolution.insert(new FunctionRect.EdgeShape(interval, leftValue, rightValue, interpolation));
            return this;
        }
    }
}
