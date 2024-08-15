package com.gregtechceu.gtceu.data.recipe.configurable;

import com.gregtechceu.gtceu.api.material.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.material.material.MarkerMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;
import java.util.function.Consumer;

public class RecipeRemoval {

    public static void init(Consumer<ResourceLocation> registry) {
        generalRemovals(registry);
        WoodMachineRecipes.hardWoodRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.disableManualCompression) disableManualCompression(registry);
        if (ConfigHolder.INSTANCE.recipes.harderBrickRecipes) harderBrickRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) hardWoodRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardIronRecipes) hardIronRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) hardRedstoneRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardToolArmorRecipes) hardToolArmorRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardMiscRecipes) hardMiscRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardGlassRecipes) hardGlassRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.nerfPaperCrafting) nerfPaperCrafting(registry);
        if (ConfigHolder.INSTANCE.recipes.hardAdvancedIronRecipes) hardAdvancedIronRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardDyeRecipes) hardDyeRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.flintAndSteelRequireSteel) flintAndSteelRequireSteel(registry);
        if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) removeVanillaBlockRecipes(registry);
    }

    private static void generalRemovals(Consumer<ResourceLocation> registry) {
        if (ConfigHolder.INSTANCE.recipes.removeVanillaTNTRecipe)
            registry.accept(ResourceLocation.withDefaultNamespace("tnt"));

        // todo
        /*
         * // always remove these, GT ore processing changes their output
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.COAL_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.IRON_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.GOLD_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.DIAMOND_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.EMERALD_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.LAPIS_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.REDSTONE_ORE));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.QUARTZ_ORE));
         * 
         * // Remove a bunch of processing recipes for tools and armor, since we have significantly better options
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HELMET, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_CHESTPLATE, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_LEGGINGS, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_BOOTS, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HORSE_ARMOR, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_PICKAXE, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_SHOVEL, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_AXE, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_SWORD, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HOE, 1, W));
         * 
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HELMET, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_CHESTPLATE, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_LEGGINGS, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_BOOTS, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HORSE_ARMOR, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_PICKAXE, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_SHOVEL, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_AXE, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_SWORD, 1, W));
         * ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HOE, 1, W));
         */

        // removed these for parity with the other torch recipes
        registry.accept(ResourceLocation.withDefaultNamespace("soul_torch"));
        registry.accept(ResourceLocation.withDefaultNamespace("soul_lantern"));
    }

    private static void disableManualCompression(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("gold_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("gold_nugget"));
        registry.accept(ResourceLocation.withDefaultNamespace("gold_ingot_from_gold_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("gold_ingot_from_nuggets"));
        registry.accept(ResourceLocation.withDefaultNamespace("coal_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("coal"));
        registry.accept(ResourceLocation.withDefaultNamespace("redstone_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("redstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("emerald_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("emerald"));
        registry.accept(ResourceLocation.withDefaultNamespace("diamond_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("diamond"));
        registry.accept(ResourceLocation.withDefaultNamespace("iron_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("iron_nugget"));
        registry.accept(ResourceLocation.withDefaultNamespace("iron_ingot_from_iron_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("iron_ingot_from_nuggets"));
        registry.accept(ResourceLocation.withDefaultNamespace("lapis_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("lapis_lazuli"));
        registry.accept(ResourceLocation.withDefaultNamespace("quartz_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("clay"));
        registry.accept(ResourceLocation.withDefaultNamespace("nether_brick"));
        registry.accept(ResourceLocation.withDefaultNamespace("glowstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("amethyst_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("copper_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("copper"));
        registry.accept(ResourceLocation.withDefaultNamespace("honeycomb_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("snow_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("netherite_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("netherite_ingot_from_netherite_block"));
    }

    private static void harderBrickRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("brick"));
        registry.accept(ResourceLocation.withDefaultNamespace("bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("nether_brick"));
        registry.accept(ResourceLocation.withDefaultNamespace("nether_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("red_nether_bricks"));
    }

    private static void hardWoodRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("minecraft:ladder"));
        registry.accept(ResourceLocation.withDefaultNamespace("minecraft:bowl"));
        registry.accept(ResourceLocation.withDefaultNamespace("minecraft:chest"));
        registry.accept(ResourceLocation.withDefaultNamespace("minecraft:barrel"));
    }

    private static void hardIronRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("cauldron"));
        registry.accept(ResourceLocation.withDefaultNamespace("hopper"));
        registry.accept(ResourceLocation.withDefaultNamespace("iron_bars"));
        registry.accept(ResourceLocation.withDefaultNamespace("bucket"));
        registry.accept(ResourceLocation.withDefaultNamespace("chain"));
    }

    private static void hardRedstoneRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("dispenser"));
        registry.accept(ResourceLocation.withDefaultNamespace("sticky_piston"));
        registry.accept(ResourceLocation.withDefaultNamespace("piston"));
        registry.accept(ResourceLocation.withDefaultNamespace("lever"));
        registry.accept(ResourceLocation.withDefaultNamespace("daylight_detector"));
        registry.accept(ResourceLocation.withDefaultNamespace("redstone_lamp"));
        registry.accept(ResourceLocation.withDefaultNamespace("tripwire_hook"));
        registry.accept(ResourceLocation.withDefaultNamespace("dropper"));
        registry.accept(ResourceLocation.withDefaultNamespace("observer"));
        registry.accept(ResourceLocation.withDefaultNamespace("repeater"));
        registry.accept(ResourceLocation.withDefaultNamespace("comparator"));
        registry.accept(ResourceLocation.withDefaultNamespace("powered_rail"));
        registry.accept(ResourceLocation.withDefaultNamespace("detector_rail"));
        registry.accept(ResourceLocation.withDefaultNamespace("rail"));
        registry.accept(ResourceLocation.withDefaultNamespace("activator_rail"));
        registry.accept(ResourceLocation.withDefaultNamespace("redstone_torch"));
        registry.accept(ResourceLocation.withDefaultNamespace("stone_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("oak_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("birch_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("spruce_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("jungle_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("acacia_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("dark_oak_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("crimson_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("warped_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("mangrove_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("heavy_weighted_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("light_weighted_pressure_plate"));
        registry.accept(ResourceLocation.withDefaultNamespace("stone_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("oak_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("birch_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("spruce_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("jungle_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("acacia_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("dark_oak_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("crimson_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("warped_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("mangrove_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("cherry_button"));
        registry.accept(ResourceLocation.withDefaultNamespace("bamboo_button"));
    }

    private static void hardToolArmorRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("compass"));
        registry.accept(ResourceLocation.withDefaultNamespace("fishing_rod"));
        registry.accept(ResourceLocation.withDefaultNamespace("clock"));
        registry.accept(ResourceLocation.withDefaultNamespace("shears"));
        registry.accept(ResourceLocation.withDefaultNamespace("shield"));
        for (String type : new String[] { "iron", "golden", "diamond" }) {
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_shovel"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_pickaxe"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_axe"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_sword"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_hoe"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_helmet"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_chestplate"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_leggings"));
            registry.accept(ResourceLocation.withDefaultNamespace(type + "_boots"));
        }
    }

    /**
     * - Removes Vanilla Golden Apple Recipe
     * - Removes Vanilla Ender Eye Recipe
     * - Removes Vanilla Glistering Melon Recipe
     * - Removes Vanilla Golden Carrot Recipe
     * - Removes Vanilla Magma Cream Recipe
     * - Removes Vanilla Polished Stone Variant Recipes
     * - Removes Vanilla Brick Smelting Recipe
     * - Removes Vanilla Fermented Spider Eye recipe
     * - Removes Vanilla Fire Charge recipe
     */
    private static void hardMiscRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("beacon"));
        registry.accept(ResourceLocation.withDefaultNamespace("jack_o_lantern"));
        registry.accept(ResourceLocation.withDefaultNamespace("golden_apple"));
        registry.accept(ResourceLocation.withDefaultNamespace("book"));
        registry.accept(ResourceLocation.withDefaultNamespace("brewing_stand"));
        registry.accept(ResourceLocation.withDefaultNamespace("ender_eye"));
        registry.accept(ResourceLocation.withDefaultNamespace("glistering_melon_slice"));
        registry.accept(ResourceLocation.withDefaultNamespace("golden_carrot"));
        registry.accept(ResourceLocation.withDefaultNamespace("magma_cream"));
        registry.accept(ResourceLocation.withDefaultNamespace("enchanting_table"));
        registry.accept(ResourceLocation.withDefaultNamespace("jukebox"));
        registry.accept(ResourceLocation.withDefaultNamespace("note_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("furnace"));
        registry.accept(ResourceLocation.withDefaultNamespace("crafting_table"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_granite"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_diorite"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_andesite"));
        registry.accept(ResourceLocation.withDefaultNamespace("lead"));
        registry.accept(ResourceLocation.withDefaultNamespace("bow"));
        registry.accept(ResourceLocation.withDefaultNamespace("item_frame"));
        registry.accept(ResourceLocation.withDefaultNamespace("painting"));
        registry.accept(ResourceLocation.withDefaultNamespace("chest_minecart"));
        registry.accept(ResourceLocation.withDefaultNamespace("furnace_minecart"));
        registry.accept(ResourceLocation.withDefaultNamespace("tnt_minecart"));
        registry.accept(ResourceLocation.withDefaultNamespace("hopper_minecart"));
        registry.accept(ResourceLocation.withDefaultNamespace("flower_pot"));
        registry.accept(ResourceLocation.withDefaultNamespace("armor_stand"));
        registry.accept(ResourceLocation.withDefaultNamespace("trapped_chest"));
        registry.accept(ResourceLocation.withDefaultNamespace("ender_chest"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(ResourceLocation.withDefaultNamespace(color.getName() + "_bed"));
        }
        registry.accept(ResourceLocation.withDefaultNamespace("fermented_spider_eye"));
        registry.accept(ResourceLocation.withDefaultNamespace("fire_charge"));
        // All items from here downward need to be checked for if they belong to miscRecipes or
        // removeVanillaBlockRecipes
        registry.accept(ResourceLocation.withDefaultNamespace("lantern"));
        registry.accept(ResourceLocation.withDefaultNamespace("tinted_glass"));
        registry.accept(ResourceLocation.withDefaultNamespace("stonecutter"));
        registry.accept(ResourceLocation.withDefaultNamespace("cartography_table"));
        registry.accept(ResourceLocation.withDefaultNamespace("fletching_table"));
        registry.accept(ResourceLocation.withDefaultNamespace("smithing_table"));
        registry.accept(ResourceLocation.withDefaultNamespace("grindstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("smoker"));
        registry.accept(ResourceLocation.withDefaultNamespace("blast_furnace"));
        registry.accept(ResourceLocation.withDefaultNamespace("loom"));
        registry.accept(ResourceLocation.withDefaultNamespace("composter"));
        registry.accept(ResourceLocation.withDefaultNamespace("bell"));
        registry.accept(ResourceLocation.withDefaultNamespace("conduit"));
        registry.accept(ResourceLocation.withDefaultNamespace("candle"));
        registry.accept(ResourceLocation.withDefaultNamespace("scaffolding"));
        registry.accept(ResourceLocation.withDefaultNamespace("beehive"));
        registry.accept(ResourceLocation.withDefaultNamespace("lightning_rod"));
        registry.accept(ResourceLocation.withDefaultNamespace("lectern"));
        registry.accept(ResourceLocation.withDefaultNamespace("music_disc_5"));
        registry.accept(ResourceLocation.withDefaultNamespace("turtle_helmet"));
        registry.accept(ResourceLocation.withDefaultNamespace("brush"));
        registry.accept(ResourceLocation.withDefaultNamespace("recovery_compass"));
        registry.accept(ResourceLocation.withDefaultNamespace("spyglass"));
    }

    private static void hardGlassRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("glass"));
        registry.accept(ResourceLocation.withDefaultNamespace("glass_bottle"));
        registry.accept(ResourceLocation.withDefaultNamespace("glass_pane"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(ResourceLocation.withDefaultNamespace(String.format("%s_stained_glass_pane_from_glass_pane",
                    color.name().toLowerCase(Locale.ROOT))));
            registry.accept(ResourceLocation.withDefaultNamespace(
                    String.format("%s_stained_glass_pane", color.name().toLowerCase(Locale.ROOT))));
        }
    }

    private static void nerfPaperCrafting(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("paper"));
        registry.accept(ResourceLocation.withDefaultNamespace("sugar_from_sugar_cane"));
    }

    private static void hardAdvancedIronRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("iron_door"));
        registry.accept(ResourceLocation.withDefaultNamespace("anvil"));
        registry.accept(ResourceLocation.withDefaultNamespace("iron_trapdoor"));
        registry.accept(ResourceLocation.withDefaultNamespace("minecart"));
    }

    private static void hardDyeRecipes(Consumer<ResourceLocation> registry) {
        for (MarkerMaterial colorMaterial : MarkerMaterials.Color.VALUES) {
            registry.accept(
                    ResourceLocation.withDefaultNamespace(String.format("%s_concrete_powder", colorMaterial.getName())));
            registry.accept(ResourceLocation.withDefaultNamespace(String.format("%s_terracotta", colorMaterial.getName())));
            registry.accept(ResourceLocation.withDefaultNamespace(String.format("%s_stained_glass", colorMaterial.getName())));
            registry.accept(ResourceLocation.withDefaultNamespace(String.format("%s_candle", colorMaterial.getName())));
            if (colorMaterial != MarkerMaterials.Color.White) {
                registry.accept(
                        ResourceLocation.withDefaultNamespace(String.format("%s_wool", colorMaterial.getName())));
            }
        }
        registry.accept(ResourceLocation.withDefaultNamespace("dark_prismarine"));
    }

    private static void flintAndSteelRequireSteel(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("flint_and_steel"));
    }

    private static void removeVanillaBlockRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(ResourceLocation.withDefaultNamespace("slime_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("slime_ball"));
        registry.accept(ResourceLocation.withDefaultNamespace("melon"));
        registry.accept(ResourceLocation.withDefaultNamespace("hay_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("wheat"));
        registry.accept(ResourceLocation.withDefaultNamespace("magma_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("nether_wart_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("bone_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("bone_meal_from_bone_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("honey_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("purpur_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("prismarine_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("prismarine"));
        registry.accept(ResourceLocation.withDefaultNamespace("snow_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("sandstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_andesite"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_diorite"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_granite"));
        registry.accept(ResourceLocation.withDefaultNamespace("coarse_dirt"));
        registry.accept(ResourceLocation.withDefaultNamespace("smooth_sandstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("chiseled_sandstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("chiseled_quartz_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("stone_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("chiseled_stone_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("purpur_pillar"));
        registry.accept(ResourceLocation.withDefaultNamespace("end_stone_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("red_nether_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("red_sandstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("chiseled_red_sandstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("smooth_red_sandstone"));
        registry.accept(ResourceLocation.withDefaultNamespace("bookshelf"));
        registry.accept(ResourceLocation.withDefaultNamespace("chiseled_bookshelf"));
        registry.accept(ResourceLocation.withDefaultNamespace("quartz_pillar"));
        registry.accept(ResourceLocation.withDefaultNamespace("sea_lantern"));
        // TODO Add extruder/laser engraver recipes for all vanilla stones to keep parity with GT stones
        registry.accept(ResourceLocation.withDefaultNamespace("cracked_stone_bricks"));
        registry.accept(ResourceLocation.withDefaultNamespace("mossy_cobblestone_from_moss_block"));
        registry.accept(ResourceLocation.withDefaultNamespace("mossy_cobblestone_from_vine"));
        // registry.accept(ResourceLocation.withDefaultNamespace("deepslate_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("cracked_nether_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("chiseled_nether_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("polished_blackstone_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("cracked_polished_blackstone_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("quartz_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("polished_deepslate"));
        // registry.accept(ResourceLocation.withDefaultNamespace("polished_basalt"));
        // registry.accept(ResourceLocation.withDefaultNamespace("chiseled_polished_blackstone"));
        // registry.accept(ResourceLocation.withDefaultNamespace("deepslate_tiles"));
        // registry.accept(ResourceLocation.withDefaultNamespace("cracked_deepslate_tiles"));
        // registry.accept(ResourceLocation.withDefaultNamespace("chiseled_deepslate"));
        // registry.accept(ResourceLocation.withDefaultNamespace("cracked_deepslate_bricks"));
        // registry.accept(ResourceLocation.withDefaultNamespace("cut_red_sandstone"));
        // registry.accept(ResourceLocation.withDefaultNamespace("polished_basalt"));
        // registry.accept(ResourceLocation.withDefaultNamespace("polished_blackstone"));
        // registry.accept(ResourceLocation.withDefaultNamespace("cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("exposed_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("weathered_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("oxidized_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("waxed_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("waxed_exposed_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("waxed_weathered_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("waxed_oxidized_cut_copper"));
        // registry.accept(ResourceLocation.withDefaultNamespace("end_crystal"));
        // registry.accept(ResourceLocation.withDefaultNamespace("end_rod")); // wait for approval before uncommenting this one
        // registry.accept(ResourceLocation.withDefaultNamespace("mud_bricks")); //no other way to obtain these rn

        // Carpet replacement
        for (DyeColor color : DyeColor.values()) {
            registry.accept(ResourceLocation.withDefaultNamespace(String.format("%s_carpet",
                    color.name().toLowerCase(Locale.ROOT))));
        }

        // Slab replacement
        registry.accept(ResourceLocation.withDefaultNamespace("stone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("smooth_stone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("andesite_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("granite_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("diorite_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_andesite_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_granite_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_diorite_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("smooth_sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("red_sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("smooth_red_sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("cobblestone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("blackstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_blackstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_blackstone_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("stone_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("mud_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("nether_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("red_nether_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("quartz_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("smooth_quartz_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("exposed_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("oxidized_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("weathered_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("waxed_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("waxed_exposed_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("waxed_oxidized_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("waxed_weathered_cut_copper_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("red_sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("purpur_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("end_stone_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("prismarine_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("prismarine_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("dark_prismarine_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("mossy_cobblestone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("mossy_stone_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("cut_sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("cut_red_sandstone_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("bamboo_mosaic_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("cobbled_deepslate_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("polished_deepslate_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("deepslate_brick_slab"));
        registry.accept(ResourceLocation.withDefaultNamespace("deepslate_tile_slab"));
    }
}
