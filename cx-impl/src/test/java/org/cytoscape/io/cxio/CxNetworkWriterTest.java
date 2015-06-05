package org.cytoscape.io.cxio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.cytoscape.io.internal.cxio.CxNetworkWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;

public class CxNetworkWriterTest {
	
	@Test
	public void testEmptyNetwork() throws Exception {
		NetworkTestSupport t = new NetworkTestSupport();
		CyNetwork n = t.getNetwork();
	    CxNetworkWriterFactory factory = new CxNetworkWriterFactory();
	    OutputStream out = new ByteArrayOutputStream();
	    CyWriter w = factory.createWriter(out, n);
	    w.run(null);
	    final String out_str = out.toString();
	    assertTrue(out_str.equals("[]"));
	}
	
	@Test
	public void testNodesWriting() throws Exception {
		NetworkTestSupport t = new NetworkTestSupport();
		CyNetwork n = t.getNetwork();
		CyNode a = n.addNode();
		CyNode b = n.addNode();
		CyNode c = n.addNode();
		CyNode d = n.addNode();
		CyNode e = n.addNode();
		
	    CxNetworkWriterFactory factory = new CxNetworkWriterFactory();	
	    
	    OutputStream out = new ByteArrayOutputStream();
	    
	    CyWriter w = factory.createWriter(out, n);
	    
	    w.run(null);

	    final String out_str = out.toString();
	    
	    assertTrue(out_str.indexOf("nodes") > 0 );
	    assertFalse(out_str.indexOf("edges") > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(a.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(b.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(c.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(d.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(e.getSUID() ) ) > 0 );
	}
	
	@Test
	public void testNodesAndEdgesWriting() throws Exception {
		NetworkTestSupport t = new NetworkTestSupport();
		CyNetwork n = t.getNetwork();
		
		CyNode a = n.addNode();
		CyNode b = n.addNode();
		CyNode c = n.addNode();
		CyNode d = n.addNode();
		CyNode e = n.addNode();
		
		n.addEdge(a, b, true);
		n.addEdge(a, c, true);
		n.addEdge(a, d, true);
		n.addEdge(a, e, true);
		n.addEdge(d, e, true);
		
	    CxNetworkWriterFactory factory = new CxNetworkWriterFactory();
	    
	    OutputStream out = new ByteArrayOutputStream();
	    
	    CyWriter w = factory.createWriter(out, n);
	    w.run(null);

	    final String out_str = out.toString();
	    
	    assertTrue(out_str.indexOf("nodes") > 0 );
	    assertTrue(out_str.indexOf("edges") > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(a.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(b.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(c.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(d.getSUID() ) ) > 0 );
	    assertTrue(out_str.indexOf(String.valueOf(e.getSUID() ) ) > 0 );
	    
	}
	
}
