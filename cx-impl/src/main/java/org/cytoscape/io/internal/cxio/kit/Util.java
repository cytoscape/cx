package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public final class Util {

    final public static boolean isEmpty(final String s) {
        return (s == null) || (s.length() < 1);
    }

    final static List<String> parseSimpleList(final JsonParser jp, JsonToken t) throws IOException,
            JsonParseException {
        final List<String> elements = new ArrayList<String>();
        while (t != JsonToken.END_ARRAY) {
            if (t == JsonToken.VALUE_STRING) {
                elements.add(jp.getText());
            }
            else if (t != JsonToken.START_OBJECT) {
                throw new IOException("malformed cx json, expected " + JsonToken.START_OBJECT
                                      + ", got " + t);
            }
            t = jp.nextToken();
        }
        return elements;
    }

    public final static CxConstants.ATTRIBUTE_TYPE determineAttributeType(final String s) {
        if (s.equals(CxConstants.ATTRIBUTE_TYPE.STRING.toString())) {
            return CxConstants.ATTRIBUTE_TYPE.STRING;
        }
        else if (s.equals(CxConstants.ATTRIBUTE_TYPE.BOOLEAN.toString())) {
            return CxConstants.ATTRIBUTE_TYPE.BOOLEAN;
        }
        else if (s.equals(CxConstants.ATTRIBUTE_TYPE.DOUBLE.toString())) {
            return CxConstants.ATTRIBUTE_TYPE.DOUBLE;
        }
        else if (s.equals(CxConstants.ATTRIBUTE_TYPE.INTEGER.toString())) {
            return CxConstants.ATTRIBUTE_TYPE.INTEGER;
        }
        else if (s.equals(CxConstants.ATTRIBUTE_TYPE.LONG.toString())) {
            return CxConstants.ATTRIBUTE_TYPE.LONG;
        }
        else if (s.equals(CxConstants.ATTRIBUTE_TYPE.FLOAT.toString())) {
            return CxConstants.ATTRIBUTE_TYPE.FLOAT;
        }
        else {
            throw new IllegalArgumentException("unknown attribute type '" + s + "'");
        }

    }

}
