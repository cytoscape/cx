package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

public class JsonWriter {

    private static JsonGenerator g;

    public final static JsonWriter createInstance(final OutputStream out) throws IOException {
        return createInstance(out, false);
    }

    public final static JsonWriter createInstance(final OutputStream out,
                                                  final boolean use_default_pretty_printer)
                                                          throws IOException {
        final JsonFactory f = new JsonFactory();
        g = f.createGenerator(out);
        if (use_default_pretty_printer) {
            g.useDefaultPrettyPrinter();
        }
        return new JsonWriter();
    }

    public final void end() throws JsonGenerationException, IOException {
        g.writeEndArray();
        g.close();
    }

    public final void endArray() throws JsonGenerationException, IOException {
        g.writeEndArray();
        g.writeEndObject();
    }

    public final void start() throws JsonGenerationException, IOException {
        g.writeStartArray();
    }

    public final void startArray(final String label) throws JsonGenerationException, IOException {
        g.writeStartObject();
        g.writeArrayFieldStart(label);
    }

    public final void writeBooleanField(final String field_name, final boolean value)
            throws IOException {
        g.writeBooleanField(field_name, value);
    }

    public final void writeEndObject() throws IOException {
        g.writeEndObject();
    }

    public final void writeList(final String label, final Iterator<String> it)
            throws JsonGenerationException, IOException {
        g.writeArrayFieldStart(label);
        while (it.hasNext()) {
            g.writeString(it.next().toString());
        }
        g.writeEndArray();
    }

    public final void writeList(final String label, final List<String> list)
            throws JsonGenerationException, IOException {
        if ((list != null) && !list.isEmpty()) {
            g.writeArrayFieldStart(label);
            for (final String s : list) {
                g.writeString(s);
            }
            g.writeEndArray();
        }
    }

    public final void writeNumberField(final String field_name, final double value)
            throws IOException {
        g.writeNumberField(field_name, value);
    }

    public final void writeObjectFieldStart(final String label) throws IOException {
        g.writeObjectFieldStart(label);
    }

    public final void writeStartObject() throws IOException {
        g.writeStartObject();
    }

    public final void writeStringField(final String field_name, final String value)
            throws IOException {
        g.writeStringField(field_name, value);
    }

}
