package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class NodeAttributesFragmentReader implements AspectFragmentReader {
    private static final boolean STRICT = true;

    public static NodeAttributesFragmentReader createInstance() {
        return new NodeAttributesFragmentReader();
    }

    private NodeAttributesFragmentReader() {
    }

    @Override
    public String getAspectName() {
        return CxConstants.NODE_ATTRIBUTES;
    }

    @Override
    public List<AspectElement> readAspectFragment(final JsonParser jp) throws IOException {
        JsonToken t = jp.nextToken();
        if (t != JsonToken.START_ARRAY) {
            throw new IOException("malformed cx json in '" + CxConstants.EDGES + "'");
        }
        final List<AspectElement> na_aspects = new ArrayList<AspectElement>();
        while (t != JsonToken.END_ARRAY) {
            if (t == JsonToken.START_OBJECT) {
                final NodeAttributesElement nae = new NodeAttributesElement();
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    final String namefield = jp.getCurrentName();
                    jp.nextToken(); // move to value
                    if (CxConstants.ID.equals(namefield)) {
                        nae.setId(jp.getText());
                    }
                    else if (CxConstants.NODES.equals(namefield)) {
                        for (final String node : Util.parseSimpleList(jp, t)) {
                            nae.addNode(node);
                        }
                    }
                    else if (CxConstants.ATTRIBUTES.equals(namefield)) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            jp.nextToken(); // move to value
                            nae.putValues(jp.getCurrentName(), Util.parseSimpleList(jp, t));
                        }
                    }
                    else if (CxConstants.ATTRIBUTE_TYPES.equals(namefield)) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            jp.nextToken(); // move to value
                            nae.putType(jp.getCurrentName(), jp.getText());
                        }
                    }
                    else if (STRICT) {
                        throw new IOException("malformed cx json: unrecognized field '" + namefield
                                + "'");
                    }
                }
                if (Util.isEmpty(nae.getId())) {
                    throw new IOException(
                            "malformed cx json: attribute id in node attributes is missing");
                }
                if ((nae.getNodes() == null) || nae.getNodes().isEmpty()) {
                    throw new IOException(
                            "malformed cx json: node ids in node attributes are missing");
                }

                na_aspects.add(nae);
            }
            t = jp.nextToken();
        }
        return na_aspects;
    }
}
