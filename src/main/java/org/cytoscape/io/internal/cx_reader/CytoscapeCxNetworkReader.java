package org.cytoscape.io.internal.cx_reader;

import java.io.InputStream;
import java.util.List;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.CxUtil;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.internal.nicecy.NiceCyRootNetwork;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.ndexbio.model.cx.NiceCXNetwork;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

	private CyNetwork[] _networks;
	private String _network_collection_name;
	private NiceCyRootNetwork niceCy;
	private final InputStream _in;

	public CytoscapeCxNetworkReader(
			final InputStream input_stream,
			final String network_collection_name,
			final CyNetworkViewFactory networkview_factory,
			final CyNetworkFactory network_factory,
			final CyNetworkManager network_manager,
			final CyRootNetworkManager root_network_manager) {

		super(input_stream, networkview_factory, network_factory, network_manager, root_network_manager);

		if (input_stream == null) {
			throw new IllegalArgumentException("input stream must not be null");
		}
		_in = input_stream;
		_network_collection_name = network_collection_name;
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		System.out.println("Creating view for " + network);
		List<CyNetworkView> views = niceCy.createViews();
		return views.get(0);
	}

	@Override
	public CyNetwork[] getNetworks() {
		return _networks;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {

		final long t0 = System.currentTimeMillis();
		final CxImporter cx_importer = new CxImporter();

		NiceCXNetwork niceCX = cx_importer.getCXNetworkFromStream(_in);

		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "total time parsing", -1);
		}

		// Select the root collection name from the list.
		if (_network_collection_name != null) {
			final ListSingleSelection<String> root_list = getRootNetworkList();
			if (root_list.getPossibleValues().contains(_network_collection_name)) {
				// Collection already exists.
				root_list.setSelectedValue(_network_collection_name);
			}
		}

		// Throw an error if trying to import CX network into existing collection.
		if (getRootNetwork() != null) {
			throw new IllegalArgumentException("CX Support is changing to disallow import into existing collections");
		}
		
//		List<CyNetwork> networks = _cx_to_cy.createNetwork(niceCX, root_network,
//				_network_collection_name);
		Long t1 = System.currentTimeMillis();
		niceCy = new NiceCyRootNetwork(niceCX);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t1, "Time to create NiceCyNetwork", -1);
		}
		
		t1 = System.currentTimeMillis();
		List<CyNetwork> networks = niceCy.apply();
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t1, "Time to create networks in Cytoscape", -1);
		}
		_networks = new CyNetwork[networks.size()];
		networks.toArray(_networks);

		if (CxUtil.isCollection(niceCX)) {
			_network_collection_name = niceCX.getNetworkName();
		}
		// Set the collection name
		if (_network_collection_name != null) {
			CyRootNetwork root = ((CySubNetwork) _networks[0]).getRootNetwork();
			root.getRow(root).set(CyNetwork.NAME, _network_collection_name);
			root.getRow(root).set(CyRootNetwork.SHARED_NAME, _network_collection_name);
		}
		
		if (Settings.INSTANCE.isTiming()) {
			System.out.println();
			TimingUtil.reportTimeDifference(t0, "total time to build network(s) (not views)", -1);
			System.out.println();
		}
	}

}