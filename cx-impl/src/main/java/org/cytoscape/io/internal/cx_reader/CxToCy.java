package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.cxio.aspects.datamodels.AbstractAttributesAspectElement;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.CyGroupsElement;
import org.cxio.aspects.datamodels.CyViewsElement;
import org.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.HiddenAttributesElement;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NetworkRelationsElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.util.CxioUtil;
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

    private final static boolean       DEBUG          = true;
    private final static boolean       STRICT         = false;
    private static final long          DEFAULT_SUBNET = -Long.MAX_VALUE;

    private Set<CyNode>                _nodes_with_visual_properties;
    private Set<CyEdge>                _edges_with_visual_properties;
    private VisualElementCollectionMap _visual_element_collections;
    private Map<Long, Long>            _network_suid_to_networkrelations_map;
    private Map<Long, CyNode>          _cxid_to_cynode_map;
    private Map<Long, CyEdge>          _cxid_to_cyedge_map;
    private Map<Long, Long>            _view_to_subnet_map;
    private Map<Long, List<Long>>      _subnet_to_views_map;

    public final Map<Long, Long> getViewToSubNetworkMap() {
        return _view_to_subnet_map;
    }

    public final Map<Long, List<Long>> getSubNetworkToViewsMap() {
        return _subnet_to_views_map;
    }

    public final static Map<Long, Long> makeViewToSubNetworkMap(final List<AspectElement> network_relations) {
        final Map<Long, Long> view_to_subnet_map = new TreeMap<Long, Long>();
        for (final AspectElement e : network_relations) {
            final NetworkRelationsElement nwe = (NetworkRelationsElement) e;
            if (nwe.getRelationship() == NetworkRelationsElement.TYPE_VIEW) {
                view_to_subnet_map.put(nwe.getChild(), nwe.getParent());
            }
        }
        return view_to_subnet_map;
    }

    public final static Map<Long, List<Long>> makeSubNetworkToViewsMap(final List<AspectElement> network_relations) {
        final Map<Long, List<Long>> subnet_to_views_map = new TreeMap<Long, List<Long>>();
        for (final AspectElement e : network_relations) {
            final NetworkRelationsElement nwe = (NetworkRelationsElement) e;
            if (nwe.getRelationship() == NetworkRelationsElement.TYPE_VIEW) {

                if (!subnet_to_views_map.containsKey(nwe.getParent())) {
                    subnet_to_views_map.put(nwe.getParent(), new ArrayList<Long>());
                }
                subnet_to_views_map.get(nwe.getParent()).add(nwe.getChild());
            }
        }
        return subnet_to_views_map;
    }

    public final static Map<Long, String> makeSubNetworkToNameMap(final List<AspectElement> network_relations) {
        final Map<Long, String> m = new HashMap<Long, String>();
        for (final AspectElement e : network_relations) {
            final NetworkRelationsElement nwe = (NetworkRelationsElement) e;
            if (nwe.getRelationship() == NetworkRelationsElement.TYPE_SUBNETWORK) {
                m.put(nwe.getChild(), nwe.getChildName());
            }
        }
        return m;
    }

    public final List<CyNetwork> createNetwork(final SortedMap<String, List<AspectElement>> aspect_collection,
                                               CyRootNetwork root_network,
                                               final CyNetworkFactory network_factory,
                                               final String collection_name,
                                               final boolean perform_basic_integrity_checks) throws IOException {

        final List<AspectElement> nodes = aspect_collection.get(NodesElement.ASPECT_NAME);
        final List<AspectElement> edges = aspect_collection.get(EdgesElement.ASPECT_NAME);
        final List<AspectElement> cartesian_layout_elements = aspect_collection.get(CartesianLayoutElement.ASPECT_NAME);
        final List<AspectElement> node_attributes = aspect_collection.get(NodeAttributesElement.ASPECT_NAME);
        final List<AspectElement> edge_attributes = aspect_collection.get(EdgeAttributesElement.ASPECT_NAME);
        final List<AspectElement> network_attributes = aspect_collection.get(NetworkAttributesElement.ASPECT_NAME);
        final List<AspectElement> hidden_attributes = aspect_collection.get(HiddenAttributesElement.ASPECT_NAME);
        final List<AspectElement> visual_properties = aspect_collection.get(CyVisualPropertiesElement.ASPECT_NAME);
        final List<AspectElement> subnetworks = aspect_collection.get(SubNetworkElement.ASPECT_NAME);
        final List<AspectElement> network_relations = aspect_collection.get(NetworkRelationsElement.ASPECT_NAME);
        final List<AspectElement> groups = aspect_collection.get(CyGroupsElement.ASPECT_NAME);
        final List<AspectElement> views = aspect_collection.get(CyViewsElement.ASPECT_NAME);

        final Map<Long, List<NodeAttributesElement>> node_attributes_map = new HashMap<Long, List<NodeAttributesElement>>();
        final Map<Long, List<EdgeAttributesElement>> edge_attributes_map = new HashMap<Long, List<EdgeAttributesElement>>();
        final Map<Long, List<NetworkAttributesElement>> network_attributes_map = new HashMap<Long, List<NetworkAttributesElement>>();
        final Map<Long, List<HiddenAttributesElement>> hidden_attributes_map = new HashMap<Long, List<HiddenAttributesElement>>();

        
       
        if ((nodes == null) || nodes.isEmpty()) {
            throw new IOException("no nodes in input");
        }

        final Set<Long> node_ids = new HashSet<Long>();

        if ( perform_basic_integrity_checks) {
            checkNodeIds(nodes, node_ids);
        }

        final Set<Long> edge_ids = new HashSet<Long>();

        if ((edges != null) && perform_basic_integrity_checks) {
            checkEdgeIds(edges, edge_ids);
        }

        _network_suid_to_networkrelations_map = new HashMap<Long, Long>();
        _visual_element_collections = new VisualElementCollectionMap();
        _cxid_to_cynode_map = new HashMap<Long, CyNode>();
        _cxid_to_cyedge_map = new HashMap<Long, CyEdge>();

        if ((subnetworks != null) && !subnetworks.isEmpty()) {
            for (final AspectElement element : subnetworks) {
                final SubNetworkElement subnetwork_element = (SubNetworkElement) element;
                _visual_element_collections.addSubNetworkElement(subnetwork_element.getId(), subnetwork_element);
            }
        }

        // Dealing with subnetwork relations:
        Long parent_network_id = null;
        List<Long> subnetwork_ids;
        int number_of_subnetworks;
        boolean subnet_info_present;

        _view_to_subnet_map = new HashMap<Long, Long>();
        _subnet_to_views_map = new HashMap<Long, List<Long>>();
        Map<Long, String> subnet_to_subnet_name_map = new HashMap<Long, String>();
        if ((network_relations != null) && !network_relations.isEmpty()) {
            subnet_info_present = true;
            final Set<Long> subnetwork_parent_ids = NetworkRelationsElement
                    .getAllSubNetworkParentNetworkIds(network_relations);
            if ((subnetwork_parent_ids == null) || (subnetwork_parent_ids.size() < 1)) {
                throw new IOException("no subnetwork parent network id");
            }
            else if (subnetwork_parent_ids.size() > 1) {
                throw new IOException("multiple subnetwork parent network ids: " + subnetwork_parent_ids);
            }
            for (final Long s : subnetwork_parent_ids) {
                parent_network_id = s;
            }
            subnetwork_ids = NetworkRelationsElement.getSubNetworkIds(parent_network_id, network_relations);
            if (DEBUG) {
            System.out.println("subnetwork_ids: " + subnetwork_ids);
            }
            if ((subnetwork_ids == null) || subnetwork_ids.isEmpty()) {
                throw new IOException("no subnetwork ids for: " + parent_network_id);
            }
            number_of_subnetworks = subnetwork_ids.size();
            _view_to_subnet_map = makeViewToSubNetworkMap(network_relations);
            _subnet_to_views_map = makeSubNetworkToViewsMap(network_relations);
            subnet_to_subnet_name_map = makeSubNetworkToNameMap(network_relations);
            if (DEBUG) {
                System.out.println("view to subnet:");
                System.out.println(_view_to_subnet_map);
                System.out.println("subnet to views:");
                System.out.println(_subnet_to_views_map);
                System.out.println("subnet to names:");
                System.out.println(subnet_to_subnet_name_map);
            }
        }
        else {
            if (DEBUG) {
                System.out.println("no network relations");
            }
            subnet_info_present = false;
            number_of_subnetworks = 1;
            subnetwork_ids = new ArrayList<Long>();
        }

        final CySubNetwork[] subnetworks_ary = new CySubNetwork[number_of_subnetworks];
        for (int i = 0; i < number_of_subnetworks; ++i) {

            CySubNetwork sub_network;
            if (root_network != null) {
                // Root network exists
                sub_network = root_network.addSubNetwork();
            }
            else {
                sub_network = (CySubNetwork) network_factory.createNetwork();
                root_network = sub_network.getRootNetwork();
                if (!CxioUtil.isEmpty(collection_name)) {
                    root_network.getRow(root_network).set(CyNetwork.NAME, collection_name);
                }
            }
            subnetworks_ary[i] = sub_network;
        }

        processNodeAttributes(node_attributes,
                              node_attributes_map,
                              perform_basic_integrity_checks,
                              node_ids,
                              subnet_info_present);

        processEdgeAttributes(edge_attributes,
                              edge_attributes_map,
                              perform_basic_integrity_checks,
                              edge_ids,
                              subnet_info_present);

        processNetworkAttributes(network_attributes, network_attributes_map, subnet_info_present, subnetwork_ids);

        processHiddenAttributes(hidden_attributes, hidden_attributes_map);

        final List<CyNetwork> new_networks = new ArrayList<CyNetwork>();

        if (DEBUG) {
            System.out.println("number of subnetworks: " + number_of_subnetworks);
        }

        for (int i = 0; i < number_of_subnetworks; ++i) {

            final CySubNetwork sub_network = subnetworks_ary[i];

            final Long subnetwork_id = subnetwork_ids.size() > 0 ? subnetwork_ids.get(i) : sub_network.getSUID();

            if (DEBUG) {
                System.out.println("subnetwork id: " + subnetwork_id);
            }

            _network_suid_to_networkrelations_map.put(sub_network.getSUID(), subnetwork_id);
            if (DEBUG) {
                System.out.println("added " + sub_network.getSUID() + "->" + subnetwork_id
                        + " to network suid to networkrelations map");
            }

            if (!subnet_info_present) {
                _view_to_subnet_map.put(subnetwork_id, subnetwork_id);
                final List<Long> l = new ArrayList<Long>();
                l.add(subnetwork_id);
                _subnet_to_views_map.put(subnetwork_id, l);
            }

            Set<Long> nodes_in_subnet = null;
            Set<Long> edges_in_subnet = null;

            if (_visual_element_collections != null) {
                if (_visual_element_collections.getSubNetworkElement(subnetwork_id) != null) {
                    nodes_in_subnet = new HashSet<Long>(_visual_element_collections.getSubNetworkElement(subnetwork_id)
                            .getNodes());
                    edges_in_subnet = new HashSet<Long>(_visual_element_collections.getSubNetworkElement(subnetwork_id)
                            .getEdges());
                    if (DEBUG) {
                        System.out.println("nodes count in subnet: " + nodes_in_subnet.size());
                        System.out.println("edges count in subnet: " + edges_in_subnet.size());
                    }
                }

            }

            addNodes(sub_network, nodes, nodes_in_subnet, node_attributes_map, subnetwork_id, subnet_info_present);

            if (edges != null) {
                addEdges(sub_network,
                         edges,
                         edges_in_subnet,
                         edge_attributes_map,
                         subnetwork_id,
                         node_ids,
                         perform_basic_integrity_checks,
                         subnet_info_present);
            }

            final CyTable network_attribute_table = sub_network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);

            if (network_attributes_map.containsKey(subnetwork_id)) {
                addNetworkAttributeData(network_attributes_map.get(subnetwork_id), sub_network, network_attribute_table);
            }
            else if (network_attributes_map.containsKey(DEFAULT_SUBNET)) {
                if (DEBUG) {
                    System.out.println("adding network attributes lacking sub-network information");
                }
                addNetworkAttributeData(network_attributes_map.get(DEFAULT_SUBNET),
                                        sub_network,
                                        network_attribute_table);
            }

            addNetworkNames(sub_network, subnet_to_subnet_name_map, subnetwork_id, network_attribute_table);

            final CyTable hidden_attribute_table = sub_network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
            // TODO
            // addHiddenAttributeData(hidden_attributes_map.get(subnetwork_id),
            // sub_network, hidden_attribute_table);

            if (visual_properties != null) {
                _nodes_with_visual_properties = new HashSet<CyNode>();
                _edges_with_visual_properties = new HashSet<CyEdge>();
                for (final AspectElement element : visual_properties) {
                    final CyVisualPropertiesElement vpe = (CyVisualPropertiesElement) element;

                    final Long view = vpe.getView() != null ? vpe.getView() : subnetwork_id;

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
                        final List<Long> applies_to_nodes = vpe.getAppliesTo();
                        for (final Long applies_to_node : applies_to_nodes) {
                            _nodes_with_visual_properties.add(_cxid_to_cynode_map.get(applies_to_node));
                            _visual_element_collections.addNodeVisualPropertiesElement(view, _cxid_to_cynode_map
                                    .get(applies_to_node), vpe);
                        }
                    }
                    else if (vpe.getPropertiesOf().equals(VisualPropertyType.EDGES.asString())) {
                        final List<Long> applies_to_edges = vpe.getAppliesTo();
                        for (final Long applies_to_edge : applies_to_edges) {
                            _edges_with_visual_properties.add(_cxid_to_cyedge_map.get(applies_to_edge));
                            _visual_element_collections.addEdgeVisualPropertiesElement(view, _cxid_to_cyedge_map
                                    .get(applies_to_edge), vpe);
                        }
                    }
                }
            }

            if ((cartesian_layout_elements != null) && !cartesian_layout_elements.isEmpty()) {
                if (subnetwork_ids.size() > 0) {
                    addPositions(cartesian_layout_elements, _cxid_to_cynode_map);
                }
                else {
                    addPositions(cartesian_layout_elements, _cxid_to_cynode_map, subnetwork_id);
                }
            }

            new_networks.add(sub_network);

        }

        return new_networks;
    }

    public Set<CyEdge> getEdgesWithVisualProperties() {
        return _edges_with_visual_properties;
    }

    public final Map<Long, Long> getNetworkSuidToNetworkRelationsMap() {
        return _network_suid_to_networkrelations_map;
    }

    public Set<CyNode> getNodesWithVisualProperties() {
        return _nodes_with_visual_properties;
    }

    public final VisualElementCollectionMap getVisualElementCollectionMap() {
        return _visual_element_collections;
    }

    private final void addEdges(final CyNetwork network,
                                final List<AspectElement> edges,
                                final Set<Long> edges_in_subnet,
                                final Map<Long, List<EdgeAttributesElement>> edge_attributes_map,
                                final Long subnetwork_id,
                                final Set<Long> node_ids,
                                final boolean perform_basic_integrity_checks,
                                final boolean subnet_info_present) throws IOException {

        final CyTable edge_table = network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS);
        // final CyTable edge_table_default = network.getTable(CyEdge.class,
        // CyNetwork.DEFAULT_ATTRS);
        for (final AspectElement e : edges) {

            final EdgesElement edge_element = (EdgesElement) e;
            final Long edge_id = edge_element.getId();
            if ((edge_id == null)) {
                throw new IOException("edge identifiers must not be null or empty");
            }

            if ((edges_in_subnet != null) && !edges_in_subnet.contains(edge_id)) {
                continue;
            }
            CyEdge cy_edge = _cxid_to_cyedge_map.get(edge_id);
            if (cy_edge == null) {

                final Long source_id = edge_element.getSource();
                final Long target_id = edge_element.getTarget();

                if (perform_basic_integrity_checks) {
                    if ((source_id == null)) {
                        throw new IOException("source node identifiers in edges must not be null");
                    }
                    if ((target_id == null)) {
                        throw new IOException("target node identifiers in edges must not be null");
                    }
                    if (!node_ids.contains(source_id)) {
                        throw new IOException("source node with id '" + source_id + "' not present in nodes aspect");
                    }
                    if (!node_ids.contains(target_id)) {
                        throw new IOException("target node with id '" + target_id + "' not present in nodes aspect");
                    }
                }

                final CyNode source = _cxid_to_cynode_map.get(source_id);
                final CyNode target = _cxid_to_cynode_map.get(target_id);
                cy_edge = network.addEdge(source, target, true);
                if (edge_element.getInteraction() != null) {
                    // if
                    // (edge_table_default.getColumn(org.cytoscape.io.internal.cxio.Util.SHARED_INTERACTION)
                    // == null) {
                    // edge_table_default.createColumn(org.cytoscape.io.internal.cxio.Util.SHARED_INTERACTION,
                    // String.class,
                    // false);
                    // }
                    network.getRow(cy_edge).set(org.cytoscape.io.internal.cxio.CxUtil.SHARED_INTERACTION,
                                                edge_element.getInteraction());

                }
                _cxid_to_cyedge_map.put(edge_id, cy_edge);
            }
            else {
                ((CySubNetwork) network).addEdge(cy_edge);
            }
            if ((edge_attributes_map != null) && !edge_attributes_map.isEmpty()) {
                addEdgeTableData(edge_attributes_map.get(edge_id),
                                 cy_edge,
                                 network,
                                 edge_table,
                                 edge_id,
                                 subnetwork_id,
                                 subnet_info_present);
            }
        }
    }

    private final void addEdgeTableData(final List<EdgeAttributesElement> elements,
                                        final CyIdentifiable graph_object,
                                        final CyNetwork network,
                                        final CyTable table,
                                        final Long cx_edge_id,
                                        final Long subnetwork_id,
                                        final boolean subnet_info_present) throws IOException {
        if (elements == null) {
            if (DEBUG) {
                System.out.println("info: no edge attributes for cx edge " + cx_edge_id);
            }
            return;
        }
        final CyRow row = network.getRow(graph_object);
        if (row != null) {
            for (final EdgeAttributesElement e : elements) {
                if (e != null) {

                    if (!subnet_info_present || (e.getSubnetwork() == null) || subnetwork_id.equals(e.getSubnetwork())) {

                        final String name = e.getName();
                        if (name != null) {
                            if (!(name.equals(CyIdentifiable.SUID))) {
                                // New column creation:
                                if (table.getColumn(name) == null) {
                                    final Class<?> data_type = getDataType(e.getDataType());

                                    if (e.isSingleValue()) {
                                        table.createColumn(name, data_type, false);
                                    }
                                    else {
                                        table.createListColumn(name, data_type, false);
                                    }
                                }
                                final CyColumn col = table.getColumn(name);
                                row.set(name, getValue(e, col));
                            }
                        }
                    }
                }
            }
        }
    }

    private final void addHiddenAttributeData(final List<HiddenAttributesElement> elements,
                                              final CyNetwork network,
                                              final CyTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table (hidden) must not be null");
        }
        if (elements == null) {
            return;
        }
        final CyRow row = network.getRow(network);
        if (row != null) {
            for (final AbstractAttributesAspectElement e : elements) {
                addToColumn(table, row, e);
            }
        }
    }

    private final void addNetworkAttributeData(final List<NetworkAttributesElement> elements,
                                               final CyNetwork network,
                                               final CyTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table (network) must not be null");
        }
        if (elements == null) {
            return;
        }
        final CyRow row = network.getRow(network);
        if (row != null) {
            for (final AbstractAttributesAspectElement e : elements) {
                addToColumn(table, row, e);
            }
        }
    }

    private void addNetworkNames(final CySubNetwork sub_network,
                                 final Map<Long, String> subnet_to_subnet_name_map,
                                 final Long subnet_id,
                                 final CyTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table (network) must not be null");
        }
        final CyRow row = sub_network.getRow(sub_network);
        if (row != null) {
            if (table.getColumn("name") == null) {
                table.createColumn("name", String.class, false);
            }
            row.set("name", subnet_to_subnet_name_map.get(subnet_id));
        }
    }

    private final void addNodes(final CySubNetwork network,
                                final List<AspectElement> nodes,
                                final Set<Long> nodes_in_subnet,
                                final Map<Long, List<NodeAttributesElement>> node_attributes_map,
                                final Long subnetwork_id,
                                final boolean subnet_info_present) throws IOException {

        final CyTable node_table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
        final CyTable node_table_default = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
        for (final AspectElement e : nodes) {
            final NodesElement node_element = (NodesElement) e;
            final Long node_id = node_element.getId();

            if ((nodes_in_subnet != null) && !nodes_in_subnet.contains(node_id)) {
                continue;
            }
            CyNode cy_node = _cxid_to_cynode_map.get(node_id);
            if (cy_node == null) {
                cy_node = network.addNode();
                network.getRow(cy_node).set(CyNetwork.NAME, String.valueOf(node_id));
                if (node_element.getNodeRepresents() != null) {
                    if (node_table_default.getColumn(org.cytoscape.io.internal.cxio.CxUtil.REPRESENTS) == null) {
                        node_table_default.createColumn(org.cytoscape.io.internal.cxio.CxUtil.REPRESENTS,
                                                        String.class,
                                                        false);
                    }
                    network.getRow(cy_node).set(org.cytoscape.io.internal.cxio.CxUtil.REPRESENTS,
                                                node_element.getNodeRepresents());
                }
                if (node_element.getNodeName() != null) {
                    network.getRow(cy_node).set(org.cytoscape.io.internal.cxio.CxUtil.SHARED_NAME_COL,
                                                node_element.getNodeName());
                }

                _cxid_to_cynode_map.put(node_id, cy_node);
            }
            else {
                network.addNode(cy_node);
            }
            if ((node_attributes_map != null) && !node_attributes_map.isEmpty()) {
                addNodeTableData(node_attributes_map.get(node_id),
                                 cy_node,
                                 network,
                                 node_table,
                                 node_id,
                                 subnetwork_id,
                                 subnet_info_present);
            }
        }
    }

    private final void addNodeTableData(final List<NodeAttributesElement> elements,
                                        final CyIdentifiable graph_object,
                                        final CySubNetwork network,
                                        final CyTable table,
                                        final Long cx_node_id,
                                        final Long subnetwork_id,
                                        final boolean subnet_info_present) throws IOException {
        if (elements == null) {
            if (DEBUG) {
                System.out.println("info: no node attributes for cx node " + cx_node_id);
            }
            return;
        }
        final CyRow row = network.getRow(graph_object);
        if (row != null) {
            for (final NodeAttributesElement e : elements) {
                if (e != null) {

                    if (!subnet_info_present || (e.getSubnetwork() == null) || subnetwork_id.equals(e.getSubnetwork())) {
                        final String name = e.getName();
                        if (name != null) {
                            if (!(name.equals(CyIdentifiable.SUID))) {
                                // New column creation:
                                if (table.getColumn(name) == null) {
                                    final Class<?> data_type = getDataType(e.getDataType());

                                    if (e.isSingleValue()) {
                                        table.createColumn(name, data_type, false);
                                    }
                                    else {
                                        table.createListColumn(name, data_type, false);
                                    }
                                }
                                final CyColumn col = table.getColumn(name);
                                row.set(name, getValue(e, col));
                            }
                        }
                    }
                }
            }
        }
    }

    private final void addPositions(final List<AspectElement> layout, final Map<Long, CyNode> node_map) {
        for (final AspectElement e : layout) {
            final CartesianLayoutElement cle = (CartesianLayoutElement) e;
            _visual_element_collections.addCartesianLayoutElement(cle.getView(), node_map.get(cle.getNode()), cle);
        }
    }

    private final void addPositions(final List<AspectElement> layout,
                                    final Map<Long, CyNode> node_map,
                                    final Long subnetwork_id) {
        for (final AspectElement e : layout) {
            final CartesianLayoutElement cle = (CartesianLayoutElement) e;
            _visual_element_collections.addCartesianLayoutElement(subnetwork_id, node_map.get(cle.getNode()), cle);
        }
    }

    private void addToColumn(final CyTable table, final CyRow row, final AbstractAttributesAspectElement e) {
        if (e != null) {
            final String name = e.getName();
            if (name != null) {
                if (!(name.equals(CyIdentifiable.SUID))) {
                    // New column creation:
                    if (table.getColumn(name) == null) {
                        final Class<?> data_type = getDataType(e.getDataType());
                        if (e.isSingleValue()) {
                            table.createColumn(name, data_type, false);
                        }
                        else {
                            table.createListColumn(name, data_type, false);
                        }
                    }
                    final CyColumn col = table.getColumn(name);
                    row.set(name, getValue(e, col));
                }
            }
        }
    }

    private Class<?> getDataType(final ATTRIBUTE_DATA_TYPE type) {
        switch (type) {
        case STRING:
        case LIST_OF_STRING:
            return String.class;
        case BOOLEAN:
        case LIST_OF_BOOLEAN:
            return Boolean.class;
        case DOUBLE:
        case LIST_OF_DOUBLE:
            return Double.class;
        case FLOAT:
        case LIST_OF_FLOAT:
            return Float.class;
        case INTEGER:
        case LIST_OF_INTEGER:
            return Integer.class;
        case LONG:
        case LIST_OF_LONG:
            return Long.class;
        case SHORT:
        case LIST_OF_SHORT:
            return Integer.class;
        default:
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

    private void processHiddenAttributes(final List<AspectElement> hidden_attributes,
                                         final Map<Long, List<HiddenAttributesElement>> hidden_attributes_map) {
        if (hidden_attributes != null) {
            for (final AspectElement e : hidden_attributes) {
                final HiddenAttributesElement hae = (HiddenAttributesElement) e;
                final Long subnet = hae.getSubnetwork();
                if (!hidden_attributes_map.containsKey(subnet)) {
                    hidden_attributes_map.put(subnet, new ArrayList<HiddenAttributesElement>());
                }
                hidden_attributes_map.get(subnet).add(hae);

            }
        }
    }

    private void processNetworkAttributes(final List<AspectElement> network_attributes,
                                          final Map<Long, List<NetworkAttributesElement>> network_attributes_map,
                                          final boolean subnet_info_present,
                                          final List<Long> subnetworks_ids) throws IOException {

        if (network_attributes != null) {
            for (final AspectElement e : network_attributes) {
                final NetworkAttributesElement nae = (NetworkAttributesElement) e;
                if (STRICT) {
                    if (!subnet_info_present && (nae.getSubnetwork() != null)) {
                        throw new IOException("no sub-network information is present, but node attribute with sub-network data found");
                    }
                }
                Long subnet = DEFAULT_SUBNET;
                if (subnet_info_present) {
                    if (nae.getSubnetwork() != null) {
                        subnet = nae.getSubnetwork();
                    }
                    else if (subnetworks_ids.size() == 1) {
                        subnet = subnetworks_ids.get(0);
                    }
                }

                if (!network_attributes_map.containsKey(subnet)) {
                    network_attributes_map.put(subnet, new ArrayList<NetworkAttributesElement>());
                }
                network_attributes_map.get(subnet).add(nae);
            }
        }
    }

    private final static void checkEdgeIds(final List<AspectElement> edges, final Set<Long> edge_ids)
            throws IOException {
        for (final AspectElement edge : edges) {
            final Long edge_id = ((EdgesElement) edge).getId();
            if ((edge_id == null)) {
                throw new IOException("edge identifiers must not be null");
            }
            if (edge_ids.contains(edge_id)) {
                throw new IOException("edge identifier '" + edge_id + "' is not unique");
            }
            edge_ids.add(edge_id);
        }
    }

    private final static void checkNodeIds(final List<AspectElement> nodes, final Set<Long> node_ids)
            throws IOException {
        for (final AspectElement node : nodes) {
            final Long node_id = ((NodesElement) node).getId();
            if ((node_id == null)) {
                throw new IOException("node identifiers must not be null ");
            }
            if (node_ids.contains(node_id)) {
                throw new IOException("node identifier '" + node_id + "' is not unique");
            }
            node_ids.add(node_id);
        }
    }

    private final static Object getValue(final AbstractAttributesAspectElement e, final CyColumn column) {
        if (e.isSingleValue()) {
            return parseValue(e.getValue(), column.getType());
        }
        else {
            final List<Object> list = new ArrayList<Object>();
            for (final String value : e.getValues()) {
                list.add(parseValue(value, column.getListElementType()));
            }
            return list;
        }
    }

    private final static Object parseValue(final String value, final Class<?> type) {
        if (type == String.class) {
            return value;
        }
        else if (type == Long.class) {
            return Long.valueOf(value);
        }
        else if (type == Integer.class) {
            return Integer.valueOf(value);
        }
        else if (type == Short.class) {
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
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "' for value '" + value
                                               + "'");
        }
    }

    private final static void processEdgeAttributes(final List<AspectElement> edge_attributes,
                                                    final Map<Long, List<EdgeAttributesElement>> edge_attributes_map,
                                                    final boolean perform_basic_integrity_checks,
                                                    final Set<Long> edge_ids,
                                                    final boolean subnet_info_present) throws IOException {
        if (edge_attributes != null) {
            for (final AspectElement e : edge_attributes) {
                final EdgeAttributesElement eae = (EdgeAttributesElement) e;
                if (STRICT) {
                    if (!subnet_info_present && (eae.getSubnetwork() != null)) {
                        throw new IOException("no sub-network information is present, but edge attribute with sub-network data found");
                    }
                }
                final List<Long> pos = eae.getPropertyOf();
                for (final Long po : pos) {
                    if (!edge_attributes_map.containsKey(po)) {
                        if (perform_basic_integrity_checks) {
                            if ((po == null)) {
                                throw new IOException("edge identifiers must not be null in edge attributes");
                            }
                            if (!edge_ids.contains(po)) {
                                throw new IOException("edge with id '" + po + "' not present in edges aspect");
                            }
                        }
                        edge_attributes_map.put(po, new ArrayList<EdgeAttributesElement>());
                    }
                    edge_attributes_map.get(po).add(eae);
                }
            }
        }
    }

    private final static void processNodeAttributes(final List<AspectElement> node_attributes,
                                                    final Map<Long, List<NodeAttributesElement>> node_attributes_map,
                                                    final boolean perform_basic_integrity_checks,
                                                    final Set<Long> node_ids,
                                                    final boolean subnet_info_present) throws IOException {
        if (node_attributes != null) {
            for (final AspectElement e : node_attributes) {
                final NodeAttributesElement nae = (NodeAttributesElement) e;
                if (STRICT) {
                    if (!subnet_info_present && (nae.getSubnetwork() != null)) {
                        throw new IOException("no sub-network information is present, but node attribute with sub-network data found");
                    }
                }
                final List<Long> pos = nae.getPropertyOf();
                for (final Long po : pos) {
                    if (perform_basic_integrity_checks) {
                        if ((po == null)) {
                            throw new IOException("node identifiers must not be null in node attributes");
                        }
                        if (!node_ids.contains(po)) {
                            throw new IOException("node with id '" + po + "' not present in nodes aspect");
                        }
                    }
                    if (!node_attributes_map.containsKey(po)) {
                        node_attributes_map.put(po, new ArrayList<NodeAttributesElement>());
                    }
                    node_attributes_map.get(po).add(nae);
                }
            }
        }
    }

}
