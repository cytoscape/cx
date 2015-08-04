package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReaderFactory;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.osgi.framework.BundleContext;

/**
 * Activator for CX support module.
 */
public class CyActivator extends AbstractCyActivator {

    public CyActivator() {
        super();
    }

    @Override
    public void start(final BundleContext bc) {

        final StreamUtil streamUtil = getService(bc, StreamUtil.class);

        final BasicCyFileFilter cx_filter = new BasicCyFileFilter(new String[] { "cx" },
                                                                 new String[] { "application/json" },
                                                                 "CX JSON",
                                                                 DataCategory.NETWORK,
                                                                 streamUtil);

        // Writer:
        final VisualMappingManager visual_mapping_manager = getService(bc, VisualMappingManager.class);
        final CyApplicationManager application_manager = getService(bc, CyApplicationManager.class);
        final CyNetworkViewManager networkview_manager = getService(bc, CyNetworkViewManager.class);
        // final CustomGraphicsManager custom_graphics_manager = getService(bc,
        // CustomGraphicsManager.class);

        final CxNetworkWriterFactory cxNetworkWriterFactory = new CxNetworkWriterFactory(cx_filter,
                                                                                         visual_mapping_manager,
                                                                                         application_manager,
                                                                                         null,
                                                                                         networkview_manager);

        final Properties cxWriterFactoryProperties = new Properties();

        cxWriterFactoryProperties.put(ID, "cxNetworkWriterFactory");

        registerAllServices(bc, cxNetworkWriterFactory, cxWriterFactoryProperties);

        // Reader:
        final CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
        final CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
        final CyRootNetworkManager cyRootNetworkManager = getService(bc, CyRootNetworkManager.class);
        final RenderingEngineManager renderingEngineMgr = getService(bc, RenderingEngineManager.class);
        final BasicCyFileFilter cytoscapejsReaderFilter = new BasicCyFileFilter(new String[] { "cx", "json" },
                                                                                new String[] { "application/json" },
                                                                                "CX JSON",
                                                                                DataCategory.NETWORK,
                                                                                streamUtil);
        final CytoscapeCxNetworkReaderFactory cxReaderFactory = new CytoscapeCxNetworkReaderFactory(cytoscapejsReaderFilter,
                                                                                                    application_manager,
                                                                                                    cyNetworkFactory,
                                                                                                    cyNetworkManager,
                                                                                                    cyRootNetworkManager,
                                                                                                    visual_mapping_manager,
                                                                                                    renderingEngineMgr);
        final Properties cytoscapeJsNetworkReaderFactoryProps = new Properties();

        // This is the unique identifier for this reader. 3rd party developer
        // can use this service by using this ID.
        cytoscapeJsNetworkReaderFactoryProps.put(ID, "cytoscapejsNetworkReaderFactory");
        registerService(bc, cxReaderFactory, InputStreamTaskFactory.class, cytoscapeJsNetworkReaderFactoryProps);

    }
}