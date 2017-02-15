package org.cytoscape.io.internal.cx_writer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.cxio.filters.AspectKeyFilter;
import org.cxio.filters.AspectKeyFilterBasic;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.cx.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.FilterSet;
import org.cytoscape.io.internal.cxio.Settings;
import org.cytoscape.io.internal.cxio.TimingUtil;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
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

    private final static Logger        logger                 = LoggerFactory.getLogger(CxNetworkWriter.class);
    private final static String        ENCODING               = "UTF-8";

    private final OutputStream         _os;
    private final CyNetwork            _network;
    private final CharsetEncoder       _encoder;
    private final VisualMappingManager _visual_mapping_manager;
    private final VisualLexicon        _lexicon;
    private final CyNetworkViewManager _networkview_manager;
    private final CyGroupManager       _group_manager;

    
	public ListMultipleSelection<String> aspectFilter = new ListMultipleSelection<>();
	public ListMultipleSelection<String> nodeColFilter = new ListMultipleSelection<>();
	public ListMultipleSelection<String> edgeColFilter = new ListMultipleSelection<>();
	public ListMultipleSelection<String> networkColFilter = new ListMultipleSelection<>();

	
	@Tunable(description="Write all networks in the collection")
    public Boolean writeSiblings = true;

	@Tunable(description="Aspects")
	public ListMultipleSelection<String> getFilter() {
		return aspectFilter;
	}

	@Tunable(description="Node Columns")
	public ListMultipleSelection<String> getNodeColFilter() {
		return nodeColFilter;
	}
	
	@Tunable(description="Edge Columns")
	public ListMultipleSelection<String> getEdgeColFilter() {
		return edgeColFilter;
	}
	
	@Tunable(description="Network Columns")
	public ListMultipleSelection<String> getNetworkColFilter() {
		return networkColFilter;
	}
    
	
	private final Map<String, Aspect> aspectMap;
	
	
    public CxNetworkWriter(final OutputStream os,
                           final CyNetwork network,
                           final VisualMappingManager visual_mapping_manager,
                           final CyNetworkViewManager networkview_manager,
                           final CyNetworkManager network_manager,
                           final CyGroupManager group_manager,
                           final CyNetworkTableManager table_manager,
                           final VisualLexicon lexicon) {

    		_visual_mapping_manager = visual_mapping_manager;
        _networkview_manager = networkview_manager;
        _lexicon = lexicon;
        _os = os;
        _network = network;
        _group_manager = group_manager;
        
        // Create Aspect Map
    
        aspectMap = new HashMap<>();
        for(Aspect aspect: Aspect.values()) {
        		aspectMap.put(aspect.toString(),aspect);
        }

    		// Add all
    		final List<String> vals = new ArrayList<>();
    		for(Aspect a: Aspect.values()){
    			vals.add(a.toString());
    		}
    		
    		// Select all
    		aspectFilter.setPossibleValues(vals);
    		aspectFilter.setSelectedValues(vals);
    		
    		// Node Column filter
    		final List<String> nodeColumnNames = getAllColumnNames(CyNode.class);
    		nodeColFilter.setPossibleValues(nodeColumnNames);
    		nodeColFilter.setSelectedValues(nodeColumnNames);
    		
    		// Edge Column filter
    		final List<String> edgeColumnNames = getAllColumnNames(CyEdge.class);
    		edgeColFilter.setPossibleValues(edgeColumnNames);
    		edgeColFilter.setSelectedValues(edgeColumnNames);
    		
    		// Network Column filter
    		final List<String> networkColumnNames = getAllColumnNames(CyNetwork.class);
    		networkColFilter.setPossibleValues(networkColumnNames);
    		networkColFilter.setSelectedValues(networkColumnNames);

        if (Charset.isSupported(ENCODING)) {
            // UTF-8 is supported by system
            _encoder = Charset.forName(ENCODING).newEncoder();
        }
        else {
            // Use default.
            logger.warn("UTF-8 is not supported by this system.  This can be a problem for non-English annotations.");
            _encoder = Charset.defaultCharset().newEncoder();
        }
    }

    
    private final AspectKeyFilter createColumnFilter(Class<? extends CyIdentifiable> type) {
        // Create colum filter
        final AspectKeyFilter filter;
        final ListMultipleSelection<String> colFilter;
        
        if(type == CyNode.class) {
        		filter = new AspectKeyFilterBasic(Aspect.NODE_ATTRIBUTES.asString());
        		colFilter = nodeColFilter;
        } else if(type == CyEdge.class) {
        		filter = new AspectKeyFilterBasic(Aspect.EDGE_ATTRIBUTES.asString());        	
        		colFilter = edgeColFilter;
        } else if(type == CyNetwork.class) {
        		filter = new AspectKeyFilterBasic(Aspect.NETWORK_ATTRIBUTES.asString());
        		colFilter = networkColFilter;
        } else {
        		throw new IllegalStateException("There is no valid aspect: " + type);
        }
        colFilter.getSelectedValues().stream().forEach(colName->filter.addIncludeAspectKey(colName));
        return filter;
    }
    
    private final List<String> getAllColumnNames(final Class<? extends CyIdentifiable> type) {
        
    		// Shared
    		final CyTable sharedTable = _network.getTable(type, CyNetwork.DEFAULT_ATTRS);
        
    		// Local
    		final CyTable localTable = _network.getTable(type, CyNetwork.LOCAL_ATTRS);
        
        final SortedSet<String> colNames = new TreeSet<>();
        
        colNames.addAll(sharedTable.getColumns().stream()
        		.map(col-> col.getName()).collect(Collectors.toList()));
        colNames.addAll(
        		localTable.getColumns().stream()
        			.map(col-> col.getName()).collect(Collectors.toList())
        	);
    		
        return new ArrayList<String>(colNames);
    }
    
    
    @Override
    public void run(final TaskMonitor taskMonitor) throws Exception {
        if (taskMonitor != null) {
            taskMonitor.setProgress(0.0);
            taskMonitor.setTitle("Exporting to CX");
            taskMonitor.setStatusMessage("Exporting current network as CX...");
        }

        if (Settings.INSTANCE.isDebug()) {
            System.out.println("Encoding = " + _encoder.charset());
        }

        // Create aspect-level filter
        final List<String> selected = aspectFilter.getSelectedValues();
        final AspectSet aspects = new AspectSet();
        
        selected.stream().forEach(aspect->aspects.addAspect(aspectMap.get(aspect)));
        
        // Create column-level filter
        AspectKeyFilter nodeFilter = createColumnFilter(CyNode.class);
        AspectKeyFilter edgeFilter = createColumnFilter(CyEdge.class);
        AspectKeyFilter networkFilter = createColumnFilter(CyNetwork.class);
        
        final FilterSet filterSet = new FilterSet();
        filterSet.addFilter(nodeFilter);
        filterSet.addFilter(edgeFilter);
        filterSet.addFilter(networkFilter);
 
        final CxExporter exporter = CxExporter.createInstance();
        exporter.setUseDefaultPrettyPrinting(true);
        exporter.setLexicon(_lexicon);
        exporter.setVisualMappingManager(_visual_mapping_manager);
        exporter.setNetworkViewManager(_networkview_manager);
        exporter.setGroupManager(_group_manager);
        exporter.setWritePreMetadata(true);
        exporter.setWritePostMetadata(true);
        exporter.setNextSuid(SUIDFactory.getNextSUID());

        final long t0 = System.currentTimeMillis();

        if (TimingUtil.WRITE_TO_DEV_NULL) {
            exporter.writeNetwork(_network, writeSiblings, aspects, new FileOutputStream(new File("/dev/null")));
        }
        else if (TimingUtil.WRITE_TO_BYTE_ARRAY_OUTPUTSTREAM) {
            exporter.writeNetwork(_network, writeSiblings, aspects, new ByteArrayOutputStream());
        }
        else {
            exporter.writeNetwork(_network, writeSiblings, aspects, filterSet, _os);
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
        }
        catch (final IOException e) {
            logger.error("Could not close Outputstream for CxNetworkWriter.", e);
        }
    }

    public void setWriteSiblings(final boolean write_siblings) {
        writeSiblings = write_siblings;
    }

}
