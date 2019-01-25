package org.cytoscape.io.cx;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cx_reader.CxToCy;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReader;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.ndexbio.model.cx.NiceCXNetwork;

public class TestUtil {
	
	private static NetworkTestSupport nts = new NetworkTestSupport();
	private static CyNetworkFactory network_factory = nts.getNetworkFactory();
	private static NetworkViewTestSupport nvts = new NetworkViewTestSupport();
	private static CyNetworkViewFactory networkview_factory = nvts.getNetworkViewFactory();
	
	private static CyGroupFactory group_factory = mock(CyGroupFactory.class);
	private static CyGroupManager group_manager = mock(CyGroupManager.class);
	private static CyNetworkViewManager networkview_manager = mock(CyNetworkViewManager.class);

	private static final VisualMappingMock vmm = new VisualMappingMock();
	
	final SynchronousTaskManager<?> synchronousTaskManager = mock(SynchronousTaskManager.class);
	
	public static void initServices() {
		CyServiceModule.setService(CyNetworkViewFactory.class, networkview_factory); 
		CyServiceModule.setService(CyNetworkViewManager.class, networkview_manager);
		CyServiceModule.setService(CyNetworkFactory.class, network_factory);
		CyServiceModule.setService(CyNetworkManager.class, nts.getNetworkManager());
		CyServiceModule.setService(CyRootNetworkManager.class, nts.getRootNetworkFactory());
		CyServiceModule.setService(CyGroupManager.class, group_manager);
        
		CyServiceModule.setService(CyGroupFactory.class, group_factory);
		CyServiceModule.setService(VisualMappingManager.class, vmm.getVisualMappingManager());
		CyServiceModule.setService(VisualStyleFactory.class, vmm.getVisualStyleFactory());
		CyServiceModule.setService(RenderingEngineManager.class, vmm.getRenderManager());
		CyServiceModule.setService(CyApplicationManager.class, vmm.getApplicationManager());
		
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		CyServiceModule.setPassthroughMapping(new PassthroughMappingFactory(eventHelper));
		CyServiceModule.setDiscreteMapping(new DiscreteMappingFactory(eventHelper));
		CyServiceModule.setContinuousMapping(new ContinuousMappingFactory(eventHelper));
		
	}
	
	public static CyNetwork[] doImportTask(InputStream input_stream, String collection_name) throws Exception {
		CytoscapeCxNetworkReader reader = new CytoscapeCxNetworkReader(input_stream, collection_name, 
				networkview_factory, network_factory, nts.getNetworkManager(), nts.getRootNetworkFactory());

		reader.run(null);
		
		for (CyNetwork net : reader.getNetworks()) {
			CyNetworkView view = reader.buildCyNetworkView(net);
			assertNotNull("Network view was not created", view);

			Collection<CyNetworkView> views = new HashSet<>();
        	views.add(view);//networkview_factory.createNetworkView(net));
        	when(networkview_manager.getNetworkViews(net)).thenReturn(views);
        	
		}
		return reader.getNetworks();
		
	}
	
	public static void doExport(CyNetwork network, boolean collection, boolean use_cxId, AspectSet aspects, OutputStream out) {
		CxExporter cx_exporter = new CxExporter();
		
		try {
			cx_exporter.writeNetwork(network, collection, use_cxId, aspects, out);
		} catch (IOException e) {
			fail("Failed to export network to CX: " + e.getMessage());
		}
	}
}
