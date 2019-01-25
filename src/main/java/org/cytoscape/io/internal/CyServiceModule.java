package org.cytoscape.io.internal;

import java.util.HashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;

public class CyServiceModule {

	public CyServiceModule(
			CyApplicationManager application_manager,
			CyNetworkManager network_manager,
	        CyNetworkFactory network_factory,
	        CyRootNetworkManager root_network_manager,
			CyNetworkViewManager networkview_manager,
	        CyNetworkViewFactory networkview_factory,
	        CyGroupManager group_manager,
	        CyGroupFactory group_factory,
	        CyLayoutAlgorithmManager layout_manager,
	        VisualMappingManager visual_mapping_manager,
	        RenderingEngineManager rendering_engine_manager,
	        VisualStyleFactory visual_style_factory,
	        DialogTaskManager task_manager) {
		
	}
	
	
	private static HashMap<Class<?>, Object> services = new HashMap<Class<?>, Object>();
	
	private static VisualMappingFunctionFactory continuous, passthrough, discrete;
	
	@SuppressWarnings("unchecked")
	public static final <S> S getService(Class<S> serviceClass) {
		if (!services.containsKey(serviceClass)) {
			throw new IllegalArgumentException("Failed to initialize service: " + serviceClass.getName());
		}
		return (S) services.get(serviceClass);
	}
	public static void setService(Class<?> serviceClass, Object service) {
		services.put(serviceClass, service);
	}
	
	public static VisualMappingFunctionFactory getPassthroughMapping() {
		return passthrough;
	}
	public static void setPassthroughMapping(VisualMappingFunctionFactory vmff) {
		passthrough = vmff;
	}
	
	public static VisualMappingFunctionFactory getContinuousMapping() {
		return continuous;
	}
	public static void setContinuousMapping(VisualMappingFunctionFactory vmff) {
		continuous = vmff;
	}
	
	public static VisualMappingFunctionFactory getDiscreteMapping() {
		return discrete;
	}
	public static void setDiscreteMapping(VisualMappingFunctionFactory vmff) {
		discrete = vmff;
	}

}
