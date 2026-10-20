package com.rodriaum.mtg.util.gen;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.util.ChunkCoordinate;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

/**
 * ChunkBuffer wraps a platform-specific chunk object for
 * base terrain generation and carvers in the common project.
 */
public abstract class ChunkBuffer
{
	public abstract ChunkCoordinate getChunkCoordinate();

	// TODO: Chunkbuffer uses internal coordinates that do not go from 0 to 16,
	// How large is the provided chunk actually, and how do the coord work?
	
	public abstract void setBlock(int blockX, int blockY, int blockZ, LocalMaterialData material);

	public abstract LocalMaterialData getBlock(int blockX, int blockY, int blockZ);

	// TODO: Are these really necessary, can use heightmaps?
	
	private final short[] highestBlockHeight = new short[Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
	public int getHighestBlockForColumn(int blockX, int blockZ)
	{
		return highestBlockHeight[blockX * Constants.CHUNK_SIZE + blockZ];
	}

	public void setHighestBlockForColumn(int blockX, int blockZ, int height)
	{
		if(height > highestBlockHeight[blockX * Constants.CHUNK_SIZE + blockZ])
		{
			highestBlockHeight[blockX * Constants.CHUNK_SIZE + blockZ] = (short)height;
		}
	}
}
