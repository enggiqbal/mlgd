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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Euclidean geometry that operates on 2D vectorial spaces. Unless otherwise
 * specified, it should be assumed that any dimension exceeding the first two is
 * ignored by the methods of this class and dropped from the returned results.
 */
public class Geom2D extends GeomXD {

    @Override
    public int geomDim() {
        return 2;
    }

    /**
     * Returns the angle (in radians) of the vector with given coordinates.
     *
     * @param vector the coordinates of the vector.
     * @return the calculated angle.
     */
    public double angle(Coordinates vector) {
        double angle = -1.0;
        double x = vector.x();
        double y = vector.y();
        if (x > 0 && y >= 0) {
            angle = Math.atan(y / x);
        } else if (x > 0 && y < 0) {
            angle = Math.atan(y / x) + 2.0 * Math.PI;
        } else if (x < 0) {
            angle = Math.atan(y / x) + Math.PI;
        } else if (almostZero(x) && y > 0) {
            angle = Math.PI / 2.0;
        } else if (almostZero(x) && y < 0) {
            angle = Math.PI * 3.0 / 2.0;
        }
        return angle;
    }

    /**
     * Creates the unit vector of the plane with given angle (in radians).
     *
     * @param angle the angle.
     * @return the unit vector.
     */
    public Coordinates unitVector(double angle) {
        return new Coordinates(Math.cos(angle), Math.sin(angle));
    }

    /**
     * Creates the unit vector of the plane with given angle (in radians). The
     * result will have the same values of the origin for dimensions exceeding
     * the first two.
     *
     * @param angle the angle.
     * @param origin the origin of the vector
     * @return the unit vector.
     */
    public Coordinates unitVector(double angle, Coordinates origin) {
        return unitVector(angle).plusIP(origin);
    }

    /**
     * Rotates a vector by the given angle (in radians). This method can be
     * applied to high dimensional vectors without it changing the third or
     * further dimensions.
     *
     * @param vector the vector.
     * @param angle the angle.
     * @return the rotated vector.
     */
    public Coordinates rotateVector(Coordinates vector, double angle) {
        double originalAngle = angle(vector);
        double magnitude = magnitude(vector);
        return vector.withHead(unitVector(originalAngle + angle).timesIP(magnitude));
    }

    /**
     * Rotates a vector by the given angle (in radians). This method can be
     * applied to high dimensional vectors without it changing the third or
     * further dimensions.
     *
     * @param vector the vector.
     * @param angle the angle.
     * @param origin the origin of the vector.
     * @return the rotated vector.
     */
    public Coordinates rotateVector(Coordinates vector, double angle, Coordinates origin) {
        return rotateVector(vector.minus(origin), angle).plusIP(origin);
    }

    /**
     * Returns the unit vector that lays in the bisector of two vectors. The
     * resulting vector always lays on the smallest angle.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @return the unit vector on the bisector.
     */
    public Coordinates bisector(Coordinates a, Coordinates b) {
        double angleA = angle(a);
        double angleB = angle(b);
        double bisecAngle = (angleA + angleB) / 2;
        if (Math.abs(GeomNumeric.normalizeRadiansAngle(bisecAngle - angleA)) > Math.PI / 2) {
            bisecAngle = bisecAngle + Math.PI;
        }
        return unitVector(bisecAngle);
    }

    /**
     * Returns the unit vector that lays in the bisector of two vectors. The
     * resulting vector always lays on the smallest angle.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @param origin the origin of the vectors.
     * @return the unit vector with given origin on the bisector.
     */
    public Coordinates bisector(Coordinates a, Coordinates b, Coordinates origin) {
        return bisector(a.minus(origin), b.minus(origin)).plusIP(origin);
    }

    /**
     * Return the position of a point p with respect to a vector from a to b.
     *
     * @param p the point.
     * @param a the vector origin.
     * @param b the vector target.
     * @return the position of p compared to ab.
     */
    public Turn pointPositionToVector(Coordinates p, Coordinates a, Coordinates b) {
        double dxAB = b.x() - a.x();
        double dyAB = b.y() - a.y();
        double dxBP = p.x() - b.x();
        double dyBP = p.y() - b.y();
        double zOfCrossProd = dxAB * dyBP - dyAB * dxBP;
        if (almostZero(zOfCrossProd)) {
            return Turn.Collinear;
        } else if (zOfCrossProd > 0) {
            return Turn.Left;
        } else {
            return Turn.Right;
        }
    }

    /**
     *
     * Verifies if the polygon is simple.
     *
     * @param polygon the polygon.
     * @return true if the polygon is simple, false if not.
     */
    public boolean isPolygonSimple(Polygon polygon) {
        if (polygon.size() < 3) {
            return false;
        }

        try {
            for (int i = 0; i < polygon.size(); i++) {
                for (int j = i + 1; j < polygon.size(); j++) {
                    Coordinates aSource = polygon.get(i);
                    Coordinates aTarget = polygon.get((i + 1) % polygon.size());
                    Coordinates bSource = polygon.get(j);
                    Coordinates bTarget = polygon.get((j + 1) % polygon.size());
                    LineRelation segmRelation = segmentSegmentRelation(aSource, aTarget, bSource, bTarget);
                    Coordinates intersection = segmRelation.intersection();

                    if (!segmRelation.areIntersecting()) {
                        continue;
                    }

                    boolean aConsecutiveB = almostEqual(aTarget, intersection)
                            && almostEqual(bSource, intersection);
                    boolean bConsecutiveA = almostEqual(bTarget, intersection)
                            && almostEqual(aSource, intersection);

                    if (!aConsecutiveB && !bConsecutiveA) {
                        return false;
                    }
                }
            }
        } catch (ParallelSubspacesException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a polygon is convex.
     *
     * @param polygon the polygon.
     * @return true if it is convex, false otherwise.
     */
    public boolean isPolygonConvex(Polygon polygon) {
        Turn typeOfTurn = null;
        for (int i = 0; i < polygon.size(); i++) {
            Coordinates a = polygon.get(i);
            Coordinates b = polygon.get((i + 1) % polygon.size());
            Coordinates p = polygon.get((i + 2) % polygon.size());
            Turn currentTurn = pointPositionToVector(p, a, b);
            if (currentTurn != Turn.Collinear) {
                if (typeOfTurn == null) {
                    typeOfTurn = currentTurn;
                } else if (typeOfTurn != currentTurn) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the area of a simple polygon (Shoelace formula).
     *
     * @param polygon the polygon.
     * @return the area of the polygon.
     */
    public double polygonArea(Polygon polygon) {
        return Math.abs(polygonSignedArea(polygon));
    }

    /**
     * Gets the signed area of a simple polygon (Shoelace formula). The area is
     * positive when the polygon is provided in anticlockwise order, negative
     * otherwise.
     *
     * @param polygon the polygon.
     * @return the area of the polygon.
     */
    public double polygonSignedArea(Polygon polygon) {
        if (polygon.size() < 3) {
            return 0.0;
        }
        assert (isPolygonSimple(polygon)) : "The polygon must be simple.";

        double firstSum = 0.0;
        double secondSum = 0.0;
        Coordinates previousPoint = polygon.get(polygon.size() - 1);
        for (Coordinates currentPoint : polygon) {
            firstSum += previousPoint.x() * currentPoint.y();
            secondSum += currentPoint.x() * previousPoint.y();
            previousPoint = currentPoint;
        }
        return (secondSum - firstSum) / 2.0;
    }

    /**
     * Computes the perimeter of the given polygon.
     *
     * @param polygon the polygon.
     * @return the perimeter of the polygon.
     */
    public double polygonPerimeter(Polygon polygon) {
        double perimeter = 0;
        Coordinates previousPoint = polygon.get(polygon.size() - 1);
        for (Coordinates currentPoint : polygon) {
            perimeter += magnitude(currentPoint.restrMinus(previousPoint, 2));
            previousPoint = currentPoint;
        }
        return perimeter;
    }

    /**
     * Computes the isoperimetric quotient of a polygon. The isoperimetric
     * quotient is the ratio between the area of the polygon and that of a
     * circle with equal circumference.
     *
     * @param polygon the polygon.
     * @return the polygon isoperimetric quotient.
     */
    public double isoperimetricQuotient(Polygon polygon) {
        double perimeter = polygonPerimeter(polygon);
        double area = polygonArea(polygon);
        return 4 * Math.PI * area / (perimeter * perimeter);
    }

    /**
     * Returns the centroid of a polygon.
     *
     * @param polygon the polygon.
     * @return its centroid.
     */
    public Coordinates polygonCentroid(Polygon polygon) {
        double x = 0;
        double y = 0;
        for (int i = 0; i < polygon.size(); i++) {
            int j = (i + 1) % polygon.size();
            double factor = polygon.get(i).y() * polygon.get(j).x() - polygon.get(i).x() * polygon.get(j).y();
            x += (polygon.get(i).x() + polygon.get(j).x()) * factor;
            y += (polygon.get(i).y() + polygon.get(j).y()) * factor;
        }
        return (new Coordinates(x, y)).divide(6 * polygonSignedArea(polygon));
    }

    /**
     * Verifies if the point is in the polygon boundary.
     *
     * @param point the point.
     * @param polygon the polygon.
     * @return true if point is in the polygon boundary, false otherwise.
     */
    public boolean isPointInPolygonBoundary(Coordinates point, Polygon polygon) {
        Coordinates previousPoint = polygon.get(polygon.size() - 1);
        for (Coordinates currentPoint : polygon) {
            if (isPointInSegment(point, previousPoint, currentPoint)) {
                return true;
            }
            previousPoint = currentPoint;
        }
        return false;
    }

    /**
     * Verifies if the point is inside a polygon.
     *
     * @param point the point.
     * @param polygon the polygon.
     * @return true if the point is in the polygon or its boundary, false
     * otherwise.
     */
    public boolean isPointInPolygon(Coordinates point, Polygon polygon) {
        if (isPointInPolygonBoundary(point, polygon)) {
            return true;
        }

        try {
            double x = Collections.max(polygon, new Coordinates.LeftMostComparator()).x() + GeomNumeric.randomDouble(10, 20);
            double y = Collections.max(polygon, new Coordinates.BottomMostComparator()).y() + GeomNumeric.randomDouble(10, 20);
            Coordinates farAwayPoint = new Coordinates(x, y);

            int crossingsCount = 0;
            Coordinates previousPoint = polygon.get(polygon.size() - 1);
            for (Coordinates currentPoint : polygon) {
                Coordinates intersection = segmentSegmentRelation(point, farAwayPoint, previousPoint, currentPoint).intersection();
                if (intersection != null) {
                    boolean countingTwiceForZeroLengthEdge = almostEqual(previousPoint, currentPoint);
                    boolean countingTwiceForCrossingAtEdgeExtremity = almostEqual(intersection, previousPoint);
                    if (!countingTwiceForZeroLengthEdge && !countingTwiceForCrossingAtEdgeExtremity) {
                        crossingsCount++;
                    }
                }
                previousPoint = currentPoint;
            }
            return (crossingsCount % 2 == 1);

        } catch (ParallelSubspacesException e) {
            return isPointInPolygon(point, polygon);
        }
    }

    /**
     * Get the convex hull for a set of points (Graham scan).
     *
     * @param points the points.
     * @return the convex hull.
     */
    public Polygon convexHull(List<Coordinates> points) {
        if (points.size() <= 1) {
            return new Polygon(points);
        }

        Polygon convexHull = new Polygon();
        Coordinates bottomMost = Collections.min(points, new Coordinates.BottomMostComparator());
        convexHull.add(bottomMost);

        List<Coordinates> otherPoints = new ArrayList<>();
        for (Coordinates point : points) {
            if (!almostEqual(bottomMost, point)) {
                otherPoints.add(point);
            }
        }

        if (otherPoints.isEmpty()) {
            return convexHull;
        }

        Collections.sort(otherPoints, new AngleComparator(bottomMost));
        convexHull.add(otherPoints.get(0));

        Coordinates[] pastEdge = {convexHull.get(0), convexHull.get(convexHull.size() - 1)};
        for (Coordinates point : otherPoints) {
            if (!almostEqual(point, pastEdge[1])) {
                while (getTurn(pastEdge, point) == Turn.Right) {
                    convexHull.remove(convexHull.size() - 1);
                    pastEdge[0] = convexHull.get(Math.max(convexHull.size() - 2, 0));
                    pastEdge[1] = convexHull.get(convexHull.size() - 1);
                }
                if (getTurn(pastEdge, point) == Turn.Collinear) {
                    convexHull.remove(convexHull.size() - 1);
                }
                convexHull.add(point);
                pastEdge[0] = convexHull.get(convexHull.size() - 2);
                pastEdge[1] = convexHull.get(convexHull.size() - 1);
            }
        }
        return convexHull;
    }

    /**
     * Detects the kind of turn performed considering the last convex hull edge
     * previously inserted and the new point to be evaluated.
     *
     * @param edge the last edge inserted in the convex hull.
     * @param point the new point to be evaluated.
     * @return the kind of turn.
     */
    private Turn getTurn(Coordinates[] edge, Coordinates point) {
        double crossProductZ = (edge[1].x() - edge[0].x()) * (point.y() - edge[0].y())
                - (edge[1].y() - edge[0].y()) * (point.x() - edge[0].x());

        if (almostZero(crossProductZ)) {
            return Turn.Collinear;
        }

        return crossProductZ > 0 ? Turn.Left : Turn.Right;
    }

    /**
     * Computes the intersection between two convex polygons.
     *
     * @param first the first polygon.
     * @param second the second polygon.
     * @return their intersection.
     */
    public Polygon convexIntersection(Polygon first, Polygon second) {
        assert (isPolygonConvex(first) && isPolygonConvex(second)) : "The input polygons are not convex.";
        Polygon clipPolygon = first;
        if (polygonSignedArea(clipPolygon) > 0) {
            clipPolygon = new Polygon(first);
            Collections.reverse(clipPolygon);
        }
        Polygon intersection = second;
        for (int i = 0; i < clipPolygon.size(); i++) {
            Coordinates a = clipPolygon.get(i);
            Coordinates b = clipPolygon.get((i + 1) % clipPolygon.size());
            intersection = clipConvexPolygonWithLine(intersection, a, b);
        }
        return intersection;
    }

    /**
     * Clips a convex polygon with a line, considering inside the half plane at
     * its left.
     *
     * @param subject the subject polygon.
     * @param a the first line point.
     * @param b the second line point.
     * @return the polygon at the left of the line from a to b.
     */
    private Polygon clipConvexPolygonWithLine(Polygon subject, Coordinates a, Coordinates b) {
        Polygon result = new Polygon();
        for (int i = 0; i < subject.size(); i++) {
            Coordinates r = subject.get(i);
            Coordinates s = subject.get((i + 1) % subject.size());
            boolean rInside = pointPositionToVector(r, a, b) != Turn.Right;
            boolean sInside = pointPositionToVector(s, a, b) != Turn.Right;
            if (rInside && sInside) {
                result.add(s);
            } else if (rInside && !sInside) {
                result.add(lineLineRelation(a, b, r, s).intersection());
            } else if (!rInside && sInside) {
                result.add(lineLineRelation(a, b, r, s).intersection());
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Indicate the kind of turn or position with respect to a line.
     */
    public enum Turn {

        Left,
        Right,
        Collinear
    }

    /**
     * Orders Coordinates according to increasing angles.
     */
    public class AngleComparator implements Comparator<Coordinates> {

        private Coordinates reference = new Coordinates(0.0, 0.0);

        public AngleComparator() {
        }

        public AngleComparator(Coordinates customReference) {
            reference = customReference;
        }

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(angle(a.minus(reference)), angle(b.minus(reference)));
        }
    }

}
