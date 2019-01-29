package org.cytoscape.io.cx;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.junit.Before;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.misc.OpaqueElement;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.Provenance;
import org.ndexbio.model.cx.NamespacesElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class CxIOTest {
	Logger logger = Logger.getLogger("CxIOTest");
	Gson gson = new Gson();

	private static final String[] CY_ADDED_ASPECTS = new String[] { CyTableColumnElement.ASPECT_NAME };

	private static final String[] CY_REMOVED_ASPECTS = new String[] { Provenance.ASPECT_NAME,
			NamespacesElement.ASPECT_NAME, };

	private static final String[] CY_ADDED_ATTRIBUTES = new String[] { CyNetwork.NAME, CyEdge.INTERACTION,
			CyRootNetwork.SHARED_INTERACTION, CyRootNetwork.SHARED_NAME };

	private static final boolean SAVE_CX_FILES = true;

	@Before
	public void init() {
		TestUtil.initServices();
	}

	public File getPath(String... dir) {
		File file = new File("src/test/resources/");
		for (String s : dir) {
			file = new File(file, s);
		}
		return file;
	}
	
// TODO: Add other tests for collections
//	@Test
//	public void testCollections() {
//		File path = getPath("collections");
//		for (File f : path.listFiles()) {
//			if (f.getName().endsWith(".cx")) {
//				try {
//					run(f, null, false);
//				} catch (IOException e) {
//					fail("Failed to run CX IO test on " + path.getName() + ": " + e.getMessage());
//				}
//			}
//			break; // TODO: remove
//		}
//	}

	@Test
	public void testSubnets() {
		File path = getPath("subnets");
		for (File f : path.listFiles()) {
			if (f.getName().endsWith(".cx")) {
				try {
					run(f, null, true);
				} catch (IOException e) {
					fail("Failed to run CX IO test on " + path.getName() + ": " + e.getMessage());
				}
			}
		}
	}
	
	@Test
	public void testSUIDs() throws IOException {
		CyNetworkManager network_manager = CyServiceModule.getService(CyNetworkManager.class);
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		
		CyNetwork network = network_factory.createNetwork();
		int nodeCount = 10;
		CyNode[] nodes = new CyNode[nodeCount];
		for (int i = 0; i < nodeCount; i++) {
			nodes[i] = network.addNode();
		}
		for (int j = 0; j < 10; j++) {
			int source = (int)(Math.random() * nodeCount);
			int target = (int)(Math.random() * nodeCount);
			network.addEdge(nodes[source], nodes[target], true);
		}
		network_manager.addNetwork(network);
		
		final CxImporter cx_importer = new CxImporter();
		final AspectSet aspects = AspectSet.getCytoscapeAspectSet();
		ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
		TestUtil.doExport(network, false, false, aspects, out_stream);
		InputStream export_input_stream = pipe(out_stream);
		NiceCXNetwork exportedCX = cx_importer.getCXNetworkFromStream(export_input_stream);
		
		Set<Long> nodeIDs = exportedCX.getNodes().keySet();
		Set<Long> suids = new HashSet<Long>();
		network.getNodeList().forEach(el -> {
			suids.add(el.getSUID());
		});
		
		nodeIDs.removeAll(suids);
		assertTrue(nodeIDs.isEmpty());
		SetView<Long> diff = Sets.difference(nodeIDs, suids);
		System.out.println(diff.size());
	}

	public void run(File path, String collection_name, boolean use_cxId) throws IOException {
		final CxImporter cx_importer = new CxImporter();
		final AspectSet aspects = AspectSet.getCytoscapeAspectSet();
		logger.info("Testing round trip of " + path.getParent() + " " + path.getName());
		// Import to Cytoscape and compare with CX Network(s)
		InputStream import_input_stream = new FileInputStream(path);
		CyNetwork[] networks;
		try {
			networks = TestUtil.doImportTask(import_input_stream, collection_name);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
		import_input_stream.close();

		import_input_stream = new FileInputStream(path);
		NiceCXNetwork importedCX = cx_importer.getCXNetworkFromStream(import_input_stream);
		// CyNetwork[] networks = TestUtil.doImport(importedCX, collection_name);
		// import_input_stream.close();

		// Export CX and compare with Cytoscape Network(s)
		boolean collection = isCollection(importedCX);

		InputStream export_input_stream = null;
		if (SAVE_CX_FILES) {
			File f = new File(path.getParentFile().getParentFile(), "test_output");
			f = new File(f, path.getName());
			FileOutputStream out_stream = new FileOutputStream(f);
			TestUtil.doExport(networks[0], collection, use_cxId, aspects, out_stream);
			export_input_stream = new FileInputStream(f);
			f.deleteOnExit();
		} else {
			ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
			TestUtil.doExport(networks[0], collection, use_cxId, aspects, out_stream);
			export_input_stream = pipe(out_stream);
		}

		// Compare by NiceCX
		NiceCXNetwork exportedCX = cx_importer.getCXNetworkFromStream(export_input_stream);
		compare(importedCX, exportedCX);

		import_input_stream.close();
		export_input_stream.close();

	}

	private boolean isCollection(NiceCXNetwork niceCX) {
		return niceCX.getOpaqueAspectTable().get(NetworkRelationsElement.ASPECT_NAME) != null;
	}

	private void compare(NiceCXNetwork input, NiceCXNetwork output) {
		if (isCollection(input)) {
			throw new IllegalArgumentException("Cannot compare NiceCX collections. CX IDs will mismatch");
		}
		
		// Compare network name
		assertEquals(input.getNetworkName(), output.getNetworkName());

		// Check for aspects that are altered:
		// @context -> moved to network attributes
		compareNamespaces(input, output);

		compareNetworkAttributes(input, output);

		// Compare nodes
		assertEquals(input.getEdges(), output.getEdges());
		assertEquals(input.getNodes(), output.getNodes());

		// Compare node attributes and values
		assertEquals(input.getNodeAttributes().keySet(), output.getNodeAttributes().keySet());
		input.getNodeAttributes().forEach((cxid, attrs) -> {
			getCytoscapeAdditions(attrs, output.getNodeAttributes().get(cxid));
		});

		compareNodeAssociatedAspects(input.getNodeAssociatedAspects(), output.getNodeAssociatedAspects());

		// Compare edge attributes and values
		assertEquals(input.getEdgeAttributes().keySet(), output.getEdgeAttributes().keySet());
		input.getEdgeAttributes().forEach((cxid, attrs) -> {
			getCytoscapeAdditions(attrs, output.getEdgeAttributes().get(cxid));
		});

		compareOpqqueTables(input.getOpaqueAspectTable(), output.getOpaqueAspectTable());
		checkDeprecatedAspects(input, output);
	}

	private void compareNetworkAttributes(NiceCXNetwork input, NiceCXNetwork output) {
		Collection<NetworkAttributesElement> leftNetworkAttributes = input.getNetworkAttributes();
		Collection<NetworkAttributesElement> rightNetworkAttributes = output.getNetworkAttributes();

		getCytoscapeAdditions(leftNetworkAttributes, rightNetworkAttributes);
	}

	private void compareNamespaces(NiceCXNetwork input, NiceCXNetwork output) {
		// assert namespace is in network attributes on export
		assertTrue(output.getNamespaces().isEmpty());
		assertFalse(output.getOpaqueAspectTable().containsKey(NamespacesElement.ASPECT_NAME));
		
		JsonElement context_out = findContext(output);
		if (context_out == null) {
			return;
		}
		JsonElement context_in = findContext(input);
		assertEquals("@context does not match", context_in, context_out);

	}

	private JsonElement findContext(NiceCXNetwork niceCX) {
		/* Search for context in  NiceCX
		 * in opaque aspects
		 * deprecated @context object
		 * network attributes
		 * 
		 * Should always be exported in network attributes
		*/
		JsonParser parser = new JsonParser();

		// Network Attributes
		Iterator<NetworkAttributesElement> iter = niceCX.getNetworkAttributes().stream().filter(el -> {
			return el.getName().equals(NamespacesElement.ASPECT_NAME);
		}).iterator();
		if (iter.hasNext()) {
			NetworkAttributesElement nae = iter.next();
			niceCX.getNetworkAttributes().remove(nae);
			JsonElement ele = parser.parse(nae.getValue());
			return ele.getAsJsonArray().get(0);
		}

		// Deprecated Namespaces Attribute
		if (!niceCX.getNamespaces().isEmpty()) {
			return gson.toJsonTree(niceCX.getNamespaces());
		}

		// Opaque Aspects
		Collection<AspectElement> context = niceCX.getOpaqueAspectTable().remove(NamespacesElement.ASPECT_NAME);
		if (context != null) {
			OpaqueElement ae = (OpaqueElement) context.iterator().next();
			JsonNode node = ae.getData();
			return parser.parse(node.toString());
		}
		return null;
	}

	private void checkDeprecatedAspects(NiceCXNetwork input, NiceCXNetwork output) {
		// TODO: Handle deprecated aspects if they were in the input
		if (input.getCitations().isEmpty()) {
			assertTrue(output.getCitations().isEmpty());
		} else {
			fail("Not checking Citations yet");
		}

		if (input.getEdgeAssociatedAspects().isEmpty()) {
			assertTrue(output.getEdgeAssociatedAspects().isEmpty());
		} else {
			fail("Not checking edge associated aspects yet");
		}

		if (input.getProvenance() == null) {
			assertNull(output.getProvenance());
		} else {
			fail("Not checking provenance yet");
		}

	}

	private void compareNodeAssociatedAspects(Map<String, Map<Long, Collection<AspectElement>>> leftAspects,
			Map<String, Map<Long, Collection<AspectElement>>> rightAspects) {

		Map<Long, Collection<AspectElement>> leftCartesian = leftAspects.remove(CartesianLayoutElement.ASPECT_NAME);
		Map<Long, Collection<AspectElement>> rightCartesian = rightAspects.remove(CartesianLayoutElement.ASPECT_NAME);
		compareCartesianLayouts(leftCartesian, rightCartesian);

		MapDifference<String, Map<Long, Collection<AspectElement>>> diff = Maps.difference(leftAspects, rightAspects);

		// Assert that no aspects were left out on export
		assertTrue("Node associated aspects not in export: " + diff.entriesOnlyOnLeft().keySet(),
				diff.entriesOnlyOnLeft().isEmpty());

	}

	private void compareCartesianLayouts(Map<Long, Collection<AspectElement>> leftCartesian,
			Map<Long, Collection<AspectElement>> rightCartesian) {

		// Assert that export contains cartesianLayout
		assertNotNull("Exported network has no cartesian layout. Was a view created?", rightCartesian);

		// Nothing to check if no cartesian layout in input
		// TODO: check that they are not all at (0, 0)
		if (leftCartesian == null) {
			return;
		}

		JsonElement leftEle = gson.toJsonTree(leftCartesian);
		JsonElement rightEle = gson.toJsonTree(rightCartesian);
		assertEquals(leftEle, rightEle);

	}

	private void compareOpqqueTables(Map<String, Collection<AspectElement>> leftOpaqueAspectTable,
			Map<String, Collection<AspectElement>> rightOpaqueAspectTable) {

		// Ensure opaque aspects survive round trip (except special cases)
		SetView<String> leftOnly = Sets.difference(leftOpaqueAspectTable.keySet(), rightOpaqueAspectTable.keySet());
		leftOnly.forEach(name -> {
			assertTrue("Input aspect not in output: " + name, ArrayUtils.contains(CY_REMOVED_ASPECTS, name));
		});

		SetView<String> rightOnly = Sets.difference(rightOpaqueAspectTable.keySet(), leftOpaqueAspectTable.keySet());
		rightOnly.forEach(name -> {
			assertTrue("Cytoscape added unexpected opaqueAspect: " + name, ArrayUtils.contains(CY_ADDED_ASPECTS, name));
		});
		
		SetView<String> keys = Sets.intersection(leftOpaqueAspectTable.keySet(), rightOpaqueAspectTable.keySet());
		keys.forEach(name -> {
			switch (name) {
			case CyVisualPropertiesElement.ASPECT_NAME:
				System.out.println("Not comparing visual properties");
//				compareVisualProperties(leftOpaqueAspectTable.get(name), rightOpaqueAspectTable.get(name));
				break;
			default:
				compareOpaqueAspect(leftOpaqueAspectTable.get(name), rightOpaqueAspectTable.get(name));
			}
		});

	}
	

	private void compareOpaqueAspect(Collection<? extends AspectElement> leftAttrs,
			Collection<? extends AspectElement> rightAttrs) {

		JsonElement leftEle = gson.toJsonTree(leftAttrs);
		JsonElement rightEle = gson.toJsonTree(rightAttrs);
		assertEquals(leftEle, rightEle);

	}

	private void getCytoscapeAdditions(Collection<? extends AspectElement> leftAttrs,
			Collection<? extends AspectElement> rightAttrs) {
		// Convert to JsonElement Sets
		HashSet<JsonElement> leftSet = new HashSet<JsonElement>();
		leftAttrs.forEach(attr -> {
			leftSet.add(gson.toJsonTree(attr));
		});
		HashSet<JsonElement> rightSet = new HashSet<JsonElement>();
		if (rightAttrs != null) {
			rightAttrs.forEach(attr -> {
				rightSet.add(gson.toJsonTree(attr));
			});
		}

		// Check that Cytoscape kept all non-empty values
		SetView<JsonElement> diff = Sets.difference(leftSet, rightSet);
		diff.forEach(ele -> {
			JsonObject obj = ele.getAsJsonObject();
			JsonElement value = obj.get("_values");
			assertNotNull("No value in for " + obj, value);
			assertEquals("Input only element is non-null: " + obj, "", value.getAsString());
		});

		// Check Cytoscape added values
		diff = Sets.difference(rightSet, leftSet);
		diff.forEach(ele -> {
			JsonObject obj = ele.getAsJsonObject();
			String ele_name = obj.get("_name").getAsString();
			assertTrue("Cytoscape added unexpected attribute: " + obj,
					ArrayUtils.contains(CY_ADDED_ATTRIBUTES, ele_name));
		});
	}

	private ByteArrayInputStream pipe(ByteArrayOutputStream os) throws IOException {
		return new ByteArrayInputStream(os.toByteArray());
	}

	// TODO: TESTS. Refer to google doc

	// Valid
	// Simple network (name, attributes, node/edge count, etc)
	// Network with cartesianLayout should position correctly
	// Network without cartesianLayout should not leave nodes at (0, 0). Apply
	// layout?
	// Export with CxIDs vs SUIDs

	// Invalid network
	// Empty cx
	// Network with no nodes
	// Edge with nonexistant node
	//

}