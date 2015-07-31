package org.cytoscape.io.internal.cx_writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cxio.aspects.datamodels.VisualPropertiesElement;
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
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

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
                                                                                   final CyNetwork network,
                                                                                   final VisualMappingManager visual_mapping_manager,
                                                                                   final VisualLexicon lexicon,
                                                                                   final Set<VisualPropertyType> types) {

        final List<AspectElement> elements = new ArrayList<AspectElement>();
        final VisualStyle current_visual_style = visual_mapping_manager.getVisualStyle(view);
        final Set<VisualProperty<?>> all_visual_properties = lexicon.getAllVisualProperties();

        if (types.contains(VisualPropertyType.NETWORK)) {
            gatherNetworkVisualProperties(view, elements, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.NODES_DEFAULT)) {
            gatherNodesDefaultVisualProperties(elements, current_visual_style, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.EDGES_DEFAULT)) {
            gatherEdgesDefaultVisualProperties(elements, current_visual_style, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.NODES)) {
            gatherNodeVisualProperties(view, network, elements, all_visual_properties);
        }

        if (types.contains(VisualPropertyType.EDGES)) {
            gatherEdgeVisualProperties(view, network, elements, all_visual_properties);
        }

        return elements;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final View view, final VisualProperty vp, final VisualPropertiesElement cvp) {

        if (view.isSet(vp) && view.isValueLocked(vp)) {
            final Object vp_value = view.getVisualProperty(vp);
            if (vp_value != null) {
                final String value_str = vp.toSerializableString(vp_value);
                if (!Util.isEmpty(value_str)) {
                    final String id_string = vp.getIdString();
                    if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) { // TODO
                        // //FIXME
                    }
                    else {
                        cvp.putProperty(id_string, value_str);
                    }
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
                if (id_string.equals("NODE") || id_string.equals("EDGE") || id_string.equals("NETWORK")) { // TODO
                    // //FIXME
                }
                else {
                    cvp.putProperty(id_string, value_str);
                }
            }
        }
    }

    // private final static void x(VisualProperty vp,
    // final View view ) {
    // final Object vp_value = view.isSet(vp)
    //
    // }

    @SuppressWarnings("rawtypes")
    private static void gatherEdgesDefaultVisualProperties(final List<AspectElement> visual_properties,
                                                           final VisualStyle current_visual_style,
                                                           final Set<VisualProperty<?>> all_visual_properties) {
        final VisualPropertiesElement edge_default_cxvp = new VisualPropertiesElement(VisualPropertyType.EDGES_DEFAULT.asString());
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
            final VisualPropertiesElement edge_cxvp = new VisualPropertiesElement(VisualPropertyType.EDGES.asString());
            edge_cxvp.addAppliesTo(String.valueOf(edge.getSUID()));
            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyEdge.class) {
                    addProperties(edge_view, visual_property, edge_cxvp);
                }
            }
            visual_properties.add(edge_cxvp);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void gatherNetworkVisualProperties(final CyNetworkView view,
                                                      final List<AspectElement> visual_properties,
                                                      final Set<VisualProperty<?>> all_visual_properties) {
        final VisualPropertiesElement cvp = new VisualPropertiesElement(VisualPropertyType.NETWORK.asString());
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
        final VisualPropertiesElement node_default_cxvp = new VisualPropertiesElement(VisualPropertyType.NODES_DEFAULT.asString());
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
            final VisualPropertiesElement node_cxvp = new VisualPropertiesElement(VisualPropertyType.NODES.asString());
            node_cxvp.addAppliesTo(String.valueOf(cy_node.getSUID()));
            for (final VisualProperty visual_property : all_visual_properties) {
                if (visual_property.getTargetDataType() == CyNode.class) {
                    addProperties(node_view, visual_property, node_cxvp);
                }

            }
            visual_properties.add(node_cxvp);
        }
    }

}
