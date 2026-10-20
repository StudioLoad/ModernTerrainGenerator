package com.rodriaum.mtg.paper.api;

import java.util.Optional;

import com.rodriaum.mtg.paper.gen.OTGPaperChunkGen;
import org.bukkit.Location;

import com.rodriaum.mtg.interfaces.IBiomeConfig;

public class OTGPaperAPI
{
	public static IBiomeConfig getOTGBiome(Location location)
	{
		if (location.getWorld().getGenerator() instanceof OTGPaperChunkGen generator) {
			return generator.generator.getCachedBiomeProvider().getBiomeConfig(location.getBlockX(), location.getBlockZ());
		}
		return null;
	}

	public static Optional<IBiomeConfig> getOTGBiomeOptional(Location location)
	{
		return Optional.ofNullable(getOTGBiome(location));
	}
}
