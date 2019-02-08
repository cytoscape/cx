package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.AbstractAttributesAspectElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.metadata.MetaDataElement;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.apache.log4j.Logger;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CxUtil {
	private static Logger logger = Logger.getLogger("CxUtil");
	
    public final static String REPRESENTS                     = "represents";
    public final static String CONTINUOUS_MAPPING             = "CONTINUOUS_MAPPING_";
    public final static String DISCRETE_MAPPING               = "DISCRETE_MAPPING_";
    public final static String PASSTHROUGH_MAPPING            = "PASSTHROUGH_MAPPING_";
    public final static String VM_COL                         = "COL";
    public final static String VM_TYPE                        = "T";
    public final static String DISCRETE                       = "DISCRETE";
    public final static String CONTINUOUS                     = "CONTINUOUS";
    public final static String PASSTHROUGH                    = "PASSTHROUGH";
    public static final String NODE_SIZE_LOCKED               = "nodeSizeLocked";
    public static final String NODE_CUSTOM_GRAPHICS_SIZE_SYNC = "nodeCustomGraphicsSizeSync";
    public static final String ARROW_COLOR_MATCHES_EDGE       = "arrowColorMatchesEdge";
    
    public static final String CX_ID_MAPPING				  = "CX Element ID";
    public static final String CX_METADATA				  	  = "CX MetaData";
	public static final String OPAQUE_ASPECT_PREFIX 		  = "CX_OPAQUE::";
	
	private static String CXID_NAMESPACE = CyNetwork.HIDDEN_ATTRS;
    
    
    public static MetaDataCollection getMetaData(CyNetwork network) {
    	CyTable hidden_network_table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		CyRow row = hidden_network_table.getRow(network.getSUID());
		if (row != null) {
			String metaDataStr = row.get(CxUtil.CX_METADATA, String.class);
			if (metaDataStr != null) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					return mapper.readValue(metaDataStr, MetaDataCollection.class);
				}catch(IOException e) {
					Settings.INSTANCE.debug("Get Metadata threw an IOException: " + e);
				}
			}
		}
		return new MetaDataCollection();
    }
    
    public static void setMetaData(CyNetwork network, MetaDataCollection metaData) {
    	CyTable hidden_table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
    	if (hidden_table.getColumn(CxUtil.CX_METADATA) == null) {
    		hidden_table.createColumn(CxUtil.CX_METADATA, String.class, true);
    	}
    	CyRow row = hidden_table.getRow(network.getSUID());
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    	String metaDataStr = mapper.writeValueAsString(metaData);
		row.set(CxUtil.CX_METADATA, metaDataStr);
    	}catch (JsonProcessingException e) {
    		System.out.println("Failed to update metaData. This should not happen. " + e);
    	}
    }
    
	public static boolean isCollection(NiceCXNetwork niceCX) {
		return niceCX.getOpaqueAspectTable().get(NetworkRelationsElement.ASPECT_NAME) != null;
	}

    // CX ID mapping in CyRootNetwork hidden node/edge tables
	
	private static CyTable getCxTable(CyIdentifiable ele, CyRootNetwork root) {
		CyTable table;
		if (ele instanceof CyNode) {
			table = root.getTable(CyNode.class, CXID_NAMESPACE);
		}else {
			table = root.getTable(CyEdge.class, CXID_NAMESPACE);
		}
		
		if (table.getColumn(CX_ID_MAPPING) == null) {
			table.createColumn(CX_ID_MAPPING, Long.class, false);
		}
		return table;
	}

	private final static Long getMaxId(Class<? extends CyIdentifiable> type, CyRootNetwork network) {
		CyTable table = network.getTable(type, CXID_NAMESPACE);
		CyColumn column = table.getColumn(CX_ID_MAPPING);
		if (column == null) {
			return null;
		}
		List<Long> values = column.getValues(Long.class);
		if (values == null || values.isEmpty()) {
			return null;
		}
		return Collections.max(values, (a, b) -> { 
			if (a == null && b == null) {
				return 0;
			}
			else if (a == null) {
				return -1;
			}else if (b == null) {
				return 1;
			}
			return Long.compare(a, b);
		});
	}

	/**
	 * For safety, update the metadata id counter for all subnetworks in the collection
	 * @param type
	 * @param root
	 * @param counter
	 * @throws JsonProcessingException
	 */
	private static void updateCxIdCounter(Class<? extends CyIdentifiable> type, CyRootNetwork root, Long counter) {
		//TODO: Is this necessary?
		for (CySubNetwork network : root.getSubNetworkList()) {
			MetaDataCollection metadata = CxUtil.getMetaData(network);
			String aspect = type.equals(CyNode.class) ? NodesElement.ASPECT_NAME : EdgesElement.ASPECT_NAME;
			if (metadata == null) {
				metadata = new MetaDataCollection();
				MetaDataElement mde = new MetaDataElement(aspect, "1.0");
				metadata.add(mde);
			}
			metadata.setIdCounter(aspect, counter);
			CxUtil.setMetaData(network, metadata);
		}
	}
	
	private final static void populateCxIdColumn(Class<? extends CyIdentifiable> type, CyRootNetwork root) {
		Long counter = getMaxId(type, root);
		CyTable table = root.getTable(type, CXID_NAMESPACE);
		boolean had_ids = counter != null;
		List<? extends CyIdentifiable> eles = type.equals(CyNode.class) ? root.getNodeList() : root.getEdgeList();
		for (CyIdentifiable ele : eles) {
			CyRow row = table.getRow(ele.getSUID());
			if (row.get(CX_ID_MAPPING, Long.class) == null) {
				if (had_ids) {
					row.set(CX_ID_MAPPING, ++counter);
				}else {
					row.set(CX_ID_MAPPING, ele.getSUID());
					counter = counter == null ? ele.getSUID() : Math.max(counter, ele.getSUID());
				}
			}
		}
		updateCxIdCounter(type, root, counter);
	}
	
	private static CyRootNetwork getRoot(CyNetwork network) {
		if (network instanceof CySubNetwork) {
			return ((CySubNetwork) network).getRootNetwork();
		}else {
			return (CyRootNetwork) network;
		}
	}
	
	
	public static final void saveCxId(CyIdentifiable ele, CyNetwork network, Long id) {
		CyRootNetwork root = getRoot(network);
		CyTable table = getCxTable(ele, root);
		
		CyRow row = table.getRow(ele.getSUID());
		row.set(CX_ID_MAPPING, id);
	}
	
	/**
	 * Retrieve the CX ID of an element if there is one in the node/edge hidden table, otherwise null
	 * @param cyEle
	 * @param network
	 * @return
	 */
	public static Long getCxId(CyIdentifiable cyEle, CyRootNetwork root) {
		CyTable table = getCxTable(cyEle, root);
		CyRow row = table.getRow(cyEle.getSUID());
		return row.get(CX_ID_MAPPING, Long.class);
	}
	
	/**
	 * If there is a CX ID in the hidden node/edge column, return it. Otherwise, set it 
	 * @param cy_ele
	 * @param network
	 * @param useCxId
	 * @return
	 * @throws JsonProcessingException
	 */
	public static Long getElementId(CyIdentifiable cy_ele, CyNetwork network, boolean useCxId) {
		CyRootNetwork root = getRoot(network);
		
		if (!useCxId) {
			return cy_ele.getSUID();
		}
		Long cx_id = getCxId(cy_ele, root);

		if (cx_id != null) {
			return cx_id;
		}
		
		if (cy_ele instanceof CyNode) {
			populateCxIdColumn(CyNode.class, root);
		}else if (cy_ele instanceof CyEdge) {
			populateCxIdColumn(CyEdge.class, root);
		}
		
		return getCxId(cy_ele, root);
	}
	
	public static final ATTRIBUTE_DATA_TYPE toAttributeType(final Class<?> attr_class) {
		if (attr_class == String.class) {
			return ATTRIBUTE_DATA_TYPE.STRING;
		} else if (attr_class == Double.class) {
			return ATTRIBUTE_DATA_TYPE.DOUBLE;
		} else if ((attr_class == Integer.class)) {
			return ATTRIBUTE_DATA_TYPE.INTEGER;
		} else if (attr_class == Long.class) {
			return ATTRIBUTE_DATA_TYPE.LONG;
		} else if (attr_class == Boolean.class) {
			return ATTRIBUTE_DATA_TYPE.BOOLEAN;
		} else {
			throw new IllegalArgumentException("Don't know how to deal with type '" + attr_class + "'");
		}
	}

	public static final ATTRIBUTE_DATA_TYPE toListAttributeType(final Class<?> attr_class) {
		if (attr_class == String.class) {
			return ATTRIBUTE_DATA_TYPE.LIST_OF_STRING;
		} else if ((attr_class == Double.class)) {
			return ATTRIBUTE_DATA_TYPE.LIST_OF_DOUBLE;
		} else if ((attr_class == Integer.class)) {
			return ATTRIBUTE_DATA_TYPE.LIST_OF_INTEGER;
		} else if (attr_class == Long.class) {
			return ATTRIBUTE_DATA_TYPE.LIST_OF_LONG;
		} else if (attr_class == Boolean.class) {
			return ATTRIBUTE_DATA_TYPE.LIST_OF_BOOLEAN;
		} else {
			throw new IllegalArgumentException("Don't know how to deal with type '" + attr_class + "'");
		}
	}
	
	public static String getNetworkName(final CyNetwork net) {
		final CyRow row = net.getRow(net, CyNetwork.DEFAULT_ATTRS);
		if (row == null) {
			throw new NullPointerException("Unable to find network name in network table");
		}
		String name = null;
		final Map<String, Object> values = row.getAllValues();
		if ((values != null) && !values.isEmpty()) {
			if (values.get(CyNetwork.NAME) != null) {
				final String str = String.valueOf(values.get(CyNetwork.NAME));
				if ((str != null) && (str.trim().length() > 0)) {
					name = str;
				}
			}
		}
		return name;
	}
	
	public static VisualLexicon getLexicon(CyNetworkView view) {
		CyApplicationManager _application_manager = CyServiceModule.getService(CyApplicationManager.class);
		NetworkViewRenderer renderer = _application_manager.getNetworkViewRenderer(view.getRendererId());

		RenderingEngineFactory<CyNetwork> factory = renderer == null ? null

				: renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);

		VisualLexicon lexicon = factory == null ? null : factory.getVisualLexicon();
		return lexicon;
	}

	public static boolean hasCxIds(CyNetwork network) {
		CyRootNetwork root = getRoot(network);
		CyTable table = root.getTable(CyNode.class, CXID_NAMESPACE);
		return table.getColumn(CX_ID_MAPPING) != null;
	}
	
	public static Class<?> getDataType(final ATTRIBUTE_DATA_TYPE type) {
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
	
	public static CyColumn createColumn(final CyTable table, 
    		final String name,
    		final Class<?> data_type,
    		final boolean is_single) {
		if (table == null) {
			return null;
		}
		if (table.getColumn(name) == null) {
			if (is_single) {
		       table.createColumn(name, data_type, false);
		   }else {
		       table.createListColumn(name, data_type, false);
		   }
	    }
		return table.getColumn(name);
	}
	
	public final static void addToColumn(final CyTable table, final CyRow row, final AbstractAttributesAspectElement e) {
		if (e == null) {
			return;
		}

		final String name = e.getName();
		if (name == null) {
			return;
		}
				
		if ((!Settings.INSTANCE.isIgnoreSuidColumn() || !name.equals(CyNetwork.SUID))
				&& (!Settings.INSTANCE.isIgnoreSelectedColumn() || !name.equals(CyNetwork.SELECTED))) {
			
			final Class<?> data_type = CxUtil.getDataType(e.getDataType());
			
			CyColumn col = CxUtil.createColumn(table, name, data_type, e.isSingleValue());

			if(col == null) {
				// Invalid entry.
				logger.warn("Failed to create column " + name + " in table " + table);
				return;
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
	
	public final static Object getValue(final AbstractAttributesAspectElement e, final CyColumn column) {
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
		} 
			
		return e.getValues().stream()
				.map(value -> parseValue(value, column.getListElementType()))
				.collect(Collectors.toList());
		
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
    
}
