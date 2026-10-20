package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.helpers.StringHelper;

/**
 * Reads and writes a single float number.
 *
 * <p>Numbers are limited to the given min and max values.
 */
class FloatSetting extends Setting<Float>
{
	private final float defaultValue;
	private final float minValue;
	private final float maxValue;

	FloatSetting(String name, float defaultValue, float minValue, float maxValue)
	{
		super(name);
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Float getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public Float read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return (float) StringHelper.readDouble(string, minValue, maxValue);
	}

	public Float getMinValue()
	{
		return minValue;
	}
	
	public Float getMaxValue()
	{
		return maxValue;
	}	  
}
