package org.cytoscape.io.internal.cxio.kit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NodeAttributesElement extends AttributesElement {

    private final List<String> nodes;

    public NodeAttributesElement() {
        this.id = null;
        this.nodes = new ArrayList<String>();
    }

    public NodeAttributesElement(final String id) {
        this.id = id;
        this.nodes = new ArrayList<String>();
    }

    public NodeAttributesElement(final String id, final String node_id) {
        this.id = id;
        this.nodes = new ArrayList<String>();
        addNode(node_id);
    }

    public final void addNode(final long node_id) {
        addNode(String.valueOf(node_id));
    }

    public final void addNode(final String node_id) {
        if (Util.isEmpty(node_id)) {
            throw new IllegalArgumentException("attempt to add null or empty node id");
        }
        nodes.add(node_id);
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

    public final List<String> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("id: ");
        sb.append(id);
        sb.append("\n");
        sb.append("nodes: ");
        sb.append(nodes);
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
