package com.rodriaum.mtg.paper.util;

import java.util.ArrayList;
import java.util.List;

import com.rodriaum.mtg.util.biome.WeightedMobSpawnGroup;

import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

public class MobSpawnGroupHelper
{
	public static List<WeightedMobSpawnGroup> getListFromMinecraftBiome(Biome biome, MobCategory type)
	{
		WeightedList mobList = biome.getMobSettings().getMobs(type);
		List<WeightedMobSpawnGroup> result = new ArrayList<WeightedMobSpawnGroup>();
		for (Object entry : mobList.unwrap())
		{
			if (!(entry instanceof Weighted weighted) || !(weighted.value() instanceof SpawnerData spawner))
			{
				continue;
			}

			// Keep the weight stored in the WeightedList entry and the count range from SpawnerData.
			WeightedMobSpawnGroup wMSG = new WeightedMobSpawnGroup(
					spawner.type().getDescriptionId().replace("entity.minecraft.", ""),
					weighted.weight(),
					spawner.minCount(),
					spawner.maxCount());
			result.add(wMSG);
		}
		return result;
	}
}
