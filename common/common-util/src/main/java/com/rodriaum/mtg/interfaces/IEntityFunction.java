package com.rodriaum.mtg.interfaces;

import com.rodriaum.mtg.util.nbt.NamedBinaryTag;

public interface IEntityFunction
{
	public double getX();
	public int getY();
	public double getZ();
	public int getGroupSize();
	public String getNameTagOrNBTFileName();
	public String getResourceLocation();
	public String getMetaData();
	NamedBinaryTag getNBTTag();
	public String makeString();
}
