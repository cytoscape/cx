package org.cytoscape.io.internal.cx_reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.cytoscape.model.subnetwork.CySubNetwork;
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

    private CyNetwork                    _network = null;          // Supports
    // only one
    // CyNetwork
    // per file.
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
    }

    @Override
    public CyNetwork[] getNetworks() {
        final CyNetwork[] result = new CyNetwork[1];
        result[0] = _network;
        return result;
    }

    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {
        final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
        
        final VisualLexicon lexicon = _rendering_engine_manager.getDefaultVisualLexicon();

        setProperties(lexicon, _cx_to_cy.getNetworkVisualPropertiesElement().getProperties(), view, CyNetwork.class);
        
        final VisualStyle default_style =  _visual_mapping_manager.getDefaultVisualStyle();
        setProperties(lexicon, _cx_to_cy.getNodesDefaultVisualPropertiesElement().getProperties(), default_style , CyNode.class);
        setProperties(lexicon, _cx_to_cy.getEdgesDefaultVisualPropertiesElement().getProperties(), default_style , CyEdge.class);
        

        final Map<CyNode, VisualPropertiesElement> node_vpe = _cx_to_cy.getNodeVisualPropertiesElementsMap();
        for (final CyNode node : node_vpe.keySet()) {
            setProperties(lexicon, node_vpe.get(node).getProperties(), view.getNodeView(node), CyNode.class);
        }

        final Map<CyEdge, VisualPropertiesElement> edge_vpe = _cx_to_cy.getEdgeVisualPropertiesElementsMap();
        for (final CyEdge edge : edge_vpe.keySet()) {
            setProperties(lexicon, edge_vpe.get(edge).getProperties(), view.getEdgeView(edge), CyEdge.class);
        }

        return view;
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
            final CxReader cxr = cx_importer.getCxReader(aspects, _in);
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

        final CyRootNetwork rootNetwork = getRootNetwork();

        // Select Network Collection
        // 1. Check from Tunable
        // 2. If not available, use optional parameter
        CySubNetwork subNetwork;
        if (rootNetwork != null) {
            // Root network exists
            subNetwork = rootNetwork.addSubNetwork();
            _network = _cx_to_cy.createNetwork(res, subNetwork, null);
        }
        else {
            // Need to create new network with new root.
            subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
            _network = _cx_to_cy.createNetwork(res, subNetwork, _network_collection_name);
        }

        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "total time build", 0);
        }
    }
}