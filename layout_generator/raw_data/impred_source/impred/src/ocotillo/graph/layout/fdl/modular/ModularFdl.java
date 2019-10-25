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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.GeomE;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser;
import ocotillo.graph.extra.BendExplicitGraphSynchroniser.BegsBuilder;
import ocotillo.graph.layout.locator.ElementLocator;
import ocotillo.graph.layout.locator.ElementLocator.EdgePolicy;
import ocotillo.graph.layout.locator.ElementLocator.NodePolicy;
import ocotillo.graph.layout.locator.intervaltree.IntervalTreeLocator.ItlBuilder;

/**
 * A force directed algorithm that supports multiple forces and constraints.
 */
public class ModularFdl {

    protected final Graph originalGraph;
    protected final Graph mirrorGraph;
    protected final NodeAttribute<Coordinates> mirrorPositions;
    protected final NodeAttribute<Coordinates> mirrorSizes;
    protected final NodeAttribute<Integer> mirrorLevels;
    protected final BendExplicitGraphSynchroniser synchronizer;
    protected final ElementLocator locator;
    protected final ModularThermostat thermostat;
    protected final GeomE geometry;

    protected final NodeAttribute<Coordinates> forces = new NodeAttribute<>(new Coordinates(0, 0));
    protected final NodeAttribute<Double> constraints = new NodeAttribute<>(Double.POSITIVE_INFINITY);
    protected final NodeAttribute<Coordinates> movements = new NodeAttribute<>(new Coordinates(0, 0));

    private final Collection<ModularForce> forceSystem;
    private final Collection<ModularConstraint> constraintSystem;
    private final Collection<ModularPreMovement> preMovementSteps;
    private final Collection<ModularPostProcessing> postProcessingSteps;
    private final Collection<ModularMetric> metrics;

    public static final double safetyMovementFactor = 0.9;

    /**
     * A builder for ModularFdl instances.
     */
    public static class ModularFdlBuilder {

        private final Graph graph;
        private ModularThermostat thermostat = new ModularThermostat.LinearCoolDown();
        private GeomE geometry = Geom.e2D;
        private final Collection<ModularForce> forces = new ArrayList<>();
        private final Collection<ModularConstraint> constraints = new ArrayList<>();
        private final Collection<ModularPreMovement> preMovements = new ArrayList<>();
        private final Collection<ModularPostProcessing> postProcessings = new ArrayList<>();
        private final Collection<ModularMetric> metrics = new ArrayList<>();

        /**
         * Constructs an ModularFdl builder.
         *
         * @param graph the graph to be used.
         */
        public ModularFdlBuilder(Graph graph) {
            this.graph = graph;
        }

        /**
         * Indicates the temperature controller to be used.
         *
         * @param thermostat the desired temperature controller.
         * @return the builder.
         */
        public ModularFdlBuilder withThermostat(ModularThermostat thermostat) {
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
        public ModularFdlBuilder withGeometry(GeomE geometry) {
            this.geometry = geometry;
            return this;
        }

        /**
         * Inserts the given force in the force system.
         *
         * @param force the force.
         * @return the builder.
         */
        public ModularFdlBuilder withForce(ModularForce force) {
            this.forces.add(force);
            return this;
        }

        /**
         * Inserts the given forces in the force system.
         *
         * @param forces the forces.
         * @return the builder.
         */
        public ModularFdlBuilder withForces(Collection<ModularForce> forces) {
            this.forces.addAll(forces);
            return this;
        }

        /**
         * Inserts the given constraint in the constraint system.
         *
         * @param constraint the constraint.
         * @return the builder.
         */
        public ModularFdlBuilder withConstraint(ModularConstraint constraint) {
            this.constraints.add(constraint);
            return this;
        }

        /**
         * Inserts the given constraints in the constraint system.
         *
         * @param constraints the constraints.
         * @return the builder.
         */
        public ModularFdlBuilder withConstraints(Collection<ModularConstraint> constraints) {
            this.constraints.addAll(constraints);
            return this;
        }

        /**
         * Inserts the given pre-movement step in the algorithm.
         *
         * @param preMovement the pre-movement step.
         * @return the builder.
         */
        public ModularFdlBuilder withPreMovmement(ModularPreMovement preMovement) {
            this.preMovements.add(preMovement);
            return this;
        }

        /**
         * Inserts the given pre-movement steps in the algorithm.
         *
         * @param preMovements the pre-movement steps.
         * @return the builder.
         */
        public ModularFdlBuilder withPreMovmements(Collection<ModularPreMovement> preMovements) {
            this.preMovements.addAll(preMovements);
            return this;
        }

        /**
         * Inserts the given post-processing step in the algorithm.
         *
         * @param postProcessing the post-processing step.
         * @return the builder.
         */
        public ModularFdlBuilder withPostProcessing(ModularPostProcessing postProcessing) {
            this.postProcessings.add(postProcessing);
            return this;
        }

        /**
         * Inserts the given post-processing steps in the algorithm.
         *
         * @param postProcessings the post-processing steps.
         * @return the builder.
         */
        public ModularFdlBuilder withPostProcessings(Collection<ModularPostProcessing> postProcessings) {
            this.postProcessings.addAll(postProcessings);
            return this;
        }

        /**
         * Inserts the given metric computation in the algorithm.
         *
         * @param metric the metric to compute.
         * @return the builder.
         */
        public ModularFdlBuilder withMetric(ModularMetric metric) {
            this.metrics.add(metric);
            return this;
        }

        /**
         * Inserts the given metric computations in the algorithm.
         *
         * @param metrics the metrics to compute.
         * @return the builder.
         */
        public ModularFdlBuilder withMetrics(Collection<ModularMetric> metrics) {
            this.metrics.addAll(metrics);
            return this;
        }

        /**
         * Builds the ModularFdl instance.
         *
         * @return the ModularFdl instance.
         */
        public ModularFdl build() {
            ModularFdl modularFdl = new ModularFdl(graph, thermostat, geometry, forces, constraints, preMovements, postProcessings, metrics);

            thermostat.attachTo(modularFdl);

            for (ModularForce force : forces) {
                force.attachTo(modularFdl);
            }
            for (ModularConstraint constraint : constraints) {
                constraint.attachTo(modularFdl);
            }
            for (ModularPreMovement preMovement : preMovements) {
                preMovement.attachTo(modularFdl);
            }
            for (ModularPostProcessing postProcessing : postProcessings) {
                postProcessing.attachTo(modularFdl);
            }
            for (ModularMetric metric : metrics) {
                metric.attachTo(modularFdl);
            }
            return modularFdl;
        }
    }

    /**
     * Constructs an ModularFdl instance.
     *
     * @param originalGraph the original graph.
     * @param thermostat the thermostat.
     * @param geometry the geometry.
     * @param forces the force system.
     * @param constraints the constraint system.
     * @param preMovements the pre-movement steps.
     * @param postProcessings the post-processing steps.
     * @param metrics the metrics.
     */
    private ModularFdl(Graph originalGraph, ModularThermostat thermostat, GeomE geometry,
            Collection<ModularForce> forces, Collection<ModularConstraint> constraints,
            Collection<ModularPreMovement> preMovements, Collection<ModularPostProcessing> postProcessings,
            Collection<ModularMetric> metrics) {
        if (!originalGraph.hasNodeAttribute(StdAttribute.nodeSize)) {
            originalGraph.nodeAttribute(StdAttribute.nodeSize);
        }
        
        //Node Levels
        if (!originalGraph.hasNodeAttribute(StdAttribute.nodeLevel)) {
            originalGraph.nodeAttribute(StdAttribute.nodeLevel);
        }

        this.synchronizer = new BegsBuilder(originalGraph)
                .preserveNodeAttribute(StdAttribute.nodeSize, true)
                .preserveNodeAttribute(StdAttribute.nodeLevel, true) //Node Levels
                .build();

        this.originalGraph = originalGraph;
        this.mirrorGraph = synchronizer.getMirrorGraph();
        this.mirrorPositions = synchronizer.getMirrorPositions();
        this.mirrorSizes = mirrorGraph.nodeAttribute(StdAttribute.nodeSize);
        
        //Node Levels
        this.mirrorLevels = mirrorGraph.nodeAttribute(StdAttribute.nodeLevel);

        this.locator = new ItlBuilder(mirrorGraph, NodePolicy.nodesAsGlyphs, EdgePolicy.edgesAsGlyphs)
                .withGeometry(geometry).disableAutoSync().build();
        this.thermostat = thermostat;
        this.geometry = geometry;
        this.forceSystem = forces;
        this.constraintSystem = constraints;
        this.preMovementSteps = preMovements;
        this.postProcessingSteps = postProcessings;
        this.metrics = metrics;
    }

    /**
     * Execute the main cycle for the given number of iterations.
     *
     * @param numberOfIterations the number of iterations.
     * @return the statistics for this execution.
     */
    public ModularStatistics iterate(int numberOfIterations) {
        ModularStatistics stats = new ModularStatistics(metrics);
        long totalStartTime = System.nanoTime();

        synchronizer.updateMirror();
        for (int i = 0; i < numberOfIterations; i++) {
        	System.out.println("\n####################### it: " + i);
            long iterationStartTime = System.nanoTime();

            mirrorPositions.startBulkNotification();
            forces.reset();
            constraints.reset(Double.POSITIVE_INFINITY);
            locator.rebuild();
            thermostat.updateTemperature(i, numberOfIterations);

            computeForces();
            computeConstraints();
            computeMovements();

            for (ModularPreMovement preMovement : preMovementSteps) {
                preMovement.execute();
            }

            moveNodes();

            for (ModularPostProcessing postProcessing : postProcessingSteps) {
                postProcessing.execute();
            }

            mirrorPositions.stopBulkNotification();
            synchronizer.updateOriginal();

            stats.runAtIterationEnd(Duration.ofNanos(System.nanoTime() - iterationStartTime));
        }

        stats.runAtComputationEnd(Duration.ofNanos(System.nanoTime() - totalStartTime));
        return stats;
    }

    /**
     * Computes the final force for each graph node.
     */
    private void computeForces() {
        for (ModularForce forceDefinition : forceSystem) {
            NodeAttribute<Coordinates> computedForces = forceDefinition.computeForces();
            for (Node node : mirrorGraph.nodes()) {
            	//System.out.print(forces.get(node));
            	
            	//System.out.print(computedForces.get(node));
                forces.set(node, computedForces.get(node).plus(forces.get(node)));
            }
        }
    }

    /**
     * Computes the final constraints for each graph node.
     */
    private void computeConstraints() {
        for (ModularConstraint constraintDefinition : constraintSystem) {
            NodeAttribute<Double> computedconstraint = constraintDefinition.computeConstraints();
            constraints.setDefault(Math.min(constraints.getDefault(), computedconstraint.getDefault()));
            for (Node node : mirrorGraph.nodes()) {
                double nodeMovement = Math.min(constraints.get(node), constraints.getDefault());
                nodeMovement = Math.min(nodeMovement, computedconstraint.get(node));
                constraints.set(node, nodeMovement);
            }
        }
    }

    /**
     * Computes the node movements.
     */
    private void computeMovements() {
        movements.reset();
        for (Node node : mirrorGraph.nodes()) {
            Coordinates force = forces.get(node);
            double constraint = constraints.get(node) * safetyMovementFactor;
            double magnitude = geometry.magnitude(force);
            if (!geometry.almostZero(magnitude) && !geometry.almostZero(constraint)) {
                Coordinates movement = new Coordinates(force);
                if (magnitude > constraint) {
                    movement.timesIP(constraint / magnitude);
                }
                movements.set(node, movement);
            }
        }
    }

    /**
     * Moves the graph nodes.
     */
    private void moveNodes() {
        for (Node node : mirrorGraph.nodes()) {
            mirrorPositions.set(node, movements.get(node).plus(mirrorPositions.get(node)));
        }
    }
//[653.9927779168787, 318.6489166875318, 0.0]
    /**
     * Terminates the ModularFdl instance.
     */
    public void close() {
        locator.close();
    }
}
