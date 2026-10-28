package com.rodriaum.mtg.paper.events;

import com.rodriaum.mtg.core.OTG;
import com.rodriaum.mtg.core.presets.Preset;
import com.rodriaum.mtg.customobject.CustomObjectManager;
import com.rodriaum.mtg.customobject.config.CustomObjectResourcesManager;
import com.rodriaum.mtg.customobject.resource.SaplingResource;
import com.rodriaum.mtg.interfaces.*;
import com.rodriaum.mtg.paper.gen.OTGPaperChunkGen;
import com.rodriaum.mtg.paper.gen.PaperWorldGenRegion;
import com.rodriaum.mtg.paper.materials.PaperMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterialData;
import com.rodriaum.mtg.util.materials.LocalMaterials;
import com.rodriaum.mtg.util.minecraft.SaplingType;
import net.minecraft.core.BlockPos;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.world.StructureGrowEvent;

import java.nio.file.Path;
import java.util.Random;

public class SaplingHandler {

    void onStructureGrow(StructureGrowEvent event) {
        if (!(event.getWorld() instanceof CraftWorld craftWorld)) {
            return;
        }

        if (!(event.getWorld().getGenerator() instanceof OTGPaperChunkGen chunkGen)) {
            return;
        }

        Preset preset = chunkGen.generator.getPreset();
        PaperWorldGenRegion worldGenRegion = new PaperWorldGenRegion(
                preset.getFolderName(),
                preset.getWorldConfig(),
                craftWorld.getHandle(),
                chunkGen.generator
        );

        BlockPos blockPos = new BlockPos(
                event.getLocation().getBlockX(),
                event.getLocation().getBlockY(),
                event.getLocation().getBlockZ()
        );

        CustomObjectManager customObjectManager = OTG.getEngine().getCustomObjectManager();
        CustomObjectResourcesManager customObjectResourcesManager =
                OTG.getEngine().getCustomObjectResourcesManager();
        ILogger logger = OTG.getEngine().getLogger();
        Path otgRootFolder = OTG.getEngine().getOTGRootFolder();
        IModLoadedChecker modLoadedChecker = OTG.getEngine().getModLoadedChecker();
        IMaterialReader materialReader =
                OTG.getEngine().getPresetLoader().getMaterialReader(preset.getFolderName());

        IBiomeConfig biomeConfig = worldGenRegion.getCachedBiomeProvider()
                .getBiomeConfig(blockPos.getX(), blockPos.getZ());

        // The event location may not resolve to a valid OTG material.
        LocalMaterialData material = worldGenRegion.getMaterial(
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ()
        );

        if (material == null) {
            return;
        }

        BlockPos result = findFourSaplings(blockPos, material, worldGenRegion);
        boolean wideTrunk = result != null;

        if (wideTrunk) {
            blockPos = result;
        }

        // Keep the Bukkit/Paper-specific conversion only where it is needed.
        if (!(material instanceof PaperMaterialData paperMaterial)) {
            return;
        }

        ISaplingSpawner sapling = biomeConfig.getCustomSaplingGen(
                PaperMaterialData.ofBlockData(
                        paperMaterial.internalBlock().getBlock().defaultBlockState()
                ),
                wideTrunk
        );

        if (sapling != null && sapling.hasWideTrunk() && !wideTrunk) {
            return;
        }

        if (sapling == null) {
            SaplingType saplingType = null;
            LocalMaterialData normalizedMaterial = PaperMaterialData.ofBlockData(
                    paperMaterial.internalBlock().getBlock().defaultBlockState()
            );

            if (wideTrunk) {
                saplingType = getBigSaplingType(normalizedMaterial);
            }

            if (saplingType == null) {
                saplingType = getSmallSaplingType(normalizedMaterial);
            }

            if (saplingType != null) {
                sapling = biomeConfig.getSaplingGen(saplingType);
            }
        }

        if (sapling == null) {
            return;
        }

        // OTG handles this growth itself.
        event.setCancelled(true);

        // Remove the sapling(s) before attempting custom growth.
        if (sapling.hasWideTrunk()) {
            worldGenRegion.setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ(), LocalMaterials.AIR);
            worldGenRegion.setBlock(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ(), LocalMaterials.AIR);
            worldGenRegion.setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 1, LocalMaterials.AIR);
            worldGenRegion.setBlock(blockPos.getX() + 1, blockPos.getY(), blockPos.getZ() + 1, LocalMaterials.AIR);
        } else {
            worldGenRegion.setBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ(), LocalMaterials.AIR);
        }

        boolean saplingGrown = false;
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            if (((SaplingResource) sapling).growSapling(
                    worldGenRegion,
                    random,
                    wideTrunk,
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ(),
                    preset.getFolderName(),
                    otgRootFolder,
                    logger,
                    customObjectManager,
                    materialReader,
                    customObjectResourcesManager,
                    modLoadedChecker
            )) {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown) {
            int saplingX = blockPos.getX();
            int saplingY = blockPos.getY();
            int saplingZ = blockPos.getZ();

            if (sapling.hasWideTrunk()) {
                worldGenRegion.setBlock(saplingX, saplingY, saplingZ, material);
                worldGenRegion.setBlock(saplingX + 1, saplingY, saplingZ, material);
                worldGenRegion.setBlock(saplingX, saplingY, saplingZ + 1, material);
                worldGenRegion.setBlock(saplingX + 1, saplingY, saplingZ + 1, material);
            } else {
                worldGenRegion.setBlock(saplingX, saplingY, saplingZ, material);
            }
        }
    }

    /**
     * Gets the sapling type, based on the assumption that the sapling is
     * not placed in a 2x2 pattern.
     */
    private SaplingType getSmallSaplingType(LocalMaterialData saplingMaterial) {
        if (saplingMaterial == null) {
            return null;
        }

        if (saplingMaterial == LocalMaterials.OAK_SAPLING) {
            return SaplingType.Oak;
        } else if (saplingMaterial == LocalMaterials.SPRUCE_SAPLING) {
            return SaplingType.Redwood;
        } else if (saplingMaterial == LocalMaterials.BIRCH_SAPLING) {
            return SaplingType.Birch;
        } else if (saplingMaterial == LocalMaterials.JUNGLE_SAPLING) {
            return SaplingType.SmallJungle;
        } else if (saplingMaterial == LocalMaterials.ACACIA_SAPLING) {
            return SaplingType.Acacia;
        } else if (saplingMaterial == LocalMaterials.BAMBOO_SAPLING) {
            return SaplingType.Bamboo;
        }

        return null;
    }

    /**
     * Gets the sapling type, based on the assumption that the saplings must
     * be placed in a 2x2 pattern. Will never return one of the smaller sapling types.
     */
    private SaplingType getBigSaplingType(LocalMaterialData saplingMaterial) {
        if (saplingMaterial == null) {
            return null;
        }

        if (saplingMaterial == LocalMaterials.DARK_OAK_SAPLING) {
            return SaplingType.DarkOak;
        } else if (saplingMaterial == LocalMaterials.SPRUCE_SAPLING) {
            return SaplingType.HugeRedwood;
        } else if (saplingMaterial == LocalMaterials.JUNGLE_SAPLING) {
            return SaplingType.BigJungle;
        }

        return null;
    }

    /**
     * Checks whether four saplings are placed in a 2x2 pattern.
     * Returns the sapling at the lowest X and Z coordinates, or null if no
     * matching 2x2 pattern exists.
     */
    private BlockPos findFourSaplings(
            BlockPos blockPos,
            LocalMaterialData material,
            IWorldGenRegion worldGenRegion
    ) {
        if (material == null) {
            return null;
        }

        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        for (int treeOffsetX = 0; treeOffsetX >= -1; --treeOffsetX) {
            for (int treeOffsetZ = 0; treeOffsetZ >= -1; --treeOffsetZ) {
                LocalMaterialData first = worldGenRegion.getMaterial(
                        x + treeOffsetX, y, z + treeOffsetZ
                );
                LocalMaterialData second = worldGenRegion.getMaterial(
                        x + treeOffsetX + 1, y, z + treeOffsetZ
                );
                LocalMaterialData third = worldGenRegion.getMaterial(
                        x + treeOffsetX, y, z + treeOffsetZ + 1
                );
                LocalMaterialData fourth = worldGenRegion.getMaterial(
                        x + treeOffsetX + 1, y, z + treeOffsetZ + 1
                );

                // A missing material cannot be considered a matching sapling.
                if (first != null && second != null && third != null && fourth != null
                        && material.isMaterial(first)
                        && material.isMaterial(second)
                        && material.isMaterial(third)
                        && material.isMaterial(fourth)) {
                    return blockPos.offset(treeOffsetX, 0, treeOffsetZ);
                }
            }
        }

        return null;
    }
}
