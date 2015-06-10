package org.cytoscape.io.cxio;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.cytoscape.io.internal.cxio.kit.AspectElement;
import org.cytoscape.io.internal.cxio.kit.CxConstants;
import org.cytoscape.io.internal.cxio.kit.JsonWriter;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesElement;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesFragmentWriter;
import org.junit.Test;

public class NodeAttributesFragmentWriterTest {

    @Test
    public void test() throws IOException {

        final List<AspectElement> l0 = new ArrayList<AspectElement>();
        final OutputStream out0 = new ByteArrayOutputStream();
        final JsonWriter t0 = JsonWriter.createInstance(out0);

        final NodeAttributesFragmentWriter w0 = NodeAttributesFragmentWriter.createInstance();

        t0.start();
        w0.write(l0, t0);
        t0.end();

        assertEquals("[{\"" + CxConstants.NODE_ATTRIBUTES + "\":[]}]", out0.toString());

        final List<String> nodes = new ArrayList<String>();
        nodes.add("000");
        nodes.add("001");
        final SortedMap<String, List<String>> attributes = new TreeMap<String, List<String>>();

        final List<String> v1 = new ArrayList<String>();
        v1.add("x1");
        v1.add("x2");
        v1.add("x3");

        final List<String> v2 = new ArrayList<String>();
        v2.add("y1");
        v2.add("y2");
        v2.add("y3");

        attributes.put("X", v1);
        attributes.put("Y", v2);

        final NodeAttributesElement ea0 = new NodeAttributesElement("00", nodes, attributes);

        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(ea0);

        final OutputStream out1 = new ByteArrayOutputStream();
        final JsonWriter t1 = JsonWriter.createInstance(out1);

        final NodeAttributesFragmentWriter w1 = NodeAttributesFragmentWriter.createInstance();

        t1.start();
        w1.write(l1, t1);
        t1.end();

        assertEquals(
                "[{\""
                        + CxConstants.NODE_ATTRIBUTES
                        + "\":[{\"@id\":\"00\",\"nodes\":[\"000\",\"001\"],\"attributes\":{\"X\":[\"x1\",\"x2\",\"x3\"],\"Y\":[\"y1\",\"y2\",\"y3\"]}}]}]",
                        out1.toString());

    }

}
