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

import java.util.Comparator;

/**
 * Euclidean geometry that operates on vectorial spaces of arbitrary dimensions.
 */
public class GeomXD implements GeomE {

    /**
     * The default tolerance value.
     */
    public static final double defaultEpsilon = 0.00000001;

    /**
     * This geometry epsilon, that is the distance under which differences are
     * no more relevant.
     */
    private final double epsilon;

    /**
     * Builds an arbitrary-dimensional Euclidean geometry with the default
     * epsilon.
     */
    public GeomXD() {
        epsilon = defaultEpsilon;
    }

    /**
     * Builds an arbitrary-dimensional Euclidean geometry with the default
     * epsilon.
     *
     * @param epsilon the geometry epsilon.
     */
    public GeomXD(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Returns the geometry epsilon.
     *
     * @return the geometry epsilon.
     */
    public double getEpsilon() {
        return epsilon;
    }

    @Override
    public int geomDim() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean almostZero(double a) {
        return almostEqual(a, 0);
    }

    @Override
    public boolean almostEqual(double a, double b) {
        return Math.abs(a - b) <= epsilon;
    }

    @Override
    public boolean almostZero(Coordinates a) {
        return almostEqual(a, new Coordinates(a.dim()));
    }

    @Override
    public boolean almostEqual(Coordinates a, Coordinates b) {
        for (int i = 0; i < Math.min(Math.max(a.dim(), b.dim()), geomDim()); ++i) {
            if (!almostEqual(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double magnitude(Coordinates vector) {
        double sum = 0;
        for (int i = 0; i < Math.min(vector.dim(), geomDim()); ++i) {
            sum += vector.get(i) * vector.get(i);
        }
        return Math.sqrt(sum);
    }

    @Override
    public double dotProduct(Coordinates vectorA, Coordinates vectorB) {
        double sum = 0;
        for (int i = 0; i < Math.min(Math.min(vectorA.dim(), vectorB.dim()), geomDim()); i++) {
            sum += vectorA.get(i) * vectorB.get(i);
        }
        return sum;
    }

    @Override
    public Coordinates unitVector(Coordinates vector) {
        return scaleVector(vector, 1 / magnitude(vector));
    }

    @Override
    public Coordinates unitVector(Coordinates vector, Coordinates origin) {
        return unitVector(vector.restrMinus(origin, geomDim())).plusIP(origin);
    }

    @Override
    public Coordinates scaleVector(Coordinates vector, double factor) {
        return vector.restrTimes(factor, geomDim());
    }

    @Override
    public Coordinates scaleVector(Coordinates vector, double factor, Coordinates origin) {
        return scaleVector(vector.restrMinus(origin, geomDim()), factor).plusIP(origin);
    }

    @Override
    public double betweenAngle(Coordinates vectorA, Coordinates vectorB) {
        double dotProd = dotProduct(vectorA, vectorB);
        double value = Math.acos(dotProd / (magnitude(vectorA) * magnitude(vectorB)));
        if (Double.isNaN(value)) {
            if (dotProd > 0) {
                return 0;
            } else {
                return Math.PI;
            }
        }
        return value;
    }

    @Override
    public double betweenAngle(Coordinates vectorA, Coordinates vectorB, Coordinates origin) {
        return betweenAngle(vectorA.minus(origin), vectorB.minus(origin));
    }

    @Override
    public Coordinates midPoint(Coordinates a, Coordinates b) {
        Coordinates result = new Coordinates(Math.max(a.dim(), b.dim()));
        result.plusIP(a);
        result.plusIP(b);
        result.divideIP(2.0);
        return result;
    }

    @Override
    public Coordinates inBetweenPoint(Coordinates a, Coordinates b, double ratio) {
        Coordinates result = new Coordinates(Math.max(a.dim(), b.dim()));
        result.plusIP(b);
        result.minusIP(a);
        result.timesIP(ratio);
        result.plusIP(a);
        return result;
    }

    @Override
    public boolean isPointInLine(Coordinates point, Coordinates linePointA, Coordinates linePointB) {
        if (almostEqual(linePointA, linePointB)) {
            throw new UndefinedSubspace("One of the lines is not defined as its points coincide.");
        }
        Coordinates ab = linePointB.restrMinus(linePointA, geomDim());
        Coordinates ap = point.restrMinus(linePointA, geomDim());
        double magAb = magnitude(ab);
        double magAp = magnitude(ap);
        return almostEqual(Math.abs(dotProduct(ap, ab)), magAb * magAp);
    }

    @Override
    public boolean isPointInSegment(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB) {
        if (almostEqual(segmExtremityA, segmExtremityB)) {
            return almostEqual(point, segmExtremityA);
        }
        Coordinates ab = segmExtremityB.restrMinus(segmExtremityA, geomDim());
        Coordinates ap = point.restrMinus(segmExtremityA, geomDim());
        double magAb = magnitude(ab);
        double magAp = magnitude(ap);
        double dotProduct = dotProduct(ap, ab);
        return dotProduct >= 0 && almostEqual(dotProduct, magAb * magAp) && (magAp < magAb || almostEqual(magAp, magAb));
    }

    @Override
    public PointRelation pointLineRelation(Coordinates point, Coordinates linePointA, Coordinates linePointB) {
        PointRelation relation = new PointRelation(this);
        if (almostEqual(linePointA, linePointB)) {
            throw new UndefinedSubspace("The line is not defined as its points coincide. P:" + point + " A: " + linePointA + " B: " +linePointB);
        }

        Coordinates ab = linePointB.restrMinus(linePointA, geomDim());
        Coordinates ap = point.restrMinus(linePointA, geomDim());
        double factor = dotProduct(ap, ab) / dotProduct(ab, ab);
        relation.projectionAsLine = linePointA.restrPlus(ab.restrTimes(factor, geomDim()), geomDim());
        relation.projection = relation.projectionAsLine;
        relation.closestPoint = relation.projection;
        relation.distance = magnitude(point.restrMinus(relation.projection, geomDim()));
        relation.projectionDistance = 0;
        return relation;
    }

    @Override
    public PointRelation pointSegmentRelation(Coordinates point, Coordinates segmExtremityA, Coordinates segmExtremityB) {
        PointRelation relation = new PointRelation(this);
        if (almostEqual(segmExtremityA, segmExtremityB)) {
            relation.closestPoint = segmExtremityA.restr(geomDim());
            relation.projectionAsLine = null;
            relation.projection = null;
            relation.distance = magnitude(point.restrMinus(relation.closestPoint, geomDim()));
            relation.projectionDistance = -1;
            return relation;
        }

        PointRelation relationAsLine = pointLineRelation(point, segmExtremityA, segmExtremityB);
        relation.projectionAsLine = relationAsLine.projection;

        if (isPointInSegment(relation.projectionAsLine, segmExtremityA, segmExtremityB)) {
            relation.closestPoint = relationAsLine.closestPoint;
            relation.distance = relationAsLine.distance;
            relation.projection = relation.projectionAsLine;
            relation.projectionDistance = 0;
        } else {
            double magProjA = magnitude(relation.projectionAsLine.restrMinus(segmExtremityA, geomDim()));
            double magProjB = magnitude(relation.projectionAsLine.restrMinus(segmExtremityB, geomDim()));
            if (magProjA < magProjB) {
                relation.closestPoint = segmExtremityA.restr(geomDim());
                relation.projectionDistance = magProjA;
            } else {
                relation.closestPoint = segmExtremityB.restr(geomDim());
                relation.projectionDistance = magProjB;
            }
            relation.projection = null;
            relation.distance = magnitude(point.restrMinus(relation.closestPoint, geomDim()));
        }
        return relation;
    }

    @Override
    public LineRelation lineLineRelation(Coordinates line1PointA, Coordinates line1PointB,
            Coordinates line2PointA, Coordinates line2PointB) {

        LineRelation relation = new LineRelation(this);
        if (almostEqual(line1PointA, line1PointB) || almostEqual(line2PointA, line2PointB)) {
            throw new UndefinedSubspace("One of the lines is not defined as its points coincide.");
        }

        Coordinates a = line1PointA;
        Coordinates b = line1PointB;
        Coordinates c = line2PointA;
        Coordinates d = line2PointB;

        /* First line is a + r*ab, second line is c + s*cd. The points A and C
         * of minimal distance are perpendicular to the other line, which means
         * ab*AC=0 and cd*AC=0 simultaneously. This leads to:
         * (ab*ab)A-(ab*cd)C = -ab(a-c)
         * (cd*ab)A-(cd*cd)C = -cd(a-c)
         */
        Coordinates ab = b.restrMinus(a, geomDim());
        Coordinates cd = d.restrMinus(c, geomDim());
        Coordinates ac = a.restrMinus(c, geomDim());

        double abab = dotProduct(ab, ab);  // a
        double abcd = dotProduct(ab, cd);  // b
        double cdcd = dotProduct(cd, cd);  // c
        double acab = dotProduct(ac, ab);  // d
        double accd = dotProduct(ac, cd);  // e

        double denom = abab * cdcd - abcd * abcd;

        double rA, sC;
        if (!almostZero(denom)) {
            relation.areParallel = false;
            rA = (abcd * accd - cdcd * acab) / denom;
            sC = (abab * accd - abcd * acab) / denom;
        } else {
            relation.areParallel = true;
            rA = 0;
            sC = acab / abcd;
        }

        Coordinates A = a.restrPlus(ab.restrTimes(rA, geomDim()), geomDim());
        Coordinates C = c.restrPlus(cd.restrTimes(sC, geomDim()), geomDim());
        relation.distance = magnitude(A.restrMinus(C, geomDim()));

        if (relation.areParallel) {
            relation.areOverlapping = almostZero(relation.distance);
            relation.closestPointA = null;
            relation.closestPointB = null;
            relation.intersectionAsLines = null;
            relation.intersection = null;
        } else {
            relation.areOverlapping = false;
            relation.closestPointA = A;
            relation.closestPointB = C;
            relation.intersectionAsLines = relation.intersectionFromClosestPoints();
            relation.intersection = relation.intersectionAsLines;
        }
        return relation;
    }

    @Override
    public LineRelation lineSegmentRelation(Coordinates linePointA, Coordinates linePointB,
            Coordinates segmExtremityA, Coordinates segmExtremityB) {

        LineRelation relation = new LineRelation(this);
        if (almostEqual(linePointA, linePointB)) {
            throw new UndefinedSubspace("The line is not defined as its points coincide.");
        } else if (almostEqual(segmExtremityA, segmExtremityB)) {
            PointRelation relationAsPoint = pointLineRelation(segmExtremityA, linePointA, linePointB);
            relation.areParallel = null;
            relation.closestPointA = relationAsPoint.projection;
            relation.closestPointB = segmExtremityA.restr(geomDim());
            relation.intersectionAsLines = relation.intersectionFromClosestPoints();
            relation.areOverlapping = almostZero(relationAsPoint.distance);
            relation.intersection = relation.intersectionAsLines;
            relation.distance = relationAsPoint.distance;
            relation.areAdjacent = false;
            return relation;
        }

        LineRelation relationAsLines = lineLineRelation(linePointA, linePointB, segmExtremityA, segmExtremityB);
        if (relationAsLines.areParallel()) {
            return relationAsLines;
        } else {
            relation.areParallel = false;
            relation.areOverlapping = false;
            relation.intersectionAsLines = relationAsLines.intersection();
            if (relationAsLines.areIntersecting()
                    && isPointInSegment(relationAsLines.intersection(), segmExtremityA, segmExtremityB)) {
                relation.intersection = relation.intersectionAsLines;
                relation.closestPointA = relation.intersectionAsLines;
                relation.closestPointB = relation.intersectionAsLines;
                relation.distance = 0;
                relation.areAdjacent = almostEqual(relation.intersection, segmExtremityA) || almostEqual(relation.intersection, segmExtremityB);
                return relation;
            } else {
                relation.intersection = null;
                relation.areAdjacent = false;
                PointRelation pointRelationA = pointLineRelation(segmExtremityA, linePointA, linePointB);
                PointRelation pointRelationB = pointLineRelation(segmExtremityB, linePointA, linePointB);
                if (pointRelationA.distance < pointRelationB.distance) {
                    relation.closestPointA = pointRelationA.projection;
                    relation.closestPointB = segmExtremityA.restr(geomDim());
                    relation.distance = pointRelationA.distance;
                } else {
                    relation.closestPointA = pointRelationB.projection;
                    relation.closestPointB = segmExtremityB.restr(geomDim());
                    relation.distance = pointRelationB.distance;
                }
            }
        }
        return relation;
    }

    @Override
    public LineRelation segmentSegmentRelation(Coordinates segm1PointA, Coordinates segm1PointB,
            Coordinates segm2PointA, Coordinates segm2PointB) {

        LineRelation relation = new LineRelation(this);

        if (almostEqual(segm1PointA, segm1PointB) || almostEqual(segm2PointA, segm2PointB)) {
            PointRelation relationAsPoint;
            if (almostEqual(segm1PointA, segm1PointB)) {
                relationAsPoint = pointSegmentRelation(segm1PointA, segm2PointA, segm2PointB);
                relation.closestPointA = segm1PointA.restr(geomDim());
                relation.closestPointB = relationAsPoint.closestPoint;
                relation.areAdjacent = almostEqual(segm1PointA, segm2PointA) || almostEqual(segm1PointA, segm2PointB);
            } else {
                relationAsPoint = pointSegmentRelation(segm2PointA, segm1PointA, segm1PointB);
                relation.closestPointA = relationAsPoint.closestPoint;
                relation.closestPointB = segm2PointA.restr(geomDim());
                relation.areAdjacent = almostEqual(segm2PointA, segm1PointA) || almostEqual(segm2PointA, segm1PointB);
            }
            relation.areParallel = null;
            relation.intersectionAsLines = relation.intersectionFromClosestPoints();
            relation.intersection = relation.intersectionAsLines;
            relation.distance = relationAsPoint.distance;
            relation.areOverlapping = almostZero(relationAsPoint.distance);
            return relation;
        }

        LineRelation relationAsLine = lineSegmentRelation(segm1PointA, segm1PointB, segm2PointA, segm2PointB);
        relation.areParallel = relationAsLine.areParallel;
        relation.intersectionAsLines = relationAsLine.intersectionAsLines;

        boolean a1_eq_b1 = almostEqual(segm1PointA, segm2PointA);
        boolean a1_eq_b2 = almostEqual(segm1PointA, segm2PointB);
        boolean a2_eq_b1 = almostEqual(segm1PointB, segm2PointA);
        boolean a2_eq_b2 = almostEqual(segm1PointB, segm2PointB);
        if (relation.areParallel) {
            // Checking for overlapping or projection overlapping cases, eg:
            //           -----------------
            //                        -------
            boolean a1_in = pointSegmentRelation(segm1PointA, segm2PointA, segm2PointB).isProjectionIncluded();
            boolean a2_in = pointSegmentRelation(segm1PointB, segm2PointA, segm2PointB).isProjectionIncluded();
            boolean b1_in = pointSegmentRelation(segm2PointA, segm1PointA, segm1PointB).isProjectionIncluded();
            boolean b2_in = pointSegmentRelation(segm2PointB, segm1PointA, segm1PointB).isProjectionIncluded();

            if ((a1_in && a2_in) || (b1_in && b2_in)
                    || (a1_in && (!a1_eq_b1 || !a1_eq_b2))
                    || (a2_in && (!a2_eq_b1 || !a2_eq_b2))) {
                relation.closestPointA = null;
                relation.closestPointB = null;
                relation.intersection = null;
                relation.areOverlapping = relationAsLine.areOverlapping;
                relation.distance = relationAsLine.distance;
                relation.areAdjacent = false;
                return relation;
            }
            // Continues the computation for the standard case even for
            // parallel, projection non-overlapping case, eg:
            //      -------
            //               -------          or           ------    ------
        }

        relation.areOverlapping = false;
        relation.areAdjacent = a1_eq_b1 || a1_eq_b2 || a2_eq_b1 || a2_eq_b2;
        if (relationAsLine.areIntersecting()
                && isPointInSegment(relationAsLine.intersection(), segm1PointA, segm1PointB)) {
            // Case of clean intersection between the segments
            relation.closestPointA = relationAsLine.intersection();
            relation.closestPointB = relationAsLine.intersection();
            relation.intersection = relationAsLine.intersection();
            relation.distance = 0;
        } else {
            relation.intersection = null;
            relation.distance = Double.MAX_VALUE;
            tryAndUpdateClosestPoints(relation, segm1PointA, segm2PointA);
            tryAndUpdateClosestPoints(relation, segm1PointA, segm2PointB);
            tryAndUpdateClosestPoints(relation, segm1PointB, segm2PointA);
            tryAndUpdateClosestPoints(relation, segm1PointB, segm2PointB);

            PointRelation projA1 = pointSegmentRelation(segm1PointA, segm2PointA, segm2PointB);
            PointRelation projA2 = pointSegmentRelation(segm1PointB, segm2PointA, segm2PointB);
            PointRelation projB1 = pointSegmentRelation(segm2PointA, segm1PointA, segm1PointB);
            PointRelation projB2 = pointSegmentRelation(segm2PointB, segm1PointA, segm1PointB);
            tryAndUpdateClosestPoints(relation, segm1PointA, projA1.isProjectionIncluded() ? projA1.projection : null);
            tryAndUpdateClosestPoints(relation, segm1PointB, projA2.isProjectionIncluded() ? projA2.projection : null);
            tryAndUpdateClosestPoints(relation, projB1.isProjectionIncluded() ? projB1.projection : null, segm2PointA);
            tryAndUpdateClosestPoints(relation, projB2.isProjectionIncluded() ? projB2.projection : null, segm2PointB);
        }
        return relation;
    }

    /**
     * Checks and overlap the closest points given two coordinates and the
     * current status.
     *
     * @param closestPoints the current closest points.
     * @param currentDistance the current distance.
     * @param a the first coordinates.
     * @param b the second coordinates.
     * @return the new distance and updates the closest points parameter.
     */
    private void tryAndUpdateClosestPoints(LineRelation relation, Coordinates a, Coordinates b) {
        if (a != null && b != null) {
            double newDistance = magnitude(a.restrMinus(b, geomDim()));
            if (newDistance < relation.distance) {
                relation.closestPointA = a.restr(geomDim());
                relation.closestPointB = b.restr(geomDim());
                relation.distance = newDistance;
            }
        }
    }

    /**
     * Orders Coordinates from lowest to highest magnitude.
     */
    public class MagnitudeComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(magnitude(a), magnitude(b));
        }
    }
}
