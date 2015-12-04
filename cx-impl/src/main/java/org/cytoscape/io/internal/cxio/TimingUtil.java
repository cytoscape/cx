package org.cytoscape.io.internal.cxio;

public final class TimingUtil {

    public final static boolean WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM = false;
    public static final boolean WRITE_TO_DEV_NULL                = false;

    public final static void reportTime(final long t, final String label, final int n) {
        if (n >= 0) {
            System.out.println(String.format("%-20s%-8s: %s ms", label, n, t));
        }
        else {
            System.out.println(String.format("%-20s%-8s: %s ms", label, " ", t));
        }

    }

    public final static void reportTimeDifference(final long t0, final String label, final int n) {
        if (n >= 0) {
            System.out.println(String.format("%-20s%-8s: %s ms", label, n, (System.currentTimeMillis() - t0)));
        }
        else {
            System.out.println(String.format("%-20s%-8s: %s ms", label, " ", (System.currentTimeMillis() - t0)));
        }
    }

}
