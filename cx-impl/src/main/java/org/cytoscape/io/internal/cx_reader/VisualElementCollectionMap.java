package org.cytoscape.io.internal.cx_reader;

import java.util.HashMap;
import java.util.Map;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

final public class VisualElementCollectionMap {

    private final Map<Long, VisualElementCollection> _data;

    public VisualElementCollectionMap() {
        _data = new HashMap<Long, VisualElementCollection>();
    }

    public final void addCartesianLayoutElement(final Long subnet,
                                                final CyNode node,
                                                final CartesianLayoutElement layout) {
        checkForKey(subnet);
        _data.get(subnet).getCartesianLayoutElementsMap().put(node, layout);
    }

    public final void addEdgesDefaultVisualPropertiesElement(final Long subnet,
                                                             final CyVisualPropertiesElement edges_default_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).setEdgesDefaultVisualPropertiesElement(edges_default_visual_properties_element);
    }

    public final void addEdgeVisualPropertiesElement(final Long subnet,
                                                     final CyEdge edge,
                                                     final CyVisualPropertiesElement edges_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).getEdgeVisualPropertiesElementsMap().put(edge, edges_visual_properties_element);
    }

    public final void addNetworkVisualPropertiesElement(final Long subnet,
                                                        final CyVisualPropertiesElement network_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).setNetworkVisualPropertiesElement(network_visual_properties_element);
    }

    public final void addNodesDefaultVisualPropertiesElement(final Long subnet,
                                                             final CyVisualPropertiesElement nodes_default_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).setNodesDefaultVisualPropertiesElement(nodes_default_visual_properties_element);
    }

    public final void addNodeVisualPropertiesElement(final Long subnet,
                                                     final CyNode node,
                                                     final CyVisualPropertiesElement nodes_visual_properties_element) {
        checkForKey(subnet);
        _data.get(subnet).getNodeVisualPropertiesElementsMap().put(node, nodes_visual_properties_element);
    }

    private final void checkForKey(final Long subnet) {
        if (subnet == null) {
            throw new IllegalArgumentException("subnetwork must not be null");
        }
        if (!_data.containsKey(subnet)) {
            _data.put(subnet, new VisualElementCollection());
        }
    }

    public final Map<CyNode, CartesianLayoutElement> getCartesianLayoutElements(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getCartesianLayoutElementsMap();
    }

    public final CyVisualPropertiesElement getEdgesDefaultVisualPropertiesElement(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getEdgesDefaultVisualPropertiesElement();
    }

    public final Map<CyEdge, CyVisualPropertiesElement> getEdgeVisualPropertiesElementsMap(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getEdgeVisualPropertiesElementsMap();
    }

    public final CyVisualPropertiesElement getNetworkVisualPropertiesElement(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getNetworkVisualPropertiesElement();
    }

    public final CyVisualPropertiesElement getNodesDefaultVisualPropertiesElement(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getNodesDefaultVisualPropertiesElement();
    }

    public final Map<CyNode, CyVisualPropertiesElement> getNodeVisualPropertiesElementsMap(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getNodeVisualPropertiesElementsMap();
    }

    public final SubNetworkElement getSubNetworkElement(final Long subnet) {
        if (!_data.containsKey(subnet)) {
            return null;
        }
        return _data.get(subnet).getSubNetworkElement();
    }

    public final void addSubNetworkElement(final Long subnet, final SubNetworkElement subnetwork_element) {
        checkForKey(subnet);
        _data.get(subnet).setSubNetworkElement(subnetwork_element);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<Long, VisualElementCollection> entry : _data.entrySet()) {
            sb.append("key: ");
            sb.append(entry.getKey());
            sb.append("\n");
            sb.append("value: ");
            sb.append("\n");
            sb.append(entry.getValue().toString());
            sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();

    }
    
    public final boolean isEmpty() {
        return _data.isEmpty();
    }

}
