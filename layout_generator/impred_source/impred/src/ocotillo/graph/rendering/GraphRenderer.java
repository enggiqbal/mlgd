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

import java.awt.Color;
import java.awt.Graphics2D;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.StdAttribute;

/**
 * Defines a class that draws a graph.
 */
public abstract class GraphRenderer {

    /**
     * The scaling factor used in the drawing. The scaling factor has been
     * empirically determined in order to make the letter X with font scaling 1
     * fit nicely in a node of size 1.
     */
    public static final int scaling = 13;

    /**
     * Draws a graph into a graphics object.
     *
     * @param graphics the graphics.
     */
    public abstract void draw(Graphics2D graphics);

    /**
     * Returns the rendering box for the graph.
     *
     * @return the rendering box.
     */
    public abstract Box computeBox();

    /**
     * Draws the background of the drawing. Must be called by the graphical
     * component, so that it can pass its width and height. Must be called
     * before setting any transform.
     *
     * @param graph the graph.
     * @param graphics the graphics.
     * @param width the component width.
     * @param height the component height.
     */
    public void drawBackground(Graph graph, Graphics2D graphics, int width, int height) {
        if (graph.hasLocalGraphAttribute(StdAttribute.background)) {
            Color backgroundColor = graph.<Color>graphAttribute(StdAttribute.background).get();
            if (backgroundColor.getAlpha() > 0) {
                Graphics2D graphicsCopy = (Graphics2D) graphics.create();
                graphicsCopy.setPaint(backgroundColor);
                graphicsCopy.fillRect(0, 0, width, height);
                graphicsCopy.dispose();
            }
        }
    }

    /**
     * Returns the central position of the graph with respect to the Z axis.
     *
     * @param graph the graph.
     * @return the central Z position.
     */
    public double getOffsetZ(Graph graph) {
        if (graph.nodes().isEmpty()) {
            return 0;
        }
        NodeAttribute<Coordinates> position = graph.nodeAttribute(StdAttribute.nodePosition);
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Node node : graph.nodes()) {
            double z = position.get(node).z();
            min = Math.min(min, z);
            max = Math.max(max, z);
        }
        return (min + max) / 2.0;
    }
}
