package org.cytoscape.io.cx.helpers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.DummyCyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.internal.CyGroupFactoryImpl;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.LockedVisualPropertiesManager;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReader;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
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
import org.cytoscape.work.swing.DialogTaskManager;

public class TestUtil {
	private static Logger logger = Logger.getLogger("TestUtil");
	private static final boolean SAVE_CX_FILES = true;
	private static final AspectSet aspects = AspectSet.getCytoscapeAspectSet();
	
	private static NetworkTestSupport nts = new NetworkTestSupport();
	private static CyNetworkFactory network_factory = nts.getNetworkFactory();
	private static NetworkViewTestSupport nvts = new NetworkViewTestSupport();
	private static CyNetworkViewFactory networkview_factory = nvts.getNetworkViewFactory();

	private static CyGroupManagerImpl group_manager;
	private static CyNetworkViewManager networkview_manager = mock(CyNetworkViewManager.class);

	private static VisualMappingMock vmm = new VisualMappingMock();

	final SynchronousTaskManager<?> synchronousTaskManager = mock(SynchronousTaskManager.class);

	private static void initGroups() {
		final DummyCyEventHelper eventHelper = new DummyCyEventHelper();
		final VisualMappingManager vmMgr = mock(VisualMappingManager.class);
		final CyNetworkViewManager netViewMgr = mock(CyNetworkViewManager.class);
		
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		
		group_manager = new CyGroupManagerImpl(serviceRegistrar, eventHelper);
		final LockedVisualPropertiesManager lvpMgr = new LockedVisualPropertiesManager(serviceRegistrar);
		CyGroupFactory group_factory = new CyGroupFactoryImpl(group_manager, lvpMgr, eventHelper);
		
		CyServiceModule.setService(CyGroupManager.class, group_manager);
		CyServiceModule.setService(CyGroupFactory.class, group_factory);
	}
	
	public static void initServices() {
		CyServiceModule.setService(CyNetworkViewFactory.class, networkview_factory);
		CyServiceModule.setService(CyNetworkViewManager.class, networkview_manager);
		CyServiceModule.setService(CyNetworkFactory.class, network_factory);
		CyServiceModule.setService(CyNetworkManager.class, nts.getNetworkManager());
		CyServiceModule.setService(CyRootNetworkManager.class, nts.getRootNetworkFactory());
		
		CyServiceModule.setService(SynchronousTaskManager.class, mock(SynchronousTaskManager.class));
		
		CyServiceModule.setService(DialogTaskManager.class, mock(DialogTaskManager.class));
		
		CyServiceModule.setService(VisualMappingManager.class, vmm.getVisualMappingManager());
		CyServiceModule.setService(VisualStyleFactory.class, vmm.getVisualStyleFactory());
		CyServiceModule.setService(RenderingEngineManager.class, vmm.getRenderManager());
		CyServiceModule.setService(CyApplicationManager.class, vmm.getApplicationManager());
		

		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		CyServiceModule.setPassthroughMapping(new PassthroughMappingFactory(eventHelper));
		CyServiceModule.setDiscreteMapping(new DiscreteMappingFactory(eventHelper));
		CyServiceModule.setContinuousMapping(new ContinuousMappingFactory(eventHelper));
		
		initGroups();

	}
	
	public static File getResource(String... dir) {
		File file = new File("src/test/resources/");
		for (String s : dir) {
			file = new File(file, s);
		}
		return file;
	}
	
	public static CyNetwork[] loadFile(File path, String collection_name) throws IOException {
		logger.info("Testing round trip of " + path.getParent() + " " + path.getName());
		// Import to Cytoscape and compare with CX Network(s)
		InputStream import_input_stream = new FileInputStream(path);
		CyNetwork[] networks;
		try {
			networks = TestUtil.doImportTask(import_input_stream, collection_name);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
		import_input_stream.close();

		return networks;
	}
	
	public static InputStream saveNetwork(CyNetwork network, boolean collection, boolean useCxId) throws IOException {
		if (collection && useCxId) {
			logger.info("Not using cxId for collection");
			useCxId = false;
		}
		
		String name = CxUtil.getNetworkName(network);
		logger.info("Exporting " + name + " as " + (collection ? "collection" : "subnetwork"));
		File output = getResource("output");
		if (!output.exists()) {
			output.mkdir();
		}else {
			for (File f :output.listFiles()) {
				f.delete();
			}
		}
		InputStream export_input_stream = null;
		if (SAVE_CX_FILES) {
			File f = new File(output, "CX_TEST_OUTPUT_" + name + ".cx");
			logger.info("Creating temp file at " + f.getAbsolutePath());
			FileOutputStream out_stream = new FileOutputStream(f);
			TestUtil.doExport(network, collection, useCxId, aspects, out_stream);
			export_input_stream = new FileInputStream(f);
		} else {
			ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
			TestUtil.doExport(network, collection, useCxId, aspects, out_stream);
			export_input_stream = TestUtil.pipe(out_stream);
		}
		
		return export_input_stream;
	}

	public static CyNetwork[] doImportTask(InputStream input_stream, String collection_name) throws Exception {
		CytoscapeCxNetworkReader reader = new CytoscapeCxNetworkReader(input_stream, collection_name,
				networkview_factory, network_factory, nts.getNetworkManager(), nts.getRootNetworkFactory());

		reader.run(null);
		
		for (CyNetwork net : reader.getNetworks()) {
			CyNetworkView view = reader.buildCyNetworkView(net);
			assertNotNull("Network view was not created", view);

			Collection<CyNetworkView> views = new HashSet<>();
			views.add(view);// networkview_factory.createNetworkView(net));
			when(networkview_manager.getNetworkViews(net)).thenReturn(views);
		}
		
		return reader.getNetworks();
	}
	
	public static ByteArrayInputStream pipe(ByteArrayOutputStream os) throws IOException {
		return new ByteArrayInputStream(os.toByteArray());
	}

	public static void doExport(CyNetwork network, boolean writeSiblings, boolean useCxId, AspectSet aspects,
			OutputStream out) {
		CxExporter cx_exporter = new CxExporter(network, writeSiblings, useCxId);

		try {
			cx_exporter.writeNetwork(aspects, out);
		} catch (IOException e) {
			fail("Failed to export network to CX: " + e.getMessage());
		}
	}
}
