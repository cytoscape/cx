package org.cytoscape.io.internal.cxio;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;

public final class Settings {

    public final static Settings INSTANCE                                                              = new Settings();

    private static final boolean IGNORE_SELECTED_COLUMN_DEFAULT                                        = false;
    private static final boolean IGNORE_SUID_COLUMN_DEFAULT                                            = true;
    private static final boolean WRITE_SELECTED_ONLY_IF_TRUE_DEFAULT                                   = true;
    private static final boolean TIMING_DEFAULT                                                        = true;

	public final static Set<String> IGNORE_EDGE_ATTRIBUTES = new HashSet<>();
	public final static Set<String> IGNORE_NODE_ATTRIBUTES = new HashSet<>();
	public final static Set<String> IGNORE_NETWORK_ATTRIBUTES = new HashSet<>();

	public final static Set<String> IGNORE_SINGLE_NETWORK_EDGE_ATTRIBUTES = new HashSet<>();
	public final static Set<String> IGNORE_SINGLE_NETWORK_NODE_ATTRIBUTES = new HashSet<>();
	public final static Set<String> IGNORE_SINGLE_NETWORK_NETWORK_ATTRIBUTES = new HashSet<>();
	
	public final static Set<String> CX2_IGNORE_NODE_ATTRIBUTES = new HashSet<>();

	public final static Set<String> CX2_IGNORE_EDGE_ATTRIBUTES = new HashSet<>();
	
	public final static Set<String> cytoscapeBuiltinEdgeTableAttributes = 
			new HashSet<>(Arrays.asList(CyNetwork.NAME, CyNetwork.SELECTED, CyNetwork.SUID,CxUtil.INTERACTION,
					CyRootNetwork.SHARED_NAME, CyRootNetwork.SHARED_INTERACTION));

	public final static Set<String> cytoscapeBuiltinTableAttributes = 
			new HashSet<>(Arrays.asList(CyNetwork.NAME, CyNetwork.SELECTED, CyNetwork.SUID,
					CyRootNetwork.SHARED_NAME));	
	
	static {
		IGNORE_NODE_ATTRIBUTES.add(CxUtil.REPRESENTS);
		
		IGNORE_SINGLE_NETWORK_NODE_ATTRIBUTES.add(CyNetwork.NAME);
		IGNORE_SINGLE_NETWORK_NODE_ATTRIBUTES.add(CyRootNetwork.SHARED_NAME);
		
		IGNORE_SINGLE_NETWORK_EDGE_ATTRIBUTES.add(CxUtil.INTERACTION);
		IGNORE_SINGLE_NETWORK_EDGE_ATTRIBUTES.add(CyRootNetwork.SHARED_INTERACTION);
		IGNORE_SINGLE_NETWORK_EDGE_ATTRIBUTES.add(CyRootNetwork.SHARED_NAME);
		
		IGNORE_SINGLE_NETWORK_NETWORK_ATTRIBUTES.add(CyRootNetwork.SHARED_NAME);
		IGNORE_SINGLE_NETWORK_NETWORK_ATTRIBUTES.add(CyNetwork.SELECTED);
		
	}
	
    private boolean              _timing                                                               = TIMING_DEFAULT;
    private boolean              _ignore_selected_column                                               = IGNORE_SELECTED_COLUMN_DEFAULT;
    private boolean              _write_selected_only_if_true                                          = WRITE_SELECTED_ONLY_IF_TRUE_DEFAULT;
    private boolean              _ignore_suid_column                                                   = IGNORE_SUID_COLUMN_DEFAULT;
    

    public boolean isIgnoreSelectedColumn() {
        return _ignore_selected_column;
    }

    public boolean isIgnoreSuidColumn() {
        return _ignore_suid_column;
    }

    public boolean isTiming() {
        return _timing;
    }

    public boolean isWriteSelectedOnlyIfTrue() {
        return _write_selected_only_if_true;
    }

    public void setIgnoreSelectedColumn(final boolean ignore_selected_column) {
        _ignore_selected_column = ignore_selected_column;
    }

    public void setIgnoreSuidColumn(final boolean ignore_suid_column) {
        _ignore_suid_column = ignore_suid_column;
    }

    public void setTiming(final boolean timing) {
        _timing = timing;
    }

    public void setWriteSelectedOnlyIfTrue(final boolean write_selected_only_if_true) {
        _write_selected_only_if_true = write_selected_only_if_true;
    }
    
    public final static boolean isIgnore(final String column_name, final Set<String> additional_to_ignore, Object value) {
    	
    	if (value instanceof String && ((String) value).isEmpty()) {
    		return true;
    	}
    	if (value instanceof List<?> && ((List<?>) value).isEmpty()) {
    		return true;
    	}
    	
    	switch (column_name) {
		case CyNetwork.SUID:
			return Settings.INSTANCE.isIgnoreSuidColumn();
		case CyNetwork.SELECTED:
			Boolean boolVal = value == null ? false : Boolean.valueOf(value.toString());
			return Settings.INSTANCE.isIgnoreSelectedColumn()
					|| (boolVal != true && Settings.INSTANCE.isWriteSelectedOnlyIfTrue());
		case CxUtil.CX_ID_MAPPING:
		case CxUtil.CX_METADATA:
			return true;
		default:
			return ((additional_to_ignore != null) && additional_to_ignore.contains(column_name));
		}
	}
	

    private Settings() {
        // hidden constructor
    }

	

}
