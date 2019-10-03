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
package ocotillo.dygraph.rendering;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import ocotillo.geometry.Interval;

/**
 * Provides support for frame computation in a dynamic graph.
 */
public class Animation {

    private final Interval playedInterval;
    private final Duration duration;
    private final int framesPerSecond;

    private final List<Double> frames;

    /**
     * Builds an animation.
     *
     * @param playedInterval the interval to play.
     * @param duration the duration of the animation.
     */
    public Animation(Interval playedInterval, Duration duration) {
        this.playedInterval = playedInterval;
        this.duration = duration;
        this.framesPerSecond = 25;
        this.frames = computeFrames();
    }

    /**
     * Builds an animation.
     *
     * @param playedInterval the interval to play.
     * @param duration the duration of the animation.
     * @param framesPerSecond the number of frames per second.
     */
    public Animation(Interval playedInterval, Duration duration, int framesPerSecond) {
        this.playedInterval = playedInterval;
        this.duration = duration;
        this.framesPerSecond = framesPerSecond;
        this.frames = computeFrames();
    }

    /**
     * Computes the interval point to be used for the snapshots.
     *
     * @return the list of snapshot points.
     */
    private List<Double> computeFrames() {
        assert (playedInterval.leftBound() != Double.NEGATIVE_INFINITY
                && playedInterval.rightBound() != Double.POSITIVE_INFINITY) : "Animating interval with infinite bounds.";
        int numberOfFrames = Math.max(framesPerSecond * (int) duration.getSeconds(), 1);
        int numberOfDivisions = numberOfFrames - 1;
        int startOffset = 0;
        if (!playedInterval.isLeftClosed()) {
            numberOfDivisions++;
            startOffset = 1;
        }
        if (!playedInterval.isRightClosed()) {
            numberOfDivisions++;
        }
        double step = playedInterval.width() / numberOfDivisions;
        List<Double> result = new ArrayList<>(numberOfFrames);
        for (int i = startOffset; i < numberOfFrames + startOffset; i++) {
            result.add(playedInterval.leftBound() + i * step);
        }
        return result;
    }

    /**
     * Returns the interval to be played.
     *
     * @return the interval to be played.
     */
    public Interval playedInterval() {
        return playedInterval;
    }

    /**
     * Returns the duration of the animation.
     *
     * @return the duration.
     */
    public Duration duration() {
        return duration;
    }

    /**
     * Returns the number of frames per second used in the animation.
     *
     * @return the number of frames per second.
     */
    public int framesPerSecond() {
        return framesPerSecond;
    }

    /**
     * Returns the interval point to be used for the snapshots.
     *
     * @return the list of snapshot points.
     */
    public List<Double> frames() {
        return frames;
    }
}
