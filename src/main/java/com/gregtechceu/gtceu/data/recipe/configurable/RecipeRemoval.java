package com.gregtechceu.gtceu.data.recipe.configurable;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
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
            registry.accept(new ResourceLocation("minecraft:tnt"));

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
        registry.accept(new ResourceLocation("minecraft:soul_torch"));
        registry.accept(new ResourceLocation("minecraft:soul_lantern"));
        registry.accept(new ResourceLocation("minecraft:leather_horse_armor"));

        // remove vanilla dye recipes to gregify
        registry.accept(new ResourceLocation("minecraft:white_dye"));
    }

    /**
     * Remove recipes for any item that is 4x4 or 9x9 crafting (nuggets <-> ingot, ingot <-> block, etc.)
     */
    private static void disableManualCompression(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:gold_block"));
        registry.accept(new ResourceLocation("minecraft:gold_nugget"));
        registry.accept(new ResourceLocation("minecraft:gold_ingot_from_gold_block"));
        registry.accept(new ResourceLocation("minecraft:gold_ingot_from_nuggets"));
        registry.accept(new ResourceLocation("minecraft:coal_block"));
        registry.accept(new ResourceLocation("minecraft:coal"));
        registry.accept(new ResourceLocation("minecraft:redstone_block"));
        registry.accept(new ResourceLocation("minecraft:redstone"));
        registry.accept(new ResourceLocation("minecraft:emerald_block"));
        registry.accept(new ResourceLocation("minecraft:emerald"));
        registry.accept(new ResourceLocation("minecraft:diamond_block"));
        registry.accept(new ResourceLocation("minecraft:diamond"));
        registry.accept(new ResourceLocation("minecraft:iron_block"));
        registry.accept(new ResourceLocation("minecraft:iron_nugget"));
        registry.accept(new ResourceLocation("minecraft:iron_ingot_from_iron_block"));
        registry.accept(new ResourceLocation("minecraft:iron_ingot_from_nuggets"));
        registry.accept(new ResourceLocation("minecraft:lapis_block"));
        registry.accept(new ResourceLocation("minecraft:lapis_lazuli"));
        registry.accept(new ResourceLocation("minecraft:quartz_block"));
        registry.accept(new ResourceLocation("minecraft:clay"));
        registry.accept(new ResourceLocation("minecraft:nether_brick"));
        registry.accept(new ResourceLocation("minecraft:glowstone"));
        registry.accept(new ResourceLocation("minecraft:amethyst_block"));
        registry.accept(new ResourceLocation("minecraft:copper_block"));
        registry.accept(new ResourceLocation("minecraft:copper_ingot"));
        registry.accept(new ResourceLocation("minecraft:copper_ingot_from_waxed_copper_block"));
        registry.accept(new ResourceLocation("minecraft:honeycomb_block"));
        registry.accept(new ResourceLocation("minecraft:snow_block"));
        registry.accept(new ResourceLocation("minecraft:netherite_block"));
        registry.accept(new ResourceLocation("minecraft:netherite_ingot_from_netherite_block"));
        registry.accept(new ResourceLocation("minecraft:dripstone_block"));
    }

    private static void harderBrickRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:brick"));
        registry.accept(new ResourceLocation("minecraft:bricks"));
        registry.accept(new ResourceLocation("minecraft:nether_brick"));
        registry.accept(new ResourceLocation("minecraft:nether_bricks"));
        registry.accept(new ResourceLocation("minecraft:red_nether_bricks"));
    }

    private static void hardWoodRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:ladder"));
        registry.accept(new ResourceLocation("minecraft:bowl"));
        registry.accept(new ResourceLocation("minecraft:chest"));
        registry.accept(new ResourceLocation("minecraft:barrel"));
    }

    private static void hardIronRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:cauldron"));
        registry.accept(new ResourceLocation("minecraft:hopper"));
        registry.accept(new ResourceLocation("minecraft:iron_bars"));
        registry.accept(new ResourceLocation("minecraft:bucket"));
        registry.accept(new ResourceLocation("minecraft:chain"));
    }

    private static void hardRedstoneRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:dispenser"));
        registry.accept(new ResourceLocation("minecraft:sticky_piston"));
        registry.accept(new ResourceLocation("minecraft:piston"));
        registry.accept(new ResourceLocation("minecraft:lever"));
        registry.accept(new ResourceLocation("minecraft:daylight_detector"));
        registry.accept(new ResourceLocation("minecraft:redstone_lamp"));
        registry.accept(new ResourceLocation("minecraft:tripwire_hook"));
        registry.accept(new ResourceLocation("minecraft:dropper"));
        registry.accept(new ResourceLocation("minecraft:observer"));
        registry.accept(new ResourceLocation("minecraft:repeater"));
        registry.accept(new ResourceLocation("minecraft:comparator"));
        registry.accept(new ResourceLocation("minecraft:powered_rail"));
        registry.accept(new ResourceLocation("minecraft:detector_rail"));
        registry.accept(new ResourceLocation("minecraft:rail"));
        registry.accept(new ResourceLocation("minecraft:activator_rail"));
        registry.accept(new ResourceLocation("minecraft:redstone_torch"));
        registry.accept(new ResourceLocation("minecraft:stone_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:heavy_weighted_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:light_weighted_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:stone_button"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_button"));
        registry.accept(new ResourceLocation("minecraft:calibrated_sculk_sensor"));
    }

    private static void hardToolArmorRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:compass"));
        registry.accept(new ResourceLocation("minecraft:fishing_rod"));
        registry.accept(new ResourceLocation("minecraft:clock"));
        registry.accept(new ResourceLocation("minecraft:shears"));
        registry.accept(new ResourceLocation("minecraft:shield"));
        registry.accept(new ResourceLocation("minecraft:crossbow"));
        registry.accept(new ResourceLocation("minecraft:bow"));
        for (String type : new String[] { "iron", "golden", "diamond" }) {
            registry.accept(new ResourceLocation("minecraft:" + type + "_shovel"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_pickaxe"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_axe"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_sword"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_hoe"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_helmet"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_chestplate"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_leggings"));
            registry.accept(new ResourceLocation("minecraft:" + type + "_boots"));
        }
    }

    /**
     * Remove recipes for items that don't fit in any other config option.
     * Vanilla items go here only if they not fit the criteria for removeVanillaBlockRecipes,
     * disableManualCompression, or any of the other config options
     */
    private static void hardMiscRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:jack_o_lantern"));
        registry.accept(new ResourceLocation("minecraft:beacon"));
        registry.accept(new ResourceLocation("minecraft:respawn_anchor"));
        registry.accept(new ResourceLocation("minecraft:chiseled_bookshelf"));
        registry.accept(new ResourceLocation("minecraft:brewing_stand"));
        registry.accept(new ResourceLocation("minecraft:enchanting_table"));
        registry.accept(new ResourceLocation("minecraft:jukebox"));
        registry.accept(new ResourceLocation("minecraft:note_block"));
        registry.accept(new ResourceLocation("minecraft:furnace"));
        registry.accept(new ResourceLocation("minecraft:crafting_table"));
        registry.accept(new ResourceLocation("minecraft:flower_pot"));
        registry.accept(new ResourceLocation("minecraft:armor_stand"));
        registry.accept(new ResourceLocation("minecraft:trapped_chest"));
        registry.accept(new ResourceLocation("minecraft:ender_chest"));
        registry.accept(new ResourceLocation("minecraft:lantern"));
        registry.accept(new ResourceLocation("minecraft:stonecutter"));
        registry.accept(new ResourceLocation("minecraft:cartography_table"));
        registry.accept(new ResourceLocation("minecraft:fletching_table"));
        registry.accept(new ResourceLocation("minecraft:smithing_table"));
        registry.accept(new ResourceLocation("minecraft:grindstone"));
        registry.accept(new ResourceLocation("minecraft:smoker"));
        registry.accept(new ResourceLocation("minecraft:blast_furnace"));
        registry.accept(new ResourceLocation("minecraft:loom"));
        registry.accept(new ResourceLocation("minecraft:composter"));
        registry.accept(new ResourceLocation("minecraft:bell"));
        registry.accept(new ResourceLocation("minecraft:conduit"));
        registry.accept(new ResourceLocation("minecraft:candle"));
        registry.accept(new ResourceLocation("minecraft:scaffolding"));
        registry.accept(new ResourceLocation("minecraft:beehive"));
        registry.accept(new ResourceLocation("minecraft:lightning_rod"));
        registry.accept(new ResourceLocation("minecraft:lectern"));
        registry.accept(new ResourceLocation("minecraft:golden_apple"));
        registry.accept(new ResourceLocation("minecraft:book"));
        registry.accept(new ResourceLocation("minecraft:ender_eye"));
        registry.accept(new ResourceLocation("minecraft:glistering_melon_slice"));
        registry.accept(new ResourceLocation("minecraft:golden_carrot"));
        registry.accept(new ResourceLocation("minecraft:magma_cream"));
        registry.accept(new ResourceLocation("minecraft:lead"));
        registry.accept(new ResourceLocation("minecraft:item_frame"));
        registry.accept(new ResourceLocation("minecraft:painting"));
        registry.accept(new ResourceLocation("minecraft:chest_minecart"));
        registry.accept(new ResourceLocation("minecraft:furnace_minecart"));
        registry.accept(new ResourceLocation("minecraft:tnt_minecart"));
        registry.accept(new ResourceLocation("minecraft:hopper_minecart"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(new ResourceLocation(color.getName() + "_bed"));
        }
        registry.accept(new ResourceLocation("minecraft:fermented_spider_eye"));
        registry.accept(new ResourceLocation("minecraft:fire_charge"));
        registry.accept(new ResourceLocation("minecraft:music_disc_5"));
        registry.accept(new ResourceLocation("minecraft:turtle_helmet"));
        registry.accept(new ResourceLocation("minecraft:brush"));
        registry.accept(new ResourceLocation("minecraft:recovery_compass"));
        registry.accept(new ResourceLocation("minecraft:spyglass"));
        registry.accept(new ResourceLocation("minecraft:respawn_anchor"));
        registry.accept(new ResourceLocation("minecraft:lodestone"));
        registry.accept(new ResourceLocation("minecraft:chiseled_bookshelf"));
        registry.accept(new ResourceLocation("minecraft:bread"));
        registry.accept(new ResourceLocation("minecraft:cake"));
        registry.accept(new ResourceLocation("minecraft:cookie"));
        registry.accept(new ResourceLocation("minecraft:pumpkin_pie"));
    }

    private static void hardGlassRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:glass"));
        registry.accept(new ResourceLocation("minecraft:glass_bottle"));
        registry.accept(new ResourceLocation("minecraft:glass_pane"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(new ResourceLocation(String.format("minecraft:%s_stained_glass_pane_from_glass_pane",
                    color.name().toLowerCase(Locale.ROOT))));
            registry.accept(new ResourceLocation(
                    String.format("minecraft:%s_stained_glass_pane", color.name().toLowerCase(Locale.ROOT))));
        }
        registry.accept(new ResourceLocation("minecraft:tinted_glass"));
    }

    private static void nerfPaperCrafting(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:paper"));
        registry.accept(new ResourceLocation("minecraft:sugar_from_sugar_cane"));
    }

    private static void hardAdvancedIronRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:iron_door"));
        registry.accept(new ResourceLocation("minecraft:anvil"));
        registry.accept(new ResourceLocation("minecraft:iron_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:minecart"));
    }

    private static void hardDyeRecipes(Consumer<ResourceLocation> registry) {
        for (MarkerMaterial colorMaterial : MarkerMaterials.Color.VALUES) {
            registry.accept(
                    new ResourceLocation(String.format("minecraft:%s_concrete_powder", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:%s_terracotta", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:%s_stained_glass", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:%s_candle", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:dye_%s_wool", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:dye_%s_carpet", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:dye_%s_bed", colorMaterial.getName())));
            registry.accept(new ResourceLocation("minecraft:black_dye"));
            registry.accept(new ResourceLocation("black_dye_from_wither_rose"));
            registry.accept(new ResourceLocation("blue_dye"));
            registry.accept(new ResourceLocation("white_dye_from_lily_of_the_valley"));
            registry.accept(new ResourceLocation("light_blue_dye_from_blue_orchid"));
            registry.accept(new ResourceLocation("yellow_dye_from_dandelion"));
            registry.accept(new ResourceLocation("light_gray_dye_from_white_tulip"));
            registry.accept(new ResourceLocation("light_gray_dye_from_azure_bluet"));
            registry.accept(new ResourceLocation("red_dye_from_poppy"));
            registry.accept(new ResourceLocation("red_dye_from_tulip"));
            registry.accept(new ResourceLocation("red_dye_from_rose_bush"));
            registry.accept(new ResourceLocation("red_dye_from_beetroot"));
            registry.accept(new ResourceLocation("orange_dye_from_orange_tulip"));
            registry.accept(new ResourceLocation("orange_dye_from_torchflower"));
            registry.accept(new ResourceLocation("yellow_dye_from_dandelion"));
            registry.accept(new ResourceLocation("cyan_dye_from_pitcher_plant"));
            registry.accept(new ResourceLocation("light_blue_dye_from_blue_orchid"));
            registry.accept(new ResourceLocation("blue_dye_from_cornflower"));
            registry.accept(new ResourceLocation("magenta_dye_from_allium"));
            registry.accept(new ResourceLocation("magenta_dye_from_lilac"));
            registry.accept(new ResourceLocation("lime_dye_from_lime"));
            registry.accept(new ResourceLocation("pink_dye_from_pink_tulip"));
            registry.accept(new ResourceLocation("pink_dye_from_pink_petals"));
            registry.accept(new ResourceLocation("pink_dye_from_peony"));
            registry.accept(new ResourceLocation("yellow_dye_from_sunflower"));
            registry.accept(new ResourceLocation("light_gray_dye_from_oxeye_daisy"));
        }
        registry.accept(new ResourceLocation("minecraft:dark_prismarine"));
    }

    private static void flintAndSteelRequireSteel(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:flint_and_steel"));
    }

    /**
     * Removes the vanilla recipe for an item that would have BOTH a normal recipe as well as a GT recipe in
     * normal recipe configs (think stairs, ladders, etc. having a crafting table recipe as well as a machine recipe)
     */
    private static void removeVanillaBlockRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:dripstone_block"));
        registry.accept(new ResourceLocation("minecraft:polished_granite"));
        registry.accept(new ResourceLocation("minecraft:polished_diorite"));
        registry.accept(new ResourceLocation("minecraft:polished_andesite"));
        registry.accept(new ResourceLocation("minecraft:packed_ice"));
        registry.accept(new ResourceLocation("minecraft:blue_ice"));
        registry.accept(new ResourceLocation("minecraft:slime_block"));
        registry.accept(new ResourceLocation("minecraft:slime_ball"));
        registry.accept(new ResourceLocation("minecraft:melon"));
        registry.accept(new ResourceLocation("minecraft:hay_block"));
        registry.accept(new ResourceLocation("minecraft:wheat"));
        registry.accept(new ResourceLocation("minecraft:magma_block"));
        registry.accept(new ResourceLocation("minecraft:nether_wart_block"));
        registry.accept(new ResourceLocation("minecraft:bone_block"));
        registry.accept(new ResourceLocation("minecraft:bone_meal_from_bone_block"));
        registry.accept(new ResourceLocation("minecraft:honey_block"));
        registry.accept(new ResourceLocation("minecraft:purpur_block"));
        registry.accept(new ResourceLocation("minecraft:prismarine_bricks"));
        registry.accept(new ResourceLocation("minecraft:prismarine"));
        registry.accept(new ResourceLocation("minecraft:snow_block"));
        registry.accept(new ResourceLocation("minecraft:sandstone"));
        registry.accept(new ResourceLocation("minecraft:polished_andesite"));
        registry.accept(new ResourceLocation("minecraft:polished_diorite"));
        registry.accept(new ResourceLocation("minecraft:polished_granite"));
        registry.accept(new ResourceLocation("minecraft:coarse_dirt"));
        registry.accept(new ResourceLocation("minecraft:chiseled_sandstone"));
        registry.accept(new ResourceLocation("minecraft:chiseled_quartz_block"));
        registry.accept(new ResourceLocation("minecraft:stone_bricks"));
        registry.accept(new ResourceLocation("minecraft:chiseled_stone_bricks"));
        registry.accept(new ResourceLocation("minecraft:purpur_pillar"));
        registry.accept(new ResourceLocation("minecraft:end_stone_bricks"));
        registry.accept(new ResourceLocation("minecraft:red_nether_bricks"));
        registry.accept(new ResourceLocation("minecraft:red_sandstone"));
        registry.accept(new ResourceLocation("minecraft:chiseled_red_sandstone"));
        registry.accept(new ResourceLocation("minecraft:bookshelf"));
        registry.accept(new ResourceLocation("minecraft:quartz_pillar"));
        registry.accept(new ResourceLocation("minecraft:sea_lantern"));
        registry.accept(new ResourceLocation("minecraft:white_wool_from_string"));
        registry.accept(new ResourceLocation("minecraft:cracked_stone_bricks"));
        registry.accept(new ResourceLocation("minecraft:mossy_cobblestone_from_moss_block"));
        registry.accept(new ResourceLocation("minecraft:mossy_cobblestone_from_vine"));
        registry.accept(new ResourceLocation("minecraft:deepslate_bricks"));
        registry.accept(new ResourceLocation("minecraft:cracked_nether_bricks"));
        registry.accept(new ResourceLocation("minecraft:chiseled_nether_bricks"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_bricks"));
        registry.accept(new ResourceLocation("minecraft:cracked_polished_blackstone_bricks"));
        registry.accept(new ResourceLocation("minecraft:quartz_bricks"));
        registry.accept(new ResourceLocation("minecraft:polished_deepslate"));
        registry.accept(new ResourceLocation("minecraft:polished_basalt"));
        registry.accept(new ResourceLocation("minecraft:chiseled_polished_blackstone"));
        registry.accept(new ResourceLocation("minecraft:deepslate_tiles"));
        registry.accept(new ResourceLocation("minecraft:cracked_deepslate_tiles"));
        registry.accept(new ResourceLocation("minecraft:chiseled_deepslate"));
        registry.accept(new ResourceLocation("minecraft:cracked_deepslate_bricks"));
        registry.accept(new ResourceLocation("minecraft:cut_red_sandstone"));
        registry.accept(new ResourceLocation("minecraft:polished_basalt"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone"));
        registry.accept(new ResourceLocation("minecraft:cut_copper"));
        registry.accept(new ResourceLocation("minecraft:exposed_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:weathered_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:oxidized_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:waxed_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:waxed_exposed_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:waxed_weathered_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:waxed_oxidized_cut_copper"));
        registry.accept(new ResourceLocation("minecraft:end_crystal"));
        registry.accept(new ResourceLocation("minecraft:end_rod"));
        registry.accept(new ResourceLocation("minecraft:mud_bricks"));
        registry.accept(new ResourceLocation("minecraft:mossy_stone_bricks_from_vine"));
        registry.accept(new ResourceLocation("minecraft:mossy_stone_bricks_from_moss_block"));
        registry.accept(new ResourceLocation("minecraft:packed_mud"));

        // Carpet replacement
        for (DyeColor color : DyeColor.values()) {
            registry.accept(new ResourceLocation(String.format("minecraft:%s_carpet",
                    color.name().toLowerCase(Locale.ROOT))));
        }

        // Slab replacement
        registry.accept(new ResourceLocation("minecraft:stone_slab"));
        registry.accept(new ResourceLocation("minecraft:smooth_stone_slab"));
        registry.accept(new ResourceLocation("minecraft:andesite_slab"));
        registry.accept(new ResourceLocation("minecraft:granite_slab"));
        registry.accept(new ResourceLocation("minecraft:diorite_slab"));
        registry.accept(new ResourceLocation("minecraft:polished_andesite_slab"));
        registry.accept(new ResourceLocation("minecraft:polished_granite_slab"));
        registry.accept(new ResourceLocation("minecraft:polished_diorite_slab"));
        registry.accept(new ResourceLocation("minecraft:sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:smooth_sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:red_sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:smooth_red_sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:cobblestone_slab"));
        registry.accept(new ResourceLocation("minecraft:blackstone_slab"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_slab"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:brick_slab"));
        registry.accept(new ResourceLocation("minecraft:stone_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:mud_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:nether_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:red_nether_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:quartz_slab"));
        registry.accept(new ResourceLocation("minecraft:smooth_quartz_slab"));
        registry.accept(new ResourceLocation("minecraft:cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:exposed_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:oxidized_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:weathered_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:waxed_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:waxed_exposed_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:waxed_oxidized_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:waxed_weathered_cut_copper_slab"));
        registry.accept(new ResourceLocation("minecraft:red_sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:purpur_slab"));
        registry.accept(new ResourceLocation("minecraft:end_stone_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:prismarine_slab"));
        registry.accept(new ResourceLocation("minecraft:prismarine_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:dark_prismarine_slab"));
        registry.accept(new ResourceLocation("minecraft:mossy_cobblestone_slab"));
        registry.accept(new ResourceLocation("minecraft:mossy_stone_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:cut_sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:cut_red_sandstone_slab"));
        registry.accept(new ResourceLocation("minecraft:bamboo_mosaic_slab"));
        registry.accept(new ResourceLocation("minecraft:cobbled_deepslate_slab"));
        registry.accept(new ResourceLocation("minecraft:polished_deepslate_slab"));
        registry.accept(new ResourceLocation("minecraft:deepslate_brick_slab"));
        registry.accept(new ResourceLocation("minecraft:deepslate_tile_slab"));
        // stair
        registry.accept(new ResourceLocation("minecraft:stone_stairs"));
        registry.accept(new ResourceLocation("minecraft:cobblestone_stairs"));
        registry.accept(new ResourceLocation("minecraft:mossy_cobblestone_stairs"));
        registry.accept(new ResourceLocation("minecraft:stone_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:mossy_stone_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:granite_stairs"));
        registry.accept(new ResourceLocation("minecraft:polished_granite_stairs"));
        registry.accept(new ResourceLocation("minecraft:diorite_stairs"));
        registry.accept(new ResourceLocation("minecraft:polished_diorite_stairs"));
        registry.accept(new ResourceLocation("minecraft:andesite_stairs"));
        registry.accept(new ResourceLocation("minecraft:polished_andesite_stairs"));
        registry.accept(new ResourceLocation("minecraft:cobbled_deepslate_stairs"));
        registry.accept(new ResourceLocation("minecraft:polished_deepslate_stairs"));
        registry.accept(new ResourceLocation("minecraft:deepslate_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:deepslate_tile_stairs"));
        registry.accept(new ResourceLocation("minecraft:brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:mud_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:sandstone_stairs"));
        registry.accept(new ResourceLocation("minecraft:smooth_sandstone_stairs"));
        registry.accept(new ResourceLocation("minecraft:red_sandstone_stairs"));
        registry.accept(new ResourceLocation("minecraft:smooth_red_sandstone_stairs"));
        registry.accept(new ResourceLocation("minecraft:prismarine_stairs"));
        registry.accept(new ResourceLocation("minecraft:prismarine_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:dark_prismarine_stairs"));
        registry.accept(new ResourceLocation("minecraft:nether_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:red_nether_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:blackstone_stairs"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_stairs"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:end_stone_brick_stairs"));
        registry.accept(new ResourceLocation("minecraft:purpur_stairs"));
        registry.accept(new ResourceLocation("minecraft:quartz_stairs"));
        registry.accept(new ResourceLocation("minecraft:smooth_quartz_stairs"));
        registry.accept(new ResourceLocation("minecraft:cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:exposed_cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:weathered_cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:oxidized_cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:waxed_cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:waxed_exposed_cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:waxed_weathered_cut_copper_stairs"));
        registry.accept(new ResourceLocation("minecraft:waxed_oxidized_cut_copper_stairs"));
        // wall
        registry.accept(new ResourceLocation("minecraft:cobblestone_wall"));
        registry.accept(new ResourceLocation("minecraft:mossy_cobblestone_wall"));
        registry.accept(new ResourceLocation("minecraft:stone_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:mossy_stone_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:granite_wall"));
        registry.accept(new ResourceLocation("minecraft:diorite_wall"));
        registry.accept(new ResourceLocation("minecraft:andesite_wall"));
        registry.accept(new ResourceLocation("minecraft:cobbled_deepslate_wall"));
        registry.accept(new ResourceLocation("minecraft:polished_deepslate_wall"));
        registry.accept(new ResourceLocation("minecraft:deepslate_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:deepslate_tile_wall"));
        registry.accept(new ResourceLocation("minecraft:brick_wall"));
        registry.accept(new ResourceLocation("minecraft:mud_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:sandstone_wall"));
        registry.accept(new ResourceLocation("minecraft:red_sandstone_wall"));
        registry.accept(new ResourceLocation("minecraft:prismarine_wall"));
        registry.accept(new ResourceLocation("minecraft:nether_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:red_nether_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:blackstone_wall"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_wall"));
        registry.accept(new ResourceLocation("minecraft:polished_blackstone_brick_wall"));
        registry.accept(new ResourceLocation("minecraft:end_stone_brick_wall"));
    }
}
