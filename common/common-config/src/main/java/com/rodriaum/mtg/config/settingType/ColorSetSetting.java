package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.biome.ColorSet;
import com.rodriaum.mtg.util.biome.SimpleColorSet;
import com.rodriaum.mtg.util.helpers.StringHelper;

public class ColorSetSetting extends Setting<ColorSet>
{

	protected ColorSetSetting(String name)
	{
		super(name);
	}

	@Override
	public ColorSet getDefaultValue(IMaterialReader materialReader)
	{
		return new ColorSet();
	}

	@Override
	public ColorSet read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return new SimpleColorSet(StringHelper.readCommaSeperatedString(string), materialReader);
	}

}
