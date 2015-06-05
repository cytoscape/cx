package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.List;

public class CartesianLayoutFragmentWriter implements AspectFragmentWriter {
    public static CartesianLayoutFragmentWriter createInstance() {
        return new CartesianLayoutFragmentWriter();
    }

    private CartesianLayoutFragmentWriter() {
    }

    private final void addCartesianLayoutElement(final String node_id, final int x, final int y, final JsonWriter w)
            throws IOException {
        w.writeStartObject();
        w.writeStringField(CxConstants.NODE, node_id);
        w.writeStringField(CxConstants.X, Integer.toString(x));
        w.writeStringField(CxConstants.Y, Integer.toString(y));
        w.writeEndObject();
    }

    @Override
    public void write(final List<AspectElement> cartesian_layout_aspects, final JsonWriter w) throws IOException {
        if (cartesian_layout_aspects == null) {
            return;
        }
        w.startArray(CxConstants.CARTESIAN_LAYOUT);
        for (final AspectElement cartesian_layout_aspect : cartesian_layout_aspects) {
            final CartesianLayoutElement c = (CartesianLayoutElement) cartesian_layout_aspect;
            addCartesianLayoutElement(c.getNode(), c.getX(), c.getY(), w);
        }
        w.endArray();
    }

    @Override
    public String getAspectName() {
        return CxConstants.CARTESIAN_LAYOUT;
    }
}
