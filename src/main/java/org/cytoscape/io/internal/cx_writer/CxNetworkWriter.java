package org.cytoscape.io.internal.cx_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.AspectSet;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
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

	public ListMultipleSelection<String> aspectFilter = new ListMultipleSelection<>();
	public ListMultipleSelection<String> nodeColFilter = new ListMultipleSelection<>();
	public ListMultipleSelection<String> edgeColFilter = new ListMultipleSelection<>();
	public ListMultipleSelection<String> networkColFilter = new ListMultipleSelection<>();

	@Tunable(description = "Aspects")
	public ListMultipleSelection<String> getAspectFilter() {
		return aspectFilter;
	}

	@Tunable(description = "Node Columns")
	public ListMultipleSelection<String> getNodeColFilter() {
		return nodeColFilter;
	}

	@Tunable(description = "Edge Columns")
	public ListMultipleSelection<String> getEdgeColFilter() {
		return edgeColFilter;
	}

	@Tunable(description = "Network Columns")
	public ListMultipleSelection<String> getNetworkColFilter() {
		return networkColFilter;
	}

	@Tunable(description = "Write all networks in the collection")
	public boolean writeSiblings = WRITE_SIBLINGS_DEFAULT;

	public boolean useCxId = USE_CXID_DEFAULT;

	@Tunable(description = "Use CX ID (recommended)", dependsOn = "writeSiblings=false", listenForChange = "writeSiblings")
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

	public CxNetworkWriter(final OutputStream os, final CyNetwork network, final boolean writeSiblings,
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

		populateFilters();
	}

	private void populateFilters() {
		ArrayList<String> aspects = AspectSet.getAspectNames();
		aspectFilter.setPossibleValues(aspects);

		// Node Column filter
		final ArrayList<String> nodeColumnNames = getAllColumnNames(CyNode.class);
		nodeColFilter.setPossibleValues(nodeColumnNames);
		nodeColFilter.setSelectedValues(nodeColumnNames);

		// Edge Column filter
		final ArrayList<String> edgeColumnNames = getAllColumnNames(CyEdge.class);
		edgeColFilter.setPossibleValues(edgeColumnNames);
		edgeColFilter.setSelectedValues(edgeColumnNames);

		// Network Column filter
		final ArrayList<String> networkColumnNames = getAllColumnNames(CyNetwork.class);
		networkColFilter.setPossibleValues(networkColumnNames);
		networkColFilter.setSelectedValues(networkColumnNames);
	}

	private final ArrayList<String> getAllColumnNames(final Class<? extends CyIdentifiable> type) {

		// Shared
		final CyTable sharedTable = _network.getTable(type, CyNetwork.DEFAULT_ATTRS);

		// Local
		final CyTable localTable = _network.getTable(type, CyNetwork.LOCAL_ATTRS);

		final SortedSet<String> colNames = new TreeSet<>();

		colNames.addAll(sharedTable.getColumns().stream().map(col -> col.getName()).collect(Collectors.toList()));
		colNames.addAll(localTable.getColumns().stream().map(col -> col.getName()).collect(Collectors.toList()));

		if (type == CyNetwork.class) {
			// Add Root table to available column names
			final CyTable rootTable = ((CySubNetwork) _network).getRootNetwork().getDefaultNetworkTable();
			colNames.addAll(rootTable.getColumns().stream().map(col -> col.getName()).collect(Collectors.toList()));
		}

		return new ArrayList<String>(colNames);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws FileNotFoundException, IOException {
		if (taskMonitor != null) {
			taskMonitor.setProgress(0.0);
			taskMonitor.setTitle("Exporting to CX");
			taskMonitor.setStatusMessage("Exporting current network as CX...");
		}
		
		String network_type = writeSiblings ? "collection" : "subnetwork";
		String id_type = useCxId ? "CX IDs" : "SUIDs";
		logger.info("Exporting network " + _network + " as " + network_type + " with " + id_type);
		Settings.INSTANCE.debug("Encoding = " + _encoder.charset());

		final CxExporter exporter = new CxExporter(_network, writeSiblings, useCxId);

		List<String> aspects = aspectFilter.getSelectedValues();
		exporter.setNodeColumnFilter(nodeColFilter.getSelectedValues());
		exporter.setEdgeColumnFilter(edgeColFilter.getSelectedValues());
		exporter.setNetworkColumnFilter(networkColFilter.getSelectedValues());

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

	public void setWriteSiblings(final boolean write_siblings) {
		writeSiblings = write_siblings;
	}

	public void setUseCxId(boolean useCxId) {
		this.useCxId = useCxId;
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

}
