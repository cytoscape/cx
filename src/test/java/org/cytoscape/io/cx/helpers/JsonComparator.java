package org.cytoscape.io.cx.helpers;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonComparator {
	private static final String[] IGNORE_PATHS = new String[] {
			"metaData",
			"supports",
			"citations",
			"edgeSupports",
			"nodeSupports",
			"edgeCitations",
			"status",
	};
	private static final Map<String, String[]> JSON_ARR_KEYS = new HashMap<String, String[]>();
	{
		JSON_ARR_KEYS.put("cyVisualProperties", new String[] {"properties_of", "applies_to"});
		JSON_ARR_KEYS.put("nodes", new String[] {"@id"});
		JSON_ARR_KEYS.put("edges", new String[] {"@id"});
		JSON_ARR_KEYS.put("nodeAttributes", new String[] { "po", "n" });
		JSON_ARR_KEYS.put("edgeAttributes", new String[] { "po", "n" });
		JSON_ARR_KEYS.put("networkAttributes", new String[] { "n" });
		
	}
	
	public class JsonDifference{
		private final String path;
		private final JsonElement element;
		private JsonDifference(String path, JsonElement element) {
			this.path = path;
			this.element = element;
		}
		
		public String getPath() {
			return path;
		}
		public JsonElement getElement() {
			return element;
		}
	}
	
	private final List<JsonDifference> leftOnly, rightOnly;
	private JsonElement leftIdMap, rightIdMap;
	private Map<Long, Long> ltrNetworkViewMap;
	
	public JsonComparator(JsonElement leftEle, JsonElement rightEle) {
		leftOnly = new ArrayList<JsonDifference>();
		rightOnly = new ArrayList<JsonDifference>();
		JsonObject leftObj = JsonComparator.gatherAspects(leftEle);
		JsonObject rightObj = JsonComparator.gatherAspects(rightEle);
		
		leftIdMap = leftObj.get(CxUtil.CX_ID_MAPPING);
		rightIdMap = leftObj.get(CxUtil.CX_ID_MAPPING);
		
		ltrNetworkViewMap = createNetworkViewMap(
				leftObj.getAsJsonArray(NetworkRelationsElement.ASPECT_NAME), 
				rightObj.getAsJsonArray(NetworkRelationsElement.ASPECT_NAME));
		
		compareElements("", leftObj, rightObj);
	}
	
	private Map<Long, Long> createNetworkViewMap(JsonArray leftNetworkRelations, JsonArray rightNetworkRelations) {
		if ((leftNetworkRelations == null) != (rightNetworkRelations == null)) {
			fail("Network relations must be present in both CX to create ");
		}
		Map<Long, Long> map = new HashMap<Long, Long>();
		for (JsonElement el : leftNetworkRelations) {
			System.out.println(el);	
		}
		for (JsonElement el : rightNetworkRelations) {
			System.out.println(el);
		}
		return map;
	}
	
	private static JsonObject gatherAspects(JsonElement ele) {
		JsonObject return_aspect = new JsonObject();
		
		JsonArray arr = ele.getAsJsonArray();
		for (JsonElement aspect : arr) {
			JsonObject aspect_object = aspect.getAsJsonObject();
			Set<String> keys = aspect_object.keySet();
			assert(keys.size() == 1);
			String aspect_name = keys.iterator().next();
			
			JsonArray aspect_data = aspect_object.getAsJsonArray(aspect_name);
			if (return_aspect.has(aspect_name)) {
				return_aspect.getAsJsonArray(aspect_name).addAll(aspect_data);
			}else {
				return_aspect.add(aspect_name, aspect_data);
			}
		}
		return return_aspect;
	}

	private static String getKey(JsonObject eleObj, String path) {
		StringBuilder builder = new StringBuilder();
		String[] keys = JSON_ARR_KEYS.get(path);
		for (int i = 0; i < keys.length; i++) {
			if (i > 0) {
				builder.append(",");
			}
			JsonElement val = eleObj.remove(keys[i]);
			builder.append(keys[i]);
			builder.append("=");
			builder.append(String.valueOf(val));
		}
		return builder.toString();
	}
	
	public void compareElements(String path, JsonElement leftEle, JsonElement rightEle) {
		if (ArrayUtils.contains(IGNORE_PATHS, path)){
			return;
		}
		if (leftEle == null && rightEle != null) {
			rightOnly.add(new JsonDifference(path, rightEle));
		}else if (rightEle == null && leftEle != null) {
			leftOnly.add(new JsonDifference(path, leftEle));
		}else if (leftEle.isJsonObject() && rightEle.isJsonObject()) {
			JsonObject leftObj = leftEle.getAsJsonObject();
			JsonObject rightObj = rightEle.getAsJsonObject();

			for (String key : leftObj.keySet()) {
				String newPath = path.isEmpty() ? key : path + "/" + key;
				compareElements(newPath, leftObj.get(key), rightObj.get(key));
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
					if (newLeft.has(key)) {
						fail("Invalid duplicate key " + key + " at " + path + " in left");
					}
					newLeft.add(key, obj);
				});
				
				rightArr.forEach(ele -> {
					JsonObject obj = ele.getAsJsonObject();
					String key = getKey(obj, path);
					if (newRight.has(key)) {
						fail("Invalid duplicate key " + key + " at " + path + " in right");
					}
					newRight.add(key, obj);
				});
				compareElements(path, newLeft, newRight);
				return;
			}else {
				System.out.println(leftArr.get(0));
				fail("Unable to align array: " + path);
				return;
			}
		}else {
			if (!leftEle.equals(rightEle)) {
				System.out.println("\n" + path + " json not equal: \nLEFT:" + leftEle + "\nRIGHT:" + rightEle);
			}
		}
	}

	public List<JsonDifference> getLeftOnly() {
		return leftOnly;
	}
	public List<JsonDifference> getRightOnly() {
		return rightOnly;
	}
}
