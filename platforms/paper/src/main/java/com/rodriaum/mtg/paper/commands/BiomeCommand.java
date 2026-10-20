package com.rodriaum.mtg.paper.commands;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.paper.gen.OTGNoiseChunkGenerator;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class BiomeCommand extends BaseCommand
{
	private static final String[] OPTIONS = new String[]
	{ "info", "spawns", "features" };

	public BiomeCommand()
	{
		super("biome");
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/mtg biome";
	}

	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("biome")
				.executes(context -> showBiome(context.getSource(), ""))
				.then(Commands.argument("option", StringArgumentType.word()).suggests(this::suggestTypes).executes(
						context -> showBiome(context.getSource(), StringArgumentType.getString(context, "option")))));
	}

	@Override
	public String getPermission() {
		return "mtg.cmd.biome";
	}

	private int showBiome(CommandSourceStack source, String option)
	{
		if (!source.getBukkitSender().hasPermission(getPermission())) {
			source.sendSuccess(() -> (Component.literal("\u00a7cPermission denied!")), false);
			return 0;
		}
		if (!(source.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			source.sendSuccess(() -> (Component.literal("MTG is not enabled in this world")), false);
			return 0;
		}

		Biome biome = source.getLevel()
				.getBiome(BlockPos.containing(source.getPosition().x, source.getPosition().y, source.getPosition().z)).value();
		IBiomeConfig config = ((OTGNoiseChunkGenerator) source.getLevel().getChunkSource().getGenerator())
				.getCachedBiomeProvider().getBiomeConfig((int) source.getPosition().x, (int) source.getPosition().z);

		source.sendSuccess(() -> (Component.literal("=====================================================")), false);
		if (config.getIsTemplateForBiome())
		{
			source.sendSuccess(() -> (Component.literal("According to MTG, this biome uses the ").withStyle(ChatFormatting.GOLD)
							.append(Component.literal(config.getName()).withStyle(ChatFormatting.GREEN))
							.append(Component.literal(" template.").withStyle(ChatFormatting.GOLD))),
					false);
			source.sendSuccess(() -> (Component.literal("This biome belongs to either another mod or the vanilla game.")
					.withStyle(ChatFormatting.GRAY)), false);
		} else
		{
			source.sendSuccess(() -> (Component.literal("According to MTG, you are in the ").withStyle(ChatFormatting.GOLD)
							.append(Component.literal(config.getName()).withStyle(ChatFormatting.GREEN))
							.append(Component.literal(" biome.").withStyle(ChatFormatting.GOLD))),
					false);
		}
		source.sendSuccess(() -> (createComponent("Biome registry name: ", source.getServer().registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome).toString(),
				ChatFormatting.GOLD, ChatFormatting.GREEN)), false);

		switch (option)
		{
			case "info" -> showBiomeInfo(source, biome, config);
			case "spawns" -> showBiomeMobs(source, biome, config);
			case "features" -> showBiomeFeatures(source, biome, config);
			default -> {}
		}
		return 0;
	}

	private void showBiomeFeatures(CommandSourceStack source, Biome biome, IBiomeConfig config)
	{
		source.sendSuccess(() -> (Component.literal("Vanilla features in this biome: ").withStyle(ChatFormatting.GREEN)), false);
		List<HolderSet<PlacedFeature>> featureList = biome.getGenerationSettings().features();
		for (int i = 0; i < featureList.size(); i++)
		{
			HolderSet<PlacedFeature> innerList = featureList.get(i);
			String stageName = GenerationStep.Decoration.values()[i].toString().toLowerCase(Locale.ROOT);
			source.sendSuccess(() -> (Component.literal(" " + stageName + ":").withStyle(ChatFormatting.GOLD)), false);
			for (Holder<PlacedFeature> holder : innerList)
			{
				source.sendSuccess(() -> (Component.literal(" - "+holder.value().toString()).withStyle(ChatFormatting.GREEN)), false);
			}
		}
	}

	private void showBiomeInfo(CommandSourceStack source, Biome biome, IBiomeConfig config)
	{
		// Biome Category seems to be deprecated?
		//source.sendSuccess(() -> (createComponent("Biome Category: ", biome.getBiomeCategory().toString(), ChatFormatting.GOLD, ChatFormatting.GREEN)), false);
		source.sendSuccess(() -> (createComponent("Inherit Mobs: ", config.getInheritMobsBiomeName(), ChatFormatting.GOLD,
				ChatFormatting.GREEN)), false);

		source.sendSuccess(() -> (createComponent("Base Size: ", Integer.toString(config.getBiomeSize()), ChatFormatting.GOLD,
				ChatFormatting.GREEN)
						.append(createComponent(" Biome Rarity: ", Integer.toString(config.getBiomeRarity()),
								ChatFormatting.GOLD, ChatFormatting.GREEN))),
				false);

		source.sendSuccess(() -> (createComponent("Biome Height: ", String.format("%.2f", config.getBiomeHeight()),
				ChatFormatting.GOLD, ChatFormatting.GREEN)), false);

		source.sendSuccess(() -> (createComponent("Volatility: ", String.format("%.2f", config.getBiomeVolatility()),
				ChatFormatting.GOLD, ChatFormatting.GREEN)
						.append(createComponent(" Volatility1: ", String.format("%.2f", config.getVolatility1()),
								ChatFormatting.GOLD, ChatFormatting.GREEN))
						.append(createComponent(" Volatility2: ", String.format("%.2f", config.getVolatility2()),
								ChatFormatting.GOLD, ChatFormatting.GREEN))),
				false);

        source.sendSuccess(() -> createComponent(
                        "Base Temperature: ",
                        String.format("%.2f", biome.getBaseTemperature()),
                        ChatFormatting.GOLD,
                        ChatFormatting.GREEN
                ).append(createComponent(
                        " Current Temperature: ",
                        String.format(
                                "%.2f",
                                biome.getTemperature(
                                        BlockPos.containing(source.getPosition()),
                                        63
                                )
                        ),
                        ChatFormatting.GOLD,
                        ChatFormatting.GREEN
                )),
                false);
	}

	private void showBiomeMobs(CommandSourceStack source, Biome biome, IBiomeConfig config)
	{
		source.sendSuccess(() -> (Component.literal("Spawns:").withStyle(ChatFormatting.GOLD)), false);
		source.sendSuccess(() -> (Component.literal("  Monsters:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.MONSTER));
		source.sendSuccess(() -> (Component.literal("  Creatures:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.CREATURE));
		source.sendSuccess(() -> (Component.literal("  Water Creatures:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.WATER_CREATURE));
		source.sendSuccess(() -> (Component.literal("  Underground Water Creatures:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.UNDERGROUND_WATER_CREATURE));
		source.sendSuccess(() -> (Component.literal("  Ambient Creatures:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.AMBIENT));
		source.sendSuccess(() -> (Component.literal("  Water Ambient:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.WATER_AMBIENT));
		source.sendSuccess(() -> (Component.literal("  Misc:").withStyle(ChatFormatting.GOLD)), false);
		showSpawns(source, biome.getMobSettings().getMobs(MobCategory.MISC));
	}

	public void showSpawns(CommandSourceStack source, WeightedList spawns)
	{
		for (Object entry : spawns.unwrap())
		{
			if (!(entry instanceof Weighted weighted) || !(weighted.value() instanceof SpawnerData spawn))
			{
				continue;
			}

			Component message = createComponent("   - Entity: ", BuiltInRegistries.ENTITY_TYPE.getKey(spawn.type()).toString(),
					ChatFormatting.GOLD, ChatFormatting.GREEN)
					.append(createComponent(", Weight: ", Integer.toString(weighted.weight()), ChatFormatting.GOLD, ChatFormatting.GREEN))
					.append(createComponent(", Min: ", Integer.toString(spawn.minCount()), ChatFormatting.GOLD, ChatFormatting.GREEN))
					.append(createComponent(", Max: ", Integer.toString(spawn.maxCount()), ChatFormatting.GOLD, ChatFormatting.GREEN));
			source.sendSuccess(() -> message, false);
		}
	}

	private CompletableFuture<Suggestions> suggestTypes(CommandContext<CommandSourceStack> context,
			SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(OPTIONS, builder);
	}
}