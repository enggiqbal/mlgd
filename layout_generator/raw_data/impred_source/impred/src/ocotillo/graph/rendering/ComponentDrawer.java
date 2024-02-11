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
package ocotillo.graph.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ListIterator;
import ocotillo.geometry.Coordinates;
import static ocotillo.graph.rendering.GraphRenderer.scaling;

/**
 * Rendered for the elements of a graph drawing.
 */
public class ComponentDrawer {

    /**
     * Draws a rectangle in the given graphics.
     *
     * @param graphics2D the graphics.
     * @param center the rectangle center.
     * @param size the rectangle size.
     * @param fillColor the rectangle fill color.
     */
    public static void drawRectangle(Graphics2D graphics2D, Coordinates center, Coordinates size, Color fillColor) {
        if (fillColor.getAlpha() == 0) {
            return;
        }

        Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();
        graphicsCopy.setPaint(fillColor);
        Coordinates leftBottom = new Coordinates(center.x() - size.x() / 2, -center.y() - size.y() / 2);
        graphicsCopy.fill(new Rectangle2D.Double(leftBottom.x() * scaling, leftBottom.y() * scaling, size.x() * scaling, size.y() * scaling));
        graphicsCopy.dispose();
    }

    /**
     * Draws an ellipse in the given graphics.
     *
     * @param graphics2D the graphics.
     * @param center the ellipse center.
     * @param size the ellipse size.
     * @param fillColor the ellipse fill color.
     */
    public static void drawEllipse(Graphics2D graphics2D, Coordinates center, Coordinates size, Color fillColor) {
        if (fillColor.getAlpha() == 0) {
            return;
        }

        Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();
        graphicsCopy.setPaint(fillColor);
        Coordinates leftBottom = new Coordinates(center.x() - size.x() / 2, -center.y() - size.y() / 2);
        graphicsCopy.fill(new Ellipse2D.Double(leftBottom.x() * scaling, leftBottom.y() * scaling, size.x() * scaling, size.y() * scaling));
        graphicsCopy.dispose();
    }

    /**
     * Draws text in the given graphics.
     *
     * @param graphics2D the graphics.
     * @param text the text.
     * @param center the text central position.
     * @param dimension the text dimension.
     * @param fillColor the text fill color.
     */
    public static void drawText(Graphics2D graphics2D, String text, Coordinates center, double dimension, Color fillColor) {
        if (text.isEmpty() || fillColor.getAlpha() == 0) {
            return;
        }

        Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();

        double labelFontSize = dimension * scaling;                        // TODO check me!
        Font font = new Font("SansSerif", Font.PLAIN, (int) labelFontSize);
        FontRenderContext frc = graphicsCopy.getFontRenderContext();
        TextLayout layout = new TextLayout(text, font, frc);
        Rectangle2D bounds = layout.getBounds();
        float xPosition = (float) (center.x() * scaling - bounds.getWidth() / 2.0);
        float yPosition = (float) -(center.y() * scaling - bounds.getHeight() / 2.0);

        graphicsCopy.setFont(font);
        graphicsCopy.setPaint(fillColor);
        layout.draw(graphicsCopy, xPosition, yPosition);
        graphicsCopy.dispose();
    }

    /**
     * Draws a polyline in the given graphics.
     *
     * @param graphics2D the graphics.
     * @param points the polyline points.
     * @param width the polyline width.
     * @param strokeColor the polyline stoke color.
     */
    public static void drawPolyline(Graphics2D graphics2D, List<Coordinates> points, double width, Color strokeColor) {
        if (strokeColor.getAlpha() == 0) {
            return;
        }

        Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();
        graphicsCopy.setStroke(new BasicStroke((float) (width * scaling)));
        graphicsCopy.setPaint(strokeColor);

        GeneralPath polyline = new GeneralPath();

        ListIterator<Coordinates> pointIterator = points.listIterator();
        Coordinates firstPoint = pointIterator.next();
        polyline.moveTo(firstPoint.x() * scaling, -firstPoint.y() * scaling);

        while (pointIterator.hasNext()) {
            Coordinates nextPoint = pointIterator.next();
            polyline.lineTo(nextPoint.x() * scaling, -nextPoint.y() * scaling);
        }

        graphicsCopy.draw(polyline);
        graphicsCopy.dispose();
    }

    /**
     * Draws a polyline in the given graphics.
     *
     * @param graphics2D the graphics.
     * @param startingPoint the starting point.
     * @param endingPoint the ending point.
     * @param middlePoints the middle points.
     * @param width the polyline width.
     * @param strokeColor the polyline stoke color.
     */
    public static void drawPolyline(Graphics2D graphics2D, Coordinates startingPoint, Coordinates endingPoint, List<Coordinates> middlePoints, double width, Color strokeColor) {
        if (strokeColor.getAlpha() == 0) {
            return;
        }

        Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();
        graphicsCopy.setStroke(new BasicStroke((float) (width * scaling)));
        graphicsCopy.setPaint(strokeColor);

        GeneralPath polyline = new GeneralPath();
        polyline.moveTo(startingPoint.x() * scaling, -startingPoint.y() * scaling);
        for (Coordinates middlePoint : middlePoints) {
            polyline.lineTo(middlePoint.x() * scaling, -middlePoint.y() * scaling);
        }
        polyline.lineTo(endingPoint.x() * scaling, -endingPoint.y() * scaling);

        graphicsCopy.draw(polyline);
        graphicsCopy.dispose();
    }

    /**
     * Draws a polygon in the given graphics.
     *
     * @param graphics2D the graphics.
     * @param points the polygon points.
     * @param fillColor the polygon fill color.
     * @param strokeWidth the polygon stroke width.
     * @param strokeColor the polygon stroke color.
     */
    public static void drawPolygon(Graphics2D graphics2D, List<Coordinates> points, Color fillColor, double strokeWidth, Color strokeColor) {
        if (strokeColor.getAlpha() == 0) {
            return;
        }

        Polygon polygon = new Polygon();
        for (Coordinates point : points) {
            int x = (int) Math.round(point.x() * scaling);
            int y = (int) Math.round(-point.y() * scaling);
            polygon.addPoint(x, y);
        }

        Graphics2D graphicsCopy = (Graphics2D) graphics2D.create();

        graphicsCopy.setPaint(fillColor);
        graphicsCopy.fill(polygon);

        graphicsCopy.setStroke(new BasicStroke((float) (strokeWidth * scaling)));
        graphicsCopy.setPaint(strokeColor);
        graphicsCopy.draw(polygon);

        graphicsCopy.dispose();
    }

}
