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
import com.rodriaum.mtg.util.materials.MaterialProperties;

public class KelpResource extends FrequencyResourceBase
{
	public KelpResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = world.getBlockAboveSolidHeight(x, z);

		// TODO: sourceblocks
		LocalMaterialData below = world.getMaterial(x, y - 1, z);
		if (below == null || !below.isSolid())
		{
			return;
		}

		int height = 1 + random.nextInt(10);

		// Iterate upwards
		int dy;
		for (int y1 = 0; y1 <= height; y1++)
		{
			dy = y + y1;

			// Stop if we hit non-water
			if (!world.getMaterial(x, dy, + z).isLiquid())
			{
				break;
			}

			// If we hit the surface of the water, place the top and return
			if (!world.getMaterial(x, dy + 1, + z).isLiquid())
			{
				if (y1 > 0)
				{
					world.setBlock(x, dy, z, LocalMaterials.KELP.withProperty(MaterialProperties.AGE_0_25, 20 + random.nextInt(4)));
				}

				break;
			}

			// Place the top if we're at the top of the column
			if (y1 == height)
			{
				world.setBlock(x, dy, z, LocalMaterials.KELP.withProperty(MaterialProperties.AGE_0_25, 20 + random.nextInt(4)));
			} else {
				world.setBlock(x, dy, z, LocalMaterials.KELP_PLANT);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "Kelp(" + this.frequency + ", " + this.rarity + ")";
	}	
}
