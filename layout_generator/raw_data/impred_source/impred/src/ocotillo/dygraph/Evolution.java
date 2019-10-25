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

import java.util.Collection;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import ocotillo.structures.IntervalTree;

/**
 * Describes a property that change over time as a piecewise function.
 *
 * @param <T> the type of property handled.
 */
@EqualsAndHashCode
public class Evolution<T> implements Iterable<Function<T>> {

    private final IntervalTree<Function<T>> intervalTree = new IntervalTree<>();
    private T defaultValue;

    /**
     * Defines a new evolution.
     *
     * @param defaultValue the value returned by the evolution for an undefined
     * point.
     */
    public Evolution(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the default value, that is the of an undefined point.
     *
     * @return the default value.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value, that is the value of an undefined point.
     *
     * @param defaultValue the default value.
     */
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Checks if the evolution is defined ad a given point.
     *
     * @param x the point to check.
     * @return true if the evolution is defined for that point.
     */
    public boolean isDefinedAt(double x) {
        return intervalTree.getAnyContaining(x) != null;
    }

    /**
     * Gets the value of the evolution at a given point.
     *
     * @param x the point.
     * @return the evolution value at that point.
     */
    public T valueAt(double x) {
        Function<T> function = intervalTree.getAnyContaining(x);
        if (function != null) {
            return function.valueAt(x);
        } else {
            return defaultValue;
        }
    }

    /**
     * Inserts a function in the evolution.
     *
     * @param function the function to insert.
     */
    public void insert(Function<T> function) {
        intervalTree.insert(function);
    }

    /**
     * Inserts all the given functions in the evolution.
     *
     * @param functionSet the functions to insert.
     */
    public void insertAll(Collection<Function<T>> functionSet) {
        intervalTree.insertAll(functionSet);
    }

    /**
     * Deletes a function from the evolution.
     *
     * @param function the function to delete.
     */
    public void delete(Function<T> function) {
        intervalTree.delete(function);
    }

    /**
     * Deletes all given functions from the evolution.
     *
     * @param functionSet the functions to delete.
     */
    public void deleteAll(Collection<Function<T>> functionSet) {
        intervalTree.deleteAll(functionSet);
    }

    /**
     * Checks if a function is contained in the evolution.
     *
     * @param function the function.
     * @return true if the function is contained.
     */
    public boolean contains(Function<T> function) {
        return intervalTree.contains(function);
    }

    /**
     * Returns the number of functions in the evolution.
     *
     * @return the number of functions in the evolution.
     */
    public int size() {
        return intervalTree.size();
    }

    /**
     * Checks if the evolution is empty.
     *
     * @return true if the evolution contains no functions.
     */
    public boolean isEmpty() {
        return intervalTree.isEmpty();
    }

    /**
     * Clears the evolution.
     */
    public void clear() {
        intervalTree.clear();
    }

    @Override
    public Iterator<Function<T>> iterator() {
        return intervalTree.iterator();
    }
}
