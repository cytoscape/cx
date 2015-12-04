package org.cytoscape.io.internal.cxio;

import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cxio.filters.AspectKeyFilter;

/**
 * This class is primarily for storing of AspectKeyFilters to be applied to
 * certain aspects in {@link CxExporter}.
 *
 *
 * @see CxExporter
 *
 */
public final class FilterSet {

    final private SortedSet<AspectKeyFilter> _filters;

    /**
     * Constructor, creates an empty FilterSet.
     *
     */
    public FilterSet() {
        _filters = new TreeSet<AspectKeyFilter>();
    }

    /**
     * Constructor, creates an FilterSet containing AspectKeyFilters.
     *
     * @param filters
     *            the AspectKeyFilters to initialize this FilterSet with
     */
    public FilterSet(final Collection<AspectKeyFilter> filters) {
        _filters = new TreeSet<AspectKeyFilter>();
        _filters.addAll(filters);
    }

    /**
     * To add a single AspectKeyFilter.
     *
     * @param filter
     *            the AspectKeyFilter to add
     */
    public final void addFilter(final AspectKeyFilter filter) {
        _filters.add(filter);
    }

    final SortedSet<AspectKeyFilter> getFilters() {
        return _filters;
    }

    final SortedMap<String, AspectKeyFilter> getFiltersAsMap() {
        if (_filters == null) {
            return null;
        }
        final SortedMap<String, AspectKeyFilter> filters_map = new TreeMap<String, AspectKeyFilter>();
        for (final AspectKeyFilter filter : _filters) {
            final String aspect = filter.getAspectName();
            if (filters_map.containsKey(aspect)) {
                throw new IllegalArgumentException("cannot have multiple filters for same aspect ['" + aspect + "']");
            }
            filters_map.put(aspect, filter);
        }
        return filters_map;
    }

}
