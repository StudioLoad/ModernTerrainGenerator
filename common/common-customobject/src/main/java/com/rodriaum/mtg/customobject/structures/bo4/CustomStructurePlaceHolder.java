package com.rodriaum.mtg.customobject.structures.bo4;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.rodriaum.mtg.customobject.CustomObjectManager;
import com.rodriaum.mtg.customobject.config.CustomObjectResourcesManager;
import com.rodriaum.mtg.customobject.structures.bo4.smoothing.SmoothingAreaLine;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IModLoadedChecker;
import com.rodriaum.mtg.util.ChunkCoordinate;

public class CustomStructurePlaceHolder extends BO4CustomStructure
{
	public CustomStructurePlaceHolder(long worldSeed, BO4CustomStructureCoordinate structureStart, Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn, int minY, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		super(worldSeed, structureStart, objectsToSpawn, smoothingAreasToSpawn, minY, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
	}

	public void mergeWithCustomStructure(BO4CustomStructure structure)
	{		
		structure.getObjectsToSpawn().putAll(this.getObjectsToSpawn());
		
		Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> mergedSmoothingAreas = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
		mergedSmoothingAreas.putAll(structure.getSmoothingAreaManager().smoothingAreasToSpawn);
		mergedSmoothingAreas.putAll(this.getSmoothingAreaManager().smoothingAreasToSpawn);
		structure.getSmoothingAreaManager().fillSmoothingLineCaches(mergedSmoothingAreas);		
	}
}
