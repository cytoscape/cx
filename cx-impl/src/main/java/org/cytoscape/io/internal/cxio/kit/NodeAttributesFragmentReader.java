package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
                String id = null;
                CxConstants.ATTRIBUTE_TYPE type = null;
                List<String> nodes = null;
                final SortedMap<String, List<String>> attributes = new TreeMap<String, List<String>>();
                while (jp.nextToken() != JsonToken.END_OBJECT) {
                    final String namefield = jp.getCurrentName();
                    jp.nextToken(); // move to value
                    if (CxConstants.ID.equals(namefield)) {
                        id = jp.getText().trim();
                    }
                    else if (CxConstants.NODES.equals(namefield)) {
                        nodes = Util.parseSimpleList(jp, t);
                    }
                    else if (CxConstants.TYPE.equals(namefield)) {
                        type = Util.determineAttributeType(jp.getText().trim());
                    }
                    else if (CxConstants.ATTRIBUTES.equals(namefield)) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            jp.nextToken(); // move to value
                            attributes.put(jp.getCurrentName(), Util.parseSimpleList(jp, t));
                        }
                    }
                    else if (STRICT) {
                        throw new IOException("malformed cx json: unrecognized field '" + namefield
                                              + "'");
                    }
                }
                if (Util.isEmpty(id)) {
                    throw new IOException(
                            "malformed cx json: attribute id in node attributes is missing");
                }
                if (type == null) {
                    throw new IOException(
                            "malformed cx json: type in node attributes is missing");
                }
                if ((nodes == null) || nodes.isEmpty()) {
                    throw new IOException(
                            "malformed cx json: node ids in node attributes are missing");
                }
                na_aspects.add(new NodeAttributesElement(id, nodes, type, attributes));
            }
            t = jp.nextToken();
        }
        return na_aspects;
    }
}
