package org.cytoscape.io.internal.nicecy;

import java.util.List;
import java.util.stream.Collectors;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CySubNetwork;

public abstract class Identifiable {
	protected long id;
	public Identifiable(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public class NiceCyGroup extends Identifiable {
		private final NiceCyRootNetwork root;
		private CyGroup group;
		
		private final List<Long> nodes;
		private final List<Long> internal_edges;
		private final List<Long> external_edges;
		private final boolean collapsed;
		private final String name;
		
		public NiceCyGroup(final long id, 
				final NiceCyRootNetwork root, 
				final List<Long> nodes, 
				final List<Long> internal_edges, 
				final List<Long> external_edges,
				final boolean collapsed,
				final String name) {
			super(id);
			this.root = root;
			this.nodes = nodes;
			this.internal_edges = internal_edges;
			this.external_edges = external_edges;
			this.collapsed = collapsed;
			this.name = name;
		}
		
		public void addTo(CySubNetwork network) {
			CyGroupFactory group_factory = CyServiceModule.getService(CyGroupFactory.class);
			if (group == null) {
				CyNode node = root.getNode(id);
				
				List<CyNode> internal_nodes = nodes.stream().map(suid -> root.getNode(suid)).collect(Collectors.toList());
				List<CyEdge> int_edges = internal_edges.stream().map(suid -> root.getEdge(suid)).collect(Collectors.toList());
				
				List<CyEdge> ext_edges = external_edges.stream().map(suid -> root.getEdge(suid)).collect(Collectors.toList());
				int_edges.addAll(ext_edges);
				
				group = group_factory.createGroup(network, node, internal_nodes, int_edges, true);
				
			}else {
				group.addGroupToNetwork(network);
			}
			CyRow row = network.getDefaultNodeTable().getRow(group.getGroupNode().getSUID());
			row.set(CyNetwork.NAME, name);
		}
		
		public CyGroup getGroup() {
			if (group == null) {
				throw new RuntimeException("Group " + id + " was never initialized");
			}
			return group;
		}

		public boolean isCollapsed() {
			return collapsed;
		}

		public void updateInView(CySubNetwork network) {
//			group.collapse(network);
//			group.expand(network);
			
			if (isCollapsed()) {
				group.collapse(network);
			}
		}

	}
	
	public class NiceCyNode extends Identifiable {
		private final String name;
		private final String represents;
		private CyNode node;
		
		public NiceCyNode(long id, String name, String represents) {
			super(id);
			this.name = name;
			this.represents = represents;
		}
		
		public CyNode addTo(CySubNetwork subnet) {
			if (node == null) {
				node = subnet.addNode();
				CxUtil.saveCxId(node, subnet, id);
			}else {
				subnet.addNode(node);
			}
			CyRow row = subnet.getDefaultNodeTable().getRow(node.getSUID());
			if (name != null) {
				row.set(CyNetwork.NAME, name);
			}
			if (represents != null) {
				CxUtil.createColumn(subnet.getDefaultNodeTable(), CxUtil.REPRESENTS, String.class, true);
				row.set(CxUtil.REPRESENTS, represents);
			}
			return node;
		}
		
		public CyNode getNode() {
			if (node == null) {
				throw new RuntimeException("Node " + id + " was never created.");
			}
			return node;
		}
	}
	
	public class NiceCyEdge extends Identifiable {
		private final Long source, target;
		private final NiceCyRootNetwork root;
		private final String interaction;
		private CyEdge edge;
		
		public NiceCyEdge(long id, NiceCyRootNetwork root, Long source, Long target, String interaction) {
			super(id);
			this.root = root;
			this.source = source;
			this.target = target;
			this.interaction = interaction;
		}
		
		public CyEdge addTo(CySubNetwork subnet) {
			if (edge == null) {
				CyNode sourceNode  = root.getNode(source);
				CyNode targetNode = root.getNode(target);
				edge = subnet.addEdge(sourceNode, targetNode, true);
				CxUtil.saveCxId(edge, subnet, id);
			}else {
				subnet.addEdge(edge);
			}
			CyRow row = subnet.getDefaultEdgeTable().getRow(edge.getSUID());
			if (interaction != null) {
				row.set(CyEdge.INTERACTION, interaction);
			}
			return edge;
		}
		
		public CyEdge getEdge() {
			if (edge == null) {
				throw new RuntimeException("Edge " + id + " was never created.");
			}
			return edge;
		}
	}
}
