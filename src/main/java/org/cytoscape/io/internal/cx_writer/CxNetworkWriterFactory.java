package org.cytoscape.io.internal.cx_writer;

import java.io.OutputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class CxNetworkWriterFactory implements CyNetworkViewWriterFactory {
    private final CyFileFilter          _filter;

    public CxNetworkWriterFactory(final CyFileFilter filter) {
        _filter = filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetwork network) {
    	System.out.println(network);
        return new CxNetworkWriter(os,
                                       network,
                                       true,
                                       false);
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
