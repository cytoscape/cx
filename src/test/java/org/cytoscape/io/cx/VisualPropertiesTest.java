package org.cytoscape.io.cx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.cytoscape.io.cx.helpers.JsonComparator;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.model.CyNetwork;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

public class VisualPropertiesTest {
	
	@BeforeClass
	public static void init() {
		TestUtil.initServices();
	}
	
	@Test 
	public void testJsonCompare() throws IOException {
		File path = TestUtil.getResource("subnets");
		boolean useCxId = true;
		for (File file : path.listFiles()) {
			CyNetwork[] networks = TestUtil.loadFile(file, null);
			FileInputStream input_stream = new FileInputStream(file);
			InputStream export_input_stream = TestUtil.saveNetwork(networks[0], false, useCxId);
			compareAsJson(input_stream, export_input_stream);
			break;
		}
	}
	
	
	public void compareAsJson(InputStream in, InputStream out) {
		JsonStreamParser input = new JsonStreamParser(new InputStreamReader(in));
		JsonStreamParser output = new JsonStreamParser(new InputStreamReader(out));
		JsonElement left = input.next();
		JsonElement right = output.next();
		JsonComparator jc = new JsonComparator(left, right);
		
//		System.out.println("Left only:" );
//		for (JsonDifference jd : jc.getLeftOnly()) {
//			System.out.println(jd.getPath());
////			System.out.println(" " + jd.getElement());
//		}
//		
//		System.out.println("Right only:" );
//		for (JsonDifference jd : jc.getRightOnly()) {
//			System.out.println(jd.getPath());
////			System.out.println(" " + jd.getElement());
//		}
	}
}
