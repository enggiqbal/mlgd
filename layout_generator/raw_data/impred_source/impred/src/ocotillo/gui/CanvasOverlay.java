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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;
import javax.swing.JPanel;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;

/**
 * Additional interface for graph canvas.
 */
public class CanvasOverlay extends JPanel {

    private final CameraControl camera;
    private boolean isOn = false;

    private static final double margin = 10;
    private static final double compassLength = 40;
    private static final Coordinates compassCenter = new Coordinates(
            3 * margin + compassLength,
            3 * margin + compassLength);

    private static final double minAlpha = 10;
    private static final Color centerColor = colorAtZ(0);

    private static final Font font = new Font("SansSerif", Font.PLAIN, 10);
    private static final double xLabelOffset = 2.5;
    private static final double yLabelOffset = -3.0;

    private static final long serialVersionUID = 1L;

    /**
     * Construct the canvas interface.
     *
     * @param camera the camera control.
     */
    public CanvasOverlay(CameraControl camera) {
        this.camera = camera;
    }

    /**
     * Returns the visibility of the overlay.
     *
     * @return true if the overlay is on, false otherwise.
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * Toggles the overlay on or off.
     */
    public void toggleOnOff() {
        isOn = !isOn;
        setOpaque(isOn);
        repaint();
    }

    /**
     * Draws an axis as a unit vector.
     *
     * @param graphics the graphics.
     * @param axisTip the tip of the unit vector.
     * @param label the axis label.
     */
    private void drawAxis(Graphics2D graphics, Coordinates axisTip, String label) {
        Coordinates transformedTip = camera.viewAngle().transformedPosition(axisTip);
        transformedTip.setY(-transformedTip.y());
        Coordinates tipPosition = transformedTip.plus(compassCenter);
        Color tipColor = colorAtZ(tipPosition.z());

        drawAxisStick(graphics, tipPosition, tipColor);
        drawAxisLabel(graphics, transformedTip, label, tipColor);
    }

    /**
     * Draws an axis stick.
     *
     * @param graphics the graphics.
     * @param tipPosition the transformed position of the axis tip.
     * @param tipColor the colour of the axis tip.
     */
    private void drawAxisStick(Graphics2D graphics, Coordinates tipPosition, Color tipColor) {
        graphics.setStroke(new BasicStroke(2f));
        GradientPaint paint = new GradientPaint(
                (int) compassCenter.x(), (int) compassCenter.y(), centerColor,
                (int) tipPosition.x(), (int) tipPosition.y(), tipColor);
        graphics.setPaint(paint);
        GeneralPath arrowStick = new GeneralPath();
        arrowStick.moveTo(compassCenter.x(), compassCenter.y());
        arrowStick.lineTo(tipPosition.x(), tipPosition.y());
        graphics.draw(arrowStick);
    }

    /**
     * Draws an axis label.
     *
     * @param graphics the graphics.
     * @param transformedTip the transformation of the axis unit vector.
     * @param label the axis label.
     * @param tipColor the colour of the axis tip.
     */
    private void drawAxisLabel(Graphics2D graphics, Coordinates transformedTip, String label, Color tipColor) {
        Coordinates textPos = Geom.e2D.unitVector(Geom.e2D.angle(transformedTip))
                .timesIP(Geom.e2D.magnitude(transformedTip) + margin).plusIP(compassCenter);

        FontRenderContext frc = graphics.getFontRenderContext();
        TextLayout layout = new TextLayout(label, font, frc);
        graphics.setFont(font);
        graphics.setPaint(tipColor);
        layout.draw(graphics, (float) (textPos.x() - xLabelOffset), (float) (textPos.y() - yLabelOffset));
    }

    /**
     * Computes the colour at a given depth. Higher z values (farther away from
     * the user) will have a more faded colour.
     *
     * @param z the depth.
     * @return the colour at that depth.
     */
    private static Color colorAtZ(double z) {
        double rangeZ = 2 * compassLength;
        double rangeAlpha = 255 - minAlpha;
        double relativeZ = (z + compassLength) / rangeZ;
        double relativeAlpha = (1 - relativeZ) * rangeAlpha + minAlpha;
        return new Color(0, 0, 0, (int) relativeAlpha);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if (isOn) {
            Graphics2D graphicsCopy = (Graphics2D) graphics.create();
            graphicsCopy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawAxis(graphicsCopy, new Coordinates(compassLength, 0, 0 + camera.viewAngle().offsetZ()), "x");
            drawAxis(graphicsCopy, new Coordinates(0, compassLength, 0 + camera.viewAngle().offsetZ()), "y");
            drawAxis(graphicsCopy, new Coordinates(0, 0, compassLength + camera.viewAngle().offsetZ()), "z");

            graphicsCopy.dispose();
        }
    }
}
