package org.cytoscape.io.cx.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.LockedVisualPropertiesManager;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReader;
import org.cytoscape.io.internal.cx_reader.StringParser;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriter;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.nicecy.NiceCyRootNetwork;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.internal.CyNetworkViewManagerImpl;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.ndexbio.cxio.aspects.datamodels.AbstractAttributesAspectElement;
import org.ndexbio.cxio.aspects.datamodels.AbstractElementAttributesAspectElement;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.HiddenAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.Mapping;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.misc.OpaqueElement;
import org.ndexbio.cxio.util.JsonWriter;
import org.ndexbio.model.cx.CitationElement;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.Provenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("deprecation")
public class TestUtil {
	private static Logger logger = LoggerFactory.getLogger("TestUtil");
	// Some aspects change or are unnecessary on round trip
	private String[] VALID_INCONSISTENT_VIS_PROPS = new String[] {
			BasicVisualLexicon.NODE_LABEL_FONT_FACE.getIdString(),
			BasicVisualLexicon.EDGE_LABEL_FONT_FACE.getIdString(),
			BasicVisualLexicon.NETWORK_TITLE.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_1.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_2.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_3.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_4.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_5.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_6.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_7.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_8.getIdString(),
			DVisualLexicon.NODE_CUSTOMGRAPHICS_9.getIdString(),
	};
	private String[] VALID_REMOVED_VIS_PROPS = new String[] {
			BasicVisualLexicon.NODE_Z_LOCATION.getIdString(),
	};
		
	private NetworkTestSupport nts = new NetworkTestSupport();
	private CyNetworkFactory network_factory = nts.getNetworkFactory();
	private NetworkViewTestSupport nvts = new NetworkViewTestSupport();
	private CyNetworkViewFactory networkview_factory = nvts.getNetworkViewFactory();
	
	private CyGroupManagerImpl group_manager;

	final SynchronousTaskManager<?> synchronousTaskManager = mock(SynchronousTaskManager.class);
	
	public static TestUtil INSTANCE;
	public static void init() {
		INSTANCE = new TestUtil();
	}
	
	public static class CxReaderWrapper extends CytoscapeCxNetworkReader {

		public CxReaderWrapper(InputStream input_stream, String network_collection_name,
				CyNetworkViewFactory networkview_factory, CyNetworkFactory network_factory,
				CyNetworkManager network_manager, CyRootNetworkManager root_network_manager) {
			super(input_stream, network_collection_name, networkview_factory, network_factory, network_manager,
					root_network_manager);
		}
		
		public NiceCXNetwork getNiceCX(){
			return niceCX;
		}
		
	}
	
	private void initGroups() {
		final DummyCyEventHelper eventHelper = new DummyCyEventHelper();
		final VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		final CyNetworkViewManager netViewMgr = new CyNetworkViewManagerImpl(serviceRegistrar);
		
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(nts.getNetworkManager());
		
		
		group_manager = new CyGroupManagerImpl(serviceRegistrar);
		final LockedVisualPropertiesManager lvpMgr = new LockedVisualPropertiesManager(serviceRegistrar);
		CyGroupFactory group_factory = new CyGroupFactoryImpl(group_manager, lvpMgr);
		
		CyServiceModule.setService(CyNetworkViewManager.class, netViewMgr);
		CyServiceModule.setService(CyGroupManager.class, group_manager);
		CyServiceModule.setService(CyGroupFactory.class, group_factory);
	}
	
	public TestUtil() {
		CyServiceRegistrar reg = mock(CyServiceRegistrar.class);
		CyServiceModule.setServiceRegistrar(reg);
		
		CyServiceModule.setService(CySessionManager.class, mock(CySessionManager.class));
		CyServiceModule.setService(CyNetworkViewFactory.class, networkview_factory);
		CyServiceModule.setService(CyNetworkFactory.class, network_factory);
		CyServiceModule.setService(CyNetworkManager.class, nts.getNetworkManager());
		CyServiceModule.setService(CyRootNetworkManager.class, nts.getRootNetworkFactory());
		
		CyServiceModule.setService(SynchronousTaskManager.class, mock(SynchronousTaskManager.class));
		CyServiceModule.setService(DialogTaskManager.class, mock(DialogTaskManager.class));
		
		VisualMappingMock.init();

		initGroups();
	}
	
	public static File getResource(String... dir) {
		File file = new File("src/test/resources/");
		for (String s : dir) {
			file = new File(file, s);
		}
		return file;
	}

	public CxReaderWrapper getReader(InputStream in, String collection_name) {
		CxReaderWrapper reader = new CxReaderWrapper(in, 
				collection_name, 
				networkview_factory, 
				network_factory, 
				nts.getNetworkManager(),
				nts.getRootNetworkFactory());
		return reader;
	}
	
	public static CyNetwork[] loadNetworks(CxReaderWrapper reader) throws IOException {

		reader.run(null);
		
		CyNetworkManager network_manager = CyServiceModule.getService(CyNetworkManager.class);
		for (CyNetwork net : reader.getNetworks()) {
			network_manager.addNetwork(net);
			reader.buildCyNetworkView(net);
		}
		
		return reader.getNetworks();
	}
	
	private static ByteArrayOutputStream saveNetwork(CyNetwork network, boolean collection, boolean useCxId) throws IOException {
		if (collection && useCxId) {
			logger.info("Not using cxId for collection");
			useCxId = false;
		}
		
		String name = CxUtil.getNetworkName(network);
		logger.info("Exporting " + name + " as " + (collection ? "collection" : "subnetwork"));
		File output = getResource("output");
		if (!output.exists()) {
			output.mkdir();
		}
		
		ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
		TestUtil.doExport(network, collection, useCxId, out_stream);
		
		return out_stream;
	}
	
	public static InputStream pipe(ByteArrayOutputStream os) throws IOException {
		return new ByteArrayInputStream(os.toByteArray());
	}
	
	public static File saveOutputStreamToFile(ByteArrayOutputStream out, String name) throws IOException {
		File f = getResource("output", name);
		FileOutputStream fos = new FileOutputStream(f);
		out.writeTo(fos);
		logger.info("Wrote to:" + f.getAbsolutePath());
		return f;
	}

	public static void doExport(CyNetwork network, boolean writeSiblings, boolean useCxId,
			OutputStream out) {
		
		StreamUtil streamUtil = CyServiceModule.getService(StreamUtil.class);
		CytoscapeCxFileFilter filter = new CytoscapeCxFileFilter(streamUtil);
		CxNetworkWriterFactory writerFactory = new CxNetworkWriterFactory(filter);
		CxNetworkWriter writer = (CxNetworkWriter) writerFactory.createWriter(out, network);
		writer.useCxId = useCxId;
		writer.writeSiblings = writeSiblings;
		try {
			writer.run(null);
		} catch (FileNotFoundException e1) {
			fail("Failed to export network to CX: " + e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			fail("Failed to export network to CX: " + e1.getMessage());
			e1.printStackTrace();
		}

	}
	
	// Test helper to use base subnetwork with optional node list (on top of id:0 starter node)
	public static CxReaderWrapper getSubNetwork(File file, NodesElement... nodes)  {
		FileInputStream in;
		try {
			in = new FileInputStream(file);
		}catch(FileNotFoundException e) {
			throw new IllegalArgumentException("Unable to find resource subnetwork " + file.getPath());
		}
		logger.info("\n--------------------\nCreating reader for " + file.getName());
		CxReaderWrapper reader = INSTANCE.getReader(in, null);
		NiceCXNetwork niceCX = reader.getNiceCX();
		
		for (NodesElement node : nodes) {
			niceCX.addNode(node);
		}
		return reader;
	}
	
	/* Main test wrapper, check that all input aspects persist */
	public static void withAspects(CxReaderWrapper reader, AspectElement... aspects) throws IOException {
		
		NiceCXNetwork niceCX = reader.getNiceCX();
		
		Map<Long, Long> idMap = INSTANCE.getCxIdMapping(niceCX.getOpaqueAspectTable());
		INSTANCE.applyCxMapping(niceCX, idMap);
		
		if (aspects.length == 0) {
			aspects = INSTANCE.getAllAspects(niceCX);
		}else {
			for (AspectElement el : aspects) {
				INSTANCE.addAspect(niceCX, el);
			}
		}
		NiceCXNetwork output = getOutput(reader);
		
		String aspectName = null;
		for (AspectElement el : aspects) {
			if (el == null) {
				continue;
			}
			if (!el.getAspectName().equals(aspectName)) {
				aspectName = el.getAspectName();
				logger.info("Checking " + aspectName);
			}
			
			assertTrue("Missing " + el.getAspectName() + "(" + el.getClass() + "): " + el, INSTANCE.containsAspect(output, el));
		}
	}
	
	public void addAspect(NiceCXNetwork niceCX, AspectElement aspect) {
		switch (aspect.getAspectName()) {
		case NetworkAttributesElement.ASPECT_NAME:
			niceCX.addNetworkAttribute((NetworkAttributesElement) aspect);
			break;
		case HiddenAttributesElement.ASPECT_NAME:
			niceCX.addOpaqueAspect(aspect);
			break;
		case NodesElement.ASPECT_NAME:
			niceCX.addNode((NodesElement) aspect);
			break;
		case EdgesElement.ASPECT_NAME:
			niceCX.addEdge((EdgesElement) aspect);
			break;
		case NodeAttributesElement.ASPECT_NAME:
			niceCX.addNodeAttribute((NodeAttributesElement) aspect);
			break;
		case EdgeAttributesElement.ASPECT_NAME:
			niceCX.addEdgeAttribute((EdgeAttributesElement) aspect);
			break;
		case CitationElement.ASPECT_NAME:
			niceCX.addCitation((CitationElement) aspect);
			break;
		case NamespacesElement.ASPECT_NAME:
			NamespacesElement ne = (NamespacesElement) aspect;
			for (Entry<String, String> entry : ne.entrySet()) {
				niceCX.addNameSpace(entry.getKey(), entry.getValue());
			}
			break;
		case Provenance.ASPECT_NAME:
			niceCX.setProvenance((Provenance) aspect);
			break;
		case CartesianLayoutElement.ASPECT_NAME:
			CartesianLayoutElement cle = (CartesianLayoutElement) aspect;
			long nodeId = cle.getNode();
			niceCX.addNodeAssociatedAspectElement(nodeId, cle);
			break;
		default:
			niceCX.addOpaqueAspect(aspect);
		}
	}
	
	private static Map<Long, Long> getNetworkViewMapping(NiceCXNetwork input, NiceCXNetwork output) {
		Map<Long, Long> map = new HashMap<Long, Long>();
		if (!CxUtil.isCollection(input)) {
			return map;
		} 
		
		Map<String, Collection<AspectElement>> in_table = input.getOpaqueAspectTable();
		Map<String, Collection<AspectElement>> out_table = output.getOpaqueAspectTable();
		if (in_table == null || out_table == null) {
			fail("Missing opaque table?");
		}

		Collection<AspectElement> in_nres = in_table.get(NetworkRelationsElement.ASPECT_NAME);
		Collection<AspectElement> out_nres = out_table.get(NetworkRelationsElement.ASPECT_NAME);
		
		if (in_nres == null) {
			fail("No network relations in input");
		}
		if (out_nres == null) {
			fail("No network relations in output");
		}
		
		Map<String, NetworkRelationsElement> in_map = new HashMap<String, NetworkRelationsElement>();
		in_nres.forEach(ae -> {
			NetworkRelationsElement nre = (NetworkRelationsElement) ae;
			if (in_map.containsKey(nre.getChildName())) {
				fail("Duplicate " + nre.getRelationship() + " names in input: " + nre.getChildName());
			}
			in_map.put(nre.getChildName(), nre);
		});
		Map<String, NetworkRelationsElement> out_map = new HashMap<String, NetworkRelationsElement>();
		out_nres.forEach(ae -> {
			NetworkRelationsElement nre = (NetworkRelationsElement) ae;
			if (out_map.containsKey(nre.getChildName())) {
				fail("Duplicate " + nre.getRelationship() + " names in output: " + nre.getChildName());
			}
			out_map.put(nre.getChildName(), nre);
		});

		for (String key : in_map.keySet()) {
			NetworkRelationsElement nre = in_map.get(key);
			Long idValue = nre.getChild();
			if (!out_map.containsKey(key)) {
				System.out.println(out_map);
				System.out.println(in_map);
				fail("No name mapping for " + nre.getRelationship() + " " + key);
			}
			Long idKey = out_map.get(key).getChild();
			map.put(idKey, idValue);
		}
		return map;
	}

	private AspectElement[] getAllAspects(NiceCXNetwork niceCX) {
		List<AspectElement> aspects = new ArrayList<AspectElement>();
		
		aspects.addAll(niceCX.getNetworkAttributes());
		aspects.addAll(niceCX.getEdges().values());
		aspects.addAll(niceCX.getNodes().values());
		for (Collection<NodeAttributesElement> collection : niceCX.getNodeAttributes().values()) {
			aspects.addAll(collection);
		}
		for (Collection<EdgeAttributesElement> collection : niceCX.getEdgeAttributes().values()) {
			aspects.addAll(collection);
		}
		niceCX.getNodeAssociatedAspects().values().forEach(aspect_collections -> {
			aspect_collections.values().forEach(asps -> {
				aspects.addAll(asps);
			});
		});
		
		niceCX.getEdgeAssociatedAspects().values().forEach(aspect_collections -> {
			aspect_collections.values().forEach(asps -> {
				aspects.addAll(asps);
			});
		});
		
		// Deprecated: This should always be empty
		if (niceCX.getNamespaces().isEmpty()) {
			aspects.add(niceCX.getNamespaces());
		}
		aspects.add(niceCX.getProvenance());
		aspects.addAll(niceCX.getCitations().values());
		
		for (Collection<AspectElement> op : niceCX.getOpaqueAspectTable().values()) {
			// Only include the first 50 opaque elements
			List<AspectElement> limited = op.stream().limit(50).collect(Collectors.toList());
			aspects.addAll(limited);
		}
		
		AspectElement[] arr = new AspectElement[aspects.size()];
		aspects.toArray(arr);
		return arr;
	}

	private <T extends AspectElement> boolean compareAspect(AspectElement in, AspectElement out) throws IOException {
		if (out == null && in != null) {
			System.out.println(in);
			return false;
		}
		
		try {
			String out_json =  getJson(out);
			String in_json = getJson(in);
			if (out_json.equals(in_json)) {
				return true;
			}
		}catch(NullPointerException e) {
			//Sometimes this fails with an NPE. Okay to not fail
		}
		
		// Check value as JSON comparison for attributes.
		if (out instanceof AbstractAttributesAspectElement) {
			AbstractAttributesAspectElement out_asp = (AbstractAttributesAspectElement) out;
			AbstractAttributesAspectElement in_asp = (AbstractAttributesAspectElement) out;
			
			if (out_asp.getName().equals(in_asp.getName())) {
				String out_json_value = out_asp.getValueAsJsonString();
				String in_json_value = in_asp.getValueAsJsonString();
				assertTrue(out_asp.getDataType().equals(in_asp.getDataType()));
				if (out_json_value.equals(in_json_value)) {
					return true;
				}else {
					throw new RuntimeException("Attribute names match but json differs: " + out_json_value + "\n-\n" + in_json_value);
				}
			}
		}else if (out instanceof CyVisualPropertiesElement) {
			CyVisualPropertiesElement out_cvpe = (CyVisualPropertiesElement) out;
			CyVisualPropertiesElement in_cvpe = (CyVisualPropertiesElement) in;
			return visualPropertiesPersist(in_cvpe, out_cvpe);
		}else if (out instanceof SubNetworkElement) {
			SubNetworkElement in_sne = (SubNetworkElement) in;
			SubNetworkElement out_sne = (SubNetworkElement) out;
			
			return Long.compare(in_sne.getId(), out_sne.getId()) == 0 &&
					compareUnordered(in_sne.getEdges(), out_sne.getEdges()) &&
					compareUnordered(in_sne.getNodes(), out_sne.getNodes());
		} else if (out instanceof CyGroupsElement) {
			CyGroupsElement in_cge = (CyGroupsElement) in;
			CyGroupsElement out_cge = (CyGroupsElement) out;
			
			return in_cge.getName().equals(out_cge.getName()) &&
					Long.compare(in_cge.getGroupId(), out_cge.getGroupId()) == 0 &&
					in_cge.isCollapsed() == out_cge.isCollapsed() &&
					compareUnordered(in_cge.getNodes(), out_cge.getNodes()) &&
					compareUnordered(in_cge.getInternalEdges(), out_cge.getInternalEdges()) &&
					compareUnordered(in_cge.getExternalEdges(), out_cge.getExternalEdges());
					
		}
		
		return false;
	}
	
	
	/* Visual Property comparison helpers */
	private boolean visualPropertiesPersist(CyVisualPropertiesElement in_cvpe, CyVisualPropertiesElement out_cvpe) {
		if (!in_cvpe.getProperties_of().equals(out_cvpe.getProperties_of())) {
			System.out.println("VisProp: Props of " + in_cvpe.getProperties_of() + "!=" + out_cvpe.getProperties_of());
			return false;
		}
		if(!(in_cvpe.getApplies_to() == null || Objects.equals(in_cvpe.getApplies_to(), out_cvpe.getApplies_to()))) {
			System.out.println("VisProp: Applies to: " + in_cvpe.getApplies_to() + "!=" + out_cvpe.getApplies_to());
			return false;
		}
		
		if (!(in_cvpe.getView() == null || Objects.equals(in_cvpe.getView(), out_cvpe.getView()))) {
			System.out.println("VisProp: View: " + in_cvpe.getView() + "!=" + out_cvpe.getView());
			return false;
		}
		
		// Dependencies do not always persist. Any better way to check them?
		return visMappingsPersist(in_cvpe.getMappings(), out_cvpe.getMappings()) && 
				visPropsPersist("dependency", in_cvpe.getDependencies(), out_cvpe.getDependencies()) &&
				visPropsPersist("property", in_cvpe.getProperties(), out_cvpe.getProperties());
	}

	private boolean visPropsPersist(String type, SortedMap<String, String> in_map, SortedMap<String, String> out_map) {
		for (String key : in_map.keySet()) {
			if (!out_map.containsKey(key)) {
				if (type.equals("dependency")) {
					// Dependencies only written if required
					continue;
				}
				logger.error(type + " missing: " + key + "=" + in_map.get(key));
				return false;
			}
			// Some fonts/props change over round trip
			if (ArrayUtils.contains(VALID_INCONSISTENT_VIS_PROPS, key)) {
				continue;
			}
			
			
			if (!in_map.get(key).equals(out_map.get(key))) {
				logger.error(String.format("%s %s mismatch: \nIN: %s\nOUT: %s", type, key, in_map.get(key), out_map.get(key)));
				return false;
			}
		}
		return true;
	}

	private boolean visMappingsPersist(SortedMap<String, Mapping> in_mapping, SortedMap<String, Mapping> out_mapping) {
		for (String key : in_mapping.keySet()) {
			if (!out_mapping.containsKey(key)) {
				logger.error("Mapping missing :" + key + "=" + in_mapping.get(key));
				return false;
			}
			Mapping in = in_mapping.get(key);
			Mapping out = out_mapping.get(key);
			if (!in.getType().equals(out.getType())) {
				logger.error("Mapping type mismatch: " + in.getType() + " != " + out.getType());
				return false;
			}
			
			// Some fonts/props change over round trip
			if (ArrayUtils.contains(VALID_INCONSISTENT_VIS_PROPS, key)) {
				continue;
			}
			try {
				if (!compareMappingDefinition(in.getDefinition(), out.getDefinition(), in.getType())) {
					logger.error("Mapping " + key + " definition mismatch:\n< " +  in.getDefinition() + "\n> " + out.getDefinition());
					return false;
				}
			}catch (IOException e) {
				logger.error("Unable to build StringParser");
				
				return false;
			}
		}
			
		return true;
	}
	
	private boolean compareMappingDefinition(String in, String out, String type) throws IOException {
		StringParser inParser = new StringParser(in);
		StringParser outParser = new StringParser(out);
		
//		if (inParser.get("COL=").equals(outParser.get("COL="))) {
//			return false;
//		}
//		if (inParser.get("T=").equals(outParser.get("T="))) {
//			return false;
//		}
		ArrayList<ArrayList<String>> inMap = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> outMap = new ArrayList<ArrayList<String>>();
		
		String[] keys;
		switch(type) {
		case "DISCRETE":
			keys = new String[] {"K=", "V="};
			break;
		case "CONTINUOUS":
			keys = new String[] {"L=", "E=", "G=", "OV="};
			break;
		default:
			keys = new String[0];
		}
		
		int counter = 0;
		while (true) {
			ArrayList<String> ins = new ArrayList<String>();
			ArrayList<String> outs = new ArrayList<String>();
			
			
			for (String k : keys) {
				String inVal = inParser.get(k + counter);
				if (inVal == null) {
					break;
				}
				String outVal = outParser.get(k + counter);
				
				// Remove .0 from mappings so Integer/Double alignment matches
				inVal = inVal.replaceAll("\\.0([^0-9]|$)", "$1");
				outVal = outVal.replaceAll("\\.0([^0-9]|$)", "$1");
				
				ins.add(inVal);
				outs.add(outVal);
			}
			counter++;
			if (ins.isEmpty()) {
				break;
			}

			if (!ins.equals(outs)) {
				inMap.add(ins);
				outMap.add(outs);
			}
		}
		
		return compareUnordered(inMap, outMap);
	}

	private boolean isValidRemovedVisProp(CyVisualPropertiesElement cvpe) {
		for (String key : VALID_REMOVED_VIS_PROPS) {
			cvpe.getProperties().remove(key);
		}
		// Remove old mapping properties
		List<String> keys = cvpe.getProperties().keySet().stream().filter(name -> {
			return name.startsWith("PASSTHROUGH_MAPPING") || 
					name.startsWith("DISCRETE_MAPPING") || 
					name.startsWith("CONTINUOUS_MAPPING");
		}).collect(Collectors.toList());
		for (String key : keys) {
			cvpe.getProperties().remove(key);
		}
		
		return cvpe.getProperties().isEmpty() && cvpe.getDependencies().isEmpty() && cvpe.getMappings().isEmpty();
	}

	
	private <T extends AspectElement> boolean containsAspect(Collection<? extends T> output_aspects, T aspect) throws IOException {
		if (output_aspects == null || output_aspects.isEmpty()) {
			return false;
		}
		// Pre-filter aspects
		List<? extends T> output_filtered = output_aspects.stream().filter(asp -> {
			return filter(aspect, asp);
		}).collect(Collectors.toList());
		
		if (output_filtered.isEmpty()) {
			System.out.println(output_aspects);
			fail("No filtered matches for " + aspect);
		}
		
		for (T output_aspect : output_filtered) {
			if (compareAspect(aspect, output_aspect)) {
//				output_aspects.remove(output_aspect);
				return true;
			}
		}
		logger.info("Failed to find " + aspect + " in: \n" + output_filtered);
		return false;
	}
	
	
	private boolean containsAspect(NiceCXNetwork output, AspectElement aspect) throws IOException {
		if (aspect instanceof AbstractAttributesAspectElement) {
			AbstractAttributesAspectElement aaae = (AbstractAttributesAspectElement) aspect;
			if (isIgnore(aaae)) {
				return true;
			}
		}
		switch (aspect.getAspectName()) {
		case NodesElement.ASPECT_NAME:
			NodesElement ne = (NodesElement) aspect;
			NodesElement ne2 = output.getNodes().get(ne.getId());
			return compareAspect(ne, ne2);
		case EdgesElement.ASPECT_NAME:
			EdgesElement ee = (EdgesElement) aspect;
			EdgesElement ee2 = output.getEdges().get(ee.getId());
			return compareAspect(ee, ee2);
		case NetworkAttributesElement.ASPECT_NAME:
			return containsAspect(output.getNetworkAttributes(), aspect);
		case HiddenAttributesElement.ASPECT_NAME:
			Collection<AspectElement> hidden = output.getOpaqueAspectTable().get(HiddenAttributesElement.ASPECT_NAME);
			return containsAspect(hidden, aspect);
		case NodeAttributesElement.ASPECT_NAME:
			NodeAttributesElement nodeAttr = (NodeAttributesElement) aspect;
			Long node = nodeAttr.getPropertyOf();
			return containsAspect(output.getNodeAttributes().get(node), nodeAttr);
		case EdgeAttributesElement.ASPECT_NAME:
			EdgeAttributesElement eae = (EdgeAttributesElement) aspect;
			Long edge = eae.getPropertyOf();
			return containsAspect(output.getEdgeAttributes().get(edge), eae);	
		case NamespacesElement.ASPECT_NAME:
			return verifyNamespace(output, aspect);
		case SubNetworkElement.ASPECT_NAME:
			return containsAspect(output.getOpaqueAspectTable().get(SubNetworkElement.ASPECT_NAME), aspect);
		case CyVisualPropertiesElement.ASPECT_NAME:
			CyVisualPropertiesElement cvpe = (CyVisualPropertiesElement) aspect;
			if (isValidRemovedVisProp(cvpe)) {
				return true;
			}
			return containsAspect(output.getOpaqueAspectTable().get(CyVisualPropertiesElement.ASPECT_NAME), cvpe);
		case CartesianLayoutElement.ASPECT_NAME:
			CartesianLayoutElement cle = (CartesianLayoutElement) aspect;
			return containsNodeAspect(output.getNodeAssociatedAspects().get(CartesianLayoutElement.ASPECT_NAME), cle);
		case CyGroupsElement.ASPECT_NAME:
			return containsAspect(output.getOpaqueAspectTable().get(CyGroupsElement.ASPECT_NAME), aspect);
		case Provenance.ASPECT_NAME:
			// Provenance is excluded from output
			if (output.getOpaqueAspectTable().containsKey(Provenance.ASPECT_NAME)) {
				fail("Provenance was not removed");
			}
		case CitationElement.ASPECT_NAME:
			// Citations are inconsistent 
		case NetworkRelationsElement.ASPECT_NAME:
			// Network Relations are used to build a network/view ID map
			return true;
		case CyTableColumnElement.ASPECT_NAME:
		
			CyTableColumnElement cyTableColumnElement=(CyTableColumnElement) aspect;
			
			Collection<AspectElement> outputTableColumns = output.getOpaqueAspectTable().get(CyTableColumnElement.ASPECT_NAME);
			
			final Long subnetwork = cyTableColumnElement.getSubnetwork();
			
			long count = outputTableColumns.stream().filter(x -> {
				return cyTableColumnElement.getName().equals(((CyTableColumnElement) x).getName()) 
						&& (subnetwork == null && ((CyTableColumnElement) x).getSubnetwork() == null) || (subnetwork != null && subnetwork.equals(((CyTableColumnElement) x).getSubnetwork()));
			}).count();
			
			return count > 0;
		default:
			OpaqueElement oe = (OpaqueElement) aspect;
			Map<String, Collection<AspectElement>> table = output.getOpaqueAspectTable();
			if (ArrayUtils.contains(NiceCyRootNetwork.UNSERIALIZED_OPAQUE_ASPECTS, oe.getAspectName())){
				return true;
			}
			assertTrue(oe.getAspectName() + " not in opaque table", table.containsKey(oe.getAspectName()));
			Collection<AspectElement> els = table.get(oe.getAspectName());
			return containsAspect(els, aspect);
		}
	}

	
	
	private boolean containsNodeAspect(Map<Long, Collection<AspectElement>> map, CartesianLayoutElement cle) {
		//NODE_LOCATION mapping overwrites cartesianLayout
		if (!map.containsKey(cle.getNode())) {
			return false;
		}
		for (AspectElement ae : map.get(cle.getNode())){
			CartesianLayoutElement out_cle = (CartesianLayoutElement) ae;
			if (cle.getNode() != null && Long.compare(cle.getNode(), out_cle.getNode()) != 0) {
				continue;
			}
			if (cle.getView() != null && Long.compare(cle.getView(), out_cle.getView()) != 0) {
				continue;
			}
			if (Double.compare(cle.getX(), out_cle.getX()) != 0) {
				continue;
			}
			if (Double.compare(cle.getY(), out_cle.getY()) != 0) {
				continue;
			}
			if (cle.getZ() != null && Double.compare(cle.getZ(), out_cle.getZ()) != 0) {
				continue;
			}
			return true;
		}
		return false;
	}

	
	
	private boolean verifyNamespace(NiceCXNetwork output, AspectElement aspect) throws JsonProcessingException, IOException {
		if (aspect instanceof NamespacesElement) {
			NamespacesElement ne = (NamespacesElement) aspect;
			if (ne.isEmpty()) {
				return true;
			}
		}
		assertFalse("@context should be moved into NetworkAttributes", output.getOpaqueAspectTable().containsKey(NamespacesElement.ASPECT_NAME));
		// context is written to a network attribute as @context->[list, of, serialized]
		
		OpaqueElement el = (OpaqueElement) aspect;
		JsonNode left = el.getData();
		ObjectMapper mapper = new ObjectMapper();
		
		for (NetworkAttributesElement nae : output.getNetworkAttributes()) {
			if (nae.getName().equals(NamespacesElement.ASPECT_NAME)) {
				JsonNode right = mapper.readTree(nae.getValue());
				Iterator<String> names = right.fieldNames();
				while(names.hasNext()) {
					String name = names.next();
					if (!left.has(name)) {
						throw new RuntimeException("Output Namespace missing " + name);
					}
					assertEquals("Ineven value in Namespace at " + name + 
							":\nLEFT:\n" + left.get(name) +
							":\nRIGHT:\n" + right.get(name)
							, right.get(name), left.get(name));
				}
				return true;
			}
		}
		return false;
	}

	
	/* Helper methods for data comparison */
	private String getJson(AspectElement e) throws IOException {
		OutputStream out = new ByteArrayOutputStream();
		JsonWriter writer = JsonWriter.createInstance(out, false);
		e.write(writer);
		writer.close();
		return out.toString();
	}
	
	private <T> boolean compareUnordered(Collection<T> in, Collection<T> out) {
		return in.size() == out.size() && in.containsAll(out);
	}
	
	private <T extends AspectElement> boolean filter(T l, T r) {
		if (l instanceof CyVisualPropertiesElement) {
			CyVisualPropertiesElement l_cvpe = (CyVisualPropertiesElement) l;
			CyVisualPropertiesElement r_cvpe = (CyVisualPropertiesElement) r;
			
			// Handle single view collections that have null "view"
			return l_cvpe.getProperties_of().equals(r_cvpe.getProperties_of()) && 
					(l_cvpe.getApplies_to() == null || Objects.equals(l_cvpe.getApplies_to(), r_cvpe.getApplies_to())) &&
					(l_cvpe.getView() == null || Objects.equals(l_cvpe.getView(), r_cvpe.getView()));
			
		}else if (l instanceof AbstractAttributesAspectElement) {
			AbstractAttributesAspectElement l_asp = (AbstractAttributesAspectElement) l;
			AbstractAttributesAspectElement r_asp = (AbstractAttributesAspectElement) r;
			
			return l_asp.getName().equals(r_asp.getName());
		}
		return true;
	}

	public final boolean isIgnore(final AbstractAttributesAspectElement element) {
    	String column_name = element.getName();
    	if (!element.isSingleValue()) {
    		return element.getValues() == null || element.getValues().isEmpty();    		
    	}
    	
    	Object value = CxUtil.getValue(element);
    	if (value == null || (value instanceof String && ((String) value).isEmpty())) {
    		return true;
    	}
    	
    	return Settings.isIgnore(column_name, null, value);
	}

	// Run CxNetworkReader, export to file, and import back to NiceCX for compare
	public static NiceCXNetwork getOutput(CxReaderWrapper reader) throws IOException {
		boolean collection = CxUtil.isCollection(reader.getNiceCX());
		boolean useCxId = !collection;
		String name = reader.getNiceCX().getNetworkName();
		if (name == null) {
			name = "Unnamed network";
		}
		
		CyNetwork[] networks = loadNetworks(reader);
		ByteArrayOutputStream out = saveNetwork(networks[0], collection, useCxId);
		
		File outf = TestUtil.saveOutputStreamToFile(out, name + "_test_output.cx");
		// TODO: Delete test files on exit
//		outf.deleteOnExit();
		FileInputStream export_in = new FileInputStream(outf);
		
		CxReaderWrapper out_reader = INSTANCE.getReader(export_in, null);
		NiceCXNetwork niceCX = out_reader.getNiceCX();
		
		Map<Long, Long> idMap = INSTANCE.getCxIdMapping(niceCX.getOpaqueAspectTable());
		idMap.putAll(getNetworkViewMapping(reader.getNiceCX(), niceCX));
		INSTANCE.applyCxMapping(niceCX, idMap);
		
		return niceCX;
	}
	
	
	/* Apply CX ID Mapping to all affected aspects in the output for easy comparison */
	
	private Map<Long, Long> getCxIdMapping(Map<String, Collection<AspectElement>> opaqueAspectTable) {
		Map<Long, Long> idMap = new HashMap<Long, Long>();
		if (opaqueAspectTable == null || !opaqueAspectTable.containsKey(CxUtil.CX_ID_MAPPING)) {
			return idMap;
		}
		for(AspectElement ae : opaqueAspectTable.remove(CxUtil.CX_ID_MAPPING)) {
			OpaqueElement oe = (OpaqueElement) ae;
			JsonNode node = oe.getData();
			node.fields().forEachRemaining(entry -> {
				String suidStr = entry.getKey();
				Long suid = Long.valueOf(suidStr);
				Long cxid = entry.getValue().asLong();
				idMap.put(suid, cxid);
			});
		}
		return idMap;
	}
	public void applyCxMapping(NiceCXNetwork niceCX, Map<Long, Long> cxMapping) { 
		Map<String, Collection<AspectElement>> table = niceCX.getOpaqueAspectTable();
		if (cxMapping.isEmpty()) {
			return;
		}
		
		updateNodeIds(niceCX.getNodes(), cxMapping);
		updateEdgeIds(niceCX.getEdges(), cxMapping);
		updateGroupIds(niceCX.getOpaqueAspectTable().get(CyGroupsElement.ASPECT_NAME), cxMapping);
		updateAttributeIds(niceCX.getNodeAttributes(), cxMapping);
		updateAttributeIds(niceCX.getEdgeAttributes(), cxMapping);
		updateCartesianLayoutIds(niceCX.getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME), cxMapping);
		Collection<AspectElement> visProps = table.get(CyVisualPropertiesElement.ASPECT_NAME);
		
		if (visProps == null) {
			visProps = table.get("visualProperties");
		}
		updateCyVisualPropertyIds(visProps, cxMapping);
		updateSubNetworkIds(table.get(SubNetworkElement.ASPECT_NAME), cxMapping);
		updateColumnIds(table, cxMapping);
		
	}
	
	private void updateGroupIds(Collection<AspectElement> collection, Map<Long, Long> cxMapping) {
		if (collection == null) {
			return;
		}
		List<AspectElement> copy = new ArrayList<AspectElement>();
		for (AspectElement ae : collection) {
			CyGroupsElement cge = (CyGroupsElement) ae;
			CyGroupsElement cge_new = new CyGroupsElement(cxMapping.get(cge.getGroupId()), null, cge.getName());;
			cge.getNodes().forEach(node -> {
				cge_new.addNode(cxMapping.get(node));
			});
			cge.getInternalEdges().forEach(edge -> {
				cge_new.addInternalEdge(cxMapping.get(edge));
			});
			cge.getExternalEdges().forEach(edge -> {
				cge_new.addExternalEdge(cxMapping.get(edge));
			});
			cge_new.set_isCollapsed(cge.isCollapsed());
			copy.add(cge_new);
		}
		collection.clear();
		collection.addAll(copy);
	}
	private void updateSubNetworkIds(Collection<AspectElement> collection, Map<Long, Long> cxMapping) {
		collection.forEach(ae -> {
			SubNetworkElement sne = (SubNetworkElement) ae;
				sne.setId(cxMapping.getOrDefault(sne.getId(), sne.getId()));
				sne.setNodes(sne.getNodes().stream().map(id -> cxMapping.getOrDefault(id, id)).collect(Collectors.toList()));
				sne.setEdges(sne.getEdges().stream().map(id -> cxMapping.getOrDefault(id, id)).collect(Collectors.toList()));
		});
	}

	private void updateColumnIds(Map<String, Collection<AspectElement>> table, Map<Long, Long> cxMapping) {
		ArrayList<AspectElement> newCollection  = new ArrayList<AspectElement>();
		Collection<AspectElement> collection = table.get(CyTableColumnElement.ASPECT_NAME);
			collection.forEach(ae -> {
			CyTableColumnElement tce = (CyTableColumnElement) ae;
			Long newId = cxMapping.get(tce.getSubnetwork());
			newCollection.add(new CyTableColumnElement(newId, tce.getAppliesTo(), tce.getName(), tce.getDataType()));
		});
		table.put(CyTableColumnElement.ASPECT_NAME, newCollection);
	}
	
	private void updateCyVisualPropertyIds(Collection<AspectElement> visProps, Map<Long, Long> cxMapping) {
		if (visProps == null) {
			return;
		}
		for (AspectElement visProp : visProps) {
			CyVisualPropertiesElement cvpe = (CyVisualPropertiesElement) visProp;
			cvpe.setApplies_to(cxMapping.get(cvpe.getApplies_to()));
			cvpe.setView(cxMapping.get(cvpe.getView()));
		}
	}

	private void updateCartesianLayoutIds(Map<Long, Collection<AspectElement>> map, Map<Long, Long> cxMapping) {
		if (map == null) {
			return;
		}
		Map<Long, Collection<AspectElement>> copy = new HashMap<Long, Collection<AspectElement>>();
		for (Collection<AspectElement> cae : map.values()) {
			for (AspectElement ae : cae) {
				CartesianLayoutElement cle = (CartesianLayoutElement) ae;
				Long suid = cle.getNode();
				Long cxid = cxMapping.get(suid);
				Long view = cle.getView();
				Long viewCx = cxMapping.get(view);
				String x = String.valueOf(cle.getX());
				String y = String.valueOf(cle.getY());
				String z = String.valueOf(cle.getZ());
				
				if (!copy.containsKey(cxid)) {
					copy.put(cxid, new ArrayList<AspectElement>());
				}
				if (cle.getZ() == null) {
					copy.get(cxid).add(new CartesianLayoutElement(cxid, viewCx, x, y));
				}else {
					copy.get(cxid).add(new CartesianLayoutElement(cxid, viewCx, x, y, z));
				}
			}
		}
		map.clear();
		map.putAll(copy);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends AbstractElementAttributesAspectElement> void updateAttributeIds(
			Map<Long, Collection<T>> attrMap, 
			Map<Long, Long> cxMapping) {
		Map<Long, Collection<T>> attrMapCopy = new HashMap<Long, Collection<T>>();
		while (!attrMap.isEmpty()) {
			Long suid = attrMap.keySet().iterator().next();
			Collection<T> attrs = attrMap.remove(suid);
			
			Long cxid  = cxMapping.get(suid);
			if(!attrMapCopy.containsKey(cxid)) {
				attrMapCopy.put(cxid, new ArrayList<T>());
			}
			
			for(T nae : attrs) {
				Long subnet = cxMapping.get(nae.getSubnetwork());
				AbstractAttributesAspectElement new_attr;
				
				switch (nae.getAspectName()) {
				case EdgeAttributesElement.ASPECT_NAME:
					if (nae.isSingleValue()) {
						new_attr = new EdgeAttributesElement(subnet, cxid, nae.getName(), nae.getValue(), nae.getDataType());
					}else {
						new_attr = new EdgeAttributesElement(subnet, cxid, nae.getName(), nae.getValues(), nae.getDataType());
					}
					break;
				case NodeAttributesElement.ASPECT_NAME:
					if (nae.isSingleValue()) {
						new_attr = new NodeAttributesElement(subnet, cxid, nae.getName(), nae.getValue(), nae.getDataType());
					}else {
						new_attr = new NodeAttributesElement(subnet, cxid, nae.getName(), nae.getValues(), nae.getDataType());
					}
					break;
					default:
						throw new RuntimeException("");
				}
				attrMapCopy.get(cxid).add((T) new_attr);
			}
		}
		attrMap.putAll(attrMapCopy);
	}	
	private void updateNodeIds(Map<Long, NodesElement> nodeMap, Map<Long, Long> cxMapping) {
		Map<Long, NodesElement> nodeCopy = new HashMap<Long, NodesElement>();
		
		while (!nodeMap.isEmpty()) {
			Long suid = nodeMap.keySet().iterator().next();
			NodesElement node = nodeMap.remove(suid);
			Long cxid = cxMapping.get(suid);
			
			node.setId(cxid);
			nodeCopy.put(cxid, node);
		}
		nodeCopy.values().forEach(node -> {
			nodeMap.put(node.getId(), node);
		});
	}
	
	private void updateEdgeIds(Map<Long, EdgesElement> edgeMap, Map<Long, Long> cxMapping) {
		Map<Long, EdgesElement> edgeCopy = new HashMap<Long, EdgesElement>();
		while (!edgeMap.isEmpty()) {
			Long suid = edgeMap.keySet().iterator().next();
			EdgesElement edge = edgeMap.remove(suid);
			Long cxid = cxMapping.get(suid);
			
			edge.setId(cxid);
			edge.setSource(cxMapping.get(edge.getSource()));
			edge.setTarget(cxMapping.get(edge.getTarget()));
			edgeCopy.put(cxid, edge);
		}
		edgeCopy.values().forEach(edge -> {
			edgeMap.put(edge.getId(), edge);
		});
	}
	
}
