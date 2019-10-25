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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing.FlexibleTimeTrajectories;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.samples.parsers.Commons;
import ocotillo.serialization.ParserTools;

/**
 * DynnoSlice execution with custom data.
 */
public class CustomRun {

    public static final double defaultDelta = 5.0;
    public static final double defaultTau = 1.0;
    public static final int defaultNumberOfIterations = 100;
    public static final String defaultOutput = "output.txt";

    /**
     * Executes the custom run.
     *
     * @param argv the parameters.
     */
    public static void main(String[] argv) {
        if (argv.length < 2) {
            showHelp();
        }

        File nodeDataSet = new File(argv[0]);
        if (!nodeDataSet.exists()) {
            System.err.println("The node data set file \"" + argv[0] + "\" does not exist. \n");
            showHelp();
        }
        List<String> nodeDataSetLines = ParserTools.readFileLines(nodeDataSet);

        File edgeDataSet = new File(argv[1]);
        if (!edgeDataSet.exists()) {
            System.err.println("The node edge set file \"" + argv[1] + "\" does not exist. \n");
            showHelp();
        }
        List<String> edgeDataSetLines = ParserTools.readFileLines(edgeDataSet);

        double delta = defaultDelta;
        if (argv.length >= 3) {
            try {
                double possibleDelta = Double.parseDouble(argv[2]);
                delta = possibleDelta > 0 ? possibleDelta : delta;
            } catch (Exception e) {
                System.err.println("Cannot parse delta correctly. \n");
                showHelp();
            }
        }

        double tau = defaultTau;
        if (argv.length >= 4) {
            try {
                double possibleTau = Double.parseDouble(argv[3]);
                delta = possibleTau >= 0 ? possibleTau : tau;
            } catch (Exception e) {
                System.err.println("Cannot parse tau correctly. \n");
                showHelp();
            }
        }

        String output = defaultOutput;
        if (argv.length >= 5) {
            System.out.println("Carramba");
            output = argv[4];
        }

        CustomRun customRun = new CustomRun(nodeDataSetLines, edgeDataSetLines,
                delta, tau, output);
        DyGraph dyGraph = customRun.createDynamicGraph();
        customRun.runDynnoSlice(dyGraph);
        customRun.saveOutput(dyGraph);
    }

    /**
     * Shows the command line help.
     */
    private static void showHelp() {
        System.out.println("Custom run needs the following parameters in this order:");
        System.out.println("nodeDataSetPath:       the path to the node dataset (csv file with nodeId,startTime,duration)");
        System.out.println("edgeDataSetPath:       the path to the edge dataset (csv file with sourceId,targetId,startTime,duration)");
        System.out.println("delta (optional):      the desired node distance on the plane.");
        System.out.println("tau (optional):        the conversion factor of time into space.");
        System.out.println("ouputFile (optional):  the output file (csv file with node,xCoord,yCoord,time).");
        System.out.println("");
        System.out.println("Node dataset example:");
        System.out.println("Alice,1,5");
        System.out.println("Bob,2,4.6");
        System.out.println("Carol,1.5,3");
        System.out.println("");
        System.out.println("Edge dataset example:");
        System.out.println("Alice,Bob,2.5,1");
        System.out.println("Bob,Carol,2.1,0.6");
        System.exit(0);
    }

    private final List<NodeAppearance> nodeDataSet;
    private final List<EdgeAppearance> edgeDataSet;
    private final double delta;
    private final double tau;
    private final String output;

    /**
     * Custom run constructor.
     *
     * @param nodeDataSetLines the lines of the node data set.
     * @param edgeDataSetLines the lines of the edge data set.
     * @param delta the delta parameter.
     * @param tau the tau parameter.
     * @param output the path of the output file.
     */
    public CustomRun(List<String> nodeDataSetLines, List<String> edgeDataSetLines,
            double delta, double tau, String output) {
        this.nodeDataSet = NodeAppearance.parseDataSet(nodeDataSetLines);
        this.edgeDataSet = EdgeAppearance.parseDataSet(edgeDataSetLines);
        this.delta = delta;
        this.tau = tau;
        this.output = output;
        checkNodeAppearanceCorrectness(nodeDataSet);
        checkEdgeAppearanceCorrectness(edgeDataSet);
    }

    /**
     * Checks if the node appearances are in correct order (no earlier
     * appearance of a node is later in the list) and there are no overlaps
     * between any two appearances of the same node.
     *
     * @param nodeDataSet the node data set.
     */
    public static void checkNodeAppearanceCorrectness(List<NodeAppearance> nodeDataSet) {
        for (int i = 0; i < nodeDataSet.size(); i++) {
            for (int j = i + 1; j < nodeDataSet.size(); j++) {
                NodeAppearance first = nodeDataSet.get(i);
                NodeAppearance second = nodeDataSet.get(j);
                if (first.id.equals(second.id)) {
                    if (first.startTime >= second.startTime) {
                        String error = "The appearances of node " + first.id
                                + " at time " + first.startTime + " and "
                                + second.startTime + " are not in the correct order.";
                        throw new RuntimeException(error);
                    } else if (first.startTime + first.duration >= second.startTime) {
                        String error = "The appearances of node " + first.id
                                + " at time " + first.startTime + " and duration "
                                + first.duration + " overlaps with appearance at time "
                                + second.startTime + ".";
                        throw new RuntimeException(error);
                    }
                }
            }
        }
    }

    /**
     * Checks if all edges are not loop (e.g. a-a) or do not have source and
     * target in alphabetical order (e.g. b-a). Also, checks that the edge
     * appearances are in correct order (no earlier appearance of an edge is
     * later in the list) and there are no overlaps between any two appearances
     * of the same edge.
     *
     * @param edgeDataSet the edge data set.
     */
    public static void checkEdgeAppearanceCorrectness(List<EdgeAppearance> edgeDataSet) {
        for (EdgeAppearance appearance : edgeDataSet) {
            if (appearance.sourceId.compareTo(appearance.targetId) >= 0) {
                String error = "An appearance with source node " + appearance.sourceId
                        + " and target node " + appearance.targetId + " either identifies"
                        + " a loop or does not have source and target in alphabetical order.";
                throw new RuntimeException(error);

            }
        }
        for (int i = 0; i < edgeDataSet.size(); i++) {
            for (int j = i + 1; j < edgeDataSet.size(); j++) {
                EdgeAppearance first = edgeDataSet.get(i);
                EdgeAppearance second = edgeDataSet.get(j);
                if (first.sourceId.equals(second.sourceId) && first.targetId.equals(second.targetId)) {
                    if (first.startTime >= second.startTime) {
                        String error = "The appearances of edge " + first.sourceId + " - "
                                + first.targetId + " at time " + first.startTime + " and "
                                + second.startTime + " are not in the correct order.";
                        throw new RuntimeException(error);
                    } else if (first.startTime + first.duration >= second.startTime) {
                        String error = "The appearances of edge " + first.sourceId + " - "
                                + first.targetId + " at time " + first.startTime + " and duration "
                                + first.duration + " overlaps with appearance at time "
                                + second.startTime + ".";
                        throw new RuntimeException(error);
                    }
                }
            }
        }
    }

    /**
     * Creates the dynamic dyGraph.
     *
     * @return the dynamic dyGraph.
     */
    public DyGraph createDynamicGraph() {
        DyGraph graph = new DyGraph();
        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);

        for (NodeAppearance appearance : nodeDataSet) {
            if (!graph.hasNode(appearance.id)) {
                Node node = graph.newNode(appearance.id);
                label.set(node, new Evolution<>(appearance.id));
                presence.set(node, new Evolution<>(false));
                position.set(node, new Evolution<>(new Coordinates(0, 0)));
            }
            Node node = graph.getNode(appearance.id);
            Interval presenceInterval = Interval.newClosed(appearance.startTime,
                    appearance.startTime + appearance.duration);
            presence.get(node).insert(new FunctionConst<>(presenceInterval, true));
        }

        for (EdgeAppearance appearance : edgeDataSet) {
            Node source = graph.getNode(appearance.sourceId);
            Node target = graph.getNode(appearance.targetId);
            if (graph.betweenEdge(source, target) != null) {
                Edge edge = graph.newEdge(source, target);
                edgePresence.set(edge, new Evolution<>(false));
            }
            Edge edge = graph.betweenEdge(source, target);
            Interval presenceInterval = Interval.newClosed(appearance.startTime,
                    appearance.startTime + appearance.duration);
            edgePresence.get(edge).insert(new FunctionConst<>(presenceInterval, true));
        }

        double graphDiameterEstimate = Math.sqrt(graph.nodeCount() * delta);
        Commons.scatterNodes(graph, graphDiameterEstimate);
        return graph;
    }

    /**
     * Runs the layout algorithm.
     *
     * @param graph the dynamic dyGraph.
     */
    public void runDynnoSlice(DyGraph graph) {
        DyModularFdl algorithm = new DyModularFdl.DyModularFdlBuilder(graph, tau)
                .withForce(new DyModularForce.TimeStraightning(delta))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.ConnectionAttraction(delta))
                .withForce(new DyModularForce.EdgeRepulsion(delta))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
                .withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
                .withPostProcessing(new FlexibleTimeTrajectories(delta * 1.5, delta * 2.0, Geom.e3D))
                .build();

        algorithm.iterate(defaultNumberOfIterations);
    }

    /**
     * Saves the output in a given file.
     *
     * @param graph the dyGraph to save.
     */
    public void saveOutput(DyGraph graph) {
        List<String> outputLines = new ArrayList<>();
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        for (Node node : graph.nodes()) {
            Evolution<Coordinates> evolution = position.get(node);
            double previousEntryTime = Double.NEGATIVE_INFINITY;
            for (Function<Coordinates> function : evolution) {
                Interval interval = function.interval();

                if (interval.leftBound() != previousEntryTime) {
                    Coordinates startPosition = function.leftValue();
                    outputLines.add(node + "," + startPosition.x() + ","
                            + startPosition.y() + "," + interval.leftBound());
                }
                previousEntryTime = interval.leftBound();

                if (interval.rightBound() != previousEntryTime) {
                    Coordinates endPosition = function.rightValue();
                    outputLines.add(node + "," + endPosition.x() + ","
                            + endPosition.y() + "," + interval.rightBound());
                }
                previousEntryTime = interval.rightBound();
            }
        }
        ParserTools.writeFileLines(outputLines, new File(output));
    }
}
