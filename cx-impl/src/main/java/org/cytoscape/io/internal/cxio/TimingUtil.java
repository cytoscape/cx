package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;

public final class TimingUtil {

    public final static boolean TIMING                           = true;
    public final static boolean WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM = false;
    public static final boolean WRITE_TO_DEV_NULL                = false;
    public static final boolean THROW_AWAY_ELEMENTS              = true;

    public final static SortedMap<String, List<AspectElement>> parseAsMap(final CxReader cxr, long t)
            throws IOException {
        long time_total = 0;
        if (cxr == null) {
            throw new IllegalArgumentException("reader is null");
        }
        long prev_time = System.currentTimeMillis() - t;

        System.out.println();
        System.out.println();

        final SortedMap<String, List<AspectElement>> all_aspects = new TreeMap<String, List<AspectElement>>();

        while (cxr.hasNext()) {
            t = System.currentTimeMillis();
            final List<AspectElement> aspects = cxr.getNext();
            if ((aspects != null) && !aspects.isEmpty()) {
                final String name = aspects.get(0).getAspectName();

                reportTime(prev_time, name, aspects.size());
                time_total += prev_time;
                prev_time = System.currentTimeMillis() - t;

                if (!all_aspects.containsKey(name)) {
                    all_aspects.put(name, aspects);
                }
                else {
                    all_aspects.get(name).addAll(aspects);
                }
            }
        }
        reportTime(time_total, "sum", 0);
        return all_aspects;
    }

    public final static void reportTime(final long t, final String label, final int n) {
        if (n > 0) {
            System.out.println(String.format("%-20s%-8s: %s ms", label, n, t));
        }
        else {
            System.out.println(String.format("%-20s%-8s: %s ms", label, " ", t));
        }

    }

    public final static void reportTimeDifference(final long t0, final String label, final int n) {
        if (n > 0) {
            System.out.println(String.format("%-20s%-8s: %s ms", label, n, (System.currentTimeMillis() - t0)));
        }
        else {
            System.out.println(String.format("%-20s%-8s: %s ms", label, " ", (System.currentTimeMillis() - t0)));
        }
    }

}
