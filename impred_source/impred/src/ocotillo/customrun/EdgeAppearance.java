/**
 * Copyright © 2014-2017 Paolo Simonetto
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
package ocotillo.customrun;

import java.util.ArrayList;
import java.util.List;

/**
 * The appearance of an edge.
 */
public class EdgeAppearance {

    public final String sourceId;
    public final String targetId;
    public final double startTime;
    public final double duration;

    /**
     * Constructor.
     *
     * @param sourceId the id of the source node.
     * @param targetId the id of the target node.
     * @param startTime the time at which it starts appearing.
     * @param duration the duration of the appearance.
     */
    public EdgeAppearance(String sourceId, String targetId, double startTime, double duration) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.startTime = startTime;
        this.duration = duration;
    }

    /**
     * Parses an edge appearance data set.
     *
     * @param lines the data set lines.
     * @return the parsed list of appearances.
     */
    public static List<EdgeAppearance> parseDataSet(List<String> lines) {
        List<EdgeAppearance> appearances = new ArrayList<>();
        for (String line : lines) {
            try {
                String[] tokens = line.split(",");
                EdgeAppearance appearance = new EdgeAppearance(
                        tokens[0],
                        tokens[1],
                        Double.parseDouble(tokens[2]),
                        Double.parseDouble(tokens[3]));
                appearances.add(appearance);
            } catch (Exception e) {
                System.err.println("Error when parsing edge data set line:");
                System.err.println(line);
            }
        }
        return appearances;
    }
}
