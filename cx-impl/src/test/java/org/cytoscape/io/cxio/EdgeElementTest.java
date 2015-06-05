package org.cytoscape.io.cxio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.cytoscape.io.internal.cxio.EdgesElement;
import org.junit.Test;

public class EdgeElementTest {

    @Test
    public void test() {
        final EdgesElement e0 = new EdgesElement("0", "a", "b");
        final EdgesElement e1 = new EdgesElement("0", "c", "d");
        final EdgesElement e2 = new EdgesElement("1", "e", "f");
        assertTrue(e0.equals(e1));
        assertTrue(e0.equals(e0));
        assertTrue(e1.equals(e0));
        assertFalse(e2.equals(e1));
        assertFalse(e1.equals(e2));
        assertTrue(e0.getId().equals("0"));
        assertTrue(e0.getSource().equals("a"));
        assertTrue(e0.getTarget().equals("b"));
    }

}
