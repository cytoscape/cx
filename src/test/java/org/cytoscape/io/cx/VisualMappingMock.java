package org.cytoscape.io.cx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.mockito.Mockito;

public class VisualMappingMock{

	protected NetworkTestSupport nts = new NetworkTestSupport();
	protected NetworkViewTestSupport nvts = new NetworkViewTestSupport();
	
	private RenderingEngineManager renderManager = mock(RenderingEngineManager.class);
	private VisualMappingManager vmm = mock(VisualMappingManager.class);
	private CyApplicationManager applicationMgr = mock(CyApplicationManager.class);
	
	protected VisualStyle style;
	protected VisualLexicon lexicon;

	private PassthroughMappingFactory passthroughFactory;
	private ContinuousMappingFactory continuousFactory;
	private DiscreteMappingFactory discreteFactory;
	private VisualStyleFactory vsFactory;

	protected InputStreamTaskFactory inputStreamCXTaskFactory;

	public VisualMappingMock() {
		
		Set<VisualStyle> styles = new HashSet<VisualStyle>();
		VisualStyle mockStyle = mock(VisualStyle.class);
		when(mockStyle.getTitle()).thenReturn("mock1");
		styles.add(mockStyle);
		try {
			this.style = initStyle();
			styles.add(this.style);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Could not init Style.", e);
		}
		when(vmm.getAllVisualStyles()).thenReturn(styles);
		Set<VisualLexicon> lex = new HashSet<>();
		lex.add(lexicon);
		when(vmm.getAllVisualLexicon()).thenReturn(lex);
		when(vmm.getDefaultVisualStyle()).thenReturn(this.style);
		when(renderManager.getDefaultVisualLexicon()).thenReturn(lexicon);
	}


	private VisualStyle initStyle() throws Exception {
		final CustomGraphicsManager cgManager = mock(CustomGraphicsManager.class);
		lexicon = new DVisualLexicon(cgManager);

		final CyEventHelper eventHelper = mock(CyEventHelper.class);

		final CyServiceRegistrar cyServiceRegistrar = mock(CyServiceRegistrar.class);
		when(cyServiceRegistrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
		passthroughFactory = new PassthroughMappingFactory(eventHelper);
		discreteFactory = new DiscreteMappingFactory(eventHelper);
		continuousFactory = new ContinuousMappingFactory(eventHelper);

//		mappingFactoryManager.addFactory(passthroughFactory, null);
//		mappingFactoryManager.addFactory(continuousFactory, null);
//		mappingFactoryManager.addFactory(discreteFactory, null);

		this.style = generateVisualStyle(lexicon);
		when(vmm.getVisualStyle(Mockito.any())).thenReturn(style);
		setDefaults();
		setMappings();

		return style;
	}


	private final VisualStyle generateVisualStyle(final VisualLexicon lexicon) {
		final Set<VisualLexicon> lexiconSet = Collections.singleton(lexicon);

		when(vmm.getAllVisualLexicon()).thenReturn(lexiconSet);

		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		CyEventHelper eventHelper = mock(CyEventHelper.class);

		RenderingEngineFactory engineFactory = mock(RenderingEngineFactory.class);
		when(engineFactory.getVisualLexicon()).thenReturn(lexicon);
		
		RenderingEngine renderingEngine = mock(RenderingEngine.class);
		when(renderingEngine.getRendererId()).thenReturn("org.cytoscape.ding");
		Collection<RenderingEngine<?>> renderingEngines = new ArrayList<RenderingEngine<?>>();
		renderingEngines.add(renderingEngine);

		when(renderManager.getRenderingEngines(Mockito.anyObject())).thenReturn(renderingEngines);
		

		NetworkViewRenderer netViewRenderer = mock(NetworkViewRenderer.class);
		when(netViewRenderer.getRenderingEngineFactory(Mockito.anyString())).thenReturn(engineFactory);

		when(applicationMgr.getCurrentNetworkViewRenderer()).thenReturn(netViewRenderer);
		when(applicationMgr.getNetworkViewRenderer(Mockito.anyString())).thenReturn(netViewRenderer);
		

		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmm);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(applicationMgr);

		vsFactory = new VisualStyleFactoryImpl(serviceRegistrar, ptFactory);
		return vsFactory.createVisualStyle("vs1");
	}

	private final void setDefaults() {
		// Node default values
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(10, 10, 200));
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);

		style.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 40d);
		style.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		style.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 60d);

		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);

		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 2d);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 150);

		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 18);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font("Helvetica", Font.PLAIN, 12));
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 122);
//		style.setDefaultValue(DVisualLexicon.NODE_LABEL_POSITION,
//				new ObjectPosition(Position.NORTH_EAST, Position.CENTER, Justification.JUSTIFY_CENTER, 0,0));

		// For Selected
		style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, Color.RED);

		// Edge default values
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(12,100,200));
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, new Color(222, 100, 10));

		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DOT);

		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 3d);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, Color.red);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_FACE, new Font("SansSerif", Font.BOLD, 12));
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 11);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 220);

		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.DELTA);
		style.setDefaultValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.T);

		style.setDefaultValue(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, new Color(20, 100, 100));
		style.setDefaultValue(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT, new Color(10, 100, 100));

		// For Selected
		style.setDefaultValue(BasicVisualLexicon.EDGE_SELECTED_PAINT, Color.PINK);
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.ORANGE);
	}

	private final void setMappings() {
		// Passthrough mappings
		final VisualMappingFunction<String, String> nodeLabelMapping = passthroughFactory.createVisualMappingFunction(
				CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		final VisualMappingFunction<String, String> edgeLabelMapping = passthroughFactory.createVisualMappingFunction(
				CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LABEL);
		style.addVisualMappingFunction(nodeLabelMapping);
		style.addVisualMappingFunction(edgeLabelMapping);

		// Continuous mappings
		// Simple two points mapping.
		final ContinuousMapping<Integer, Paint> nodeLabelColorMapping = (ContinuousMapping<Integer, Paint>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_LABEL_COLOR);

		final ContinuousMapping<Double, Integer> nodeOpacityMapping = (ContinuousMapping<Double, Integer>) continuousFactory
				.createVisualMappingFunction("Betweenness Centrality", Double.class, BasicVisualLexicon.NODE_TRANSPARENCY);

		final ContinuousMapping<Integer, Double> nodeWidthMapping = (ContinuousMapping<Integer, Double>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_WIDTH);
		final ContinuousMapping<Integer, Double> nodeHeightMapping = (ContinuousMapping<Integer, Double>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_HEIGHT);

		// Complex multi-point mapping
		final ContinuousMapping<Integer, Paint> nodeColorMapping = (ContinuousMapping<Integer, Paint>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);

		final BoundaryRangeValues<Paint> lc1 = new BoundaryRangeValues<Paint>(Color.black, Color.yellow, Color.green);
		final BoundaryRangeValues<Paint> lc2 = new BoundaryRangeValues<Paint>(Color.red, Color.pink, Color.blue);
		nodeLabelColorMapping.addPoint(3, lc1);
		nodeLabelColorMapping.addPoint(10, lc2);
		style.addVisualMappingFunction(nodeLabelColorMapping);

		final BoundaryRangeValues<Paint> color1 = new BoundaryRangeValues<Paint>(Color.black, Color.red, Color.orange);
		final BoundaryRangeValues<Paint> color2 = new BoundaryRangeValues<Paint>(Color.white, Color.white, Color.white);
		final BoundaryRangeValues<Paint> color3= new BoundaryRangeValues<Paint>(Color.green, Color.pink, Color.blue);

		// Shuffle insertion.
		nodeColorMapping.addPoint(2, color1);
		nodeColorMapping.addPoint(5, color2);
		nodeColorMapping.addPoint(10, color3);

		final BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(20d, 20d, 20d);
		final BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(200d, 200d, 400d);
		nodeWidthMapping.addPoint(1, bv0);
		nodeWidthMapping.addPoint(20, bv1);
		nodeHeightMapping.addPoint(1, bv0);
		nodeHeightMapping.addPoint(20, bv1);

		final BoundaryRangeValues<Integer> trans0 = new BoundaryRangeValues<Integer>(10, 10, 10);
		final BoundaryRangeValues<Integer> trans1 = new BoundaryRangeValues<Integer>(80, 80, 100);
		final BoundaryRangeValues<Integer> trans2 = new BoundaryRangeValues<Integer>(222, 222, 250);
		nodeOpacityMapping.addPoint(0.22, trans0);
		nodeOpacityMapping.addPoint(0.61, trans1);
		nodeOpacityMapping.addPoint(0.95, trans2);

		style.addVisualMappingFunction(nodeWidthMapping);
		style.addVisualMappingFunction(nodeHeightMapping);
		style.addVisualMappingFunction(nodeOpacityMapping);
		style.addVisualMappingFunction(nodeColorMapping);

		// Discrete mappings
		final DiscreteMapping<String, NodeShape> nodeShapeMapping = (DiscreteMapping<String, NodeShape>) discreteFactory
				.createVisualMappingFunction("Node Type", String.class, BasicVisualLexicon.NODE_SHAPE);
		nodeShapeMapping.putMapValue("gene", NodeShapeVisualProperty.DIAMOND);
		nodeShapeMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
		nodeShapeMapping.putMapValue("compound", NodeShapeVisualProperty.ROUND_RECTANGLE);
		nodeShapeMapping.putMapValue("pathway", NodeShapeVisualProperty.OCTAGON);

		style.addVisualMappingFunction(nodeShapeMapping);

//		final DiscreteMapping<String, ObjectPosition> nodeLabelPosMapping = (DiscreteMapping<String, ObjectPosition>) discreteFactory
//				.createVisualMappingFunction("Node Type", String.class, DVisualLexicon.NODE_LABEL_POSITION);
//		nodeLabelPosMapping.putMapValue("gene", new ObjectPosition(Position.SOUTH, Position.NORTH_WEST, Justification.JUSTIFY_CENTER, 0,0));
//		nodeLabelPosMapping.putMapValue("protein", new ObjectPosition(Position.EAST, Position.WEST, Justification.JUSTIFY_CENTER, 0,0));

//		style.addVisualMappingFunction(nodeLabelPosMapping);

		final DiscreteMapping<String, Paint> edgeColorMapping = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction("interaction", String.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		edgeColorMapping.putMapValue("pp", Color.green);
		edgeColorMapping.putMapValue("pd", Color.red);

		style.addVisualMappingFunction(edgeColorMapping);

		final DiscreteMapping<String, Integer> edgeTransparencyMapping = (DiscreteMapping<String, Integer>) discreteFactory
				.createVisualMappingFunction("interaction", String.class,
						BasicVisualLexicon.EDGE_TRANSPARENCY);
		edgeTransparencyMapping.putMapValue("pp", 222);
		edgeTransparencyMapping.putMapValue("pd", 123);

		style.addVisualMappingFunction(edgeTransparencyMapping);
	}


	public VisualMappingManager getVisualMappingManager() {
		return vmm;
	}
	public CyApplicationManager getApplicationManager() {
		return applicationMgr;
	}
	
	public VisualStyleFactory getVisualStyleFactory() {
		return vsFactory;
	}
	
	public PassthroughMappingFactory getPassthroughFactory() {
		return passthroughFactory;
	}
	public DiscreteMappingFactory getDiscreteFactory() {
		return discreteFactory;
	}
	public ContinuousMappingFactory getContinuousFactory() {
		return continuousFactory;
	}
	public RenderingEngineManager getRenderManager() {
		return renderManager;
	}
}