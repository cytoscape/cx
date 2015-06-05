package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.List;

public interface AspectFragmentWriter {
    public String getAspectName();

    public void write(final List<AspectElement> aspects, final JsonWriter json_writer) throws IOException;

}
