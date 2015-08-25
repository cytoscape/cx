package org.cytoscape.io.internal.cx_reader;

import java.util.HashMap;
import java.util.Map;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

class VisualElementCollection {

    private final Map<CyEdge, VisualPropertiesElement> _edge_vpe_map;
    private VisualPropertiesElement                    _edges_default_vpe;
    private VisualPropertiesElement                    _network_vpe;
    private final Map<CyNode, VisualPropertiesElement> _node_vpe_map;
    private VisualPropertiesElement                    _nodes_default_vpe;
    private final Map<CyNode, CartesianLayoutElement>  _layout_elements_map;
    private SubNetworkElement                          _subnetwork_element;
    private String                                     _property_of;

    VisualElementCollection() {
        _property_of = null;
        _node_vpe_map = new HashMap<CyNode, VisualPropertiesElement>();
        _edge_vpe_map = new HashMap<CyEdge, VisualPropertiesElement>();
        _layout_elements_map = new HashMap<CyNode, CartesianLayoutElement>();
        _nodes_default_vpe = null;
        _edges_default_vpe = null;
        _subnetwork_element = null;
        _network_vpe = null;

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("subnetwork_element:");
        sb.append("\n");
        sb.append(_subnetwork_element);
        sb.append("nodes:");
        sb.append("\n");
        sb.append(_node_vpe_map.toString());
        sb.append("\n");
        sb.append("edges:");
        sb.append("\n");
        sb.append(_edge_vpe_map.toString());
        sb.append("\n");
        sb.append("layout:");
        sb.append("\n");
        sb.append(_layout_elements_map.toString());
        sb.append("\n");
        sb.append("nodes default:");
        sb.append("\n");
        sb.append(_nodes_default_vpe);
        sb.append("\n");
        sb.append("edges default:");
        sb.append("\n");
        sb.append(_edges_default_vpe);
        sb.append("\n");
        sb.append("network:");
        sb.append("\n");
        sb.append(_network_vpe);
        sb.append("\n");
        return sb.toString();

    }

    public SubNetworkElement getSubNetworkElement() {
        return _subnetwork_element;
    }

    public VisualPropertiesElement getEdgesDefaultVisualPropertiesElement() {
        return _edges_default_vpe;
    }

    public Map<CyEdge, VisualPropertiesElement> getEdgeVisualPropertiesElementsMap() {
        return _edge_vpe_map;
    }

    public VisualPropertiesElement getNetworkVisualPropertiesElement() {
        return _network_vpe;
    }

    public VisualPropertiesElement getNodesDefaultVisualPropertiesElement() {
        return _nodes_default_vpe;
    }

    public Map<CyNode, VisualPropertiesElement> getNodeVisualPropertiesElementsMap() {
        return _node_vpe_map;
    }

    public Map<CyNode, CartesianLayoutElement> getCartesianLayoutElementsMap() {
        return _layout_elements_map;
    }

    public String getPropertyOf() {
        return _property_of;
    }

    public void setEdgesDefaultVisualPropertiesElement(final VisualPropertiesElement vpe) {
        _edges_default_vpe = vpe;
    }

    public void setNetworkVisualPropertiesElement(final VisualPropertiesElement vpe) {
        _network_vpe = vpe;
    }

    public void setNodesDefaultVisualPropertiesElement(final VisualPropertiesElement vpe) {
        _nodes_default_vpe = vpe;
    }

    public void setPropertOf(final String property_of) {
        _property_of = property_of;
    }

    public void setSubNetworkElement(final SubNetworkElement subnetwork_element) {
        _subnetwork_element = subnetwork_element;
    }

}
