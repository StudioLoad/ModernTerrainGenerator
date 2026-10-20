package com.rodriaum.mtg.paper.commands;

import java.nio.file.Path;
import java.util.ArrayList;
import org.bukkit.entity.Player;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.rodriaum.mtg.config.ConfigFunction;
import com.rodriaum.mtg.constants.SettingsEnums.CustomStructureType;
import com.rodriaum.mtg.core.OTG;
import com.rodriaum.mtg.core.config.biome.BiomeConfig;
import com.rodriaum.mtg.core.presets.Preset;
import com.rodriaum.mtg.customobject.CustomObject;
import com.rodriaum.mtg.customobject.bo4.BO4;
import com.rodriaum.mtg.customobject.bo4.BO4Data;
import com.rodriaum.mtg.customobject.resource.CustomStructureResource;
import com.rodriaum.mtg.customobject.structures.bo4.BO4CustomStructure;
import com.rodriaum.mtg.customobject.structures.bo4.BO4CustomStructureCoordinate;
import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IBiomeConfig;
import com.rodriaum.mtg.interfaces.IStructuredCustomObject;
import com.rodriaum.mtg.interfaces.IWorldGenRegion;
import com.rodriaum.mtg.paper.gen.OTGNoiseChunkGenerator;
import com.rodriaum.mtg.paper.gen.PaperWorldGenRegion;
import com.rodriaum.mtg.util.bo3.Rotation;
import com.rodriaum.mtg.util.logging.LogCategory;
import com.rodriaum.mtg.util.logging.LogLevel;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ExportBO4DataCommand extends BaseCommand
{
	private static boolean isRunning = false;
	private static boolean isDone = false;
	private static int current = 0;
	private static int total = 0;
	private static String boName = "";	
	
	public ExportBO4DataCommand()
	{
		super("exportbo4data");
		this.helpMessage = "Exports all BO4 files and BO3 files that have isMTGPlus:true as BO4Data files (if none exist already). BO4Data files can significantly reduce filesize and loading times, and should be used by MTG content creators when packaging presets for players.";
		this.usage = "/mtg exportbo4data";
	}
	
	@Override
	public void build(LiteralArgumentBuilder<CommandSourceStack> builder)
	{
		builder.then(Commands.literal("exportbo4data")
			.executes(context -> exportBO4Data(context.getSource()))
		);
	}

	private int exportBO4Data(CommandSourceStack sender)
	{
		if (!(sender instanceof Player))
		{
			sender.sendSuccess(() -> (Component.literal("\u00a7cOnly players can execute this command!")), false);
			return 0;
		}
		Player player = (Player) sender;

		if (!(sender.getLevel().getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			sender.sendSuccess(() -> (Component.literal("\u00a7cMTG is not enabled in this world!")), false);
			return 0;
		}

		Preset preset = ((OTGNoiseChunkGenerator) sender.getLevel().getChunkSource().getGenerator()).getPreset();
        if(preset.getWorldConfig().getCustomStructureType() == CustomStructureType.BO4)
        {
        	if(!isRunning)
        	{
        		isDone = false;
        		isRunning = true;
        		player.sendMessage("Exporting .BO4Data files for world, this may take a while.");
	        	player.sendMessage("Run this command again to see progress or check the logs.");	        	
        		new Thread(() -> {
		            OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Initializing and exporting structure starts");		            

			        // Make sure all structure starts in the world have been initialised
			        // so that getMinimumSize has been done and its data can be saved with the BO4Data.
			        for(IBiomeConfig biomeConfig : preset.getAllBiomeConfigs())
			        {
			        	for(ConfigFunction<IBiomeConfig> res : ((BiomeConfig)biomeConfig).getResourceQueue())
			        	{
			        		if(res instanceof CustomStructureResource)
			        		{
			        			for(IStructuredCustomObject structure : ((CustomStructureResource)res).getObjects(preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker()))
			        			{
			        				if(structure != null) // Structure was in resource list but file could not be found.
			        				{
			        					if(structure instanceof BO4)
			        					{
			        						if(!BO4Data.bo4DataExists(((BO4)structure).getConfig()))
			        						{
				        	        			BO4CustomStructureCoordinate structureCoord = new BO4CustomStructureCoordinate(preset.getFolderName(), structure, null, Rotation.NORTH, 0, (short)0, 0, 0, false, false, null);
				        	        			BO4CustomStructure structureStart = new BO4CustomStructure(sender.getLevel().getSeed(), structureCoord, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				        	        			
				        	                	// Get minimum size (size if spawned with branchDepth 0)
				        	                	try {
				        	                		// World save folder name may not be identical to level name, fetch it.
				        	                		Path worldSaveFolder = sender.getLevel().getWorld().getWorldFolder().toPath();
				        	                		IWorldGenRegion worldGenRegion = new PaperWorldGenRegion(preset.getFolderName(), preset.getWorldConfig(), sender.getLevel(), (OTGNoiseChunkGenerator)sender.getLevel().getChunkSource().getGenerator());
				        	                		structureStart.getMinimumSize(((OTGNoiseChunkGenerator)sender.getLevel().getChunkSource().getGenerator()).getStructureCache(worldSaveFolder), worldGenRegion, OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				        						}
				        	                	catch (InvalidConfigException e)
				        	                	{
				        							((BO4)structure).isInvalidConfig = true;
				        						}
				        	                	
				        	                	OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Exporting .BO4Data for structure start " + ((BO4)structure).getName());
				        	                	boName = ((BO4)structure).getName();
				        	                	BO4Data.generateBO4Data(((BO4)structure).getConfig(), preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				        	    	            OTG.getEngine().getCustomObjectManager().getGlobalObjects().unloadCustomObjectFiles();
			        						}
			        					}
			        				}
			        			}
			        		}
			        	}
			        }
		
			        ArrayList<String> boNames = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getAllBONamesForPreset(preset.getFolderName(), OTG.getEngine().getLogger(), OTG.getEngine().getOTGRootFolder());
		
			        current = 0;
			        total = boNames.size();
			        for (String boName : boNames)
			        {
			        	current++;
			        	CustomObject bo = OTG.getEngine().getCustomObjectManager().getGlobalObjects().getObjectByName(boName, preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
			        	if(bo != null && bo instanceof BO4 && !BO4Data.bo4DataExists(((BO4)bo).getConfig()))
			        	{
			        		OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Exporting .BO4Data " + current + "/" + total + " " + boName);
			        		BO4Data.generateBO4Data(((BO4)bo).getConfig(), preset.getFolderName(), OTG.getEngine().getOTGRootFolder(), OTG.getEngine().getLogger(), OTG.getEngine().getCustomObjectManager(), OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName()), OTG.getEngine().getCustomObjectResourcesManager(), OTG.getEngine().getModLoadedChecker());
				            OTG.getEngine().getCustomObjectManager().getGlobalObjects().unloadCustomObjectFiles();
			        	}
			        }
			        OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Exporting .BO4Data done.");
			        isDone = true;
		        }).start();
			} else {
				if(isDone)
				{
					isRunning = false;
					isDone = false;
					player.sendMessage("MTG exportbo4data is done.");
				} else {
					player.sendMessage("MTG exportbo4data is running, " + (current == 0 ? "exporting structure start " + boName : " exporting " + current + "/" + total));
				}
			}        
        } else {
        	player.sendMessage("The ExportBO4Data command is only available for CustomStructureType:BO4 worlds.");
        }
        return 0;
	}
	
	@Override
	public String getPermission()
	{
		return "mtg.cmd.exportbo4data";
	}
}
