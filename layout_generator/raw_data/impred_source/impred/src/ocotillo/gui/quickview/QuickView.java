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
package ocotillo.gui.quickview;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import ocotillo.graph.Graph;
import ocotillo.gui.GraphCanvas;

/**
 * Simple graph visualiser.
 */
public class QuickView extends JFrame {

    private GraphCanvas canvas;

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a QuickView for the graph.
     *
     * @param graph the graph to be visualised.
     */
    public QuickView(Graph graph) {
        setTitle("Graph QuickView");
        add(new GraphCanvas(graph));
        setSize(1200, 1200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Sets the canvas to be 2D only by disabling rotations.
     */
    public void set2D() {
        canvas.set2D();
    }

    /**
     * Sets the canvas to be 3D by enabling rotations.
     */
    public void set3D() {
        canvas.set3D();
    }

    /**
     * Display the DyQuickView window.
     *
     */
    public void showNewWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }

    /**
     * Runs a new instance of QuickView to display the given graph.
     *
     * @param graph the graph to be visualised.
     */
    public static void showNewWindow(final Graph graph) {
        SwingUtilities.invokeLater(() -> {
            QuickView view = new QuickView(graph);
            view.setVisible(true);
        });
    }

}
