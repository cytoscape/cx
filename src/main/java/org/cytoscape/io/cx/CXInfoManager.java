package org.cytoscape.io.cx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.cxio.core.AspectIterator;
import org.cxio.core.CXAspectWriter;
import org.cxio.core.OpaqueAspectIterator;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.metadata.MetaDataCollection;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.Provenance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CXInfoManager {

	private static ObjectMapper mapper = new ObjectMapper();
	
	public static void addNetworkUUID (CyNetwork network, UUID networkId) {
    	CyTable net_table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
    	createColumnIfNotExists(net_table, CxUtil.UUID_COLUMN, String.class);
    	CyRow row = net_table.getRow(network.getSUID());
    	row.set(CxUtil.UUID_COLUMN, networkId.toString());
    }
    
    public static UUID getNdexNetworkId(CyNetwork network) {
    	CyTable net_table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
    	if (net_table.getColumn(CxUtil.UUID_COLUMN) == null) {
    		return null;
    	}
    	CyRow row = net_table.getRow(network.getSUID());
    	String uuidStr = row.get(CxUtil.UUID_COLUMN, String.class);
    	if (uuidStr == null) {
    		return null;
    	}
    	return UUID.fromString(uuidStr);
    }
	
	public static boolean hasUUID(CyNetwork network) {
		return getNdexNetworkId(network) != null;
	}
	
	private static void createColumnIfNotExists(CyTable table, String name, Class<?> clz) {
		if (table.getColumn(name) == null) {
			table.createColumn(name, clz, true);
		}
	}

	public static void addNodeMapping(CyNetwork network, Long cyNodeId, Long cxNodeId) {
		CyTable table = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNotExists(table, CxUtil.CX_ID_COLUMN, Long.class);
		CyRow row = table.getRow(cyNodeId);
		if (row == null) {
			System.out.println("Row with id " + cyNodeId + " not found");
		}
		row.set(CxUtil.CX_ID_COLUMN, cxNodeId);
	}

	public static void addEdgeMapping(CyNetwork network, Long cyEdgeId, Long cxEdgeId) {
		CyTable table = network.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNotExists(table, CxUtil.CX_ID_COLUMN, Long.class);
		CyRow row = table.getRow(cyEdgeId);
		if (row == null) {
			System.out.println("Row with id " + cyEdgeId + " not found");
		}
		row.set(CxUtil.CX_ID_COLUMN, cxEdgeId);
	}

	public static Long getCXNodeId(CyNetwork network, Long cyNodeId) {
		CyTable table = network.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		if (table.getColumn(CxUtil.CX_ID_COLUMN) == null) {
			return null;
		}
		CyRow row = table.getRow(cyNodeId);
		return row.get(CxUtil.CX_ID_COLUMN, Long.class);
	}

	public static Long getCXEdgeId(CyNetwork network, Long cyEdgeId) {
		CyTable table = network.getTable(CyEdge.class, CyNetwork.HIDDEN_ATTRS);
		if (table.getColumn(CxUtil.CX_ID_COLUMN) == null) {
			return null;
		}
		CyRow row = table.getRow(cyEdgeId);
		return row.get(CxUtil.CX_ID_COLUMN, Long.class);
	}

	public static MetaDataCollection getMetadata(CyNetwork network) {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		if (table.getColumn(CxUtil.METADATA_COLUMN) == null) {
			return null;
		}
		CyRow row = table.getRow(network.getSUID());
		String metadataStr = row.get(CxUtil.METADATA_COLUMN, String.class);
		try {
			return mapper.readValue(metadataStr, MetaDataCollection.class);
		} catch (IOException e) {
			// TODO: ignore?
			return null;
		}
	}

	public static NamespacesElement getNamespaces(CyNetwork network) {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		if (table.getColumn(CxUtil.NAMESPACES_COLUMN) == null) {
			return null;
		}
		CyRow row = table.getRow(network.getSUID());
		String namespacesStr = row.get(CxUtil.NAMESPACES_COLUMN, String.class);
		try {
			NamespacesElement namespaces = mapper.readValue(namespacesStr, NamespacesElement.class);
			return namespaces;
		} catch (IOException e) {
			// TODO: ignore?
		}
		return null;
	}

	public static Provenance getProvenance(CyNetwork network) {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		if (table.getColumn(CxUtil.PROVENANCE_COLUMN) == null) {
			return null;
		}
		CyRow row = table.getRow(network.getSUID());
		String provenanceStr = row.get(CxUtil.PROVENANCE_COLUMN, String.class);
		try {
			Provenance provenance = mapper.readValue(provenanceStr, Provenance.class);
			return provenance;
		} catch (IOException e) {
			// TODO: ignore?
		}
		return null;
	}

	public static Map<String, Collection<AspectElement>> getOpaqueAspectsTable(CyNetwork network) {

		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		CyRow row = table.getRow(network.getSUID());

		Map<String, Collection<AspectElement>> opaqueAspectsTable = new HashMap<String, Collection<AspectElement>>();
		for (CyColumn col : table.getColumns()) {
			String name = col.getName();
			if (name.startsWith(CxUtil.OPAQUE_ASPECTS_COLUMN_PREFIX)) {
				String value = row.get(name, String.class);
				name = name.substring(CxUtil.OPAQUE_ASPECTS_COLUMN_PREFIX.length() + 1);
				try (ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes())) {
					
					OpaqueAspectIterator iterator = new OpaqueAspectIterator(in);
					ArrayList<AspectElement> elements = new ArrayList<AspectElement>();
					while (iterator.hasNext()) {
						elements.add(iterator.next());
					}
					opaqueAspectsTable.put(name, elements);
				} catch (IOException e) {
					// TODO: handle missed aspect
				}
			}
		}
		return opaqueAspectsTable;

	}

	public static void setMetadata(CyNetwork network, MetaDataCollection metadata) throws JsonProcessingException {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNotExists(table, CxUtil.METADATA_COLUMN, String.class);
		CyRow row = table.getRow(network.getSUID());
		row.set(CxUtil.METADATA_COLUMN, mapper.writeValueAsString(metadata));
	}

	public static void setProvenance(CyNetwork network, Provenance provenance) throws JsonProcessingException {
		String ser = mapper.writeValueAsString(provenance);

		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNotExists(table, CxUtil.PROVENANCE_COLUMN, String.class);
		CyRow row = table.getRow(network.getSUID());
		row.set(CxUtil.PROVENANCE_COLUMN, ser);
	}

	public static void setNamespaces(CyNetwork network, NamespacesElement namespaces) throws JsonProcessingException {
		String ser = mapper.writeValueAsString(namespaces);

		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		createColumnIfNotExists(table, CxUtil.NAMESPACES_COLUMN, String.class);
		CyRow row = table.getRow(network.getSUID());
		row.set(CxUtil.NAMESPACES_COLUMN, ser);
	}

	public static void setOpaqueAspectsTable(CyNetwork network,
			Map<String, Collection<AspectElement>> opaqueAspectsTable) throws JsonProcessingException {

		for (Entry<String, Collection<AspectElement>> entry : opaqueAspectsTable.entrySet()) {
			String col_name = CxUtil.OPAQUE_ASPECTS_COLUMN_PREFIX + " " + entry.getKey();

			CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
			createColumnIfNotExists(table, col_name, String.class);
			CyRow row = table.getRow(network.getSUID());

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
			try (CXAspectWriter writer = new CXAspectWriter(output)) {
				for (AspectElement aspect : entry.getValue()) {
					writer.writeCXElement(aspect);
				}
			} catch (IOException e) {
				// TODO: handle exception
			}

			row.set(col_name, new String(output.toByteArray()));
		}
	}

}
