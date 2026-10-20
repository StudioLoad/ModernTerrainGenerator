package com.rodriaum.mtg.gen.biome.layers;

import com.rodriaum.mtg.gen.biome.layers.type.ParentedLayer;
import com.rodriaum.mtg.gen.biome.layers.util.LayerSampleContext;
import com.rodriaum.mtg.interfaces.ILayerSampler;

public class ApplyOceanLayer implements ParentedLayer
{
	//private final BiomeLayerData data;

	public ApplyOceanLayer(BiomeLayerData data)
	{
		//this.data = data;
	}

	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		// If there is no land bit, leave the biome bit at 0.
		// 0 is the world's ocean biome, set by PresetLoader 
		// when registering biomes.

		// TODO: Do we still need this class?
		
		return sample;
	}
}
