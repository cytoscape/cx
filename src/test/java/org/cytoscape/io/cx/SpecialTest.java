 package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.io.internal.AspectSet;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.DoubleVisualProperty;
import org.cytoscape.view.presentation.property.IntegerVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.ndexbio.cxio.core.CxElementReader2;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.model.cx.NiceCXNetwork;

public class SpecialTest {

	@BeforeClass
	public static void init() {
		TestUtil.init();
	}
	
	@Test
	public void testEdgesAndEdgeAttributesForSingleEdgeNetwork() throws IOException {
		
			// If no CX IDs are set, use SUIDs
			final CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
			final CyNetwork network = network_factory.createNetwork();
			final CyNode node = network.addNode();
			final CyNode nodeb = network.addNode();
			
			final CyEdge edge = network.addEdge(node, nodeb, true);
				
			System.out.println("network edges: " + network.getEdgeCount());
			final ByteArrayOutputStream out = TestUtil.saveNetwork(network, false, false);
			
			final File outf = TestUtil.saveOutputStreamToFile(out, "zero_edges_test_output.cx");
			// TODO: Delete test files on exit
//			outf.deleteOnExit();
			final FileInputStream export_in = new FileInputStream(outf);
			
			final Collection<String> aspects = AspectSet.getAspectNames();
		        
			final Set<AspectFragmentReader> all_readers = new HashSet<>();
		        for (final AspectFragmentReader reader : AspectSet.getAspectFragmentReaders(aspects)) {
		            all_readers.add(reader);
		        }
			
			final CxElementReader2 r = new CxElementReader2(export_in, all_readers, true);
	        final MetaDataCollection preMetaData = r.getPreMetaData();
			final MetaDataCollection postMetaData = r.getPostMetaData();
	      	
			assertEquals( 1,  Stream.concat(
	        	preMetaData != null ? preMetaData.getMetaData().stream() : Stream.empty(), 
	        	postMetaData != null ? postMetaData.getMetaData().stream() : Stream.empty()).filter(
	        		el -> el != null && "edges".equals(el.getName())
	        	).count()
	        );
			assertEquals(1,  Stream.concat(
		        	preMetaData != null ? preMetaData.getMetaData().stream() : Stream.empty(), 
		        	postMetaData != null ? postMetaData.getMetaData().stream() : Stream.empty()).filter(
		        		el -> el != null && "edgeAttributes".equals(el.getName())
		        	).count()
		    );
	}
	
	@Test
	public void testNoEdgesAndEdgeAttributesForZeroEdgeNetwork() throws IOException {
		
			// If no CX IDs are set, use SUIDs
			final CyNetworkFactory network_factory = CyServiceModule.getService(CyNetworkFactory.class);
			final CyNetwork network = network_factory.createNetwork();
			final CyNode node = network.addNode();
			final CyNode nodeb = network.addNode();
			
			//final CyEdge edge = network.addEdge(node, nodeb, true);
				
			System.out.println("network edges: " + network.getEdgeCount());
			final ByteArrayOutputStream out = TestUtil.saveNetwork(network, false, false);
			
			final File outf = TestUtil.saveOutputStreamToFile(out, "zero_edges_test_output.cx");
			// TODO: Delete test files on exit
//			outf.deleteOnExit();
			final FileInputStream export_in = new FileInputStream(outf);
			
			final Collection<String> aspects = AspectSet.getAspectNames();
		        
			final Set<AspectFragmentReader> all_readers = new HashSet<>();
		        for (final AspectFragmentReader reader : AspectSet.getAspectFragmentReaders(aspects)) {
		            all_readers.add(reader);
		        }
			
			final CxElementReader2 r = new CxElementReader2(export_in, all_readers, true);
	        final MetaDataCollection preMetaData = r.getPreMetaData();
			final MetaDataCollection postMetaData = r.getPostMetaData();
	      	
			assertEquals(0,  Stream.concat(
	        	preMetaData != null ? preMetaData.getMetaData().stream() : Stream.empty(), 
	        	postMetaData != null ? postMetaData.getMetaData().stream() : Stream.empty()).filter(
	        		el -> el != null && "edges".equals(el.getName())
	        	).count()
	        );
			assertEquals(0,  Stream.concat(
		        	preMetaData != null ? preMetaData.getMetaData().stream() : Stream.empty(), 
		        	postMetaData != null ? postMetaData.getMetaData().stream() : Stream.empty()).filter(
		        		el -> el != null && "edgeAttributes".equals(el.getName())
		        	).count()
		    );
	}
	
	@Test
	public void testLocationPassthrough() throws IOException {
		File f = TestUtil.getResource("specialCases", "node_location_map.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		Map<Long, Collection<AspectElement>> nodeAspectIn = reader.getNiceCX().getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME);
		Collection<AspectElement> aesIn = nodeAspectIn.get(1018l);
		assertEquals(aesIn.size(), 1);
		CartesianLayoutElement caeIn = (CartesianLayoutElement) aesIn.iterator().next();
		assertEquals(caeIn.getX(), -3204.789794921875, 0);
		assertEquals(caeIn.getY(), 775.8062232824261, 0);
		
		NiceCXNetwork output = TestUtil.getOutput(reader);
		Map<Long, Collection<AspectElement>> nodeAspect = output.getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME);
		
		Collection<AspectElement> aes = nodeAspect.get(1018l);
		assertEquals(aes.size(), 1);
		CartesianLayoutElement cae = (CartesianLayoutElement) aes.iterator().next();
		assertEquals(cae.getX(), -39.129654, 0);
		assertEquals(cae.getY(), 90.83788, 0);
	}
	
	@Test
	public void testGroups() throws IOException {
		File f = TestUtil.getResource("collections", "groups_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		TestUtil.withAspects(reader);
	}
	
	@Test
	public void testMismatchedAttributeTypes() throws IOException {
		File f = TestUtil.getResource("specialCases", "n3_pp.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		try {
			TestUtil.getOutput(reader);
		}catch(IllegalArgumentException e) {
			assertEquals(e.getMessage(), "Cannot set value in column length(class java.lang.Integer) to [1079, 1081] (type class java.util.ArrayList). invalid type: class java.util.ArrayList");
		}
	}
	
	@Test
	public void testWikiPathway() throws IOException {
		File f = TestUtil.getResource("specialCases", "TCA Cycle Nutrient Utilization and Invasiveness of Ovarian Cancer - Homo sapiens.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		TestUtil.withAspects(reader);
	}
	
	@Test
	public void testMapFontSizeToDouble() throws IOException {
		CxReaderWrapper reader = TestUtil.getSubNetwork(TestUtil.getResource("base", "subnetwork.cx"));
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		networks[0].getDefaultNodeTable().createColumn("LabelSize", Double.class, true);
		CyNode node = networks[0].getNodeList().get(0);
		networks[0].getDefaultNodeTable().getRow(node.getSUID()).set("LabelSize", 12.0);
		
		CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);
		Collection<CyNetworkView> views = view_manager.getNetworkViews(networks[0]);
		CyNetworkView view = views.iterator().next();
		VisualMappingManager vmm = CyServiceModule.getService(VisualMappingManager.class);
		VisualStyle style = vmm.getVisualStyle(view);
		ContinuousRange<Integer> range = new ContinuousRange<Integer>(Integer.class, 1, Integer.MAX_VALUE, true, true);
		IntegerVisualProperty ivp = new IntegerVisualProperty(
				new Integer(10), 
				range,
				BasicVisualLexicon.NODE_LABEL_FONT_SIZE.getIdString(), 
				BasicVisualLexicon.NODE_LABEL_FONT_SIZE.getDisplayName(), 
				CyNode.class);
		
		VisualMappingFunction<Double, Integer> pm = CyServiceModule.getPassthroughMapping()
				.createVisualMappingFunction("LabelSize", Double.class, ivp); 
		style.addVisualMappingFunction(pm);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TestUtil.doExport(networks[0], false, true, out);
	}
	
	@Test
	public void testMapDoubleToInteger() throws IOException {
		CxReaderWrapper reader = TestUtil.getSubNetwork(TestUtil.getResource("base", "subnetwork.cx"));
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		networks[0].getDefaultNodeTable().createColumn("LabelSize", Integer.class, true);
		CyNode node = networks[0].getNodeList().get(0);
		networks[0].getDefaultNodeTable().getRow(node.getSUID()).set("LabelSize", 12);
		
		CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);
		Collection<CyNetworkView> views = view_manager.getNetworkViews(networks[0]);
		CyNetworkView view = views.iterator().next();
		VisualMappingManager vmm = CyServiceModule.getService(VisualMappingManager.class);
		VisualStyle style = vmm.getVisualStyle(view);
		ContinuousRange<Double> range = new ContinuousRange<Double>(Double.class, 1.0, Double.MAX_VALUE, true, true);
		DoubleVisualProperty ivp = new DoubleVisualProperty(
				new Double(10), 
				range,
				BasicVisualLexicon.NODE_BORDER_WIDTH.getIdString(), 
				BasicVisualLexicon.NODE_BORDER_WIDTH.getDisplayName(),
				CyNode.class);
		
		VisualMappingFunction<Integer, Double> pm = CyServiceModule.getPassthroughMapping()
				.createVisualMappingFunction("LabelSize", Integer.class, ivp); 
		style.addVisualMappingFunction(pm);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TestUtil.doExport(networks[0], false, true, out);
	}
	
	@Test
	public void testMultiViewSubnet() throws IOException {
		CxReaderWrapper reader = TestUtil.getSubNetwork(TestUtil.getResource("base", "subnetwork.cx"));
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		CyNetworkViewFactory view_factory = CyServiceModule.getService(CyNetworkViewFactory.class);
		CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);
		
		CyNetworkView view2 = view_factory.createNetworkView(networks[0]);
		CyNetworkView view3 = view_factory.createNetworkView(networks[0]);
		view_manager.addNetworkView(view2);
		view_manager.addNetworkView(view3);
				
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TestUtil.doExport(networks[0], false, true, out);
		
		File outf = TestUtil.saveOutputStreamToFile(out, "multiview_test_output.cx");
		FileInputStream export_in = new FileInputStream(outf);
		
		CxReaderWrapper reader2 = TestUtil.INSTANCE.getReader(export_in, null);
		TestUtil.withAspects(reader2);
	}
	
	@Test
	public void testCollectionName() throws IOException {
		CxReaderWrapper reader = TestUtil.getSubNetwork(TestUtil.getResource("base", "subnetwork.cx"));
		NiceCXNetwork niceCx = reader.getNiceCX();
		niceCx.addNetworkAttribute(new NetworkAttributesElement(null, CyNetwork.NAME, "Collection"));
		niceCx.addNetworkAttribute(new NetworkAttributesElement(1l, CyNetwork.NAME, "Network"));

		SubNetworkElement subnet = new SubNetworkElement(1l);
		subnet.addNode(0l);
		subnet.setId(1l);
		NetworkRelationsElement nre = new NetworkRelationsElement(1l, "subnetwork", "Network");
		NetworkRelationsElement nre2 = new NetworkRelationsElement(1l, 2l, "view", "Network view");
		
		
		TestUtil.withAspects(reader, subnet, nre, nre2);
		
	}
}
