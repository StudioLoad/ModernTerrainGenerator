package com.rodriaum.mtg.gen.biome.layers;

import com.rodriaum.mtg.gen.biome.layers.type.ParentedLayer;

/**
 * Places the biomes at a specific depth, given the biome groups.
 */
abstract class BiomeLayerBase implements ParentedLayer
{
	protected final BiomeLayerData data;
	protected final int depth;

	BiomeLayerBase(BiomeLayerData data, int depth)
	{
		this.data = data;
		this.depth = depth;
	}
}
