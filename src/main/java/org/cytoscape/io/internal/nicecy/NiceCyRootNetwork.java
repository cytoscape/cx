package org.cytoscape.io.internal.nicecy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyViewsElement;
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
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.misc.NumberVerification;
import org.ndexbio.cxio.misc.OpaqueElement;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NdexNetworkStatus;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.Provenance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("deprecation")
public class NiceCyRootNetwork extends NiceCyNetwork{

	private Map<Long, Long> suid_to_cxid_map;
	
	protected final Map<String, Collection<AspectElement>> opaqueAspects;
	private final Map<Long, NiceCySubNetwork> subnetworks;
	protected final boolean isCollection;
	protected final Map<Long, NiceCyNode> root_nodes;
	protected final Map<Long, NiceCyEdge> root_edges;
	protected final Map<Long, NiceCyGroup> root_groups;
	
	public NiceCyRootNetwork(NiceCXNetwork niceCX) {
		super(CxUtil.DEFAULT_SUBNET);
		subnetworks = new HashMap<Long, NiceCySubNetwork>();
		root_nodes = new HashMap<Long, NiceCyNode>();
		root_edges = new HashMap<Long, NiceCyEdge>();
		root_groups = new HashMap<Long, NiceCyGroup>();
		
		opaqueAspects = niceCX.getOpaqueAspectTable();
		isCollection = opaqueAspects.containsKey(SubNetworkElement.ASPECT_NAME);
		
		try {
			handleOpaqueAspects();
			
			handleNetworkAttributes(niceCX.getNetworkAttributes());
			handleNodes(niceCX.getNodes());
			handleEdges(niceCX.getEdges());
			handleNodeAttributes(niceCX.getNodeAttributes());
			handleEdgeAttributes(niceCX.getEdgeAttributes());
			handleNodeAssociatedAspects(niceCX.getNodeAssociatedAspects());
			handleEdgeAssociatedAspects(niceCX.getEdgeAssociatedAspects());
		}catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to process JSON in CX: " + e.getMessage());
		}
	}
	
	private void handleOpaqueAspects() throws JsonProcessingException{
		handleCxMapping(opaqueAspects.remove(CxUtil.CX_ID_MAPPING));
		handleNetworkRelations(opaqueAspects.remove(NetworkRelationsElement.ASPECT_NAME));
		
		handleCyTableColumns(opaqueAspects.remove(CyTableColumnElement.ASPECT_NAME));
		handleGroups(opaqueAspects.remove(CyGroupsElement.ASPECT_NAME));		
		handleHiddenAttributes(opaqueAspects.remove(HiddenAttributesElement.ASPECT_NAME));
		handleSubNetworks(opaqueAspects.remove(SubNetworkElement.ASPECT_NAME));
		handleNamespaces(opaqueAspects.remove(NamespacesElement.ASPECT_NAME));
		
		//Handle old name for CyVisualProperties
		Collection<AspectElement> visualProps = opaqueAspects.remove(CyVisualPropertiesElement.ASPECT_NAME);
		if (visualProps == null) {
			visualProps = opaqueAspects.remove("visualProperties");
		}
		handleCyVisualProperties(visualProps);
		
		opaqueAspects.remove(MetaDataCollection.NAME);
		
		opaqueAspects.remove(CyViewsElement.ASPECT_NAME);
		opaqueAspects.remove(Provenance.ASPECT_NAME);
		opaqueAspects.remove(NdexNetworkStatus.ASPECT_NAME);
		opaqueAspects.remove(NumberVerification.NAME);
	}
	
	/**
	 * If there is not a networkAttribute with the Namespaces context already, create one
	 * @param aspects
	 * @throws JsonProcessingException 
	 */
	private void handleNamespaces(Collection<AspectElement> aspects) throws JsonProcessingException {
		if (aspects == null) {
			return;
		}
		String contextStr = serializeNamespaces(aspects);
		boolean hasContextAttribute = attributes.stream().anyMatch(nae -> nae.getName().equals(NamespacesElement.ASPECT_NAME));
		if (!hasContextAttribute) {
			NetworkAttributesElement value = new NetworkAttributesElement(this.getId(), NamespacesElement.ASPECT_NAME, contextStr);
			attributes.add(value);
		}
	}
	
	private String serializeNamespaces(Collection<AspectElement> collection) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
 		ArrayList<JsonNode> nodes = new ArrayList<>();
 		for (AspectElement el : collection) {
 			OpaqueElement op = (OpaqueElement) el;
 			nodes.add(op.getData());
 		}    	
    	return mapper.writeValueAsString(nodes);
	}

	private void handleSubNetworks(Collection<AspectElement> aspects) {
		if (aspects == null) {
			return;
		}
		
		aspects.forEach(aspect -> {
			SubNetworkElement sne = (SubNetworkElement) aspect;
			NiceCySubNetwork network = subnetworks.get(sne.getId());
			
			sne.getNodes().forEach(nodeId -> {
				long id = getCxId(nodeId);
				network.nodes.add(id);
			});
			sne.getEdges().forEach(edgeId -> {
				long id = getCxId(edgeId);
				network.edges.add(id);
			});
			
		});
	}

	private void handleHiddenAttributes(Collection<AspectElement> aspects) {
		if (aspects == null) {
			return;
		}
		aspects.forEach(aspect -> {
			HiddenAttributesElement hae = (HiddenAttributesElement) aspect;
			getNetwork(hae.getSubnetwork()).hiddenAttributes.add(hae);
		});
	}

	private void handleCyVisualProperties(Collection<AspectElement> visualProps) {
		if (visualProps == null) {
			return;
		}
		visualProps.forEach(aspect -> {
			CyVisualPropertiesElement cvpe = (CyVisualPropertiesElement) aspect;
			NiceCyView view = getViewWithId(cvpe.getView());
			
			switch(cvpe.getProperties_of()) {
			case "network":
			case "nodes:default":
			case "edges:default":
				view.addVisualProperties(cvpe);
				break;
			case "nodes":
				Long node_id = getCxId(cvpe.getApplies_to());
				view.addNodeBypass(node_id, cvpe);
				break;
			case "edges":
				Long edge_id = getCxId(cvpe.getApplies_to());
				view.addEdgeBypass(edge_id, cvpe);
				break;
			}
		});
	}
	
	private void handleNodeAssociatedAspects(Map<String, Map<Long, Collection<AspectElement>>> nodeAssociatedAspects) {
		
		nodeAssociatedAspects.forEach((name, map) -> {
			switch(name) {
				case CartesianLayoutElement.ASPECT_NAME:
					handleCartesianLayout(map);
					break;
				default:
					System.out.println("Node associcated " + name);
			}
		});
	}
	
	private void handleCartesianLayout(Map<Long, Collection<AspectElement>> map) {
		map.forEach((suid, aspects) -> {
			long id = getCxId(suid);
			aspects.forEach(aspect -> {
				CartesianLayoutElement cl = (CartesianLayoutElement) aspect;
				Long viewId = cl.getView();
				NiceCyView view = getViewWithId(viewId);
				view.addCartesianLayout(id, cl);
			});
		});
	}

	private void handleEdgeAssociatedAspects(Map<String, Map<Long, Collection<AspectElement>>> edgeAssociatedAspects) {
		edgeAssociatedAspects.forEach((name, map) -> {
			System.out.println("Edge associcated " + name);
		});
	}

	private void handleNodeAttributes(Map<Long, Collection<NodeAttributesElement>> nodeAttributes) {
		nodeAttributes.forEach((suid, attrs) -> {
			long id = getCxId(suid);

			attrs.forEach(attr -> { 
				attr.setPropertyOf(id);
				NiceCyNetwork net = getNetwork(attr.getSubnetwork());
				if (!net.nodeAttributes.containsKey(id)) {
					net.nodeAttributes.put(id, new ArrayList<NodeAttributesElement>());
				}
				net.nodeAttributes.get(id).add(attr);
			});
		});
	}
	private void handleEdgeAttributes(Map<Long, Collection<EdgeAttributesElement>> edgeAttributes) {
		edgeAttributes.forEach((suid, attrs) -> {
			long id = getCxId(suid);
			
			attrs.forEach(attr -> {
				//TODO: unnecessary
				attr.setPropertyOf(id); 
				NiceCyNetwork net = getNetwork(attr.getSubnetwork());
				if (!net.edgeAttributes.containsKey(id)) {
					net.edgeAttributes.put(id, new ArrayList<EdgeAttributesElement>());
				}
				net.edgeAttributes.get(id).add(attr);
			});
			
			
		});
	}

	private void handleGroups(Collection<AspectElement> aspects) {
		if (aspects == null) {
			return;
		}
		aspects.forEach(aspect -> {
			CyGroupsElement cge = (CyGroupsElement) aspect;
			long id = getCxId(cge.getGroupId());
			
			List<Long> nodes = cge.getNodes().stream().map(suid -> getCxId(suid)).collect(Collectors.toList());
			List<Long> internal_edges = cge.getInternalEdges().stream().map(suid -> getCxId(suid)).collect(Collectors.toList());
			List<Long> external_edges = cge.getExternalEdges().stream().map(suid -> getCxId(suid)).collect(Collectors.toList());
			boolean collapsed = cge.isCollapsed();
			
			NiceCyGroup group = new NiceCyGroup(id, this, nodes, internal_edges, external_edges, collapsed, cge.getName());
			root_groups.put(id, group);
		});
	}

	private void handleCxMapping(Collection<AspectElement> cxMapping) {
		if (cxMapping == null) {
			return;
		}
		suid_to_cxid_map = new HashMap<Long, Long>();
		cxMapping.forEach(aspect -> {
			OpaqueElement oe = (OpaqueElement) aspect;
			JsonNode node = oe.getData();
			node.fields().forEachRemaining(entry -> {
				String suid_str = entry.getKey();
				Long suid = Long.valueOf(suid_str);
				Long cxid = entry.getValue().asLong();
				suid_to_cxid_map.put(suid, cxid);
			});
		});
	}

	private void handleNodes(Map<Long, NodesElement> nodes) {
		nodes.forEach((suid, node) -> {
			Long id = getCxId(suid);
			root_nodes.put(id, new NiceCyNode(id, node.getNodeName(), node.getNodeRepresents()));
		});
	}
	
	private void handleEdges(Map<Long, EdgesElement> edges) {
		edges.forEach((suid, edge) -> {
			Long id = getCxId(suid);
			
			Long source = getCxId(edge.getSource());
			Long target = getCxId(edge.getTarget());
			
			root_edges.put(id, new NiceCyEdge(id, this, source, target, edge.getInteraction()));
		});
	}

	private void handleCyTableColumns(Collection<AspectElement> aspects) {
		if (aspects == null) {
			return;
		}
		aspects.forEach(aspect -> {
			CyTableColumnElement ctce = (CyTableColumnElement) aspect;
			NiceCyNetwork net = getNetwork(ctce.getSubnetwork());
			net.tableColumns.add(ctce);
		});
	}

	private void handleNetworkAttributes(Collection<NetworkAttributesElement> networkAttributes) {
		networkAttributes.forEach(attr -> {
			getNetwork(attr.getSubnetwork()).attributes.add(attr);
		});
	}

	/**
	 * Build subnetwork and view maps
	 * @param aspects
	 */
	private void handleNetworkRelations(Collection<AspectElement> aspects) {
		if (aspects != null) {
		
			// Create subnetworks
			aspects.stream().filter(aspect -> {
				return ((NetworkRelationsElement) aspect).getRelationship() == NetworkRelationsElement.TYPE_SUBNETWORK;
			}).forEach(aspect -> {
				NetworkRelationsElement nre = (NetworkRelationsElement) aspect;
				Long suid = nre.getChild();
				NiceCySubNetwork network = new NiceCySubNetwork(suid, this);
				NetworkAttributesElement nae = new NetworkAttributesElement(suid, CyNetwork.NAME, nre.getChildName());
				network.attributes.add(nae);
				subnetworks.put(suid, network);
			});
			
			// Create views
			aspects.stream().filter(aspect -> {
				return ((NetworkRelationsElement) aspect).getRelationship() == NetworkRelationsElement.TYPE_VIEW;
			}).forEach(aspect -> {
				NetworkRelationsElement nre = (NetworkRelationsElement) aspect;
				Long suid = nre.getChild();
				Long net = nre.getParent();
				NiceCySubNetwork subnet = (NiceCySubNetwork) getNetwork(net);
				NiceCyView view = new NiceCyView(suid, subnet);
				subnet.views.put(suid, view);
			});
		}
		
		//Add a default subnetwork if the aspect doesn't exist or lists no subnets
		if (subnetworks.isEmpty()) {
			NiceCySubNetwork subnet = new NiceCySubNetwork(CxUtil.DEFAULT_SUBNET, this);
			subnet.views.put(CxUtil.DEFAULT_VIEW, new NiceCyView(CxUtil.DEFAULT_VIEW, subnet));
			subnetworks.put(CxUtil.DEFAULT_SUBNET, subnet);
		}
		
	}
	
	private NiceCyNetwork getNetwork(Long suid) {
		if (suid == null) {
			if (!isCollection) {
				return subnetworks.values().iterator().next();
			}
			return this;
		}
		return subnetworks.get(suid);
	}
	
	private long getCxId(long suid) {
		if (suid_to_cxid_map == null) {
			return suid;
		}
		if (!suid_to_cxid_map.containsKey(suid)) {
			throw new RuntimeException("Unable to find suid " + suid + " in cxMapping.");
		}
		return suid_to_cxid_map.get(suid);
	}
	
	private NiceCyView getViewWithId(Long view) {
		if (view == null) {
			view = CxUtil.DEFAULT_VIEW;
		}
		for (NiceCySubNetwork subnet : subnetworks.values()) {
			if (subnet.views.containsKey(view)) {
				return subnet.views.get(view);
			}
		}
		throw new RuntimeException("No view found with ID " + view);
	}

	public List<CyNetwork> apply() {
		if (subnetworks.isEmpty()) {
			throw new RuntimeException("No networks were detected in the CX");
		}
		List<CyNetwork> networks = new ArrayList<CyNetwork>();
		
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		CyNetwork base = network_factory.createNetwork();
		
		CyRootNetwork root = ((CySubNetwork)base).getRootNetwork();
		networks.add(base);
		
		// Build subnetworks
		Iterator<NiceCySubNetwork> nice_subs = subnetworks.values().iterator();
		NiceCySubNetwork nice_sub = nice_subs.next();
		nice_sub.apply((CySubNetwork) root.getBaseNetwork());
		
		while (nice_subs.hasNext()) {
			nice_sub = nice_subs.next();
			CyNetwork network = root.addSubNetwork();
			nice_sub.apply((CySubNetwork) network);
			networks.add(network);
		}
		
		// Build Root network information
		super.apply(root);
		NiceCyNetwork subnet = getNetwork(null);
		opaqueAspects.forEach((name, opaque) -> {
			try {
				subnet.serializeAspect(CxUtil.OPAQUE_ASPECT_PREFIX + name, opaque);
			} catch (IOException e) {
				System.out.println("Failed to serialize opaque aspect: " + name);
			}
		});
		
		return networks;
	}
	
	public Iterator<NiceCySubNetwork> getSubnetworks() {
		return subnetworks.values().iterator();
	}

	@Override
	protected String getNamespace() {
		return CyRootNetwork.SHARED_ATTRS;
	}
	
	@Override
	public void addElements() {
		// Nothing to add for root network
	}

	public List<CyNetworkView> createViews() {
		List<CyNetworkView> views = new ArrayList<CyNetworkView>();
		subnetworks.forEach((suid, subnet) -> {
			views.addAll(subnet.createViews());
		});
		return views;
	}

	public CyNode getNode(Long suid) {
		return root_nodes.get(suid).getNode();
	}

	public CyEdge getEdge(Long suid) {
		return root_edges.get(suid).getEdge();
	}

	public CyRootNetwork getCollection() {
		return (CyRootNetwork) network;
	}
}
