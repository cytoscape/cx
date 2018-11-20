package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.AbstractAttributesAspectElement;
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
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NdexNetworkStatus;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.Provenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CxToCy {
    
	private static final Logger logger = LoggerFactory.getLogger(CxToCy.class);

	private static final Long          DEFAULT_SUBNET = Long.valueOf(-Long.MAX_VALUE);
	private static final Long          DEFAULT_VIEW = Long.valueOf(-Long.MAX_VALUE);
    

    private Set<CyNode>                _nodes_with_visual_properties;
    private Set<CyEdge>                _edges_with_visual_properties;
    private VisualElementCollectionMap _visual_element_collections;
    
    // Map subnetwork SUIDs to CX IDs
    private Map<Long, Long> _suid_to_cxid_map;
    
	private Map<Long, CyNode>          _cxid_to_cynode_map;
	private Map<Long, CyEdge>          _cxid_to_cyedge_map;
	
	// View <> subnet ID mappings
    private Map<Long, Long>            _view_to_subnet_map;
    private Map<Long, List<Long>>      _subnet_to_views_map;
    
    
    /* Getters */
    
    public Map<Long, CyNode> get_cxid_to_cynode_map() {
		return _cxid_to_cynode_map;
	}

	public Map<Long, CyEdge> get_cxid_to_cyedge_map() {
		return _cxid_to_cyedge_map;
	}
	
    public final Map<Long, Long> getViewToSubNetworkMap() {
        return _view_to_subnet_map;
    }

    public final Map<Long, List<Long>> getSubNetworkToViewsMap() {
        return _subnet_to_views_map;
    }

    public Map<Long, Long> getNetworkSuidToNetworkRelationsMap() {
		return _suid_to_cxid_map;
	}
    
	private CySubNetwork buildNetwork(CyRootNetwork root_network,
						            final CyNetworkFactory network_factory,
						            final String collection_name,
						            final Long cx_id) {
		// Build a subnetwork in the specified collection, or create a new collection with the given name if it doesn't exist
		CySubNetwork sub_network;
        if (root_network != null) {
            // Root network exists
            sub_network = root_network.addSubNetwork();
        }
        else {
            sub_network = (CySubNetwork) network_factory.createNetwork();
            root_network = sub_network.getRootNetwork();
            if (!CxioUtil.isEmpty(collection_name)) {
                root_network.getRow(root_network).set(CyNetwork.NAME,
                                                      collection_name);
            }
        }
        if (sub_network == null) {
        	throw new IllegalArgumentException("Failed to create subnetwork for root " + root_network);
        }
        Settings.INSTANCE.debug(String.format("Adding %s to root %s", sub_network, root_network));
        _suid_to_cxid_map.put(sub_network.getSUID(), cx_id);
        return sub_network;
	}
	
	
	
	private final void processViewRelation(NetworkRelationsElement nre) {
		if (!nre.getRelationship().equals(NetworkRelationsElement.TYPE_VIEW)) {
			throw new IllegalArgumentException("Trying to add non-view relationship as view. This shouldn't happen");
		}
		Long view_id = nre.getChild();
		Long subnet_id = nre.getParent();
		
		view_id = view_id == null ? DEFAULT_VIEW : view_id;
		subnet_id = subnet_id == null ? DEFAULT_SUBNET : subnet_id;
		
		if (!_subnet_to_views_map.containsKey(subnet_id)) {
			_subnet_to_views_map.put(subnet_id, new ArrayList<Long>());
		}
		
		_subnet_to_views_map.get(subnet_id).add(view_id);
		_view_to_subnet_map.put(view_id, subnet_id);
	}
	
	private final CyRootNetwork processNetworkRelation(NetworkRelationsElement nre,
			CyRootNetwork root_network,
			Map<Long, CyNetwork> networks,
			CyNetworkFactory network_factory,
			String collection_name) {
		switch(nre.getRelationship()) {
		case NetworkRelationsElement.TYPE_SUBNETWORK:
			Long cx_id = nre.getChild();
			CySubNetwork network = buildNetwork(root_network, network_factory, collection_name, cx_id);
			if (root_network == null) {
				root_network = network.getRootNetwork();
			}
			if (!networks.containsKey(DEFAULT_SUBNET)) {
				networks.put(DEFAULT_SUBNET, root_network);
			}
			if (nre.getChildName() != null) {
				network.getRow(network).set(CyNetwork.NAME, nre.getChildName());
			}
			networks.put(cx_id, network);
			break;
		case NetworkRelationsElement.TYPE_VIEW:
			processViewRelation(nre);
			break;
		}
		return root_network;
	}
	
	private final Map<Long, CyNetwork> processNetworkRelations(Collection<AspectElement> aspect,
														CyRootNetwork root_network,
											            final CyNetworkFactory network_factory,
											            final String collection_name){
		// Create subnetworks outlined by NetworkRelations aspect
		// If the aspect exists, it is a collection
		
		Map<Long, CyNetwork> networks = new HashMap<Long, CyNetwork>();
		if (aspect == null) { // Single subnetwork
			CySubNetwork network = buildNetwork(root_network, network_factory, collection_name, DEFAULT_SUBNET);
			networks.put(DEFAULT_SUBNET, network);
			List<Long> views = new ArrayList<Long>();
			views.add(DEFAULT_VIEW);
			_subnet_to_views_map.put(DEFAULT_SUBNET, views);
			_view_to_subnet_map.put(DEFAULT_VIEW, DEFAULT_SUBNET);
		}else { // Collection
			for (AspectElement e : aspect) {
				NetworkRelationsElement nre = (NetworkRelationsElement) e;
				root_network = processNetworkRelation(nre, root_network, networks, network_factory, collection_name);

			}
		}
        Settings.INSTANCE.debug(String.format("Found %s network(s). Collection: %s\nNetworks: %s", networks.size(), aspect != null, networks));

		
		return networks;
	}
	
    public final List<CyNetwork> createNetwork(final NiceCXNetwork niceCX, 
                                               CyRootNetwork root_network,
                                               final CyNetworkFactory network_factory,
                                               final CyGroupFactory group_factory,
                                               final String collection_name) throws IOException {

        if ( niceCX.getNodes().isEmpty()) {
            throw new IOException("No nodes in input");
        }

        _suid_to_cxid_map = new HashMap<>();
        _visual_element_collections = new VisualElementCollectionMap();
        _cxid_to_cynode_map = new HashMap<>();
        _cxid_to_cyedge_map = new HashMap<>();
        _subnet_to_views_map = new HashMap<>();
        _view_to_subnet_map = new HashMap<>();
        
        // create subnetwork(s) that will be populated by niceCX
        Collection<AspectElement> network_relations = niceCX.getOpaqueAspectTable().get(NetworkRelationsElement.ASPECT_NAME);
        boolean isCollection = network_relations != null;
        Map<Long, CyNetwork> cx_network_map = processNetworkRelations(network_relations, root_network, network_factory, collection_name);
        
        if (!isCollection) {
        	SubNetworkElement sub_net_element = new SubNetworkElement(DEFAULT_SUBNET);
        	sub_net_element.setEdgesAll(true);
        	sub_net_element.setNodesAll(true);
        	_visual_element_collections.addSubNetworkElement(DEFAULT_SUBNET, sub_net_element);
        }else {
        	if (niceCX.getOpaqueAspectTable().get(SubNetworkElement.ASPECT_NAME) != null) {
    			for (final AspectElement element : niceCX.getOpaqueAspectTable().get(SubNetworkElement.ASPECT_NAME)) {
    				final SubNetworkElement subnetwork_element = (SubNetworkElement) element;
    				_visual_element_collections.addSubNetworkElement(subnetwork_element.getId(), subnetwork_element);
    			}
    		}
        }
        
        
        // cyTableColumns -> create columns in data tables
        processTableColumns(niceCX.getOpaqueAspectTable().get(CyTableColumnElement.ASPECT_NAME), cx_network_map, isCollection);
        
        // networkAttrs -> Network default table
        processNetworkAttributes(niceCX.getNetworkAttributes(), cx_network_map, isCollection);
        
        // nodes, edges, and their attributes
        processNodesAndEdges(niceCX, cx_network_map, isCollection);
        
        // visual layout information, hidden attributes, opaques, and group nodes
        processOpaqueAspects(niceCX.getOpaqueAspectTable(), cx_network_map, group_factory);
        
        //cartesianLayout
        addPositions(niceCX.getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME));
        
        
        ArrayList<CyNetwork> networks = new ArrayList<CyNetwork>();
        cx_network_map.values().stream().forEach(network -> {
        	if (network instanceof CySubNetwork) {
        		networks.add(network);
        	}
        });
        
        if (Settings.INSTANCE.isDebug()) {
        	System.out.println("SUID to CXID map: " + _suid_to_cxid_map);
        	System.out.println("Subnetwork to views map: " + _subnet_to_views_map);
        }
        
        return networks;
    }
    
    private final void processOpaqueAspects(Map<String, Collection<AspectElement>> opaque_table, 
    		Map<Long, CyNetwork> cx_network_map, CyGroupFactory group_factory) throws IOException {
    	   
		//      opaque aspects -> serialize to network hidden table
		//    	cyVisualProperties -> create Styling for network
		//      cyHiddenAttrs -> network hidden table
		//    	cyGroups -> create from nodes and edges
		//		@context (?) -> serialize into network default table
		//      including metaData
		//      omit: status, ndexStatus, numberVerification, provenanceHistory, cyTableColumns, cyViews
		      
		      
	  for (Entry<String, Collection<AspectElement>> aspect : opaque_table.entrySet()) {
	  	switch( aspect.getKey()) {
	  	case HiddenAttributesElement.ASPECT_NAME:
	  		processHiddenAttributes(aspect.getValue(), cx_network_map);
	  		break;
	  	case CyVisualPropertiesElement.ASPECT_NAME:
	  		processVisualProperties(aspect.getValue());
	  		break;
	  	case CyGroupsElement.ASPECT_NAME:
	  		addGroups(group_factory, aspect.getValue(), cx_network_map);
	  		break;
	  	case NamespacesElement.ASPECT_NAME:
	  		CyTable net_table = cx_network_map.get(DEFAULT_SUBNET).getDefaultNetworkTable();
	 		if (net_table.getColumn(NamespacesElement.ASPECT_NAME) != null) {
	 			// Only move context if it does not exist in network table
	 			return;
	 		}
	  		serializeAspect(cx_network_map.get(DEFAULT_SUBNET), aspect.getKey(), CyNetwork.DEFAULT_ATTRS, aspect.getValue());
	  		break;
	  	case "status":
	  	case NdexNetworkStatus.ASPECT_NAME:
	  	case NumberVerification.NAME:
	  	case Provenance.ASPECT_NAME:
	  	case CyViewsElement.ASPECT_NAME:
	  	case CyTableColumnElement.ASPECT_NAME:
	  	case SubNetworkElement.ASPECT_NAME:
	  	case NetworkRelationsElement.ASPECT_NAME:
	  		break;
	  	case MetaDataCollection.NAME:
	  	default:
	  		serializeAspect(cx_network_map.get(DEFAULT_SUBNET), CxUtil.OPAQUE_ASPECT_PREFIX + aspect.getKey(), CyNetwork.HIDDEN_ATTRS, aspect.getValue());
	  		break;
	  	}
	  }
	}
    
    private void processTableColumns(Collection<AspectElement> collection, Map<Long, CyNetwork> cx_network_map,
			boolean isCollection) {
		if (collection == null) {
			return;
		}
		for (AspectElement element : collection) {
			CyTableColumnElement tce = (CyTableColumnElement) element;
			Long cx_subnet_id = tce.getSubnetwork();
			
			String namespace = CyNetwork.LOCAL_ATTRS;
			if (cx_subnet_id == null) {
				cx_subnet_id = DEFAULT_SUBNET;
				namespace = CyNetwork.DEFAULT_ATTRS;
			}
			
			CyNetwork network = cx_network_map.get(cx_subnet_id);
			if (network == null) {
				throw new IllegalArgumentException(String.format(
						"Could not find network for CyTableColumn %s", cx_subnet_id));
			}
			
			processTableColumnHelper(tce, network, namespace);
		}
    }
    
    private void processTableColumnHelper(CyTableColumnElement tce, CyNetwork network, String namespace) {
        final String name = tce.getName();
        if (name == null || name.equals(CxUtil.SUID)) {
        	return;
        }
        final ATTRIBUTE_DATA_TYPE dt = tce.getDataType();
        
        final boolean is_single = dt.isSingleValueType();
        final Class<?> data_type = getDataType(dt);
        CyTable table = null;
        
        switch(tce.getAppliesTo()) {
	        case "node_table":
	        	table = network.getTable(CyNode.class,
	                    namespace);
	        	break;
	        case "edge_table":
	        	table = network.getTable(CyEdge.class,
	        			namespace);
	        	break;
	        case "network_table":
	        	table = network.getTable(CyNetwork.class,
	        			namespace);
	        	break;
	        default:
	        	throw new IllegalArgumentException("Unrecognized 'applies_to' value for " + name + ": " + tce.getAppliesTo());
        }
        
        createColumn(is_single,
                     data_type,
                     name,
                     table);
        
                
    }
    
    private static void createColumn(final boolean is_single,
            final Class<?> data_type,
            final String name,
            final CyTable table) {
		if (table == null) {
			return;
		}
		if (table.getColumn(name) != null) {
			return;
		}
		if (is_single) {
	       table.createColumn(name, data_type, false);
	   }else {
	       table.createListColumn(name, data_type, false);
	   }
	}
    
 	
 	private void serializeAspect(CyNetwork network, String column, String namespace, Collection<AspectElement> collection) throws IOException {
 		ObjectMapper mapper = new ObjectMapper();
 		ArrayList<JsonNode> nodes = new ArrayList<>();
 		for (AspectElement el : collection) {
 			OpaqueElement op = (OpaqueElement) el;
 			nodes.add(op.getData());
 		}
 		CyTable table = network.getTable(CyNetwork.class, namespace);
    	
    	String aspectStr = mapper.writeValueAsString(nodes);
    	if (table.getColumn(column) == null) {
    		table.createColumn(column, String.class, true);
    	}
    	table.getRow(network.getSUID()).set(column, aspectStr);
 		
 	}
    
    private void processNodesAndEdges(NiceCXNetwork niceCX, Map<Long, CyNetwork> cx_network_map, boolean isCollection) {
    	
    	for (Entry<Long, CyNetwork> entry : cx_network_map.entrySet()) {
        	if (entry.getValue() instanceof CyRootNetwork) {
        		continue;
        	}
        	Long cx_id = entry.getKey();
        	CySubNetwork subnet = (CySubNetwork) entry.getValue();
        	if (_visual_element_collections != null) {
                if (_visual_element_collections.getSubNetworkElement(cx_id) != null) {
                	Set<Long> nodes_in_subnet = null;
                    Set<Long> edges_in_subnet = null;
                    if (isCollection) {
                    	nodes_in_subnet = new HashSet<>(_visual_element_collections.getSubNetworkElement(cx_id)
                            .getNodes());
                    	edges_in_subnet = new HashSet<>(_visual_element_collections.getSubNetworkElement(cx_id)
                            .getEdges());
                    	Settings.INSTANCE.debug(String.format("sub-network %s nodes/edges: %s/%s\n",
                        		cx_id, nodes_in_subnet.size(), edges_in_subnet.size()));
                    }
                    
                    addNodes(subnet,
                            niceCX,
                            nodes_in_subnet,
                            isCollection);

                    addEdges(subnet,
                            niceCX,
                            edges_in_subnet,
                            isCollection);
                }
            }
        }
    }
    
    private void processHiddenAttributes(Collection<AspectElement> hidden_attributes, Map<Long, CyNetwork> cx_network_map) {
    	if (hidden_attributes != null) {
            for (final AspectElement e : hidden_attributes) {
                final HiddenAttributesElement hae = (HiddenAttributesElement) e;
                Long subnet = hae.getSubnetwork();
                if (subnet == null) {
                	 subnet = DEFAULT_SUBNET;
                }

                addHiddenAttributeData(hae, cx_network_map.get(subnet));
            }
        }
    }
    
    private void processVisualProperties(Collection<AspectElement> visualProperties) {
    	_nodes_with_visual_properties = new HashSet<>();
        _edges_with_visual_properties = new HashSet<>();
    	for (final AspectElement element : visualProperties) {
            final CyVisualPropertiesElement vpe = (CyVisualPropertiesElement) element;
            Long view_id = vpe.getView();
            if (view_id == null) {
            	view_id = DEFAULT_VIEW;
            	vpe.setView(view_id);
            }
            
            addVisualProperties(vpe, view_id);
    	}
    }
    
    
    private void addVisualProperties(CyVisualPropertiesElement vpe, Long view_id) {
    	
        // Stores visual properties in map, to be applied later       
        if (vpe.getProperties_of().equals(VisualPropertyType.NETWORK.asString())) {
            _visual_element_collections.addNetworkVisualPropertiesElement(view_id,
                                                                          vpe);
        }
        else if (vpe.getProperties_of().equals(VisualPropertyType.NODES_DEFAULT.asString())) {
            _visual_element_collections.addNodesDefaultVisualPropertiesElement(view_id,
                                                                               vpe);
        }
        else if (vpe.getProperties_of().equals(VisualPropertyType.EDGES_DEFAULT.asString())) {
            _visual_element_collections.addEdgesDefaultVisualPropertiesElement(view_id,
                                                                               vpe);
        }
        else if (vpe.getProperties_of().equals(VisualPropertyType.NODES.asString())) {
            Long applies_to_node = vpe.getApplies_to();
            CyNode node = _cxid_to_cynode_map.get(applies_to_node);
            if (node == null) {
            	throw new IllegalArgumentException("No node exists for CX ID " + applies_to_node);
            }
            _nodes_with_visual_properties.add(node);
            _visual_element_collections.addNodeVisualPropertiesElement(view_id,
                                                                       node,
                                                                       vpe);
            
        }
        else if (vpe.getProperties_of().equals(VisualPropertyType.EDGES.asString())) {
            Long applies_to_edge = vpe.getApplies_to();
            CyEdge edge = _cxid_to_cyedge_map.get(applies_to_edge);
            if (edge == null) {
            	throw new IllegalArgumentException("No edge exists for CX ID " + applies_to_edge);
            }
                _edges_with_visual_properties.add(edge);
                _visual_element_collections.addEdgeVisualPropertiesElement(view_id,
                                                                           edge,
                                                                           vpe);
            
        }
    }

    private void addGroups(final CyGroupFactory group_factory,
                          final Collection<AspectElement> groupElements,
                          final Map<Long, CyNetwork> cx_subnetwork_map) {

        for (final AspectElement a : groupElements) {
        	CyGroupsElement ge = (CyGroupsElement) a;
        	Long subnetwork_id = ge.getSubNet() == null ? DEFAULT_SUBNET : ge.getSubNet();
        	CyNetwork sub_network = cx_subnetwork_map.get(subnetwork_id);
        	if (sub_network instanceof CyRootNetwork) {
        		sub_network = ((CyRootNetwork) sub_network).getBaseNetwork();
        	}
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
    		
    		CyNode node = _cxid_to_cynode_map.get(ge.getGroupId());
    		if (node == null) {
    			Settings.INSTANCE.debug(String.format("Group node %s not listed in 'nodes' and/or 'cySubNetworks' aspect(s)", ge.getGroupId()));
    			node = sub_network.addNode();
    		}
    		
    		CyGroup grp = group_factory.createGroup(sub_network,
							  node,
                              nodes_for_group,
                              null,
                              true);
    		
    		final CyRow row = sub_network.getRow(grp.getGroupNode());
    		row.set(CxUtil.SHARED_NAME_COL, ge.getName());
    		
    		grp.removeGroupFromNetwork(sub_network);
    		grp.expand(sub_network);
    		//grp.expand(sub_network);
//    		if (!ge.isCollapsed()) {
//    			grp.collapse(sub_network);
//    		}
        }
                
    }

    public Set<CyEdge> getEdgesWithVisualProperties() {
        return _edges_with_visual_properties;
    }

    public Set<CyNode> getNodesWithVisualProperties() {
        return _nodes_with_visual_properties;
    }

    public final VisualElementCollectionMap getVisualElementCollectionMap() {
        return _visual_element_collections;
    }
    

    private final void addEdges(final CyNetwork network,
                                final NiceCXNetwork niceCX,
                                final Set<Long> edges_in_subnet,
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
                cy_edge = network.addEdge(source,
                                          target,
                                          true);
                
                // TODO: Create edge name from source, target, and interaction?
                String interaction = edge_element.getInteraction();
                
                if (interaction != null) {
                	network.getRow(cy_edge).set(CyEdge.INTERACTION,
                            interaction);
                	
                	String source_name = network.getRow(source).get(CxUtil.NAME_COL, String.class);
                    String target_name = network.getRow(target).get(CxUtil.NAME_COL, String.class);
                	String name = String.format("%s (%s) %s", source_name, interaction, target_name);
                	network.getRow(cy_edge).set(CxUtil.NAME_COL, name);
                }
                CxUtil.saveCxId(network, cy_edge, edge_id);
                _cxid_to_cyedge_map.put(edge_id,
                                        cy_edge);
            }
            else {
                ((CySubNetwork) network).addEdge(cy_edge);
            }
            if (!niceCX.getEdgeAttributes().isEmpty()) {
                addEdgeTableData(niceCX.getEdgeAttributes().get(edge_id),
                                 cy_edge,
                                 network,
                                 subnet_info_present);
            }
        }
    }

    private final void addEdgeTableData(final Collection<EdgeAttributesElement> elements,
                                        final CyIdentifiable graph_object,
                                        final CyNetwork network,
                                        final boolean subnet_info_present) {
        if (elements == null) {
            return;
        }
        final CyTable table_default = network.getTable(CyEdge.class,
                                                       CyNetwork.DEFAULT_ATTRS);
        final CyTable table_local = network.getTable(CyEdge.class,
                                                     CyNetwork.LOCAL_ATTRS);
        final CyRow row = network.getRow(graph_object);
        if (row == null) {
        	return;
        }
        for (final EdgeAttributesElement e : elements) {
            if (e == null) {
            	continue;
            }
            CyTable my_table;
            if (!subnet_info_present || e.getSubnetwork() == null) {
                my_table = table_default;
            }
            else {
                my_table = table_local;
            }
            final String name = e.getName();
            if (name != null) {
                if (!name.equals(CyIdentifiable.SUID)) {
                    // New column creation:
                    if (my_table.getColumn(name) == null) {
                        final Class<?> data_type = getDataType(e.getDataType());

                        if (e.isSingleValue()) {
                            my_table.createColumn(name,
                                                  data_type,
                                                  false);
                        }
                        else {
                            my_table.createListColumn(name,
                                                      data_type,
                                                      false);
                        }
                    }
                    final CyColumn col = my_table.getColumn(name);
                    Object val = getValue(e, col);
                    try {
                    	row.set(name, val);
                    }catch (IllegalArgumentException e2) {
                    	throw new IllegalArgumentException(String.format("Edge column %s (%s) cannot be set with %s", 
                    			col.getName(), col.getType(), val.getClass()));
                    }
                }
            }
        }
    }
    

    private final void addHiddenAttributeData(final HiddenAttributesElement element,
                                              final CyNetwork network) {
    	
    	CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
        if (table == null) {
            throw new IllegalArgumentException("table (hidden) must not be null");
        }
        if (element == null) {
            return;
        }
        final CyRow row = table.getRow(network.getSUID());
        if (row != null) {
            addToColumn(table, row, element);
        }
    }

	private final void addNetworkAttributeData(
			final Collection<NetworkAttributesElement> elements, final CyNetwork network,
			final CyTable table) {
		if (table == null) {
			throw new IllegalArgumentException("table (network) must not be null");
		}
		
		if (elements == null) {
			return;
		}
		final CyRow row = table.getRow(network.getSUID());
		if(row == null) {
			return;
		}

		elements.stream()
			.forEach(e -> addToColumn(table, row, e));
		
	}

    private final void addNodes(final CySubNetwork network,
                                final NiceCXNetwork niceCX,
                                final Set<Long> nodes_in_subnet,
                                final boolean subnet_info_present) {

        final CyTable node_table_default = network.getTable(CyNode.class,
                                                            CyNetwork.DEFAULT_ATTRS);

        for (final NodesElement node_element : niceCX.getNodes().values()) {
            final Long node_id = node_element.getId();
            
            if (nodes_in_subnet != null && !nodes_in_subnet.contains(node_id)) {
                continue;
            }
            CyNode cy_node = _cxid_to_cynode_map.get(node_id);
            if (cy_node == null) {
                cy_node = network.addNode();
                if (node_element.getNodeRepresents() != null) {
                    if (node_table_default.getColumn(CxUtil.REPRESENTS) == null) {
                        node_table_default.createColumn(CxUtil.REPRESENTS,
                                                        String.class,
                                                        false);
                    }
                    network.getRow(cy_node).set(CxUtil.REPRESENTS,
                                                node_element.getNodeRepresents());
                }
                if (node_element.getNodeName() != null) {
                    network.getRow(cy_node).set(CxUtil.SHARED_NAME_COL,
                                                node_element.getNodeName());
                    network.getRow(cy_node).set(CxUtil.NAME_COL,
                                                node_element.getNodeName());
                }
                CxUtil.saveCxId(network, cy_node, node_id);
                
                _cxid_to_cynode_map.put(node_id,
                                        cy_node);
            }
            else {
                network.addNode(cy_node);
            }
            
            if ( !niceCX.getNodeAttributes().isEmpty()) {
                addNodeTableData(niceCX.getNodeAttributes().get(node_id),
                                 cy_node,
                                 network,
                                 subnet_info_present);
            }
        }
    }

    private final void addNodeTableData(final Collection<NodeAttributesElement> elements,
                                        final CyIdentifiable graph_object,
                                        final CySubNetwork network,
                                        final boolean subnet_info_present) {
        if (elements == null) {
            return;
        }
        final CyTable table_default = network.getTable(CyNode.class,
                                                       CyNetwork.DEFAULT_ATTRS);
        final CyTable table_local = network.getTable(CyNode.class,
                                                     CyNetwork.LOCAL_ATTRS);
        final CyRow row = network.getRow(graph_object);
        if (row == null) {
        	return;
        }
        for (final NodeAttributesElement e : elements) {
            if (e == null) {
            	continue;
            }
            CyTable my_table;
            if (!subnet_info_present || e.getSubnetwork() == null) {
                my_table = table_default;
            }
            else {
                my_table = table_local;
            }
            final String name = e.getName();
            if (name == null) {
            	continue;
            }
            if (!name.equals(CyIdentifiable.SUID)) {
                // New column creation:
                if (my_table.getColumn(name) == null) {
                    final Class<?> data_type = getDataType(e.getDataType());

                    if (e.isSingleValue()) {
                        my_table.createColumn(name,
                                              data_type,
                                              false);

                    }
                    else {
                        my_table.createListColumn(name,
                                                  data_type,
                                                  false);
                    }
                }
                final CyColumn col = my_table.getColumn(name);
                Object val = getValue(e, col);
                try {
                	row.set(name, val);
                }catch (IllegalArgumentException e2) {
                	throw new IllegalArgumentException(String.format("Node column %s (%s) cannot be set with %s", 
                			col.getName(), col.getType(), val.getClass()));
                }
            }
        }
    }
    
    

    private final void addPositions(final Map<Long, Collection<AspectElement>> layout) {
    	if (layout == null) {
    		return;
    	}
    	for (long node : layout.keySet()) {
	    	for (final AspectElement e : layout.get(node)) {
				final CartesianLayoutElement cle = (CartesianLayoutElement) e;
				Long cx_view = cle.getView();
				
				if (cx_view == null) {
					cx_view = DEFAULT_VIEW;
				}
				_visual_element_collections.addCartesianLayoutElement(cx_view,
	                                                              _cxid_to_cynode_map.get(node),
	                                                              cle);
			}
    	}
    }

	private final void addToColumn(final CyTable table, final CyRow row, final AbstractAttributesAspectElement e) {
		if (e == null) {
			return;
		}

		final String name = e.getName();
		if (name == null) {
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
				if(isSingle && type.startsWith("list_of")) {
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
				if(col == null) {
					// Invalid entry.
					logger.warn("Failed to create table column");
					return;
				}
			}
			
			if(col.getListElementType() != null) {
				if(e.isSingleValue()) {
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

	private void processNetworkAttributes(
			final Collection<NetworkAttributesElement> network_attributes,
			final Map<Long, CyNetwork> cx_network_map,
			final boolean isCollection) {

		Map<Long, Collection<NetworkAttributesElement>> network_attributes_map = new HashMap<>();
		
		if (network_attributes == null) {
			return;
		}
		
		for (final NetworkAttributesElement nae : network_attributes) {
			Long subnet = nae.getSubnetwork() ;
			
			if (subnet == null)
				subnet = DEFAULT_SUBNET;

			if (!network_attributes_map.containsKey(subnet)) {
				network_attributes_map.put(subnet, new ArrayList<NetworkAttributesElement>());
			}
			network_attributes_map.get(subnet).add(nae);
		}
		
		for (Entry<Long, Collection<NetworkAttributesElement>> entry : network_attributes_map.entrySet()) {
			CyNetwork network = cx_network_map.get(entry.getKey());
			CyTable table = network.getDefaultNetworkTable();
			addNetworkAttributeData(entry.getValue(), network, table);
		}
	}

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
			return e.getValues().stream()
				.map(value -> parseValue(value, column.getListElementType()))
				.collect(Collectors.toList());
		}
	}

    private final static Object parseValue(final String value,
                                           final Class<?> type) {
        if (type != String.class
                && (value == null || value.equals("") || value.equals("NaN") || value.equals("nan") || value
                        .toLowerCase().equals("null"))) {
            return null;
        }
        if (type == String.class || type == null) {
            return value;
        }
        else if (type == Long.class) {
            try {
                return Long.valueOf(value);
            }
            catch (final NumberFormatException e) {
                throw new IllegalArgumentException("could not convert '" + value + "' to long");
            }
        }
        else if (type == Integer.class) {
            try {
                return Integer.valueOf(value);
            }
            catch (final NumberFormatException e) {
                throw new IllegalArgumentException("could not convert '" + value + "' to integer");
            }
        }
        else if (type == Double.class) {
            try {
                return Double.valueOf(value);
            }
            catch (final NumberFormatException e) {
                throw new IllegalArgumentException("could not convert '" + value + "' to double");
            }
        }
        else if (type == Boolean.class) {
            try {
                return Boolean.valueOf(value);
            }
            catch (final NumberFormatException e) {
                throw new IllegalArgumentException("could not convert '" + value + "' to boolean");
            }
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "' for value '" + value
                    + "'");
        }
    }

    
    public final static String getCollectionNameFromNetworkAttributes(final Collection<NetworkAttributesElement> network_attributes) {
        String collection_name_from_network_attributes = null;
        if (network_attributes != null) {
            for (final AspectElement e : network_attributes) {
                final NetworkAttributesElement nae = (NetworkAttributesElement) e;
                if (nae.getSubnetwork() == null && nae.getName() != null
                        && nae.getDataType() == ATTRIBUTE_DATA_TYPE.STRING && nae.getName().equals(CxUtil.NAME_COL)
                        && nae.isSingleValue() && nae.getValue() != null && nae.getValue().length() > 0) {
                    if (collection_name_from_network_attributes == null) {
                        collection_name_from_network_attributes = nae.getValue();
                    }
                    else {
                        return null;
                    }
                }
            }
        }
        return collection_name_from_network_attributes;
    }

}
