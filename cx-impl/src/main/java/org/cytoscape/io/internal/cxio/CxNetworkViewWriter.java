package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.internal.cxio.kit.AspectFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.CartesianLayoutFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.EdgeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.EdgesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.NodesFragmentWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxNetworkViewWriter implements CyWriter {

    private final static Logger             logger   = LoggerFactory
                                                             .getLogger(CxNetworkViewWriter.class);
    private final static String             ENCODING = "UTF-8";

    private final OutputStream              os;
    private final CyNetwork                 network;
    private final CyNetworkView             network_view;
    private final CharsetEncoder            encoder;
    private final Set<AspectFragmentWriter> aspect_fragment_writers;

    public CxNetworkViewWriter(final OutputStream os, final CyNetworkView network_view,
                               final Set<AspectFragmentWriter> aspect_fragment_writers) {
        this.os = os;
        this.network_view = network_view;
        this.network = network_view.getModel();
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

    public CxNetworkViewWriter(final OutputStream os, final CyNetworkView network_view) {
        this.os = os;
        this.network_view = network_view;
        this.network = network_view.getModel();
        this.aspect_fragment_writers = new HashSet<AspectFragmentWriter>();
        aspect_fragment_writers.add(NodesFragmentWriter.createInstance());
        aspect_fragment_writers.add(EdgesFragmentWriter.createInstance());
        aspect_fragment_writers.add(NodeAttributesFragmentWriter.createInstance());
        aspect_fragment_writers.add(EdgeAttributesFragmentWriter.createInstance());
        aspect_fragment_writers.add(CartesianLayoutFragmentWriter.createInstance());

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

        w.serializeCyNetworkView(network, network_view, os);

        os.close();
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
