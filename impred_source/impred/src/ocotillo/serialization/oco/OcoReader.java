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

import java.util.LinkedList;
import java.util.List;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.GraphAttribute;
import ocotillo.graph.GraphWithAttributes;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.Rules;
import ocotillo.graph.StdAttribute;

/**
 * Reader for oco files.
 *
 * @param <T> the type of graph to create.
 * @param <U> the type of associated graph attribute.
 * @param <V> the type of associated node attribute.
 * @param <Z> the type of associated edge attribute
 */
public class OcoReader<T extends GraphWithAttributes<T, U, V, Z>, U extends GraphAttribute<?>, V extends NodeAttribute<?>, Z extends EdgeAttribute<?>> {

    private final OcoConverterSet converters;
    private final GraphGenerator<T, U, V, Z> generator;
    private final String graphHeader;

    /**
     * Constructs an oco reader.
     *
     * @param converters the converters.
     * @param generator the graph generator.
     * @param graphHeader the header used to identify this king of graph in oco.
     */
    protected OcoReader(OcoConverterSet converters, GraphGenerator<T, U, V, Z> generator, String graphHeader) {
        this.converters = converters;
        this.generator = generator;
        this.graphHeader = graphHeader;
    }

    /**
     * Class that generates the desired graph instance for this reader.
     *
     * @param <T> the type of graph to create.
     * @param <U> the type of associated graph attribute.
     * @param <V> the type of associated node attribute.
     * @param <Z> the type of associated edge attribute
     */
    protected static interface GraphGenerator<T extends GraphWithAttributes<T, U, V, Z>, U extends GraphAttribute<?>, V extends NodeAttribute<?>, Z extends EdgeAttribute<?>> {

        public T newGraph();
    }

    /**
     * Reads the given oco lines and generates a graph.
     *
     * @param lines the lines in oco format.
     * @return the generated graph.
     */
    protected T read(List<String> lines) {
        LinkedList<T> graphStack = new LinkedList<>();

        LinkedList<Block> blocks = getBlocks(lines);
        while (!blocks.isEmpty()) {
            parseGraphBlock(blocks, graphStack);
            parseNodeBlock(blocks, graphStack.peek());
            parseEdgeBlock(blocks, graphStack.peek());
        }

        return graphStack.peekLast();
    }

    /**
     * Groups the oco file lines into blocks.
     *
     * @param lines the lines.
     * @return the blocks
     */
    private static LinkedList<Block> getBlocks(List<String> lines) {
        LinkedList<Block> blocks = new LinkedList<>();
        LinkedList<Line> blockLines = null;
        int lineNumber = 1;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("#")) {
                if (blockLines != null) {
                    blocks.add(new Block(blockLines));
                }
                blockLines = new LinkedList<>();
            }

            if (!line.isEmpty() && blockLines != null) {
                blockLines.add(new Line(line, lineNumber));
            }

            lineNumber++;
        }

        blocks.add(new Block(blockLines));
        return blocks;
    }

    /**
     * Parses the next block in the list, assuming it is a graph one.
     *
     * @param blocks the blocks.
     * @param graphStack the current stack of graph levels.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void parseGraphBlock(LinkedList<Block> blocks, LinkedList<T> graphStack) {
        Block graphBlock = blocks.pop();
        int graphLevel = checkAndParseGraphHeader(graphBlock, graphStack, graphHeader);
        T graph = createGraphAtLevel(graphStack, graphLevel, generator);

        if (graphBlock.attributeNamesLn != -1 && graphBlock.attributeDefaultsLn == -1) {
            throw new MalformedFileException(graphBlock.attributeNamesLn, "it is not possible to define graph attributes without giving their value.");
        }

        for (int i = 1; i < graphBlock.attributeNames.length; i++) {
            String attributeName = graphBlock.getAttributeName(i);
            String attributeValue = graphBlock.getAttributeDefault(i);
            String attributeType = graphBlock.getAttributeType(i);
            OcoValueConverter<?> converter = getConverter(graphBlock, attributeName, attributeType);
            GraphAttribute attribute = graph.newLocalGraphAttribute(attributeName, converter.baseDefaultValue());

            if (attributeValue.isEmpty()) {
                throw new MalformedFileException(graphBlock.attributeDefaultsLn, "it is not possible to define graph attributes without giving their value.");
            }

            Object value = converter.ocoToGraphLib(attributeValue);
            attribute.setDefault(value);
        }
    }

    /**
     * Checks that the current block header correspond to a graph one and checks
     * that the graph level is consistent with previously defined graphs.
     *
     * @param graphBlock the current (supposedly) graph block.
     * @param graphStack the current graph stack.
     * @return the graph level deduced from the header.
     */
    private static <T> int checkAndParseGraphHeader(Block graphBlock, LinkedList<T> graphStack, String graphHeader) {
        if (!graphBlock.header.matches("#+" + graphHeader)) {
            throw new MalformedFileException(graphBlock.headerLn, "this should be a graph block, but it is not.");
        }
        int graphLevel = graphBlock.header.replace(graphHeader, "").length() - 1;
        if (graphLevel > graphStack.size()) {
            throw new MalformedFileException(graphBlock.headerLn, "cannot associate a parent graph to this block as this graph is too deep in the hierarchy.");
        }
        if (graphLevel == 0 && graphStack.size() > 0) {
            throw new MalformedFileException(graphBlock.headerLn, "cannot define two root graphs in the same file.");
        }
        return graphLevel;
    }

    /**
     * Creates a new graph or subgraph at the given level in the hierarchy.
     *
     * @param graphStack the graph stack.
     * @param graphLevel the desired graph level.
     * @return the created graph.
     */
    private static <T extends GraphWithAttributes<T, U, V, Z>, U extends GraphAttribute<?>, V extends NodeAttribute<?>, Z extends EdgeAttribute<?>>
            T createGraphAtLevel(LinkedList<T> graphStack, int graphLevel, GraphGenerator<T, U, V, Z> generator) {
        if (graphLevel == 0) {
            graphStack.push(generator.newGraph());
        } else {
            while (graphStack.size() != graphLevel) {
                graphStack.pop();
            }
            T subGraph = graphStack.peek().newSubGraph();
            graphStack.push(subGraph);
        }
        return graphStack.peek();
    }

    /**
     * Parses the next block in the list, assuming it is a nodes one.
     *
     * @param blocks the blocks.
     * @param graph the current graph.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void parseNodeBlock(LinkedList<Block> blocks, T graph) {
        Block block = blocks.peek();
        if (block != null && isNodeBlock(block)) {

            createNodes(block, graph);

            for (int j = 1; j < block.attributeNames.length; j++) {
                String attributeName = block.getAttributeName(j);
                String attributeType = block.getAttributeType(j);
                String attributeDefault = block.getAttributeDefault(j);

                OcoValueConverter<?> converter = getConverter(block, attributeName, attributeType);
                NodeAttribute attribute = graph.newLocalNodeAttribute(attributeName, converter.baseDefaultValue());

                Object defaultValue;
                if (!attributeDefault.isEmpty()) {
                    defaultValue = converter.ocoToGraphLib(attributeDefault);
                } else if (StdAttribute.isStandard(attributeName)) {
                    defaultValue = generator.newGraph().nodeAttribute(attributeName).getDefault();
                } else {
                    defaultValue = converter.defaultValue();
                }

                attribute.setDefault(defaultValue);

                for (int i = 0; i < block.values.length; i++) {
                    Node node = graph.rootGraph().getNode(block.getValue(i, 0));
                    if (!block.getValue(i, j).isEmpty()) {
                        attribute.set(node, converter.ocoToGraphLib(block.getValue(i, j)));
                    }
                }
            }

            blocks.pop();
        }
    }

    /**
     * Checks if it is a node block. The method also checks if successive blocks
     * are called before a nodes one.
     *
     * @param block the block.
     * @return true if it is a nodes block, false otherwise.
     */
    private boolean isNodeBlock(Block block) {
        if (block.header.matches("#edges")) {
            throw new MalformedFileException(block.headerLn, "it is not possible to define graph edges without defining its nodes.");
        }
        return block.header.matches("#nodes");
    }

    /**
     * Creates the graph nodes.
     *
     * @param block the nodes block.
     * @param graph the current graph.
     */
    private void createNodes(Block block, T graph) {
        boolean isRootGraph = (graph == graph.rootGraph());
        for (String[] nodeRows : block.values) {
            String nodeId = nodeRows[0];
            if (isRootGraph) {
                graph.newNode(nodeId);
            } else {
                graph.add(graph.rootGraph().getNode(nodeId));
            }
        }
    }

    /**
     * Parses the next block in the list, assuming it is a edges one.
     *
     * @param blocks the blocks.
     * @param graph the current graph.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void parseEdgeBlock(LinkedList<Block> blocks, T graph) {
        Block block = blocks.peek();
        if (block != null && isEdgeBlock(block)) {

            boolean isRootGraph = (graph == graph.rootGraph());
            createEdges(block, graph, isRootGraph);

            int startingIndex = isRootGraph ? 3 : 1;
            for (int j = startingIndex; j < block.attributeNames.length; j++) {
                String attributeName = block.getAttributeName(j);
                String attributeType = block.getAttributeType(j);
                String attributeDefault = block.getAttributeDefault(j);

                OcoValueConverter<?> converter = getConverter(block, attributeName, attributeType);
                EdgeAttribute attribute = graph.newLocalEdgeAttribute(attributeName, converter.baseDefaultValue());

                Object defaultValue;
                if (!attributeDefault.isEmpty()) {
                    defaultValue = converter.ocoToGraphLib(attributeDefault);
                } else if (StdAttribute.isStandard(attributeName)) {
                    defaultValue = generator.newGraph().edgeAttribute(attributeName).getDefault();
                } else {
                    defaultValue = converter.defaultValue();
                }

                attribute.setDefault(defaultValue);

                for (int i = 0; i < block.values.length; i++) {
                    Edge edge = graph.rootGraph().getEdge(block.getValue(i, 0));
                    if (!block.getValue(i, j).isEmpty()) {
                        attribute.set(edge, converter.ocoToGraphLib(block.getValue(i, j)));
                    }
                }
            }
            blocks.pop();
        }
    }

    /**
     * Creates the graph edges.
     *
     * @param block the nodes block.
     * @param graph the current graph.
     */
    private void createEdges(Block block, T graph, boolean isRootGraph) {
        if (isRootGraph && block.values.length > 0 && block.values[0].length < 3) {
            throw new MalformedFileException(block.headerLn, "edges are not correctly defined.");
        }
        if (!isRootGraph && block.attributeNames.length >= 3
                && (block.getAttributeName(1).startsWith("@") || block.getAttributeName(2).startsWith("@"))) {
            throw new MalformedFileException(block.headerLn, "edges sources and targets can only be defined in the root graph.");
        }

        for (int i = 0; i < block.values.length; i++) {
            String edgeId = block.getValue(i, 0);
            if (isRootGraph) {
                Node source = graph.rootGraph().getNode(block.getValue(i, 1));
                Node target = graph.rootGraph().getNode(block.getValue(i, 2));
                graph.newEdge(edgeId, source, target);
            } else {
                graph.add(graph.rootGraph().getEdge(edgeId));
            }
        }
    }

    /**
     * Checks if it is a edge block.
     *
     * @param block the block.
     * @return true if it is a edges block, false otherwise.
     */
    private boolean isEdgeBlock(Block block) {
        return block.header.matches("#edges");
    }

    /**
     * Gets the converted for the given attribute descriptions.
     *
     * @param block the block.
     * @param attributeName the attribute name.
     * @param attributeType the attribute type.
     * @return the value converter.
     */
    private OcoValueConverter<?> getConverter(Block block, String attributeName, String attributeType) {
        OcoValueConverter<?> converter;
        if (StdAttribute.isStandard(attributeName)) {
            converter = converters.get(StdAttribute.get(attributeName).matchingClass);
            if (!attributeType.isEmpty() && !attributeType.equals(converter.typeName())) {
                throw new MalformedFileException(block.attributeTypesLn, "they type indicated do not correspond to the matching class of the standard attribute " + attributeName + ".");
            }
        } else {
            attributeType = attributeType.isEmpty() ? "String" : attributeType;
            converter = converters.get(attributeType);
        }

        if (converter == null) {
            int lineNumber = block.attributeTypesLn != -1 ? block.attributeTypesLn : block.attributeNamesLn;
            throw new MalformedFileException(lineNumber, "cannot find a converter for the type " + attributeType + ".");
        }

        return converter;
    }

    /**
     * An input oco file line.
     */
    protected static class Line {

        protected final String text;
        protected final int lineNumber;

        protected Line(String text, int lineNumber) {
            this.text = text;
            this.lineNumber = lineNumber;
        }
    }

    /**
     * A block of oco file lines. Blocks describe different aspects of a graph
     * (graph attributes, nodes and their attributes, edges and their
     * attributes), and are started by a # line called header. Each blocks
     * define attributes in similar ways, and contains (nodes and edges blocks)
     * or not (graph blocks) tables of values.
     */
    protected static class Block {

        protected String header = "";
        protected String[] attributeNames = new String[]{"@attribute"};
        protected String[] attributeTypes = new String[]{"@type"};
        protected String[] attributeDefaults = new String[]{"@default"};
        protected String[][] values;

        protected int headerLn = -1;
        protected int attributeNamesLn = -1;
        protected int attributeTypesLn = -1;
        protected int attributeDefaultsLn = -1;
        protected int[] valuesLn;

        /**
         * Construct a block by parsing its lines.
         *
         * @param lines the block lines.
         */
        protected Block(LinkedList<Line> lines) {
            Line headerLine = lines.pop();
            header = headerLine.text;
            headerLn = headerLine.lineNumber;
            parseAttributeDefinitions(lines);
            parseValues(lines);
            trimValues();
        }

        /**
         * Parses the attributes of a block.
         *
         * @param lines the currently unparsed lines.
         */
        private void parseAttributeDefinitions(LinkedList<Line> lines) {
            while (!lines.isEmpty() && lines.peek().text.startsWith("@")) {
                Line line = lines.pop();
                if (line.text.startsWith("@attribute")) {
                    attributeNames = line.text.split("\t");
                    attributeNamesLn = line.lineNumber;
                    checkAttributeNames();
                } else if (line.text.startsWith("@type")) {
                    attributeTypes = line.text.split("\t");
                    attributeTypesLn = line.lineNumber;
                } else if (line.text.startsWith("@default")) {
                    attributeDefaults = line.text.split("\t");
                    attributeDefaultsLn = line.lineNumber;
                } else {
                    throw new MalformedFileException(line.lineNumber, "attribute line not recognizable.");
                }
            }
        }

        /**
         * Checks the names of the defined attributes.
         */
        private void checkAttributeNames() {
            for (String attributeName : attributeNames) {
                if (attributeName.isEmpty()) {
                    throw new MalformedFileException(attributeNamesLn, "attribute names cannot be empty.");
                }
            }
        }

        /**
         * Parses the values of the block.
         *
         * @param lines the currently unparsed lines.
         */
        private void parseValues(LinkedList<Line> lines) {
            values = new String[lines.size()][Math.max(attributeNames.length, 3)];
            valuesLn = new int[lines.size()];
            int i = 0;
            while (!lines.isEmpty()) {
                Line line = lines.pop();
                String[] tokens = line.text.split("\t");
                System.arraycopy(tokens, 0, values[i], 0, tokens.length);
                valuesLn[i] = line.lineNumber;
                try {
                    Rules.checkId(tokens[0]);
                } catch (IllegalArgumentException e) {
                    throw new MalformedFileException(line.lineNumber, "the element does not have a valid id.");
                }
                i++;
            }
        }

        /**
         * Trims all values to eliminate leading and trailing spaces.
         */
        private void trimValues() {
            for (int i = 0; i < attributeNames.length; i++) {
                attributeNames[i] = attributeNames[i].trim();
            }
            for (int i = 0; i < attributeTypes.length; i++) {
                attributeTypes[i] = attributeTypes[i].trim();
            }
            for (int i = 0; i < attributeDefaults.length; i++) {
                attributeDefaults[i] = attributeDefaults[i].trim();
            }
            for (String[] valuesRow : values) {
                for (int j = 0; j < valuesRow.length; j++) {
                    if (valuesRow[j] != null) {
                        valuesRow[j] = valuesRow[j].trim();
                    } else {
                        valuesRow[j] = "";
                    }
                }
            }
        }

        /**
         * Gets the attribute name of the column with given index.
         *
         * @param index the column index.
         * @return the attribute name.
         */
        protected String getAttributeName(int index) {
            return attributeNames[index];
        }

        /**
         * Gets the attribute type of the column with given index.
         *
         * @param index the column index.
         * @return the attribute type.
         */
        protected String getAttributeType(int index) {
            if (attributeTypes.length <= index) {
                return "";
            } else {
                return attributeTypes[index];
            }
        }

        /**
         * Gets the attribute default value of the column with given index.
         *
         * @param index the column index.
         * @return the attribute default value.
         */
        protected String getAttributeDefault(int index) {
            if (attributeDefaults.length <= index) {
                return "";
            } else {
                return attributeDefaults[index];
            }
        }

        /**
         * Gets the value with given row and column index.
         *
         * @param i the row index.
         * @param j the column index.
         * @return the attribute default value.
         */
        protected String getValue(int i, int j) {
            return values[i][j];
        }

    }

    /**
     * Reports a malformed oco file.
     */
    public static class MalformedFileException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private MalformedFileException(int lineNumber, String message) {
            super("Parsing error at line " + lineNumber + ": " + message);
        }
    }
}
