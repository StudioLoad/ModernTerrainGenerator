package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;

/**
 * Reads and writes a string. Surrounding whitespace is stripped using
 * {@link String#trim()}.
 *
 */
class StringSetting extends Setting<String>
{
	private final String defaultValue;

	StringSetting(String name, String defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDefaultValue(IMaterialReader materialReader)
	{
		return defaultValue;
	}

	@Override
	public String read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return string.trim();
	}
}
