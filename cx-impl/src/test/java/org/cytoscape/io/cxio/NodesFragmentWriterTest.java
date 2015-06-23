package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.aspects.writers.NodesFragmentWriter;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.junit.Test;

public class NodesFragmentWriterTest {

    @Test
    public void test() throws IOException {

        final List<AspectElement> l0 = new ArrayList<AspectElement>();
        final OutputStream out0 = new ByteArrayOutputStream();
        final CxWriter w0 = CxWriter.createInstance(out0, false);
        w0.addAspectFragmentWriter(NodesFragmentWriter.createInstance());

        w0.start();
        w0.writeAspectElements(l0);
        w0.end();

        assertEquals("[]", out0.toString());

        final NodesElement n0 = new NodesElement("0");
        final NodesElement n1 = new NodesElement("1");
        final NodesElement n2 = new NodesElement("2");
        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(n0);
        l1.add(n1);
        l1.add(n2);

        final OutputStream out1 = new ByteArrayOutputStream();
        final CxWriter w1 = CxWriter.createInstance(out1, false);
        w1.addAspectFragmentWriter(NodesFragmentWriter.createInstance());

        w1.start();
        w1.writeAspectElements(l1);
        w1.end();

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
        final CxWriter w2 = CxWriter.createInstance(out2, false);
        w2.addAspectFragmentWriter(NodesFragmentWriter.createInstance());

        w2.start();
        w2.writeAspectElements(l2);
        w2.writeAspectElements(l3);
        w2.end();

        assertEquals("[{\"nodes\":[{\"@id\":\"3\"},{\"@id\":\"4\"}]},{\"nodes\":[{\"@id\":\"5\"}]}]",
                     out2.toString());

    }

}
