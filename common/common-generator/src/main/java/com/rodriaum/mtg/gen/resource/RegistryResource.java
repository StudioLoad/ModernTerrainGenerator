package com.rodriaum.mtg.gen.resource;

import java.util.List;

import com.rodriaum.mtg.config.biome.BiomeResourceBase;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;

public class RegistryResource  extends BiomeResourceBase
{
	private final String registryKey;
	private final String decorationStage;

	public RegistryResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(1, args);

		this.registryKey = args.get(0);
		if(args.size() > 1)
		{
			this.decorationStage = args.get(1);
		} else {
			this.decorationStage = "VEGETAL_DECORATION";
		}
	}

	public String getDecorationStage()
	{
		return this.decorationStage;
	}

	public String getFeatureKey()
	{
		return this.registryKey;
	}

	@Override
	public String toString()
	{
		return "Registry(" + this.registryKey + "," + this.decorationStage + ")";
	}
}
