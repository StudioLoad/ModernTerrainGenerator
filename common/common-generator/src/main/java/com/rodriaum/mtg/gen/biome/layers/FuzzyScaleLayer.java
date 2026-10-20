package com.rodriaum.mtg.gen.biome.layers;

import com.rodriaum.mtg.gen.biome.layers.util.LayerSampleContext;

class FuzzyScaleLayer extends ScaleLayer
{
	@Override
	protected int sample(LayerSampleContext<?> context, int i, int j, int k, int l)
	{
		return context.choose(i, j, k, l);
	}
}
