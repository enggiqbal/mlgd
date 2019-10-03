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
package ocotillo.samples.parsers;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.EvoBuilder;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.FunctionConst;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.Node;
import ocotillo.graph.StdAttribute;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.samples.parsers.Commons.Mode;
import ocotillo.serialization.ParserTools;

/**
 * Parses a dataset of dialog sequences.
 */
public class NewcombFraternity {

    /**
     * Produces the dynamic dataset for this data.
     *
     * @param mode the desired mode.
     * @return the dynamic dataset.
     */
    public static DyDataSet parse(Mode mode) {
        File file = new File("data/Newcomb/");
        List<Integer[][]> dataset = parseRelations(file);
        return new DyDataSet(
                parseGraph(file, mode),
                5,
                Interval.newClosed(1, dataset.size() - 1));
    }

    /**
     * Parses the dialog sequence graph.
     *
     * @param inputDir the directory with the input files.
     * @param mode the desired mode.
     * @return the dynamic graph.
     */
    public static DyGraph parseGraph(File inputDir, Mode mode) {
        DyGraph graph = new DyGraph();
        DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Double> labelSize = graph.nodeAttribute(StdAttribute.labelScaling);
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);

        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);

        List<Integer[][]> dataset = parseRelations(inputDir);
        Map<Integer, Node> nodeMap = new HashMap<>();
        int numberOfStudents = dataset.get(1).length;
        for (int i = 0; i < numberOfStudents; i++) {
            Node node = graph.newNode("" + i);
            presence.set(node, EvoBuilder.defaultAt(false)
                    .withConst(Interval.newClosed(0, dataset.size() + 1), true)
                    .build());
            label.set(node, new Evolution<>(String.format("%02d", i)));
            labelSize.set(node, new Evolution<>(0.6));
            position.set(node, new Evolution<>(new Coordinates(0, 0)));
            color.set(node, new Evolution<>(new Color(141, 211, 199)));
            nodeMap.put(i, node);
        }

        for (int t = 1; t < dataset.size(); t++) {
            Integer[][] matrix = dataset.get(t);

            for (int i = 0; i < matrix.length; i++) {
                for (int j = i + 1; j < matrix[i].length; j++) {
                    if ((0 < matrix[i][j] && matrix[i][j] <= 3)
                            || (0 < matrix[j][i] && matrix[j][i] <= 3)) {
                        Node source = nodeMap.get(i);
                        Node target = nodeMap.get(j);
                        Edge edge = graph.betweenEdge(source, target);
                        if (edge == null) {
                            edge = graph.newEdge(source, target);
                            edgePresence.set(edge, new Evolution<>(false));
                            edgeColor.set(edge, new Evolution<>(Color.BLACK));
                        }

                        Interval relationPresence = Interval.newRightClosed(
                                t - 0.5, t + 0.5);

                        edgePresence.get(edge).insert(new FunctionConst<>(relationPresence, true));
                    }
                }
            }
        }

        Commons.scatterNodes(graph, 40);
        Commons.mergeAndColor(graph, 0.5, dataset.size() + 0.5, mode, new Color(141, 211, 199), Color.BLACK, 0.001);
        return graph;
    }

    /**
     * Parses the relations of the files in the input directory.
     *
     * @param inputDir the input directory.
     * @return the relation data set.
     */
    private static List<Integer[][]> parseRelations(File inputDir) {
        List<Integer[][]> dataset = new ArrayList<>();

        for (File fileEntry : inputDir.listFiles()) {
            if (fileEntry.getName().startsWith("newfrat") && fileEntry.getName().endsWith(".csv")) {
                int sliceNumber = Integer.parseInt(fileEntry.getName().replace("newfrat", "").replace(".csv", ""));
                List<String> fileLines = ParserTools.readFileLines(fileEntry);
                while (dataset.size() <= sliceNumber) {
                    dataset.add(null);
                }

                Integer[][] sliceRelations = new Integer[fileLines.size()][fileLines.size()];
                int lineNumber = 0;
                int columnNumber = 0;
                for (String line : fileLines) {
                    String[] tokens = line.trim().split("\\s+");
                    for (String token : tokens) {
                        int value = Integer.parseInt(token);
                        sliceRelations[lineNumber][columnNumber] = value;
                        columnNumber++;
                    }
                    assert (columnNumber == fileLines.size()) :
                            "The number of columns do not correspond to the number of lines.";
                    lineNumber++;
                    columnNumber = 0;
                }
                dataset.set(sliceNumber, sliceRelations);
            }
        }
        return dataset;
    }
}
