package org.cytoscape.io.internal.cx_reader;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class StringParser {
    private final Map<String, String> _data = new HashMap<String, String>();

    public StringParser(final String str) {
        final StringTokenizer t = new StringTokenizer(str, ",");
        while (t.hasMoreTokens()) {
            final String n = t.nextToken();
            final String[] m = n.split("=");
            if (m.length == 2) {
                if ((m[0] != null) && (m[1] != null)) {
                    _data.put(m[0], m[1]);
                }
            }
            else if (m.length == 3) {
                if ((m[0] != null) && (m[1] != null) && (m[2] != null)) {
                    _data.put(m[0] + "=" + m[1], m[2]);
                }
            }
        }
    }

    public final String get(final String key) {
        return _data.get(key);
    }

}
