package org.cytoscape.io.cx.helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
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