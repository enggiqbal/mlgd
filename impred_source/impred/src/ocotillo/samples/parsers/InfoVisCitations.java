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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
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
 * Parses the InfoVis collaboration data.
 */
public class InfoVisCitations {

    public static double fadingTime = 0.5;

    /**
     * An InfoVis paper.
     */
    public static class Paper {

        public final String id;
        public final String title;
        public final int year;
        public final Set<String> authors = new HashSet<>();
        public final Set<String> concepts = new HashSet<>();
        public final Set<String> keywords = new HashSet<>();
        public final Set<Paper> citations = new HashSet<>();
        private final Set<String> citationsId = new HashSet<>();

        /**
         * Builds a paper.
         *
         * @param id the paper id.
         * @param title the title.
         * @param year the year.
         */
        public Paper(String id, String title, int year) {
            this.id = id;
            this.title = title;
            this.year = year;
        }

        @Override
        public String toString() {
            return "[" + id + "]\n"
                    + title + "\n"
                    + print(authors) + "\n"
                    + print(concepts) + "\n"
                    + print(keywords) + "\n"
                    + printCit(citations);
        }

        /**
         * Prints a set of strings.
         *
         * @param set the set of strings.
         * @return the description.
         */
        private static String print(Set<String> set) {
            List<String> list = new ArrayList<>(set);
            Collections.sort(list);
            String result = "";
            for (String item : list) {
                result += item + ", ";
            }
            return result.substring(0, Math.max(0, result.length() - 2));
        }

        /**
         * Prints a set of citations.
         *
         * @param citations the set of citations.
         * @return the description.
         */
        private static String printCit(Set<Paper> citations) {
            Set<String> idSet = new HashSet<>();
            for (Paper paper : citations) {
                idSet.add(paper.id);
            }
            return print(idSet);
        }
    }

    /**
     * Produces the dynamic dataset for this data.
     *
     * @param mode the desired mode.
     * @return the dynamic dataset.
     */
    public static DyDataSet parse(Mode mode) {
        File file = new File("data/InfoVis_citations/data.txt");
        Map<String, Paper> papers = parsePapers(file);
        int firstYear = Integer.MAX_VALUE;
        int lastYear = Integer.MIN_VALUE;
        for (Paper paper : papers.values()) {
            firstYear = Math.min(firstYear, paper.year);
            lastYear = Math.max(lastYear, paper.year);
        }
        return new DyDataSet(
                parseGraph(file, mode),
                5,
                Interval.newClosed(firstYear, lastYear));
    }

    /**
     * Parses the files to retrieve the papers.
     *
     * @param file the file.
     * @return the list of papers.
     */
    public static Map<String, Paper> parsePapers(File file) {
        List<String> lines = ParserTools.readFileLines(file);
        List<String> paperLines = new ArrayList<>();
        Map<String, Paper> papers = new HashMap<>();
        boolean onHeader = true;
        for (String line : lines) {
            if (line.startsWith("article")) {
                if (!onHeader) {
                    Paper paper = parsePaper(paperLines);
                    papers.put(paper.id, paper);
                }
                paperLines.clear();
                onHeader = false;
            } else {
                paperLines.add(line);
            }
        }
        Paper paper = parsePaper(paperLines);
        papers.put(paper.id, paper);
        matchPapers(papers);
        return papers;
    }

    /**
     * Parses the lines of a paper.
     *
     * @param paperLines the paper lines.
     * @return the paper.
     */
    private static Paper parsePaper(List<String> paperLines) {
        String id = paperLines.get(0);
        if (!id.startsWith("infovis")) {
            throw new IllegalStateException("The paper id seems to be incorrect: " + id);
        }

        String yearEnding = id.substring(7, 9);
        String yearBeginning = yearEnding.startsWith("9") ? "19" : "20";
        int year = Integer.parseInt(yearBeginning + yearEnding);

        String title = paperLines.get(3);

        Paper paper = new Paper(id, title, year);

        int rowIndex = 4;
        for (; rowIndex < paperLines.size(); rowIndex++) {
            String line = paperLines.get(rowIndex);
            if (line.startsWith("concept: ")) {
                paper.concepts.add(line.replace("concept: ", "").trim());
            } else if (line.startsWith("keyword: ")) {
                paper.keywords.add(line.replace("keyword: ", "").trim());
            } else if (line.startsWith("author: ")) {
                paper.authors.add(line.replace("author: ", "").trim());
            }
            if (line.startsWith("citations")) {
                break;
            }
        }

        for (; rowIndex < paperLines.size(); rowIndex++) {
            String line = paperLines.get(rowIndex);
            if (line.startsWith("infovis")) {
                paper.citationsId.add(line);
            }
        }
        return paper;
    }

    /**
     * Assigns the cited papers.
     *
     * @param papers the map of papers.
     */
    private static void matchPapers(Map<String, Paper> papers) {
        for (Paper paper : papers.values()) {
            for (String citationId : paper.citationsId) {
                paper.citations.add(papers.get(citationId));
            }
        }
    }

    /**
     * Parses the collaboration graph.
     *
     * @param file the input file.
     * @return the collaboration graph.
     */
    public static DyGraph parseGraph(File file) {
        return parseGraph(file, Mode.plain);
    }

    /**
     * Parses the collaboration graph.
     *
     * @param file the input file.
     * @param mode the desired mode.
     * @return the collaboration graph.
     */
    public static DyGraph parseGraph(File file, Mode mode) {
        Map<String, Paper> papers = parsePapers(file);
        DyGraph graph = new DyGraph();
        int firstYear = Integer.MAX_VALUE;
        int lastYear = Integer.MIN_VALUE;
        for (Paper paper : papers.values()) {
            firstYear = Math.min(firstYear, paper.year);
            lastYear = Math.max(lastYear, paper.year);
            Set<String> coAuthors = new HashSet<>();
            for (String author : paper.authors) {
                setAuthorPresence(graph, author, paper.year);
                for (String coAuthor : coAuthors) {
                    setCollaborationPresence(graph, author, coAuthor, paper.year);
                }
                coAuthors.add(author);
            }
        }

        Commons.scatterNodes(graph, 200);
        Commons.mergeAndColor(graph, firstYear - 0.5, lastYear + 0.5, mode, new Color(141, 211, 199), Color.BLACK, fadingTime);
        return graph;
    }

    /**
     * Sets the author presence in the given year.
     *
     * @param graph the graph.
     * @param author the author.
     * @param year the year.
     */
    private static void setAuthorPresence(DyGraph graph, String author, int year) {
        DyNodeAttribute<Boolean> presence = graph.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<String> label = graph.nodeAttribute(StdAttribute.label);
        DyNodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Color> color = graph.nodeAttribute(StdAttribute.color);

        Node node;
        if (graph.hasNode(author)) {
            node = graph.getNode(author);
        } else {
            node = graph.newNode(author);
            presence.set(node, new Evolution<>(false));
            label.set(node, new Evolution<>(author));
            position.set(node, new Evolution<>(new Coordinates(0, 0)));
            color.set(node, new Evolution<>(new Color(141, 211, 199)));
        }

        Evolution<Boolean> nodePresence = presence.get(node);
        if (nodePresence.valueAt(year) == false) {
            nodePresence.insert(new FunctionConst<>(
                    Interval.newClosed(year - 0.5, year + 0.5), true));
        }
    }

    /**
     * Sets the collaboration presence in the given year.
     *
     * @param graph the graph.
     * @param author the author.
     * @param collaborator the collaborator.
     * @param year the year.
     */
    private static void setCollaborationPresence(DyGraph graph, String author, String collaborator, int year) {
        DyEdgeAttribute<Boolean> presence = graph.edgeAttribute(StdAttribute.dyPresence);
        DyEdgeAttribute<Color> color = graph.edgeAttribute(StdAttribute.color);

        Node source = graph.getNode(author);
        Node target = graph.getNode(collaborator);
        Edge edge = graph.betweenEdge(source, target);
        if (edge == null) {
            edge = graph.newEdge(source, target);
            presence.set(edge, new Evolution<>(false));
            color.set(edge, new Evolution<>(Color.BLACK));
        }

        Evolution<Boolean> edgePresence = presence.get(edge);
        if (edgePresence.valueAt(year) == false) {
            edgePresence.insert(new FunctionConst<>(
                    Interval.newClosed(year - 0.5, year + 0.5), true));
        }
    }
}
