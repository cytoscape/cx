package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class NodesFragmentReader implements AspectFragmentReader {

    private static final boolean STRICT = true;

    @Override
    public String getAspectName() {
        return CxConstants.NODES;
    }

    @Override
    public List<AspectElement> readAspectFragment(final JsonParser jp) throws IOException {

        JsonToken t = jp.nextToken();
        if (t != JsonToken.START_ARRAY) {
            throw new IOException("malformed cx json in '" + CxConstants.NODES + "'");
        }
        final List<AspectElement> node_aspects = new ArrayList<AspectElement>();
        String id = null;
        while (t != JsonToken.END_ARRAY) {
            if (t == JsonToken.START_OBJECT) {
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    final String namefield = jp.getCurrentName();
                    jp.nextToken(); // move to value
                    if (CxConstants.ID.equals(namefield)) {
                        id = jp.getText().trim();
                    }
                    else if (STRICT) {
                        throw new IOException("malformed cx json: unrecognized field '" + namefield + "'");
                    }
                }
                if (Util.isEmpty(id)) {
                    throw new IOException("malformed cx json: node id missing");
                }
                node_aspects.add(new NodesElement(id));
            }
            t = jp.nextToken();
        }
        return node_aspects;
    }

}