package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.cxio.aspects.datamodels.AbstractAttributesElement;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NetworkRelationsElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cxio.filters.AspectKeyFilter;
import org.cytoscape.io.internal.cx_writer.VisualPropertiesGatherer;
import org.cytoscape.io.internal.cxio.CxOutput.Status;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * This class is for serializing Cytoscape networks, views, and attribute tables
 * as CX formatted output streams. <br>
 * <br>
 * In particular, it provides the following methods for writing CX: <br>
 * <ul>
 * <li>
 * {@link #writeCX(CyNetwork, AspectSet, OutputStream)}</li>
 * <li>
 * {@link #writeCX(CyNetworkView, AspectSet, OutputStream)}</li>
 * <li>
 * {@link #writeNetwork(CyNetwork, AspectSet, FilterSet, OutputStream)}</li>
 * <li>
 * {@link #writeNetworkView(CyNetworkView, AspectSet, FilterSet, OutputStream)}</li>
 * </ul>
 * <br>
 * <br>
 * These methods use: <br>
 * <ul>
 * <li>
 * {@link AspectSet} to control which aspects to serialize</li>
 * <li>
 * {@link FilterSet} to control which aspect keys/fields to include within an
 * aspect</li>
 * </ul>
 * <br>
 *
 * @see AspectSet
 * @see Aspect
 * @see FilterSet
 * @see CxOutput
 * @see CxImporter
 *
 *
 */
public final class CxExporter {

    private static final String  SELECTED                            = "selected";
    private static final String  SUID                                = "SUID";
    private final static boolean DEFAULT_USE_DEFAULT_PRETTY_PRINTING = false;

    private VisualLexicon        _lexicon;
    private boolean              _use_default_pretty_printing;
    private VisualMappingManager _visual_mapping_manager;
    private CyNetworkViewManager _networkview_manager;
    private CyNetworkManager     _network_manager;

    private CxExporter() {
        _use_default_pretty_printing = DEFAULT_USE_DEFAULT_PRETTY_PRINTING;
        _visual_mapping_manager = null;
    }

    /**
     * This returns a new instance of CxExporter.
     *
     * @return a new CxExporter
     */
    public final static CxExporter createInstance() {
        return new CxExporter();
    }

    public void setLexicon(final VisualLexicon lexicon) {
        _lexicon = lexicon;
    }

    public void setUseDefaultPrettyPrinting(final boolean use_default_pretty_printing) {
        _use_default_pretty_printing = use_default_pretty_printing;
    }

    public void setVisualMappingManager(final VisualMappingManager visual_mapping_manager) {
        _visual_mapping_manager = visual_mapping_manager;
    }

    public void setNetworkViewManager(final CyNetworkViewManager networkview_manager) {
        _networkview_manager = networkview_manager;
    }

    /**
     * This is a method for serializing a Cytoscape network and associated table
     * data as CX formatted OutputStream. <br>
     * Method arguments control which aspects to serialize, and for data stored
     * in node and tables (serialized as node attributes and edge attributes
     * aspects), which table columns to include or exclude.
     *
     *
     * @param network
     *            the CyNetwork, and by association, tables to be serialized
     * @param aspects
     *            the set of aspects to serialize
     * @param filters
     *            the set of filters controlling which node and edge table
     *            columns to include or exclude
     * @param out
     *            the stream to write to
     * @return a CxOutput object which contains the output stream as well as a
     *         status
     * @throws IOException
     *
     *
     * @see AspectSet
     * @see Aspect
     * @see FilterSet
     * @see CxOutput
     *
     */
    public final CxOutput writeNetwork(final CyNetwork network,
                                       final AspectSet aspects,
                                       final FilterSet filters,
                                       final OutputStream out,
                                       final String time_stamp) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        if ((filters != null) && !filters.getFilters().isEmpty()) {
            addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp), filters.getFiltersAsMap());
        }
        else {
            addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp));
        }

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(network, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(network, w);
        }
        // if (aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
        // writeCartesianLayout(network, w);
        // }
        if (aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            writeNetworkAttributes(network, w);
        }
        if (aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writeNodeAttributes(network, w);
        }
        if (aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            writeEdgeAttributes(network, w);
        }
        // if (aspects.contains(Aspect.VISUAL_PROPERTIES)) {
        // writeVisualProperties(network, _visual_mapping_manager, _lexicon, w);
        // }
        if (aspects.contains(Aspect.GROUPS)) {
            writeGroups(network, w);
        }
        if (aspects.contains(Aspect.SUBNETWORKS)) {
            writeSubNetworks(network, w);
        }
        if (aspects.contains(Aspect.NETWORK_RELATIONS)) {
            writeNetworkRelations(network, w);
        }

        w.end();

        return new CxOutput(out, Status.OK);

    }

    /**
     * This is a method for serializing a Cytoscape network and associated table
     * data as CX formatted OutputStream. <br>
     * Method arguments control which aspects to serialize.
     *
     *
     * @param network
     *            the CyNetwork, and by association, tables to be serialized
     * @param aspects
     *            the set of aspects to serialize
     * @param out
     *            the stream to write to
     * @return a CxOutput object which contains the output stream as well as a
     *         status
     * @throws IOException
     *
     *
     * @see AspectSet
     * @see Aspect
     * @see CxOutput
     *
     */
    public final CxOutput writeNetwork(final CyNetwork network,
                                       final AspectSet aspects,
                                       final OutputStream out,
                                       final String time_stamp) throws IOException {

        return writeNetwork(network, aspects, null, out, time_stamp);

    }

    /**
     * This is a method for serializing a Cytoscape network view and associated
     * table data as CX formatted OutputStream. <br>
     * Method arguments control which aspects to serialize, and for data stored
     * in node and tables (serialized as node attributes and edge attributes
     * aspects), which table columns to include or exclude.
     *
     *
     * @param view
     *            the CyNetworkView, and by association, tables to be serialized
     * @param aspects
     *            the set of aspects to serialize
     * @param filters
     *            the set of filters controlling which node and edge table
     *            columns to include or exclude
     * @param out
     *            the stream to write to
     * @return a CxOutput object which contains the output stream as well as a
     *         status
     * @throws IOException
     *
     *
     * @see AspectSet
     * @see Aspect
     * @see FilterSet
     * @see CxOutput
     *
     */
    public final CxOutput writeNetworkView(final CyNetworkView view,
                                           final AspectSet aspects,
                                           final FilterSet filters,
                                           final OutputStream out,
                                           final String time_stamp) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        if ((filters != null) && !filters.getFilters().isEmpty()) {
            addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp), filters.getFiltersAsMap());
        }
        else {
            addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp));
        }

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(view, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(view, w);
        }
        if (aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
            writeCartesianLayout(view, w);
        }
        if (aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            writeNetworkAttributes(view, w);
        }
        if (aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writeNodeAttributes(view, w);
        }
        if (aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            writeEdgeAttributes(view, w);
        }
        if (aspects.contains(Aspect.VISUAL_PROPERTIES)) {
            writeVisualProperties(view, _visual_mapping_manager, _lexicon, w);
        }

        w.end();

        return new CxOutput(out, Status.OK);

    }

    /**
     * This is a method for serializing a Cytoscape network view and associated
     * table data as CX formatted OutputStream. <br>
     * Method arguments control which aspects to serialize.
     *
     *
     * @param view
     *            the CyNetworkView, and by association, tables to be serialized
     * @param aspects
     *            the set of aspects to serialize
     * @param out
     *            the stream to write to
     * @return a CxOutput object which contains the output stream as well as a
     *         status
     * @throws IOException
     *
     *
     * @see AspectSet
     * @see Aspect
     * @see FilterSet
     * @see CxOutput
     *
     */
    public final CxOutput writeNetworkView(final CyNetworkView view,
                                           final AspectSet aspects,
                                           final OutputStream out,
                                           final String time_stamp) throws IOException {
        return writeNetworkView(view, aspects, null, out, time_stamp);

    }

    private final void writeView(final CyNetworkView view, final AspectSet aspects, final CxWriter w)
            throws IOException {

        // if (aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
        writeCartesianLayout(view, w);
        // }
        // if (aspects.contains(Aspect.VISUAL_PROPERTIES)) {
        writeVisualProperties(view, _visual_mapping_manager, _lexicon, w);
        // }

    }

    private final static void writeCartesianLayout(final CyNetworkView view, final CxWriter w) throws IOException {
        final CyNetwork network = view.getModel();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            elements.add(new CartesianLayoutElement(Util.makeId(cy_node.getSUID()),
                                                    String.valueOf(network.getSUID()),
                                                    node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
                                                    node_view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION),
                                                    node_view.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)));

        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "cartesian layout", elements.size());
        }
    }

    @SuppressWarnings("rawtypes")
    private final static void writeEdgeAttributes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        for (final CyEdge cy_edge : network.getEdgeList()) {
            final CyRow row = network.getRow(cy_edge);
            if (row != null) {

                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    for (final String column_name : values.keySet()) {
                        if (column_name.equals(SUID)) {
                            continue;
                        }
                        final Object value = values.get(column_name);
                        if (value == null) {
                            continue;
                        }
                        EdgeAttributesElement e = null;
                        if (value instanceof List) {
                            final List<String> attr_values = new ArrayList<String>();
                            for (final Object v : (List) value) {
                                attr_values.add(String.valueOf(v));
                            }
                            if (!attr_values.isEmpty()) {
                                e = new EdgeAttributesElement(Util.makeId(cy_edge.getSUID()),
                                                              column_name,
                                                              attr_values,
                                                              AbstractAttributesElement.determineType(((List) value)
                                                                      .get(0)));
                            }
                        }
                        else {
                            e = new EdgeAttributesElement(Util.makeId(cy_edge.getSUID()),
                                                          column_name,
                                                          String.valueOf(value),
                                                          AbstractAttributesElement.determineType(value));
                        }
                        if (e != null) {
                            elements.add(e);
                        }
                    }
                }
            }
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "edge attributes", elements.size());
        }
    }

    private final static void writeEdgeAttributes(final CyNetworkView view, final CxWriter w) throws IOException {
        writeEdgeAttributes(view.getModel(), w);
    }

    private final static void writeEdges(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyEdge cyEdge : network.getEdgeList()) {
            elements.add(new EdgesElement(Util.makeId(cyEdge.getSUID()),
                                          Util.makeId(cyEdge.getSource().getSUID()),
                                          Util.makeId(cyEdge.getTarget().getSUID())));
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "edges", elements.size());
        }
    }

    private final static void writeEdges(final CyNetworkView view, final CxWriter w) throws IOException {
        writeEdges(view.getModel(), w);
    }

    private final static void writeNetworkAttributes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final CyRow row = network.getRow(network);
        final Map<String, Object> values = row.getAllValues();
        if ((values != null) && !values.isEmpty()) {
            for (final String column_name : values.keySet()) {
                if (column_name.equals(SUID) || column_name.equals(SELECTED)) {
                    continue;
                }
                final Object value = values.get(column_name);
                if (value == null) {
                    continue;
                }
                NetworkAttributesElement e = null;
                if (value instanceof List) {
                    final List<String> attr_values = new ArrayList<String>();
                    for (final Object v : (List) value) {
                        attr_values.add(String.valueOf(v));
                    }
                    if (!attr_values.isEmpty()) {
                        e = new NetworkAttributesElement(Util.makeId(network.getSUID()),
                                                         column_name,
                                                         attr_values,
                                                         AbstractAttributesElement.determineType(((List) value).get(0)));
                    }
                }
                else {
                    e = new NetworkAttributesElement(Util.makeId(network.getSUID()),
                                                     column_name,
                                                     String.valueOf(value),
                                                     AbstractAttributesElement.determineType(value));
                }
                if (e != null) {
                    elements.add(e);
                }
            }
        }

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "network attributes", elements.size());
        }
    }

    private final static void writeNetworkAttributes(final CyNetworkView view, final CxWriter w) throws IOException {
        writeNetworkAttributes(view.getModel(), w);
    }

    private final static void writeNetworkRelations(final CyNetwork network, final CxWriter w) throws IOException {
        final CySubNetwork as_subnet = (CySubNetwork) network;
        final CyRootNetwork root = as_subnet.getRootNetwork();
        final List<CySubNetwork> subnetworks = root.getSubNetworkList();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final String parent = String.valueOf(root.getSUID());
        for (final CySubNetwork subnetwork : subnetworks) {
            final NetworkRelationsElement rel = new NetworkRelationsElement(parent,
                                                                            String.valueOf(subnetwork.getSUID()),
                    "subnetwork");
            elements.add(rel);
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "network relations", elements.size());
        }

    }

    private final void writeGroups(final CyNetwork network, final CxWriter w) throws IOException {
        if (_networkview_manager == null) {
            System.out.println("networkview manager is null");
            return;
        }
        final Set<CyNetwork> networks = _network_manager.getNetworkSet();
        for (final CyNetwork n : networks) {
            final CySubNetwork subnet = (CySubNetwork) n;

            final CyRootNetwork root = subnet.getRootNetwork();
            final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(n);
            for (final CyNetworkView view : views) {
                final CyNetwork my_network = view.getModel();

                writeView(view, null, w);
            }
        }

    }

    private final void writeSubNetworks(final CyNetwork network, final CxWriter w) throws IOException {
        if (_networkview_manager == null) {
            System.out.println("networkview manager is null");
            return;
        }
        final Set<CyNetwork> networks = _network_manager.getNetworkSet();

        for (final CyNetwork n : networks) {
            final CySubNetwork subnet = (CySubNetwork) n;
            final CyRootNetwork root = subnet.getRootNetwork();
            System.out.println("__ root              = " + root.getSUID());
            System.out.println("__         nodecount = " + root.getNodeCount());
            System.out.println("__ network.getSUID() = " + n.getSUID());
            System.out.println("__         nodecount = " + n.getNodeCount());
            final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(n);
            for (final CyNetworkView view : views) {
                final CyNetwork my_network = view.getModel();
                writeView(view, null, w);
            }
        }

        final List<AspectElement> elements = new ArrayList<AspectElement>();

        // final Collection<CyNetworkView> views =
        // _networkview_manager.getNetworkViews(network);
        final Collection<CyNetworkView> views = _networkview_manager.getNetworkViewSet();

        System.out.println("network.getSUID() = " + network.getSUID());
        System.out.println("        nodecount = " + network.getNodeCount());

        for (final CyNetworkView view : views) {

            final CyNetwork my_network = view.getModel();

            System.out.println("  view.getModel().getSUID() = " + my_network.getSUID());
            System.out.println("                  nodecount = " + my_network.getNodeCount());

            final CyRow row = my_network.getRow(my_network);
            final String name = row.get("name", String.class);
            final SubNetworkElement subnetwork = new SubNetworkElement(String.valueOf(my_network.getSUID()), name);

            System.out.println("subnet " + name + "-------------------------------------------------");
            final Collection<View<CyEdge>> edgeviews = view.getEdgeViews();
            System.out.println("e: " + edgeviews.size());

            for (final View<CyEdge> edgeview : edgeviews) {
                subnetwork.addEdge(String.valueOf(edgeview.getModel().getSUID()));
            }
            final Collection<View<CyNode>> nodeviews = view.getNodeViews();
            System.out.println("n: " + nodeviews.size());
            for (final View<CyNode> nodeview : nodeviews) {
                subnetwork.addNode(String.valueOf(nodeview.getModel().getSUID()));
            }
            elements.add(subnetwork);
        }

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "subnetworks", elements.size());
        }
    }

    // private final void writeSubNetworks(final CyNetworkView networkview,
    // final CxWriter w) throws IOException {
    // // Collection<CyNetworkView> views =
    // // _networkview_manager.getNetworkViews(network);
    //
    // final List<AspectElement> elements = new ArrayList<AspectElement>();
    //
    // final Collection<CyNetworkView> views =
    // _networkview_manager.getNetworkViewSet();
    //
    // for (final CyNetworkView view : views) {
    //
    // final CyNetwork network = view.getModel();
    //
    // System.out.println( "view.getModel().getSUID() = " + network.getSUID() );
    // System.out.println( "                nodecount = " +
    // network.getNodeCount() );
    //
    // final CyRow row = network.getRow(network);
    //
    // final String name = row.get("name", String.class);
    // final SubNetworkElement subnetwork = new
    // SubNetworkElement(String.valueOf(view.getSUID()), name);
    //
    // System.out.println("subnet " + name +
    // "-------------------------------------------------");
    // final Collection<View<CyEdge>> edgeviews = view.getEdgeViews();
    // System.out.println("e: " + edgeviews.size());
    //
    // for (final View<CyEdge> edgeview : edgeviews) {
    // subnetwork.addEdge(String.valueOf(edgeview.getSUID()));
    // }
    // final Collection<View<CyNode>> nodeviews = view.getNodeViews();
    // System.out.println("n: " + nodeviews.size());
    // for (final View<CyNode> nodeview : nodeviews) {
    // subnetwork.addNode(String.valueOf(nodeview.getSUID()));
    // }
    // elements.add(subnetwork);
    // }
    //
    // final long t0 = System.currentTimeMillis();
    // w.writeAspectElements(elements);
    // if (TimingUtil.TIMING) {
    // TimingUtil.reportTimeDifference(t0, "subnetworks", elements.size());
    // }
    // }

    @SuppressWarnings("rawtypes")
    private final static void writeNodeAttributes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        for (final CyNode cy_node : network.getNodeList()) {

            final CyRow row = network.getRow(cy_node);
            if (row != null) {
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    for (final String column_name : values.keySet()) {
                        if (column_name.equals(SUID)) {
                            continue;
                        }
                        final Object value = values.get(column_name);
                        if (value == null) {
                            continue;
                        }
                        NodeAttributesElement e = null;
                        if (value instanceof List) {
                            final List<String> attr_values = new ArrayList<String>();
                            for (final Object v : (List) value) {
                                attr_values.add(String.valueOf(v));
                            }
                            if (!attr_values.isEmpty()) {
                                e = new NodeAttributesElement(Util.makeId(cy_node.getSUID()),
                                                              column_name,
                                                              attr_values,
                                                              AbstractAttributesElement.determineType(((List) value)
                                                                      .get(0)));
                            }
                        }
                        else {
                            e = new NodeAttributesElement(Util.makeId(cy_node.getSUID()),
                                                          column_name,
                                                          String.valueOf(value),
                                                          AbstractAttributesElement.determineType(value));
                        }
                        if (e != null) {
                            elements.add(e);
                        }
                    }
                }
            }
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "node attributes", elements.size());
        }
    }

    private final static void writeNodeAttributes(final CyNetworkView view, final CxWriter w) throws IOException {
        writeNodeAttributes(view.getModel(), w);
    }

    private final static void writeNodes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            elements.add(new NodesElement(Util.makeId(cy_node.getSUID())));
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "nodes", elements.size());
        }
    }

    private final static void writeNodes(final CyNetworkView view, final CxWriter w) throws IOException {
        writeNodes(view.getModel(), w);
    }

    private final static void writeVisualProperties(final CyNetworkView view,
                                                    final VisualMappingManager visual_mapping_manager,
                                                    final VisualLexicon lexicon,
                                                    final CxWriter w) throws IOException {
        final CyNetwork network = view.getModel();
        final Set<VisualPropertyType> types = new HashSet<VisualPropertyType>();
        types.add(VisualPropertyType.NETWORK);
        types.add(VisualPropertyType.NODES);
        types.add(VisualPropertyType.EDGES);
        types.add(VisualPropertyType.NODES_DEFAULT);
        types.add(VisualPropertyType.EDGES_DEFAULT);

        final List<AspectElement> elements = VisualPropertiesGatherer
                .gatherVisualPropertiesAsAspectElements(view, network, visual_mapping_manager, lexicon, types);

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "visual properties", elements.size());
        }
    }

    private final void addAspectFragmentWriters(final CxWriter w, final Set<AspectFragmentWriter> writers) {
        for (final AspectFragmentWriter writer : writers) {
            w.addAspectFragmentWriter(writer);
        }
    }

    private final void addAspectFragmentWriters(final CxWriter w,
                                                final Set<AspectFragmentWriter> writers,
                                                final SortedMap<String, AspectKeyFilter> filters) {
        for (final AspectFragmentWriter writer : writers) {
            if (filters != null) {
                final String aspect = writer.getAspectName();
                if (filters.containsKey(aspect)) {
                    writer.addAspectKeyFilter(filters.get(aspect));
                }
            }
            w.addAspectFragmentWriter(writer);
        }
    }

    public void setNetworkManager(final CyNetworkManager network_manager) {
        _network_manager = network_manager;

    }

}
