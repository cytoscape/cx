package org.cytoscape.io.internal.cx_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxNetworkWriter implements CyWriter {

    private final static Logger  logger   = LoggerFactory.getLogger(CxNetworkWriter.class);
    private final static String  ENCODING = "UTF-8";

    private final OutputStream   os;
    private final CyNetwork      network;
    private final CharsetEncoder encoder;

    public CxNetworkWriter(final OutputStream os, final CyNetwork network) {
        this.os = os;
        this.network = network;

        if (Charset.isSupported(ENCODING)) {
            // UTF-8 is supported by system
            this.encoder = Charset.forName(ENCODING).newEncoder();
        }
        else {
            // Use default.
            logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
            this.encoder = Charset.defaultCharset().newEncoder();
        }
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {
        if (taskMonitor != null) {
            taskMonitor.setProgress(0.0);
            taskMonitor.setTitle("Exporting to CX");
            taskMonitor.setStatusMessage("Exporting current network as CX...");
        }

        System.out.println("Encoding = " + encoder.charset());

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);

        final CxExporter exporter = CxExporter.createInstance();
        exporter.setUseDefaultPrettyPrinting(true);

        final long t0 = System.currentTimeMillis();

        if (TimingUtil.WRITE_TO_DEV_NULL) {
            exporter.writeCX(network, aspects, new FileOutputStream(new File("/dev/null")));
        }
        else if (TimingUtil.WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM) {
            exporter.writeCX(network, aspects, new ByteArrayOutputStream());
        }
        else {
            exporter.writeCX(network, aspects, os);
            os.close();
        }

        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "total time", 0);
        }
    }

    @Override
    public void cancel() {
        if (os == null) {
            return;
        }

        try {
            os.close();
        }
        catch (final IOException e) {
            logger.error("Could not close Outputstream for CxNetworkWriter.", e);
        }
    }

}
