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
import java.util.Comparator;

/**
 * The coordinates of a point in a vectorial space.
 */
public class Coordinates {

    private final ArrayList<Double> coordinates;
    private final int defaultInitialCapacity = 3;

    /**
     * Constructs a coordinates by passing their value.
     *
     * @param x the first coordinate.
     * @param others the other coordinates.
     */
    public Coordinates(double x, double... others) {
        coordinates = new ArrayList<>(Math.max(others.length + 1, defaultInitialCapacity));
        coordinates.add(x);
        for (double coordinate : others) {
            coordinates.add(coordinate);
        }
    }

    /**
     * Constructs coordinates of arbitrary dimension.
     *
     * @param dim the coordinates dimension.
     */
    public Coordinates(int dim) {
        coordinates = new ArrayList<>(Math.max(dim, defaultInitialCapacity));
        for (int i = 0; i < dim; i++) {
            coordinates.add(0.0);
        }
    }

    /**
     * Copies an existing coordinates instance.
     *
     * @param otherCoordinates the existing instance.
     */
    public Coordinates(Coordinates otherCoordinates) {
        coordinates = new ArrayList<>(otherCoordinates.coordinates);
    }

    /**
     * Returns the first coordinate.
     *
     * @return the first coordinate.
     */
    public double x() {
        return get(0);
    }

    /**
     * Returns the second coordinate.
     *
     * @return the second coordinate.
     */
    public double y() {
        return get(1);
    }

    /**
     * Returns the third coordinate.
     *
     * @return the third coordinate.
     */
    public double z() {
        return get(2);
    }

    /**
     * Sets the first coordinate.
     *
     * @param x the first coordinate.
     */
    public void setX(double x) {
        setAt(0, x);
    }

    /**
     * Sets the second coordinate.
     *
     * @param y the second coordinate.
     */
    public void setY(double y) {
        setAt(1, y);
    }

    /**
     * Sets the third coordinate.
     *
     * @param z the third coordinate.
     */
    public void setZ(double z) {
        setAt(2, z);
    }

    /**
     * Gets the coordinate with given index.
     *
     * @param i the coordinate index.
     * @return the coordinate at given index, or zero if the dimension is less
     * or equal to the index.
     */
    public double get(int i) {
        if (i < coordinates.size()) {
            return coordinates.get(i);
        }
        return 0;
    }

    /**
     * Sets the coordinate with given index.
     *
     * @param i the coordinate index.
     * @param value the value to assign.
     */
    public void setAt(int i, double value) {
        assert (!Double.isNaN(value)) : "Assigned NaN value in position " + i;
        while (coordinates.size() <= i) {
            coordinates.add(0.0);
        }
        coordinates.set(i, value);
    }

    /**
     * Sets the coordinates with the given values.
     *
     * @param values the values to assign.
     */
    public void set(double... values) {
        while (coordinates.size() < values.length) {
            coordinates.add(0.0);
        }
        for (int i = 0; i < values.length; i++) {
            setAt(i, values[i]);
        }
    }

    /**
     * Sets the coordinates with the values of the parameter one.
     *
     * @param values the coordinates to assign to this.
     */
    public void set(Coordinates values) {
        while (coordinates.size() < values.dim()) {
            coordinates.add(0.0);
        }
        for (int i = 0; i < values.dim(); i++) {
            setAt(i, values.get(i));
        }
    }

    /**
     * Restricts the vector to the given number of dimensions.
     *
     * @param dimensions the number of dimensions,to keep.
     * @return the restricted vector.
     */
    public Coordinates restr(int dimensions) {
        if (dim() > dimensions) {
            return (new Coordinates(dimensions)).restrPlusIP(this, dimensions);
        }
        return this;
    }

    /**
     * Returns this vector whit the first values substituted by the parameter
     * ones.
     *
     * @param values the coordinates to assign to this vector head.
     * @return this vector with a different head.
     */
    public Coordinates withHead(Coordinates values) {
        Coordinates result = new Coordinates(this);
        result.set(values);
        return result;
    }

    /**
     * Resets all the coordinates to zero.
     */
    public void reset() {
        for (int i = 0; i < coordinates.size(); i++) {
            coordinates.set(i, 0.0);
        }
    }

    /**
     * Returns the dimension of the coordinates.
     *
     * @return the dimension of the coordinates.
     */
    public int dim() {
        return coordinates.size();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (int i = 0; i < dim(); i++) {
            hash += 83 * i * coordinates.get(i).hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coordinates other = (Coordinates) obj;
        for (int i = 0; i < Math.max(dim(), other.dim()); i++) {
            if (get(i) != other.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String answer = "(";
        String divider = ", ";
        for (int i = 0; i < dim(); i++) {
            answer += get(i) + divider;
        }
        return answer.substring(0, answer.length() - divider.length()) + ")";
    }

    /**
     * Computes the opposite of a vector (a -> -a). This instance is not
     * modified. The result will have the same dimensions than the implicit
     * parameter.
     *
     * @return the opposite of this vector.
     */
    public Coordinates minus() {
        return minusComp(new Coordinates(dim()), Integer.MAX_VALUE);
    }

    /**
     * Computes the opposite of a vector (a -> -a) in place. This instance is
     * modified to contain the new value.
     *
     * @return the opposite of this vector.
     */
    public Coordinates minusIP() {
        return minusComp(this, Integer.MAX_VALUE);
    }

    /**
     * Computes the opposite of a vector (a -> -a). This instance is not
     * modified. The result will have the same dimensions than the implicit
     * parameter. The operation is performed only on the first dimensions as
     * specified by the input parameter.
     *
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the opposite of this vector.
     */
    public Coordinates restrMinus(int onDimensions) {
        int resultDim = Math.min(dim(), onDimensions);
        return minusComp(new Coordinates(resultDim), onDimensions);
    }

    /**
     * Computes the opposite of a vector (a -> -a) in place. This instance is
     * modified to contain the new value. The operation is performed only on the
     * first dimensions as specified by the input parameter.
     *
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the opposite of this vector.
     */
    public Coordinates restrMinusIP(int onDimensions) {
        return minusComp(this, onDimensions);
    }

    private Coordinates minusComp(Coordinates result, int onDimensions) {
        return timesComp(-1, result, onDimensions);
    }

    /**
     * Computes the sum of two vectors (a,b -> a+b). This instance is not
     * modified. The result will have the same dimensions than the implicit
     * parameter.
     *
     * @param b the second vector.
     * @return the sum of two vectors.
     */
    public Coordinates plus(Coordinates b) {
        return plusComp(b, new Coordinates(dim()), Integer.MAX_VALUE);
    }

    /**
     * Computes the sum of two vectors (a,b -> a+b) in place. This instance is
     * modified to contain the new value.
     *
     * @param b the second vector.
     * @return the sum of the two vectors.
     */
    public Coordinates plusIP(Coordinates b) {
        return plusComp(b, this, Integer.MAX_VALUE);
    }

    /**
     * Computes the sum of two vectors (a,b -> a+b). This instance is not
     * modified. The result will have the same dimensions than the implicit
     * parameter. The operation is performed only on the first dimensions as
     * specified by the input parameter.
     *
     * @param b the second vector.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the sum of two vectors.
     */
    public Coordinates restrPlus(Coordinates b, int onDimensions) {
        int resultDim = Math.min(Math.max(dim(), b.dim()), onDimensions);
        return plusComp(b, new Coordinates(resultDim), onDimensions);
    }

    /**
     * Computes the sum of two vectors (a,b -> a+b) in place. This instance is
     * modified to contain the new value. The operation is performed only on the
     * first dimensions as specified by the input parameter.
     *
     * @param b the second vector.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the sum of the two vectors.
     */
    public Coordinates restrPlusIP(Coordinates b, int onDimensions) {
        return plusComp(b, this, onDimensions);
    }

    private Coordinates plusComp(Coordinates b, Coordinates result, int onDimensions) {
        for (int i = 0; i < Math.min(Math.max(dim(), b.dim()), onDimensions); i++) {
            result.setAt(i, get(i) + b.get(i));
        }
        return result;
    }

    /**
     * Computes the difference of two vectors (a,b -> a-b). This instance is not
     * modified.
     *
     * @param b the second vector.
     * @return the difference of the two vectors.
     */
    public Coordinates minus(Coordinates b) {
        return minusComp(b, new Coordinates(dim()), Integer.MAX_VALUE);
    }

    /**
     * Computes the difference of two vectors (a,b -> a-b) in place. This
     * instance is modified to contain the new value.
     *
     * @param b the second vector.
     * @return the difference of the two vectors.
     */
    public Coordinates minusIP(Coordinates b) {
        return minusComp(b, this, Integer.MAX_VALUE);
    }

    /**
     * Computes the difference of two vectors (a,b -> a-b). This instance is not
     * modified. The operation is performed only on the first dimensions as
     * specified by the input parameter.
     *
     * @param b the second vector.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the difference of the two vectors.
     */
    public Coordinates restrMinus(Coordinates b, int onDimensions) {
        int resultDim = Math.min(Math.max(dim(), b.dim()), onDimensions);
        return minusComp(b, new Coordinates(resultDim), onDimensions);
    }

    /**
     * Computes the difference of two vectors (a,b -> a-b) in place. This
     * instance is modified to contain the new value. The operation is performed
     * only on the first dimensions as specified by the input parameter.
     *
     * @param b the second vector.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the difference of the two vectors.
     */
    public Coordinates restrMinusIP(Coordinates b, int onDimensions) {
        return minusComp(b, this, onDimensions);
    }

    private Coordinates minusComp(Coordinates b, Coordinates result, int onDimensions) {
        for (int i = 0; i < Math.min(Math.max(dim(), b.dim()), onDimensions); i++) {
            result.setAt(i, get(i) - b.get(i));
        }
        return result;
    }

    /**
     * Computes the scalar product of this vector (a,s -> a*s). This instance is
     * not modified.
     *
     * @param scalar the scalar factor.
     * @return the scalar product of this vector.
     */
    public Coordinates times(double scalar) {
        return timesComp(scalar, new Coordinates(dim()), Integer.MAX_VALUE);
    }

    /**
     * Computes the scalar product of of this vector (a,s -> a*s) in place. This
     * instance is modified to contain the new value.
     *
     * @param scalar the scalar factor.
     * @return the scalar product of this vector.
     */
    public Coordinates timesIP(double scalar) {
        return timesComp(scalar, this, Integer.MAX_VALUE);
    }

    /**
     * Computes the scalar product of this vector (a,s -> a*s). This instance is
     * not modified. The operation is performed only on the first dimensions as
     * specified by the input parameter.
     *
     * @param scalar the scalar factor.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the scalar product of this vector.
     */
    public Coordinates restrTimes(double scalar, int onDimensions) {
        int resultDim = Math.min(dim(), onDimensions);
        return timesComp(scalar, new Coordinates(resultDim), onDimensions);
    }

    /**
     * Computes the scalar product of of this vector (a,s -> a*s) in place. This
     * instance is modified to contain the new value. The operation is performed
     * only on the first dimensions as specified by the input parameter.
     *
     * @param scalar the scalar factor.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the scalar product of this vector.
     */
    public Coordinates restrTimesIP(double scalar, int onDimensions) {
        return timesComp(scalar, this, onDimensions);
    }

    private Coordinates timesComp(double scalar, Coordinates result, int onDimensions) {
        for (int i = 0; i < Math.min(dim(), onDimensions); i++) {
            result.setAt(i, get(i) * scalar);
        }
        return result;
    }

    /**
     * Computes the scalar division of this vector (a,s -> a/s). This instance
     * is not modified.
     *
     * @param scalar the scalar factor.
     * @return the scalar division of this vector.
     */
    public Coordinates divide(double scalar) {
        return divideComp(scalar, new Coordinates(dim()), Integer.MAX_VALUE);
    }

    /**
     * Computes the scalar division of this vector (a,s -> a/s) in place. This
     * instance is modified to contain the new value.
     *
     * @param scalar the scalar factor.
     * @return the scalar division of this vector.
     */
    public Coordinates divideIP(double scalar) {
        return divideComp(scalar, this, Integer.MAX_VALUE);
    }

    /**
     * Computes the scalar division of this vector (a,s -> a/s). This instance
     * is not modified. The operation is performed only on the first dimensions
     * as specified by the input parameter.
     *
     * @param scalar the scalar factor.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the scalar division of this vector.
     */
    public Coordinates restrDivide(double scalar, int onDimensions) {
        int resultDim = Math.min(dim(), onDimensions);
        return divideComp(scalar, new Coordinates(resultDim), onDimensions);
    }

    /**
     * Computes the scalar division of this vector (a,s -> a/s) in place. This
     * instance is modified to contain the new value. The operation is performed
     * only on the first dimensions as specified by the input parameter.
     *
     * @param scalar the scalar factor.
     * @param onDimensions the number of dimensions affected by the operation.
     * @return the scalar division of this vector.
     */
    public Coordinates restrDivideIP(double scalar, int onDimensions) {
        return divideComp(scalar, this, onDimensions);
    }

    private Coordinates divideComp(double scalar, Coordinates result, int onDimensions) {
        return timesComp(1.0 / scalar, result, onDimensions);
    }

    /**
     * Parses a string representation of Coordinates into a Coordinates object.
     *
     * @param string the string description.
     * @return the coordinates object.
     */
    public static Coordinates parse(String string) {
        String clearedString = string.replaceAll("[\\(\\) ]", "");
        String[] tokens = clearedString.split(",");
        Coordinates output = new Coordinates(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            output.setAt(i, Double.parseDouble(tokens[i]));
        }
        return output;
    }

    /**
     * Orders Coordinates from left to right.
     */
    public static class LeftMostComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(a.x(), b.x());
        }
    }

    /**
     * Orders Coordinates from bottom to top.
     */
    public static class BottomMostComparator implements Comparator<Coordinates> {

        @Override
        public int compare(Coordinates a, Coordinates b) {
            return Double.compare(a.y(), b.y());
        }
    }
}
