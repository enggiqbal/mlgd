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
package ocotillo.graph.extra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ocotillo.graph.Element;

/**
 * A lookup table for containing symmetric relations between elements. Symmetric
 * relations imply that the results of operation(a, b) and operation(b, a) is
 * the same. Therefore, the values set or retrieved from this data structure do
 * not depend on the order of the parameters.
 *
 * @param <U> the first type of element in the relation.
 * @param <V> the second type of element in the relation.
 * @param <E> the type of value to store in the lookup.
 */
public class SymmetricLookupTable<U extends Element, V extends Element, E> {

    /**
     * The structure containing the data to store and return. The lookup has two
     * levels, which can be retrieved using the map keys. First level elements
     * are those used directly on the lookup (e.g. element a in lookup.get(a)).
     * Second level elements are used at a deeper stage (e.g. element b in
     * lookup.get(a).get(b)). First and second level elements can have the same
     * or different types. When they have the same type, the elements are
     * automatically swapped so that the first element is always minor than the
     * second.
     */
    private final Map<Element, Map<Element, E>> lookup = new HashMap<>();

    /**
     * This structure containing references to second level entries in the
     * lookup. When an element needs to be erased in the lookup, it is not
     * sufficient to clear the first level entry for the element, since the
     * lookup might contains values stored using the element as second-level
     * key. This structure contains all the first-level keys for which a
     * specific element appears as second-level.
     */
    private final Map<Element, Set<Element>> presenceInLookup = new HashMap<>();

    /**
     * Gets the stored value for a given pair.
     *
     * @param elementA the first element of the pair.
     * @param elementB the second element of the pair.
     * @return null if the lookup does not contain a value for the pair, the
     * value otherwise.
     */
    @SuppressWarnings("unchecked")
    public E get(U elementA, V elementB) {
        U first = elementA;
        V second = elementB;
        if (sameClass(elementA, elementB) && elementA.compareTo(elementB) > 0) {
            first = (U) elementB;
            second = (V) elementA;
        }

        if (lookup.containsKey(first) && lookup.get(first).containsKey(second)) {
            return lookup.get(first).get(second);
        } else {
            return null;
        }
    }

    /**
     * Stores a value for a given pair.
     *
     * @param elementA the first element of the pair.
     * @param elementB the second element of the pair.
     * @param value the value to store.
     */
    @SuppressWarnings("unchecked")
    public void set(U elementA, V elementB, E value) {
        U first = elementA;
        V second = elementB;
        if (sameClass(elementA, elementB) && elementA.compareTo(elementB) > 0) {
            first = (U) elementB;
            second = (V) elementA;
        }

        ensureMapEntriesPresence(first, second);
        lookup.get(first).put(second, value);
        presenceInLookup.get(second).add(first);
    }

    /**
     * Clears all the stored values involving a given element.
     *
     * @param element the element for which erasing all stored values.
     */
    public void erase(Element element) {
        if (presenceInLookup.containsKey(element)) {
            for (Element first : presenceInLookup.get(element)) {
                if (lookup.containsKey(first)) {
                    lookup.get(first).remove(element);
                }
            }
        }
        lookup.remove(element);
        presenceInLookup.remove(element);
    }

    /**
     * Clears all stored values.
     */
    public void clear() {
        lookup.clear();
        presenceInLookup.clear();
    }

    /**
     * Returns true if the elements are both nodes or edges.
     *
     * @param elementA the first element of the pair.
     * @param elementB the second element of the pair.
     * @return true if the are of the same type, false otherwise.
     */
    private boolean sameClass(Element a, Element b) {
        return a.getClass().equals(b.getClass());
    }

    /**
     * Ensures that the data structures can perform operations on the given
     * pair.
     *
     * @param first the first element of the pair.
     * @param second the second element of the pair.
     */
    private void ensureMapEntriesPresence(U first, V second) {
        if (!lookup.containsKey(first)) {
            lookup.put(first, new HashMap<>());
        }
        if (!presenceInLookup.containsKey(second)) {
            presenceInLookup.put(second, new HashSet<>());
        }
    }
}
