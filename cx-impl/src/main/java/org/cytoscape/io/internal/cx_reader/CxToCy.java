package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private Map<CyNode, VisualPropertiesElement>             _node_vpe_map;
    private Map<CyEdge, VisualPropertiesElement>             _edge_vpe_map;

    private VisualPropertiesElement                          _nodes_default_vpe;
    private VisualPropertiesElement                          _edges_default_vpe;
    private VisualPropertiesElement                          _network_vpe;
    private Map<String, Map<CyNode, CartesianLayoutElement>> _position_map;
    private NetworkRelationsElement                          _network_relations;
    private Map<Long,String>                          _network_suid_to_networkrelations_map;

    
    public final Map<Long, String> getNetworkSuidToNetworkRelationsMap() {
        return _network_suid_to_networkrelations_map;
    }
    
    
    public final List<CyNetwork> createNetwork(final SortedMap<String, List<AspectElement>> aspect_collection,
                                               final CyRootNetwork root_network,
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
        
        // Dealing with subnetwork relations:
        String parent_network_id;
        List<String> subnetwork_ids;
        int number_of_subnetworks;
        
        if (network_relations != null && !network_relations.isEmpty()) {
            _network_relations = (NetworkRelationsElement) network_relations.get(0);
            final List<String> parent_ids = NetworkRelationsElement.getAllParentNetworkIds(network_relations);
            if ( parent_ids == null || parent_ids.isEmpty() ) {
                throw new IOException("no parent network id");
            }
            else if ( parent_ids.size() > 1 ) {
                throw new IOException("multiple parent network ids: " + parent_ids );
            }
            parent_network_id = parent_ids.get(0);
            subnetwork_ids = NetworkRelationsElement.getSubNetworkIds(parent_network_id, network_relations);
            if ( subnetwork_ids == null || subnetwork_ids.isEmpty() ) {
                throw new IOException("no subnetwork ids for: " + parent_network_id);
            }
            number_of_subnetworks = subnetwork_ids.size();
        }
        else {
            _network_relations = null;
            parent_network_id = null;
            subnetwork_ids = null;
            number_of_subnetworks = 1;
            
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
                if (!Util.isEmpty(collectionName)) {
                    final CyRootNetwork my_root_network = sub_network.getRootNetwork();
                    my_root_network.getRow(my_root_network).set(CyNetwork.NAME, collectionName);
                }

            }
            
           
            _network_suid_to_networkrelations_map.put( sub_network.getSUID(), subnetwork_ids.get(i));

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
                _node_vpe_map = new HashMap<CyNode, VisualPropertiesElement>();
                _edge_vpe_map = new HashMap<CyEdge, VisualPropertiesElement>();
                for (final AspectElement element : visual_properties) {
                    final VisualPropertiesElement vpe = (VisualPropertiesElement) element;

                    if (vpe.getPropertiesOf().equals(VisualPropertyType.NETWORK.asString())) {
                        _network_vpe = vpe;
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.NODES_DEFAULT.asString())) {
                        _nodes_default_vpe = vpe;
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.EDGES_DEFAULT.asString())) {
                        _edges_default_vpe = vpe;
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.NODES.asString())) {
                        final List<String> applies_to_nodes = vpe.getAppliesTo();
                        for (final String applies_to_node : applies_to_nodes) {
                            _node_vpe_map.put(node_map.get(applies_to_node), vpe);
                        }
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.EDGES.asString())) {
                        final List<String> applies_to_edges = vpe.getAppliesTo();
                        for (final String applies_to_edge : applies_to_edges) {
                            _edge_vpe_map.put(edge_map.get(applies_to_edge), vpe);
                        }
                    }
                }
            }

            if ((layout != null) && !layout.isEmpty()) {
                _position_map = new HashMap<String, Map<CyNode, CartesianLayoutElement>>();
                addPositions(layout, node_map);
            }
            else {
                _position_map = null;
            }

            new_networks.add(sub_network);
            
            // if (collectionName != null) {
            // final CyRootNetwork rootNetwork = ((CySubNetwork)
            // network).getRootNetwork();
            // rootNetwork.getRow(rootNetwork).set(CyNetwork.NAME,
            // collectionName);
            // }
        }
        // ////////////////////////////////////////////////////////////////////////////////////////
        return new_networks;
    }

    public final NetworkRelationsElement getNetworkRelations() {
        return _network_relations;
    }

    public final Map<String, Map<CyNode, CartesianLayoutElement>> getNodePosition() {
        return _position_map;
    }

    private final void addPositions(final List<AspectElement> layout, final Map<String, CyNode> node_map) {
        for (final AspectElement e : layout) {
            final CartesianLayoutElement cle = (CartesianLayoutElement) e;
            final String view = cle.getView();
            if (!_position_map.containsKey(view)) {
                _position_map.put(view, new HashMap<CyNode, CartesianLayoutElement>());
            }
            _position_map.get(view).put(node_map.get(cle.getNode()), cle);
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

    public Map<CyNode, VisualPropertiesElement> getNodeVisualPropertiesElementsMap() {
        return _node_vpe_map;
    }

    public Map<CyEdge, VisualPropertiesElement> getEdgeVisualPropertiesElementsMap() {
        return _edge_vpe_map;
    }

    public VisualPropertiesElement getNodesDefaultVisualPropertiesElement() {
        return _nodes_default_vpe;
    }

    public VisualPropertiesElement getEdgesDefaultVisualPropertiesElement() {
        return _edges_default_vpe;
    }

    public VisualPropertiesElement getNetworkVisualPropertiesElement() {
        return _network_vpe;
    }
}
