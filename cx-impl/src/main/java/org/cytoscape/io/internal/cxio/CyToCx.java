package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;

import org.cytoscape.model.CyNetwork;

public interface CyToCx {

    void serializeCyNetwork(final CyNetwork network, final OutputStream out) throws IOException;

}