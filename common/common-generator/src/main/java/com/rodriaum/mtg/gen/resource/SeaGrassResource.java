package com.rodriaum.mtg.gen.resource;

import java.util.List;
import java.util.Random;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterials;

public class SeaGrassResource extends FrequencyResourceBase
{
	private final double tallChance;

	public SeaGrassResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
		this.tallChance = readDouble(args.get(2), 0.0, 1.0);
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		// Find lowest point
		int y = world.getBlockAboveSolidHeight(x, z);

		// TODO: sourceblocks
		LocalMaterialData below = world.getMaterial(x, y - 1, z);
		if (below == null || !below.isSolid())
		{
			return;
		}

		LocalMaterialData material = world.getMaterial(x, y, z);

		if (material == null)
		{
			return;
		}

		if (material.isLiquid())
		{
			// If the tall chance check succeeds and the above material is also water, place tall seagrass
			if (random.nextDouble() <= this.tallChance && world.getMaterial(x, y + 1, z).isLiquid())
			{
				world.setBlock(x, y, z, LocalMaterials.TALL_SEAGRASS_LOWER);
				world.setBlock(x, y + 1, z, LocalMaterials.TALL_SEAGRASS_UPPER);
			} else {
				world.setBlock(x, y, z, LocalMaterials.SEAGRASS);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "SeaGrass(" + this.frequency + "," + this.rarity + "," + this.tallChance + ")";
	}	
}
