package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.AspectSet;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.nicecy.NiceCyNetwork;
import org.cytoscape.io.internal.nicecy.NiceCyRootNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.ndexbio.cx2.aspect.element.core.CxAspectElement;
import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxEdge;
import org.ndexbio.cx2.aspect.element.core.CxEdgeBypass;
import org.ndexbio.cx2.aspect.element.core.CxNetworkAttribute;
import org.ndexbio.cx2.aspect.element.core.CxNode;
import org.ndexbio.cx2.aspect.element.core.CxNodeBypass;
import org.ndexbio.cx2.aspect.element.core.CxOpaqueAspectElement;
import org.ndexbio.cx2.aspect.element.core.CxVisualProperty;
import org.ndexbio.cx2.aspect.element.core.DeclarationEntry;
import org.ndexbio.cx2.io.CXReader;
import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.ndexbio.cxio.core.CxElementReader2;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.metadata.MetaDataElement;
import org.ndexbio.cxio.misc.OpaqueElement;
import org.ndexbio.model.cx.NdexNetworkStatus;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.exceptions.NdexException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is for de-serializing CX2 formatted network. 
 *
 *
 *
 */
public final class Cx2Importer {

    private InputStream input;
	
    private CyNetwork base;
    
    private CyRootNetwork root;
    
    private CxAttributeDeclaration attrDecls;
    
    private boolean createView;
    
    private CyNetworkView currentView;
    
    private CyTable nodeTable;
    private CyTable edgeTable;
    private CyTable networkTable;
    
    //CX ID to suid mapping table
    private Map<Long,Long> nodeIdMap;
    
    //CX ID to suid mapping table
    private Map<Long,Long> edgeIdMap;
    
    // CX ID to CxNodes mapping table. 
    private Map<Long, CxNode> cxNodes;
    
    private CxVisualProperty visualProperties;
    
    private List<CxNodeBypass> nodeBypasses;
    private List<CxEdgeBypass> edgeBypasses;
    
	private Map<String, Collection<CxOpaqueAspectElement>> opaqueAspects;
	
	private String name;

    public Cx2Importer(InputStream in, boolean createView) {

    	this.input = in;
    	this.createView = createView;
    	base = null;
    	currentView = null;
    	visualProperties = null;
    	nodeBypasses = new LinkedList<>();
    	edgeBypasses = new LinkedList<>();
    	opaqueAspects = new HashMap<>();
    	name = null;
    }


  
    public /*NiceCyRootNetwork*/ void importNetwork() throws IOException, NdexException {
        long t0 = System.currentTimeMillis();
		
        long nodeIdCounter = 0;
        long edgeIdCounter = 0;
        
		CXReader cxreader = new CXReader(input);
		
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		
		base = network_factory.createNetwork();
		root = ((CySubNetwork)base).getRootNetwork();
		
		if ( createView) {
			CyNetworkViewFactory view_factory = CyServiceModule.getService(CyNetworkViewFactory.class);
			CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);

			currentView = view_factory.createNetworkView(root);		
			view_manager.addNetworkView(currentView);
		}
			
		  
		for ( CxAspectElement elmt : cxreader ) {
			switch ( elmt.getAspectName() ) {
				case CxAttributeDeclaration.ASPECT_NAME:
					attrDecls = (CxAttributeDeclaration)elmt;
					if ( !attrDecls.getDeclarations().isEmpty())
						initializeTables();
					break;
				case CxNode.ASPECT_NAME :       //Node
					createNode((CxNode) elmt);
					break;
				case CxEdge.ASPECT_NAME:       // Edge
					CxEdge ee = (CxEdge) elmt;
					createEdge(ee);
					break;
				case CxNetworkAttribute.ASPECT_NAME: //network attributes
					createNetworkAttribute(( CxNetworkAttribute) elmt);
					break;
				case CxVisualProperty.ASPECT_NAME: 
					visualProperties = (CxVisualProperty) elmt;
					break;
				case CxNodeBypass.ASPECT_NAME: 
					nodeBypasses.add((CxNodeBypass) elmt );
					break;
				case CxEdgeBypass.ASPECT_NAME:
					edgeBypasses.add((CxEdgeBypass) elmt);
					break;
				default:    // opaque aspect
					addOpaqueAspectElement((CxOpaqueAspectElement)elmt);
			}

		} 
   
		serializeOpaqueAspects();
		
     	// create the view
		CyEventHelper cyEventHelper = CyServiceModule.getService(CyEventHelper.class);
		cyEventHelper.flushPayloadEvents();
		 
    }
    
    private void initializeTables() {
        	
    	if (attrDecls.getDeclarations().isEmpty())
    		return;
    	
    	networkTable = root.getTable(CyNetwork.class, CyRootNetwork.DEFAULT_ATTRS); 
		createTableAttrs(attrDecls.getDeclarations().get(CxNetworkAttribute.ASPECT_NAME),networkTable);
		
		nodeTable = root.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
		createTableAttrs(attrDecls.getDeclarations().get(CxNode.ASPECT_NAME),nodeTable);

		edgeTable = root.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
		createTableAttrs(attrDecls.getDeclarations().get(CxNetworkAttribute.ASPECT_NAME),edgeTable);
		
    }

    private static void createTableAttrs(Map<String, DeclarationEntry> attrsDecls, CyTable table) {
    	if ( attrsDecls!=null && !attrsDecls.isEmpty()) {
    		for ( Map.Entry<String, DeclarationEntry> e: attrsDecls.entrySet()) {
    			ATTRIBUTE_DATA_TYPE dtype= e.getValue().getDataType();
    			if (dtype == null)
    				dtype = ATTRIBUTE_DATA_TYPE.STRING;
    			CxUtil.createColumn(table, e.getKey(), CxUtil.getDataType( dtype), dtype.isSingleValueType());
    		}
    	}	
    }
    
    
    private void createNode(CxNode node) throws NdexException {
    	node.extendToFullNode(attrDecls.getAttributesInAspect(CxNode.ASPECT_NAME));
		
    	// add node to cy data model.
    	Long nodesuid = this.nodeIdMap.get(node.getId());
    	if ( nodesuid == null) {
    		CyNode cyNode = createCyNodeByCXId(node.getId());
    		nodesuid = cyNode.getSUID();		
    	} 

    	// add attributes
		final CyRow localRow = nodeTable.getRow(nodesuid);

		for ( Map.Entry<String,Object> e: node.getAttributes().entrySet()) {
			if (nodeTable.getColumn(e.getKey()) != null) {
				localRow.set(e.getKey(), e.getValue());
			} else 
				throw new NdexException("Node attribute " + e.getKey() + " is not declared.");
		}
    	
		// add cxnode to table
		cxNodes.put(node.getId(), node);
		
		/*if ( currentView !=null) {
            final View<CyNode> node_view = currentView.getNodeView(cyNode);

		}*/
    }
    
    
    private CyNode createCyNodeByCXId(Long cxNodeId) { 
		CyNode cyNode = base.addNode();		
		CxUtil.saveCxId(cyNode, base, cxNodeId);
		nodeIdMap.put(cxNodeId, cyNode.getSUID());
		return cyNode;
    }
    
    private void createEdge(CxEdge edge) throws NdexException {
    	edge.extendToFullNode(attrDecls.getAttributesInAspect(CxEdge.ASPECT_NAME));
    	
    	// add edge 
    	CyNode src,tgt;
    	
    	Long srcsuid = this.nodeIdMap.get(edge.getSource());
    	if ( srcsuid == null) {
    		src = createCyNodeByCXId(edge.getSource());
    	} else 
    		src = root.getNode(srcsuid);
    	
    	Long tgtsuid = this.nodeIdMap.get(edge.getTarget());
    	if ( tgtsuid == null) {
    		tgt = createCyNodeByCXId ( edge.getTarget());
    	} else
    		tgt = root.getNode(tgtsuid);
    	
		CyEdge cyEdge = base.addEdge(src, tgt, true);
		CxUtil.saveCxId(cyEdge, base, edge.getId() );
    	this.edgeIdMap.put(edge.getId(), cyEdge.getSUID());
    	
    	// edge edge attributes
		final CyRow localRow = edgeTable.getRow(cyEdge.getSUID());

		for ( Map.Entry<String,Object> e: edge.getAttributes().entrySet()) {
			if (edgeTable.getColumn(e.getKey()) != null) {
				localRow.set(e.getKey(), e.getValue());
			} else 
				throw new NdexException("Edge attribute " + e.getKey() + " is not declared.");
		}
		
    }
    
    private void createNetworkAttribute(CxNetworkAttribute netAttrs) throws NdexException {
		final CyRow sharedRow = networkTable.getRow(root.getSUID());
		
		netAttrs.extendToFullNode(this.attrDecls.getAttributesInAspect(CxNetworkAttribute.ASPECT_NAME));

		for ( Map.Entry<String,Object> e: netAttrs.getAttributes().entrySet()) {
			if (edgeTable.getColumn(e.getKey()) != null) {
				sharedRow.set(e.getKey(), e.getValue());
				if ( e.getKey().equals(CxNetworkAttribute.nameAttribute))
					this.name = (String)e.getValue();
			} else 
				throw new NdexException("Network attribute " + e.getKey() + " is not declared.");
		}
    }
    
    private void addOpaqueAspectElement(CxOpaqueAspectElement e) {
    	Collection<CxOpaqueAspectElement> aspect = this.opaqueAspects.get(e.getAspectName());
    	if ( aspect == null)
    		aspect = new ArrayList<>();
    	aspect.add(e);
    }
    
	private void serializeOpaqueAspects() {
		ObjectMapper mapper = new ObjectMapper();

		opaqueAspects.forEach((name, opaque) -> {
			if (ArrayUtils.contains(NiceCyRootNetwork.UNSERIALIZED_OPAQUE_ASPECTS, name)) {
				// Do not serialize some opaque aspects
				return;
			}
				String column = CxUtil.OPAQUE_ASPECT_PREFIX + name;
				CyTable table = base.getTable(CyNetwork.class, CyRootNetwork.SHARED_ATTRS);

				String aspectStr;
				try {
					aspectStr = mapper.writeValueAsString(opaque);
					CxUtil.createColumn(table, name, String.class, true);
					table.getRow(base.getSUID()).set(column, aspectStr);
				} catch (JsonProcessingException e) {	
					//TODO: log warning messages.
					e.printStackTrace();
				}

			
		});
	}

	public String getNetworkName() {
		return name;
	}
    
}