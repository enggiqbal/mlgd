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
package ocotillo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.time.Duration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.extra.DyClustering;
import ocotillo.dygraph.extra.SpaceTimeCubeSynchroniser;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.graph.Graph;
import ocotillo.graph.layout.fdl.modular.ModularStatistics;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.gui.quickview.QuickView;
import ocotillo.various.ColorCollection;

/**
 * Default GUI for continuous graph experiments.
 */
public class Gui extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Color activeButton = new Color(220, 245, 220);
    private final JTabbedPane tabbedPane = new JTabbedPane();

    public Gui() {
        setTitle("Continuous Dynamic Graph Experiments");
        setSize(1450, 1000);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        add(Box.createRigidArea(new Dimension(10, 10)));
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        JLabel title = new JLabel("Continuous Dynamic Graph Experiments");
        Font currentFont = title.getFont();
        title.setFont(new Font(currentFont.getName(), Font.BOLD, 20));

        header.add(Box.createRigidArea(new Dimension(15, 15)));
        header.add(title);
        header.add(Box.createGlue());
        JButton instructionButton = new JButton("Instructions");
        instructionButton.setBackground(activeButton);
        instructionButton.addActionListener((ActionEvent ae) -> {
            JDialog dialog = new JDialog(this, "Instruction Dialog",
                    Dialog.ModalityType.MODELESS);
            dialog.add(new JLabel(instructions()));
            dialog.setBounds(100, 100, 600, 700);
            dialog.setVisible(true);
        });
        header.add(instructionButton);
        header.add(Box.createRigidArea(new Dimension(15, 15)));
        add(header);

        add(Box.createRigidArea(new Dimension(15, 15)));
        add(tabbedPane);

        addExperiment(new Experiment.Bunt());
        addExperiment(new Experiment.Newcomb());
        addExperiment(new Experiment.InfoVis());
        addExperiment(new Experiment.Rugby());
        addExperiment(new Experiment.Pride());
        pack();
    }

    private void addExperiment(Experiment experiment) {
        JPanel experimentPanel = new ExperimentPanel(experiment);
        experimentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab(experiment.name, experimentPanel);
    }

    public String instructions() {
        return "<html><h1>Instructions</h1>"
                + "<p> First, you need to parse or compute the graph you want to visualise. "
                + "Computing certain graphs, in particular with the discrete algorithm, can take up "
                + "to a half hour (check the paper for the expected running time). "
                + "Once the computation is ended, the buttons to view and cluster the graph activate.</p>"
                + "<h2>Graph Navigation</h2>"
                + "<p>Both the space-time cube and the dynamic graph can be panned by clicking and "
                + "dragging with the left mouse button. Zoom can be performed using the mouse wheel. "
                + "It is possible to recenter the view and reset the zoom using the 'r' key.</p>"
                + "<h2>Graph Rotations</h2>"
                + "<p>The axes can be rotated by clicking and dragging the right mouse button. "
                + "It is also possible decide which axis should be put as third dimension by "
                + "using the keys 'x', 'y' or 'z'. For example, the standard x-y 2D view is obtained "
                + "by pressing 'z'."
                + "<h2>Dynamic Graph Animation</h2>"
                + "<p>The dynamic graph (not the space-time cube) can be animated to see the evolution "
                + "over time by pressing 'p'. The animation can be restarted at any time by pressing 'p'. "
                + "The animation can be stopped by pressing 's'. During an animation, it is no more possible"
                + "to navigate or rotate the graph. If this is required, stop the animation, select the "
                + "desired level of zoom and view point and restart the animation.</p>"
                + "<h2>Dynamic Graph Clustering</h2>"
                + "<p>The graph can be clustered by using the k-means algorithm. The algorithm can be "
                + "applied to the time dimension only or to the entire space-time cube. For colouring "
                + "and perception issues, the maximum number of clusters is 12. Once the clustering "
                + "is applied, it can be visualised by opening a new dynamic graph view window. Old "
                + "windows are not updated to reflect the latest clustering.</p>"
                + "<h2>Cluster Visualisation</h2>"
                + "<p>The computed clusters can also be seen as a flattened dynamic graph. Once a clustering "
                + "is computed, the view cluster button is activated. It is possible to select a cluster "
                + "index using the appropriate spinner field and open it as a new static graph by pressing "
                + "the view button.</p>"
                + "</html>";
    }

    public static class ExperimentPanel extends JPanel {

        private final JPanel visonePanel;
        private final ContentPanel discretePanel;
        private final ContentPanel continuousPanel;

        private static final long serialVersionUID = 1L;

        public ExperimentPanel(Experiment experiment) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
            visonePanel = new VisonePanel(experiment);
            contentPanel.add(visonePanel);

            contentPanel.add(Box.createRigidArea(new Dimension(25, 25)));

            discretePanel = new DiscretePanel(experiment);
            contentPanel.add(discretePanel);

            contentPanel.add(Box.createRigidArea(new Dimension(25, 25)));

            continuousPanel = new ContinuousPanel(experiment);
            contentPanel.add(continuousPanel);

            add(Box.createRigidArea(new Dimension(10, 10)));
            this.add(contentPanel);
        }
    }

    public static abstract class ContentPanel extends JPanel {

        protected final Experiment experiment;
        protected final String type;
        private final JLabel titleLabel;
        private final JButton computeButton;
        private final JLabel computationReport;
        private final JButton viewCubeButton;
        private final JButton viewAnimationButton;
        private final JPanel clusterRow;
        private final JSpinner kSpinner;
        private final JButton onTimeButton;
        private final JButton onCubeButton;
        private final JPanel clusterViewRow;
        private final JSpinner clusterSpinner;
        private final JButton viewClusterButton;
        private List<Graph> flattenedClusters;

        private SpaceTimeCubeSynchroniser synchro;

        private static final long serialVersionUID = 1L;

        public ContentPanel(String type, String buttonText, Experiment experiment) {
            this.type = type;
            this.experiment = experiment;
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            titleLabel = new JLabel(type, JLabel.LEFT);
            setSize(titleLabel, 180, 20);

            computeButton = new JButton(buttonText);
            setSize(computeButton, 365, 25);
            computeButton.addActionListener((ActionEvent ae) -> {
                compute();
            });
            computeButton.setBackground(activeButton);

            computationReport = new JLabel("Computation in progress...");
            setSize(computationReport, 365, 25);

            viewCubeButton = new JButton("View space-time cube");
            setSize(viewCubeButton, 365, 25);
            viewCubeButton.addActionListener((ActionEvent ae) -> {
                viewCube();
            });
            viewCubeButton.setEnabled(false);

            viewAnimationButton = new JButton("View dynamic graph");
            setSize(viewAnimationButton, 365, 25);
            viewAnimationButton.addActionListener((ActionEvent ae) -> {
                viewAnimation();
            });
            viewAnimationButton.setEnabled(false);

            clusterRow = new JPanel();
            clusterRow.setLayout(new BoxLayout(clusterRow, BoxLayout.X_AXIS));
            JLabel clusterRowLabel = new JLabel("Cluster with k: ");
            setSize(clusterRowLabel, 110, 25);
            clusterRow.add(clusterRowLabel);
            clusterRow.add(Box.createRigidArea(new Dimension(5, 5)));

            kSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 12, 1));
            setSize(kSpinner, 40, 25);
            clusterRow.add(kSpinner);
            clusterRow.add(Box.createRigidArea(new Dimension(5, 5)));

            onTimeButton = new JButton("On Time");
            onTimeButton.setEnabled(false);
            setSize(onTimeButton, 100, 25);
            clusterRow.add(onTimeButton);
            clusterRow.add(Box.createRigidArea(new Dimension(5, 5)));

            onCubeButton = new JButton("On Cube");
            onCubeButton.setEnabled(false);
            setSize(onCubeButton, 100, 25);
            clusterRow.add(onCubeButton);
            clusterRow.setAlignmentX(LEFT_ALIGNMENT);

            clusterViewRow = new JPanel();
            clusterViewRow.setLayout(new BoxLayout(clusterViewRow, BoxLayout.X_AXIS));
            JLabel clusterViewLabel = new JLabel("View cluster: ");
            setSize(clusterViewLabel, 110, 25);
            clusterViewRow.add(clusterViewLabel);
            clusterViewRow.add(Box.createRigidArea(new Dimension(5, 5)));

            clusterSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
            setSize(clusterSpinner, 40, 25);
            clusterViewRow.add(clusterSpinner);
            clusterViewRow.add(Box.createRigidArea(new Dimension(5, 5)));

            viewClusterButton = new JButton("View");
            viewClusterButton.setEnabled(false);
            setSize(viewClusterButton, 205, 25);
            clusterViewRow.add(viewClusterButton);
            clusterViewRow.setAlignmentX(LEFT_ALIGNMENT);

            preComputationLayout();

            onTimeButton.addActionListener((ActionEvent ae) -> {
                int k = (int) kSpinner.getValue();
                cluster(new DyClustering.Stc.KMeansTime(
                        synchro.originalGraph(), experiment.dataset.suggestedTimeFactor,
                        experiment.delta / 3.0, k,
                        ColorCollection.cbQualitativePastel, experiment.dataset.suggestedInterval));
            });

            onCubeButton.addActionListener((ActionEvent ae) -> {
                int k = (int) kSpinner.getValue();
                cluster(new DyClustering.Stc.KMeans3D(
                        synchro.originalGraph(), experiment.dataset.suggestedTimeFactor,
                        experiment.delta / 3.0, k,
                        ColorCollection.cbQualitativePastel, experiment.dataset.suggestedInterval));
            });

            viewClusterButton.addActionListener((ActionEvent ae) -> {
                int clusterNumber = (int) clusterSpinner.getValue() - 1;
                QuickView.showNewWindow(flattenedClusters.get(clusterNumber));
            });
        }

        private void preComputationLayout() {
            removeAll();
            add(titleLabel);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(computeButton);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(viewCubeButton);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(viewAnimationButton);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(clusterRow);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(clusterViewRow);
        }

        private void postComputationLayout() {
            removeAll();
            add(titleLabel);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(computationReport);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(viewCubeButton);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(viewAnimationButton);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(clusterRow);
            add(Box.createRigidArea(new Dimension(5, 5)));
            this.add(clusterViewRow);
        }

        private void setSize(Component component, int xSize, int ySize) {
            component.setMinimumSize(new Dimension(xSize, ySize));
            component.setMaximumSize(new Dimension(xSize, ySize));
            component.setPreferredSize(new Dimension(xSize, ySize));
        }

        private void compute() {
            postComputationLayout();
            revalidate();
            EventQueue.invokeLater(() -> {
                synchro = getSynchro();
                viewCubeButton.setBackground(activeButton);
                viewAnimationButton.setBackground(activeButton);
                onTimeButton.setBackground(activeButton);
                onCubeButton.setBackground(activeButton);
                viewCubeButton.setEnabled(true);
                viewAnimationButton.setEnabled(true);
                onTimeButton.setEnabled(true);
                onCubeButton.setEnabled(true);
            });
        }

        private void cluster(DyClustering clustering) {
            int k = (int) kSpinner.getValue();
            clustering.colorGraph();
            flattenedClusters = clustering.flattenClusters();
            clusterSpinner.setModel(new SpinnerNumberModel(1, 1, k, 1));
            viewClusterButton.setBackground(activeButton);
            clusterSpinner.setEnabled(true);
            viewClusterButton.setEnabled(true);
//            for (int i = 0; i < flattenedClusters.size(); i++) {
//                Graph graph = flattenedClusters.get(i);
//                SvgExporter.saveSvg(graph, new File(experiment.name + "_" + type + "_" + i + ".svg"));
//            }
        }

        protected void viewCube() {
            QuickView.showNewWindow(synchro.mirrorGraph());
        }

        private void viewAnimation() {
            DyQuickView view = new DyQuickView(synchro.originalGraph(), experiment.dataset.suggestedInterval.leftBound());
            view.setAnimation(new Animation(experiment.dataset.suggestedInterval, Duration.ofSeconds(30)));
            view.showNewWindow();
        }

        protected void updateComputationReport(String text) {
            computationReport.setForeground(Color.GRAY);
            computationReport.setText(text);
        }

        protected abstract SpaceTimeCubeSynchroniser getSynchro();
    }

    private static class VisonePanel extends ContentPanel {

        private static final long serialVersionUID = 1L;

        public VisonePanel(Experiment experiment) {
            super("Visone", "Parse layout", experiment);
        }

        @Override
        protected SpaceTimeCubeSynchroniser getSynchro() {
            DyGraph discreteGraph = experiment.discretise();
            experiment.importVisone(experiment.directory, discreteGraph);
            updateComputationReport("Graph successfully parsed.");
            return new SpaceTimeCubeSynchroniser.StcsBuilder(
                    discreteGraph, experiment.dataset.suggestedTimeFactor).build();
        }
    }

    private static class DiscretePanel extends ContentPanel {

        private static final long serialVersionUID = 1L;

        public DiscretePanel(Experiment experiment) {
            super("Discrete", "Compute layout", experiment);
        }

        @Override
        protected SpaceTimeCubeSynchroniser getSynchro() {
            DyGraph graph = experiment.discretise();
            DyModularFdl algorithm = experiment.getDiscreteLayoutAlgorithm(graph, null);
            SpaceTimeCubeSynchroniser syncrho = algorithm.getSyncro();
            ModularStatistics stats = algorithm.iterate(100);
            String time = stats.getTotalRunnningTime().getSeconds() + "."
                    + String.format("%02d", stats.getTotalRunnningTime().getNano() / 10000000);
            updateComputationReport("Layout computed in " + time + " s");
            return syncrho;
        }
    }

    private static class ContinuousPanel extends ContentPanel {

        private static final long serialVersionUID = 1L;

        public ContinuousPanel(Experiment experiment) {
            super("Continuous", "Compute layout", experiment);
        }

        @Override
        protected SpaceTimeCubeSynchroniser getSynchro() {
            DyGraph graph = experiment.getContinuousCopy();
            DyModularFdl algorithm = experiment.getContinuousLayoutAlgorithm(graph, null);
            SpaceTimeCubeSynchroniser synchro = algorithm.getSyncro();
            ModularStatistics stats = algorithm.iterate(100);
            String time = stats.getTotalRunnningTime().getSeconds() + "."
                    + String.format("%02d", stats.getTotalRunnningTime().getNano() / 10000000);
            updateComputationReport("Layout computed in " + time + " s");
            return synchro;
        }
    }

}
