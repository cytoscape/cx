package org.cytoscape.io.internal.cx_writer;

import java.io.OutputStream;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class CxNetworkWriterFactory implements CyNetworkViewWriterFactory {
    private final CyFileFilter          _filter;

    public CxNetworkWriterFactory(final CyFileFilter filter) {
        _filter = filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetwork network) {
        return new CxNetworkWriter(os,
                                   network,
                                   false,
                                   true);
    }

    @Override
    public CyFileFilter getFileFilter() {
        return _filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetworkView view) {
        return new CxNetworkWriter(os,
                                   view.getModel(),
                                   false,
                                   true);

    }

}
