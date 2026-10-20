package com.rodriaum.mtg.gen.resource;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterials;
import com.rodriaum.mtg.util.materials.MaterialSet;
import com.rodriaum.mtg.util.minecraft.PlantType;

import java.util.List;
import java.util.Random;

public class UnderWaterPlantResource extends FrequencyResourceBase
{
	private final int maxAltitude;
	private final int minAltitude;
	private final PlantType plant;
	private final MaterialSet sourceBlocks;

	public UnderWaterPlantResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(6, args);

		this.plant = PlantType.getPlant(args.get(0), materialReader);
		this.frequency = readInt(args.get(1), 1, 100);
		this.rarity = readRarity(args.get(2));
		this.minAltitude = readInt(args.get(3), Constants.MIN_POSSIBLE_Y, Constants.MAX_POSSIBLE_Y);
		this.maxAltitude = readInt(args.get(4), this.minAltitude, Constants.MAX_POSSIBLE_Y);
		this.sourceBlocks = readMaterials(args, 5, materialReader);
	}

	@Override
	public void spawn(IWorldGenRegion world, Random rand, int x, int z)
	{
		int y = getValidYInRange(rand, this.minAltitude, this.maxAltitude, world);

		int j;
		int k;
		int m;
		LocalMaterialData worldMaterial;
		LocalMaterialData worldMaterialBelow;		
		for (int i = 0; i < 64; i++)
		{
			j = x + rand.nextInt(8) - rand.nextInt(8);
			k = y + rand.nextInt(4) - rand.nextInt(4);
			m = z + rand.nextInt(8) - rand.nextInt(8);
			worldMaterial = world.getMaterial(j, k , m);
			worldMaterialBelow = world.getMaterial(j, k - 1, m);
			if (
				(worldMaterial == null || !worldMaterial.isMaterial(LocalMaterials.WATER)) ||
				(worldMaterialBelow == null || !this.sourceBlocks.contains(worldMaterialBelow))
			)
			{
				continue;
			}
			this.plant.spawn(world, j, k, m);
		}
	}
	
	@Override
	public String toString()
	{
		return "UnderWaterPlant(" + this.plant.getName() + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
