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
package ocotillo.graph;

/**
 * Attribute interface.
 *
 * @param <T> The value type associated to this attribute.
 */
public interface Attribute<T> {

    /**
     * Returns the default value for the attribute.
     *
     * @return the default attribute.
     */
    public T getDefault();

    /**
     * Sets the default value for the attribute.
     *
     * @param value the default value.
     */
    public void setDefault(T value);

    /**
     * Returns the attribute type for the current instance.
     *
     * @return the attribute type.
     */
    public Type getAttributeType();

    /**
     * Sets a description for the attribute.
     *
     * @param description a description for the property.
     */
    public void setDescription(String description);

    /**
     * Gets the attribute description.
     *
     * @return the attribute description.
     */
    public String getDescription();

    /**
     * Sets a description for the attribute state.
     *
     * @param stateDescription a description for the current attribute state.
     */
    public void setStateDescription(String stateDescription);

    /**
     * Gets the attribute state description.
     *
     * @return the attribute state description.
     */
    public String getStateDescription();

    /**
     * Checks if an attribute is sleeping, meaning that is an auto-generated
     * standard property with no changes from its default values.
     *
     * @return true if the attribute is sleeping, false otherwise.
     */
    public boolean isSleeping();

    public static enum Type {

        graph,
        node,
        edge
    }

}
