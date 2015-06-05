package org.cytoscape.io.internal.cxio;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class EdgeAttributesElement implements AspectElement {

    private final String id;
    private final List<String> edges;
    private final SortedMap<String, List<String>> attributes;

    public EdgeAttributesElement(final String id, final List<String> edges,
            final SortedMap<String, List<String>> attributes) {
        this.id = id;
        this.edges = edges;
        this.attributes = attributes;
    }

    public EdgeAttributesElement(final String id) {
        this.id = id;
        this.edges = new ArrayList<String>();
        this.attributes = new TreeMap<String, List<String>>();
    }

    public EdgeAttributesElement(final String id, final String edge_id) {
        this.id = id;
        this.edges = new ArrayList<String>();
        this.attributes = new TreeMap<String, List<String>>();
        addEdge(edge_id);
    }

    public final void addEdge(final String edge_id) {
        if (Util.isEmpty(edge_id)) {
            throw new IllegalArgumentException("attempt to add null or empty edge id");
        }
        edges.add(edge_id);
    }

    public final void addAttribute(final String key, final String value) {
        if (Util.isEmpty(key)) {
            throw new IllegalArgumentException("attempt to use null or empty attribute key");
        }
        if (value == null) {
            throw new IllegalArgumentException("attempt to use null value");
        }
        if (!attributes.containsKey(key)) {
            attributes.put(key, new ArrayList<String>());
        }
        attributes.get(key).add(value);
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

    public final SortedMap<String, List<String>> getAttributes() {
        return attributes;
    }

    public final List<String> getEdges() {
        return edges;
    }

    public final String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("id: ");
        sb.append(id);
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
