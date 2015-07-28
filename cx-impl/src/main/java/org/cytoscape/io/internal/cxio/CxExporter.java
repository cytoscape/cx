package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.cxio.aspects.datamodels.AbstractAttributesElement;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cxio.filters.AspectKeyFilter;
import org.cytoscape.io.internal.cxio.CxOutput.Status;
import org.cytoscape.io.internal.visual_properties.VisualPropertiesWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
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

    private final static boolean DEFAULT_USE_DEFAULT_PRETTY_PRINTING = false;

    private boolean              _use_default_pretty_printing;
    private VisualMappingManager _visual_mapping_manager;
    private VisualLexicon        _lexicon;

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

    public void setVisualMappingManager(final VisualMappingManager visual_mapping_manager) {
        _visual_mapping_manager = visual_mapping_manager;
    }

    public void setUseDefaultPrettyPrinting(final boolean use_default_pretty_printing) {
        _use_default_pretty_printing = use_default_pretty_printing;
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
                                  final OutputStream out) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(), filters.getFiltersAsMap());

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(network, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(network, w);
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
    public final CxOutput writeCX(final CyNetwork network, final AspectSet aspects, final OutputStream out)
            throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);
        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters());

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(network, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(network, w);
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
                                  final OutputStream out) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);

        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters(), filters.getFiltersAsMap());

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(view, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(view, w);
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
    public final CxOutput writeCX(final CyNetworkView view, final AspectSet aspects, final OutputStream out)
            throws IOException {
        final CxWriter w = CxWriter.createInstance(out, _use_default_pretty_printing);
        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters());

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(view, w);
        }
        if (aspects.contains(Aspect.EDGES)) {
            writeEdges(view, w);
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

    @SuppressWarnings("unchecked")
    private final static void addAttributes(final Map<String, Object> values, final AbstractAttributesElement element) {
        for (final String column_name : values.keySet()) {
            final Object value = values.get(column_name);
            if (value == null) {
                continue;
            }
            if (value instanceof List) {
                final List<Object> list = ((List<Object>) value);
                for (final Object o : list) {
                    element.putValue(column_name, o);
                }
            }
            else {
                element.putValue(column_name, value);
            }
        }
    }

    private final static String makeEdgeAttributeId(final long edge_suid) {
        return "_ea" + edge_suid;
    }

    private final static String makeNodeAttributeId(final long node_suid) {
        return "_na" + node_suid;
    }

    private final static void writeCartesianLayout(final CyNetworkView view, final CxWriter w) throws IOException {
        final CyNetwork network = view.getModel();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            elements.add(new CartesianLayoutElement(cy_node.getSUID(), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));

        }
        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "cartesian layout", elements.size());
        }
    }

    private final static void writeVisualProperties(final CyNetworkView view,
                                                    final VisualMappingManager visual_mapping_manager,
                                                    final VisualLexicon lexicon,
                                                    final CxWriter w) throws IOException {
        final CyNetwork network = view.getModel();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        VisualPropertiesWriter.gatherVisualProperties(view, network, visual_mapping_manager, lexicon, elements);

        final long t0 = System.currentTimeMillis();
        w.writeAspectElements(elements);
        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "visual properties", elements.size());
        }
    }

    private final static void writeEdgeAttributes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        for (final CyEdge cy_edge : network.getEdgeList()) {
            final CyRow row = network.getRow(cy_edge);
            if (row != null) {
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    final EdgeAttributesElement eae = new EdgeAttributesElement(makeEdgeAttributeId(cy_edge.getSUID()));
                    eae.addEdge(cy_edge.getSUID());

                    addAttributes(values, eae);
                    elements.add(eae);
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
        // final long t0 = System.currentTimeMillis();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyEdge cyEdge : network.getEdgeList()) {
            elements.add(new EdgesElement(cyEdge.getSUID(), cyEdge.getSource().getSUID(), cyEdge.getTarget().getSUID()));
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

    private final static void writeNodeAttributes(final CyNetwork network, final CxWriter w) throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        for (final CyNode cy_node : network.getNodeList()) {

            final CyRow row = network.getRow(cy_node);
            if (row != null) {
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    final NodeAttributesElement nae = new NodeAttributesElement(makeNodeAttributeId(cy_node.getSUID()));
                    nae.addNode(cy_node.getSUID());
                    addAttributes(values, nae);
                    elements.add(nae);
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
        // final long t0 = System.currentTimeMillis();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            elements.add(new NodesElement(cy_node.getSUID()));
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

}
