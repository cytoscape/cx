package org.cytoscape.io.internal.cxio;

import java.util.HashSet;
import java.util.Set;

public final class AspectFragmentWriterManager {

    private final Set<AspectFragmentWriter> writers = new HashSet<AspectFragmentWriter>();

    public static AspectFragmentWriterManager createInstance() {
        return new AspectFragmentWriterManager();
    }

    public final void addWriter(final AspectFragmentWriter writer) {
        writers.add(writer);
    }

    public Set<AspectFragmentWriter> getWriters() {
        return writers;
    }

    public final static Set<AspectFragmentWriter> createDefaultWriters() {
        final AspectFragmentWriter nodes_writer = NodesFragmentWriter.createInstance();
        final AspectFragmentWriter edges_writer = EdgesFragmentWriter.createInstance();
        final AspectFragmentWriter ege_aspects_writer = EdgeAttributesFragmentWriter.createInstance();
        final AspectFragmentWriter node_aspects_writer = NodeAttributesFragmentWriter.createInstance();
        final AspectFragmentWriter cartesian_layout_writer = CartesianLayoutFragmentWriter.createInstance();
        final Set<AspectFragmentWriter> writers = new HashSet<AspectFragmentWriter>();
        writers.add(nodes_writer);
        writers.add(edges_writer);
        writers.add(ege_aspects_writer);
        writers.add(node_aspects_writer);
        writers.add(cartesian_layout_writer);
        return writers;
    }

}
