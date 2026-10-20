package com.rodriaum.mtg.gen.biome.layers.type;

import com.rodriaum.mtg.gen.biome.layers.util.LayerFactory;
import com.rodriaum.mtg.gen.biome.layers.util.LayerSampleContext;
import com.rodriaum.mtg.interfaces.ILayerSampler;

/**
 * The type for layers that create a new layer generation stack.
 */
public interface InitLayer
{
	default <R extends ILayerSampler> LayerFactory<R> create(LayerSampleContext<R> context)
	{
	  return () -> context.createSampler((x, z) -> {
		 context.initSeed(x, z);
		 return this.sample(context, x, z);
	  });
	}

	int sample(LayerSampleContext<?> context, int x, int z);
}
