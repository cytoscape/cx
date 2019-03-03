package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReaderFactory;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

/**
 * Activator for CX support module.
 */
public class CyActivator extends AbstractCyActivator {

    public CyActivator() {
        super();
    }

    private <S> S cacheService(final BundleContext bc, Class<S> cls) {
    	S service = getService(bc, cls);
    	CyServiceModule.setService(cls, service);
    	return service;
    }
    
    @Override
    public void start(final BundleContext bc) {

    	StreamUtil streamUtil = cacheService(bc, StreamUtil.class);
        final CytoscapeCxFileFilter cx_filter = new CytoscapeCxFileFilter(streamUtil);

        cacheService(bc, CyLayoutAlgorithmManager.class);
        // Writer:
        cacheService(bc, VisualMappingManager.class);
        cacheService(bc, CyApplicationManager.class);
        cacheService(bc, CyNetworkViewManager.class);
        cacheService(bc, CyNetworkManager.class);
        cacheService(bc, CyGroupManager.class);
        cacheService(bc, CyNetworkViewFactory.class);
        cacheService(bc, DialogTaskManager.class);
     // Reader:
        cacheService(bc, CyNetworkFactory.class);
        cacheService(bc, CyRootNetworkManager.class);
        cacheService(bc, RenderingEngineManager.class);
        cacheService(bc, VisualStyleFactory.class);
        cacheService(bc, CyGroupFactory.class);
        
//        final VisualMappingManager visual_mapping_manager = getService(bc, VisualMappingManager.class);
//        final CyApplicationManager application_manager = getService(bc, CyApplicationManager.class);
//        final CyNetworkViewManager networkview_manager = getService(bc, CyNetworkViewManager.class);
//        final CyNetworkManager network_manager = getService(bc, CyNetworkManager.class);
//        final CyGroupManager group_manager = getService(bc, CyGroupManager.class);
//        final CyNetworkViewFactory network_view_factory = getService(bc, CyNetworkViewFactory.class);
//        final DialogTaskManager task_manager = getService(bc, DialogTaskManager.class);
        
        final CxNetworkWriterFactory network_writer_factory = new CxNetworkWriterFactory(cx_filter);

        final Properties cx_writer_factory_properties = new Properties();

        cx_writer_factory_properties.put(ID, "cxNetworkWriterFactory");

        registerAllServices(bc, network_writer_factory, cx_writer_factory_properties);

        
        
//        final CyNetworkFactory network_factory = getService(bc, CyNetworkFactory.class);
//        final CyRootNetworkManager root_network_manager = getService(bc, CyRootNetworkManager.class);
//        final RenderingEngineManager rendering_engine_manager = getService(bc, RenderingEngineManager.class);
//        final VisualStyleFactory visual_style_factory = getService(bc, VisualStyleFactory.class);
//        final CyGroupFactory group_factory = getService(bc, CyGroupFactory.class);

        final VisualMappingFunctionFactory vmfFactoryC = getService(bc,
                                                                    VisualMappingFunctionFactory.class,
                                                                    "(mapping.type=continuous)");
        
        final VisualMappingFunctionFactory vmfFactoryD = getService(bc,
                                                                    VisualMappingFunctionFactory.class,
                                                                    "(mapping.type=discrete)");
        
        final VisualMappingFunctionFactory vmfFactoryP = getService(bc,
                                                                    VisualMappingFunctionFactory.class,
                                                                    "(mapping.type=passthrough)");
        CyServiceModule.setPassthroughMapping(vmfFactoryP);
        CyServiceModule.setDiscreteMapping(vmfFactoryD);
        CyServiceModule.setContinuousMapping(vmfFactoryC);
        
        final CytoscapeCxNetworkReaderFactory cx_reader_factory = new CytoscapeCxNetworkReaderFactory(cx_filter);
        final Properties reader_factory_properties = new Properties();

        // This is the unique identifier for this reader. 3rd party developer
        // can use this service by using this ID.
        reader_factory_properties.put(ID, "cytoscapeCxNetworkReaderFactory");
        registerService(bc, cx_reader_factory, InputStreamTaskFactory.class, reader_factory_properties);

    }
}