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
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.GeomE;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.MirrorEdge;
import ocotillo.graph.layout.fdl.modular.*;

/**
 * Post processing step for the DyModularFdl algorithm.
 */
public abstract class DyModularPostProcessing extends ModularPostProcessing {

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
     * Expands or contracts the dynamic time edges in the dynamic graph.
     */
    public static class FlexibleTimeTrajectories extends DyModularPostProcessing {

        protected final double contractDistance;
        protected final double expandDistance;
        protected GeomE geometry;
        public int refreshInterval = 1;
        public double shutDownTemperature = 0.2;
        private int refreshCounter = 0;

        /**
         * Constructs a flexible edges post processing.
         *
         * @param contractDistance the distance at which a flexible chain is
         * contracted.
         * @param expandDistance the distance at which a flexible segment is
         * expanded.
         */
        public FlexibleTimeTrajectories(double contractDistance, double expandDistance) {
            this.contractDistance = contractDistance;
            this.expandDistance = expandDistance;
        }

        /**
         * Constructs a flexible edges post processing.
         *
         * @param contractDistance the distance at which a flexible chain is
         * contracted.
         * @param expandDistance the distance at which a flexible segment is
         * expanded.
         * @param geometry the geometry to use.
         */
        public FlexibleTimeTrajectories(double contractDistance, double expandDistance, GeomE geometry) {
            this.contractDistance = contractDistance;
            this.expandDistance = expandDistance;
            this.geometry = geometry;
        }

        @Override
        protected void execute() {
            if (geometry == null) {
                geometry = dyModularFdl.geometry;
            }
            if (refreshCounter % refreshInterval == 0 && temperature() > shutDownTemperature) {
                expandFlexibleEdges();
                contractFlexibleEdges();
                refreshCounter++;
            }
        }

        /**
         * Expands the flexible segments whose length exceed the expand
         * distance.
         */
        private void expandFlexibleEdges() {
            for (Edge flexibleEdge : stcSynchronizer().mirrorGraph().edges()) {
                MirrorEdge mirrorEdge = synchronizer().getMirrorEdge(flexibleEdge);
                for (Edge segment : new ArrayList<>(mirrorEdge.segments())) {
                    Coordinates sourcePos = mirrorPositions().get(segment.source());
                    Coordinates targetPos = mirrorPositions().get(segment.target());
                    if (geometry.magnitude(targetPos.minus(sourcePos)) > expandDistance
                            && Math.abs(targetPos.z() - sourcePos.z()) > expandDistance / 2) {
                        synchronizer().addMirrorBend(mirrorEdge, segment);
                    }
                }
            }
        }

        /**
         * Contracts the flexible chains n1,e1,n2,e2,n3 where n1 and n3 are
         * closer than the contract distance.
         */
        private void contractFlexibleEdges() {
            for (Edge flexibleEdge : stcSynchronizer().mirrorGraph().edges()) {
                MirrorEdge mirrorEdge = synchronizer().getMirrorEdge(flexibleEdge);
                for (Node bend : new ArrayList<>(mirrorEdge.bends())) {
                    Node n1 = mirrorGraph().inEdges(bend).iterator().next().source();
                    Node n3 = mirrorGraph().outEdges(bend).iterator().next().target();
                    Coordinates n1Pos = mirrorPositions().get(n1);
                    Coordinates n2Pos = mirrorPositions().get(bend);
                    Coordinates n3Pos = mirrorPositions().get(n3);
                    double distance12 = geometry.magnitude(n1Pos.minus(n2Pos));
                    double distance23 = geometry.magnitude(n2Pos.minus(n3Pos));
                    double distance13 = geometry.magnitude(n1Pos.minus(n3Pos));
                    if (distance13 < contractDistance
                            || distance12 < contractDistance / 5
                            || distance23 < contractDistance / 5) {
                        synchronizer().removeMirrorBend(mirrorEdge, bend);
                    }
                }
            }
        }
    }
}
