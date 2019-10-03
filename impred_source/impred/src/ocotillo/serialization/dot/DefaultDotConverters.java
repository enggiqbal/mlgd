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
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of converters for the most common data types.
 */
public class DefaultDotConverters {

    private final Map<Class<?>, DotValueConverter<?>> map = new HashMap<>();

    /**
     * Construct a collection of default converters.
     */
    public DefaultDotConverters() {
        put(Boolean.class, new DotValueConverter.BooleanConverter());
        put(Integer.class, new DotValueConverter.IntegerConverter());
        put(Double.class, new DotValueConverter.DoubleConverter());
        put(String.class, new DotValueConverter.StringConverter());
        put(Coordinates.class, new DotValueConverter.CoordinatesConverter());
        put(Color.class, new DotValueConverter.ColorConverter());
    }

    /**
     * Adds or substitutes a default converter to the collection.
     *
     * @param <T> the type of object handled.
     * @param type the type class.
     * @param converter the converter.
     */
    public final <T> void put(Class<T> type, DotValueConverter<T> converter) {
        map.put(type, converter);
    }

    /**
     * Gets the default converter associated with the given type.
     *
     * @param <T> the type of object handled.
     * @param type the type supported by the converter.
     * @return the converter.
     */
    @SuppressWarnings("unchecked")
    public <T> DotValueConverter<T> get(Class<T> type) {
        return (DotValueConverter<T>) map.get(type);
    }

    /**
     * Verifies if it contains a default converter for the giver type.
     *
     * @param type the type.
     * @return true if the collection contains it, false otherwise.
     */
    public boolean contains(Class<?> type) {
        return map.containsKey(type);
    }

    /**
     * Removes a default converter from the collection.
     *
     * @param type the class for which removing the converter.
     */
    public void remove(Class<?> type) {
        map.remove(type);
    }

    /**
     * Clears the collection.
     */
    public void clear() {
        map.clear();
    }

}
