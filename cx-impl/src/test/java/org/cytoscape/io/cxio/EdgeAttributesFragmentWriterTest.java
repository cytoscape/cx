package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.io.internal.cxio.kit.AspectElement;
import org.cytoscape.io.internal.cxio.kit.CxConstants;
import org.cytoscape.io.internal.cxio.kit.CxConstants.ATTRIBUTE_TYPE;
import org.cytoscape.io.internal.cxio.kit.EdgeAttributesElement;
import org.cytoscape.io.internal.cxio.kit.EdgeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.JsonWriter;
import org.junit.Test;

public class EdgeAttributesFragmentWriterTest {

    @Test
    public void test() throws IOException {

        final List<AspectElement> l0 = new ArrayList<AspectElement>();
        final OutputStream out0 = new ByteArrayOutputStream();
        final JsonWriter t0 = JsonWriter.createInstance(out0);

        final EdgeAttributesFragmentWriter w0 = EdgeAttributesFragmentWriter.createInstance();

        t0.start();
        w0.write(l0, t0);
        t0.end();

        assertEquals("[{\"" + CxConstants.EDGE_ATTRIBUTES + "\":[]}]", out0.toString());

        final EdgeAttributesElement ea0 = new EdgeAttributesElement("00");
        ea0.addEdge("000");
        ea0.addEdge("001");
        ea0.putValue("A", "a1");
        ea0.putValue("A", "a2");
        ea0.putValue("A", "a3");

        ea0.putValue("B", "b1");
        ea0.putValue("B", "b2");
        ea0.putValue("B", "b3");

        ea0.putValue("X", "false");
        ea0.putType("X", "boolean");

        ea0.putValue("Y", "true");
        ea0.putType("Y", ATTRIBUTE_TYPE.BOOLEAN);
        ea0.putValue("Z", true);

        ea0.putValue("L", 1l);
        ea0.putValue("D", 2.0);
        ea0.putValue("F", 3.0f);
        ea0.putValue("I", 4);
        ea0.putValue("I", 5);
        ea0.putValue("I", 6);

        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(ea0);

        final OutputStream out1 = new ByteArrayOutputStream();
        final JsonWriter t1 = JsonWriter.createInstance(out1);

        final EdgeAttributesFragmentWriter w1 = EdgeAttributesFragmentWriter.createInstance();

        t1.start();
        w1.write(l1, t1);
        t1.end();

        assertEquals("[{\"edgeAttributes\":[{\"@id\":\"00\",\"edges\":[\"000\",\"001\"],\"types\":{\"D\":\"double\",\"F\":\"float\",\"I\":\"integer\",\"L\":\"long\",\"X\":\"boolean\",\"Y\":\"boolean\",\"Z\":\"boolean\"},\"attributes\":{\"A\":[\"a1\",\"a2\",\"a3\"],\"B\":[\"b1\",\"b2\",\"b3\"],\"D\":[\"2.0\"],\"F\":[\"3.0\"],\"I\":[\"4\",\"5\",\"6\"],\"L\":[\"1\"],\"X\":[\"false\"],\"Y\":[\"true\"],\"Z\":[\"true\"]}}]}]",
                     out1.toString());
    }

}
