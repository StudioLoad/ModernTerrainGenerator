package com.rodriaum.mtg.interfaces;

import com.rodriaum.mtg.util.logging.LogCategory;
import com.rodriaum.mtg.util.logging.LogLevel;

public interface ILogger
{
	public void init(LogLevel level, boolean logCustomObjects, boolean logStructurePlotting, boolean logConfigs, boolean logPerformance, boolean logBiomeRegistry, boolean logDecoration, boolean logMobs, String logPresets);

	public boolean getLogCategoryEnabled(LogCategory category);	
	public void log(LogLevel level, LogCategory category, String message);
	public void printStackTrace(LogLevel marker, LogCategory category, Exception e);
	public boolean canLogForPreset(String presetFolderName);
}
