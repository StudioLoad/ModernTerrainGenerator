package com.pg85.otg.util.minecraft;

// Mapping of legacy configured feature names to the best matching new names.
public class LegacyRegistry {
    public static String convertLegacyResourceLocation(String old) {
        old = old.replace("minecraft:", "");
        switch (old) {
            case "glowstone":
                return "minecraft:glowstone_extra";
            case "warped_fungi":
                return "minecraft:warped_fungus";
            case "spring_delta":
                return "minecraft:delta";
            case "crimson_fungi":
                return "crimson_fungus";
            case "birch_other":
                // 1.21.11 renamed the old mixed birch/oak placement feature.
                return "minecraft:trees_birch_and_oak_leaf_litter";
            case "seagrass_swamp":
            case "seagrass_deep_warm":
            case "seagrass_warm":
            case "seagrass_cold":
            case "seagrass_river":
                return "minecraft:seagrass_simple";
            case "patch_berry_sparse":
                return "minecraft:patch_berry_bush";
            case "bamboo_light":
                return "minecraft:bamboo_no_podzol";
            case "plain_vegetation":
                return "minecraft:trees_plains";
            case "kelp_warm":
            case "kelp_cold":
                return "minecraft:kelp";
            case "forest_flower_trees":
                return "minecraft:trees_flower_forest";
            case "bamboo":
                return "minecraft:bamboo_some_podzol";
            default:
                return null;
        }
    }
}
