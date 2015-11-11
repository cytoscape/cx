package org.cytoscape.io.internal.cx_reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.cxio.aux.AspectElementCounts;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.metadata.MetaDataCollection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

    private static final boolean               DEBUG = true;

    private final List<CyNetwork>              _networks;
    private final String                       _network_collection_name;
    private CxToCy                             _cx_to_cy;
    private final InputStream                  _in;
    private final VisualMappingManager         _visual_mapping_manager;
    private final RenderingEngineManager       _rendering_engine_manager;
    private final CyNetworkViewFactory         _networkview_factory;
    private final boolean                      _perform_basic_integrity_checks;
    private final VisualStyleFactory           _visual_style_factory;
    private final VisualMappingFunctionFactory _vmf_factory_c;
    private final VisualMappingFunctionFactory _vmf_factory_d;
    private final VisualMappingFunctionFactory _vmf_factory_p;

    public CytoscapeCxNetworkReader(final String network_collection_name,
                                    final InputStream input_stream,
                                    final CyApplicationManager application_manager,
                                    final CyNetworkFactory network_factory,
                                    final CyNetworkManager network_manager,
                                    final CyRootNetworkManager root_network_manager,
                                    final VisualMappingManager visual_mapping_manager,
                                    final VisualStyleFactory visual_style_factory,
                                    final RenderingEngineManager rendering_engine_manager,
                                    final CyNetworkViewFactory networkview_factory,
                                    final VisualMappingFunctionFactory vmf_factory_c,
                                    final VisualMappingFunctionFactory vmf_factory_d,
                                    final VisualMappingFunctionFactory vmf_factory_p,

                                    final boolean perform_basic_integrity_checks) throws IOException {

        super(input_stream, networkview_factory, network_factory, network_manager, root_network_manager);

        if (input_stream == null) {
            throw new NullPointerException("input stream cannot be null");
        }
        _in = input_stream;
        _network_collection_name = network_collection_name;
        _visual_mapping_manager = visual_mapping_manager;
        _rendering_engine_manager = rendering_engine_manager;
        _networkview_factory = networkview_factory;
        _networks = new ArrayList<CyNetwork>();
        _perform_basic_integrity_checks = perform_basic_integrity_checks;
        _visual_style_factory = visual_style_factory;
        _vmf_factory_c = vmf_factory_c;
        _vmf_factory_d = vmf_factory_d;
        _vmf_factory_p = vmf_factory_p;
    }

    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {

        final CyNetworkView view = ViewMaker.makeView(network,
                                                      _cx_to_cy,
                                                      _network_collection_name,
                                                      _networkview_factory,
                                                      _rendering_engine_manager,
                                                      _visual_mapping_manager,
                                                      _visual_style_factory,
                                                      _vmf_factory_c,
                                                      _vmf_factory_d,
                                                      _vmf_factory_p);
        view.updateView();
        return view;

    }

    @Override
    public CyNetwork[] getNetworks() {
        final CyNetwork[] results = new CyNetwork[_networks.size()];
        for (int i = 0; i < results.length; ++i) {
            results[i] = _networks.get(i);
        }
        return results;
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
        aspects.addAspect(Aspect.HIDDEN_ATTRIBUTES);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.NETWORK_RELATIONS);
        aspects.addAspect(Aspect.SUBNETWORKS);
        aspects.addAspect(Aspect.GROUPS);

        final CxImporter cx_importer = CxImporter.createInstance();

        long t0 = 0;
        SortedMap<String, List<AspectElement>> res = null;
        if (TimingUtil.TIMING) {
            final byte[] buff = new byte[8000];
            int bytes_read = 0;
            final ByteArrayOutputStream bao = new ByteArrayOutputStream();
            while ((bytes_read = _in.read(buff)) != -1) {
                bao.write(buff, 0, bytes_read);
            }
            final ByteArrayInputStream bis = new ByteArrayInputStream(bao.toByteArray());
            t0 = System.currentTimeMillis();
            final CxReader cxr = cx_importer.obtainCxReader(aspects, bis);

            res = TimingUtil.parseAsMap(cxr, t0);
            TimingUtil.reportTimeDifference(t0, "total time parsing", -1);
            t0 = System.currentTimeMillis();
        }
        else {
            final CxReader cxr = cx_importer.obtainCxReader(aspects, _in);
            res = CxReader.parseAsMap(cxr);
            final AspectElementCounts counts = cxr.getAspectElementCounts();
            final MetaDataCollection pre = cxr.getPreMetaData();
            final MetaDataCollection post = cxr.getPostMetaData();
            if (DEBUG) {
                if (counts != null) {
                    System.out.println("Aspects elements read in:");
                    System.out.println(counts);
                }
                if (pre != null) {
                    System.out.println("Pre metadata :");
                    System.out.println(post);
                }
                if (post != null) {
                    System.out.println("Post metadata :");
                    System.out.println(post);
                }
            }
        }

        _cx_to_cy = new CxToCy();

        // Select the root collection name from the list.
        if (_network_collection_name != null) {
            final ListSingleSelection<String> rootList = getRootNetworkList();
            if (rootList.getPossibleValues().contains(_network_collection_name)) {
                // Collection already exists.
                rootList.setSelectedValue(_network_collection_name);
            }
        }

        final CyRootNetwork root_network = getRootNetwork();

        // Select Network Collection
        // 1. Check from Tunable
        // 2. If not available, use optional parameter

        if (root_network != null) {
            // Root network exists
            // subNetwork = root_network.addSubNetwork();
            // _network = _cx_to_cy.createNetwork(res, subNetwork, null);
            _networks.addAll(_cx_to_cy.createNetwork(res, root_network, null, null, _perform_basic_integrity_checks));
        }
        else {
            // Need to create new network with new root.
            // subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
            // _network = _cx_to_cy.createNetwork(res, subNetwork,
            // _network_collection_name);
            _networks.addAll(_cx_to_cy.createNetwork(res,
                                                     null,
                                                     cyNetworkFactory,
                                                     _network_collection_name,
                                                     _perform_basic_integrity_checks));
        }

        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "total time build", 0);
        }
    }

}