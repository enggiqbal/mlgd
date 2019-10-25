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

import java.awt.CardLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import ocotillo.dygraph.DyGraph;
import ocotillo.dygraph.rendering.Animation;
import ocotillo.graph.Graph;
import ocotillo.gui.GraphCanvas;

/**
 * Simple dynamic graph visualiser.
 */
public class DyQuickView extends JFrame {

    private final DyGraph dyGraph;
    private GraphCanvas canvas;
    private Animation animation;

    private final JPanel content = new JPanel(new CardLayout());
    private final PlayCommandListner currentListner = new PlayCommandListner();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<GraphCanvas> frameCanvases = new ArrayList<>();
    private ScheduledFuture<?> refreshTaskHandle;

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DyQuickView for the graph.
     *
     * @param dyGraph the dynamic graph to be visualised.
     * @param staticTiming the time used for the static image.
     */
    public DyQuickView(DyGraph dyGraph, double staticTiming) {
        setTitle("Graph QuickView");
        add(content);
        this.dyGraph = dyGraph;
        this.canvas = new GraphCanvas(dyGraph.snapshotAt(staticTiming));
        content.add(canvas);
        canvas.addKeyListener(currentListner);
        canvas.requestFocus();
        setSize(1200, 1200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Gets the currently set animation.
     *
     * @return the current animation.
     */
    public Animation animation() {
        return animation;
    }

    /**
     * Sets the current animation.
     *
     * @param animation the current animation.
     */
    public void setAnimation(Animation animation) {
        this.animation = animation;
        computeFrameCanvases();
    }

    private void computeFrameCanvases() {
        if (animation == null) {
            return;
        }
        for (GraphCanvas frameCanvas : frameCanvases) {
            content.remove(frameCanvas);
        }
        frameCanvases.clear();
        for (Double frameTiming : animation.frames()) {
            Graph snapshot = dyGraph.snapshotAt(frameTiming);
            GraphCanvas frameCanvas = new GraphCanvas(snapshot);
            frameCanvases.add(frameCanvas);
            content.add(frameCanvas);
        }
        validate();
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
     * Prepares the listener for start and stop commands.
     */
    private class PlayCommandListner implements KeyListener {

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            switch (ke.getKeyChar()) {
                case 'p':
                    startPlaying();
                    break;
                case 's':
                    stopPlaying();
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }
    }

    /**
     * Performs the operations required to start the animation.
     */
    private void startPlaying() {
        if (animation == null) {
            return;
        }
        if (refreshTaskHandle != null) {
            stopPlaying();
        }
        content.addKeyListener(currentListner);
        content.requestFocusInWindow();

        canvas.disableCameraControl();
        for (GraphCanvas frameCanvas : frameCanvases) {
            frameCanvas.copyCameraSettings(canvas);
            frameCanvas.viewAngleMoved();
            frameCanvas.disableCameraControl();
        }

        final AnimationTask animationTask = new AnimationTask();
        int refreshPeriod = 1000 / animation.framesPerSecond();
        refreshTaskHandle = scheduler.scheduleAtFixedRate(animationTask,
                0, refreshPeriod, TimeUnit.MILLISECONDS);
    }

    /**
     * Performs the operations required to stop the animation.
     */
    private void stopPlaying() {
        if (refreshTaskHandle != null) {
            refreshTaskHandle.cancel(false);
            refreshTaskHandle = null;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                throw new IllegalStateException("Something went wrong");
            }
            content.removeKeyListener(currentListner);
            canvas.addKeyListener(currentListner);
            canvas.enableCameraControl();
            canvas.requestFocusInWindow();
        }
    }

    /**
     * Refreshes the graph canvas view with the current animation.
     */
    private class AnimationTask implements Runnable {

        private int index;

        /**
         * Constructs the refresh task.
         *
         * @param dyGraph the dynamic graph.
         * @param animation the animation to perform.
         */
        public AnimationTask() {
            this.index = 0;
        }

        @Override
        public void run() {
            try {
                canvas = frameCanvases.get(index);
                CardLayout cardLayout = (CardLayout) content.getLayout();
                if (index == 0) {
                    cardLayout.first(content);
                }
                cardLayout.next(content);
                index++;
                if (index == frameCanvases.size()) {
                    stopPlaying();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
