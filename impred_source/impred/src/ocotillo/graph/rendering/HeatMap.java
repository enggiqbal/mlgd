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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.graph.Element;
import ocotillo.graph.Graph;
import ocotillo.graph.Node;
import ocotillo.graph.NodeAttribute;
import ocotillo.graph.Observer;
import ocotillo.graph.StdAttribute;
import ocotillo.graph.layout.Layout2D;

/**
 * HeatMap on graph nodes.
 */
public class HeatMap {

    private static final int hotSpotRadius = 60;
    private static final int pixelFactor = 10;

    private Gradient gradient = new Gradient(Arrays.asList(new Color(255, 0, 0, 0), new Color(255, 0, 0, 200)), 200);
    private Double coldestHeatValue = null;
    private Double hottestHeatValue = null;

    private Graph graph;
    private Box graphBox;
    private double[][] pixelHeat;
    private Image image;
    private boolean needHeatRecomputing = true;
    private boolean needImageRecomputing = true;

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Set the heat value corresponding to the coldest gradient colour.
     *
     * @param coldestHeatValue the heat value of the coldest colour.
     */
    public void setColdestHeatValue(double coldestHeatValue) {
        this.coldestHeatValue = coldestHeatValue;
        this.needImageRecomputing = true;
    }

    /**
     * Set the heat value corresponding to the hottest gradient colour.
     *
     * @param hottestHeatValue the heat value of the hottest colour.
     */
    public void setHottestHeatValue(double hottestHeatValue) {
        this.hottestHeatValue = hottestHeatValue;
        this.needImageRecomputing = true;
    }

    /**
     * Indicates to compute the heat value of the coldest colour as the minimum
     * heat value in the graph.
     */
    public void setDynamicColdest() {
        this.coldestHeatValue = null;
        this.needImageRecomputing = true;
    }

    /**
     * Indicates to compute the heat value of the hottest colour as the maximum
     * heat value in the graph.
     */
    public void setDynamicHottest() {
        this.hottestHeatValue = null;
        this.needImageRecomputing = true;
    }

    /**
     * Sets the gradient to be used.
     *
     * @param gradient the gradient.
     */
    public void setGradient(Gradient gradient) {
        this.gradient = gradient;
        this.needImageRecomputing = true;
    }

    /**
     * Returns the heat map image for the graph.
     *
     * @param graph the graph.
     * @return the heat map image.
     */
    public Image getImage(Graph graph) {
        if (needHeatRecomputing || this.graph != graph) {
            recompute(graph);
            needHeatRecomputing = false;
            needImageRecomputing = false;
        } else if (needImageRecomputing) {
            image = recomputeImage(pixelHeat, graph, gradient, coldestHeatValue, hottestHeatValue);
            needImageRecomputing = false;
        }
        return image;
    }

    /**
     * Returns the box of the image.
     *
     * @return the image box.
     */
    public Box getImageBox() {
        double margin = (double) hotSpotRadius / pixelFactor;
        return graphBox.expand(new Coordinates(margin, margin));
    }

    /**
     * Completely recompute the heat map for a given graph.
     *
     * @param graph the graph.
     */
    public void recompute(Graph graph) {
        for (Observer observer : observers) {
            observer.unregister();
        }

        this.graph = graph;
        this.graphBox = Layout2D.graphBox(graph);
        pixelHeat = recomputePixelHeat(graph, graphBox);
        image = recomputeImage(pixelHeat, graph, gradient, coldestHeatValue, hottestHeatValue);

        registerGraphObserver();
        registerPositionObserver();
        registerHeatObserver();
    }

    /**
     * Registers a graph observer to be notified of node insertions and
     * removals.
     */
    private void registerGraphObserver() {
        observers.add(new Observer.GraphElements(graph) {

            @Override
            public void theseElementsChanged(Collection<Element> changedElements) {
                for (Element element : changedElements) {
                    if (element instanceof Node) {
                        needHeatRecomputing = true;
                    }
                }
            }
        });
    }

    /**
     * Registers a node position observer to be notified of changes in node
     * positions.
     */
    private void registerPositionObserver() {
        observers.add(new Observer.ElementAttributeChanges<Node>(graph.nodeAttribute(StdAttribute.nodePosition)) {

            @Override
            public void update(Collection<Node> changedElements) {
                needHeatRecomputing = true;
            }

            @Override
            public void updateAll() {
                needHeatRecomputing = true;
            }
        });
    }

    /**
     * Registers a node heat observer to be notified of changes in node heat.
     */
    private void registerHeatObserver() {
        observers.add(new Observer.ElementAttributeChanges<Node>(graph.nodeAttribute(StdAttribute.nodeHeat)) {

            @Override
            public void update(Collection<Node> changedElements) {
                needHeatRecomputing = true;
            }

            @Override
            public void updateAll() {
                needHeatRecomputing = true;
            }
        });
    }

    /**
     * Closes the HeatMap and detach the observers.
     */
    public void close() {
        for (Observer observer : observers) {
            observer.unregister();
        }
    }

    /**
     * Recompute the heat of each heat map pixel.
     *
     * @param graph the graph.
     * @param graphBox the graph box.
     * @return
     */
    private static double[][] recomputePixelHeat(Graph graph, Box graphBox) {
        int width = (int) graphBox.width() * pixelFactor + 2 * hotSpotRadius + 10;
        int height = (int) graphBox.height() * pixelFactor + 2 * hotSpotRadius + 10;
        double[][] pixelHeat = new double[width][height];

        NodeAttribute<Coordinates> positions = graph.nodeAttribute(StdAttribute.nodePosition);
        NodeAttribute<Double> heatValues = graph.nodeAttribute(StdAttribute.nodeHeat);

        for (Node node : graph.nodes()) {
            double nodeHeat = heatValues.get(node);
            if (!Geom.eXD.almostZero(nodeHeat)) {
                Coordinates relativePos = positions.get(node).minus(new Coordinates(graphBox.left(), graphBox.bottom()));
                int x = (int) (relativePos.x() * pixelFactor + hotSpotRadius + 5);
                int y = (int) (relativePos.y() * pixelFactor + hotSpotRadius + 5);
                fillHotSpot(pixelHeat, x, y, heatValues.get(node));
            }
        }
        return pixelHeat;
    }

    /**
     * Fills the pixels of an hot spot.
     *
     * @param pixelHeat the pixel heat matrix.
     * @param x the x pixel index.
     * @param y the y pixel index.
     * @param maxHeat the heat of the central pixel.
     */
    private static void fillHotSpot(double[][] pixelHeat, int x, int y, double maxHeat) {
        for (int i = -hotSpotRadius; i <= hotSpotRadius; i++) {
            for (int j = -hotSpotRadius; j <= hotSpotRadius; j++) {
                double radiusXComp = ((double) i) / hotSpotRadius;
                double radiusYComp = ((double) j) / hotSpotRadius;
                double radius = Math.sqrt(radiusXComp * radiusXComp + radiusYComp * radiusYComp);
                double distanceFactor = Math.pow(Math.max(0, 1 - radius), 2);
                double heat = maxHeat * distanceFactor;
                pixelHeat[x + i][y + j] = Math.max(pixelHeat[x + i][y + j], heat);
            }
        }
    }

    /**
     * Recomputes the heat map image.
     *
     * @param pixelHeat the pixel heat matrix.
     * @param graph the graph.
     * @param gradient the gradient.
     * @param coldestHeatValue the set value of the coldest color, null if
     * dynamic.
     * @param hottestHeatValue the set value of the hottest color, null if
     * dynamic.
     * @return
     */
    private static Image recomputeImage(double[][] pixelHeat, Graph graph, Gradient gradient, Double coldestHeatValue, Double hottestHeatValue) {
        if (pixelHeat == null) {
            return null;
        }

        BufferedImage image = new BufferedImage(pixelHeat.length, pixelHeat[0].length, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        double[] heatRange = getHeatRange(graph, coldestHeatValue, hottestHeatValue);

        for (int i = 0; i < pixelHeat.length; i++) {
            for (int j = 0; j < pixelHeat[0].length; j++) {
                double normalizedHeat = (pixelHeat[i][j] - heatRange[0]) / (heatRange[1] - heatRange[0]);
                normalizedHeat = Math.max(0, normalizedHeat);
                normalizedHeat = Math.min(1, normalizedHeat);
                Color pixelColor = gradient.get(normalizedHeat);
                if (pixelColor.getAlpha() > 0) {
                    graphics.setColor(pixelColor);
                    int transformedJ = pixelHeat[0].length - 1 - j;  // Java uses reverse coordinates for the y axis
                    graphics.fillRect(i, transformedJ, 1, 1);
                }
            }
        }
        return image;
    }

    /**
     * Computes the heat values corresponding to the coldest and hottest colors
     * for a given graph.
     *
     * @param graph the graph.
     * @param coldestHeatValue the set value of the coldest color, null if
     * dynamic.
     * @param hottestHeatValue the set value of the hottest color, null if
     * dynamic.
     * @return the heat range.
     */
    private static double[] getHeatRange(Graph graph, Double coldestHeatValue, Double hottestHeatValue) {
        NodeAttribute<Double> heatValues = graph.nodeAttribute(StdAttribute.nodeHeat);
        double graphColdest = Double.POSITIVE_INFINITY;
        double graphHottest = Double.NEGATIVE_INFINITY;
        if (coldestHeatValue == null || hottestHeatValue == null) {
            for (Node node : graph.nodes()) {
                double value = heatValues.get(node);
                graphColdest = Math.min(value, graphColdest);
                graphHottest = Math.max(value, graphHottest);
            }
        }
        double coldest = coldestHeatValue != null ? coldestHeatValue : graphColdest;
        double hottest = hottestHeatValue != null ? hottestHeatValue : graphHottest;
        return new double[]{coldest, hottest};
    }

    /**
     * Provides a gradient as a discrete list of colors.
     */
    public static class Gradient implements Iterable<Color> {

        private final List<Color> colors = new ArrayList<>();

        /**
         * Generates a gradient given a list of seed colors and the number of
         * gradient colors to be generated.
         *
         * @param seedColors the seeds colors.
         * @param gradientSize the number of gradient colors to be generated.
         */
        public Gradient(List<Color> seedColors, int gradientSize) {
            assert (seedColors.size() >= 2) : "The gradient requires at least two seed colors.";
            colors.add(seedColors.get(0));
            for (int i = 0; i < seedColors.size() - 1; i++) {
                Color left = seedColors.get(i);
                Color right = seedColors.get(i + 1);
                int emptyColors = gradientSize - colors.size();
                int colorsToAppend = emptyColors / (seedColors.size() - 1 - i);
                appendGradientColors(left, right, colorsToAppend);
            }
        }

        /**
         * Generates and appends gradient colors. The function appends the right
         * color, but not the left one.
         *
         * @param left the first seed color.
         * @param right the second seed color.
         * @param colorsToAppend the number of colors to append.
         */
        private void appendGradientColors(Color left, Color right, int colorsToAppend) {
            for (int i = 1; i <= colorsToAppend; i++) {
                double interpolationValue = (double) i / colorsToAppend;
                int red = interpolate(left.getRed(), right.getRed(), interpolationValue);
                int green = interpolate(left.getGreen(), right.getGreen(), interpolationValue);
                int blue = interpolate(left.getBlue(), right.getBlue(), interpolationValue);
                int alpha = interpolate(left.getAlpha(), right.getAlpha(), interpolationValue);
                colors.add(new Color(red, green, blue, alpha));
            }
        }

        /**
         * Interpolates a color value between the left value and the right one.
         *
         * @param left the left tone value.
         * @param right the right tone value.
         * @param interpolationValue the value indicating the distance from the
         * extremities, where 0 is left and 1 is right.
         * @return the interpolated value for the tone.
         */
        private static int interpolate(int left, int right, double interpolationValue) {
            return (int) (left + (right - left) * interpolationValue);
        }

        /**
         * Returns the gradient color with given normalized ratio.
         *
         * @param ratio the normalized ration in the range [0,1].
         * @return the color.
         */
        public Color get(double ratio) {
            int index = (int) (ratio * colors.size());
            index = Math.max(0, index);
            index = Math.min(colors.size() - 1, index);
            return colors.get(index);
        }

        @Override
        public Iterator<Color> iterator() {
            return colors.iterator();
        }

        /**
         * Returns the number of colors in the gradient.
         *
         * @return the number of gradient colors.
         */
        public int size() {
            return colors.size();
        }

    }

}
