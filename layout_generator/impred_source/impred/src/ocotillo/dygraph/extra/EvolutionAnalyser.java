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
package ocotillo.dygraph.extra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ocotillo.dygraph.Evolution;
import ocotillo.dygraph.Function;
import ocotillo.dygraph.FunctionConst;
import ocotillo.dygraph.FunctionRect;
import ocotillo.dygraph.Interpolation;
import ocotillo.geometry.Geom;
import ocotillo.geometry.Interval;

/**
 * Class that provides several type of analysis on an evolution.
 */
public class EvolutionAnalyser {

    /**
     * Converts the functions of a non-interpolating evolution into constant
     * ones. A non-interpolating evolution is an evolution that does not involve
     * value interpolation, as one that contains constant functions or
     * rectilinear functions with constant or step interpolation.
     *
     * @param <T> the type of data.
     * @param evolution the non-interpolating evolution.
     * @return the list of constant functions that replicate the given
     * evolution.
     */
    public static <T> List<FunctionConst<T>> convertToConstFunctions(Evolution<T> evolution) {
        List<FunctionConst<T>> result = new ArrayList<>();
        for (Function<T> function : evolution) {
            if (function instanceof FunctionConst) {
                result.add((FunctionConst<T>) function);
            } else if (function instanceof FunctionRect) {
                FunctionRect<T> rectFunction = (FunctionRect<T>) function;
                T leftValue = rectFunction.leftValue();
                T rightValue = rectFunction.rightValue();
                double leftIntervalBound = rectFunction.interval().leftBound();
                double rightIntervalBound = rectFunction.interval().rightBound();
                boolean isLeftClosed = rectFunction.interval().isLeftClosed();
                boolean isRightClosed = rectFunction.interval().isRightClosed();
                if (rectFunction.interpolation() == Interpolation.Std.constant.get()) {
                    if (leftValue == rightValue || !isRightClosed) {
                        result.add(new FunctionConst<>(
                                Interval.newCustom(leftIntervalBound, rightIntervalBound, isLeftClosed, isRightClosed),
                                leftValue));
                    } else {
                        result.add(new FunctionConst<>(
                                Interval.newCustom(leftIntervalBound, rightIntervalBound, isLeftClosed, false),
                                leftValue));
                        result.add(new FunctionConst<>(
                                Interval.newClosed(rightIntervalBound, rightIntervalBound),
                                rightValue));
                    }
                } else if (rectFunction.interpolation() == Interpolation.Std.step.get()) {
                    double centre = (leftIntervalBound + rightIntervalBound) / 2.0;
                    result.add(new FunctionConst<>(
                            Interval.newCustom(leftIntervalBound, centre, isLeftClosed, true),
                            leftValue));
                    result.add(new FunctionConst<>(
                            Interval.newCustom(centre, rightIntervalBound, false, isRightClosed),
                            rightValue));
                } else {
                    throw new UnsupportedOperationException("This interpolation is not supported yet: " + rectFunction.interpolation());
                }
            } else {
                throw new UnsupportedOperationException("This function is not supported yet: " + function);
            }
        }
        return result;
    }

    /**
     * Merges the functions of a non-interpolating evolution in a sequence of
     * constant non-overlapping functions. A non-interpolating evolution is an
     * evolution that does not involve value interpolation, as one that contains
     * constant functions or rectilinear functions with constant or step
     * interpolation.
     *
     * @param <T> the type of data handled.
     * @param evolution the non-interpolating evolution.
     * @return the equivalent evolution formed by merged constant functions.
     */
    public static <T> Evolution<T> mergeFunctions(Evolution<T> evolution) {
        List<FunctionConst<T>> convertedFunc = convertToConstFunctions(evolution);
        List<FunctionConst<T>> mergedFunc = new ArrayList<>();
        FunctionConst<T> current = null;
        for (FunctionConst<T> next : convertedFunc) {
            if (current == null) {
                current = next;
            } else {
                if (canMerge(current, next)) {
                    current = new FunctionConst<>(
                            current.interval().fusion(next.interval()),
                            current.leftValue());
                } else {
                    mergedFunc.add(current);
                    current = next;
                }
            }
        }
        if (current != null) {
            mergedFunc.add(current);
        }

        T defaultValue = evolution.getDefaultValue();
        Evolution<T> result = new Evolution<>(defaultValue);
        for (FunctionConst<T> function : mergedFunc) {
            if (function.leftValue() != defaultValue) {
                result.insert(function);
            }
        }
        return result;
    }

    /**
     * Checks if two constant functions can be merged.
     *
     * @param <T> the type of data handled.
     * @param a the first function.
     * @param b the second function.
     * @return true if the functions can merge, false otherwise.
     */
    private static <T> boolean canMerge(FunctionConst<T> a, FunctionConst<T> b) {
        if (a.interval().overlapsWith(b.interval())) {
            assert (Objects.equals(a.leftValue(), b.leftValue())) :
                    "Discording overlapping values for: " + a + " and: " + b;
            return true;
        } else if (Geom.eXD.almostEqual(a.interval().rightBound(), b.interval().leftBound())
                && a.rightValue() == b.leftValue()
                && (a.interval().isRightClosed() || b.interval().isLeftClosed())) {
            return true;
        }
        return false;
    }

    /**
     * Given a non-interpolating evolution and a value, returns the intervals in
     * which the original evolution assumes that value.
     *
     * @param <T> the type of data handled.
     * @param evolution the original evolution.
     * @param value the desired value.
     * @return the evolution for that value.
     */
    public static <T> List<Interval> getIntervalsWithValue(Evolution<T> evolution, T value) {
        Evolution<T> mergedEvolution = mergeFunctions(evolution);
        List<Interval> result = new ArrayList<>();
        if (evolution.getDefaultValue() != value) {
            for (Function<T> function : mergedEvolution) {
                if (function.leftValue() == value) {
                    result.add(function.interval());
                }
            }
        } else {
            double newLeftBound = Double.NEGATIVE_INFINITY;
            boolean newLeftClosed = false;
            for (Function<T> next : mergedEvolution) {
                Interval nextInterval = next.interval();
                if (nextInterval.leftBound() != Double.NEGATIVE_INFINITY) {
                    if (!Geom.eXD.almostEqual(newLeftBound, nextInterval.leftBound())
                            || (newLeftClosed && !nextInterval.isLeftClosed())) {
                        result.add(Interval.newCustom(newLeftBound, nextInterval.leftBound(),
                                newLeftClosed, !nextInterval.isLeftClosed()));
                    }
                }
                newLeftBound = nextInterval.rightBound();
                newLeftClosed = !nextInterval.isRightClosed();
            }
            if (newLeftBound != Double.POSITIVE_INFINITY) {
                result.add(Interval.newCustom(newLeftBound, Double.POSITIVE_INFINITY,
                        newLeftClosed, false));
            }
        }
        return result;
    }
}
