package org.cytoscape.io.internal.nicecy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.ndexbio.cx2.aspect.element.core.TableColumnVisualStyle;
import org.ndexbio.cx2.aspect.element.core.VPMappingType;
import org.ndexbio.cx2.aspect.element.core.VisualPropertyMapping;
import org.ndexbio.cx2.aspect.element.cytoscape.AbstractTableVisualProperty;
import org.ndexbio.cx2.aspect.element.cytoscape.DefaultTableType;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableVisualPropertiesElement;
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
import org.ndexbio.model.exceptions.NdexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SuppressWarnings("deprecation")
public class NiceCyRootNetwork extends NiceCyNetwork{

	private static Logger logger = LoggerFactory.getLogger(NiceCyRootNetwork.class);
	
	public static String[] UNSERIALIZED_OPAQUE_ASPECTS = new String[] {
			NetworkRelationsElement.ASPECT_NAME,
			MetaDataCollection.NAME,
			CyViewsElement.ASPECT_NAME,
			Provenance.ASPECT_NAME,
			NdexNetworkStatus.ASPECT_NAME,
			NumberVerification.NAME,
			CxUtil.CX_ID_MAPPING,
			CyTableColumnElement.ASPECT_NAME,
			CyGroupsElement.ASPECT_NAME,
			HiddenAttributesElement.ASPECT_NAME,
			SubNetworkElement.ASPECT_NAME,
			NamespacesElement.ASPECT_NAME,
			CyVisualPropertiesElement.ASPECT_NAME,
			"visualProperties",
			AbstractTableVisualProperty.ASPECT_NAME
	};
	
	private Map<Long, Long> suid_to_cxid_map;
	
	protected final Map<String, Collection<AspectElement>> opaqueAspects;
	private final Map<Long, NiceCySubNetwork> subnetworks;
	protected final boolean isCollection;
	protected final Map<Long, NiceCyNode> root_nodes;
	protected final Map<Long, NiceCyEdge> root_edges;
	protected final Map<Long, NiceCyGroup> root_groups;
	
	//table styles
	// Keys: 0. subnetId(-1) if not a collection 1. table type, 2. Column name, 3. Visual property name
	private Map<Long,Map<DefaultTableType, Map<String, Map<String,TableColumnVisualStyle>>>> tableVisualStyles;
	
	
	public NiceCyRootNetwork(NiceCXNetwork niceCX) {
		super(CxUtil.DEFAULT_SUBNET);
		subnetworks = new HashMap<>();
		root_nodes = new HashMap<>();
		root_edges = new HashMap<>();
		root_groups = new HashMap<>();
		tableVisualStyles = new HashMap<>();

		opaqueAspects = niceCX.getOpaqueAspectTable();
		isCollection = opaqueAspects.containsKey(SubNetworkElement.ASPECT_NAME);
		
		logger.info("Converting NiceCX to NiceCY: ");
		long t0 = System.currentTimeMillis();
		try {
			// Must run first to detect CX IDs, NetworkRelations, and subnetworks
			handleCxMapping(opaqueAspects.get(CxUtil.CX_ID_MAPPING));
			handleNetworkRelations(opaqueAspects.get(NetworkRelationsElement.ASPECT_NAME));
			
			handleNetworkAttributes(niceCX.getNetworkAttributes());
			handleNodes(niceCX.getNodes());
			handleEdges(niceCX.getEdges());
			handleNodeAttributes(niceCX.getNodeAttributes());
			handleEdgeAttributes(niceCX.getEdgeAttributes());
			handleNodeAssociatedAspects(niceCX.getNodeAssociatedAspects());
			handleEdgeAssociatedAspects(niceCX.getEdgeAssociatedAspects());
			handleOpaqueAspects();
		}catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to process JSON in CX: " + e.getMessage());
		}
		TimingUtil.reportTimeDifference(t0, "Convert to NiceCY", -1);
		
	}
	
	private void handleOpaqueAspects() throws JsonProcessingException{
		
		handleCyTableColumns(opaqueAspects.get(CyTableColumnElement.ASPECT_NAME));
		handleGroups(opaqueAspects.get(CyGroupsElement.ASPECT_NAME));		
		handleHiddenAttributes(opaqueAspects.get(HiddenAttributesElement.ASPECT_NAME));
		handleSubNetworks(opaqueAspects.get(SubNetworkElement.ASPECT_NAME));
		handleNamespaces(opaqueAspects.get(NamespacesElement.ASPECT_NAME));
		
		//Handle old name for CyVisualProperties
		Collection<AspectElement> visualProps = opaqueAspects.get(CyVisualPropertiesElement.ASPECT_NAME);
		if (visualProps == null) {
			visualProps = opaqueAspects.get("visualProperties");
		}
		handleCyVisualProperties(visualProps);
		handleCyTableVisualProperties(opaqueAspects.get(AbstractTableVisualProperty.ASPECT_NAME));
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
		NiceCyNetwork net = getNetwork(null);
		boolean hasContextAttribute = net.attributes.stream().anyMatch(nae -> nae.getName().equals(NamespacesElement.ASPECT_NAME));
		if (!hasContextAttribute) {
			NetworkAttributesElement value = new NetworkAttributesElement(this.getId(), NamespacesElement.ASPECT_NAME, contextStr);
			net.attributes.add(value);
		}
	}
	
	public static String serializeNamespaces(Collection<AspectElement> collection) throws JsonProcessingException {
		JsonObject obj = new JsonObject();
 		for (AspectElement el : collection) {
 			OpaqueElement op = (OpaqueElement) el;
 			JsonNode node = op.getData();
 			
 			node.fields().forEachRemaining(entry -> {
 				JsonNode val_node = entry.getValue();
 				JsonPrimitive val = new JsonPrimitive(val_node.asText());
 				obj.add(entry.getKey(), val);
 			});
 		}
 		Gson gson = new Gson();
    	return gson.toJson(obj);
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
				if (root_groups.containsKey(id)) {
					network.groups.add(id);
				}
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
			if ( (!hae.getName().equals(CxUtil.UUID_COLUMN)) && (!hae.getName().equals(CxUtil.MODIFICATION_COLUMN))) {
				NiceCyNetwork net = getNetwork(hae.getSubnetwork());
				net.hiddenAttributes.add(hae);
			}
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
	
	private void handleCyTableVisualProperties(Collection<AspectElement> tableStyles) {
		if (tableStyles != null && !tableStyles.isEmpty()) {
			for (AspectElement e : tableStyles) {
				CyTableVisualPropertiesElement tableStyle = (CyTableVisualPropertiesElement) e;
				this.tableVisualStyles.put(tableStyle.getSubnetId(),tableStyle.getTableStyles());
			}
		}
	}
	
	private void handleNodeAssociatedAspects(Map<String, Map<Long, Collection<AspectElement>>> nodeAssociatedAspects) {
		
		nodeAssociatedAspects.forEach((name, map) -> {
			switch(name) {
				case CartesianLayoutElement.ASPECT_NAME:
					handleCartesianLayout(map);
					break;
				default:
					logger.info("Not handling node associcated " + name);
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
			logger.info("Not handling edge associcated " + name);
		});
	}

	private void handleNodeAttributes(Map<Long, Collection<NodeAttributesElement>> nodeAttributes) {
		nodeAttributes.forEach((suid, attrs) -> {
			long id = getCxId(suid);
			attrs.forEach(attr -> {
				try {
					attr.setPropertyOf(id);
					NiceCyNetwork net = getNetwork(attr.getSubnetwork());
					if (net == null) {
						throw new RuntimeException("No network found for SUID " + attr.getSubnetwork() + ". Check your CX attribute " + attr);
					}
					if (!net.nodeAttributes.containsKey(id)) {
						net.nodeAttributes.put(id, new ArrayList<NodeAttributesElement>());
					}
					net.nodeAttributes.get(id).add(attr);
				}catch (NullPointerException e) {
					throw new RuntimeException("Error processing attribute: " + attr);
				}
			});
			
		});
	}
	private void handleEdgeAttributes(Map<Long, Collection<EdgeAttributesElement>> edgeAttributes) {
		edgeAttributes.forEach((suid, attrs) -> {
			long id = getCxId(suid);
			
			attrs.forEach(attr -> {
//				attr.setPropertyOf(id); 
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
		Long t0 = System.currentTimeMillis();
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
		TimingUtil.reportTimeDifference(t0, "CX Mapping", -1);
	}

	private void handleNodes(Map<Long, NodesElement> nodes) {
		Long t0 = System.currentTimeMillis();
		nodes.forEach((suid, node) -> {
			Long id = getCxId(suid);
			root_nodes.put(id, new NiceCyNode(id, node.getNodeName(), node.getNodeRepresents()));
		});
		TimingUtil.reportTimeDifference(t0, NodesElement.ASPECT_NAME, -1);
	}
	
	private void handleEdges(Map<Long, EdgesElement> edges) {
		if (edges == null) {
			return;
		}
		Long t0 = System.currentTimeMillis();
		edges.forEach((suid, edge) -> {
			Long id = getCxId(suid);
			
			Long source = getCxId(edge.getSource());
			Long target = getCxId(edge.getTarget());
			
			root_edges.put(id, new NiceCyEdge(id, this, source, target, edge.getInteraction()));
		});
		TimingUtil.reportTimeDifference(t0, EdgesElement.ASPECT_NAME, -1);
	}

	private void handleCyTableColumns(Collection<AspectElement> aspects) {
		if (aspects == null) {
			return;
		}
		Long t0 = System.currentTimeMillis();
		aspects.forEach(aspect -> {
			CyTableColumnElement ctce = (CyTableColumnElement) aspect;
			NiceCyNetwork net = getNetwork(ctce.getSubnetwork());
			net.tableColumns.add(ctce);
		});
		TimingUtil.reportTimeDifference(t0, CyTableColumnElement.ASPECT_NAME, -1);
	}

	private void handleNetworkAttributes(Collection<NetworkAttributesElement> networkAttributes) {
		if (networkAttributes == null) {
			return;
		}
		Long t0 = System.currentTimeMillis();
		networkAttributes.forEach(attr -> {
			getNetwork(attr.getSubnetwork()).attributes.add(attr);
		});
		TimingUtil.reportTimeDifference(t0, NetworkAttributesElement.ASPECT_NAME, -1);
	}

	/**
	 * Build subnetwork and view maps
	 * @param aspects
	 */
	private void handleNetworkRelations(Collection<AspectElement> aspects) {
		if (aspects != null) {
			Long t0 = System.currentTimeMillis();
			// Create subnetworks
			aspects.stream().filter(aspect -> {
				return ((NetworkRelationsElement) aspect).getRelationship().equals(NetworkRelationsElement.TYPE_SUBNETWORK);
			}).forEach(aspect -> {
				NetworkRelationsElement nre = (NetworkRelationsElement) aspect;
				Long suid = nre.getChild();
				NiceCySubNetwork network = new NiceCySubNetwork(suid, this);
				
				NetworkAttributesElement nae = new NetworkAttributesElement(suid, CyNetwork.NAME, nre.getChildName());
				network.attributes.add(nae);
				
				if (!isCollection || getNetworkName() == null) {
					attributes.add(nae);
				}
				subnetworks.put(suid, network);
			});
			
			// Create views
			aspects.stream().filter(aspect -> {
				return ((NetworkRelationsElement) aspect).getRelationship().equals(NetworkRelationsElement.TYPE_VIEW);
			}).forEach(aspect -> {
				NetworkRelationsElement nre = (NetworkRelationsElement) aspect;
				Long suid = nre.getChild();
				Long net = nre.getParent();
				NiceCySubNetwork subnet = (NiceCySubNetwork) getNetwork(net);
				NiceCyView view = new NiceCyView(suid, subnet, nre.getChildName());
				subnet.views.put(suid, view);
			});
			TimingUtil.reportTimeDifference(t0, SubNetworkElement.ASPECT_NAME, -1);
		}
		
		//Add a default subnetwork if the aspect doesn't exist or lists no subnets
		if (subnetworks.isEmpty()) {
			NiceCySubNetwork subnet = new NiceCySubNetwork(CxUtil.DEFAULT_SUBNET, this);
			subnet.views.put(CxUtil.DEFAULT_VIEW, new NiceCyView(CxUtil.DEFAULT_VIEW, subnet, null));
			subnetworks.put(CxUtil.DEFAULT_SUBNET, subnet);
		}
		
	}
	
	protected NiceCyNetwork getNetwork(Long suid) {
		if (suid == null) {
			if (!isCollection){// || subnetworks.size() == 1) {
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
			throw new IllegalArgumentException("Unable to find suid " + suid + " in CX ID Mapping.");
		}
		return suid_to_cxid_map.get(suid);
	}
	
	private NiceCyView getViewWithId(Long view) {
		
		if (view == null) {
			if (!isCollection) {
				return subnetworks.get(CxUtil.DEFAULT_SUBNET).views.get(CxUtil.DEFAULT_VIEW);
			}
			List<NiceCyView> views = new ArrayList<NiceCyView>();
			for (NiceCySubNetwork subnet : subnetworks.values()) {
				views.addAll(subnet.views.values());
			}
			if (views.size() == 1) {
				return views.get(0);
			}
			
		}else {
			for (NiceCySubNetwork subnet : subnetworks.values()) {
				if (subnet.views.containsKey(view)) {
					return subnet.views.get(view);
				}
			}
		}
		
		
		throw new RuntimeException("No view found with ID " + view);
	}

	public List<CyNetwork> apply() {
		if (subnetworks.isEmpty()) {
			throw new RuntimeException("No networks were detected in the CX");
		}
		
		long t0 = System.currentTimeMillis();
		List<CyNetwork> networks = new ArrayList<>();
		
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		
		CyNetwork base = network_factory.createNetwork();
		CyRootNetwork root = ((CySubNetwork)base).getRootNetwork();
		networks.add(base);
		
		// Build Root network information
		this.network = root;
		
		//Add root network columns here, otherwise, subnetworks will have nowhere to put their network table attributes.
		addRootNetworkColumns();
		
		// Build subnetworks (this builds all nodes and edges)
		Iterator<NiceCySubNetwork> nice_subs = subnetworks.values().iterator();
		NiceCySubNetwork nice_sub = nice_subs.next();
		nice_sub.apply((CySubNetwork) root.getBaseNetwork());
		
		
		
		while (nice_subs.hasNext()) {
			nice_sub = nice_subs.next();
			CyNetwork currentNetwork = root.addSubNetwork();
			nice_sub.apply((CySubNetwork) currentNetwork);
			networks.add(currentNetwork);
		}
		
		//add collection level attributes (must be done after nodes/edges are created)
		addTableColumns();
		addAttributes();
				
		serializeOpaqueAspects();
		TimingUtil.reportTimeDifference(t0, "time to build cynetwork(s)", -1);
		
		return networks;
	}
	
	public void addTableVisualStyles(CyNetwork currentNetwork) throws Exception {
		if (this.tableVisualStyles.isEmpty()) return;
		
		Map<DefaultTableType, Map<String, Map<String,TableColumnVisualStyle>>> currentNetTableStyles; 
		if ( isCollection ) {
			Long subNetId = getSubNetId(currentNetwork);
			currentNetTableStyles =this.tableVisualStyles.get(subNetId);
		} else {
			currentNetTableStyles = tableVisualStyles.get(null);
		}
		
		Map<String, Map<String,TableColumnVisualStyle>> tableStyles = currentNetTableStyles.get(DefaultTableType.Network);
		if ( tableStyles != null) {
			addStyleToTable(currentNetwork.getDefaultNetworkTable(), tableStyles);
		}
		
		tableStyles = currentNetTableStyles.get(DefaultTableType.Node);
		if ( tableStyles != null) {
			addStyleToTable(currentNetwork.getDefaultNodeTable(), tableStyles);
		}
		
		tableStyles = currentNetTableStyles.get(DefaultTableType.Edge);
		if ( tableStyles != null) {
			addStyleToTable(currentNetwork.getDefaultEdgeTable(), tableStyles);
		}
	}
	
	private Long getSubNetId ( CyNetwork currentNetwork) {
	
		for (Map.Entry<Long, NiceCySubNetwork> entry : this.subnetworks.entrySet()) {
			NiceCyNetwork subnet = entry.getValue();
			if (subnet.network == null) {
				throw new RuntimeException("No CySubNetwork created for " + subnet);
			}
			if (subnet.network.equals(currentNetwork)) {
				return entry.getKey();
			}
		}
		throw new RuntimeException("No CySubNetwork found for " + currentNetwork);
	}

	public static <K,T> void addStyleToTable(CyTable table, Map<String, Map<String,TableColumnVisualStyle>> nodeTableStyles) throws Exception {
		var appManager = CyServiceModule.getService(CyApplicationManager.class);
        var tableViewManager = CyServiceModule.getService(CyTableViewManager.class);
		var tableViewFactory = CyServiceModule.getService(CyTableViewFactory.class);
        var tableVisualMappingManager = CyServiceModule.getService(TableVisualMappingManager.class);
    	VisualStyleFactory visualStyleFactory = CyServiceModule.getService(VisualStyleFactory.class);
    	
    	VisualMappingFunctionFactory vmfFactoryC = CyServiceModule.getContinuousMapping();
        VisualMappingFunctionFactory vmfFactoryD = CyServiceModule.getDiscreteMapping();
        VisualMappingFunctionFactory vmfFactoryP = CyServiceModule.getPassthroughMapping();

		if (table != null) {
			
			CyTableView tableView = tableViewManager.getTableView(table);
            
			if ( tableView == null) {
				tableView = tableViewFactory.createTableView(table);
				tableViewManager.setTableView(tableView);
			}
			TableViewRenderer renderer = appManager.getTableViewRenderer(tableView.getRendererId());

			RenderingEngineFactory<CyTable> factory = renderer
					.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
			VisualLexicon lexicon = factory.getVisualLexicon();

			for (Map.Entry<String,Map<String,TableColumnVisualStyle>> e : nodeTableStyles.entrySet()) {
				String colName = e.getKey();
				View<CyColumn> colView = tableView.getColumnView(colName);

				VisualStyle columnStyle = tableVisualMappingManager.getVisualStyle(colView);

				if ( columnStyle == null)  {
			    	columnStyle = visualStyleFactory.createVisualStyle(UUID.randomUUID().toString());
			    	tableVisualMappingManager.setVisualStyle(colView, columnStyle);
				}
				Map<String, TableColumnVisualStyle> colStyles = e.getValue();
				for ( Map.Entry<String,TableColumnVisualStyle> e2 : colStyles.entrySet()) {
					String vpName = e2.getKey();
					TableColumnVisualStyle style = e2.getValue();
					
					VisualProperty<T> vp = (VisualProperty<T>)lexicon.lookup(CyColumn.class, vpName);

					Object v = style.getDefaultValue(); 
                    VisualPropertyMapping mapping = style.getMapping();
					
					if ( vp!= null) {
						if ( v!= null) {
							T finalV = CxUtil.cvtCX2ObjToVisualPropertyValue(v, vp);
							columnStyle.setDefaultValue(vp, finalV);
						}
						if ( mapping !=null) {
							Class attrDataType = table.getColumn(mapping.getMappingDef().getAttributeName()).getType();
							if (mapping.getType() == VPMappingType.PASSTHROUGH) {
								PassthroughMapping<?,T> pmf = (PassthroughMapping<?,T>) 
										vmfFactoryP.createVisualMappingFunction(
												mapping.getMappingDef().getAttributeName(), 
												attrDataType, vp);
								columnStyle.addVisualMappingFunction(pmf);
							} else if ( mapping.getType() == VPMappingType.DISCRETE) {
								DiscreteMapping<K,T> dmf = (DiscreteMapping<K,T>) 
										vmfFactoryD.createVisualMappingFunction(
												mapping.getMappingDef().getAttributeName(), 
												attrDataType, vp);
								for (Map<String, Object> dMappingEntry : mapping.getMappingDef().getMapppingList()) {
									//Object rowValue = ;
									T vpValue = CxUtil.cvtCX2ObjToVisualPropertyValue(dMappingEntry.get("vp"), vp);
									dmf.putMapValue((K)dMappingEntry.get("v"), vpValue);
								}	
								columnStyle.addVisualMappingFunction(dmf);
							} else if (mapping.getType() == VPMappingType.CONTINUOUS) {
								ContinuousMapping cmf = (ContinuousMapping) 
										vmfFactoryC.createVisualMappingFunction(
												mapping.getMappingDef().getAttributeName(), 
												attrDataType, vp);
								
								
					            int counter = 0;

								int cyCounter = 0;
								T L = null;
								T E = null;
								T G = null;
								Object ov = null;
								for ( Map<String,Object> m : mapping.getMappingDef().getMapppingList() ) {
									
									Object minV = m.get("min");
									Object maxV = m.get("max");
									Boolean includeMin = (Boolean)m.get("includeMin");
									Boolean includeMax = (Boolean)m.get("includeMax");
									Object minVP = m.get("minVPValue");
									Object maxVP = m.get("maxVPValue");
									
									if ( minVP == null && maxVP == null)
										throw new Exception ("minVPValue and maxVPValue are both missing in CONTINUOUS mapping of " + vpName + " on column " + colName);
									
									if ( counter == 0) { // first range
									    L = CxUtil.cvtCX2ObjToVisualPropertyValue(maxVP, vp);
									    ov = maxV;
									    if ( includeMax.booleanValue()) 
									    	E = L;
									} else {  // middle ranges and the last range
										G = CxUtil.cvtCX2ObjToVisualPropertyValue(minVP, vp);
										if (includeMin.booleanValue())
											E=G;
						                
						                BoundaryRangeValues<T> point = new BoundaryRangeValues<>(L, E, G);
				                        cmf.addPoint(ov, point);
						                // prepare for the next point
						                if ( maxV != null) {
						                	ov = maxV;
						                	L = CxUtil.cvtCX2ObjToVisualPropertyValue(maxVP, vp);
						                	if (includeMax.booleanValue())
						                		E = L;
						                	else 
						                		E = null;
						                }	
						                
									}
									counter++;
								}	
						       // vp.putMapping(cx1VPName, VPMappingType.CONTINUOUS.toString(), sb.toString());
								columnStyle.addVisualMappingFunction(cmf);
							} 
						}
					}
					
				}				
			}
		}	

	}
	
	protected void addRootNetworkColumns() {
		tableColumns.stream().filter( x -> "network_table".equals(x.getAppliesTo())).forEach(column -> {
			
			CyTable table = network.getTable(CyNetwork.class, CyRootNetwork.DEFAULT_ATTRS);
			String name = column.getName();
			
			if (table.getColumn(name) == null) {
				CxUtil.createColumn(table, name, CxUtil.getDataType(column.getDataType()), column.isSingleValue());
			}
		}
		);
	}
	
protected void addTableColumns() {
		
		tableColumns.stream().filter( x -> !"network_table".equals(x.getAppliesTo())).forEach(column -> {
			
			CyTable table;
			String name = column.getName();
			
			final boolean isLocal = column.getSubnetwork() != null;
			
			switch(column.getAppliesTo()) {
			case "node_table":
				table = isLocal ? network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS) : network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
				break;
			case "edge_table": 
				table = isLocal ? network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS) : network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
				break;
			default:
					throw new IllegalArgumentException("Unrecognized CyTableColumn applies_to: " + column.getAppliesTo());
			}
			
			if (table.getColumn(name) == null) {
				CxUtil.createColumn(table, name, CxUtil.getDataType(column.getDataType()), column.isSingleValue());
			}
		});
	}
	
	private void serializeOpaqueAspects() {
		Long t0 = System.currentTimeMillis();
		NiceCyNetwork subnet = getNetwork(null);
		opaqueAspects.forEach((name, opaque) -> {
			if (ArrayUtils.contains(UNSERIALIZED_OPAQUE_ASPECTS, name)) {
				// Do not serialize some opaque aspects
				return;
			}
			try {
				subnet.serializeAspect(CxUtil.OPAQUE_ASPECT_PREFIX + name, opaque);
			} catch (IOException e) {
				logger.warn("Failed to serialize opaque aspect: " + name);
			}
		});
		TimingUtil.reportTimeDifference(t0, "Opaque Elements", -1);
	}

	@Override
	protected String getNamespace() {
		return CyRootNetwork.SHARED_ATTRS;
	}

	public List<CyNetworkView> createViews(CyNetwork network, Boolean explicitCreateViews) {
		List<CyNetworkView> views = new ArrayList<CyNetworkView>();
		subnetworks.forEach((suid, subnet) -> {
			if (subnet.network == null) {
				throw new RuntimeException("No CySubNetwork created for " + subnet);
			}
			if (subnet.network.equals(network)) {
				views.addAll(subnet.createViews(explicitCreateViews));
			}
		});
		return views;
	}
	
	public Collection<NiceCyEdge> getRootEdges() {
		return root_edges.values();
	}
	
	public Collection<NiceCyNode> getRootNodes() {
		return root_nodes.values();
	}
	
	public Collection<NiceCyGroup> getRootGroups() {
		return root_groups.values();
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
	
	
	public Collection<NiceCySubNetwork> getSubnetworks() {
		return subnetworks.values();
	}
	
	@Override
	public String getNetworkName() {
		if (!isCollection) {
			if (!subnetworks.isEmpty()) {
				return subnetworks.values().iterator().next().getNetworkName();
			}
		}
		return super.getNetworkName();
	}

	/**
	 * Apply the network and view IDs of one NiceCyRootNetwork to another,
	 * using name attributes to map networks and views together.
	 * 
	 * @param other
	 */
	public void updateNetworkAndViewIds(NiceCyRootNetwork other) {
		this.id = other.getId();
		Map<String, NiceCySubNetwork> netNameMap = new HashMap<>();
		for (NiceCySubNetwork net : other.subnetworks.values()) {
			String name = net.getNetworkName();
			if (netNameMap.containsKey(name)) {
				throw new RuntimeException("Cannot align networks with duplicate names");
			}
			netNameMap.put(name, net);
		}
		
		for (NiceCySubNetwork net : subnetworks.values()) {
			String name = net.getNetworkName();
			if (!netNameMap.containsKey(name)) {
				throw new RuntimeException("Network named " + name + " not in other");
			}
			
			// Replace in subnetwork mapping, update views, and remove from name map to avoid duplicates
			subnetworks.remove(net.getId());
			NiceCySubNetwork otherNet = netNameMap.get(name);
			net.updateViewIds(otherNet);
			subnetworks.put(net.getId(), net);
			netNameMap.remove(name);
		}
		
	}

	public void setNetworkName(String _network_collection_name) {
		attributes.add(new NetworkAttributesElement(null, CyNetwork.NAME, _network_collection_name));
		attributes.add(new NetworkAttributesElement(null, CyRootNetwork.SHARED_NAME, _network_collection_name));
				
	}
}
