package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.biome.WeightedMobSpawnGroup;

import java.util.Collections;
import java.util.List;

/**
 * Reads and writes a list of mobs. Mobs are read using
 * {@link WeightedMobSpawnGroup#fromJson(String)} and written using
 * {@link WeightedMobSpawnGroup#toJson(List)}.
 *
 */
class MobGroupListSetting extends Setting<List<WeightedMobSpawnGroup>>
{

	MobGroupListSetting(String name)
	{
		super(name);
	}

	@Override
	public List<WeightedMobSpawnGroup> getDefaultValue(IMaterialReader materialReader)
	{
		return Collections.emptyList();
	}

	@Override
	public List<WeightedMobSpawnGroup> read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return WeightedMobSpawnGroup.fromJson(string);
	}

	@Override
	public String write(List<WeightedMobSpawnGroup> groups)
	{
		return WeightedMobSpawnGroup.toJson(groups);
	}

}
