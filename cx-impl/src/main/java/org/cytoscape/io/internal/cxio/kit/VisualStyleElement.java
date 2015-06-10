package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;

public final class VisualStyleElement implements AspectElement {

    private final String name;
    private final List<VisualProperty> visual_properties;

    public VisualStyleElement(final String name) {
        this.name = name;
        visual_properties = new ArrayList<VisualProperty>();
    }

    @Override
    public String getAspectName() {
        return CxConstants.VISUAL_STYLE;
    }

    public final String getName() {
        return name;
    }

    public final List<VisualProperty> getVisualProperties() {
        return visual_properties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("name: ");
        sb.append(name);
        sb.append("\n");
        sb.append("visual properties:");
        sb.append("\n");
        sb.append(visual_properties);
        return sb.toString();
    }

}
