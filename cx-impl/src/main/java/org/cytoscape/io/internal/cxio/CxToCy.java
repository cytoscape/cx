package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.cytoscape.io.internal.cxio.kit.AspectElement;
import org.cytoscape.io.internal.cxio.kit.CartesianLayoutElement;
import org.cytoscape.io.internal.cxio.kit.CxConstants;
import org.cytoscape.io.internal.cxio.kit.EdgesElement;
import org.cytoscape.io.internal.cxio.kit.NodesElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

public class CxToCy {

    private Map<CyNode, Double[]> position_map;

    public CyNetwork createNetwork(final SortedMap<String, List<AspectElement>> res,
            final CyNetwork network, final String collectionName) throws IOException {

        final List<AspectElement> nodes = res.get(CxConstants.NODES);
        final List<AspectElement> edges = res.get(CxConstants.EDGES);
        final List<AspectElement> layout = res.get(CxConstants.CARTESIAN_LAYOUT);

        if ((nodes == null) || nodes.isEmpty()) {
            throw new IOException("no nodes in input");
        }

        position_map = new HashMap<CyNode, Double[]>();
        final Map<String, CyNode> nodeMap = addNodes(network, nodes);
        addEdges(network, edges, nodeMap);
        if ((layout != null) && !layout.isEmpty()) {
            addPositions(layout, nodeMap);
        }
 
        if (collectionName != null) {
            final CyRootNetwork rootNetwork = ((CySubNetwork) network).getRootNetwork();
            rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, collectionName);
        }

        return network;
    }

    private final void addPositions(final List<AspectElement> layout,
            final Map<String, CyNode> node_map) {
        for (final AspectElement ae : layout) {
            final CartesianLayoutElement cle = (CartesianLayoutElement) ae;
            position_map.put(node_map.get(cle.getNode()), new Double[] {
                Double.valueOf(cle.getX()), Double.valueOf(cle.getY()) });
        }
    }

    private final Map<String, CyNode> addNodes(final CyNetwork network,
            final List<AspectElement> nodes) {

        final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

        for (final AspectElement node : nodes) {

            final String node_id = ((NodesElement) node).getId();
            CyNode cyNode = nodeMap.get(node_id);
            if (cyNode == null) {
                cyNode = network.addNode();

                // Use ID as unique name.
                network.getRow(cyNode).set(CyNetwork.NAME, node_id);
                nodeMap.put(node_id, cyNode);
            }

        }
        return nodeMap;
    }

    private final void addEdges(final CyNetwork network, final List<AspectElement> edges,
            final Map<String, CyNode> nodeMap) {

        final CyTable edgeTable = network.getDefaultEdgeTable();

        for (final AspectElement edge : edges) {
            final CyNode sourceNode = nodeMap.get(((EdgesElement) edge).getSource());
            final CyNode targetNode = nodeMap.get(((EdgesElement) edge).getTarget());
            final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
        }
    }

    protected Map<CyNode, Double[]> getNodePosition() {
        return position_map;
    }
}
