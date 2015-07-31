package org.cytoscape.io.internal.cx_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxNetworkViewWriter implements CyWriter {

    private final static Logger         logger   = LoggerFactory.getLogger(CxNetworkViewWriter.class);
    private final static String         ENCODING = "UTF-8";
    private final OutputStream          _os;
    private final CyNetworkView         _network_view;
    private final CharsetEncoder        _encoder;
    private final VisualMappingManager  _visual_mapping_manager;
    private final VisualLexicon         _lexicon;
    private final CustomGraphicsManager _custom_graphics_manager;

    public CxNetworkViewWriter(final OutputStream os, final CyNetworkView network_view) {
        _os = os;
        _network_view = network_view;
        _visual_mapping_manager = null;
        _custom_graphics_manager = null;
        _lexicon = null;
        if (Charset.isSupported(ENCODING)) {
            // UTF-8 is supported by system
            _encoder = Charset.forName(ENCODING).newEncoder();
        }
        else {
            // Use default.
            logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
            _encoder = Charset.defaultCharset().newEncoder();
        }
    }

    public CxNetworkViewWriter(final OutputStream os,
                               final CyNetworkView network_view,
                               final VisualMappingManager visual_mapping_manager,
                               final CustomGraphicsManager custom_graphics_manager,
                               final VisualLexicon lexicon) {
        _os = os;
        _network_view = network_view;
        _visual_mapping_manager = visual_mapping_manager;
        _custom_graphics_manager = custom_graphics_manager;
        _lexicon = lexicon;
        if (Charset.isSupported(ENCODING)) {
            // UTF-8 is supported by system
            _encoder = Charset.forName(ENCODING).newEncoder();
        }
        else {
            // Use default.
            logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
            _encoder = Charset.defaultCharset().newEncoder();
        }

        // Collection<CyCustomGraphics> x =
        // _custom_graphics_manager.getAllCustomGraphics();
        // for (CyCustomGraphics cyCustomGraphics : x) {
        // Image i = cyCustomGraphics.getRenderedImage();
        //
        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // try {
        // ImageIO.write((RenderedImage) i, "png", baos);
        // }
        // catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // baos.toByteArray();
        //
        // }
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {
        if (taskMonitor != null) {
            taskMonitor.setProgress(0.0);
            taskMonitor.setTitle("Exporting to CX");
            taskMonitor.setStatusMessage("Exporting current network view as CX...");
        }

        System.out.println("Encoding = " + _encoder.charset());

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);

        // final AspectKeyFilter na_filter = new
        // AspectKeyFilterBasic(NodeAttributesElement.NAME);
        // final AspectKeyFilter ea_filter = new
        // AspectKeyFilterBasic(EdgeAttributesElement.NAME);

        // na_filter.addExcludeAspectKey("name");
        // ea_filter.addExcludeAspectKey("selected");
        // ea_filter.addExcludeAspectKey("SUID");
        // final Set<AspectKeyFilter> filters = new HashSet<AspectKeyFilter>();
        // filters.add(na_filter);
        // filters.add(ea_filter);

        final CxExporter exporter = CxExporter.createInstance();
        exporter.setUseDefaultPrettyPrinting(true);
        exporter.setLexicon(_lexicon);
        exporter.setVisualMappingManager(_visual_mapping_manager);
        final long t0 = System.currentTimeMillis();
        if (TimingUtil.WRITE_TO_DEV_NULL) {
            exporter.writeCX(_network_view, aspects, new FileOutputStream(new File("/dev/null")));
        }
        else if (TimingUtil.WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM) {
            exporter.writeCX(_network_view, aspects, new ByteArrayOutputStream());
        }
        else {
            exporter.writeCX(_network_view, aspects, _os);
            _os.close();
        }

        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "total time", 0);
        }
    }

    @Override
    public void cancel() {
        if (_os == null) {
            return;
        }

        try {
            _os.close();
        }
        catch (final IOException e) {
            logger.error("Could not close Outputstream for CxNetworkViewWriter.", e);
        }
    }

}
