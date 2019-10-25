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
 * Node attribute.
 *
 * @param <V> the type of value accepted.
 */
public class NodeAttribute<V> extends ElementAttribute<Node, V> {

    /**
     * Constructs a node attribute.
     *
     * @param defaultValue the value of a node when not directly set.
     */
    public NodeAttribute(V defaultValue) {
        super(defaultValue);
    }

    @Override
    public Attribute.Type getAttributeType() {
        return Attribute.Type.node;
    }
}
