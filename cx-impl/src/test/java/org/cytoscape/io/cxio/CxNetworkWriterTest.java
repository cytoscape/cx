package org.cytoscape.io.cxio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.cytoscape.io.internal.cx_writer.CxNetworkWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.Test;

public class CxNetworkWriterTest {

    @Test
    public void testEmptyNetwork() throws Exception {
        final NetworkTestSupport t = new NetworkTestSupport();
        final CyNetwork n = t.getNetwork();
        final CxNetworkWriterFactory factory = new CxNetworkWriterFactory(null);
        final OutputStream out = new ByteArrayOutputStream();
        final CyWriter w = factory.createWriter(out, n);
        w.run(null);
        final String out_str = out.toString();
        assertTrue(out_str.replaceAll("\\s+", "").equals("[]"));

    }

    @Test
    public void testNodesWriting() throws Exception {
        final NetworkTestSupport t = new NetworkTestSupport();
        final CyNetwork n = t.getNetwork();
        final CyNode a = n.addNode();
        final CyNode b = n.addNode();
        final CyNode c = n.addNode();
        final CyNode d = n.addNode();
        final CyNode e = n.addNode();

        final CxNetworkWriterFactory factory = new CxNetworkWriterFactory(null);

        final OutputStream out = new ByteArrayOutputStream();

        final CyWriter w = factory.createWriter(out, n);

        w.run(null);

        final String out_str = out.toString();

        assertTrue(out_str.indexOf("nodes") > 0);
        assertFalse(out_str.indexOf("edges") > 0);
        assertTrue(out_str.indexOf(String.valueOf(a.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(b.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(c.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(d.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(e.getSUID())) > 0);
    }

    @Test
    public void testNodesAndEdgesWriting() throws Exception {
        final NetworkTestSupport t = new NetworkTestSupport();
        final CyNetwork n = t.getNetwork();

        final CyNode a = n.addNode();
        final CyNode b = n.addNode();
        final CyNode c = n.addNode();
        final CyNode d = n.addNode();
        final CyNode e = n.addNode();

        n.addEdge(a, b, true);
        n.addEdge(a, c, true);
        n.addEdge(a, d, true);
        n.addEdge(a, e, true);
        n.addEdge(d, e, true);

        final CxNetworkWriterFactory factory = new CxNetworkWriterFactory(null);

        final OutputStream out = new ByteArrayOutputStream();

        final CyWriter w = factory.createWriter(out, n);
        w.run(null);

        final String out_str = out.toString();

        assertTrue(out_str.indexOf("nodes") > 0);
        assertTrue(out_str.indexOf("edges") > 0);
        assertTrue(out_str.indexOf(String.valueOf(a.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(b.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(c.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(d.getSUID())) > 0);
        assertTrue(out_str.indexOf(String.valueOf(e.getSUID())) > 0);

    }

}
