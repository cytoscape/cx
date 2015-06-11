package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.internal.cxio.kit.AspectFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.EdgeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.EdgesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.NodesFragmentWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxNetworkWriter implements CyWriter {

    private final static Logger             logger   = LoggerFactory
                                                             .getLogger(CxNetworkWriter.class);
    private final static String             ENCODING = "UTF-8";

    private final OutputStream              outputStream;
    private final CyNetwork                 network;
    private final CharsetEncoder            encoder;
    private final Set<AspectFragmentWriter> aspect_fragment_writers;

    public CxNetworkWriter(final OutputStream outputStream, final CyNetwork network,
                           final Set<AspectFragmentWriter> aspect_fragment_writers) {
        this.outputStream = outputStream;
        this.network = network;
        this.aspect_fragment_writers = aspect_fragment_writers;

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

    public CxNetworkWriter(final OutputStream outputStream, final CyNetwork network) {
        this.outputStream = outputStream;
        this.network = network;
        this.aspect_fragment_writers = new HashSet<AspectFragmentWriter>();
        aspect_fragment_writers.add(NodesFragmentWriter.createInstance());
        aspect_fragment_writers.add(EdgesFragmentWriter.createInstance());
        aspect_fragment_writers.add(NodeAttributesFragmentWriter.createInstance());
        aspect_fragment_writers.add(EdgeAttributesFragmentWriter.createInstance());

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

        final CyToCxImpl w = new CyToCxImpl();
        for (final AspectFragmentWriter aspect_fragment_writer : aspect_fragment_writers) {
            w.addAspectFragmentWriter(aspect_fragment_writer);
        }

        w.serializeCyNetwork(network, outputStream);

        outputStream.close();
    }

    @Override
    public void cancel() {
        if (outputStream == null) {
            return;
        }

        try {
            outputStream.close();
        }
        catch (final IOException e) {
            logger.error("Could not close Outputstream for CxNetworkWriter.", e);
        }
    }

}
