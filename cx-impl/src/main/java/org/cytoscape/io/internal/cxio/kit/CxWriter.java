package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CxWriter {

    private final Map<String, AspectFragmentWriter> writers;
    private final JsonWriter                        jw;
    private boolean                                 started;
    private boolean                                 ended;

    private CxWriter(final OutputStream out, final boolean use_default_pretty_printer)
            throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("output stream is null");
        }
        this.writers = new HashMap<String, AspectFragmentWriter>();
        this.jw = JsonWriter.createInstance(out, use_default_pretty_printer);
        started = false;
        ended = false;
    }

    public void start() throws IOException {
        if (started) {
            throw new IllegalStateException("already started");
        }
        if (ended) {
            throw new IllegalStateException("already ended");
        }
        started = true;
        jw.start();
    }

    public void end() throws IOException {
        if (!started) {
            throw new IllegalStateException("not started");
        }
        if (ended) {
            throw new IllegalStateException("already ended");
        }
        ended = true;
        jw.end();
    }

    public void write(final List<AspectElement> elements) throws IOException {
        if (!started) {
            throw new IllegalStateException("not started");
        }
        if (ended) {
            throw new IllegalStateException("already ended");
        }
        if ((elements == null) || elements.isEmpty()) {
            return;
        }
        if (writers.containsKey(elements.get(0).getAspectName())) {
            final AspectFragmentWriter writer = writers.get(elements.get(0).getAspectName());
            writer.write(elements, jw);
        }

    }

    public final static CxWriter createInstance(final OutputStream out) throws IOException {
        return new CxWriter(out, false);
    }

    public final static CxWriter createInstance(final OutputStream out,
                                                final boolean use_default_pretty_printer)
            throws IOException {
        return new CxWriter(out, use_default_pretty_printer);
    }

    public void addAspectFragmentWriter(final AspectFragmentWriter writer) {
        if (writer == null) {
            throw new IllegalArgumentException("aspect fragment writer is null");
        }
        if (Util.isEmpty(writer.getAspectName())) {
            throw new IllegalArgumentException("aspect name is null or empty");
        }
        writers.put(writer.getAspectName(), writer);
    }

}
