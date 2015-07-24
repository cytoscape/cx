package org.cytoscape.io.internal.visual_properties;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cxio.aspects.datamodels.VisualProperties;
import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cxio.util.Util;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public final class VisualPropertiesWriter {

    @SuppressWarnings("rawtypes")
    final private static Map<VisualProperty, String> M                      = new HashMap<VisualProperty, String>();

    @SuppressWarnings("rawtypes")
    final private static List<VisualProperty>        NODE_VISUAL_PROPERTIES = new ArrayList<VisualProperty>();

    @SuppressWarnings("rawtypes")
    final private static List<VisualProperty>        EDGE_VISUAL_PROPERTIES = new ArrayList<VisualProperty>();

    static {
        // Node visual properties to be used:
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_BORDER_LINE_TYPE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_BORDER_PAINT);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_BORDER_WIDTH);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_DEPTH);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_FILL_COLOR);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_HEIGHT);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_LABEL);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_LABEL_COLOR);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_LABEL_FONT_FACE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_LABEL_WIDTH);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_PAINT);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_SELECTED);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_SELECTED_PAINT);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_SHAPE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_SIZE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_TOOLTIP);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_TRANSPARENCY);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_VISIBLE);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_WIDTH);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_X_LOCATION);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_Y_LOCATION);
        NODE_VISUAL_PROPERTIES.add(BasicVisualLexicon.NODE_Z_LOCATION);

        // Edge visual properties to be used:
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LABEL);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LABEL_COLOR);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LABEL_FONT_FACE);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LABEL_WIDTH);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_LINE_TYPE);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_PAINT);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_SELECTED);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_SELECTED_PAINT);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_TOOLTIP);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_TRANSPARENCY);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_VISIBLE);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_WIDTH);
        EDGE_VISUAL_PROPERTIES.add(BasicVisualLexicon.EDGE_BEND);

        // Mappings for nodes:
        M.put(BasicVisualLexicon.NODE_BORDER_LINE_TYPE, "border_line_type");
        M.put(BasicVisualLexicon.NODE_BORDER_PAINT, "border_color");
        M.put(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, "border_transparency");
        M.put(BasicVisualLexicon.NODE_BORDER_WIDTH, "border_width");
        M.put(BasicVisualLexicon.NODE_DEPTH, "depth");
        M.put(BasicVisualLexicon.NODE_FILL_COLOR, "fill_color");
        M.put(BasicVisualLexicon.NODE_HEIGHT, "height");
        M.put(BasicVisualLexicon.NODE_LABEL, "label");
        M.put(BasicVisualLexicon.NODE_LABEL_COLOR, "label_color");
        M.put(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, "label_font_size");
        M.put(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, "label_transparency");
        M.put(BasicVisualLexicon.NODE_LABEL_WIDTH, "label_width");
        M.put(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, "nested_network_visible");
        M.put(BasicVisualLexicon.NODE_PAINT, "color");
        M.put(BasicVisualLexicon.NODE_SELECTED, "selected");
        M.put(BasicVisualLexicon.NODE_SELECTED_PAINT, "selected_color");
        M.put(BasicVisualLexicon.NODE_SHAPE, "shape");
        M.put(BasicVisualLexicon.NODE_SIZE, "size");
        M.put(BasicVisualLexicon.NODE_TOOLTIP, "toolip");
        M.put(BasicVisualLexicon.NODE_TRANSPARENCY, "transparency");
        M.put(BasicVisualLexicon.NODE_VISIBLE, "visible");
        M.put(BasicVisualLexicon.NODE_WIDTH, "width");
        M.put(BasicVisualLexicon.NODE_X_LOCATION, "x_location");
        M.put(BasicVisualLexicon.NODE_Y_LOCATION, "y_location");
        M.put(BasicVisualLexicon.NODE_Z_LOCATION, "z_location");

        // Mappings for edges:
        M.put(BasicVisualLexicon.EDGE_LABEL, "label");
        M.put(BasicVisualLexicon.EDGE_LABEL_COLOR, "label_color");
        M.put(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, "label_font_size");
        M.put(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, "label_transparency");
        M.put(BasicVisualLexicon.EDGE_LABEL_WIDTH, "label_width");
        M.put(BasicVisualLexicon.EDGE_LINE_TYPE, "line_type");
        M.put(BasicVisualLexicon.EDGE_PAINT, "color");
        M.put(BasicVisualLexicon.EDGE_SELECTED, "selected");
        M.put(BasicVisualLexicon.EDGE_SELECTED_PAINT, "selected_color");
        M.put(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, "source_arrow_shape");
        M.put(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, "stroke_selected_color");
        M.put(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, "stroke_unselected_paint");
        M.put(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, "target_arrow_shape");
        M.put(BasicVisualLexicon.EDGE_TOOLTIP, "tooltip");
        M.put(BasicVisualLexicon.EDGE_TRANSPARENCY, "transparency");
        M.put(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, "unselected_color");
        M.put(BasicVisualLexicon.EDGE_VISIBLE, "visible");
        M.put(BasicVisualLexicon.EDGE_WIDTH, "width");
        M.put(BasicVisualLexicon.EDGE_BEND, "bend");

    }

    private final static String processColor(final Color c) {
        final StringBuilder sb = new StringBuilder();
        sb.append("rgb(");
        sb.append(c.getRed());
        sb.append(",");
        sb.append(c.getGreen());
        sb.append(",");
        sb.append(c.getBlue());
        sb.append(")");
        return sb.toString();
    }

    private final static void processFont(final VisualProperties cvp, final String suffix, final Font f) {
        cvp.put(suffix + "_font_family", f.getFamily());
        if (f.isPlain()) {
            cvp.put(suffix + "_font_style", "plain");
        }
        else if (f.isBold() && f.isItalic()) {
            cvp.put(suffix + "_font_style", "bold_italic");
        }
        else if (f.isBold()) {
            cvp.put(suffix + "_font_style", "bold");
        }
        else {
            cvp.put(suffix + "_font_style", "italic");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final View view, final VisualProperty vp, final VisualProperties cvp) {
        final Object vp_value = view.getVisualProperty(vp);
        if (vp_value != null) {
            if ((vp == BasicVisualLexicon.NODE_BORDER_PAINT) || (vp == BasicVisualLexicon.NODE_FILL_COLOR)
                    || (vp == BasicVisualLexicon.NODE_LABEL_COLOR) || (vp == BasicVisualLexicon.NODE_PAINT)
                    || (vp == BasicVisualLexicon.NODE_SELECTED_PAINT) || (vp == BasicVisualLexicon.EDGE_LABEL_COLOR)
                    || (vp == BasicVisualLexicon.EDGE_PAINT) || (vp == BasicVisualLexicon.EDGE_SELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_UNSELECTED_PAINT)) {
                cvp.put(obtainLabel(vp), processColor((Color) vp_value));
            }
            else if ((vp == BasicVisualLexicon.NODE_LABEL_FONT_FACE) || (vp == BasicVisualLexicon.EDGE_LABEL_FONT_FACE)) {
                processFont(cvp, "label", (Font) vp_value);
            }
            else {
                final String value_str = String.valueOf(vp_value);
                if (!Util.isEmpty(value_str)) {
                    cvp.put(obtainLabel(vp), value_str);
                }
            }
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final VisualStyle style, final VisualProperty vp, final VisualProperties cvp) {
        final Object vp_value = style.getDefaultValue(vp);
        if (vp_value != null) {
            if ((vp == BasicVisualLexicon.NODE_BORDER_PAINT) || (vp == BasicVisualLexicon.NODE_FILL_COLOR)
                    || (vp == BasicVisualLexicon.NODE_LABEL_COLOR) || (vp == BasicVisualLexicon.NODE_PAINT)
                    || (vp == BasicVisualLexicon.NODE_SELECTED_PAINT) || (vp == BasicVisualLexicon.EDGE_LABEL_COLOR)
                    || (vp == BasicVisualLexicon.EDGE_PAINT) || (vp == BasicVisualLexicon.EDGE_SELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_UNSELECTED_PAINT)) {
                cvp.put(obtainLabel(vp), processColor((Color) vp_value));
            }
            else if ((vp == BasicVisualLexicon.NODE_LABEL_FONT_FACE) || (vp == BasicVisualLexicon.EDGE_LABEL_FONT_FACE)) {
                processFont(cvp, "label", (Font) vp_value);
            }
            else {
                final String value_str = String.valueOf(vp_value);
                if (!Util.isEmpty(value_str)) {
                    cvp.put(obtainLabel(vp), value_str);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private final static String obtainLabel(final VisualProperty vp) {
        final String label = M.get(vp);
        if (label == null) {
            throw new IllegalStateException("no label found for " + vp);
        }
        return label;
    }

    @SuppressWarnings("rawtypes")
    public static final void obtainVisualProperties(final CyNetworkView view,
                                                    final CyNetwork network,
                                                    final VisualMappingManager visual_mapping_manager,
                                                    final VisualPropertiesElement node_visual_properties,
                                                    final VisualPropertiesElement network_visual_properties,
                                                    final VisualPropertiesElement edge_visual_properties) {

        final VisualStyle current_visual_style = visual_mapping_manager.getVisualStyle(view);

        final VisualProperties node_default_cxvp = new VisualProperties("nodes default", "X");
        for (final VisualProperty visual_property : VisualPropertiesWriter.NODE_VISUAL_PROPERTIES) {
            addProperties(current_visual_style, visual_property, node_default_cxvp);
        }
        node_visual_properties.addProperties(node_default_cxvp);
        
        final VisualProperties edge_default_cxvp = new VisualProperties("edges default", "X");
        for (final VisualProperty visual_property : VisualPropertiesWriter.EDGE_VISUAL_PROPERTIES) {
            addProperties(current_visual_style, visual_property, edge_default_cxvp);
        }
        edge_visual_properties.addProperties(edge_default_cxvp);
        
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            final VisualProperties node_cxvp = new VisualProperties("nodes", String.valueOf(cy_node.getSUID()));
            for (final VisualProperty visual_property : VisualPropertiesWriter.NODE_VISUAL_PROPERTIES) {
                addProperties(node_view, visual_property,  node_cxvp);
            }
            node_visual_properties.addProperties( node_cxvp);
        }

        for (final CyEdge edge : network.getEdgeList()) {
            final View<CyEdge> edge_view = view.getEdgeView(edge);
            final VisualProperties edge_cxvp = new VisualProperties("edges", String.valueOf(edge.getSUID()));
            for (final VisualProperty visual_property : VisualPropertiesWriter.EDGE_VISUAL_PROPERTIES) {
                addProperties(edge_view, visual_property, edge_cxvp);
            }
            edge_visual_properties.addProperties(edge_cxvp);
        }

        final VisualProperties cvp = new VisualProperties("network", String.valueOf(network.getSUID()));

        cvp.put("background_paint", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT)));
        cvp.put("center_x_location",
                String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)));
        cvp.put("center_y_location",
                String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)));
        cvp.put("center_z_location",
                String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION)));
        cvp.put("depth", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_DEPTH)));
        cvp.put("height", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT)));
        cvp.put("scale_factor", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR)));
        cvp.put("size", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_SIZE)));
        cvp.put("title", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE)));
        cvp.put("width", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH)));
        network_visual_properties.addProperties(cvp);

    }

}
