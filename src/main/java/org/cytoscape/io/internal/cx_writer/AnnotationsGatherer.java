package org.cytoscape.io.internal.cx_writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ndexbio.cxio.aspects.datamodels.CyAnnotationsElement;
import org.ndexbio.cxio.core.interfaces.AspectElement;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/**
 * This class is used to gather visual properties from network views.
 *
 * @author cmzmasek
 *
 */
public final class AnnotationsGatherer {


    public static final List<AspectElement> gatherAnnotationsAsAspectElements(final CyNetworkView view, AnnotationManager annotationManager) {

        final List<AspectElement> elements = new ArrayList<>();
      
        for (Annotation annotation : annotationManager.getAnnotations(view))
        {
        	final CyAnnotationsElement e = new CyAnnotationsElement(annotation.getUUID().toString(),
				annotation.getNetworkView().getSUID());
        	for (Map.Entry<String, String> parameters :annotation.getArgMap().entrySet()) {
        		if (!parameters.getKey().equalsIgnoreCase(CyAnnotationsElement.UUID) && 
        			!parameters.getKey().equalsIgnoreCase(CyAnnotationsElement.VIEW)) {
        			e.putProperty(parameters.getKey(), parameters.getValue());
        		}
        	}
        	elements.add(e);
        }
        return elements;

    }

   

}
