package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class CxUtilTest {

	
	@BeforeClass
	public static void init() {
		TestUtil.init();
	}
	
	
	@Test
	public void testIsCollection() throws IOException {
		File f = TestUtil.getResource("collections", "groups_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		assertTrue(CxUtil.isCollection(reader.getNiceCX()));
		
		TestUtil.withAspects(reader);
	}
	
	@Test
	public void testSUIDasCXID() {
		// If no CX IDs are set, use SUIDs
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		CyNetwork network1 = network_factory.createNetwork();
		CyNode node = network1.addNode();
		CyRootNetwork root = ((CySubNetwork) network1).getRootNetwork();
		CyNetwork network2 = root.addSubNetwork();
		CyNode node2 = network2.addNode();
		assertEquals(CxUtil.getElementId(node, root, true), node.getSUID());
		assertEquals(CxUtil.getCxId(node2, root), node2.getSUID());
	}
	
	@Test
	public void testCXIDCounter() throws JsonProcessingException {
		// If one CX ID is set and a node is added, increment the counter
		CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		CyNetwork network1 = network_factory.createNetwork();
		CyNode node = network1.addNode();
		CyRootNetwork root = ((CySubNetwork) network1).getRootNetwork();
		CyNetwork network2 = root.addSubNetwork();
		CyNode node2 = network2.addNode();
		CxUtil.saveCxId(node, root, 2l);
		CxUtil.getElementId(node2, root, true);
		assertEquals(CxUtil.getCxId(node2, root).longValue(), 3l);
	}
}
