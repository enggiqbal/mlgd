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
package ocotillo.various;

import java.awt.Color;

/**
 * A collection of colours.
 */
public abstract class ColorCollection {

    public static final ColorCollection cbQualitativeSolid = new ColorBrewerQualitativeSolid();
    public static final ColorCollection cbQualitativePastel = new ColorBrewerQualitativePastel();

    /**
     * Provides the colour with given index.
     *
     * @param colorIndex the colour index.
     * @return the colour.
     */
    public abstract Color get(int colorIndex);

    /**
     * Solid qualitative colour collection provided by ColorBrewer 2.0.
     */
    private static class ColorBrewerQualitativeSolid extends ColorCollection {

        @Override
        public Color get(int colorIndex) {
            switch (colorIndex) {
                case 0:
                    return new Color(166, 206, 227);
                case 1:
                    return new Color(31, 120, 180);
                case 2:
                    return new Color(178, 223, 138);
                case 3:
                    return new Color(51, 160, 44);
                case 4:
                    return new Color(251, 154, 153);
                case 5:
                    return new Color(227, 26, 28);
                case 6:
                    return new Color(253, 191, 111);
                case 7:
                    return new Color(255, 127, 0);
                case 8:
                    return new Color(202, 178, 214);
                case 9:
                    return new Color(106, 61, 154);
                case 10:
                    return new Color(255, 255, 153);
                case 11:
                    return new Color(177, 89, 40);
                default:
                    return Color.BLACK;
            }
        }
    }

    /**
     * Pastel qualitative colour collection provided by ColorBrewer 2.0.
     */
    private static class ColorBrewerQualitativePastel extends ColorCollection {

        @Override
        public Color get(int colorIndex) {
            switch (colorIndex) {
                case 0:
                    return new Color(141, 211, 199);
                case 1:
                    return new Color(255, 255, 179);
                case 2:
                    return new Color(190, 186, 218);
                case 3:
                    return new Color(251, 128, 114);
                case 4:
                    return new Color(128, 177, 211);
                case 5:
                    return new Color(253, 180, 98);
                case 6:
                    return new Color(179, 222, 105);
                case 7:
                    return new Color(252, 205, 229);
                case 8:
                    return new Color(217, 217, 217);
                case 9:
                    return new Color(188, 128, 189);
                case 10:
                    return new Color(204, 235, 197);
                case 11:
                    return new Color(255, 237, 111);
                default:
                    return Color.BLACK;
            }
        }
    }
}
