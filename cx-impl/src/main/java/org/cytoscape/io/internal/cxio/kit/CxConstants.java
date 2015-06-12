package org.cytoscape.io.internal.cxio.kit;

public final class CxConstants {

    public enum ATTRIBUTE_TYPE {
        BOOLEAN("boolean"), DOUBLE("double"), FLOAT("float"), INTEGER("integer"), LONG("long"), STRING(
                "string");

        private final String name;

        private ATTRIBUTE_TYPE(final String s) {
            name = s;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final String ATTRIBUTE_TYPES  = "types";
    public final static String ATTRIBUTES       = "attributes";
    public final static String CARTESIAN_LAYOUT = "cartesianLayout";
    public final static String EDGE             = "edge";
    public final static String EDGE_ATTRIBUTES  = "edgeAttributes";
    public final static String EDGES            = "edges";
    public final static String ID               = "@id";
    public final static String NODE             = "node";
    public final static String NODE_ATTRIBUTES  = "nodeAttributes";
    public final static String NODES            = "nodes";
    public final static String SOURCE_NODE_ID   = "source";
    public final static String TARGET_NODE_ID   = "target";
    public final static String VISUAL_STYLE     = "visualStyle";
    public final static String X                = "x";
    public final static String Y                = "y";

}
