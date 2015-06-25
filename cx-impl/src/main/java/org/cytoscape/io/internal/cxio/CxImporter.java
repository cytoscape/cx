package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cxio.core.CxReader;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentReader;

public final class CxImporter {

    private final SortedSet<AspectFragmentReader> _additional_readers;

    private CxImporter() {
        _additional_readers = new TreeSet<AspectFragmentReader>();
    }

    public final static CxImporter createInstance() {
        return new CxImporter();
    }

    public final void addAdditionalReaders(final Collection<AspectFragmentReader> additional_readers) {
        _additional_readers.addAll(additional_readers);
    }

    public final void addAdditionalReader(final AspectFragmentReader additional_reader) {
        _additional_readers.add(additional_reader);
    }

    public final CxReader getCxReader(final AspectSet aspects, final InputStream in)
            throws IOException {
        final Set<AspectFragmentReader> all_readers = getAllAspectFragmentReaders(aspects
                .getAspectFragmentReaders());
        final CxReader r = CxReader.createInstance(in, all_readers);
        return r;
    }

    public final SortedMap<String, List<AspectElement>> readAsMap(final AspectSet aspects,
                                                                  final InputStream in)
                                                                          throws IOException {
        final CxReader r = getCxReader(aspects, in);
        return CxReader.parseAsMap(r);

    }

    private Set<AspectFragmentReader> getAllAspectFragmentReaders(final Set<AspectFragmentReader> readers) {

        final Set<AspectFragmentReader> all = new HashSet<AspectFragmentReader>();
        for (final AspectFragmentReader reader : readers) {
            all.add(reader);
        }
        if (_additional_readers != null) {
            for (final AspectFragmentReader reader : _additional_readers) {
                all.add(reader);
            }
        }
        return all;
    }

}
