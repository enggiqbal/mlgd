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

import java.util.HashSet;
import java.util.Set;
import ocotillo.geometry.Coordinates;
import ocotillo.geometry.Geom;

/**
 * Defines the angle at which an object is viewed.
 */
public final class ViewAngle {

    private TransMatrix transMatrix;
    private double offsetZ;

    private final Set<Observer> observers = new HashSet<>();

    /**
     * Sets a standard, frontal view angle.
     */
    public ViewAngle() {
        setAngles(0, 0);
    }

    /**
     * Sets a view angle with given yaw and pitch.
     *
     * @param yaw the yaw angle.
     * @param pitch the pitch angle.
     */
    public ViewAngle(double yaw, double pitch) {
        setAngles(yaw, pitch);
    }

    /**
     * Copies the settings of another viewAngle.
     *
     * @param other the other view angle.
     */
    public void copySettings(ViewAngle other) {
        System.arraycopy(other.transMatrix.values, 0,
                transMatrix.values, 0, transMatrix.values.length);
        other.offsetZ = offsetZ;
    }

    /**
     * Checks if the view angle is that of a standard 2D view.
     *
     * @return true if the view angle is a standard 2D one.
     */
    public boolean isStandard2D() {
        return transMatrix.isIdentity();
    }

    /**
     * Gets the current Z offset.
     *
     * @return the current Z offset.
     */
    public double offsetZ() {
        return offsetZ;
    }

    /**
     * Sets the Z offset. This is used to indicate the rotation centre around
     * the Z axis.
     *
     * @param offsetZ the central Z position of the graph.
     */
    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    /**
     * Sets the angles used for the drawing.
     *
     * @param yaw the yaw angle.
     * @param pitch the pitch angle.
     */
    public void setAngles(double yaw, double pitch) {
        transMatrix = TransMatrix.rotation(yaw, pitch);
        notifyObservers();
    }

    /**
     * Performs a yaw rotation.
     *
     * @param yaw the yaw angle.
     */
    public void yawRotate(double yaw) {
        transMatrix = transMatrix.combine(TransMatrix.yawRotation(yaw));
        notifyObservers();
    }

    /**
     * Performs a pitch rotation.
     *
     * @param pitch the pitch angle.
     */
    public void pitchRotate(double pitch) {
        transMatrix = transMatrix.combine(TransMatrix.pitchRotation(pitch));
        notifyObservers();
    }

    /**
     * Performs a yaw and pitch rotation.
     *
     * @param yaw the yaw angle.
     * @param pitch the pitch angle.
     */
    public void rotate(double yaw, double pitch) {
        transMatrix = transMatrix.combine(TransMatrix.yawRotation(yaw))
                .combine(TransMatrix.pitchRotation(pitch));
        notifyObservers();
    }

    /**
     * Transforms a point according to the current view angle.
     *
     * @param original the input point.
     * @return the transformed point.
     */
    public Coordinates transformedPosition(Coordinates original) {
        return transMatrix.transform(original.minus(new Coordinates(0, 0, offsetZ)));
    }

    /**
     * Register a ViewAngle observer.
     *
     * @param observer the observer.
     */
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    /**
     * Unregister a ViewAngle observer.
     *
     * @param observer the observer.
     */
    public void unregisterObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all ViewAngle observer.
     */
    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.viewAngleMoved();
        }
    }

    /**
     * Matrix that defines the point transformation under the current rotation
     * angles.
     */
    private static class TransMatrix {

        private final double[] values;

        /**
         * Private constructor.
         *
         * @param values the matrix values.
         */
        private TransMatrix(double[] values) {
            this.values = values;
        }

        /**
         * Creates a transformation matrix that operates a yaw rotation.
         *
         * @param yaw the yaw angle.
         * @return the transformation matrix.
         */
        public static TransMatrix yawRotation(double yaw) {
            return new TransMatrix(new double[]{
                Math.cos(yaw), 0, -Math.sin(yaw),
                0, 1, 0,
                Math.sin(yaw), 0, Math.cos(yaw)
            });
        }

        /**
         * Creates a transformation matrix that operates a pitch rotation.
         *
         * @param pitch the pitch angle.
         * @return the transformation matrix.
         */
        public static TransMatrix pitchRotation(double pitch) {
            return new TransMatrix(new double[]{
                1, 0, 0,
                0, Math.cos(pitch), Math.sin(pitch),
                0, -Math.sin(pitch), Math.cos(pitch)
            });
        }

        /**
         * Creates a transformation matrix that operates a yaw-pitch rotation.
         *
         * @param yaw the yaw angle.
         * @param pitch the pitch angle.
         * @return the transformation matrix.
         */
        public static TransMatrix rotation(double yaw, double pitch) {
            return yawRotation(yaw).combine(pitchRotation(pitch));
        }

        /**
         * Combines different transformation matrixes.
         *
         * @param other the other transformation matrix.
         * @return the combination of the two transformations.
         */
        private TransMatrix combine(TransMatrix other) {
            double[] result = new double[9];
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    for (int i = 0; i < 3; i++) {
                        result[row * 3 + col]
                                += this.values[row * 3 + i] * other.values[i * 3 + col];
                    }
                }
            }
            return new TransMatrix(result);
        }

        /**
         * Transforms a point.
         *
         * @param original the input point.
         * @return the transformed point.
         */
        public Coordinates transform(Coordinates original) {
            return new Coordinates(
                    original.x() * values[0] + original.y() * values[3] + original.z() * values[6],
                    original.x() * values[1] + original.y() * values[4] + original.z() * values[7],
                    original.x() * values[2] + original.y() * values[5] + original.z() * values[8]
            );
        }

        /**
         * Checks if the transformation matrix is an identity.
         *
         * @return true if the matrix is an identity, false otherwise.
         */
        public boolean isIdentity() {
            return Geom.eXD.almostEqual(values[0], 1) && Geom.eXD.almostZero(values[1]) && Geom.eXD.almostZero(values[2])
                    && Geom.eXD.almostZero(values[3]) && Geom.eXD.almostEqual(values[4], 1) && Geom.eXD.almostZero(values[5])
                    && Geom.eXD.almostZero(values[6]) && Geom.eXD.almostZero(values[7]) && Geom.eXD.almostEqual(values[8], 1);
        }
    }

    /**
     * Interface for ViewAngle observers.
     */
    public static interface Observer {

        /**
         * Runs when the view angle moved.
         */
        public void viewAngleMoved();

    }

}
