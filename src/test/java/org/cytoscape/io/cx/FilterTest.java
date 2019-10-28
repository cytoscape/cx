package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriter;
import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.misc.NumberVerification;
import org.ndexbio.model.exceptions.NdexException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FilterTest {
	private CxNetworkWriter writer;
	private ByteArrayOutputStream out;
	
	@BeforeClass
	public static void init() {
		TestUtil.init();
	}
	
	
	@Before
	public void readNetwork() {
		CxReaderWrapper reader = TestUtil.getSubNetwork(TestUtil.getResource("specialCases", "all_aspects.cx"));
		try {
			CyNetwork[] networks = TestUtil.loadNetworks(reader);
			StreamUtil streamUtil = CyServiceModule.getService(StreamUtil.class);
			CytoscapeCxFileFilter filter = new CytoscapeCxFileFilter(streamUtil);
			CxNetworkWriterFactory writerFactory = new CxNetworkWriterFactory(filter);
			
			out = new ByteArrayOutputStream();
			writer = (CxNetworkWriter) writerFactory.createWriter(out, networks[0]);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}
	
	public JsonObject getOutput() throws IOException {
		writer.writeSiblings = false;
		writer.useCxId = true;
		writer.run(null);
		
		InputStream in = TestUtil.pipe(out);
//		NiceCXNetwork niceCX = reader.readNiceCXNetwork(in);
		InputStreamReader inReader = new InputStreamReader(in);
		JsonParser parser = new JsonParser();
		JsonElement cx = parser.parse(inReader);
		
		JsonObject object = new JsonObject();
		
		cx.getAsJsonArray().forEach(aspect -> {
			JsonObject obj = aspect.getAsJsonObject();
			assertTrue(obj.keySet().size() == 1);
			obj.entrySet().forEach(entry -> {
				if (object.has(entry.getKey())) {
					JsonArray arr = object.get(entry.getKey()).getAsJsonArray();
					arr.addAll(entry.getValue().getAsJsonArray());
				}else {
					object.add(entry.getKey(), entry.getValue());
				}
			});
		});
		
		return object;
	}
	
	@Test
	public void testUnknownAspectFilter() {
		List<String> values = new ArrayList<String>();
		values.add("INVALID");
		
		
		try {
			writer.aspectFilter.setSelectedValues(values);
		} catch(IllegalArgumentException e) {
			return;
		}
		fail();
	}
	
	@Test 
	public void testUnknownAspectFilter2() {
		List<String> values = new ArrayList<String>();
		values.add("INVALID");
		writer.aspectFilter.setPossibleValues(values);
		writer.aspectFilter.setSelectedValues(values);
		
		try {
			getOutput();
		} catch (IOException e) {
			fail(e.getMessage());
		} catch(IllegalArgumentException e) {
			return;
		}
		fail();
	}
	
	@Test
	public void testInvalidAttributeFilter() throws IOException {
		
	}
	
	@Test
	public void testAspectFilter() throws IOException, NdexException {
		
		List<String> values = new ArrayList<String>();
		values.add(NodesElement.ASPECT_NAME);
		writer.aspectFilter.setSelectedValues(values);
		
		JsonObject obj = getOutput();
		
		Set<String> keys = obj.keySet();
		keys.remove(MetaDataCollection.NAME);
		keys.remove(NumberVerification.NAME);
		keys.remove("status");
	}
	
	@Test
	public void testNodeColFilter() throws IOException {
		List<String> values = new ArrayList<String>();
		values.add("nodeCol");
		writer.nodeColFilter.setSelectedValues(values);
		
		JsonObject obj = getOutput();
		JsonElement attrs = obj.get(NodeAttributesElement.ASPECT_NAME);
		JsonArray arr = attrs.getAsJsonArray();
		arr.forEach(aspectEl -> {
			JsonObject aspectObj = aspectEl.getAsJsonObject();
			assertEquals(aspectObj.get("n").getAsString(), "nodeCol");
		});
	}
	
	@Test
	public void testEdgeColFilter() throws IOException{
		List<String> values = new ArrayList<String>();
		values.add("edgeCol");
		writer.edgeColFilter.setSelectedValues(values);
		
		JsonObject obj = getOutput();
		JsonElement attrs = obj.get(EdgeAttributesElement.ASPECT_NAME);
		JsonArray arr = attrs.getAsJsonArray();
		arr.forEach(aspectEl -> {
			JsonObject aspectObj = aspectEl.getAsJsonObject();
			assertEquals(aspectObj.get("n").getAsString(), "edgeCol");
		});
	}
	
	@Test
	public void testNetworkColFilter() throws IOException{
		List<String> values = new ArrayList<String>();
		values.add("networkCol");
		writer.networkColFilter.setSelectedValues(values);
		
		JsonObject obj = getOutput();
		JsonElement attrs = obj.get(NetworkAttributesElement.ASPECT_NAME);
		JsonArray arr = attrs.getAsJsonArray();
		arr.forEach(aspectEl -> {
			JsonObject aspectObj = aspectEl.getAsJsonObject();
			assertEquals(aspectObj.get("n").getAsString(), "networkCol");
		});
	}
	
	@Test
	public void testAllFilters() throws IOException {
		List<String> values = new ArrayList<String>();
		values.add(NetworkAttributesElement.ASPECT_NAME);
		values.add(NodeAttributesElement.ASPECT_NAME);
		values.add(EdgeAttributesElement.ASPECT_NAME);
		writer.aspectFilter.setSelectedValues(values);
		
		List<String> networkValues = new ArrayList<String>();
		networkValues.add("networkCol");
		writer.networkColFilter.setSelectedValues(networkValues);
		
		List<String> nodeValues = new ArrayList<String>();
		nodeValues.add("nodeCol");
		writer.nodeColFilter.setSelectedValues(nodeValues);
		
		List<String> edgeValues = new ArrayList<String>();
		edgeValues.add("edgeCol");
		writer.edgeColFilter.setSelectedValues(edgeValues);
		
		JsonObject obj = getOutput();
		
		JsonElement networkAttrs = obj.get(NetworkAttributesElement.ASPECT_NAME);
		JsonArray networkArr = networkAttrs.getAsJsonArray();
		networkArr.forEach(aspectEl -> {
			JsonObject aspectObj = aspectEl.getAsJsonObject();
			assertEquals(aspectObj.get("n").getAsString(), "networkCol");
		});
		
		JsonElement nodeAttrs = obj.get(NodeAttributesElement.ASPECT_NAME);
		JsonArray nodeArr = nodeAttrs.getAsJsonArray();
		nodeArr.forEach(aspectEl -> {
			JsonObject aspectObj = aspectEl.getAsJsonObject();
			assertEquals(aspectObj.get("n").getAsString(), "nodeCol");
		});
		
		JsonElement edgeAttrs = obj.get(EdgeAttributesElement.ASPECT_NAME);
		JsonArray edgeArr = edgeAttrs.getAsJsonArray();
		edgeArr.forEach(aspectEl -> {
			JsonObject aspectObj = aspectEl.getAsJsonObject();
			assertEquals(aspectObj.get("n").getAsString(), "edgeCol");
		});
		
	}
	
}
