package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.cytoscape.io.cx.helpers.CyPropertiesMock;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.io.internal.AspectSet;
import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.ndexbio.cxio.core.CxElementReader2;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.model.cx.NiceCXNetwork;

public class PreferencesTest {

	@Before
	public void init() {
		TestUtil.init();
	}
	
	@Test
	public void testNeverCreateViewUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("never");
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);

		assertEquals(0, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	@Test
	public void testNeverCreateViewOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("never");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("5000");
		
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);

		assertEquals(0, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	@Test
	public void testAutoCreateViewUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("auto");
	
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(1, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	
	@Test
	public void testAutoCreateViewOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("auto");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("5000");
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(0, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	
	@Test
	public void testAlwaysCreateViewUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(1, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	@Test
	public void testAlwaysCreateViewOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("5000");
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(1, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	
	
	@Test
	public void testLayoutOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.LARGE_LAYOUT_THRESHOLD_PROPERTY))).thenReturn("3000");
		
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		CyLayoutAlgorithmManager layoutManager = CyServiceModule.getService(CyLayoutAlgorithmManager.class);
		verify(layoutManager.getLayout("grid"), times(1));
	}
	
	@Test
	public void testLayoutUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.LARGE_LAYOUT_THRESHOLD_PROPERTY))).thenReturn("10000");
		
		File f = TestUtil.getResource("collections", "c_elegans.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		CyLayoutAlgorithmManager layoutManager = CyServiceModule.getService(CyLayoutAlgorithmManager.class);
		
		verify(layoutManager.getLayout("force-directed"), times(1));
	
	}
}
