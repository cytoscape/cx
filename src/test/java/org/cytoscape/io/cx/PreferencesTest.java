package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class PreferencesTest {

	@Before
	public void init() {
		TestUtil.init();
	}
	
	@Test
	public void testNeverCreateViewUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("never");
		File f = TestUtil.getResource("collections", "gal_filtered_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);

		assertEquals(0, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	@Test
	public void testNeverCreateViewOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("never");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("200");
		
		File f = TestUtil.getResource("collections", "gal_filtered_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);

		assertEquals(0, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	@Test
	public void testAutoCreateViewUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("auto");
	
		File f = TestUtil.getResource("collections", "gal_filtered_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(1, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	
	@Test
	public void testAutoCreateViewOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("auto");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("200");
		File f = TestUtil.getResource("collections", "gal_filtered_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(0, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	
	@Test
	public void testAlwaysCreateViewUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		File f = TestUtil.getResource("collections", "gal_filtered_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(1, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	@Test
	public void testAlwaysCreateViewOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("200");
		File f = TestUtil.getResource("collections", "gal_filtered_1.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		assertEquals(1, TestUtil.INSTANCE.getCyNetworkViewManager().getNetworkViewSet().size());
	}
	
	
	
	@Test
	public void testAutoLayoutOverThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.LARGE_LAYOUT_THRESHOLD_PROPERTY))).thenReturn("200");
		
		File f = TestUtil.getResource("collections", "gal_filtered_1_no_layout.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		CyLayoutAlgorithmManager layoutManager = CyServiceModule.getService(CyLayoutAlgorithmManager.class);
		verify(layoutManager, times(1)).getLayout(Mockito.eq("grid"));
		verify(layoutManager, times(0)).getLayout(Mockito.eq("force-directed"));
	}

	
	@Test
	public void testAutoLayoutUnderThreshold() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.LARGE_LAYOUT_THRESHOLD_PROPERTY))).thenReturn("10000");
		
		File f = TestUtil.getResource("collections", "gal_filtered_1_no_layout.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		CyLayoutAlgorithmManager layoutManager = CyServiceModule.getService(CyLayoutAlgorithmManager.class);
		
		verify(layoutManager, times(0)).getLayout(Mockito.eq("grid"));
		verify(layoutManager, times(1)).getLayout(Mockito.eq("force-directed"));
	}
	
	@Test
	public void testNeverLayout() throws IOException {
		Properties propertiesMock = TestUtil.INSTANCE.getPropertiesMock();
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("always");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.LARGE_LAYOUT_THRESHOLD_PROPERTY))).thenReturn("200");
		when(propertiesMock.getProperty(Mockito.eq(CxPreferences.APPLY_LAYOUT_PROPERTY))).thenReturn("never");
		
		
		File f = TestUtil.getResource("collections", "gal_filtered_1_no_layout.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		CyNetwork[] networks = TestUtil.loadNetworks(reader);
		
		CyLayoutAlgorithmManager layoutManager = CyServiceModule.getService(CyLayoutAlgorithmManager.class);
		verify(layoutManager, times(0)).getLayout(Mockito.eq("grid"));
		verify(layoutManager, times(0)).getLayout(Mockito.eq("force-directed"));
	}

	
	
}
