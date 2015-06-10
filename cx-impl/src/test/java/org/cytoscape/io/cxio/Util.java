package org.cytoscape.io.cxio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.SortedMap;

import org.cytoscape.io.internal.cxio.kit.AspectElement;
import org.cytoscape.io.internal.cxio.kit.AspectFragmentReaderManager;
import org.cytoscape.io.internal.cxio.kit.CartesianLayoutFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.CxConstants;
import org.cytoscape.io.internal.cxio.kit.CxReader;
import org.cytoscape.io.internal.cxio.kit.CxWriter;
import org.cytoscape.io.internal.cxio.kit.EdgeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.EdgesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.NodeAttributesFragmentWriter;
import org.cytoscape.io.internal.cxio.kit.NodesFragmentWriter;

final class TestUtil {

    final static String cyCxRoundTrip(final String input_cx) throws IOException {
        final CxReader p = CxReader.createInstance(input_cx, AspectFragmentReaderManager
                .createInstance().getAvailableAspectFragmentReaders());
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
