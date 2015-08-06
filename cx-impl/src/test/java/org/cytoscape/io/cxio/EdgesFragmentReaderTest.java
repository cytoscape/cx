package org.cytoscape.io.cxio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.junit.Test;

public class EdgesFragmentReaderTest {

    @Test
    public void testEdgesAspectParsing() throws IOException, ClassNotFoundException {
        final String t0 = "["
                + "{\"nodes_we_ignore\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"},{\"@id\":\"_2\"},{\"@id\":\"_3\"}]},"
                + "{\"nodes\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"},{\"@id\":\"_2\"},{\"@id\":\"_3\"}]},"
                + "{\"edges\":[{\"@id\":\"e0\",\"source\":\"_0\",\"target\":\"_1\"},{\"@id\":\"e1\",\"source\":\"_1\",\"target\":\"_2\"}]},"
                + "{\"nodeIdentities\":[{\"@id\":\"ni0\",\"nodes\":\"_0\",\"represents\":\"name is zero\"},{\"@id\":\"ni1\",\"node\":\"_1\",\"represents\":\"name is one\"}]},"
                + "{\"edgeIdentities\":[{\"@id\":\"ei0\",\"edges\":\"e0\",\"relationship\":\"BEL:INCREASES\"},{\"@id\":\"ei1\",\"edge\":\"e1\",\"relationship\":\"BEL:DECREASES\"}]},"
                + "{\"elementProperties\":[{\"@id\":\"ep0\",\"elementId\":\"_0\",\"property\":\"property zero\",\"value\":\"value is zero\"},{\"@id\":\"ep1\",\"elementId\":\"_1\",\"property\":\"propery one\",\"value\":\"value is one\"}]},"
                + "{\"functionTerms\":[{\"@id\":\"ft0\",\"function\":\"functions zero\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]},{\"@id\":\"ft1\",\"function\":\"functions one\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]}]},"
                + "{\"weHaveNodesAndEdges\":[{\"nodes\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"}]}]},"
                + "{\"weHaveNodesAndEdges\":[{\"edges\":[{\"@id\":\"e0\",\"source\":\"_0\",\"target\":\"_1\"}]}]},"
                + "{\"weHaveNodesToo\":[{\"nodes\":\"nodes\"}]}," + "{\"weHaveEdgesToo\":[{\"edges\":\"edges\"}]},"
                + "{\"nodes\":[{\"@id\":\"_5\"}]},"
                + "{\"edges\":[{\"@id\":\"e2\",\"source\":\"_4\",\"target\":\"_5\"}]},"
                + "{\"edges\":[{\"@id\":\"e3\",\"source\":\"_6\",\"target\":\"_7\"}]},"
                + "{\"nodes\":[{\"@id\":\"_4\"}]}," + "{\"nodes\":[{\"@id\":\"_6\"}]},"
                + "{\"nodes\":[{\"@id\":\"_7\"}]}" + "]";

        final CxReader p = CxReader.createInstance(t0, TestUtil.getCytoscapeAspectFragmentReaders());
        final SortedMap<String, List<AspectElement>> r0 = CxReader.parseAsMap(p);

        assertTrue("failed to parse " + EdgesElement.NAME + " aspect", r0.containsKey(EdgesElement.NAME));

        assertFalse("failed to parse " + EdgesElement.NAME + " aspect", r0.get(EdgesElement.NAME).isEmpty());

        assertTrue("failed to parse expected number of " + EdgesElement.NAME + " aspects", r0.get(EdgesElement.NAME)
                .size() == 4);

        final List<AspectElement> edge_aspects = r0.get(EdgesElement.NAME);

        assertTrue("failed to get expected NodeAspect instance", edge_aspects.get(0) instanceof EdgesElement);

        assertTrue("failed to get expected " + EdgesElement.NAME + " aspect",
                   edge_aspects.contains(new EdgesElement("e0", "0", "0")));
        assertTrue("failed to get expected " + EdgesElement.NAME + " aspect",
                   edge_aspects.contains(new EdgesElement("e1", "0", "0")));
        assertTrue("failed to get expected " + EdgesElement.NAME + " aspect",
                   edge_aspects.contains(new EdgesElement("e2", "0", "0")));
        assertTrue("failed to get expected " + EdgesElement.NAME + " aspect",
                   edge_aspects.contains(new EdgesElement("e3", "0", "0")));

    }

    @Test(expected = java.io.IOException.class)
    public void testEdgesAspectParsingWithExpectedException2() throws IOException {
        final String t0 = "["
                + "{\"nodes\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"},{\"@id\":\"_2\"},{\"@id\":\"_3\"}]},"
                + "{\"edges\":[{\"@id\":\"e0\",\"sources\":\"_0\",\"target\":\"_1\"},{\"@id\":\"e1\",\"source\":\"_1\",\"target\":\"_2\"}]},"
                + "{\"nodeIdentities\":[{\"@id\":\"ni0\",\"node\":\"_0\",\"represents\":\"name is zero\"},{\"@id\":\"ni1\",\"node\":\"_1\",\"represents\":\"name is one\"}]},"
                + "{\"edgeIdentities\":[{\"@id\":\"ei0\",\"edge\":\"e0\",\"relationship\":\"BEL:INCREASES\"},{\"@id\":\"ei1\",\"edge\":\"e1\",\"relationship\":\"BEL:DECREASES\"}]},"
                + "{\"elementProperties\":[{\"@id\":\"ep0\",\"elementId\":\"_0\",\"property\":\"property zero\",\"value\":\"value is zero\"},{\"@id\":\"ep1\",\"elementId\":\"_1\",\"property\":\"propery one\",\"value\":\"value is one\"}]},"
                + "{\"functionTerms\":[{\"@id\":\"ft0\",\"function\":\"functions zero\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]},{\"@id\":\"ft1\",\"function\":\"functions one\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]}]},"
                + "{\"edges\":[{\"@id\":\"e2\",\"source\":\"_4\",\"target\":\"_5\"}]},"
                + "{\"edges\":[{\"@id\":\"e3\",\"source\":\"_6\",\"target\":\"_7\"}]}" + "]";

        final CxReader p = CxReader.createInstance(t0, TestUtil.getCytoscapeAspectFragmentReaders());
        CxReader.parseAsMap(p);

    }

    @Test(expected = java.io.IOException.class)
    public void testEdgesAspectParsingWithExpectedException3() throws IOException {
        final String t0 = "["
                + "{\"nodes\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"},{\"@id\":\"_2\"},{\"@id\":\"_3\"}]},"
                + "{\"edges\":[{\"@id\":\"e0\",\"source\":\"_0\",\"targets\":\"_1\"},{\"@id\":\"e1\",\"source\":\"_1\",\"target\":\"_2\"}]},"
                + "{\"nodeIdentities\":[{\"@id\":\"ni0\",\"node\":\"_0\",\"represents\":\"name is zero\"},{\"@id\":\"ni1\",\"node\":\"_1\",\"represents\":\"name is one\"}]},"
                + "{\"edgeIdentities\":[{\"@id\":\"ei0\",\"edge\":\"e0\",\"relationship\":\"BEL:INCREASES\"},{\"@id\":\"ei1\",\"edge\":\"e1\",\"relationship\":\"BEL:DECREASES\"}]},"
                + "{\"elementProperties\":[{\"@id\":\"ep0\",\"elementId\":\"_0\",\"property\":\"property zero\",\"value\":\"value is zero\"},{\"@id\":\"ep1\",\"elementId\":\"_1\",\"property\":\"propery one\",\"value\":\"value is one\"}]},"
                + "{\"functionTerms\":[{\"@id\":\"ft0\",\"function\":\"functions zero\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]},{\"@id\":\"ft1\",\"function\":\"functions one\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]}]},"
                + "{\"edges\":[{\"@id\":\"e2\",\"source\":\"_4\",\"target\":\"_5\"}]},"
                + "{\"edges\":[{\"@id\":\"e3\",\"source\":\"_6\",\"target\":\"_7\"}]}" + "]";

        final CxReader p = CxReader.createInstance(t0, TestUtil.getCytoscapeAspectFragmentReaders());
        CxReader.parseAsMap(p);

    }

    @Test
    public void testEdgesAspectParsingWithNoEdgesAspectPresent() throws IOException {
        final String t0 = "["
                + "{\"nodes_we_ignore\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"},{\"@id\":\"_2\"},{\"@id\":\"_3\"}]},"
                + "{\"edges_we_ignore\":[{\"@id\":\"e0\",\"source\":\"_0\",\"target\":\"_1\"},{\"@id\":\"e1\",\"source\":\"_1\",\"target\":\"_2\"}]},"
                + "{\"nodeIdentities\":[{\"@id\":\"ni0\",\"nodes\":\"_0\",\"represents\":\"name is zero\"},{\"@id\":\"ni1\",\"node\":\"_1\",\"represents\":\"name is one\"}]},"
                + "{\"edgeIdentities\":[{\"@id\":\"ei0\",\"edges\":\"e0\",\"relationship\":\"BEL:INCREASES\"},{\"@id\":\"ei1\",\"edge\":\"e1\",\"relationship\":\"BEL:DECREASES\"}]},"
                + "{\"elementProperties\":[{\"@id\":\"ep0\",\"elementId\":\"_0\",\"property\":\"property zero\",\"value\":\"value is zero\"},{\"@id\":\"ep1\",\"elementId\":\"_1\",\"property\":\"propery one\",\"value\":\"value is one\"}]},"
                + "{\"functionTerms\":[{\"@id\":\"ft0\",\"function\":\"functions zero\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]},{\"@id\":\"ft1\",\"function\":\"functions one\",\"parameters\":[\"HGNC:FAS\",\"HGNC:MAPK1\"]}]},"
                + "{\"weHaveNodesAndEdges\":[{\"nodes\":[{\"@id\":\"_0\"},{\"@id\":\"_1\"}]}]},"
                + "{\"weHaveNodesAndEdges\":[{\"edges\":[{\"@id\":\"e0\",\"source\":\"_0\",\"target\":\"_1\"}]}]},"
                + "{\"weHaveNodesToo\":[{\"nodes\":\"nodes\"}]}," + "{\"weHaveEdgesToo\":[{\"edges\":\"edges\"}]},"
                + "{\"edges_we_ignore\":[{\"@id\":\"e2\",\"source\":\"_4\",\"target\":\"_5\"}]},"
                + "{\"edges_we_ignore\":[{\"@id\":\"e3\",\"source\":\"_6\",\"target\":\"_7\"}]}"

                + "]";

        final CxReader p = CxReader.createInstance(t0, TestUtil.getCytoscapeAspectFragmentReaders());
        final SortedMap<String, List<AspectElement>> r0 = CxReader.parseAsMap(p);

        assertFalse("expected to parse no " + EdgesElement.NAME + " aspects, got some",
                    r0.containsKey(EdgesElement.NAME));

    }

}
