package com.rodriaum.mtg.customobject.structures;

import com.rodriaum.mtg.customobject.CustomObjectManager;
import com.rodriaum.mtg.customobject.config.CustomObjectResourcesManager;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IModLoadedChecker;
import com.rodriaum.mtg.util.bo3.Rotation;

import java.nio.file.Path;
import java.util.Random;

/**
 * Represents a branch of a CustomObject.
 *
 */
public interface Branch
{
	/**
	 * Makes a CustomObjectCoordinate out of this branch. Is allowed
	 * to return null if based on the random number generator no
	 * branch should spawn here.
	 * 
	 * @param world  The world.
	 * @param random The random number generator.
	 * @param rotation Rotation of the origin of the object.
	 * @param x	  X coordinate of the origin of the object.
	 * @param y	  Y coordinate of the origin of the object.
	 * @param z	  Z coordinate of the origin of the object.
	 * @return The CustomObjectCoordinate of this branch.
	 */
	public CustomStructureCoordinate toCustomObjectCoordinate(String presetFolderName, Random random, Rotation rotation, int x, int y, int z, String startBO3Name, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);
}
