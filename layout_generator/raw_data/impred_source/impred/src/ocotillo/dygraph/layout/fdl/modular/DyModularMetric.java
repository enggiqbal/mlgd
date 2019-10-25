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
package ocotillo.dygraph.layout.fdl.modular;

import ocotillo.dygraph.extra.DyGraphMetric;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.StcGraphMetric;
import ocotillo.graph.layout.fdl.modular.ModularMetric;

/**
 * Metrics for the DyModularFdl Algorithm.
 */
public abstract class DyModularMetric extends ModularMetric {

    protected DyModularFdl dyModularFdl;

    /**
     * Attaches the modular element to a DyModularFdl instance. Should be only
     * called by DyModularFdlBuilder.
     *
     * @param dyModularFdl the DyModularFdl instance.
     */
    protected void attachTo(DyModularFdl dyModularFdl) {
        assert (this.dyModularFdl == null) : "The ModularFdl element was already attached to a ModularFdl instance.";
        assert (dyModularFdl != null) : "Attaching the ModularFdl element to a null ModularFdl instance.";
        this.dyModularFdl = dyModularFdl;
    }

    /**
     * Returns the space-time cube synchroniser that acts on the mirror graph.
     *
     * @return the synchroniser.
     */
    protected SpaceTimeCubeSynchroniser stcSynchronizer() {
        assert (dyModularFdl != null) : "The ModularFdl element has not been attached yet.";
        return dyModularFdl.synchronizer;
    }

    /**
     * Computes a dynamic metric at each iteration.
     */
    public static class IterationMetric extends DyModularMetric {

        private final String name;
        private final DyGraphMetric<?> dyMetric;
        private final StcGraphMetric<?> stcMetric;

        /**
         * Builds a module that computes a dynamic graph metric.
         *
         * @param name the metric name;
         * @param metric the metric.
         */
        public IterationMetric(String name, DyGraphMetric<?> metric) {
            this.name = name;
            this.dyMetric = metric;
            this.stcMetric = null;
        }

        /**
         * Builds a module that computes an STC graph metric.
         *
         * @param name the metric name;
         * @param metric the metric.
         */
        public IterationMetric(String name, StcGraphMetric<?> metric) {
            this.name = name;
            this.dyMetric = null;
            this.stcMetric = metric;
        }

        @Override
        public String metricName() {
            return name;
        }

        @Override
        protected void runAtIterationEnd() {
            if (dyMetric != null) {
                stcSynchronizer().updateOriginal();
                values.add(dyMetric.computeMetric(stcSynchronizer().originalGraph()));
            } else {
                values.add(stcMetric.computeMetric(stcSynchronizer()));
            }
        }
    }
}
