package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class NodeAttributesElement implements AspectElement {

    private final String                          id;
    private final List<String>                    nodes;
    private final SortedMap<String, List<String>> attributes;

    public NodeAttributesElement(final String id,
                                 final List<String> nodes,
                                 final SortedMap<String, List<String>> attributes) {
        this.id = id;
        this.nodes = nodes;
        this.attributes = attributes;
    }

    public NodeAttributesElement(final String id) {
        this.id = id;
        this.nodes = new ArrayList<String>();
        this.attributes = new TreeMap<String, List<String>>();
    }

    public NodeAttributesElement(final String id, final String node_id) {
        this.id = id;
        this.nodes = new ArrayList<String>();
        this.attributes = new TreeMap<String, List<String>>();
        addNode(node_id);
    }

    public final void addNode(final String node_id) {
        if (Util.isEmpty(node_id)) {
            throw new IllegalArgumentException("attempt to add null or empty node id");
        }
        nodes.add(node_id);
    }
    
    public final void addNode(final long node_id) {
        addNode(String.valueOf(node_id));
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
        return (o instanceof NodeAttributesElement)
                && id.equals(((NodeAttributesElement) o).getId());

    }

    @Override
    public String getAspectName() {
        return CxConstants.NODE_ATTRIBUTES;
    }

    public final SortedMap<String, List<String>> getAttributes() {
        return attributes;
    }

    public final String getId() {
        return id;
    }

    public final List<String> getNodes() {
        return nodes;
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
        sb.append("nodes:");
        sb.append("\n");
        sb.append(nodes);
        sb.append("\n");
        sb.append("attributes:");
        sb.append("\n");
        sb.append(attributes);
        return sb.toString();
    }

}
