package org.cytoscape.io.cx;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.cytoscape.model.CyNetwork;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.HiddenAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.model.cx.CitationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTests {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@BeforeClass
	public static void init() {
		TestUtil.init();
	}
	
	private CxReaderWrapper getBaseSubNetwork(NodesElement...nodes) {
		return TestUtil.getSubNetwork(TestUtil.getResource("base", "subnetwork.cx"), nodes);
	}
/*	
	@Test
	public void testNetworkAttribute() throws IOException {
		CxReaderWrapper reader = getBaseSubNetwork();
		// Add Network Attribute
		NetworkAttributesElement STRING = new NetworkAttributesElement(null, CyNetwork.NAME, "NAME TEST");
		NetworkAttributesElement INT = new NetworkAttributesElement(null, "value", "1", ATTRIBUTE_DATA_TYPE.INTEGER);
		NetworkAttributesElement BOOL = new NetworkAttributesElement(null, "bool", "true", ATTRIBUTE_DATA_TYPE.BOOLEAN);
		NetworkAttributesElement DOUBLE = new NetworkAttributesElement(null, "double", "0.2", ATTRIBUTE_DATA_TYPE.DOUBLE);
		NetworkAttributesElement LONG = new NetworkAttributesElement(null, "long", "10000", ATTRIBUTE_DATA_TYPE.LONG);
		NetworkAttributesElement STRING_EXP = new NetworkAttributesElement(null, "str", "10000", ATTRIBUTE_DATA_TYPE.STRING);
		
		//List attributes
		String[] ints = new String[] { "1", "-1", "1000"};
		NetworkAttributesElement INT_LIST = new NetworkAttributesElement(null, "values", Arrays.asList(ints), ATTRIBUTE_DATA_TYPE.LIST_OF_INTEGER);
		String[] bools = new String[] {"true", "false"};
		NetworkAttributesElement BOOL_LIST = new NetworkAttributesElement(null, "bools", Arrays.asList(bools), ATTRIBUTE_DATA_TYPE.LIST_OF_BOOLEAN);
		String[] doubles = new String[] {"0.1", "-2.4", "3.1"};
		NetworkAttributesElement DOUBLE_LIST = new NetworkAttributesElement(null, "doubles", Arrays.asList(doubles), ATTRIBUTE_DATA_TYPE.LIST_OF_DOUBLE);
		String[] longs = new String[] {"1029435", "5364563456", "356", "324245"};
		NetworkAttributesElement LONG_LIST = new NetworkAttributesElement(null, "longs", Arrays.asList(longs), ATTRIBUTE_DATA_TYPE.LIST_OF_LONG);
		String[] strings = new String[] {"1029435", "5364563456", "356", "324245"};
		NetworkAttributesElement STRING_LIST = new NetworkAttributesElement(null, "strings", Arrays.asList(strings), ATTRIBUTE_DATA_TYPE.LIST_OF_STRING);
		
		TestUtil.withAspects(reader, 
				STRING, INT, BOOL, DOUBLE, LONG, STRING_EXP,
				INT_LIST, BOOL_LIST, DOUBLE_LIST, LONG_LIST, STRING_LIST
			);
	}
	
	@Test
	public void testHiddenAttributes() throws IOException {
		CxReaderWrapper reader = getBaseSubNetwork();
		// Add Attribute
		HiddenAttributesElement STRING = new HiddenAttributesElement(null, CyNetwork.NAME, "NAME TEST");
		HiddenAttributesElement INT = new HiddenAttributesElement(null, "value", "1", ATTRIBUTE_DATA_TYPE.INTEGER);
		HiddenAttributesElement BOOL = new HiddenAttributesElement(null, "bool", "true", ATTRIBUTE_DATA_TYPE.BOOLEAN);
		HiddenAttributesElement DOUBLE = new HiddenAttributesElement(null, "double", "0.2", ATTRIBUTE_DATA_TYPE.DOUBLE);
		HiddenAttributesElement LONG = new HiddenAttributesElement(null, "long", "10000", ATTRIBUTE_DATA_TYPE.LONG);
		HiddenAttributesElement STRING_EXP = new HiddenAttributesElement(null, "str", "10000", ATTRIBUTE_DATA_TYPE.STRING);
		
		//List attributes
		String[] ints = new String[] { "1", "-1", "1000"};
		HiddenAttributesElement INT_LIST = new HiddenAttributesElement(null, "values", Arrays.asList(ints), ATTRIBUTE_DATA_TYPE.LIST_OF_INTEGER);
		String[] bools = new String[] {"true", "false"};
		HiddenAttributesElement BOOL_LIST = new HiddenAttributesElement(null, "bools", Arrays.asList(bools), ATTRIBUTE_DATA_TYPE.LIST_OF_BOOLEAN);
		String[] doubles = new String[] {"0.1", "-2.4", "3.1"};
		HiddenAttributesElement DOUBLE_LIST = new HiddenAttributesElement(null, "doubles", Arrays.asList(doubles), ATTRIBUTE_DATA_TYPE.LIST_OF_DOUBLE);
		String[] longs = new String[] {"1029435", "5364563456", "356", "324245"};
		HiddenAttributesElement LONG_LIST = new HiddenAttributesElement(null, "longs", Arrays.asList(longs), ATTRIBUTE_DATA_TYPE.LIST_OF_LONG);
		String[] strings = new String[] {"1029435", "5364563456", "356", "324245"};
		HiddenAttributesElement STRING_LIST = new HiddenAttributesElement(null, "strings", Arrays.asList(strings), ATTRIBUTE_DATA_TYPE.LIST_OF_STRING);
		
		TestUtil.withAspects(reader, 
				STRING, INT, BOOL, DOUBLE, LONG, STRING_EXP,
				INT_LIST, BOOL_LIST, DOUBLE_LIST, LONG_LIST, STRING_LIST
			);
	}
	
	@Test
	public void testNodes() throws IOException {
		NodesElement[] nodes = new NodesElement[] {
				new NodesElement(1, "RED", "r"),
				new NodesElement(2, "BLUE", "b"),
		};
		CxReaderWrapper reader = getBaseSubNetwork(nodes);
		
		TestUtil.withAspects(reader, nodes);
	}
	
	
	@Test	
	public void testEdges() throws IOException {
		NodesElement[] nodes = new NodesElement[9];
		// Node with id:0 already in CX
		for (int i = 0; i < nodes.length; i++){
				nodes[i] = new NodesElement(i+1, null, null);
		};
		CxReaderWrapper reader = getBaseSubNetwork(nodes);
		
		EdgesElement[] edges = new EdgesElement[20];
		for (int i = 0; i < edges.length; i++) {
			Long id = (long) i;
			Long s = (long) (Math.random() * nodes.length);
			Long t = (long) (Math.random() * nodes.length);

			edges[i] = new EdgesElement(id, s, t, null);
		}
		TestUtil.withAspects(reader, edges);
	}
	
	@Test
	public void testNodeAttributes() throws IOException{
		CxReaderWrapper reader = getBaseSubNetwork();
		// Add Attribute
		NodeAttributesElement STRING = new NodeAttributesElement(0l, CyNetwork.NAME, "NAME TEST", ATTRIBUTE_DATA_TYPE.STRING);
		NodeAttributesElement INT = new NodeAttributesElement(0l, "value", "1", ATTRIBUTE_DATA_TYPE.INTEGER);
		NodeAttributesElement BOOL = new NodeAttributesElement(0l, "bool", "true", ATTRIBUTE_DATA_TYPE.BOOLEAN);
		NodeAttributesElement DOUBLE = new NodeAttributesElement(0l, "double", "0.2", ATTRIBUTE_DATA_TYPE.DOUBLE);
		NodeAttributesElement LONG = new NodeAttributesElement(0l, "long", "10000", ATTRIBUTE_DATA_TYPE.LONG);
		NodeAttributesElement STRING_EXP = new NodeAttributesElement(0l, "str", "10000", ATTRIBUTE_DATA_TYPE.STRING);
		
		//List attributes
		String[] ints = new String[] { "1", "-1", "1000"};
		NodeAttributesElement INT_LIST = new NodeAttributesElement(0l, "values", Arrays.asList(ints), ATTRIBUTE_DATA_TYPE.LIST_OF_INTEGER);
		String[] bools = new String[] {"true", "false"};
		NodeAttributesElement BOOL_LIST = new NodeAttributesElement(0l, "bools", Arrays.asList(bools), ATTRIBUTE_DATA_TYPE.LIST_OF_BOOLEAN);
		String[] doubles = new String[] {"0.1", "-2.4", "3.1"};
		NodeAttributesElement DOUBLE_LIST = new NodeAttributesElement(0l, "doubles", Arrays.asList(doubles), ATTRIBUTE_DATA_TYPE.LIST_OF_DOUBLE);
		String[] longs = new String[] {"1029435", "5364563456", "356", "324245"};
		NodeAttributesElement LONG_LIST = new NodeAttributesElement(0l, "longs", Arrays.asList(longs), ATTRIBUTE_DATA_TYPE.LIST_OF_LONG);
		String[] strings = new String[] {"1029435", "5364563456", "356", "324245"};
		NodeAttributesElement STRING_LIST = new NodeAttributesElement(0l, "strings", Arrays.asList(strings), ATTRIBUTE_DATA_TYPE.LIST_OF_STRING);
		
		
		TestUtil.withAspects(reader, 
				STRING, INT, BOOL, DOUBLE, LONG, STRING_EXP,
				INT_LIST, BOOL_LIST, DOUBLE_LIST, LONG_LIST, STRING_LIST
			);
	}
	
	
	@Test
	public void testEdgeAttributes() throws IOException{
		CxReaderWrapper reader = getBaseSubNetwork();
		reader.getNiceCX().addEdge(new EdgesElement(1l, 0l, 0l, null));
		// Add Attribute
		EdgeAttributesElement STRING = new EdgeAttributesElement(1l, CyNetwork.NAME, "NAME TEST", ATTRIBUTE_DATA_TYPE.STRING);
		EdgeAttributesElement INT = new EdgeAttributesElement(1l, "value", "1", ATTRIBUTE_DATA_TYPE.INTEGER);
		EdgeAttributesElement BOOL = new EdgeAttributesElement(1l, "bool", "true", ATTRIBUTE_DATA_TYPE.BOOLEAN);
		EdgeAttributesElement DOUBLE = new EdgeAttributesElement(1l, "double", "0.2", ATTRIBUTE_DATA_TYPE.DOUBLE);
		EdgeAttributesElement LONG = new EdgeAttributesElement(1l, "long", "10000", ATTRIBUTE_DATA_TYPE.LONG);
		EdgeAttributesElement STRING_EXP = new EdgeAttributesElement(1l, "str", "10000", ATTRIBUTE_DATA_TYPE.STRING);
		
		//List attributes
		String[] ints = new String[] { "1", "-1", "1000"};
		EdgeAttributesElement INT_LIST = new EdgeAttributesElement(1l, "values", Arrays.asList(ints), ATTRIBUTE_DATA_TYPE.LIST_OF_INTEGER);
		String[] bools = new String[] {"true", "false"};
		EdgeAttributesElement BOOL_LIST = new EdgeAttributesElement(1l, "bools", Arrays.asList(bools), ATTRIBUTE_DATA_TYPE.LIST_OF_BOOLEAN);
		String[] doubles = new String[] {"0.1", "-2.4", "3.1"};
		EdgeAttributesElement DOUBLE_LIST = new EdgeAttributesElement(1l, "doubles", Arrays.asList(doubles), ATTRIBUTE_DATA_TYPE.LIST_OF_DOUBLE);
		String[] longs = new String[] {"1029435", "5364563456", "356", "324245"};
		EdgeAttributesElement LONG_LIST = new EdgeAttributesElement(1l, "longs", Arrays.asList(longs), ATTRIBUTE_DATA_TYPE.LIST_OF_LONG);
		String[] strings = new String[] {"1029435", "5364563456", "356", "324245"};
		EdgeAttributesElement STRING_LIST = new EdgeAttributesElement(1l, "strings", Arrays.asList(strings), ATTRIBUTE_DATA_TYPE.LIST_OF_STRING);
		
		
		TestUtil.withAspects(reader, 
				STRING, INT, BOOL, DOUBLE, LONG, STRING_EXP,
				INT_LIST, BOOL_LIST, DOUBLE_LIST, LONG_LIST, STRING_LIST
			);
	}
	
	@Test
	public void testCitation() throws IOException {
		CxReaderWrapper reader = getBaseSubNetwork();
		
		CitationElement citation = new CitationElement();
		citation.setDescription("DESCRIPTION");
		citation.setTitle("TITLE");
		TestUtil.withAspects(reader, citation);
	}
	
	@Test
	public void testEdgeAssociated() throws IOException {
		CxReaderWrapper reader = getBaseSubNetwork(new NodesElement(1, null, null));
		reader.getNiceCX().addEdge(new EdgesElement(2l, 0l, 1l, null));
		
		AspectElement el = new EdgeAttributesElement(2l, "UNKNOWN", "OK", ATTRIBUTE_DATA_TYPE.STRING);
		reader.getNiceCX().addEdgeAssociatedAspectElement(2l, el);
		AspectElement el2 = new EdgeAttributesElement(2l, "UNKNOWN", "1", ATTRIBUTE_DATA_TYPE.INTEGER);
		reader.getNiceCX().addEdgeAssociatedAspectElement(2l, el2);
		
		TestUtil.withAspects(reader, el2);
	}
	
	@Test
	public void testNodeAssociated() throws IOException {
		CxReaderWrapper reader = getBaseSubNetwork(new NodesElement(1, null, null));
		AspectElement el = new NodeAttributesElement(1l, "UNKNOWN", "OK", ATTRIBUTE_DATA_TYPE.STRING);
		reader.getNiceCX().addNodeAssociatedAspectElement(0l, el);
		AspectElement el2 = new NodeAttributesElement(0l, "UNKNOWN", "1", ATTRIBUTE_DATA_TYPE.INTEGER);
		reader.getNiceCX().addNodeAssociatedAspectElement(0l, el2);
		
		TestUtil.withAspects(reader, el2);
	}
	
	@Test
	public void testCartesianLayout() throws IOException {
		CxReaderWrapper reader = getBaseSubNetwork(new NodesElement(1, null, null));
		
		CartesianLayoutElement c1 = new CartesianLayoutElement(0l, "10.3", "4.2");
		reader.getNiceCX().addNodeAssociatedAspectElement(c1.getNode(), c1);
		CartesianLayoutElement c2 = new CartesianLayoutElement(1l, "-3.3", "0.2");
		reader.getNiceCX().addNodeAssociatedAspectElement(c1.getNode(), c2);
		
		TestUtil.withAspects(reader, c1, c2);
	}	

	/* Whole CX file tests */
/*	@Test
	public void testStyles() throws IOException {
		File dir = TestUtil.getResource("visualStyles");
		for (File f : dir.listFiles()){
			if (!f.getName().endsWith("label_position_errors.cx")) {
				continue;
			}
			System.out.println("------- testing: " + f.getName() + " ............");
			CxReaderWrapper reader = TestUtil.getSubNetwork(f);
			TestUtil.withAspects(reader);
		}
	} */
	
/*	@Test
	public void testSubnets() throws IOException {
		File dir = TestUtil.getResource("subnets");
		for (File f : dir.listFiles()){
			if (!f.getName().endsWith(".cx")) {
				continue;
			}
			CxReaderWrapper reader = TestUtil.getSubNetwork(f);
			TestUtil.withAspects(reader);
		}
	}
	
/*	@Test
	public void testCollections() throws IOException {
		File dir = TestUtil.getResource("collections");
		for (File f : dir.listFiles()){
			if (!f.getName().endsWith(".cx")) {
				continue;
			}
			CxReaderWrapper reader = TestUtil.getSubNetwork(f);
			TestUtil.withAspects(reader);
		}
	} */
	
}



