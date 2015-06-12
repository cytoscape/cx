package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.cxio.kit.AspectElement;
import org.cytoscape.io.internal.cxio.kit.AspectFragmentReader;
import org.cytoscape.io.internal.cxio.kit.CxReader;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

    private CyNetwork      network = null;       // Supports only one CyNetwork
    // per file.
    private final String   networkCollectionName;
    private final CxReader cx_reader;
    private CxToCy         cx_to_cy;

    public CytoscapeCxNetworkReader(final String networkCollectionName,
                                    final InputStream input_stream,
                                    final CyApplicationManager cyApplicationManager,
                                    final CyNetworkFactory cyNetworkFactory,
                                    final CyNetworkManager cyNetworkManager,
                                    final CyRootNetworkManager cyRootNetworkManager,
                                    final Set<AspectFragmentReader> aspect_fragment_readers)
                                            throws IOException {
        super(input_stream, cyApplicationManager, cyNetworkFactory, cyNetworkManager,
                cyRootNetworkManager);

        this.networkCollectionName = networkCollectionName;

        if (input_stream == null) {
            throw new NullPointerException("input stream cannot be null");
        }

        cx_reader = CxReader.createInstance(input_stream);

        for (final AspectFragmentReader aspect_fragment_reader : aspect_fragment_readers) {
            cx_reader.addAspectFragmentReader(aspect_fragment_reader);
        }
    }

    @Override
    public CyNetwork[] getNetworks() {
        final CyNetwork[] result = new CyNetwork[1];
        result[0] = network;
        return result;
    }

    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {
        final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
        final Map<CyNode, Double[]> positionMap = cx_to_cy.getNodePosition();
        for (final CyNode node : positionMap.keySet()) {
            final Double[] position = positionMap.get(node);
            view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
                                                     position[0]);
            view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
                                                     position[1]);
        }
        return view;
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {
        cx_reader.reset();
        final SortedMap<String, List<AspectElement>> res = CxReader.parseAsMap(cx_reader);

        cx_to_cy = new CxToCy();

        // Select the root collection name from the list.
        if (networkCollectionName != null) {
            final ListSingleSelection<String> rootList = getRootNetworkList();
            if (rootList.getPossibleValues().contains(networkCollectionName)) {
                // Collection already exists.
                rootList.setSelectedValue(networkCollectionName);
            }
        }

        final CyRootNetwork rootNetwork = getRootNetwork();

        // Select Network Collection
        // 1. Check from Tunable
        // 2. If not available, use optional parameter
        CySubNetwork subNetwork;
        if (rootNetwork != null) {
            // Root network exists
            subNetwork = rootNetwork.addSubNetwork();
            this.network = cx_to_cy.createNetwork(res, subNetwork, null);
        }
        else {
            // Need to create new network with new root.
            subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
            this.network = cx_to_cy.createNetwork(res, subNetwork, networkCollectionName);
        }
    }
}