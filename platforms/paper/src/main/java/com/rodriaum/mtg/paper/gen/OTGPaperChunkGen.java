package com.rodriaum.mtg.paper.gen;

import com.rodriaum.mtg.core.OTG;
import com.rodriaum.mtg.core.presets.Preset;
import com.rodriaum.mtg.paper.PaperEngine;
import com.rodriaum.mtg.util.ChunkCoordinate;
import com.rodriaum.mtg.util.FifoMap;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class OTGPaperChunkGen extends ChunkGenerator
{
	public OTGNoiseChunkGenerator generator = null;
	private final FifoMap<ChunkCoordinate, ChunkData> chunkDataCache = new FifoMap<>(128);
	private final Preset preset;
	
	public OTGPaperChunkGen(Preset preset)
	{
		this.preset = preset;
	}
	
	// In case generator isn't loaded yet, expose preset.
	public Preset getPreset()
	{
		return this.preset;
	}
	
	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
	{		
		if (generator == null)
		{
			((PaperEngine) OTG.getEngine()).getPlugin().injectInternalGenerator(world);
			if (generator == null)
			{
				throw new IllegalStateException("MTG failed to initialise its internal generator for world '" + world.getName() + "'. Check the server log for the generator injection error.");
			}
		}
		generator.fixBiomes(chunkX, chunkZ);

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
		ChunkData chunkData = chunkDataCache.get(chunkCoord);
		if (chunkData == null)
		{
			chunkData = createChunkData(world);
			generator.buildNoiseSpigot(((CraftWorld)world).getHandle(), chunkData, chunkCoord, random);
			chunkDataCache.put(chunkCoord, chunkData);
		}
		return chunkData;
	}

	@Override
	public boolean isParallelCapable()
	{
		return false;
	}

	@Override
	public boolean shouldGenerateCaves()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateDecorations()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateMobs()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateStructures()
	{
		return true;
	}
}
