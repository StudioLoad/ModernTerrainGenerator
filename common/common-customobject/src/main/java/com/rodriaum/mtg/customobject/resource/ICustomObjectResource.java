package com.rodriaum.mtg.customobject.resource;

import java.nio.file.Path;
import java.util.Random;

import com.rodriaum.mtg.customobject.CustomObjectManager;
import com.rodriaum.mtg.customobject.config.CustomObjectResourcesManager;
import com.rodriaum.mtg.customobject.structures.CustomStructureCache;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IModLoadedChecker;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;

public interface ICustomObjectResource
{
	default void processForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		// TODO: Fire Forge resource decoration events, when they're available.
		spawnForChunkDecoration(structureCache, worldGenRegion, random, otgRootFolder, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	void spawnForChunkDecoration(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Path otgRootFolder, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
}
