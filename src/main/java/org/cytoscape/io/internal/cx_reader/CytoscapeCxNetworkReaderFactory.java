package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class CytoscapeCxNetworkReaderFactory extends AbstractInputStreamTaskFactory {

    private final CyApplicationManager         _application_manager;
    protected final CyNetworkFactory           _network_factory;
    private final CyNetworkManager             _network_manager;
    private final CyRootNetworkManager         _root_network_manager;
    private final VisualMappingManager         _visual_mapping_manager;
    private final RenderingEngineManager       _rendering_engine_manager;
    private final CyNetworkViewFactory         _networkview_factory;
    private final CyNetworkViewManager         _networkview_manager;
    private final CyGroupFactory               _group_factory;
    private final VisualStyleFactory           _visual_style_factory;
    private final VisualMappingFunctionFactory _vmf_factory_c;
    private final VisualMappingFunctionFactory _vmf_factory_d;
    private final VisualMappingFunctionFactory _vmf_factory_p;
    private final CyLayoutAlgorithmManager 	   _layout_manager;
    private final DialogTaskManager			   _task_manager;

    public CytoscapeCxNetworkReaderFactory(final CyFileFilter filter,
                                           final CyApplicationManager application_manager,
                                           final CyNetworkFactory network_factory,
                                           final CyNetworkManager network_manager,
                                           final CyRootNetworkManager root_network_manager,
                                           final VisualMappingManager visual_mapping_manager,
                                           final VisualStyleFactory visual_style_factory,
                                           final CyGroupFactory group_factory,
                                           final RenderingEngineManager rendering_engine_manager,
                                           final CyNetworkViewFactory networkview_factory,
                                           final CyNetworkViewManager networkview_manager, 
                                           final VisualMappingFunctionFactory vmf_factory_c,
                                           final VisualMappingFunctionFactory vmf_factory_d,
                                           final VisualMappingFunctionFactory vmf_factory_p,
                                           final CyLayoutAlgorithmManager layout_manager,
                                           final DialogTaskManager task_manager) {
        super(filter);
        _application_manager = application_manager;
        _network_factory = network_factory;
        _network_manager = network_manager;
        _root_network_manager = root_network_manager;
        _visual_mapping_manager = visual_mapping_manager;
        _visual_style_factory = visual_style_factory;
        _group_factory = group_factory;
        _rendering_engine_manager = rendering_engine_manager;
        _networkview_factory = networkview_factory;
        _networkview_manager = networkview_manager;
        _vmf_factory_c = vmf_factory_c;
        _vmf_factory_d = vmf_factory_d;
        _vmf_factory_p = vmf_factory_p;
        _layout_manager = layout_manager;
        _task_manager = task_manager;
    }

    @Override
    public TaskIterator createTaskIterator(final InputStream is, final String collection_name) {
    	
        try {
            return new TaskIterator(new CytoscapeCxNetworkReader(collection_name,
                                                                 is,
                                                                 _application_manager,
                                                                 _network_factory,
                                                                 _network_manager,
                                                                 _root_network_manager,
                                                                 _group_factory,
                                                                 _visual_mapping_manager,
                                                                 _visual_style_factory,
                                                                 _rendering_engine_manager,
                                                                 _networkview_factory,
                                                                 _networkview_manager,
                                                                 _layout_manager, 
                                                                 _task_manager, 
                                                                 _vmf_factory_c,
                                                                 _vmf_factory_d,
                                                                 _vmf_factory_p));
        }
        catch (final IOException e) {

            e.printStackTrace();
            return null;
        }
    }
}
