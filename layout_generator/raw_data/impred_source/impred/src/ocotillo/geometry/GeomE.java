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

/**
 * Euclidean geometry interface.
 */
public interface GeomE {

    /**
     * Returns the number of dimensions considered in this geometry.
     *
     * @return the number of dimensions.
     */
    public int geomDim();

    /**
     * Checks if a number should be considered zero in this geometry.
     *
     * @param a the number.
     * @return true if the number is almost zero, false if not.
     */
    public boolean almostZero(double a);

    /**
     * Checks if a coordinates should be considered zero in this geometry.
     *
     * @param a the coordinates.
     * @return true if almost zero, false if not.
     */
    public boolean almostZero(Coordinates a);

    /**
     * Determines if two numbers should be considered equal in this geometry.
     *
     * @param a the first number.
     * @param b the second number.
     * @return true if they are almost equal, false if not.
     */
    public boolean almostEqual(double a, double b);

    /**
     * Checks if two coordinates should be considered equal in this geometry.
     *
     * @param a the first coordinates.
     * @param b the second coordinates.
     * @return true if almost equal, false if not.
     */
    public boolean almostEqual(Coordinates a, Coordinates b);

    /**
     * Computes the magnitude of a vector.
     *
     * @param vector the coordinates of a vector.
     * @return the vector's magnitude.
     */
    public double magnitude(Coordinates vector);

    /**
     * Returns the dot product of two vectors.
     *
     * @param vectorA the coordinates of the first vector.
     * @param vectorB the coordinates of the second vector.
     * @return the dot product.
     */
    public double dotProduct(Coordinates vectorA, Coordinates vectorB);

    /**
     * Creates the unit vector obtained by rescaling a given one.
     *
     * @param vector the vector.
     * @return its unit vector.
     */
    public Coordinates unitVector(Coordinates vector);

    /**
     * Creates the unit vector obtained by rescaling a given one.
     *
     * @param vector the vector.
     * @param origin the origin of the vector
     * @return its unit vector.
     */
    public Coordinates unitVector(Coordinates vector, Coordinates origin);

    /**
     * Scales a vector by a given factor.
     *
     * @param vector the vector.
     * @param factor the factor.
     * @return the scaled vector.
     */
    public Coordinates scaleVector(Coordinates vector, double factor);

    /**
     * Scales a vector with given origin by a given factor.
     *
     * @param vector the vector.
     * @param factor the factor.
     * @param origin the origin of the vector.
     * @return the scaled vector.
     */
    public Coordinates scaleVector(Coordinates vector, double factor, Coordinates origin);

    /**
     * Returns the amplitude of the angle between two vectors.
     *
     * @param vectorA the first vector.
     * @param vectorB the second vector.
     * @return the angle between them.
     */
    public double betweenAngle(Coordinates vectorA, Coordinates vectorB);

    /**
     * Returns the amplitude of the angle between two vectors with given origin.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @param origin the origin of the vectors.
     * @return the angle between them.
     */
    public double betweenAngle(Coordinates a, Coordinates b, Coordinates origin);

    /**
     * Computes the point in the middle of two other ones.
     *
     * @param a the first point.
     * @param b the second point.
     * @return the midpoint.
     */
    public Coordinates midPoint(Coordinates a, Coordinates b);

    /**
     * Computes a point in between two other ones. The point is computed as
     * (b-a)*ratio+a, therefore for ratio=0 returns a, for ratio=1 returns b,
     * and the result will be really in between the two points only for ratios
     * in (0,1).
     *
     * @param a the first point.
     * @param b the second point.
     * @param ratio the ratio of the distance a-b to consider.
     * @return the in between point at the desired distance from a.
     */
    public Coordinates inBetweenPoint(Coordinates a, Coordinates b, double ratio);

    /**
     * Verifies if a point belongs to a line.
     *
     * @param point the point.
     * @param linePointA the first point of the line.
     * @param linePointB the second point of the line.
     * @return true if the point belongs to the line, false if not.
     */
    public boolean isPointInLine(Coordinates point, Coordinates linePointA, Coordinates linePointB);

    /**
     * Verifies if a point belongs to a segment.
     *
     * @param point the point.
     * @param segmExtremityA the first extremity of the segment.
     * @param segmExtremityB the second extremity of the segment.
     * @return true if the point belongs to the segment, false if not.
     */
    public boolean isPointInSegment(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB);

    /**
     * Computes the relation between a point and a line.
     *
     * @param point the point.
     * @param linePointA the first point of the line.
     * @param linePointB the second point of the line.
     * @return the relation details.
     */
    public PointRelation pointLineRelation(Coordinates point, Coordinates linePointA, Coordinates linePointB);

    /**
     * Computes the relation between a point and a segment.
     *
     * @param point the point.
     * @param segmExtremityA the first extremity of the segment.
     * @param segmExtremityB the second extremity of the segment.
     * @return the relation details.
     */
    public PointRelation pointSegmentRelation(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB);

    /**
     * Computes the relation between two lines.
     *
     * @param line1PointA The first point of the first line.
     * @param line1PointB The second point of the first line.
     * @param line2PointA The first point of the second line.
     * @param line2PointB The second point of the second line.
     * @return the closest points.
     */
    public LineRelation lineLineRelation(Coordinates line1PointA, Coordinates line1PointB,
            Coordinates line2PointA, Coordinates line2PointB);

    /**
     * Gets the closest points between a line and a segment, whenever they are
     * univocally identifiable. Throws UndefinedSubspace when a line is
     * undefined as its points are coincident. Throws CollinearLinesException
     * when the lines are collinear.
     *
     * @param linePointA The first point of the line.
     * @param linePointB The second point of the line.
     * @param segmExtremityA the first point of the segment.
     * @param segmExtremityB the second point of the segment.
     * @return the closest points.
     */
    public LineRelation lineSegmentRelation(Coordinates linePointA, Coordinates linePointB,
            Coordinates segmExtremityA, Coordinates segmExtremityB);

    /**
     * Gets the closest points between two segments, whenever they are
     * univocally identifiable. Throws CollinearLinesException when the lines
     * are collinear.
     *
     * @param segm1PointA the first point of the first segment.
     * @param segm1PointB the second point of the first segment.
     * @param segm2PointA the first point of the second segment.
     * @param segm2PointB the second point of the second segment.
     * @return the closest points.
     */
    public LineRelation segmentSegmentRelation(Coordinates segm1PointA, Coordinates segm1PointB,
            Coordinates segm2PointA, Coordinates segm2PointB);

    /**
     * Collects the details about the relation between a point and a line or
     * segment.
     */
    public static class PointRelation {

        private final GeomE geometry;
        protected Coordinates closestPoint = null;
        protected Coordinates projection = null;
        protected Coordinates projectionAsLine = null;
        protected double distance = -1;
        protected double projectionDistance = -1;

        /**
         * Creates a point relation. This class is only intended to return the
         * computation of geometry methods, therefore the protected constructor.
         *
         * @param geometry the geometry to use.
         */
        protected PointRelation(GeomE geometry) {
            this.geometry = geometry;
        }

        /**
         * Returns the closest line or segment point to the given one, whenever
         * it exists and it is univocally identifiable.
         *
         * @return the closest line or segment point.
         */
        public Coordinates closestPoint() {
            return closestPoint;
        }

        /**
         * Gets the projection of the point in the line or segment, whenever it
         * exists and it is univocally identifiable
         *
         * @return the projection, or null if it is outside the segment.
         */
        public Coordinates projection() {
            return projection;
        }

        /**
         * Gets the projection of the point in the line or segment, whenever it
         * exists and it is univocally identifiable. Since the segment is also
         * interpreted as a line, the projection will be returned even when it
         * lays outside the segment.
         *
         * @return the projection.
         */
        public Coordinates projectionAsLine() {
            return projectionAsLine;
        }

        /**
         * The distance between the point and the segment or line.
         *
         * @return the point distance.
         */
        public double distance() {
            return distance;
        }

        /**
         * Checks if the point is in the line or segment.
         *
         * @return true if the point is in the line or segment, false otherwise.
         */
        public boolean isPointIncluded() {
            return geometry.almostZero(distance);
        }

        /**
         * Checks if the point projection is in the line or segment. This is
         * always true in a well defined line, but can be either true or false
         * in the case of a segment.
         *
         * @return true if the point is in the segment, false otherwise.
         */
        public boolean isProjectionIncluded() {
        	//TODO: canged here
        	if(projection() == null) return false;
            return geometry.almostZero(projectionDistance);
        }
    }

    /**
     * Collects the details about the relation between two lines or segments.
     */
    public static class LineRelation {

        private final GeomE geometry;
        protected Coordinates closestPointA = null;
        protected Coordinates closestPointB = null;
        protected Coordinates intersectionAsLines = null;
        protected Coordinates intersection = null;
        protected double distance = 0;
        protected Boolean areParallel = null;
        protected Boolean areOverlapping = false;
        protected Boolean areAdjacent = false;

        /**
         * Creates a lines/segments relation. This class is only intended to
         * return the computation of geometry methods, therefore the protected
         * constructor.
         *
         * @param geometry the geometry to use.
         */
        protected LineRelation(GeomE geometry) {
            this.geometry = geometry;
        }

        /**
         * Checks if we have an intersection by checking if the closest points
         * correspond.
         *
         * @return the intersection if it exists, null otherwise.
         */
        protected Coordinates intersectionFromClosestPoints() {
            if (closestPointA != null && closestPointB != null
                    && geometry.almostEqual(closestPointA, closestPointB)) {
                return closestPointA;
            } else {
                return null;
            }
        }

        /**
         * Returns the closest point of line or segment A, whenever it exists
         * and it is univocally identifiable.
         *
         * @return the closest point A.
         */
        public Coordinates closestPointA() {
            return closestPointA;
        }

        /**
         * Returns the closest point of line or segment B, whenever it exists
         * and it is univocally identifiable.
         *
         * @return the closest point B.
         */
        public Coordinates closestPointB() {
            return closestPointB;
        }

        /**
         * Returns the intersection point between the lines/segments, whenever
         * it exists and it is univocally identifiable.
         *
         * @return the line/segment intersection.
         */
        public Coordinates intersection() {
            return intersection;
        }

        /**
         * Returns the intersection point between the lines/segments, whenever
         * it exists and it is univocally identifiable. The eventual segments
         * are here interpreted as lines, therefore an intersection is returned
         * even if it lays outside the segments.
         *
         * @return the line/segment intersection as if they were lines.
         */
        public Coordinates intersectionAsLines() {
            return intersectionAsLines;
        }

        /**
         * The distance between the lines/segments.
         *
         * @return the lines/segments distance.
         */
        public double distance() {
            return distance;
        }

        /**
         * Checks if the lines/segments are disjoint, which mean they have no
         * common points.
         *
         * @return true if the lines/segments are disjoint, false otherwise.
         */
        public boolean areDisjoint() {
            return !geometry.almostZero(distance);
        }

        /**
         * Checks if the lines/segments have a uniquely identifiable
         * intersection point.
         *
         * @return true if the lines/segments intersect in a single point, false
         * otherwise.
         */
        public boolean areIntersecting() {
            return intersection != null;
        }

        /**
         * Checks if the lines/segments are parallel.
         *
         * @return true if the lines/segments are parallel, false otherwise.
         */
        public boolean areParallel() {
            if (areParallel == null) {
                throw new UndefinedSubspace("It was not possible to establish the parallelism, probably because a parameter segment had zero length.");
            }
            return areParallel;
        }

        /**
         * Checks if the lines/segments partially or totally overlap, which
         * means they share more than one point.
         *
         * @return true if the lines/segments overlap, false otherwise.
         */
        public boolean areOverlapping() {
            return areOverlapping;
        }

        /**
         * Checks if the lines/segments partially or totally overlap, which
         * means they share more than one point.
         *
         * @return true if the lines/segments overlap, false otherwise.
         */
        public boolean areAdjacent() {
            return areAdjacent;
        }

    }

    /**
     * Exception indicating that an intersection cannot be computed as two lines
     * or segments are parallel.
     */
    public static class UndefinedSubspace extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public UndefinedSubspace(String message) {
            super(message);
        }
    }

    /**
     * Exception indicating that a computation failed because two subspaces are
     * parallel. This exception is likely thrown when the parallelism this
     * condition prevents to detect the intersection between subspaces (e.g. the
     * intersection between two parallel line in the plane).
     */
    public static class ParallelSubspacesException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private Double distance = null;

        public ParallelSubspacesException(String message) {
            super(message);
        }

        public ParallelSubspacesException(String message, double distance) {
            super(message);
            this.distance = distance;
        }

        public Double getDistance() {
            return distance != null ? distance.doubleValue() : null;
        }
    }

    /**
     * Exception indicating that a computation failed because two subspaces
     * coincide.
     */
    public static class CoincidentSubspacesException extends ParallelSubspacesException {

        private static final long serialVersionUID = 1L;

        public CoincidentSubspacesException(String message) {
            super(message, 0);
        }

    }

}
