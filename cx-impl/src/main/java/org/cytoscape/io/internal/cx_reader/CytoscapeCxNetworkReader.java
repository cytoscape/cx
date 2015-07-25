package org.cytoscape.io.internal.cx_reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.cxio.aspects.datamodels.VisualPropertiesElement;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.internal.visual_properties.VisualPropertiesWriter;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListSingleSelection;

public class CytoscapeCxNetworkReader extends AbstractCyNetworkReader {

    private CyNetwork                  _network = null;         // Supports
                                                                 // only one
    // CyNetwork
    // per file.
    private final String               _network_collection_name;
    private CxToCy                     _cx_to_cy;
    private final InputStream          _in;
    private final VisualMappingManager _visual_mapping_manager;
    private final CyApplicationManager _application_manager;
    private final RenderingEngineManager _rendering_engine_manager;

    public CytoscapeCxNetworkReader(final String networkCollectionName,
                                    final InputStream input_stream,
                                    final CyApplicationManager cyApplicationManager,
                                    final CyNetworkFactory cyNetworkFactory,
                                    final CyNetworkManager cyNetworkManager,
                                    final CyRootNetworkManager cyRootNetworkManager,
                                    final VisualMappingManager visualMappingManager,
                                    final RenderingEngineManager renderingEngineMgr ) throws IOException {
        super(input_stream, cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

        if (input_stream == null) {
            throw new NullPointerException("input stream cannot be null");
        }
        _application_manager = cyApplicationManager;
        _in = input_stream;
        _network_collection_name = networkCollectionName;
        _visual_mapping_manager = visualMappingManager;
        _rendering_engine_manager =  renderingEngineMgr;
    }

    @Override
    public CyNetwork[] getNetworks() {
        final CyNetwork[] result = new CyNetwork[1];
        result[0] = _network;
        return result;
    }

    @Override
    public CyNetworkView buildCyNetworkView(final CyNetwork network) {
        final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
        // final Map<CyNode, Double[]> positionMap = cx_to_cy.getNodePosition();
        // for (final CyNode node : positionMap.keySet()) {
        // final Double[] position = positionMap.get(node);
        // view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
        // position[0]);
        // view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
        // position[1]);
        // }
        
        
        final VisualLexicon lexicon = _rendering_engine_manager.getDefaultVisualLexicon();
        final Map<CyNode, VisualPropertiesElement> node_vpe = _cx_to_cy.getNodeVisualPropertiesElementsMap();
        for (final CyNode node : node_vpe.keySet()) {
            final VisualPropertiesElement vpe = node_vpe.get(node);

            final View<CyNode> node_view = view.getNodeView(node);
            final SortedMap<String, String> props = vpe.getProperties();

            if (props.containsKey(VisualPropertiesWriter.X_LOCATION)) {
                node_view.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
                                            Double.valueOf(props.get(VisualPropertiesWriter.X_LOCATION)));
            }
            if (props.containsKey(VisualPropertiesWriter.Y_LOCATION)) {
                node_view.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
                                            Double.valueOf(props.get(VisualPropertiesWriter.Y_LOCATION)));
            }
            if (props.containsKey(VisualPropertiesWriter.Z_LOCATION)) {
                node_view.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION,
                                            Double.valueOf(props.get(VisualPropertiesWriter.Z_LOCATION)));
            }
            if (props.containsKey(VisualPropertiesWriter.HEIGHT)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_HEIGHT,
                                         Double.valueOf(props.get(VisualPropertiesWriter.HEIGHT)));
            }
            if (props.containsKey(VisualPropertiesWriter.WIDTH)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_WIDTH,
                                         Double.valueOf(props.get(VisualPropertiesWriter.WIDTH)));
            }
            if (props.containsKey(VisualPropertiesWriter.VISIBLE)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_VISIBLE,
                                         Boolean.valueOf(props.get(VisualPropertiesWriter.VISIBLE)));
            }
            if (props.containsKey(VisualPropertiesWriter.TRANSPARENCY)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY,
                                         Integer.valueOf(props.get(VisualPropertiesWriter.TRANSPARENCY)));
            }

            if (props.containsKey(VisualPropertiesWriter.NESTED_NETWORK_VISIBLE)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE,
                                         Boolean.valueOf(props.get(VisualPropertiesWriter.NESTED_NETWORK_VISIBLE)));
            }
            if (props.containsKey(VisualPropertiesWriter.BORDER_TRANSPARENCY)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY,
                                         Integer.valueOf(props.get(VisualPropertiesWriter.BORDER_TRANSPARENCY)));
            }
            if (props.containsKey(VisualPropertiesWriter.BORDER_WIDTH)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_BORDER_WIDTH,
                                         Double.valueOf(props.get(VisualPropertiesWriter.BORDER_WIDTH)));
            }
            if (props.containsKey(VisualPropertiesWriter.DEPTH)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_DEPTH,
                                         Double.valueOf(props.get(VisualPropertiesWriter.DEPTH)));
            }
            if (props.containsKey(VisualPropertiesWriter.LABEL)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_LABEL, props.get(VisualPropertiesWriter.LABEL));
            }
            if (props.containsKey(VisualPropertiesWriter.LABEL_TRANSPARENCY)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY,
                                         Integer.valueOf(props.get(VisualPropertiesWriter.LABEL_TRANSPARENCY)));
            }
            if (props.containsKey(VisualPropertiesWriter.LABEL_WIDTH)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_LABEL_WIDTH,
                                         Double.valueOf(props.get(VisualPropertiesWriter.LABEL_WIDTH)));
            }
            if (props.containsKey(VisualPropertiesWriter.SELECTED)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_SELECTED,
                                         Boolean.valueOf(props.get(VisualPropertiesWriter.SELECTED)));
            }
            if (props.containsKey(VisualPropertiesWriter.TOOLTIP)) {
                node_view.setLockedValue(BasicVisualLexicon.NODE_TOOLTIP, props.get(VisualPropertiesWriter.TOOLTIP));
            }

            // _visual_mapping_manager.getVisualStyle(networkViewModel);

            final VisualProperty vp = lexicon.lookup(CyNode.class, BasicVisualLexicon.NODE_SHAPE.getIdString());

            final Object parsedValue = vp.parseSerializableString(props.get(VisualPropertiesWriter.SHAPE));
            node_view.setLockedValue(vp, parsedValue);

            //

            // final VisualProperty vp = visualLexicon.lookup(type, attName);
            // final Object o = lexicon.lookup(CyNode.class,
            // BasicVisualLexicon.NODE_SHAPE.getIdString());
            //
            // node_view.setLockedValue(BasicVisualLexicon.NODE_SHAPE, vp);
            // vp.parseSerializableString(props.get(VisualPropertiesWriter.SHAPE));
            //
            // node_view.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShape
            // = new NodeShapeVisualProperty();
            // props.get(VisualPropertiesWriter.SHAPE));

        }

        return view;
    }

    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {

        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        // aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);

        final CxImporter cx_importer = CxImporter.createInstance();

        long t0 = 0;
        SortedMap<String, List<AspectElement>> res = null;
        if (TimingUtil.TIMING) {

            final byte[] buff = new byte[8000];
            int bytes_read = 0;
            final ByteArrayOutputStream bao = new ByteArrayOutputStream();
            while ((bytes_read = _in.read(buff)) != -1) {
                bao.write(buff, 0, bytes_read);
            }
            final ByteArrayInputStream bis = new ByteArrayInputStream(bao.toByteArray());
            t0 = System.currentTimeMillis();
            final CxReader cxr = cx_importer.getCxReader(aspects, bis);

            res = TimingUtil.parseAsMap(cxr, t0);
            TimingUtil.reportTimeDifference(t0, "total time parsing", 0);
            t0 = System.currentTimeMillis();
        }
        else {
            final CxReader cxr = cx_importer.getCxReader(aspects, _in);
            res = cx_importer.readAsMap(aspects, _in);
        }

        _cx_to_cy = new CxToCy();

        // Select the root collection name from the list.
        if (_network_collection_name != null) {
            final ListSingleSelection<String> rootList = getRootNetworkList();
            if (rootList.getPossibleValues().contains(_network_collection_name)) {
                // Collection already exists.
                rootList.setSelectedValue(_network_collection_name);
            }
        }

        final CyRootNetwork rootNetwork = getRootNetwork();

        // Select Network Collection
        // 1. Check from Tunable
        // 2. If not available, use optional parameter
        CySubNetwork subNetwork;
        if (rootNetwork != null) {
            // Root network exists
            subNetwork = rootNetwork.addSubNetwork();
            _network = _cx_to_cy.createNetwork(res, subNetwork, null);
        }
        else {
            // Need to create new network with new root.
            subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
            _network = _cx_to_cy.createNetwork(res, subNetwork, _network_collection_name);
        }

        if (TimingUtil.TIMING) {
            TimingUtil.reportTimeDifference(t0, "total time build", 0);
        }
    }
}