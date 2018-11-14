package org.cytoscape.io.cx;

import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.HiddenAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.cytoscape.io.internal.cxio.AspectSet;

/**
 *	This enumeration is used to identify aspects relevant for Cytoscape networks
 *	and tables.
 *
 *	This is the list of core CX aspects supported by Cytoscape.
 *
 *
 * @see AspectSet
 *
 */
public enum Aspect {

    NODES(NodesElement.ASPECT_NAME),
    EDGES(EdgesElement.ASPECT_NAME),
    CARTESIAN_LAYOUT(CartesianLayoutElement.ASPECT_NAME),
    EDGE_ATTRIBUTES(EdgeAttributesElement.ASPECT_NAME),
    NODE_ATTRIBUTES(NodeAttributesElement.ASPECT_NAME),
    NETWORK_ATTRIBUTES(NetworkAttributesElement.ASPECT_NAME),
    SUBNETWORKS(SubNetworkElement.ASPECT_NAME),
    VISUAL_PROPERTIES(CyVisualPropertiesElement.ASPECT_NAME),
    NETWORK_RELATIONS(NetworkRelationsElement.ASPECT_NAME),
    GROUPS(CyGroupsElement.ASPECT_NAME),
    HIDDEN_ATTRIBUTES(HiddenAttributesElement.ASPECT_NAME),
    TABLE_COLUMN_LABELS(CyTableColumnElement.ASPECT_NAME);

    private final String _s;

    private Aspect(final String s) {
        _s = s;
    }

    /**
     * This returns an aspect identifier as String. The returned String is the
     * official name of the aspect in question.
     *
     *
     * @return official name of the aspect identifier
     */
    public final String asString() {
        return _s;
    }

    @Override
    public String toString() {
        return asString();
    }

}
