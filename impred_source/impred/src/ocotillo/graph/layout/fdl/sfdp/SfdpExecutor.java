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
package ocotillo.graph.layout.fdl.sfdp;

import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.serialization.dot.DotReader;
import ocotillo.serialization.dot.DotReader.DotReaderBuilder;
import ocotillo.serialization.dot.DotWriter;
import ocotillo.serialization.dot.DotWriter.DotWriterBuilder;
import ocotillo.serialization.dot.DotValueConverter.PositionConverter;
import ocotillo.serialization.dot.DotValueConverter.SizeConverter;
import ocotillo.serialization.dot.DotValueConverter.SizeDimensionConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Executor for GraphViz's Scalable Force Directed Placement algorithm.
 */
public class SfdpExecutor {

    private final String[] arguments;
    private final DotReader dotReader;
    private final DotWriter dotWriter;

    /**
     * Builds a sfdp executor.
     */
    public static class SfdpBuilder {

        private String[] arguments;
        private DotReader dotReader;
        private DotWriter dotWriter;

        /**
         * Constructs a SfdpBuilder.
         */
        public SfdpBuilder() {
            arguments = new String[]{};

            DotReaderBuilder readerBuilder = new DotReaderBuilder();
            readerBuilder.nodeAttributes
                    .convert("pos", StdAttribute.nodePosition, new PositionConverter())
                    .convert("width,height", StdAttribute.nodeSize, new SizeConverter());
            dotReader = readerBuilder.build();

            DotWriterBuilder writerBuilder = new DotWriterBuilder();
            writerBuilder.nodeAttributes
                    .convert(StdAttribute.nodePosition, "pos")
                    .convert(StdAttribute.nodeSize, "width", new SizeDimensionConverter(0))
                    .convert(StdAttribute.nodeSize, "height", new SizeDimensionConverter(1));
            dotWriter = writerBuilder.build();
        }

        /**
         * Specifies the arguments to be used for the algorithm.
         *
         * @param arguments the arguments.
         * @return the builder.
         */
        public SfdpBuilder withArguments(String[] arguments) {
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
        public SfdpBuilder withDotReader(DotReader dotReader) {
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
        public SfdpBuilder withDotWriter(DotWriter dotWriter) {
            this.dotWriter = dotWriter;
            return this;
        }

        /**
         * Builds a sfdp executor.
         *
         * @return the sfdp executor.
         */
        public SfdpExecutor build() {
            return new SfdpExecutor(arguments, dotReader, dotWriter);
        }
    }

    /**
     * Constructs a sfdp executor.
     *
     * @param arguments the arguments.
     * @param dotReader the dot reader.
     * @param dotWriter the dot writer.
     */
    private SfdpExecutor(String[] arguments, DotReader dotReader, DotWriter dotWriter) {
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
            executor.execute(CommandLine.parse("sfdp -V"));
        } catch (IOException ex) {
            throw new IllegalStateException("sfdp executable has not been found.");
        }
    }

    /**
     * Runs the algorithm to compute the new positions for the given graph.
     *
     * @param graph the graph.
     */
    public void execute(Graph graph) {
        List<String> dotInput = dotWriter.writeGraph(graph);
        List<String> dotOutput = run(dotInput);
        Graph generatedGraph = dotReader.parseFile(dotOutput);
        NodeAttribute<Coordinates> newPositions = generatedGraph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        for(Node node : graph.nodes()){
            Node correspondingNode = generatedGraph.getNode(node.id());
            positions.set(node, newPositions.get(correspondingNode));
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

        CommandLine cmdLine = new CommandLine("sfdp");
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
            throw new IllegalStateException("Error while executing sfdp.");
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
