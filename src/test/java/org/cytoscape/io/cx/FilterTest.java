package org.cytoscape.io.cx;

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
import org.cytoscape.work.util.ListMultipleSelection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
	public void testAspectFilter() throws IOException, NdexException {
		
		List<String> values = new ArrayList<String>();
		values.add(Aspect.NODES.name());
		writer.aspectFilter = new ListMultipleSelection<String>(values);
		writer.aspectFilter.setSelectedValues(values);
		
		JsonObject obj = getOutput();
		
		Set<String> keys = obj.keySet();
		keys.remove(MetaDataCollection.NAME);
		keys.remove(NumberVerification.NAME);
		keys.remove("status");
	}
	
	@Test
	public void testNodeColFilter() {
		
	}
	@Test
	public void testEdgeColFilter() {
		
	}
	@Test
	public void testNetworkColFilter() {
		
	}
	
}
