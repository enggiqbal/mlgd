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

import ocotillo.graph.GraphAttribute;
import ocotillo.graph.Rules;

/**
 * Dynamic graph attribute.
 *
 * @param <V> the type of value accepted.
 */
public class DyGraphAttribute<V> extends GraphAttribute<Evolution<V>> implements DyAttribute<V> {

    /**
     * Constructs a graph attribute.
     *
     * @param value its current value.
     */
    public DyGraphAttribute(V value) {
        super(new Evolution<>(value));
        Rules.checkAttributeValue(value);
    }

    /**
     * Constructs a graph attribute.
     *
     * @param value its current value.
     */
    public DyGraphAttribute(Evolution<V> value) {
        super(value);
        Rules.checkAttributeValue(value);
    }

    @Override
    public GraphAttribute<V> snapshotAt(double time) {
        GraphAttribute<V> snapshotAttribute = new GraphAttribute<>(getDefault().valueAt(time));
        return snapshotAttribute;
    }

}
