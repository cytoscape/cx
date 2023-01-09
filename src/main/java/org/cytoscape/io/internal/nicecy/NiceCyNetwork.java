package org.cytoscape.io.internal.nicecy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.ndexbio.cxio.aspects.datamodels.AbstractAttributesAspectElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.HiddenAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.misc.OpaqueElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class NiceCyNetwork extends Identifiable {
	
	protected final List<CyTableColumnElement> tableColumns = new ArrayList<>();
	protected List<NetworkAttributesElement> attributes = new ArrayList<>();
	protected List<HiddenAttributesElement> hiddenAttributes = new ArrayList<>();
	protected Map<Long, List<NodeAttributesElement>> nodeAttributes = new HashMap<>();
	protected Map<Long, List<EdgeAttributesElement>> edgeAttributes = new HashMap<>();

	protected CyNetwork network;

	public NiceCyNetwork(long id) {
		super(id);
	}

	protected abstract String getNamespace();

	public List<NetworkAttributesElement> getAttributes() {
		return attributes;
	}

	public List<HiddenAttributesElement> getHiddenAttributes() {
		return hiddenAttributes;
	}

	public Map<Long, List<NodeAttributesElement>> getNodeAttributes() {
		return nodeAttributes;
	}

	public class NiceCySubNetwork extends NiceCyNetwork {

		protected final NiceCyRootNetwork parent;
		protected final List<Long> nodes;
		protected final List<Long> edges;
		protected final List<Long> groups;
		protected final Map<Long, NiceCyView> views;

		public NiceCySubNetwork(long id, NiceCyRootNetwork parent) {
			super(id);
			this.parent = parent;
			views = new HashMap<>();
			nodes = new ArrayList<>();
			edges = new ArrayList<>();
			groups = new ArrayList<>();
		}

		protected void apply(CyNetwork cyNetwork) {
			if (cyNetwork == null) {
				throw new RuntimeException("Subnetwork can not be null");
			}
			this.network = cyNetwork;

			addElements();
			addTableColumns();
			addAttributes();
		}

		private void addElements() {
			// If the parent was created from a singleton, add all nodes to the network
			if (!parent.isCollection) {
				nodes.addAll(parent.root_nodes.keySet());
				edges.addAll(parent.root_edges.keySet());
				groups.addAll(parent.root_groups.keySet());
			}
			// MUST add nodes to subnetworks first so shared node attrs work
			nodes.forEach(suid -> {
				parent.root_nodes.get(suid).addTo((CySubNetwork) network);
			});

			edges.forEach(suid -> {
				NiceCyEdge edge = parent.root_edges.get(suid);
				edge.addTo((CySubNetwork) network);
			});

			groups.forEach(suid -> {
				NiceCyGroup group = parent.root_groups.get(suid);
				group.addTo((CySubNetwork) this.network);
			});
		}

		@Override
		protected String getNamespace() {
			return CyNetwork.DEFAULT_ATTRS;
		}

		public Collection<? extends CyNetworkView> createViews(Boolean explicitCreateView) {
			List<CyNetworkView> cy_views = new ArrayList<>();
			CyNetworkViewFactory view_factory = CyServiceModule.getService(CyNetworkViewFactory.class);
			CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);
		
			views.forEach((suid, view) -> {
			/*	final boolean hasExplicitView = !view.isCartesianLayoutEmpty() 
						|| !view.isVisualPropertiesEmpty() 
						|| !view.isNodeBypassEmpty() 
						|| !view.isEdgeBypassEmpty(); */
				
				final long networkSize = network.getEdgeCount() + network.getNodeCount();
				
				final long viewThreshold = CxPreferences.getViewThreshold();
				final CxPreferences.CreateViewEnum createViewPreference = CxPreferences.getCreateView();
				System.out.println("View Preference: " + createViewPreference);
				System.out.println("Explicit Create View: " + explicitCreateView);
				final boolean createView;
				
				if (explicitCreateView != null) {
					createView = explicitCreateView.booleanValue();
				} else {
					if(CxPreferences.getCreateView() == CxPreferences.CreateViewEnum.NEVER) { 
						createView = false;
					} else if (createViewPreference.equals(CxPreferences.CreateViewEnum.ALWAYS) || networkSize < viewThreshold){
						createView = true;
					} else {
						createView = false;
					}
				}
				
				if (createView) {
					CyNetworkView v = view_factory.createNetworkView(network);
					
					view.apply(v);
					view_manager.addNetworkView(v);
					cy_views.add(v);
				}
			});
			return cy_views;
		}

		
		
		public void updateViewIds(NiceCySubNetwork otherNet) {
			this.id = otherNet.getId();
			Map<String, NiceCyView> nameMap = new HashMap<>();
			for (NiceCyView view : otherNet.views.values()) {
				String name = view.getName();
				if (nameMap.containsKey(name)) {
					throw new RuntimeException("Cannot align views with duplicate names");
				}
				nameMap.put(name, view);
			}
			for (NiceCyView view : views.values()) {
				String name = view.getName();
				if (!nameMap.containsKey(name)) {
					throw new RuntimeException("View named " + name + " not in other");
				}

				// Replace in subnetwork mapping, update views, and remove from name map to
				// avoid duplicates
				views.remove(view.getId());
				NiceCyView otherView = nameMap.get(name);
				view.updateIds(otherView);
				views.put(view.getId(), view);
				nameMap.remove(name);
			}
		}

		public Collection<NiceCyView> getViews() {
			return views.values();
		}

		public List<Long> getNodes() {
			return nodes;
		}

		public List<Long> getEdges() {
			return edges;
		}

		public List<Long> getGroups() {
			return groups;
		}
	}

	public String getNetworkName() {
		for (NetworkAttributesElement nae : attributes) {
			if (nae.getName().equals(CyNetwork.NAME)) {
				return nae.getValue();
			}
		}
		return null;
	}

	protected void serializeAspect(String column, Collection<AspectElement> collection) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<JsonNode> nodes = new ArrayList<>();
		for (AspectElement el : collection) {
			OpaqueElement op = (OpaqueElement) el;
			nodes.add(op.getData());
		}
		CyTable table = network.getTable(CyNetwork.class, getNamespace());

		String aspectStr = mapper.writeValueAsString(nodes);
		CxUtil.createColumn(table, column, String.class, true);

		table.getRow(network.getSUID()).set(column, aspectStr);

	}

	protected void addAttributes() {

		CyTable node_table = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		CyTable node_local_table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		CyTable edge_table = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		CyTable edge_local_table = network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS);

		NiceCyRootNetwork root;
		if (this instanceof NiceCyRootNetwork) {
			root = (NiceCyRootNetwork) this;
		} else {
			root = ((NiceCySubNetwork) this).parent;
		}

		long t0 = System.currentTimeMillis();
		addNetworkAttributesHelper(network, attributes, hiddenAttributes);
		// addNetworkAttributesHelper(hidden_table, network, hiddenAttributes);
		nodeAttributes.forEach((suid, attrs) -> {
			CyNode node = root.getNode(suid);
			addAttributesHelper(node_table, node_local_table, node, attrs);
		});
		edgeAttributes.forEach((suid, attrs) -> {
			CyEdge edge = root.getEdge(suid);
			addAttributesHelper(edge_table, edge_local_table, edge, attrs);
		});
		TimingUtil.reportTimeDifference(t0, "attributes of " + getNetworkName(), -1);
	}

	
	private void addNetworkAttributesHelper(CyIdentifiable ele, List<? extends AbstractAttributesAspectElement> attrs,
			List<? extends AbstractAttributesAspectElement> hidden_attrs) {

		final CyTable sharedTable = network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS);
		final CyTable localTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);

		final CyRow sharedRow = sharedTable.getRow(ele.getSUID());
		final CyRow localRow = localTable.getRow(ele.getSUID());

		final CyTable baseSharedTable;
		final CyTable baseLocalTable;

		final CyRow baseSharedRow;
		final CyRow baseLocalRow;

		final CySubNetwork baseNetwork = (network instanceof CySubNetwork)
				? ((CySubNetwork) network).getRootNetwork().getBaseNetwork()
				: ((CyRootNetwork) network).getBaseNetwork();

		baseSharedTable = baseNetwork.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS);
		baseLocalTable = baseNetwork.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);

		baseSharedRow = baseSharedTable.getRow(baseNetwork.getSUID());
		baseLocalRow = baseLocalTable.getRow(baseNetwork.getSUID());

		attrs.forEach(attr -> {
			final String name = attr.getName();
			final boolean isLocal = attr.getSubnetwork() != null;

			final CyTable table = isLocal ? localTable : sharedTable;
			final CyTable baseTable = isLocal ? baseLocalTable : baseSharedTable;
			final CyRow row;// = isLocal ? localRow : sharedRow;

			if (table.getColumn(name) == null) {
				if (baseTable.getColumn(name) != null) {
					row = isLocal ? baseLocalRow : baseSharedRow;
				} else {
					CxUtil.createColumn(table, name, CxUtil.getDataType(attr.getDataType()), attr.isSingleValue());
					row = isLocal ? localRow : sharedRow;
				}
			} else {
				row = isLocal ? localRow : sharedRow;
			}
			Object value = CxUtil.getValue(attr);

			try {
				row.set(name, value);
				// row.set(namespace, columnName, value);
			} catch (NullPointerException e) {
				throw new NullPointerException(
						"NullPointerException setting " + name + " to " + value + ". Is there a null value in a list?");
			} catch (IllegalArgumentException e) {
				String message = String.format("Cannot set value in column %s(%s) to %s (type %s). %s", name,
						CxUtil.getDataType(attr.getDataType()), value, value.getClass(), e.getMessage());
				throw new IllegalArgumentException(message, e);
			}
		});

		final CyTable hiddenTable = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		final CyRow hiddenRow = network.getRow(ele, CyNetwork.HIDDEN_ATTRS);

		hidden_attrs.forEach(attr -> {
			final String name = attr.getName();

			// special handling of __parentNetwork.SUID column
			if ( name.equals(CxUtil.PARENT_NETWORK_COLUMN)) {
				if ( attr.getSubnetwork()!=null) {
			//		Long realSUID = ((NiceCySubNetwork)this).parent.getNetwork(attr.getSubnetwork()).network.getSUID();
			//		CxUtil.createColumn(hiddenTable, name, CxUtil.getDataType(attr.getDataType()), attr.isSingleValue());					
			//		hiddenRow.set(name, realSUID);
				}
				return;
				
			}
			if (hiddenTable.getColumn(name) == null) {
				CxUtil.createColumn(hiddenTable, name, CxUtil.getDataType(attr.getDataType()), attr.isSingleValue());
			}
			Object value = CxUtil.getValue(attr);

			try {
				hiddenRow.set(name, value);
			} catch (NullPointerException e) {
				throw new NullPointerException(
						"NullPointerException setting " + name + " to " + value + ". Is there a null value in a list?");
			} catch (IllegalArgumentException e) {
				String message = String.format("Cannot set value in column %s(%s) to %s (type %s). %s", name,
						CxUtil.getDataType(attr.getDataType()), value, value.getClass(), e.getMessage());
				throw new IllegalArgumentException(message, e);
			}
		});
	}

	private static void addAttributesHelper(CyTable sharedTable, CyTable localTable, CyIdentifiable ele,
			List<? extends AbstractAttributesAspectElement> attrs) {
		// System.out.println(" Adding attributes to row for SUID: " + ele.getSUID() + "
		// into shared table " + sharedTable.getTitle() + " and local table " +
		// localTable.getTitle());

		final CyRow sharedRow = sharedTable.getRow(ele.getSUID());
		final CyRow localRow = localTable.getRow(ele.getSUID());

		attrs.forEach(attr -> {
			final String name = attr.getName();
			final boolean isLocal = attr.getSubnetwork() != null;

			final CyTable table = isLocal ? localTable : sharedTable;
			final CyRow row = isLocal ? localRow : sharedRow;

			if (table.getColumn(name) == null) {
				CxUtil.createColumn(table, name, CxUtil.getDataType(attr.getDataType()), attr.isSingleValue());
			}
			Object value = CxUtil.getValue(attr);

			try {
				row.set(name, value);
			} catch (NullPointerException e) {
				throw new NullPointerException(
						"NullPointerException setting " + name + " to " + value + ". Is there a null value in a list?");
			} catch (IllegalArgumentException e) {
				String message = String.format("Cannot set value in column %s(%s) to %s (type %s). %s", name,
						CxUtil.getDataType(attr.getDataType()), value, value.getClass(), e.getMessage());
				throw new IllegalArgumentException(message, e);
			}
		});
	}

	protected void addTableColumns() {

		tableColumns.forEach(column -> {

			CyTable table;
			String name = column.getName();

			final boolean isLocal = column.getSubnetwork() != null;

			switch (column.getAppliesTo()) {
			case "node_table":
				table = isLocal ? network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS)
						: network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
				break;
			case "edge_table":
				table = isLocal ? network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS)
						: network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
				break;
			case "network_table":
				table = isLocal // && network.getSUID() == column.getSubnetwork()
						? network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS)
						: network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS);

				break;
			default:
				throw new IllegalArgumentException("Unrecognized CyTableColumn applies_to: " + column.getAppliesTo());
			}

			if (table.getColumn(name) == null) {
				CxUtil.createColumn(table, name, CxUtil.getDataType(column.getDataType()), column.isSingleValue());
			}
		});
	}
}
