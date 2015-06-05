package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.model.CyNetwork;

public interface CxToCy {

    void deserializeCyNetwork(final CyNetwork network, final InputStream in)  throws IOException;

}