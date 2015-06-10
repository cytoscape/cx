package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CytoscapeCxFileFilter extends BasicCyFileFilter {

    private static final Logger logger = LoggerFactory.getLogger(CytoscapeCxFileFilter.class);

    public CytoscapeCxFileFilter(final Set<String> extensions, final Set<String> contentTypes,
            final String description, final DataCategory category, final StreamUtil streamUtil) {
        super(extensions, contentTypes, description, category, streamUtil);
    }

    public CytoscapeCxFileFilter(final String[] extensions, final String[] contentTypes,
            final String description, final DataCategory category, final StreamUtil streamUtil) {
        super(extensions, contentTypes, description, category, streamUtil);
    }

    @Override
    public boolean accepts(final InputStream stream, final DataCategory category) {
        return super.accepts(stream, category);
    }

    @Override
    public boolean accepts(final URI uri, final DataCategory category) {
        try {
            return accepts(uri.toURL().openStream(), category);
        }
        catch (final IOException e) {
            logger.error("Error while opening stream: " + uri, e);
            return false;
        }
    }
}
