package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterials;

import java.util.List;
import java.util.Random;

public class VinesResource extends FrequencyResourceBase
{
	private static final LocalMaterialData[] FROM_DIRECTION =
	{
		LocalMaterials.VINE_SOUTH, 
		LocalMaterials.VINE_NORTH, 
		LocalMaterials.VINE_EAST, 
		LocalMaterials.VINE_WEST
	};

	private final int maxAltitude;
	private final int minAltitude;

	public VinesResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);

		assureSize(4, args);
		this.frequency = readInt(args.get(0), 1, 100);
		this.rarity = readRarity(args.get(1));
		this.minAltitude = readInt(args.get(2), Constants.MIN_POSSIBLE_Y, Constants.MAX_POSSIBLE_Y);
		this.maxAltitude = readInt(args.get(3), this.minAltitude, Constants.MAX_POSSIBLE_Y);
	}
	
	private boolean canPlace(IWorldGenRegion worldGenRegion, int x, int y, int z, int direction)
	{
		LocalMaterialData sourceBlock;
		switch (direction)
		{
			default:
				return false;
			case 1:
				sourceBlock = worldGenRegion.getMaterial(x, y + 1, z);
				break;
			case 2:
				sourceBlock = worldGenRegion.getMaterial(x, y, z + 1);
				break;
			case 3:
				sourceBlock = worldGenRegion.getMaterial(x, y, z - 1);
				break;
			case 5:
				sourceBlock = worldGenRegion.getMaterial(x - 1, y, z);
				break;
			case 4:
				sourceBlock = worldGenRegion.getMaterial(x + 1, y, z);
				break;
		}
		return sourceBlock != null && sourceBlock.isSolid() && !sourceBlock.isMaterial(LocalMaterials.BAMBOO);
	}

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random rand, int x, int z)
	{
		int _x = x;
		int _z = z;
		int y = Math.max(this.minAltitude, worldGenRegion.getWorldMinY());

		LocalMaterialData worldMaterial;		
		while (y <= this.maxAltitude)
		{
			worldMaterial = worldGenRegion.getMaterial(_x, y, _z);
			if (worldMaterial != null && worldMaterial.isAir())
			{
				// TODO: Refactor to enum
				for (int direction = 2; direction <= 5; direction++)
				{
					if (canPlace(worldGenRegion, _x, y, _z, direction))
					{
						worldGenRegion.setBlock(_x, y, _z, FROM_DIRECTION[direction - 2]);
						break;
					}
				}
			} else {
				_x = x + rand.nextInt(4) - rand.nextInt(4);
				_z = z + rand.nextInt(4) - rand.nextInt(4);
			}
			y++;
		}
	}
	
	@Override
	public String toString()
	{
		return "Vines(" + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + ")";
	}	
}
