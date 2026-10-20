package com.rodriaum.mtg.gen.biome.layers;

import com.rodriaum.mtg.gen.biome.layers.type.InitLayer;
import com.rodriaum.mtg.gen.biome.layers.util.LayerSampleContext;

/**
 * Initializes the biome sample for biome generation.
 */
class InitializationLayer implements InitLayer
{
	@Override
	public int sample(LayerSampleContext<?> context, int x, int y)
	{
		return 0;
	}
}
