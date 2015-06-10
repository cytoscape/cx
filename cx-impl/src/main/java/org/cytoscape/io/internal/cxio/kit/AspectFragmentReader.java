package org.cytoscape.io.internal.cxio.kit;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;

public interface AspectFragmentReader {
    public String getAspectName();

    public List<AspectElement> readAspectFragment(final JsonParser jp) throws IOException;
}
