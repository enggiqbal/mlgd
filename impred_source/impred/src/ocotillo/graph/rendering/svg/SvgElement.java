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

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import ocotillo.geometry.Box;
import ocotillo.geometry.Coordinates;
import ocotillo.graph.rendering.ComponentDrawer;
import ocotillo.graph.rendering.RenderingTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Facilitates the creation of an SVG file.
 */
public abstract class SvgElement {

    protected String type = "";
    protected Map<String, Object> attributes;
    protected String content = "";

    /**
     * Parses an SVG definition to extract the SVG elements.
     *
     * @param definition the SVG definition.
     * @return the SVG elements.
     */
    public static List<SvgElement> parseSvg(String definition) {
        List<SvgElement> elements = new ArrayList<>();
        for (Element xmlElement : parseXmlElements(definition)) {
            switch (xmlElement.getTagName()) {
                case "rect":
                    elements.add(SvgRectangle.parse(xmlElement));
                    break;
                case "ellipse":
                    elements.add(SvgEllipse.parse(xmlElement));
                    break;
                case "polyline":
                    elements.add(SvgPolyline.parse(xmlElement));
                    break;
                case "text":
                    elements.add(SvgText.parse(xmlElement));
                    break;
                case "polygon":
                    elements.add(SvgPolygon.parse(xmlElement));
                    break;
                default:
                    throw new UnsupportedOperationException("The svg element " + xmlElement.getTagName() + " is unsupported.");
            }
        }
        return elements;
    }

    @Override
    public String toString() {
        if (attributes == null) {
            fillSvgFields();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<").append(type).append(" ");
        for (String attributeId : attributes.keySet()) {
            builder.append(attributeId).append("=\"").append(attributes.get(attributeId)).append("\" ");
        }
        builder.delete(builder.length() - 1, builder.length());
        if (content.isEmpty()) {
            builder.append("/>");
        } else {
            builder.append(">").append(content).append("</").append(type).append(">");
        }
        return builder.toString();
    }

    /**
     * Draws the SVG element itself into the given graphics.
     *
     * @param graphics the graphics.
     */
    public abstract void drawYourself(Graphics2D graphics);

    /**
     * Returns the box containing the element.
     *
     * @return the element box.
     */
    public abstract Box box();

    /**
     * Fills the SVG related fields if they have not been initialised yet.
     */
    protected abstract void fillSvgFields();

    /**
     * Parses a list of XML elements from their string definition.
     *
     * @param definition the XML definition.
     * @return the XML elements.
     */
    private static List<Element> parseXmlElements(String definition) {
        List<Element> elements = new ArrayList<>();
        String wrappedDefinition = "<wrapper> " + definition + " </wrapper>";
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(wrappedDefinition)));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node nNode = nodeList.item(i);
                if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    elements.add((Element) nodeList.item(i));
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.err.println("The following svg definition cannot be parsed and will be ignored: " + definition);
        }
        return elements;
    }

    /**
     * Converts a points SVG definition into a list of coordinates.
     *
     * @param pointsDefinition the points SVG definition.
     * @return the point coordinates.
     */
    private static List<Coordinates> parsePoints(String pointsDefinition) {
        List<Coordinates> points = new ArrayList<>();
        String[] pointStrings = pointsDefinition.split(" ");
        for (String pointString : pointStrings) {
            String[] coordinates = pointString.split(",");
            assert (coordinates.length == 2) : "The follwing polyline points definition is incorrect: " + pointsDefinition;
            points.add(new Coordinates(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
        }
        return points;
    }

    /**
     * Converts a list of coordinates into the SVG point definition.
     *
     * @param points the point coordinates.
     * @return the SVG points definition.
     */
    private static String writePoints(List<Coordinates> points) {
        StringBuilder builder = new StringBuilder();
        for (Coordinates point : points) {
            builder.append(point.x()).append(",").append(point.y()).append(" ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * SVG rectangle element.
     */
    public static class SvgRectangle extends SvgElement {

        public static final String typeName = "rect";

        protected final String id;
        protected final Coordinates center;
        protected final Coordinates size;
        protected final Color fillColor;

        public SvgRectangle(String id, Coordinates center, Coordinates size, Color fillColor) {
            this.id = id;
            this.center = center;
            this.size = size;
            this.fillColor = fillColor;
            type = typeName;
        }

        private static SvgRectangle parse(Element xmlElement) {
            assert (xmlElement.getTagName().equals(typeName)) : "Parsing a non-" + typeName + " svg element as " + typeName + ".";
            String id = xmlElement.getAttribute("id");
            double x = Double.parseDouble(xmlElement.getAttribute("x"));
            double y = Double.parseDouble(xmlElement.getAttribute("y"));
            double width = Double.parseDouble(xmlElement.getAttribute("width"));
            double height = Double.parseDouble(xmlElement.getAttribute("height"));
            Color fillColor = RenderingTools.colorHexReader(xmlElement.getAttribute("fill"));
            Coordinates size = new Coordinates(width, height);
            Coordinates center = size.divide(2).plus(new Coordinates(x, y));
            return new SvgRectangle(id, center, size, fillColor);
        }

        @Override
        protected void fillSvgFields() {
            attributes = new HashMap<>();
            Coordinates leftBottomCorner = center.minus(size.divide(2));
            attributes.put("id", id);
            attributes.put("x", leftBottomCorner.x());
            attributes.put("y", leftBottomCorner.y());
            attributes.put("width", size.x());
            attributes.put("height", size.y());
            attributes.put("fill", RenderingTools.colorHexWriter(fillColor));
        }

        @Override
        public void drawYourself(Graphics2D graphics) {
            ComponentDrawer.drawRectangle(graphics, center, size, fillColor);
        }

        @Override
        public Box box() {
            return new Box.Box2D(center, size);
        }

    }

    /**
     * SVG ellipse element.
     */
    public static class SvgEllipse extends SvgElement {

        public static final String typeName = "ellipse";

        protected final String id;
        protected final Coordinates center;
        protected final Coordinates size;
        protected final Color fillColor;

        public SvgEllipse(String id, Coordinates center, Coordinates size, Color fillColor) {
            this.id = id;
            this.center = center;
            this.size = size;
            this.fillColor = fillColor;
            type = typeName;
        }

        private static SvgEllipse parse(Element xmlElement) {
            assert (xmlElement.getTagName().equals(typeName)) : "Parsing a non-" + typeName + " svg element as " + typeName + ".";
            String id = xmlElement.getAttribute("id");
            double x = Double.parseDouble(xmlElement.getAttribute("cx"));
            double y = Double.parseDouble(xmlElement.getAttribute("cy"));
            double width = Double.parseDouble(xmlElement.getAttribute("rx"));
            double height = Double.parseDouble(xmlElement.getAttribute("ry"));
            Color fillColor = RenderingTools.colorHexReader(xmlElement.getAttribute("fill"));
            Coordinates center = new Coordinates(x, y);
            Coordinates size = new Coordinates(width, height).times(2);
            return new SvgEllipse(id, center, size, fillColor);
        }

        @Override
        protected void fillSvgFields() {
            attributes = new HashMap<>();
            attributes.put("id", id);
            attributes.put("cx", center.x());
            attributes.put("cy", center.y());
            Coordinates radii = size.divide(2);
            attributes.put("rx", radii.x());
            attributes.put("ry", radii.y());
            attributes.put("fill", RenderingTools.colorHexWriter(fillColor));
        }

        @Override
        public void drawYourself(Graphics2D graphics) {
            ComponentDrawer.drawEllipse(graphics, center, size, fillColor);
        }

        @Override
        public Box box() {
            return new Box.Box2D(center, size);
        }

    }

    /**
     * SVG text element.
     */
    public static class SvgText extends SvgElement {

        public static final String typeName = "text";
        private static final double vOffsetFactor = 0.3;

        protected final String id;
        protected final String text;
        protected final Coordinates center;
        protected final double dimension;
        protected final Color fillColor;

        public SvgText(String id, String text, Coordinates center, double dimension, Color fillColor) {
            this.id = id;
            this.text = text;
            this.center = center;
            this.dimension = dimension;
            this.fillColor = fillColor;
            type = typeName;
        }

        private static SvgText parse(Element xmlElement) {
            assert (xmlElement.getTagName().equals(typeName)) : "Parsing a non-" + typeName + " svg element as " + typeName + ".";
            String id = xmlElement.getAttribute("id");
            String text = xmlElement.getTextContent();
            double x = Double.parseDouble(xmlElement.getAttribute("x"));
            double y = Double.parseDouble(xmlElement.getAttribute("y"));
            double dimension = Double.parseDouble(xmlElement.getAttribute("font-size"));
            Color fillColor = RenderingTools.colorHexReader(xmlElement.getAttribute("fill"));
            Coordinates center = new Coordinates(x, y - dimension * vOffsetFactor);
            return new SvgText(id, text, center, dimension, fillColor);
        }

        @Override
        protected void fillSvgFields() {
            attributes = new HashMap<>();
            attributes.put("id", id);
            attributes.put("x", center.x());
            attributes.put("y", center.y() + dimension * vOffsetFactor);
            attributes.put("font-size", dimension);
            attributes.put("fill", RenderingTools.colorHexWriter(fillColor));
            content = text;
        }

        @Override
        public void drawYourself(Graphics2D graphics) {
            ComponentDrawer.drawText(graphics, text, center, dimension, fillColor);
        }

        @Override
        public Box box() {
            return RenderingTools.textBox(text, center, dimension);
        }

    }

    /**
     * SVG polyline element.
     */
    public static class SvgPolyline extends SvgElement {

        public static final String typeName = "polyline";

        protected final String id;
        protected final List<Coordinates> points;
        protected final double width;
        protected final Color strokeColor;

        public SvgPolyline(String id, List<Coordinates> points, double width, Color strokeColor) {
            this.id = id;
            this.points = points;
            this.width = width;
            this.strokeColor = strokeColor;
            type = typeName;
        }

        private static SvgPolyline parse(Element xmlElement) {
            assert (xmlElement.getTagName().equals(typeName)) : "Parsing a non-" + typeName + " svg element as " + typeName + ".";
            String id = xmlElement.getAttribute("id");
            List<Coordinates> points = parsePoints(xmlElement.getAttribute("points"));
            double width = Double.parseDouble(xmlElement.getAttribute("stroke-width"));
            Color strokeColor = RenderingTools.colorHexReader(xmlElement.getAttribute("stroke"));
            return new SvgPolyline(id, points, width, strokeColor);
        }

        @Override
        protected void fillSvgFields() {
            attributes = new HashMap<>();
            attributes.put("id", id);
            attributes.put("stroke-width", width);
            attributes.put("stroke", RenderingTools.colorHexWriter(strokeColor));
            attributes.put("points", writePoints(points));
        }

        @Override
        public void drawYourself(Graphics2D graphics) {
            ComponentDrawer.drawPolyline(graphics, points, width, strokeColor);
        }

        @Override
        public Box box() {
            return Box.boundingBox(points);
        }

    }

    /**
     * SVG polygon element.
     */
    public static class SvgPolygon extends SvgElement {

        public static final String typeName = "polygon";

        protected final String id;
        protected final List<Coordinates> points;
        protected final Color fillColor;
        protected final double strokeWidth;
        protected final Color strokeColor;

        public SvgPolygon(String id, List<Coordinates> points, Color fillColor, double strokeWidth, Color strokeColor) {
            this.id = id;
            this.points = points;
            this.fillColor = fillColor;
            this.strokeWidth = strokeWidth;
            this.strokeColor = strokeColor;
            type = typeName;
        }

        private static SvgPolygon parse(Element xmlElement) {
            assert (xmlElement.getTagName().equals(typeName)) : "Parsing a non-" + typeName + " svg element as " + typeName + ".";
            String id = xmlElement.getAttribute("id");
            Color fillColor = RenderingTools.colorHexReader(xmlElement.getAttribute("fill"));
            double width = Double.parseDouble(xmlElement.getAttribute("stroke-width"));
            Color strokeColor = RenderingTools.colorHexReader(xmlElement.getAttribute("stroke"));
            List<Coordinates> points = parsePoints(xmlElement.getAttribute("points"));
            return new SvgPolygon(id, points, fillColor, width, strokeColor);
        }

        @Override
        protected void fillSvgFields() {
            attributes = new HashMap<>();
            attributes.put("id", id);
            attributes.put("fill", RenderingTools.colorHexWriter(fillColor));
            attributes.put("stroke-width", strokeWidth);
            attributes.put("stroke", RenderingTools.colorHexWriter(strokeColor));
            attributes.put("points", writePoints(points));
        }

        @Override
        public void drawYourself(Graphics2D graphics) {
            ComponentDrawer.drawPolygon(graphics, points, fillColor, strokeWidth, strokeColor);
        }

        @Override
        public Box box() {
            return Box.boundingBox(points);
        }
    }
}
