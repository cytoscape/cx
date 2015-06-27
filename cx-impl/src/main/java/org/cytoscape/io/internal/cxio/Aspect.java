package org.cytoscape.io.internal.cxio;

import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;

/**
 * This enumeration is used to identify aspects relevant to Cytoscape.
 *
 */
public enum Aspect {

    NODES(NodesElement.NAME), EDGES(EdgesElement.NAME), EDGE_ATTRIBUTES(EdgeAttributesElement.NAME), NODE_ATTRIBUTES(
            NodeAttributesElement.NAME), CARTESIAN_LAYOUT(CartesianLayoutElement.NAME);

    private final String _s;

    private Aspect(final String s) {
        _s = s;
    }

    /**
     * Returns aspect identifier as a String
     *
     *
     */
    @Override
    public String toString() {
        return _s;
    }

}
