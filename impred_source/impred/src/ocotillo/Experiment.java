/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ocotillo;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.DyNodeAttribute;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.dygraph.extra.DyClustering;
import ocotillo.dygraph.extra.DyGraphDiscretiser;
import ocotillo.dygraph.extra.DyGraphMetric;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.extra.StcGraphMetric;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing;
import ocotillo.dygraph.layout.fdl.modular.DyModularPreMovement;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;
import ocotillo.graph.Edge;
import ocotillo.graph.EdgeAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.StdAttribute.ControlPoints;
import ocotillo.graph.extra.GraphMetric;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.graph.layout.fdl.modular.ModularMetric;
import ocotillo.graph.layout.fdl.modular.ModularPostProcessing;
import ocotillo.graph.layout.fdl.modular.ModularStatistics;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.samples.parsers.Commons;
import ocotillo.samples.parsers.DialogSequences;
import ocotillo.samples.parsers.InfoVisCitations;
import ocotillo.samples.parsers.NewcombFraternity;
import ocotillo.samples.parsers.RugbyTweets;
import ocotillo.samples.parsers.VanDeBunt;
import ocotillo.serialization.ParserTools;
import ocotillo.various.ColorCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Experiment on dynamic graph drawing.
 */
public abstract class Experiment {

    protected final String name;
    protected final String directory;
    protected final Commons.DyDataSet dataset;
    protected final double delta;

    /**
     * Builds the experiment.
     *
     * @param name the name of the experiment.
     * @param directory the dataset directory.
     * @param dataset the dataset to use.
     * @param delta the default edge length.
     */
    public Experiment(String name, String directory, Commons.DyDataSet dataset, double delta) {
        this.name = name;
        this.directory = directory;
        this.dataset = dataset;
        this.delta = delta;
    }

    /**
     * Runs the layout algorithm treating the graph in continuous time.
     *
     * @param k the number of clusters. Negative for no clustering.
     */
    public void runContinuous(int k) {
        DyModularFdl algorithm = getContinuousLayoutAlgorithm(dataset.dygraph, new ModularPostProcessing.DisplayCurrentIteration());

        algorithm.showMirrorGraph();
        ModularStatistics stats = algorithm.iterate(100);
        stats.saveCsv(new File("build/" + name + "_Continuous.csv"));
        System.out.println("Total running time: " + stats.getTotalRunnningTime().getSeconds());

        if (k > 1) {
            DyClustering clustering = new DyClustering.Stc.KMeans3D(
                    dataset.dygraph, dataset.suggestedTimeFactor, delta / 3.0, k,
                    ColorCollection.cbQualitativePastel);
            clustering.colorGraph();
        }

        DyQuickView view = new DyQuickView(dataset.dygraph, dataset.suggestedInterval.leftBound());
        view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
        view.showNewWindow();
    }

    /**
     * Runs the layout algorithm treating the graph in discrete time.
     *
     * @param k the number of clusters. Negative for no clustering.
     */
    public void runDiscrete(int k) {
        DyGraph discreteGraph = discretise();
        DyModularFdl algorithm = getDiscreteLayoutAlgorithm(discreteGraph, new ModularPostProcessing.DisplayCurrentIteration());

        algorithm.showMirrorGraph();
        ModularStatistics stats = algorithm.iterate(100);
        stats.saveCsv(new File("build/" + name + "_Discrete.csv"));
        System.out.println("Total running time: " + stats.getTotalRunnningTime().getSeconds());

        if (k > 1) {
            DyClustering clustering = new DyClustering.Stc.KMeans3D(
                    dataset.dygraph, dataset.suggestedTimeFactor, delta / 3.0, k,
                    ColorCollection.cbQualitativePastel);
            clustering.colorGraph();
        }

        DyQuickView view = new DyQuickView(discreteGraph, dataset.suggestedInterval.leftBound());
        view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
        view.showNewWindow();
    }

    /**
     * Builds the layout algorithm for the given dynamic graph.
     *
     * @param dyGraph the dynamic graph.
     * @param postProcessing eventual post processing.
     * @return the graph drawing algorithm.
     */
    public DyModularFdl getContinuousLayoutAlgorithm(DyGraph dyGraph, ModularPostProcessing postProcessing) {
        DyModularFdl.DyModularFdlBuilder builder = new DyModularFdl.DyModularFdlBuilder(dyGraph, dataset.suggestedTimeFactor)
                .withForce(new DyModularForce.TimeStraightning(delta))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.ConnectionAttraction(delta))
                .withForce(new DyModularForce.EdgeRepulsion(delta))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
                .withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
                .withPostProcessing(new DyModularPostProcessing.FlexibleTimeTrajectories(delta * 1.5, delta * 2.0, Geom.e3D));

        if (postProcessing != null) {
            builder.withPostProcessing(postProcessing);
        }

        return builder.build();
    }

    /**
     * Builds the layout algorithm for the given dynamic graph.
     *
     * @param dyGraph the dynamic graph.
     * @param postProcessing eventual post processing.
     * @return the graph drawing algorithm.
     */
    public DyModularFdl getDiscreteLayoutAlgorithm(DyGraph dyGraph, ModularPostProcessing postProcessing) {
        DyModularFdl.DyModularFdlBuilder builder = new DyModularFdl.DyModularFdlBuilder(dyGraph, dataset.suggestedTimeFactor)
                .withForce(new DyModularForce.TimeStraightning(delta))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.ConnectionAttraction(delta))
                .withForce(new DyModularForce.EdgeRepulsion(delta))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(2 * delta))
                .withConstraint(new ModularConstraint.MovementAcceleration(2 * delta, Geom.e3D))
                .withPreMovmement(new DyModularPreMovement.ForbidTimeShitfing());

        if (postProcessing != null) {
            builder.withPostProcessing(postProcessing);
        }

        return builder.build();
    }

    /**
     * Compute the metrics for the current experiment.
     *
     * @param visoneTime the running time recorded for visone.
     * @return the lines of the metric table.
     */
    public List<String> computeMetrics(String visoneTime) {
        List<String> lines = new ArrayList<>();

        DyGraph visoneGraph = exportImportVisone(directory);

        DyGraph discGraph = discretise();
        DyModularFdl discAlgorithm = getDiscreteLayoutAlgorithm(discGraph, new ModularPostProcessing.DisplayCurrentIteration());
//        discAlgorithm.showMirrorGraph();
        SpaceTimeCubeSynchroniser discSyncro = discAlgorithm.getSyncro();
        ModularStatistics discStats = discAlgorithm.iterate(100);
        double discTime = computeRunningTime(discStats);

        DyGraph contGraph = dataset.dygraph;
        DyModularFdl contAlgorithm = getContinuousLayoutAlgorithm(contGraph, new ModularPostProcessing.DisplayCurrentIteration());
//        contAlgorithm.showMirrorGraph();
        SpaceTimeCubeSynchroniser contSyncro = contAlgorithm.getSyncro();
        ModularStatistics contStats = contAlgorithm.iterate(100);
        double contTime = computeRunningTime(contStats);

        List<Double> snapTimes = readSnapTimes(discGraph);
        double visoneScaling = computeIdealScaling(visoneGraph, snapTimes);
        double discreteScaling = computeIdealScaling(discGraph, snapTimes);
        double continuousScaling = computeIdealScaling(contGraph, snapTimes);
        applyIdealScaling(visoneGraph, visoneScaling);
        applyIdealScaling(discSyncro, discreteScaling);
        applyIdealScaling(contSyncro, continuousScaling);

        DyGraph contVisone = getContinuousCopy();
        copyNodeLayoutFromTo(visoneGraph, contVisone);
        DyGraph contDiscrete = getContinuousCopy();
        copyNodeLayoutFromTo(discGraph, contDiscrete);
        DyGraph discContinuous = discretise();
        copyNodeLayoutFromTo(contGraph, discContinuous);

        lines.add(name + "," + "v" + "," + visoneTime + "," + 1 / visoneScaling + ","
                + computeOtherMetrics(visoneGraph, contVisone, snapTimes, null));
        lines.add("," + "d" + "," + discTime + "," + 1 / discreteScaling + ","
                + computeOtherMetrics(discGraph, contDiscrete, snapTimes, discSyncro));
        lines.add("," + "c" + "," + contTime + "," + 1 / continuousScaling + ","
                + computeOtherMetrics(discContinuous, contGraph, snapTimes, contSyncro));

        return lines;
    }

    /**
     * Computes the running times for the DyModularFDL algorithm.
     *
     * @param stats the algorithm statistics.
     * @return the running time for the layout computation.
     */
    public double computeRunningTime(ModularStatistics stats) {
        double time = 0;
        for (ModularMetric metric : stats.getMetrics()) {
            if (metric.metricName().equals("RunningTime")) {
                for (Object value : metric.values()) {
                    if (value != null) {
                        time += (Double) value;
                    }
                }
            }
        }
        return time;
    }

    /**
     * Computes the other metrics of interest.
     *
     * @param discGraph the graph with discrete edges.
     * @param contGraph the graph with continuous edges.
     * @param snapTimes the snapshot times.
     * @param synchro the synchroniser.
     * @return the metrics text.
     */
    public String computeOtherMetrics(DyGraph discGraph, DyGraph contGraph, List<Double> snapTimes, SpaceTimeCubeSynchroniser synchro) {
        SpaceTimeCubeSynchroniser synchroniser = synchro != null ? synchro
                : new SpaceTimeCubeSynchroniser.StcsBuilder(
                        discGraph, dataset.suggestedTimeFactor).build();

//        DyQuickView view = new DyQuickView(discGraph, dataset.suggestedInterval.leftBound());
//        view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
//        view.showNewWindow();
//
//        DyQuickView view2 = new DyQuickView(contGraph, dataset.suggestedInterval.leftBound());
//        view2.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(30)));
//        view2.showNewWindow();
//
        int slicesForOff = snapTimes.size() + (snapTimes.size() - 1) * 10;
        Interval interval = Interval.newClosed(snapTimes.get(0), snapTimes.get(snapTimes.size() - 1));

        DyGraphMetric<Double> stressOn = new DyGraphMetric.AverageSnapshotMetricCalculation(
                new GraphMetric.StressMetric.Builder().withScaling(delta).build(), interval, snapTimes.size());
        DyGraphMetric<Double> stressOff = new DyGraphMetric.AverageSnapshotMetricCalculation(
                new GraphMetric.StressMetric.Builder().withScaling(delta).build(), interval, slicesForOff);
        StcGraphMetric<Double> nodeMovement = new StcGraphMetric.AverageNodeMovement2D();
        StcGraphMetric<Integer> crowding = new StcGraphMetric.Crowding(dataset.suggestedInterval, 600);

        return stressOn.computeMetric(discGraph) + "," + stressOff.computeMetric(discGraph) + ","
                + stressOn.computeMetric(contGraph) + "," + stressOff.computeMetric(contGraph) + ","
                + nodeMovement.computeMetric(synchroniser) + "," + crowding.computeMetric(synchroniser);
    }

    public double computeIdealScaling(DyGraph graph, List<Double> snapTimes) {
        Interval interval = Interval.newClosed(snapTimes.get(0), snapTimes.get(snapTimes.size() - 1));

        double bestScaling = 0;
        double bestStress = Double.POSITIVE_INFINITY;

        for (int i = -20; i <= 20; i++) {

            double scaling = Math.pow(1.1, i);
            DyGraphMetric<Double> stressMetric = new DyGraphMetric.AverageSnapshotMetricCalculation(
                    new GraphMetric.StressMetric.Builder().withScaling(delta * scaling).build(),
                    interval, snapTimes.size());
            double stress = stressMetric.computeMetric(graph);

            if (stress < bestStress) {
                bestStress = stress;
                bestScaling = scaling;
            }
        }
        System.out.println("Best scaling: " + bestScaling);
        return bestScaling;
    }

    public void applyIdealScaling(DyGraph graph, double idealScaling) {
        DyNodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        for (Node node : graph.nodes()) {
            Evolution<Coordinates> evolution = new Evolution<>(new Coordinates(0, 0));
            for (Function<Coordinates> function : positions.get(node)) {
                if (function instanceof FunctionConst) {
                    evolution.insert(new FunctionConst<>(function.interval(),
                            function.leftValue().divide(idealScaling)));
                } else if (function instanceof FunctionRect) {
                    evolution.insert(new FunctionRect.Coordinates(function.interval(),
                            function.leftValue().divide(idealScaling),
                            function.rightValue().divide(idealScaling),
                            ((FunctionRect<Coordinates>) function).interpolation()));
                }
            }
            positions.set(node, evolution);
        }
    }

    public void applyIdealScaling(SpaceTimeCubeSynchroniser synchro, double idealScaling) {
        Graph mirror = synchro.mirrorGraph();
        NodeAttribute<Coordinates> positions = mirror.nodeAttribute(StdAttribute.nodePosition);
        for (Node node : mirror.nodes()) {
            Coordinates position = positions.get(node);
            position.setX(position.x() / idealScaling);
            position.setY(position.y() / idealScaling);
        }
        EdgeAttribute<ControlPoints> bends = mirror.edgeAttribute(StdAttribute.edgePoints);
        for (Edge edge : mirror.edges()) {
            for (Coordinates position : bends.get(edge)) {
                position.setX(position.x() / idealScaling);
                position.setY(position.y() / idealScaling);
            }
        }
        synchro.updateOriginal();
    }

    /**
     * Handles the export and import of the visone data.
     *
     * @param directory the visone directory.
     * @return the discrete graph with the layout computed by visone.
     */
    public DyGraph exportImportVisone(String directory) {
        DyGraph discreteGraph = discretise();
        exportVisone(directory, discreteGraph);
        importVisone(directory, discreteGraph);
        return discreteGraph;
    }

    /**
     * Exports the graphs in a format that can be processed by visone.
     *
     * @param directory the visone directory.
     * @param discreteGraph the original discrete graph.
     */
    public void exportVisone(String directory, DyGraph discreteGraph) {
        NodeMap map = new NodeMap(discreteGraph);

        int sliceNumber = 0;
        for (Double time : readSnapTimes(discreteGraph)) {
            List<String> lines = new ArrayList<>();

            Graph snapshot = discreteGraph.snapshotAt(time);
            for (String a : map) {
                String line = "";
                Node aNode = snapshot.hasNode(a) ? snapshot.getNode(a) : null;
                for (String b : map) {
                    Node bNode = snapshot.hasNode(b) ? snapshot.getNode(b) : null;
                    if (aNode == null || bNode == null || aNode == bNode
                            || snapshot.betweenEdge(aNode, bNode) == null) {
                        line += " 0";
                    } else {
                        line += " 1";
                    }
                }
                lines.add(line);
            }

            File dir = new File(directory + "visoneIn/");
            try {
                dir.mkdir();
            } catch (SecurityException se) {
                throw new IllegalStateException("Cannot create the directory.");
            }
            File file = new File(directory + "visoneIn/" + name + String.format("%03d", sliceNumber) + ".csv");
            ParserTools.writeFileLines(lines, file);
            sliceNumber++;
        }
    }

    /**
     * Imports the layout computed by visone and applies it to the given graph.
     *
     * @param directory the visone directory.
     * @param discreteGraph the graph that will receive the visone layout.
     */
    public void importVisone(String directory, DyGraph discreteGraph) {
        NodeMap map = new NodeMap(discreteGraph);
        List<Double> snapTimes = readSnapTimes(discreteGraph);
        List<Map<Node, Coordinates>> nodePositions = readNodePositions(directory, discreteGraph, map);

        DyNodeAttribute<Coordinates> positions = discreteGraph.nodeAttribute(StdAttribute.nodePosition);
        for (Node node : discreteGraph.nodes()) {
            Evolution<Coordinates> evolution = new Evolution<>(new Coordinates(0, 0));
            evolution.insert(new FunctionConst<>(
                    Interval.newRightClosed(Double.NEGATIVE_INFINITY, snapTimes.get(0)),
                    nodePositions.get(0).get(node)));
            for (int i = 0; i < snapTimes.size() - 1; i++) {
                evolution.insert(new FunctionRect.Coordinates(
                        Interval.newRightClosed(snapTimes.get(i), snapTimes.get(i + 1)),
                        nodePositions.get(i).get(node),
                        nodePositions.get(i + 1).get(node),
                        Interpolation.Std.linear));
            }
            evolution.insert(new FunctionConst<>(
                    Interval.newOpen(snapTimes.get(snapTimes.size() - 1), Double.POSITIVE_INFINITY),
                    nodePositions.get(snapTimes.size() - 1).get(node)));
            positions.set(node, evolution);
        }
    }

    /**
     * Reads the suggested snapshot times.
     *
     * @param discreteGraph the dynamic graph.
     * @return the list of snapshot times.
     */
    private List<Double> readSnapTimes(DyGraph discreteGraph) {
        String snapString = discreteGraph.<String>graphAttribute("SnapTimes").get().getDefaultValue();
        List<Double> snapTimes = new ArrayList<>();
        for (String token : snapString.split(",")) {
            snapTimes.add(Double.parseDouble(token));
        }
        return snapTimes;
    }

    private List<Map<Node, Coordinates>> readNodePositions(String directory, DyGraph discreteGraph, NodeMap map) {
        List<Map<Node, Coordinates>> nodePositions = new ArrayList<>();

        for (File fileEntry : new File(directory + "/visoneAfter").listFiles()) {

            int sliceNumber = Integer.parseInt(fileEntry.getName().replaceAll("[^\\d]", ""));
            while (nodePositions.size() <= sliceNumber) {
                nodePositions.add(new HashMap<>());
            }

            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fileEntry);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("node");

                for (int i = 0; i < nList.getLength(); i++) {
                    org.w3c.dom.Node nNode = nList.item(i);
                    Element eElement = (Element) nNode;
                    int nodeIndex = Integer.parseInt(eElement.getAttribute("id").replace("n", ""));
                    org.w3c.dom.Node nDataD2 = eElement.getElementsByTagName("data").item(2);
                    Element dataD2 = (Element) nDataD2;
                    assert (dataD2.getAttribute("key").equals("d2")) : "Format mismatch on data d2";
                    Element vShapeNode = (Element) dataD2.getElementsByTagName("visone:shapeNode").item(0);
                    Element yShapeNode = (Element) vShapeNode.getElementsByTagName("y:ShapeNode").item(0);
                    Element geom = (Element) yShapeNode.getElementsByTagName("y:Geometry").item(0);
                    double x = Double.parseDouble(geom.getAttribute("x")) / 40.0;
                    double y = -Double.parseDouble(geom.getAttribute("y")) / 40.0;
                    Coordinates position = new Coordinates(x, y);

                    Node node = discreteGraph.getNode(map.get(nodeIndex));
                    nodePositions.get(sliceNumber).put(node, position);
                }

            } catch (SAXException | IOException | ParserConfigurationException ex) {
                throw new IllegalStateException("Cannot read the xml file.");
            }
        }
        return nodePositions;
    }

    private void copyNodeLayoutFromTo(DyGraph source, DyGraph target) {
        DyNodeAttribute<Coordinates> sourcePositions = source.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Coordinates> targetPositions = target.nodeAttribute(StdAttribute.nodePosition);
        DyNodeAttribute<Boolean> sourcePresences = source.nodeAttribute(StdAttribute.dyPresence);
        DyNodeAttribute<Boolean> targetPresences = target.nodeAttribute(StdAttribute.dyPresence);

        for (Node node : source.nodes()) {

            Evolution<Coordinates> newEvolution = new Evolution<>(new Coordinates(0, 0));
            for (Function<Coordinates> function : sourcePositions.get(node)) {
                newEvolution.insert(function);
            }
            targetPositions.set(target.getNode(node.id()), newEvolution);

            Evolution<Boolean> newPresence = new Evolution<>(false);
            for (Function<Boolean> function : sourcePresences.get(node)) {
                newPresence.insert(function);
            }
            targetPresences.set(target.getNode(node.id()), newPresence);
        }
    }

    /**
     * Map that keep the correspondence between nodes in the ocotillo library
     * and the visone one.
     */
    private static class NodeMap implements Iterable<String> {

        private final List<String> list = new ArrayList<>();
        private final Map<String, Integer> map = new HashMap<>();

        public NodeMap(DyGraph graph) {
            for (Node node : graph.nodes()) {
                list.add(node.id());
            }
            Collections.sort(list);
            int i = 0;
            for (String string : list) {
                map.put(string, i);
                i++;
            }
        }

        public String get(int index) {
            return list.get(index);
        }

        public int get(String label) {
            return map.get(label);
        }

        @Override
        public Iterator<String> iterator() {
            return list.iterator();
        }
    }

    /**
     * Creates the discrete version of the dataset.
     *
     * @return the discrete dynamic graph.
     */
    public abstract DyGraph discretise();

    /**
     * Gets a copy of the continuous graph.
     *
     * @return a copy of the continuous graph.
     */
    public abstract DyGraph getContinuousCopy();

    /**
     * Experiment with the InfoVis dataset.
     */
    public static class InfoVis extends Experiment {

        public InfoVis() {
            super("InfoVis", "data/InfoVis_citations/", InfoVisCitations.parse(Commons.Mode.plain), 5);
        }

        @Override
        public DyGraph discretise() {
            List<Double> snapshotTimes = new ArrayList<>();
            for (int i = 1995; i <= 2015; i++) {
                snapshotTimes.add((double) i);
            }
            return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, 0.49);
        }

        @Override
        public DyGraph getContinuousCopy() {
            return InfoVisCitations.parse(Commons.Mode.plain).dygraph;
        }

    }

    /**
     * Experiment with the Rugby dataset.
     */
    public static class Rugby extends Experiment {

        public Rugby() {
            super("Rugby", "data/Rugby_tweets/", RugbyTweets.parse(Commons.Mode.keepAppearedNode), 5);
        }

        @Override
        public DyGraph discretise() {
            List<Double> snapshotTimes = new ArrayList<>();
            int slices = 20;
            double gap = dataset.suggestedInterval.width() / slices;
            for (int i = 0; i < slices; i++) {
                double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
                snapshotTimes.add(snapTime);
            }
            return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
        }

        @Override
        public DyGraph getContinuousCopy() {
            return RugbyTweets.parse(Commons.Mode.keepAppearedNode).dygraph;
        }
    }

    /**
     * Experiment with the Pride and Prejudice dataset.
     */
    public static class Pride extends Experiment {

        public Pride() {
            super("Pride", "data/DialogSequences/Pride_and_Prejudice/", DialogSequences.parse(Commons.Mode.keepAppearedNode), 5);
        }

        @Override
        public DyGraph discretise() {
            List<Double> snapshotTimes = new ArrayList<>();
            int slices = (int) dataset.suggestedInterval.width();
            double gap = dataset.suggestedInterval.width() / slices;
            for (int i = 0; i < slices; i++) {
                double snapTime = dataset.suggestedInterval.leftBound() + gap * (i + 0.5);
                snapshotTimes.add(snapTime);
            }
            return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, gap * 0.49);
        }

        @Override
        public DyGraph getContinuousCopy() {
            return DialogSequences.parse(Commons.Mode.keepAppearedNode).dygraph;
        }
    }

    /**
     * Experiment with the van de Bunt dataset.
     */
    public static class Bunt extends Experiment {

        public Bunt() {
            super("VanDeBunt", "data/van_De_Bunt/", VanDeBunt.parse(Commons.Mode.keepAppearedNode), 5);
        }

        @Override
        public DyGraph discretise() {
            List<Double> snapshotTimes = new ArrayList<>();
            for (int i = 0; i <= 6; i++) {
                snapshotTimes.add((double) i);
            }
            return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, 0.49);
        }

        @Override
        public DyGraph getContinuousCopy() {
            return VanDeBunt.parse(Commons.Mode.keepAppearedNode).dygraph;
        }
    }

    /**
     * Experiment with the Newcomb fraternity dataset.
     */
    public static class Newcomb extends Experiment {

        public Newcomb() {
            super("Newcomb", "data/Newcomb/", NewcombFraternity.parse(Commons.Mode.keepAppearedNode), 5);
        }

        @Override
        public DyGraph discretise() {
            List<Double> snapshotTimes = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                snapshotTimes.add((double) i);
            }
            return DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes, 0.49);
        }

        @Override
        public DyGraph getContinuousCopy() {
            return NewcombFraternity.parse(Commons.Mode.keepAppearedNode).dygraph;
        }
    }
}
