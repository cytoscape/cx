package org.cytoscape.io.internal.cxio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.AttributesAspectUtils;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.HiddenAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.ndexbio.cxio.core.CxWriter;
import org.ndexbio.cxio.core.OpaqueAspectIterator;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.core.interfaces.AspectFragmentWriter;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.metadata.MetaDataElement;
import org.ndexbio.cxio.misc.AspectElementCounts;
import org.ndexbio.cxio.misc.OpaqueElement;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.cx.Aspect;
import org.cytoscape.io.internal.cx_writer.AnnotationsGatherer;
import org.cytoscape.io.internal.cx_writer.VisualPropertiesGatherer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * This class is for serializing Cytoscape networks, views, and attribute tables
 * as CX formatted output streams. <br>
 * <br>
 * In particular, it provides the following methods for writing CX: <br>
 * <ul>
 * <li>{@link #writeCX(CyNetwork, AspectSet, OutputStream)}</li>
 * <li>{@link #writeCX(CyNetworkView, AspectSet, OutputStream)}</li>
 * <li>
 * <li>
 * </ul>
 * <br>
 * <br>
 * These methods use: <br>
 * <ul>
 * <li>{@link AspectSet} to control which aspects to serialize</li>
 * <li>aspect</li>
 * </ul>
 * <br>
 *
 * @see AspectSet
 * @see Aspect
 * @see CxOutput
 * @see CxImporter
 *
 *
 */
public final class CxExporter {

	private VisualMappingManager _visual_mapping_manager;
	private AnnotationManager _annotation_manager;
	private CyNetworkViewManager _networkview_manager;
	private CyGroupManager _group_manager;
	private CyApplicationManager _application_manager;

	private final static Set<String> ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES = new HashSet<>();
	private final static Set<String> ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES = new HashSet<>();
	private final static Set<String> ADDITIONAL_IGNORE_FOR_NETWORK_ATTRIBUTES = new HashSet<>();

	static {
		//TODO: We do not want to ignore data, it will mess up styling
//		ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES.add(CxUtil.SHARED_INTERACTION);
//		ADDITIONAL_IGNORE_FOR_NETWORK_ATTRIBUTES.add(CxUtil.SHARED_NAME_COL);
//		ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES.add(CxUtil.SHARED_NAME_COL);
		ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES.add(CxUtil.REPRESENTS);
	}

	/**
	 * This returns a new instance of CxExporter.
	 *
	 * @return a new CxExporter
	 */
	public final static CxExporter createInstance() {
		return new CxExporter();
	}

	final static Set<AspectFragmentWriter> getCySupportedAspectFragmentWriters() {
		return AspectSet.getCytoscapeAspectSet().getAspectFragmentWriters();
	}

	public void setGroupManager(final CyGroupManager group_manager) {
		_group_manager = group_manager;
	}

	public void setApplicationManager(final CyApplicationManager application_manager) {
		_application_manager = application_manager;
	}

	public void setNetworkViewManager(final CyNetworkViewManager networkview_manager) {
		_networkview_manager = networkview_manager;
	}

	public void setVisualMappingManager(final VisualMappingManager visual_mapping_manager) {
		_visual_mapping_manager = visual_mapping_manager;
	}

	public void setAnnotationManager(final AnnotationManager annotation_manager) {
		_annotation_manager = annotation_manager;
	}
	
	/**
	 * This is a method for serializing a Cytoscape network and associated table
	 * data as CX formatted OutputStream. <br>
	 * Method arguments control which aspects to serialize, and for data stored in
	 * node and tables (serialized as node attributes and edge attributes aspects),
	 * which table columns to include or exclude.
	 *
	 *
	 * @param network
	 *            the CyNetwork, and by association, tables to be serialized
	 * @param aspects
	 *            the set of aspects to serialize
	 * @param out
	 *            the stream to write to
	 * @return a CxOutput object which contains the output stream as well as a
	 *         status
	 * @throws IOException
	 *
	 *
	 * @see AspectSet
	 * @see Aspect
	 * @see FilterSet
	 *
	 */

	public final boolean writeNetwork(final CyNetwork network, final boolean write_siblings, final boolean use_cxId, 
			final AspectSet aspects, final OutputStream out) throws IOException {
		if (write_siblings && use_cxId) {
			throw new IllegalArgumentException("Cannot export a collection with CX IDs.");
		}
		Settings.INSTANCE.debug("Writing " + network + " as " + (write_siblings ? "collection" : "subnetwork"));
		if (!aspects.contains(Aspect.SUBNETWORKS)) {
			if (aspects.contains(Aspect.VISUAL_PROPERTIES)) {
				throw new IllegalArgumentException("need to write sub-networks in order to write visual properties");
			}
			if (aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
				throw new IllegalArgumentException("need to write sub-networks in order to write cartesian layout");
			}
		}

		final CxWriter w = CxWriter.createInstance(out, false);

		for (final AspectFragmentWriter writer : getCySupportedAspectFragmentWriters()) {
			w.addAspectFragmentWriter(writer);
		}

		Set<Long> groupNodeIds = this.getGroupNodeIds(network, write_siblings);

		w.start();

		String msg = null;
		boolean success = true;

		try {
			writeNodes(network, w, write_siblings, use_cxId, groupNodeIds);
			writeGroups(network, w, write_siblings, use_cxId);
			writeNodeAttributes(network, w, write_siblings, use_cxId, CyNetwork.DEFAULT_ATTRS, groupNodeIds);
			writeEdges(network, w, write_siblings, use_cxId);
			writeEdgeAttributes(network, w, write_siblings, use_cxId, CyNetwork.DEFAULT_ATTRS);
			
			if (write_siblings) {
				writeNetworkRelations(network, w, true);
				writeCxIds(network, w);
			}
			// Writes cartesianlayout and visual props, only writes subnets for collections
			writeSubNetworks(network, write_siblings, use_cxId, w, aspects);
			
			writeTableColumns(network, write_siblings, w);
			writeNetworkAttributes(network, write_siblings, w);
			writeHiddenAttributes(network, write_siblings, w, CyNetwork.HIDDEN_ATTRS); //This also handles Opaque aspects

			final AspectElementCounts aspects_counts = w.getAspectElementCounts();
			
			MetaDataCollection mdc = addPostMetadata(w, aspects_counts, write_siblings, network);
			CxUtil.setMetaData(network, mdc);

		} catch (final Exception e) {
			e.printStackTrace();
			msg = "Failed to create complete network from cyNDEx: " + e.getMessage();
			success = false;
		}

		w.end(success, msg);

		if (success) {
			final AspectElementCounts counts = w.getAspectElementCounts();
			if (counts != null) {
				System.out.println("Aspects elements written out:");
				System.out.println(counts);
			}

		}

		return success;
	}
	
	private void writeCxIds(CyNetwork network, CxWriter w) throws IOException {
		if (!CxUtil.hasCxIds(network)) {
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode data = mapper.createObjectNode();
		for (CyNode node : network.getNodeList()) {
			String suid = String.valueOf(node.getSUID());
			long cxId = CxUtil.getCxId(node, network);
			data.put(suid, cxId);
		}
		for (CyEdge edge : network.getEdgeList()) {
			String suid = String.valueOf(edge.getSUID());
			long cxId = CxUtil.getCxId(edge, network);
			data.put(suid, cxId);
		}
		
		String aspectName = "cySubNetworkCxIds";
		OpaqueElement element = new OpaqueElement(aspectName, data);
		w.startAspectFragment(aspectName);
		w.writeOpaqueAspectElement(element);
		w.endAspectFragment();
		
	}

	private final static void writeTableColumnsHelper(CyNetwork subnet, String applies_to, List<AspectElement> elements, boolean write_siblings) {
		CyTable table = null;
		switch(applies_to) {
		case "node_table":
			table = subnet.getDefaultNodeTable();
			break;
		case "edge_table":
			table = subnet.getDefaultEdgeTable();
			break;
		case "network_table":
			table = subnet.getDefaultNetworkTable();
			break;
		default:
			throw new IllegalArgumentException("Unknown applies_to in CyTableColumn: " + applies_to);
		}
		Collection<CyColumn> c = table.getColumns();
		Long subNetId = write_siblings ? subnet.getSUID() : null;
		for (CyColumn col : c) {
			if (col.getName().equals(CxUtil.SUID)) {
				continue;
			}
			ATTRIBUTE_DATA_TYPE type = ATTRIBUTE_DATA_TYPE.STRING;
			if (col.getType() != List.class) {
				type = toAttributeType(col.getType());
			} else {
				type = toListAttributeType(col.getListElementType());
			}

			CyTableColumnElement x = new CyTableColumnElement(subNetId, applies_to, col.getName(), type);
			elements.add(x);
		}
	}
	
	private final static void writeTableColumns(final CyNetwork network, final boolean write_siblings,
			final CxWriter w) throws IOException {
		
		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			writeTableColumnsHelper(subnet, "node_table", elements, write_siblings);
			writeTableColumnsHelper(subnet, "edge_table", elements, write_siblings);
			writeTableColumnsHelper(subnet, "network_table", elements, write_siblings);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "table columns ", elements.size());
		}
	}
	
	private final static ATTRIBUTE_DATA_TYPE toAttributeType(final Class<?> attr_class) {
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
			throw new IllegalArgumentException("don't know how to deal with type '" + attr_class + "'");
		}
	}

	private final static ATTRIBUTE_DATA_TYPE toListAttributeType(final Class<?> attr_class) {
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
			throw new IllegalArgumentException("don't know how to deal with type '" + attr_class + "'");
		}
	}
	

	private final static void addDataToMetaDataCollection(final MetaDataCollection meta_data,
			final String aspect_name, final Long count, final Long id_counter) {

		if (count != null && count == 0) {
			return;
		}
		MetaDataElement e = meta_data.getMetaDataElement(aspect_name);
		if (e == null) {
			e = new MetaDataElement(aspect_name, "1.0");
			meta_data.add(e);
		}
		if (count != null) {
			e.setElementCount(count);
		}
		if (id_counter != null) {
			e.setIdCounter(id_counter);
		}
	}

	private final static String getSharedInteractionFromEdgeTable(final CyNetwork network, final CyEdge edge) {
		final CyRow row = network.getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS).getRow(edge.getSUID());
		if (row != null) {
			final Object o = row.getRaw(CxUtil.SHARED_INTERACTION);
			if ((o != null) && (o instanceof String)) {
				return String.valueOf(o);
			}
		}
		return null;
	}

	private final static <T> T getNodeAttributeValue(final CyNetwork network, final CyNode node, String colName,
			Class<? extends T> type) {
		final CyRow row = network.getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS).getRow(node.getSUID());
		if (row != null) {
			final T o = row.get(colName, type);
			if ((o != null)) {
				return o;
			}
		}
		return null;
	}

	private final static List<CySubNetwork> makeSubNetworkList(final boolean write_siblings,
			final CySubNetwork sub_network, final CyRootNetwork root, final boolean ignore_nameless_sub_networks) {
		List<CySubNetwork> subnets = new ArrayList<>();

		if (write_siblings) {
			for (final CySubNetwork s : root.getSubNetworkList()) {
				if (!ignore_nameless_sub_networks || (getSubNetworkName(s) != null)) {
					subnets.add(s);
				}
			}
		} else {
			subnets = new ArrayList<>();
			if (!ignore_nameless_sub_networks || (getSubNetworkName(sub_network) != null)) {
				subnets.add(sub_network);
			}
		}
		return subnets;
	}

	private final static void writeCartesianLayout(final CyNetworkView view, final CxWriter w, boolean writeSiblings, boolean use_cxId)
			throws IOException {
		
		final CyNetwork network = view.getModel();
		final List<AspectElement> elements = new ArrayList<>(network.getNodeCount());

		boolean z_used = false;
		for (final CyNode cy_node : network.getNodeList()) {
			final View<CyNode> node_view = view.getNodeView(cy_node);
			if (Math.abs(node_view.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)) > 0.000000001) {
				z_used = true;
				break;
			}
		}

		final Long viewId = writeSiblings ? view.getSUID() : null;
		for (final CyNode cy_node : network.getNodeList()) {
			Long nodeId = getElementId(cy_node, network, use_cxId);
			final View<CyNode> node_view = view.getNodeView(cy_node);
			if (z_used) {
				elements.add(new CartesianLayoutElement(nodeId, viewId,
						node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
						node_view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION),
						node_view.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)));
			} else {
				Double x = node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				Double y = node_view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

				elements.add(new CartesianLayoutElement(nodeId, viewId, x.toString(), y.toString()));
			}

		}
		
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "cartesian layout", elements.size());
		}
	}

	private final void writeEdges(final CyNetwork network, final CxWriter w, final boolean write_siblings, final boolean use_cxId)
			throws IOException {
		
		
		final List<AspectElement> elements = new ArrayList<>(network.getEdgeCount());
		final CyRootNetwork my_root = ((CySubNetwork) network).getRootNetwork();
		if (write_siblings) {
			for (final CyEdge cy_edge : my_root.getEdgeList()) {
				elements.add(new EdgesElement(cy_edge.getSUID(), cy_edge.getSource().getSUID(),
						cy_edge.getTarget().getSUID(), getSharedInteractionFromEdgeTable(my_root, cy_edge)));
			}
		} else {
			for (final CyEdge cy_edge : ((CySubNetwork) network).getEdgeList()) {
				elements.add(new EdgesElement(getElementId(cy_edge, network, use_cxId),
						getElementId(cy_edge.getSource(), network, use_cxId),
						getElementId(cy_edge.getTarget(), network, use_cxId),
						getSharedInteractionFromEdgeTable(network, cy_edge)));
			}
		}
		
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "edges", elements.size());
		}
	}

	private final void writeNetworkRelations(final CyNetwork network, final CxWriter w,
			final boolean ignore_nameless_sub_networks) throws IOException {
		
		
		final CySubNetwork as_subnet = (CySubNetwork) network;
		final CyRootNetwork root = as_subnet.getRootNetwork();
		final List<CySubNetwork> subnetworks = makeSubNetworkList(true, as_subnet, root, true);

		final List<AspectElement> elements = new ArrayList<>();

		for (final CySubNetwork subnetwork : subnetworks) {

			final String name = getSubNetworkName(subnetwork);
			if (ignore_nameless_sub_networks && (name == null)) {
				continue;
			}

			// Subnetworks does not need root ID since it's not used in CX.
			final NetworkRelationsElement rel_subnet = new NetworkRelationsElement(null, subnetwork.getSUID(),
					NetworkRelationsElement.TYPE_SUBNETWORK, name);
			// PLEASE NOTE:
			// Cytoscape currently has only one view per sub-network.
			final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(subnetwork);
			for (final CyNetworkView view : views) {
				final NetworkRelationsElement rel_view = new NetworkRelationsElement(subnetwork.getSUID(),
						view.getSUID(), NetworkRelationsElement.TYPE_VIEW, name + " view");
				elements.add(rel_view);
			}
			elements.add(rel_subnet);

		}
		
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "network relations", elements.size());
		}

	}

	public static String getSubNetworkName(final CySubNetwork subnetwork) {
		final CyRow row = subnetwork.getRow(subnetwork, CyNetwork.DEFAULT_ATTRS);
		String name = null;
		final Map<String, Object> values = row.getAllValues();
		if ((values != null) && !values.isEmpty()) {
			if (values.get(CxUtil.NAME_COL) != null) {
				final String str = String.valueOf(values.get(CxUtil.NAME_COL));
				if ((str != null) && (str.trim().length() > 0)) {
					name = str;
				}
			}
		}
		return name;
	}

	private final static void writeNodes(final CyNetwork network, final CxWriter w, final boolean write_siblings, 
			final boolean use_cxId, Set<Long> grpNodes) throws IOException {
		
		final CyNetwork workingNet = write_siblings ? ((CySubNetwork) network).getRootNetwork() : network;
		final List<AspectElement> elements = new ArrayList<>(workingNet.getNodeCount());
		
		//TODO: Decide which column to use, and if that column should be omitted from nodeAttributes
		String attName = write_siblings ? CxUtil.SHARED_NAME_COL : CxUtil.NAME_COL;

		for (final CyNode cy_node : workingNet.getNodeList()) {

			Long cxId = getElementId(cy_node, network, use_cxId);

			NodesElement elmt = grpNodes.contains(cy_node.getSUID()) ? 
					new NodesElement(cxId.longValue(), null, null)
					: new NodesElement(cxId.longValue(),
							getNodeAttributeValue(network, cy_node, attName, String.class),
							getNodeAttributeValue(network, cy_node, CxUtil.REPRESENTS, String.class));

			elements.add(elmt);
		}

		w.writeAspectElements(elements);

	}
	
	
	public static Long getElementId(CyIdentifiable cy_ele, CyNetwork network, boolean use_cxId) {
		if (!use_cxId) {
			return cy_ele.getSUID();
		}
		
		Long cx_id = CxUtil.getCxId(cy_ele, network);
		
		if (cx_id != null) {
			return cx_id;
		}
		if (cy_ele instanceof CyNode) {
			CxUtil.populateCxIdColumn(CyNode.class, network);
		}else {
			CxUtil.populateCxIdColumn(CyEdge.class, network);
		}

		return CxUtil.getCxId(cy_ele, network);
	}
	
	private final static void writeVisualProperties(final CyNetworkView view,
			final VisualMappingManager visual_mapping_manager, final VisualLexicon lexicon, final CxWriter w,
			boolean writeSiblings, boolean use_cxId) throws IOException {
		
		
		final Set<VisualPropertyType> types = new HashSet<>();
		types.add(VisualPropertyType.NETWORK);
		types.add(VisualPropertyType.NODES);
		types.add(VisualPropertyType.EDGES);
		types.add(VisualPropertyType.NODES_DEFAULT);
		types.add(VisualPropertyType.EDGES_DEFAULT);

		final List<AspectElement> elements = VisualPropertiesGatherer.gatherVisualPropertiesAsAspectElements(view,
				visual_mapping_manager, lexicon, types, writeSiblings, use_cxId);
		
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "visual properties", elements.size());
		}
	}

	private final static void writeAnnotations(final CyNetworkView view, final AnnotationManager annotationManager,
			final CxWriter w,
			boolean writeSiblings) throws IOException {
		
		final List<AspectElement> elements = AnnotationsGatherer.gatherAnnotationsAsAspectElements(view, annotationManager);
		
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "annotations", elements.size());
		}
	}
	
	private final MetaDataCollection addPostMetadata(final CxWriter w, final AspectElementCounts aspects_counts,
			boolean write_siblings, CyNetwork network) {

		MetaDataCollection post_meta_data = CxUtil.getMetaData(network);
		if (post_meta_data == null) {
			post_meta_data = new MetaDataCollection();
		}
		
		for (String name : aspects_counts.getAllAspectNames()) {
			long count = (long) aspects_counts.getAspectElementCount(name);
			Long idCounter = null;
			switch(name) {
			case NodesElement.ASPECT_NAME:
				count = (long) network.getNodeList().size();
				idCounter = (write_siblings ? SUIDFactory.getNextSUID() : null);
				break;
			case EdgesElement.ASPECT_NAME:
				count = (long) network.getEdgeList().size();
				idCounter = (write_siblings ? SUIDFactory.getNextSUID() : null);
				break;
			case NetworkRelationsElement.ASPECT_NAME:
			case SubNetworkElement.ASPECT_NAME:
				if (!write_siblings) {
					continue;
				}
			}
			addDataToMetaDataCollection(post_meta_data, name, count, idCounter);
		}
		
		
		final long t0 = System.currentTimeMillis();
		w.addPostMetaData(post_meta_data);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "post meta-data", -1);
		}
		return post_meta_data;
	}

	private final static void writeEdgeAttributes(final CyNetwork network, final CxWriter w, final boolean write_siblings, boolean use_cxId,
			 final String namespace) throws IOException {
		
		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			writeEdgeAttributesHelper(namespace, subnet, subnet.getEdgeList(), elements, write_siblings, use_cxId);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "edge attributes", elements.size());
		}
	}

	private final static boolean isIgnore(final String column_name, final Set<String> additional_to_ignore,
			final Settings setttings, Object value) {
		switch(column_name) {
			case CxUtil.SUID:
				return setttings.isIgnoreSuidColumn();
			case CxUtil.SELECTED:
				return setttings.isIgnoreSelectedColumn() || (value instanceof Boolean && 
						(Boolean) value != true && setttings.isWriteSelectedOnlyIfTrue());
			case CxUtil.CX_ID_MAPPING:
			case CxUtil.CX_METADATA:
				return true;
			default:
				return ((additional_to_ignore != null) && additional_to_ignore.contains(column_name));
		}
	}

	@SuppressWarnings("rawtypes")
	private static void writeEdgeAttributesHelper(final String namespace, final CyNetwork my_network,
			final List<CyEdge> edges, final List<AspectElement> elements, boolean writeSiblings, boolean use_cxId) {

		for (final CyEdge cy_edge : edges) {
			final CyRow row = my_network.getRow(cy_edge, namespace);
			if (row != null) {

				final Map<String, Object> values = row.getAllValues();
				if ((values != null) && !values.isEmpty()) {
					for (String column_name : values.keySet()) {
						final Object value = values.get(column_name);
						if (value == null || (value instanceof String && ((String) value).length() == 0)) {
							continue;
						}
						
						if (isIgnore(column_name, ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES, Settings.INSTANCE, value)) {
							continue;
						}
						
						EdgeAttributesElement e = null;
						final Long subnet = writeSiblings ? my_network.getSUID() : null;
						if (column_name.equals(CxUtil.SHARED_NAME_COL))
							column_name = "name";
						long edge_id = getElementId(cy_edge, my_network, use_cxId);
						if (value instanceof List) {
							final List<String> attr_values = new ArrayList<>();
							for (final Object v : (List) value) {
								attr_values.add(String.valueOf(v));
							}
							if (!attr_values.isEmpty()) {
								e = new EdgeAttributesElement(subnet, edge_id,
										column_name, attr_values, AttributesAspectUtils.determineDataType(value));
							}
						} else {
							e = new EdgeAttributesElement(subnet, edge_id, column_name,
									String.valueOf(value), AttributesAspectUtils.determineDataType(value));
						}
						if (e != null) {
							elements.add(e);
						}
					}
				}
			}
		}
	}

	private final Set<Long> getGroupNodeIds(final CyNetwork network, boolean writeSiblings) {
		Set<Long> cyGrpNodeIds = new TreeSet<>();

		if (writeSiblings) {
			final CySubNetwork my_subnet = (CySubNetwork) network;
			final CyRootNetwork my_root = my_subnet.getRootNetwork();

			final List<CySubNetwork> subnets = makeSubNetworkList(true, my_subnet, my_root, true);

			for (final CySubNetwork subnet : subnets) {
				getGroupNodeIdsInSubNet(cyGrpNodeIds, subnet);
			}
		} else
			getGroupNodeIdsInSubNet(cyGrpNodeIds, network);

		return cyGrpNodeIds;
	}

	private void getGroupNodeIdsInSubNet(Set<Long> resultHolder, CyNetwork subnet) {
		final Set<CyGroup> groups = _group_manager.getGroupSet(subnet);
		for (final CyGroup group : groups) {
			resultHolder.add(group.getGroupNode().getSUID());
		}
	}

	private final void writeGroups(final CyNetwork network, final CxWriter w, boolean writeSiblings, boolean use_cxId)
			throws IOException {
		
		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();

		final List<CySubNetwork> subnets = makeSubNetworkList(writeSiblings, my_subnet, my_root, false);

		final List<AspectElement> elements = new ArrayList<>();
		for (final CySubNetwork subnet : subnets) {
			/*
			 * final Collection<CyNetworkView> views =
			 * _networkview_manager.getNetworkViews(subnet); if ((views == null) ||
			 * (views.size() < 1)) { continue; } if (views.size() > 1) {
			 * System.out.println("multiple views for sub-network " + subnet +
			 * ", problem with attaching groups"); continue; } Long view_id = 0L; for (final
			 * CyNetworkView view : views) { view_id = view.getSUID(); }
			 */
			final Set<CyGroup> groups = _group_manager.getGroupSet(subnet);
			for (final CyGroup group : groups) {
				String name = null;
				final CyRow row = my_root.getRow(group.getGroupNode(), CyNetwork.DEFAULT_ATTRS);
				if (row != null) {
					name = row.get(CxUtil.SHARED_NAME_COL, String.class);
				}
				/*
				 * if ((name == null) || (name.length() < 1)) { name = "group " +
				 * group.getGroupNode().getSUID(); }
				 */

				final CyGroupsElement group_element = new CyGroupsElement(
						getElementId(group.getGroupNode(), network, use_cxId), writeSiblings ? subnet.getSUID() : null,
						name);
				for (final CyEdge e : group.getExternalEdgeList()) {
					group_element.addExternalEdge(Long.valueOf(getElementId(e, network, use_cxId)));
				}
				for (final CyEdge e : group.getInternalEdgeList()) {
					group_element.addInternalEdge(Long.valueOf(getElementId(e, network, use_cxId)));
				}
				for (final CyNode n : group.getNodeList()) {
					group_element.addNode(getElementId(n, network, use_cxId));
				}
				boolean isCollapsed = group.isCollapsed(subnet);
				group_element.set_isCollapsed(isCollapsed);
				elements.add(group_element);
			}

		}
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			 TimingUtil.reportTimeDifference(t0, "groups", elements.size()); 
		}
	}

	private final static void writeHiddenAttributes(final CyNetwork network, final boolean write_siblings,
			final CxWriter w, final String namespace) throws IOException {
		
		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);
		
		final long t0 = System.currentTimeMillis();
		
		for (final CySubNetwork subnet : subnets) {
			writeHiddenAttributesHelper(namespace, subnet, w, write_siblings);
		}
		
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "network attributes", elements.size());
		}
	}

	@SuppressWarnings("rawtypes")
	private static void writeHiddenAttributesHelper(final String namespace, final CyNetwork my_network,
			final CxWriter w, boolean writeSiblings) throws IOException {

		final CyRow row = my_network.getRow(my_network, namespace);
		if (row != null) {
			final Map<String, Object> values = row.getAllValues();

			if ((values != null) && !values.isEmpty()) {
				for (final String column_name : values.keySet()) {
					final Object value = values.get(column_name);
					if (value == null) {
						continue;
					}
					if (isIgnore(column_name, null, Settings.INSTANCE, value)) {
						continue;
					}
					
					if (column_name.startsWith(CxUtil.OPAQUE_ASPECT_PREFIX)) {
						writeOpaqueElement(column_name.substring(CxUtil.OPAQUE_ASPECT_PREFIX.length()), (String)value, w);
						continue;
					}
					
					HiddenAttributesElement e = null;
					Long subnet = writeSiblings ? my_network.getSUID() : null;
					if (value instanceof List) {
						final List<String> attr_values = new ArrayList<>();
						for (final Object v : (List) value) {
							attr_values.add(String.valueOf(v));
						}
						if (!attr_values.isEmpty()) {
							e = new HiddenAttributesElement(subnet, column_name, attr_values,
									AttributesAspectUtils.determineDataType(value));
						}
					} else {
						e = new HiddenAttributesElement(subnet, column_name, String.valueOf(value),
								AttributesAspectUtils.determineDataType(value));
					}
					if (e != null) {
						w.startAspectFragment(column_name);
						w.writeAspectElement(e);
						w.endAspectFragment();
					}
				}
			}

		}
	}

	private static void writeOpaqueElement(String column, String value, CxWriter w) throws JsonParseException, IOException {
		InputStream in = new ByteArrayInputStream(value.getBytes());
		OpaqueAspectIterator iter = new OpaqueAspectIterator(in);
		w.startAspectFragment(column);
		while (iter.hasNext()) {
			w.writeOpaqueAspectElement(iter.next());
		}
		w.endAspectFragment();
	}

	private final static void writeNetworkAttributes(final CyNetwork network, final boolean write_siblings,
			final CxWriter w) throws IOException {
		
		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();

		final String collection_name = obtainNetworkCollectionName(my_root);
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		Settings.INSTANCE.debug("collection name: " + collection_name);

		// Write root table
		if (write_siblings)
			writeNetworkAttributesHelper(CyNetwork.DEFAULT_ATTRS, my_root, elements, false);

		for (final CySubNetwork subnet : subnets) {
			writeNetworkAttributesHelper(CyNetwork.DEFAULT_ATTRS, subnet, elements, write_siblings);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "network attributes", elements.size());
		}
	}

	public final static String obtainNetworkCollectionName(final CyRootNetwork root_network) {
		String collection_name = null;
		if (root_network != null) {
			final CyRow row = root_network.getRow(root_network, CyNetwork.DEFAULT_ATTRS);
			if (row != null) {
				try {
					collection_name = String.valueOf(row.getRaw("name"));
				} catch (final Exception e) {
					collection_name = null;
				}
			}
		}
		return collection_name;
	}

	@SuppressWarnings("rawtypes")
	private static void writeNetworkAttributesHelper(final String namespace, final CyNetwork my_network,
			final List<AspectElement> elements, boolean writeSiblings) throws IOException {

		final CyRow row = my_network.getRow(my_network, namespace);

		if (row != null) {
			final Map<String, Object> values = row.getAllValues();

			if ((values != null) && !values.isEmpty()) {
				for (final String column_name : values.keySet()) {
					final Object value = values.get(column_name);
					if (value == null) {
						continue;
					}
					if (isIgnore(column_name, ADDITIONAL_IGNORE_FOR_NETWORK_ATTRIBUTES, Settings.INSTANCE, value)) {
						continue;
					}
					
					NetworkAttributesElement e = null;

					Long subnet = null;
					if (writeSiblings) {
						subnet = my_network.getSUID();
					}

					if (value instanceof List) {
						final List<String> attr_values = new ArrayList<>();
						for (final Object v : (List) value) {
							attr_values.add(String.valueOf(v));
						}
						if (!attr_values.isEmpty()) {
							e = new NetworkAttributesElement(subnet, column_name, attr_values,
									AttributesAspectUtils.determineDataType(value));
						}
					} else {
						e = new NetworkAttributesElement(subnet, column_name, String.valueOf(value),
								AttributesAspectUtils.determineDataType(value));
					}
					if (e != null) {
						elements.add(e);
					}
				}
			}
		}
	}

	private final static void writeNodeAttributes(final CyNetwork network, final CxWriter w, final boolean write_siblings, boolean use_cxId,
			 final String namespace, Set<Long> groupNodeIds) throws IOException {

		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			writeNodeAttributesHelper(namespace, subnet, subnet.getNodeList(), elements, write_siblings, use_cxId, groupNodeIds);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "node attributes", elements.size());
		}
	}

	@SuppressWarnings("rawtypes")
	private static void writeNodeAttributesHelper(final String namespace, final CySubNetwork my_network,
			final List<CyNode> nodes, final List<AspectElement> elements, boolean writeSiblings, boolean use_cxId, Set<Long> grpNodeIds) {
		
		for (final CyNode cy_node : nodes) {
//			if (grpNodeIds.contains(cy_node.getSUID()))
//				continue;
			final CyRow row = my_network.getRow(cy_node, namespace);
			if (row != null) {
				final Map<String, Object> values = row.getAllValues();

				if ((values != null) && !values.isEmpty()) {
					for (final String column_name : values.keySet()) {
						final Object value = values.get(column_name);
						if (value == null) {
							continue;
						}
						
						if (isIgnore(column_name, ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES, Settings.INSTANCE, value)) {
							continue;
						}
//						if (writeSiblings == false && column_name.equals(CxUtil.NAME_COL)) {
//							continue;
//						}
						
						NodeAttributesElement e = null;
						
						final Long subnet = writeSiblings ? my_network.getSUID() : null;
						
						Long nodeId = getElementId(cy_node, my_network, use_cxId);

						if (value instanceof List) {
							final List<String> attr_values = new ArrayList<>();
							for (final Object v : (List) value) {
								attr_values.add(String.valueOf(v));
							}
							if (!attr_values.isEmpty()) {
								e = new NodeAttributesElement(subnet, nodeId, column_name, attr_values,
										AttributesAspectUtils.determineDataType(value));
							}
						} else {
							e = new NodeAttributesElement(subnet, nodeId, column_name, String.valueOf(value),
									AttributesAspectUtils.determineDataType(value));
						}
						if (e != null) {
							elements.add(e);
						}
					}
				}
			}
		}
	}
	
	private VisualLexicon getLexicon(CyNetworkView view) {
    	NetworkViewRenderer renderer = _application_manager.getNetworkViewRenderer(view.getRendererId());

		RenderingEngineFactory<CyNetwork> factory = renderer == null ? null

				: renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);

		VisualLexicon lexicon = factory == null ? null : factory.getVisualLexicon();
		return lexicon;
	}

	private final void writeSubNetworks(final CyNetwork network, final boolean write_siblings, final boolean use_cxId, final CxWriter w,
			final AspectSet aspects) throws IOException {

		// write the subNetwork only when exporting the collection
		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		// write the visual properties and coordinates
		for (final CySubNetwork subnet : subnets) {
			final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(subnet);
			for (final CyNetworkView view : views) {
				final VisualLexicon _lexicon = getLexicon(view);
				writeCartesianLayout(view, w, write_siblings, use_cxId);
				writeVisualProperties(view, _visual_mapping_manager, _lexicon, w, write_siblings, use_cxId);
				writeAnnotations(view, _annotation_manager, w, write_siblings);
			}
		}

		if (write_siblings) {
			final List<AspectElement> elements = new ArrayList<>();
			for (final CySubNetwork subnet : subnets) {
				final SubNetworkElement subnetwork_element = new SubNetworkElement(subnet.getSUID());
				for (final CyEdge edgeview : subnet.getEdgeList()) {
					subnetwork_element.addEdge(edgeview.getSUID());
				}
				for (final CyNode nodeview : subnet.getNodeList()) {
					subnetwork_element.addNode(nodeview.getSUID());
				}
				elements.add(subnetwork_element);
			}
			
			final long t0 = System.currentTimeMillis();
			w.writeAspectElements(elements);
			if (Settings.INSTANCE.isTiming()) {
				TimingUtil.reportTimeDifference(t0, "subnetworks", elements.size());
			}
		}

	}

}
