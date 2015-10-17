package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class CytoscapeCxNetworkReaderFactory extends AbstractInputStreamTaskFactory {

    private final CyApplicationManager   cyApplicationManager;
    protected final CyNetworkFactory     cyNetworkFactory;
    private final CyNetworkManager       cyNetworkManager;
    private final CyRootNetworkManager   cyRootNetworkManager;
    private final VisualMappingManager   _visual_mapping_manager;
    private final RenderingEngineManager _rendering_engine_manager;
    private final CyNetworkViewFactory   _networkview_factory;

    public CytoscapeCxNetworkReaderFactory(final CyFileFilter filter,
                                           final CyApplicationManager cyApplicationManager,
                                           final CyNetworkFactory cyNetworkFactory,
                                           final CyNetworkManager cyNetworkManager,
                                           final CyRootNetworkManager cyRootNetworkManager,
                                           final VisualMappingManager visualMappingManager,
                                           final RenderingEngineManager renderingEngineMgr,
                                           final CyNetworkViewFactory networkview_factory) {
        super(filter);
        this.cyApplicationManager = cyApplicationManager;
        this.cyNetworkFactory = cyNetworkFactory;
        this.cyNetworkManager = cyNetworkManager;
        this.cyRootNetworkManager = cyRootNetworkManager;
        _visual_mapping_manager = visualMappingManager;
        _rendering_engine_manager = renderingEngineMgr;
        _networkview_factory = networkview_factory;
    }

    @Override
    public TaskIterator createTaskIterator(final InputStream is, final String collectionName) {
        try {

            return new TaskIterator(new CytoscapeCxNetworkReader(collectionName,
                                                                 is,
                                                                 cyApplicationManager,
                                                                 cyNetworkFactory,
                                                                 cyNetworkManager,
                                                                 cyRootNetworkManager,
                                                                 _visual_mapping_manager,
                                                                 _rendering_engine_manager,
                                                                 _networkview_factory));
        }
        catch (final IOException e) {

            e.printStackTrace();
            return null;
        }
    }
}
