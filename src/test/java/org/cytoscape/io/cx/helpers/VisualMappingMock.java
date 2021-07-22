package org.cytoscape.io.cx.helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.BendFactoryImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.network.CyNetworkViewManagerImpl;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.NetworkVisualMappingManagerImpl;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.mockito.Mockito;


public class VisualMappingMock{

	public abstract class DummyLayoutContext {
		@Tunable(description = "DummyDescription")
		public Integer size;
	}
	
	private VisualMappingMock() { }

	public static void init(CyServiceRegistrar serviceRegistrar) {
		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		final VisualStyleFactory vsFactory = new VisualStyleFactoryImpl(serviceRegistrar, ptFactory);
		final NetworkViewRenderer netViewRenderer = mock(NetworkViewRenderer.class);
		final CyApplicationManager appManager = mock(CyApplicationManager.class);
		final CyTableViewManager tableViewManager = mock(CyTableViewManager.class);
		final TableVisualMappingManager tableVisualMappingManager = mock(TableVisualMappingManager.class);
		@SuppressWarnings("unchecked")
		final RenderingEngineFactory<CyNetwork> engineFactory = mock(RenderingEngineFactory.class);
		final RenderingEngineManager renderManager = mock(RenderingEngineManager.class);
		final CyLayoutAlgorithmManager layoutManager = mock(CyLayoutAlgorithmManager.class);
		
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appManager);
		when(serviceRegistrar.getService(CyTableViewManager.class)).thenReturn(tableViewManager);
		when(serviceRegistrar.getService(TableVisualMappingManager.class)).thenReturn(tableVisualMappingManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(netViewRenderer.getRenderingEngineFactory(Mockito.anyString())).thenReturn(engineFactory);
		when(appManager.getCurrentNetworkViewRenderer()).thenReturn(netViewRenderer);
		when(appManager.getNetworkViewRenderer(Mockito.anyString())).thenReturn(netViewRenderer);
		Set<NetworkViewRenderer> renderers = new HashSet<NetworkViewRenderer>();
		renderers.add(netViewRenderer);
		when(appManager.getNetworkViewRendererSet()).thenReturn(renderers);
		
		DummyLayoutContext dummyLayoutContext = mock(DummyLayoutContext.class);
		
		CyLayoutAlgorithm gridDef = mock(CyLayoutAlgorithm.class);
		
		when(gridDef.createLayoutContext()).thenReturn(dummyLayoutContext);
		when(gridDef.getDefaultLayoutContext()).thenReturn(dummyLayoutContext);
		when(gridDef.getName()).thenReturn("grid");
		
		when(layoutManager.getLayout(Mockito.eq("grid"))).thenReturn(gridDef);
		
		CyLayoutAlgorithm forceDirectedDef = mock(CyLayoutAlgorithm.class);
		when(forceDirectedDef.createLayoutContext()).thenReturn(dummyLayoutContext);
		when(forceDirectedDef.getDefaultLayoutContext()).thenReturn(dummyLayoutContext);
		when(forceDirectedDef.getName()).thenReturn("force-directed");
		
		when(layoutManager.getLayout(Mockito.eq("force-directed"))).thenReturn(forceDirectedDef);
		
		
		CyServiceModule.setService(CyApplicationManager.class, appManager);
		CyServiceModule.setService(CyTableViewManager.class, tableViewManager);
		CyServiceModule.setService(TableVisualMappingManager.class, tableVisualMappingManager);
		CyServiceModule.setService(RenderingEngineManager.class, renderManager);
		CyServiceModule.setService(VisualStyleFactory.class, vsFactory);
		CyServiceModule.setService(CyLayoutAlgorithmManager.class, layoutManager);
		CyServiceModule.setService(CyEventHelper.class, eventHelper);
		
		CyServiceModule.setPassthroughMapping(new PassthroughMappingFactory(serviceRegistrar));
		CyServiceModule.setDiscreteMapping(new DiscreteMappingFactory(serviceRegistrar));
		CyServiceModule.setContinuousMapping(new ContinuousMappingFactory(serviceRegistrar));
		
		final VisualMappingManager vmm = new NetworkVisualMappingManagerImpl(vsFactory, serviceRegistrar);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmm);
		CyServiceModule.setService(VisualMappingManager.class, vmm);
		
		Set<VisualLexicon> lexicons = new HashSet<>();
		//final CustomGraphicsManager cgManager = mock(CustomGraphicsManager.class);
		
		DVisualLexicon lexicon = new DVisualLexicon();
		lexicons.add(lexicon);
		
		BendFactory bendFactory = new BendFactoryImpl();
//		registerService(bc, bendFactory, BendFactory.class);
		lexicon.addBendFactory(bendFactory, new HashMap<>());
		
		when(renderManager.getDefaultVisualLexicon()).thenReturn(lexicon);
		when(engineFactory.getVisualLexicon()).thenReturn(lexicon);
		
		when(serviceRegistrar.getService(CyApplicationConfiguration.class)).thenReturn(mock(CyApplicationConfiguration.class));
		when(serviceRegistrar.getService(DialogTaskManager.class)).thenReturn(mock(DialogTaskManager.class));
		
		new CyNetworkViewManagerImpl(serviceRegistrar);
		
	}
}