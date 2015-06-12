package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.cxio.kit.CxConstants.ATTRIBUTE_TYPE;

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

    public final static ATTRIBUTE_TYPE toType(final String s) {
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
            throw new IllegalArgumentException("type '" + s + "' is not supported");
        }
    }

    public final static ATTRIBUTE_TYPE determineType(final Object o) {

        if (o instanceof String) {
            return ATTRIBUTE_TYPE.STRING;
        }
        else if (o instanceof Boolean) {
            return ATTRIBUTE_TYPE.BOOLEAN;
        }
        else if (o instanceof Double) {
            return ATTRIBUTE_TYPE.DOUBLE;
        }
        else if (o instanceof Integer) {
            return ATTRIBUTE_TYPE.INTEGER;
        }
        else if (o instanceof Long) {
            return ATTRIBUTE_TYPE.LONG;
        }
        else if (o instanceof Float) {
            return ATTRIBUTE_TYPE.FLOAT;
        }
        else {
            throw new IllegalArgumentException("type '" + o.getClass() + "' is not supported");
        }
    }

}
