package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cytoscape.io.internal.cxio.AspectElement;
import org.cytoscape.io.internal.cxio.CxConstants;
import org.cytoscape.io.internal.cxio.EdgeAttributesElement;
import org.cytoscape.io.internal.cxio.EdgeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.JsonWriter;
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

        final List<String> edges = new ArrayList<String>();
        edges.add("000");
        edges.add("001");
        final SortedMap<String, List<String>> attributes = new TreeMap<String, List<String>>();

        final List<String> v1 = new ArrayList<String>();
        v1.add("a1");
        v1.add("a2");
        v1.add("a3");

        final List<String> v2 = new ArrayList<String>();
        v2.add("b1");
        v2.add("b2");
        v2.add("b3");

        attributes.put("A", v1);
        attributes.put("B", v2);

        final EdgeAttributesElement ea0 = new EdgeAttributesElement("00", edges, attributes);

        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(ea0);

        final OutputStream out1 = new ByteArrayOutputStream();
        final JsonWriter t1 = JsonWriter.createInstance(out1);

        final EdgeAttributesFragmentWriter w1 = EdgeAttributesFragmentWriter.createInstance();

        t1.start();
        w1.write(l1, t1);
        t1.end();

        assertEquals(
                "[{\""
                        + CxConstants.EDGE_ATTRIBUTES
                        + "\":[{\"@id\":\"00\",\"edges\":[\"000\",\"001\"],\"attributes\":{\"A\":[\"a1\",\"a2\",\"a3\"],\"B\":[\"b1\",\"b2\",\"b3\"]}}]}]",
                        out1.toString());
    }

}
