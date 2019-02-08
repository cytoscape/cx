package org.cytoscape.io.internal;

import java.util.HashMap;

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;

public class CyServiceModule {
	
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
