package com.rodriaum.mtg.customobject.structures;

import com.rodriaum.mtg.customobject.CustomObject;
import com.rodriaum.mtg.customobject.config.CustomObjectConfigFile;
import com.rodriaum.mtg.customobject.util.ObjectType;
import com.rodriaum.mtg.customobject.util.BoundingBox;
import com.rodriaum.mtg.interfaces.IStructuredCustomObject;
import com.rodriaum.mtg.util.bo3.Rotation;

/**
 * Represents CustomObjects that can have other objects attached
 * to it making a structure.
 *
 */
public interface StructuredCustomObject extends CustomObject, IStructuredCustomObject
{
	ObjectType getType();
	CustomObjectConfigFile getConfig();
	BoundingBox getBoundingBox(Rotation north);
}
