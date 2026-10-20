package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.bo3.Rotation;

/**
 * Reads and writes a single integer.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class RotationSetting extends Setting<Rotation>
{
	private final Rotation defaultValue;

	RotationSetting(String name, Rotation defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	@Override
	public Rotation getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public Rotation read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return
			string == null || string.length() == 0 ? defaultValue :
			string.toUpperCase().trim().equals("NORTH") ? Rotation.NORTH :
			string.toUpperCase().trim().equals("EAST") ? Rotation.EAST :
			string.toUpperCase().trim().equals("SOUTH") ? Rotation.SOUTH :
			string.toUpperCase().trim().equals("WEST") ? Rotation.WEST :
			defaultValue;
	}
}
