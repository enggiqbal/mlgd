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
package ocotillo.serialization.oco;

import java.awt.Color;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.StdAttribute.EdgeShape;
import ocotillo.graph.StdAttribute.NodeShape;
import ocotillo.graph.rendering.RenderingTools;
import ocotillo.geometry.Interval;

/**
 * Collection of standard oco converters.
 */
public abstract class OcoStandardConverter {

    /**
     * Base for static data converters.
     *
     * @param <T> the type of values handled.
     */
    private abstract static class StOcoValueConverter<T> extends OcoValueConverter<T> {

        @Override
        public Object baseDefaultValue() {
            return defaultValue();
        }
    }

    /**
     * Base for dynamic data converters.
     *
     * @param <T> the type of values handled.
     */
    private abstract static class DyOcoValueConverter<T> extends OcoValueConverter<Evolution<T>> {

        private final OcoValueConverter<T> staticConverter;

        private DyOcoValueConverter(OcoValueConverter<T> staticConverter) {
            this.staticConverter = staticConverter;
        }

        @Override
        public String graphLibToOco(Evolution<T> evolution) {
            String result = staticConverter.graphLibToOco(evolution.getDefaultValue());
            for (Function<T> function : evolution) {
                if (function instanceof FunctionConst) {
                    result += " § const ^ " + function.interval()
                            + " ^ " + staticConverter.graphLibToOco(function.leftValue());
                } else if (function instanceof FunctionRect) {
                    FunctionRect<T> rectFunction = (FunctionRect<T>) function;
                    result += " § rect ^ " + rectFunction.interval()
                            + " ^ " + staticConverter.graphLibToOco(rectFunction.leftValue())
                            + " ^ " + staticConverter.graphLibToOco(rectFunction.rightValue())
                            + " ^ " + rectFunction.interpolation().name();
                } else {
                    throw new UnsupportedOperationException("Oco representation for the following function is not available: " + function);
                }
            }
            return result;
        }

        @Override
        public Evolution<T> ocoToGraphLib(String value) {
            String[] evolutionTokens = value.split(" § ");
            T defaultValue = staticConverter.ocoToGraphLib(evolutionTokens[0]);
            Evolution<T> result = new Evolution<>(defaultValue);
            for (int i = 1; i < evolutionTokens.length; i++) {
                String[] functionTokens = evolutionTokens[i].split(" \\^ ");
                Interval interval = Interval.parse(functionTokens[1]);
                T leftValue = staticConverter.ocoToGraphLib(functionTokens[2]);
                switch (functionTokens[0]) {
                    case "const":
                        result.insert(new FunctionConst<>(interval, leftValue));
                        break;
                    case "rect":
                        T rightValue = staticConverter.ocoToGraphLib(functionTokens[3]);
                        Interpolation.Std interpolation = Interpolation.Std.valueOf(functionTokens[4]);
                        result.insert(buildRect(interval, leftValue, rightValue, interpolation.get()));
                        break;
                    default:
                        throw new UnsupportedOperationException("The type of parameter data is not supported as standard.");
                }
            }
            return result;
        }

        @Override
        public Evolution<T> defaultValue() {
            return new Evolution<>(staticConverter.defaultValue());
        }

        @Override
        public Object baseDefaultValue() {
            return staticConverter.defaultValue();
        }

        @Override
        public String typeName() {
            return staticConverter.typeName();
        }

        @Override
        public Class<?> typeClass() {
            return staticConverter.typeClass();
        }

        public abstract Function<T> buildRect(Interval interval, T leftValue, T rightValue, Interpolation interpolation);
    }

    /**
     * Converter for boolean values in static graphs.
     */
    public static class StaticBoolean extends StOcoValueConverter<Boolean> {

        @Override
        public Boolean ocoToGraphLib(String value) {
            return Boolean.parseBoolean(value);
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }

        @Override
        public String typeName() {
            return "Boolean";
        }

        @Override
        public Class<?> typeClass() {
            return Boolean.class;
        }
    }

    /**
     * Converter for boolean values in dynamic graphs.
     */
    public static class DynamicBoolean extends DyOcoValueConverter<Boolean> {

        public DynamicBoolean() {
            super(new StaticBoolean());
        }

        @Override
        public Function<Boolean> buildRect(Interval interval, Boolean leftValue, Boolean rightValue, Interpolation interpolation) {
            return new FunctionRect.Boolean(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for integer values in static graphs.
     */
    public static class StaticInteger extends StOcoValueConverter<Integer> {

        @Override
        public Integer ocoToGraphLib(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public Integer defaultValue() {
            return 0;
        }

        @Override
        public String typeName() {
            return "Integer";
        }

        @Override
        public Class<?> typeClass() {
            return Integer.class;
        }
    }

    /**
     * Converter for integer values in dynamic graphs.
     */
    public static class DynamicInteger extends DyOcoValueConverter<Integer> {

        public DynamicInteger() {
            super(new StaticInteger());
        }

        @Override
        public Function<Integer> buildRect(Interval interval, Integer leftValue, Integer rightValue, Interpolation interpolation) {
            return new FunctionRect.Integer(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for double values in static graphs.
     */
    public static class StaticDouble extends StOcoValueConverter<Double> {

        @Override
        public Double ocoToGraphLib(String value) {
            return Double.parseDouble(value);
        }

        @Override
        public Double defaultValue() {
            return 0.0;
        }

        @Override
        public String typeName() {
            return "Double";
        }

        @Override
        public Class<?> typeClass() {
            return Double.class;
        }
    }

    /**
     * Converter for double values in dynamic graphs.
     */
    public static class DynamicDouble extends DyOcoValueConverter<Double> {

        public DynamicDouble() {
            super(new StaticDouble());
        }

        @Override
        public Function<Double> buildRect(Interval interval, Double leftValue, Double rightValue, Interpolation interpolation) {
            return new FunctionRect.Double(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for string values in static graphs.
     */
    public static class StaticString extends StOcoValueConverter<String> {

        @Override
        public String ocoToGraphLib(String value) {
            return value;
        }

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public String typeName() {
            return "String";
        }

        @Override
        public Class<?> typeClass() {
            return String.class;
        }
    }

    /**
     * Converter for string values in dynamic graphs.
     */
    public static class DynamicString extends DyOcoValueConverter<String> {

        public DynamicString() {
            super(new StaticString());
        }

        @Override
        public Function<String> buildRect(Interval interval, String leftValue, String rightValue, Interpolation interpolation) {
            return new FunctionRect.String(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for coordinates values in static graphs.
     */
    public static class StaticCoordinates extends StOcoValueConverter<Coordinates> {

        @Override
        public Coordinates ocoToGraphLib(String value) {
            return Coordinates.parse(value);
        }

        @Override
        public String graphLibToOco(Coordinates value) {
            return value.toString();
        }

        @Override
        public Coordinates defaultValue() {
            return new Coordinates(0, 0);
        }

        @Override
        public String typeName() {
            return "Coordinates";
        }

        @Override
        public Class<?> typeClass() {
            return Coordinates.class;
        }
    }

    /**
     * Converter for coordinates values in dynamic graphs.
     */
    public static class DynamicCoordinates extends DyOcoValueConverter<Coordinates> {

        public DynamicCoordinates() {
            super(new StaticCoordinates());
        }

        @Override
        public Function<Coordinates> buildRect(Interval interval, Coordinates leftValue, Coordinates rightValue, Interpolation interpolation) {
            return new FunctionRect.Coordinates(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for colour values in static graphs.
     */
    public static class StaticColor extends StOcoValueConverter<Color> {

        @Override
        public Color ocoToGraphLib(String value) {
            return RenderingTools.colorHexReader(value);
        }

        @Override
        public String graphLibToOco(Color value) {
            return RenderingTools.colorHexWriter(value);
        }

        @Override
        public Color defaultValue() {
            return Color.BLACK;
        }

        @Override
        public String typeName() {
            return "Color";
        }

        @Override
        public Class<?> typeClass() {
            return Color.class;
        }
    }

    /**
     * Converter for colour values in dynamic graphs.
     */
    public static class DynamicColor extends DyOcoValueConverter<Color> {

        public DynamicColor() {
            super(new StaticColor());
        }

        @Override
        public Function<Color> buildRect(Interval interval, Color leftValue, Color rightValue, Interpolation interpolation) {
            return new FunctionRect.Color(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for node shape values in static graphs.
     */
    public static class StaticNodeShape extends StOcoValueConverter<NodeShape> {

        @Override
        public NodeShape ocoToGraphLib(String value) {
            return NodeShape.valueOf(value);
        }

        @Override
        public String graphLibToOco(NodeShape value) {
            return value.name();
        }

        @Override
        public NodeShape defaultValue() {
            return NodeShape.cuboid;
        }

        @Override
        public String typeName() {
            return "NodeShape";
        }

        @Override
        public Class<?> typeClass() {
            return NodeShape.class;
        }
    }

    /**
     * Converter for node shape values in dynamic graphs.
     */
    public static class DynamicNodeShape extends DyOcoValueConverter<NodeShape> {

        public DynamicNodeShape() {
            super(new StaticNodeShape());
        }

        @Override
        public Function<NodeShape> buildRect(Interval interval, NodeShape leftValue, NodeShape rightValue, Interpolation interpolation) {
            return new FunctionRect.NodeShape(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for edge shape values in static graphs.
     */
    public static class StaticEdgeShape extends StOcoValueConverter<EdgeShape> {

        @Override
        public EdgeShape ocoToGraphLib(String value) {
            return EdgeShape.valueOf(value);
        }

        @Override
        public String graphLibToOco(EdgeShape value) {
            return value.name();
        }

        @Override
        public EdgeShape defaultValue() {
            return EdgeShape.polyline;
        }

        @Override
        public String typeName() {
            return "EdgeShape";
        }

        @Override
        public Class<?> typeClass() {
            return EdgeShape.class;
        }
    }

    /**
     * Converter for edge shape values in dynamic graphs.
     */
    public static class DynamicEdgeShape extends DyOcoValueConverter<EdgeShape> {

        public DynamicEdgeShape() {
            super(new StaticEdgeShape());
        }

        @Override
        public Function<EdgeShape> buildRect(Interval interval, EdgeShape leftValue, EdgeShape rightValue, Interpolation interpolation) {
            return new FunctionRect.EdgeShape(interval, leftValue, rightValue, interpolation);
        }
    }

    /**
     * Converter for edge control points values in static graphs.
     */
    public static class StaticControlPoints extends StOcoValueConverter<ControlPoints> {

        @Override
        public ControlPoints ocoToGraphLib(String value) {
            ControlPoints controlPoints = new ControlPoints();
            String[] pointStrings = value.split(" \\| ");
            for (String pointString : pointStrings) {
                controlPoints.add(Coordinates.parse(pointString));
            }
            return controlPoints;

        }

        @Override
        public String graphLibToOco(ControlPoints value) {
            StringBuilder builder = new StringBuilder();
            for (Coordinates point : value) {
                builder.append(point.toString());
                builder.append(" | ");
            }
            return builder.substring(0, Math.max(builder.length() - 3, 0));
        }

        @Override
        public ControlPoints defaultValue() {
            return new ControlPoints();
        }

        @Override
        public String typeName() {
            return "ControlPoints";
        }

        @Override
        public Class<?> typeClass() {
            return ControlPoints.class;
        }
    }
}
