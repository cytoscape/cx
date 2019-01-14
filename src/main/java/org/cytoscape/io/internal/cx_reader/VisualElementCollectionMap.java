package org.cytoscape.io.internal.cx_reader;

import java.util.HashMap;
import java.util.Map;

import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;

final public class VisualElementCollectionMap {

	private final Map<Long, VisualElementCollection> view_data;
	private final Map<Long, VisualElementCollection> subnet_data;
    

    public VisualElementCollectionMap() {
    	view_data = new HashMap<>();
    	subnet_data = new HashMap<>();
        
    }

    public final void addCartesianLayoutElement(final Long view,
                                                final CyNode node,
                                                final CartesianLayoutElement layout) {
        checkForView(view);
        view_data.get(view).getCartesianLayoutElementsMap().put(node, layout);
    }

    public final void addEdgesDefaultVisualPropertiesElement(final Long view,
                                                             final CyVisualPropertiesElement edges_default_visual_properties_element) {
        checkForView(view);
        view_data.get(view).setEdgesDefaultVisualPropertiesElement(edges_default_visual_properties_element);
    }

    public final void addEdgeVisualPropertiesElement(final Long view,
                                                     final CyEdge edge,
                                                     final CyVisualPropertiesElement edges_visual_properties_element) {
    	checkForView(view);
        view_data.get(view).getEdgeVisualPropertiesElementsMap().put(edge, edges_visual_properties_element);
    }

    public final void addNetworkVisualPropertiesElement(final Long view,
                                                        final CyVisualPropertiesElement network_visual_properties_element) {
    	checkForView(view);
        view_data.get(view).setNetworkVisualPropertiesElement(network_visual_properties_element);
    }

    public final void addNodesDefaultVisualPropertiesElement(final Long view,
                                                             final CyVisualPropertiesElement nodes_default_visual_properties_element) {
    	checkForView(view);
        view_data.get(view).setNodesDefaultVisualPropertiesElement(nodes_default_visual_properties_element);
    }

    public final void addNodeVisualPropertiesElement(final Long view,
                                                     final CyNode node,
                                                     final CyVisualPropertiesElement nodes_visual_properties_element) {
    	checkForView(view);
        view_data.get(view).getNodeVisualPropertiesElementsMap().put(node, nodes_visual_properties_element);
    }

    private final void checkForView(final Long view) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null");
        }
        if (!view_data.containsKey(view)) {
            view_data.put(view, new VisualElementCollection());
        }
    }

    public final Map<CyNode, CartesianLayoutElement> getCartesianLayoutElements(final Long view) {
        if (!view_data.containsKey(view)) {
            return null;
        }
        return view_data.get(view).getCartesianLayoutElementsMap();
    }

    public final CyVisualPropertiesElement getEdgesDefaultVisualPropertiesElement(final Long view) {
        if (!view_data.containsKey(view)) {
            return null;
        }
        return view_data.get(view).getEdgesDefaultVisualPropertiesElement();
    }

    public final Map<CyEdge, CyVisualPropertiesElement> getEdgeVisualPropertiesElementsMap(final Long view) {
        if (!view_data.containsKey(view)) {
            return null;
        }
        return view_data.get(view).getEdgeVisualPropertiesElementsMap();
    }

    public final CyVisualPropertiesElement getNetworkVisualPropertiesElement(final Long view) {
        if (!view_data.containsKey(view)) {
            return null;
        }
        return view_data.get(view).getNetworkVisualPropertiesElement();
    }

    public final CyVisualPropertiesElement getNodesDefaultVisualPropertiesElement(final Long view) {
        if (!view_data.containsKey(view)) {
            return null;
        }
        return view_data.get(view).getNodesDefaultVisualPropertiesElement();
    }

    public final Map<CyNode, CyVisualPropertiesElement> getNodeVisualPropertiesElementsMap(final Long view) {
        if (!view_data.containsKey(view)) {
            return null;
        }
        return view_data.get(view).getNodeVisualPropertiesElementsMap();
    }

    // Subnetwork Elements are not view specific, and need their own mapping
    public final SubNetworkElement getSubNetworkElement(final Long subnet) {
        if (!subnet_data.containsKey(subnet)) {
            return null;
        }
        return subnet_data.get(subnet).getSubNetworkElement();
    }
    
    private final void checkForSubnet(final Long subnet) {
        if (subnet == null) {
            throw new IllegalArgumentException("subnet must not be null");
        }
        if (!subnet_data.containsKey(subnet)) {
        	subnet_data.put(subnet, new VisualElementCollection());
        }
    }

    public final void addSubNetworkElement(final Long subnet, final SubNetworkElement subnetwork_element) {
        checkForSubnet(subnet);
        subnet_data.get(subnet).setSubNetworkElement(subnetwork_element);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<Long, VisualElementCollection> entry : view_data.entrySet()) {
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

    public final boolean isEmptyViews() {
        return view_data.isEmpty();
    }
    
    public final boolean isEmptySubnets() {
        return subnet_data.isEmpty();
    }

	public boolean isEmpty() {
		return isEmptySubnets() && isEmptySubnets();
	}

}
