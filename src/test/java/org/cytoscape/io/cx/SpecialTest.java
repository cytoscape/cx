package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
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
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.model.cx.NiceCXNetwork;

public class SpecialTest {

	@BeforeClass
	public static void init() {
		TestUtil.init();
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
		TestUtil.doExport(networks[0], false, true, null, out);
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
		TestUtil.doExport(networks[0], false, true, null, out);
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
		TestUtil.doExport(networks[0], false, true, null, out);
		
		File outf = TestUtil.saveOutputStreamToFile(out, "multiview_test_output.cx");
		FileInputStream export_in = new FileInputStream(outf);
		
		CxReaderWrapper reader2 = TestUtil.INSTANCE.getReader(export_in, null);
		TestUtil.withAspects(reader2);
	}
}
