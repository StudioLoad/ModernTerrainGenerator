package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IWorldConfig;
import com.rodriaum.mtg.util.biome.ReplaceBlockMatrix;

/**
 * Setting that handles {@link ReplaceBlockMatrix}.
 *
 */
class ReplacedBlocksSetting extends BiomeSetting<ReplaceBlockMatrix>
{

	ReplacedBlocksSetting(String name)
	{
		super(name);
	}

	@Override
	public ReplaceBlockMatrix read(String string, IMaterialReader materialReader, IWorldConfig worldConfig) throws InvalidConfigException {
		return new ReplaceBlockMatrix(string, materialReader, worldConfig.getWorldMinY(), worldConfig.getWorldMaxY());
	}

	@Override
	public ReplaceBlockMatrix getDefaultValue(IMaterialReader materialReader)
	{
		return ReplaceBlockMatrix.createEmptyMatrix(Constants.WORLD_DEFAULT_MIN_Y, Constants.WORLD_DEFAULT_MAX_Y, materialReader);
	}

	// Should probably not be used, but keeping it just in case.
	@Override
	public ReplaceBlockMatrix read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return new ReplaceBlockMatrix(string, materialReader, Constants.WORLD_DEFAULT_MIN_Y, Constants.WORLD_DEFAULT_MAX_Y);
	}
}
