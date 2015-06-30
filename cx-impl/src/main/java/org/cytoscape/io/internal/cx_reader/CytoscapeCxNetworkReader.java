package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.cx_writer.CxNetworkViewWriter;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.CxImporter;
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

    private CyNetwork         network = null;       // Supports only one
    // CyNetwork
    // per file.
    private final String      networkCollectionName;
    // private final CxReader cx_reader;
    private CxToCy            cx_to_cy;
    private final InputStream in;

    public CytoscapeCxNetworkReader(final String networkCollectionName,
                                    final InputStream input_stream,
                                    final CyApplicationManager cyApplicationManager,
                                    final CyNetworkFactory cyNetworkFactory,
                                    final CyNetworkManager cyNetworkManager,
                                    final CyRootNetworkManager cyRootNetworkManager)
                                            throws IOException {
        super(input_stream, cyApplicationManager, cyNetworkFactory, cyNetworkManager,
                cyRootNetworkManager);

        this.networkCollectionName = networkCollectionName;

        if (input_stream == null) {
            throw new NullPointerException("input stream cannot be null");
        }

        in = input_stream;

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

    private static SortedMap<String, List<AspectElement>> parseAsMap(final CxReader cxr, long t)
            throws IOException {
        long time_total = 0;
        if (cxr == null) {
            throw new IllegalArgumentException("reader is null");
        }
        long prev_time = System.currentTimeMillis() - t;
        if (CxNetworkViewWriter.TIMING) {
            System.out.println();
            System.out.println();
        }
        final SortedMap<String, List<AspectElement>> all_aspects = new TreeMap<String, List<AspectElement>>();

        while (cxr.hasNext()) {
            t = System.currentTimeMillis();
            final List<AspectElement> aspects = cxr.getNext();

            if ((aspects != null) && !aspects.isEmpty()) {
                final String name = aspects.get(0).getAspectName();
                if (CxNetworkViewWriter.TIMING) {

                    reportTime(prev_time, name, aspects.size());
                    time_total += prev_time;
                    prev_time = System.currentTimeMillis() - t;
                }
                if (!all_aspects.containsKey(name)) {
                    all_aspects.put(name, aspects);
                }
                else {
                    all_aspects.get(name).addAll(aspects);
                }
            }
        }
        reportTime(time_total, "sum", 0);
        return all_aspects;
    }

    public final static void reportTime(final long t, final String label, final int n) {

        System.out.println(String.format("%-20s%-8s: %s ms", label, n, t));

    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);

        final CxImporter cx_importer = CxImporter.createInstance();

        final long t0 = System.currentTimeMillis();
        final CxReader cxr = cx_importer.getCxReader(aspects, in);

        final SortedMap<String, List<AspectElement>> res = parseAsMap(cxr, t0);

        if (CxNetworkViewWriter.TIMING) {
            CxExporter.reportTime(t0, "total time", 0);

        }

        // final SortedMap<String, List<AspectElement>> res =
        // cx_importer.readAsMap(aspects, in);

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