package org.cytoscape.io.internal.visual_properties;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public final class VisualPropertiesWriter {

    public final static String                       NESTED_NETWORK_VISIBLE = "nested_network_visible";
    public final static String                       BORDER_TRANSPARENCY    = "border_transparency";
    public final static String                       BORDER_WIDTH           = "border_width";
    public final static String                       DEPTH                  = "depth";
    public final static String                       LABEL                  = "label";
    public final static String                       LABEL_TRANSPARENCY     = "label_transparency";
    public final static String                       LABEL_WIDTH            = "label_width";
    public final static String                       SELECTED               = "selected";
    public final static String                       TOOLTIP                = "tooltip";
    public final static String                       SHAPE                  = "shape";
    public final static String                       TRANSPARENCY           = "transparency";
    public final static String                       VISIBLE                = "visible";
    public final static String                       WIDTH                  = "width";
    public final static String                       HEIGHT                 = "height";
    public final static String                       Z_LOCATION             = "z_location";
    public final static String                       Y_LOCATION             = "y_location";
    public final static String                       X_LOCATION             = "x_location";
    public final static String                       NODES                  = "nodes";
    public final static String                       EDGES                  = "edges";

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
        M.put(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, BORDER_TRANSPARENCY);
        M.put(BasicVisualLexicon.NODE_BORDER_WIDTH, BORDER_WIDTH);
        M.put(BasicVisualLexicon.NODE_DEPTH, DEPTH);
        M.put(BasicVisualLexicon.NODE_FILL_COLOR, "fill_color");
        M.put(BasicVisualLexicon.NODE_HEIGHT, HEIGHT);
        M.put(BasicVisualLexicon.NODE_LABEL, LABEL);
        M.put(BasicVisualLexicon.NODE_LABEL_COLOR, "label_color");
        M.put(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, "label_font_size");
        M.put(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, LABEL_TRANSPARENCY);
        M.put(BasicVisualLexicon.NODE_LABEL_WIDTH, LABEL_WIDTH);
        M.put(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE, NESTED_NETWORK_VISIBLE);
        M.put(BasicVisualLexicon.NODE_PAINT, "color");
        M.put(BasicVisualLexicon.NODE_SELECTED, SELECTED);
        M.put(BasicVisualLexicon.NODE_SELECTED_PAINT, "selected_color");
        M.put(BasicVisualLexicon.NODE_SHAPE, SHAPE);
        M.put(BasicVisualLexicon.NODE_SIZE, "size");
        M.put(BasicVisualLexicon.NODE_TOOLTIP, TOOLTIP);
        M.put(BasicVisualLexicon.NODE_TRANSPARENCY, TRANSPARENCY);
        M.put(BasicVisualLexicon.NODE_VISIBLE, VISIBLE);
        M.put(BasicVisualLexicon.NODE_WIDTH, WIDTH);
        M.put(BasicVisualLexicon.NODE_X_LOCATION, X_LOCATION);
        M.put(BasicVisualLexicon.NODE_Y_LOCATION, Y_LOCATION);
        M.put(BasicVisualLexicon.NODE_Z_LOCATION, Z_LOCATION);

        // Mappings for edges:
        M.put(BasicVisualLexicon.EDGE_LABEL, LABEL);
        M.put(BasicVisualLexicon.EDGE_LABEL_COLOR, "label_color");
        M.put(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, "label_font_size");
        M.put(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, LABEL_TRANSPARENCY);
        M.put(BasicVisualLexicon.EDGE_LABEL_WIDTH, LABEL_WIDTH);
        M.put(BasicVisualLexicon.EDGE_LINE_TYPE, "line_type");
        M.put(BasicVisualLexicon.EDGE_PAINT, "color");
        M.put(BasicVisualLexicon.EDGE_SELECTED, SELECTED);
        M.put(BasicVisualLexicon.EDGE_SELECTED_PAINT, "selected_color");
        M.put(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, "source_arrow_shape");
        M.put(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, "stroke_selected_color");
        M.put(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, "stroke_unselected_paint");
        M.put(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, "target_arrow_shape");
        M.put(BasicVisualLexicon.EDGE_TOOLTIP, "tooltip");
        M.put(BasicVisualLexicon.EDGE_TRANSPARENCY, TRANSPARENCY);
        M.put(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, "unselected_color");
        M.put(BasicVisualLexicon.EDGE_VISIBLE, VISIBLE);
        M.put(BasicVisualLexicon.EDGE_WIDTH, WIDTH);
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

    private final static void processFont(final VisualPropertiesElement cvp, final String suffix, final Font f) {
        cvp.putProperty(suffix + "_font_family", f.getFamily());
        if (f.isPlain()) {
            cvp.putProperty(suffix + "_font_style", "plain");
        }
        else if (f.isBold() && f.isItalic()) {
            cvp.putProperty(suffix + "_font_style", "bold_italic");
        }
        else if (f.isBold()) {
            cvp.putProperty(suffix + "_font_style", "bold");
        }
        else {
            cvp.putProperty(suffix + "_font_style", "italic");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final View view,
                                            final VisualProperty vp,
                                            final VisualLexicon lexicon,
                                            final VisualPropertiesElement cvp) {
        final Object vp_value = view.getVisualProperty(vp);

        if (vp_value != null) {
            // if ((vp == BasicVisualLexicon.NODE_BORDER_PAINT) || (vp ==
            // BasicVisualLexicon.NODE_FILL_COLOR)
            // || (vp == BasicVisualLexicon.NODE_LABEL_COLOR) || (vp ==
            // BasicVisualLexicon.NODE_PAINT)
            // || (vp == BasicVisualLexicon.NODE_SELECTED_PAINT) || (vp ==
            // BasicVisualLexicon.EDGE_LABEL_COLOR)
            // || (vp == BasicVisualLexicon.EDGE_PAINT) || (vp ==
            // BasicVisualLexicon.EDGE_SELECTED_PAINT)
            // || (vp == BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)
            // || (vp == BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)
            // || (vp == BasicVisualLexicon.EDGE_UNSELECTED_PAINT)) {
            // cvp.putProperty(obtainLabel(vp), processColor((Color) vp_value));
            //
            // }
            // if ((vp == BasicVisualLexicon.NODE_LABEL_FONT_FACE) || (vp ==
            // BasicVisualLexicon.EDGE_LABEL_FONT_FACE)) {
            // processFont(cvp, LABEL, (Font) vp_value);
            // }
            // else {

            // final VisualProperty vpl = lexicon.lookup(CyNode.class,
            // BasicVisualLexicon.NODE_SHAPE.getIdString());

            final String value_str = vp.toSerializableString(vp_value);

            // final String value_str = String.valueOf(vp_value);
            if (!Util.isEmpty(value_str)) {
                cvp.putProperty(vp.getIdString(), value_str);
            }
        }
        // }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final static void addProperties(final VisualStyle style,
                                            final VisualProperty vp,
                                            final VisualPropertiesElement cvp) {
        final Object vp_value = style.getDefaultValue(vp);
        if (vp_value != null) {
            if ((vp == BasicVisualLexicon.NODE_BORDER_PAINT) || (vp == BasicVisualLexicon.NODE_FILL_COLOR)
                    || (vp == BasicVisualLexicon.NODE_LABEL_COLOR) || (vp == BasicVisualLexicon.NODE_PAINT)
                    || (vp == BasicVisualLexicon.NODE_SELECTED_PAINT) || (vp == BasicVisualLexicon.EDGE_LABEL_COLOR)
                    || (vp == BasicVisualLexicon.EDGE_PAINT) || (vp == BasicVisualLexicon.EDGE_SELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)
                    || (vp == BasicVisualLexicon.EDGE_UNSELECTED_PAINT)) {
                cvp.putProperty(obtainLabel(vp), processColor((Color) vp_value));
            }
            else if ((vp == BasicVisualLexicon.NODE_LABEL_FONT_FACE) || (vp == BasicVisualLexicon.EDGE_LABEL_FONT_FACE)) {
                processFont(cvp, LABEL, (Font) vp_value);
            }
            else {
                final String value_str = String.valueOf(vp_value);
                if (!Util.isEmpty(value_str)) {
                    cvp.putProperty(obtainLabel(vp), value_str);
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
                                                    final VisualLexicon lexicon,
                                                    final List<AspectElement> visual_properties) {

        final VisualStyle current_visual_style = visual_mapping_manager.getVisualStyle(view);

        final VisualPropertiesElement node_default_cxvp = new VisualPropertiesElement("nodes:default");
        for (final VisualProperty visual_property : VisualPropertiesWriter.NODE_VISUAL_PROPERTIES) {
            addProperties(current_visual_style, visual_property, node_default_cxvp);
        }
        visual_properties.add(node_default_cxvp);

        final VisualPropertiesElement edge_default_cxvp = new VisualPropertiesElement("edges:default");
        for (final VisualProperty visual_property : VisualPropertiesWriter.EDGE_VISUAL_PROPERTIES) {
            addProperties(current_visual_style, visual_property, edge_default_cxvp);
        }
        visual_properties.add(edge_default_cxvp);

        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            final VisualPropertiesElement node_cxvp = new VisualPropertiesElement(NODES);
            node_cxvp.addAppliesTo(String.valueOf(cy_node.getSUID()));
            for (final VisualProperty visual_property : VisualPropertiesWriter.NODE_VISUAL_PROPERTIES) {
                addProperties(node_view, visual_property, lexicon, node_cxvp);
            }
            visual_properties.add(node_cxvp);
        }

        for (final CyEdge edge : network.getEdgeList()) {
            final View<CyEdge> edge_view = view.getEdgeView(edge);
            final VisualPropertiesElement edge_cxvp = new VisualPropertiesElement("edges");

            edge_cxvp.addAppliesTo(String.valueOf(edge.getSUID()));
            for (final VisualProperty visual_property : VisualPropertiesWriter.EDGE_VISUAL_PROPERTIES) {
                addProperties(edge_view, visual_property, lexicon, edge_cxvp);
            }
            visual_properties.add(edge_cxvp);
        }

        final VisualPropertiesElement cvp = new VisualPropertiesElement("network");

        cvp.putProperty("background_paint",
                        String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT)));
        cvp.putProperty("center_x_location",
                        String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION)));
        cvp.putProperty("center_y_location",
                        String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION)));
        cvp.putProperty("center_z_location",
                        String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION)));
        cvp.putProperty(DEPTH, String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_DEPTH)));
        cvp.putProperty(HEIGHT, String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT)));
        cvp.putProperty("scale_factor", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR)));
        cvp.putProperty("size", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_SIZE)));
        cvp.putProperty("title", String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE)));
        cvp.putProperty(WIDTH, String.valueOf(view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH)));
        visual_properties.add(cvp);

    }

}
