package com.rodriaum.mtg.gen.surface;

import com.rodriaum.mtg.config.settingType.Setting;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.helpers.StringHelper;

/**
 * Setting that handles the {@link SurfaceGenerator}.
 *
 */
public class SurfaceGeneratorSetting extends Setting<SurfaceGenerator>
{
	public static final Setting<SurfaceGenerator> SURFACE_AND_GROUND_CONTROL = surfaceGeneratorSetting("SurfaceAndGroundControl");	

	private SurfaceGeneratorSetting(String name)
	{
		super(name);
	}

	@Override
	public SurfaceGenerator getDefaultValue(IMaterialReader materialReader)
	{
		return new SimpleSurfaceGenerator();
	}

	@Override
	public SurfaceGenerator read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		if (string.length() > 0)
		{
			SurfaceGenerator mesa = MesaSurfaceGenerator.getFor(string);
			if (mesa != null)
			{
				return mesa;
			}

			SurfaceGenerator iceberg = IcebergSurfaceGenerator.getFor(string, materialReader);
			if (iceberg != null)
			{
				return iceberg;
			}

			String[] parts = StringHelper.readCommaSeperatedString(string);
			return new MultipleLayersSurfaceGenerator(parts, materialReader);
		}
		return new SimpleSurfaceGenerator();
	}
	
	/**
	 * Creates a setting that represents a {@link SurfaceGenerator}.
	 * @param name Name of the setting.
	 * @return The newly created setting.
	 */
	private static final Setting<SurfaceGenerator> surfaceGeneratorSetting(String name)
	{
		return new SurfaceGeneratorSetting(name);
	}
}
