package com.rodriaum.mtg.gen.biome.layers.util;

import com.rodriaum.mtg.gen.noise.PerlinNoiseSampler;

public interface LayerRandomnessSource
{
	int nextInt(int bound);

	PerlinNoiseSampler getNoiseSampler();
}
