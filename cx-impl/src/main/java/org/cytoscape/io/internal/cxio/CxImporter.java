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

/**
 * This class is for de-serializing CX formatted networks, views, and attribute tables.
 *
 */
public final class CxImporter {

    private final SortedSet<AspectFragmentReader> _additional_readers;

    private CxImporter() {
        _additional_readers = new TreeSet<AspectFragmentReader>();
    }

    /**
     * This creates a new CxImporter
     * 
     * @return a new CxImporter
     */
    public final static CxImporter createInstance() {
        return new CxImporter();
    }

    /**
     * This method allows to use custom readers (for other aspects than the standard nodes, edges,
     * node attributes, edge attributes and cartesian layout).
     * 
     * 
     * @param additional_readers a collection of additional custom readers to add
     */
    public final void addAdditionalReaders(final Collection<AspectFragmentReader> additional_readers) {
        _additional_readers.addAll(additional_readers);
    }

    /**
     * This method allows to use custom readers (for other aspects than the standard nodes, edges,
     * node attributes, edge attributes and cartesian layout).
     * 
     * 
     * @param additional_reader an additional custom readers to add
     */
    public final void addAdditionalReader(final AspectFragmentReader additional_reader) {
        _additional_readers.add(additional_reader);
    }

    /**
     * This returns a CxReader.
     * A CxReader in turn is used to obtain aspect fragments from a stream.
     * <br>
     * By way of example:
     * <pre>
     * {@code}
     * CxImporter cx_importer = CxImporter.createInstance(); 
     * CxReader r = cx_importer.getCxReader(aspects, in);
     *  
     * while (r.hasNext()) { 
     *     List<AspectElement> elements = r.getNext();
     *     if (!elements.isEmpty()) {
     *     String aspect_name = elements.get(0).getAspectName();
     *     // Do something with "elements":
     *     for (AspectElement element : elements) {
     *         System.out.println(element.toString());
     *     }
     * }
     * </pre>
     * 
     * @see <a href="https://github.com/cmzmasek/cxio/wiki/Java-Library-for-CX-Serialization-and-De-serialization">cxio</a>
     * 
     * @param aspects the set of aspects to de-serialize
     * @param in the CX formatted input stream
     * @return
     * @throws IOException
     */
    public final CxReader getCxReader(final AspectSet aspects, final InputStream in)
            throws IOException {
        final Set<AspectFragmentReader> all_readers = getAllAspectFragmentReaders(aspects
                .getAspectFragmentReaders());
        final CxReader r = CxReader.createInstance(in, all_readers);
        return r;
    }

    /**
     * 
     * 
     * 
     * @param aspects
     * @param in
     * @return
     * @throws IOException
     */
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
