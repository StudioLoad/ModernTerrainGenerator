package com.rodriaum.mtg.gen.surface;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiome;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.rodriaum.mtg.util.gen.ChunkBuffer;
import com.rodriaum.mtg.util.gen.GeneratingChunk;
import com.rodriaum.mtg.util.helpers.StringHelper;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MultipleLayersSurfaceGenerator extends SimpleSurfaceGenerator
{
	// Must be sorted based on the noise field
	protected List<MultipleLayersSurfaceGeneratorLayer> layers;

	MultipleLayersSurfaceGenerator(String[] args, IMaterialReader materialReader) throws InvalidConfigException
	{
		this.layers = new ArrayList<MultipleLayersSurfaceGeneratorLayer>();
		int entryLength = 3;
		for (int i = 0; i < args.length - 2; i += entryLength)
		{
			LocalMaterialData firstBlock = materialReader.readMaterial(args[i]);
			LocalMaterialData secondBlock = materialReader.readMaterial(args[i + 1]);
			LocalMaterialData thirdBlock = null;
			float maxNoise;
			try
			{
				maxNoise = (float) StringHelper.readDouble(args[i + 2], -20, 20);
			}
			catch(InvalidConfigException ex)
			{
				thirdBlock = materialReader.readMaterial(args[i + 2]);
				maxNoise = (float) StringHelper.readDouble(args[i + 3], -20, 20);
			}
			if(thirdBlock != null)
			{
				this.layers.add(new MultipleLayersSurfaceGeneratorLayer(firstBlock, secondBlock, thirdBlock, maxNoise));
				entryLength = 4;
			} else {
				this.layers.add(new MultipleLayersSurfaceGeneratorLayer(firstBlock, firstBlock, secondBlock, maxNoise));
				entryLength = 3;
			}
		}
		Collections.sort(layers);
	}

	@Override
	public LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{
		if(this.layers.size() > 0)
		{
			double noise = noiseProvider.getBiomeBlocksNoiseValue(xInWorld, zInWorld);
			for (MultipleLayersSurfaceGeneratorLayer layer : this.layers)
			{
				if (noise <= layer.maxNoise)
				{
					return layer.getSurfaceBlockReplaced(yInWorld, biomeConfig);
				}
			}
		}
		return biomeConfig.getSurfaceBlockReplaced(yInWorld);
	}

	@Override
	public LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{
		if(this.layers.size() > 0)
		{
			double noise = noiseProvider.getBiomeBlocksNoiseValue(xInWorld, zInWorld);  		
			for (MultipleLayersSurfaceGeneratorLayer layer : this.layers)
			{
				if (noise <= layer.maxNoise)
				{
					return layer.getGroundBlockReplaced(yInWorld, biomeConfig);
				}
			}
		}
		return biomeConfig.getGroundBlockReplaced(yInWorld);
	}

	@Override
	public void spawn(long worldSeed, GeneratingChunk generatingChunkInfo, ChunkBuffer chunkBuffer, IBiome biome, int xInWorld, int zInWorld, int minY)
	{
		int x = xInWorld & 0xf;
		int z = zInWorld & 0xf;
		if(this.layers.size() > 0)
		{
			double noise = generatingChunkInfo.getNoise(x, z);
			for (MultipleLayersSurfaceGeneratorLayer layer : this.layers)
			{
				if (noise <= layer.maxNoise)
				{
					spawnColumn(worldSeed, layer, generatingChunkInfo, chunkBuffer, biome, xInWorld, zInWorld, minY);
					return;
				}
			}
		}

		// Fall back on normal column
		spawnColumn(worldSeed, null, generatingChunkInfo, chunkBuffer, biome, xInWorld, zInWorld, minY);
	}

	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (MultipleLayersSurfaceGeneratorLayer groundLayer : this.layers)
		{
			stringBuilder.append(groundLayer.surfaceBlock);
			stringBuilder.append(',').append(' ');
			stringBuilder.append(groundLayer.underWaterSurfaceBlock);
			stringBuilder.append(',').append(' ');
			stringBuilder.append(groundLayer.groundBlock);
			stringBuilder.append(',').append(' ');
			stringBuilder.append(groundLayer.maxNoise);
			stringBuilder.append(',').append(' ');
		}
		// Delete last ", "
		if(stringBuilder.length() > 0)
		{
			stringBuilder.deleteCharAt(stringBuilder.length() - 2);
		}
		return stringBuilder.toString();
	}
}
