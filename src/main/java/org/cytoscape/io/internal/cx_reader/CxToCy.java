package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.AbstractAttributesAspectElement;
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
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.util.CxioUtil;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.Settings;
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
import org.ndexbio.model.cx.NiceCXNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public final class CxToCy {

	private static final Logger logger = LoggerFactory.getLogger(CxToCy.class);

	private static final Long DEFAULT_SUBNET = Long.valueOf(-Long.MAX_VALUE);

	private Set<CyNode> _nodes_with_visual_properties;
	private Set<CyEdge> _edges_with_visual_properties;
	private VisualElementCollectionMap _visual_element_collections;
	private Map<Long, Long> _network_suid_to_networkrelations_map;
	private Map<Long, CyNode> _cxid_to_cynode_map;

	public Map<Long, CyNode> get_cxid_to_cynode_map() {
		return _cxid_to_cynode_map;
	}

	public Map<Long, CyEdge> get_cxid_to_cyedge_map() {
		return _cxid_to_cyedge_map;
	}

	private Map<Long, CyEdge> _cxid_to_cyedge_map;
	private Map<Long, Long> _view_to_subnet_map;
	private Map<Long, List<Long>> _subnet_to_views_map;

	public final Map<Long, Long> getViewToSubNetworkMap() {
		return _view_to_subnet_map;
	}

	public final Map<Long, List<Long>> getSubNetworkToViewsMap() {
		return _subnet_to_views_map;
	}

	public final static Map<Long, Long> makeViewToSubNetworkMap(final Collection<AspectElement> network_relations) {
		final Map<Long, Long> view_to_subnet_map = new TreeMap<>();
		for (final AspectElement e : network_relations) {
			final NetworkRelationsElement nwe = (NetworkRelationsElement) e;
			if (nwe.getRelationship() == NetworkRelationsElement.TYPE_VIEW) {
				view_to_subnet_map.put(nwe.getChild(), nwe.getParent());
			}
		}
		return view_to_subnet_map;
	}

	public final static Map<Long, List<Long>> makeSubNetworkToViewsMap(
			final Collection<AspectElement> network_relations) {
		final Map<Long, List<Long>> subnet_to_views_map = new TreeMap<>();
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

	public final static Map<Long, String> makeSubNetworkToNameMap(final Collection<AspectElement> network_relations) {
		final Map<Long, String> m = new HashMap<>();
		for (final AspectElement e : network_relations) {
			final NetworkRelationsElement nwe = (NetworkRelationsElement) e;
			if (nwe.getRelationship() == NetworkRelationsElement.TYPE_SUBNETWORK) {
				m.put(nwe.getChild(), nwe.getChildName());
			}
		}
		return m;
	}

	/**
	 * return the set of parent network ids from the cx Network
	 * 
	 * @param networks_relations
	 * @return
	 */
	private final static Set<Long> getAllSubNetworkParentNetworkIds(
			final Collection<AspectElement> networks_relations) {
		final Set<Long> parents = new HashSet<>();
		for (final AspectElement e : networks_relations) {
			final NetworkRelationsElement nwe = (NetworkRelationsElement) e;
			if (nwe.getRelationship() == NetworkRelationsElement.TYPE_SUBNETWORK) {
				parents.add(nwe.getParent());
			}
		}
		return parents;
	}

	private final static List<Long> getSubNetworkIds(final Collection<NetworkRelationsElement> networks_relations) {
		final List<Long> subnets = new ArrayList<>();
		for (final NetworkRelationsElement nwe : networks_relations) {
			if (nwe.getRelationship() == NetworkRelationsElement.TYPE_SUBNETWORK) {
				subnets.add(nwe.getChild());
			}
		}
		return subnets;
	}

	private final void createColumn(final NetworkAttributesElement el, final CyTable table) {
		final String colName = el.getName();
		if (table.getColumn(colName) == null) {
			if (el.isSingleValue()) {
				table.createColumn(colName, getDataType(el.getDataType()), false);
			} else {
				table.createListColumn(colName, getDataType(el.getDataType()), false);
			}
		}
	}

	private final void addNetworkCollectionAttributes(final Collection<NetworkAttributesElement> netAttr,
			final CyRootNetwork root) {

		// If network attribute is not available, simply skip it.
		if (netAttr == null) {
			return;
		}

		// Get default table and the row for the Collection
		final CyTable table = root.getDefaultNetworkTable();
		final CyRow row = table.getRow(root.getSUID());

		final Set<NetworkAttributesElement> rootAttrs = netAttr.stream().filter(attr -> attr.getSubnetwork() == null)
				.collect(Collectors.toSet());

		// Create new columns if necessary
		rootAttrs.stream().forEach(rootAttr -> createColumn(rootAttr, table));

		// Assign actual collection attributes
		rootAttrs.stream().forEach(rootAttr -> addToColumn(table, row, rootAttr));

	}

	public final List<CyNetwork> createNetwork(final NiceCXNetwork niceCX, CyRootNetwork root_network,
			final CyNetworkFactory network_factory, final String collection_name) throws IOException {

		final Map<Long, List<CyTableColumnElement>> subnetwork_to_col_labels_map = new HashMap<>();

		if (niceCX.getNodes().isEmpty()) {
			throw new IOException("no nodes in input");
		}

		_network_suid_to_networkrelations_map = new HashMap<>();
		_visual_element_collections = new VisualElementCollectionMap();
		_cxid_to_cynode_map = new HashMap<>();
		_cxid_to_cyedge_map = new HashMap<>();

		boolean isCollection = false;

		if (niceCX.getOpaqueAspectTable().get(SubNetworkElement.ASPECT_NAME) != null) {
			isCollection = true;
			for (final AspectElement element : niceCX.getOpaqueAspectTable().get(SubNetworkElement.ASPECT_NAME)) {
				final SubNetworkElement subnetwork_element = (SubNetworkElement) element;
				_visual_element_collections.addSubNetworkElement(subnetwork_element.getId(), subnetwork_element);
			}
		}

		// Dealing with subnetwork relations:
		Long parent_network_id = null;
		List<Long> subnetwork_ids;
		int number_of_subnetworks;

		_view_to_subnet_map = new HashMap<>();
		_subnet_to_views_map = new HashMap<>();
		Map<Long, String> subnet_to_subnet_name_map = new HashMap<>();
		if (niceCX.getOpaqueAspectTable().containsKey(NetworkRelationsElement.ASPECT_NAME)) { // network collection
			Collection<AspectElement> network_relations = niceCX.getOpaqueAspectTable()
					.get(NetworkRelationsElement.ASPECT_NAME);
			final Set<Long> subnetwork_parent_ids = getAllSubNetworkParentNetworkIds(network_relations);
			if (subnetwork_parent_ids == null || subnetwork_parent_ids.size() < 1) {
				throw new IOException("no subnetwork parent network id");
			} else if (subnetwork_parent_ids.size() > 1) {
				throw new IOException("multiple subnetwork parent network ids: " + subnetwork_parent_ids);
			}
			for (final Long s : subnetwork_parent_ids) {
				parent_network_id = s;
			}
			if (Settings.INSTANCE.isDebug()) {
				System.out.println("parent_network_id: " + parent_network_id);
			}

			subnetwork_ids = getSubNetworkIds((Collection) network_relations);
			if (Settings.INSTANCE.isDebug()) {
				System.out.println("subnetwork_ids: " + subnetwork_ids);
			}
			if (subnetwork_ids == null || subnetwork_ids.isEmpty()) {
				throw new IOException("no subnetwork ids for: " + parent_network_id);
			}
			number_of_subnetworks = subnetwork_ids.size();
			_view_to_subnet_map = makeViewToSubNetworkMap(network_relations);
			_subnet_to_views_map = makeSubNetworkToViewsMap(network_relations);
			subnet_to_subnet_name_map = makeSubNetworkToNameMap(network_relations);
			if (Settings.INSTANCE.isDebug()) {
				System.out.println("view to subnet:");
				System.out.println(_view_to_subnet_map);
				System.out.println("subnet to views:");
				System.out.println(_subnet_to_views_map);
				System.out.println("subnet to names:");
				System.out.println(subnet_to_subnet_name_map);
			}
		} else {
			if (Settings.INSTANCE.isDebug()) {
				System.out.println("no network relations");
			}
			number_of_subnetworks = 1;
			subnetwork_ids = new ArrayList<>(1);
		}

		final CySubNetwork[] subnetworks_ary = new CySubNetwork[number_of_subnetworks];
		for (int i = 0; i < number_of_subnetworks; ++i) {

			CySubNetwork sub_network;
			if (root_network != null) {
				// Root network exists
				sub_network = root_network.addSubNetwork();
			} else {
				sub_network = (CySubNetwork) network_factory.createNetwork();
				root_network = sub_network.getRootNetwork();
				if (!CxioUtil.isEmpty(collection_name)) {

					root_network.getRow(root_network).set(CyNetwork.NAME, collection_name);
				}
			}
			subnetworks_ary[i] = sub_network;
		}

		if (isCollection)
			addNetworkCollectionAttributes(niceCX.getNetworkAttributes(), root_network);

		processColumnLabels(niceCX.getOpaqueAspectTable().get(CyTableColumnElement.ASPECT_NAME),
				subnetwork_to_col_labels_map);

		/*
		 * processNodeAttributes(node_attributes, node_attributes_map,
		 * perform_basic_integrity_checks, node_ids, subnet_info_present);
		 */

		/*
		 * processEdgeAttributes(edge_attributes, edge_attributes_map,
		 * perform_basic_integrity_checks, edge_ids, subnet_info_present);
		 */

		// subnetwork id -> network attribute list
		final Map<Long, Collection<NetworkAttributesElement>> network_attributes_map = processNetworkAttributes(
				niceCX.getNetworkAttributes(), isCollection, subnetwork_ids);
		// System.out.println(network_attributes_map);

		//
		final Map<Long, List<HiddenAttributesElement>> hidden_attributes_map = new HashMap<>();

		processHiddenAttributes(niceCX.getOpaqueAspectTable().get(HiddenAttributesElement.ASPECT_NAME),
				hidden_attributes_map);

		final List<CyNetwork> new_networks = new ArrayList<>();

		if (Settings.INSTANCE.isDebug()) {
			System.out.println("number of subnetworks: " + number_of_subnetworks);
		}

		for (int i = 0; i < number_of_subnetworks; ++i) {

			final CySubNetwork sub_network = subnetworks_ary[i];

			final Long subnetwork_id = subnetwork_ids.size() > 0 ? subnetwork_ids.get(i) : sub_network.getSUID();

			_network_suid_to_networkrelations_map.put(sub_network.getSUID(), subnetwork_id);
			if (Settings.INSTANCE.isDebug()) {
				System.out.println("network suid->network-relations: " + sub_network.getSUID() + "->" + subnetwork_id);
			}

			if (!isCollection) {
				_view_to_subnet_map.put(subnetwork_id, subnetwork_id);
				final List<Long> l = new ArrayList<>();
				l.add(subnetwork_id);
				_subnet_to_views_map.put(subnetwork_id, l);
			}

			Set<Long> nodes_in_subnet = null;
			Set<Long> edges_in_subnet = null;

			if (_visual_element_collections != null) {
				if (_visual_element_collections.getSubNetworkElement(subnetwork_id) != null) {
					nodes_in_subnet = new HashSet<>(
							_visual_element_collections.getSubNetworkElement(subnetwork_id).getNodes());
					edges_in_subnet = new HashSet<>(
							_visual_element_collections.getSubNetworkElement(subnetwork_id).getEdges());
					if (Settings.INSTANCE.isDebug()) {
						System.out.println(
								"sub-network nodes/edges: " + nodes_in_subnet.size() + "/" + edges_in_subnet.size());
					}
				}
			}

			addNodes(sub_network, niceCX, nodes_in_subnet, subnetwork_id, isCollection);

			addEdges(sub_network, niceCX, edges_in_subnet, subnetwork_id,
					// node_ids,
					// perform_basic_integrity_checks,
					isCollection);

			addColumns(sub_network, subnetwork_to_col_labels_map, subnetwork_id);

			if (network_attributes_map.containsKey(subnetwork_id)) {
				addNetworkAttributeData(network_attributes_map.get(subnetwork_id), sub_network,
						sub_network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));
			} else if (network_attributes_map.containsKey(DEFAULT_SUBNET)) {
				// This is the root network!
				if (Settings.INSTANCE.isDebug()) {
					System.out.println("adding network attributes lacking sub-network information");
				}

				addNetworkAttributeData(network_attributes_map.get(DEFAULT_SUBNET), sub_network,
						sub_network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS));
			}
			if (subnet_to_subnet_name_map != null && !subnet_to_subnet_name_map.isEmpty()) {
				addNetworkNames(sub_network, subnet_to_subnet_name_map, subnetwork_id,
						sub_network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS));
			}

			// final Map<Long, List<CyGroupsElement>> subnet_to_groups_map = new
			// HashMap<>();

			// processGroups(niceCX.getOpaqueAspectTable().get(CyGroupsElement.ASPECT_NAME),
			// subnet_to_groups_map,subnetwork_id);

			Collection<AspectElement> groups_elements = niceCX.getOpaqueAspectTable().get(CyGroupsElement.ASPECT_NAME);

			// if (group_factory != null && groups_elements != null) {
			// addGroups(group_factory,
			// groups_elements,
			// sub_network,
			// subnetwork_id);
			// }

			final CyTable hidden_attribute_table = sub_network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
			// TODO
			// addHiddenAttributeData(hidden_attributes_map.get(subnetwork_id),
			// sub_network, hidden_attribute_table);

			if (niceCX.getOpaqueAspectTable().get(CyVisualPropertiesElement.ASPECT_NAME) != null) {
				_nodes_with_visual_properties = new HashSet<>();
				_edges_with_visual_properties = new HashSet<>();
				for (final AspectElement element : niceCX.getOpaqueAspectTable()
						.get(CyVisualPropertiesElement.ASPECT_NAME)) {
					final CyVisualPropertiesElement vpe = (CyVisualPropertiesElement) element;

					// final Long view = vpe.getView() != null ? vpe.getView() : subnetwork_id;

					final Long view;
					if (vpe.getView() != null)
						view = vpe.getView();
					else {
						view = subnetwork_id;
						vpe.setView(view);
					}

					if (vpe.getProperties_of().equals(VisualPropertyType.NETWORK.asString())) {
						_visual_element_collections.addNetworkVisualPropertiesElement(view, vpe);
					} else if (vpe.getProperties_of().equals(VisualPropertyType.NODES_DEFAULT.asString())) {
						_visual_element_collections.addNodesDefaultVisualPropertiesElement(view, vpe);
					} else if (vpe.getProperties_of().equals(VisualPropertyType.EDGES_DEFAULT.asString())) {
						_visual_element_collections.addEdgesDefaultVisualPropertiesElement(view, vpe);
					} else if (vpe.getProperties_of().equals(VisualPropertyType.NODES.asString())) {
						Long applies_to_node = vpe.getApplies_to();
						_nodes_with_visual_properties.add(_cxid_to_cynode_map.get(applies_to_node));
						_visual_element_collections.addNodeVisualPropertiesElement(view,
								_cxid_to_cynode_map.get(applies_to_node), vpe);

					} else if (vpe.getProperties_of().equals(VisualPropertyType.EDGES.asString())) {
						Long applies_to_edge = vpe.getApplies_to();
						_edges_with_visual_properties.add(_cxid_to_cyedge_map.get(applies_to_edge));
						_visual_element_collections.addEdgeVisualPropertiesElement(view,
								_cxid_to_cyedge_map.get(applies_to_edge), vpe);

					}
				}
			}

			if (niceCX.getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME) != null) {
				Map<Long, Collection<AspectElement>> cartesian_layout_elements = niceCX
						.getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME);
				if (subnetwork_ids.size() > 0) {
					addPositions(cartesian_layout_elements, _cxid_to_cynode_map);
				} else {
					addPositions(cartesian_layout_elements, _cxid_to_cynode_map, subnetwork_id);
				}
			}

			new_networks.add(sub_network);

		}

		return new_networks;
	}

	private void addGroups(final CyGroupFactory group_factory, final Collection<AspectElement> groupElements,
			final CySubNetwork sub_network, final Long subnetwork_id) {

		/*
		 * if (_subnet_to_views_map.containsKey(subnetwork_id)) { final List<Long> vs =
		 * _subnet_to_views_map.get(subnetwork_id); for (final Long v : vs) {
		 * 
		 * final List<CyGroupsElement> g = subnet_to_groups_map.get(v);
		 */
		for (final AspectElement a : groupElements) {
			CyGroupsElement ge = (CyGroupsElement) a;
			if (ge.getSubNet() == null || ge.getSubNet().equals(subnetwork_id)) {
				final List<CyNode> nodes_for_group = new ArrayList<>();

				for (final Long nod : ge.getNodes()) {
					nodes_for_group.add(_cxid_to_cynode_map.get(nod));
				}
				final List<CyEdge> edges_for_group = new ArrayList<>();
				for (final Long ed : ge.getInternalEdges()) {
					edges_for_group.add(_cxid_to_cyedge_map.get(ed));
				}
				for (final Long ed : ge.getExternalEdges()) {
					edges_for_group.add(_cxid_to_cyedge_map.get(ed));
				}
				CyNode grpNode = _cxid_to_cynode_map.get(ge.getGroupId());
				final CyRow row = sub_network.getRow(grpNode, CyNetwork.DEFAULT_ATTRS);
				row.set(CxUtil.SHARED_NAME_COL, ge.getName());
				CyGroup grp = group_factory.createGroup(sub_network, grpNode, nodes_for_group, edges_for_group, true);
				grp.expand(sub_network);
			}
		}
		// }
		// }
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

	private final void addEdges(final CyNetwork network, final NiceCXNetwork niceCX, final Set<Long> edges_in_subnet,
			final Long subnetwork_id,
			// final Set<Long> node_ids,
			// final boolean perform_basic_integrity_checks,
			final boolean subnet_info_present) {

		for (final EdgesElement edge_element : niceCX.getEdges().values()) {

			final Long edge_id = edge_element.getId();

			if (edges_in_subnet != null && !edges_in_subnet.contains(edge_id)) {
				continue;
			}
			CyEdge cy_edge = _cxid_to_cyedge_map.get(edge_id);
			if (cy_edge == null) {

				final Long source_id = edge_element.getSource();
				final Long target_id = edge_element.getTarget();

				final CyNode source = _cxid_to_cynode_map.get(source_id);
				final CyNode target = _cxid_to_cynode_map.get(target_id);
				cy_edge = network.addEdge(source, target, true);
				if (edge_element.getInteraction() != null) {
					network.getRow(cy_edge).set(CxUtil.SHARED_INTERACTION, edge_element.getInteraction());

				}
				_cxid_to_cyedge_map.put(edge_id, cy_edge);
			} else {
				((CySubNetwork) network).addEdge(cy_edge);
			}
			if (!niceCX.getEdgeAttributes().isEmpty()) {
				addEdgeTableData(niceCX.getEdgeAttributes().get(edge_id), cy_edge, network, subnetwork_id,
						subnet_info_present);
			}
		}
	}

	private final void addEdgeTableData(final Collection<EdgeAttributesElement> elements,
			final CyIdentifiable graph_object, final CyNetwork network, final Long subnetwork_id,
			final boolean subnet_info_present) {
		if (elements == null) {
			return;
		}
		final CyTable table_default = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		final CyTable table_local = network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS);
		final CyRow row = network.getRow(graph_object);
		if (row != null) {
			for (final EdgeAttributesElement e : elements) {
				if (e != null) {
					CyTable my_table;
					if (!subnet_info_present || e.getSubnetwork() == null || subnetwork_id.equals(e.getSubnetwork())) {
						if (!subnet_info_present || e.getSubnetwork() == null) {
							my_table = table_default;
						} else {
							my_table = table_local;
						}
						final String name = e.getName();
						if (name != null) {
							if (!name.equals(CyIdentifiable.SUID)) {
								// New column creation:
								if (my_table.getColumn(name) == null) {
									final Class<?> data_type = getDataType(e.getDataType());

									if (e.isSingleValue()) {
										my_table.createColumn(name, data_type, false);
									} else {
										my_table.createListColumn(name, data_type, false);
									}
								}
								final CyColumn col = my_table.getColumn(name);
								row.set(name, getValue(e, col));
							}
						}
					}
				}
			}
		}
	}

	/*
	 * private final void addHiddenAttributeData(final List<HiddenAttributesElement>
	 * elements, final CyNetwork network, final CyTable table) { if (table == null)
	 * { throw new IllegalArgumentException("table (hidden) must not be null"); } if
	 * (elements == null) { return; } final CyRow row = network.getRow(network); if
	 * (row != null) { for (final AbstractAttributesAspectElement e : elements) {
	 * addToColumn(table, row, e); } } }
	 */
	private final void addNetworkAttributeData(final Collection<NetworkAttributesElement> elements,
			final CyNetwork network, final CyTable table) {
		if (table == null) {
			throw new IllegalArgumentException("table (network) must not be null");
		}
		if (elements == null) {
			return;
		}
		final CyRow row = network.getRow(network);
		if (row == null) {
			return;
		}

		elements.stream().forEach(e -> addToColumn(table, row, e));

	}

	private void addNetworkNames(final CySubNetwork sub_network, final Map<Long, String> subnet_to_subnet_name_map,
			final Long subnet_id, final CyTable table) {
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

	private final void addColumns(final CySubNetwork network,
			final Map<Long, List<CyTableColumnElement>> subnetwork_to_col_labels_map, final Long subnetwork_id) {
		if (subnetwork_to_col_labels_map != null) {
			final List<CyTableColumnElement> col_labels = subnetwork_to_col_labels_map.get(subnetwork_id);
			if (col_labels != null) {
				for (final CyTableColumnElement col_label : col_labels) {
					if (col_label != null) {
						final String name = col_label.getName();
						if (name != null && !name.equals(CxUtil.SUID)) {
							final ATTRIBUTE_DATA_TYPE dt = col_label.getDataType();
							final boolean is_single = isSingle(dt);
							final Class<?> data_type = getDataType(dt);
							CyTable table = null;
							if (col_label.getAppliesTo().equals("node_table")) {
								table = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
							} else if (col_label.getAppliesTo().equals("edge_table")) {
								table = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
							} else if (col_label.getAppliesTo().equals("network_table")) {
								table = network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS);
							}
							createColumn(is_single, data_type, name, table);
						}
					}
				}
			}
		}
	}

	private static void createColumn(final boolean is_single, final Class<?> data_type, final String name,
			final CyTable table) {
		if (table != null) {
			if (table.getColumn(name) == null) {
				try {
					if (is_single) {
						table.createColumn(name, data_type, false);
					} else {
						table.createListColumn(name, data_type, false);
					}
				} catch (final IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	private static boolean isSingle(final ATTRIBUTE_DATA_TYPE dt) {

		return (dt == ATTRIBUTE_DATA_TYPE.BOOLEAN || dt == ATTRIBUTE_DATA_TYPE.DOUBLE
				|| dt == ATTRIBUTE_DATA_TYPE.INTEGER || dt == ATTRIBUTE_DATA_TYPE.LONG
				|| dt == ATTRIBUTE_DATA_TYPE.STRING);
	}

	private final void addNodes(final CySubNetwork network, final NiceCXNetwork niceCX, final Set<Long> nodes_in_subnet,
			final Long subnetwork_id, final boolean subnet_info_present) {

		final CyTable node_table_default = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);

		for (final NodesElement node_element : niceCX.getNodes().values()) {
			final Long node_id = node_element.getId();

			if (nodes_in_subnet != null && !nodes_in_subnet.contains(node_id)) {
				continue;
			}
			CyNode cy_node = _cxid_to_cynode_map.get(node_id);
			if (cy_node == null) {
				cy_node = network.addNode();
				// network.getRow(cy_node).set(CyNetwork.NAME, String.valueOf(node_id));
				if (node_element.getNodeRepresents() != null) {
					if (node_table_default.getColumn(CxUtil.REPRESENTS) == null) {
						node_table_default.createColumn(CxUtil.REPRESENTS, String.class, false);
					}
					network.getRow(cy_node).set(CxUtil.REPRESENTS, node_element.getNodeRepresents());
				}
				if (node_element.getNodeName() != null) {
					network.getRow(cy_node).set(CxUtil.SHARED_NAME_COL, node_element.getNodeName());
					network.getRow(cy_node).set(CxUtil.NAME_COL, node_element.getNodeName());
				}

				_cxid_to_cynode_map.put(node_id, cy_node);
			} else {
				network.addNode(cy_node);
			}
			if (!niceCX.getNodeAttributes().isEmpty()) {
				addNodeTableData(niceCX.getNodeAttributes().get(node_id), cy_node, network, subnetwork_id,
						subnet_info_present);
			}
		}
	}

	private final void addNodeTableData(final Collection<NodeAttributesElement> elements,
			final CyIdentifiable graph_object, final CySubNetwork network, final Long subnetwork_id,
			final boolean subnet_info_present) {
		if (elements == null) {
			return;
		}
		final CyTable table_default = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		final CyTable table_local = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		final CyRow row = network.getRow(graph_object);
		if (row != null) {
			for (final NodeAttributesElement e : elements) {
				if (e != null) {
					CyTable my_table;
					if (!subnet_info_present || e.getSubnetwork() == null || subnetwork_id.equals(e.getSubnetwork())) {
						if (!subnet_info_present || e.getSubnetwork() == null) {
							my_table = table_default;
						} else {
							my_table = table_local;
						}
						final String name = e.getName();
						if (name != null) {
							if (!name.equals(CyIdentifiable.SUID)) {
								// New column creation:
								if (my_table.getColumn(name) == null) {
									final Class<?> data_type = getDataType(e.getDataType());

									if (e.isSingleValue()) {
										my_table.createColumn(name, data_type, false);

									} else {
										my_table.createListColumn(name, data_type, false);
									}
								}
								final CyColumn col = my_table.getColumn(name);
								row.set(name, getValue(e, col));
							}
						}
					}
				}
			}
		}
	}

	private final void addPositions(final Map<Long, Collection<AspectElement>> layout,
			final Map<Long, CyNode> node_map) {
		for (Collection<AspectElement> ec : layout.values()) {
			for (final AspectElement e : ec) {
				final CartesianLayoutElement cle = (CartesianLayoutElement) e;
				_visual_element_collections.addCartesianLayoutElement(cle.getView(), node_map.get(cle.getNode()), cle);
			}
		}
	}

	private final void addPositions(final Map<Long, Collection<AspectElement>> layout, final Map<Long, CyNode> node_map,
			final Long subnetwork_id) {
		for (Collection<AspectElement> ec : layout.values()) {
			for (final AspectElement e : ec) {
				final CartesianLayoutElement cle = (CartesianLayoutElement) e;
				_visual_element_collections.addCartesianLayoutElement(subnetwork_id, node_map.get(cle.getNode()), cle);
			}
		}
	}

	private final void addToColumn(final CyTable table, final CyRow row, final AbstractAttributesAspectElement e) {
		if (e == null) {
			return;
		}

		final String name = e.getName();
		if (name == null){
			return;
		}

		if ((!Settings.INSTANCE.isIgnoreSuidColumn() || !name.equals(CxUtil.SUID))
				&& (!Settings.INSTANCE.isIgnoreSelectedColumn() || !name.equals(CxUtil.SELECTED))) {

			final String type = e.getDataType().toString();
			final Class<?> data_type = getDataType(e.getDataType());

			// New column creation:
			CyColumn col = table.getColumn(name);
			if (col == null) {
				final boolean isSingle = e.isSingleValue();
				if (isSingle && type.startsWith("list_of")) {
					// Invalid entry.
					logger.warn("Invalid entry found: " + e.toString());
					return;
				}
				if (e.isSingleValue()) {
					table.createColumn(name, data_type, false);
				} else {
					table.createListColumn(name, data_type, false);
				}

				col = table.getColumn(name);
				if (col == null) {
					// Invalid entry.
					logger.warn("Failed to create table column");
					return;
				}
			}

			if (col.getListElementType() != null) {
				if (e.isSingleValue()) {
					// Contradiction, i.e., invalid CX element.
					logger.warn("Invalid entry.  Not a list: " + e.toString());
					return;
				}
			}

			final Object val = getValue(e, col);
			try {
				row.set(name, val);
			} catch (Exception ex) {
				logger.warn("Invalid element found: " + e, ex);
			}
		}
	}

	private static Class<?> getDataType(final ATTRIBUTE_DATA_TYPE type) {
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
		case INTEGER:
		case LIST_OF_INTEGER:
			return Integer.class;
		case LONG:
		case LIST_OF_LONG:
			return Long.class;
		default:
			throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
		}
	}

	private static void processHiddenAttributes(final Collection<AspectElement> hidden_attributes,
			final Map<Long, List<HiddenAttributesElement>> hidden_attributes_map) {
		if (hidden_attributes != null) {
			for (final AspectElement e : hidden_attributes) {
				final HiddenAttributesElement hae = (HiddenAttributesElement) e;
				Long subnet = hae.getSubnetwork();
				if (subnet == null)
					subnet = DEFAULT_SUBNET;

				if (!hidden_attributes_map.containsKey(subnet)) {
					hidden_attributes_map.put(subnet, new ArrayList<HiddenAttributesElement>());
				}
				hidden_attributes_map.get(subnet).add(hae);

			}
		}
	}

	private static Map<Long, Collection<NetworkAttributesElement>> processNetworkAttributes(
			final Collection<NetworkAttributesElement> network_attributes, final boolean isCollection,
			final List<Long> subnetworks_ids) {

		Map<Long, Collection<NetworkAttributesElement>> network_attributes_map = new HashMap<>();

		if (network_attributes == null) {
			return network_attributes_map;
		}

		if (!isCollection) {
			network_attributes_map.put(DEFAULT_SUBNET, network_attributes);
			return network_attributes_map;
		}

		for (final NetworkAttributesElement nae : network_attributes) {

			Long subnet = nae.getSubnetwork();
			if (subnet == null)
				subnet = DEFAULT_SUBNET;
			/*
			 * if (nae.getSubnetwork() != null) { subnet = nae.getSubnetwork(); } else if
			 * (subnetworks_ids.size() == 1) { subnet = subnetworks_ids.get(0); }
			 */

			if (!network_attributes_map.containsKey(subnet)) {
				network_attributes_map.put(subnet, new ArrayList<NetworkAttributesElement>());
			}
			network_attributes_map.get(subnet).add(nae);
		}

		return network_attributes_map;
	}

	/*
	 * private final static Map<String, List<AspectElement>> parseAsMap(final
	 * CxReader cxr, long t, final boolean report_timings) throws IOException { if
	 * (cxr == null) { throw new IllegalArgumentException("reader is null"); }
	 * 
	 * final SortedMap<String, List<AspectElement>> all_aspects = new
	 * TreeMap<String, List<AspectElement>>();
	 * 
	 * while (cxr.hasNext()) { t = System.currentTimeMillis(); final
	 * List<AspectElement> aspects = cxr.getNext(); if (aspects != null &&
	 * !aspects.isEmpty()) { String name = aspects.get(0).getAspectName();
	 * 
	 * if (!all_aspects.containsKey(name)) { all_aspects.put(name, aspects); } else
	 * { all_aspects.get(name).addAll(aspects); } } } return all_aspects; }
	 * 
	 * 
	 * 
	 * private final static void checkEdgeIds(final List<AspectElement> edges, final
	 * Set<Long> edge_ids) throws IOException { for (final AspectElement edge :
	 * edges) { final Long edge_id = ((EdgesElement) edge).getId(); if (edge_id ==
	 * null) { throw new IOException("edge identifiers must not be null"); } if
	 * (edge_ids.contains(edge_id)) { throw new IOException("edge identifier '" +
	 * edge_id + "' is not unique"); } edge_ids.add(edge_id); } }
	 * 
	 * private final static void checkNodeIds(final List<AspectElement> nodes, final
	 * Set<Long> node_ids) throws IOException { for (final AspectElement node :
	 * nodes) { final Long node_id = ((NodesElement) node).getId(); if (node_id ==
	 * null) { throw new IOException("node identifiers must not be null "); } if
	 * (node_ids.contains(node_id)) { throw new IOException("node identifier '" +
	 * node_id + "' is not unique"); } node_ids.add(node_id); } }
	 */

	private final static Object getValue(final AbstractAttributesAspectElement e, final CyColumn column) {
		if (e.isSingleValue()) {
			Object val = null;
			try {
				val = parseValue(e.getValue(), column.getType());
			} catch (Exception ex) {
				logger.warn("Could not process element: " + e, ex);
				ex.printStackTrace();
				val = null;
			}
			return val;
		} else {
			List<Object> vals = new ArrayList<Object>();
			for (String value : e.getValues()) {
				Object val = parseValue(value, column.getListElementType());
				if (val == null) {
					val = Double.NaN;
				}
				vals.add(val);
			}
			return vals;
//			return e.getValues().stream().map(value -> parseValue(value, column.getListElementType()))
//					.collect(Collectors.toList());
		}
	}

	private final static Object parseValue(final String value, final Class<?> type) {
		if (type != String.class && (value == null || value.equals("") || value.equals("NaN") || value.equals("nan")
				|| value.toLowerCase().equals("null"))) {
			return null;
		}
		if (type == String.class) {
			return value;
		} else if (type == Long.class) {
			try {
				return Long.valueOf(value);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("could not convert '" + value + "' to long");
			}
		} else if (type == Integer.class) {
			try {
				return Integer.valueOf(value);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("could not convert '" + value + "' to integer");
			}
		}
		/*
		 * else if (type == Short.class) { try { return Integer.valueOf(value); } catch
		 * (final NumberFormatException e) { throw new
		 * IllegalArgumentException("could not convert '" + value + "' to short"); } }
		 * else if (type == Float.class) { try { return Float.valueOf(value); } catch
		 * (final NumberFormatException e) { throw new
		 * IllegalArgumentException("could not convert '" + value + "' to float"); } }
		 */
		else if (type == Double.class) {
			try {
				return Double.valueOf(value);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("could not convert '" + value + "' to double");
			}
		} else if (type == Boolean.class) {
			try {
				return Boolean.valueOf(value);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("could not convert '" + value + "' to boolean");
			}
		} else {
			throw new IllegalArgumentException(
					"don't know how to deal with type '" + type + "' for value '" + value + "'");
		}
	}

	/*
	 * private final static void processEdgeAttributes(final List<AspectElement>
	 * edge_attributes, final Map<Long, List<EdgeAttributesElement>>
	 * edge_attributes_map, final boolean perform_basic_integrity_checks, final
	 * Set<Long> edge_ids, final boolean subnet_info_present) throws IOException {
	 * if (edge_attributes != null) { for (final AspectElement e : edge_attributes)
	 * { final EdgeAttributesElement eae = (EdgeAttributesElement) e; Long po =
	 * eae.getPropertyOf(); if (!edge_attributes_map.containsKey(po)) { if
	 * (perform_basic_integrity_checks) { if (po == null) { throw new
	 * IOException("edge identifiers must not be null in edge attributes"); } if
	 * (!edge_ids.contains(po)) { if (Settings.INSTANCE.isDebug()) {
	 * System.out.println("edge with id '" + po + "' not present in edges aspect");
	 * } continue; } } edge_attributes_map.put(po, new
	 * ArrayList<EdgeAttributesElement>()); } edge_attributes_map.get(po).add(eae);
	 * 
	 * } } }
	 */

	/*
	 * private final static void processNodeAttributes(final List<AspectElement>
	 * node_attributes, final Map<Long, List<NodeAttributesElement>>
	 * node_attributes_map, final boolean perform_basic_integrity_checks, final
	 * Set<Long> node_ids, final boolean subnet_info_present) throws IOException {
	 * if (node_attributes != null) { for (final AspectElement e : node_attributes)
	 * { final NodeAttributesElement nae = (NodeAttributesElement) e; Long po =
	 * nae.getPropertyOf(); if (perform_basic_integrity_checks) { if (po == null) {
	 * throw new
	 * IOException("node identifiers must not be null in node attributes"); } if
	 * (!node_ids.contains(po)) { if (Settings.INSTANCE.isDebug()) {
	 * System.out.println("node with id '" + po + "' not present in nodes aspect");
	 * } continue; } } if (!node_attributes_map.containsKey(po)) {
	 * node_attributes_map.put(po, new ArrayList<NodeAttributesElement>()); }
	 * node_attributes_map.get(po).add(nae); } } }
	 */

	/*
	 * private final static void processGroups(final Collection<AspectElement>
	 * groups_elements, final Map<Long, List<CyGroupsElement>>
	 * subnet_to_groups_map,Long subNetworkId ) { if (groups_elements != null) { for
	 * (final AspectElement e : groups_elements) { final CyGroupsElement ge =
	 * (CyGroupsElement) e; Long subnetId = ge.getSubNet(); if (subnetId == null) {
	 * subnetId = subNetworkId; ge.setSubNet(subNetworkId);
	 * 
	 * }
	 * 
	 * if (!subnet_to_groups_map.containsKey(subnetId)) {
	 * subnet_to_groups_map.put(subnetId, new ArrayList<CyGroupsElement>()); }
	 * subnet_to_groups_map.get(subnetId).add(ge); } } }
	 */

	private final static void processColumnLabels(final Collection<AspectElement> col_labels_elements,
			final Map<Long, List<CyTableColumnElement>> subnetwork_to_col_labels_map) {
		if (col_labels_elements != null) {
			for (final AspectElement e : col_labels_elements) {
				final CyTableColumnElement ce = (CyTableColumnElement) e;
				Long subnetworkId = ce.getSubnetwork();
				if (!subnetwork_to_col_labels_map.containsKey(subnetworkId)) {
					subnetwork_to_col_labels_map.put(subnetworkId, new ArrayList<CyTableColumnElement>());
				}
				subnetwork_to_col_labels_map.get(subnetworkId).add(ce);
			}
		}
	}

	public final static String getCollectionNameFromNetworkAttributes(
			final SortedMap<String, List<AspectElement>> res) {
		
		final List<AspectElement> network_attributes = res.get(NetworkAttributesElement.ASPECT_NAME);
		String collection_name_from_network_attributes = null;
		if (network_attributes != null) {
			for (final AspectElement e : network_attributes) {
				final NetworkAttributesElement nae = (NetworkAttributesElement) e;
				if (nae.getSubnetwork() == null && nae.getName() != null
						&& nae.getDataType() == ATTRIBUTE_DATA_TYPE.STRING && nae.getName().equals(CxUtil.NAME_COL)
						&& nae.isSingleValue() && nae.getValue() != null && nae.getValue().length() > 0) {
					if (collection_name_from_network_attributes == null) {
						collection_name_from_network_attributes = nae.getValue();
					} else {
						return null;
					}
				}
			}
		}
		return collection_name_from_network_attributes;
	}

}
