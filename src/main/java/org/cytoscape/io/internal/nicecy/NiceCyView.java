package org.cytoscape.io.internal.nicecy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.internal.cx_reader.ViewMaker;
import org.cytoscape.io.internal.nicecy.NiceCyNetwork.NiceCySubNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;

public class NiceCyView extends Identifiable {
	
	private final NiceCySubNetwork subnet;
	private Map<Long, CartesianLayoutElement> cartesianLayout;
	private HashMap<String, CyVisualPropertiesElement> visualProperties;
	private Map<Long, List<CyVisualPropertiesElement>> nodeBypass;
	private Map<Long, List<CyVisualPropertiesElement>> edgeBypass;

	public NiceCyView(long id, NiceCySubNetwork subnet) {
		super(id);
		this.subnet = subnet;
		cartesianLayout = new HashMap<Long, CartesianLayoutElement>();
		visualProperties = new HashMap<String, CyVisualPropertiesElement>();
		nodeBypass = new HashMap<Long, List<CyVisualPropertiesElement>>();
		edgeBypass = new HashMap<Long, List<CyVisualPropertiesElement>>();
	}
	
	public void addNodeBypass(Long id, CyVisualPropertiesElement cvpe) {
		if (!nodeBypass.containsKey(id)) {
			nodeBypass.put(id, new ArrayList<CyVisualPropertiesElement>());
		}
		nodeBypass.get(id).add(cvpe);
	}

	public void addEdgeBypass(Long id, CyVisualPropertiesElement cvpe) {
		if (!edgeBypass.containsKey(id)) {
			edgeBypass.put(id, new ArrayList<CyVisualPropertiesElement>());
		}
		edgeBypass.get(id).add(cvpe);
	}

	public void addCartesianLayout(long id, CartesianLayoutElement cl) {
		cartesianLayout.put(id, cl);
	}
	
	public void addVisualProperties(CyVisualPropertiesElement cvpe){
		if (visualProperties.containsKey(cvpe.getProperties_of())) {
			throw new IllegalArgumentException("Duplicate visual property in CX: " + cvpe.getProperties_of());
		}
		visualProperties.put(cvpe.getProperties_of(), cvpe);
	}
	
	public void apply(CyNetworkView v) {
		ViewMaker.makeView(v, subnet.parent, cartesianLayout, visualProperties, nodeBypass, edgeBypass);
		subnet.groups.forEach(suid -> {
			NiceCyGroup group = subnet.parent.root_groups.get(suid);
			group.updateInView((CySubNetwork)subnet.network);
		});
	}
}