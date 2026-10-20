package com.rodriaum.mtg.customobject.bo4;

import java.io.File;

import com.rodriaum.mtg.customobject.CustomObject;
import com.rodriaum.mtg.customobject.CustomObjectLoader;
import com.rodriaum.mtg.customobject.bo4.bo4function.BO4BlockFunction;
import com.rodriaum.mtg.customobject.bo4.bo4function.BO4BranchFunction;
import com.rodriaum.mtg.customobject.bo4.bo4function.BO4EntityFunction;
import com.rodriaum.mtg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.rodriaum.mtg.customobject.bo4.bo4function.BO4WeightedBranchFunction;
import com.rodriaum.mtg.customobject.config.CustomObjectResourcesManager;
import com.rodriaum.mtg.interfaces.ILogger;

public class BO4Loader implements CustomObjectLoader
{
	public BO4Loader(CustomObjectResourcesManager registry)
	{
		// Register BO4 ConfigFunctions
		registry.registerConfigFunction("Block", BO4BlockFunction.class);
		registry.registerConfigFunction("B", BO4BlockFunction.class);
		registry.registerConfigFunction("Branch", BO4BranchFunction.class);
		registry.registerConfigFunction("BR", BO4BranchFunction.class);
		registry.registerConfigFunction("WeightedBranch", BO4WeightedBranchFunction.class);
		registry.registerConfigFunction("WBR", BO4WeightedBranchFunction.class);
		registry.registerConfigFunction("RandomBlock", BO4RandomBlockFunction.class);
		registry.registerConfigFunction("RB", BO4RandomBlockFunction.class);
		registry.registerConfigFunction("Entity", BO4EntityFunction.class);
		registry.registerConfigFunction("E", BO4EntityFunction.class);
	}

	@Override
	public CustomObject loadFromFile(String objectName, File file, ILogger logger)
	{
		return new BO4(objectName, file);
	}
}
