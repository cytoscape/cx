package org.cytoscape.io.internal.cx_reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CytoscapeCxFileFilter extends BasicCyFileFilter {
	
	private static final String[] extensions = new String[] { "cx" };
    private static final String[] types = new String[] { "application/json" };
    private static final String description = "CX JSON";
    private static final DataCategory category = DataCategory.NETWORK;
    

    private static final Logger locallogger            = LoggerFactory.getLogger(CytoscapeCxFileFilter.class);
    public static final Pattern CX_HEADER_PATTERN = Pattern
    													.compile("\\s*\\{\\s*\"\\s*metaData\"\\s*:");
    public static final Pattern CX_HEADER_PATTERN_OLD = Pattern
                                                          .compile("\\s*\\[\\s*\\{\\s*\"\\s*numberVerification\"\\s*:");

    public CytoscapeCxFileFilter(final String[] extensions,
            final String[] contentTypes,
            final String description,
            final DataCategory category,
            final StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}
    
    public CytoscapeCxFileFilter(final StreamUtil streamUtil) {
        this(extensions, types, description, category, streamUtil);
    }

    @Override
    public boolean accepts(final InputStream stream,
                           final DataCategory dataCategory) {
        if (!dataCategory.equals(DataCategory.NETWORK)) {
            return false;
        }
        try {
            return (getCXstartElement(stream) != null);
        }
        catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("Error while checking header",
                         e);
            return false;
        }
    }

    @Override
    public boolean accepts(final URI uri,
                           final DataCategory dataCategory) {
        try (InputStream is = uri.toURL().openStream()) {
			return accepts(is, dataCategory);
        }
        catch (final IOException e) {
            locallogger.error("Error while opening stream: " + uri,
                         e);
            return false;
        }
    }

    /**
     * @param stream
     * @return null if not an CX file
     */
    protected String getCXstartElement(final InputStream stream) {
        final String header = getHeaderCharacters(stream, 400);
        final Matcher matcher = CX_HEADER_PATTERN.matcher(header);
        String root = null;

        if (matcher.find()) {
            root = matcher.group(0);
        }
        if (root == null) {
        	final Matcher matcher_old = CX_HEADER_PATTERN_OLD.matcher(header);
            
            if (matcher_old.find()) {
                root = matcher_old.group(0);
            }
        }

        return root;
    }
    
    protected String getHeaderCharacters(InputStream stream, int numCharacters) {

		String header;
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));

		try {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < numCharacters; i++) {
				char[] c = new char[1];
				br.read(c);
				builder.append(c);
			}
			header = builder.toString();
		} catch (IOException ioe) {
			header = "";
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}

			br = null;
		}

		return header;
	}
    
}
