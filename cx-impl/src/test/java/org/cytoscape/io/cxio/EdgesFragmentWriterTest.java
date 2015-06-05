package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.cxio.AspectElement;
import org.cytoscape.io.internal.cxio.EdgesElement;
import org.cytoscape.io.internal.cxio.EdgesFragmentWriter;
import org.cytoscape.io.internal.cxio.JsonWriter;
import org.junit.Test;



public class EdgesFragmentWriterTest {

    @Test
    public void test() throws IOException {

        final List<AspectElement> l0 = new ArrayList<AspectElement>();
        final ByteArrayOutputStream out0 = new ByteArrayOutputStream();
        final JsonWriter t0 = JsonWriter.createInstance(out0);

        final EdgesFragmentWriter w0 = EdgesFragmentWriter.createInstance();

        t0.start();
        w0.write(l0, t0);
        t0.end();

        assertEquals("[{\"edges\":[]}]", out0.toString());

        final EdgesElement e0 = new EdgesElement("0", "f0", "t0");
        final EdgesElement e1 = new EdgesElement("1", "f1", "t1");

        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(e0);
        l1.add(e1);

        final ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        final JsonWriter t1 = JsonWriter.createInstance(out1);

        final EdgesFragmentWriter w1 = EdgesFragmentWriter.createInstance();

        t1.start();
        w1.write(l1, t1);
        t1.end();

        assertEquals(
                "[{\"edges\":[{\"@id\":\"0\",\"source\":\"f0\",\"target\":\"t0\"},{\"@id\":\"1\",\"source\":\"f1\",\"target\":\"t1\"}]}]",
                out1.toString());

        final EdgesElement e3 = new EdgesElement("3", "f3", "t3");
        final EdgesElement e4 = new EdgesElement("4", "f4", "t4");
        final List<AspectElement> l2 = new ArrayList<AspectElement>();
        l2.add(e3);
        final List<AspectElement> l3 = new ArrayList<AspectElement>();
        l3.add(e4);

        final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        final JsonWriter t2 = JsonWriter.createInstance(out2);

        final EdgesFragmentWriter w2 = EdgesFragmentWriter.createInstance();

        t2.start();
        w2.write(l2, t2);
        w2.write(l3, t2);
        t2.end();

        assertEquals(
                "[{\"edges\":[{\"@id\":\"3\",\"source\":\"f3\",\"target\":\"t3\"}]},{\"edges\":[{\"@id\":\"4\",\"source\":\"f4\",\"target\":\"t4\"}]}]",
                out2.toString());

    }

}
