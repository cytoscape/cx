package org.cytoscape.io.internal.cx_writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.util.Util;
import org.cytoscape.io.internal.cxio.VisualPropertyType;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
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
     */
    public static final List<AspectElement> gatherVisualPropertiesAsAspectElements(final CyNetworkView view,
                                                                                   final boolean z_used,
                                                                                   final CyNetwork network,
                                                                                   final VisualMappingManager visual_mapping_manager,
                                                                                   final VisualLexicon lexicon,
                                                                                   final Set<VisualPropertyType> types) {

        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final VisualStyle current_visual_style = visual_mapping_manager.getVisualStyle(view);
        final Set<VisualProperty<?>> all_visual_properties = lexicon.getAllVisualProperties();

        if (types.contains(VisualPropertyType.NETWORK)) {
            gatherNetworkVisualProperties(view, network, elements, current_visual_style, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.NODES_DEFAULT)) {
            gatherNodesDefaultVisualProperties(network, elements, current_visual_style, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.EDGES_DEFAULT)) {
            gatherEdgesDefaultVisualProperties(network, elements, current_visual_style, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.NODES)) {
            gatherNodeVisualProperties(view, z_used, network, elements, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.EDGES)) {
            gatherEdgeVisualProperties(view, network, elements, all_visual_properties);
        }

        return elements;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final View view,
                                            final boolean z_used,
                                            final VisualProperty vp,
                                            final CyVisualPropertiesElement cvp) {
        if (view.isSet(vp) && view.isValueLocked(vp)) {
            final Object vp_value = view.getVisualProperty(vp);
            if (vp_value != null) {
                final String value_str = vp.toSerializableString(vp_value);
                if (!Util.isEmpty(value_str)) {
                    final String id_string = vp.getIdString();
                    if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")
                            || (!z_used && (id_string.equals(BasicVisualLexicon.NODE_Z_LOCATION)))) {
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
    private final static void addPropertiesNetwork(final View view,
                                                   final VisualStyle style,
                                                   final VisualProperty vp,
                                                   final CyVisualPropertiesElement cvp) {
        if (view.isSet(vp) && view.isValueLocked(vp)) {
            final Object vp_value = view.getVisualProperty(vp);
            if (vp_value != null) {
                final String value_str = vp.toSerializableString(vp_value);
                if (!Util.isEmpty(value_str)) {
                    final String id_string = vp.getIdString();
                    if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) {
                        // TODO
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
                if (!Util.isEmpty(value_str)) {
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
            if (!Util.isEmpty(value_str)) {
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addMappings(final VisualStyle style,
                                          final VisualProperty vp,
                                          final CyVisualPropertiesElement cvp) {
        final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);

        if (mapping != null) {
            if (mapping instanceof PassthroughMapping<?, ?>) {

                final PassthroughMapping<?, ?> pm = (PassthroughMapping<?, ?>) mapping;
                final String col = pm.getMappingColumnName();
                final String type = toAttributeType(pm.getMappingColumnType());
                final StringBuilder sb = new StringBuilder();
                sb.append("COL=");
                sb.append(col);
                sb.append(",T=");
                sb.append(type);
                cvp.putProperty("PASSTHROUGH_MAPPING_" + vp.getIdString(), sb.toString());
            }
            else if (mapping instanceof DiscreteMapping<?, ?>) {
                final DiscreteMapping<?, ?> dm = (DiscreteMapping<?, ?>) mapping;

                final String type = toAttributeType(dm.getMappingColumnType());
                final String col = dm.getMappingColumnName();
                final Map<?, ?> map = dm.getAll();
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    final Object value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    try {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("COL=");
                        sb.append(col);
                        sb.append(",K=");
                        sb.append(entry.getKey().toString());
                        sb.append(",V=");
                        sb.append(vp.toSerializableString(value));
                        sb.append(",T=");
                        sb.append(type);
                        cvp.putProperty("DISCRETE_MAPPING_" + vp.getIdString(), sb.toString());
                    }
                    catch (final Exception e) {
                        System.out.println("could not add Discrete Mapping entry: " + value);
                        e.printStackTrace();
                    }
                }
            }
            else if (mapping instanceof ContinuousMapping<?, ?>) {
                final ContinuousMapping<?, ?> cm = (ContinuousMapping<?, ?>) mapping;
                final String type = toAttributeType(cm.getMappingColumnType());
                final String col = cm.getMappingColumnName();
                final List<?> points = cm.getAllPoints();
                for (final Object point : points) {
                    final ContinuousMappingPoint<?, ?> cp = (ContinuousMappingPoint<?, ?>) point;
                    final Object lesser = cp.getRange().lesserValue;
                    final Object equal = cp.getRange().equalValue;
                    final Object greater = cp.getRange().greaterValue;
                    final StringBuilder sb = new StringBuilder();
                    sb.append("COL=");
                    sb.append(col);
                    sb.append(",L=");
                    sb.append(vp.toSerializableString(lesser));
                    sb.append(",E=");
                    sb.append(vp.toSerializableString(equal));
                    sb.append(",G=");
                    sb.append(vp.toSerializableString(greater));
                    sb.append(",OV=");
                    sb.append(cp.getValue());
                    sb.append(",T=");
                    sb.append(type);
                    cvp.putProperty("CONTINUOUS_MAPPING_" + vp.getIdString(), sb.toString());
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void gatherEdgesDefaultVisualProperties(final CyNetwork network,
                                                           final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties) {
        final String network_suid = String.valueOf(network.getSUID());
        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.EDGES_DEFAULT.asString(),
                                                                          network_suid);
        e.addAppliesTo(network_suid);
        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyEdge.class) {
                addDefaultProperties(current_visual_style, visual_property, e);
                addMappings(current_visual_style, visual_property, e);
            }
        }
        visual_properties.add(e);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherEdgeVisualProperties(final CyNetworkView view,
                                                   final CyNetwork network,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties) {
        for (final CyEdge edge : network.getEdgeList()) {
            final View<CyEdge> edge_view = view.getEdgeView(edge);
            final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.EDGES.asString(),
                                                                              String.valueOf(network.getSUID()));
            e.addAppliesTo(org.cytoscape.io.internal.cxio.Util.makeId(edge.getSUID()));
            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyEdge.class) {
                    addProperties(edge_view, false, visual_property, e);

                }
            }
            if ((e.getProperties() != null) && !e.getProperties().isEmpty()) {
                visual_properties.add(e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNetworkVisualProperties(final CyNetworkView view,
                                                      final CyNetwork network,
                                                      final List<AspectElement> visual_properties,
                                                      final VisualStyle current_visual_style,
                                                      final Set<VisualProperty<?>> all_visual_properties) {
        final String network_suid = String.valueOf(network.getSUID());

        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NETWORK.asString(),
                                                                          network_suid);
        e.addAppliesTo(network_suid);
        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyNetwork.class) {
                addPropertiesNetwork(view, current_visual_style, visual_property, e);
            }
        }
        visual_properties.add(e);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNodesDefaultVisualProperties(final CyNetwork network,
                                                           final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties) {
        final String network_suid = String.valueOf(network.getSUID());
        final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NODES_DEFAULT.asString(),
                                                                          network_suid);
        e.addAppliesTo(network_suid);
        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyNode.class) {

                addDefaultProperties(current_visual_style, visual_property, e);
                addMappings(current_visual_style, visual_property, e);
            }
        }
        visual_properties.add(e);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNodeVisualProperties(final CyNetworkView view,
                                                   final boolean z_used,
                                                   final CyNetwork network,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties) {
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            final CyVisualPropertiesElement e = new CyVisualPropertiesElement(VisualPropertyType.NODES.asString(),
                                                                              String.valueOf(network.getSUID()));

            e.addAppliesTo(org.cytoscape.io.internal.cxio.Util.makeId(cy_node.getSUID()));

            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyNode.class) {
                    addProperties(node_view, z_used, visual_property, e);
                }

            }
            if ((e.getProperties() != null) && !e.getProperties().isEmpty()) {
                visual_properties.add(e);
            }

        }
    }

    private final static String toAttributeType(final Class<?> attr_class) {
        String type = "string";
        if (attr_class == Boolean.class) {
            type = "boolean";
        }
        else if ((attr_class == Byte.class) || (attr_class == Short.class) || (attr_class == Integer.class)) {
            type = "integer";
        }
        else if (attr_class == Long.class) {
            type = "long";
        }
        else if (Number.class.isAssignableFrom(attr_class)) {
            type = "float";
        }
        return type;
    }

}
