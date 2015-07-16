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
 * This class is for de-serializing CX formatted networks, views, and attribute
 * tables.
 *
 * In particular, it provides the following methods for writing CX: <br>
 * <ul>
 * <li>
 * {@link #getCxReader(AspectSet, InputStream)}</li>
 * <li>
 * {@link #readAsMap(AspectSet, InputStream)}</li>
 * </ul>
 * <br>
 * <br>
 * These methods use: <br>
 * <ul>
 * <li>
 * {@link AspectSet} to control which aspects to de-serialize</li>
 * </ul>
 * <br>
 * <br>
 * <br>
 * Example using {@link #getCxReader(AspectSet, InputStream)}:
 *
 * <pre>
 * {@code}
 * CxImporter cx_importer = CxImporter.createInstance();
 * AspectSet aspects = new AspectSet();
 * aspects.addAspect(Aspect.NODES);
 * aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
 * aspects.addAspect(Aspect.EDGES);
 * 
 * CxReader r = cx_importer.getCxReader(aspects, in);
 * 
 * while (r.hasNext()) {
 *     List&lt;AspectElement&gt; elements = r.getNext();
 *     if (!elements.isEmpty()) {
 *         String aspect_name = elements.get(0).getAspectName();
 *         // Do something with "elements":
 *         for (AspectElement element : elements) {
 *             System.out.println(element.toString());
 *         }
 *     }
 * }
 * </pre>
 *
 * <br>
 * <br>
 * Example using {@link #readAsMap(AspectSet, InputStream)}:
 *
 * <pre>
 * {@code}
 * CxImporter cx_importer = CxImporter.createInstance();
 * AspectSet aspects = new AspectSet();
 * aspects.addAspect(Aspect.NODES);
 * aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
 * aspects.addAspect(Aspect.EDGES);
 * 
 * SortedMap&lt;String, List&lt;AspectElement&gt;&gt; res = cx_importer.readAsMap(aspects, in);
 * </pre>
 *
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
     * To use custom readers for other aspects than the standard nodes, edges,
     * node attributes, edge attributes and cartesian layout.
     *
     *
     * @param additional_readers
     *            a collection of additional custom readers to add
     */
    public final void addAdditionalReaders(final Collection<AspectFragmentReader> additional_readers) {
        _additional_readers.addAll(additional_readers);
    }

    /**
     * To use a custom reader for another aspect than the standard nodes, edges,
     * node attributes, edge attributes and cartesian layout.
     *
     *
     * @param additional_reader
     *            an additional custom readers to add
     */
    public final void addAdditionalReader(final AspectFragmentReader additional_reader) {
        _additional_readers.add(additional_reader);
    }

    /**
     * This is the primary method to parse a CX formatted input stream by
     * returning a CxReader for a given InputStream and set of Aspects. The
     * CxReader in turn is then used to obtain aspect fragments from the stream.
     * Which aspects are de-serialized and which ones are ignored is controlled
     * by the AspectSet argument. <br>
     * By way of example:
     *
     * <pre>
     * {@code}
     * CxImporter cx_importer = CxImporter.createInstance();
     * AspectSet aspects = new AspectSet();
     * aspects.addAspect(Aspect.NODES);
     * 
     * CxReader r = cx_importer.getCxReader(aspects, in);
     * 
     * while (r.hasNext()) {
     *     List&lt;AspectElement&gt; elements = r.getNext();
     *     if (!elements.isEmpty()) {
     *         String aspect_name = elements.get(0).getAspectName();
     *         // Do something with "elements":
     *         for (AspectElement element : elements) {
     *             System.out.println(element.toString());
     *         }
     *     }
     * }
     *
     * </pre>
     *
     * @see <a
     *      href="https://github.com/cmzmasek/cxio/wiki/Java-Library-for-CX-Serialization-and-De-serialization">cxio</a>
     *
     * @param aspects
     *            the set of aspects to de-serialize
     * @param in
     *            a CX formatted input stream
     * @return
     * @throws IOException
     *
     * @see AspectSet
     * @see Aspect
     */
    public final CxReader getCxReader(final AspectSet aspects, final InputStream in) throws IOException {
        final Set<AspectFragmentReader> all_readers = getAllAspectFragmentReaders(aspects.getAspectFragmentReaders());
        final CxReader r = CxReader.createInstance(in, all_readers);
        return r;
    }

    /**
     * This is a convenience method to parse a CX formatted input stream. It
     * returns the aspect present in the input stream as a mapping of aspect
     * names to lists of aspect elements. This convenience method is not
     * recommended for very large data sets or if speed is of the essence.
     *
     *
     * @param aspects
     *            the set of aspects to de-serialize
     * @param in
     *            a CX formatted input stream
     * @return map of aspect names to lists of aspect elements
     * @throws IOException
     *
     * @see AspectSet
     * @see Aspect
     */
    public final SortedMap<String, List<AspectElement>> readAsMap(final AspectSet aspects, final InputStream in)
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
