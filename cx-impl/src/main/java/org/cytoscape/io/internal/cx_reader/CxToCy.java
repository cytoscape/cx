package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.cxio.aspects.datamodels.AbstractAttributesElement.ATTRIBUTE_TYPE;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.io.internal.visual_properties.VisualPropertiesWriter;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

public final class CxToCy {

    private Map<CyNode, Double[]>                position_map;
    private Map<CyNode, VisualPropertiesElement> node_vpe_map;
    private Map<String, VisualPropertiesElement> edge_vpe_map;

    public final CyNetwork createNetwork(final SortedMap<String, List<AspectElement>> res,
                                         final CyNetwork network,
                                         final String collectionName) throws IOException {

        final List<AspectElement> nodes = res.get(NodesElement.NAME);
        final List<AspectElement> edges = res.get(EdgesElement.NAME);
        // final List<AspectElement> layout =
        // res.get(CartesianLayoutElement.NAME);
        final List<AspectElement> node_attributes = res.get(NodeAttributesElement.NAME);
        final List<AspectElement> edge_attributes = res.get(EdgeAttributesElement.NAME);
        final List<AspectElement> visual_properties = res.get(VisualPropertiesElement.NAME);

        final Map<String, NodeAttributesElement> node_attributes_map = new HashMap<String, NodeAttributesElement>();
        final Map<String, EdgeAttributesElement> edge_attributes_map = new HashMap<String, EdgeAttributesElement>();

        if ((nodes == null) || nodes.isEmpty()) {
            throw new IOException("no nodes in input");
        }

        if (node_attributes != null) {
            for (final AspectElement node_attribute : node_attributes) {
                final NodeAttributesElement nae = (NodeAttributesElement) node_attribute;
                node_attributes_map.put(nae.getNodes().get(0), nae);
            }
        }

        if (edge_attributes != null) {
            for (final AspectElement edge_attribute : edge_attributes) {
                final EdgeAttributesElement eae = (EdgeAttributesElement) edge_attribute;
                edge_attributes_map.put(eae.getEdges().get(0), eae);
            }
        }

        position_map = new HashMap<CyNode, Double[]>();
        final Map<String, CyNode> nodeMap = addNodes(network, nodes, node_attributes_map);

        addEdges(network, edges, nodeMap, edge_attributes_map);

        if (visual_properties != null) {
            node_vpe_map = new HashMap<CyNode, VisualPropertiesElement>();
            for (final AspectElement element : visual_properties) {
                final VisualPropertiesElement vpe = (VisualPropertiesElement) element;
                if (vpe.getPropertiesOf().equals(VisualPropertiesWriter.NODES)) {
                    final List<String> applies_to_nodes = vpe.getAppliesTo();
                    for (final String applies_to_node : applies_to_nodes) {
                        node_vpe_map.put(nodeMap.get(applies_to_node), vpe);
                    }
                }
            }
            edge_vpe_map = new HashMap<String, VisualPropertiesElement>();
            // for (final AspectElement element : visual_properties) {
            // final VisualPropertiesElement vpe = (VisualPropertiesElement)
            // element;
            // if ( vpe.getPropertiesOf().equals("edges") ) {
            // List<String> applies_to_edges = vpe.getAppliesTo();
            // for (String applies_to_node : applies_to_edges) {
            // edge_vpe_map.put(applies_to_node, vpe);
            // }
            // }
            // }
        }

        // if ((layout != null) && !layout.isEmpty()) {
        // addPositions(layout, nodeMap);
        // }

        if (collectionName != null) {
            final CyRootNetwork rootNetwork = ((CySubNetwork) network).getRootNetwork();
            rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME, collectionName);
        }

        return network;
    }

    private Class<?> getDataType(final ATTRIBUTE_TYPE type) {
        if (type == ATTRIBUTE_TYPE.STRING) {
            return String.class;
        }
        else if (type == ATTRIBUTE_TYPE.BOOLEAN) {
            return Boolean.class;
        }
        else if (type == ATTRIBUTE_TYPE.DOUBLE) {
            return Double.class;
        }
        else if (type == ATTRIBUTE_TYPE.FLOAT) {
            return Float.class;
        }
        else if (type == ATTRIBUTE_TYPE.INTEGER) {
            return Integer.class;
        }
        else if (type == ATTRIBUTE_TYPE.LONG) {
            return Long.class;
        }
        else if (type == ATTRIBUTE_TYPE.SHORT) {
            return Integer.class;
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

    private final Object parseValue(final String value, final Class<?> type) {
        if (type == String.class) {
            return value;
        }
        else if (type == Long.class) {
            return Long.valueOf(value);
        }
        else if (type == Integer.class) {
            return Integer.valueOf(value);
        }
        else if (type == Float.class) {
            return Float.valueOf(value);
        }
        else if (type == Double.class) {
            return Double.valueOf(value);
        }
        else if (type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

    private final Object getValue(final List<String> values, final CyColumn column) {
        if (values.size() > 1) {
            final List<Object> list = new ArrayList<Object>();
            for (final String value : values) {
                list.add(parseValue(value, column.getListElementType()));
            }
            return list;
        }
        else {
            return parseValue(values.get(0), column.getType());
        }
    }

    private final void addTableData(final NodeAttributesElement nae,
                                    final CyIdentifiable graphObject,
                                    final CyNetwork network,
                                    final CyTable table) {
        if (nae == null) {
            throw new IllegalArgumentException("NodeAttributesElement is null");
        }
        final SortedMap<String, List<String>> attributes = nae.getAttributes();
        final SortedMap<String, ATTRIBUTE_TYPE> types = nae.getAttributesTypes();

        for (final Entry<String, List<String>> entry : attributes.entrySet()) {
            final String field_name = entry.getKey();
            // Ignore unnecessary fields (ID, SUID, SELECTED)
            if ((field_name.equals(CyIdentifiable.SUID) == false) && (field_name.equals(CyNetwork.SELECTED) == false)) {

                final List<String> values = entry.getValue();
                // New column creation:
                if (table.getColumn(field_name) == null) {
                    Class<?> dataType = String.class;
                    if (types.containsKey(field_name)) {
                        dataType = getDataType(types.get(field_name));
                    }
                    if (values.size() == 1) {
                        table.createColumn(field_name, dataType, false);
                    }
                    else if (values.size() > 1) {
                        table.createListColumn(field_name, dataType, false);
                    }
                }
                final CyColumn col = table.getColumn(field_name);
                network.getRow(graphObject).set(field_name, getValue(values, col));
            }
        }

    }

    private final void addTableData(final EdgeAttributesElement eae,
                                    final CyIdentifiable graphObject,
                                    final CyNetwork network,
                                    final CyTable table) {
        if (eae == null) {
            throw new IllegalArgumentException("EdgeAttributesElement is null");
        }

        final SortedMap<String, List<String>> attributes = eae.getAttributes();
        final SortedMap<String, ATTRIBUTE_TYPE> types = eae.getAttributesTypes();

        for (final Entry<String, List<String>> entry : attributes.entrySet()) {
            final String field_name = entry.getKey();
            // Ignore unnecessary fields (ID, SUID, SELECTED)
            if ((field_name.equals(CyIdentifiable.SUID) == false) && (field_name.equals(CyNetwork.SELECTED) == false)) {

                final List<String> values = entry.getValue();
                // New column creation:
                if (table.getColumn(field_name) == null) {
                    Class<?> dataType = String.class;
                    if (types.containsKey(field_name)) {
                        dataType = getDataType(types.get(field_name));
                    }
                    if (values.size() == 1) {
                        table.createColumn(field_name, dataType, false);
                    }
                    else if (values.size() > 1) {
                        table.createListColumn(field_name, dataType, false);
                    }
                }
                final CyColumn col = table.getColumn(field_name);
                network.getRow(graphObject).set(field_name, getValue(values, col));
            }
        }

    }

    private final void addPositions(final List<AspectElement> layout, final Map<String, CyNode> node_map) {
        for (final AspectElement ae : layout) {
            final CartesianLayoutElement cle = (CartesianLayoutElement) ae;
            position_map.put(node_map.get(cle.getNode()),
                             new Double[] { Double.valueOf(cle.getX()), Double.valueOf(cle.getY()) });
        }
    }

    private final Map<String, CyNode> addNodes(final CyNetwork network,
                                               final List<AspectElement> nodes,
                                               final Map<String, NodeAttributesElement> node_attributes_map) {

        final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

        final CyTable nodeTable = network.getDefaultNodeTable();

        for (final AspectElement node : nodes) {

            final String node_id = ((NodesElement) node).getId();
            CyNode cyNode = nodeMap.get(node_id);
            if (cyNode == null) {
                cyNode = network.addNode();

                // Use ID as unique name.
                network.getRow(cyNode).set(CyNetwork.NAME, node_id);
                nodeMap.put(node_id, cyNode);
                if ((node_attributes_map != null) && !node_attributes_map.isEmpty()) {
                    addTableData(node_attributes_map.get(node_id), cyNode, network, nodeTable);
                }
            }

        }
        return nodeMap;
    }

    private final void addEdges(final CyNetwork network,
                                final List<AspectElement> edges,
                                final Map<String, CyNode> nodeMap,
                                final Map<String, EdgeAttributesElement> edge_attributes_map) {

        final CyTable edgeTable = network.getDefaultEdgeTable();

        for (final AspectElement edge : edges) {
            final EdgesElement e = (EdgesElement) edge;
            final CyNode sourceNode = nodeMap.get(e.getSource());
            final CyNode targetNode = nodeMap.get(e.getTarget());
            final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
            if ((edge_attributes_map != null) && !edge_attributes_map.isEmpty()) {
                addTableData(edge_attributes_map.get(e.getId()), newEdge, network, edgeTable);
            }
        }
    }

    public Map<CyNode, Double[]> getNodePosition() {
        return position_map;
    }

    public Map<CyNode, VisualPropertiesElement> getNodeVisualPropertiesElementsMap() {
        return node_vpe_map;
    }
}
