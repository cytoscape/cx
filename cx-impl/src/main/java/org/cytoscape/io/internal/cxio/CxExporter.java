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

import org.cxio.aspects.datamodels.AbstractAttributesAspectElement;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.CyGroupsElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.HiddenAttributesElement;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NetworkRelationsElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.core.AspectElementCounts;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cxio.filters.AspectKeyFilter;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.internal.cx_writer.VisualPropertiesGatherer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
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

    private static final String  SUID                                = "SUID";
    private final static boolean DEFAULT_USE_DEFAULT_PRETTY_PRINTING = true;
    private static final boolean DEBUG                               = true;

    private VisualLexicon        _lexicon;
    private boolean              _use_default_pretty_printing;
    private VisualMappingManager _visual_mapping_manager;
    private CyNetworkViewManager _networkview_manager;
    private CyGroupManager       _group_manager;

    /**
     * This returns a new instance of CxExporter.
     *
     * @return a new CxExporter
     */
    public final static CxExporter createInstance() {
        return new CxExporter();
    }

    private CxExporter() {
        _use_default_pretty_printing = DEFAULT_USE_DEFAULT_PRETTY_PRINTING;
        _visual_mapping_manager = null;
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

    public void setGroupManager(final CyGroupManager group_manager) {
        _group_manager = group_manager;

    }

    public void setLexicon(final VisualLexicon lexicon) {
        _lexicon = lexicon;
    }

    public void setNetworkViewManager(final CyNetworkViewManager networkview_manager) {
        _networkview_manager = networkview_manager;
    }

    public void setUseDefaultPrettyPrinting(final boolean use_default_pretty_printing) {
        _use_default_pretty_printing = use_default_pretty_printing;
    }

    public void setVisualMappingManager(final VisualMappingManager visual_mapping_manager) {
        _visual_mapping_manager = visual_mapping_manager;
    }

    private final void writeEdgeAttributes(final CyNetwork network, final CxWriter w, final String namespace)
            throws IOException {

        final List<AspectElement> elements = new ArrayList<AspectElement>();

        final CySubNetwork my_subnet = (CySubNetwork) network;
        final CyRootNetwork my_root = my_subnet.getRootNetwork();
        final List<CySubNetwork> subnets = my_root.getSubNetworkList();

        for (final CySubNetwork subnet : subnets) {
            writeEdgeAttributesHelper(namespace, subnet, subnet.getEdgeList(), elements);
        }

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "edge attributes", elements.size());
        }
    }

    @SuppressWarnings("rawtypes")
    private void writeEdgeAttributesHelper(final String namespace,
                                           final CyNetwork my_network,
                                           final List<CyEdge> edges,
                                           final List<AspectElement> elements) {

        for (final CyEdge cy_edge : edges) {
            final CyRow row = my_network.getRow(cy_edge, namespace);
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
                        final String subnet = String.valueOf(my_network.getSUID());
                        if (value instanceof List) {
                            final List<String> attr_values = new ArrayList<String>();
                            for (final Object v : (List) value) {
                                attr_values.add(String.valueOf(v));
                            }
                            if (!attr_values.isEmpty()) {
                                e = new EdgeAttributesElement(subnet,
                                                              Util.makeId(cy_edge.getSUID()),
                                                              column_name,
                                                              attr_values,
                                                              AbstractAttributesAspectElement.determineDataType(value));
                            }
                        }
                        else {
                            e = new EdgeAttributesElement(subnet,
                                                          Util.makeId(cy_edge.getSUID()),
                                                          column_name,
                                                          String.valueOf(value),
                                                          AbstractAttributesAspectElement.determineDataType(value));
                        }
                        if (e != null) {
                            elements.add(e);
                        }
                    }
                }
            }
        }
    }

    private final void writeGroups(final CyNetwork network, final CxWriter w) throws IOException {
        final CySubNetwork my_subnet = (CySubNetwork) network;
        final CyRootNetwork my_root = my_subnet.getRootNetwork();

        final List<CySubNetwork> subnets = my_root.getSubNetworkList();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CySubNetwork subnet : subnets) {
            final CyRow row = subnet.getRow(subnet);
            final Set<CyGroup> groups = _group_manager.getGroupSet(subnet);
            for (final CyGroup group : groups) {
                final String name = row.get("name", String.class);
                final CyGroupsElement group_element = new CyGroupsElement(String.valueOf(group.getGroupNode().getSUID()),
                                                                          name,
                                                                          String.valueOf(subnet.getSUID()));
                for (final CyEdge e : group.getExternalEdgeList()) {
                    group_element.addExternalEdge(String.valueOf(e.getSUID()));
                }
                for (final CyEdge e : group.getInternalEdgeList()) {
                    group_element.addInternalEdge(String.valueOf(e.getSUID()));
                }
                for (final CyNode e : group.getNodeList()) {
                    group_element.addNode(String.valueOf(e.getSUID()));
                }
                elements.add(group_element);
            }

        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "groups", elements.size());
        }

    }

    private final void writeHiddenAttributes(final CyNetwork network, final CxWriter w, final String namespace)
            throws IOException {

        final List<AspectElement> elements = new ArrayList<AspectElement>();

        final CySubNetwork my_subnet = (CySubNetwork) network;
        final CyRootNetwork my_root = my_subnet.getRootNetwork();
        final List<CySubNetwork> subnets = my_root.getSubNetworkList();

        for (final CySubNetwork subnet : subnets) {
            writeHiddenAttributesHelper(namespace, subnet, elements);
        }

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "network attributes", elements.size());
        }
    }

    @SuppressWarnings("rawtypes")
    private void writeHiddenAttributesHelper(final String namespace,
                                             final CyNetwork my_network,
                                             final List<AspectElement> elements) {

        final CyRow row = my_network.getRow(my_network, namespace);
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
                    HiddenAttributesElement e = null;
                    final String subnet = String.valueOf(my_network.getSUID());
                    if (value instanceof List) {
                        final List<String> attr_values = new ArrayList<String>();
                        for (final Object v : (List) value) {
                            attr_values.add(String.valueOf(v));
                        }
                        if (!attr_values.isEmpty()) {
                            e = new HiddenAttributesElement(subnet,
                                                            column_name,
                                                            attr_values,
                                                            AbstractAttributesAspectElement.determineDataType(value));
                        }
                    }
                    else {
                        e = new HiddenAttributesElement(subnet,
                                                        column_name,
                                                        String.valueOf(value),
                                                        AbstractAttributesAspectElement.determineDataType(value));
                    }
                    if (e != null) {
                        elements.add(e);
                    }
                }
            }

        }
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
     *
     */
    public final boolean writeNetwork(final CyNetwork network,
                                      final AspectSet aspects,
                                      final FilterSet filters,
                                      final OutputStream out) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        if ((filters != null) && !filters.getFilters().isEmpty()) {
            addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(), filters.getFiltersAsMap());
        }
        else {
            addAspectFragmentWriters(w, aspects.getAspectFragmentWriters());
        }

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(network, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(network, w);
        }
        if (aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            writeNetworkAttributes(network, w, CyNetwork.DEFAULT_ATTRS);
        }
        if (aspects.contains(Aspect.HIDDEN_ATTRIBUTES)) {
            writeHiddenAttributes(network, w, CyNetwork.HIDDEN_ATTRS);
        }
        if (aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writeNodeAttributes(network, w, CyNetwork.DEFAULT_ATTRS);
        }
        if (aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            writeEdgeAttributes(network, w, CyNetwork.DEFAULT_ATTRS);
        }
        if (aspects.contains(Aspect.SUBNETWORKS)) {
            writeSubNetworks(network, w);
        }
        if (aspects.contains(Aspect.GROUPS)) {
            writeGroups(network, w);
        }
        if (aspects.contains(Aspect.NETWORK_RELATIONS)) {
            writeNetworkRelations(network, w);
        }

        w.end();

        final AspectElementCounts counts = w.getAspectElementCounts();

        if (DEBUG) {
            if (counts != null) {
                System.out.println("Aspects elements written out:");
                System.out.println(counts);
            }

        }
        return true;

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
     * @throws IOException
     *
     *
     * @see AspectSet
     * @see Aspect
     *
     */
    public final boolean writeNetwork(final CyNetwork network, final AspectSet aspects, final OutputStream out)
            throws IOException {

        return writeNetwork(network, aspects, null, out);

    }

    private final void writeNetworkAttributes(final CyNetwork network, final CxWriter w, final String namespace)
            throws IOException {

        final List<AspectElement> elements = new ArrayList<AspectElement>();

        final CySubNetwork my_subnet = (CySubNetwork) network;
        final CyRootNetwork my_root = my_subnet.getRootNetwork();
        final List<CySubNetwork> subnets = my_root.getSubNetworkList();

        for (final CySubNetwork subnet : subnets) {
            writeNetworkAttributesHelper(namespace, subnet, elements);
        }

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "network attributes", elements.size());
        }
    }

    @SuppressWarnings("rawtypes")
    private void writeNetworkAttributesHelper(final String namespace,
                                              final CyNetwork my_network,
                                              final List<AspectElement> elements) {

        final CyRow row = my_network.getRow(my_network, namespace);
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
                    NetworkAttributesElement e = null;
                    final String subnet = String.valueOf(my_network.getSUID());
                    if (value instanceof List) {
                        final List<String> attr_values = new ArrayList<String>();
                        for (final Object v : (List) value) {
                            attr_values.add(String.valueOf(v));
                        }
                        if (!attr_values.isEmpty()) {
                            e = new NetworkAttributesElement(subnet,
                                                             column_name,
                                                             attr_values,
                                                             AbstractAttributesAspectElement.determineDataType(value));
                        }
                    }
                    else {
                        e = new NetworkAttributesElement(subnet,
                                                         column_name,
                                                         String.valueOf(value),
                                                         AbstractAttributesAspectElement.determineDataType(value));
                    }
                    if (e != null) {
                        elements.add(e);
                    }
                }
            }

        }
    }

    private final void writeNodeAttributes(final CyNetwork network, final CxWriter w, final String namespace)
            throws IOException {

        final List<AspectElement> elements = new ArrayList<AspectElement>();

        final CySubNetwork my_subnet = (CySubNetwork) network;
        final CyRootNetwork my_root = my_subnet.getRootNetwork();
        final List<CySubNetwork> subnets = my_root.getSubNetworkList();

        for (final CySubNetwork subnet : subnets) {
            writeNodeAttributesHelper(namespace, subnet, subnet.getNodeList(), elements);
        }

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "node attributes", elements.size());
        }
    }

    @SuppressWarnings("rawtypes")
    private void writeNodeAttributesHelper(final String namespace,
                                           final CyNetwork my_network,
                                           final List<CyNode> nodes,
                                           final List<AspectElement> elements) {
        for (final CyNode cy_node : nodes) {

            final CyRow row = my_network.getRow(cy_node, namespace);
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
                        final String subnet = String.valueOf(my_network.getSUID());
                        if (value instanceof List) {
                            final List<String> attr_values = new ArrayList<String>();
                            for (final Object v : (List) value) {
                                attr_values.add(String.valueOf(v));
                            }
                            if (!attr_values.isEmpty()) {
                                e = new NodeAttributesElement(subnet,
                                                              Util.makeId(cy_node.getSUID()),
                                                              column_name,
                                                              attr_values,
                                                              AbstractAttributesAspectElement.determineDataType(value));
                            }
                        }
                        else {
                            e = new NodeAttributesElement(subnet,
                                                          Util.makeId(cy_node.getSUID()),
                                                          column_name,
                                                          String.valueOf(value),
                                                          AbstractAttributesAspectElement.determineDataType(value));
                        }
                        if (e != null) {
                            elements.add(e);
                        }
                    }
                }
            }
        }
    }

    private final void writeSubNetworks(final CyNetwork network, final CxWriter w) throws IOException {

        final CySubNetwork my_subnet = (CySubNetwork) network;
        final CyRootNetwork my_root = my_subnet.getRootNetwork();
        final List<CySubNetwork> subnets = my_root.getSubNetworkList();

        for (final CySubNetwork subnet : subnets) {
            final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(subnet);
            for (final CyNetworkView view : views) {
                writeView(view, null, w);
            }
        }
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CySubNetwork subnet : subnets) {
            final SubNetworkElement subnetwork_element = new SubNetworkElement(String.valueOf(subnet.getSUID()));
            for (final CyEdge edgeview : subnet.getEdgeList()) {
                subnetwork_element.addEdge(Util.makeId(String.valueOf(edgeview.getSUID())));
            }
            for (final CyNode nodeview : subnet.getNodeList()) {
                subnetwork_element.addNode(Util.makeId(String.valueOf(nodeview.getSUID())));
            }
            elements.add(subnetwork_element);
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "subnetworks", elements.size());
        }
    }

    private final void writeView(final CyNetworkView view, final AspectSet aspects, final CxWriter w)
            throws IOException {

        final boolean z_used = writeCartesianLayout(view, w);

        writeVisualProperties(view, z_used, _visual_mapping_manager, _lexicon, w);

    }

    private final static boolean writeCartesianLayout(final CyNetworkView view, final CxWriter w) throws IOException {
        final CyNetwork network = view.getModel();
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        boolean z_used = false;
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            if (Math.abs(node_view.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)) > 0.000000001) {
                z_used = true;
                break;
            }
        }

        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            if (z_used) {
                elements.add(new CartesianLayoutElement(Util.makeId(cy_node.getSUID()), String.valueOf(network
                                                                                                       .getSUID()), node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), node_view
                                                                                                       .getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION), node_view
                                                                                                       .getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)));
            }
            else {
                elements.add(new CartesianLayoutElement(Util.makeId(cy_node.getSUID()), String.valueOf(network
                                                                                                       .getSUID()), node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), node_view
                                                                                                       .getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));
            }

        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "cartesian layout", elements.size());
        }
        return z_used;
    }

    private final static void writeEdges(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final CyRootNetwork my_root = ((CySubNetwork) network).getRootNetwork();
        for (final CyEdge cyEdge : my_root.getEdgeList()) {
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

    private final static void writeNetworkRelations(final CyNetwork network, final CxWriter w) throws IOException {
        final CySubNetwork as_subnet = (CySubNetwork) network;
        final CyRootNetwork root = as_subnet.getRootNetwork();
        final List<CySubNetwork> subnetworks = root.getSubNetworkList();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final String parent = String.valueOf(root.getSUID());
        int counter = 0;
        for (final CySubNetwork subnetwork : subnetworks) {

            final CyRow row = subnetwork.getRow(subnetwork, CyNetwork.DEFAULT_ATTRS);
            String name = null;
            if (row != null) {
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    if (values.get("name") != null) {
                        try {
                            final String str = String.valueOf(values.get("name"));
                            if ((str != null) && (str.trim().length() > 0)) {
                                name = str;
                            }
                        }
                        catch (final Exception e) {
                            name = null;
                        }
                    }
                    if (name == null) {
                        if (values.get("shared name") != null) {
                            try {
                                final String str = String.valueOf(values.get("shared name"));
                                if ((str != null) && (str.trim().length() > 0)) {
                                    name = str;
                                }
                            }
                            catch (final Exception e) {
                                name = null;
                            }
                        }
                    }
                }
            }
            if ((name == null) || (name.trim().length() < 1)) {
                name = "subnetwork " + counter;
            }
            counter++;
            final NetworkRelationsElement rel = new NetworkRelationsElement(parent,
                                                                            String.valueOf(subnetwork.getSUID()),
                                                                            "subnetwork",
                                                                            name);
            elements.add(rel);
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "network relations", elements.size());
        }

    }

    private final static void writeNodes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final CyRootNetwork my_root = ((CySubNetwork) network).getRootNetwork();
        for (final CyNode cy_node : my_root.getNodeList()) {
            elements.add(new NodesElement(Util.makeId(cy_node.getSUID())));
        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "nodes", elements.size());
        }
    }

    private final static void writeVisualProperties(final CyNetworkView view,
                                                    final boolean z_used,
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
                .gatherVisualPropertiesAsAspectElements(view, z_used, network, visual_mapping_manager, lexicon, types);

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "visual properties", elements.size());
        }
    }

}
