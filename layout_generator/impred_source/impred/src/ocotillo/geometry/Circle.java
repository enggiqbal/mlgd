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

import java.util.Collection;

/**
 * A circle.
 */
public class Circle {

    private final Coordinates center;
    private final double radius;

    /**
     * Constructs a circle.
     *
     * @param center the circle center.
     * @param radius the circle radius.
     */
    public Circle(Coordinates center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    /**
     * Copies a circle.
     *
     * @param otherCircle the other circle.
     */
    public Circle(Circle otherCircle) {
        this.center = new Coordinates(otherCircle.center);
        this.radius = otherCircle.radius;
    }

    /**
     * Gets the center.
     *
     * @return the center.
     */
    public Coordinates center() {
        return center;
    }

    /**
     * Gets the radius.
     *
     * @return the radius.
     */
    public double radius() {
        return radius;
    }

    /**
     * Gets the circle area.
     *
     * @return the area.
     */
    public double area() {
        return radius * radius * Math.PI;
    }

    /**
     * Gets the perimeter.
     *
     * @return the perimeter.
     */
    public double perimeter() {
        return 2 * Math.PI * radius;
    }

    /**
     * Constructs the smallest circle given two circumference points.
     *
     * @param A the first circumference point.
     * @param B the second circumference point.
     * @return the smallest circle that has the given points in the
     * circumference.
     */
    public static Circle fromCircPoints(Coordinates A, Coordinates B) {
        Coordinates center = Geom.e2D.midPoint(A, B);
        double radius = Geom.e2D.magnitude(A.minus(B)) / 2;
        return new Circle(center, radius);
    }

    /**
     * Constructs a circle given three circumference points.
     *
     * @param A the first circumference point.
     * @param B the second circumference point.
     * @param C the third circumference point.
     * @return the circle that has the given points in the circumference.
     */
    public static Circle fromCircPoints(Coordinates A, Coordinates B, Coordinates C) {
        double offset = Math.pow(B.x(), 2) + Math.pow(B.y(), 2);
        double bc = (Math.pow(A.x(), 2) + Math.pow(A.y(), 2) - offset) / 2.0;
        double cd = (offset - Math.pow(C.x(), 2) - Math.pow(C.y(), 2)) / 2.0;
        double det = (A.x() - B.x()) * (B.y() - C.y()) - (B.x() - C.x()) * (A.y() - B.y());

        if (Geom.e2D.almostZero(det)) {
            throw new IllegalArgumentException("The points are collinear");
        }

        double x = (bc * (B.y() - C.y()) - cd * (A.y() - B.y())) / det;
        double y = (cd * (A.x() - B.x()) - bc * (B.x() - C.x())) / det;
        double radius = Math.sqrt(Math.pow(B.x() - x, 2) + Math.pow(B.y() - y, 2));
        return new Circle(new Coordinates(x, y), radius);
    }

    /**
     * Constructs the smallest circle containing all given points.
     *
     * @param points the points.
     * @return the smallest circle that contains the points.
     */
    public static Circle boundingCircle(Collection<Coordinates> points) {
        Coordinates[] pointArray = (Coordinates[]) points.toArray();
        return smallestEnclosingCircle(pointArray, points.size(), new Coordinates[3], 0);
    }

    /**
     * Constructs the smallest circle containing all given points.
     *
     * @param points the points.
     * @return the smallest circle that contains the points.
     */
    public static Circle boundingCircle(Coordinates... points) {
        return smallestEnclosingCircle(points, points.length, new Coordinates[3], 0);
    }

    /**
     * Identifies the smallest enclosing circle for the given parameters.
     *
     * @param points the points to be contained.
     * @param pointsToConsider the number of points to consider.
     * @param circumPoints the points contained in the circumference.
     * @param circumPointsToConsider the number of circumference points to
     * consider.
     * @return the smallest enclosing circle for the given parameters.
     */
    private static Circle smallestEnclosingCircle(Coordinates[] points, int pointsToConsider, Coordinates[] circumPoints, int circumPointsToConsider) {
        Circle enclosingCircle = new Circle(new Coordinates(0, 0), 0);

        switch (circumPointsToConsider) {
            case 1:
                enclosingCircle = new Circle(circumPoints[0], 0);
                break;
            case 2:
                enclosingCircle = fromCircPoints(circumPoints[0], circumPoints[1]);
                break;
            case 3:
                return fromCircPoints(circumPoints[0], circumPoints[1], circumPoints[2]);
        }

        for (int i = 0; i < pointsToConsider; i++) {
            if (!enclosingCircle.contains(points[i])) {
                circumPoints[circumPointsToConsider] = new Coordinates(points[i]);
                enclosingCircle = smallestEnclosingCircle(points, i, circumPoints, circumPointsToConsider + 1);
            }
        }
        return enclosingCircle;
    }

    /**
     * Checks if the circle contains a point. The check is performed in the
     * first two coordinates, ignoring any exceeding dimensions.
     *
     * @param point the point.
     * @return true if the circle contains the point, false otherwise.
     */
    public boolean contains(Coordinates point) {
        return Geom.e2D.magnitude(center.restrMinus(point, 2)) <= radius;
    }

    /**
     * Checks if the circle circumference contains a point.
     *
     * @param point the point.
     * @return true if the circumference contains the point, false otherwise.
     */
    public boolean isPointInCircumference(Coordinates point) {
        return Geom.e2D.almostEqual(Geom.e2D.magnitude(center.restrMinus(point, 2)), radius);
    }
}
