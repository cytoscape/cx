package org.cytoscape.io.cx.helpers;

import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphicsmgr.internal.CustomGraphicsManagerImpl;
import org.cytoscape.ding.impl.BendFactoryImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.CyServiceModule;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.internal.VisualMappingManagerImpl;
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
		@SuppressWarnings("unchecked")
		final RenderingEngineFactory<CyNetwork> engineFactory = mock(RenderingEngineFactory.class);
		final RenderingEngineManager renderManager = mock(RenderingEngineManager.class);
		final CyLayoutAlgorithmManager layoutManager = mock(CyLayoutAlgorithmManager.class);
		
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appManager);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		when(netViewRenderer.getRenderingEngineFactory(Mockito.anyString())).thenReturn(engineFactory);
		when(appManager.getCurrentNetworkViewRenderer()).thenReturn(netViewRenderer);
		when(appManager.getNetworkViewRenderer(Mockito.anyString())).thenReturn(netViewRenderer);
		Set<NetworkViewRenderer> renderers = new HashSet<NetworkViewRenderer>();
		renderers.add(netViewRenderer);
		when(appManager.getNetworkViewRendererSet()).thenReturn(renderers);
		
		CyLayoutAlgorithm def = mock(CyLayoutAlgorithm.class);
		DummyLayoutContext dummyLayoutContext = mock(DummyLayoutContext.class);
		when(def.createLayoutContext()).thenReturn(dummyLayoutContext);
		when(def.getDefaultLayoutContext()).thenReturn(dummyLayoutContext);
		when(def.getName()).thenReturn("grid");
		when(layoutManager.getLayout(any())).thenReturn(def);
		
		CyServiceModule.setService(CyApplicationManager.class, appManager);
		CyServiceModule.setService(RenderingEngineManager.class, renderManager);
		CyServiceModule.setService(VisualStyleFactory.class, vsFactory);
		CyServiceModule.setService(CyLayoutAlgorithmManager.class, layoutManager);
		CyServiceModule.setService(CyEventHelper.class, eventHelper);
		
		CyServiceModule.setPassthroughMapping(new PassthroughMappingFactory(serviceRegistrar));
		CyServiceModule.setDiscreteMapping(new DiscreteMappingFactory(serviceRegistrar));
		CyServiceModule.setContinuousMapping(new ContinuousMappingFactory(serviceRegistrar));
		
		final VisualMappingManager vmm = new VisualMappingManagerImpl(vsFactory, serviceRegistrar);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmm);
		CyServiceModule.setService(VisualMappingManager.class, vmm);
		
		Set<VisualLexicon> lexicons = new HashSet<VisualLexicon>();
		final CustomGraphicsManager cgManager = mock(CustomGraphicsManager.class);
		
		DVisualLexicon lexicon = new DVisualLexicon(cgManager);
		lexicons.add(lexicon);
		
		BendFactory bendFactory = new BendFactoryImpl();
//		registerService(bc, bendFactory, BendFactory.class);
		lexicon.addBendFactory(bendFactory, new HashMap<Object, Object>());
		
		when(renderManager.getDefaultVisualLexicon()).thenReturn(lexicon);
		when(engineFactory.getVisualLexicon()).thenReturn(lexicon);
		
		when(serviceRegistrar.getService(CyApplicationConfiguration.class)).thenReturn(mock(CyApplicationConfiguration.class));
		when(serviceRegistrar.getService(DialogTaskManager.class)).thenReturn(mock(DialogTaskManager.class));
		
		new CustomGraphicsManagerImpl(new HashSet<URL>(), serviceRegistrar);
		
	}
}