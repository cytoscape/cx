package org.cytoscape.io.internal.cx_reader;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CytoscapeCx2FileFilter extends CytoscapeCxFileFilter {
	
	private static final String[] cx2Extensions = new String[] { "cx2" };
    private static final String cx2Description = "CX2 JSON";
        
    public static final Pattern CX2_HEADER_PATTERN = Pattern
                                                          .compile("\\s*\\[\\s*\\{\\s*\"\\s*CXVersion\"\\s*:\\s*\"2.0\"");

    private CytoscapeCx2FileFilter(final String[] extensions,
            final String[] contentTypes,
            final String description,
            final StreamUtil streamUtil) {
		super(extensions, contentTypes, description, streamUtil);
	}
    
    public CytoscapeCx2FileFilter(final StreamUtil streamUtil) {
        this(cx2Extensions, types, cx2Description,streamUtil);
    }

    @Override
    public boolean accepts(final InputStream stream,
                           final DataCategory dataCategory) {
        if (!dataCategory.equals(DataCategory.NETWORK)) {
            return false;
        }
        try {
            return (getCX2startElement(stream) != null);
        }
        catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("Error while checking header",
                         e);
            return false;
        }
    }


    /**
     * @param stream
     * @return null if not an CX file
     */
    protected static String getCX2startElement(final InputStream stream) {
        final String header = getHeaderCharacters(stream, 400);
        final Matcher matcher = CX2_HEADER_PATTERN.matcher(header);
        String root = null;

        if (matcher.find()) {
            root = matcher.group(0);
        }
        return root;
    }
    
  
}
