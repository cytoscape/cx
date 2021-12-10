package org.cytoscape.io.internal.cx_writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ndexbio.cx2.aspect.element.core.CxEdgeBypass;
import org.ndexbio.cx2.aspect.element.core.CxNodeBypass;
import org.ndexbio.cx2.aspect.element.core.MappingDefinition;
import org.ndexbio.cx2.aspect.element.core.VPMappingType;
import org.ndexbio.cx2.aspect.element.core.VisualPropertyMapping;
import org.ndexbio.cx2.aspect.element.core.VisualPropertyTable;
import org.ndexbio.cx2.aspect.element.cytoscape.VisualEditorProperties;
import org.ndexbio.cx2.converter.CXToCX2VisualPropertyConverter;
import org.ndexbio.cx2.converter.ConverterUtilities;
import org.ndexbio.cx2.converter.ConverterUtilitiesResult;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.util.CxioUtil;
import org.ndexbio.model.exceptions.NdexException;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.log4j.Logger;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.VisualPropertyType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 * This class is used to gather visual properties from network views.
 *
 * @author cmzmasek
 *
 */
public final class VisualPropertiesGatherer {

	private static final Logger logger = Logger.getLogger("VisualPropertiesGatherer");
    private static final boolean ALLOW_NODE_CUSTOM_PROPERTIES = true;

    /**
     * This method is for gathering visual properties from a view and network
     * into aspect elements.
     *
     * @param view
     *            the view to gather visual properties from
     * @param network
     *            the network to gather visual properties from
     * @param visual_mapping_manager
     *            used to obtain the current visual style
     * @param lexicon
     *            the lexicon to get all visual properties from
     * @param types
     *            the visual types (nodes, edges, network, nodes default, etc.)
     *            to gather
     *
     * @return a List of AspectElement
     * @throws JsonProcessingException 
     */
    public static final List<AspectElement> gatherVisualPropertiesAsAspectElements(final CyNetworkView view,
                                                                                   final VisualLexicon lexicon,
                                                                                   final Set<VisualPropertyType> types,
                                                                                   final Long viewId,
                                                                                   boolean use_cxId) throws JsonProcessingException {

        final List<AspectElement> elements = new ArrayList<>();
        final VisualMappingManager vmm = CyServiceModule.getService(VisualMappingManager.class);
        final VisualStyle current_visual_style = vmm.getVisualStyle(view);
        
        if (lexicon == null) {
        	throw new IllegalArgumentException("VisualLexicon is not initialized. This should not happen");
        }
        final Set<VisualProperty<?>> all_visual_properties = lexicon.getAllVisualProperties();
        if (current_visual_style == null) {
        	throw new IllegalArgumentException("Failed to get Visual Style from view " + view);
        }
        
        if (types.contains(VisualPropertyType.NETWORK)) {
            gatherNetworkVisualProperties(view, elements, current_visual_style, all_visual_properties, viewId);
        }

        if (types.contains(VisualPropertyType.NODES_DEFAULT)) {
            gatherNodesDefaultVisualProperties(view, elements, current_visual_style, all_visual_properties, viewId);
        }

        if (types.contains(VisualPropertyType.EDGES_DEFAULT)) {
            gatherEdgesDefaultVisualProperties(view, elements, current_visual_style, all_visual_properties, viewId);
        }

        if (types.contains(VisualPropertyType.NODES)) {
            gatherNodeVisualProperties(view, elements, all_visual_properties, viewId, use_cxId);
        }

        if (types.contains(VisualPropertyType.EDGES)) {
            gatherEdgeVisualProperties(view, elements, all_visual_properties, viewId, use_cxId);
        }

        return elements;

    }

    private  final static <T> void addProperties(final View<? extends CyIdentifiable> view,
                                            final VisualProperty<T> vp,
                                            final CyVisualPropertiesElement cvp) {
        if (!view.isSet(vp) || !view.isValueLocked(vp)) {
        	return;
        }
        
        try {
        	final String value_str = getSerializableVisualProperty(view, vp);
        	if (CxioUtil.isEmpty(value_str)) {
            	return;
            }
        	final String id_string = vp.getIdString();
            if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) {
                // TODO
            	logger.info("Need to add handler for Property: " + id_string + " " + value_str);
            }
            else {
                cvp.putProperty(id_string, value_str);
            }
        }catch (IllegalArgumentException e) {
        	String message = String.format("Writing %s(%s) = %s caused Error:\n%s", vp.getDisplayName(), vp.getTargetDataType(), view.getVisualProperty(vp), e.getMessage());
        	throw new IllegalArgumentException(message);
        }
        
    }

    private static <T> String getSerializableVisualProperty(View<? extends CyIdentifiable> view, VisualProperty<T> vp) {
    	T prop = view.getVisualProperty(vp);
		if (prop == null) {
			return null;
		}
		String val = null;
		try {
			val = vp.toSerializableString(prop);
		}catch (ClassCastException e) {
			val = String.valueOf(prop);
			String message = String.format("Class cast exception for %s: %s(%s) = %s. Error: %s\n", vp.getClass(), vp.getDisplayName(), vp.getTargetDataType(), view.getVisualProperty(vp), e.getMessage());
			logger.info(message);
//        	throw new ClassCastException(message);
		}
		
		return val;
	}

    private final static <T> void addPropertiesNetwork(final View<? extends CyIdentifiable> view,
                                                   final VisualStyle style,
                                                   final VisualProperty<T> vp,
                                                   final CyVisualPropertiesElement cvp) {
    	String value_str = getSerializableVisualProperty(view, vp);
        if (CxioUtil.isEmpty(value_str)) {
        	return;
        }
        final String id_string = vp.getIdString();
        // TODO: Why is this conditional here?
        if (view.isSet(vp) && view.isValueLocked(vp)) {
            if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) {
                // TODO
            	throw new RuntimeException("Failed to add property " + vp);
            }
            else {
                cvp.putProperty(id_string, value_str);
            }
        
        }
        else {
            if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")
                    || id_string.startsWith("NODE_CUSTOM")) {
                // TODO
            }
            else {
                cvp.putProperty(id_string, value_str);
            }
        }
    }

    private final static <T> void addDefaultProperties(final VisualStyle style,
                                                   final VisualProperty<T> vp,
                                                   final CyVisualPropertiesElement cvp) {
        final T vp_value = style.getDefaultValue(vp);
        if (vp_value != null) {
            final String value_str = vp.toSerializableString(vp_value);
            if (!CxioUtil.isEmpty(value_str)) {
                final String id_string = vp.getIdString();
                if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) {
                    // ignore
                }
                else if (id_string.startsWith("NODE_CUSTOM")) {
                    if (ALLOW_NODE_CUSTOM_PROPERTIES) {
                        cvp.putProperty(id_string, value_str);
                    }
                }
                else {
                    cvp.putProperty(id_string, value_str);
                }
            }
        }
    }

    public final static <T> String getDefaultPropertyAsString(final VisualStyle style,
            final VisualProperty<T> vp) {
		final String id_string = vp.getIdString();
		if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")
				|| id_string.startsWith("NODE_CUSTOM") ) {
			return null;
		}

    	final T vp_value = style.getDefaultValue(vp);
    	if (vp_value != null) {
    		final String value_str = vp.toSerializableString(vp_value);
    		if (!CxioUtil.isEmpty(value_str)) {
        		return value_str;
    		}
    	}
    	return null;
}

    
    final public static char COMMA = ',';
    
    // escape ',' with double ',' 
    private static String escapeString(String str) {
        if (str == null) {
          return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
          char curChar = str.charAt(i);
          if (curChar == COMMA) {
            // special char
            result.append(COMMA);
          }
          result.append(curChar);
        }
        return result.toString();
      }

    // Note: ',' in column name and value are escaped by ',,' 
    private final static <T> void addMappings(final VisualStyle style,
                                          final VisualProperty<T> vp,
                                          final CyVisualPropertiesElement cvp,
                                          final CyTable table) {
        final VisualMappingFunction<?, T> mapping = style.getVisualMappingFunction(vp);
        if (mapping == null) {
        	return;
        }
        final String col = mapping.getMappingColumnName();
        
        if (mapping instanceof PassthroughMapping<?, ?>) {
            final PassthroughMapping<?, T> pm = (PassthroughMapping<?, T>) mapping;
            String type = null;
            try {
                type = toAttributeType(pm.getMappingColumnType(), table, col);
            }
            catch (final IOException e) {
                logger.info("WARNING: problem with mapping/column '" + col
                        + "': column not present, ignoring corresponding passthrough mapping. " + e.getMessage());
                return;
            }
            final StringBuilder sb = new StringBuilder();
            sb.append(CxUtil.VM_COL);
            sb.append("=");
            sb.append(escapeString(col));
            sb.append(",");
            sb.append(CxUtil.VM_TYPE);
            sb.append("=");
            sb.append(escapeString(type));
            cvp.putMapping(vp.getIdString(), CxUtil.PASSTHROUGH, sb.toString());
        }
        else if (mapping instanceof DiscreteMapping<?, ?>) {
            final DiscreteMapping<?, T> dm = (DiscreteMapping<?, T>) mapping;
            String type = null;
            try {
                type = toAttributeType(dm.getMappingColumnType(), table, col);
            }
            catch (final IOException e) {
                logger.info("WARNING: problem with mapping/column '" + col
                        + "': column not present, ignoring corresponding discrete mapping. " + e.getMessage());
                return;
            }
            final Map<?, T> map = dm.getAll();
            final StringBuilder sb = new StringBuilder();
            sb.append(CxUtil.VM_COL);
            sb.append("=");
            sb.append(escapeString(col));
            sb.append(",");
            sb.append(CxUtil.VM_TYPE);
            sb.append("=");
            sb.append(escapeString(type));
            int counter = 0;
            for (final Map.Entry<?, T> entry : map.entrySet()) {
                final T value = entry.getValue();
                if (value == null) {
                    continue;
                }
                try {
                    sb.append(",K=");
                    sb.append(counter);
                    sb.append("=");
                    sb.append(escapeString(entry.getKey().toString()));
                    sb.append(",V=");
                    sb.append(counter);
                    sb.append("=");
                    sb.append(escapeString(vp.toSerializableString(value)));
                }
                catch (final Exception e) {
                    logger.info("could not add discrete mapping entry: " + value);
                    e.printStackTrace();
                    return;
                }
                ++counter;
            }
            cvp.putMapping(vp.getIdString(), CxUtil.DISCRETE, sb.toString());
        }
        else if (mapping instanceof ContinuousMapping<?, ?>) {
            final ContinuousMapping<?, T> cm = (ContinuousMapping<?, T>) mapping;
            String type = null;
            try {
                type = toAttributeType(cm.getMappingColumnType(), table, col);
            }
            catch (final IOException e) {
                logger.info("WARNING: problem with mapping/column '" + col
                        + "': column not present, ignoring corresponding continuous mapping." + e.getMessage());
                return;
            }
            final StringBuilder sb = new StringBuilder();
            sb.append(CxUtil.VM_COL);
            sb.append("=");
            sb.append(escapeString(col));
            sb.append(",");
            sb.append(CxUtil.VM_TYPE);
            sb.append("=");
            sb.append(escapeString(type));
            int counter = 0;
            for (final ContinuousMappingPoint<?, T> cp : cm.getAllPoints()) {
                final T lesser = cp.getRange().lesserValue;
                final T equal = cp.getRange().equalValue;
                final T greater = cp.getRange().greaterValue;
                sb.append(",L=");
                sb.append(counter);
                sb.append("=");
                sb.append(escapeString(vp.toSerializableString(lesser)));
                sb.append(",E=");
                sb.append(counter);
                sb.append("=");
                sb.append(escapeString(vp.toSerializableString(equal)));
                sb.append(",G=");
                sb.append(counter);
                sb.append("=");
                sb.append(escapeString(vp.toSerializableString(greater)));
                sb.append(",OV=");
                sb.append(counter);
                sb.append("=");
                sb.append(escapeString(cp.getValue().toString()));
                ++counter;
            }
            cvp.putMapping(vp.getIdString(), CxUtil.CONTINUOUS, sb.toString());
        }
    }

    public static <T> VisualPropertyMapping getCX2Mapping(final VisualStyle style,
            final VisualProperty<T> vp,
            final CyTable table) throws NdexException {
    	final VisualMappingFunction<?, T> mapping = style.getVisualMappingFunction(vp);
    	if (mapping == null) {
    		return null;
    	}
    	
    	String col = mapping.getMappingColumnName();
    	String vpName = vp.getIdString();
		VisualPropertyMapping cx2Mapping = new VisualPropertyMapping();
		MappingDefinition def = new MappingDefinition (col);
		cx2Mapping.setMappingDef(def);
		CXToCX2VisualPropertyConverter cvtr = CXToCX2VisualPropertyConverter.getInstance();
		
    	if (mapping instanceof PassthroughMapping<?, ?>) {
    		cx2Mapping.setType(VPMappingType.PASSTHROUGH);
    		final PassthroughMapping<?, T> pm = (PassthroughMapping<?, T>) mapping;
    		String type = null;
    		try {
    			type = toAttributeType(pm.getMappingColumnType(), table, col);
    		}
    		catch (final IOException e) {
    			logger.info("WARNING: problem with mapping/column '" + col
    					+ "': column not present, ignoring corresponding passthrough mapping. " + e.getMessage());
    			return null;
    		}
    	} else if (mapping instanceof DiscreteMapping<?, ?>) {
    		cx2Mapping.setType(VPMappingType.DISCRETE);
    		final DiscreteMapping<?, T> dm = (DiscreteMapping<?, T>) mapping;
    		String type = null;
    		try {
    			type = toAttributeType(dm.getMappingColumnType(), table, col);
    		}
    		catch (final IOException e) {
    			logger.info("WARNING: problem with mapping/column '" + col
    					+ "': column not present, ignoring corresponding discrete mapping. " + e.getMessage());
    			return null;
    		}
    		
    		final Map<?, T> map = dm.getAll();
    		
    		List<Map<String, Object>> mappingList = new ArrayList<>();
    		for (final Map.Entry<?, T> entry : map.entrySet()) {
    			final T value = entry.getValue();
    			if (value == null) {
    				continue;
    			}	
    			Map<String,Object> mapEntry = new HashMap<>();
    			String catVPStr = vpName.equals("NODE_SIZE") ? "NODE_HEIGHT" : vpName ;
    			Object newVP = cvtr.getNewEdgeOrNodePropertyValue(catVPStr, vp.toSerializableString(value));
    			if ( newVP!=null) {
    				mapEntry.put("v",  entry.getKey());
        			mapEntry.put("vp", newVP);
        			mappingList.add(mapEntry);
    			}
    		}
    		if (!mappingList.isEmpty())
    			def.setMapppingList(mappingList);
    	}	else if (mapping instanceof ContinuousMapping<?, ?>) {
    		cx2Mapping.setType(VPMappingType.CONTINUOUS);
    		final ContinuousMapping<?, T> cm = (ContinuousMapping<?, T>) mapping;
    		String type = null;
    		try {
    			type = toAttributeType(cm.getMappingColumnType(), table, col);
    		}
    		catch (final IOException e) {
    			logger.info("WARNING: problem with mapping/column '" + col
    					+ "': column not present, ignoring corresponding continuous mapping." + e.getMessage());
    			return null;
    		}
    		
			List<Map<String, Object>> m = new ArrayList<>();

			Object min = null;
			Boolean includeMin = null;
			// Object max = null;
			Object minVP = null;
			// Object maxVP = null;

			int counter = 0;
			Map<String, Object> currentMapping = new HashMap<>();

			for (ContinuousMappingPoint<?, T> cp : cm.getAllPoints()) {
				T L = cp.getRange().lesserValue;
				
				Object LO = cvtr.getNewEdgeOrNodePropertyValue(vpName, vp.toSerializableString(L));

				T E = cp.getRange().equalValue;
				
				// Object EO = vpConverter.getNewEdgeOrNodePropertyValue(vpName,E);

				T G = cp.getRange().greaterValue;
				if (G == null) {
					break;
				}
				Object GO = cvtr.getNewEdgeOrNodePropertyValue(vpName, vp.toSerializableString(G));

				Object OV = cp.getValue();
				
				Object OVO = cp.getValue();

				if (counter == 0) { // min side
					currentMapping.put("includeMin", Boolean.FALSE);
					currentMapping.put("includeMax", Boolean.valueOf(E.equals(L)));
					currentMapping.put("maxVPValue", LO);
					currentMapping.put("max", OVO);
					m.add(currentMapping);
				} else {
					currentMapping.put("includeMin", includeMin);
					currentMapping.put("includeMax", Boolean.valueOf(E.equals(L)));
					currentMapping.put("minVPValue", minVP);
					currentMapping.put("min", min);
					currentMapping.put("maxVPValue", LO);
					currentMapping.put("max", OVO);
					m.add(currentMapping);
				}

				// store the max values as min for the next segment
				includeMin = Boolean.valueOf(E.equals(G));

				min = OVO;
				minVP = GO;

				currentMapping = new HashMap<>();
				counter++;
			}

			// add the last entry
			currentMapping.put("includeMin", includeMin);
			currentMapping.put("includeMax", Boolean.FALSE);
			currentMapping.put("minVPValue", minVP);
			currentMapping.put("min", min);
			m.add(currentMapping);

			// add the list
			if (!m.isEmpty())
    			def.setMapppingList(m);
    	} else {
    		throw new NdexException("Mapping type on column " +col + " is not supported by CX2.");
    	}
		return cx2Mapping;

    }

    
    
    private static void gatherEdgesDefaultVisualProperties(final CyNetworkView view,
                                                           final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties,
                                                           final Long viewId) {

        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.EDGES_DEFAULT.asString(),
        		viewId,
        		viewId);
        
        for (final VisualProperty<?> visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyEdge.class) {
                addDefaultProperties(current_visual_style, visual_property, e);
                final CyTable table = view.getModel().getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
                addMappings(current_visual_style, visual_property, e, table);
            }
        }
        addDependency(CxUtil.ARROW_COLOR_MATCHES_EDGE, current_visual_style, e);
        visual_properties.add(e);
    }

    private static void gatherEdgeVisualProperties(final CyNetworkView view,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties,
                                                   final Long viewId,
                                                   boolean use_cxId) throws JsonProcessingException {
        for (View<CyEdge> edge_view : view.getEdgeViews()) {
        	final CyEdge edge = edge_view.getModel();
            final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.EDGES.asString(),
            												CxUtil.getElementId(edge, (CySubNetwork)view.getModel(), use_cxId),                                                                           
            												viewId);

            for (final VisualProperty<?> visual_property : all_visual_properties) {
            	
                if (visual_property.getTargetDataType() == CyEdge.class) {
                    addProperties(edge_view, visual_property, e);
                }
            }
            if ((e.getProperties() != null) && !e.getProperties().isEmpty()) {
                visual_properties.add(e);
            }
        }
    }

    private static void gatherNetworkVisualProperties(final CyNetworkView view,
                                                      final List<AspectElement> visual_properties,
                                                      final VisualStyle current_visual_style,
                                                      final Set<VisualProperty<?>> all_visual_properties,
                                                      final Long viewId) {
        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NETWORK.asString(),
        												viewId,
                                                        viewId);

        for (final VisualProperty<?> visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyNetwork.class) {
                addPropertiesNetwork(view, current_visual_style, visual_property, e);
            }
        }
        visual_properties.add(e);
    }

    private static void gatherNodesDefaultVisualProperties(final CyNetworkView view,
                                                           final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties,
                                                           final Long viewId) {
        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NODES_DEFAULT.asString(),
        											viewId, viewId);
        
        for (final VisualProperty<?> visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyNode.class) {
                addDefaultProperties(current_visual_style, visual_property, e);
                final CyTable table = view.getModel().getTable(CyNode.class, CyNetwork.DEFAULT_ATTRS);
                addMappings(current_visual_style, visual_property, e, table);
            }
        }
        addDependency(CxUtil.NODE_CUSTOM_GRAPHICS_SIZE_SYNC, current_visual_style, e);
        addDependency(CxUtil.NODE_SIZE_LOCKED, current_visual_style, e);
        visual_properties.add(e);
    }

    private final static void addDependency(final String id_string,
                                            final VisualStyle style,
                                            final CyVisualPropertiesElement vpe) {
        for (final VisualPropertyDependency<?> d : style.getAllVisualPropertyDependencies()) {
            if (d.getIdString().equals(id_string)) {
                vpe.putDependency(id_string, String.valueOf(d.isDependencyEnabled()));
                return;
            }
        }
    }

    /** add editor property into the give props object.
     * 
     * @param id_string dependency property name
     * @param style current style
     * @param props property object that this dependency should be added to.
     */
    public static void addCx2EditorPropsDependency(final String id_string,
            final VisualStyle style,
            VisualEditorProperties props) {
    	for (final VisualPropertyDependency<?> d : style.getAllVisualPropertyDependencies()) {
    		if (d.getIdString().equals(id_string)) {
    			props.getProperties().put(id_string, d.isDependencyEnabled());
    		}
    	}
    }

    
    private static void gatherNodeVisualProperties(final CyNetworkView view,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties,
                                                   final Long viewId,
                                                   boolean use_cxId) {
        for (View<CyNode> node_view : view.getNodeViews()) {
            final CyNode cy_node = node_view.getModel();
            final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NODES.asString(),
            																CxUtil.getElementId(cy_node, view.getModel(), use_cxId),
                                                                            viewId);
     
            for (final VisualProperty<?> visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyNode.class) {
                    addProperties(node_view, visual_property, e);
                }
            }
            if ((e.getProperties() != null) && !e.getProperties().isEmpty()) {
                visual_properties.add(e);
            }
        }
    }

	public static List<CxNodeBypass> getNodeBypasses(final CyNetworkView view,
			final Set<VisualProperty<?>> all_visual_properties, boolean use_cxId, boolean nodeSizeLocked) throws NdexException {
		
		List<CxNodeBypass> nodeByPasses = new LinkedList<>();

		List<VisualProperty<?>> filteredVPs = all_visual_properties
				.stream().filter(x -> x.getTargetDataType() == CyNode.class)
				.collect(Collectors.toList());
		
		for (View<CyNode> node_view : view.getNodeViews()) {
			Map<String,String> cx1VPTable = new HashMap<>();
			for (final VisualProperty<?> visual_property : filteredVPs) {
				final String value_str = getSerializableVisualProperty(node_view, visual_property);
	        	if (!CxioUtil.isEmpty(value_str)) {
		        	final String id_string = visual_property.getIdString();
		            if (!id_string.equals("NODE") && !id_string.equals("EDGE") && !id_string.equals("NETWORK")) {
		                cx1VPTable.put(id_string, value_str);   
		            }
	            }
			}
			if (!cx1VPTable.isEmpty()) {
				final CyNode cy_node = node_view.getModel();
				Long nodeId = CxUtil.getElementId(cy_node, view.getModel(), use_cxId);

				if (nodeSizeLocked) { // handle node size
					CXToCX2VisualPropertyConverter.cvtCx1NodeSize(cx1VPTable);
				}
				VisualPropertyTable t = CXToCX2VisualPropertyConverter.getInstance().convertEdgeOrNodeVPs(cx1VPTable);
				nodeByPasses.add(new CxNodeBypass(nodeId, t));
			}
		}
		return nodeByPasses;
	}
 
	public static List<CxEdgeBypass> getEdgeBypasses(final CyNetworkView view,
			final Set<VisualProperty<?>> all_visual_properties, boolean use_cxId, boolean arrowColorMatchesEdge) throws NdexException {
		
		List<CxEdgeBypass> edgeByPasses = new LinkedList<>();

		List<VisualProperty<?>> filteredVPs = all_visual_properties
				.stream().filter(x -> x.getTargetDataType() == CyEdge.class)
				.collect(Collectors.toList());
		
		for (View<CyEdge> edgeView : view.getEdgeViews()) {
			Map<String,String> cx1VPTable = new HashMap<>();
			for (final VisualProperty<?> visual_property : filteredVPs) {
				final String value_str = getSerializableVisualProperty(edgeView, visual_property);
	        	if (!CxioUtil.isEmpty(value_str)) {
		        	final String id_string = visual_property.getIdString();
		            if (!id_string.equals("NODE") && !id_string.equals("EDGE") && !id_string.equals("NETWORK")) {
		                cx1VPTable.put(id_string, value_str);   
		            }
	            }
			}
			if (!cx1VPTable.isEmpty()) {
				final CyEdge cyEdge = edgeView.getModel();
				Long edgeId = CxUtil.getElementId(cyEdge, view.getModel(), use_cxId);

				if (arrowColorMatchesEdge) { // handle node size
					CXToCX2VisualPropertyConverter.cvtCx1EdgeColor(cx1VPTable);
				}
				VisualPropertyTable t = CXToCX2VisualPropertyConverter.getInstance().convertEdgeOrNodeVPs(cx1VPTable);
				edgeByPasses.add(new CxEdgeBypass(edgeId, t));
			}
		}
		return edgeByPasses;
	}
    
    private final static String toAttributeType(final Class<?> attr_class, final CyTable table, final String col_name)
            throws IOException {
        if (attr_class == String.class) {
            return "string";
        }
        else if ((attr_class == Float.class) || (attr_class == Double.class)) {
            return "double";
        }
        else if ((attr_class == Integer.class) || (attr_class == Short.class)) {
            return "integer";
        }
        else if (attr_class == Long.class) {
            return "long";
        }
        else if (attr_class == Boolean.class) {
            return "boolean";
        }
        else if (Number.class.isAssignableFrom(attr_class)) {
            Class<?> col_type = null;
            if ((table != null) && (col_name != null)) {
                final CyColumn col = table.getColumn(col_name);
                if (col != null) {
                    col_type = table.getColumn(col_name).getType();
                }
                else {
                    throw new IOException("failed to obtain column '" + col_name + "'");
                }
            }
            if (col_type != null) {
                logger.info("mapping type is '" + attr_class + "' will use (from table column) '" + col_type
                                   + "' instead");
                if ((col_type == Float.class) || (col_type == Double.class)) {
                    return "double";
                }
                else if ((col_type == Integer.class) || (col_type == Short.class)) {
                    return "integer";
                }
                else if (col_type == Long.class) {
                    return "long";
                }
                else {
                    throw new IllegalArgumentException("don't know how to deal with type '" + col_type
                                                       + "' (from table column "+ col_name + ")");
                }
            }
            
            throw new IllegalStateException("failed to obtain type for mapping from table");
            
        }
        else {
            throw new IllegalArgumentException("don't know how to deal with type '" + attr_class + "' for column " + col_name);
        }
    }

}
