package org.cytoscape.io.cx;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.internal.cx_reader.CxToCy;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.model.cx.NiceCXNetwork;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;


public class CxIOTest {
	protected NetworkTestSupport nts = new NetworkTestSupport();
	CyNetworkManager network_manager = nts.getNetworkManager();
	CyNetworkFactory network_factory = nts.getNetworkFactory();
    
    
	Logger logger = Logger.getLogger("CxIOTest");
	
	CyGroupFactory group_factory = mock(CyGroupFactory.class);
	CyNetworkViewManager view_manager = mock(CyNetworkViewManager.class);
	
	public InputStream getStream(String dir, String path) throws FileNotFoundException {
		File file = new File("src/test/resources/", dir);
		file = new File(file, path);
		return new FileInputStream(file);
	}
	
	@Test
	public void NDEx1Test() throws IOException {
		String network_collection_name = "Collection Test";
		InputStream input_stream = getStream("testData", "ndex1.cx");
		CyNetwork[] networks = run(input_stream, network_collection_name);
		
		final CxImporter cx_importer = new CxImporter();
		input_stream = getStream("testData", "ndex1.cx");
		NiceCXNetwork niceCX = cx_importer.getCXNetworkFromStream(input_stream);
		
		verifyImport(networks, niceCX);
		
//		/* Collection level */
//		assertEquals(networks.length, 2);
//		CyRootNetwork root = ((CySubNetwork) networks[0]).getRootNetwork();
//		assertNotNull(root);
//		assertEquals(root.getRow(root).get(CyNetwork.NAME, String.class), "AB2");
//		
//		assertEquals(root.getNodeCount(), 5);
//		assertEquals(root.getEdgeCount(), 4);
//		
//		/* Subnetworks */
//		CyNetwork networkA, networkB;
//		if (networks[0].getEdgeCount() == 2) {
//			networkA = networks[0];
//			networkB = networks[1];
//		}else {
//			networkB = networks[0];
//			networkA = networks[1];
//		}
//		
//		// Node and edge count
//		assertEquals(networkA.getNodeCount(), 2);
//		assertEquals(networkA.getEdgeCount(), 1);
//		
//		assertEquals(networkB.getNodeCount(), 3);
//		assertEquals(networkB.getEdgeCount(), 3);
//		
//		// Network attributes
//		assertEquals(networkA.getRow(networkA).get(CyNetwork.NAME, String.class), "A");
//		assertEquals(networkB.getRow(networkB).get(CyNetwork.NAME, String.class), "B");
//		
//		// Node attributes
//		
//		
//		// Edge attributes
//		
//		
//		// Views
//		Collection<CyNetworkView> viewsA = view_manager.getNetworkViews(networkA);
//		assertEquals(viewsA.size(), 1);
//		Collection<CyNetworkView> viewsB = view_manager.getNetworkViews(networkB);
//		assertEquals(viewsB.size(), 1);
//		
//		CyNetworkView viewA = viewsA.iterator().next();
//		CyNetworkView viewB = viewsB.iterator().next();
//		
//		// Visual Properties
//		
//		
//		// cartesianLayout
	}
	
	private void verifyImport(CyNetwork[] networks, NiceCXNetwork niceCX) {
		
		niceCX.getOpaqueAspectTable();
		Collection<AspectElement> network_relations = niceCX.getOpaqueAspectTable().get(NetworkRelationsElement.ASPECT_NAME);
        Map<Long, CyNetwork> subnetwork_id_map = new HashMap<Long, CyNetwork>();
        if (network_relations == null) {
        	assertEquals(networks.length, 1);
        	subnetwork_id_map.put(Long.MAX_VALUE, networks[0]);
        }else {
        	CyRootNetwork root = ((CySubNetwork) networks[0]).getRootNetwork();
        	subnetwork_id_map.put(Long.MAX_VALUE, root);
        	
        }
		
		niceCX.getNetworkAttributes();
		niceCX.getNetworkName();
		niceCX.getCitations();
		niceCX.getMetadata();
		niceCX.getNamespaces();
		niceCX.getProvenance();
		
		niceCX.getEdgeAssociatedAspects();
		niceCX.getEdgeAttributes();
		niceCX.getEdges();

		niceCX.getNodeAssociatedAspects();
		niceCX.getNodeAttributes();
		niceCX.getNodes();
		
//		boolean isCollection = false;
//		
//		for (Entry<String, Collection<AspectElement>> entry : niceCX.getOpaqueAspectTable().entrySet()) {
//			switch(entry.getKey()) {
//			case "cySubNetworks":
//				isCollection = true;
//				break;
//			default:
//				System.out.println(entry.getKey());
//			}
//		}
//		
//		if (isCollection) {
//			CyRootNetwork root = ((CySubNetwork) networks[0]).getRootNetwork();
//			verifyRoot(root, niceCX);
//			
//			
//		}else {
//			verifySubnetwork(networks[0], niceCX);
//		}
		
	}
	
	private void verifyRoot(CyRootNetwork root, NiceCXNetwork niceCX) {
		assertEquals(root.getRow(root).get(CyNetwork.NAME, String.class), niceCX.getNetworkName());
	}
	
	private void verifySubnetwork(CyNetwork network, NiceCXNetwork niceCX) {
		Iterator<NetworkAttributesElement> attrs = niceCX.getNetworkAttributes().iterator();
		while(attrs.hasNext()) {
			NetworkAttributesElement nae = attrs.next();
			String name = nae.getName();
			ATTRIBUTE_DATA_TYPE type = nae.getDataType();
			String value = nae.getValue();
			System.out.println(name + "(" + type + "): " + value);
		}
		assertEquals(network.getRow(network).get(CyNetwork.NAME, String.class), niceCX.getNetworkName());
		for (CyNetworkView view : view_manager.getNetworkViews(network)) {
			verifyView(view, niceCX);
		}
	}
	
	private void verifyView(CyNetworkView view, NiceCXNetwork niceCX) {
		double x = 0, y = 0;
		for (View<CyNode> nodeView : view.getNodeViews()) {
			checkNodePosition(nodeView, x, y);
		}
	}
	
	private void checkNodePosition(View<CyNode> nodeView, double x, double y) {
		Double x_loc = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		Double y_loc = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		assertEquals(x, x_loc.doubleValue(), 0);
		assertEquals(y, y_loc.doubleValue(), 0);
		
	}
	
	public CyNetwork[] run(InputStream stream, String collection_name) throws FileNotFoundException, IOException {
		
		final long t0 = System.currentTimeMillis();
        final CxImporter cx_importer = new CxImporter();

        NiceCXNetwork niceCX = cx_importer.getCXNetworkFromStream(stream);
        
        if (Settings.INSTANCE.isTiming()) {
            TimingUtil.reportTimeDifference(t0, "total time parsing", -1);
        }

        CxToCy _cx_to_cy = new CxToCy();

        List<CyNetwork> networks = _cx_to_cy.createNetwork(niceCX, null, network_factory, group_factory,
        		collection_name);
        
        CyNetwork[] _networks = new CyNetwork[networks.size()];
        networks.toArray(_networks);
        

        if (Settings.INSTANCE.isTiming()) {
            System.out.println();
            TimingUtil.reportTimeDifference(t0, "total time to build network(s) (not views)", -1);
            System.out.println();
        }
        return _networks;
	}
	
	// TESTS
	
	//Valid
	// Simple network (name, attributes, node/edge count, etc)
	// Network with cartesianLayout should position correctly
	// Network without cartesianLayout should not leave nodes at (0, 0). Apply layout?
	// Export with CxIDs vs SUIDs
	
	//Invalid network
	// Empty cx
	// Network with no nodes
	// Edge with nonexistant node
	// 
	
	
}