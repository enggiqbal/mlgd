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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;
import ocotillo.graph.rendering.ViewAngle;

/**
 * Listener that can detect and report a rotation mouse gesture.
 *
 */
public class RotationListener implements MouseListener, MouseMotionListener {

    public final double fullScreenRotation = Math.PI;

    private final Component targetComponent;
    private final ViewAngle viewAngle;
    private Point dragStart;

    /**
     * Creates a zoom and pan listener.
     *
     * @param targetComponent the target component.
     * @param viewAngle the view angle.
     */
    public RotationListener(Component targetComponent, ViewAngle viewAngle) {
        this.targetComponent = targetComponent;
        this.viewAngle = viewAngle;
    }

    /**
     * Resets the view to the standard 2D view.
     *
     */
    public void resetView() {
        viewAngle.setAngles(0, 0);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event)) {
            dragStart = event.getPoint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent event) {
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event)) {
            Point dragEndDeviceCoordinates = event.getPoint();
            rotate(dragEndDeviceCoordinates);
        }
    }

    /**
     * Performs the rotate operation.
     *
     * @param dragEnd the device coordinate at which the drag movement ended.
     */
    private void rotate(Point dragEnd) {
        double windowWidth = targetComponent.getSize().width;
        double windowHeight = targetComponent.getSize().height;
        double dx = (dragEnd.x - dragStart.x) / windowWidth;
        double dy = (dragEnd.y - dragStart.y) / windowHeight;
        viewAngle.rotate(-dx * fullScreenRotation, -dy * fullScreenRotation);
        dragStart = dragEnd;
        targetComponent.repaint();
    }

}
