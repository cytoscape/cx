package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cytoscape.model.CyNetwork;

public class CxToCyImpl implements CxToCy {

    @Override
    public void deserializeCyNetwork(final CyNetwork network, final InputStream in)
            throws IOException {

        final CxReader r = CxReader.createInstance(in);
        r.addAspectFragmentReader(EdgesFragmentReader.createInstance());
        r.addAspectFragmentReader(NodesFragmentReader.createInstance());
        while (r.hasNext()) {

            final List<AspectElement> aspects = r.getNext();
            if ((aspects != null) && !aspects.isEmpty()) {
                final String name = aspects.get(0).getAspectName();
                if (name.equals(CxConstants.NODES)) {
                    addNodes(network, aspects);
                }
                else if (name.equals(CxConstants.EDGES)) {
                    addEdges(network, aspects);
                }
            }

        }
    }

    private final static void addEdges(final CyNetwork network, final List<AspectElement> aspects) {
        for (final AspectElement edge : aspects) {
            System.out.println(edge.toString());
            // network.addEdge(source, target, true);
        }
    }

    private final static void addNodes(final CyNetwork network, final List<AspectElement> aspects) {
        for (final AspectElement node : aspects) {
            System.out.println(node.toString());
            // network.
            // CyNode node = new CyNodeTest()
        }
    }
}
