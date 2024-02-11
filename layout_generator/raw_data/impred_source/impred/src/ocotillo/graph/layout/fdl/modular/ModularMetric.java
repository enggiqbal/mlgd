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
package ocotillo.graph.layout.fdl.modular;

import java.util.ArrayList;
import java.util.List;

/**
 * Metric for the ModularFdl algorithm.
 */
public abstract class ModularMetric extends ModularElement {

    protected List<Object> values = new ArrayList<>();

    /**
     * Returns the metric name.
     *
     * @return the metric name.
     */
    public abstract String metricName();

    /**
     * Returns the computed values.
     *
     * @return the metric values.
     */
    public List<Object> values() {
        return values;
    }

    /**
     * Runs the metric code at the end of an iteration.
     */
    protected void runAtIterationEnd() {
        values.add(null);
    }

    /**
     * Runs the metric code at the end of the whole computation.
     */
    protected void runAtComputationEnd() {
        values.add(null);
    }

    /**
     * Metrics that reports the iteration number.
     */
    protected static class IterationNumber extends ModularMetric {

        @Override
        public String metricName() {
            return "Iteration";
        }

        @Override
        protected void runAtIterationEnd() {
            values.add(values.size() + 1);
        }
    }

    /**
     * Metrics that collects the iteration running times. In order to compute an
     * exact value, the values for this metric are directly filled in by the
     * algorithm.
     */
    protected static class IterationRunningTime extends ModularMetric {

        @Override
        public String metricName() {
            return "RunningTime";
        }
    }
}
