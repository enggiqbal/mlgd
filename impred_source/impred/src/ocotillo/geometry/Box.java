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
package ocotillo.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * A box.
 */
@EqualsAndHashCode
public class Box {

    private final double[] leftBounds;
    private final double[] rightBounds;

    /**
     * Builds a box of given dimension.
     *
     * @param leftBounds the left bounds.
     * @param rightBounds the right bounds.
     */
    public Box(double[] leftBounds, double[] rightBounds) {
        assert (leftBounds.length == rightBounds.length) : "Bounds of different dimension";
        this.leftBounds = leftBounds;
        this.rightBounds = rightBounds;
    }

    /**
     * Builds a box by copying another one.
     *
     * @param other the other box.
     */
    public Box(Box other) {
        this.leftBounds = other.leftBounds;
        this.rightBounds = other.rightBounds;
    }

    /**
     * Checks if the left bounds are smaller or equal to the corresponding right
     * ones.
     *
     * @return true if the bounds are valid, false otherwise.
     */
    public boolean isValid() {
        for (int i = 0; i < dimensions(); i++) {
            if (rightBounds[i] < leftBounds[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of dimensions for this box.
     *
     * @return the box dimensions.
     */
    public int dimensions() {
        return leftBounds.length;
    }

    /**
     * Returns the left bound.
     *
     * @return the left bound.
     */
    public double left() {
        if (dimensions() > 0) {
            return leftBounds[0];
        }
        throw new IllegalStateException("The dimension idx required (" + 0 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the right bound.
     *
     * @return the right bound.
     */
    public double right() {
        if (dimensions() > 0) {
            return rightBounds[0];
        }
        throw new IllegalStateException("The dimension idx required (" + 0 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the bottom bound.
     *
     * @return the bottom bound.
     */
    public double bottom() {
        if (dimensions() > 1) {
            return leftBounds[1];
        }
        throw new IllegalStateException("The dimension idx required (" + 1 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the top bound.
     *
     * @return the top bound.
     */
    public double top() {
        if (dimensions() > 1) {
            return rightBounds[1];
        }
        throw new IllegalStateException("The dimension idx required (" + 1 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the near bound.
     *
     * @return the near bound.
     */
    public double near() {
        if (dimensions() > 2) {
            return leftBounds[2];
        }
        throw new IllegalStateException("The dimension idx required (" + 2 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Returns the far bound.
     *
     * @return the far bound.
     */
    public double far() {
        if (dimensions() > 2) {
            return rightBounds[2];
        }
        throw new IllegalStateException("The dimension idx required (" + 2 + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Gets the left bound for the given dimension.
     *
     * @param dimension the dimension index.
     * @return the left bound for that dimension.
     */
    public double leftBound(int dimension) {
        if (dimensions() > dimension) {
            return leftBounds[dimension];
        }
        throw new IllegalStateException("The dimension idx required (" + dimension + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Gets the right bound for the given dimension.
     *
     * @param dimension the dimension index.
     * @return the right bound for that dimension.
     */
    public double rightBound(int dimension) {
        if (dimensions() > dimension) {
            return rightBounds[dimension];
        }
        throw new IllegalStateException("The dimension idx required (" + dimension + ") exceeds the box dimension (" + dimensions() + ")");
    }

    /**
     * Gets the coordinates of the centre of the box.
     *
     * @return the centre.
     */
    public Coordinates center() {
        Coordinates center = new Coordinates(dimensions());
        for (int i = 0; i < dimensions(); i++) {
            center.setAt(i, (leftBounds[i] + rightBounds[i]) / 2);
        }
        return center;
    }

    /**
     * Gets the dimensions of the box in coordinate form.
     *
     * @return the box dimensions.
     */
    public Coordinates size() {
        Coordinates center = new Coordinates(dimensions());
        for (int i = 0; i < dimensions(); i++) {
            center.setAt(i, rightBounds[i] - leftBounds[i]);
        }
        return center;
    }

    /**
     * Computes the width of the box.
     *
     * @return the width.
     */
    public double width() {
        return right() - left();
    }

    /**
     * Computes the height of the box.
     *
     * @return the height.
     */
    public double height() {
        return top() - bottom();
    }

    /**
     * Computes the depth of the box.
     *
     * @return the height.
     */
    public double depth() {
        return far() - near();
    }

    /**
     * Returns the largest of the two box dimensions.
     *
     * @return the largest dimension.
     */
    public double maxDim() {
        double max = Double.NEGATIVE_INFINITY;
        Coordinates size = size();
        for (int i = 0; i < size.dim(); i++) {
            max = Math.max(max, size.get(i));
        }
        return max;
    }

    /**
     * Returns the smallest of the two box dimensions.
     *
     * @return the smallest dimension.
     */
    public double minDim() {
        double min = Double.POSITIVE_INFINITY;
        Coordinates size = size();
        for (int i = 0; i < size.dim(); i++) {
            min = Math.min(min, size.get(i));
        }
        return min;
    }

    /**
     * Scales a box by a given factor.
     *
     * @param factor the scaling factor.
     * @return the resulting box.
     */
    public Box scale(double factor) {
        double[] newLeftBounds = new double[dimensions()];
        double[] newRightBounds = new double[dimensions()];
        for (int i = 0; i < dimensions(); i++) {
            newLeftBounds[i] = leftBounds[i] * factor;
            newRightBounds[i] = rightBounds[i] * factor;
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Scales a box by the given factors.
     *
     * @param factors the scaling factors.
     * @return the resulting box.
     */
    public Box scale(Coordinates factors) {
        double[] newLeftBounds = new double[dimensions()];
        double[] newRightBounds = new double[dimensions()];
        for (int i = 0; i < dimensions(); i++) {
            newLeftBounds[i] = leftBounds[i] * factors.get(i);
            newRightBounds[i] = rightBounds[i] * factors.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Expands a box by the given margin.
     *
     * @param margin the margin.
     * @return the resulting box.
     */
    public Box expand(double margin) {
        double[] newLeftBounds = new double[dimensions()];
        double[] newRightBounds = new double[dimensions()];
        for (int i = 0; i < dimensions(); i++) {
            newLeftBounds[i] = leftBounds[i] - margin;
            newRightBounds[i] = rightBounds[i] + margin;
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Expands a box by the given margins.
     *
     * @param margins the margins.
     * @return the resulting box.
     */
    public Box expand(Coordinates margins) {
        double[] newLeftBounds = new double[dimensions()];
        double[] newRightBounds = new double[dimensions()];
        for (int i = 0; i < dimensions(); i++) {
            newLeftBounds[i] = leftBounds[i] - margins.get(i);
            newRightBounds[i] = rightBounds[i] + margins.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Shifts a box by a given vector.
     *
     * @param movement the movement vector.
     * @return the resulting box.
     */
    public Box shift(Coordinates movement) {
        double[] newLeftBounds = new double[dimensions()];
        double[] newRightBounds = new double[dimensions()];
        for (int i = 0; i < dimensions(); i++) {
            newLeftBounds[i] = leftBounds[i] + movement.get(i);
            newRightBounds[i] = rightBounds[i] + movement.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Combines this box with another one.
     *
     * @param box the other box.
     * @return the combination of the boxes.
     */
    public Box combine(Box box) {
        return combine(Arrays.asList(this, box));
    }

    /**
     * Computes the intersection of this box with another one.
     *
     * @param box the other box.
     * @return null if they do not intersect, the intersection box otherwise.
     */
    public Box intersect(Box box) {
        return intersect(Arrays.asList(this, box));
    }

    @Override
    public String toString() {
        String result = "<";
        for (int i = 0; i < dimensions(); i++) {
            result += i + "=(" + leftBounds[i] + "," + rightBounds[i] + ") ";
        }
        return result.substring(0, result.length() - 1) + ">";
    }

    /**
     * Verifies if a point is inside a box.
     *
     * @param point the point.
     * @return true if the point is in the box, false otherwise.
     */
    public boolean contains(Coordinates point) {
        for (int i = 0; i < dimensions(); i++) {
            if (point.get(i) < leftBounds[i] || rightBounds[1] < point.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies if a collection of points are all inside a box.
     *
     * @param points the points.
     * @return true if all the point are in the box, false otherwise.
     */
    public boolean contains(Collection<Coordinates> points) {
        for (Coordinates point : points) {
            if (!contains(point)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the bounding box of the points.
     *
     * @param points the points.
     * @return their bounding box.
     */
    public static Box boundingBox(Coordinates... points) {
        return boundingBox(Arrays.asList(points));
    }

    /**
     * Returns the bounding box of the points.
     *
     * @param points the points.
     * @return their bounding box.
     */
    public static Box boundingBox(Collection<Coordinates> points) {
        return boundingBox(points, 0.0);
    }

    /**
     * Returns the bounding box of the points, inserting a margin around them.
     *
     * @param points the points.
     * @param margin the margin to consider around each point.
     * @return their bounding box.
     */
    public static Box boundingBox(Collection<Coordinates> points, double margin) {
        List<Double> leftBounds = new ArrayList<>();
        List<Double> rightBounds = new ArrayList<>();
        for (Coordinates point : points) {
            for (int i = 0; i < point.dim(); i++) {
                while (leftBounds.size() <= i) {
                    leftBounds.add(Double.POSITIVE_INFINITY);
                    rightBounds.add(Double.NEGATIVE_INFINITY);
                }
                leftBounds.set(i, Math.min(leftBounds.get(i), point.get(i) - margin));
                rightBounds.set(i, Math.max(rightBounds.get(i), point.get(i) + margin));
            }
        }
        double[] newLeftBounds = new double[leftBounds.size()];
        double[] newRightBounds = new double[rightBounds.size()];
        for (int i = 0; i < leftBounds.size(); i++) {
            newLeftBounds[i] = leftBounds.get(i);
            newRightBounds[i] = rightBounds.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Returns the bounding box of the points, inserting a margin around them.
     *
     * @param points the points.
     * @param margins the margins to consider around each point.
     * @return their bounding box.
     */
    public static Box boundingBox(Collection<Coordinates> points, Coordinates margins) {
        List<Double> leftBounds = new ArrayList<>();
        List<Double> rightBounds = new ArrayList<>();
        for (Coordinates point : points) {
            for (int i = 0; i < point.dim(); i++) {
                while (leftBounds.size() <= i) {
                    leftBounds.add(Double.POSITIVE_INFINITY);
                    rightBounds.add(Double.NEGATIVE_INFINITY);
                }
                leftBounds.set(i, Math.min(leftBounds.get(i), point.get(i) - margins.get(i)));
                rightBounds.set(i, Math.max(rightBounds.get(i), point.get(i) + margins.get(i)));
            }
        }
        double[] newLeftBounds = new double[leftBounds.size()];
        double[] newRightBounds = new double[rightBounds.size()];
        for (int i = 0; i < leftBounds.size(); i++) {
            newLeftBounds[i] = leftBounds.get(i);
            newRightBounds[i] = rightBounds.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Combine multiple boxes into one.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static Box combine(Box... boxes) {
        return combine(Arrays.asList(boxes));
    }

    /**
     * Combine multiple boxes into one.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static Box combine(Collection<Box> boxes) {
        List<Double> leftBounds = new ArrayList<>();
        List<Double> rightBounds = new ArrayList<>();
        for (Box box : boxes) {
            if (box != null) {
                for (int i = 0; i < box.dimensions(); i++) {
                    while (leftBounds.size() <= i) {
                        leftBounds.add(Double.POSITIVE_INFINITY);
                        rightBounds.add(Double.NEGATIVE_INFINITY);
                    }
                    leftBounds.set(i, Math.min(leftBounds.get(i), box.leftBounds[i]));
                    rightBounds.set(i, Math.max(rightBounds.get(i), box.rightBounds[i]));
                }
            }
        }
        double[] newLeftBounds = new double[leftBounds.size()];
        double[] newRightBounds = new double[rightBounds.size()];
        for (int i = 0; i < leftBounds.size(); i++) {
            newLeftBounds[i] = leftBounds.get(i);
            newRightBounds[i] = rightBounds.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * Computes the intersection of multiple boxes.
     *
     * @param boxes the boxes.
     * @return the resulting one.
     */
    public static Box intersect(Box... boxes) {
        return intersect(Arrays.asList(boxes));
    }

    /**
     * Computes the intersection of multiple boxes.
     *
     * @param boxes the boxes.
     * @return null if the boxes do not have a region shared by all of them, the
     * intersection box otherwise.
     */
    public static Box intersect(Collection<Box> boxes) {
        List<Double> leftBounds = new ArrayList<>();
        List<Double> rightBounds = new ArrayList<>();
        for (Box box : boxes) {
            if (box != null) {
                for (int i = 0; i < box.dimensions(); i++) {
                    while (leftBounds.size() <= i) {
                        leftBounds.add(Double.NEGATIVE_INFINITY);
                        rightBounds.add(Double.POSITIVE_INFINITY);
                    }
                    leftBounds.set(i, Math.max(leftBounds.get(i), box.leftBounds[i]));
                    rightBounds.set(i, Math.min(rightBounds.get(i), box.rightBounds[i]));
                }
            }
        }
        double[] newLeftBounds = new double[leftBounds.size()];
        double[] newRightBounds = new double[rightBounds.size()];
        for (int i = 0; i < leftBounds.size(); i++) {
            if (rightBounds.get(i) < leftBounds.get(i)) {
                return null;
            }
            newLeftBounds[i] = leftBounds.get(i);
            newRightBounds[i] = rightBounds.get(i);
        }
        return new Box(newLeftBounds, newRightBounds);
    }

    /**
     * A 2D box.
     */
    public static class Box2D extends Box {

        /**
         * Constructs a box.
         *
         * @param left the left most x value.
         * @param right the right most x value.
         * @param bottom the bottom most y value.
         * @param top the top most y value.
         */
        public Box2D(double left, double right, double bottom, double top) {
            super(
                    new double[]{left, bottom},
                    new double[]{right, top}
            );
        }

        /**
         * Constructs a box.
         *
         * @param center the box centre.
         * @param size the box size.
         */
        public Box2D(Coordinates center, double size) {
            super(
                    new double[]{center.x() - size / 2, center.y() - size / 2},
                    new double[]{center.x() + size / 2, center.y() + size / 2}
            );
        }

        /**
         * Constructs a box.
         *
         * @param center the box centre.
         * @param size the box size.
         */
        public Box2D(Coordinates center, Coordinates size) {
            super(
                    new double[]{center.x() - size.x() / 2, center.y() - size.y() / 2},
                    new double[]{center.x() + size.x() / 2, center.y() + size.y() / 2}
            );
        }

        /**
         * Gets the coordinates of the bottom left corner.
         *
         * @return the bottom left corner.
         */
        public Coordinates bottomLeft() {
            return new Coordinates(left(), bottom());
        }

        /**
         * Gets the coordinates of the top left corner.
         *
         * @return the top left corner.
         */
        public Coordinates topLeft() {
            return new Coordinates(left(), top());
        }

        /**
         * Gets the coordinates of the bottom right corner.
         *
         * @return the bottom right corner.
         */
        public Coordinates bottomRight() {
            return new Coordinates(right(), bottom());
        }

        /**
         * Gets the coordinates of the top right corner.
         *
         * @return the top right corner.
         */
        public Coordinates topRight() {
            return new Coordinates(right(), top());
        }
    }

    /**
     * A 3D box.
     */
    public static class Box3D extends Box {

        /**
         * Constructs a box.
         *
         * @param left the left most x value.
         * @param right the right most x value.
         * @param bottom the bottom most y value.
         * @param top the top most y value.
         * @param near the nearest z value.
         * @param far the farthest z value.
         */
        public Box3D(double left, double right, double bottom, double top, double near, double far) {
            super(
                    new double[]{left, bottom, near},
                    new double[]{right, top, far}
            );
        }
    }
}
