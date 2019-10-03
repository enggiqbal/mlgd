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
package ocotillo.serialization.dot;

import java.util.HashMap;

public class DotTools {

    /**
     * The id of the attribute that converts the dot parameter strict.
     */
    public final static String strictAttr = "strict";
    /**
     * The id of the attribute that converts the dot parameter for directed
     * graphs and for edge directionality.
     */
    public final static String directedAttr = "directed";
    /**
     * The id of the attribute that stores the dot graph name.
     */
    public final static String graphNameAttr = "name";
    /**
     * The id of the attribute that stores the dot polygons.
     */
    public final static String polygonIdAttr = "polygons";
    /**
     * The factor that converts dot units of node size in dot units of space.
     */
    public final static double sizeFactor = 72;

    /**
     * The id of the attribute temporarily used to store the edge source.
     */
    protected final static String edgeSourceAttr = "# EdgeSource #";
    /**
     * The id of the attribute temporarily used to store the edge target.
     */
    protected final static String edgeTargetAttr = "# EdgeTarget #";
    /**
     * The dot graph attribute that stores the polygons.
     */
    protected final static String[] polygonDotAttr = {"_background", "_draw_"};

    /**
     * A map that associate dot attributes of a graph entity to their values.
     */
    protected static class DotAttributes extends HashMap<String, String> {

        private static final long serialVersionUID = 1L;

        public DotAttributes() {
            super();
        }

        public DotAttributes(DotAttributes attributes) {
            super(attributes);
        }
    }

    /**
     * Lists and detect the possible type of lines present in a dot file.
     */
    protected enum DotLineType {

        opening,
        graphGlobal,
        nodeGlobal,
        edgeGlobal,
        node,
        edge,
        empty;

        /**
         * Detects the type of an dot line.
         *
         * @param lineHeader the line header.
         * @return the type detected for the line.
         */
        protected static DotLineType detect(String lineHeader) {
            if (lineHeader.isEmpty() || lineHeader.equals("}")) {
                return DotLineType.empty;
            }
            if (lineHeader.endsWith("{")
                    && (lineHeader.startsWith("graph")
                    || lineHeader.startsWith("digraph")
                    || lineHeader.startsWith("strict"))) {
                return DotLineType.opening;
            }
            if (lineHeader.startsWith("graph")) {
                return DotLineType.graphGlobal;
            }
            if (lineHeader.startsWith("node")) {
                return DotLineType.nodeGlobal;
            }
            if (lineHeader.startsWith("edge")) {
                return DotLineType.edgeGlobal;
            }
            if (lineHeader.contains("--") || lineHeader.contains("->")) {
                return DotLineType.edge;
            }
            return DotLineType.node;
        }
    }

}
