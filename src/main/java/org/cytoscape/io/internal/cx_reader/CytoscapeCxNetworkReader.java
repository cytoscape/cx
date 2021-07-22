package org.cytoscape.io.internal.cx_reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.internal.nicecy.NiceCyRootNetwork;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;
import org.ndexbio.model.cx.NiceCXNetwork;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

	private CyNetwork[] _networks;
	private String _network_collection_name;
	private NiceCyRootNetwork niceCy;
	protected final NiceCXNetwork niceCX;

	final CxImporter cx_importer = new CxImporter();

	private Boolean createView = null;
	
	// Warning: HACK. Cytoscape doesn't allow access to reader parameters programmatically. This method allows Java reflections 
	// access to it. This method should not be renamed or made private.
	public void setCreateView(final Boolean createView) {
		this.createView = createView;
	}
	
	public CytoscapeCxNetworkReader(final InputStream input_stream, final String network_collection_name,
			final CyNetworkViewFactory networkview_factory, final CyNetworkFactory network_factory,
			final CyNetworkManager network_manager, final CyRootNetworkManager root_network_manager) {

		super(input_stream, networkview_factory, network_factory, network_manager, root_network_manager);

		if (input_stream == null) {
			throw new IllegalArgumentException("input stream must not be null");
		}
		try {
			niceCX = cx_importer.getCXNetworkFromStream(input_stream);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to import file as CX");
		}

		_network_collection_name = network_collection_name;
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
	
			System.out.println("Creating view for " + network);
			List<CyNetworkView> views = niceCy.createViews(network, createView);
			if (views.isEmpty()) {
				CyNetworkViewFactory view_factory = CyServiceModule.getService(CyNetworkViewFactory.class);
				final CyNetworkView createdView = view_factory.createNetworkView(network);
				return createdView;
			} else {
		
			}
			
			try {
				niceCy.addTableVisualStyles(network);
			} catch (Exception e) {
				System.out.println("Failed to create table style for " + network + ": " + e.getMessage());

			}
			return views.get(0);
		
	}

	

	@Override
	public CyNetwork[] getNetworks() {
		return _networks;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) throws IOException {

		System.out.println("create view value: " + createView);
		final long t0 = System.currentTimeMillis();

		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "total time parsing", -1);
		}

		// Throw an error if trying to import CX network into existing collection.
		if (getRootNetwork() != null) {
			System.out.println("CX Support is changing to disallow import into existing collections");
			setRootNetworkList(new ListSingleSelection<String>());
		}

		long t1 = System.currentTimeMillis();
		niceCy = new NiceCyRootNetwork(niceCX);
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t1, "Time to create NiceCyNetwork", -1);
		}

		if (niceCy.getNetworkName() == null) {
			// Set the name of collection/network to be imported
			if (_network_collection_name == null) {
				_network_collection_name = "Unnamed CX Network";
			}
			niceCy.setNetworkName(_network_collection_name);
		}

		t1 = System.currentTimeMillis();
		List<CyNetwork> importedNetworks = niceCy.apply();
		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t1, "Time to create networks in Cytoscape", -1);
		}
		_networks = new CyNetwork[importedNetworks.size()];
		importedNetworks.toArray(_networks);

		if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "total time to build network(s) (not views)", -1);
		}
	}

}