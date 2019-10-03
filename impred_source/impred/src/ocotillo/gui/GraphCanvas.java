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
package ocotillo.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import ocotillo.geometry.Box;
import ocotillo.graph.Element;
import ocotillo.graph.ElementAttribute;
import ocotillo.graph.Graph;
import ocotillo.graph.GraphAttribute;
import ocotillo.graph.Observer;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.rendering.GraphRenderer;
import ocotillo.graph.rendering.GraphRenderer2D;
import ocotillo.graph.rendering.GraphRenderer3D;
import ocotillo.graph.rendering.ViewAngle;

/**
 * Panned-zoomed canvas for depicting graphs.
 */
public class GraphCanvas extends JLayeredPane implements ViewAngle.Observer {

    private final Graph graph;
    private GraphRenderer renderer;

    private final CameraControl cameraControl;
    private final List<Observer> observers = new ArrayList<>();
    private final DrawingPanel drawing;
    private final CanvasOverlay overlay;

    private boolean firstPaint = true;
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a graph canvas.
     *
     * @param graph the graph to visualise.
     */
    public GraphCanvas(Graph graph) {
        this.graph = graph;
        this.renderer = new GraphRenderer2D(graph);

        cameraControl = new CameraControl(this);
        cameraControl.viewAngle().registerObserver(this);

        drawing = new DrawingPanel();
        overlay = new CanvasOverlay(cameraControl);
        add(drawing, 0);
        add(overlay, 1);

        addGraphModificationObservers();
        setFocusable(true);
    }

    /**
     * Copies the camera control settings of another graph canvas.
     *
     * @param other the other canvas.
     */
    public void copyCameraSettings(GraphCanvas other) {
        cameraControl.copySettings(other.cameraControl);
        firstPaint = false;
    }

    /**
     * Returns the current status of the camera control.
     *
     * @return true if camera control is on, false otherwise.
     */
    public boolean isCameraControlOn() {
        return cameraControl.isControlOn();
    }

    /**
     * Enables the camera control.
     */
    public void enableCameraControl() {
        cameraControl.enableControl();
    }

    /**
     * Disables the camera control.
     */
    public void disableCameraControl() {
        cameraControl.disableControl();
    }

    /**
     * Sets the canvas to be 2D only by disabling rotations.
     */
    public void set2D() {
        cameraControl.set2D();
    }

    /**
     * Sets the canvas to be 3D by enabling rotations.
     */
    public void set3D() {
        cameraControl.set3D();
    }

    /**
     * Resets the view.
     */
    public void resetView() {
        cameraControl.resetView();
        repaint();
    }

    /**
     * Returns the box that contains a compete view of the graph.
     *
     * @return the view box.
     */
    protected Box getViewBox() {
        return renderer.computeBox();
    }

    /**
     * Gets the central Z position of the graph.
     *
     * @return the central Z position of the graph.
     */
    protected double getOffsetZ() {
        return renderer.getOffsetZ(graph);
    }

    /**
     * Forces the usage of a 2D rendering.
     */
    protected void setRendering2D() {
        renderer = new GraphRenderer2D(graph);
    }

    /**
     * Toggles the overlay.
     */
    protected void toggleOverlay() {
        overlay.toggleOnOff();
    }

    /**
     * Adds an observer for each graph modification that alter the graph
     * rendering.
     */
    private void addGraphModificationObservers() {
        addGraphAttributeObserver(graph);
        for (GraphAttribute<?> attribute : StdAttribute.thatAffectRendering.graphAttributes(graph)) {
            addGraphAttributeObserver(attribute);
        }
        for (ElementAttribute<?, ?> attribute : StdAttribute.thatAffectRendering.nodeAttributes(graph)) {
            addElementAttributeObserver(attribute);
        }
        for (ElementAttribute<?, ?> attribute : StdAttribute.thatAffectRendering.edgeAttributes(graph)) {
            addElementAttributeObserver(attribute);
        }
    }

    /**
     * Adds a graph observer that force the canvas redrawing on graph
     * modifications.
     *
     * @param graph the graph to observe.
     */
    private void addGraphAttributeObserver(Graph graph) {
        observers.add(new Observer.GraphElements(graph) {

            @Override
            public void theseElementsChanged(Collection<Element> changedElements) {
                repaint();
            }
        });
    }

    /**
     * Adds an attribute observer that force the canvas redrawing on
     * modifications of the attribute.
     *
     * @param attribute the attribute to observe.
     */
    private void addGraphAttributeObserver(GraphAttribute<?> attribute) {
        observers.add(new Observer.GraphAttributeChanges(attribute) {

            @Override
            public void update() {
                repaint();
            }
        });
    }

    /**
     * Adds an attribute observer that force the canvas redrawing on
     * modifications of the attribute.
     *
     * @param attribute the attribute to observe.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addElementAttributeObserver(ElementAttribute<?, ?> attribute) {
        observers.add(new Observer.ElementAttributeChanges(attribute) {

            @Override
            public void update(Collection changedElements) {
                repaint();
            }

            @Override
            public void updateAll() {
                repaint();
            }
        });
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawing.setBounds(0, 0, getWidth(), getHeight());
        overlay.setBounds(0, 0, getWidth(), getHeight());
    }

    /**
     * Close the graph canvas preventing memory leaks.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void close() {
        cameraControl.close();
        cameraControl.viewAngle().unregisterObserver(this);
        for (Observer observer : observers) {
            observer.unregister();
        }
    }

    @Override
    public void viewAngleMoved() {
        if (!cameraControl.viewAngle().isStandard2D() && renderer instanceof GraphRenderer2D) {
            renderer = new GraphRenderer3D(graph, cameraControl.viewAngle());
            if (!overlay.isOn()) {
                overlay.toggleOnOff();
            }
        }
        repaint();
    }

    /**
     * The panel containing the actual drawing.
     */
    private class DrawingPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        public void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (firstPaint) {
                cameraControl.resetView();
                firstPaint = false;
            }
            renderer.drawBackground(graph, graphics2D, getWidth(), getHeight());
            graphics2D.setTransform(cameraControl.getTransform());
            renderer.draw(graphics2D);
            overlay.repaint();
        }
    }
}
