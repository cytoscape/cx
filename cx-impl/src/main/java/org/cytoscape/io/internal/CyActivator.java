package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReaderFactory;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
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
        final CyNetworkManager network_manager = getService(bc, CyNetworkManager.class);
        final CyGroupManager group_manager = getService(bc, CyGroupManager.class);
        final CyNetworkTableManager table_manager = getService(bc, CyNetworkTableManager.class);
        final CyNetworkViewFactory network_view_factory = getService(bc, CyNetworkViewFactory.class);

        final CxNetworkWriterFactory network_writer_factory = new CxNetworkWriterFactory(cx_filter,
                                                                                         visual_mapping_manager,
                                                                                         application_manager,
                                                                                         networkview_manager,
                                                                                         network_manager,
                                                                                         group_manager,
                                                                                         table_manager);

        final Properties cx_writer_factory_properties = new Properties();

        cx_writer_factory_properties.put(ID, "cxNetworkWriterFactory");

        registerAllServices(bc, network_writer_factory, cx_writer_factory_properties);

        // Reader:
        final CyNetworkFactory network_factory = getService(bc, CyNetworkFactory.class);
        final CyRootNetworkManager root_network_manager = getService(bc, CyRootNetworkManager.class);
        final RenderingEngineManager rendering_engine_manager = getService(bc, RenderingEngineManager.class);
        final VisualStyleFactory visual_style_factory = getService(bc, VisualStyleFactory.class);

        final BasicCyFileFilter basic_file_filter = new BasicCyFileFilter(new String[] { "cx" },
                                                                          new String[] { "application/json" },
                                                                          "CX JSON",
                                                                          DataCategory.NETWORK,
                                                                          streamUtil);

        final VisualMappingFunctionFactory vmfFactoryC = getService(bc,
                                                                    VisualMappingFunctionFactory.class,
                "(mapping.type=continuous)");
        final VisualMappingFunctionFactory vmfFactoryD = getService(bc,
                                                                    VisualMappingFunctionFactory.class,
                "(mapping.type=discrete)");
        final VisualMappingFunctionFactory vmfFactoryP = getService(bc,
                                                                    VisualMappingFunctionFactory.class,
                "(mapping.type=passthrough)");

        final CytoscapeCxNetworkReaderFactory cx_reader_factory = new CytoscapeCxNetworkReaderFactory(basic_file_filter,
                                                                                                      application_manager,
                                                                                                      network_factory,
                                                                                                      network_manager,
                                                                                                      root_network_manager,
                                                                                                      visual_mapping_manager,
                                                                                                      visual_style_factory,
                                                                                                      rendering_engine_manager,
                                                                                                      network_view_factory,
                                                                                                      vmfFactoryC,
                                                                                                      vmfFactoryD,
                                                                                                      vmfFactoryP

                );
        final Properties reader_factory_properties = new Properties();

        // This is the unique identifier for this reader. 3rd party developer
        // can use this service by using this ID.
        reader_factory_properties.put(ID, "cytoscapejsNetworkReaderFactory");
        registerService(bc, cx_reader_factory, InputStreamTaskFactory.class, reader_factory_properties);

    }
}