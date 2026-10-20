package com.rodriaum.mtg.interfaces;

public interface IChunkDecorator
{
	Object getLockingObject();

	boolean isDecorating();

	public void beginSave();

	public void endSave();

	boolean getIsSaveRequired();
}
