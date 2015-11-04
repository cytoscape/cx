package org.cytoscape.io.internal.cxio;

public final class CxUtil {

    public final static String makeId(final long id) {
        return ("_:" + id);
    }

    public final static String makeId(final String id) {
        return ("_:" + id);
    }

    public final static String SELECTED           = "selected";
    public final static String REPRESENTS         = "represents";
    public final static String SHARED_NAME        = "shared name";
    public static final String SHARED_INTERACTION = "shared interaction";
    public static final String CONTINUOUS_MAPPING = "CONTINUOUS_MAPPING_";
    public static final String DISCRETE_MAPPING = "DISCRETE_MAPPING_";
    public static final String PASSTHROUGH_MAPPING = "PASSTHROUGH_MAPPING_";

}
