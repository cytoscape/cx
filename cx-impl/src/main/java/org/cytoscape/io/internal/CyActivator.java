package org.cytoscape.io.internal;

import static org.cytoscape.work.ServiceProperties.ID;

import java.util.Properties;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.cxio.CxNetworkWriterFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
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
        final BasicCyFileFilter cxFilter = new BasicCyFileFilter(new String[] { "cx" },
                new String[] { "text/cx" }, "CX", DataCategory.NETWORK, streamUtil);
        final CxNetworkWriterFactory cxNetworkViewWriterFactory = new CxNetworkWriterFactory(
                cxFilter);
        
        final Properties cxWriterFactoryProperties = new Properties();
        cxWriterFactoryProperties.put(ID, "cxNetworkWriterFactory");
        registerAllServices(bc, cxNetworkViewWriterFactory, cxWriterFactoryProperties);
        
        
        //registerService(bc, cxNetworkViewWriterFactory, CyWriterFactory.class, new Properties());
        

    }
}