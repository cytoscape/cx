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

}
