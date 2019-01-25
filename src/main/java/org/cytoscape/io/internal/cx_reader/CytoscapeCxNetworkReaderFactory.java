package org.cytoscape.io.internal.cx_reader;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class CytoscapeCxNetworkReaderFactory extends AbstractInputStreamTaskFactory {

    public CytoscapeCxNetworkReaderFactory(final CyFileFilter filter) {
        super(filter);

    }

    @Override
    public TaskIterator createTaskIterator(final InputStream is, final String collection_name) {
    	
       return new TaskIterator(new CytoscapeCxNetworkReader(is, 
    		   collection_name,
    		   CyServiceModule.getService(CyNetworkViewFactory.class),
    		   CyServiceModule.getService(CyNetworkFactory.class),
    		   CyServiceModule.getService(CyNetworkManager.class),
    		   CyServiceModule.getService(CyRootNetworkManager.class)));       
    }
}
