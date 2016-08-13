package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.cxio.misc.AspectElementCounts;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.metadata.MetaDataCollection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.Settings;
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

    private final List<CyNetwork>              _networks;
    private String                             _network_collection_name;
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
    private final CyGroupFactory               _group_factory;

    public CytoscapeCxNetworkReader(final String network_collection_name,
                                    final InputStream input_stream,
                                    final CyApplicationManager application_manager,
                                    final CyNetworkFactory network_factory,
                                    final CyNetworkManager network_manager,
                                    final CyRootNetworkManager root_network_manager,
                                    final CyGroupFactory group_factory,
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
            throw new IllegalArgumentException("input stream must not be null");
        }
        _in = input_stream;
        _network_collection_name = network_collection_name;
        _visual_mapping_manager = visual_mapping_manager;
        _rendering_engine_manager = rendering_engine_manager;
        _networkview_factory = networkview_factory;
        _group_factory = group_factory;
        _networks = new ArrayList<CyNetwork>();
        _perform_basic_integrity_checks = perform_basic_integrity_checks;
        _visual_style_factory = visual_style_factory;
        _vmf_factory_c = vmf_factory_c;
        _vmf_factory_d = vmf_factory_d;
        _vmf_factory_p = vmf_factory_p;
    }

    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {

        CyNetworkView view = null;
        try {
            view = ViewMaker.makeView(network,
                                      _cx_to_cy,
                                      _network_collection_name,
                                      _networkview_factory,
                                      _rendering_engine_manager,
                                      _visual_mapping_manager,
                                      _visual_style_factory,
                                      _vmf_factory_c,
                                      _vmf_factory_d,
                                      _vmf_factory_p);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        if (view != null) {
            view.updateView();
        }
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

    		System.out.println("\n\n\n=============== RUN called ====================\n\n\n");

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
        aspects.addAspect(Aspect.VIEWS);
        aspects.addAspect(Aspect.TABLE_COLUMN_LABELS);

        final CxImporter cx_importer = CxImporter.createInstance();

        SortedMap<String, List<AspectElement>> res = null;

        final CxReader cxr = cx_importer.obtainCxReader(aspects, _in);
        final long t0 = System.currentTimeMillis();
        res = CxToCy.parseAsMap(cxr, t0, Settings.INSTANCE.isTiming());
        if (Settings.INSTANCE.isTiming()) {
            TimingUtil.reportTimeDifference(t0, "total time parsing", -1);
        }
        final AspectElementCounts counts = cxr.getAspectElementCounts();
        final MetaDataCollection pre = cxr.getPreMetaData();
        final MetaDataCollection post = cxr.getPostMetaData();
        if (Settings.INSTANCE.isDebug()) {
            if (counts != null) {
                System.out.println("Aspects elements read in:");
                System.out.println(counts);
            }
            else {
            	System.out.println("No aspects elements read in (!)");
            }
            if (pre != null) {
                System.out.println("Pre metadata:");
                System.out.println(pre);
            }
            else {
            	System.out.println("No pre metadata");
            }
            if (post != null) {
                System.out.println("Post metadata:");
                System.out.println(post);
            }
            else {
            	System.out.println("No post metadata");
            }
        }

        _cx_to_cy = new CxToCy();

        // Select the root collection name from the list.
        if (_network_collection_name != null) {
            final ListSingleSelection<String> root_list = getRootNetworkList();
            if (root_list.getPossibleValues().contains(_network_collection_name)) {
                // Collection already exists.
                root_list.setSelectedValue(_network_collection_name);
            }
        }

        final CyRootNetwork root_network = getRootNetwork();

        // Select Network Collection
        // 1. Check from Tunable
        // 2. If not available, use optional parameter

        if (root_network != null) {
            // Root network exists
            _networks.addAll(_cx_to_cy.createNetwork(res,
                                                     root_network,
                                                     null,
                                                     _group_factory,
                                                     null,
                                                     _perform_basic_integrity_checks));
        }
        else {
            // Need to create new network with new root.
            if (Settings.INSTANCE.isAllowToUseNetworkCollectionNameFromNetworkAttributes()) {
                final String collection_name_from_network_attributes = CxToCy
                        .getCollectionNameFromNetworkAttributes(res);
                if (collection_name_from_network_attributes != null) {
                    _network_collection_name = collection_name_from_network_attributes;
                    if (Settings.INSTANCE.isDebug()) {
                        System.out.println("collection name from network attributes: " + _network_collection_name);
                    }
                }
            }
            _networks.addAll(_cx_to_cy.createNetwork(res,
                                                     null,
                                                     cyNetworkFactory,
                                                     _group_factory,
                                                     _network_collection_name,
                                                     _perform_basic_integrity_checks));
        }

        if (Settings.INSTANCE.isTiming()) {
            System.out.println();
            TimingUtil.reportTimeDifference(t0, "total time to build network(s) (not views)", -1);
            System.out.println();
        }
    }

}