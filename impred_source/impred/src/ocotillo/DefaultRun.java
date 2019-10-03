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
package ocotillo;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import ocotillo.customrun.CustomRun;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.extra.DyGraphDiscretiser;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl;
import ocotillo.dygraph.layout.fdl.modular.DyModularFdl.DyModularFdlBuilder;
import ocotillo.dygraph.layout.fdl.modular.DyModularForce;
import ocotillo.dygraph.layout.fdl.modular.DyModularPostProcessing;
import ocotillo.dygraph.layout.fdl.modular.DyModularPreMovement;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.geometry.Geom;
import ocotillo.graph.layout.fdl.modular.ModularConstraint;
import ocotillo.gui.quickview.DyQuickView;
import ocotillo.samples.DyGraphSamples;
import ocotillo.samples.parsers.Commons.DyDataSet;
import ocotillo.serialization.ParserTools;

/**
 * Default code for run target.
 */
public class DefaultRun {

    private enum AvailableMode {

        discretisationTest,
        infovis,
        infovisDisc,
        infovisAndDiscrete,
        rugby,
        rugbyDisc,
        rugbyAndDiscrete,
        pride,
        prideDisc,
        prideAndDiscrete,
        vanDeBunt,
        vanDeBuntAndDiscrete,
        computeMetrics,
        gui,
        custom;
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        AvailableMode mode = null;

        if (args.length == 0) {
            showHelp();
            return;
        } else {
            for (AvailableMode availableMode : AvailableMode.values()) {
                if (availableMode.name().equals(args[0])) {
                    mode = availableMode;
                }
            }
            if (mode == null) {
                System.out.println("Mode " + args[0] + " not available.\n");
                showHelp();
                return;
            }
        }

        Experiment experiment;

        switch (mode) {
            case discretisationTest:
                discretisationTest();
                break;
            case infovis:
                experiment = new Experiment.InfoVis();
                experiment.runContinuous(5);
                break;
            case infovisDisc:
                experiment = new Experiment.InfoVis();
                experiment.runDiscrete(5);
                break;
            case infovisAndDiscrete:
                experiment = new Experiment.InfoVis();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case rugby:
                experiment = new Experiment.Rugby();
                experiment.runContinuous(5);
                break;
            case rugbyDisc:
                experiment = new Experiment.Rugby();
                experiment.runDiscrete(5);
                break;
            case rugbyAndDiscrete:
                experiment = new Experiment.Rugby();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case pride:
                experiment = new Experiment.Pride();
                experiment.runContinuous(5);
                break;
            case prideDisc:
                experiment = new Experiment.Pride();
                experiment.runDiscrete(5);
                break;
            case prideAndDiscrete:
                experiment = new Experiment.Pride();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case vanDeBunt:
                experiment = new Experiment.Bunt();
                experiment.runContinuous(0);
                break;
            case vanDeBuntAndDiscrete:
                experiment = new Experiment.Bunt();
                experiment.runContinuous(5);
                experiment.runDiscrete(5);
                break;
            case computeMetrics:
                List<String> lines = new ArrayList<>();
                lines.add("Graph,Type,Time,Scaling,StressOn(d),StressOff(d),StressOn(c),StressOff(c),Movement,Crowding");

                experiment = new Experiment.Bunt();
                lines.addAll(experiment.computeMetrics("0.128"));
                experiment = new Experiment.Newcomb();
                lines.addAll(experiment.computeMetrics("0.109"));
                experiment = new Experiment.InfoVis();
                lines.addAll(experiment.computeMetrics("77.430"));
                experiment = new Experiment.Rugby();
                lines.addAll(experiment.computeMetrics("0.079"));
                experiment = new Experiment.Pride();
                lines.addAll(experiment.computeMetrics("3.391"));

                for (String line : lines) {
                    System.out.println(line);
                }
                ParserTools.writeFileLines(lines,
                        new File("/home/paolo/Development/ContinuousDyGraph/Paper/data/data.csv"));
                ParserTools.writeFileLines(lines,
                        new File("/home/paolo/Dropbox/data.csv"));
                break;
            case gui:
                Gui gui = new Gui();
                SwingUtilities.invokeLater(() -> {
                    gui.setVisible(true);
                });
                break;
            case custom:
                String[] arguments = new String[args.length - 1];
                for (int i = 0; i < arguments.length; i++) {
                    arguments[i] = args[i + 1];
                }
                CustomRun.main(arguments);
                break;
            default:
                throw new UnsupportedOperationException("Not supported");
        }
    }

    private static void showHelp() {
        System.out.println("DynNoSlyce Demo");
        System.out.println("This software is distributed as demo of the approach detailed at: http://cs.swan.ac.uk/~dynnoslice/software.html");
        System.out.println("");
        System.out.println("Append a mode to perform the desired operation:");
        System.out.println("gui          Shows the GUI that allow to run the approach on the default datasets.");
        System.out.println("custom       Allows to execute the software on a custom dataset.");
    }

    public static void discretisationTest() {
        DyDataSet dataset = DyGraphSamples.discretisationExample();

        DyQuickView initialView = new DyQuickView(dataset.dygraph, dataset.suggestedInterval.leftBound());
        initialView.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(10)));
        initialView.showNewWindow();

        List<Double> snapshotTimes = new ArrayList<>();
        snapshotTimes.add(25.0);
        snapshotTimes.add(50.0);
        snapshotTimes.add(75.0);
        snapshotTimes.add(100.0);
        DyGraph discreteGraph = DyGraphDiscretiser.discretiseWithSnapTimes(dataset.dygraph, snapshotTimes);

        DyModularFdl discreteAlgorithm = new DyModularFdlBuilder(discreteGraph, dataset.suggestedTimeFactor)
                .withForce(new DyModularForce.TimeStraightning(5))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.MentalMapPreservation(2))
                .withForce(new DyModularForce.ConnectionAttraction(5))
                .withForce(new DyModularForce.EdgeRepulsion(5))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(10))
                .withConstraint(new ModularConstraint.MovementAcceleration(10, Geom.e3D))
                .withPreMovmement(new DyModularPreMovement.ForbidTimeShitfing())
                .build();

        discreteAlgorithm.iterate(100);

        DyQuickView discreteView = new DyQuickView(discreteGraph, dataset.suggestedInterval.leftBound());
        discreteView.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(10)));
        discreteView.showNewWindow();

        DyModularFdl algorithm = new DyModularFdlBuilder(dataset.dygraph, dataset.suggestedTimeFactor)
                .withForce(new DyModularForce.TimeStraightning(5))
                .withForce(new DyModularForce.Gravity())
                .withForce(new DyModularForce.MentalMapPreservation(2))
                .withForce(new DyModularForce.ConnectionAttraction(5))
                .withForce(new DyModularForce.EdgeRepulsion(5))
                .withConstraint(new ModularConstraint.DecreasingMaxMovement(10))
                .withConstraint(new ModularConstraint.MovementAcceleration(10, Geom.e3D))
                .withPostProcessing(new DyModularPostProcessing.FlexibleTimeTrajectories(7, 8))
                .build();

        algorithm.iterate(100);

        DyQuickView view = new DyQuickView(dataset.dygraph, dataset.suggestedInterval.leftBound());
        view.setAnimation(new Animation(dataset.suggestedInterval, Duration.ofSeconds(10)));
        view.showNewWindow();
    }
}
