package org.cytoscape.io.cx.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.misc.OpaqueElement;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.Provenance;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("deprecation")
public class NiceCxComparator {

	private static final String[] CY_ADDED_ASPECTS = new String[] { 
			CyTableColumnElement.ASPECT_NAME,
			NetworkRelationsElement.ASPECT_NAME,
		};

	private static final String[] CY_REMOVED_ASPECTS = new String[] { 
			Provenance.ASPECT_NAME,
			NamespacesElement.ASPECT_NAME,
		};

	private static final String[] CY_ADDED_ATTRIBUTES = new String[] {
			CyNetwork.NAME, CyEdge.INTERACTION,
			CyRootNetwork.SHARED_INTERACTION, 
			CyRootNetwork.SHARED_NAME,
		};
	
	private static final boolean COMPARE_VIS_PROPS = false;

	
	private static final Map<String, String[]> JSON_ARR_KEYS = new HashMap<String, String[]>();
	{
		JSON_ARR_KEYS.put("cyVisualProperties", new String[] {"_properties_of", "_applies_to"});
	}
	
	Gson gson = new Gson();

	public static NiceCxComparator INSTANCE = new NiceCxComparator();
	
	private NiceCxComparator() {
	}
	
	public void compareCollections(NiceCXNetwork input, NiceCXNetwork output) {
		/*
		 *  Comparing collections breaks because the network, node, and edge ids are inconsistent.
		 *  
		 *  Node and edge IDs are stored in an opaque aspect, so there may be a way to write a complex
		 *  Comparator to match nodes and align collections
		 */
		System.out.println("Cannot compare CX Collections yet...");
	}


	public void compare(NiceCXNetwork input, NiceCXNetwork output) {
		if (CxUtil.isCollection(input)) {
			compareCollections(input, output);
			return;
		}

		// Compare network name
		if (input.getNetworkName() != null) {
			assertEquals(input.getNetworkName(), output.getNetworkName());
		}

		// Check for aspects that are altered:
		// @context -> moved to network attributes
		compareNamespaces(input, output);

		compareNetworkAttributes(input, output);

		// Compare nodes
		assertEquals(input.getNodes(), output.getNodes());
		// Edges don't always match because of meta edges in groups
		compareEdges(input.getEdges(), output.getEdges());

		// Compare node attributes and values
		assertEquals(input.getNodeAttributes().keySet(), output.getNodeAttributes().keySet());
		input.getNodeAttributes().forEach((cxid, attrs) -> {
			getCytoscapeAdditions(attrs, output.getNodeAttributes().get(cxid));
		});

		compareNodeAssociatedAspects(input.getNodeAssociatedAspects(), output.getNodeAssociatedAspects());

		// Compare edge attributes and values
		compareEdgeAttributes(input.getEdgeAttributes(), output.getEdgeAttributes());


		compareOpqqueTables(input.getOpaqueAspectTable(), output.getOpaqueAspectTable());
		checkDeprecatedAspects(input, output);
	}

	private void compareEdgeAttributes(Map<Long, Collection<EdgeAttributesElement>> input,
			Map<Long, Collection<EdgeAttributesElement>> output) {
		
		SetView<Long> diff = Sets.difference(input.keySet(), output.keySet());
		assertTrue("Edge properties removed by Cytoscape: " + diff, diff.isEmpty());
		
		diff = Sets.difference(output.keySet(), input.keySet());
		diff.forEach(suid -> {
			System.out.println("Cytoscape added " + output.get(suid).size() + " properties for edge: " + suid);
		});
		
		input.forEach((cxid, attrs) -> {
			getCytoscapeAdditions(attrs, output.get(cxid));
		});
	}

	private void compareEdges(Map<Long, EdgesElement> input, Map<Long, EdgesElement> output) {
		MapDifference<Long, EdgesElement> diff = Maps.difference(input, output);
		List<Entry<Long, EdgesElement>> rightOnly = diff.entriesOnlyOnRight().entrySet().stream().filter(entry -> {
			if (entry.getValue().getInteraction().equals("meta")) {
				System.out.println("Ignoring meta edge: " + entry.getValue() + ". This doesn't occur outside tests?");
				return false;
			}
			return true;
		}).collect(Collectors.toList());
		
		
		assertTrue("Edges differ: " + diff.entriesDiffering(), diff.entriesDiffering().isEmpty());
		assertTrue("Edges not in input: " + rightOnly, rightOnly.isEmpty());
		assertTrue("Edges not in output: " + diff.entriesOnlyOnLeft(), diff.entriesOnlyOnLeft().isEmpty());
				
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
		/*
		 * Search for context in NiceCX in opaque aspects deprecated @context object
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
				if (!COMPARE_VIS_PROPS) {
					System.out.println("Skipping visual properties");
					break;
				}
			default:
				compareOpaqueAspect(name, leftOpaqueAspectTable.get(name), rightOpaqueAspectTable.get(name));
			}
		});

	}

	private void compareOpaqueAspect(String name, Collection<? extends AspectElement> leftAttrs,
			Collection<? extends AspectElement> rightAttrs) {

		JsonElement leftEle = gson.toJsonTree(leftAttrs);
		JsonElement rightEle = gson.toJsonTree(rightAttrs);
		
		compareElements(name, leftEle, rightEle);

	}
	
	private String getKey(JsonObject eleObj, String path) {
		StringBuilder builder = new StringBuilder();
		String[] keys = JSON_ARR_KEYS.get(path);
		for (int i = 0; i < keys.length; i++) {
			if (i > 0) {
				builder.append(":");
			}
			JsonElement val = eleObj.remove(keys[i]);
			String v = val == null ? "" : val.getAsString();
			builder.append(v);
		}
		return builder.toString();
	}

	private void compareElements(String path, JsonElement leftEle, JsonElement rightEle) {
		if (leftEle == null) {
			System.out.println(path + " not in left");
		}else if (rightEle == null) {
			System.out.println(path + " not in right");
		}else if (leftEle.isJsonObject() && rightEle.isJsonObject()) {
			JsonObject leftObj = leftEle.getAsJsonObject();
			JsonObject rightObj = rightEle.getAsJsonObject();

			for (String key : leftObj.keySet()) {
				compareElements(path + "/" + key, leftObj.get(key), rightObj.get(key));
			}
			
		}else if (leftEle.isJsonArray() && rightEle.isJsonArray()) {
			JsonArray leftArr = leftEle.getAsJsonArray();
			JsonArray rightArr = rightEle.getAsJsonArray();
			
			if (JSON_ARR_KEYS.containsKey(path)) {
				JsonObject newLeft = new JsonObject();
				JsonObject newRight = new JsonObject();
				
				leftArr.forEach(ele -> {
					JsonObject obj = ele.getAsJsonObject();
					String key = getKey(obj, path);
					newLeft.add(key, obj);
				});
				
				rightArr.forEach(ele -> {
					JsonObject obj = ele.getAsJsonObject();
					String key = getKey(obj, path);
					newRight.add(key, obj);
				});
				compareElements(path, newLeft, newRight);
				return;
			}
			
			assertEquals(leftArr.size(), rightArr.size());
			if (leftArr.equals(rightArr)) {
				return;
			}
			HashSet<Integer> left = new HashSet<Integer>(), right = new HashSet<Integer>();
			for (int i = 0; i < leftArr.size(); i++) {
				left.add(i);
				right.add(i);
			}
			for (int i = 0; i < leftArr.size(); i++) {
				int match = -1;
				for (int j : right) {
					try {
						compareElements(path + "(" + i + "/" + j + ")", leftArr.get(i), rightArr.get(j));
						match = j;
						break;
					}catch(AssertionError e) {
						
					}
				}
				if (match >= 0) {
					left.remove(i);
					right.remove(match);
				}
			}
			if (!left.isEmpty()) {
				System.out.println("Unmatched in left at " + path + ": ");
				left.forEach(ind -> {System.out.println(leftArr.get(ind)); });
			}
			if (!right.isEmpty()) {
				System.out.println("Unmatched in right at " + path + ": ");
				right.forEach(ind -> {System.out.println(rightArr.get(ind)); });
			}
			
		}else {
			if (!leftEle.equals(rightEle)) {
				System.out.println(path + " json not equal: " + leftEle + "\n=\\=\n" + rightEle);
			}
		}
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
			
			assertNotNull("No in value for " + obj, value);
			if (!value.isJsonNull() && !value.getAsString().isEmpty()) {
				System.out.println("Only in input: " + ele);
			}
//			assertEquals("Input only element is non-null in output: " + obj, "", value.getAsString());
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

}
