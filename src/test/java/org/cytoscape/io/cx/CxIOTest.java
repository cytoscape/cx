package org.cytoscape.io.cx;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.internal.cx_reader.CxToCy;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.NetworkTestSupportTest;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.junit.Test;
import org.ndexbio.model.cx.NiceCXNetwork;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;


public class CxIOTest {
	protected NetworkTestSupport nts = new NetworkTestSupport();
	CyNetworkManager network_manager = nts.getNetworkManager();
	CyNetworkFactory network_factory = nts.getNetworkFactory();
    
    
	Logger logger = Logger.getLogger("CxIOTest");
	
	CyGroupFactory group_factory = mock(CyGroupFactory.class);
	
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
	
}