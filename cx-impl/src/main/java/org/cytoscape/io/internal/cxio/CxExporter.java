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
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cxio.filters.AspectKeyFilter;
import org.cytoscape.io.internal.cx_writer.VisualPropertiesGatherer;
import org.cytoscape.io.internal.cxio.CxOutput.Status;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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
 * {@link #writeCX(CyNetwork, AspectSet, FilterSet, OutputStream)}</li>
 * <li>
 * {@link #writeCX(CyNetworkView, AspectSet, FilterSet, OutputStream)}</li>
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

    /**
     * This returns a new instance of CxExporter.
     *
     * @return a new CxExporter
     */
    public final static CxExporter createInstance() {
        return new CxExporter();
    }

    private final static void writeCartesianLayout(final CyNetworkView view, final CxWriter w) throws IOException {
        final CyNetwork network = view.getModel();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            elements.add(new CartesianLayoutElement(Util.makeId(cy_node.getSUID()), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)));

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

    
    private final  void writeSubNetworks(final CyNetwork network, final CxWriter w) throws IOException {
       Collection<CyNetworkView> views =  _networkview_manager.getNetworkViews(network);
       for (CyNetworkView view : views) {
           Collection<View<CyEdge>> edgeviews = view.getEdgeViews();
           for (View<CyEdge> edgeview : edgeviews) {
               System.out.println("e=" +  edgeview.getModel().getSUID() ); 
           }
           Collection<View<CyNode>> nodeviews = view.getNodeViews();
           for (View<CyNode> nodeview : nodeviews) {
               System.out.println("n=" +  nodeview.getModel().getSUID() ); 
           }
       } 
    }
    
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

    private VisualLexicon        _lexicon;
    private boolean              _use_default_pretty_printing;
    private VisualMappingManager _visual_mapping_manager;
    private CyNetworkViewManager _networkview_manager;

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

    public void setLexicon(final VisualLexicon lexicon) {
        _lexicon = lexicon;
    }

    public void setUseDefaultPrettyPrinting(final boolean use_default_pretty_printing) {
        _use_default_pretty_printing = use_default_pretty_printing;
    }

    public void setVisualMappingManager(final VisualMappingManager visual_mapping_manager) {
        _visual_mapping_manager = visual_mapping_manager;
    }
    
    public void setNetworkViewManager(CyNetworkViewManager networkview_manager) {
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
    public final CxOutput writeCX(final CyNetwork network,
                                  final AspectSet aspects,
                                  final FilterSet filters,
                                  final OutputStream out,
                                  final String time_stamp) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp), filters.getFiltersAsMap());

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(network, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(network, w);
        }
        if (aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            writeNetworkAttributes(network, w);
        }
        if (aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writeNodeAttributes(network, w);
        }
        if (aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            writeEdgeAttributes(network, w);
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
    public final CxOutput writeCX(final CyNetwork network,
                                  final AspectSet aspects,
                                  final OutputStream out,
                                  final String time_stamp) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);
        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp));

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(network, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(network, w);
        }
        if (aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            writeNetworkAttributes(network, w);
        }
        if (aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writeNodeAttributes(network, w);
        }
        if (aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            writeEdgeAttributes(network, w);
        }

        w.end();

        return new CxOutput(out, Status.OK);

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
    public final CxOutput writeCX(final CyNetworkView view,
                                  final AspectSet aspects,
                                  final FilterSet filters,
                                  final OutputStream out,
                                  final String time_stamp) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp), filters.getFiltersAsMap());

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
    public final CxOutput writeCX(final CyNetworkView view,
                                  final AspectSet aspects,
                                  final OutputStream out,
                                  final String time_stamp) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);
        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(time_stamp));

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
       // if (aspects.contains(Aspect.VISUAL_PROPERTIES)) { //TODO
            writeSubNetworks(view.getModel(), w);
      //  }

        w.end();

        return new CxOutput(out, Status.OK);

    }

    

}
