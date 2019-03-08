package org.cytoscape.io.internal.nicecy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
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

public abstract class NiceCyNetwork extends Identifiable{
	protected final List<CyTableColumnElement> tableColumns = new ArrayList<CyTableColumnElement>();
	protected List<NetworkAttributesElement> attributes = new ArrayList<NetworkAttributesElement>();
	protected List<HiddenAttributesElement> hiddenAttributes = new ArrayList<HiddenAttributesElement>();
	protected Map<Long, List<NodeAttributesElement>> nodeAttributes = new HashMap<Long, List<NodeAttributesElement>>();
	protected Map<Long, List<EdgeAttributesElement>> edgeAttributes = new HashMap<Long, List<EdgeAttributesElement>>();

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
			views = new HashMap<Long, NiceCyView>();
			nodes = new ArrayList<Long>();
			edges = new ArrayList<Long>();
			groups = new ArrayList<Long>();
		}
		
		protected void apply(CyNetwork network) {
			if (network == null) {
				throw new RuntimeException("Subnetwork can not be null");
			}
			this.network = network;
			
			addElements();
			addAttributes();
		}
		
		private void addElements() {
			// If the parent was created from a singleton, add all nodes to the network
			if (!parent.isCollection) {
				nodes.addAll(parent.root_nodes.keySet());
				edges.addAll(parent.root_edges.keySet());
				groups.addAll(parent.root_groups.keySet());
			}
			//MUST add nodes to subnetworks first so shared node attrs work
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

		public Collection<? extends CyNetworkView> createViews() {
			List<CyNetworkView> cy_views = new ArrayList<CyNetworkView>();
			CyNetworkViewFactory view_factory = CyServiceModule.getService(CyNetworkViewFactory.class);
			CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);
			views.forEach((suid, view) -> {
				CyNetworkView v = view_factory.createNetworkView(network);
				view.apply(v);
				view_manager.addNetworkView(v);
				cy_views.add(v);
			});
			return cy_views;
		}

		public void updateViewIds(NiceCySubNetwork otherNet) {
			this.id = otherNet.getId();
			Map<String, NiceCyView> nameMap = new HashMap<String, NiceCyView>();
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
				
				// Replace in subnetwork mapping, update views, and remove from name map to avoid duplicates
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
		addTableColumns();
		CyTable node_table = network.getTable(CyNode.class, getNamespace());
		CyTable edge_table = network.getTable(CyEdge.class, getNamespace());
		CyTable net_table = network.getTable(CyNetwork.class, CyNetwork.DEFAULT_ATTRS);//getNamespace());
		CyTable hidden_table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		
		NiceCyRootNetwork root;
		if (this instanceof NiceCyRootNetwork) {
			root = (NiceCyRootNetwork) this;
		}else {
			root = ((NiceCySubNetwork) this).parent;
		}
		
		Long t0 = System.currentTimeMillis();
		addAttributesHelper(net_table, network, attributes);
		addAttributesHelper(hidden_table, network, hiddenAttributes);
		nodeAttributes.forEach((suid, attrs) -> {
			CyNode node = root.getNode(suid);
			
			addAttributesHelper(node_table, node, attrs);
		});
		edgeAttributes.forEach((suid, attrs) -> {
			CyEdge edge = root.getEdge(suid);
			addAttributesHelper(edge_table, edge, attrs);
		});
		TimingUtil.reportTimeDifference(t0, "attributes of " + getNetworkName(), -1);
	}
	
	private void addAttributesHelper(CyTable table, CyIdentifiable ele, List<? extends AbstractAttributesAspectElement> attrs) {
		CyRow row = table.getRow(ele.getSUID());
		
		attrs.forEach(attr -> {
			String name = attr.getName();
			if (table.getColumn(name) == null) {
				CxUtil.createColumn(table, name, CxUtil.getDataType(attr.getDataType()), attr.isSingleValue());
			}
			Object value = CxUtil.getValue(attr);
			
			try{
				row.set(name, value);
			}catch(NullPointerException e) {
				throw new NullPointerException("NullPointerException setting " + name + " to " + value + ". Is there a null value in a list?");
			} catch (IllegalArgumentException e) {
				String message = String.format("Cannot set value in column %s(%s) to %s (type %s). %s", name, CxUtil.getDataType(attr.getDataType()), value, value.getClass(), e.getMessage()); 
				throw new IllegalArgumentException(message);
			}
		});
	}

	protected void addTableColumns() {
		
		tableColumns.forEach(column -> {
			CyTable table;
			String name = column.getName();
			
			switch(column.getAppliesTo()) {
			case "node_table":
				table = network.getTable(CyNode.class, getNamespace());
				break;
			case "edge_table": 
				table = network.getTable(CyEdge.class, getNamespace());
				break;
			case "network_table":
				table = network.getTable(CyNetwork.class, getNamespace());
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
