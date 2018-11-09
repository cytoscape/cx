package org.cytoscape.io.internal.cxio;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ndexbio.cxio.aspects.readers.CartesianLayoutFragmentReader;
import org.ndexbio.cxio.aspects.readers.CyGroupsFragmentReader;
import org.ndexbio.cxio.aspects.readers.CyTableColumnFragmentReader;
import org.ndexbio.cxio.aspects.readers.CyVisualPropertiesFragmentReader;
import org.ndexbio.cxio.aspects.readers.EdgeAttributesFragmentReader;
import org.ndexbio.cxio.aspects.readers.EdgesFragmentReader;
import org.ndexbio.cxio.aspects.readers.HiddenAttributesFragmentReader;
import org.ndexbio.cxio.aspects.readers.NetworkAttributesFragmentReader;
import org.ndexbio.cxio.aspects.readers.NetworkRelationsFragmentReader;
import org.ndexbio.cxio.aspects.readers.NodeAttributesFragmentReader;
import org.ndexbio.cxio.aspects.readers.NodesFragmentReader;
import org.ndexbio.cxio.aspects.readers.SubNetworkFragmentReader;
import org.ndexbio.cxio.aspects.writers.CartesianLayoutFragmentWriter;
import org.ndexbio.cxio.aspects.writers.CyGroupsFragmentWriter;
import org.ndexbio.cxio.aspects.writers.CyTableColumnFragmentWriter;
import org.ndexbio.cxio.aspects.writers.EdgeAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.EdgesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.HiddenAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NetworkAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NetworkRelationsFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NodeAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NodesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.SubNetworkFragmentWriter;
import org.ndexbio.cxio.aspects.writers.VisualPropertiesFragmentWriter;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.core.interfaces.AspectFragmentWriter;
import org.cytoscape.io.cx.Aspect;

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
    
    public final static AspectSet getCytoscapeAspectSet() {
    	AspectSet aspects = new AspectSet();
    	aspects.addAspect(Aspect.NODES);
		aspects.addAspect(Aspect.EDGES);
		aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
		aspects.addAspect(Aspect.NODE_ATTRIBUTES);
		aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
		aspects.addAspect(Aspect.HIDDEN_ATTRIBUTES);
		aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
		aspects.addAspect(Aspect.VISUAL_PROPERTIES);
		aspects.addAspect(Aspect.SUBNETWORKS);
		aspects.addAspect(Aspect.NETWORK_RELATIONS);
		aspects.addAspect(Aspect.GROUPS);
		aspects.addAspect(Aspect.TABLE_COLUMN_LABELS);
    	return aspects;
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
        if (_aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            writers.add(NetworkAttributesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            writers.add(NodeAttributesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.HIDDEN_ATTRIBUTES)) {
            writers.add(HiddenAttributesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.NODES)) {
            writers.add(NodesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.VISUAL_PROPERTIES)) {
            writers.add(VisualPropertiesFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.SUBNETWORKS)) {
            writers.add(SubNetworkFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.NETWORK_RELATIONS)) {
            writers.add(NetworkRelationsFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.GROUPS)) {
            writers.add(CyGroupsFragmentWriter.createInstance());
        }
        if (_aspects.contains(Aspect.TABLE_COLUMN_LABELS)) {
            writers.add(CyTableColumnFragmentWriter.createInstance());
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
        if (_aspects.contains(Aspect.NETWORK_ATTRIBUTES)) {
            readers.add(NetworkAttributesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.NODE_ATTRIBUTES)) {
            readers.add(NodeAttributesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.HIDDEN_ATTRIBUTES)) {
            readers.add(HiddenAttributesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.NODES)) {
            readers.add(NodesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.VISUAL_PROPERTIES)) {
            readers.add(CyVisualPropertiesFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.SUBNETWORKS)) {
            readers.add(SubNetworkFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.GROUPS)) {
            readers.add(CyGroupsFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.NETWORK_RELATIONS)) {
            readers.add(NetworkRelationsFragmentReader.createInstance());
        }
        if (_aspects.contains(Aspect.TABLE_COLUMN_LABELS)) {
            readers.add(CyTableColumnFragmentReader.createInstance());
        }
        return readers;
    }
}
