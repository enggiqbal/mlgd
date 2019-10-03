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
package ocotillo.graph.layout.other.gmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ocotillo.geometry.Polygon;
import ocotillo.graph.Graph;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.rendering.RenderingTools;
import ocotillo.serialization.dot.DotReader;
import ocotillo.serialization.dot.DotReader.DotReaderBuilder;
import ocotillo.serialization.dot.DotTools;
import ocotillo.serialization.dot.DotValueConverter.PositionConverter;
import ocotillo.serialization.dot.DotValueConverter.SizeConverter;
import ocotillo.serialization.dot.DotValueConverter.SizeDimensionConverter;
import ocotillo.serialization.dot.DotWriter;
import ocotillo.serialization.dot.DotWriter.DotWriterBuilder;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Executor for GraphViz's graph as maps algorithm.
 */
public class GmapExecutor {

    private final String[] arguments;
    private final DotReader dotReader;
    private final DotWriter dotWriter;

    /**
     * Builds a GMap executor.
     */
    public static class GmapBuilder {

        private String[] arguments;
        private DotReader dotReader;
        private DotWriter dotWriter;

        /**
         * Constructs a GMapBuilder.
         */
        public GmapBuilder() {
            arguments = new String[]{};

            DotReaderBuilder readerBuilder = new DotReaderBuilder();
            readerBuilder.nodeAttributes
                    .convert("pos", StdAttribute.nodePosition, new PositionConverter())
                    .convert("width,height", StdAttribute.nodeSize, new SizeConverter());
            readerBuilder.clusterBy("clusterLabel");
            dotReader = readerBuilder.build();

            DotWriterBuilder writerBuilder = new DotWriterBuilder();
            writerBuilder.nodeAttributes
                    .convert(StdAttribute.nodePosition, "pos", new PositionConverter())
                    .convert(StdAttribute.nodeSize, "width", new SizeDimensionConverter(0))
                    .convert(StdAttribute.nodeSize, "height", new SizeDimensionConverter(1));
            writerBuilder.clusterToNodeAttributes
                    .convert(StdAttribute.label, "clusterLabel")
                    .convert("clusterIdx", "cluster");
            dotWriter = writerBuilder.build();
        }

        /**
         * Specifies the arguments to be used for the algorithm.
         *
         * @param arguments the arguments.
         * @return the builder.
         */
        public GmapBuilder withArguments(String[] arguments) {
            this.arguments = arguments;
            return this;
        }

        /**
         * Indicates which dot reader to use to convert the algorithm output
         * into graph positions.
         *
         * @param dotReader the dot reader.
         * @return the builder.
         */
        public GmapBuilder withDotReader(DotReader dotReader) {
            this.dotReader = dotReader;
            return this;
        }

        /**
         * Indicates which dot reader to use to convert the current graph into
         * dot input.
         *
         * @param dotWriter the dot writer.
         * @return the builder.
         */
        public GmapBuilder withDotWriter(DotWriter dotWriter) {
            this.dotWriter = dotWriter;
            return this;
        }

        /**
         * Builds a sfdp executor.
         *
         * @return the sfdp executor.
         */
        public GmapExecutor build() {
            return new GmapExecutor(arguments, dotReader, dotWriter);
        }
    }

    /**
     * Constructs a sfdp executor.
     *
     * @param arguments the arguments.
     * @param dotReader the dot reader.
     * @param dotWriter the dot writer.
     */
    private GmapExecutor(String[] arguments, DotReader dotReader, DotWriter dotWriter) {
        checkExecutable();
        this.arguments = arguments;
        this.dotReader = dotReader;
        this.dotWriter = dotWriter;
    }

    /**
     * Checks if the executable exists.
     */
    private static void checkExecutable() {
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(new ByteArrayOutputStream()));
            executor.execute(CommandLine.parse("gvmap -?"));
        } catch (IOException ex) {
            throw new IllegalStateException("gvmap executable has not been found.");
        }
    }

    /**
     * Runs the algorithm to compute the new positions for the given graph.
     *
     * @param graph the graph.
     */
    public void execute(Graph graph) {
        int clusterIdx = 0;
        for (Graph cluster : graph.subGraphs()) {
            if (cluster.hasLocalGraphAttribute("clusterIdx")) {
                cluster.removeGraphAttribute("clusterIdx");
            }
            cluster.newLocalGraphAttribute("clusterIdx", clusterIdx);
            clusterIdx++;
        }

        List<String> dotInput = dotWriter.writeGraph(graph);
        List<String> dotOutput = run(dotInput);
        Graph generatedGraph = dotReader.parseFile(dotOutput);

        Map<String, Graph> clusterMap = new HashMap<>();
        for (Graph cluster : graph.subGraphs()) {
            String label = cluster.<String>graphAttribute(StdAttribute.label).get();
            clusterMap.put(label, cluster);
        }

        for (Graph cluster : generatedGraph.subGraphs()) {
            String label = cluster.<String>graphAttribute(StdAttribute.label).get();
            Graph originalCluster = clusterMap.get(label);

            List<Polygon> polygons = cluster.<List<Polygon>>graphAttribute(DotTools.polygonIdAttr).get();
            originalCluster.newLocalGraphAttribute(DotTools.polygonIdAttr, polygons);

            if (cluster.hasLocalGraphAttribute(StdAttribute.graphics)) {
                String newGraphics = cluster.<String>graphAttribute(StdAttribute.graphics).get();
                RenderingTools.Graphics.addLocalGraphicFirst(originalCluster, newGraphics);
            }
        }
    }

    /**
     * Runs the algorithm.
     *
     * @param dotInput the dot input.
     * @return the dot output.
     */
    private List<String> run(List<String> dotInput) {
        for (String argment : arguments) {
            assert (argment.startsWith("-") && !argment.startsWith("-o") && !argment.startsWith("-O")) : "Arguments that control the input/ouput streams cannot be used here.";
        }

        CommandLine cmdLine = new CommandLine("gvmap");
        cmdLine.addArguments(arguments);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(listToString(dotInput).getBytes());
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(outputStream, errorStream, inputStream));
            executor.execute(cmdLine);
        } catch (IOException ex) {
            System.err.println("ERROR: " + errorStream.toString() + "\n");
            System.out.println("OUTPUT: " + outputStream.toString() + "\n");
            throw new IllegalStateException("Error while executing gvmap.");
        }

        return stringToList(outputStream.toString());
    }

    /**
     * Converts a list of lines into a multi-line string.
     *
     * @param list the list of lines.
     * @return the multi-line string.
     */
    private static String listToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String string : list) {
            builder.append(string);
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Converts a multi-line string into a list of lines.
     *
     * @param string the multi-line string.
     * @return the list of lines.
     */
    private static List<String> stringToList(String string) {
        return Arrays.asList(string.split("\n"));
    }

}
