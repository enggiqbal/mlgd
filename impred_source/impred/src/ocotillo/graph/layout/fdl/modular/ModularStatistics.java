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

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import ocotillo.serialization.ParserTools;

/**
 * Statistics for the ModularFdl algorithm.
 */
public class ModularStatistics {

    private Duration totalRunningTime = Duration.ZERO;
    private final ModularMetric iterationRunningTimes;
    private final List<ModularMetric> metrics = new ArrayList<>();

    /**
     * Creates a set of statistics for a modular FDL algorithm.
     *
     * @param metrics the metrics to add to the statistics.
     */
    public ModularStatistics(Collection<ModularMetric> metrics) {
        ModularMetric iterationNumber = new ModularMetric.IterationNumber();
        iterationRunningTimes = new ModularMetric.IterationRunningTime();
        this.metrics.add(iterationNumber);
        this.metrics.add(iterationRunningTimes);
        this.metrics.addAll(metrics);
    }

    /**
     * Performs the metric computation at the end of an iteration.
     *
     * @param iterationRunningTime the running time of this iteration.
     */
    protected void runAtIterationEnd(Duration iterationRunningTime) {
        for (ModularMetric metric : metrics) {
            if (metric != iterationRunningTimes) {
                metric.runAtIterationEnd();
            } else {
                iterationRunningTimes.values.add(iterationRunningTime.toMillis() / 1000.0);
            }
        }
    }

    /**
     * Performs the metric computation at the end of a computation.
     *
     * @param totalRunningTime the total running time at the end of this
     * computation.
     */
    protected void runAtComputationEnd(Duration totalRunningTime) {
        for (ModularMetric metric : metrics) {
            metric.runAtComputationEnd();
        }
        this.totalRunningTime = this.totalRunningTime.plus(totalRunningTime);
    }

    /**
     * Gets the total running time.
     *
     * @return the total running time.
     */
    public Duration getTotalRunnningTime() {
        return totalRunningTime;
    }

    /**
     * Gets the list of metrics.
     *
     * @return the metrics.
     */
    public List<ModularMetric> getMetrics() {
        return metrics;
    }

    /**
     * Saves the metrics into a tab-separated values file.
     *
     * @param csvFile the CSV file.
     */
    public void saveCsv(File csvFile) {
        List<String> lines = new ArrayList<>();
        int lineIdx = -1;
        boolean noMoreValues = false;
        while (!noMoreValues) {
            String line = "";
            for (ModularMetric metric : metrics) {
                if (lineIdx == -1) {
                    line += metric.metricName() + "\t";
                } else {
                    if (lineIdx == metric.values.size()) {
                        noMoreValues = true;
                        line += "\t";
                    } else {
                        Object metricValue = metric.values.get(lineIdx);
                        if (metricValue != null) {
                            line += metricValue + "\t";
                        } else {
                            line += "\t";
                        }
                    }
                }
            }
            lines.add(line);
            lineIdx++;
        }
        ParserTools.writeFileLines(lines, csvFile);
    }
}
