package com.rodriaum.mtg.paper;

import com.rodriaum.mtg.constants.Constants;
import com.rodriaum.mtg.core.OTG;
import com.rodriaum.mtg.core.presets.Preset;
import com.rodriaum.mtg.interfaces.IWorldConfig;
import com.rodriaum.mtg.paper.biome.OTGBiomeProvider;
import com.rodriaum.mtg.paper.commands.OTGCommandExecutor;
import com.rodriaum.mtg.paper.events.OTGHandler;
import com.rodriaum.mtg.paper.gen.OTGNoiseChunkGenerator;
import com.rodriaum.mtg.paper.gen.OTGPaperChunkGen;
import com.rodriaum.mtg.paper.networking.NetworkingListener;
import com.rodriaum.mtg.paper.util.ObfuscationHelper;
import com.rodriaum.mtg.util.logging.LogCategory;
import com.rodriaum.mtg.util.logging.LogLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;


public class MTGPlugin extends JavaPlugin implements Listener
{
	private static final ReentrantLock initLock = new ReentrantLock();
	private static final HashMap<String, String> worlds = new HashMap<>();
	private static final HashSet<String> processedWorlds = new HashSet<>();

	@SuppressWarnings("unused")
	private OTGHandler handler;

	public static MTGPlugin plugin;



	@Override
	public void onDisable ()
	{
		// Experimental test to stop crash on server stop for spigot
		// OTG.stopEngine();
	}

	@Override
	public void onEnable ()
	{
		plugin = this;
		Field frozen;
		try
		{
			frozen = ObfuscationHelper.getField(MappedRegistry.class, "frozen", "bL");
			// Make the frozen boolean accessible
			frozen.setAccessible(true);
			// Set the 'frozen' boolean to false for this registry
			frozen.set(BuiltInRegistries.BIOME_SOURCE, false);
			frozen.set(BuiltInRegistries.CHUNK_GENERATOR, false);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.BIOME_REGISTRY, "Failed to unfreeze registry");
			e.printStackTrace();
		}
		Registry.register(BuiltInRegistries.BIOME_SOURCE, Identifier.fromNamespaceAndPath(Constants.MOD_ID_SHORT, "default"), OTGBiomeProvider.CODEC);
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier.fromNamespaceAndPath(Constants.MOD_ID_SHORT, "default"), OTGNoiseChunkGenerator.CODEC);

		// Re-freeze the two registries
		try
		{
			frozen = ObfuscationHelper.getField(MappedRegistry.class, "frozen", "bL");
			frozen.setAccessible(true);
			frozen.set(BuiltInRegistries.BIOME_SOURCE, true);
			frozen.set(BuiltInRegistries.CHUNK_GENERATOR, true);
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.BIOME_REGISTRY, "Failed to re-freeze registry");
			e.printStackTrace();
		}

		OTG.startEngine(new PaperEngine(this));
		
		OTGCommandExecutor.registerArguments();
		OTGCommandExecutor.register(((CraftServer) Bukkit.getServer()).getServer().getCommands().getDispatcher());
		// Does this go here?
		OTG.getEngine().getPresetLoader().registerBiomes();

		Registry<Biome> biome_registry = ((CraftServer) Bukkit.getServer()).getServer().registryAccess().lookupOrThrow(Registries.BIOME);
		int i = 0;

		if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.BIOME_REGISTRY))
		{
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "-----------------");
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "Registered biomes:");
			for (Biome biomeBase : biome_registry)
			{
				OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, (i++) + ": " + biomeBase.toString());
			}
			OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.BIOME_REGISTRY, "-----------------");
		}
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		this.handler = new OTGHandler(this);	
		Bukkit.getPluginManager().registerEvents(new NetworkingListener(this), this);
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator (String worldName, String id)
	{
		if (id == null || id.equals(""))
		{
			id = "Default";
		}
		Preset preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(id);
		if (preset == null)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Could not find preset '" + id + "', did you install it correctly?");
			return null;
		}
		worlds.put(worldName, id);
		return new OTGPaperChunkGen(preset);
	}

	@EventHandler
	public void onWorldEnable (WorldInitEvent event)
	{
		if (worlds.containsKey(event.getWorld().getName()))
		{
			// Most likely no longer needed, but keeping it just in case. The lock keeps it from doing it double anyway.
			injectInternalGenerator(event.getWorld());
		}
	}

	public void injectInternalGenerator(World world)
	{
		initLock.lock();
		try
		{
		if (processedWorlds.contains(world.getName()))
		{
			// We have already processed this world, return
			return;
		}

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Taking over world " + world.getName());
		ServerLevel serverWorld = ((CraftWorld) world).getHandle();

		net.minecraft.world.level.chunk.ChunkGenerator generator = serverWorld.getChunkSource().getGenerator();
		if (!(generator instanceof CustomChunkGenerator))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "Mission failed, we'll get them next time");
			return;
		}
		if (!(world.getGenerator() instanceof OTGPaperChunkGen OTGGen))
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, "World generator was not an MTG generator, cannot take over, something has gone wrong");
			return;
		}
		// We have a CustomChunkGenerator and a NoiseChunkGenerator
		OTGNoiseChunkGenerator OTGDelegate;
		RegistryAccess registryAccess = ((CraftServer) Bukkit.getServer()).getServer().registryAccess();
		// If generator is null, it has not been initialized yet. Initialize it.
		// The lock is used to avoid the accidental creation of two separate objects, in case
		// of a race condition.
		if (OTGGen.generator == null)
		{
			NoiseSettings noiseSettings = getNoiseSettings(OTGGen.getPreset().getWorldConfig());
			OTGDelegate = new OTGNoiseChunkGenerator(
				OTGGen.getPreset().getFolderName(),
				new OTGBiomeProvider(OTGGen.getPreset().getFolderName(), world.getSeed(), registryAccess.lookupOrThrow(Registries.BIOME)),
				registryAccess.lookupOrThrow(Registries.STRUCTURE_SET),
				registryAccess.lookupOrThrow(Registries.NOISE),
				world.getSeed(),
				Holder.direct(new NoiseGeneratorSettings(noiseSettings, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), getNoiseRouter(noiseSettings, registryAccess), SurfaceRuleData.overworld(), java.util.List.of(), 63, false, true, true, false))
			);
			// add the weird Spigot config; it was complaining about this
			//OTGDelegate.conf = serverWorld.spigotConfig;
		} else {
			OTGDelegate = OTGGen.generator;
		}

		try
		{
			ChunkMap chunkMap = serverWorld.getChunkSource().chunkMap;
			Field contextField = ChunkMap.class.getDeclaredField("worldGenContext");
			Field randomStateField = ChunkMap.class.getDeclaredField("randomState");
			Field structureStateField = ChunkMap.class.getDeclaredField("chunkGeneratorState");
			contextField.setAccessible(true);
			randomStateField.setAccessible(true);
			structureStateField.setAccessible(true);

			WorldGenContext oldContext = (WorldGenContext) contextField.get(chunkMap);
			RandomState otgRandomState = RandomState.create(
					OTGDelegate.getGeneratorSettingsHolder().value(),
					registryAccess.lookupOrThrow(Registries.NOISE),
					world.getSeed());
			ChunkGeneratorStructureState otgStructureState = OTGDelegate.createState(
					registryAccess.lookupOrThrow(Registries.STRUCTURE_SET),
					otgRandomState,
					world.getSeed(),
					serverWorld.spigotConfig
			);
			WorldGenContext otgContext = new WorldGenContext(
					oldContext.level(),
					OTGDelegate,
					oldContext.structureManager(),
					oldContext.lightEngine(),
					oldContext.mainThreadExecutor(),
					oldContext.unsavedListener());

			Object oldRandomState = randomStateField.get(chunkMap);
			Object oldStructureState = structureStateField.get(chunkMap);
			try
			{
				randomStateField.set(chunkMap, otgRandomState);
				structureStateField.set(chunkMap, otgStructureState);
				contextField.set(chunkMap, otgContext);
			}
			catch (ReflectiveOperationException ex)
			{
				try
				{
					randomStateField.set(chunkMap, oldRandomState);
					structureStateField.set(chunkMap, oldStructureState);
				}
				catch (ReflectiveOperationException rollbackException)
				{
					ex.addSuppressed(rollbackException);
				}
				throw ex;
			}
		} catch (ReflectiveOperationException ex)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN,
					"Failed to install the MTG chunk generator for world " + world.getName()
							+ ". The world will not be marked as processed.");
			OTG.getEngine().getLogger().printStackTrace(LogLevel.ERROR, LogCategory.MAIN, ex);
			return;
		}

		if (OTGGen.generator == null)
		{
			OTGGen.generator = OTGDelegate;
		}

		// Spigot may have started generating - we gotta regen if so

		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Success!");

		processedWorlds.add(world.getName());
		}
		finally
		{
			initLock.unlock();
		}
	}

	// Taken from vanilla NoiseRouterData::overworldWithoutCaves
	private NoiseRouter getNoiseRouter(NoiseSettings noiseSettings, RegistryAccess registryAccess) {
		DensityFunction shiftX = getFunction("shift_x", registryAccess);
		DensityFunction shiftZ = getFunction("shift_z", registryAccess);
		return new NoiseRouter(
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, registryAccess.lookupOrThrow(Registries.NOISE).getOrThrow(Noises.TEMPERATURE)),
				DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, registryAccess.lookupOrThrow(Registries.NOISE).getOrThrow(Noises.VEGETATION)),
				getFunction("overworld/continents", registryAccess),
				getFunction("overworld/erosion", registryAccess),
				getFunction("overworld/depth", registryAccess),
				getFunction("overworld/ridges", registryAccess),
				DensityFunctions.mul(DensityFunctions.constant(4.0D), DensityFunctions.mul(getFunction("overworld/depth", registryAccess), DensityFunctions.cache2d(getFunction("overworld/factor", registryAccess))).quarterNegative()),
				DensityFunctions.mul(DensityFunctions.interpolated(DensityFunctions.blendDensity(getFunction("overworld/sloped_cheese", registryAccess))), DensityFunctions.constant(0.64D)).squeeze(),
				DensityFunctions.zero(),
				DensityFunctions.zero(),
				DensityFunctions.zero()
		);
	}

	private DensityFunction getFunction(String string, RegistryAccess registryAccess) {
		return registryAccess.lookupOrThrow(Registries.DENSITY_FUNCTION).getOrThrow(ResourceKey.create(Registries.DENSITY_FUNCTION, Identifier.parse(string))).value();
	}

	private NoiseSettings getNoiseSettings(IWorldConfig worldConfig) {
		return NoiseSettings.create(
				worldConfig.getWorldMinY(),
				worldConfig.getWorldHeight(),
				1,
				2);
	}
}
