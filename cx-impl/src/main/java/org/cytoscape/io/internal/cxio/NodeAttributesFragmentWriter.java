package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public class NodeAttributesFragmentWriter implements AspectFragmentWriter {
    public static NodeAttributesFragmentWriter createInstance() {
        return new NodeAttributesFragmentWriter();
    }

    private NodeAttributesFragmentWriter() {
    }

    private final void addNodeAttributesAspect(final NodeAttributesElement na, final JsonWriter w)
            throws IOException {
        w.writeStartObject();
        w.writeStringField(CxConstants.ID, na.getId());
        w.writeList(CxConstants.NODES, na.getNodes());
        if ((na.getAttributes() != null) && !na.getAttributes().isEmpty()) {
            w.writeObjectFieldStart(CxConstants.ATTRIBUTES);
            for (final Entry<String, List<String>> a : na.getAttributes().entrySet()) {
                w.writeList(a.getKey(), a.getValue());
            }
            w.writeEndObject();
        }
        w.writeEndObject();
    }

    @Override
    public void write(final List<AspectElement> node_attributes_aspects, final JsonWriter w)
            throws IOException {
        if (node_attributes_aspects == null) {
            return;
        }
        w.startArray(CxConstants.NODE_ATTRIBUTES);
        for (final AspectElement node_attributes_aspect : node_attributes_aspects) {
            final NodeAttributesElement na = (NodeAttributesElement) node_attributes_aspect;
            addNodeAttributesAspect(na, w);
        }
        w.endArray();
    }

    @Override
    public String getAspectName() {
        return CxConstants.NODE_ATTRIBUTES;
    }

}
