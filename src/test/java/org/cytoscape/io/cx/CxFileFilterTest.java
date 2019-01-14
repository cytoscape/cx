package org.cytoscape.io.cx;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.util.StreamUtil;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;


public class CxFileFilterTest {
	
	final String[] valid = new String[] {
			"simple1"
	};
	final String[] invalid = new String[] {
			"empty"
	};
	
	@Test
	public void FileFilterTest() throws FileNotFoundException {
		StreamUtil streamUtil = mock(StreamUtil.class);
		CytoscapeCxFileFilter filter = new CytoscapeCxFileFilter(streamUtil);
		
		File dir = new File("src/test/resources/testData");
		File [] files = dir.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".cx");
		    }
		});

		for (File xmlfile : files) {
			InputStream stream = new FileInputStream(xmlfile);
			assertTrue(filter.accepts(stream, DataCategory.NETWORK));
		}
		
		for (String str : valid) {
			InputStream stream = new FileInputStream("src/test/resources/valid/" + str + ".cx");
			assertTrue(filter.accepts(stream, DataCategory.NETWORK));
		}
		
		for (String str : invalid) {
			InputStream stream = new FileInputStream("src/test/resources/invalid/" + str + ".cx");
			assertFalse(filter.accepts(stream, DataCategory.NETWORK));
		}
	}
}