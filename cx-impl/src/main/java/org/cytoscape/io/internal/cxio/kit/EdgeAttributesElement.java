package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EdgeAttributesElement extends AttributesElement {

    private final List<String> edges;

    public EdgeAttributesElement() {
        this.id = null;
        this.edges = new ArrayList<String>();

    }

    public EdgeAttributesElement(final String id) {
        this.id = id;
        this.edges = new ArrayList<String>();
    }

    public EdgeAttributesElement(final String id, final String edge_id) {
        this.id = id;
        this.edges = new ArrayList<String>();
        addEdge(edge_id);
    }

    public final void addEdge(final long edge_id) {
        addEdge(String.valueOf(edge_id));
    }

    public final void addEdge(final String edge_id) {
        if (Util.isEmpty(edge_id)) {
            throw new IllegalArgumentException("attempt to add null or empty edge id");
        }
        edges.add(edge_id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        return (o instanceof EdgeAttributesElement)
                && id.equals(((EdgeAttributesElement) o).getId());

    }

    @Override
    public String getAspectName() {
        return CxConstants.EDGE_ATTRIBUTES;
    }

    public final List<String> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("id: ");
        sb.append(id);
        sb.append("\n");
        sb.append("edges: ");
        sb.append(edges);
        sb.append("\n");
        sb.append("attributes:");
        for (final Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            sb.append("\n");
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append(" (");
            sb.append(attributes_types.get(entry.getKey()));
            sb.append(")");
        }
        return sb.toString();
    }

}
