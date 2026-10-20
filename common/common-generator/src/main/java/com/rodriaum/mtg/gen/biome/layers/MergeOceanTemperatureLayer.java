package com.rodriaum.mtg.gen.biome.layers;

import com.rodriaum.mtg.gen.biome.layers.type.MergingLayer;
import com.rodriaum.mtg.gen.biome.layers.util.LayerRandomnessSource;
import com.rodriaum.mtg.interfaces.ILayerSampler;

public class MergeOceanTemperatureLayer implements MergingLayer
{
	@Override
	public int sample(LayerRandomnessSource context, ILayerSampler mainSampler, ILayerSampler oceanSampler, int x, int z)
	{
		int sample = mainSampler.sample(x, z);

		// If we're not land, return ocean
		if (!BiomeLayers.isLand(sample))
		{
			return oceanSampler.sample(x, z);
		}

		// Return biome sample
		return sample;
	}
}
