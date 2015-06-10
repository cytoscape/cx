package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class CyToCxImpl implements CyToCx {

    private final static boolean USE_DEFAULT_PRETTY_PRINTER = true;

    @Override
    public void serializeCyNetwork(final CyNetwork network, final OutputStream out)
            throws IOException {

        final CxWriter w = CxWriter.createInstance(out, USE_DEFAULT_PRETTY_PRINTER);
        w.addAspectFragmentWriter(EdgesFragmentWriter.createInstance());
        w.addAspectFragmentWriter(NodesFragmentWriter.createInstance());

        w.start();

        List<AspectElement> elements = new ArrayList<AspectElement>();
        for (final CyNode cyNode : network.getNodeList()) {
            elements.add(new NodesElement(String.valueOf(cyNode.getSUID())));
        }
        w.write(elements);

        elements = new ArrayList<AspectElement>();
        for (final CyEdge cyEdge : network.getEdgeList()) {
            elements.add(new EdgesElement(String.valueOf(cyEdge.getSUID()), String.valueOf(cyEdge
                    .getSource().getSUID()), String.valueOf(cyEdge.getTarget().getSUID())));
        }
        w.write(elements);

        w.end();
    }

    public void serializeCyNetworkView(final CyNetwork network, final CyNetworkView view,
            final OutputStream out) throws IOException {

        final CxWriter w = CxWriter.createInstance(out, USE_DEFAULT_PRETTY_PRINTER);
        w.addAspectFragmentWriter(EdgesFragmentWriter.createInstance());
        w.addAspectFragmentWriter(NodesFragmentWriter.createInstance());
        w.addAspectFragmentWriter(CartesianLayoutFragmentWriter.createInstance());

        w.start();

        final List<AspectElement> node_elements = new ArrayList<AspectElement>();
        final List<AspectElement> cartesian_layout_elements = new ArrayList<AspectElement>();
        for (final CyNode cy_node : network.getNodeList()) {
            node_elements.add(new NodesElement(String.valueOf(cy_node.getSUID())));
            final View<CyNode> node_view = view.getNodeView(cy_node);
            cartesian_layout_elements.add(new CartesianLayoutElement(String.valueOf(cy_node
                    .getSUID()), node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
                    node_view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));

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
