package org.cytoscape.io.internal.cx_writer;

import java.io.OutputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class CxNetworkWriterFactory implements CyNetworkViewWriterFactory {
    private final CyFileFilter _filter;
    private final VisualMappingManager _visual_mapping_manager;
    private final CyApplicationManager _application_manager;

    public CxNetworkWriterFactory(final CyFileFilter filter) {
        _filter = filter;
        _visual_mapping_manager = null;
        _application_manager = null;
    }
    
    public CxNetworkWriterFactory(final CyFileFilter filter,
                                  final VisualMappingManager visual_mapping_manager,
                                  final CyApplicationManager application_manager) {
        _filter = filter;
        _visual_mapping_manager = visual_mapping_manager;
        _application_manager = application_manager;
    }

    @Override
    public CyWriter createWriter(final OutputStream outputStream, final CyNetwork network) {
        return new CxNetworkWriter(outputStream, network);
    }

    @Override
    public CyFileFilter getFileFilter() {
        return _filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetworkView view) {
        if ( _visual_mapping_manager != null &&  _application_manager != null ) {
            return new CxNetworkViewWriter(os,
                                           view,
                                           _visual_mapping_manager,
                                           _application_manager);
        }
        return new CxNetworkViewWriter(os, view);

    }
}
