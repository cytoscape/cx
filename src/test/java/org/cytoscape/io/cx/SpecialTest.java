package org.cytoscape.io.cx;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.cx.helpers.TestUtil.CxReaderWrapper;
import org.junit.Test;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.model.cx.NiceCXNetwork;

public class SpecialTest {

	@Test
	public void testLocationPassthrough() throws IOException {
		File f = TestUtil.getResource("specialCases", "node_location_map.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		Map<Long, Collection<AspectElement>> nodeAspectIn = reader.getNiceCX().getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME);
		Collection<AspectElement> aesIn = nodeAspectIn.get(1018l);
		assertEquals(aesIn.size(), 1);
		CartesianLayoutElement caeIn = (CartesianLayoutElement) aesIn.iterator().next();
		assertEquals(caeIn.getX(), -3204.789794921875, 0);
		assertEquals(caeIn.getY(), 775.8062232824261, 0);
		
		NiceCXNetwork output = TestUtil.getOutput(reader);
		Map<Long, Collection<AspectElement>> nodeAspect = output.getNodeAssociatedAspect(CartesianLayoutElement.ASPECT_NAME);
		
		Collection<AspectElement> aes = nodeAspect.get(1018l);
		assertEquals(aes.size(), 1);
		CartesianLayoutElement cae = (CartesianLayoutElement) aes.iterator().next();
		assertEquals(cae.getX(), -39.129654, 0);
		assertEquals(cae.getY(), 90.83788, 0);
		
		
	}
	
	@Test
	public void testMismatchedAttributeTypes() throws IOException {
		File f = TestUtil.getResource("specialCases", "n3_pp.cx");
		CxReaderWrapper reader = TestUtil.getSubNetwork(f);
		try {
			TestUtil.getOutput(reader);
		}catch(IllegalArgumentException e) {
			assertEquals(e.getMessage(), "Invalid value for length:[1079, 1081]. invalid type: class java.util.ArrayList");
		}
	}
}
