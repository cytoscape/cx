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

        final NodeAttributesElement na0 = new NodeAttributesElement("00");
        na0.addNode("000");
        na0.addNode("001");
        na0.put("X", "x1", ATTRIBUTE_TYPE.STRING);
        na0.put("X", "x2", ATTRIBUTE_TYPE.STRING);
        na0.put("X", "x3", ATTRIBUTE_TYPE.STRING);
        na0.put("Y", "y1", ATTRIBUTE_TYPE.STRING);
        na0.put("Y", "y2", ATTRIBUTE_TYPE.STRING);
        na0.put("Y", "y3", ATTRIBUTE_TYPE.STRING);
        na0.put("I", "1", ATTRIBUTE_TYPE.INTEGER);
        na0.put("D", "-1.111", ATTRIBUTE_TYPE.DOUBLE);
        na0.put("F", "2.01", ATTRIBUTE_TYPE.FLOAT);
        na0.put("L", "1111", ATTRIBUTE_TYPE.LONG);
        na0.put("B", "true", ATTRIBUTE_TYPE.BOOLEAN);

        final List<AspectElement> l1 = new ArrayList<AspectElement>();
        l1.add(na0);

        final OutputStream out1 = new ByteArrayOutputStream();
        final JsonWriter t1 = JsonWriter.createInstance(out1);

        final NodeAttributesFragmentWriter w1 = NodeAttributesFragmentWriter.createInstance();

        t1.start();
        w1.write(l1, t1);
        t1.end();

        assertEquals("[{\"nodeAttributes\":[{\"@id\":\"00\",\"nodes\":[\"000\",\"001\"],\"types\":{\"B\":\"boolean\",\"D\":\"double\",\"F\":\"float\",\"I\":\"integer\",\"L\":\"long\"},\"attributes\":{\"B\":[\"true\"],\"D\":[\"-1.111\"],\"F\":[\"2.01\"],\"I\":[\"1\"],\"L\":[\"1111\"],\"X\":[\"x1\",\"x2\",\"x3\"],\"Y\":[\"y1\",\"y2\",\"y3\"]}}]}]",
                     out1.toString());

    }

}
