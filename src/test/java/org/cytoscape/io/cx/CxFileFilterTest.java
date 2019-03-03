package org.cytoscape.io.cx;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.cx.helpers.TestUtil;
import org.cytoscape.io.internal.cx_reader.CytoscapeCxFileFilter;
import org.cytoscape.io.util.StreamUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;


public class CxFileFilterTest {
	
	private CytoscapeCxFileFilter filter;
	
	final String[] valid = new String[] {
			"simple1"
	};
	final String[] invalid = new String[] {
			"empty"
	};
	
	@Before
	public void init() {
		StreamUtil streamUtil = mock(StreamUtil.class);
		filter = new CytoscapeCxFileFilter(streamUtil);
	}
	
	@Test
	public void FileFilterTest() throws FileNotFoundException {
		
		File dir = TestUtil.getResource("subnets");
		checkFiles(dir, true);

		checkFile(TestUtil.getResource("specialCases", "empty.cx"), false);
		
	}
	
	public void checkFile(File f, boolean valid) throws FileNotFoundException {
		InputStream stream = new FileInputStream(f);
		assertEquals(f.getName() + " incorrectly read by CxFileFilter", valid, filter.accepts(stream, DataCategory.NETWORK));
	}
	
	public void checkFiles(File dir, boolean valid) throws FileNotFoundException {
		File [] files = dir.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.endsWith(".cx");
		    }
		});
		for (File file : files) {
			checkFile(file, valid);
		}
	}
}