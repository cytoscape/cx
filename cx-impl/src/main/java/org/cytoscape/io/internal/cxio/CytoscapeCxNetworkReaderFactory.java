package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskIterator;

public class CytoscapeCxNetworkReaderFactory extends AbstractInputStreamTaskFactory {

	private final CyApplicationManager cyApplicationManager;
	protected final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;

	public CytoscapeCxNetworkReaderFactory(final CyFileFilter filter,
										   final CyApplicationManager cyApplicationManager,
										   final CyNetworkFactory cyNetworkFactory,
										   final CyNetworkManager cyNetworkManager,
										   final CyRootNetworkManager cyRootNetworkManager) {
		super(filter);
		this.cyApplicationManager = cyApplicationManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream is, String collectionName)  {
		try {
		    final Set<String> aspects = new HashSet<String>(); 
		    aspects.add(CxConstants.EDGES);
		    aspects.add(CxConstants.EDGES);
		    aspects.add(CxConstants.NODE_ATTRIBUTES);
		    aspects.add(CxConstants.EDGE_ATTRIBUTES);
		    aspects.add(CxConstants.CARTESIAN_LAYOUT);
		    
            return new TaskIterator(new CytoscapeCxNetworkReader(collectionName, is, cyApplicationManager, cyNetworkFactory,
            		cyNetworkManager, cyRootNetworkManager, aspects));
        }
        catch (IOException e) {
          
            e.printStackTrace();
            return null;
        }
	}
}
