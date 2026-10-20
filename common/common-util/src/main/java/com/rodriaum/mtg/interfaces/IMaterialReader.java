package com.rodriaum.mtg.interfaces;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterialTag;

public interface IMaterialReader
{
	public LocalMaterialData readMaterial(String material) throws InvalidConfigException;	
	public LocalMaterialTag readTag(String tag) throws InvalidConfigException;
}
