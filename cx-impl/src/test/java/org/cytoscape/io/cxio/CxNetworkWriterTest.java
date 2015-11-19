package org.cytoscape.io.cxio;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.SortedMap;

import org.cxio.aux.AspectElementCounts;
import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.metadata.MetaDataCollection;
import org.cytoscape.io.internal.cx_reader.CxToCy;
import org.cytoscape.io.internal.cxio.Aspect;
import org.cytoscape.io.internal.cxio.AspectSet;
import org.cytoscape.io.internal.cxio.CxExporter;
import org.cytoscape.io.internal.cxio.CxImporter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.junit.Test;

public class CxNetworkWriterTest {

    private List<CyNetwork> loadNetwork(final File test_file, final boolean must_have_meta) throws Exception {
        final NetworkTestSupport nts = new NetworkTestSupport();
        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
        aspects.addAspect(Aspect.HIDDEN_ATTRIBUTES);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.NETWORK_RELATIONS);
        aspects.addAspect(Aspect.SUBNETWORKS);
        aspects.addAspect(Aspect.GROUPS);

        final CxImporter cx_importer = CxImporter.createInstance();

        SortedMap<String, List<AspectElement>> res = null;
        final InputStream is = new FileInputStream(test_file);
        final CxReader cxr = cx_importer.obtainCxReader(aspects, is);
        res = CxReader.parseAsMap(cxr);
        final AspectElementCounts counts = cxr.getAspectElementCounts();

        final MetaDataCollection pre = cxr.getPreMetaData();
        final MetaDataCollection post = cxr.getPostMetaData();
        assertTrue(counts != null);
        if (must_have_meta) {
            assertTrue((pre != null) || ((post != null) && ((pre.size() > 0) || (post.size() > 0))));
        }

        final CxToCy cx_to_cy = new CxToCy();

        final CyNetworkFactory network_factory = nts.getNetworkFactory();

        final CyRootNetwork root_network = null;
        final List<CyNetwork> networks = cx_to_cy.createNetwork(res, root_network, network_factory, null, true);
        assertTrue((networks != null));
        return networks;
    }

    private List<CyNetwork> loadNetwork(final InputStream in, final boolean must_have_meta) throws Exception {
        final NetworkTestSupport nts = new NetworkTestSupport();
        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
        aspects.addAspect(Aspect.HIDDEN_ATTRIBUTES);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.NETWORK_RELATIONS);
        aspects.addAspect(Aspect.SUBNETWORKS);
        aspects.addAspect(Aspect.GROUPS);

        final CxImporter cx_importer = CxImporter.createInstance();

        SortedMap<String, List<AspectElement>> res = null;

        final CxReader cxr = cx_importer.obtainCxReader(aspects, in);
        res = CxReader.parseAsMap(cxr);
        final AspectElementCounts counts = cxr.getAspectElementCounts();

        final MetaDataCollection pre = cxr.getPreMetaData();
        final MetaDataCollection post = cxr.getPostMetaData();
        assertTrue(counts != null);
        if (must_have_meta) {
            assertTrue((pre != null) || ((post != null) && ((pre.size() > 0) || (post.size() > 0))));
        }

        final CxToCy cx_to_cy = new CxToCy();

        final CyNetworkFactory network_factory = nts.getNetworkFactory();

        final CyRootNetwork root_network = null;
        final List<CyNetwork> networks = cx_to_cy.createNetwork(res, root_network, network_factory, null, true);
        assertTrue((networks != null));
        return networks;
    }

    private ByteArrayOutputStream writeNetwork(final CyNetwork network) throws Exception {
        final AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.HIDDEN_ATTRIBUTES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);
        aspects.addAspect(Aspect.SUBNETWORKS);
        aspects.addAspect(Aspect.VIEWS);
        aspects.addAspect(Aspect.NETWORK_RELATIONS);
        aspects.addAspect(Aspect.GROUPS);

        final CxExporter exporter = CxExporter.createInstance();
        exporter.setUseDefaultPrettyPrinting(true);
        // exporter.setLexicon(_lexicon);
        // exporter.setVisualMappingManager(_visual_mapping_manager);
        // exporter.setNetworkViewManager(_networkview_manager);
        exporter.setGroupManager(null);
        exporter.setWritePreMetadata(true);
        exporter.setWritePostMetadata(true);
        exporter.setNextSuid(SUIDFactory.getNextSUID());

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.writeNetwork(network, true, aspects, out);
        return out;

    }

    @Test
    public void test1() throws Exception {
        final File test_file = new File("src/test/resources/testData/gal_filtered_1.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, true);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 331);
        assertTrue(n.getEdgeCount() == 362);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 331);
        assertTrue(n2.getEdgeCount() == 362);

    }

    @Test
    public void test2() throws Exception {
        final File test_file = new File("src/test/resources/testData/gal_filtered_2.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, true);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 331);
        assertTrue(n.getEdgeCount() == 362);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 331);
        assertTrue(n2.getEdgeCount() == 362);
    }

    @Test
    public void test3() throws Exception {
        final File test_file = new File("src/test/resources/testData/various_mappings_gradients.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, true);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 6);
        assertTrue(n.getEdgeCount() == 5);
    }

    @Test
    public void test4() throws Exception {
        final File test_file = new File("src/test/resources/testData/collection_1.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, true);
        assertTrue((networks.size() == 3));
        final CyNetwork n1 = networks.get(0);
        assertTrue(n1.getNodeCount() == 2);
        assertTrue(n1.getEdgeCount() == 1);
        final CyNetwork n2 = networks.get(1);
        assertTrue(n2.getNodeCount() == 3);
        assertTrue(n2.getEdgeCount() == 3);
        final CyNetwork n3 = networks.get(2);
        assertTrue(n3.getNodeCount() == 2);
        assertTrue(n3.getEdgeCount() == 1);

        final ByteArrayOutputStream out = writeNetwork(n1);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);

    }

    @Test
    public void test5() throws Exception {
        final File test_file = new File("src/test/resources/testData/collection_2.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, true);
        assertTrue((networks.size() == 4));
        final CyNetwork n1 = networks.get(0);
        assertTrue(n1.getNodeCount() == 2);
        assertTrue(n1.getEdgeCount() == 1);
        final CyNetwork n2 = networks.get(1);
        assertTrue(n2.getNodeCount() == 5);
        assertTrue(n2.getEdgeCount() == 10);
        final CyNetwork n3 = networks.get(2);
        assertTrue(n3.getNodeCount() == 8);
        assertTrue(n3.getEdgeCount() == 28);
        final CyNetwork n4 = networks.get(3);
        assertTrue(n4.getNodeCount() == 4);
        assertTrue(n4.getEdgeCount() == 6);
    }

    @Test
    public void test6() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_only.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 4);
        assertTrue(n2.getEdgeCount() == 3);
    }

    @Test
    public void test7() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_network_atts.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 4);
        assertTrue(n2.getEdgeCount() == 3);
    }

    @Test
    public void test8() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_netw_node_edge_atts.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 4);
        assertTrue(n2.getEdgeCount() == 3);
    }

    @Test
    public void test9() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_netw_node_edge_atts_coords.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 4);
        assertTrue(n2.getEdgeCount() == 3);
    }

    @Test
    public void test10() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_netw_node_edge_atts_coords_vis_prop_1.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);
    }

    @Test
    public void test11() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_netw_node_edge_atts_coords_vis_prop_2.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);
    }

    @Test
    public void test12() throws Exception {
        final File test_file = new File("src/test/resources/testData/nodes_edges_netw_node_edge_atts_coords_vis_prop_3.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 4);
        assertTrue(n.getEdgeCount() == 3);
    }

    @Test
    public void test13() throws Exception {
        final File test_file = new File("src/test/resources/testData/groups_1.cx");
        final List<CyNetwork> networks = loadNetwork(test_file, false);
        assertTrue((networks.size() == 1));
        final CyNetwork n = networks.get(0);
        assertTrue(n.getNodeCount() == 5);
        assertTrue(n.getEdgeCount() == 5);

        final ByteArrayOutputStream out = writeNetwork(n);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
        final List<CyNetwork> networks_2 = loadNetwork(in, true);
        assertTrue((networks_2.size() == 1));
        final CyNetwork n2 = networks_2.get(0);
        assertTrue(n2.getNodeCount() == 5);
        assertTrue(n2.getEdgeCount() == 5);
    }

}
