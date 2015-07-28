package org.cytoscape.io.internal.visual_properties;

import java.util.List;
import java.util.Set;

import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.util.Util;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public final class VisualPropertiesWriter {

    public final static String EDGES_VPE_LABEL         = "edges";
    public final static String NODES_VPE_LABEL         = "nodes";
    public final static String NETWORK_VPE_LABEL       = "network";
    public final static String NODES_DEFAULT_VPE_LABEL = "nodes:default";
    public final static String EDGES_DEFAULT_VPE_LABEL = "edges:default";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final View view, final VisualProperty vp, final VisualPropertiesElement cvp) {
        final Object vp_value = view.getVisualProperty(vp);

        if (vp_value != null) {
            final String value_str = vp.toSerializableString(vp_value);
            if (!Util.isEmpty(value_str)) {
                final String id_string = vp.getIdString();
                if (id_string.startsWith("NODE_CUSTOM") || id_string.equals("NODE") || id_string.equals("EDGE")
                        || id_string.equals("NETWORK")) { // TODO //FIXME
                }
                else {
                    cvp.putProperty(id_string, value_str);
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final VisualStyle style,
                                            final VisualProperty vp,
                                            final VisualPropertiesElement cvp) {
        final Object vp_value = style.getDefaultValue(vp);
        final VisualMappingFunction mapping_function = style.getVisualMappingFunction(vp); // TODO

        if (vp_value != null) {
            final String value_str = vp.toSerializableString(vp_value);
            if (!Util.isEmpty(value_str)) {
                final String id_string = vp.getIdString();
                if (id_string.startsWith("NODE_CUSTOM") || id_string.equals("NODE") || id_string.equals("EDGE")
                        || id_string.equals("NETWORK")) { // TODO //FIXME
                }
                else {
                    cvp.putProperty(id_string, value_str);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void gatherEdgesDefaultVisualProperties(final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties) {
        final VisualPropertiesElement edge_default_cxvp = new VisualPropertiesElement(EDGES_DEFAULT_VPE_LABEL);
        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyEdge.class) {

                addProperties(current_visual_style, visual_property, edge_default_cxvp);
            }
        }
        visual_properties.add(edge_default_cxvp);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherEdgeVisualProperties(final CyNetworkView view,
                                                   final CyNetwork network,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties) {
        for (final CyEdge edge : network.getEdgeList()) {
            final View<CyEdge> edge_view = view.getEdgeView(edge);
            final VisualPropertiesElement edge_cxvp = new VisualPropertiesElement(EDGES_VPE_LABEL);
            edge_cxvp.addAppliesTo(String.valueOf(edge.getSUID()));
            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyEdge.class) {
                    addProperties(edge_view, visual_property, edge_cxvp);
                    System.out.println(visual_property.toString());
                }
            }
            visual_properties.add(edge_cxvp);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNetworkVisualProperties(final CyNetworkView view,
                                                      final List<AspectElement> visual_properties,
                                                      final Set<VisualProperty<?>> all_visual_properties) {
        final VisualPropertiesElement cvp = new VisualPropertiesElement(NETWORK_VPE_LABEL);
        for (final VisualProperty visual_property : all_visual_properties) {

            if (visual_property.getTargetDataType() == CyNetwork.class) {
                addProperties(view, visual_property, cvp);
            }
        }
        visual_properties.add(cvp);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNodesDefaultVisualProperties(final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties) {
        final VisualPropertiesElement node_default_cxvp = new VisualPropertiesElement(NODES_DEFAULT_VPE_LABEL);
        for (final VisualProperty visual_property : all_visual_properties) {
            if (visual_property.getTargetDataType() == CyNode.class) {
                addProperties(current_visual_style, visual_property, node_default_cxvp);
            }
        }
        visual_properties.add(node_default_cxvp);
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNodeVisualProperties(final CyNetworkView view,
                                                   final CyNetwork network,
                                                   final List<AspectElement> visual_properties,
                                                   final Set<VisualProperty<?>> all_visual_properties) {
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            final VisualPropertiesElement node_cxvp = new VisualPropertiesElement(NODES_VPE_LABEL);
            node_cxvp.addAppliesTo(String.valueOf(cy_node.getSUID()));
            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyNode.class) {
                    addProperties(node_view, visual_property, node_cxvp);
                    System.out.println(visual_property.toString());
                }

            }
            visual_properties.add(node_cxvp);
        }
    }

    public static final void gatherVisualProperties(final CyNetworkView view,
                                                    final CyNetwork network,
                                                    final VisualMappingManager visual_mapping_manager,
                                                    final VisualLexicon lexicon,
                                                    final List<AspectElement> visual_properties) {

        final VisualStyle current_visual_style = visual_mapping_manager.getVisualStyle(view);
        final Set<VisualProperty<?>> all_visual_properties = lexicon.getAllVisualProperties();

        gatherNetworkVisualProperties(view, visual_properties, all_visual_properties);

        gatherNodesDefaultVisualProperties(visual_properties, current_visual_style, all_visual_properties);

        gatherEdgesDefaultVisualProperties(visual_properties, current_visual_style, all_visual_properties);

        gatherNodeVisualProperties(view, network, visual_properties, all_visual_properties);

        gatherEdgeVisualProperties(view, network, visual_properties, all_visual_properties);

    }

}
