package org.cytoscape.io.cxio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.SortedMap;

import org.cytoscape.io.internal.cxio.AspectElement;
import org.cytoscape.io.internal.cxio.AspectFragmentReaderManager;
import org.cytoscape.io.internal.cxio.CartesianLayoutFragmentWriter;
import org.cytoscape.io.internal.cxio.CxConstants;
import org.cytoscape.io.internal.cxio.CxReader;
import org.cytoscape.io.internal.cxio.CxWriter;
import org.cytoscape.io.internal.cxio.EdgeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.EdgesFragmentWriter;
import org.cytoscape.io.internal.cxio.NodeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.NodesFragmentWriter;



final class TestUtil {

    final static String cyCxRoundTrip(final String input_cx) throws IOException {
        final CxReader p = CxReader.createInstance(input_cx, AspectFragmentReaderManager.createInstance()
                .getAvailableAspectFragmentReaders());
        final SortedMap<String, List<AspectElement>> res = CxReader.parseAsMap(p);

        final OutputStream out = new ByteArrayOutputStream();

        final CxWriter w = CxWriter.createInstance(out);
        w.addAspectFragmentWriter(NodesFragmentWriter.createInstance());
        w.addAspectFragmentWriter(EdgesFragmentWriter.createInstance());
        w.addAspectFragmentWriter(CartesianLayoutFragmentWriter.createInstance());
        w.addAspectFragmentWriter(NodeAttributesFragmentWriter.createInstance());
        w.addAspectFragmentWriter(EdgeAttributesFragmentWriter.createInstance());

        w.start();
        w.write(res.get(CxConstants.NODES));
        w.write(res.get(CxConstants.EDGES));
        w.write(res.get(CxConstants.CARTESIAN_LAYOUT));
        w.write(res.get(CxConstants.NODE_ATTRIBUTES));
        w.write(res.get(CxConstants.EDGE_ATTRIBUTES));
        w.end();

        return out.toString();
    }
}
