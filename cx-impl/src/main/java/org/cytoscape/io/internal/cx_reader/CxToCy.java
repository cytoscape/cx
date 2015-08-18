package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.cxio.aspects.datamodels.AbstractAttributesElement.ATTRIBUTE_TYPE;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.GroupElement;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NetworkRelationsElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.util.Util;
import org.cytoscape.io.internal.cxio.VisualPropertyType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

public final class CxToCy {

    private Set<CyNode>                _nodes_with_visiual_properties;
    private Set<CyEdge>                _edges_with_visual_properties;

    private VisualElementCollectionMap _visual_element_collections;

    private NetworkRelationsElement    _network_relations;
    private Map<Long, String>          _network_suid_to_networkrelations_map;

    public final Map<Long, String> getNetworkSuidToNetworkRelationsMap() {
        return _network_suid_to_networkrelations_map;
    }

    public final List<CyNetwork> createNetwork(final SortedMap<String, List<AspectElement>> aspect_collection,
                                               CyRootNetwork root_network,
                                               final CyNetworkFactory network_factory,
                                               final String collectionName) throws IOException {

        final List<AspectElement> nodes = aspect_collection.get(NodesElement.NAME);
        final List<AspectElement> edges = aspect_collection.get(EdgesElement.NAME);
        final List<AspectElement> layout = aspect_collection.get(CartesianLayoutElement.NAME);
        final List<AspectElement> node_attributes = aspect_collection.get(NodeAttributesElement.NAME);
        final List<AspectElement> edge_attributes = aspect_collection.get(EdgeAttributesElement.NAME);
        final List<AspectElement> network_attributes = aspect_collection.get(NetworkAttributesElement.NAME);
        final List<AspectElement> visual_properties = aspect_collection.get(VisualPropertiesElement.NAME);
        final List<AspectElement> groups = aspect_collection.get(GroupElement.NAME);
        final List<AspectElement> subnetworks = aspect_collection.get(SubNetworkElement.NAME);
        final List<AspectElement> network_relations = aspect_collection.get(NetworkRelationsElement.NAME);

        final Map<String, List<NodeAttributesElement>> node_attributes_map = new HashMap<String, List<NodeAttributesElement>>();
        final Map<String, List<EdgeAttributesElement>> edge_attributes_map = new HashMap<String, List<EdgeAttributesElement>>();
        final Map<String, List<NetworkAttributesElement>> network_attributes_map = new HashMap<String, List<NetworkAttributesElement>>();

        if ((nodes == null) || nodes.isEmpty()) {
            throw new IOException("no nodes in input");
        }

        _network_suid_to_networkrelations_map = new HashMap<Long, String>();
        _visual_element_collections = new VisualElementCollectionMap();

        // Dealing with subnetwork relations:
        String parent_network_id = null;
        List<String> subnetwork_ids;
        int number_of_subnetworks = 1;
        
        
        if (subnetworks != null && !subnetworks.isEmpty()) {
            for (AspectElement element : subnetworks) {
                SubNetworkElement subnetwork_element = (SubNetworkElement) element;
                _visual_element_collections.setSubNetworkElement(subnetwork_element.getId(), subnetwork_element);
            }
        }

        if ((network_relations != null) && !network_relations.isEmpty()) {
            _network_relations = (NetworkRelationsElement) network_relations.get(0);
            System.out.println(_network_relations.toString());
            final Set<String> parent_ids = NetworkRelationsElement.getAllParentNetworkIds(network_relations);
            if ((parent_ids == null) || parent_ids.isEmpty()) {
                throw new IOException("no parent network id");
            }
            else if (parent_ids.size() > 1) {
                throw new IOException("multiple parent network ids: " + parent_ids);
            }
           
            for (String s : parent_ids) {
                parent_network_id = s;
            }
            subnetwork_ids = NetworkRelationsElement.getSubNetworkIds(parent_network_id, network_relations);
            if ((subnetwork_ids == null) || subnetwork_ids.isEmpty()) {
                throw new IOException("no subnetwork ids for: " + parent_network_id);
            }
            number_of_subnetworks = subnetwork_ids.size();
        }
        else {
            System.out.println("NO network relations");
            _network_relations = null;
            subnetwork_ids = new ArrayList<String>();
        }
        // ------------------------------------------------

        final List<CyNetwork> new_networks = new ArrayList<CyNetwork>();

        // ///////////////////////////////////////////////////////////////////////////////////////////
        for (int i = 0; i < number_of_subnetworks; ++i) {

            CySubNetwork sub_network;

            if (root_network != null) {
                // Root network exists
                sub_network = root_network.addSubNetwork();
            }
            else {
                sub_network = (CySubNetwork) network_factory.createNetwork();
                root_network = sub_network.getRootNetwork();
                if (!Util.isEmpty(collectionName)) {
                    root_network.getRow(root_network).set(CyNetwork.NAME, collectionName);
                }
            }

            if (_network_relations == null) {
                _network_suid_to_networkrelations_map.put(sub_network.getSUID(), String.valueOf(sub_network.getSUID()));
            }
            else {
                _network_suid_to_networkrelations_map.put(sub_network.getSUID(), subnetwork_ids.get(i));
            }

            if (node_attributes != null) {
                for (final AspectElement node_attribute : node_attributes) {
                    final NodeAttributesElement nae = (NodeAttributesElement) node_attribute;
                    final List<String> pos = nae.getPropertyOf();
                    for (final String po : pos) {
                        if (!node_attributes_map.containsKey(po)) {
                            node_attributes_map.put(po, new ArrayList<NodeAttributesElement>());
                        }
                        node_attributes_map.get(po).add(nae);
                    }
                }
            }

            if (edge_attributes != null) {
                for (final AspectElement edge_attribute : edge_attributes) {
                    final EdgeAttributesElement eae = (EdgeAttributesElement) edge_attribute;
                    final List<String> pos = eae.getPropertyOf();
                    for (final String po : pos) {
                        if (!edge_attributes_map.containsKey(po)) {
                            edge_attributes_map.put(po, new ArrayList<EdgeAttributesElement>());
                        }
                        edge_attributes_map.get(po).add(eae);
                    }
                }
            }

            if (network_attributes != null) {
                for (final AspectElement e : network_attributes) {
                    final NetworkAttributesElement nae = (NetworkAttributesElement) e;
                    final List<String> pos = nae.getPropertyOf();
                    for (final String po : pos) {
                        if (!network_attributes_map.containsKey(po)) {
                            network_attributes_map.put(po, new ArrayList<NetworkAttributesElement>());
                        }
                        network_attributes_map.get(po).add(nae);
                    }
                }
            }

            final Map<String, CyNode> node_map = addNodes(sub_network, nodes, node_attributes_map);

            final Map<String, CyEdge> edge_map = addEdges(sub_network, edges, node_map, edge_attributes_map);

            if (visual_properties != null) {
                _nodes_with_visiual_properties = new HashSet<CyNode>();
                _edges_with_visual_properties = new HashSet<CyEdge>();
                for (final AspectElement element : visual_properties) {
                    final VisualPropertiesElement vpe = (VisualPropertiesElement) element;

                    final String view = vpe.getView();

                    if (vpe.getPropertiesOf().equals(VisualPropertyType.NETWORK.asString())) {
                        _visual_element_collections.addNetworkVisualPropertiesElement(view, vpe);
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.NODES_DEFAULT.asString())) {
                        _visual_element_collections.addNodesDefaultVisualPropertiesElement(view, vpe);
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.EDGES_DEFAULT.asString())) {
                        _visual_element_collections.addEdgesDefaultVisualPropertiesElement(view, vpe);
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.NODES.asString())) {
                        final List<String> applies_to_nodes = vpe.getAppliesTo();
                        for (final String applies_to_node : applies_to_nodes) {
                            _nodes_with_visiual_properties.add(node_map.get(applies_to_node));
                            _visual_element_collections.addNodeVisualPropertiesElement(view,
                                                                                       node_map.get(applies_to_node),
                                                                                       vpe);
                        }
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.EDGES.asString())) {
                        final List<String> applies_to_edges = vpe.getAppliesTo();
                        for (final String applies_to_edge : applies_to_edges) {
                            _edges_with_visual_properties.add(edge_map.get(applies_to_edge));
                            _visual_element_collections.addEdgeVisualPropertiesElement(view,
                                                                                       edge_map.get(applies_to_edge),
                                                                                       vpe);
                        }
                    }
                }
            }

            addPositions(layout, node_map);
            new_networks.add(sub_network);

            System.out.println(_visual_element_collections.toString());

        }
        // ////////////////////////////////////////////////////////////////////////////////////////
        return new_networks;
    }

    public final NetworkRelationsElement getNetworkRelations() {
        return _network_relations;
    }

    public final VisualElementCollectionMap getVisualElementCollectionMap() {
        return _visual_element_collections;
    }

    private final void addPositions(final List<AspectElement> layout, final Map<String, CyNode> node_map) {
        for (final AspectElement e : layout) {
            final CartesianLayoutElement cle = (CartesianLayoutElement) e;
            _visual_element_collections.addCartesianLayoutElement(cle.getView(), node_map.get(cle.getNode()), cle);
        }
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

    private final void addNodeTableData(final List<NodeAttributesElement> naes,
                                        final CyIdentifiable graph_object,
                                        final CyNetwork network,
                                        final CyTable table) {
        if (naes == null) {
            throw new IllegalArgumentException("NodeAttributesElement is null");
        }
        final CyRow row = network.getRow(graph_object);
        for (final NodeAttributesElement nae : naes) {
            final String name = nae.getName();
            final List<String> values = nae.getValues();

            if (!(name.equals(CyIdentifiable.SUID)) && !(name.equals(CyNetwork.SELECTED))) {

                // New column creation:
                if (table.getColumn(name) == null) {
                    final Class<?> data_type = getDataType(nae.getType());

                    if (values.size() == 1) {
                        table.createColumn(name, data_type, false);
                    }
                    else if (values.size() > 1) {
                        table.createListColumn(name, data_type, false);
                    }
                }
                final CyColumn col = table.getColumn(name);
                row.set(name, getValue(values, col));
            }
        }

    }

    private final void addEdgeTableData(final List<EdgeAttributesElement> eaes,
                                        final CyIdentifiable graph_object,
                                        final CyNetwork network,
                                        final CyTable table) {
        if (eaes == null) {
            throw new IllegalArgumentException("EdgeAttributesElement is null");
        }
        final CyRow row = network.getRow(graph_object);
        for (final EdgeAttributesElement eae : eaes) {
            final String name = eae.getName();
            final List<String> values = eae.getValues();

            if (!(name.equals(CyIdentifiable.SUID)) && !(name.equals(CyNetwork.SELECTED))) {

                // New column creation:
                if (table.getColumn(name) == null) {
                    final Class<?> data_type = getDataType(eae.getType());

                    if (values.size() == 1) {
                        table.createColumn(name, data_type, false);
                    }
                    else if (values.size() > 1) {
                        table.createListColumn(name, data_type, false);
                    }
                }
                final CyColumn col = table.getColumn(name);
                row.set(name, getValue(values, col));
            }
        }

    }

    private final Map<String, CyNode> addNodes(final CyNetwork network,
                                               final List<AspectElement> nodes,
                                               final Map<String, List<NodeAttributesElement>> node_attributes_map) {

        final Map<String, CyNode> node_map = new HashMap<String, CyNode>();

        final CyTable node_table = network.getDefaultNodeTable();

        for (final AspectElement node : nodes) {
            final String node_id = ((NodesElement) node).getId();
            CyNode cyNode = node_map.get(node_id);
            if (cyNode == null) {
                cyNode = network.addNode();
                // Use ID as unique name.
                network.getRow(cyNode).set(CyNetwork.NAME, node_id);
                node_map.put(node_id, cyNode);
                if ((node_attributes_map != null) && !node_attributes_map.isEmpty()) {
                    addNodeTableData(node_attributes_map.get(node_id), cyNode, network, node_table);
                }
            }
        }
        return node_map;
    }

    private final Map<String, CyEdge> addEdges(final CyNetwork network,
                                               final List<AspectElement> edges,
                                               final Map<String, CyNode> node_map,
                                               final Map<String, List<EdgeAttributesElement>> edge_attributes_map) {

        final CyTable edgeTable = network.getDefaultEdgeTable();
        final Map<String, CyEdge> edge_map = new HashMap<String, CyEdge>();
        for (final AspectElement edge : edges) {
            final EdgesElement e = (EdgesElement) edge;

            final CyNode source = node_map.get(e.getSource());
            final CyNode target = node_map.get(e.getTarget());
            final CyEdge newEdge = network.addEdge(source, target, true);
            edge_map.put(e.getId(), newEdge);
            if ((edge_attributes_map != null) && !edge_attributes_map.isEmpty()) {
                addEdgeTableData(edge_attributes_map.get(e.getId()), newEdge, network, edgeTable);
            }
        }
        return edge_map;
    }

    public Set<CyNode> getNodeWithVisualProperties() {
        return _nodes_with_visiual_properties;
    }

    public Set<CyEdge> getEdgeWithVisualProperties() {
        return _edges_with_visual_properties;
    }

}
