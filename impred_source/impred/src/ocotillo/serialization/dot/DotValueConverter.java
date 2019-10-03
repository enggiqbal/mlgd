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
package ocotillo.serialization.dot;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.rendering.RenderingTools;
import java.awt.Color;
import java.util.Locale;

/**
 * Specifications on how to convert a graph attribute to a dot one, and
 * vice-versa. Note that some converted might handle only the conversion in one
 * direction.
 *
 * @param <T> The type of object handled.
 */
public abstract class DotValueConverter<T> {

    /**
     * Returns the object built according to the dot string specifications.
     *
     * @param value the specifications.
     * @return the built object.
     */
    public T dotToGraphLib(String value) {
        return null;
    }

    /**
     * Returns the dot string that describes the given object.
     *
     * @param value the object.
     * @return the string description.
     */
    public String graphLibToDot(T value) {
        return value.toString();
    }

    /**
     * Returns the default value for a graph library object of this type.
     *
     * @return the default value.
     */
    public T defaultValue() {
        return null;
    }

    /**
     * Converter for boolean values.
     */
    public static class BooleanConverter extends DotValueConverter<Boolean> {

        @Override
        public Boolean dotToGraphLib(String value) {
            return Boolean.parseBoolean(value);
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }

    }

    /**
     * Converter for integer values.
     */
    public static class IntegerConverter extends DotValueConverter<Integer> {

        @Override
        public Integer dotToGraphLib(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public Integer defaultValue() {
            return 0;
        }

    }

    /**
     * Converter for double values.
     */
    public static class DoubleConverter extends DotValueConverter<Double> {

        @Override
        public Double dotToGraphLib(String value) {
            return Double.parseDouble(value);
        }

        @Override
        public Double defaultValue() {
            return 0.0;
        }

    }

    /**
     * Converter for string values.
     */
    public static class StringConverter extends DotValueConverter<String> {

        @Override
        public String dotToGraphLib(String value) {
            return value;
        }

        @Override
        public String defaultValue() {
            return "";
        }

    }

    /**
     * Converter for coordinates values.
     */
    public static class CoordinatesConverter extends DotValueConverter<Coordinates> {

        private final double scaling;

        /**
         * Constructs a converter for coordinates value.
         */
        public CoordinatesConverter() {
            this.scaling = 1;
        }

        /**
         * Constructs a converter for coordinates value with a scaling factor.
         *
         * @param scaling the dot to graph scaling factor.
         */
        public CoordinatesConverter(double scaling) {
            this.scaling = scaling;
        }

        @Override
        public Coordinates dotToGraphLib(String value) {
            String[] components = value.split(",");
            Coordinates coordinates = new Coordinates(Math.max(components.length, 3));
            for (int i = 0; i < components.length; i++) {
                coordinates.setAt(i, Double.parseDouble(components[i]) * scaling);
            }
            return coordinates;
        }

        @Override
        public String graphLibToDot(Coordinates value) {
            String x = String.format(Locale.ENGLISH, "%.2f", value.x() / scaling);
            String y = String.format(Locale.ENGLISH, "%.2f", value.y() / scaling);
            return x + "," + y;
        }

        @Override
        public Coordinates defaultValue() {
            return new Coordinates(0, 0);
        }

    }

    /**
     * Converter for a single dimension of a coordinates object. Only operates
     * from graph to dot.
     */
    public static class CoordDimensionConverter extends DotValueConverter<Coordinates> {

        final int dimensionIdx;
        final double scaling;

        /**
         * Constructs a converter for a single dimension of a coordinates
         * object.
         *
         * @param dimensionIdx the dimension index.
         */
        public CoordDimensionConverter(int dimensionIdx) {
            this.dimensionIdx = dimensionIdx;
            this.scaling = 1;
        }

        /**
         * Constructs a converter for a single dimension of a coordinates object
         * with a scaling factor.
         *
         * @param dimensionIdx the dimension index.
         * @param scaling the dot to graph scaling factor.
         */
        public CoordDimensionConverter(int dimensionIdx, double scaling) {
            this.dimensionIdx = dimensionIdx;
            this.scaling = scaling;
        }

        @Override
        public String graphLibToDot(Coordinates value) {
            return String.format(Locale.ENGLISH, "%.2f", value.get(dimensionIdx) / scaling);
        }
    }

    /**
     * Converter for size values.
     */
    public static class PositionConverter extends CoordinatesConverter {

    	  public PositionConverter(double factor) {
    	    	 super(factor);
    	     //   super(1/DotTools.sizeFactor);
    	    }
        public PositionConverter( ) {
        	 super();
         //   super(1/DotTools.sizeFactor);
        }
    }


  
 

    /**
     * Converter for size values.
     */
    public static class PositionDimensionConverter extends CoordDimensionConverter {

        public PositionDimensionConverter(int dimensionIdx) {
            super(dimensionIdx, 1/DotTools.sizeFactor);
        }
    }

    /**
     * Converter for size values.
     */
    public static class SizeConverter extends CoordinatesConverter {

        public SizeConverter() {
            super();
        }
    }

    /**
     * Converter for size values.
     */
    public static class SizeDimensionConverter extends CoordDimensionConverter {

        public SizeDimensionConverter(int dimensionIdx) {
            super(dimensionIdx );
          //  super(dimensionIdx,DotTools.sizeFactor);
        }
    }

    /**
     * Converter for string values.
     */
    public static class ColorConverter extends DotValueConverter<Color> {

        @Override
        public Color defaultValue() {
            return Color.BLACK;
        }

        @Override
        public String graphLibToDot(Color value) {
            return RenderingTools.colorHexWriter(value);
        }

        @Override
        public Color dotToGraphLib(String value) {
            return RenderingTools.colorHexReader(value);
        }

    }

}
