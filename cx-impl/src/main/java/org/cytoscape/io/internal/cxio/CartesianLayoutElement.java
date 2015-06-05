package org.cytoscape.io.internal.cxio;

public final class CartesianLayoutElement implements AspectElement {

    private final String node;
    private final int    x;
    private final int    y;

    public CartesianLayoutElement(final String node, final int x, final int y) {
        this.node = node;
        this.x = x;
        this.y = y;
    }

    @Override
    public String getAspectName() {
        return CxConstants.CARTESIAN_LAYOUT;
    }

    public String getNode() {
        return node;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("node: ");
        sb.append(node);
        sb.append(", x: ");
        sb.append(x);
        sb.append(", y: ");
        sb.append(y);
        return sb.toString();
    }

}
