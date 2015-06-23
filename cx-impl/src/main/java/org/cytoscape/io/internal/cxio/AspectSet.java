package org.cytoscape.io.internal.cxio;

import java.util.Collection;
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

public final class AspectSet {

    final SortedSet<Aspect> _aspects;

    public AspectSet() {
        _aspects = new TreeSet<Aspect>();
    }

    public AspectSet(final Collection<Aspect> aspects) {
        _aspects = new TreeSet<Aspect>();
        _aspects.addAll(aspects);
    }

    public final void addAspect(final Aspect aspect) {
        _aspects.add(aspect);
    }

    public final SortedSet<Aspect> getAspects() {
        return _aspects;
    }

    public final boolean contains(final Aspect aspect) {
        return _aspects.contains(aspect);
    }
    
    /**
     * For each Aspect in the set, this returns the appropriate AspectFragmentWriter 
     * (in a SortedSet). 
     * 
     * @return a SortedSet of AspectFragmentWriters
     */
    public final SortedSet<AspectFragmentWriter> getAspectAspectFragmentWriters() {
        final SortedSet<AspectFragmentWriter> writers = new TreeSet<AspectFragmentWriter>();
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

    
    public final SortedSet<AspectFragmentReader> getAspectAspectFragmentReaders() {
        final SortedSet<AspectFragmentReader> readers = new TreeSet<AspectFragmentReader>();
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
