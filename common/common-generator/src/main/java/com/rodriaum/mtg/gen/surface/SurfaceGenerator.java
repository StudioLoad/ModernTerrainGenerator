package com.rodriaum.mtg.gen.surface;

import com.rodriaum.mtg.interfaces.IBiome;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.rodriaum.mtg.util.gen.ChunkBuffer;
import com.rodriaum.mtg.util.gen.GeneratingChunk;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

// TODO: Lol, ye olde comment. Refactor this? 
/**
 * Minecraft 1.7 added block data support to the initial terrain generation.
 * CraftBukkit doesn't support this (yet), so all things with block data have
 * to be moved to the terrain decoration step.
 * <p />
 * The purpose of classes implementing this interface is to add the block data
 * to the surface blocks and the blocks just below that. At the moment, these
 * are the only blocks that require block data during the initial terrain
 * generation.
 */
public interface SurfaceGenerator
{
	/**
	 * Spawns this surface layer in the world.
	 * @param generatingChunk Information about the chunk being generated.
	 * @param chunkBuffer	 The chunk buffer.
	 * @param biome			 The biome to use for the settings.
	 * @param xInWorld		 X position in the world.
	 * @param zInWorld		 Z position in the world.
	 * @param minY			 The minimum allowed Y value in the world
	 */
	void spawn(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiome biome, int xInWorld, int zInWorld, int minY);

	/**
	 * Writes the settings used to a string. There must be a constructor to
	 * read this string again.
	 * 
	 * @return The settings as a string.
	 */
	String toString();

	LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld);

	LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld);
}
