package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.util.List;

import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.metadata.MetaDataElement;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CxUtil {

    public final static String SELECTED                       = CyNetwork.SELECTED;
    public final static String REPRESENTS                     = "represents";
    public final static String SHARED_NAME_COL                = CyRootNetwork.SHARED_NAME;
    public final static String NAME_COL                       = CyNetwork.NAME;
    public final static String SHARED_INTERACTION             = CyRootNetwork.SHARED_INTERACTION;
    public final static String CONTINUOUS_MAPPING             = "CONTINUOUS_MAPPING_";
    public final static String DISCRETE_MAPPING               = "DISCRETE_MAPPING_";
    public final static String PASSTHROUGH_MAPPING            = "PASSTHROUGH_MAPPING_";
    public final static String VM_COL                         = "COL";
    public final static String VM_TYPE                        = "T";
    public final static String SUID                           = CyIdentifiable.SUID;
    public final static String DISCRETE                       = "DISCRETE";
    public final static String CONTINUOUS                     = "CONTINUOUS";
    public final static String PASSTHROUGH                    = "PASSTHROUGH";
    public static final String NODE_SIZE_LOCKED               = "nodeSizeLocked";
    public static final String NODE_CUSTOM_GRAPHICS_SIZE_SYNC = "nodeCustomGraphicsSizeSync";
    public static final String ARROW_COLOR_MATCHES_EDGE       = "arrowColorMatchesEdge";
    
    public static final String CX_ID_MAPPING				  = "CX Element ID";
    public static final String CX_METADATA				  	  = "CX MetaData";
	public static final String OPAQUE_ASPECT_PREFIX 		  = "CX_OPAQUE::";
    
    
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
		return null;
    }
    
    public static void setMetaData(CyNetwork network, MetaDataCollection metaData) {
    	CyTable hidden_table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
    	if (hidden_table.getColumn(CxUtil.CX_METADATA) == null) {
    		hidden_table.createColumn(CxUtil.CX_METADATA, String.class, true);
    	}
    	CyRow row = hidden_table.getRow(network.getSUID());
    	ObjectMapper mapper = new ObjectMapper();
    	String metaDataStr;
		try {
			metaDataStr = mapper.writeValueAsString(metaData);
			row.set(CxUtil.CX_METADATA, metaDataStr);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	public static void saveCxId(CyNetwork network, CyIdentifiable cy_ele, Long cx_id) {
		CyTable table;
		if (cy_ele instanceof CyNode) {
			table = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		}else if (cy_ele instanceof CyEdge) {
			table = network.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		}else {
			throw new IllegalArgumentException("Can only save CX ID of nodes/edges. Not "  + cy_ele);
		}
        if (table.getColumn(CxUtil.CX_ID_MAPPING) == null) {
        	table.createColumn(CxUtil.CX_ID_MAPPING, Long.class, false);
        }
        
        table.getRow(cy_ele.getSUID()).set(CxUtil.CX_ID_MAPPING, cx_id);
		
	}
	
	private final static Long getIdCounter(String aspect, CyNetwork network) {
		// Try to get the idCounter from the metadata. Return null if no metadata or counter
		MetaDataCollection metadata = null;
		metadata = CxUtil.getMetaData(network);
		if (metadata == null) {
			return null;
		}
		Long counter = metadata.getIdCounter(aspect);
		return counter;	
	}
	
	private final static Long getIdCounterWrapper(Class<? extends CyIdentifiable> type, CyNetwork network) {
		// Return idCounter of aspect if it exists, otherwise gets maximum value in CX ID column if it exists
		// Otherwise returns null
		Long counter = null;
		if (type.equals(CyNode.class)) {
			counter = getIdCounter(NodesElement.ASPECT_NAME, network);
		}else if (type.equals(CyEdge.class)) {
			counter = getIdCounter(EdgesElement.ASPECT_NAME, network);
		}
		if (counter != null) {
			return counter;
		}
		
		final String col = CxUtil.CX_ID_MAPPING;
		final CyTable hidden_table = network.getTable(type, CyNetwork.HIDDEN_ATTRS);
		if (hidden_table.getColumn(col) == null) {
			hidden_table.createColumn(col, Long.class, true);
			return null;
		}
		CyColumn cx_ids = hidden_table.getColumn(col);
		for (Long cx_id : cx_ids.getValues(Long.class)) {
			if (cx_id != null)
				counter = counter == null ? cx_id : Math.max(counter, cx_id);
		}
		return counter;
	}
	
	private static void updateCxIdCounter(Class<? extends CyIdentifiable> type, CyNetwork network, Long counter) {
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
	
	final static void populateCxIdColumn(Class<? extends CyIdentifiable> type, CyNetwork network) {
		// Populate the CX ID column in a hidden node/edge table
		// Uses the metadata idCounter if it exists otherwise uses SUIDs
		
		Long counter = getIdCounterWrapper(type, network);
		boolean has_id_counter = counter != null;
		final String col = CxUtil.CX_ID_MAPPING;
		CyTable hidden_table = network.getTable(type, CyNetwork.HIDDEN_ATTRS);
		if (hidden_table.getColumn(col) == null) {
			hidden_table.createColumn(col, Long.class, true);
		}
		List<? extends CyIdentifiable> eles = type.equals(CyNode.class) ? network.getNodeList() : network.getEdgeList();
		for (CyIdentifiable ele : eles) {
			CyRow row = hidden_table.getRow(ele.getSUID());
			if (row.get(col, Long.class) == null) {
				if (!has_id_counter) {
					row.set(col, ele.getSUID());
					counter = counter == null ? ele.getSUID() : Math.max(counter, ele.getSUID());
				}else {
					row.set(col, ++counter);
				}
			}
		}
		updateCxIdCounter(type, network, counter);
	}
	
	static Long getCxId(CyIdentifiable cyEle, CyNetwork network) {
		CyTable hidden_table = network.getTable(cyEle instanceof CyNode ? CyNode.class : CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		CyRow row = hidden_table.getRow(cyEle.getSUID());
		return row.get(CxUtil.CX_ID_MAPPING, Long.class);
	}
	
	public static boolean hasCxIds(CyNetwork network) {
		CyTable hidden_table = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		return hidden_table.getColumn(CxUtil.CX_ID_MAPPING) != null;
	}
    
}
