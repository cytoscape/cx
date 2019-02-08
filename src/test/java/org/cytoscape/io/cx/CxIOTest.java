package org.cytoscape.io.cx;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.cytoscape.io.cx.helpers.NiceCxComparator;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.model.cx.NiceCXNetwork;
import com.google.gson.Gson;

public class CxIOTest {
	Logger logger = Logger.getLogger("CxIOTest");
	
	Gson gson = new Gson();
	static CyNetworkFactory network_factory;

	private static final boolean SAVE_CX_FILES = true;

	@BeforeClass
	public static void init() {
		Logger.getGlobal().setFilter(new Filter() {

			@Override
			public boolean isLoggable(LogRecord record) {
				return record.getMessage().contains("Error");
			}
		});
		TestUtil.initServices();
		network_factory = CyServiceModule.getService(CyNetworkFactory.class);
	}

	private File getPath(String... dir) {
		File file = new File("src/test/resources/");
		for (String s : dir) {
			file = new File(file, s);
		}
		return file;
	}
	private void testFile(File f, boolean useCxId) {
		if (f.getName().endsWith(".cx")) {
			try {
				run(f, null, useCxId);
			} catch (IOException e) {
				fail("Failed to run CX IO test on " + f.getName() + ": " + e.getMessage());
			}
		}
	}
	
//	@Test
	public void testCollections() {
		File path = getPath("collections");
		for (File f : path.listFiles()) {
			if (!f.getName().startsWith("gal_filtered_3"))
				continue;
			testFile(f, false);
		}
	}

	@Test
	public void testSubnets() {
		File path = getPath("subnets");
		for (File f : path.listFiles()) {
			testFile(f, true);
		}
	}
	
//	@Test
	public void testSpecialCases() {
		File path = getPath("specialCases");
		for (File f : path.listFiles()) {
			testFile(f, true);
		}
	}
	
//	@Test
	public void testTemp() {
		File path = new File("/Users/bsettle/Desktop/collapsed.cx");
		testFile(path, true);
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

		// Export CX and compare with Cytoscape Network(s)
		boolean collection = CxUtil.isCollection(importedCX);
		logger.info("Exporting " + importedCX.getNetworkName() + " as " + (collection ? "collection" : "subnetwork"));
		
		InputStream export_input_stream = null;
		if (SAVE_CX_FILES) {
			File f = File.createTempFile("CX_TEST_OUTPUT", path.getName());
			logger.info("Creating temp file at " + f.getAbsolutePath());
			FileOutputStream out_stream = new FileOutputStream(f);
			TestUtil.doExport(networks[0], collection, use_cxId, aspects, out_stream);
			export_input_stream = new FileInputStream(f);
			f.deleteOnExit();
		} else {
			ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
			TestUtil.doExport(networks[0], collection, use_cxId, aspects, out_stream);
			export_input_stream = TestUtil.pipe(out_stream);
		}

		// Compare by NiceCX
		NiceCXNetwork exportedCX = cx_importer.getCXNetworkFromStream(export_input_stream);
		NiceCxComparator.INSTANCE.compare(importedCX, exportedCX);

		import_input_stream.close();
		export_input_stream.close();

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