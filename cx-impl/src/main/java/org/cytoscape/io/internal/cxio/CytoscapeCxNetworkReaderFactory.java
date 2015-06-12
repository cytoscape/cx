package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.cxio.kit.AspectFragmentReader;
import org.cytoscape.io.internal.cxio.kit.CartesianLayoutFragmentReader;
import org.cytoscape.io.internal.cxio.kit.EdgeAttributesFragmentReader;
import org.cytoscape.io.internal.cxio.kit.EdgesFragmentReader;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesFragmentReader;
import org.cytoscape.io.internal.cxio.kit.NodesFragmentReader;
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
            final Set<AspectFragmentReader> aspect_fragment_readers = new HashSet<AspectFragmentReader>();

            aspect_fragment_readers.add(NodesFragmentReader.createInstance());
            aspect_fragment_readers.add(EdgesFragmentReader.createInstance());
            aspect_fragment_readers.add(NodeAttributesFragmentReader.createInstance());
            aspect_fragment_readers.add(EdgeAttributesFragmentReader.createInstance());
            aspect_fragment_readers.add(CartesianLayoutFragmentReader.createInstance());

            return new TaskIterator(new CytoscapeCxNetworkReader(collectionName, is,
                                                                 cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager,
                                                                 aspect_fragment_readers));
        }
        catch (final IOException e) {

            e.printStackTrace();
            return null;
        }
    }
}
