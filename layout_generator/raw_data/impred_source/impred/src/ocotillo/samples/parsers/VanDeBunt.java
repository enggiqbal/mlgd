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
public class VanDeBunt {

    private static class RelationDataset {

        List<Student> students = new ArrayList<>();
        List<RelationType[][]> relations = new ArrayList<>();
    }

    private static class Student {

        int label;
        boolean male;
        boolean smoker;
        int programme;
    }

    private enum RelationType {
        unknown(0),
        bestFriend(4),
        friend(3),
        aquintance(2),
        random(1),
        dislike(-1);

        int betterScale;

        RelationType(int betterScale) {
            this.betterScale = betterScale;
        }
    }

    /**
     * Produces the dynamic dataset for this data.
     *
     * @param mode the desired mode.
     * @return the dynamic dataset.
     */
    public static DyDataSet parse(Mode mode) {
        File file = new File("data/van_De_Bunt/");
        RelationDataset dataset = parseRelations(file);
        return new DyDataSet(
                parseGraph(file, mode),
                5,
                Interval.newClosed(0, dataset.relations.size() - 1.1));
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
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);
        DyNodeAttribute<Boolean> male = graph.newNodeAttribute("Male", false);
        DyNodeAttribute<Integer> programme = graph.newNodeAttribute("Programme", 0);
        DyNodeAttribute<Boolean> smoker = graph.newNodeAttribute("Smoker", false);

        DyEdgeAttribute<Boolean> edgePresence = graph.edgeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Color> edgeColor = graph.edgeAttribute(StdAttribute.color);
        DyEdgeAttribute<Double> edgeStrength = graph.newEdgeAttribute("Strength", 0.0);

        RelationDataset dataset = parseRelations(inputDir);
        Map<Student, Node> nodeMap = new HashMap<>();
        for (Student student : dataset.students) {
            Node node = graph.newNode("" + student.label);
            presence.set(node, EvoBuilder.defaultAt(false)
                    .withConst(Interval.newClosed(-1, dataset.relations.size()), true)
                    .build());
            label.set(node, new Evolution<>(String.format("%02d", student.label)));
            position.set(node, new Evolution<>(new Coordinates(0, 0)));
            color.set(node, new Evolution<>(new Color(141, 211, 199)));
            male.set(node, new Evolution<>(student.male));
            programme.set(node, new Evolution<>(student.programme));
            smoker.set(node, new Evolution<>(student.smoker));
            nodeMap.put(student, node);
        }

        for (int t = 0; t < dataset.relations.size(); t++) {
            RelationType[][] matrix = dataset.relations.get(t);

            for (int i = 0; i < matrix.length; i++) {
                for (int j = i + 1; j < matrix[i].length; j++) {
                    double strength = (matrix[i][j].betterScale + matrix[j][i].betterScale) / 2.0;
                    if ((matrix[i][j] == RelationType.bestFriend || matrix[i][j] == RelationType.friend)
                            && (matrix[j][i] == RelationType.bestFriend || matrix[j][i] == RelationType.friend)) {
                        Node source = nodeMap.get(dataset.students.get(i));
                        Node target = nodeMap.get(dataset.students.get(j));
                        Edge edge = graph.betweenEdge(source, target);
                        if (edge == null) {
                            edge = graph.newEdge(source, target);
                            edgePresence.set(edge, new Evolution<>(false));
                            edgeColor.set(edge, new Evolution<>(Color.BLACK));
                            edgeStrength.set(edge, new Evolution<>(strength));
                        }

                        Interval relationPresence = Interval.newRightClosed(
                                t - 0.5, t + 0.5);

                        edgePresence.get(edge).insert(new FunctionConst<>(relationPresence, true));
                    }
                }
            }
        }

        Commons.scatterNodes(graph, 40);
        Commons.mergeAndColor(graph, -0.5, dataset.relations.size() - 0.5, mode, new Color(141, 211, 199), Color.BLACK, 0.001);
        return graph;
    }

    /**
     * Parses the relations of the files in the input directory.
     *
     * @param inputDir the input directory.
     * @return the relation data set.
     */
    private static RelationDataset parseRelations(File inputDir) {
        RelationDataset dataset = new RelationDataset();
        File studentDataFile = new File(inputDir.getAbsolutePath() + "/VARS.DAT");
        List<String> fileLines = ParserTools.readFileLines(studentDataFile);
        for (String line : fileLines) {
            Student student = new Student();
            dataset.students.add(student);
            String[] tokens = line.trim().split("\\s+");
            student.label = dataset.students.size();
            student.male = tokens[0].equals("1");
            student.programme = Integer.parseInt(tokens[1]);
            student.smoker = tokens[2].equals("1");
        }

        for (File fileEntry : inputDir.listFiles()) {
            if (fileEntry.getName().startsWith("VRND32T") && fileEntry.getName().endsWith(".DAT")) {
                int sliceNumber = Integer.parseInt(fileEntry.getName().replace("VRND32T", "").replace(".DAT", ""));
                fileLines = ParserTools.readFileLines(fileEntry);
                while (dataset.relations.size() <= sliceNumber) {
                    dataset.relations.add(null);
                }

                RelationType[][] sliceRelations = new RelationType[dataset.students.size()][dataset.students.size()];
                int lineNumber = 0;
                int columnNumber = 0;
                for (String line : fileLines) {
                    String[] tokens = line.trim().split("\\s+");
                    for (String token : tokens) {
                        int value = Integer.parseInt(token);
                        int correctedValue = value >= RelationType.values().length ? 0 : value;
                        sliceRelations[lineNumber][columnNumber] = RelationType.values()[correctedValue];
                        columnNumber++;
                    }
                    assert (columnNumber == dataset.students.size()) :
                            "The number of columns do not correspond to the number of students.";
                    lineNumber++;
                    columnNumber = 0;
                }
                assert (lineNumber == dataset.students.size()) :
                        "The number of lines do not correspond to the number of students.";

                dataset.relations.set(sliceNumber, sliceRelations);
            }
        }
        return dataset;
    }
}
