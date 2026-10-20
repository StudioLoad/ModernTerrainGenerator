package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.gen.resource.util.CoralHelper;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.MTGDirection;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

import java.util.*;

public class CoralTreeResource extends FrequencyResourceBase
{
	private static final MTGDirection[] HORIZONTAL =
	{
		MTGDirection.NORTH,
		MTGDirection.EAST,
		MTGDirection.SOUTH,
		MTGDirection.WEST
	};

	public CoralTreeResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
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

		int height = random.nextInt(3) + 1;
		for (int i = 0; i < height; i++)
		{
			// Return if we don't have enough space to place the rest of the tree
			if (y + i < world.getWorldMinY() || y + i > world.getWorldMaxY() || !CoralHelper.placeCoralBlock(world, random, x, y + i, z, coral))
			{
				return;
			}
		}

		y += height;

		// 2-4 branch, with a randomized index
		int dirEnd = random.nextInt(3) + 2;
		List<MTGDirection> directions = Arrays.asList(HORIZONTAL);
		Collections.shuffle(directions, random);

		// Iterate 2-4 directions
		int dx;
		int dy;
		int dz;
		int count;
		int placedIndex;
		for (MTGDirection direction : directions.subList(0, dirEnd))
		{
			// Initial branch out
			dx = x + direction.getX();
			dy = y;
			dz = z + direction.getZ();

			// Branch size
			count = random.nextInt(5) + 2;
			placedIndex = 0;

			for (int i = 0; i < count && dy >= world.getWorldMinY() && dy <= world.getWorldMaxY() && CoralHelper.placeCoralBlock(world, random, dx, dy, dz, coral); i++)
			{
				placedIndex++;
				dy++;

				// Branch out if we're either the first index or if we've placed 2 or more with a 1/4 chance
				if (i == 0 || placedIndex >= 2 && random.nextFloat() < 0.25F)
				{
					dx += direction.getX();
					dz += direction.getZ();
					placedIndex = 0;
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "CoralTree(" + this.frequency + "," + this.rarity + ")";
	}	
}
