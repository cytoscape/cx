package org.cytoscape.io.internal.cxio;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Paint;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


public class CxUtilTest {

	  @SuppressWarnings("unchecked")
	    @Test
	    public void testDataTypeIsValid() {
	        // Mock VisualProperty for Boolean
		  
		  VisualProperty<Boolean> b = BasicVisualLexicon.EDGE_LABEL_AUTOROTATE;
	        boolean result = CxUtil.dataTypeIsValid(b, "boolean");
	        assertTrue(result); 
		    assertFalse(CxUtil.dataTypeIsValid(b, "integer"));
		    assertTrue(CxUtil.dataTypeIsValid(b, "string"));
		    assertFalse(CxUtil.dataTypeIsValid(b, "long"));
		    assertFalse(CxUtil.dataTypeIsValid(b, "double"));
		    assertFalse(CxUtil.dataTypeIsValid(b, "list-of-float"));
            
            VisualProperty<Integer> d = BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
            assertTrue(CxUtil.dataTypeIsValid(d, "double"));
            assertFalse(CxUtil.dataTypeIsValid(d, "boolean"));
            assertTrue(CxUtil.dataTypeIsValid(d, "string"));
            
            VisualProperty<Double> i = BasicVisualLexicon.EDGE_LABEL_WIDTH;
            assertTrue(CxUtil.dataTypeIsValid(i, "integer"));
            assertFalse(CxUtil.dataTypeIsValid(i, "boolean"));
            assertTrue(CxUtil.dataTypeIsValid(i, "string"));
            assertTrue(CxUtil.dataTypeIsValid(i, "long"));
            assertTrue(CxUtil.dataTypeIsValid(i, "double"));
            
            VisualProperty<Paint> s = BasicVisualLexicon.EDGE_LABEL_COLOR;
            assertFalse(CxUtil.dataTypeIsValid(s, "integer"));
            assertFalse(CxUtil.dataTypeIsValid(s, "boolean"));
            assertTrue(CxUtil.dataTypeIsValid(s, "string"));
            assertFalse(CxUtil.dataTypeIsValid(s, "long"));
            assertFalse(CxUtil.dataTypeIsValid(s, "double"));
            assertFalse(CxUtil.dataTypeIsValid(s, "list-of-float"));
            
	        

	    }
}
