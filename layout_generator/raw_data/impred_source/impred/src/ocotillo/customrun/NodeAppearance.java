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
 * The appearance of a node.
 */
public class NodeAppearance {

    public final String id;
    public final double startTime;
    public final double duration;

    /**
     * Constructor.
     *
     * @param id the node id.
     * @param startTime the time at which it starts appearing.
     * @param duration the duration of the appearance.
     */
    public NodeAppearance(String id, double startTime, double duration) {
        this.id = id;
        this.startTime = startTime;
        this.duration = duration;
    }

    /**
     * Parses a node appearance data set.
     *
     * @param lines the data set lines.
     * @return the parsed list of appearances.
     */
    public static List<NodeAppearance> parseDataSet(List<String> lines) {
        List<NodeAppearance> appearances = new ArrayList<>();
        for (String line : lines) {
            try {
                String[] tokens = line.split(",");
                NodeAppearance appearance = new NodeAppearance(
                        tokens[0],
                        Double.parseDouble(tokens[1]),
                        Double.parseDouble(tokens[2]));
                appearances.add(appearance);
            } catch (Exception e) {
                System.err.println("Error when parsing node data set line:");
                System.err.println(line);
            }
        }
        return appearances;
    }
}
