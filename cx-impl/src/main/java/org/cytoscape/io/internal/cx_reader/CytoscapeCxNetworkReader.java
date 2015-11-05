package org.cytoscape.io.internal.cx_reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.cxio.aux.AspectElementCounts;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.metadata.MetaDataCollection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.CxUtil;
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
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

    private static final Pattern               DIRECT_NET_PROPS_PATTERN = Pattern
                                                                                .compile("GRAPH_VIEW_(ZOOM|CENTER_(X|Y))|NETWORK_(WIDTH|HEIGHT|SCALE_FACTOR|CENTER_(X|Y|Z)_LOCATION)");

    private static final boolean               DEBUG                    = true;

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

        final CyNetworkView view = _networkview_factory.createNetworkView(network);

        final VisualLexicon lexicon = _rendering_engine_manager.getDefaultVisualLexicon();

        final VisualElementCollectionMap collection = _cx_to_cy.getVisualElementCollectionMap();

        final String network_id = obtainNetworkId(network);

        if (collection != null) {

            if (collection.getNetworkVisualPropertiesElement(network_id) != null) {
                setProperties(lexicon,
                              collection.getNetworkVisualPropertiesElement(network_id).getProperties(),
                              view,
                              CyNetwork.class);
            }
            VisualStyle new_visual_style = null;

            if ((collection.getNodesDefaultVisualPropertiesElement(network_id) != null)
                    || (collection.getEdgesDefaultVisualPropertiesElement(network_id) != null)) {
                final VisualStyle default_visual_style = _visual_mapping_manager.getDefaultVisualStyle();
                new_visual_style = _visual_style_factory.createVisualStyle(default_visual_style);

                // Disable "Lock Node height and width"
                for (final VisualPropertyDependency<?> dep : new_visual_style.getAllVisualPropertyDependencies()) {
                    if (dep.getIdString().equals("nodeCustomGraphicsSizeSync")
                            || dep.getIdString().equals("arrowColorMatchesEdge")) {
                        dep.setDependency(true);
                    }
                    else if (dep.getIdString().equals("nodeSizeLocked")) {
                        dep.setDependency(false);
                    }
                }

                final String viz_style_title = createTitleForNewVisualStyle();

                removeVisualStyle(viz_style_title);

                new_visual_style.setTitle(viz_style_title);

                if (collection.getNodesDefaultVisualPropertiesElement(network_id) != null) {
                    setProperties(lexicon, collection.getNodesDefaultVisualPropertiesElement(network_id)
                            .getProperties(), new_visual_style, CyNode.class);
                }
                if (collection.getEdgesDefaultVisualPropertiesElement(network_id) != null) {
                    setProperties(lexicon, collection.getEdgesDefaultVisualPropertiesElement(network_id)
                            .getProperties(), new_visual_style, CyEdge.class);
                }
            }

            setNodeVisualProperties(view, lexicon, collection, network_id);

            setEdgeVisualProperties(view, lexicon, collection, network_id);

            final Map<CyNode, CartesianLayoutElement> position_map_for_view = collection
                    .getCartesianLayoutElements(network_id);

            if ((position_map_for_view != null) && (view != null)) {
                for (final CyNode node : position_map_for_view.keySet()) {
                    if (node != null) {
                        final CartesianLayoutElement e = position_map_for_view.get(node);
                        if (e != null) {
                            final View<CyNode> node_view = view.getNodeView(node);
                            if (node_view != null) {
                                node_view.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
                                                            Double.valueOf(e.getX()));
                                node_view.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
                                                            Double.valueOf(e.getY()));
                                if (e.isZset()) {
                                    node_view.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION,
                                                                Double.valueOf(e.getZ()));
                                }
                            }
                        }
                    }
                }
            }

            if ((collection.getNodesDefaultVisualPropertiesElement(network_id) != null)
                    || (collection.getEdgesDefaultVisualPropertiesElement(network_id) != null)) {
                _visual_mapping_manager.addVisualStyle(new_visual_style);
                new_visual_style.apply(view);
                // _visual_mapping_manager.setCurrentVisualStyle(new_visual_style);
                _visual_mapping_manager.setVisualStyle(new_visual_style, view);

            }
        }
        view.updateView();
        return view;
    }

    private void removeVisualStyle(final String viz_style_title) {
        final Iterator<VisualStyle> it = _visual_mapping_manager.getAllVisualStyles().iterator();
        while (it.hasNext()) {
            final VisualStyle vs = it.next();
            if (vs.getTitle().equalsIgnoreCase(viz_style_title)) {
                _visual_mapping_manager.removeVisualStyle(vs);
                break;
            }
        }
    }

    private String createTitleForNewVisualStyle() {
        String viz_style_title = "new vizStyle";
        if (_network_collection_name != null) {
            if (_network_collection_name.toLowerCase().endsWith(".cx")) {
                viz_style_title = String.format("%s-Style", _network_collection_name.substring(0,
                                                                                               _network_collection_name
                                                                                                       .length() - 3));
            }
            else {
                viz_style_title = String.format("%s-Style", _network_collection_name);
            }
        }
        return viz_style_title;
    }

    @Override
    public CyNetwork[] getNetworks() {
        final CyNetwork[] results = new CyNetwork[_networks.size()];
        for (int i = 0; i < results.length; ++i) {
            results[i] = _networks.get(i);
        }
        return results;
    }

    private final String obtainNetworkId(final CyNetwork network) {
        return _cx_to_cy.getNetworkSuidToNetworkRelationsMap().get(network.getSUID());
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

    private final void setEdgeVisualProperties(final CyNetworkView view,
                                               final VisualLexicon lexicon,
                                               final VisualElementCollectionMap collection,
                                               final String subnetwork_id) {
        final Set<CyEdge> edges_vpe = _cx_to_cy.getEdgesWithVisualProperties();
        if (edges_vpe != null) {
            for (final CyEdge edge : edges_vpe) {
                final Map<CyEdge, CyVisualPropertiesElement> evpm = collection
                        .getEdgeVisualPropertiesElementsMap(subnetwork_id);
                final CyVisualPropertiesElement vpe = evpm.get(edge);
                if (DEBUG) {
                    if (vpe == null) {
                        System.out.println(">>>> edge vpe is null");
                    }
                }
                if (vpe != null) {
                    final SortedMap<String, String> props = vpe.getProperties();
                    if (props != null) {
                        final View<CyEdge> v = view.getEdgeView(edge);
                        setProperties(lexicon, props, v, CyEdge.class);
                    }
                }
            }
        }
    }

    private final void setNodeVisualProperties(final CyNetworkView view,
                                               final VisualLexicon lexicon,
                                               final VisualElementCollectionMap collection,
                                               final String subnetwork_id) {
        final Set<CyNode> nodes_vpe = _cx_to_cy.getNodesWithVisualProperties();
        if (nodes_vpe != null) {
            for (final CyNode node : nodes_vpe) {
                final Map<CyNode, CyVisualPropertiesElement> nvpm = collection
                        .getNodeVisualPropertiesElementsMap(subnetwork_id);
                final CyVisualPropertiesElement vpe = nvpm.get(node);
                if (DEBUG) {
                    if (vpe == null) {
                        System.out.println(">>>> node vpe is null");
                    }

                }
                if (vpe != null) {
                    final SortedMap<String, String> props = vpe.getProperties();
                    if (props != null) {
                        final View<CyNode> v = view.getNodeView(node);
                        setProperties(lexicon, props, v, CyNode.class);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void setProperties(final VisualLexicon lexicon,
                                            final SortedMap<String, String> props,
                                            final View view,
                                            final Class my_class) {
        if (props != null) {
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
    }

    @SuppressWarnings({ "rawtypes" })
    private final void setProperties(final VisualLexicon lexicon,
                                     final SortedMap<String, String> props,
                                     final VisualStyle style,
                                     final Class my_class) {
        if (props != null) {

            for (final Map.Entry<String, String> entry : props.entrySet()) {
                final String key = entry.getKey();
                boolean is_mapping = false;
                char mapping = '?';
                String mapping_key = null;
                if (key.startsWith(CxUtil.PASSTHROUGH_MAPPING)) {
                    is_mapping = true;
                    mapping = 'p';
                    mapping_key = key.substring(20);
                }
                else if (key.startsWith(CxUtil.CONTINUOUS_MAPPING)) {
                    is_mapping = true;
                    mapping = 'c';
                    mapping_key = key.substring(19);
                }
                else if (key.startsWith(CxUtil.DISCRETE_MAPPING)) {
                    is_mapping = true;
                    mapping = 'd';
                    mapping_key = key.substring(17);
                }

                if (is_mapping) {
                    final VisualProperty vp = lexicon.lookup(my_class, mapping_key);
                    final StringParser sp = new StringParser(entry.getValue());
                    final String col = sp.get(CxUtil.VM_COL);
                    final String type = sp.get(CxUtil.VM_TYPE);
                    final Class<?> type_class = toClass(type);
                    if (vp != null) {
                        if (mapping == 'p') {
                            addPasstroughMapping(style, vp, col, type_class);
                        }
                        else if (mapping == 'c') {
                            addContinuousMapping(style, vp, sp, col, type, type_class);
                        }
                        else if (mapping == 'd') {
                            addDiscreteMappingFunction(style, vp, sp, col, type, type_class);
                        }
                        else {
                            throw new IllegalStateException("unknown mapping type: " + mapping);
                        }
                    }
                }
                else {
                    final VisualProperty vp = lexicon.lookup(my_class, key);
                    if (vp != null) {
                        addDefaultVisualProperty(style, entry, vp);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final static void addDefaultVisualProperty(final VisualStyle style,
                                                       final Map.Entry<String, String> entry,
                                                       final VisualProperty vp) {
        final Object parsed_value = vp.parseSerializableString(entry.getValue());
        if (parsed_value != null) {
            style.setDefaultValue(vp, parsed_value);
        }
        else {
            System.out.println("could not parse serializable string from '" + entry.getValue() + "'");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final void addContinuousMapping(final VisualStyle style,
                                            final VisualProperty vp,
                                            final StringParser sp,
                                            final String col,
                                            final String type,
                                            final Class<?> type_class) {
        final ContinuousMapping cmf = (ContinuousMapping) _vmf_factory_c.createVisualMappingFunction(col,
                                                                                                     type_class,
                                                                                                     vp);

        if (cmf != null) {
            int counter = 0;
            while (true) {
                final String ov = sp.get("OV=" + counter);
                if (ov == null) {
                    break;
                }
                final String l = sp.get("L=" + counter);
                final String e = sp.get("E=" + counter);
                final String g = sp.get("G=" + counter);
                if ((l != null) && (e != null) && (g != null)) {
                    final Object lp = vp.parseSerializableString(l);
                    final Object ep = vp.parseSerializableString(e);
                    final Object gp = vp.parseSerializableString(g);
                    if ((lp != null) && (ep != null) && (gp != null)) {
                        final BoundaryRangeValues point = new BoundaryRangeValues(lp, ep, gp);
                        cmf.addPoint(toTypeValue(ov, type), point);
                    }
                    else {
                        System.out.println("could not parse from string in continuous mapping for col '" + col + "'");
                    }
                }
                else {
                    System.out.println("could not get expected values in continuous mapping for col '" + col + "'");
                }
                counter++;
            }
            style.addVisualMappingFunction(cmf);
        }
        else {
            System.out.println("could not create continuous mapping for col '" + col + "'");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addPasstroughMapping(final VisualStyle style,
                                      final VisualProperty vp,
                                      final String col,
                                      final Class<?> type_class) {
        final PassthroughMapping pmf = (PassthroughMapping) _vmf_factory_p.createVisualMappingFunction(col,
                                                                                                       type_class,
                                                                                                       vp);
        if (pmf != null) {
            style.addVisualMappingFunction(pmf);
        }
        else {
            System.out.println("could not create passthrough mapping for col '" + col + "'");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final void addDiscreteMappingFunction(final VisualStyle style,
                                                  final VisualProperty vp,
                                                  final StringParser sp,
                                                  final String col,
                                                  final String type,
                                                  final Class<?> type_class) {
        final DiscreteMapping dmf = (DiscreteMapping) _vmf_factory_d.createVisualMappingFunction(col, type_class, vp);
        if (dmf != null) {
            int counter = 0;
            while (true) {
                final String k = sp.get("K=" + counter);
                if (k == null) {
                    break;
                }
                final String v = sp.get("V=" + counter);
                if (v != null) {
                    final Object pv = vp.parseSerializableString(v);
                    if (pv != null) {
                        dmf.putMapValue(toTypeValue(k, type), pv);
                    }
                    else {
                        System.out.println("could not parse serializable string from discrete mapping value '" + v
                                + "'");
                    }
                }
                else {
                    System.out.println("error: discrete mapping function string is corrupt");
                }
                counter++;
            }
            style.addVisualMappingFunction(dmf);
        }
        else {
            System.out.println("could not create discrete mapping for col '" + col + "'");
        }
    }

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

    private class StringParser {
        final Map<String, String> _data = new HashMap<String, String>();

        StringParser(final String str) {
            final StringTokenizer t = new StringTokenizer(str, ",");
            while (t.hasMoreTokens()) {
                final String n = t.nextToken();
                final String[] m = n.split("=");
                if (m.length == 2) {
                    if ((m[0] != null) && (m[1] != null)) {
                        _data.put(m[0], m[1]);
                    }
                }
                else if (m.length == 3) {
                    if ((m[0] != null) && (m[1] != null) && (m[2] != null)) {
                        _data.put(m[0] + "=" + m[1], m[2]);
                    }
                }
            }
        }

        String get(final String key) {
            return _data.get(key);
        }

    }

    private final static Class<?> toClass(final String type) {
        if (type.equals("string")) {
            return String.class;
        }
        else if (type.equals("integer")) {
            return Integer.class;
        }
        else if (type.equals("long")) {
            return Long.class;
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.class;
        }
        else if (type.equals("boolean")) {
            return Boolean.class;
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

    private final Object toTypeValue(final String s, final String type) {
        if (type.equals("string")) {
            return s;
        }
        else if (type.equals("integer")) {
            return Double.valueOf(s).intValue();
        }
        else if (type.equals("long")) {
            return Double.valueOf(s).longValue();
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.valueOf(s);
        }
        else if (type.equals("boolean")) {
            return Boolean.valueOf(s);
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

}