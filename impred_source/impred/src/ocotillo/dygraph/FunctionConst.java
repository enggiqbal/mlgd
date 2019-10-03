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

import lombok.EqualsAndHashCode;
import ocotillo.geometry.Interval;

/**
 * A function that is constant in the defined interval.
 *
 * @param <T> the type of data handled.
 */
@EqualsAndHashCode
public class FunctionConst<T> implements Function<T> {

    private final Interval interval;
    private final T value;

    public FunctionConst(Interval interval, T value) {
        this.interval = interval;
        this.value = value;
    }

    @Override
    public Interval interval() {
        return interval;
    }

    @Override
    public T leftValue() {
        return value;
    }

    @Override
    public T rightValue() {
        return value;
    }

    @Override
    public T valueAt(double point) {
        return value;
    }

}
