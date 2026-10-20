package com.rodriaum.mtg.util.gen;

import com.rodriaum.mtg.constants.Constants;

import java.util.Random;

/**
 * Holds early generator information about a chunk, like water levels, noise
 * values, etc.
 */
public final class GeneratingChunk
{

	private static final int BEDROCK_LAYER_HEIGHT = 5;

	public final int maxY;
	private final int minY;
	public final Random random;
	private final int[] waterLevel;
	private final double[] surfaceNoise;

	public GeneratingChunk(Random random, int[] waterLevel, double[] surfaceNoise, int maxY, int minY)
	{
		this.random = random;
		this.waterLevel = waterLevel;
		this.surfaceNoise = surfaceNoise;
		this.maxY = maxY;
		this.minY = minY;
	}

	/**
	 * Gets the surface noise value at the given position.
	 * 
	 * @param x X position, 0 <= x < {@value Constants#CHUNK_SIZE}.
	 * @param z Z position, 0 <= z < {@value Constants#CHUNK_SIZE}.
	 * @return The surface noise value.
	 */
	public double getNoise(int x, int z)
	{
		return this.surfaceNoise[x + z * Constants.CHUNK_SIZE];
	}

	/**
	 * Gets the water level at the given position.
	 * 
	 * @param x X position, 0 <= x < {@value Constants#CHUNK_SIZE}.
	 * @param z Z position, 0 <= z < {@value Constants#CHUNK_SIZE}.
	 * @return The water level.
	 */
	public int getWaterLevel(int x, int z)
	{
		return this.waterLevel[z + x * Constants.CHUNK_SIZE];
	}

	/**
	 * Gets whether bedrock should be created at the given position.
	 *
	 * @param y			The y position.
	 * @return True if bedrock should be created, false otherwise.
	 */
	public boolean mustCreateBedrockAt(boolean flatBedrock, boolean disableBedrock, boolean ceilingBedrock, int y)
	{
		// The "- 2" that appears in this method, comes from that heightCap -
		// 1 is the highest place where a block can be placed, and heightCap -
		// 2 is the highest place where bedrock can be generated to make sure
		// there are no light glitches - see #117

		// Handle flat bedrock
		if (flatBedrock)
		{
			if (!disableBedrock && y == minY)
			{
				return true;
			}
			return ceilingBedrock && y >= this.maxY - 1;
		}

		// Otherwise we have normal (non-flat) bedrock
		if (!disableBedrock && y < minY + BEDROCK_LAYER_HEIGHT)
		{
			return y <= minY + this.random.nextInt(5);
		}
		if (ceilingBedrock)
		{
			int amountBelowHeightCap = this.maxY - y - 1;
			if (amountBelowHeightCap < 0 || amountBelowHeightCap > BEDROCK_LAYER_HEIGHT)
			{
				return false;
			}

			return amountBelowHeightCap <= this.random.nextInt(BEDROCK_LAYER_HEIGHT);
		}
		return false;
	} 
}
