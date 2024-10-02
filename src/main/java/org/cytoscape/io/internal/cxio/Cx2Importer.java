package org.cytoscape.io.internal.cxio;

import java.awt.Paint;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cx_reader.ViewMaker;
import org.cytoscape.io.internal.nicecy.NiceCyNetwork;
import org.cytoscape.io.internal.nicecy.NiceCyRootNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
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
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.BooleanVisualProperty;
import org.cytoscape.view.presentation.property.DoubleVisualProperty;
import org.cytoscape.view.presentation.property.IntegerVisualProperty;
import org.cytoscape.view.presentation.property.ObjectPositionVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.ndexbio.cx2.aspect.element.core.AttributeDeclaredAspect;
import org.ndexbio.cx2.aspect.element.core.ComplexVPValue;
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
import org.ndexbio.cx2.aspect.element.core.MappingDefinition;
import org.ndexbio.cx2.aspect.element.core.TableColumnVisualStyle;
import org.ndexbio.cx2.aspect.element.core.VisualPropertyMapping;
import org.ndexbio.cx2.aspect.element.core.VisualPropertyTable;
import org.ndexbio.cx2.aspect.element.cytoscape.AbstractTableVisualProperty;
import org.ndexbio.cx2.aspect.element.cytoscape.DefaultTableType;
import org.ndexbio.cx2.aspect.element.cytoscape.VisualEditorProperties;
import org.ndexbio.cx2.converter.CX2ToCXVisualPropertyConverter;
import org.ndexbio.cx2.io.CXReader;
import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.model.exceptions.NdexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is for de-serializing CX2 formatted network. 
 *
 *
 *
 */
public final class Cx2Importer {
	
	private static final Logger logger = LoggerFactory.getLogger("CX2 Importer");

    private InputStream input;
	
    private CyNetwork base;
    
 //   private CyRootNetwork root;
    
    private CxAttributeDeclaration attrDecls;
    
    private boolean createView;
    
    private CyNetworkView currentView;
    
    private CyTable baseNodeTable;
    private CyTable baseEdgeTable;
    private CyTable baseNetworkTable;
    
    //CX ID to suid mapping table
    private Map<Long,Long> nodeIdMap;
    
    //CX ID to suid mapping table
    private Map<Long,Long> edgeIdMap;
    
    // node suid to CxNodes mapping table
    private Map<Long, CxNode> cxNodes;
    
    // edge suid to CxEdges mapping table
    private Map<Long, CxEdge> cxEdges;
    
    private CxVisualProperty visualProperties;
    
    private List<CxNodeBypass> nodeBypasses;
    private List<CxEdgeBypass> edgeBypasses;
    
	private Map<String, Collection<CxOpaqueAspectElement>> opaqueAspects;
	
	private String name;
	
	private VisualEditorProperties editorProperties;
	
	private boolean hasLayout;
	
	private boolean nodeSizeLocked;
	private boolean arrowColorMatchesEdges;
	
	private String collectionName;

    private AbstractTableVisualProperty tableStyle;

	public Cx2Importer(InputStream in, boolean createView) {

    	this.input = in;
    	this.createView = createView;
    	nodeIdMap = new TreeMap<>();
    	edgeIdMap = new TreeMap<>();
    	cxNodes = new TreeMap<>();
    	cxEdges = new TreeMap<>();
    	hasLayout = false;
    	
    	base = null;
    	currentView = null;
    	visualProperties = null;
    	nodeBypasses = new LinkedList<>();
    	edgeBypasses = new LinkedList<>();
    	opaqueAspects = new HashMap<>();
    	name = null;
    	editorProperties = null;
    	nodeSizeLocked=false;
    	arrowColorMatchesEdges = false;
    	
    	//tables
    	baseNodeTable = null;
    	baseEdgeTable = null;
    	baseNetworkTable = null;
    	
    }


    public String getCollectionName() {
		return collectionName;
	}



	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}


  
    public CyNetwork importNetwork() throws IOException, NdexException {
        long t0 = System.currentTimeMillis();
		
        long nodeIdCounter = 0;
        long edgeIdCounter = 0;
        
		CXReader cxreader = new CXReader(input);
		
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		
		base = network_factory.createNetwork();
	//	root = ((CySubNetwork)base).getRootNetwork();
		
		  
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
					visualProperties.evaluate();
					break;
				case CxNodeBypass.ASPECT_NAME: 
					nodeBypasses.add((CxNodeBypass) elmt );
					break;
				case CxEdgeBypass.ASPECT_NAME:
					edgeBypasses.add((CxEdgeBypass) elmt);
					break;
				case VisualEditorProperties.ASPECT_NAME: 
					if ( this.editorProperties == null) 
						this.editorProperties = (VisualEditorProperties) elmt;
					else 
						throw new NdexException("Only one " + VisualEditorProperties.ASPECT_NAME + " element is allowed in a CX2 network.");
					break;
				case AbstractTableVisualProperty.ASPECT_NAME:
					if ( this.tableStyle == null)
						this.tableStyle = (AbstractTableVisualProperty)elmt;
					else
						throw new NdexException ("Only one " + AbstractTableVisualProperty.ASPECT_NAME + " element is allowed in a CX2 network." );
					break;
				default:    // opaque aspect
					addOpaqueAspectElement((CxOpaqueAspectElement)elmt);
			}

		} 
		postProcessTables(); 
		serializeOpaqueAspects();
		
     	// create the view
	/*	CyEventHelper cyEventHelper = CyServiceModule.getService(CyEventHelper.class);
		cyEventHelper.flushPayloadEvents();
	*/
		return base;
    }
    
    private void initializeTables() {
        	
    	if (attrDecls.getDeclarations().isEmpty())
    		return;
    	
    	baseNetworkTable = base.getDefaultNetworkTable();
    	createTableAttrs(attrDecls.getDeclarations().get(CxNetworkAttribute.ASPECT_NAME),baseNetworkTable, Settings.cytoscapeBuiltinTableAttributes);
		
		baseNodeTable = base.getDefaultNodeTable();
		createTableAttrs(attrDecls.getDeclarations().get(CxNode.ASPECT_NAME), baseNodeTable, Settings.cytoscapeBuiltinTableAttributes);

		baseEdgeTable = base.getDefaultEdgeTable();
		createTableAttrs(attrDecls.getDeclarations().get(CxEdge.ASPECT_NAME), baseEdgeTable, Settings.cytoscapeBuiltinEdgeTableAttributes);
		
    }


    private static void createTableAttrs(Map<String, DeclarationEntry> attrsDecls, CyTable sharedTable, Set<String> cytoscapeBuiltInAttrs) {
    	if ( attrsDecls!=null && !attrsDecls.isEmpty()) {
    		for ( Map.Entry<String, DeclarationEntry> e: attrsDecls.entrySet()) {
    			String attrName = e.getKey();
    			if ( cytoscapeBuiltInAttrs.contains(attrName))
    				continue;
    			ATTRIBUTE_DATA_TYPE dtype= e.getValue().getDataType();
    			if (dtype == null)
    				dtype = ATTRIBUTE_DATA_TYPE.STRING;
    			CxUtil.createColumn(sharedTable, attrName, CxUtil.getDataType( dtype), dtype.isSingleValueType());
    		}
    	}	
    }
    
    
    private void createNode(CxNode node) throws NdexException {
    	
    	if (!hasLayout) {
    		if ( node.getX()!=null)
    			hasLayout = true;
    	}
    	
    	Map<String,DeclarationEntry> attributeDeclarations = attrDecls.getAttributesInAspect(CxNode.ASPECT_NAME);
    	node.extendToFullNode(attributeDeclarations);
		node.validateAttribute(attributeDeclarations, true);
    	// add node to cy data model.
    	Long nodesuid = this.nodeIdMap.get(node.getId());
    	if ( nodesuid == null) {
    		CyNode cyNode = createCyNodeByCXId(node.getId());
    		nodesuid = cyNode.getSUID();		
    	} 

    	// add attributes
		final CyRow localRow = baseNodeTable.getRow(nodesuid);

		for ( Map.Entry<String,Object> e: node.getAttributes().entrySet()) {
			String attrName = e.getKey();
			if(attrName.equals(CyNetwork.SUID))
				continue;
			if (baseNodeTable.getColumn(attrName) != null) {
				localRow.set(attrName, e.getValue());
			} else 
				throw new NdexException("Node attribute " + e.getKey() + " is not declared.");
		}
    	
		// add cxnode to table
		cxNodes.put(nodesuid, node);
		
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

    	Map<String,DeclarationEntry> attributeDeclarations = attrDecls.getAttributesInAspect(CxEdge.ASPECT_NAME);
    	edge.extendToFullNode(attributeDeclarations);
		edge.validateAttribute(attributeDeclarations, true);
 	    	
    	// add edge 
    	CyNode src,tgt;
    	
    	Long srcsuid = this.nodeIdMap.get(edge.getSource());
    	if ( srcsuid == null) {
    		src = createCyNodeByCXId(edge.getSource());
    	} else 
    		src = base.getNode(srcsuid);
    	
    	Long tgtsuid = this.nodeIdMap.get(edge.getTarget());
    	if ( tgtsuid == null) {
    		tgt = createCyNodeByCXId ( edge.getTarget());
    	} else
    		tgt = base.getNode(tgtsuid);
    	
		CyEdge cyEdge = base.addEdge(src, tgt, true);
		CxUtil.saveCxId(cyEdge, base, edge.getId() );
    	this.edgeIdMap.put(edge.getId(), cyEdge.getSUID());
    	
    	// edge edge attributes
		CyRow localRow = baseEdgeTable.getRow(cyEdge.getSUID());

		for ( Map.Entry<String,Object> e: edge.getAttributes().entrySet()) {
			if (e.getKey().equals(CyNetwork.SUID))
				continue;
			if (baseEdgeTable.getColumn(e.getKey()) != null) {
				localRow.set(e.getKey(), e.getValue());
			} else 
				throw new NdexException("Edge attribute " + e.getKey() + " is not declared.");
		}
		
		cxEdges.put(cyEdge.getSUID(),edge);
    }
    
    private void createNetworkAttribute(CxNetworkAttribute netAttrs) throws NdexException {
		CyRow localRow = baseNetworkTable.getRow(base.getSUID());
		
		netAttrs.extendToFullNode(this.attrDecls.getAttributesInAspect(CxNetworkAttribute.ASPECT_NAME));
		netAttrs.validateAttribute(this.attrDecls.getAttributesInAspect(CxNetworkAttribute.ASPECT_NAME), true);
		
		for ( Map.Entry<String,Object> e: netAttrs.getAttributes().entrySet()) {
			String attrName = e.getKey();
			if(attrName.equals(CyNetwork.SUID)|| attrName.equals(CyNetwork.SELECTED)) //igonore
				continue;
			else if (attrName.equals(CyNetwork.NAME) || attrName.equals(CxUtil.ANNOTATIONS)) {
			   localRow.set(attrName, e.getValue());
				if ( attrName.equals(CyNetwork.NAME))
						this.name = (String)e.getValue();
			} else {
				if( (baseNetworkTable.getColumn(e.getKey()) != null) ){
					localRow.set(e.getKey(), e.getValue());					
				} else 
					throw new NdexException("Network attribute " + e.getKey() + " is not declared.");
			}
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
	
	// helper function to get the node name
	private String getNodeName(CxNode cxNode) {
	    if (cxNode == null) {
	        return "";
	    }

	    Map<String, Object> attributes = cxNode.getAttributes();
	    Object name = attributes.getOrDefault(CyRootNetwork.SHARED_NAME, attributes.get(CyNetwork.NAME));

	    return name != null ? name.toString() : "";
	}

    // post-process after iteration through the Cx2 file
	private void postProcessTables() {
		// auto-fill 'shared name' column in node table if it is empty
		for(final CyNode cyNode : base.getNodeList()) {
			CyRow row = baseNodeTable.getRow(cyNode.getSUID());
			Set<String> allAttrNames = row.getAllValues().keySet();
			Object nodeName = row.get(CyNetwork.NAME,String.class);
			Object sharedName = row.get(CyRootNetwork.SHARED_NAME,String.class);
			if(nodeName!= null && (!allAttrNames.contains(CyRootNetwork.SHARED_NAME) || sharedName == null)) {
				row.set(CyRootNetwork.SHARED_NAME,nodeName);
			}
		}
		// auto-fill specific columns("name", "shared name" and "shared interaction") in edge table if they are empty
		for (final CyEdge cyEdge : base.getEdgeList()) {
			CxEdge cxEdge = cxEdges.get(cyEdge.getSUID());
		    CyRow row = baseEdgeTable.getRow(cyEdge.getSUID());
			Set<String> allAttrNames = row.getAllValues().keySet();
			Object v = row.get(CxUtil.INTERACTION,String.class);
			if ( cxEdge != null && v != null && allAttrNames.contains(CxUtil.INTERACTION)) {
				
				if((!allAttrNames.contains(CxUtil.SHARED_INTERACTION)) || 
						row.get(CxUtil.SHARED_INTERACTION,String.class) == null) {
					row.set(CxUtil.SHARED_INTERACTION,v);			
				}
				
				CxNode srcNode = cxNodes.get(nodeIdMap.get(cxEdge.getSource()));
				CxNode tgtNode = cxNodes.get(nodeIdMap.get(cxEdge.getTarget()));
				String defaultFormattedName = getNodeName(srcNode) + " (" + v.toString() + ") " + getNodeName(tgtNode);
				
				if((!allAttrNames.contains(CyNetwork.NAME)) || 
						row.get(CyNetwork.NAME,String.class) == null) {
					row.set(CyNetwork.NAME,defaultFormattedName);
				}
				if((!allAttrNames.contains(CyRootNetwork.SHARED_NAME)) || 
						row.get(CyRootNetwork.SHARED_NAME,String.class) == null) {
					row.set(CyRootNetwork.SHARED_NAME,defaultFormattedName);
				}
			}	
		}
	}
	
	public String getNetworkName() {
		return name;
	}
	
    
	public CyNetworkView createView() throws Exception {
		if ( createView) {
			CyNetworkViewFactory view_factory = CyServiceModule.getService(CyNetworkViewFactory.class);
			CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);

			currentView = view_factory.createNetworkView(base);		
			
			currentView.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, name);
			makeView();

			view_manager.addNetworkView(currentView);

		}
			
		// add table styles
		if ( tableStyle != null)  {
			Map<String, Map<String,TableColumnVisualStyle>> tableStyles = tableStyle.getStylesInTable(DefaultTableType.Network);
			if ( tableStyles != null) {
				NiceCyRootNetwork.addStyleToTable(base.getDefaultNetworkTable(), tableStyles);
			}
			
			tableStyles = tableStyle.getStylesInTable(DefaultTableType.Node);
			if ( tableStyles != null) {
				NiceCyRootNetwork.addStyleToTable(base.getDefaultNodeTable(), tableStyles);
			}
			
			tableStyles = tableStyle.getStylesInTable(DefaultTableType.Edge);
			if ( tableStyles != null) {
				NiceCyRootNetwork.addStyleToTable(base.getDefaultEdgeTable(), tableStyles);
			}
			
		}
		
		return currentView;
	}
	
	private void makeView() throws NdexException {
		final VisualMappingManager visual_mapping_manager = CyServiceModule.getService(VisualMappingManager.class);
    	final VisualStyleFactory visual_style_factory = CyServiceModule.getService(VisualStyleFactory.class);
    	final RenderingEngineManager rendering_engine_manager = CyServiceModule.getService(RenderingEngineManager.class);
	
    	String doLayout = currentView.getEdgeViews().size() < CxPreferences.getLargeLayoutThreshold() ? "force-directed" : "grid";

        final boolean have_default_visual_properties = 
        		(visualProperties != null) ||
                (!nodeBypasses.isEmpty()) || 
                (!edgeBypasses.isEmpty());
        
        VisualStyle new_visual_style = visual_mapping_manager.getDefaultVisualStyle();
        if (have_default_visual_properties) {
            int counter = 1;
            final VisualStyle default_visual_style = visual_mapping_manager.getDefaultVisualStyle();
            new_visual_style = visual_style_factory.createVisualStyle(default_visual_style);
            
            final String viz_style_title_base = ViewMaker.createTitleForNewVisualStyle(name);
            
            String viz_style_title = viz_style_title_base;
            while (counter < 101) {
                if (ViewMaker.containsVisualStyle(viz_style_title, visual_mapping_manager)) {
                    viz_style_title = viz_style_title_base + "-" + counter;
                }
                counter++;
            }
            //ViewMaker.removeVisualStyle(viz_style_title, visual_mapping_manager);
            new_visual_style.setTitle(viz_style_title);
        }

        
        if(hasLayout) {
        	for ( Map.Entry<Long, CxNode> e: cxNodes.entrySet()) {
        		CyNode node = base.getNode(e.getKey());
                final View<CyNode> nodeView = currentView.getNodeView(node);
                if (nodeView != null) {
                	CxNode n = e.getValue();
                    nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, n.getX());
                    nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, n.getY());
                    if (n.getZ() !=null) {
                        nodeView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION,
                                                    n.getZ());
                    }
                }
        	}
        	doLayout = null;
        }
        
        this.nodeSizeLocked = editorProperties !=null && 
				editorProperties.getProperties().get(VisualEditorProperties.NODE_SIZE_LOCKED)!=null &&
				editorProperties.getProperties().get(VisualEditorProperties.NODE_SIZE_LOCKED).equals(Boolean.TRUE);
        
        this.arrowColorMatchesEdges = editorProperties !=null && 
				editorProperties.getProperties().get(VisualEditorProperties.ARROW_COLOR_MATCHES_EDGES)!=null &&
				editorProperties.getProperties().get(VisualEditorProperties.ARROW_COLOR_MATCHES_EDGES).equals(Boolean.TRUE);
        
    	VisualLexicon lexicon = rendering_engine_manager.getDefaultVisualLexicon();

    	boolean fitContent = setNetworkVPFromVisualEditorProps(lexicon,new_visual_style);
    	
    	if ( visualProperties != null) {

        	setNetworkVPs(lexicon,visualProperties.getDefaultProps().getNetworkProperties(),new_visual_style);
        
        	setNodeVPs (lexicon, visualProperties.getDefaultProps().getNodeProperties(), new_visual_style);
        	
        	setEdgeVPs(lexicon, visualProperties.getDefaultProps().getEdgeProperties(), new_visual_style);
        	
        	// process node mapping
        	Map<String,VisualPropertyMapping> nodeMapping = visualProperties.getNodeMappings();
        	if ( nodeSizeLocked) {
        		VisualPropertyMapping m = nodeMapping.remove("NODE_WIDTH");
        		if (m !=null) {
        			//nodeMapping.remove("NODE_HEIGHT");
        			nodeMapping.put("NODE_SIZE", m);
        		}
        	}
        	setMapping(CyNode.class, nodeMapping, lexicon, new_visual_style);
        	
        	// Process edge mapping
        	Map<String,VisualPropertyMapping> edgeMapping = visualProperties.getEdgeMappings();

        	if (this.arrowColorMatchesEdges) {
        		VisualPropertyMapping m = edgeMapping.remove("EDGE_LINE_COLOR");
        		if ( m != null)
        			edgeMapping.put(BasicVisualLexicon.EDGE_UNSELECTED_PAINT.getIdString(), m);
        	}
        	setMapping(CyEdge.class, edgeMapping,lexicon,new_visual_style);
        		
			if (editorProperties != null) {
				for (Map.Entry<String, Object> e : editorProperties.getProperties().entrySet()) {
					String vpName = e.getKey();
					if (vpName.startsWith("NETWORK_")) {
						final VisualProperty vp = lexicon.lookup(CyNetwork.class, vpName);
						if (vp != null) {
							Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, e.getValue());	
							if ( cyVPValue != null) {
								new_visual_style.setDefaultValue(vp, cyVPValue);
							}
						}
					} else {  //set the dependencies
				    	for ( VisualPropertyDependency<?> d : new_visual_style.getAllVisualPropertyDependencies()) {
				            if (d.getIdString().equals(vpName)) {
				                try {
				                    d.setDependency((Boolean)e.getValue());
				                    break;
				                }
				                catch (final Exception ex) {
				                    throw new NdexException("could not parse boolean from '" + vpName + "'");
				                }
				            }
				        }

					}
					
				}

			}

        }
        
    	//Node Bypasses
        for (CxNodeBypass bypass: nodeBypasses) {
        	Long suid = this.nodeIdMap.get(bypass.getId());
        	CyNode n = this.base.getNode(suid);
        	VisualPropertyTable vps = bypass.getVisualProperties();
            final View<CyNode> nv = currentView.getNodeView(n);
        	if ( nodeSizeLocked ) {
        		Object v = vps.get("NODE_WIDTH");
        		if (v!=null)
        			vps.getVisualProperties().put("NODE_SIZE", v);
        	}
        	SortedMap<String,String> cyVPs = CX2ToCXVisualPropertyConverter.getInstance().convertEdgeOrNodeVPs(vps);
        	ViewMaker.setVisualProperties(lexicon, cyVPs, nv, CyNode.class);
        	
        }
        
        //Edge bypasses
        for (CxEdgeBypass bypass: edgeBypasses) {
        	Long suid = this.edgeIdMap.get(bypass.getId());
        	CyEdge e = this.base.getEdge(suid);
            final View<CyEdge> ev = currentView.getEdgeView(e);

        	VisualPropertyTable vps = bypass.getVisualProperties();
        	if ( this.arrowColorMatchesEdges ) {
        		Object v = vps.get("EDGE_LINE_COLOR");
        		if (v!=null)
        			vps.getVisualProperties().put(BasicVisualLexicon.EDGE_PAINT.getIdString(), v);
        	}
        	SortedMap<String,String> cyVPs = CX2ToCXVisualPropertyConverter.getInstance().convertEdgeOrNodeVPs(vps);
        	ViewMaker.setVisualProperties(lexicon, cyVPs, ev, CyEdge.class);
        	
        }
        
        if (have_default_visual_properties) {
        	// Simply add & assign style.  VMM automatically apply this later.
            visual_mapping_manager.addVisualStyle(new_visual_style);
            visual_mapping_manager.setVisualStyle(new_visual_style, currentView);
        }

        ViewMaker.applyStyle(new_visual_style,currentView,doLayout, fitContent);
        
        
	}

	/**
	 * Return true if not all 3 VPs exists, which means fitcontent should be called.
	 * @param lexicon
	 * @param defaults
	 * @param style
	 * @return
	 * @throws NdexException 
	 */
	private boolean setNetworkVPFromVisualEditorProps(final VisualLexicon lexicon, VisualStyle style) throws NdexException {
		
		if (editorProperties == null)
			return false;
		
        int count = 0;
        Map<String,Object> defaults = editorProperties.getProperties();
        
		if (defaults != null) {
	        String[] desiredKeys = {"NETWORK_CENTER_X_LOCATION","NETWORK_CENTER_Y_LOCATION","NETWORK_SCALE_FACTOR"};

	        
	        for (String key : desiredKeys) {
	            if (defaults.containsKey(key)) {
					final VisualProperty vp = lexicon.lookup(CyNetwork.class, key);
					Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, defaults.get(key));	
					if ( cyVPValue != null) {
						style.setDefaultValue(vp, cyVPValue);
						count++;
					}
	            }
	        }    
			
		}
		return count != 3;
	}

	
	private void setNetworkVPs(final VisualLexicon lexicon,
			 Map<String,Object> defaults, VisualStyle style) throws NdexException {
		if (defaults != null) {
			for (final Map.Entry<String, Object> entry : defaults.entrySet()) {
				String cyVPName = CX2ToCXVisualPropertyConverter.getInstance().getCx1NetworkPropertyName(entry.getKey());
				if ( cyVPName != null) {
					final VisualProperty vp = lexicon.lookup(CyNetwork.class, cyVPName);
					if (vp != null) {
						Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, entry.getValue());	
						if ( cyVPValue != null) {
							style.setDefaultValue(vp, cyVPValue);
						}
					}
				}
			}
		}
		
	}

	private void setNodeVPs(final VisualLexicon lexicon,
			VisualPropertyTable defaults, VisualStyle style) throws NdexException {
		if (defaults != null) {
			Map<String,String> cyVPTable = CX2ToCXVisualPropertyConverter.getInstance().convertEdgeOrNodeVPs(defaults);
			for (final Map.Entry<String, String> entry : cyVPTable.entrySet()) {
					VisualProperty vp = lexicon.lookup(CyNode.class, entry.getKey());
					if (vp != null) {
						Object cyVPValue  = vp.parseSerializableString(entry.getValue());	
						if ( cyVPValue != null) {
							style.setDefaultValue(vp, cyVPValue);
						}
					}
			}
			
			//preprocess NODE_SIZE 
			if (nodeSizeLocked) {
				VisualProperty<Double> vp = BasicVisualLexicon.NODE_SIZE;
				Object v = defaults.get("NODE_WIDTH");
				if ( v!=null) {
					Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, v);
					style.setDefaultValue(vp, (Double)cyVPValue);
				}
			}
			
		/*	for (final Map.Entry<String, Object> entry : defaults.getVisualProperties().entrySet()) {
				String cyVPName = CX2ToCXVisualPropertyConverter.getInstance().getCx1EdgeOrNodeProperty(entry.getKey());
				if ( cyVPName != null) {
					VisualProperty vp = lexicon.lookup(CyNode.class, cyVPName);
					if (vp != null) {
						Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, entry.getValue());	
						if ( cyVPValue != null) {
							style.setDefaultValue(vp, cyVPValue);
						}
					}
				}
			} */
		}
		
	}

	private void setEdgeVPs(final VisualLexicon lexicon,
			VisualPropertyTable defaults, VisualStyle style) throws NdexException {
		if (defaults != null) {
			Map<String,String> cyVPTable = CX2ToCXVisualPropertyConverter.getInstance().convertEdgeOrNodeVPs(defaults);
			for (final Map.Entry<String, String> entry : cyVPTable.entrySet()) {
					VisualProperty vp = lexicon.lookup(CyEdge.class, entry.getKey());
					if (vp != null) {
						Object cyVPValue  = vp.parseSerializableString(entry.getValue());	
						if ( cyVPValue != null) {
							style.setDefaultValue(vp, cyVPValue);
						}
					}
			}
			
			
			//preprocess edge color
			if (this.arrowColorMatchesEdges) {
				VisualProperty<Paint> vp = BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
				Object v = defaults.get("EDGE_LINE_COLOR");
				if ( v!=null) {
					Paint cyVPValue  = getCyVPValueFromCX2VPValue(vp, v);
					style.setDefaultValue(vp, cyVPValue);
				}
			}
/*			
			for (final Map.Entry<String, Object> entry : defaults.getVisualProperties().entrySet()) {
				String cyVPName = CX2ToCXVisualPropertyConverter.getInstance().getCx1EdgeOrNodeProperty(entry.getKey());
				if ( cyVPName != null) {
					VisualProperty vp = lexicon.lookup(CyEdge.class, cyVPName);
					if (vp != null) {
						Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, entry.getValue());	
						if ( cyVPValue != null) {
							style.setDefaultValue(vp, cyVPValue);
						}
					}
				}
			} */
		}
		
	}
	
	
	private void setDefaultVisualPropertiesAndMappings(final VisualLexicon lexicon,
			 VisualPropertyTable defaults, Map<String, VisualPropertyMapping> mappings,
			 VisualStyle style, final Class my_class) throws NdexException {

		if (defaults != null) {
			for (final Map.Entry<String, Object> entry : defaults.getVisualProperties().entrySet()) {
				final VisualProperty vp = lexicon.lookup(my_class, entry.getKey());
				if (vp != null) {
					Object cyVPValue  = getCyVPValueFromCX2VPValue(vp, entry.getValue());	
				    if ( cyVPValue != null) {
				    	style.setDefaultValue(vp, cyVPValue);
				    }
				}
			}
		}


/*		if (maps != null) {
			for (final Entry<String, Mapping> entry : maps.entrySet()) {
				try {
					parseVisualMapping(entry.getKey(), entry.getValue(), lexicon, style, my_class);
				} catch (IOException e) {
					logger.warn("Failed to parse visual mapping: " + e);
				}

			}
		} */

	/*	if (dependencies != null) {
			for (final Entry<String, String> entry : dependencies.entrySet()) {
				try {
					parseVisualDependency(entry.getKey(), entry.getValue(), style);
				} catch (IOException e) {
					logger.warn("Failed to parse visual dependency: " + e);
				}
			}
		} */
	}

	
	public static <T> T getCyVPValueFromCX2VPValue(VisualProperty<T> vp, Object cx2Value) throws NdexException {
		if  (vp instanceof ObjectPositionVisualProperty ) {
		    String sv = ((ComplexVPValue)cx2Value).toCX1String();    
			return vp.parseSerializableString(sv);
		}	
		if ( vp.getIdString().startsWith("NODE_CUSTOMGRAPHICS_SIZE_", 0))
			return null;
	  	if ( cx2Value instanceof String)
	  		return vp.parseSerializableString((String)cx2Value);
		if(vp instanceof DoubleVisualProperty)
			return  (T)Double.valueOf(  ((Number)cx2Value).doubleValue()); 
		if ( vp instanceof BooleanVisualProperty)
	  		return (T)cx2Value;
	  	if (vp instanceof IntegerVisualProperty)
	  		return  (T)Integer.valueOf(  ((Number)cx2Value).intValue());
	  	
	  	return null;
	}
	
	private static VisualProperty getCyVPFromCX2VPName(Class myClass, VisualLexicon lexicon, String cx2Name) {
		String cx1Name = CX2ToCXVisualPropertyConverter.getInstance().getCx1EdgeOrNodeProperty(cx2Name);
		if ( cx1Name !=null) {
			return lexicon.lookup(myClass, cx1Name);
		}
		return lexicon.lookup(myClass,cx2Name);
	}
	
	private void setMapping(Class myClass, Map<String,VisualPropertyMapping> mappings, 
			VisualLexicon lexicon, VisualStyle style) throws NdexException {
		for (Map.Entry<String, VisualPropertyMapping> mapping : mappings.entrySet()) {
			String cx2VpName = mapping.getKey();
			VisualProperty vp = getCyVPFromCX2VPName(myClass, lexicon, cx2VpName);
			if (vp != null) {
				MappingDefinition defination = mapping.getValue().getMappingDef();
				String attrName = defination.getAttributeName();
				switch (mapping.getValue().getType()) {
				case PASSTHROUGH: {
			        ATTRIBUTE_DATA_TYPE dtype = getAttrDataType(myClass,attrName); 
			        if (dtype == null)
			        	dtype = defination.getAttributeType();
					ViewMaker.addPasstroughMapping(style, vp,attrName,CxUtil.getDataType(dtype));
					break;
				}
				case DISCRETE:
					addDiscreteMapping(myClass,lexicon, defination,style,vp,cx2VpName);			
					break;
				case CONTINUOUS:
					addContinuousMapping(myClass,lexicon, defination,style,vp,cx2VpName);			
					break;
				default:
					break;

				}
			}
		}
		
	}
	
	private void addDiscreteMapping(Class<?> typeClass, VisualLexicon lexicon, MappingDefinition def, VisualStyle style,
                                                VisualProperty vp,String cx2VpName ) throws NdexException {
        String colName = def.getAttributeName();
        ATTRIBUTE_DATA_TYPE dtype = getAttrDataType(typeClass,colName); 
        if ( dtype == null)
        	dtype = def.getAttributeType();
		DiscreteMapping dmf = (DiscreteMapping) ViewMaker.vmf_factory_d.createVisualMappingFunction(colName, CxUtil.getDataType(dtype), vp);
    
        for ( Map<String,Object> mappingEntry: def.getMapppingList()) {
			ATTRIBUTE_DATA_TYPE elmtDType = dtype.isSingleValueType()? dtype: dtype.elementType();
			Object v = AttributeDeclaredAspect.processAttributeValue (elmtDType, mappingEntry.get("v") );
			try {
				String cyValue = CX2ToCXVisualPropertyConverter.getInstance().
			
					getCx1EdgeOrNodePropertyValue(cx2VpName, mappingEntry.get("vp"));
				dmf.putMapValue(v, vp.parseSerializableString(cyValue));
			} catch (IllegalArgumentException e) {
				throw new NdexException("Failed to parse value for mapping " + colName + 
						" on " + cx2VpName + " : " + e.getMessage());
			}
        
		}
        style.addVisualMappingFunction(dmf);
	}
	
	private void addContinuousMapping(Class<?> typeClass, VisualLexicon lexicon, MappingDefinition def, VisualStyle style,
			VisualProperty vp, String cx2VpName) throws NdexException {
		String colName = def.getAttributeName();
		ATTRIBUTE_DATA_TYPE dtype = getAttrDataType(typeClass, colName);
		if ( dtype == null)
			dtype = def.getAttributeType();
		ContinuousMapping cmf = (ContinuousMapping) ViewMaker.vmf_factory_c.createVisualMappingFunction(colName,
				CxUtil.getDataType(dtype), vp);
		
		//int cyCounter = 0;
		int counter = 0;
		Object L = null;
		Object E = null;
		Object G = null;
		Object ov = null;
		CX2ToCXVisualPropertyConverter vpCvtr = CX2ToCXVisualPropertyConverter.getInstance();
		for (Map<String, Object> m : def.getMapppingList()) {
			Object minV = m.get("min");
			Object maxV = m.get("max");
			Boolean includeMin = (Boolean)m.get("includeMin");
			Boolean includeMax = (Boolean)m.get("includeMax");
			Object minVP = m.get("minVPValue");
			Object maxVP = m.get("maxVPValue");
			
			if ( minVP == null && maxVP == null)
				throw new NdexException ("minVPValue and maxVPValue are both missing in CONTINUOUS mapping of " + cx2VpName + " on column " + colName);
			
			if ( counter == 0) { // first range
				
				if ( minV != null) {  // no out of range definition
					ov = AttributeDeclaredAspect.processAttributeValue(dtype, minV);
					
					L = vp.parseSerializableString(vpCvtr.getCx1EdgeOrNodePropertyValue(cx2VpName,minVP));
					E = L;
					G = L;					
					BoundaryRangeValues point = new BoundaryRangeValues(L,E, G);
					
                    cmf.addPoint(cvtValueForContinuousMapping(ov), point);
	                //cyCounter++;
	                
	                ov =AttributeDeclaredAspect.processAttributeValue(dtype, maxV);
                	L = vp.parseSerializableString(vpCvtr.getCx1EdgeOrNodePropertyValue(cx2VpName,maxVP));
                	E=L;
                	G=L;
					
					point = new BoundaryRangeValues(L,E,G);
                    cmf.addPoint(cvtValueForContinuousMapping(ov), point);
                    
                	//cyCounter++;
				}
			    L = vp.parseSerializableString(vpCvtr.getCx1EdgeOrNodePropertyValue(cx2VpName,maxVP));
			    ov = AttributeDeclaredAspect.processAttributeValue(dtype, maxV);
			    if ( includeMax.booleanValue()) 
			    	E = L;
			} else {  // middle ranges and the last range
				G = vp.parseSerializableString(vpCvtr.getCx1EdgeOrNodePropertyValue(cx2VpName,minVP));
				if (includeMin.booleanValue())
					E=G;
				
				// create the mapping point
				
				BoundaryRangeValues point = new BoundaryRangeValues(L,E,G);
               
				cmf.addPoint(cvtValueForContinuousMapping(ov), point);
				
                //cyCounter++;
                
                // prepare for the next point
                if ( maxV != null) {
                	ov =AttributeDeclaredAspect.processAttributeValue(dtype, maxV);
                	L = vp.parseSerializableString(vpCvtr.getCx1EdgeOrNodePropertyValue(cx2VpName,maxVP));
                	if (includeMax.booleanValue())
                		E = L;
                	else 
                		E = null;
                }	
                
			}
			counter++;
		}
		style.addVisualMappingFunction(cmf);
	}
	
	private static Object cvtValueForContinuousMapping(Object v) {
		if (v instanceof Number) {
			
			return Double.valueOf(  ((Number)v).doubleValue());
		}
			
		return v;
	}
	
	private ATTRIBUTE_DATA_TYPE getAttrDataType(Class<?> typeClass, String attrName) throws NdexException {
		
		Map<String,DeclarationEntry> decls = null;
		if ( typeClass.equals(CyNode.class)) {
			decls = attrDecls.getAttributesInAspect(CxNode.ASPECT_NAME);
		} else if ( typeClass.equals(CyEdge.class)) {
			decls = attrDecls.getAttributesInAspect(CxEdge.ASPECT_NAME) ;
		} else 
			decls= attrDecls.getAttributesInAspect(CxNetworkAttribute.ASPECT_NAME);
		
		if ( decls !=null) {
			DeclarationEntry d = decls.get(attrName);
			if ( d != null)
			   return d.getDataType();    
		}
		return null;
	}
	

	
}
