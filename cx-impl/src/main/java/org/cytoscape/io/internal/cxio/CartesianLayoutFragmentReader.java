package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class CartesianLayoutFragmentReader implements AspectFragmentReader {

    private static final boolean STRICT = true;

    public static CartesianLayoutFragmentReader createInstance() {
        return new CartesianLayoutFragmentReader();
    }

    private CartesianLayoutFragmentReader() {
    }

    @Override
    public String getAspectName() {
        return CxConstants.CARTESIAN_LAYOUT;
    }

    @Override
    public List<AspectElement> readAspectFragment(final JsonParser jp) throws IOException {
        JsonToken t = jp.nextToken();
        if (t != JsonToken.START_ARRAY) {
            throw new IOException("malformed cx json in '" + CxConstants.EDGES + "'");
        }
        final List<AspectElement> layout_aspects = new ArrayList<AspectElement>();
        while (t != JsonToken.END_ARRAY) {
            if (t == JsonToken.START_OBJECT) {
                String node_id = null;
                String x = null;
                String y = null;
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    final String namefield = jp.getCurrentName();
                    jp.nextToken(); // move to value
                    if (CxConstants.NODE.equals(namefield)) {
                        node_id = jp.getText().trim();
                    }
                    else if (CxConstants.X.equals(namefield)) {

                        x = jp.getValueAsString();
                    }
                    else if (CxConstants.Y.equals(namefield)) {
                        y = jp.getValueAsString();
                    }
                    else if (STRICT) {
                        throw new IOException("malformed cx json: unrecognized field '" + namefield
                                + "'");
                    }
                }
                if (Util.isEmpty(node_id)) {
                    throw new IOException(
                            "malformed cx json: node id in cartesian layout is missing");
                }
                if (x == null) {
                    throw new IOException(
                            "malformed cx json: x coordinate in cartesian layout is missing");
                }
                if (y == null) {
                    throw new IOException(
                            "malformed cx json: y coordinate in cartesian layout is missing");
                }
                layout_aspects.add(new CartesianLayoutElement(node_id, x, y));
            }
            t = jp.nextToken();
        }

        return layout_aspects;
    }

}
