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
package ocotillo.graph.rendering.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import ocotillo.geometry.Box;
import ocotillo.graph.Graph;
import ocotillo.graph.rendering.GraphRenderer;
import ocotillo.graph.rendering.GraphRenderer2D;

/**
 * Export graphs as raster images.
 */
public class ImageExporter {

    /**
     * Saves a graph as a PNG image with given dimensions. The graph is scaled
     * keeping the original aspect ratio to the smallest of the two dimensions.
     *
     * @param renderer the renderer to use.
     * @param graph the graph.
     * @param destinationFile the destination file.
     * @param maxWidth the maximum image width.
     * @param maxHeight the maximum image height.
     */
    public static void savePng(GraphRenderer renderer, Graph graph, File destinationFile, int maxWidth, int maxHeight) {
        Box graphBox = renderer.computeBox();

        double xScaling = maxWidth / graphBox.width();
        double yScaling = maxHeight / graphBox.height();
        double scaling = Math.min(xScaling, yScaling);
        int imageWidth = (int) Math.round(graphBox.width() * scaling);
        int imageHeight = (int) Math.round(graphBox.height() * scaling);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        renderer.drawBackground(graph, graphics, imageWidth, imageHeight);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.translate(-graphBox.left() * scaling, graphBox.top() * scaling);
        graphics.scale(scaling / GraphRenderer2D.scaling, scaling / GraphRenderer2D.scaling);
        renderer.draw(graphics);

        try {
            ImageIO.write(image, "png", destinationFile);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write destination file " + destinationFile.getAbsolutePath());
        }
    }

    /**
     * Saves a graph as a PNG image with given dimensions. The graph is scaled
     * keeping the original aspect ratio to the smallest of the two dimensions.
     * The default, 2D graph renderer is used.
     *
     * @param graph the graph.
     * @param destinationFile the destination file.
     * @param maxWidth the maximum image width.
     * @param maxHeight the maximum image height.
     */
    public static void savePng(Graph graph, File destinationFile, int maxWidth, int maxHeight) {
        GraphRenderer renderer = new GraphRenderer2D(graph);
        savePng(renderer, graph, destinationFile, maxWidth, maxHeight);
    }
}
