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

import java.util.ArrayList;
import java.util.List;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.MirrorLine;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Node;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.MirrorEdge;
import ocotillo.graph.layout.fdl.modular.*;

/**
 * PreMovement for the DyModularFdl algorithm.
 */
public abstract class DyModularPreMovement extends ModularPreMovement {

    private DyModularFdl dyModularFdl;

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
     * Pre-movement phase that ensures that the time positions are valid. In
     * particular, this module prevents mirror segment extremities to change
     * their time coordinate, and that bends are not switching order with other
     * segment points.
     */
    public static class EnsureTimeCorrectness extends DyModularPreMovement {

        @Override
        protected void execute() {
            for (MirrorLine segment : stcSynchronizer().mirrorLines()) {
                MirrorEdge edge = synchronizer().getMirrorEdge(segment.mirrorEdge());
                movements().get(edge.source()).setZ(0);
                movements().get(edge.target()).setZ(0);
                List<Node> edgePoints = getEdgePoints(edge);
                List<Coordinates> finalPositions = getFinalPositions(edgePoints);
                List<Double> factors = initialiseFactors(finalPositions.size());
                for (int i = 0; i < edgePoints.size() - 1; i++) {
                    limitMovement(i, edgePoints, finalPositions, factors);
                }
                for (int i = 1; i < edgePoints.size() - 1; i++) {
                    movements().set(edgePoints.get(i), movements().get(edgePoints.get(i)).times(factors.get(i)));
                }
            }
        }

        /**
         * Gets the points (extremities and bends) relative to a mirror edge.
         *
         * @param edge the mirror edge.
         * @return the list of mirror edge nodes.
         */
        private List<Node> getEdgePoints(MirrorEdge edge) {
            List<Node> edgePoints = new ArrayList<>();
            edgePoints.add(edge.source());
            edgePoints.addAll(edge.bends());
            edgePoints.add(edge.target());
            return edgePoints;
        }

        /**
         * Gets the positions assumed by the edge points after the desired
         * movement.
         *
         * @param edgePoints the edge points.
         * @return their positions after the current movement.
         */
        private List<Coordinates> getFinalPositions(List<Node> edgePoints) {
            List<Coordinates> finalPositions = new ArrayList<>();
            for (Node node : edgePoints) {
                finalPositions.add(movements().get(node).plus(mirrorPositions().get(node)));
            }
            return finalPositions;
        }

        /**
         * Initialises the factors used to limit the node movements in case of
         * invalid configurations.
         *
         * @param size the number of edge points.
         * @return the list of factors initialised to 1.
         */
        private List<Double> initialiseFactors(int size) {
            List<Double> factors = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                factors.add(1.0);
            }
            return factors;
        }

        /**
         * Limits the movement for a couple of consecutive edge points.
         *
         * @param index the index of the first edge point.
         * @param edgePoints the edge points.
         * @param finalPositions the node position after the current movement.
         * @param factors the current factors.
         */
        private void limitMovement(int index, List<Node> edgePoints, List<Coordinates> finalPositions, List<Double> factors) {
            Node a = edgePoints.get(index);
            Node b = edgePoints.get(index + 1);
            Coordinates aInitialPos = mirrorPositions().get(a);
            Coordinates bInitialPos = mirrorPositions().get(b);
            double halfInitialDist = ModularFdl.safetyMovementFactor * (bInitialPos.z() - aInitialPos.z()) / 2;
            double aMovZ = finalPositions.get(index).z() - aInitialPos.z();
            double bMovZ = bInitialPos.z() - finalPositions.get(index + 1).z();
            if (aMovZ > halfInitialDist) {
                factors.set(index, Math.min(factors.get(index), halfInitialDist / aMovZ));
            }
            if (bMovZ > halfInitialDist) {
                factors.set(index + 1, Math.min(factors.get(index + 1), halfInitialDist / bMovZ));
            }
        }
    }

    /**
     * Pre-movement module that deletes a time component from any node movement.
     * This module can be used for simulating a time-sliced based dynamic graph
     * drawing.
     */
    public static class ForbidTimeShitfing extends DyModularPreMovement {

        @Override
        protected void execute() {
            for (Node node : mirrorGraph().nodes()) {
                movements().set(node, movements().get(node).restr(2));
            }
        }
    }
}
