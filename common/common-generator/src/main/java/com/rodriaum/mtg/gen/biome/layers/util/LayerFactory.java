package com.rodriaum.mtg.gen.biome.layers.util;

import com.rodriaum.mtg.interfaces.ILayerSampler;

public interface LayerFactory<A extends ILayerSampler>
{
	A make();
}
