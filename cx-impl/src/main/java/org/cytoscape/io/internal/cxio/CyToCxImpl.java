package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class CyToCxImpl implements CyToCx {

    @Override
    public void serializeCyNetwork(final CyNetwork network, final OutputStream out)
            throws IOException {

        final CxWriter w = CxWriter.createInstance(out);
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
}
