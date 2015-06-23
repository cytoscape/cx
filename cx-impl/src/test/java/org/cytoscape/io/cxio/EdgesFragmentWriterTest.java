package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.writers.EdgesFragmentWriter;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.junit.Test;

public class EdgesFragmentWriterTest {

    @Test
    public void test() throws IOException {

        final List<AspectElement> l0 = new ArrayList<AspectElement>();
        final ByteArrayOutputStream out0 = new ByteArrayOutputStream();
        final CxWriter w0 = CxWriter.createInstance(out0, false);
        w0.addAspectFragmentWriter(EdgesFragmentWriter.createInstance());

        w0.start();
        w0.writeAspectElements(l0);
        w0.end();

        assertEquals("[]", out0.toString());

        final EdgesElement e0 = new EdgesElement("0", "f0", "t0");
        final EdgesElement e1 = new EdgesElement("1", "f1", "t1");

        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(e0);
        l1.add(e1);

        final ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        final CxWriter w1 = CxWriter.createInstance(out1, false);
        w1.addAspectFragmentWriter(EdgesFragmentWriter.createInstance());

        w1.start();
        w1.writeAspectElements(l1);
        w1.end();

        assertEquals("[{\"edges\":[{\"@id\":\"0\",\"source\":\"f0\",\"target\":\"t0\"},{\"@id\":\"1\",\"source\":\"f1\",\"target\":\"t1\"}]}]",
                     out1.toString());

        final EdgesElement e3 = new EdgesElement("3", "f3", "t3");
        final EdgesElement e4 = new EdgesElement("4", "f4", "t4");
        final List<AspectElement> l2 = new ArrayList<AspectElement>();
        l2.add(e3);
        final List<AspectElement> l3 = new ArrayList<AspectElement>();
        l3.add(e4);

        final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        final CxWriter w2 = CxWriter.createInstance(out2, false);
        w2.addAspectFragmentWriter(EdgesFragmentWriter.createInstance());

        w2.start();
        w2.writeAspectElements(l2);
        w2.writeAspectElements(l3);
        w2.end();

        assertEquals("[{\"edges\":[{\"@id\":\"3\",\"source\":\"f3\",\"target\":\"t3\"}]},{\"edges\":[{\"@id\":\"4\",\"source\":\"f4\",\"target\":\"t4\"}]}]",
                     out2.toString());

    }

}
