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
package ocotillo.serialization.dot;

import ocotillo.graph.StdAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of settings indicating how to handle the attributes in a
 * conversion from either graph to dot or dot to graph.
 */
public class ConversionSettings {

    /**
     * Collects the attributes to be converted.
     */
    protected final List<AttributeConvSettings> toConvert = new ArrayList<>();

    /**
     * Collects the attributes to be ignored.
     */
    protected final List<String> toIgnore = new ArrayList<>();

    /**
     * Indicates how to handle the attributes which are not explicitely marked
     * to be converted or ignored.
     */
    protected boolean saveUnspecified = false;

    /**
     * Conversion settings that only allow to specify the attributes to be
     * converted.
     */
    public static class ConvertOnly extends ConversionSettings {

        /**
         * Converts an attribute using an auto-detected converter. All
         * conversions from dot to graph are done as strings.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttrId the destination attribute id.
         * @return these conversion settings.
         */
        public ConvertOnly convert(String sourceAttrId, String destAttrId) {
            toConvert.add(new AttributeConvSettings(sourceAttrId, destAttrId));
            return this;
        }

        /**
         * Converts an attribute using an auto-detected converter. All
         * conversions from dot to graph are done as strings.
         *
         * @param sourceAttr the source attribute.
         * @param destAttrId the destination attribute id.
         * @return these conversion settings.
         */
        public ConvertOnly convert(StdAttribute sourceAttr, String destAttrId) {
            return convert(sourceAttr.name(), destAttrId);
        }

        /**
         * Converts an attribute using an auto-detected converter. All
         * conversions from dot to graph are done as strings.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttr the destination attribute.
         * @return these conversion settings.
         */
        public ConvertOnly convert(String sourceAttrId, StdAttribute destAttr) {
            return convert(sourceAttrId, destAttr.name());
        }

        /**
         * Converts an attribute using the given converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttrId the destination attribute id.
         * @param converter the converter.
         * @return these conversion settings.
         */
        public ConvertOnly convert(String sourceAttrId, String destAttrId, DotValueConverter<?> converter) {
            toConvert.add(new AttributeConvSettings(sourceAttrId, destAttrId, converter));
            return this;
        }

        /**
         * Converts an attribute using the given converter.
         *
         * @param sourceAttr the source attribute.
         * @param destAttrId the destination attribute id.
         * @param converter the converter.
         * @return these conversion settings.
         */
        public ConvertOnly convert(StdAttribute sourceAttr, String destAttrId, DotValueConverter<?> converter) {
            return convert(sourceAttr.name(), destAttrId, converter);
        }

        /**
         * Converts an attribute using the given converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttr the destination attribute.
         * @param converter the converter.
         * @return these conversion settings.
         */
        public ConvertOnly convert(String sourceAttrId, StdAttribute destAttr, DotValueConverter<?> converter) {
            return convert(sourceAttrId, destAttr.name(), converter);
        }

        /**
         * Converts an attribute using a default converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttrId the destination attribute id.
         * @param type the type of value to convert.
         * @return these conversion settings.
         */
        public ConvertOnly convert(String sourceAttrId, String destAttrId, Class<?> type) {
            toConvert.add(new AttributeConvSettings(sourceAttrId, destAttrId, type));
            return this;
        }

        /**
         * Converts an attribute using a default converter.
         *
         * @param sourceAttr the source attribute.
         * @param destAttrId the destination attribute id.
         * @param type the type of value to convert.
         * @return these conversion settings.
         */
        public ConvertOnly convert(StdAttribute sourceAttr, String destAttrId, Class<?> type) {
            return convert(sourceAttr.name(), destAttrId, type);
        }

        /**
         * Converts an attribute using a default converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttr the destination attribute.
         * @param type the type of value to convert.
         * @return these conversion settings.
         */
        public ConvertOnly convert(String sourceAttrId, StdAttribute destAttr, Class<?> type) {
            return convert(sourceAttrId, destAttr.name(), type);
        }

    }

    public static class AllOperations extends ConversionSettings {

        /**
         * Converts an attribute using an auto-detected converter. Works only
         * from graph to dot.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttrId the destination attribute id.
         * @return these conversion settings.
         */
        public AllOperations convert(String sourceAttrId, String destAttrId) {
            toConvert.add(new AttributeConvSettings(sourceAttrId, destAttrId));
            return this;
        }

        /**
         * Converts an attribute using an auto-detected converter. Works only
         * from graph to dot.
         *
         * @param sourceAttr the source attribute.
         * @param destAttrId the destination attribute id.
         * @return these conversion settings.
         */
        public AllOperations convert(StdAttribute sourceAttr, String destAttrId) {
            return convert(sourceAttr.name(), destAttrId);
        }

        /**
         * Converts an attribute using an auto-detected converter. All
         * conversions from dot to graph are done as strings.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttr the destination attribute.
         * @return these conversion settings.
         */
        public AllOperations convert(String sourceAttrId, StdAttribute destAttr) {
            return convert(sourceAttrId, destAttr.name());
        }

        /**
         * Converts an attribute using the given converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttrId the destination attribute id.
         * @param converter the converter.
         * @return these conversion settings.
         */
        public AllOperations convert(String sourceAttrId, String destAttrId, DotValueConverter<?> converter) {
            toConvert.add(new AttributeConvSettings(sourceAttrId, destAttrId, converter));
            return this;
        }

        /**
         * Converts an attribute using the given converter.
         *
         * @param sourceAttr the source attribute.
         * @param destAttrId the destination attribute id.
         * @param converter the converter.
         * @return these conversion settings.
         */
        public AllOperations convert(StdAttribute sourceAttr, String destAttrId, DotValueConverter<?> converter) {
            return convert(sourceAttr.name(), destAttrId, converter);
        }

        /**
         * Converts an attribute using the given converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttr the destination attribute.
         * @param converter the converter.
         * @return these conversion settings.
         */
        public AllOperations convert(String sourceAttrId, StdAttribute destAttr, DotValueConverter<?> converter) {
            return convert(sourceAttrId, destAttr.name(), converter);
        }

        /**
         * Converts an attribute using a default converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttrId the destination attribute id.
         * @param type the type of value to convert.
         * @return these conversion settings.
         */
        public AllOperations convert(String sourceAttrId, String destAttrId, Class<?> type) {
            toConvert.add(new AttributeConvSettings(sourceAttrId, destAttrId, type));
            return this;
        }

        /**
         * Converts an attribute using a default converter.
         *
         * @param sourceAttr the source attribute.
         * @param destAttrId the destination attribute id.
         * @param type the type of value to convert.
         * @return these conversion settings.
         */
        public AllOperations convert(StdAttribute sourceAttr, String destAttrId, Class<?> type) {
            return convert(sourceAttr.name(), destAttrId, type);
        }

        /**
         * Converts an attribute using a default converter.
         *
         * @param sourceAttrId the source attribute id.
         * @param destAttr the destination attribute.
         * @param type the type of value to convert.
         * @return these conversion settings.
         */
        public AllOperations convert(String sourceAttrId, StdAttribute destAttr, Class<?> type) {
            return convert(sourceAttrId, destAttr.name(), type);
        }

        /**
         * Ignores the attribute when required to save the unspecified
         * attributes.
         *
         * @param attrId the attribute to ignore.
         * @return these conversion settings.
         */
        public AllOperations ignore(String attrId) {
            toIgnore.add(attrId);
            return this;
        }

        /**
         * Ignores the attribute when required to save the unspecified
         * attributes.
         *
         * @param attribute the attribute to ignore.
         * @return these conversion settings.
         */
        public AllOperations ignore(StdAttribute attribute) {
            return ignore(attribute.name());
        }

        /**
         * Indicates whether to save or not the unspecified attributes.
         *
         * @param activate
         * @return these conversion settings.
         */
        public AllOperations saveUnspecified(Boolean activate) {
            saveUnspecified = activate;
            return this;
        }

    }

    /**
     * A collection of information detailing how to convert a dot attribute into
     * a graph object, and vice-versa.
     */
    protected static class AttributeConvSettings {

        protected String sourceAttrId;
        protected String destAttrId;
        protected Class<?> type;
        protected DotValueConverter<?> converter;

        protected AttributeConvSettings(String sourceAttrId, String destAttrId) {
            assert (sourceAttrId != null && !sourceAttrId.isEmpty() && destAttrId != null && !destAttrId.isEmpty()) : "The attribute id cannot be null or empty";
            this.sourceAttrId = sourceAttrId;
            this.destAttrId = destAttrId;
            this.type = null;
            this.converter = null;
        }

        protected AttributeConvSettings(String sourceAttrId, String destAttrId, Class<?> type) {
            assert (sourceAttrId != null && !sourceAttrId.isEmpty() && destAttrId != null && !destAttrId.isEmpty()) : "The attribute id cannot be null or empty";
            this.sourceAttrId = sourceAttrId;
            this.destAttrId = destAttrId;
            this.type = type;
            this.converter = null;
        }

        protected AttributeConvSettings(String sourceAttrId, String destAttrId, DotValueConverter<?> converter) {
            assert (sourceAttrId != null && !sourceAttrId.isEmpty() && destAttrId != null && !destAttrId.isEmpty()) : "The attribute id cannot be null or empty";
            this.sourceAttrId = sourceAttrId;
            this.destAttrId = destAttrId;
            this.type = null;
            this.converter = converter;
        }

    }

}
