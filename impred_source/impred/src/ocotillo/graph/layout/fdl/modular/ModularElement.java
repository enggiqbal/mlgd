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

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser;
import ocotillo.graph.layout.locator.ElementLocator;

/**
 * Base class for modules of the ModularFdl algorithm.
 */
public class ModularElement {

    private ModularFdl modularFdl;

    /**
     * Attaches the modular element to a ModularFdl instance. Should be only
     * called by ModularFdlBuilder.
     *
     * @param modularFdl the ModularFdl instance.
     */
    protected void attachTo(ModularFdl modularFdl) {
        assert (this.modularFdl == null) : "The ModularFdl element was already attached to a ModularFdl instance.";
        assert (modularFdl != null) : "Attaching the ModularFdl element to a null ModularFdl instance.";
        this.modularFdl = modularFdl;
    }

    /**
     * Returns the mirror graph to be used to compute the ModularFdl elements.
     *
     * @return the mirror graph.
     */
    protected final Graph mirrorGraph() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.mirrorGraph;
    }

    /**
     * Returns the node positions for the mirror graph.
     *
     * @return the node positions.
     */
    protected final NodeAttribute<Coordinates> mirrorPositions() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.mirrorPositions;
    }

    /**
     * Returns the synchroniser that acts on the mirror graph.
     *
     * @return the synchroniser.
     */
    protected final BendExplicitGraphSynchroniser synchronizer() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.synchronizer;
    }

    /**
     * Returns the locator that acts on the mirror graph.
     *
     * @return the locator.
     */
    protected final ElementLocator locator() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.locator;
    }

    /**
     * Returns the current temperature.
     *
     * @return the temperature.
     */
    protected final double temperature() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.thermostat.temperature;
    }

    /**
     * Returns the current forces.
     *
     * @return the forces.
     */
    protected final NodeAttribute<Coordinates> forces() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.forces;
    }

    /**
     * Returns the current constraints.
     *
     * @return the constraints.
     */
    protected final NodeAttribute<Double> constraints() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.constraints;
    }

    /**
     * Returns the current movements.
     *
     * @return the movements.
     */
    protected final NodeAttribute<Coordinates> movements() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.movements;
    }
    
    /**
     * Returns the node levels for the mirror graph.
     *
     * @return the node levels.
     */
    protected final NodeAttribute<Integer> mirrorLevels() {
        assert (modularFdl != null) : "The ModularFdl element has not been attached yet.";
        return modularFdl.mirrorLevels;
    }
}
