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

public class ReedResource extends FrequencyResourceBase
{
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int minAltitude;
	private final MaterialSet sourceBlocks;
	
	public ReedResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.frequency = readInt(args.get(1), 1, 100);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.MIN_POSSIBLE_Y, Constants.MAX_POSSIBLE_Y);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.MAX_POSSIBLE_Y);
		this.sourceBlocks = readMaterials(args, 5, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion world, Random rand, int x, int z)
	{
		int y = world.getHighestBlockAboveYAt(x, z);
		LocalMaterialData materialA = world.getMaterial(x - 1, y - 1, z);
		LocalMaterialData materialB = world.getMaterial(x + 1, y - 1, z);
		LocalMaterialData materialC = world.getMaterial(x, y - 1, z - 1);
		LocalMaterialData materialD = world.getMaterial(x, y - 1, z + 1);
		if (
			y > this.maxAltitude || y > world.getWorldMaxY() ||
			y < this.minAltitude || y < world.getWorldMinY() ||
			(
				materialA != null && !materialA.isLiquid() &&
				materialB != null && !materialB.isLiquid() &&
				materialC != null && !materialC.isLiquid() &&
				materialD != null && !materialD.isLiquid()
			)
		)
		{
			return;
		}
		
		LocalMaterialData worldMaterial = world.getMaterial(x, y - 1, z);
		if (worldMaterial == null || !this.sourceBlocks.contains(worldMaterial))
		{
			return;
		}

		int height = 1 + rand.nextInt(2);
		for (int y1 = 0; y1 < height; y1++)
		{
			world.setBlock(x, y + y1, z, this.material);
		}
	}

	@Override
	public String toString()
	{
		return "Reed(" + this.material + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
