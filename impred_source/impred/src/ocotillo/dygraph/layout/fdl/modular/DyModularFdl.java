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
import java.util.Collection;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser.StcsBuilder;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.GeomE;
import ocotillo.graph.Graph;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.*;
import ocotillo.graph.layout.fdl.modular.ModularFdl.ModularFdlBuilder;
import ocotillo.gui.quickview.QuickView;

/**
 * A force directed algorithm that supports multiple forces and constraints.
 */
public class DyModularFdl {

    protected final DyGraph originalGraph;
    protected final Graph mirrorGraph;
    protected final double timeFactor;

    protected final DyNodeAttribute<Boolean> dyNodePresence;
    protected final DyEdgeAttribute<Boolean> dyEdgePresence;
    protected final DyNodeAttribute<Coordinates> dyNodePositions;

    protected final NodeAttribute<Coordinates> mirrorPositions;
    protected final NodeAttribute<Coordinates> mirrorSizes;

    protected final SpaceTimeCubeSynchroniser synchronizer;
    protected final ModularFdl modularFdl;
    protected final GeomE geometry;

    /**
     * A builder for ModularFdl instances.
     */
    public static class DyModularFdlBuilder {

        private final DyGraph originalGraph;
        private final double timeFactor;
        private ModularThermostat thermostat = new ModularThermostat.LinearCoolDown();
        private GeomE geometry = Geom.e3D;
        private final Collection<ModularForce> forces = new ArrayList<>();
        private final Collection<ModularConstraint> constraints = new ArrayList<>();
        private final Collection<ModularPreMovement> preMovements = new ArrayList<>();
        private final Collection<ModularPostProcessing> postProcessings = new ArrayList<>();
        private final Collection<ModularMetric> metrics = new ArrayList<>();

        /**
         * Constructs an ModularFdl builder.
         *
         * @param graph the graph to be used.
         * @param timeFactor the conversion factor of time. This indicates how
         * many space units correspond to a time unit.
         */
        public DyModularFdlBuilder(DyGraph graph, double timeFactor) {
            this.originalGraph = graph;
            this.timeFactor = timeFactor;
        }

        /**
         * Indicates the temperature controller to be used.
         *
         * @param thermostat the desired temperature controller.
         * @return the builder.
         */
        public DyModularFdlBuilder withThermostat(ModularThermostat thermostat) {
            this.thermostat = thermostat;
            return this;
        }

        /**
         * Indicates the Euclidean geometry to use when computing and
         * constraining the movement magnitude.
         *
         * @param geometry the geometry.
         * @return the builder.
         */
        public DyModularFdlBuilder withGeometry(GeomE geometry) {
            this.geometry = geometry;
            return this;
        }

        /**
         * Inserts the given force in the force system.
         *
         * @param force the force.
         * @return the builder.
         */
        public DyModularFdlBuilder withForce(ModularForce force) {
            forces.add(force);
            return this;
        }

        /**
         * Inserts the given constraint in the constraint system.
         *
         * @param constraint the constraint.
         * @return the builder.
         */
        public DyModularFdlBuilder withConstraint(ModularConstraint constraint) {
            constraints.add(constraint);
            return this;
        }

        /**
         * Inserts the given pre-movement step in the algorithm.
         *
         * @param preMovement the pre-movement step.
         * @return the builder.
         */
        public DyModularFdlBuilder withPreMovmement(ModularPreMovement preMovement) {
            preMovements.add(preMovement);
            return this;
        }

        /**
         * Inserts the given post-processing step in the algorithm.
         *
         * @param postProcessing the post-processing step.
         * @return the builder.
         */
        public DyModularFdlBuilder withPostProcessing(ModularPostProcessing postProcessing) {
            postProcessings.add(postProcessing);
            return this;
        }

        /**
         * Inserts the given metric computation in the algorithm.
         *
         * @param metric the metric to compute.
         * @return the builder.
         */
        public DyModularFdlBuilder withMetric(ModularMetric metric) {
            metrics.add(metric);
            return this;
        }

        /**
         * Builds the ModularFdl instance.
         *
         * @return the ModularFdl instance.
         */
        public DyModularFdl build() {
            preMovements.add(new DyModularPreMovement.EnsureTimeCorrectness());

            DyModularFdl dyModularFdl = new DyModularFdl(originalGraph, timeFactor,
                    forces, constraints, preMovements, postProcessings, metrics,
                    thermostat, geometry);

            for (ModularForce force : forces) {
                if (force instanceof DyModularForce) {
                    ((DyModularForce) force).attachTo(dyModularFdl);
                }
            }
            for (ModularPreMovement preMovement : preMovements) {
                if (preMovement instanceof DyModularPreMovement) {
                    ((DyModularPreMovement) preMovement).attachTo(dyModularFdl);
                }
            }
            for (ModularPostProcessing postProcessing : postProcessings) {
                if (postProcessing instanceof DyModularPostProcessing) {
                    ((DyModularPostProcessing) postProcessing).attachTo(dyModularFdl);
                }
            }
            for (ModularMetric metric : metrics) {
                if (metric instanceof DyModularMetric) {
                    ((DyModularMetric) metric).attachTo(dyModularFdl);
                }
            }
            return dyModularFdl;
        }
    }

    /**
     * Constructs an ModularFdl instance.
     *
     * @param originalGraph the original graph.
     * @param timeFactor the conversion factor of time. This indicates how many
     * space units correspond to a time unit.
     * @param thermostat the thermostat.
     * @param geometry the geometry.
     * @param forces the force system.
     * @param constraints the constraint system.
     * @param metrics the metrics to compute.
     * @param preMovements the pre-movement steps.
     * @param postProcessings the post-processing steps.
     */
    private DyModularFdl(DyGraph originalGraph, double timeFactor,
            Collection<ModularForce> forces, Collection<ModularConstraint> constraints,
            Collection<ModularPreMovement> preMovements, Collection<ModularPostProcessing> postProcessings,
            Collection<ModularMetric> metrics,
            ModularThermostat thermostat, GeomE geometry) {

        this.synchronizer = new StcsBuilder(originalGraph, timeFactor).build();

        this.originalGraph = originalGraph;
        this.mirrorGraph = synchronizer.mirrorGraph();
        this.timeFactor = timeFactor;
        this.geometry = geometry;

        this.dyNodePresence = originalGraph.nodeAttribute(StdAttribute.dyPresence);
        this.dyEdgePresence = originalGraph.edgeAttribute(StdAttribute.dyPresence);
        this.dyNodePositions = originalGraph.nodeAttribute(StdAttribute.nodePosition);
        this.mirrorPositions = mirrorGraph.nodeAttribute(StdAttribute.nodePosition);
        this.mirrorSizes = mirrorGraph.nodeAttribute(StdAttribute.nodeSize);

        this.modularFdl = new ModularFdlBuilder(mirrorGraph)
                .withForces(forces)
                .withConstraints(constraints)
                .withPreMovmements(preMovements)
                .withPostProcessings(postProcessings)
                .withMetrics(metrics)
                .withThermostat(thermostat)
                .withGeometry(geometry)
                .build();
    }

    /**
     * Execute the main cycle for the given number of iterations.
     *
     * @param numberOfIterations the number of iterations.
     * @return the execution statistics.
     */
    public ModularStatistics iterate(int numberOfIterations) {
        ModularStatistics stats = modularFdl.iterate(numberOfIterations);
        synchronizer.updateOriginal();
        return stats;
    }

    /**
     * Shows the mirror graph.
     */
    public void showMirrorGraph() {
        QuickView.showNewWindow(mirrorGraph);
    }

    public SpaceTimeCubeSynchroniser getSyncro() {
        return synchronizer;
    }
}
