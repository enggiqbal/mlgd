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
package ocotillo.graph.rendering.svg;

/**
 * Facilitates the creation of an SVG file.
 */
public class SvgDocument {

    /**
     * Hard-coded vertical offset factor. This is necessary to obtain a decent
     * text vertical alignment as the baseline definitions are not universally
     * accepted.
     */
    public final double vOffsetFactor = 0.3;

    private final StringBuilder text = new StringBuilder();
    private int currentIndentation;

    /**
     * Constructs an empty SVG text.
     *
     * @param width the drawing width.
     * @param height the drawing height.
     */
    public SvgDocument(double width, double height) {
        text.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n\n");
        text.append("<svg width=\"").append(width).append("\" height=\"").append(height).append("\"");
        text.append(" font-family=\"Sans\" text-anchor=\"middle\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        currentIndentation++;
    }

    /**
     * Opens a group of SVG elements.
     */
    public void openGroup() {
        indent();
        text.append("<g\n");
        currentIndentation++;
    }

    /**
     * Closes a group of SVG elements.
     */
    public void closeGroup() {
        currentIndentation--;
        indent();
        text.append("<g\n");
    }

    /**
     * Appends an element to the current text.
     *
     * @param element the element.
     */
    public void addElement(SvgElement element) {
        indent();
        text.append(transformColorAlpha(element.toString()));
        text.append('\n');
    }

    /**
     * Closes and returns an SVG text.
     *
     * @return the SVG text.
     */
    public String close() {
        assert (currentIndentation == 1) : "The svg groups have not been opened/closed correctly.";
        text.append("</svg>");
        return text.toString();
    }

    /**
     * Indents a row according to the current indentation level.
     */
    private void indent() {
        for (int i = 0; i < currentIndentation; i++) {
            text.append("  ");
        }
    }

    /**
     * Transform a color alpha into the opacity attribute. SVG currently does
     * not support the format #00000000, so it is converted into the format
     * #000000 and opacity.
     *
     * @param string the original string.
     * @return the string with transformed alpha.
     */
    private static String transformColorAlpha(String originalString) {
        int i = originalString.indexOf("fill=");
        while (i != -1) {
            String fillString = originalString.substring(i, i + 16);

            StringBuilder replacement = new StringBuilder();
            replacement.append(fillString.substring(0, 13)).append("\" ");

            int alpha = Integer.parseInt(fillString.substring(13, 15), 16);
            double opacity = alpha / 255.0;
            replacement.append("opacity=\"").append(opacity).append("\"");

            originalString = originalString.substring(0, i) + replacement + originalString.substring(i+16);
            i = originalString.indexOf("fill=", i+1);
        }

        i = originalString.indexOf("stroke=");
        while (i != -1) {
            String strokeString = originalString.substring(i, i + 18);

            StringBuilder replacement = new StringBuilder();
            replacement.append(strokeString.substring(0, 15)).append("\" ");

            int alpha = Integer.parseInt(strokeString.substring(15, 17), 16);
            double opacity = alpha / 255.0;
            replacement.append("stroke-opacity=\"").append(opacity).append("\"");

            originalString = originalString.substring(0, i) + replacement + originalString.substring(i+18);
            i = originalString.indexOf("stroke=", i+1);
        }
        
        return originalString;
    }

}
