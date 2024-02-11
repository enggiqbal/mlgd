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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;
import ocotillo.geometry.Box;

/**
 * Listener that can be attached to a Component to implement Zoom and Pan
 * functionality.
 *
 */
public class ZoomAndPanListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final Component targetComponent;
    private int currentZoomLevel = 0;
    private double zoomFactor = 1.2;
    private Point dragStartDeviceCoord;
    private AffineTransform transform = new AffineTransform();

    /**
     * Creates a zoom and pan listener.
     *
     * @param targetComponent the target component.
     */
    public ZoomAndPanListener(Component targetComponent) {
        this.targetComponent = targetComponent;
    }

    /**
     * Creates a zoom and pan listener with given zoom factor. The zoom factor
     * indicates the ratio dimension between elements at two consecutive zoom
     * levels.
     *
     * @param targetComponent the target component.
     * @param zoomFactor the zoom factor.
     */
    public ZoomAndPanListener(Component targetComponent, double zoomFactor) {
        this.targetComponent = targetComponent;
        this.zoomFactor = zoomFactor;
    }

    /**
     * Copies the settings of another listener.
     *
     * @param other the other listener.
     */
    public void copySettings(ZoomAndPanListener other) {
        this.currentZoomLevel = other.currentZoomLevel;
        this.zoomFactor = other.zoomFactor;
        this.dragStartDeviceCoord = other.dragStartDeviceCoord;
        this.transform = new AffineTransform(other.transform);
    }

    /**
     * Gets the canvas transformation.
     *
     * @return the transformation.
     */
    public AffineTransform getTransform() {
        return transform;
    }

    /**
     * Centres and zooms to fit the drawing nicely in the current component.
     *
     * @param boundingBox the bounding box of the drawing.
     */
    public void resetView(Box boundingBox) {
        if (!boundingBox.isValid()) {
            return;
        }

        transform = new AffineTransform();
        currentZoomLevel = 0;
        double xScaleRatio = targetComponent.getSize().width / boundingBox.width();
        double yScaleRatio = targetComponent.getSize().height / boundingBox.height();
        double scaleRatio = Math.min(xScaleRatio, yScaleRatio);
        int desiredZoomLevel = (int) Math.floor(Math.log(scaleRatio) / Math.log(zoomFactor));
        Point componentCenter = new Point(targetComponent.getWidth() / 2, targetComponent.getHeight() / 2);
        zoom(componentCenter, desiredZoomLevel);
        double dx = targetComponent.getWidth() / 2 - boundingBox.center().x() + targetComponent.getX();
        double dy = targetComponent.getHeight() / 2 + boundingBox.center().y() + targetComponent.getY();
        transform.translate(dx, dy);
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            dragStartDeviceCoord = event.getPoint();
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
        if (SwingUtilities.isLeftMouseButton(event)) {
            Point dragEndDeviceCoordinates = event.getPoint();
            pan(dragEndDeviceCoordinates);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        zoom(event.getPoint(), -event.getWheelRotation());
    }

    /**
     * Performs the pan operation.
     *
     * @param dragEndDeviceCoord the device coordinate at which the drag
     * movement ended.
     */
    private void pan(Point dragEndDeviceCoord) {
        Point2D dragStart = transformPoint(dragStartDeviceCoord);
        Point2D dragEnd = transformPoint(dragEndDeviceCoord);
        double dx = dragEnd.getX() - dragStart.getX();
        double dy = dragEnd.getY() - dragStart.getY();
        transform.translate(dx, dy);
        dragStartDeviceCoord = dragEndDeviceCoord;
        targetComponent.repaint();
    }

    /**
     * Performs the zoom operation.
     *
     * @param zoomCenterDeviceCoord the device coordinate at which the zoom
     * operation is applied.
     * @param zoomLevelIncrement the zoom level change. Positive numbers zoom
     * in, negative numbers zoom out.
     */
    private void zoom(Point zoomCenterDeviceCoord, int zoomLevelIncrement) {
        if (zoomLevelIncrement != 0) {
            Point netCenter = new Point(zoomCenterDeviceCoord.x + targetComponent.getX(),
                    zoomCenterDeviceCoord.y + targetComponent.getY());
            currentZoomLevel = currentZoomLevel + zoomLevelIncrement;
            Point2D before = transformPoint(netCenter);
            double factor = Math.pow(zoomFactor, zoomLevelIncrement);
            transform.scale(factor, factor);
            Point2D after = transformPoint(netCenter);
            transform.translate(after.getX() - before.getX(), after.getY() - before.getY());
            targetComponent.repaint();
        }
    }

    /**
     * Transforms a point from device coordinates to user space ones.
     *
     * @param pointDeviceCoord the device coordinates of the point.
     * @return its user space coordinates.
     */
    private Point2D transformPoint(Point pointDeviceCoord) {
        try {
            AffineTransform inverse = transform.createInverse();
            Point2D result = new Point2D.Float();
            inverse.transform(pointDeviceCoord, result);
            return result;
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalStateException("The transformation is not invertible");
        }
    }

}
