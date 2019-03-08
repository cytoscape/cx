package org.cytoscape.io.internal.cx_writer;

import java.io.OutputStream;
import java.util.Map;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;

public class CxNetworkWriterFactory implements CyNetworkViewWriterFactory, TunableSetter {
    private final CyFileFilter          _filter;

    public CxNetworkWriterFactory(final CyFileFilter filter) {
        _filter = filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetwork network) {
        return new CxNetworkWriter(os,
                                   network,
                                   false,
                                   true);
    }

    @Override
    public CyFileFilter getFileFilter() {
        return _filter;
    }

    @Override
    public CyWriter createWriter(final OutputStream os, final CyNetworkView view) {
        return new CxNetworkWriter(os,
                                   view.getModel(),
                                   false,
                                   true);

    }
    
    @Override
	public TaskIterator createTaskIterator(TaskIterator taskIterator, Map<String, Object> tunableValues) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public TaskIterator createTaskIterator(TaskIterator taskIterator, Map<String, Object> tunableValues,
			TaskObserver observer) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void applyTunables(Object object, Map<String, Object> tunableValues) {
		if (!(object instanceof CxNetworkWriter)) {
			throw new RuntimeException("Can only apply tunables to CxNetworkWriter, not " + object.getClass());
		}
		CxNetworkWriter writer = (CxNetworkWriter) object;
		if (tunableValues.containsKey("useCxId")) {
			if (!(tunableValues.get("useCxId") instanceof Boolean)) {
				throw new IllegalArgumentException("Tunable 'useCxId' must be a boolean");
			}
			writer.setUseCxId((boolean) tunableValues.get("useCxId"));
		}
		
		if (tunableValues.containsKey("writeSiblings")) {
			if (!(tunableValues.get("writeSiblings") instanceof Boolean)) {
				throw new IllegalArgumentException("Tunable 'writeSiblings' must be a boolean");
			}
			writer.setWriteSiblings((boolean) tunableValues.get("writeSiblings"));
		}
	}

}
