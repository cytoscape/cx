package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.io.internal.CxPreferences;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.internal.nicecy.NiceCyRootNetwork;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public final class ViewMaker {
	private static final Logger logger = LoggerFactory.getLogger("CX ViewMaker");
	
    public static final Pattern DIRECT_NET_PROPS_PATTERN = Pattern
            .compile("GRAPH_VIEW_(ZOOM|CENTER_(X|Y))|NETWORK_(WIDTH|HEIGHT|SCALE_FACTOR|CENTER_(X|Y|Z)_LOCATION)");
    
    public static final VisualMappingFunctionFactory vmf_factory_c = CyServiceModule.getContinuousMapping();
    public static final VisualMappingFunctionFactory vmf_factory_d = CyServiceModule.getDiscreteMapping();
    public static final VisualMappingFunctionFactory vmf_factory_p = CyServiceModule.getPassthroughMapping();
    
    // TODO: Cannot handle passthrough (or other?) mappings to list columns
    
    private static boolean applyCartesianLayout(CyNetworkView view, 
    		final CyNode node,
    		final CartesianLayoutElement position) {
        
        if (position == null || view == null) {
        	return false;
        }
    	if (position != null) {
            final View<CyNode> node_view = view.getNodeView(node);
            if (node_view != null) {
                node_view.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, position.getX());
                node_view.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, position.getY());
                if (position.isZset()) {
                    node_view.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION,
                                                position.getZ());
                }
            }
        }
        
        return true;
    }
    
    public static CyNetworkView applyStyle (
    		VisualStyle style, 
    		CyNetworkView network_view,
    		String layout, boolean fitContent) {
        
        if( layout != null && CxPreferences.getApplyLayout() != CxPreferences.ApplyLayoutEnum.NEVER)
        {
        	
        	CyLayoutAlgorithmManager layout_manager = CyServiceModule.getService(CyLayoutAlgorithmManager.class);
            CyLayoutAlgorithm algorithm = layout_manager.getLayout(layout);
            
            TaskIterator ti = algorithm.createTaskIterator(network_view, algorithm.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
            
            DialogTaskManager task_manager = CyServiceModule.getService(DialogTaskManager.class);
            task_manager.execute(ti);
            network_view.updateView();
        }
        
        style.apply(network_view);
        network_view.updateView();
        
    /*    CyNetworkViewManager view_manager = CyServiceModule.getService(CyNetworkViewManager.class);
        
        view_manager.addNetworkView(network_view); */
        
        if (fitContent) {
        	network_view.fitContent();
        } else {
        	network_view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION, style.getDefaultValue(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION));
          network_view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION, style.getDefaultValue(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION));
          network_view.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION, style.getDefaultValue(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION));

          network_view.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR, style.getDefaultValue(BasicVisualLexicon.NETWORK_SCALE_FACTOR));
        }
        
        return network_view;
        
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final static void addContinuousMapping(final VisualStyle style,
                                                  final VisualProperty vp,
                                                  final StringParser sp,
                                                  final String col,
                                                  final String type,
                                                  final Class<?> type_class) {
        final ContinuousMapping cmf = (ContinuousMapping) vmf_factory_c
                .createVisualMappingFunction(col, type_class, vp);

        if (cmf != null) {
            int counter = 0;
            while (true) {
                final String ov = sp.get("OV=" + counter);
                if (ov == null) {
                    break;
                }
                final String l = sp.get("L=" + counter);
                final String e = sp.get("E=" + counter);
                final String g = sp.get("G=" + counter);
                if ((l != null) && (e != null) && (g != null)) {
                    final Object lp = vp.parseSerializableString(l);
                    final Object ep = vp.parseSerializableString(e);
                    final Object gp = vp.parseSerializableString(g);
                    
                    // Some integer values exported as double
                    Object value = vp.parseSerializableString(ov);
                    
                    try {
                    	value = ViewMaker.toContinuousPointValue(ov, type);
                    }catch(NumberFormatException err) {
                    	
                    }
                    
                    if ((lp != null) && (ep != null) && (gp != null) && (value != null)) {
                        final BoundaryRangeValues point = new BoundaryRangeValues(lp, ep, gp);
                        cmf.addPoint(value, point);
                    }
                    else {
                        logger.warn("could not parse from string in continuous mapping for col '" + col + "'");
                    }
                }
                else {
                    logger.warn("could not get expected values in continuous mapping for col '" + col + "'");
                }
                counter++;
            }
            style.addVisualMappingFunction(cmf);
        }
        else {
            logger.warn("could not create continuous mapping for col '" + col + "'");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final static void addDiscreteMapping(final VisualStyle style,
                                                final VisualProperty vp,
                                                final StringParser sp,
                                                final String col,
                                                final String type,
                                                final Class<?> type_class) {
        final DiscreteMapping dmf = (DiscreteMapping) vmf_factory_d.createVisualMappingFunction(col, type_class, vp);
        try {
        	if (dmf == null) {
        		throw new RuntimeException("createVisualMappingFunction returned null");
        	}
	        int counter = 0;
	        while (true) {
	            final String k = sp.get("K=" + counter);
	            if (k == null) {
	                break;
	            }
	            final String v = sp.get("V=" + counter);
            
                if (v == null) {
                	logger.info("error: discrete mapping function string is corrupt for ");
                }
                Object key = ViewMaker.toTypeValue(k, type);
                try {
                	final Object pv = vp.parseSerializableString(v);
                	if (pv != null) {
                        dmf.putMapValue(key, pv);
                    }
                    else {
                        logger.info("Could not parse serializable string from discrete mapping value '" + v
                                + "'");
                        dmf.putMapValue(key, pv);
                    }
                }catch(NullPointerException e) {
                	throw new RuntimeException("Unable to parse serializable string " + key + " " + type + " from " + v);
                }
            	
	            counter++;
	        }
	        style.addVisualMappingFunction(dmf);
        }catch(RuntimeException e) {
        	logger.info("Failed to create discrete mapping for " + col + ": " + e.getMessage());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public final static void addPasstroughMapping(final VisualStyle style,
                                                  final VisualProperty vp,
                                                  final String col,
                                                  final Class<?> type_class) {
        
    	try {
    		final PassthroughMapping pmf = (PassthroughMapping) 
    				vmf_factory_p.createVisualMappingFunction(col, type_class, vp);
            style.addVisualMappingFunction(pmf);
    	} catch (NullPointerException e){
            logger.warn("could not create passthrough mapping for col '" + col + "'");

        }
    }

    public final static String createTitleForNewVisualStyle(final String network_collection_name) {
        String viz_style_title = "new-Style";
        if (network_collection_name != null) {
            if (network_collection_name.toLowerCase().endsWith(".cx")) {
                viz_style_title = String.format("%s-Style", network_collection_name.substring(0,
                                                                                              network_collection_name
                                                                                                      .length() - 3));
            }
            else {
                viz_style_title = String.format("%s-Style", network_collection_name);
            }
        }
        return viz_style_title;
    }

    public final static void removeVisualStyle(final String viz_style_title,
                                               final VisualMappingManager visual_mapping_manager) {
        final Iterator<VisualStyle> it = visual_mapping_manager.getAllVisualStyles().iterator();
        while (it.hasNext()) {
            final VisualStyle vs = it.next();
            if (vs.getTitle().equalsIgnoreCase(viz_style_title)) {
                visual_mapping_manager.removeVisualStyle(vs);
                break;
            }
        }
    }

    public final static boolean containsVisualStyle(final String viz_style_title,
                                                    final VisualMappingManager visual_mapping_manager) {
        final Iterator<VisualStyle> it = visual_mapping_manager.getAllVisualStyles().iterator();
        while (it.hasNext()) {
            final VisualStyle vs = it.next();
            if (vs.getTitle().equalsIgnoreCase(viz_style_title)) {
                return true;
            }
        }
        return false;
    }

    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private static void parseVisualProperty(final String key, 
    		final String value, 
    		final VisualLexicon lexicon, 
    		final VisualStyle style, 
    		final Class<? extends CyIdentifiable> my_class) throws IOException {
        

       
            final VisualProperty vp = lexicon.lookup(my_class, key);
            if (vp != null) {
                Object parsed_value = null;
                try {
                    parsed_value = vp.parseSerializableString(value);
                    if (parsed_value != null) {
                    	style.setDefaultValue(vp, parsed_value);
                    }
                }
                catch (final Exception e) {
                    logger.info("Could not parse serializable string from '" + value
                            + "' for '" + key + "'");
                }
            }
        }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final static void setDefaultVisualPropertiesAndMappings(final VisualLexicon lexicon,
                                                                   final CyVisualPropertiesElement visProp,
                                                                   final VisualStyle style,
                                                                   final Class my_class) {

    	if (visProp == null) {
    		return;
    	}
    	
        final SortedMap<String, String> props = visProp.getProperties();
        final SortedMap<String, Mapping> maps = visProp.getMappings();
        final SortedMap<String, String> dependencies = visProp.getDependencies();
        
        if (props != null) {
        	for (final Map.Entry<String, String> entry : props.entrySet()) {
	        	try {
					parseVisualProperty(entry.getKey(), entry.getValue(), lexicon, style, my_class);
				} catch (IOException e) {
					logger.warn("Failed to parse visual property: " + e);
				}
        	}
        }
        

        if (maps != null) {
            for (final Entry<String, Mapping> entry : maps.entrySet()) {
            	try {
            		parseVisualMapping(entry.getKey(), entry.getValue(), lexicon, style, my_class);
            	}catch (IOException e) {
            		logger.warn("Failed to parse visual mapping: " + e);
            	}
                
            }
        }
        
        if (dependencies != null) {
            for (final Entry<String, String> entry : dependencies.entrySet()) {
            	try {
            		parseVisualDependency(entry.getKey(), entry.getValue(), style);
            	} catch(IOException e) {
            		logger.warn("Failed to parse visual dependency: " + e);
            	}
            }
        }
    }

    private static void parseVisualDependency(final String k, 
    		final String v, 
    		final VisualStyle style) throws IOException {
    	
    	if (k == null || v == null) {
    		return;
    	}
    	for (final VisualPropertyDependency<?> d : style.getAllVisualPropertyDependencies()) {
            if (d.getIdString().equals(k)) {
                try {
                    d.setDependency(Boolean.parseBoolean(v));
                }
                catch (final Exception e) {
                    throw new IOException("could not parse boolean from '" + v + "'");
                }
            }
        }
	}

	@SuppressWarnings("rawtypes")
	private static void parseVisualMapping(
    		final String mapping_target, //VP name
    		final Mapping mapping, 
    		final VisualLexicon lexicon, 
    		final VisualStyle style, 
    		final Class<? extends CyIdentifiable> my_class) throws IOException {
        final String mapping_type = mapping.getType();
        final VisualProperty vp = lexicon.lookup(my_class, mapping_target);
        final StringParser sp = new StringParser(mapping.getDefinition());
        final String col = sp.get(CxUtil.VM_COL);
        final String type = sp.get(CxUtil.VM_TYPE);
        final Class<?> type_class = ViewMaker.toClass(type);
        if (vp == null) {
        	return;
        }
        
        if (mapping_type.equals(CxUtil.PASSTHROUGH)) {
            addPasstroughMapping(style, vp, col, type_class);
        }
        else if (mapping_type.equals(CxUtil.CONTINUOUS)) {
            addContinuousMapping(style, vp, sp, col, type, type_class);
        }
        else if (mapping_type.equals(CxUtil.DISCRETE)) {
            addDiscreteMapping(style, vp, sp, col, type, type_class);
        }
        else {
            throw new IOException("unknown mapping type: " + mapping_type);
        }
	}

	public final static void setEdgeVisualProperties(final CyNetworkView view,
                                                     final VisualLexicon lexicon,
                                                     final CyEdge edge,
                                                     final List<CyVisualPropertiesElement> edgeProps) {

    	
    	if (edgeProps == null) {
    		return;
    	}
    	
        edgeProps.forEach(vpe -> {
            if (vpe != null) {
                final SortedMap<String, String> props = vpe.getProperties();
                if (props != null) {
                    final View<CyEdge> v = view.getEdgeView(edge);
                    ViewMaker.setVisualProperties(lexicon, props, v, CyEdge.class);
                }
            }
        });
    
    }

    public final static void setNodeVisualProperties(final CyNetworkView view,
                                                     final VisualLexicon lexicon,
                                                     final CyNode node,
                                                     final List<CyVisualPropertiesElement> nodeProps) {
    	
    	
    	if (nodeProps == null) {
    		return;
    	}
    	
        nodeProps.forEach(vpe -> {
            final SortedMap<String, String> props = vpe.getProperties();
            if (props != null) {
                final View<CyNode> v = view.getNodeView(node);
                ViewMaker.setVisualProperties(lexicon, props, v, CyNode.class);
            }
        });
        
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final static void setVisualProperties(final VisualLexicon lexicon,
                                                 final SortedMap<String, String> props,
                                                 final View view,
                                                 final Class my_class) {
        if (props == null) {
        	return;
        }
        for (final Map.Entry<String, String> entry : props.entrySet()) {
            final VisualProperty vp = lexicon.lookup(my_class, entry.getKey());

            if (vp == null) {
            	continue;
            }
//        	try	{
                final Object parsed_value = vp.parseSerializableString(entry.getValue());
                if (parsed_value != null) {
                    if (ViewMaker.shouldSetAsLocked(vp)) {
                        view.setLockedValue(vp, parsed_value);
                    }
                    else {
                    	view.setVisualProperty(vp, parsed_value);
                    }
                }
//        	}catch(NullPointerException e) {
//        		e.printStackTrace();
//        		logger.info("Failed to parse string for " + entry.getKey() + ": " + entry.getValue());
//        	}
        }
    }

    @SuppressWarnings("rawtypes")
    public final static boolean shouldSetAsLocked(final VisualProperty vp) {
        if (vp.getTargetDataType() == CyNode.class) {
            if ((vp == BasicVisualLexicon.NODE_X_LOCATION) || (vp == BasicVisualLexicon.NODE_Y_LOCATION)
                    || (vp == BasicVisualLexicon.NODE_Z_LOCATION)) {
                return false;
            }
        }
        else if (vp.getTargetDataType() == CyNetwork.class) { // TODO //FIXME
            return !DIRECT_NET_PROPS_PATTERN.matcher(vp.getIdString()).matches();
        }
        return true;
    }

    public final static Class<?> toClass(final String type) {
        if (type.equals("string")) {
            return String.class;
        }
        else if (type.equals("integer")) {
            return Integer.class;
        }
        else if (type.equals("long")) {
            return Long.class;
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.class;
        }
        else if (type.equals("boolean")) {
            return Boolean.class;
        } else if ( type.startsWith("list_of_"))
        	return List.class;
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

    public final static Object toContinuousPointValue(final String s, final String type) throws NumberFormatException {
        if (type.equals("string")) {
            return s;
        }
        else if (type.equals("integer")) {
            return Double.valueOf(s);
        }
        else if (type.equals("long")) {
            return Double.valueOf(s);
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.valueOf(s);
        }
        else if (type.equals("boolean")) {
            return Boolean.valueOf(s);
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }
    
    public final static Object toTypeValue(final String s, final String type) throws NumberFormatException {
        if (type.equals("string")) {
            return s;
        }
        else if (type.equals("integer")) {
            return Double.valueOf(s).intValue();
        }
        else if (type.equals("long")) {
            return Double.valueOf(s).longValue();
        }
        else if (type.equals("double") || type.equals("float")) {
            return Double.valueOf(s);
        }
        else if (type.equals("boolean")) {
            return Boolean.valueOf(s);
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type + "'");
        }
    }

	public static void makeView(CyNetworkView view,
			NiceCyRootNetwork niceCy,
			Map<Long, CartesianLayoutElement> cartesianLayout,
			Map<String, CyVisualPropertiesElement> visualProperties, 
			Map<Long, List<CyVisualPropertiesElement>> nodeBypass,
			Map<Long, List<CyVisualPropertiesElement>> edgeBypass) {
		
		final VisualMappingManager visual_mapping_manager = CyServiceModule.getService(VisualMappingManager.class);
    	final VisualStyleFactory visual_style_factory = CyServiceModule.getService(VisualStyleFactory.class);
    	final RenderingEngineManager rendering_engine_manager = CyServiceModule.getService(RenderingEngineManager.class);
    	
    	final long t0 = System.currentTimeMillis();
    	String doLayout = view.getEdgeViews().size() < CxPreferences.getLargeLayoutThreshold() ? "force-directed" : "grid";
    	
        final boolean have_default_visual_properties = 
        		(visualProperties != null) ||
                (nodeBypass != null) || 
                (edgeBypass != null);
                
        VisualStyle new_visual_style = visual_mapping_manager.getDefaultVisualStyle();
        if (have_default_visual_properties) {
            int counter = 1;
            final VisualStyle default_visual_style = visual_mapping_manager.getDefaultVisualStyle();
            new_visual_style = visual_style_factory.createVisualStyle(default_visual_style);
            
            CyRootNetwork root = ((CySubNetwork) view.getModel()).getRootNetwork();
            String name = root.getRow(root).get(CyNetwork.NAME, String.class);
            final String viz_style_title_base = createTitleForNewVisualStyle(name);
            
            String viz_style_title = viz_style_title_base;
            while (counter < 101) {
                if (ViewMaker.containsVisualStyle(viz_style_title, visual_mapping_manager)) {
                    viz_style_title = viz_style_title_base + "-" + counter;
                }
                counter++;
            }
            //ViewMaker.removeVisualStyle(viz_style_title, visual_mapping_manager);
            new_visual_style.setTitle(viz_style_title);
        }
        	
        final VisualLexicon lexicon = rendering_engine_manager.getDefaultVisualLexicon();

        final CyVisualPropertiesElement networkVisualProperties = visualProperties.get("network");
        
        ViewMaker.setDefaultVisualPropertiesAndMappings(lexicon,
        													networkVisualProperties,
			                                                new_visual_style,
			                                                CyNetwork.class);

        final boolean fitContent = networkVisualProperties != null
        		&& !networkVisualProperties.getProperties().containsKey(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION.getIdString())
        		&& !networkVisualProperties.getProperties().containsKey(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION.getIdString())
        		&& !networkVisualProperties.getProperties().containsKey(BasicVisualLexicon.NETWORK_SCALE_FACTOR.getIdString());
 	
        System.out.println("FitContent = " + fitContent);
        
        ViewMaker.setDefaultVisualPropertiesAndMappings(lexicon,
                                                        visualProperties.get("nodes:default"),
                                                        new_visual_style,
                                                        CyNode.class);
    
    
        ViewMaker.setDefaultVisualPropertiesAndMappings(lexicon,
                                                        visualProperties.get("edges:default"),
                                                        new_visual_style,
                                                        CyEdge.class);
        
        
        nodeBypass.forEach((suid, props) -> {
        	CyNode node = niceCy.getNode(suid);
        	ViewMaker.setNodeVisualProperties(view, lexicon, node, props);	
        });
        
        edgeBypass.forEach((suid, props) -> {
        	CyEdge edge = niceCy.getEdge(suid);
        	ViewMaker.setEdgeVisualProperties(view, lexicon, edge, props);	
        });
        
        
        // If there is a Cartesian layout for the view, do not apply a layout
        for (Long suid : cartesianLayout.keySet()) {
        	CyNode node = niceCy.getNode(suid);
        	if (applyCartesianLayout(view, node, cartesianLayout.get(suid))) {
        		doLayout = null;
        	}
        }
        
        if (have_default_visual_properties) {
        	// Simply add & assign style.  VMM automatically apply this later.
            visual_mapping_manager.addVisualStyle(new_visual_style);
            visual_mapping_manager.setVisualStyle(new_visual_style, view);
        }
        
        ViewMaker.applyStyle(new_visual_style, view, doLayout, fitContent);
        
        if (Settings.INSTANCE.isTiming()) {
            TimingUtil.reportTimeDifference(t0, "time to make view", -1);
        }
		
	}

}
