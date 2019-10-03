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
 * Collects the rules used in the graph.
 */
public class Rules {

    /**
     * Checks if the id of a graph component is well formed.
     *
     * @param id the id.
     */
    public static void checkId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("The ids in a graph cannot be null or empty");
        }
        if (containsReservedCharacters(id)) {
            throw new IllegalArgumentException("The id + \"" + id + "\" contains illegal characters");
        }
    }

    /**
     * Checks if a string contains reserved characters.
     *
     * @param string a string.
     * @return true if it contains reserved characters, false otherwise.
     */
    public static boolean containsReservedCharacters(String string) {
        return string.matches("[\"#@]");
    }

    /**
     * Checks if the attribute values are acceptable.
     *
     * @param value the value to be assigned.
     */
    public static void checkAttributeValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("An attribute value cannot be null");
        }
    }

}
