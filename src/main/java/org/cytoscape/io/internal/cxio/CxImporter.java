package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.internal.AspectSet;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.core.CxElementReader2;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.metadata.MetaDataElement;
import org.ndexbio.model.cx.NdexNetworkStatus;
import org.ndexbio.model.cx.NiceCXNetwork;

/**
 * This class is for de-serializing CX formatted networks, views, and attribute
 * tables.
 *
 * In particular, it provides the following methods for writing CX: <br>
 * <ul>
 * <li>
 * {@link #obtainCxReader(AspectSet, InputStream)}</li>
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
 * Example using {@link #obtainCxReader(AspectSet, InputStream)}:
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

    private final Set<AspectFragmentReader> all_readers ;

    public CxImporter() {
        
        Collection<String> aspects = AspectSet.getAspectNames();
        
        all_readers = new HashSet<>();
        for (final AspectFragmentReader reader : AspectSet.getAspectFragmentReaders(aspects)) {
            all_readers.add(reader);
        }
        
    }

    /*
     * This creates a new CxImporter
     *
     * @return a new CxImporter
     */
   /* public final static CxImporter createInstance() {
        return new CxImporter();
    } */ 

    /**
     * To use custom readers for other aspects than the standard nodes, edges,
     * node attributes, edge attributes and cartesian layout.
     *
     *
     * @param additional_readers
     *            a collection of additional custom readers to add
     */
    public final void addAdditionalReaders(final Collection<AspectFragmentReader> additional_readers) {
        all_readers.addAll(additional_readers);
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
        all_readers.add(additional_reader);
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
  
    public NiceCXNetwork getCXNetworkFromStream( final InputStream in) throws IOException {
    	CxElementReader2 r = new CxElementReader2(in, all_readers, true);
        long t0 = System.currentTimeMillis();
        MetaDataCollection metadata = r.getPreMetaData();
		
        long nodeIdCounter = 0;
        long edgeIdCounter = 0;
        
        NiceCXNetwork niceCX = new NiceCXNetwork ();
        
     	for ( AspectElement elmt : r ) {
     		switch ( elmt.getAspectName() ) {
     			case NodesElement.ASPECT_NAME :       //Node
     				    NodesElement n = (NodesElement) elmt;
     					niceCX.addNode(n);
                        if (n.getId() > nodeIdCounter )
                        	nodeIdCounter = n.getId();
     					break;
     				case NdexNetworkStatus.ASPECT_NAME:   //ndexStatus we ignore this in CX
     					break; 
     				case EdgesElement.ASPECT_NAME:       // Edge
     					EdgesElement ee = (EdgesElement) elmt;
     					niceCX.addEdge(ee);
     					if( ee.getId() > edgeIdCounter)
     						edgeIdCounter = ee.getId();
     					break;
     				case NodeAttributesElement.ASPECT_NAME:  // node attributes
     					niceCX.addNodeAttribute((NodeAttributesElement) elmt );
     					break;
     				case NetworkAttributesElement.ASPECT_NAME: //network attributes
     					niceCX.addNetworkAttribute(( NetworkAttributesElement) elmt);
     					break;
     				case EdgeAttributesElement.ASPECT_NAME: //edge attributes
     					niceCX.addEdgeAttribute((EdgeAttributesElement)elmt);
     					break;
     				case CartesianLayoutElement.ASPECT_NAME: // cartesian layout
     					CartesianLayoutElement e = (CartesianLayoutElement)elmt;
     					niceCX.addNodeAssociatedAspectElement(e.getNode(), e);
     					break;
     				default:    // opaque aspect
     					niceCX.addOpapqueAspect(elmt);
     			}
     	} 
     	
     	MetaDataCollection postmetadata = r.getPostMetaData();
     	
  	    if ( postmetadata !=null) {
		  if( metadata == null) {
			  metadata = postmetadata;
		  } else {
			  for (MetaDataElement e : postmetadata) {
				  Long cnt = e.getIdCounter();
				  if ( cnt !=null) {
					 metadata.setIdCounter(e.getName(),cnt);
				  }
				  cnt = e.getElementCount() ;
				  if ( cnt !=null) {
					  metadata.setElementCount(e.getName(),cnt);
				  }
			  }
		  }
	    }
  	    
  	    Long cxNodeIdCounter = metadata.getIdCounter(NodesElement.ASPECT_NAME);
  	    if (cxNodeIdCounter == null || cxNodeIdCounter.longValue() < nodeIdCounter)
  	    	metadata.setIdCounter(NodesElement.ASPECT_NAME, Long.valueOf(nodeIdCounter));
  	    
  	    Long cxEdgeIdCounter = metadata.getIdCounter(EdgesElement.ASPECT_NAME);
  	    if (cxEdgeIdCounter == null || cxEdgeIdCounter.longValue() < edgeIdCounter)
  	        metadata.setIdCounter(EdgesElement.ASPECT_NAME, Long.valueOf(edgeIdCounter));
  	
  	    niceCX.setMetadata(metadata);
  	    if (Settings.INSTANCE.isTiming()) {
			TimingUtil.reportTimeDifference(t0, "niceCX", niceCX.getMetadata().size());
		}
        return niceCX;
    }
    
}
