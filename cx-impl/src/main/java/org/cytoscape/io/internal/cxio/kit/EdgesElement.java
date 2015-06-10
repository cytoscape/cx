package org.cytoscape.io.internal.cxio.kit;


public final class EdgesElement implements AspectElement {

    final private String id;
    final private String source;
    final private String target;

    public EdgesElement(final String id, final String source, final String target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        return (o instanceof EdgesElement) && id.equals(((EdgesElement) o).getId());

    }

    @Override
    public String getAspectName() {
        return CxConstants.EDGES;
    }

    public final String getId() {
        return id;
    }

    public final String getSource() {
        return source;
    }

    public final String getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getId());
        sb.append(" ");
        sb.append(getSource());
        sb.append(" ");
        sb.append(getTarget());
        return sb.toString();
    }

}