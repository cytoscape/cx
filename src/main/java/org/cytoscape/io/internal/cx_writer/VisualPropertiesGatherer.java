package org.cytoscape.io.internal.cx_writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.util.CxioUtil;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.log4j.Logger;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.VisualPropertyType;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.IntegerVisualProperty;
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final View view,
                                            final VisualProperty vp,
                                            final CyVisualPropertiesElement cvp) {
        if (!view.isSet(vp) || !view.isValueLocked(vp)) {
        	return;
        }
        
        Object vp_value = view.getVisualProperty(vp);
        if (vp_value == null) {
        	return;
        }
        if (vp_value instanceof Number && vp instanceof IntegerVisualProperty) {
        	vp_value = ((Number) vp_value).intValue();
        }
        
        try {
        	final String value_str = vp.toSerializableString(vp_value);
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
        } catch(ClassCastException e) {
        	System.out.println(vp_value);
        	System.out.println(vp_value.getClass());
        	System.out.println(vp.getTargetDataType());
        	String message = String.format("Class cast exception for %s: %s(%s) = %s. Error: %s\n", vp.getClass(), vp.getDisplayName(), vp.getTargetDataType(), vp_value, e.getMessage());
        	throw new ClassCastException(message);
        }catch (IllegalArgumentException e) {
        	String message = String.format("Writing %s(%s) = %s caused Error:\n%s", vp.getDisplayName(), vp.getTargetDataType(), vp_value, e.getMessage());
        	throw new IllegalArgumentException(message);
        }
        
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addPropertiesNetwork(final View view,
                                                   final VisualStyle style,
                                                   final VisualProperty vp,
                                                   final CyVisualPropertiesElement cvp) {
        if (view.isSet(vp) && view.isValueLocked(vp)) {
            final Object vp_value = view.getVisualProperty(vp);
            if (vp_value != null) {
                final String value_str = vp.toSerializableString(vp_value);
                if (!CxioUtil.isEmpty(value_str)) {
                    final String id_string = vp.getIdString();
                    if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) {
                        // TODO
                    	throw new RuntimeException("Failed to add property " + vp);
                    }
                    else {
                        cvp.putProperty(id_string, value_str);
                    }
                }
            }
        }
        else {
            final Object vp_value = style.getDefaultValue(vp);
            if (vp_value != null) {
                final String value_str = vp.toSerializableString(vp_value);
                if (!CxioUtil.isEmpty(value_str)) {
                    final String id_string = vp.getIdString();
                    if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")
                            || id_string.startsWith("NODE_CUSTOM")) {
                        // TODO
                    }
                    else {
                        cvp.putProperty(id_string, value_str);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addDefaultProperties(final VisualStyle style,
                                                   final VisualProperty vp,
                                                   final CyVisualPropertiesElement cvp) {
        final Object vp_value = style.getDefaultValue(vp);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    // Note: ',' in column name and value are escaped by ',,' 
    private final static void addMappings(final VisualStyle style,
                                          final VisualProperty vp,
                                          final CyVisualPropertiesElement cvp,
                                          final CyTable table) {
        final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
        if (mapping == null) {
        	return;
        }
        final String col = mapping.getMappingColumnName();
        
        if (mapping instanceof PassthroughMapping<?, ?>) {
            final PassthroughMapping<?, ?> pm = (PassthroughMapping<?, ?>) mapping;
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
            final DiscreteMapping<?, ?> dm = (DiscreteMapping<?, ?>) mapping;
            String type = null;
            try {
                type = toAttributeType(dm.getMappingColumnType(), table, col);
            }
            catch (final IOException e) {
                logger.info("WARNING: problem with mapping/column '" + col
                        + "': column not present, ignoring corresponding discrete mapping. " + e.getMessage());
                return;
            }
            final Map<?, ?> map = dm.getAll();
            final StringBuilder sb = new StringBuilder();
            sb.append(CxUtil.VM_COL);
            sb.append("=");
            sb.append(escapeString(col));
            sb.append(",");
            sb.append(CxUtil.VM_TYPE);
            sb.append("=");
            sb.append(escapeString(type));
            int counter = 0;
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                final Object value = entry.getValue();
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
            final ContinuousMapping<?, ?> cm = (ContinuousMapping<?, ?>) mapping;
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
            final List<?> points = cm.getAllPoints();
            int counter = 0;
            for (final Object point : points) {
                final ContinuousMappingPoint<?, ?> cp = (ContinuousMappingPoint<?, ?>) point;
                final Object lesser = cp.getRange().lesserValue;
                final Object equal = cp.getRange().equalValue;
                final Object greater = cp.getRange().greaterValue;
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

    @SuppressWarnings("rawtypes")
    private static void gatherEdgesDefaultVisualProperties(final CyNetworkView view,
                                                           final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties,
                                                           final Long viewId) {

        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.EDGES_DEFAULT.asString(),
        		viewId,
        		viewId);
        
        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyEdge.class) {
                addDefaultProperties(current_visual_style, visual_property, e);
                final CyTable table = view.getModel().getTable(CyEdge.class, CyNetwork.DEFAULT_ATTRS);
                addMappings(current_visual_style, visual_property, e, table);
            }
        }
        addDependency(CxUtil.ARROW_COLOR_MATCHES_EDGE, current_visual_style, e);
        visual_properties.add(e);
    }

    @SuppressWarnings("rawtypes")
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

            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyEdge.class) {
                    addProperties(edge_view, visual_property, e);
                }
            }
            if ((e.getProperties() != null) && !e.getProperties().isEmpty()) {
                visual_properties.add(e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNetworkVisualProperties(final CyNetworkView view,
                                                      final List<AspectElement> visual_properties,
                                                      final VisualStyle current_visual_style,
                                                      final Set<VisualProperty<?>> all_visual_properties,
                                                      final Long viewId) {
        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NETWORK.asString(),
        												viewId,
                                                        viewId);

        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyNetwork.class) {
                addPropertiesNetwork(view, current_visual_style, visual_property, e);
            }
        }
        visual_properties.add(e);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNodesDefaultVisualProperties(final CyNetworkView view,
                                                           final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties,
                                                           final Long viewId) {
        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NODES_DEFAULT.asString(),
        											viewId, viewId);
        
        for (final VisualProperty visual_property : all_visual_properties) {
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

    @SuppressWarnings("rawtypes")
    private static void gatherNodeVisualProperties(final CyNetworkView view,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties,
                                                   final Long viewId,
                                                   boolean use_cxId) throws JsonProcessingException {
        for (View<CyNode> node_view : view.getNodeViews()) {
            final CyNode cy_node = node_view.getModel();
            final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NODES.asString(),
            																CxUtil.getElementId(cy_node, (CySubNetwork)view.getModel(), use_cxId),
                                                                            viewId);
     
            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyNode.class) {
                    addProperties(node_view, visual_property, e);
                }
            }
            if ((e.getProperties() != null) && !e.getProperties().isEmpty()) {
                visual_properties.add(e);
            }
        }
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
