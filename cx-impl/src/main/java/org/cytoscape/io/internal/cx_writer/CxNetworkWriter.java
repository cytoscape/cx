package org.cytoscape.io.internal.cx_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.internal.cxio.Util;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxNetworkWriter implements CyWriter {

    private final static Logger         logger   = LoggerFactory.getLogger(CxNetworkWriter.class);
    private final static String         ENCODING = "UTF-8";
    private static final boolean        DEBUG    = true;

    private final OutputStream          _os;
    private final CyNetwork             _network;
    private final CharsetEncoder        _encoder;
    private final VisualMappingManager  _visual_mapping_manager;
    private final VisualLexicon         _lexicon;
    private final CustomGraphicsManager _custom_graphics_manager;
    private final CyNetworkViewManager  _networkview_manager;
    private final CyNetworkManager      _network_manager;
    private final CyGroupManager _group_manager;

    public CxNetworkWriter(final OutputStream os,
                           final CyNetwork network,
                           final VisualMappingManager visual_mapping_manager,
                           final CustomGraphicsManager custom_graphics_manager,
                           final CyNetworkViewManager networkview_manager,
                           final CyNetworkManager network_manager,
                           final CyGroupManager group_manager,
                           final VisualLexicon lexicon) {

        _visual_mapping_manager = visual_mapping_manager;
        _custom_graphics_manager = custom_graphics_manager;
        _networkview_manager = networkview_manager;
        _network_manager = network_manager;
        _lexicon = lexicon;
        _os = os;
        _network = network;
        _group_manager = group_manager;

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

    public CxNetworkWriter(final OutputStream os, final CyNetwork network) {

        _visual_mapping_manager = null;
        _custom_graphics_manager = null;
        _networkview_manager = null;
        _network_manager = null;
        _lexicon = null;
        _os = os;
        _network = network;
        _group_manager =null;

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

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {
        if (taskMonitor != null) {
            taskMonitor.setProgress(0.0);
            taskMonitor.setTitle("Exporting to CX");
            taskMonitor.setStatusMessage("Exporting current network as CX...");
        }

        if (DEBUG) {
            System.out.println("Encoding = " + _encoder.charset());
        }

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);
        aspects.addAspect(Aspect.SUBNETWORKS);
        aspects.addAspect(Aspect.NETWORK_RELATIONS);
        aspects.addAspect(Aspect.GROUPS);

        final CxExporter exporter = CxExporter.createInstance();
        exporter.setUseDefaultPrettyPrinting(true);
        exporter.setLexicon(_lexicon);
        exporter.setVisualMappingManager(_visual_mapping_manager);
        exporter.setNetworkViewManager(_networkview_manager);
        exporter.setNetworkManager(_network_manager);
        exporter.setGroupManager(_group_manager);

        final String time_stamp = Util.makeTimeStamp();

        final long t0 = System.currentTimeMillis();

        if (TimingUtil.WRITE_TO_DEV_NULL) {
            exporter.writeNetwork(_network, aspects, new FileOutputStream(new File("/dev/null")), time_stamp);
        }
        else if (TimingUtil.WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM) {
            exporter.writeNetwork(_network, aspects, new ByteArrayOutputStream(), time_stamp);
        }
        else {
            exporter.writeNetwork(_network, aspects, _os, time_stamp);
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
            logger.error("Could not close Outputstream for CxNetworkWriter.", e);
        }
    }

}
