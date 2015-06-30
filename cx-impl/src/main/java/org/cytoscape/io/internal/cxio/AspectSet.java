package org.cytoscape.io.internal.cxio;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cxio.aspects.readers.CartesianLayoutFragmentReader;
import org.cxio.aspects.readers.EdgeAttributesFragmentReader;
import org.cxio.aspects.readers.EdgesFragmentReader;
import org.cxio.aspects.readers.NodeAttributesFragmentReader;
import org.cxio.aspects.readers.NodesFragmentReader;
import org.cxio.aspects.writers.CartesianLayoutFragmentWriter;
import org.cxio.aspects.writers.EdgeAttributesFragmentWriter;
import org.cxio.aspects.writers.EdgesFragmentWriter;
import org.cxio.aspects.writers.NodeAttributesFragmentWriter;
import org.cxio.aspects.writers.NodesFragmentWriter;
import org.cxio.core.interfaces.AspectFragmentReader;
import org.cxio.core.interfaces.AspectFragmentWriter;

/**
 * This class is primarily for storing of {@link Aspect Aspect identifiers} to
 * be imported or exported in {@link CxImporter} and {@link CxExporter}.
 *
 *
 * @see Aspect
 * @see CxImporter
 * @see CxExporter
 *
 */
public final class AspectSet {

    final private SortedSet<Aspect> _aspects;

    /**
     * Constructor, creates an empty AspectSet.
     *
     */
    public AspectSet() {
        _aspects = new TreeSet<Aspect>();
    }

    /**
     * Constructor, creates an AspectSet containing Aspects identifiers.
     *
     * @param aspects
     *            the Aspects to initialize this AspectSet with
     */
    public AspectSet(final Collection<Aspect> aspects) {
        _aspects = new TreeSet<Aspect>();
        _aspects.addAll(aspects);
    }

    /**
     * To add a single Aspect.
     *
     * @param aspect
     *            the Aspect to add
     */
    public final void addAspect(final Aspect aspect) {
        _aspects.add(aspect);
    }

    final SortedSet<Aspect> getAspects() {
        return _aspects;
    }

    final boolean contains(final Aspect aspect) {
        return _aspects.contains(aspect);
    }

    final Set<AspectFragmentWriter> getAspectFragmentWriters() {
        final Set<AspectFragmentWriter> writers = new HashSet<AspectFragmentWriter>();
        if (_aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
            writers.add(CartesianLayoutFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            writers.add(EdgeAttributesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.EDGES)) {
            writers.add(EdgesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writers.add(NodeAttributesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.NODES)) {
            writers.add(NodesFragmentWriter.createInstance());
        }
        return writers;
    }

    final Set<AspectFragmentReader> getAspectFragmentReaders() {
        final Set<AspectFragmentReader> readers = new HashSet<AspectFragmentReader>();
        if (_aspects.contains(Aspect.CARTESIAN_LAYOUT)) {
            readers.add(CartesianLayoutFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.EDGE_ATTRIBUTES)) {
            readers.add(EdgeAttributesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.EDGES)) {
            readers.add(EdgesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            readers.add(NodeAttributesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.NODES)) {
            readers.add(NodesFragmentReader.createInstance());
        }
        return readers;
    }
}
