package org.cytoscape.io.internal;

import java.util.Properties;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.cxio.CxNetworkWriterFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyWriterFactory;
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
	public void start(BundleContext bc) {
		
		StreamUtil streamUtil = getService(bc, StreamUtil.class);
		BasicCyFileFilter cxFilter = new BasicCyFileFilter(new String[]{"cx"}, new String[]{"text/cx"}, "CX",DataCategory.NETWORK, streamUtil);
		CxNetworkWriterFactory cxNetworkViewWriterFactory = new CxNetworkWriterFactory(cxFilter);
		registerService(bc, cxNetworkViewWriterFactory, CyWriterFactory.class, new Properties());
		
	}
}