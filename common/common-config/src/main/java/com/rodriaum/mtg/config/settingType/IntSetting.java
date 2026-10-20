package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.helpers.StringHelper;

/**
 * Reads and writes a single integer.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class IntSetting extends Setting<Integer>
{
	private final int defaultValue;
	private final int minValue;
	private final int maxValue;

	IntSetting(String name, int defaultValue, int minValue, int maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Integer getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public Integer read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return StringHelper.readInt(string, minValue, maxValue);
	}

	public Integer getMinValue()
	{
		return minValue;
	}
	
	public Integer getMaxValue()
	{
		return maxValue;
	}
}
