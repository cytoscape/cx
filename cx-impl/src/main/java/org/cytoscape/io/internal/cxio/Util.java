package org.cytoscape.io.internal.cxio;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Util {

    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");

    public final static String makeId(final long id) {
        return ("_:" + id);
    }

    public final static String makeId(final String id) {
        return ("_:" + id);
    }

    public final static String makeTimeStamp() {
        return DATE_FORMAT.format(new Date());
    }

}
