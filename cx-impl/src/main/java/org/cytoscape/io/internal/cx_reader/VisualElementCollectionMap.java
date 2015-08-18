package org.cytoscape.io.internal.cx_reader;

import java.util.HashMap;
import java.util.Map;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

final class VisualElementCollectionMap {

    private final Map<String, VisualElementCollection> _data;

    VisualElementCollectionMap() {
        _data = new HashMap<String, VisualElementCollection>();
    }

    final void addCartesianLayoutElement(final String subnet, final CyNode node, final CartesianLayoutElement layout) {
        checkForKey(subnet);
        _data.get(subnet).getCartesianLayoutElementsMap().put(node, layout);
    }

    final void addEdgesDefaultVisualPropertiesElement(final String subnet,
                                                      final VisualPropertiesElement edges_default_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).setEdgesDefaultVisualPropertiesElement(edges_default_visual_properties_element);
    }

    final void addEdgeVisualPropertiesElement(final String subnet,
                                              final CyEdge edge,
                                              final VisualPropertiesElement edges_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).getEdgeVisualPropertiesElementsMap().put(edge, edges_visual_properties_element);
    }

    final void addNetworkVisualPropertiesElement(final String subnet,
                                                 final VisualPropertiesElement network_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).setNetworkVisualPropertiesElement(network_visual_properties_element);
    }

    final void addNodesDefaultVisualPropertiesElement(final String subnet,
                                                      final VisualPropertiesElement nodes_default_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).setNodesDefaultVisualPropertiesElement(nodes_default_visual_properties_element);
    }

    final void addNodeVisualPropertiesElement(final String subnet,
                                              final CyNode node,
                                              final VisualPropertiesElement nodes_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).getNodeVisualPropertiesElementsMap().put(node, nodes_visual_properties_element);
    }

    private final void checkForKey(final String subnet) {
        if (!_data.containsKey(subnet)) {
            _data.put(subnet, new VisualElementCollection());
        }
    }
    
    final Map<CyNode, CartesianLayoutElement> getCartesianLayoutElements(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getCartesianLayoutElementsMap();
    }

    final VisualPropertiesElement getEdgesDefaultVisualPropertiesElement(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getEdgesDefaultVisualPropertiesElement();
    }

    final Map<CyEdge, VisualPropertiesElement> getEdgeVisualPropertiesElementsMap(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getEdgeVisualPropertiesElementsMap();
    }

    final VisualPropertiesElement getNetworkVisualPropertiesElement(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getNetworkVisualPropertiesElement();
    }

    final VisualPropertiesElement getNodesDefaultVisualPropertiesElement(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getNodesDefaultVisualPropertiesElement();
    }

    final Map<CyNode, VisualPropertiesElement> getNodeVisualPropertiesElementsMap(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getNodeVisualPropertiesElementsMap();
    }
    
    
    
    final SubNetworkElement getSubNetworkElement(final String subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getSubNetworkElement();
    }

    final void setSubNetworkElement(final String subnet,
                                                      final SubNetworkElement subnetwork_element) {
        checkForKey(subnet);
        _data.get(subnet).setSubNetworkElement(subnetwork_element);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, VisualElementCollection> entry : _data.entrySet()) {
            sb.append("KEY: ");
            sb.append(entry.getKey());
            sb.append("\n");

            sb.append("VALUES: ");
           
            sb.append("\n");
            sb.append(entry.getValue().toString());
            sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();

    }

}
