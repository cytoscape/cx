package org.cytoscape.io.internal.cxio;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.aspects.readers.GeneralAspectFragmentReader;
import org.ndexbio.cxio.core.CxElementReader2;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.metadata.MetaDataCollection;
import org.ndexbio.cxio.metadata.MetaDataElement;
import org.cytoscape.io.cx.Aspect;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NdexNetworkStatus;
import org.ndexbio.model.cx.NiceCXNetwork;
import org.ndexbio.model.cx.Provenance;

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
        
        AspectSet aspects = new AspectSet();
        aspects.addAspect(Aspect.NODES);
        aspects.addAspect(Aspect.EDGES);
        aspects.addAspect(Aspect.NETWORK_ATTRIBUTES);
        aspects.addAspect(Aspect.NODE_ATTRIBUTES);
        aspects.addAspect(Aspect.EDGE_ATTRIBUTES);
        aspects.addAspect(Aspect.VISUAL_PROPERTIES);
        aspects.addAspect(Aspect.CARTESIAN_LAYOUT);
        aspects.addAspect(Aspect.NETWORK_RELATIONS);
        aspects.addAspect(Aspect.SUBNETWORKS);
        aspects.addAspect(Aspect.GROUPS);
        aspects.addAspect(Aspect.HIDDEN_ATTRIBUTES);
        aspects.addAspect(Aspect.TABLE_COLUMN_LABELS);
        aspects.addAspect(Aspect.VIEWS);
        
        all_readers = new HashSet<>();
        for (final AspectFragmentReader reader : aspects.getAspectFragmentReaders()) {
            all_readers.add(reader);
        }
        
		all_readers.add(new GeneralAspectFragmentReader<> (Provenance.ASPECT_NAME,Provenance.class));
		all_readers.add(new GeneralAspectFragmentReader<> (NamespacesElement.ASPECT_NAME,NamespacesElement.class));

        
    }
    
    public NiceCXNetwork getCXNetworkFromStream( final InputStream in) throws IOException {
        CxElementReader2 r = new CxElementReader2(in, all_readers, true);
        
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
     					
     				case EdgeAttributesElement.ASPECT_NAME:
     					niceCX.addEdgeAttribute((EdgeAttributesElement)elmt);
     					break;
     				case CartesianLayoutElement.ASPECT_NAME:
     					CartesianLayoutElement e = (CartesianLayoutElement)elmt;
     					niceCX.addNodeAssociatedAspectElement(Long.valueOf(e.getNode()), e);
     					break;
     				case Provenance.ASPECT_NAME:
     					Provenance prov = (Provenance) elmt;
     					niceCX.setProvenance(prov);
     					break;
     				case NamespacesElement.ASPECT_NAME:
     					NamespacesElement ns = (NamespacesElement) elmt;
     					niceCX.setNamespaces(ns);
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
  	    
        return niceCX;
    }
    
}
