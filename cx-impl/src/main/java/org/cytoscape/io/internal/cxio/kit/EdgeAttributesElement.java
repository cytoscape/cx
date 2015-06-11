package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class EdgeAttributesElement extends AttributeElement {

    // private final SortedMap<String, List<String>> attributes;
    private final List<String> edges;

    // private final String id;
    // private final CxConstants.ATTRIBUTE_TYPE type;

    public EdgeAttributesElement(final String id, final CxConstants.ATTRIBUTE_TYPE type) {
        this.id = id;
        this.type = type;
        this.edges = new ArrayList<String>();
        this.attributes = new TreeMap<String, List<String>>();
    }

    public EdgeAttributesElement(final String id,
                                 final List<String> edges,
                                 final CxConstants.ATTRIBUTE_TYPE type,
                                 final SortedMap<String, List<String>> attributes) {
        this.id = id;
        this.type = type;
        this.edges = edges;
        this.attributes = attributes;
    }

    public EdgeAttributesElement(final String id,
                                 final String edge_id,
                                 final CxConstants.ATTRIBUTE_TYPE type) {
        this.id = id;
        this.type = type;
        this.edges = new ArrayList<String>();
        this.attributes = new TreeMap<String, List<String>>();
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
        sb.append("type: ");
        sb.append(type);
        sb.append("\n");
        sb.append("edges:");
        sb.append("\n");
        sb.append(edges);
        sb.append("\n");
        sb.append("attributes:");
        sb.append("\n");
        sb.append(attributes);
        return sb.toString();
    }

}
