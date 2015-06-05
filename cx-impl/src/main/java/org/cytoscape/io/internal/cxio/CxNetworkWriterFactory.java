package org.cytoscape.io.internal.cxio;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.model.CyNetwork;

public class CxNetworkWriterFactory implements CyWriterFactory  {
	private final CyFileFilter filter;
	public CxNetworkWriterFactory(CyFileFilter filter) {
		this.filter = filter;
	}
	
	public CyWriter createWriter(OutputStream outputStream, CyNetwork network) {
		return new CxNetworkWriter(outputStream, network);
	}

	@Override
	public CyFileFilter getFileFilter() {
		return filter;
	}
}
