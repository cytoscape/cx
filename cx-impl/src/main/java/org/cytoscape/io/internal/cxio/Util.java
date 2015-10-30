package org.cytoscape.io.internal.cxio;

public final class Util {

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

}
