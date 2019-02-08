package org.cytoscape.io.cx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.model.cx.NiceCXNetwork;

public class CxImporterTest {

	private static CyNetworkFactory network_factory;
	private static CyGroupFactory group_factory;
	
	@BeforeClass
	public static void init() {
		TestUtil.initServices();
		network_factory = CyServiceModule.getService(CyNetworkFactory.class);
		group_factory = CyServiceModule.getService(CyGroupFactory.class);
	}
	
	@Test
	public void testGroups() throws IOException {
		CyNetwork network = network_factory.createNetwork();

		// Create some nodes and edges
		CyNode node1 = network.addNode();
		CyNode node2 = network.addNode();
		CyNode node3 = network.addNode();
		CyNode node4 = network.addNode();
		CyNode node5 = network.addNode();
		List<CyNode> groupNodes = new ArrayList<CyNode>();
		groupNodes.add(node1);
		groupNodes.add(node2);
		groupNodes.add(node3);

		CyEdge edge1 = network.addEdge(node1, node2, false);
		CyEdge edge2 = network.addEdge(node2, node3, false);
		CyEdge edge3 = network.addEdge(node2, node4, false);
		CyEdge edge4 = network.addEdge(node2, node5, false);
		network.addEdge(node3, node5, false);
		List<CyEdge> groupEdges = new ArrayList<CyEdge>();
		groupEdges.add(edge1);
		groupEdges.add(edge2);
		groupEdges.add(edge3);
		groupEdges.add(edge4);

		CyGroup group1 = group_factory.createGroup(network, groupNodes, null, true);
		CyGroup group2 = group_factory.createGroup(network, groupNodes, new ArrayList<CyEdge>(), true);
		CyGroup group3 = group_factory.createGroup(network, groupNodes, groupEdges, true);
		group2.collapse(network);
		
		CxImporter cx_importer = new CxImporter();
		ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
		TestUtil.doExport(network, false, false, null, out_stream);
		InputStream export_input_stream = TestUtil.pipe(out_stream);
		NiceCXNetwork exportedCX = cx_importer.getCXNetworkFromStream(export_input_stream);
		
		Collection<AspectElement> groups = exportedCX.getOpaqueAspectTable().get(CyGroupsElement.ASPECT_NAME);
		
		
	}
}
