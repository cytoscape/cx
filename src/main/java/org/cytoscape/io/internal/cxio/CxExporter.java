package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.cxio.aspects.datamodels.AttributesAspectUtils;
import org.cxio.aspects.datamodels.CartesianLayoutElement;
import org.cxio.aspects.datamodels.CyGroupsElement;
import org.cxio.aspects.datamodels.CyTableColumnElement;
import org.cxio.aspects.datamodels.CyViewsElement;
import org.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.HiddenAttributesElement;
import org.cxio.aspects.datamodels.NetworkAttributesElement;
import org.cxio.aspects.datamodels.NetworkRelationsElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.datamodels.SubNetworkElement;
import org.cxio.aspects.writers.CartesianLayoutFragmentWriter;
import org.cxio.aspects.writers.CyGroupsFragmentWriter;
import org.cxio.aspects.writers.CyTableColumnFragmentWriter;
import org.cxio.aspects.writers.CyViewsFragmentWriter;
import org.cxio.aspects.writers.EdgeAttributesFragmentWriter;
import org.cxio.aspects.writers.EdgesFragmentWriter;
import org.cxio.aspects.writers.GeneralAspectFragmentWriter;
import org.cxio.aspects.writers.HiddenAttributesFragmentWriter;
import org.cxio.aspects.writers.NetworkAttributesFragmentWriter;
import org.cxio.aspects.writers.NetworkRelationsFragmentWriter;
import org.cxio.aspects.writers.NodeAttributesFragmentWriter;
import org.cxio.aspects.writers.NodesFragmentWriter;
import org.cxio.aspects.writers.SubNetworkFragmentWriter;
import org.cxio.aspects.writers.VisualPropertiesFragmentWriter;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cxio.metadata.MetaDataCollection;
import org.cxio.metadata.MetaDataElement;
import org.cxio.misc.AspectElementCounts;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.cx.Aspect;
import org.cytoscape.io.cx.CXInfoManager;
import org.cytoscape.io.internal.cx_writer.VisualPropertiesGatherer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.Provenance;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.ProvenanceEvent;
import org.ndexbio.model.object.SimplePropertyValuePair;

import com.fasterxml.jackson.core.JsonProcessingException;

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

	// private final static boolean DEFAULT_USE_DEFAULT_PRETTY_PRINTING = true;

	
	// private boolean _use_default_pretty_printing;
	private VisualMappingManager _visual_mapping_manager;
	private CyNetworkViewManager _networkview_manager;
	private CyGroupManager _group_manager;
	private CyApplicationManager _application_manager;
	// private long _next_suid;

	public final static String[] cySupportedAspectNames = { NodesElement.ASPECT_NAME, EdgesElement.ASPECT_NAME,
			CartesianLayoutElement.ASPECT_NAME, EdgeAttributesElement.ASPECT_NAME, NodeAttributesElement.ASPECT_NAME,
			NetworkAttributesElement.ASPECT_NAME, SubNetworkElement.ASPECT_NAME, CyVisualPropertiesElement.ASPECT_NAME,
			NetworkRelationsElement.ASPECT_NAME, CyGroupsElement.ASPECT_NAME, CyViewsElement.ASPECT_NAME,
			HiddenAttributesElement.ASPECT_NAME, CyTableColumnElement.ASPECT_NAME };

	private final static Set<String> ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES = new HashSet<>();
	private final static Set<String> ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES = new HashSet<>();
	private final static Set<String> ADDITIONAL_IGNORE_FOR_NETWORK_ATTRIBUTES = new HashSet<>();

	static {
		ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES.add(CxUtil.SHARED_INTERACTION);
		ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES.add("name");

		ADDITIONAL_IGNORE_FOR_NETWORK_ATTRIBUTES.add(CxUtil.SHARED_NAME_COL);
		ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES.add(CxUtil.SHARED_NAME_COL);
		// ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES.add(CxUtil.NAME_COL);
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
		final Set<AspectFragmentWriter> writers = new HashSet<>();

		writers.add(CartesianLayoutFragmentWriter.createInstance());

		writers.add(EdgeAttributesFragmentWriter.createInstance());

		writers.add(EdgesFragmentWriter.createInstance());
		writers.add(NetworkAttributesFragmentWriter.createInstance());

		writers.add(NodeAttributesFragmentWriter.createInstance());

		writers.add(HiddenAttributesFragmentWriter.createInstance());

		writers.add(NodesFragmentWriter.createInstance());

		writers.add(VisualPropertiesFragmentWriter.createInstance());

		writers.add(SubNetworkFragmentWriter.createInstance());

		writers.add(NetworkRelationsFragmentWriter.createInstance());

		writers.add(CyGroupsFragmentWriter.createInstance());

		writers.add(CyViewsFragmentWriter.createInstance());

		writers.add(CyTableColumnFragmentWriter.createInstance());

		return writers;
	}

	
	public void setGroupManager(final CyGroupManager group_manager) {
	    _group_manager = group_manager; 
	}
	 
	
	public void setApplicationManager(final CyApplicationManager application_manager) {
		_application_manager = application_manager;
	}
	
	private VisualLexicon getLexicon(CyNetworkView view) {
		NetworkViewRenderer renderer = _application_manager.getNetworkViewRenderer(view.getRendererId()); 
		 
	    RenderingEngineFactory<CyNetwork> factory = renderer == null ? null 
	 
	        : renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT); 
	 
	    VisualLexicon lexicon = factory == null ? null : factory.getVisualLexicon(); 
	    return lexicon;
	}

	public void setNetworkViewManager(final CyNetworkViewManager networkview_manager) {
		_networkview_manager = networkview_manager;

	}

	/*
	 * public final void setNextSuid(final long next_suid) { _next_suid = next_suid;
	 * }
	 */

	/*
	 * public void setUseDefaultPrettyPrinting(final boolean
	 * use_default_pretty_printing) { _use_default_pretty_printing =
	 * use_default_pretty_printing; }
	 */

	public void setVisualMappingManager(final VisualMappingManager visual_mapping_manager) {
		_visual_mapping_manager = visual_mapping_manager;
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
	 * @param filters
	 *            the set of filters controlling which node and edge table columns
	 *            to include or exclude
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

	/**
	 * This is a method for serializing a Cytoscape network and associated table
	 * data as CX formatted OutputStream. <br>
	 * Method arguments control which aspects to serialize.
	 *
	 *
	 * @param network
	 *            the CyNetwork, and by association, tables to be serialized
	 * @param aspects
	 *            the set of aspects to serialize
	 * @param out
	 *            the stream to write to
	 * @throws IOException
	 *
	 *
	 * @see AspectSet
	 * @see Aspect
	 *
	 */

	public final boolean writeNetwork(final CyNetwork network, final boolean write_siblings, final AspectSet aspects,
			final OutputStream out) throws IOException {

		if (!aspects.contains(Aspect.SUBNETWORKS)) {
			if (aspects.contains(Aspect.VISUAL_PROPERTIES)) {
				throw new IllegalArgumentException("need to write sub-networks in order to write visual properties");
			}
			if (aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
				throw new IllegalArgumentException("need to write sub-networks in order to write cartesian layout");
			}
		}

		boolean hasNDExData = false;
		if (!write_siblings) {
			CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
			if (table.getColumn(CxUtil.UUID_COLUMN) != null) {
				hasNDExData = true;
			}
		}

		final CxWriter w = CxWriter.createInstance(out, false);// _use_default_pretty_printing);

		for (final AspectFragmentWriter writer : getCySupportedAspectFragmentWriters()) {
			w.addAspectFragmentWriter(writer);
		}

		w.addAspectFragmentWriter(new GeneralAspectFragmentWriter(Provenance.ASPECT_NAME));
		w.addAspectFragmentWriter(new GeneralAspectFragmentWriter(NamespacesElement.ASPECT_NAME));

		Set<Long> groupNodeIds = this.getGroupNodeIds(network, write_siblings);

		MetaDataCollection pre_meta_data = addPreMetadata(network, write_siblings, w, 1L, hasNDExData, !groupNodeIds.isEmpty());

		w.start();

		String msg = null;
		boolean success = true;

		// write namespaces out first
		if (hasNDExData && CXInfoManager.getNamespaces(network) != null && CXInfoManager.getNamespaces(network).size() > 0) {
			final List<AspectElement> nsAspect = new ArrayList<>(1);
			nsAspect.add(CXInfoManager.getNamespaces(network));

			w.writeAspectElements(nsAspect);
		}

		Provenance cytoscapeProvenance = new Provenance();

		ProvenanceEntity oldProvenanceEntity = ((hasNDExData && CXInfoManager.getProvenance(network) != null)
				? CXInfoManager.getProvenance(network).getEntity()
				: null);

		ProvenanceEvent creationEvent = new ProvenanceEvent("Cytoscape Export",
				new Timestamp(Calendar.getInstance().getTimeInMillis()));
		if (oldProvenanceEntity != null)
			creationEvent.addInput(oldProvenanceEntity);
		cytoscapeProvenance.getEntity().setCreationEvent(creationEvent);
		List<SimplePropertyValuePair> provenanceProps = new ArrayList<>(5);

		CyRootNetwork rootNetwork = ((CySubNetwork) network).getRootNetwork();

		String uploadName = write_siblings
				? (((CySubNetwork) network).getRootNetwork()).getRow(rootNetwork).get(CyNetwork.NAME, String.class)
				: network.getRow(network).get(CyNetwork.NAME, String.class);

		provenanceProps.add(new SimplePropertyValuePair("dc:title", uploadName));
		creationEvent.setProperties(provenanceProps);

		final List<AspectElement> provAspect = new ArrayList<>(1);
		provAspect.add(cytoscapeProvenance);

		w.writeAspectElements(provAspect);

		try {
			writeNodes(network, write_siblings, w, groupNodeIds);

			writeGroups(network, w, write_siblings);
			writeNodeAttributes(network, write_siblings, w, CyNetwork.DEFAULT_ATTRS, groupNodeIds);

			writeEdges(network, write_siblings, w);

			if (write_siblings) {
				writeTableColumnLabels(network, write_siblings, w);

			}
			writeNetworkAttributes(network, write_siblings, w); // CyNetwork.DEFAULT_ATTRS

			writeHiddenAttributes(network, write_siblings, w, CyNetwork.HIDDEN_ATTRS);

			writeEdgeAttributes(network, write_siblings, w, CyNetwork.DEFAULT_ATTRS);

			writeSubNetworks(network, write_siblings, w, aspects);

			if (write_siblings) {
				writeNetworkViews(network, w);
				writeNetworkRelations(network, w, true);
			}

			// write opaque aspects
			if (hasNDExData) {
				for (Map.Entry<String, Collection<AspectElement>> entry : CXInfoManager.getOpaqueAspectsTable(network)
						.entrySet()) {
					if (!iscySupportedAspect(entry.getKey())) {
						System.out.println("writing " + entry.getKey());
						w.writeOpaqueAspectFragment2(entry.getKey(), (Collection) entry.getValue());
					}
				}
			}

			final AspectElementCounts aspects_counts = w.getAspectElementCounts();
			MetaDataCollection post_meta_data = addPostMetadata(w, aspects_counts, write_siblings, network);
			
			for (MetaDataElement e : post_meta_data) {
				  Long cnt = e.getIdCounter();
				  if ( cnt !=null) {
					 pre_meta_data.setIdCounter(e.getName(),cnt);
				  }
				  cnt = e.getElementCount() ;
				  if ( cnt !=null) {
						 pre_meta_data.setElementCount(e.getName(),cnt);
				  }
			  }
			
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
			CXInfoManager.setMetadata(network, pre_meta_data);
			
		}

		return success;
	}

	private static boolean iscySupportedAspect(String aspectName) {
		for (String s : cySupportedAspectNames) {
			if (s.equals(aspectName))
				return true;
		}
		return false;
	}

	private final static void addDataToMetaDataCollection(final MetaDataCollection pre_meta_data,
			final String aspect_name, final Long consistency_group, final Long id_counter, final Long count) {
		MetaDataElement e = new MetaDataElement(aspect_name, "1.0");
		e.setElementCount(count);
		e.setConsistencyGroup(consistency_group);
		e.setIdCounter(id_counter);
		pre_meta_data.add(e);
	}

	private final static void addDataToPostMetaDataCollection(final MetaDataCollection pre_meta_data,
			final String aspect_name, final Long count, Long idCounter) {
		final MetaDataElement element = new MetaDataElement();
		element.setName(aspect_name);
		element.setElementCount(count);
		element.setIdCounter(idCounter);
		pre_meta_data.add(element);
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

	/*
	 * private final static String getRepresentsFromNodeTable(final CyNetwork
	 * network, final CyNode node) { final CyRow row =
	 * network.getTable(CyNode.class,
	 * CyNetwork.DEFAULT_ATTRS).getRow(node.getSUID()); if (row != null) { final
	 * Object o = row.getRaw(CxUtil.REPRESENTS); if ((o != null) && (o instanceof
	 * String)) { return String.valueOf(o); } } return null; }
	 * 
	 * private final static String getNameFromNodeTable(final CyNetwork network,
	 * final CyNode node) {
	 * 
	 * final CyRow row = network.getTable(CyNode.class,
	 * CyNetwork.DEFAULT_ATTRS).getRow(node.getSUID()); if (row != null) { final
	 * String o = row.get(CxUtil.NAME_COL, String.class); if (o != null) { return
	 * String.valueOf(o); } } return null; }
	 */

	/*
	 * private final static String getNameFromNodeTable(final CyNetwork network,
	 * final CyNode node) { String myNodeName =
	 * network.getRow(node).get(CyNetwork.NAME, String.class);
	 * System.out.println("  =====> name:" + myNodeName);
	 * 
	 * CyRow row2 = network.getTable(CyNode.class,
	 * CyNetwork.DEFAULT_ATTRS).getRow(node.getSUID()); if (row2 != null) {
	 * Map<String,Object> all = row2.getAllValues(); all.forEach((k,v) ->
	 * System.out.println( "--->   " + k + "=" + v.toString())); }
	 * 
	 * final CyRow row = network.getTable(CyNode.class,
	 * CyNetwork.LOCAL_ATTRS).getRow(node.getSUID()); if (row != null) {
	 * Map<String,Object> all = row.getAllValues(); all.forEach((k,v) ->
	 * System.out.println(k + "=" + v.toString()));
	 * 
	 * final Object o = row.getRaw(CxUtil.NAME_COL); if ((o != null) && (o
	 * instanceof String)) { return String.valueOf(o); } } return null; }
	 */

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

	private final static void writeCartesianLayout(final CyNetworkView view, final CxWriter w, boolean writeSiblings) throws IOException {
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
			Long nodeId = getNodeIdToExport(cy_node, network);
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

	private final void writeEdges(final CyNetwork network, final boolean write_siblings, final CxWriter w) throws IOException {
		final List<AspectElement> elements = new ArrayList<>(network.getEdgeCount());
		final CyRootNetwork my_root = ((CySubNetwork) network).getRootNetwork();
		if (write_siblings) {
			for (final CyEdge cy_edge : my_root.getEdgeList()) {
				elements.add(new EdgesElement(cy_edge.getSUID(), cy_edge.getSource().getSUID(),
						cy_edge.getTarget().getSUID(), getSharedInteractionFromEdgeTable(my_root, cy_edge)));
			}
		} else {
			for (final CyEdge cy_edge : ((CySubNetwork) network).getEdgeList()) {
				elements.add(new EdgesElement(getEdgeIdToExport(cy_edge, network),
						getNodeIdToExport(cy_edge.getSource(), network),
						getNodeIdToExport(cy_edge.getTarget(), network),
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

	private final void writeNetworkViews(final CyNetwork network, final CxWriter w) throws IOException {
		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnetworks = makeSubNetworkList(true, my_subnet, root, true);

		final List<AspectElement> elements = new ArrayList<>();

		for (final CySubNetwork subnetwork : subnetworks) {
			// PLEASE NOTE:
			// Cytoscape currently has only one view per sub-network.
			final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(subnetwork);
			for (final CyNetworkView view : views) {
				final CyViewsElement view_element = new CyViewsElement(view.getSUID(), subnetwork.getSUID());
				elements.add(view_element);
			}
		}
		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "views", elements.size());
		}

	}

	private final static void writeNodes(final CyNetwork network, final boolean write_siblings, final CxWriter w,
			Set<Long> grpNodes) throws IOException {
		// final CySubNetwork my_subnet = (CySubNetwork) network;
		// final CyRootNetwork my_root = my_subnet.getRootNetwork();

		final CyNetwork workingNet = write_siblings ? ((CySubNetwork) network).getRootNetwork() : network;
		final List<AspectElement> elements = new ArrayList<>(workingNet.getNodeCount());
		String attName = write_siblings ? CxUtil.SHARED_NAME_COL : CxUtil.NAME_COL;

		for (final CyNode cy_node : workingNet.getNodeList()) {

			Long cxId = write_siblings ? cy_node.getSUID() : getNodeIdToExport(cy_node, network);

			NodesElement elmt = grpNodes.contains(cy_node.getSUID()) ? new NodesElement(cxId.longValue(), null, null)
					: new NodesElement(cxId.longValue(), getNodeAttributeValue(network, cy_node, attName, String.class),
							getNodeAttributeValue(network, cy_node, CxUtil.REPRESENTS, String.class));

			elements.add(elmt);
		}

		/*
		 * if (write_siblings) { for (final CyNode cy_node : my_root.getNodeList()) {
		 * elements.add(new NodesElement(cy_node.getSUID().longValue(),
		 * getNodeAttributeValue(my_root,cy_node,CxUtil.SHARED_NAME_COL, String.class),
		 * getNodeAttributeValue(my_root, cy_node, CxUtil.REPRESENTS, String.class))); }
		 * } else { for (final CyNode cy_node : my_subnet.getNodeList()) {
		 * elements.add(new NodesElement(getNodeIdToExport(cy_node, cxInfoHolder),
		 * getNodeAttributeValue(my_subnet,cy_node,CxUtil.NAME_COL, String.class),
		 * getNodeAttributeValue(my_subnet, cy_node, CxUtil.REPRESENTS, String.class)
		 * )); }
		 * 
		 * }
		 */
		w.writeAspectElements(elements);

	}

	public static Long getNodeIdToExport(CyNode cyNode, CyNetwork network) {
		Long id = cyNode.getSUID();
		MetaDataCollection metadata = CXInfoManager.getMetadata(network);
		if (metadata == null) {
			return id;
		}
		Long counter = metadata.getIdCounter(NodesElement.ASPECT_NAME);
		if (counter != null) {
			Long cxNodeId = CXInfoManager.getCXNodeId(network, id);
			if (cxNodeId != null) { // this is a node in the original cx network
				return cxNodeId;
			}
			// new node in cytoscape
			Long newid = Long.valueOf(counter.longValue() + 1);
			CXInfoManager.addNodeMapping(network, id, newid);
			metadata.setIdCounter(NodesElement.ASPECT_NAME, newid);
			try {
				CXInfoManager.setMetadata(network, metadata);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return newid;

		}
		return id;
	}

	public static long getEdgeIdToExport(CyEdge cyedge, CyNetwork network) {
		long id = cyedge.getSUID().longValue();
		MetaDataCollection metadata = CXInfoManager.getMetadata(network);
		if (metadata == null) {
			return id;
		}
		Long counter = metadata.getIdCounter(EdgesElement.ASPECT_NAME);
		if (counter != null) {
			Long cxEdgeId = CXInfoManager.getCXEdgeId(network, Long.valueOf(id));
			if (cxEdgeId != null) { // this is a node in the original cx network
				return cxEdgeId.longValue();
			}
			// new edge in cytoscape
			long newid = counter.longValue() + 1;
			CXInfoManager.addEdgeMapping(network, Long.valueOf(id), Long.valueOf(newid));
			metadata.setIdCounter(EdgesElement.ASPECT_NAME, Long.valueOf(newid));
			try {
				CXInfoManager.setMetadata(network, metadata);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return newid;

		}
		return id;
	}

	private final static void writeVisualProperties(final CyNetworkView view,
			final VisualMappingManager visual_mapping_manager, final VisualLexicon lexicon, final CxWriter w,
			boolean writeSiblings) throws IOException {
		final Set<VisualPropertyType> types = new HashSet<>();
		types.add(VisualPropertyType.NETWORK);
		types.add(VisualPropertyType.NODES);
		types.add(VisualPropertyType.EDGES);
		types.add(VisualPropertyType.NODES_DEFAULT);
		types.add(VisualPropertyType.EDGES_DEFAULT);

		final List<AspectElement> elements = VisualPropertiesGatherer.gatherVisualPropertiesAsAspectElements(view,
				visual_mapping_manager, lexicon, types, writeSiblings);

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "visual properties", elements.size());
		}
	}

	private final static MetaDataCollection addPostMetadata(final CxWriter w, final AspectElementCounts aspects_counts,
			boolean write_siblings, CyNetwork network) {

		final long t0 = System.currentTimeMillis();
		final MetaDataCollection post_meta_data = new MetaDataCollection();

		if (write_siblings) {
			addDataToPostMetaDataCollection(post_meta_data, CyTableColumnElement.ASPECT_NAME,
					(long) aspects_counts.getAspectElementCount(CyTableColumnElement.ASPECT_NAME), null);
			addDataToPostMetaDataCollection(post_meta_data, SubNetworkElement.ASPECT_NAME,
					(long) aspects_counts.getAspectElementCount(SubNetworkElement.ASPECT_NAME), null);
			addDataToPostMetaDataCollection(post_meta_data, CyViewsElement.ASPECT_NAME,
					(long) aspects_counts.getAspectElementCount(CyViewsElement.ASPECT_NAME), null);
			addDataToPostMetaDataCollection(post_meta_data, CyGroupsElement.ASPECT_NAME,
					(long) aspects_counts.getAspectElementCount(CyGroupsElement.ASPECT_NAME), null);
			addDataToPostMetaDataCollection(post_meta_data, NetworkRelationsElement.ASPECT_NAME,
					(long) aspects_counts.getAspectElementCount(NetworkRelationsElement.ASPECT_NAME), null);
		}

		addDataToPostMetaDataCollection(post_meta_data, NetworkAttributesElement.ASPECT_NAME,
				(long) aspects_counts.getAspectElementCount(NetworkAttributesElement.ASPECT_NAME), null);

		addDataToPostMetaDataCollection(post_meta_data, HiddenAttributesElement.ASPECT_NAME,
				(long) aspects_counts.getAspectElementCount(HiddenAttributesElement.ASPECT_NAME), null);

		addDataToPostMetaDataCollection(post_meta_data, NodeAttributesElement.ASPECT_NAME,
				(long) aspects_counts.getAspectElementCount(NodeAttributesElement.ASPECT_NAME), null);

		addDataToPostMetaDataCollection(post_meta_data, EdgeAttributesElement.ASPECT_NAME,
				(long) aspects_counts.getAspectElementCount(EdgeAttributesElement.ASPECT_NAME), null);

		addDataToPostMetaDataCollection(post_meta_data, CyVisualPropertiesElement.ASPECT_NAME,
				(long) aspects_counts.getAspectElementCount(CyVisualPropertiesElement.ASPECT_NAME), null);

		if (CXInfoManager.hasUUID(network)) {
			addDataToPostMetaDataCollection(post_meta_data, NodesElement.ASPECT_NAME, null,
					CXInfoManager.getMetadata(network).getIdCounter(NodesElement.ASPECT_NAME));
			addDataToPostMetaDataCollection(post_meta_data, EdgesElement.ASPECT_NAME, null,
					CXInfoManager.getMetadata(network).getIdCounter(EdgesElement.ASPECT_NAME));

		}

		w.addPostMetaData(post_meta_data);

		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "post meta-data", -1);
		}
		return post_meta_data;
	}

	private final static MetaDataCollection addPreMetadata(final CyNetwork network, final boolean write_siblings, final CxWriter w,
			final Long consistency_group, boolean hasNDExData, boolean hasGroup) {

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final long t0 = System.currentTimeMillis();
		final MetaDataCollection pre_meta_data = new MetaDataCollection();

		CyNetwork my_network;
		if (write_siblings) {
			my_network = my_subnet.getRootNetwork();
		} else {
			my_network = my_subnet;
		}

		addDataToMetaDataCollection(pre_meta_data, Provenance.ASPECT_NAME, consistency_group, null, 1L);

		addDataToMetaDataCollection(pre_meta_data, NodesElement.ASPECT_NAME, consistency_group,
				((write_siblings || !hasNDExData) ? SUIDFactory.getNextSUID() : null),
				(long) my_network.getNodeList().size());

		if (my_network.getEdgeList().size() > 0) {
			addDataToMetaDataCollection(pre_meta_data, EdgesElement.ASPECT_NAME, consistency_group,
					((write_siblings || !hasNDExData) ? SUIDFactory.getNextSUID() : null),
					Long.valueOf(my_network.getEdgeList().size()));
		}

		if (write_siblings) {
			addDataToMetaDataCollection(pre_meta_data, CyTableColumnElement.ASPECT_NAME, consistency_group, null, null);
			addDataToMetaDataCollection(pre_meta_data, SubNetworkElement.ASPECT_NAME, consistency_group, null, null);

			addDataToMetaDataCollection(pre_meta_data, CyViewsElement.ASPECT_NAME, consistency_group, null, null);

			addDataToMetaDataCollection(pre_meta_data, NetworkRelationsElement.ASPECT_NAME, consistency_group, null,
					null);
		}

		if (hasGroup)
			addDataToMetaDataCollection(pre_meta_data, CyGroupsElement.ASPECT_NAME, consistency_group, null, null);

		addDataToMetaDataCollection(pre_meta_data, NetworkAttributesElement.ASPECT_NAME, consistency_group, null, null);

		addDataToMetaDataCollection(pre_meta_data, NodeAttributesElement.ASPECT_NAME, consistency_group, null, null);

		addDataToMetaDataCollection(pre_meta_data, EdgeAttributesElement.ASPECT_NAME, consistency_group, null, null);

		addDataToMetaDataCollection(pre_meta_data, CartesianLayoutElement.ASPECT_NAME, consistency_group, null,
				(long) my_network.getNodeList().size());

		addDataToMetaDataCollection(pre_meta_data, CyVisualPropertiesElement.ASPECT_NAME, consistency_group, null,
				null);

		addDataToMetaDataCollection(pre_meta_data, HiddenAttributesElement.ASPECT_NAME, consistency_group, null, null);

		if (hasNDExData) {
			MetaDataCollection originalCXMetadata = CXInfoManager.getMetadata(network);

			for (MetaDataElement mdElement : originalCXMetadata) {
				if (pre_meta_data.getMetaDataElement(mdElement.getName()) == null) { // not a cy supported aspect, then
																						// add it
					pre_meta_data.add(mdElement);
				}
			}
		}

		w.addPreMetaData(pre_meta_data);

		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "pre meta-data", -1);
		}
		return pre_meta_data;

	}

	private final static void writeEdgeAttributes(final CyNetwork network, final boolean write_siblings,
			final CxWriter w, final String namespace) throws IOException {

		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			writeEdgeAttributesHelper(namespace, subnet, subnet.getEdgeList(), elements, write_siblings);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "edge attributes", elements.size());
		}
	}

	private final static boolean isIgnore(final String column_name, final Set<String> additional_to_ignore,
			final Settings setttings) {
		if (setttings.isIgnoreSuidColumn() && column_name.equals(CxUtil.SUID)) {
			return true;
		} else if (setttings.isIgnoreSelectedColumn() && column_name.equals(CxUtil.SELECTED)) {
			return true;
		} else if ((additional_to_ignore != null) && additional_to_ignore.contains(column_name)) {
			return true;
		} else if (column_name.equals(CxUtil.UUID_COLUMN) || column_name.equals(CxUtil.CX_ID_COLUMN)
				|| column_name.equals(CxUtil.METADATA_COLUMN)
				|| column_name.equals(CxUtil.NAMESPACES_COLUMN)
				|| column_name.equals(CxUtil.PROVENANCE_COLUMN)
				|| column_name.startsWith(CxUtil.OPAQUE_ASPECTS_COLUMN_PREFIX)) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private static void writeEdgeAttributesHelper(final String namespace, final CyNetwork my_network,
			final List<CyEdge> edges, final List<AspectElement> elements, boolean writeSiblings) {

		for (final CyEdge cy_edge : edges) {
			final CyRow row = my_network.getRow(cy_edge, namespace);
			if (row != null) {

				final Map<String, Object> values = row.getAllValues();
				if ((values != null) && !values.isEmpty()) {
					for (String column_name : values.keySet()) {
						if (isIgnore(column_name, ADDITIONAL_IGNORE_FOR_EDGE_ATTRIBUTES, Settings.INSTANCE)) {
							continue;
						}
						final Object value = values.get(column_name);
						if (value == null || (value instanceof String && ((String) value).length() == 0)) {
							continue;
						}
						EdgeAttributesElement e = null;
						final Long subnet = writeSiblings ? my_network.getSUID() : null;
						if (column_name.equals(CxUtil.SHARED_NAME_COL))
							column_name = "name";
						if (value instanceof List) {
							final List<String> attr_values = new ArrayList<>();
							for (final Object v : (List) value) {
								attr_values.add(String.valueOf(v));
							}
							if (!attr_values.isEmpty()) {
								e = new EdgeAttributesElement(subnet, getEdgeIdToExport(cy_edge, my_network),
										column_name, attr_values, AttributesAspectUtils.determineDataType(value));
							}
						} else {
							e = new EdgeAttributesElement(subnet, getEdgeIdToExport(cy_edge, my_network), column_name,
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

	private final void writeGroups(final CyNetwork network, final CxWriter w, boolean writeSiblings) throws IOException {
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
						getNodeIdToExport(group.getGroupNode(), network), writeSiblings ? subnet.getSUID() : null,
						name);
				for (final CyEdge e : group.getExternalEdgeList()) {
					group_element.addExternalEdge(Long.valueOf(getEdgeIdToExport(e, network)));
				}
				for (final CyEdge e : group.getInternalEdgeList()) {
					group_element.addInternalEdge(Long.valueOf(getEdgeIdToExport(e, network)));
				}
				for (final CyNode n : group.getNodeList()) {
					group_element.addNode(getNodeIdToExport(n, network));
				}
				boolean isCollapsed = group.isCollapsed(subnet);
				group_element.set_isCollapsed(isCollapsed);
				elements.add(group_element);
			}

		}
		// final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		/*
		 * if (Settings.INSTANCE.isTiming()) { TimingUtil.reportTimeDifference(t0,
		 * "groups", elements.size()); }
		 */

	}

	private final static void writeHiddenAttributes(final CyNetwork network, final boolean write_siblings,
			final CxWriter w, final String namespace) throws IOException {

		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			writeHiddenAttributesHelper(namespace, subnet, elements, write_siblings);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "network attributes", elements.size());
		}
	}

	@SuppressWarnings("rawtypes")
	private static void writeHiddenAttributesHelper(final String namespace, final CyNetwork my_network,
			final List<AspectElement> elements, boolean writeSiblings) {

		final CyRow row = my_network.getRow(my_network, namespace);
		if (row != null) {
			final Map<String, Object> values = row.getAllValues();

			if ((values != null) && !values.isEmpty()) {
				for (final String column_name : values.keySet()) {
					if (isIgnore(column_name, null, Settings.INSTANCE)) {
						continue;
					}
					final Object value = values.get(column_name);
					if (value == null) {
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
						elements.add(e);
					}
				}
			}

		}
	}

	private final static void writeNetworkAttributes(final CyNetwork network, final boolean write_siblings,
			final CxWriter w) throws IOException {

		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();

		final String collection_name = obtainNetworkCollectionName(my_root);
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);
		if (Settings.INSTANCE.isDebug()) {
			System.out.println("collection name: " + collection_name);
		}

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
			final List<AspectElement> elements, boolean writeSiblings) {

		final CyRow row = my_network.getRow(my_network, namespace);

		if (row != null) {
			final Map<String, Object> values = row.getAllValues();

			if ((values != null) && !values.isEmpty()) {
				for (final String column_name : values.keySet()) {
					if (isIgnore(column_name, ADDITIONAL_IGNORE_FOR_NETWORK_ATTRIBUTES, Settings.INSTANCE)) {
						continue;
					}
					final Object value = values.get(column_name);
					if (value == null) {
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

	private final static void writeNodeAttributes(final CyNetwork network, final boolean write_siblings,
			final CxWriter w, final String namespace, Set<Long> groupNodeIds)
			throws IOException {

		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			writeNodeAttributesHelper(namespace, subnet, subnet.getNodeList(), elements, write_siblings,
					groupNodeIds);
		}

		final long t0 = System.currentTimeMillis();
		w.writeAspectElements(elements);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "node attributes", elements.size());
		}
	}

	private final static void writeTableColumnLabels(final CyNetwork network, final boolean write_siblings,
			final CxWriter w) throws IOException {

		final List<AspectElement> elements = new ArrayList<>();

		final CySubNetwork my_subnet = (CySubNetwork) network;
		final CyRootNetwork my_root = my_subnet.getRootNetwork();
		final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);

		for (final CySubNetwork subnet : subnets) {
			Collection<CyColumn> c = subnet.getDefaultNodeTable().getColumns();
			Long subNetId = write_siblings ? subnet.getSUID() : null;
			for (CyColumn col : c) {
				if (!col.getName().equals(CxUtil.SUID)) {
					ATTRIBUTE_DATA_TYPE type = ATTRIBUTE_DATA_TYPE.STRING;
					if (col.getType() != List.class) {
						type = toAttributeType(col.getType());
					} else {
						type = toListAttributeType(col.getListElementType());
					}

					CyTableColumnElement x = new CyTableColumnElement(subNetId, "node_table", col.getName(), type);
					elements.add(x);
				}
			}

			c = subnet.getDefaultEdgeTable().getColumns();
			for (CyColumn col : c) {
				if (!col.getName().equals(CxUtil.SUID)) {
					ATTRIBUTE_DATA_TYPE type = ATTRIBUTE_DATA_TYPE.STRING;
					if (col.getType() != List.class) {
						type = toAttributeType(col.getType());
					} else {
						type = toListAttributeType(col.getListElementType());
					}

					CyTableColumnElement x = new CyTableColumnElement(subnet.getSUID(), "edge_table", col.getName(),
							type);
					elements.add(x);
				}
			}

			c = subnet.getDefaultNetworkTable().getColumns();
			for (CyColumn col : c) {
				if (!col.getName().equals(CxUtil.SUID)) {
					ATTRIBUTE_DATA_TYPE type = ATTRIBUTE_DATA_TYPE.STRING;
					if (col.getType() != List.class) {
						type = toAttributeType(col.getType());
					} else {
						type = toListAttributeType(col.getListElementType());
					}
					CyTableColumnElement x = new CyTableColumnElement(subnet.getSUID(), "network_table", col.getName(),
							type);
					elements.add(x);
				}
			}
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

	@SuppressWarnings("rawtypes")
	private static void writeNodeAttributesHelper(final String namespace, final CySubNetwork my_network,
			final List<CyNode> nodes, final List<AspectElement> elements, boolean writeSiblings, Set<Long> grpNodeIds) {
		for (final CyNode cy_node : nodes) {
			if (grpNodeIds.contains(cy_node.getSUID()))
				continue;
			final CyRow row = my_network.getRow(cy_node, namespace);
			if (row != null) {
				final Map<String, Object> values = row.getAllValues();

				if ((values != null) && !values.isEmpty()) {
					for (final String column_name : values.keySet()) {
						if (isIgnore(column_name, ADDITIONAL_IGNORE_FOR_NODE_ATTRIBUTES, Settings.INSTANCE)) {
							continue;
						}
						if (writeSiblings == false && column_name.equals(CxUtil.NAME_COL)) {
							continue;
						}
						final Object value = values.get(column_name);
						if (value == null) {
							continue;
						}
						NodeAttributesElement e = null;
						final Long subnet = writeSiblings ? my_network.getSUID() : null;
						Long nodeId = getNodeIdToExport(cy_node, my_network);

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

	private final void writeSubNetworks(final CyNetwork network, final boolean write_siblings, final CxWriter w,
			final AspectSet aspects) throws IOException {

		// write the subNetwork only when exporting the collection
//		if (write_siblings) {
			final CySubNetwork my_subnet = (CySubNetwork) network;
			final CyRootNetwork my_root = my_subnet.getRootNetwork();
			final List<CySubNetwork> subnets = makeSubNetworkList(write_siblings, my_subnet, my_root, true);
						
			// write the visual properties and coordinates
			for (final CySubNetwork subnet : subnets) {
				final Collection<CyNetworkView> views = _networkview_manager.getNetworkViews(subnet);
				for (final CyNetworkView view : views) {
					final VisualLexicon _lexicon = getLexicon(view);
					writeCartesianLayout(view, w, write_siblings);
					writeVisualProperties(view, _visual_mapping_manager, _lexicon, w, write_siblings);
					
				}
			}

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
			// final long t0 = System.currentTimeMillis();
			w.writeAspectElements(elements);
			/*
			 * if (Settings.INSTANCE.isTiming()) { TimingUtil.reportTimeDifference(t0,
			 * "subnetworks", elements.size()); }
			 */
//		} else {
//			_networkview_manager.getNetworkViews(network);
//			CyNetworkView view = CyObjectManager.INSTANCE.getCurrentNetworkView();
//			if (view != null) {
//				writeCartesianLayout(view, w, write_siblings);
//				writeVisualProperties(view, _visual_mapping_manager, _lexicon, w, write_siblings);
//			}
//		}
	}

}
