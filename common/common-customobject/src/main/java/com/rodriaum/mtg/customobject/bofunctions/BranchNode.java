package com.rodriaum.mtg.customobject.bofunctions;

import java.nio.file.Path;

import com.rodriaum.mtg.customobject.CustomObject;
import com.rodriaum.mtg.customobject.CustomObjectManager;
import com.rodriaum.mtg.customobject.config.CustomObjectResourcesManager;
import com.rodriaum.mtg.customobject.structures.StructuredCustomObject;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.interfaces.IModLoadedChecker;
import com.rodriaum.mtg.util.bo3.Rotation;

/**
 * Simple class to hold the spawn chance and rotation of a BO3 in the Branch or
 * WeightedBranch function in the BO3 file.
 */
public class BranchNode implements Comparable<BranchNode>
{
	/**
	 * The rotation of a branch
	 */
	protected Rotation rotation;
	/**
	 * The chance of a branch
	 */
	protected double chance;
	/**
	 * The branch with associated rotation and chance values
	 */
	protected StructuredCustomObject customObject;
	public String customObjectName;

	protected BranchNode() {}
	
	public BranchNode(Rotation rotation, double chance, StructuredCustomObject branch, String customObjectName)
	{
		this.rotation = rotation;
		this.chance = chance;
		this.customObject = branch;
		this.customObjectName = branch != null ? branch.getName() : customObjectName != null && customObjectName.length() > 0 ? customObjectName : null;
	}

	/**
	 * @return the spawn chance of the branch
	 */
	public double getChance()
	{
		return chance;
	}

	/**
	 * @return the rotation object associated with the branch
	 */
	public Rotation getRotation()
	{
		return rotation;
	}

	/**
	 * @return the branch CustomObject
	 */
	public StructuredCustomObject getCustomObject(boolean lazyLoad, String presetFolderName, Path otgRootFolder, ILogger logger, CustomObjectManager customObjectManager, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
	{
		if(customObject != null || !lazyLoad)
		{
			return customObject;
		}

		CustomObject customObject = customObjectManager.getGlobalObjects().getObjectByName(customObjectName, presetFolderName, otgRootFolder, logger, customObjectManager, materialReader, manager, modLoadedChecker);
		if(customObject != null && !(customObject instanceof StructuredCustomObject))
		{
			customObject = null;
		}
		customObjectName = customObject != null ? customObject.getName() : null;

		return (StructuredCustomObject)customObject;
	}

	/**
	 * @return The string representation of this branch as seen in BO3 configs
	 */
	protected String toBranchString()
	{
		return ',' + customObjectName + ',' + rotation.name() + ',' + chance;
	}

	@Override
	public int compareTo(BranchNode that)
	{
		return (int) (this.chance - that.chance);
	}
}
