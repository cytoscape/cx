package org.cytoscape.io.internal;

import java.util.Properties;

import org.cytoscape.property.CyProperty;

public class CxPreferences {
	public static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 3000;
	
	public static int getViewThreshold() {
		return getIntegerProperty(VIEW_THRESHOLD, DEF_VIEW_THRESHOLD);
	}
	
	public static final String CREATE_VIEW_PROPERTY = "cx.createView";
	
	public enum CreateViewEnum
	{
	    ALWAYS,
	    AUTO,
	    NEVER
	}
	
	public static CreateViewEnum getCreateView() {
		final String property = getProperty(CREATE_VIEW_PROPERTY);
		System.out.println("CREATE_VIEW_PROPERTY=" + property);
		return CreateViewEnum.ALWAYS.toString().toLowerCase().equals(property) 
				? CreateViewEnum.ALWAYS 
				: CreateViewEnum.NEVER.toString().toLowerCase().equals(property) 
				  ? CreateViewEnum.NEVER 
				  : CreateViewEnum.AUTO;
	}
	
	public static final String APPLY_LAYOUT_PROPERTY = "cx.applyLayout";
	
	public enum ApplyLayoutEnum
	{
	    AUTO,
	    NEVER
	}
	
	public static ApplyLayoutEnum getApplyLayout() {
		final String property = getProperty(APPLY_LAYOUT_PROPERTY);
		return ApplyLayoutEnum.NEVER.toString().toLowerCase().equals(property) 
				  ? ApplyLayoutEnum.NEVER 
				  : ApplyLayoutEnum.AUTO;
	}
	
	public static final String LARGE_LAYOUT_THRESHOLD_PROPERTY = "cx.largeLayoutThreshold";
	
	public static final int DEF_LARGE_LAYOUT_THRESHOLD = 25000;
	
	public static Integer getLargeLayoutThreshold() {
		return getIntegerProperty(LARGE_LAYOUT_THRESHOLD_PROPERTY, DEF_LARGE_LAYOUT_THRESHOLD);
	}
	
	private static String getProperty(String key) {
		final Properties props = (Properties) CyServiceModule.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		return props.getProperty(key);
	}
	
	
	private static Integer getIntegerProperty(String key, Integer defaultValue) {
		final String property = getProperty(key);

		try {
			return Integer.parseInt(property);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
