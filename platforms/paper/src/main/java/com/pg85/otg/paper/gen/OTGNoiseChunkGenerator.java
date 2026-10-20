package com.pg85.otg.paper.gen;

import net.minecraft.core.registries.Registries;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;


import com.pg85.otg.paper.util.ObfuscationHelper;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.minecraft.StructureType;
import net.minecraft.SharedConstants;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.gen.OTGChunkDecorator;
import com.pg85.otg.core.gen.OTGChunkGenerator;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILayerSource;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.paper.biome.PaperBiome;
import com.pg85.otg.paper.biome.OTGBiomeProvider;
import com.pg85.otg.paper.presets.PaperPresetLoader;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.materials.LocalMaterialData;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.LevelResource;

public class OTGNoiseChunkGenerator extends ChunkGenerator
{	
	// Create a codec to serialise/deserialise OTGNoiseChunkGenerator
	public static final com.mojang.serialization.MapCodec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
		(p_236091_0_) -> p_236091_0_
			.group(
				Codec.STRING.fieldOf("preset_folder_name").forGetter(p -> p.getPreset().getFolderName()),
				BiomeSource.CODEC.fieldOf("biome_source").forGetter(p -> p.biomeSource),
				RegistryOps.retrieveGetter(Registries.STRUCTURE_SET),
				RegistryOps.retrieveGetter(Registries.NOISE),
				Codec.LONG.fieldOf("seed").stable().forGetter(p -> p.worldSeed),
				NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(p -> p.generatorSettingsHolder)
			).apply(
				p_236091_0_,
				p_236091_0_.stable(OTGNoiseChunkGenerator::new)
			)
	);

	private final HolderGetter<StructureSet> structureSets;
	private final Holder<NoiseGeneratorSettings> generatorSettingsHolder;
	private final long worldSeed;
	private final int noiseHeight;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;

	private final ShadowChunkGenerator shadowChunkGenerator;
	public final OTGChunkGenerator internalGenerator;
	private final OTGChunkDecorator chunkDecorator;
	private final Preset preset;
	private final NoiseRouter router;
	private final HashMap<StructureType, StructurePlacement> structurePlacementSettings;
	//protected final WorldgenRandom random;

	// TODO: Move this to WorldLoader when ready?
	private CustomStructureCache structureCache;

	private final Climate.Sampler sampler;
	private final HolderGetter<NormalNoise.NoiseParameters> noises;
	private ChunkCoordinate fixBiomesForChunk;

	public OTGNoiseChunkGenerator (BiomeSource biomeSource, long seed, HolderGetter<StructureSet> structureSetRegistry, HolderGetter<NormalNoise.NoiseParameters> noiseRegistry, Holder<NoiseGeneratorSettings> generatorSettings)
	{
		this("default", biomeSource, structureSetRegistry, noiseRegistry, seed, generatorSettings);
	}

	public OTGNoiseChunkGenerator (String presetName, BiomeSource biomeSource, HolderGetter<StructureSet> structureSetRegistry, HolderGetter<NormalNoise.NoiseParameters> noiseRegistry, long seed, Holder<NoiseGeneratorSettings> generatorSettings)
	{
		this(presetName, biomeSource, biomeSource, structureSetRegistry, noiseRegistry, seed, generatorSettings);
	}

	// Vanilla has two biome sources, where the first is population and the second is runtime. Don't know the practical difference this makes.
	private OTGNoiseChunkGenerator (String presetFolderName, BiomeSource populationSource, BiomeSource runtimeSource, HolderGetter<StructureSet> structureSetRegistry, HolderGetter<NormalNoise.NoiseParameters> noiseRegistry, long worldSeed, Holder<NoiseGeneratorSettings> generatorSettings)
	{
		super(runtimeSource);
		this.structureSets = structureSetRegistry;
		if (!(populationSource instanceof ILayerSource)) {
			throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
		}

		this.worldSeed = worldSeed;
		NoiseGeneratorSettings genSettings = generatorSettings.value();
		this.generatorSettingsHolder = generatorSettings;
		NoiseSettings noisesettings = genSettings.noiseSettings();
		this.noiseHeight = noisesettings.height();
		this.noises = noiseRegistry;
		this.defaultBlock = genSettings.defaultBlock();
		this.defaultFluid = genSettings.defaultFluid();

		this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
		this.structurePlacementSettings = getStructurePlacementMap(preset.getWorldConfig());
		this.internalGenerator = new OTGChunkGenerator(this.preset, worldSeed, (ILayerSource) populationSource, ((PaperPresetLoader) OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
		this.shadowChunkGenerator = new ShadowChunkGenerator(internalGenerator.getMinY(), internalGenerator.getMaxY());
		this.chunkDecorator = new OTGChunkDecorator();

		this.router = genSettings.noiseRouter();
		this.sampler = new Climate.Sampler(this.router.temperature(), this.router.vegetation(), this.router.continents(),
				this.router.erosion(), this.router.depth(), this.router.ridges(), genSettings.spawnTarget());
	}


	private static HashMap<StructureType, StructurePlacement> getStructurePlacementMap(IWorldConfig worldConfig)
	{
		HashMap<StructureType, StructurePlacement> placementSettings =  new HashMap<>();
		placementSettings.put(StructureType.SWAMP_HUT, new RandomSpreadStructurePlacement(worldConfig.getSwampHutSpacing(), worldConfig.getSwampHutSeparation(), RandomSpreadType.LINEAR, 14357620));
		placementSettings.put(StructureType.IGLOO, new RandomSpreadStructurePlacement(worldConfig.getIglooSpacing(), worldConfig.getIglooSeparation(), RandomSpreadType.LINEAR, 14357618));
		placementSettings.put(StructureType.DESERT_PYRAMID, new RandomSpreadStructurePlacement(worldConfig.getDesertPyramidSpacing(), worldConfig.getDesertPyramidSeparation(), RandomSpreadType.LINEAR, 14357617));
		placementSettings.put(StructureType.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(worldConfig.getJungleTempleSpacing(), worldConfig.getJungleTempleSeparation(), RandomSpreadType.LINEAR, 14357619));
		placementSettings.put(StructureType.VILLAGE, new RandomSpreadStructurePlacement(worldConfig.getVillageSpacing(), worldConfig.getVillageSeparation(), RandomSpreadType.LINEAR, 10387312));
		placementSettings.put(StructureType.PILLLAGER_OUTPOST, new RandomSpreadStructurePlacement(worldConfig.getPillagerOutpostSpacing(), worldConfig.getPillagerOutpostSeparation(), RandomSpreadType.LINEAR, 165745296));
		placementSettings.put(StructureType.STRONGHOLD, new ConcentricRingsStructurePlacement(worldConfig.getStrongHoldDistance(), worldConfig.getStrongHoldSpread(), worldConfig.getStrongHoldCount(), HolderSet.direct()));
		placementSettings.put(StructureType.OCEAN_MONUMENT, new RandomSpreadStructurePlacement(worldConfig.getOceanMonumentSpacing(), worldConfig.getOceanMonumentSeparation(), RandomSpreadType.TRIANGULAR, 10387313));
		placementSettings.put(StructureType.END_CITY, new RandomSpreadStructurePlacement(worldConfig.getEndCitySpacing(), worldConfig.getEndCitySeparation(), RandomSpreadType.TRIANGULAR, 10387313));
		placementSettings.put(StructureType.WOODLAND_MANSION, new RandomSpreadStructurePlacement(worldConfig.getWoodlandMansionSpacing(), worldConfig.getWoodlandMansionSeparation(), RandomSpreadType.TRIANGULAR, 10387319));
		placementSettings.put(StructureType.BURIED_TREASURE, new RandomSpreadStructurePlacement(
				new Vec3i(9, 0, 9), StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 0,
				Optional.empty(), worldConfig.getBuriedTreasureSpacing(), worldConfig.getBuriedTreasureSeparation(), RandomSpreadType.LINEAR));
		placementSettings.put(StructureType.MINESHAFT, new RandomSpreadStructurePlacement(worldConfig.getMineshaftSpacing(), worldConfig.getMineshaftSeparation(), RandomSpreadType.LINEAR, 0));
		placementSettings.put(StructureType.RUINED_PORTAL, new RandomSpreadStructurePlacement(worldConfig.getRuinedPortalSpacing(), worldConfig.getRuinedPortalSeparation(), RandomSpreadType.LINEAR, 34222645));
		placementSettings.put(StructureType.SHIPWRECK, new RandomSpreadStructurePlacement(worldConfig.getShipwreckSpacing(), worldConfig.getShipwreckSeparation(), RandomSpreadType.LINEAR, 165745295));
		placementSettings.put(StructureType.OCEAN_RUINS, new RandomSpreadStructurePlacement(worldConfig.getOceanRuinSpacing(), worldConfig.getOceanRuinSeparation(), RandomSpreadType.LINEAR, 14357621));
		placementSettings.put(StructureType.BASTION_REMNANT, new RandomSpreadStructurePlacement(worldConfig.getBastionRemnantSpacing(), worldConfig.getBastionRemnantSeparation(), RandomSpreadType.LINEAR, 30084232));
		placementSettings.put(StructureType.FORTRESS, new RandomSpreadStructurePlacement(worldConfig.getNetherFortressSpacing(), worldConfig.getNetherFortressSeparation(), RandomSpreadType.LINEAR, 30084232));
		placementSettings.put(StructureType.NETHER_FOSSIL, new RandomSpreadStructurePlacement(worldConfig.getNetherFossilSpacing(), worldConfig.getNetherFossilSeparation(), RandomSpreadType.LINEAR, 14357921));
		return placementSettings;
	}

	
	public ICachedBiomeProvider getCachedBiomeProvider()
	{
		return this.internalGenerator.getCachedBiomeProvider();
	}

	// TODO: This should be called in an onSave method somewhere
	public void saveStructureCache()
	{
		if (this.chunkDecorator.getIsSaveRequired() && this.structureCache != null)
		{
			this.structureCache.saveToDisk(OTG.getEngine().getLogger(), this.chunkDecorator);
		}
	}

	// Base terrain gen

	// Generates the base terrain for a chunk. Spigot compatible.
	public void buildNoiseSpigot (ServerLevel world, org.bukkit.generator.ChunkGenerator.ChunkData chunk, ChunkCoordinate chunkCoord, Random random)
	{
		ChunkBuffer buffer = new PaperChunkBuffer(chunk, chunkCoord, this.internalGenerator.getMinY());
		ChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
		} else {
			// Setup jigsaw data
			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);

			ChunkPos pos = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());
			findNoiseStructures(pos, world.structureManager(), structures, junctions, this.internalGenerator.getMinY());

			this.internalGenerator.populateNoise(random, buffer, buffer.getChunkCoordinate(), structures, junctions);
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);			
		}
	}

	public static void findNoiseStructures(ChunkPos pos, StructureManager manager, ObjectList<JigsawStructureData> structures, ObjectList<JigsawStructureData> junctions, int minY) {

		int chunkX = pos.x;
		int chunkZ = pos.z;
		int startX = chunkX << 4;
		int startZ = chunkZ << 4;

		// Structure starts can begin in neighbouring chunks but still affect this chunk's terrain.
		for (int startChunkX = chunkX - 1; startChunkX <= chunkX + 1; startChunkX++) {
			for (int startChunkZ = chunkZ - 1; startChunkZ <= chunkZ + 1; startChunkZ++) {
				ChunkPos startChunkPos = new ChunkPos(startChunkX, startChunkZ);
				for (StructureStart start : manager.startsForStructure(startChunkX, startChunkZ, structure -> structure instanceof JigsawStructure)) {
					for (StructurePiece piece : start.getPieces()) {
						if (!piece.isCloseToChunk(pos, 12)) {
							continue;
						}
						BoundingBox box = piece.getBoundingBox();
						if (piece instanceof PoolElementStructurePiece villagePiece) {
							if (villagePiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
								structures.add(new JigsawStructureData(box.minX(), box.minY() - minY, box.minZ(), box.maxX(), villagePiece.getGroundLevelDelta(), box.maxZ(), true, 0, 0, 0));
							}
							for (JigsawJunction junction : villagePiece.getJunctions()) {
								int sourceX = junction.getSourceX();
								int sourceZ = junction.getSourceZ();
								if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 27 && sourceZ < startZ + 27) {
									junctions.add(new JigsawStructureData(0, 0, 0, 0, 0, 0, false, sourceX, junction.getSourceGroundY() - minY, sourceZ));
								}
							}
						} else {
							structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(), box.maxX(), 0, box.maxZ(), false, 0, 0, 0));
						}
					}
				}
			}
		}
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager accessor, ChunkAccess chunk)
	{

		// If we've already generated and cached this
		// chunk while it was unloaded, use cached data.
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);

		// Dummy random, as we can't get the level random right now
		Random random = new Random(this.worldSeed ^ ((long) chunkCoord.getChunkX() * 341873128712L) ^ ((long) chunkCoord.getChunkZ() * 132897987541L));

		// When generating the spawn area, Spigot will get the structure and biome info for the first chunk before we can inject
		// Therefore, we need to re-do these calls now, for that one chunk
//		if (fixBiomesForChunk != null && fixBiomesForChunk.equals(chunkCoord))
//		{

			// Should only run when first creating the world, on a single chunk
			// TODO: we need a ServerLevel or similar for this
			//this.createStructures(world.getMinecraftWorld().registryAccess(), world.getMinecraftWorld().structureFeatureManager(), chunk, world.getMinecraftWorld().getStructureManager(), worldSeed);
//			this.createBiomes(chunk.biomeRegistry, executor, blender, accessor, chunk);

		//	fixBiomesForChunk = null;
		//}
		ChunkBuffer buffer = new PaperChunkBuffer(chunk, this.internalGenerator.getMinY());
		ChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
		if (cachedChunk != null)
		{
			this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
		} else {
			// Setup jigsaw data
			ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
			ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);

			ChunkPos pos = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());

			findNoiseStructures(pos, accessor, structures, junctions, this.internalGenerator.getMinY());

			this.internalGenerator.populateNoise(random, buffer, buffer.getChunkCoordinate(), structures, junctions);
			this.shadowChunkGenerator.setChunkGenerated(chunkCoord);
		}

		return CompletableFuture.completedFuture(chunk);
	}

	// Generates the base terrain for a chunk.


	// Replaces surface and ground blocks in base terrain and places bedrock.
	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structures, RandomState randomState, ChunkAccess chunk)
	{
		// OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
		// with TemplateForBiome, we want to use registered surfacebuilders though.
	}

	// Carvers: Caves and ravines

	@Override
	public void applyCarvers(WorldGenRegion chunkRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureAccess, ChunkAccess chunk)
	{
		ProtoChunk protoChunk = (ProtoChunk) chunk;
		CarvingMask carvingMaskRaw = protoChunk.getOrCreateCarvingMask();
		try {
			Field theRealMask = ObfuscationHelper.getField(CarvingMask.class, "mask", "b");
			theRealMask.setAccessible(true);
			BitSet carvingMask = (BitSet) theRealMask.get(carvingMaskRaw);
			ChunkBuffer chunkBuffer = new PaperChunkBuffer(protoChunk);
			this.internalGenerator.carve(chunkBuffer, seed, protoChunk.getPos().x, protoChunk.getPos().z, carvingMask, true, true);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			if (OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MAIN)) {
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Failed to access the carving mask; caves may not generate. Exception: " + e.getMessage());
			}
		}
	}
	// Population / decoration

	@Override
	public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager)
	{
		if (!OTG.getEngine().getPluginConfig().getDecorationEnabled()) {
			return;
		}

		ChunkPos chunkPos = chunkAccess.getPos();
		if (SharedConstants.debugVoidTerrain(chunkPos)) {
			return;
		}

		WorldGenRegion worldGenRegion = (WorldGenRegion) worldGenLevel;
		int worldX = worldGenRegion.getCenter().x * Constants.CHUNK_SIZE;
		int worldZ = worldGenRegion.getCenter().z * Constants.CHUNK_SIZE;
		ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
		IBiome noiseBiome = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome(
				(worldGenRegion.getCenter().x << 2) + 2,
				(worldGenRegion.getCenter().z << 2) + 2);
		PaperWorldGenRegion otgWorldGenRegion = new PaperWorldGenRegion(
				this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
		Path worldSaveFolder = worldGenRegion.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).getParent();

		try {
			this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, otgWorldGenRegion,
					noiseBiome.getBiomeConfig(), getStructureCache(worldSaveFolder));
		} catch (Exception exception) {
			CrashReport crashReport = CrashReport.forThrowable(exception, "OTG biome decoration");
			crashReport.addCategory("Generation")
					.setDetail("CenterX", worldX)
					.setDetail("CenterZ", worldZ)
					.setDetail("Seed", worldGenRegion.getSeed());
			throw new ReportedException(crashReport);
		}

		// Let the current Minecraft version place the biome's registered features and structures.
		super.applyBiomeDecoration(worldGenLevel, chunkAccess, structureManager);
	}
	private static BoundingBox getWritableArea(ChunkAccess p_187718_)
	{
		ChunkPos chunkpos = p_187718_.getPos();
		int i = chunkpos.getMinBlockX();
		int j = chunkpos.getMinBlockZ();
		LevelHeightAccessor levelheightaccessor = p_187718_.getHeightAccessorForGeneration();
		int k = levelheightaccessor.getMinBuildHeight() + 1;
		int l = levelheightaccessor.getMaxBuildHeight() - 1;
		return new BoundingBox(i, k, j, i + 15, l, j + 15);
	}

	// Mob spawning on initial chunk spawn (animals).
	@SuppressWarnings("deprecation")
	@Override
	public void spawnOriginalMobs(WorldGenRegion region)
	{
		if (!this.generatorSettingsHolder.value().disableMobGeneration()) {
			int chunkX = region.getCenter().x;
			int chunkZ = region.getCenter().z;
			IBiome biome = this.internalGenerator.getCachedBiomeProvider().getBiome(chunkX * Constants.CHUNK_SIZE, chunkZ * Constants.CHUNK_SIZE);
			WorldgenRandom sharedseedrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
			sharedseedrandom.setDecorationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
			NaturalSpawner.spawnMobsForChunkGeneration(region, ((PaperBiome)biome).getHolder(), region.getCenter(), sharedseedrandom);
			}
		}
	// Mob spawning on chunk tick
	@Override
	public WeightedList getMobsAt(Holder<Biome> biome, StructureManager structureManager, MobCategory entityClassification, BlockPos blockPos)
	{
		return super.getMobsAt(biome, structureManager, entityClassification, blockPos);
	}

	// Noise

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world, RandomState randomState)
	{
		NoiseSettings noiseSettings = this.generatorSettingsHolder.value().noiseSettings();
		int minGenY = Math.max(noiseSettings.minY(), world.getMinBuildHeight());
		int maxGenY = Math.min(noiseSettings.minY() + noiseSettings.height(), world.getMaxBuildHeight());
		if (maxGenY <= minGenY) {
			return minGenY;
		}
		return this.sampleHeightmap(x, z, null, heightmap.isOpaque(), minGenY, maxGenY);
	}

	// Provides a sample of the full column for structure generation.
	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState randomState)
	{
		NoiseSettings noiseSettings = this.generatorSettingsHolder.value().noiseSettings();
		int minGenY = Math.max(noiseSettings.minY(), world.getMinBuildHeight());
		int maxGenY = Math.min(noiseSettings.minY() + noiseSettings.height(), world.getMaxBuildHeight());
		if (maxGenY <= minGenY) {
			return new NoiseColumn(minGenY, new BlockState[0]);
		}

		BlockState[] blockStates = new BlockState[maxGenY - minGenY];
		Arrays.fill(blockStates, Blocks.AIR.defaultBlockState());
		this.sampleHeightmap(x, z, blockStates, null, minGenY, maxGenY);
		return new NoiseColumn(minGenY, blockStates);
	}

	@Override
	public void addDebugScreenInfo(List<String> text, RandomState randomState, BlockPos pos) {
		// TODO: what does this do? -auth
	}

	// Samples the noise at a column and provides a view of the blockstates, or fills a heightmap.
	// Samples OTG's fixed 8-block noise columns inside the requested world build range.
	private int sampleHeightmap(int x, int z, BlockState[] blockStates,
			Predicate<BlockState> predicate, int minGenY, int maxGenY)
	{
		NoiseSettings noiseSettings = this.generatorSettingsHolder.value().noiseSettings();
		int cellWidth = noiseSettings.getCellWidth();
		int xStart = Math.floorDiv(x, cellWidth);
		int zStart = Math.floorDiv(z, cellWidth);
		int xProgress = Math.floorMod(x, cellWidth);
		int zProgress = Math.floorMod(z, cellWidth);
		double xLerp = (double) xProgress / cellWidth;
		double zLerp = (double) zProgress / cellWidth;

		int noiseSizeY = this.internalGenerator.getNoiseSizeY();
		double[][] noiseData = new double[4][noiseSizeY + 1];
		this.internalGenerator.getNoiseColumn(noiseData[0], xStart, zStart);
		this.internalGenerator.getNoiseColumn(noiseData[1], xStart, zStart + 1);
		this.internalGenerator.getNoiseColumn(noiseData[2], xStart + 1, zStart);
		this.internalGenerator.getNoiseColumn(noiseData[3], xStart + 1, zStart + 1);

		for (int noiseY = noiseSizeY - 1; noiseY >= 0; --noiseY)
		{
			double x0z0y0 = noiseData[0][noiseY];
			double x0z1y0 = noiseData[1][noiseY];
			double x1z0y0 = noiseData[2][noiseY];
			double x1z1y0 = noiseData[3][noiseY];
			double x0z0y1 = noiseData[0][noiseY + 1];
			double x0z1y1 = noiseData[1][noiseY + 1];
			double x1z0y1 = noiseData[2][noiseY + 1];
			double x1z1y1 = noiseData[3][noiseY + 1];

			for (int pieceY = 7; pieceY >= 0; --pieceY)
			{
				int y = this.internalGenerator.getMinY() + (noiseY * 8) + pieceY;
				if (y < minGenY || y >= maxGenY) {
					continue;
				}

				double yLerp = (double) pieceY / 8.0D;
				double density = Mth.lerp3(xLerp, yLerp, zLerp,
						x0z0y0, x1z0y0, x0z0y1, x1z0y1,
						x0z1y0, x1z1y0, x0z1y1, x1z1y1);
				BlockState state = this.getBlockState(density, y);

				if (blockStates != null) {
					blockStates[y - minGenY] = state;
				}
				if (predicate != null && predicate.test(state)) {
					return y + 1;
				}
			}
		}

		return minGenY;
	}

	private BlockState getBlockState(double density, int y)
	{
		if (density > 0.0D)
		{
			return this.defaultBlock;
		}
		else if (y < this.getSeaLevel())
		{
			return this.defaultFluid;
		} else {
			return Blocks.AIR.defaultBlockState();
		}
	}

	// Getters / misc

	public ChunkGenerator withSeed(long seed)
	{
		if (!(this.biomeSource instanceof OTGBiomeProvider otgBiomeProvider))
		{
			throw new IllegalStateException("OTG requires OTGBiomeProvider when reseeding a chunk generator.");
		}
		return new OTGNoiseChunkGenerator(this.preset.getFolderName(), otgBiomeProvider.withSeed(seed), this.structureSets, this.noises, seed, this.generatorSettingsHolder);
	}

	@Override
	protected com.mojang.serialization.MapCodec<? extends ChunkGenerator> codec()
	{
		return CODEC;
	}

	public Climate.Sampler climateSampler() {
		return this.sampler;
	}

	@Override
	public int getGenDepth()
	{
		return this.noiseHeight;
	}

	@Override
	public int getSeaLevel()
	{
		return this.generatorSettingsHolder.value().seaLevel();
	}

	public Preset getPreset()
	{
		return preset;
	}

	public Holder<NoiseGeneratorSettings> getGeneratorSettingsHolder()
	{
		return this.generatorSettingsHolder;
	}

	@Override
	public int getMinY()
	{
		return this.generatorSettingsHolder.value().noiseSettings().minY();
	}

	public CustomStructureCache getStructureCache(Path worldSaveFolder)
	{
		if(this.structureCache == null)
		{
			this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == SettingsEnums.CustomStructureType.BO4);
		}
		return this.structureCache;
	}

	double getBiomeBlocksNoiseValue (int blockX, int blockZ)
	{
		return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
	}

	public void fixBiomes(int chunkX, int chunkZ)
	{
		this.fixBiomesForChunk = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
	}

	// Shadowgen

	public Boolean checkHasVanillaStructureWithoutLoading(ServerLevel world, ChunkCoordinate chunkCoord)
	{
		// This method needs updating to 1.18.2 in the right way. For now, has been replaced by this.checkForVanillaStructure()
		return false;
		//return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, this.biomeSource, this., chunkCoord, this.internalGenerator.getCachedBiomeProvider(), false);
	}

	public int getHighestBlockYInUnloadedChunk(Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, ServerLevel level)
	{
		return this.shadowChunkGenerator.getHighestBlockYInUnloadedChunk(this.internalGenerator, worldRandom, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow, level);
	}

	public LocalMaterialData getMaterialInUnloadedChunk(Random worldRandom, int x, int y, int z, ServerLevel level)
	{
		return this.shadowChunkGenerator.getMaterialInUnloadedChunk(this.internalGenerator, worldRandom, x, y, z, level);
	}

	public PaperChunkBuffer getChunkWithoutLoadingOrCaching(Random random, ChunkCoordinate chunkCoord, ServerLevel level)
	{
		return this.shadowChunkGenerator.getChunkWithoutLoadingOrCaching(this.internalGenerator, random, chunkCoord, level);
	}
	// Uses the vanilla method of checking if there is a vanilla structure in range
	// Might be slower than old solution in ShadowChunkGenerator
	public boolean checkForVanillaStructure(ChunkCoordinate chunkCoordinate)
	{
		int x = chunkCoordinate.getChunkX();
		int z = chunkCoordinate.getChunkZ();
		PaperBiome biome = (PaperBiome) this.getCachedBiomeProvider().getNoiseBiome((x << 2) + 2, (z << 2) + 2);
		var biomeConfig = biome.getBiomeConfig();
		IWorldConfig worldConfig = this.preset.getWorldConfig();

		if (worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.STRONGHOLD), this.worldSeed, x, z, 4))
		{
			return true;
		}

		// Use each world's enabled settings as well as biome settings. Otherwise disabled
		// structures are still treated as terrain obstacles by OTG's shadow chunk cache.
		if (worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != SettingsEnums.VillageType.disabled
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.VILLAGE), this.worldSeed, x, z, 4))
		{
			return true;
		}
		if (worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.BASTION_REMNANT), this.worldSeed, x, z, 4))
		{
			return true;
		}
		if (worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.END_CITY), this.worldSeed, x, z, 4))
		{
			return true;
		}
		if (worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.OCEAN_MONUMENT), this.worldSeed, x, z, 4))
		{
			return true;
		}
		if (worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.WOODLAND_MANSION), this.worldSeed, x, z, 4))
		{
			return true;
		}

		if (worldConfig.getRareBuildingsEnabled())
		{
			switch (biomeConfig.getRareBuildingType())
			{
				case disabled -> { }
				case desertPyramid -> {
					if (this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.DESERT_PYRAMID), this.worldSeed, x, z, 1)) return true;
				}
				case jungleTemple -> {
					if (this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.JUNGLE_TEMPLE), this.worldSeed, x, z, 1)) return true;
				}
				case swampHut -> {
					if (this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.SWAMP_HUT), this.worldSeed, x, z, 1)) return true;
				}
				case igloo -> {
					if (this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.IGLOO), this.worldSeed, x, z, 1)) return true;
				}
			}
		}

		if (worldConfig.getShipWrecksEnabled() && (biomeConfig.getShipWreckEnabled() || biomeConfig.getShipWreckBeachedEnabled())
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.SHIPWRECK), this.worldSeed, x, z, 1))
		{
			return true;
		}
		if (worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.PILLLAGER_OUTPOST), this.worldSeed, x, z, 1))
		{
			return true;
		}
		if (worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != SettingsEnums.OceanRuinsType.disabled
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.OCEAN_RUINS), this.worldSeed, x, z, 1))
		{
			return true;
		}
		if (worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.BURIED_TREASURE), this.worldSeed, x, z, 1))
		{
			return true;
		}
		if (worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != SettingsEnums.MineshaftType.disabled
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.MINESHAFT), this.worldSeed, x, z, 1))
		{
			return true;
		}
		if (worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != SettingsEnums.RuinedPortalType.disabled
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.RUINED_PORTAL), this.worldSeed, x, z, 2))
		{
			return true;
		}
		if (worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.FORTRESS), this.worldSeed, x, z, 4))
		{
			return true;
		}
		if (worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled()
				&& this.hasFeatureChunkInRange(this.structurePlacementSettings.get(StructureType.NETHER_FOSSIL), this.worldSeed, x, z, 1))
		{
			return true;
		}

		// Strongholds use concentric rings, not random-spread placements. They require a
		// separate ring-position check and must not be passed to hasFeatureChunkInRange.
		return false;
	}

	public boolean hasFeatureChunkInRange(StructurePlacement placement, long seed, int chunkX, int chunkZ, int radius)
	{
		if (placement instanceof ConcentricRingsStructurePlacement rings) {
			return hasConcentricStructureChunkInRange(rings, seed, chunkX, chunkZ, radius);
		}
		if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
			return false;
		}

		int minChunkX = chunkX - radius;
		int maxChunkX = chunkX + radius;
		int minChunkZ = chunkZ - radius;
		int maxChunkZ = chunkZ + radius;
		int spacing = spread.spacing();

		int minRegionX = Math.floorDiv(minChunkX, spacing);
		int maxRegionX = Math.floorDiv(maxChunkX, spacing);
		int minRegionZ = Math.floorDiv(minChunkZ, spacing);
		int maxRegionZ = Math.floorDiv(maxChunkZ, spacing);

		for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
			for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
				ChunkPos candidate = spread.getPotentialStructureChunk(seed, regionX * spacing, regionZ * spacing);
				if (candidate.x >= minChunkX && candidate.x <= maxChunkX
						&& candidate.z >= minChunkZ && candidate.z <= maxChunkZ) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasConcentricStructureChunkInRange(ConcentricRingsStructurePlacement placement, long seed, int chunkX, int chunkZ, int radius)
	{
		int count = placement.count();
		int spread = placement.spread();
		int distance = placement.distance();
		if (count <= 0 || spread <= 0 || distance <= 0)
		{
			return false;
		}

		// Vanilla can move a ring candidate by up to 112 blocks while searching for
		// an allowed biome. Expand the check to cover that shift without loading chunks.
		int searchRadius = radius + 8;
		RandomSource random = RandomSource.create();
		random.setSeed(seed);
		double angle = random.nextDouble() * Math.PI * 2.0D;
		int positionInCircle = 0;
		int circle = 0;

		for (int i = 0; i < count; i++)
		{
			double ringDistance = (4.0D * distance + (double) distance * circle * 6.0D)
					+ (random.nextDouble() - 0.5D) * (double) distance * 2.5D;
			int candidateX = (int) Math.round(Math.cos(angle) * ringDistance);
			int candidateZ = (int) Math.round(Math.sin(angle) * ringDistance);

			if (Math.abs((long) chunkX - candidateX) <= searchRadius
					&& Math.abs((long) chunkZ - candidateZ) <= searchRadius)
			{
				return true;
			}

			// The vanilla algorithm forks a biome-search random source each iteration.
			// Advance the parent source too, so later ring angles follow the same sequence.
			random.fork();
			angle += Math.PI * 2.0D / spread;
			positionInCircle++;
			if (positionInCircle == spread)
			{
				circle++;
				positionInCircle = 0;
				spread += 2 * spread / (circle + 1);
				spread = Math.min(spread, count - i);
				angle += random.nextDouble() * Math.PI * 2.0D;
			}
		}
		return false;
	}
}
