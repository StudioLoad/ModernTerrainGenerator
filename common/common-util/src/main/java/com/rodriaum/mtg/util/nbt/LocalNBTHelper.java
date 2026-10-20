package com.rodriaum.mtg.util.nbt;

import com.rodriaum.mtg.util.gen.LocalWorldGenRegion;

public abstract class LocalNBTHelper
{
	/**
	 * Gets the NBT tag, if any, at a location
	 *
	 * @param world The world to use
	 * @param x	 x coordinate to check
	 * @param y	 y coordinate to check
	 * @param z	 z coordinate to check
	 * @return the NBT data at that location, if any
	 */
	public abstract NamedBinaryTag getNBTFromLocation(LocalWorldGenRegion world, int x, int y, int z);
}
