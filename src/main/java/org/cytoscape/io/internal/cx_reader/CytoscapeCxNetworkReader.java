package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;
import org.ndexbio.model.cx.NiceCXNetwork;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

	private CyNetwork[] _networks;
	private String _network_collection_name;
	private CxToCy _cx_to_cy;
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
		Map<Long, Long> suid_to_cxid_map = _cx_to_cy.getNetworkSuidToNetworkRelationsMap();
		if (!suid_to_cxid_map.containsKey(network.getSUID())) {
			throw new IllegalArgumentException(
					"Failed to build view for " + network + ". Was the network created successfully?");
		}
		long cxid = suid_to_cxid_map.get(network.getSUID());

		int num_views = _cx_to_cy.getSubNetworkToViewsMap().get(cxid).size();
		Settings.INSTANCE.debug(String.format("Building %s views for %s", num_views, network));

		List<CyNetworkView> views = new ArrayList<CyNetworkView>();
		for (Long cx_view_id : _cx_to_cy.getSubNetworkToViewsMap().get(cxid)) {
			CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);
			views.add(view);

			try {
				ViewMaker.makeView(view, cx_view_id, _cx_to_cy, _network_collection_name);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

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

		_cx_to_cy = new CxToCy();

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
			throw new IllegalArgumentException("Cannot import CX network into existing collection.");
		}
		final CyRootNetwork root_network = getRootNetwork();

		// Select Network Collection
		// 1. Check from Tunable
		// 2. If not available, use optional parameter

		if (root_network == null) {
			// Need to create new network with new root.
			if (Settings.INSTANCE.isAllowToUseNetworkCollectionNameFromNetworkAttributes()) {
				final String collection_name_from_network_attributes = CxToCy
						.getCollectionNameFromNetworkAttributes(niceCX.getNetworkAttributes());
				if (collection_name_from_network_attributes != null) {
					_network_collection_name = collection_name_from_network_attributes;
					Settings.INSTANCE.debug("collection name from network attributes: " + _network_collection_name);

				}
			}
		}
		
		CyGroupFactory _group_factory = CyServiceModule.getService(CyGroupFactory.class);
		List<CyNetwork> networks = _cx_to_cy.createNetwork(niceCX, root_network, cyNetworkFactory, _group_factory,
				_network_collection_name);

		_networks = new CyNetwork[networks.size()];
		networks.toArray(_networks);

		if (Settings.INSTANCE.isTiming()) {
			System.out.println();
			TimingUtil.reportTimeDifference(t0, "total time to build network(s) (not views)", -1);
			System.out.println();
		}
	}

}