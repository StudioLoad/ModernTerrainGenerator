package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.gen.resource.util.CoralHelper;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

import java.util.List;
import java.util.Random;

public class CoralMushroomResource extends FrequencyResourceBase
{
	public CoralMushroomResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		this.frequency = readInt(args.get(0), 1, 500);
		this.rarity = readRarity(args.get(1));
	}

	@Override
	public void spawn(IWorldGenRegion world, Random random, int x, int z)
	{
		int y = world.getBlockAboveSolidHeight(x, z);
		LocalMaterialData coral = CoralHelper.getRandomCoralBlock(random);

		int xRadius = random.nextInt(3) + 3;
		int yRadius = random.nextInt(3) + 3;
		int zRadius = random.nextInt(3) + 3;
		int yOffset = random.nextInt(3) + 1;

		for(int x1 = 0; x1 <= xRadius; ++x1)
		{
			for (int y1 = 0; y1 <= yRadius; ++y1)
			{
				for (int z1 = 0; z1 <= zRadius; ++z1)
				{

					if ((x1 != 0 && x1 != yRadius || y1 != 0 && y1 != xRadius) &&
							(z1 != 0 && z1 != zRadius || y1 != 0 && y1 != xRadius) &&
							(x1 != 0 && x1 != yRadius || z1 != 0 && z1 != zRadius) &&
							(x1 == 0 || x1 == yRadius || y1 == 0 || y1 == xRadius || z1 == 0 || z1 == zRadius) &&
							!(random.nextFloat() < 0.1F))
					{
						if (y + y1 - yOffset >= world.getWorldMinY() &&
								y + y1 - yOffset <= world.getWorldMaxY())
						{
							CoralHelper.placeCoralBlock(world, random, x + x1, y + y1 - yOffset, z + z1, coral);
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "CoralMushroom(" + this.frequency + "," + this.rarity + ")";
	}	
}
