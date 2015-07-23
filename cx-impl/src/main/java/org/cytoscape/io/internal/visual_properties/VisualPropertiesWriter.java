package org.cytoscape.io.internal.visual_properties;

import java.awt.Color;
import java.awt.Font;

import org.cxio.aspects.datamodels.CytoscapeVisualProperties;
import org.cxio.aspects.datamodels.CytoscapeVisualStyleElement;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class VisualPropertiesWriter {

    private final static String mapColor(final Color c) {
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

    private final static void mapFont(final CytoscapeVisualProperties cvp, final String suffix, final Font f) {
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

    public static void obtainVisualProperties(final CyNetworkView view,
                                              final CyNetwork network,
                                              final CytoscapeVisualStyleElement node_visual_properties,
                                              final CytoscapeVisualStyleElement network_visual_properties,
                                              final CytoscapeVisualStyleElement edge_visual_properties) {
        for (final CyNode cy_node : network.getNodeList()) {
            final View<CyNode> node_view = view.getNodeView(cy_node);
            final CytoscapeVisualProperties cvp = new CytoscapeVisualProperties("nodes", String.valueOf(cy_node.getSUID()));
            cvp.put("size", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_SIZE)));
            cvp.put("border_line_type",
                    String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE)));
            cvp.put("border_paint", mapColor((Color) node_view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT)));
            cvp.put("border_transparency",
                    String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY)));
            cvp.put("border_width", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH)));
            cvp.put("depth", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_DEPTH)));
            cvp.put("fill_color", mapColor((Color) node_view.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR)));
            cvp.put("label_color", mapColor((Color) node_view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR)));
            mapFont(cvp, "label", node_view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE));
            cvp.put("label_font_size",
                    String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE)));
            cvp.put("label_transparency",
                    String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY)));
            cvp.put("label_width", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_WIDTH)));
            cvp.put("fill_color", mapColor((Color) node_view.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR)));
            cvp.put("height", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT)));
            cvp.put("paint", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_PAINT)));
            cvp.put("shape", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_SHAPE)));
            cvp.put("tooltip", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_TOOLTIP)));
            cvp.put("transparency", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY)));
            cvp.put("visible", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)));
            cvp.put("width", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_WIDTH)));
            cvp.put("x_location", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)));
            cvp.put("y_location", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));
            cvp.put("z_location", String.valueOf(node_view.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION)));

            node_visual_properties.addProperties(cvp);

        }

        for (final CyEdge edge : network.getEdgeList()) {
            final View<CyEdge> edge_view = view.getEdgeView(edge);
            final CytoscapeVisualProperties cvp = new CytoscapeVisualProperties("edges",String.valueOf(edge.getSUID()));
            cvp.put("size", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_BEND)));
            cvp.put("label", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL)));
            cvp.put("label_color", mapColor((Color) edge_view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_COLOR)));
            mapFont(cvp, "label", edge_view.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE));
            cvp.put("label_font_size",
                    String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE)));
            cvp.put("label_transparency",
                    String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY)));
            cvp.put("label_width", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_WIDTH)));
            cvp.put("line_type", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE)));
            cvp.put("paint", mapColor((Color) edge_view.getVisualProperty(BasicVisualLexicon.EDGE_PAINT)));
            cvp.put("selected", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_SELECTED)));
            cvp.put("selected_paint",
                    mapColor((Color) edge_view.getVisualProperty(BasicVisualLexicon.EDGE_SELECTED_PAINT)));
            cvp.put("source_arrow_shape",
                    String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE)));
            cvp.put("stroke_selected_paint",
                    mapColor((Color) edge_view.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT)));
            cvp.put("stroke_unselected_paint",
                    mapColor((Color) edge_view.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT)));
            cvp.put("target_arrow_shape",
                    String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE)));
            cvp.put("tooltip", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_TOOLTIP)));
            cvp.put("transparency", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY)));
            cvp.put("unselected_paint",
                    mapColor((Color) edge_view.getVisualProperty(BasicVisualLexicon.EDGE_UNSELECTED_PAINT)));
            cvp.put("visible", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE)));
            cvp.put("width", String.valueOf(edge_view.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH)));
            edge_visual_properties.addProperties(cvp);
        }
        

        final CytoscapeVisualProperties cvp = new CytoscapeVisualProperties("network", String.valueOf(network.getSUID()));
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
