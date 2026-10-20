package com.rodriaum.mtg.paper.util;

import org.bukkit.Bukkit;

import com.rodriaum.mtg.interfaces.IModLoadedChecker;

public class PaperPluginLoadedChecker implements IModLoadedChecker
{
	@Override
	public boolean isModLoaded (String mod)
	{
		return Bukkit.getServer().getPluginManager().isPluginEnabled(mod);
	}
}
