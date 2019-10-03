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

import lombok.EqualsAndHashCode;

/**
 * A graph element.
 */
@EqualsAndHashCode
public abstract class Element implements Comparable<Element> {

    private final String id;

    /**
     * Constructs a graph element.
     *
     * @param id the element id.
     */
    public Element(String id) {
        Rules.checkId(id);
        this.id = id;
    }

    /**
     * Returns the id of an element.
     *
     * @return the element id.
     */
    public String id() {
        return id;
    }

    @Override
    public int compareTo(Element other) {
        return id.compareTo(other.id());
    }

    @Override
    public String toString() {
        return id;
    }
}
