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
package ocotillo.graph.layout.fdl.modular;

import java.util.ArrayList;
import java.util.List;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;
import ocotillo.graph.Node;
import ocotillo.graph.layout.Layout2D;
import org.ejml.simple.SimpleMatrix;

/**
 * Pre-movement step for the ModularFdl algorithm.
 */
public abstract class ModularPreMovement extends ModularElement {

    /**
     * Executes this pre-movement computation.
     */
    protected abstract void execute();

    /**
     * Limits the movement of a node to the provided maximal displacement per
     * dimension.
     */
    public static class MovementLimiterPerDimension extends ModularPreMovement {

        private final Coordinates limits;

        /**
         * Constructs a movement limiter per dimension.
         *
         * @param limits coordinates indicating the maximal movement for each
         * dimension.
         */
        public MovementLimiterPerDimension(Coordinates limits) {
            this.limits = limits;
            for (int i = 0; i < limits.dim(); i++) {
                limits.setAt(i, Math.max(0, limits.get(i)));
            }
        }

        @Override
        protected void execute() {
            for (Node node : mirrorGraph().nodes()) {
                Coordinates movements = movements().get(node);
                for (int i = 0; i < movements.dim(); i++) {
                    double component = movements.get(i);
                    double limit = limits.get(i);
                    if (Math.abs(component) > limit) {
                        movements.setAt(i, component > 0 ? limit : -limit);
                    }
                }
            }
        }
    }

    /**
     * Recomputes the node movements in order to obtain a smooth morphing of the
     * graph.
     */
    public static class VectorFieldSmoothing extends ModularPreMovement {

        private final int minGridDimension;

        private Box graphBox;
        private int xDim;
        private int yDim;
        private double cellDim;
        private final List<Node> constrainedNodes = new ArrayList<>();

        private SimpleMatrix A;
        private SimpleMatrix xB;
        private SimpleMatrix yB;
        private SimpleMatrix xV;
        private SimpleMatrix yV;

        /**
         * Constructs a vector field smoothing.
         *
         * @param minGridDimension
         */
        public VectorFieldSmoothing(int minGridDimension) {
            this.minGridDimension = minGridDimension;
        }

        @Override
        protected void execute() {
            initialize();
            fillCoeffMatrixes();
            computeCornersVectors();
            recomputeMovements();
        }

        /**
         * Computes the matrix dimension and initialize them.
         */
        private void initialize() {
            graphBox = Layout2D.graphBox(mirrorGraph(), mirrorPositions(), null, null, null);
            cellDim = graphBox.minDim() / minGridDimension;
            xDim = (int) Math.ceil(graphBox.width() / cellDim);
            yDim = (int) Math.ceil(graphBox.height() / cellDim);

            constrainedNodes.clear();
            for (Node node : mirrorGraph().nodes()) {
                Coordinates movement = movements().get(node);
                double constraint = constraints().get(node);
                if (!Geom.e2D.almostZero(Geom.e2D.magnitude(movement))
                        || !Geom.e2D.almostZero(constraint)) {
                    constrainedNodes.add(node);
                }
            }

            int cornersCount = (xDim + 1) * (yDim + 1);
            A = new SimpleMatrix(constrainedNodes.size() + cornersCount, cornersCount);
            xB = new SimpleMatrix(constrainedNodes.size() + cornersCount, 1);
            yB = new SimpleMatrix(constrainedNodes.size() + cornersCount, 1);
        }

        /**
         * Fills the value in the coefficient matrixes A and B
         */
        private void fillCoeffMatrixes() {
            int rowIndex = 0;
            for (Node node : constrainedNodes) {
                NodeCellInfo cellInfo = getNodeCellInfo(node);
                A.set(rowIndex, cellInfo.leftBottomCorner, (1 - cellInfo.xOffsetInCell) * (1 - cellInfo.yOffsetInCell));
                A.set(rowIndex, cellInfo.rightBottomCorner, cellInfo.xOffsetInCell * (1 - cellInfo.yOffsetInCell));
                A.set(rowIndex, cellInfo.leftTopCorner, (1 - cellInfo.xOffsetInCell) * cellInfo.yOffsetInCell);
                A.set(rowIndex, cellInfo.rightTopCorner, cellInfo.xOffsetInCell * cellInfo.yOffsetInCell);

                xB.set(rowIndex, 0, movements().get(node).x());
                yB.set(rowIndex, 0, movements().get(node).y());
                rowIndex++;
            }

            int cornersCount = (xDim + 1) * (yDim + 1);
            for (int corner = 0; corner < cornersCount; corner++) {
                List<Integer> neighbourCorners = getNeighborCorners(corner);
                A.set(rowIndex, corner, 1);
                for (int neighbourCorner : neighbourCorners) {
                    A.set(rowIndex, neighbourCorner, -1.0 / neighbourCorners.size());
                }
                rowIndex++;
            }
        }

        /**
         * Gets the cell information on a given node.
         *
         * @param node the node.
         * @return its cell information.
         */
        private NodeCellInfo getNodeCellInfo(Node node) {
            NodeCellInfo cellInfo = new NodeCellInfo();
            cellInfo.node = node;
            Coordinates relativePosition = mirrorPositions().get(node)
                    .restrMinus(new Coordinates(graphBox.left(), graphBox.bottom()), 2);
            double normalizedX = relativePosition.x() / cellDim;
            double normalizedY = relativePosition.y() / cellDim;
            cellInfo.cellRow = Math.min((int) Math.floor(normalizedY), yDim - 1);
            cellInfo.cellColumn = Math.min((int) Math.floor(normalizedX), xDim - 1);
            cellInfo.leftBottomCorner = (xDim + 1) * cellInfo.cellRow + cellInfo.cellColumn;
            cellInfo.rightBottomCorner = cellInfo.leftBottomCorner + 1;
            cellInfo.leftTopCorner = cellInfo.leftBottomCorner + xDim + 1;
            cellInfo.rightTopCorner = cellInfo.leftTopCorner + 1;
            cellInfo.xOffsetInCell = normalizedX - cellInfo.cellColumn;
            cellInfo.yOffsetInCell = normalizedY - cellInfo.cellRow;
            return cellInfo;
        }

        /**
         * Collects all the parameter of a node with respect to the cell it is
         * contained and its relative position in the cell.
         */
        protected class NodeCellInfo {

            Node node;
            int cellRow;
            int cellColumn;
            int leftBottomCorner;
            int leftTopCorner;
            int rightBottomCorner;
            int rightTopCorner;
            double xOffsetInCell;
            double yOffsetInCell;
        }

        /**
         * Identifies the neighbours of a grid corner.
         *
         * @param corner the corner.
         * @return its neighbours.
         */
        private List<Integer> getNeighborCorners(int corner) {
            List<Integer> neighborCorners = new ArrayList<>();
            if (corner % (xDim + 1) > 0) {
                neighborCorners.add(corner - 1);
            }
            if (corner % (xDim + 1) < xDim) {
                neighborCorners.add(corner + 1);
            }
            if (corner - (xDim + 1) >= 0) {
                neighborCorners.add(corner - (xDim + 1));
            }
            if (corner + (xDim + 1) < (xDim + 1) * (yDim + 1)) {
                neighborCorners.add(corner + (xDim + 1));
            }
            return neighborCorners;
        }

        /**
         * Compute the value of the corners, which are the variables in the
         * system.
         */
        private void computeCornersVectors() {
            SimpleMatrix tranA = A.transpose();
            SimpleMatrix tildeA = (tranA.mult(A)).invert().mult(tranA);
            xV = tildeA.mult(xB);
            yV = tildeA.mult(yB);
        }

        /**
         * Recomputes the movements for each graph node.
         */
        private void recomputeMovements() {
            for (Node node : mirrorGraph().nodes()) {
                NodeCellInfo cellInfo = getNodeCellInfo(node);
                double x = computeComponent(cellInfo, xV);
                double y = computeComponent(cellInfo, yV);
                movements().set(node, new Coordinates(x, y));
            }
        }

        /**
         * Computes the movement component of a given node.
         *
         * @param cellInfo the cell info of the node.
         * @param V the V vectors corresponding to the desired component.
         * @return the movement component for the node.
         */
        private double computeComponent(NodeCellInfo cellInfo, SimpleMatrix V) {
            double lb = (1 - cellInfo.xOffsetInCell) * (1 - cellInfo.yOffsetInCell) * V.get(cellInfo.leftBottomCorner);
            double rb = cellInfo.xOffsetInCell * (1 - cellInfo.yOffsetInCell) * V.get(cellInfo.rightBottomCorner);
            double lt = (1 - cellInfo.xOffsetInCell) * cellInfo.yOffsetInCell * V.get(cellInfo.leftTopCorner);
            double rt = cellInfo.xOffsetInCell * cellInfo.yOffsetInCell * V.get(cellInfo.rightTopCorner);
            return lb + rb + lt + rt;
        }
    }
}
