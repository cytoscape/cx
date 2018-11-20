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
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
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
	private static final boolean USE_CXID_DEFAULT = false;
	private final static String ENCODING = "UTF-8";

	private final OutputStream _os;
	private final CyNetwork _network;
	private final CharsetEncoder _encoder;
	private final VisualMappingManager _visual_mapping_manager;
	private final CyNetworkViewManager _networkview_manager;
	private final CyGroupManager _group_manager;
	private final CyApplicationManager _application_manager;
	
	@Tunable(description="Write all networks in the collection")
    public Boolean writeSiblings = WRITE_SIBLINGS_DEFAULT;
	
	@Tunable(description="Use CX ID")
    public Boolean useCxId = USE_CXID_DEFAULT;
	
	

	public CxNetworkWriter(final OutputStream os, 
			final CyNetwork network,
			final VisualMappingManager visual_mapping_manager, 
			final CyNetworkViewManager networkview_manager,
			final CyGroupManager group_manager,
			final CyApplicationManager application_manager,
			final boolean write_siblings) {

		_visual_mapping_manager = visual_mapping_manager;
		_networkview_manager = networkview_manager;
		_os = os;
		_network = network;
		_group_manager = group_manager;
		_application_manager = application_manager;
		writeSiblings = write_siblings;

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

		final CxExporter exporter = CxExporter.createInstance();
		exporter.setApplicationManager(_application_manager);
		exporter.setVisualMappingManager(_visual_mapping_manager);
		exporter.setNetworkViewManager(_networkview_manager);
		exporter.setGroupManager(_group_manager);

		final long t0 = System.currentTimeMillis();
		if (TimingUtil.WRITE_TO_DEV_NULL) {
			exporter.writeNetwork(_network, writeSiblings, aspects, new FileOutputStream(new File("/dev/null")));
		} else if (TimingUtil.WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM) {
			exporter.writeNetwork(_network, writeSiblings, aspects, new ByteArrayOutputStream());
		} else {
			exporter.writeNetwork(_network, writeSiblings, aspects, _os);
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
