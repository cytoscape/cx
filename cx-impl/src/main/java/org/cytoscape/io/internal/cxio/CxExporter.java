package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cxio.aspects.datamodels.AbstractAttributesElement;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cytoscape.io.internal.cxio.CxOutput.Status;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CxExporter {
    private final static boolean USE_DEFAULT_PRETTY_PRINTER = true;

    private CxExporter() {
    }

    public final static CxExporter createInstance() {
        return new CxExporter();
    }

    public final CxOutput writeCX(final CyNetwork network,
                                  final AspectSet aspects,
                                  final OutputStream out) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, USE_DEFAULT_PRETTY_PRINTER);
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

    public final CxOutput writeCX(final CyNetworkView view,
                                  final AspectSet aspects,
                                  final OutputStream out) throws IOException {
        final CxWriter w = CxWriter.createInstance(out, USE_DEFAULT_PRETTY_PRINTER);
        addAspectFragmentWriters(w, aspects.getAspectFragmentWriters());

        w.start();

        if (aspects.contains(Aspect.NODES)) {
            writeNodes(view, w);
        }
        if (aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
            writeCartesianLayout(view, w);
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

        w.end();

        return new CxOutput(out, Status.OK);

    }

    private final static void writeNodeAttributes(final CyNetwork network, final CxWriter w)
            throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        for (final CyNode cy_node : network.getNodeList()) {

            final CyRow row = network.getRow(cy_node);
            if (row != null) {
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    final NodeAttributesElement nae = new NodeAttributesElement(
                                                                                makeNodeAttributeId(cy_node.getSUID()));
                    nae.addNode(cy_node.getSUID());
                    addAttributes(values, nae);
                    elements.add(nae);
                }
            }
        }

        w.writeAspectElements(elements);
    }

    private final static void writeNodeAttributes(final CyNetworkView view, final CxWriter w)
            throws IOException {
        writeNodeAttributes(view.getModel(), w);
    }

    private final static void writeEdges(final CyNetwork network, final CxWriter w)
            throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyEdge cyEdge : network.getEdgeList()) {
            elements.add(new EdgesElement(cyEdge.getSUID(), cyEdge.getSource().getSUID(), cyEdge
                                          .getTarget().getSUID()));
        }
        w.writeAspectElements(elements);
    }

    private final static void writeEdges(final CyNetworkView view, final CxWriter w)
            throws IOException {
        writeEdges(view.getModel(), w);
    }

    private final static void writeEdgeAttributes(final CyNetwork network, final CxWriter w)
            throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();

        for (final CyEdge cy_edge : network.getEdgeList()) {
            final CyRow row = network.getRow(cy_edge);
            if (row != null) {
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {
                    final EdgeAttributesElement eae = new EdgeAttributesElement(
                                                                                makeEdgeAttributeId(cy_edge.getSUID()));
                    eae.addEdge(cy_edge.getSUID());

                    addAttributes(values, eae);
                    elements.add(eae);
                }
            }
        }
        w.writeAspectElements(elements);
    }

    private final static void writeEdgeAttributes(final CyNetworkView view, final CxWriter w)
            throws IOException {
        writeEdgeAttributes(view.getModel(), w);
    }

    private final static void writeCartesianLayout(final CyNetworkView view, final CxWriter w)
            throws IOException {
        final CyNetwork network = view.getModel();
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            elements.add(new CartesianLayoutElement(cy_node.getSUID(), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));

        }

        w.writeAspectElements(elements);
    }

    private final static void writeNodes(final CyNetwork network, final CxWriter w)
            throws IOException {
        final List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            elements.add(new NodesElement(cy_node.getSUID()));
        }
        w.writeAspectElements(elements);
    }

    private final static void writeNodes(final CyNetworkView view, final CxWriter w)
            throws IOException {
        writeNodes(view.getModel(), w);
    }

    private void addAspectFragmentWriters(final CxWriter w, final Set<AspectFragmentWriter> writers) {
        for (final AspectFragmentWriter writer : writers) {
            w.addAspectFragmentWriter(writer);
        }
    }

    @SuppressWarnings("unchecked")
    private final static void addAttributes(final Map<String, Object> values,
                                            final AbstractAttributesElement element) {
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
            element.putValue(column_name, value);
        }
    }

    private final static String makeNodeAttributeId(final long node_suid) {
        return "_na" + node_suid;
    }

    private final static String makeEdgeAttributeId(final long edge_suid) {
        return "_ea" + edge_suid;
    }

}
