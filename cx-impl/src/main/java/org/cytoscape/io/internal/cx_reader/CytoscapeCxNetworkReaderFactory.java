package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.work.TaskIterator;

public class CytoscapeCxNetworkReaderFactory extends AbstractInputStreamTaskFactory {

    private final CyApplicationManager cyApplicationManager;
    protected final CyNetworkFactory   cyNetworkFactory;
    private final CyNetworkManager     cyNetworkManager;
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
    public TaskIterator createTaskIterator(final InputStream is, final String collectionName) {
        try {

            return new TaskIterator(new CytoscapeCxNetworkReader(collectionName, is,
                                                                 cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager));
        }
        catch (final IOException e) {

            e.printStackTrace();
            return null;
        }
    }
}
