package org.cytoscape.io.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ndexbio.cx2.aspect.element.core.CxAttributeDeclaration;
import org.ndexbio.cx2.aspect.element.core.CxEdge;
import org.ndexbio.cx2.aspect.element.core.CxEdgeBypass;
import org.ndexbio.cx2.aspect.element.core.CxNetworkAttribute;
import org.ndexbio.cx2.aspect.element.core.CxNode;
import org.ndexbio.cx2.aspect.element.core.CxNodeBypass;
import org.ndexbio.cx2.aspect.element.core.CxVisualProperty;
import org.ndexbio.cx2.aspect.element.cytoscape.AbstractTableVisualProperty;
import org.ndexbio.cx2.aspect.element.cytoscape.CxTableVisualProperty;
import org.ndexbio.cx2.aspect.element.cytoscape.VisualEditorProperties;
import org.ndexbio.cxio.aspects.datamodels.CartesianLayoutElement;
import org.ndexbio.cxio.aspects.datamodels.CyGroupsElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableColumnElement;
import org.ndexbio.cxio.aspects.datamodels.CyTableVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.CyVisualPropertiesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.EdgesElement;
import org.ndexbio.cxio.aspects.datamodels.HiddenAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NetworkRelationsElement;
import org.ndexbio.cxio.aspects.datamodels.NodeAttributesElement;
import org.ndexbio.cxio.aspects.datamodels.NodesElement;
import org.ndexbio.cxio.aspects.datamodels.SubNetworkElement;
import org.ndexbio.cxio.aspects.readers.CartesianLayoutFragmentReader;
import org.ndexbio.cxio.aspects.readers.CyGroupsFragmentReader;
import org.ndexbio.cxio.aspects.readers.CyTableColumnFragmentReader;
import org.ndexbio.cxio.aspects.readers.CyVisualPropertiesFragmentReader;
import org.ndexbio.cxio.aspects.readers.EdgeAttributesFragmentReader;
import org.ndexbio.cxio.aspects.readers.EdgesFragmentReader;
import org.ndexbio.cxio.aspects.readers.GeneralAspectFragmentReader;
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
import org.ndexbio.cxio.aspects.writers.GeneralAspectFragmentWriter;
import org.ndexbio.cxio.aspects.writers.HiddenAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NetworkAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NetworkRelationsFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NodeAttributesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.NodesFragmentWriter;
import org.ndexbio.cxio.aspects.writers.SubNetworkFragmentWriter;
import org.ndexbio.cxio.aspects.writers.VisualPropertiesFragmentWriter;
import org.ndexbio.cxio.core.interfaces.AspectFragmentReader;
import org.ndexbio.cxio.core.interfaces.AspectFragmentWriter;

public class AspectSet {

	private static final List<String> cx2Aspects =
			Arrays.asList(CxAttributeDeclaration.ASPECT_NAME,
				CxNetworkAttribute.ASPECT_NAME,
				CxNode.ASPECT_NAME,
				CxEdge.ASPECT_NAME,
				CxVisualProperty.ASPECT_NAME,
				CxNodeBypass.ASPECT_NAME,
				CxEdgeBypass.ASPECT_NAME,
				VisualEditorProperties.ASPECT_NAME,
				AbstractTableVisualProperty.ASPECT_NAME   // Cytoscape table styles
				);
	
	private static final List<String> cxAspects = Arrays.asList(
			NodesElement.ASPECT_NAME,
			EdgesElement.ASPECT_NAME,
			CartesianLayoutElement.ASPECT_NAME,
			EdgeAttributesElement.ASPECT_NAME,
			NodeAttributesElement.ASPECT_NAME,
			NetworkAttributesElement.ASPECT_NAME,
			SubNetworkElement.ASPECT_NAME,
			CyVisualPropertiesElement.ASPECT_NAME,
			NetworkRelationsElement.ASPECT_NAME,
			CyGroupsElement.ASPECT_NAME,
			HiddenAttributesElement.ASPECT_NAME,
			CyTableColumnElement.ASPECT_NAME,
			AbstractTableVisualProperty.ASPECT_NAME
		);
	
	private AspectSet() {
		// Hidden
	}

	public final static List<String> getAspectNames() {return cxAspects;}
	
	public final static List<String> getCx2AspectNames() { return cx2Aspects;}

	public final static Set<AspectFragmentWriter> getAspectFragmentWriters(Collection<String> _aspects) {
		final Set<AspectFragmentWriter> writers = new HashSet<>();
		for (String aspect : _aspects) {
			switch (aspect) {
			case CartesianLayoutElement.ASPECT_NAME:
				writers.add(CartesianLayoutFragmentWriter.createInstance());
				break;
			case EdgeAttributesElement.ASPECT_NAME:
				writers.add(EdgeAttributesFragmentWriter.createInstance());
				break;
			case EdgesElement.ASPECT_NAME:
				writers.add(EdgesFragmentWriter.createInstance());
				break;
			case NetworkAttributesElement.ASPECT_NAME:
				writers.add(NetworkAttributesFragmentWriter.createInstance());
				break;
			case NodeAttributesElement.ASPECT_NAME:
				writers.add(NodeAttributesFragmentWriter.createInstance());
				break;
			case HiddenAttributesElement.ASPECT_NAME:
				writers.add(HiddenAttributesFragmentWriter.createInstance());
				break;
			case NodesElement.ASPECT_NAME:
				writers.add(NodesFragmentWriter.createInstance());
				break;
			case CyVisualPropertiesElement.ASPECT_NAME:
				writers.add(VisualPropertiesFragmentWriter.createInstance());
				break;
			case SubNetworkElement.ASPECT_NAME:
				writers.add(SubNetworkFragmentWriter.createInstance());
				break;
			case NetworkRelationsElement.ASPECT_NAME:
				writers.add(NetworkRelationsFragmentWriter.createInstance());
				break;
			case CyGroupsElement.ASPECT_NAME:
				writers.add(CyGroupsFragmentWriter.createInstance());
				break;
			case CyTableColumnElement.ASPECT_NAME:
				writers.add(CyTableColumnFragmentWriter.createInstance());
				break;
			case AbstractTableVisualProperty.ASPECT_NAME:
				writers.add(new GeneralAspectFragmentWriter (AbstractTableVisualProperty.ASPECT_NAME));
				break;
			default:
				throw new IllegalArgumentException("Cannot get writer for unknown aspect: " + aspect);
			}
		}
		return writers;
	}

	public final static Set<AspectFragmentReader> getAspectFragmentReaders(Collection<String> _aspects) {
		final Set<AspectFragmentReader> readers = new HashSet<>();
		for (String aspect : _aspects) {
			switch (aspect) {
			case CartesianLayoutElement.ASPECT_NAME:
				readers.add(CartesianLayoutFragmentReader.createInstance());
				break;
			case EdgeAttributesElement.ASPECT_NAME:
				readers.add(EdgeAttributesFragmentReader.createInstance());
				break;
			case EdgesElement.ASPECT_NAME:
				readers.add(EdgesFragmentReader.createInstance());
				break;
			case NetworkAttributesElement.ASPECT_NAME:
				readers.add(NetworkAttributesFragmentReader.createInstance());
				break;
			case NodeAttributesElement.ASPECT_NAME:
				readers.add(NodeAttributesFragmentReader.createInstance());
				break;
			case HiddenAttributesElement.ASPECT_NAME:
				readers.add(HiddenAttributesFragmentReader.createInstance());
				break;
			case NodesElement.ASPECT_NAME:
				readers.add(NodesFragmentReader.createInstance());
				break;
			case CyVisualPropertiesElement.ASPECT_NAME:
				readers.add(CyVisualPropertiesFragmentReader.createInstance());
				break;
			case SubNetworkElement.ASPECT_NAME:
				readers.add(SubNetworkFragmentReader.createInstance());
				break;
			case NetworkRelationsElement.ASPECT_NAME:
				readers.add(NetworkRelationsFragmentReader.createInstance());
				break;
			case CyGroupsElement.ASPECT_NAME:
				readers.add(CyGroupsFragmentReader.createInstance());
				break;
			case CyTableColumnElement.ASPECT_NAME:
				readers.add(CyTableColumnFragmentReader.createInstance());
				break;
			case AbstractTableVisualProperty.ASPECT_NAME:
				readers.add(new GeneralAspectFragmentReader<> (AbstractTableVisualProperty.ASPECT_NAME,
						CyTableVisualPropertiesElement.class));
				break;
			default:
				throw new IllegalArgumentException("Cannot get writer for unknown aspect: " + aspect);
			}
		}
		return readers;
	}
}
