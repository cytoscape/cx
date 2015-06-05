package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CxNetworkWriter implements CyWriter {

	private final static Logger logger = LoggerFactory
			.getLogger(CxNetworkWriter.class);
	private final static String ENCODING = "UTF-8";

	private final OutputStream outputStream;
	private final CyNetwork network;
	private final CharsetEncoder encoder;

	public CxNetworkWriter(final OutputStream outputStream,
			final CyNetwork network) {
		this.outputStream = outputStream;
		this.network = network;

		if (Charset.isSupported(ENCODING)) {
			// UTF-8 is supported by system
			this.encoder = Charset.forName(ENCODING).newEncoder();
		} else {
			// Use default.
			logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
			this.encoder = Charset.defaultCharset().newEncoder();
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (taskMonitor != null) {
			taskMonitor.setProgress(0.0);
			taskMonitor.setTitle("Exporting to CX");
			taskMonitor.setStatusMessage("Exporting current network as CX...");
		}

		System.out.println("Encoding = " + encoder.charset());

		final CyToCxImpl w = new CyToCxImpl();
		w.serializeCyNetwork(network, outputStream);

		outputStream.close();
	}

	@Override
	public void cancel() {
		if (outputStream == null)
			return;

		try {
			outputStream.close();
		} catch (IOException e) {
			logger.error("Could not close Outputstream for CxNetworkWriter.", e);
		}
	}
}
