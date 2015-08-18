package org.cytoscape.io.internal.cx_reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

    private final List<CyNetwork>        _networks;
    private final String                 _network_collection_name;
    private CxToCy                       _cx_to_cy;
    private final InputStream            _in;
    private final VisualMappingManager   _visual_mapping_manager;
    private final RenderingEngineManager _rendering_engine_manager;

    public CytoscapeCxNetworkReader(final String networkCollectionName,
                                    final InputStream input_stream,
                                    final CyApplicationManager cyApplicationManager,
                                    final CyNetworkFactory cyNetworkFactory,
                                    final CyNetworkManager cyNetworkManager,
                                    final CyRootNetworkManager cyRootNetworkManager,
                                    final VisualMappingManager visualMappingManager,
                                    final RenderingEngineManager renderingEngineMgr) throws IOException {
        super(input_stream, cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

        if (input_stream == null) {
            throw new NullPointerException("input stream cannot be null");
        }
        _in = input_stream;
        _network_collection_name = networkCollectionName;
        _visual_mapping_manager = visualMappingManager;
        _rendering_engine_manager = renderingEngineMgr;
        _networks = new ArrayList<CyNetwork>();
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
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {

        final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);

        final VisualLexicon lexicon = _rendering_engine_manager.getDefaultVisualLexicon();

        if ((_cx_to_cy.getNetworkVisualPropertiesElement() != null)
                && (_cx_to_cy.getNetworkVisualPropertiesElement().getProperties() != null)) {
            setProperties(lexicon, _cx_to_cy.getNetworkVisualPropertiesElement().getProperties(), view, CyNetwork.class);
        }

        final VisualStyle default_style = _visual_mapping_manager.getDefaultVisualStyle();
        setProperties(lexicon,
                      _cx_to_cy.getNodesDefaultVisualPropertiesElement().getProperties(),
                      default_style,
                      CyNode.class);
        setProperties(lexicon,
                      _cx_to_cy.getEdgesDefaultVisualPropertiesElement().getProperties(),
                      default_style,
                      CyEdge.class);

        final Map<CyNode, VisualPropertiesElement> node_vpe = _cx_to_cy.getNodeVisualPropertiesElementsMap();
        for (final CyNode node : node_vpe.keySet()) {
            setProperties(lexicon, node_vpe.get(node).getProperties(), view.getNodeView(node), CyNode.class);
        }

        final Map<CyEdge, VisualPropertiesElement> edge_vpe = _cx_to_cy.getEdgeVisualPropertiesElementsMap();
        for (final CyEdge edge : edge_vpe.keySet()) {
            setProperties(lexicon, edge_vpe.get(edge).getProperties(), view.getEdgeView(edge), CyEdge.class);
        }

        final Map<String, VisualElementCollection> visual_element_collection_map = _cx_to_cy
                .getVisualElementCollectionMap();
        if (!visual_element_collection_map.containsKey(obtainNetworkId(network))) {
            System.out.println(_cx_to_cy.getNetworkSuidToNetworkRelationsMap());
            throw new IllegalArgumentException("no visual elements found for: " + obtainNetworkId(network));
        }

        final VisualElementCollection visual_element_collection = visual_element_collection_map
                .get(obtainNetworkId(network));
        final Map<CyNode, CartesianLayoutElement> position_map_for_view = visual_element_collection.getPositionMap();

        for (final CyNode node : position_map_for_view.keySet()) {
            final CartesianLayoutElement e = position_map_for_view.get(node);
            view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, e.getX());
            view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, e.getY());
            view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, e.getZ());
        }

        return view;
    }

    private final String obtainNetworkId(final CyNetwork network) {
        return _cx_to_cy.getNetworkSuidToNetworkRelationsMap().get(network.getSUID());
        // return String.valueOf(network.getSUID()); //TODO this is
        // INCORRECT!!!!! FIXME FIXME
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void setProperties(final VisualLexicon lexicon,
                                            final SortedMap<String, String> props,
                                            final View view,
                                            final Class my_class) {
        for (final Map.Entry<String, String> entry : props.entrySet()) {
            final VisualProperty vp = lexicon.lookup(my_class, entry.getKey());
            if (vp != null) {
                final Object parsed_value = vp.parseSerializableString(entry.getValue());
                if (parsed_value != null) {
                    if (shouldSetAsLocked(vp)) {
                        view.setLockedValue(vp, parsed_value);
                    }
                    else {
                        view.setVisualProperty(vp, parsed_value);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void setProperties(final VisualLexicon lexicon,
                                            final SortedMap<String, String> props,
                                            final VisualStyle style,
                                            final Class my_class) {
        for (final Map.Entry<String, String> entry : props.entrySet()) {
            final VisualProperty vp = lexicon.lookup(my_class, entry.getKey());
            if (vp != null) {
                final Object parsed_value = vp.parseSerializableString(entry.getValue());
                if (parsed_value != null) {
                    style.setDefaultValue(vp, parsed_value);

                }
            }
        }
    }

    private static final Pattern DIRECT_NET_PROPS_PATTERN = Pattern
                                                                  .compile("GRAPH_VIEW_(ZOOM|CENTER_(X|Y))|NETWORK_(WIDTH|HEIGHT|SCALE_FACTOR|CENTER_(X|Y|Z)_LOCATION)");

    @SuppressWarnings("rawtypes")
    private final static boolean shouldSetAsLocked(final VisualProperty vp) {
        if (vp.getTargetDataType() == CyNode.class) {
            if ((vp == BasicVisualLexicon.NODE_X_LOCATION) || (vp == BasicVisualLexicon.NODE_Y_LOCATION)
                    || (vp == BasicVisualLexicon.NODE_Z_LOCATION)) {
                return false;
            }
        }
        else if (vp.getTargetDataType() == CyNetwork.class) { // TODO //FIXME
            final Matcher netMatcher = DIRECT_NET_PROPS_PATTERN.matcher(vp.getIdString());
            return !netMatcher.matches();
        }
        return true;
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
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
            final CxReader cxr = cx_importer.getCxReader(aspects, bis);

            res = TimingUtil.parseAsMap(cxr, t0);
            TimingUtil.reportTimeDifference(t0, "total time parsing", 0);
            t0 = System.currentTimeMillis();
        }
        else {
            res = cx_importer.readAsMap(aspects, _in);
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
            _networks.addAll(_cx_to_cy.createNetwork(res, root_network, null, null));
        }
        else {
            // Need to create new network with new root.
            // subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
            // _network = _cx_to_cy.createNetwork(res, subNetwork,
            // _network_collection_name);
            _networks.addAll(_cx_to_cy.createNetwork(res, null, cyNetworkFactory, _network_collection_name));
        }

        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "total time build", 0);
        }
    }
}