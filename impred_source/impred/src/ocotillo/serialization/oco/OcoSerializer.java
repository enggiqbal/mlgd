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
package ocotillo.serialization.oco;

import java.io.File;
import java.util.List;
import ocotillo.dygraph.DyEdgeAttribute;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyGraphAttribute;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.GraphAttribute;
import ocotillo.graph.NodeAttribute;
import ocotillo.serialization.ParserTools;

/**
 * Reads and writes oco files.
 */
public class OcoSerializer {

    private final OcoConverterSet staticConverters = OcoConverterSet.standardStatic();
    private final OcoConverterSet dynamicConverters = OcoConverterSet.standardDynamic();

    /**
     * Reads a file and generates a static graph.
     *
     * @param file the input dot file.
     * @return the generated static graph.
     */
    public Graph readStaticFile(File file) {
        List<String> lines = ParserTools.readFileLines(file);
        return readStatic(lines);
    }

    /**
     * Reads a file and generates a dynamic graph.
     *
     * @param file the input dot file.
     * @return the generated dynamic graph.
     */
    public DyGraph readDynamicFile(File file) {
        List<String> lines = ParserTools.readFileLines(file);
        return readDynamic(lines);
    }

    /**
     * Reads the given oco lines and generates a graph.
     *
     * @param lines the lines in oco format.
     * @return the generated graph.
     */
    public Graph readStatic(List<String> lines) {
        OcoReader<Graph, GraphAttribute<?>, NodeAttribute<?>, EdgeAttribute<?>> reader
                = new OcoReader<>(staticConverters, () -> new Graph(), "graph");
        return reader.read(lines);
    }

    /**
     * Reads the given oco lines and generates a graph.
     *
     * @param lines the lines in oco format.
     * @return the generated graph.
     */
    public DyGraph readDynamic(List<String> lines) {
        OcoReader<DyGraph, DyGraphAttribute<?>, DyNodeAttribute<?>, DyEdgeAttribute<?>> reader
                = new OcoReader<>(dynamicConverters, () -> new DyGraph(), "dygraph");
        return reader.read(lines);
    }

    /**
     * Writes a static graph in the given file.
     *
     * @param graph the graph to write.
     * @param file the destination file.
     */
    public void writeFile(Graph graph, File file) {
        ParserTools.writeFileLines(write(graph), file);
    }

    /**
     * Writes a dynamic graph in the given file.
     *
     * @param graph the graph to write.
     * @param file the destination file.
     */
    public void writeFile(DyGraph graph, File file) {
        ParserTools.writeFileLines(write(graph), file);
    }

    /**
     * Writes a static graph in the oco format.
     *
     * @param graph the input graph.
     * @return the graph description in oco format.
     */
    public List<String> write(Graph graph) {
        OcoWriter writer = new OcoWriter(staticConverters, "graph");
        return writer.write(graph);
    }

    /**
     * Writes a dynamic graph in the oco format.
     *
     * @param graph the input graph.
     * @return the graph description in oco format.
     */
    public List<String> write(DyGraph graph) {
        OcoWriter writer = new OcoWriter(dynamicConverters, "dygraph");
        return writer.write(graph);
    }

    /**
     * Gets the set of static converters.
     *
     * @return the static converter set.
     */
    public OcoConverterSet staticConverters() {
        return staticConverters;
    }

    /**
     * Gets the set of dynamic converters.
     *
     * @return the dynamic converter set.
     */
    public OcoConverterSet dynamicConverters() {
        return dynamicConverters;
    }
}
