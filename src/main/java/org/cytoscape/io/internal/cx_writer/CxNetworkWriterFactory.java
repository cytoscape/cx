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
    private final VisualMappingManager  _visual_mapping_manager;
    private final CyApplicationManager  _application_manager;
    private final CyNetworkViewManager  _networkview_manager;
    private final CyGroupManager        _group_manager;

    public CxNetworkWriterFactory(final CyFileFilter filter) {
        _filter = filter;
        _visual_mapping_manager = null;
        _application_manager = null;
        _networkview_manager = null;
        _group_manager = null;
    }

    public CxNetworkWriterFactory(final CyFileFilter filter,
                                  final VisualMappingManager visual_mapping_manager,
                                  final CyApplicationManager application_manager,
                                  final CyNetworkViewManager networkview_manager,
                                  final CyGroupManager group_manager) {
        _filter = filter;
        _visual_mapping_manager = visual_mapping_manager;
        _application_manager = application_manager;
        _networkview_manager = networkview_manager;
        _group_manager = group_manager;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetwork network) {
    	System.out.println(network);
        if ((_visual_mapping_manager != null) && (_application_manager != null)) {
            
            return new CxNetworkWriter(os,
                                       network,
                                       _visual_mapping_manager,
                                       _networkview_manager,
                                       _group_manager,
                                       _application_manager,
                                       true,
                                       false);
        }
        else {
            throw new IllegalStateException("visual_mapping_manager and/or application_manager are null");
        }
    }

    @Override
    public CyFileFilter getFileFilter() {
        return _filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetworkView view) {
        if ((_visual_mapping_manager != null) && (_application_manager != null)) {
            return new CxNetworkWriter(os,
                                       view.getModel(),
                                       _visual_mapping_manager,
                                       _networkview_manager,
                                       _group_manager,
                                       _application_manager,
                                       false,
                                       true);

        }
        throw new IllegalStateException("visual_mapping_manager and/or application_manager are null");

    }
}
