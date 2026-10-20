package com.rodriaum.mtg.config.settingType;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.helpers.StringHelper;
import com.rodriaum.mtg.util.materials.MaterialSet;

/**
 * Reads and writes a set of materials, used for matching.
 *
 * <p>Materials are separated using a comma and, optionally, whitespace. Each
 * material is stripped from its whitespace and read using
 * {@link MaterialSet#parseAndAdd(String)}.
 *
 */
class MaterialSetSetting extends Setting<MaterialSet>
{
	private final String[] defaultValues;

	public MaterialSetSetting(String name, String... defaultValues)
	{
		super(name);
		this.defaultValues = defaultValues;
	}

	@Override
	public MaterialSet getDefaultValue(IMaterialReader materialReader)
	{
		try
		{
			MaterialSet blocks = new MaterialSet();
			for (String blockName : defaultValues)
			{
				blocks.parseAndAdd(blockName, materialReader);
			}
			return blocks;
		} catch (InvalidConfigException e)
		{
			throw new AssertionError(e);
		}
	}

	@Override
	public MaterialSet read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		MaterialSet blocks = new MaterialSet();

		for (String blockName : StringHelper.readCommaSeperatedString(string))
		{
			blocks.parseAndAdd(blockName, materialReader);
		}

		return blocks;
	}
}
