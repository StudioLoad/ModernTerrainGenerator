package com.rodriaum.mtg.paper.gen;

import com.rodriaum.mtg.paper.materials.PaperMaterialData;
import com.rodriaum.mtg.util.ChunkCoordinate;
import com.rodriaum.mtg.util.gen.ChunkBuffer;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;

public class PaperChunkBuffer extends ChunkBuffer
{
	private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
	private ChunkCoordinate chunkCoord = null;
	private ChunkGenerator.ChunkData chunkData = null;
	private ChunkAccess chunk = null;
	private final int yOffset;

	/**
	 * Creates a buffer whose Y coordinates are absolute world coordinates.
	 * Keep this constructor for code such as carvers, which already use world Y.
	 */
	public PaperChunkBuffer(ChunkAccess chunk)
	{
		this(chunk, 0);
	}

	/**
	 * Creates a buffer which translates OTG's zero-based terrain coordinates
	 * into Minecraft's world Y coordinates.
	 */
	public PaperChunkBuffer(ChunkAccess chunk, int yOffset)
	{
		this.chunk = chunk;
		this.yOffset = yOffset;
	}

	public PaperChunkBuffer(ChunkGenerator.ChunkData chunkData, ChunkCoordinate chunkCoord)
	{
		this(chunkData, chunkCoord, chunkData.getMinHeight());
	}

	public PaperChunkBuffer(ChunkGenerator.ChunkData chunkData, ChunkCoordinate chunkCoord, int yOffset)
	{
		this.chunkCoord = chunkCoord;
		this.chunkData = chunkData;
		this.yOffset = yOffset;
	}

	@Override
	public ChunkCoordinate getChunkCoordinate ()
	{
		if (chunkData != null)
		{
			return chunkCoord;
		}
		ChunkPos pos = this.chunk.getPos();
		return ChunkCoordinate.fromChunkCoords(pos.x, pos.z);
	}

	@Override
	public void setBlock (int blockX, int blockY, int blockZ, LocalMaterialData material)
	{
		if (chunkData != null)
		{
			this.chunkData.setBlock(blockX, blockY + this.yOffset, blockZ, ((PaperMaterialData) material).toSpigotBlockData());
		} else {
			this.chunk.setBlockState(this.mutable.set(blockX, blockY + this.yOffset, blockZ), ((PaperMaterialData) material).internalBlock());
		}
	}

	@Override
	public LocalMaterialData getBlock (int blockX, int blockY, int blockZ)
	{
		if (chunkData != null)
		{
			Material material = this.chunkData.getType(blockX, blockY + this.yOffset, blockZ);
			return PaperMaterialData.ofSpigotMaterial(material);
		}
		BlockState blockData = this.chunk.getBlockState(this.mutable.set(blockX, blockY + this.yOffset, blockZ));
		return PaperMaterialData.ofBlockData(blockData);
	}

	public ChunkAccess getChunk()
	{
		return this.chunk;
	}
}
