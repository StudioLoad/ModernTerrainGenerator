package com.pg85.otg.paper.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

public abstract class BaseCommand
{
	protected final String name;
	protected String helpMessage = "No help message specified.";
	protected String usage = "No usage message specified.";
	protected String[] detailedHelp = new String[]
	{ "No detailed help specified." };

	public BaseCommand(String name) {
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public String getHelpMessage()
	{
		return helpMessage;
	}

	public String getUsage()
	{
		return usage;
	}

	public String[] getDetailedHelp()
	{
		return this.detailedHelp;
	}

	public String getPermission() {
		return "otg.*";
	}
	
	public MutableComponent createComponent(String text, String text2, ChatFormatting color1, ChatFormatting color2) {
		return Component.literal(text).withStyle(color1).append(Component.literal(text2).withStyle(color2));
	}

	public abstract void build(LiteralArgumentBuilder<CommandSourceStack> builder);
}
