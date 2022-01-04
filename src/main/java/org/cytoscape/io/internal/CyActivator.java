package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.io.internal.cx_reader.CytoscapeCx2FileFilter;
import org.cytoscape.io.internal.cx_reader.CytoscapeCx2NetworkReaderFactory;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReaderFactory;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
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
    	CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
    	CyServiceModule.setServiceRegistrar(serviceRegistrar);
    	StreamUtil streamUtil = getService(bc, StreamUtil.class);
        
    	final CytoscapeCxFileFilter cx_filter = new CytoscapeCxFileFilter(streamUtil);

        final CxNetworkWriterFactory network_writer_factory = new CxNetworkWriterFactory(cx_filter, false);

        final Properties cx_writer_factory_properties = new Properties();

        cx_writer_factory_properties.put(ID, "cxNetworkWriterFactory");

        registerAllServices(bc, network_writer_factory, cx_writer_factory_properties);

        final CytoscapeCxFileFilter cx2Filter = new CytoscapeCx2FileFilter(streamUtil);
        final CxNetworkWriterFactory cx2networkWriterFactory = 
        		new CxNetworkWriterFactory(cx2Filter,true);
        final Properties cx2_writer_factory_properties = new Properties();

        cx2_writer_factory_properties.put(ID, "cx2NetworkWriterFactory");

        registerAllServices(bc, cx2networkWriterFactory, cx2_writer_factory_properties);
        
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

        // cx2 reader
        final CytoscapeCx2NetworkReaderFactory cx2ReaderFactory = new CytoscapeCx2NetworkReaderFactory(cx2Filter);
        final Properties cx2ReaderFactoryProperties = new Properties();
        cx2ReaderFactoryProperties.put(ID, "cytoscapeCx2NetworkReaderFactory");
        registerService(bc, cx2ReaderFactory, InputStreamTaskFactory.class, cx2ReaderFactoryProperties);
        
    }
}