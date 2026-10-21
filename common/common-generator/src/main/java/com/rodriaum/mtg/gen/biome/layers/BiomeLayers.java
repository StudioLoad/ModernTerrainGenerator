package com.rodriaum.mtg.gen.biome.layers;

import java.util.List;
import java.util.function.LongFunction;

import com.rodriaum.mtg.constants.SettingsEnums.BiomeMode;
import com.rodriaum.mtg.constants.SettingsEnums.ImageMode;
import com.rodriaum.mtg.gen.biome.BiomeData;
import com.rodriaum.mtg.gen.biome.layers.util.CachingLayerContext;
import com.rodriaum.mtg.gen.biome.layers.util.CachingLayerSampler;
import com.rodriaum.mtg.gen.biome.layers.util.LayerFactory;
import com.rodriaum.mtg.gen.biome.layers.util.LayerSampleContext;
import com.rodriaum.mtg.interfaces.ILayerSampler;
import com.rodriaum.mtg.interfaces.ILogger;

/**
 * Holds the factory and utils needed for OTG's biome layers to work.
 */
public class BiomeLayers
{
	// Bit masks for biome generation
	// TODO: Using columns atm, will need to be changed for 3D biomes.

	// The land bit marks whether a column is land or not. This is used to place biomes.
	protected static final int LAND_BIT = (1 << 31);
	// The ice bit marks which land columns should use the ice biome group.
	protected static final int ICE_BIT = (1 << 30);
	// The island bit marks isle biomes spawned inside other biomes.
	protected static final int ISLAND_BIT = (1 << 29); // TODO: Do we really need this?

	// River bits mark whether there is a river in this column.
	private static final int RIVER_SHIFT = 27;
	protected static final int RIVER_BITS = (3 << RIVER_SHIFT);
	protected static final int RIVER_BIT_ONE = (1 << RIVER_SHIFT);
	protected static final int RIVER_BIT_TWO = (1 << (RIVER_SHIFT + 1));

	// Group bits store the id of the biome group used for a column.
	protected static final int GROUP_SHIFT = 20;
	protected static final int GROUP_BITS = (127 << GROUP_SHIFT); // Max 127 biome groups, 7 bits

	// Biome bits store the id of the biome used for a column.
	protected static final int BIOME_BITS = (1 << GROUP_SHIFT) - 1;

	static boolean isLand(int sample)
	{
		return (sample & LAND_BIT) != 0;
	}

	static int getGroupId(int sample)
	{
		return (sample & GROUP_BITS) >> GROUP_SHIFT;
	}

	/**
	 * Checks for land and when present returns biome data, otherwise returns default ocean.
	 */
	static int getBiomeFromLayer(int sample)
	{
		return (sample & BiomeLayers.LAND_BIT) != 0 ? (sample & BiomeLayers.BIOME_BITS) : 0;
	}

	private static <T extends ILayerSampler, C extends LayerSampleContext<T>> LayerFactory<T> build(BiomeLayerData data, LongFunction<C> contextProvider, ILogger logger)
	{
		LayerFactory<T> factory = new InitializationLayer().create(contextProvider.apply(1L));
		LayerFactory<T> riverFactory = data.riversEnabled && data.randomRivers
			? new InitializationLayer().create(contextProvider.apply(1L))
			: null;
		LayerFactory<T> oceanTemperatureFactory = null;
		boolean riversStarted = false;
		boolean oceanTemperatureStarted = false;

		if(data.biomeMode != BiomeMode.FromImage || data.imageMode == ImageMode.ContinueNormal)
		{
			for (int depth = 0; depth <= data.generationDepth; depth++)
			{
				factory = new ScaleLayer().create(contextProvider.apply(2000L + depth), factory);

				if (data.riversEnabled && data.randomRivers && riversStarted)
				{
					riverFactory = new ScaleLayer().create(contextProvider.apply(2000L + depth), riverFactory);
				}

				if (oceanTemperatureStarted)
				{
					oceanTemperatureFactory = new ScaleLayer().create(contextProvider.apply(2000L + depth), oceanTemperatureFactory);
				}

				if (depth == data.landSize && data.landRarity > 0)
				{
					factory = new LandLayer(data.landRarity, data.forceLandAtSpawn, data.oldLandRarity).create(contextProvider.apply(1L), factory);
					factory = new FuzzyScaleLayer().create(contextProvider.apply(2000L), factory);
				}

				if (data.oceanEnabled && depth == data.oceanBiomeSize)
				{
					oceanTemperatureFactory = new OceanTemperatureLayer(data).create(contextProvider.apply(3L));
					oceanTemperatureStarted = true;
				}

				if (depth < (data.landSize + data.landFuzzy))
				{
					factory = new AddIslandsLayer().create(contextProvider.apply(depth), factory);
				}

				if(data.biomeMode == BiomeMode.Normal || data.biomeMode == BiomeMode.FromImage)
				{
					if (data.groups.containsKey(depth))
					{
						factory = new BiomeGroupLayer(data, depth).create(contextProvider.apply(depth), factory);
					}

					if (data.biomeDepths.contains(depth))
					{
						factory = new BiomeLayer(data, depth).create(contextProvider.apply(depth), factory);
					}

					if (depth == 3)
					{
						factory = new IceLayer(data).create(contextProvider.apply(depth), factory);
					}
				}
				else if(data.biomeMode == BiomeMode.NoGroups)
				{
					if (data.biomeDepths.contains(depth))
					{
						factory = new BeforeGroupsLayer(data, depth).create(contextProvider.apply(depth), factory);
					}
					NewBiomeGroup iceGroup = data.groupRegistry.get(2);
					if (iceGroup != null)
					{
						factory = new IceLayer(data).create(contextProvider.apply(depth), factory);
					}
				}

				// Only build river layers when river generation is enabled.
				if (data.riversEnabled && data.riverDepth == depth)
				{
					if (data.randomRivers)
					{
						riverFactory = new RiverInitLayer().create(contextProvider.apply(depth), riverFactory);
						riversStarted = true;
					}
					else
					{
						factory = new RiverInitLayer().create(contextProvider.apply(depth), factory);
					}
				}

				if (data.riversEnabled && (data.generationDepth - data.riverSize) == depth)
				{
					if (data.randomRivers)
					{
						riverFactory = new RiverLayer().create(contextProvider.apply(5 + depth), riverFactory);
					}
					else
					{
						factory = new RiverLayer().create(contextProvider.apply(5 + depth), factory);
					}
				}

				List<BiomeData> isleBiomes = data.isleBiomesAtDepth.get(depth);
				if(isleBiomes != null && !isleBiomes.isEmpty())
				{
					BiomeIsleLayer.IslesList islesAtCurrentDepth = new BiomeIsleLayer.IslesList();
					for (BiomeData biome : isleBiomes)
					{
						boolean[] biomeCanSpawnIn = new boolean[1024];
						boolean inOcean = false;
						for (int islandInBiome : biome.isleInBiomes)
						{
							if (data.oceanEnabled && islandInBiome == data.oceanBiomeData.id)
							{
								inOcean = true;
							}
							else
							{
								biomeCanSpawnIn[islandInBiome] = true;
							}
						}
						int chance = (data.biomeRarityScale + 1) - biome.rarity;
						islesAtCurrentDepth.addIsle(biome.id, chance, biomeCanSpawnIn, inOcean);
					}
					factory = new BiomeIsleLayer(islesAtCurrentDepth).create(contextProvider.apply(depth), factory);
				}

				List<BiomeData> borderBiomes = data.borderBiomesAtDepth.get(depth);
				if(borderBiomes != null && !borderBiomes.isEmpty())
				{
					BiomeBorderLayer.BordersList bordersAtCurrentDepth = new BiomeBorderLayer.BordersList();
					for (BiomeData biome : borderBiomes)
					{
						for(int targetBiomeId : biome.borderInBiomes)
						{
							bordersAtCurrentDepth.addBorder(biome.id, targetBiomeId, biome.onlyBorderNearBiomes, biome.notBorderNearBiomes);
						}
					}
					factory = new BiomeBorderLayer(bordersAtCurrentDepth).create(contextProvider.apply(depth), factory);
				}
			}

			if (data.oceanEnabled)
			{
				factory = new MergeOceanTemperatureLayer().create(contextProvider.apply(1L), factory, oceanTemperatureFactory);
			}

			if (data.riversEnabled)
			{
				if (data.randomRivers)
				{
					factory = new FinalizeWithRiverLayer(true, data.riverBiomes).create(contextProvider.apply(1L), factory, riverFactory);
				}
				else
				{
					factory = new FinalizeLayer(true, data.riverBiomes).create(contextProvider.apply(1L), factory);
				}
			}
			else
			{
				factory = new FinalizeLayer(false, data.riverBiomes).create(contextProvider.apply(1L), factory);
			}
		}

		if(data.biomeMode == BiomeMode.FromImage)
		{
			factory = new FromImageLayer(data, logger).create(contextProvider.apply(0), factory);
		}

		return factory;
	}

	// Create a sampler that can get a biome at a position
	public static CachingLayerSampler create(long seed, BiomeLayerData data, ILogger logger)
	{
		LayerFactory<CachingLayerSampler> factory = build(data, salt -> new CachingLayerContext(25, seed, salt), logger);
		return factory.make();
	}
}
