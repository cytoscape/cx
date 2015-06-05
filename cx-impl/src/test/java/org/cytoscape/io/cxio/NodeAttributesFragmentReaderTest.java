package org.cytoscape.io.cxio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

import org.cytoscape.io.internal.cxio.AspectElement;
import org.cytoscape.io.internal.cxio.AspectFragmentReaderManager;
import org.cytoscape.io.internal.cxio.CxConstants;
import org.cytoscape.io.internal.cxio.CxReader;
import org.cytoscape.io.internal.cxio.NodeAttributesElement;
import org.junit.Test;



public class NodeAttributesFragmentReaderTest {

    @Test
    public void test() throws IOException {
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
                + "{\"weHaveNodesToo\":[{\"nodes\":\"nodes\"}]},"
                + "{\"weHaveEdgesToo\":[{\"edges\":\"edges\"}]},"
                + "{\"nodes\":[{\"@id\":\"_5\"}]},"
                + "{\"edges\":[{\"@id\":\"e2\",\"source\":\"_4\",\"target\":\"_5\"}]},"
                + "{\"edges\":[{\"@id\":\"e3\",\"source\":\"_6\",\"target\":\"_7\"}]},"
                + "{\"cartesianLayout\":[{\"node\":\"_0\",\"x\":\"123\",\"y\":\"456\"}]},"
                + "{\"nodes\":[{\"@id\":\"_4\"}]},"
                + "{\"nodes\":[{\"@id\":\"_6\"}]},"
                + "{\"cartesianLayout\":[{\"node\":\"_1\",\"x\":\"3\",\"y\":\"4\"},{\"node\":\"_2\",\"x\":\"5\",\"y\":\"6\"}]},"
                + "{\"nodes\":[{\"@id\":\"_7\"}]},"
                + "{\"nodeAttributes\":[{\"@id\":\"_na0\",\"nodes\":[\"_0\", \"_1\"], \"attributes\":{\"entrez_gene_locuslink\":[\"322397\", \"one more\"],\"name\":[\"_322397\"],\"PSIMI_25_aliases\":[\"322397\",\"80961\"]}},"
                + "{\"@id\":\"_na1\",\"nodes\":[\"_2\"], \"attributes\":{\"key\":[\"value\"]}},"
                + "{\"@id\":\"_na2\",\"nodes\":[\"_3\"]}]},"
                + "{\"edgeAttributes\":[{\"@id\":\"_ea0\",\"edges\":[\"_e0\", \"_e22\"], \"attributes\":{\"interaction\":[\"479019\", \"one more\"],\"name\":[\"768303 (479019) 791595\"],\"PSIMI_25_detection_method\":[\"genetic interference\"]}}]},"
                + "{\"nodeAttributes\":[{\"@id\":\"_na3\",\"nodes\":[\"_33\"]}]}" + "]";

        final CxReader p = CxReader.createInstance(t0, AspectFragmentReaderManager.createInstance()
                .getAvailableAspectFragmentReaders());
        final SortedMap<String, List<AspectElement>> r0 = CxReader.parseAsMap(p);

        assertTrue("failed to parse " + CxConstants.NODE_ATTRIBUTES + " aspect",
                r0.containsKey(CxConstants.NODE_ATTRIBUTES));
        assertFalse("failed to parse " + CxConstants.NODE_ATTRIBUTES + " aspect", r0.get(CxConstants.NODE_ATTRIBUTES)
                .isEmpty());
        assertTrue("failed to get expected number of " + CxConstants.NODE_ATTRIBUTES + " aspects",
                r0.get(CxConstants.NODE_ATTRIBUTES).size() == 4);

        final List<AspectElement> aspects = r0.get(CxConstants.NODE_ATTRIBUTES);

        final NodeAttributesElement na1 = (NodeAttributesElement) aspects.get(0);
        assertTrue(na1.getId().equals("_na0"));
        assertTrue(na1.getNodes().size() == 2);
        assertTrue(na1.getAttributes().size() == 3);
        assertTrue(na1.getNodes().contains("_0"));
        assertTrue(na1.getNodes().contains("_1"));
        assertTrue(na1.getAttributes().get("PSIMI_25_aliases").size() == 2);
        assertTrue(na1.getAttributes().get("entrez_gene_locuslink").size() == 2);
        assertTrue(na1.getAttributes().get("name").size() == 1);
        assertTrue(na1.getAttributes().get("PSIMI_25_aliases").contains("322397"));
        assertTrue(na1.getAttributes().get("PSIMI_25_aliases").contains("80961"));
        assertTrue(na1.getAttributes().get("entrez_gene_locuslink").contains("322397"));
        assertTrue(na1.getAttributes().get("entrez_gene_locuslink").contains("one more"));
        assertTrue(na1.getAttributes().get("name").contains("_322397"));

        final NodeAttributesElement na2 = (NodeAttributesElement) aspects.get(1);
        assertTrue(na2.getId().equals("_na1"));
        assertTrue(na2.getNodes().size() == 1);
        assertTrue(na2.getAttributes().size() == 1);
        assertTrue(na2.getNodes().contains("_2"));
        assertTrue(na2.getAttributes().get("key").size() == 1);
        assertTrue(na2.getAttributes().get("key").contains("value"));

        final NodeAttributesElement na3 = (NodeAttributesElement) aspects.get(2);
        assertTrue(na3.getId().equals("_na2"));
        assertTrue(na3.getNodes().size() == 1);
        assertTrue(na3.getAttributes().size() == 0);
        assertTrue(na3.getNodes().contains("_3"));

        final NodeAttributesElement na4 = (NodeAttributesElement) aspects.get(3);
        assertTrue(na4.getId().equals("_na3"));
        assertTrue(na4.getNodes().size() == 1);
        assertTrue(na4.getAttributes().size() == 0);
        assertTrue(na4.getNodes().contains("_33"));

    }
}