package com.pg85.otg.paper.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.pg85.otg.customobject.util.ObjectType;
import com.pg85.otg.paper.commands.arguments.PresetArgument;
import com.pg85.otg.paper.materials.PaperMaterialData;
import com.pg85.otg.util.materials.LocalMaterialData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import static com.pg85.otg.paper.commands.ExportCommand.configMap;

public class ConfigExportCommand extends BaseCommand
{
	public ConfigExportCommand()
	{
		super("configexport");
		this.helpMessage = "Configurates the /otg export command";
		this.usage = "/otg configexport";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(
			Commands.literal("configexport").then(
				Commands.literal("preset").then(
					Commands.argument("preset", StringArgumentType.greedyString())
						.suggests((context, suggestionBuilder) -> PresetArgument.suggest(context, suggestionBuilder, true))
						.executes(context -> {
							Entity sender = context.getSource().getEntity();
							init(sender);
							String val = context.getArgument("preset", String.class);
							configMap.get(sender).preset = val;
							context.getSource().sendSuccess(Component.literal(val + " set as your default preset"), false);
							return 0;
						})
				)
			).then(
				Commands.literal("filter").then(
					Commands.literal("add").then(
						Commands.argument("block", BlockStateArgument.block())
							.executes(context -> {
								Entity sender = context.getSource().getEntity();
								init(sender);
								LocalMaterialData mat = PaperMaterialData.ofBlockData(context.getArgument("block", BlockInput.class).getState());
								configMap.get(sender).filter.add(mat);
								context.getSource().sendSuccess(Component.literal("Block "+mat.getRegistryName()+" added to filter"), false);
								return 0;
							})
					)
				).then(
					Commands.literal("remove").then(
						Commands.argument("block", BlockStateArgument.block())
							.executes(context -> {
								Entity sender = context.getSource().getEntity();
								init(sender);
								LocalMaterialData mat = PaperMaterialData.ofBlockData(context.getArgument("block", BlockInput.class).getState());
								configMap.get(sender).filter.remove(mat);
								context.getSource().sendSuccess(Component.literal("Block "+mat.getRegistryName()+" removed from filter"), false);
								return 0;
							})
					)
				).then(
					Commands.literal("list")
						.executes(context -> {
							CommandSourceStack sender = context.getSource();
							init(sender.getEntity());
							sender.sendSuccess(Component.literal("Current exclusion filter:").withStyle(ChatFormatting.GOLD), false);
							configMap.get(sender.getEntity()).filter.forEach(item ->
								sender.sendSuccess(Component.literal("- "+item.getRegistryName()).withStyle(ChatFormatting.GREEN), false));
							return 0;
						})
				)
			).then(
				Commands.literal("type").then(
					Commands.argument("type", StringArgumentType.word())
						.suggests((context, suggestionBuilder) -> ExportCommand.suggestTypes(context, suggestionBuilder, false))
						.executes(context -> {
							Entity sender = context.getSource().getEntity();
							init(sender);
							ObjectType type = ObjectType.valueOf(context.getArgument("type", String.class));
							configMap.get(sender).objectType = type;
							context.getSource().sendSuccess(Component.literal(type.getType()+" set as your default object type"), false);
							return 0;
						})
				)
			).then(
				Commands.literal("centerblock").then(
					Commands.argument("block", BlockStateArgument.block())
						.executes(context -> {
							Entity sender = context.getSource().getEntity();
							init(sender);
							var block = context.getArgument("block", BlockInput.class).getState();
							configMap.get(sender).centerBlock = block;
							context.getSource().sendSuccess(Component.literal(block.getBlock()+" set as center block"), false);
							return 0;
						})
				)
			).then(
				Commands.literal("template").then(
					Commands.argument("template", StringArgumentType.greedyString())
						.suggests((context, suggestionBuilder) ->
							ExportCommand.suggestTemplates(suggestionBuilder, getPreset(context)))
						.executes(context -> {
							// init done in getPreset
							Entity sender = context.getSource().getEntity();
							String template = context.getArgument("template", String.class);
							configMap.get(sender).template = template;
							context.getSource().sendSuccess(Component.literal(template+" set as your default template"), false);
							return 0;
						})
				)
			).then(
				Commands.literal("overwrite").then(
					Commands.argument("overwrite", BoolArgumentType.bool())
						.executes(context -> {
							Entity sender = context.getSource().getEntity();
							init(sender);
							var overwrite = context.getArgument("overwrite", Boolean.class);
							configMap.get(sender).overwrite = overwrite;
							if (overwrite)
								context.getSource().sendSuccess(Component.literal("Overwriting existing files enabled"), false);
							else
								context.getSource().sendSuccess(Component.literal("Overwriting existing files disabled"), false);
							return 0;
						})
				)
			).then(
				Commands.literal("global").then(
					Commands.argument("global", BoolArgumentType.bool())
						.executes(context -> {
							Entity sender = context.getSource().getEntity();
							init(sender);
							var global = context.getArgument("global", Boolean.class);
							configMap.get(sender).exportToGlobal = global;
							if (global)
								context.getSource().sendSuccess(Component.literal("Writing objects to GlobalObjects enabled"), false);
							else
								context.getSource().sendSuccess(Component.literal("Writing objects to GlobalObjects disabled"), false);
							return 0;
						})
				)
			)
		);
	}

	// Needs to be its own method, because we need to fetch the preset from outside of the command context
	// and we cannot init if we do so in-line
	private String getPreset(CommandContext<CommandSourceStack> context)
	{
		Entity sender = context.getSource().getEntity();
		init(sender);
		return configMap.get(sender).preset;
	}

	private static void init(Entity commandSender) {
		if (!configMap.containsKey(commandSender))
		{
			configMap.put(commandSender, new ExportCommand.CommandOptions());
		}
	}
}
