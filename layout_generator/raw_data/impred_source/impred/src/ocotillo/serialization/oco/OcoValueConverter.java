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

/**
 * Converts values from graph to oco files and vice versa.
 *
 * @param <T> the type of value handled.
 */
public abstract class OcoValueConverter<T> {

    /**
     * Returns the object built according to the oco string specifications.
     *
     * @param value the specifications.
     * @return the built object.
     */
    public T ocoToGraphLib(String value) {
        return null;
    }

    /**
     * Returns the oco string that describes the given object.
     *
     * @param value the object.
     * @return the string description.
     */
    public String graphLibToOco(T value) {
        return value.toString();
    }

    /**
     * Returns the default value for this type.
     *
     * @return the default value.
     */
    public abstract T defaultValue();

    /**
     * Returns the default value for the type of class handled by this
     * converter.
     *
     * @return the default value of the base class.
     */
    public abstract Object baseDefaultValue();

    /**
     * Returns the type name, used to identify the type in the oco file.
     *
     * @return the default name.
     */
    public abstract String typeName();

    /**
     * Returns the type of values handled.
     *
     * @return the type class.
     */
    public abstract Class<?> typeClass();
}
