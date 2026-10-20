package com.rodriaum.mtg.util.gen;

import com.rodriaum.mtg.interfaces.ICachedBiomeProvider;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IPluginConfig;
import com.rodriaum.mtg.interfaces.IWorldConfig;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.util.ChunkCoordinate;

// TODO: Split up worldgenregion into separate classes, one for decoration/worldgen, one for non-worldgen.
public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	protected final String presetFolderName;
	private final IPluginConfig pluginConfig;
	private final IWorldConfig worldConfig;
	protected final ILogger logger;
	protected final DecorationBiomeCache decorationBiomeCache;
	protected final DecorationArea decorationArea;
    protected int minY;
	protected int maxY;

    /** Creates a LocalWorldGenRegion to be used during chunk decoration */
	protected LocalWorldGenRegion(String presetFolderName, IPluginConfig pluginConfig, IWorldConfig worldConfig, ILogger logger, int worldRegionCenterX, int worldRegionCenterZ, ICachedBiomeProvider cachedBiomeProvider)
	{
		this.presetFolderName = presetFolderName;
		this.pluginConfig = pluginConfig;
		this.worldConfig = worldConfig;
		this.logger = logger;
		this.decorationArea = new DecorationArea(ChunkCoordinate.fromChunkCoords(worldRegionCenterX, worldRegionCenterZ));
		this.decorationBiomeCache = new DecorationBiomeCache(worldRegionCenterX, worldRegionCenterZ, cachedBiomeProvider);		
	}
	
	/** Creates a LocalWorldGenRegion to be used outside of world generation. */	
	protected LocalWorldGenRegion(String presetFolderName, IPluginConfig pluginConfig, IWorldConfig worldConfig, ILogger logger)
	{
		this.presetFolderName = presetFolderName;
		this.pluginConfig = pluginConfig;
		this.worldConfig = worldConfig;
		this.logger = logger;
		// TOOD: Don't use this.decorationArea == null for worldgenregions
		// doing things outside of population, split up worldgenregion
		// into separate classes, one for decoration, one for non-decoration.
		this.decorationBiomeCache = null;
		this.decorationArea = null;
	}
	
	@Override
	public IPluginConfig getPluginConfig()
	{
		return this.pluginConfig;
	}	
	
	@Override
	public IWorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}
	
	@Override
	public String getPresetFolderName()
	{
		return this.presetFolderName;
	}
	
	@Override
	public DecorationArea getDecorationArea()
	{
		return this.decorationArea;
	}

	@Override
	public int getWorldMaxY() {
		return this.maxY;
	}

	@Override
	public int getWorldMinY() {
		return this.minY;
	}
}
