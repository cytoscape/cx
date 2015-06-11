package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.internal.cxio.kit.AspectElement;
import org.cytoscape.io.internal.cxio.kit.AspectFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.CartesianLayoutElement;
import org.cytoscape.io.internal.cxio.kit.CxWriter;
import org.cytoscape.io.internal.cxio.kit.EdgesElement;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesElement;
import org.cytoscape.io.internal.cxio.kit.NodesElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CyToCxImpl implements CyToCx {

    private final static boolean            USE_DEFAULT_PRETTY_PRINTER = true;

    private final Set<AspectFragmentWriter> aspect_fragment_writers;

    public CyToCxImpl() {
        aspect_fragment_writers = new HashSet<AspectFragmentWriter>();
    }

    public final void addAspectFragmentWriter(final AspectFragmentWriter aspect_fragment_writer) {
        aspect_fragment_writers.add(aspect_fragment_writer);
    }

    @Override
    public void serializeCyNetwork(final CyNetwork network, final OutputStream out)
            throws IOException {

        final CxWriter w = CxWriter.createInstance(out, USE_DEFAULT_PRETTY_PRINTER);

        addWriters(w);

        w.start();
        final List<AspectElement> naes = new ArrayList<AspectElement>();
        List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            elements.add(new NodesElement(cy_node.getSUID()));
            //
            final CyRow row = network.getRow(cy_node);
            if (row != null) {
                // final CyTable table = row.getTable();
                final Map<String, Object> values = row.getAllValues();
                if ((values != null) && !values.isEmpty()) {

                    final NodeAttributesElement nae = new NodeAttributesElement("na"
                            + cy_node.getSUID());
                    nae.addNode(cy_node.getSUID());
                    for (final String columnName : values.keySet()) {

                        final Object value = values.get(columnName);
                        if (value == null) {
                            continue;
                        }
                        nae.addAttribute(columnName, value.toString());

                    }
                    naes.add(nae);
                }
            }

        }
        w.write(elements);

        elements = new ArrayList<AspectElement>();
        for (final CyEdge cyEdge : network.getEdgeList()) {
            elements.add(new EdgesElement(cyEdge.getSUID(), cyEdge.getSource().getSUID(), cyEdge
                                          .getTarget().getSUID()));
        }
        w.write(elements);
        w.write(naes);

        w.end();
    }

    private final void addWriters(final CxWriter w) {
        for (final AspectFragmentWriter aspect_fragment_writer : aspect_fragment_writers) {
            w.addAspectFragmentWriter(aspect_fragment_writer);
        }
    }

    public void serializeCyNetworkView(final CyNetwork network,
                                       final CyNetworkView view,
                                       final OutputStream out) throws IOException {

        final CxWriter w = CxWriter.createInstance(out, USE_DEFAULT_PRETTY_PRINTER);

        addWriters(w);

        w.start();

        final List<AspectElement> node_elements = new ArrayList<AspectElement>();
        final List<AspectElement> cartesian_layout_elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            node_elements.add(new NodesElement(String.valueOf(cy_node.getSUID())));
            final View<CyNode> node_view = view.getNodeView(cy_node);
            cartesian_layout_elements.add(new CartesianLayoutElement(cy_node.getSUID(), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION), node_view
                    .getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));

        }
        w.write(node_elements);

        final List<AspectElement> edge_elements = new ArrayList<AspectElement>();
        for (final CyEdge cyEdge : network.getEdgeList()) {
            edge_elements.add(new EdgesElement(String.valueOf(cyEdge.getSUID()), String
                    .valueOf(cyEdge.getSource().getSUID()), String.valueOf(cyEdge.getTarget()
                    .getSUID())));
        }
        w.write(edge_elements);

        w.write(cartesian_layout_elements);

        w.end();
    }

}
