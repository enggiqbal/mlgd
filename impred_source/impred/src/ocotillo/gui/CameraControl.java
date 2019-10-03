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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import ocotillo.graph.rendering.ViewAngle;

/**
 * Controls the camera in a GraphCanvas.
 */
public class CameraControl {

    private final GraphCanvas canvas;
    private final ZoomAndPanListener zoomAndPan;
    private final RotationListener rotation;
    private final ViewAngle viewAngle;
    private KeyListener keyListener;
    private ViewMode mode;
    private boolean controlsOn;

    /**
     * View mode.
     */
    private enum ViewMode {
        in2D,
        in3D;
    }

    /**
     * Builds and attach a camera control to the canvas.
     *
     * @param canvas the canvas.
     */
    public CameraControl(GraphCanvas canvas) {
        this.canvas = canvas;
        this.viewAngle = new ViewAngle();
        this.zoomAndPan = new ZoomAndPanListener(this.canvas);
        this.rotation = new RotationListener(this.canvas, this.viewAngle);
        this.keyListener = new KeyCommandListner3D();
        mode = ViewMode.in3D;
        enableControl();
    }

    /**
     * Copies the settings of another camera control.
     *
     * @param other the other camera control.
     */
    public void copySettings(CameraControl other) {
        zoomAndPan.copySettings(other.zoomAndPan);
        viewAngle.copySettings(other.viewAngle);
        if (other.mode == ViewMode.in2D) {
            set2D();
        }
    }

    /**
     * Returns the current status of the camera control.
     *
     * @return true if camera control is on, false otherwise.
     */
    public boolean isControlOn() {
        return controlsOn;
    }

    /**
     * Enables the camera control.
     */
    public final void enableControl() {
        switch (mode) {
            case in2D:
                set2D();
                break;
            case in3D:
                set3D();
                break;
            default:
                throw new IllegalStateException("This mode is not supported yet " + mode);
        }
    }

    /**
     * Disables the camera control.
     */
    public void disableControl() {
        canvas.removeMouseListener(zoomAndPan);
        canvas.removeMouseMotionListener(zoomAndPan);
        canvas.removeMouseWheelListener(zoomAndPan);

        canvas.removeMouseListener(rotation);
        canvas.removeMouseMotionListener(rotation);

        canvas.removeKeyListener(keyListener);

        controlsOn = false;
    }

    /**
     * Sets the camera to a 2D environment by disabling rotations.
     */
    public void set2D() {
        disableControl();
        if (mode != ViewMode.in2D) {
            keyListener = new KeyCommandListner2D();
            mode = ViewMode.in2D;
        }
        canvas.addKeyListener(keyListener);
        canvas.addMouseListener(zoomAndPan);
        canvas.addMouseMotionListener(zoomAndPan);
        canvas.addMouseWheelListener(zoomAndPan);
        controlsOn = true;
    }

    /**
     * Sets the camera to a 3D environment by enabling rotations.
     */
    public void set3D() {
        disableControl();
        if (mode != ViewMode.in3D) {
            keyListener = new KeyCommandListner3D();
            mode = ViewMode.in3D;
        }
        canvas.addMouseListener(zoomAndPan);
        canvas.addMouseMotionListener(zoomAndPan);
        canvas.addMouseWheelListener(zoomAndPan);
        canvas.addMouseListener(rotation);
        canvas.addMouseMotionListener(rotation);
        canvas.addKeyListener(keyListener);
        controlsOn = true;
    }

    /**
     * Gets the view angle.
     *
     * @return the view angle.
     */
    public ViewAngle viewAngle() {
        return viewAngle;
    }

    /**
     * Gets the zoom and pan transform.
     */
    AffineTransform getTransform() {
        return zoomAndPan.getTransform();
    }

    /**
     * Resets the view.
     */
    public void resetView() {
        viewAngle().setOffsetZ(canvas.getOffsetZ());
        rotation.resetView();
        canvas.setRendering2D();
        zoomAndPan.resetView(canvas.getViewBox());
    }

    /**
     * Closes the camera control and detaches all the observers.
     */
    public void close() {
        canvas.removeMouseListener(zoomAndPan);
        canvas.removeMouseMotionListener(zoomAndPan);
        canvas.removeMouseWheelListener(zoomAndPan);

        canvas.removeMouseListener(rotation);
        canvas.removeMouseMotionListener(rotation);

        canvas.removeKeyListener(keyListener);
    }

    /**
     * Defines the commands that are performed through keyboard keys in 2D only
     * environment.
     */
    private class KeyCommandListner2D implements KeyListener {

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            switch (ke.getKeyChar()) {
                case 'r':
                    resetView();
                    break;
                case 'o':
                    canvas.toggleOverlay();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }
    }

    /**
     * Defines the commands that are performed through keyboard keys in 3D
     * environment.
     */
    private class KeyCommandListner3D implements KeyListener {

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            switch (ke.getKeyChar()) {
                case 'r':
                    resetView();
                    break;
                case 'x':
                    viewAngle.setAngles(Math.PI / 2, 0);
                    break;
                case 'y':
                    viewAngle.setAngles(0, -Math.PI / 2);
                    break;
                case 'z':
                    viewAngle.setAngles(0, 0);
                    break;
                case 'h':
                    viewAngle.yawRotate(Math.PI);
                    break;
                case 'v':
                    viewAngle.pitchRotate(Math.PI);
                    break;
                case 'o':
                    canvas.toggleOverlay();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
        }
    }
}
