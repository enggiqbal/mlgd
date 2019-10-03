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
package ocotillo.graph.layout.or.repulsion;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.Layout2D;
import ocotillo.graph.layout.fdl.modular.ModularFdl;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.modular.ModularForce;

/**
 * Removes node overlap by running a force directed algorithm with repulsive
 * forces between nodes.
 */
public class RepulsionOverlapRemover {

    /**
     * Performs the overlap removal computation.
     *
     * @param graph the graph.
     * @param margin the margin to leave around each node.
     */
    public static void run(Graph graph, double margin) {
        NodeAttribute<Coordinates> positions = graph.<Coordinates>nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> sizes = graph.<Coordinates>nodeAttribute(StdAttribute.nodeSize);
        while (Layout2D.doNodesOverlap(graph, positions, sizes)) {
            ModularFdl modularFdl = new ModularFdl.ModularFdlBuilder(graph)
                    .withForce(new ModularForce.NodeNodeRepulsion2D(2 * margin))
                    .withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * margin))
                    .build();
            modularFdl.iterate(50);
        }
    }
}
