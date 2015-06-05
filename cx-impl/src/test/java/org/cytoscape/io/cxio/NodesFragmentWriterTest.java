package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.cxio.AspectElement;
import org.cytoscape.io.internal.cxio.JsonWriter;
import org.cytoscape.io.internal.cxio.NodesElement;
import org.cytoscape.io.internal.cxio.NodesFragmentWriter;
import org.junit.Test;

public class NodesFragmentWriterTest {

    @Test
    public void test() throws IOException {

        final List<AspectElement> l0 = new ArrayList<AspectElement>();
        final OutputStream out0 = new ByteArrayOutputStream();
        final JsonWriter t0 = JsonWriter.createInstance(out0);

        final NodesFragmentWriter w0 = NodesFragmentWriter.createInstance();

        t0.start();
        w0.write(l0, t0);
        t0.end();

        assertEquals("[{\"nodes\":[]}]", out0.toString());

        final NodesElement n0 = new NodesElement("0");
        final NodesElement n1 = new NodesElement("1");
        final NodesElement n2 = new NodesElement("2");
        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(n0);
        l1.add(n1);
        l1.add(n2);

        final OutputStream out1 = new ByteArrayOutputStream();
        final JsonWriter t1 = JsonWriter.createInstance(out1);

        final NodesFragmentWriter w1 = NodesFragmentWriter.createInstance();

        t1.start();
        w1.write(l1, t1);
        t1.end();

        assertEquals("[{\"nodes\":[{\"@id\":\"0\"},{\"@id\":\"1\"},{\"@id\":\"2\"}]}]",
                out1.toString());

        final NodesElement n3 = new NodesElement("3");
        final NodesElement n4 = new NodesElement("4");
        final NodesElement n5 = new NodesElement("5");
        final List<AspectElement> l2 = new ArrayList<AspectElement>();
        l2.add(n3);
        l2.add(n4);
        final List<AspectElement> l3 = new ArrayList<AspectElement>();
        l3.add(n5);

        final OutputStream out2 = new ByteArrayOutputStream();
        final JsonWriter t2 = JsonWriter.createInstance(out2);

        final NodesFragmentWriter w2 = NodesFragmentWriter.createInstance();

        t2.start();
        w2.write(l2, t1);
        w2.write(l3, t1);
        t2.end();

        assertEquals(
                "[{\"nodes\":[{\"@id\":\"3\"},{\"@id\":\"4\"}]},{\"nodes\":[{\"@id\":\"5\"}]}]",
                out2.toString());

    }

}
