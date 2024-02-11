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

import java.util.Map;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.Rules;

/**
 * Dynamic graph attribute.
 *
 * @param <V> the type of value accepted.
 */
public class DyNodeAttribute<V> extends NodeAttribute<Evolution<V>> implements DyAttribute<V> {

    /**
     * Constructs a graph attribute.
     *
     * @param defaultValue its default value.
     */
    public DyNodeAttribute(V defaultValue) {
        super(new Evolution<>(defaultValue));
        Rules.checkAttributeValue(defaultValue);
    }

    /**
     * Constructs a graph attribute.
     *
     * @param defaultValue its default value.
     */
    public DyNodeAttribute(Evolution<V> defaultValue) {
        super(defaultValue);
        Rules.checkAttributeValue(defaultValue);
    }

    @Override
    public NodeAttribute<V> snapshotAt(double time) {
        NodeAttribute<V> snapshotAttribute = new NodeAttribute<>(getDefault().getDefaultValue());
        for (Map.Entry<Node, Evolution<V>> entry : this) {
            Node node = entry.getKey();
            V value = entry.getValue().valueAt(time);
            snapshotAttribute.set(node, value);
        }
        return snapshotAttribute;
    }
}
