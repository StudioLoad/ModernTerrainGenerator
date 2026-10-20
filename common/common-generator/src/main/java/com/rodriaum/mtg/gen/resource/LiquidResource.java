package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates a waterfall feature, called a Spring in minecraft code.
 */
public class LiquidResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int minAltitude;
	private final MaterialSet sourceBlocks;

	public LiquidResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.frequency = readInt(args.get(1), 1, 5000);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.MIN_POSSIBLE_Y, Constants.MAX_POSSIBLE_Y);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.MAX_POSSIBLE_Y);
		this.sourceBlocks = readMaterials(args, 5, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion world, Random rand, int x, int z)
	{
		int y = getValidYInRange(rand, this.minAltitude, this.maxAltitude, world);
		LocalMaterialData worldMaterial = world.getMaterial(x, y + 1, z);
		if (worldMaterial == null || !this.sourceBlocks.contains(worldMaterial))
		{
			return;
		}
		
		worldMaterial = world.getMaterial(x, y - 1, z);
		if (worldMaterial == null || !this.sourceBlocks.contains(worldMaterial))
		{
			return;
		}

		worldMaterial = world.getMaterial(x, y, z);
		if (worldMaterial == null || (!worldMaterial.isAir() && !this.sourceBlocks.contains(worldMaterial)))
		{
			return;
		}

		int sourceCount = 0;
		int airCount = 0;

		worldMaterial = world.getMaterial(x - 1, y, z);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		worldMaterial = world.getMaterial(x + 1, y, z);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		worldMaterial = world.getMaterial(x, y, z - 1);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		worldMaterial = world.getMaterial(x, y, z + 1);
		sourceCount = (worldMaterial != null && this.sourceBlocks.contains(worldMaterial)) ? sourceCount + 1 : sourceCount;
		airCount = (worldMaterial != null && worldMaterial.isAir()) ? airCount + 1 : airCount;

		if ((sourceCount == 3) && (airCount == 1))
		{
			world.setBlock(x, y, z, this.material);
		}
	}
	
	@Override
	public String toString()
	{
		return "Liquid(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
