package org.cytoscape.io.cx.helpers;

import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphicsmgr.internal.CustomGraphicsManagerImpl;
import org.cytoscape.ding.impl.BendFactoryImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxNetworkReader;
import org.cytoscape.io.internal.nicecy.NiceCyNetwork;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.VisualMappingManagerImpl;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.mockito.Mockito;


public class CyPropertiesMock{

	public static Properties init(CyServiceRegistrar serviceRegistrar) {	
		CyProperty cyProps = mock(CyProperty.class);
		
		Properties props = mock(Properties.class);
	
		setDefaultProperties(props);
		
		when(cyProps.getProperties()).thenReturn(props);
		when(serviceRegistrar.getService(Mockito.eq(CyProperty.class), Mockito.eq("(cyPropertyName=cytoscape3.props)"))).thenReturn(cyProps);
		
		CyServiceModule.setService(CyProperty.class, cyProps);
		return props;
	}
	
	public static void setDefaultProperties(Properties props) {
		when(props.getProperty(Mockito.eq(CxPreferences.VIEW_THRESHOLD))).thenReturn("300000");
		when(props.getProperty(Mockito.eq(CxPreferences.CREATE_VIEW_PROPERTY))).thenReturn("auto");
	}
}