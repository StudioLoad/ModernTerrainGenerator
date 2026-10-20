package com.rodriaum.mtg.paper.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.rodriaum.mtg.core.presets.Preset;
import com.rodriaum.mtg.paper.gen.OTGNoiseChunkGenerator;
import com.rodriaum.mtg.util.biome.MTGBiomeResourceLocation;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class TpCommand extends BaseCommand
{
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> Component.translatable("commands.locatebiome.notFound", object));

	public TpCommand()
	{
		super("tp");
		this.helpMessage = "Teleports you to a specific MTG biome.";
		this.usage = "/mtg tp <biome> [range]";
		this.detailedHelp = new String[]
		{ "<biome>: The name of the biome to teleport to.",
				"[range]: The radius in blocks to search for the target biome, defaults to 10000.",
				"Note: large numbers will make the command take a long time." };
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(
				Commands.literal("tp")
						.executes(context -> showHelp(context.getSource()))
						.then(Commands.argument("biome", StringArgumentType.word())
								.executes(context -> locateBiome(context.getSource(),
										StringArgumentType.getString(context, "biome"), 10000))
								.suggests(this::suggestBiomes)
								.then(Commands.argument("range", IntegerArgumentType.integer(0))
										.executes(context -> locateBiome(context.getSource(),
												StringArgumentType.getString(context, "biome"),
												IntegerArgumentType.getInteger(context, "range"))))));
	}

	@SuppressWarnings("resource")
	private int locateBiome(CommandSourceStack source, String biome, int range) throws CommandSyntaxException
	{
		if (!source.hasPermission(Permissions.COMMANDS_GAMEMASTER, getPermission())) {
			source.sendSuccess(() -> (Component.literal("\u00a7cPermission denied!")), false);
			return 0;
		}
		biome = biome.toLowerCase();

		ServerLevel world = source.getLevel();
		if (!(world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(() -> (Component.literal("MTG is not enabled in this world")), false);
			return 0;
		}

		Preset preset = ((OTGNoiseChunkGenerator) world.getChunkSource().getGenerator()).getPreset();

		Identifier key = Identifier.parse(new MTGBiomeResourceLocation(preset.getPresetFolder(),
				preset.getShortPresetName(), preset.getMajorVersion(), biome).toResourceLocationString());

		BlockPos origin = BlockPos.containing(source.getPosition());

		var result = world.getChunkSource()
				.getGenerator()
				.getBiomeSource()
				.findClosestBiome3d(
						origin,
						range,
						8,
						8,
						biomeHolder -> biomeHolder.is(key),
						world.getChunkSource().randomState().sampler(),
						world
				);

		BlockPos pos = result != null ? result.getFirst() : null;

		if (pos == null)
		{
			throw ERROR_BIOME_NOT_FOUND.create(biome);
		} else
		{
			int y = world.getChunkSource().getGenerator().getBaseHeight(
					pos.getX(),
					pos.getZ(),
					Types.MOTION_BLOCKING_NO_LEAVES,
					world,
					world.getChunkSource().randomState()
			);
			source.getPlayerOrException().teleportTo(pos.getX(), y, pos.getZ());
			String finalBiome = biome;
			source.sendSuccess(() -> (Component.literal("Teleporting you to the nearest " + finalBiome + ".")), false);
			return 0;
		}
	}

	private CompletableFuture<Suggestions> suggestBiomes(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		List<String> biomes = new ArrayList<>();

		if (context.getSource().getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator)
		{
			biomes = ((OTGNoiseChunkGenerator) context.getSource().getLevel().getChunkSource().getGenerator()).getPreset()
					.getAllBiomeNames().stream().map(name -> name.replace(' ', '_')).collect(Collectors.toList());
		}

		return SharedSuggestionProvider.suggest(biomes, builder);
	}

	@Override
	public String getPermission() {
		return "mtg.cmd.tp";
	}

	private int showHelp(CommandSourceStack source)
	{
		source.sendSuccess(() -> (Component.literal(getUsage())), false);
		return 0;
	}
}
