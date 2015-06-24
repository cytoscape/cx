package org.cytoscape.io.internal.cx_writer;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class CxNetworkWriterFactory implements CyNetworkViewWriterFactory {
    private final CyFileFilter filter;

    public CxNetworkWriterFactory(final CyFileFilter filter) {
        this.filter = filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream outputStream, final CyNetwork network) {
        return new CxNetworkWriter(outputStream, network);
    }

    @Override
    public CyFileFilter getFileFilter() {
        return filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetworkView view) {
        return new CxNetworkViewWriter(os, view);

    }
}
