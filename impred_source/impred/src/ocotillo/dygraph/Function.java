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

import ocotillo.geometry.Interval;
import ocotillo.structures.IntervalTree;

/**
 * A function defined on a single interval.
 *
 * @param <T> the type of data handled by the function.
 */
public interface Function<T> extends IntervalTree.Data {

    /**
     * Returns the definition interval of the function.
     *
     * @return the definition interval.
     */
    @Override
    public Interval interval();

    /**
     * Returns the value at the beginning of the interval.
     *
     * @return the initial value.
     */
    public T leftValue();

    /**
     * Returns the value at the end of the interval.
     *
     * @return the final value.
     */
    public T rightValue();

    /**
     * Returns the value assumed by the function at a given point.
     *
     * @param point the point.
     * @return the function value at that point.
     */
    public T valueAt(double point);

}
