package org.cytoscape.io.internal.cxio;

public enum VisualPropertyType {

    EDGES_DEFAULT("edges:default"), EDGES("edges"), NETWORK("network"), NODES_DEFAULT("nodes:default"), NODES("nodes");

    private final String _s;

    private VisualPropertyType(final String s) {
        _s = s;
    }

    /**
     * This returns an visual property type as String. The returned String is
     * the official name of the property type in question.
     *
     *
     * @return official name of the property type
     */
    public final String asString() {
        return _s;
    }

    @Override
    public String toString() {
        return asString();
    }

}
