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
package ocotillo.dygraph;

import ocotillo.geometry.Geom;

/**
 * Defines a way to interpolate between the initial and the final value of an
 * interval. The interpolation strategy is a function from [0,1] to [0,1] that
 * indicates to what degree the output should be similar to the interval
 * beginning (y=0) or the interval end (y=1) when moving in the interval.
 */
public abstract class Interpolation {

    private String name;

    /**
     * Returns the interpolated value for an x value in [0,1].
     *
     * @param x the x value.
     * @return the y value of the interpolation function for the given x.
     */
    public abstract double valueAt(double x);

    /**
     * Returns the interpolation name.
     *
     * @return the interpolation name.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the interpolation name.
     *
     * @param name the interpolation name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Collection of standard interpolations.
     */
    public enum Std {
        /**
         * A interpolation function that always return the initial output value
         * for all but 1.
         */
        constant(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                if (Geom.eXD.almostEqual(x, 1.0)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }),
        /**
         * A interpolation function that returns the closest between initial and
         * final output value.
         */
        step(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                if (x <= 0.5) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }),
        /**
         * Linear interpolation.
         */
        linear(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return x;
            }
        }),
        /**
         * Smooth step interpolation.
         */
        smoothStep(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return x * x * (3 - 2 * x);
            }
        }),
        /**
         * Smoother step interpolation.
         */
        smootherStep(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return x * x * x * (x * (x * 6 - 15) + 10);
            }
        }),
        /**
         * Large Gaussian interpolation (sigma = 0.15).
         */
        largeGaussian(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                double sigma = 0.15;
                double dist = x - 0.5;
                return Math.exp(-(dist * dist) / (2 * sigma * sigma));
            }
        }),
        /**
         * Medium Gaussian interpolation (sigma = 0.10).
         */
        mediumGaussian(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                double sigma = 0.1;
                double dist = x - 0.5;
                return Math.exp(-(dist * dist) / (2 * sigma * sigma));
            }
        }),
        /**
         * Small Gaussian interpolation (sigma = 0.05).
         */
        smallGaussian(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                double sigma = 0.05;
                double dist = x - 0.5;
                return Math.exp(-(dist * dist) / (2 * sigma * sigma));
            }
        }),
        /**
         * Slow charge interpolation (time constant = 0.2).
         */
        slowCharge(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return 1 - Math.exp(-x / 0.2);
            }
        }),
        /**
         * Medium charge interpolation (time constant = 0.1).
         */
        mediumCharge(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return 1 - Math.exp(-x / 0.1);
            }
        }),
        /**
         * Fast charge interpolation (time constant = 0.05).
         */
        fastCharge(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return 1 - Math.exp(-x / 0.05);
            }
        }),
        /**
         * Slow end charge interpolation (time constant = 0.2).
         */
        slowEndCharge(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return Math.exp((x - 1) / 0.2);
            }
        }),
        /**
         * Medium end charge interpolation (time constant = 0.1).
         */
        mediumEndCharge(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return Math.exp((x - 1) / 0.1);
            }
        }),
        /**
         * Fast end charge interpolation (time constant = 0.05).
         */
        fastEndCharge(new Interpolation() {
            @Override
            public double valueAt(double x) {
                assert (0 <= x && x <= 1) : "Interpolating a value outside [0,1]";
                return Math.exp((x - 1) / 0.05);
            }
        });

        private final Interpolation interpolation;

        /**
         * Builds the standard interpolation.
         *
         * @param interpolation
         */
        private Std(Interpolation interpolation) {
            this.interpolation = interpolation;
            interpolation.setName(name());
        }

        /**
         * Gets the actual interpolation.
         *
         * @return
         */
        public Interpolation get() {
            return interpolation;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
