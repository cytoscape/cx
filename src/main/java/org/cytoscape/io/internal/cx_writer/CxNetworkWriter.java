package org.cytoscape.io.internal.cx_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an example on how to use CxExporter in a Cytoscape task.
 *
 * @author cmzmasek
 *
 */
public class CxNetworkWriter implements CyWriter {

	private final static Logger logger = LoggerFactory.getLogger(CxNetworkWriter.class);
	private static final boolean WRITE_SIBLINGS_DEFAULT = false;
	private static final boolean USE_CXID_DEFAULT = true;
	private final static String ENCODING = "UTF-8";

	private final OutputStream _os;
	private final CyNetwork _network;
	private final CharsetEncoder _encoder;	
	
	@Tunable(description="Write all networks in the collection")
    public boolean writeSiblings = WRITE_SIBLINGS_DEFAULT;
	
	public boolean useCxId = USE_CXID_DEFAULT;
	
	@Tunable(description="Use CX ID", dependsOn="writeSiblings=false", listenForChange="writeSiblings")
    public boolean getUseCxId() {
		if (writeSiblings) {
			return false;
		}
		final CyApplicationManager _application_manager = CyServiceModule.getService(CyApplicationManager.class);

		if (!CxUtil.hasCxIds(_application_manager.getCurrentNetwork())) {
			return false;
		}
		return useCxId;
	}
	
	public void setUseCxId(boolean useCxId) {
		this.useCxId = useCxId;
	}
	
	

	public CxNetworkWriter(final OutputStream os, 
			final CyNetwork network,
			final boolean writeSiblings,
			final boolean use_cxId) {

		_os = os;
		_network = network;
		this.writeSiblings = writeSiblings;
		setUseCxId(use_cxId);

		if (Charset.isSupported(ENCODING)) {
			// UTF-8 is supported by system
			_encoder = Charset.forName(ENCODING).newEncoder();
		} else {
			// Use default.
			logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
			_encoder = Charset.defaultCharset().newEncoder();
		}
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws FileNotFoundException, IOException {
		if (taskMonitor != null) {
			taskMonitor.setProgress(0.0);
			taskMonitor.setTitle("Exporting to CX");
			taskMonitor.setStatusMessage("Exporting current network as CX...");
		}

		Settings.INSTANCE.debug("Encoding = " + _encoder.charset());

		final AspectSet aspects = AspectSet.getCytoscapeAspectSet();

		final CxExporter exporter = new CxExporter(_network, writeSiblings, useCxId);

		final long t0 = System.currentTimeMillis();
		if (TimingUtil.WRITE_TO_DEV_NULL) {
			exporter.writeNetwork(aspects, new FileOutputStream(new File("/dev/null")));
		} else if (TimingUtil.WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM) {
			exporter.writeNetwork(aspects, new ByteArrayOutputStream());
		} else {
			exporter.writeNetwork(aspects, _os);
			_os.close();

		}

		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "total time", -1);
		}
	}

	@Override
	public void cancel() {
		if (_os == null) {
			return;
		}

		try {
			_os.close();
		} catch (final IOException e) {
			logger.error("Could not close Outputstream for CxNetworkWriter.", e);
		}
	}

	public void setWriteSiblings(final boolean write_siblings) {
		writeSiblings = write_siblings;
	}

}
