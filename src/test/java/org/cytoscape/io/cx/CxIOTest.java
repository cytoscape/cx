package org.cytoscape.io.cx;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.cytoscape.io.cx.helpers.NiceCxComparator;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.model.cx.NiceCXNetwork;

public class CxIOTest {
	Logger logger = Logger.getLogger("CxIOTest");
	
	static CyNetworkFactory network_factory;
	private static final CxImporter cx_importer = new CxImporter();
	
	

	@BeforeClass
	public static void init() {
		TestUtil.initServices();
		network_factory = CyServiceModule.getService(CyNetworkFactory.class);
	}

	
	private void testFile(File f, boolean useCxId) {
		if (f.getName().endsWith(".cx")) {
			try {
				run(f, useCxId);
			} catch (IOException e) {
				fail("Failed to run CX IO test on " + f.getName() + ": " + e.getMessage());
			}
		}
	}
	
	@Test
	public void testCollections() {
		File path = TestUtil.getResource("collections");
		for (File f : path.listFiles()) {
			if (!f.getName().startsWith("gal_filtered_3"))
				continue;
			testFile(f, false);
		}
	}

	@Test
	public void testSubnets() {
		File path = TestUtil.getResource("subnets");
		for (File f : path.listFiles()) {
			testFile(f, true);
		}
	}
	
	@Test
	public void testSpecialCases() {
		File path = TestUtil.getResource("specialCases");
		for (File f : path.listFiles()) {
			testFile(f, true);
		}
	}
	
	
	public void run(File file, boolean useCxId) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(file);
		NiceCXNetwork importedCX = cx_importer.getCXNetworkFromStream(fis);
		
		CyNetwork[] networks = TestUtil.loadFile(file, null);
		boolean collection = CxUtil.isCollection(importedCX);
		InputStream export_input_stream = TestUtil.saveNetwork(networks[0], collection, useCxId);
		NiceCXNetwork exportedCX = cx_importer.getCXNetworkFromStream(export_input_stream);
		NiceCxComparator.INSTANCE.compare(importedCX, exportedCX);
	}

}