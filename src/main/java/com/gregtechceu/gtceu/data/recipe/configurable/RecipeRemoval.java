package com.gregtechceu.gtceu.data.recipe.configurable;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;
import java.util.function.Consumer;

public class RecipeRemoval {

    public static void init(Consumer<Identifier> registry) {
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

    private static void generalRemovals(Consumer<Identifier> registry) {
        if (ConfigHolder.INSTANCE.recipes.removeVanillaTNTRecipe)
            registry.accept(Identifier.withDefaultNamespace("tnt"));

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
        registry.accept(Identifier.withDefaultNamespace("soul_torch"));
        registry.accept(Identifier.withDefaultNamespace("soul_lantern"));
        registry.accept(Identifier.withDefaultNamespace("leather_horse_armor"));
    }

    /**
     * Remove recipes for any item that is 4x4 or 9x9 crafting (nuggets <-> ingot, ingot <-> block, etc.)
     */
    private static void disableManualCompression(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("gold_block"));
        registry.accept(Identifier.withDefaultNamespace("gold_nugget"));
        registry.accept(Identifier.withDefaultNamespace("gold_ingot_from_gold_block"));
        registry.accept(Identifier.withDefaultNamespace("gold_ingot_from_nuggets"));
        registry.accept(Identifier.withDefaultNamespace("coal_block"));
        registry.accept(Identifier.withDefaultNamespace("coal"));
        registry.accept(Identifier.withDefaultNamespace("redstone_block"));
        registry.accept(Identifier.withDefaultNamespace("redstone"));
        registry.accept(Identifier.withDefaultNamespace("emerald_block"));
        registry.accept(Identifier.withDefaultNamespace("emerald"));
        registry.accept(Identifier.withDefaultNamespace("diamond_block"));
        registry.accept(Identifier.withDefaultNamespace("diamond"));
        registry.accept(Identifier.withDefaultNamespace("iron_block"));
        registry.accept(Identifier.withDefaultNamespace("iron_nugget"));
        registry.accept(Identifier.withDefaultNamespace("iron_ingot_from_iron_block"));
        registry.accept(Identifier.withDefaultNamespace("iron_ingot_from_nuggets"));
        registry.accept(Identifier.withDefaultNamespace("lapis_block"));
        registry.accept(Identifier.withDefaultNamespace("lapis_lazuli"));
        registry.accept(Identifier.withDefaultNamespace("quartz_block"));
        registry.accept(Identifier.withDefaultNamespace("clay"));
        registry.accept(Identifier.withDefaultNamespace("nether_brick"));
        registry.accept(Identifier.withDefaultNamespace("glowstone"));
        registry.accept(Identifier.withDefaultNamespace("amethyst_block"));
        registry.accept(Identifier.withDefaultNamespace("copper_block"));
        registry.accept(Identifier.withDefaultNamespace("copper_ingot"));
        registry.accept(Identifier.withDefaultNamespace("copper_ingot_from_waxed_copper_block"));
        registry.accept(Identifier.withDefaultNamespace("honeycomb_block"));
        registry.accept(Identifier.withDefaultNamespace("snow_block"));
        registry.accept(Identifier.withDefaultNamespace("netherite_block"));
        registry.accept(Identifier.withDefaultNamespace("netherite_ingot_from_netherite_block"));
        registry.accept(Identifier.withDefaultNamespace("dripstone_block"));
    }

    private static void harderBrickRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("brick"));
        registry.accept(Identifier.withDefaultNamespace("bricks"));
        registry.accept(Identifier.withDefaultNamespace("nether_brick"));
        registry.accept(Identifier.withDefaultNamespace("nether_bricks"));
        registry.accept(Identifier.withDefaultNamespace("red_nether_bricks"));
    }

    private static void hardWoodRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("ladder"));
        registry.accept(Identifier.withDefaultNamespace("bowl"));
        registry.accept(Identifier.withDefaultNamespace("chest"));
        registry.accept(Identifier.withDefaultNamespace("barrel"));
    }

    private static void hardIronRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("cauldron"));
        registry.accept(Identifier.withDefaultNamespace("hopper"));
        registry.accept(Identifier.withDefaultNamespace("iron_bars"));
        registry.accept(Identifier.withDefaultNamespace("bucket"));
        registry.accept(Identifier.withDefaultNamespace("chain"));
    }

    private static void hardRedstoneRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("dispenser"));
        registry.accept(Identifier.withDefaultNamespace("sticky_piston"));
        registry.accept(Identifier.withDefaultNamespace("piston"));
        registry.accept(Identifier.withDefaultNamespace("lever"));
        registry.accept(Identifier.withDefaultNamespace("daylight_detector"));
        registry.accept(Identifier.withDefaultNamespace("redstone_lamp"));
        registry.accept(Identifier.withDefaultNamespace("tripwire_hook"));
        registry.accept(Identifier.withDefaultNamespace("dropper"));
        registry.accept(Identifier.withDefaultNamespace("observer"));
        registry.accept(Identifier.withDefaultNamespace("repeater"));
        registry.accept(Identifier.withDefaultNamespace("comparator"));
        registry.accept(Identifier.withDefaultNamespace("powered_rail"));
        registry.accept(Identifier.withDefaultNamespace("detector_rail"));
        registry.accept(Identifier.withDefaultNamespace("rail"));
        registry.accept(Identifier.withDefaultNamespace("activator_rail"));
        registry.accept(Identifier.withDefaultNamespace("redstone_torch"));
        registry.accept(Identifier.withDefaultNamespace("stone_pressure_plate"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_pressure_plate"));
        registry.accept(Identifier.withDefaultNamespace("heavy_weighted_pressure_plate"));
        registry.accept(Identifier.withDefaultNamespace("light_weighted_pressure_plate"));
        registry.accept(Identifier.withDefaultNamespace("stone_button"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_button"));
        registry.accept(Identifier.withDefaultNamespace("calibrated_sculk_sensor"));
    }

    private static void hardToolArmorRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("compass"));
        registry.accept(Identifier.withDefaultNamespace("fishing_rod"));
        registry.accept(Identifier.withDefaultNamespace("clock"));
        registry.accept(Identifier.withDefaultNamespace("shears"));
        registry.accept(Identifier.withDefaultNamespace("shield"));
        registry.accept(Identifier.withDefaultNamespace("crossbow"));
        registry.accept(Identifier.withDefaultNamespace("bow"));
        for (String type : new String[] { "iron", "golden", "diamond" }) {
            registry.accept(Identifier.withDefaultNamespace(type + "_shovel"));
            registry.accept(Identifier.withDefaultNamespace(type + "_pickaxe"));
            registry.accept(Identifier.withDefaultNamespace(type + "_axe"));
            registry.accept(Identifier.withDefaultNamespace(type + "_sword"));
            registry.accept(Identifier.withDefaultNamespace(type + "_hoe"));
            registry.accept(Identifier.withDefaultNamespace(type + "_helmet"));
            registry.accept(Identifier.withDefaultNamespace(type + "_chestplate"));
            registry.accept(Identifier.withDefaultNamespace(type + "_leggings"));
            registry.accept(Identifier.withDefaultNamespace(type + "_boots"));
        }
    }

    /**
     * Remove recipes for items that don't fit in any other config option.
     * Vanilla items go here only if they not fit the criteria for removeVanillaBlockRecipes,
     * disableManualCompression, or any of the other config options
     */
    private static void hardMiscRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("jack_o_lantern"));
        registry.accept(Identifier.withDefaultNamespace("beacon"));
        registry.accept(Identifier.withDefaultNamespace("respawn_anchor"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_bookshelf"));
        registry.accept(Identifier.withDefaultNamespace("brewing_stand"));
        registry.accept(Identifier.withDefaultNamespace("enchanting_table"));
        registry.accept(Identifier.withDefaultNamespace("jukebox"));
        registry.accept(Identifier.withDefaultNamespace("note_block"));
        registry.accept(Identifier.withDefaultNamespace("furnace"));
        registry.accept(Identifier.withDefaultNamespace("crafting_table"));
        registry.accept(Identifier.withDefaultNamespace("flower_pot"));
        registry.accept(Identifier.withDefaultNamespace("armor_stand"));
        registry.accept(Identifier.withDefaultNamespace("trapped_chest"));
        registry.accept(Identifier.withDefaultNamespace("ender_chest"));
        registry.accept(Identifier.withDefaultNamespace("lantern"));
        registry.accept(Identifier.withDefaultNamespace("stonecutter"));
        registry.accept(Identifier.withDefaultNamespace("cartography_table"));
        registry.accept(Identifier.withDefaultNamespace("fletching_table"));
        registry.accept(Identifier.withDefaultNamespace("smithing_table"));
        registry.accept(Identifier.withDefaultNamespace("grindstone"));
        registry.accept(Identifier.withDefaultNamespace("smoker"));
        registry.accept(Identifier.withDefaultNamespace("blast_furnace"));
        registry.accept(Identifier.withDefaultNamespace("loom"));
        registry.accept(Identifier.withDefaultNamespace("composter"));
        registry.accept(Identifier.withDefaultNamespace("bell"));
        registry.accept(Identifier.withDefaultNamespace("conduit"));
        registry.accept(Identifier.withDefaultNamespace("candle"));
        registry.accept(Identifier.withDefaultNamespace("scaffolding"));
        registry.accept(Identifier.withDefaultNamespace("beehive"));
        registry.accept(Identifier.withDefaultNamespace("lightning_rod"));
        registry.accept(Identifier.withDefaultNamespace("lectern"));
        registry.accept(Identifier.withDefaultNamespace("golden_apple"));
        registry.accept(Identifier.withDefaultNamespace("book"));
        registry.accept(Identifier.withDefaultNamespace("ender_eye"));
        registry.accept(Identifier.withDefaultNamespace("glistering_melon_slice"));
        registry.accept(Identifier.withDefaultNamespace("golden_carrot"));
        registry.accept(Identifier.withDefaultNamespace("magma_cream"));
        registry.accept(Identifier.withDefaultNamespace("lead"));
        registry.accept(Identifier.withDefaultNamespace("item_frame"));
        registry.accept(Identifier.withDefaultNamespace("painting"));
        registry.accept(Identifier.withDefaultNamespace("chest_minecart"));
        registry.accept(Identifier.withDefaultNamespace("furnace_minecart"));
        registry.accept(Identifier.withDefaultNamespace("tnt_minecart"));
        registry.accept(Identifier.withDefaultNamespace("hopper_minecart"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(Identifier.withDefaultNamespace(color.getName() + "_bed"));
        }
        registry.accept(Identifier.withDefaultNamespace("fermented_spider_eye"));
        registry.accept(Identifier.withDefaultNamespace("fire_charge"));
        registry.accept(Identifier.withDefaultNamespace("music_disc_5"));
        registry.accept(Identifier.withDefaultNamespace("turtle_helmet"));
        registry.accept(Identifier.withDefaultNamespace("brush"));
        registry.accept(Identifier.withDefaultNamespace("recovery_compass"));
        registry.accept(Identifier.withDefaultNamespace("spyglass"));
        registry.accept(Identifier.withDefaultNamespace("respawn_anchor"));
        registry.accept(Identifier.withDefaultNamespace("lodestone"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_bookshelf"));
        registry.accept(Identifier.withDefaultNamespace("bread"));
        registry.accept(Identifier.withDefaultNamespace("cake"));
        registry.accept(Identifier.withDefaultNamespace("cookie"));
        registry.accept(Identifier.withDefaultNamespace("pumpkin_pie"));
    }

    private static void hardGlassRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("glass"));
        registry.accept(Identifier.withDefaultNamespace("glass_bottle"));
        registry.accept(Identifier.withDefaultNamespace("glass_pane"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(Identifier.withDefaultNamespace(String.format("%s_stained_glass_pane_from_glass_pane",
                    color.name().toLowerCase(Locale.ROOT))));
            registry.accept(Identifier.withDefaultNamespace(
                    String.format("%s_stained_glass_pane", color.name().toLowerCase(Locale.ROOT))));
        }
        registry.accept(Identifier.withDefaultNamespace("tinted_glass"));
    }

    private static void nerfPaperCrafting(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("paper"));
        registry.accept(Identifier.withDefaultNamespace("sugar_from_sugar_cane"));
    }

    private static void hardAdvancedIronRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("iron_door"));
        registry.accept(Identifier.withDefaultNamespace("anvil"));
        registry.accept(Identifier.withDefaultNamespace("iron_trapdoor"));
        registry.accept(Identifier.withDefaultNamespace("minecart"));
    }

    private static void hardDyeRecipes(Consumer<Identifier> registry) {
        for (MarkerMaterial colorMaterial : MarkerMaterials.Color.VALUES) {
            registry.accept(
                    Identifier
                            .withDefaultNamespace(String.format("%s_concrete_powder", colorMaterial.getName())));
            registry.accept(
                    Identifier.withDefaultNamespace(String.format("%s_terracotta", colorMaterial.getName())));
            registry.accept(
                    Identifier.withDefaultNamespace(String.format("%s_stained_glass", colorMaterial.getName())));
            registry.accept(Identifier.withDefaultNamespace(String.format("%s_candle", colorMaterial.getName())));
            registry.accept(
                    Identifier.withDefaultNamespace(String.format("dye_%s_wool", colorMaterial.getName())));
            registry.accept(
                    Identifier.withDefaultNamespace(String.format("dye_%s_carpet", colorMaterial.getName())));
            registry.accept(
                    Identifier.withDefaultNamespace(String.format("dye_%s_bed", colorMaterial.getName())));
        }
        registry.accept(Identifier.withDefaultNamespace("black_dye"));
        registry.accept(Identifier.withDefaultNamespace("black_dye_from_wither_rose"));
        registry.accept(Identifier.withDefaultNamespace("blue_dye"));
        registry.accept(Identifier.withDefaultNamespace("white_dye"));
        registry.accept(Identifier.withDefaultNamespace("white_dye_from_lily_of_the_valley"));
        registry.accept(Identifier.withDefaultNamespace("light_blue_dye_from_blue_orchid"));
        registry.accept(Identifier.withDefaultNamespace("yellow_dye_from_dandelion"));
        registry.accept(Identifier.withDefaultNamespace("light_gray_dye_from_white_tulip"));
        registry.accept(Identifier.withDefaultNamespace("light_gray_dye_from_azure_bluet"));
        registry.accept(Identifier.withDefaultNamespace("red_dye_from_poppy"));
        registry.accept(Identifier.withDefaultNamespace("red_dye_from_tulip"));
        registry.accept(Identifier.withDefaultNamespace("red_dye_from_rose_bush"));
        registry.accept(Identifier.withDefaultNamespace("red_dye_from_beetroot"));
        registry.accept(Identifier.withDefaultNamespace("orange_dye_from_orange_tulip"));
        registry.accept(Identifier.withDefaultNamespace("orange_dye_from_torchflower"));
        registry.accept(Identifier.withDefaultNamespace("yellow_dye_from_dandelion"));
        registry.accept(Identifier.withDefaultNamespace("cyan_dye_from_pitcher_plant"));
        registry.accept(Identifier.withDefaultNamespace("light_blue_dye_from_blue_orchid"));
        registry.accept(Identifier.withDefaultNamespace("blue_dye_from_cornflower"));
        registry.accept(Identifier.withDefaultNamespace("magenta_dye_from_allium"));
        registry.accept(Identifier.withDefaultNamespace("magenta_dye_from_lilac"));
        registry.accept(Identifier.withDefaultNamespace("lime_dye_from_lime"));
        registry.accept(Identifier.withDefaultNamespace("pink_dye_from_pink_tulip"));
        registry.accept(Identifier.withDefaultNamespace("pink_dye_from_pink_petals"));
        registry.accept(Identifier.withDefaultNamespace("pink_dye_from_peony"));
        registry.accept(Identifier.withDefaultNamespace("yellow_dye_from_sunflower"));
        registry.accept(Identifier.withDefaultNamespace("light_gray_dye_from_oxeye_daisy"));

        registry.accept(Identifier.withDefaultNamespace("dark_prismarine"));
    }

    private static void flintAndSteelRequireSteel(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("flint_and_steel"));
    }

    /**
     * Removes the vanilla recipe for an item that would have BOTH a normal recipe as well as a GT recipe in
     * normal recipe configs (think stairs, ladders, etc. having a crafting table recipe as well as a machine recipe)
     */
    private static void removeVanillaBlockRecipes(Consumer<Identifier> registry) {
        registry.accept(Identifier.withDefaultNamespace("dripstone_block"));
        registry.accept(Identifier.withDefaultNamespace("polished_granite"));
        registry.accept(Identifier.withDefaultNamespace("polished_diorite"));
        registry.accept(Identifier.withDefaultNamespace("polished_andesite"));
        registry.accept(Identifier.withDefaultNamespace("packed_ice"));
        registry.accept(Identifier.withDefaultNamespace("blue_ice"));
        registry.accept(Identifier.withDefaultNamespace("slime_block"));
        registry.accept(Identifier.withDefaultNamespace("slime_ball"));
        registry.accept(Identifier.withDefaultNamespace("melon"));
        registry.accept(Identifier.withDefaultNamespace("hay_block"));
        registry.accept(Identifier.withDefaultNamespace("wheat"));
        registry.accept(Identifier.withDefaultNamespace("magma_block"));
        registry.accept(Identifier.withDefaultNamespace("nether_wart_block"));
        registry.accept(Identifier.withDefaultNamespace("bone_block"));
        registry.accept(Identifier.withDefaultNamespace("bone_meal_from_bone_block"));
        registry.accept(Identifier.withDefaultNamespace("honey_block"));
        registry.accept(Identifier.withDefaultNamespace("purpur_block"));
        registry.accept(Identifier.withDefaultNamespace("prismarine_bricks"));
        registry.accept(Identifier.withDefaultNamespace("prismarine"));
        registry.accept(Identifier.withDefaultNamespace("snow_block"));
        registry.accept(Identifier.withDefaultNamespace("sandstone"));
        registry.accept(Identifier.withDefaultNamespace("polished_andesite"));
        registry.accept(Identifier.withDefaultNamespace("polished_diorite"));
        registry.accept(Identifier.withDefaultNamespace("polished_granite"));
        registry.accept(Identifier.withDefaultNamespace("coarse_dirt"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_sandstone"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_quartz_block"));
        registry.accept(Identifier.withDefaultNamespace("stone_bricks"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_stone_bricks"));
        registry.accept(Identifier.withDefaultNamespace("purpur_pillar"));
        registry.accept(Identifier.withDefaultNamespace("end_stone_bricks"));
        registry.accept(Identifier.withDefaultNamespace("red_nether_bricks"));
        registry.accept(Identifier.withDefaultNamespace("red_sandstone"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_red_sandstone"));
        registry.accept(Identifier.withDefaultNamespace("bookshelf"));
        registry.accept(Identifier.withDefaultNamespace("quartz_pillar"));
        registry.accept(Identifier.withDefaultNamespace("sea_lantern"));
        registry.accept(Identifier.withDefaultNamespace("white_wool_from_string"));
        registry.accept(Identifier.withDefaultNamespace("cracked_stone_bricks"));
        registry.accept(Identifier.withDefaultNamespace("mossy_cobblestone_from_moss_block"));
        registry.accept(Identifier.withDefaultNamespace("mossy_cobblestone_from_vine"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_bricks"));
        registry.accept(Identifier.withDefaultNamespace("cracked_nether_bricks"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_nether_bricks"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_bricks"));
        registry.accept(Identifier.withDefaultNamespace("cracked_polished_blackstone_bricks"));
        registry.accept(Identifier.withDefaultNamespace("quartz_bricks"));
        registry.accept(Identifier.withDefaultNamespace("polished_deepslate"));
        registry.accept(Identifier.withDefaultNamespace("polished_basalt"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_polished_blackstone"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_tiles"));
        registry.accept(Identifier.withDefaultNamespace("cracked_deepslate_tiles"));
        registry.accept(Identifier.withDefaultNamespace("chiseled_deepslate"));
        registry.accept(Identifier.withDefaultNamespace("cracked_deepslate_bricks"));
        registry.accept(Identifier.withDefaultNamespace("cut_red_sandstone"));
        registry.accept(Identifier.withDefaultNamespace("polished_basalt"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone"));
        registry.accept(Identifier.withDefaultNamespace("cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("exposed_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("weathered_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("oxidized_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("waxed_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("waxed_exposed_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("waxed_weathered_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("waxed_oxidized_cut_copper"));
        registry.accept(Identifier.withDefaultNamespace("end_crystal"));
        registry.accept(Identifier.withDefaultNamespace("end_rod"));
        registry.accept(Identifier.withDefaultNamespace("mud_bricks"));
        registry.accept(Identifier.withDefaultNamespace("mossy_stone_bricks_from_vine"));
        registry.accept(Identifier.withDefaultNamespace("mossy_stone_bricks_from_moss_block"));
        registry.accept(Identifier.withDefaultNamespace("packed_mud"));

        // Carpet replacement
        for (DyeColor color : DyeColor.values()) {
            registry.accept(Identifier.withDefaultNamespace(String.format("%s_carpet",
                    color.name().toLowerCase(Locale.ROOT))));
        }

        // Slab replacement
        registry.accept(Identifier.withDefaultNamespace("stone_slab"));
        registry.accept(Identifier.withDefaultNamespace("smooth_stone_slab"));
        registry.accept(Identifier.withDefaultNamespace("andesite_slab"));
        registry.accept(Identifier.withDefaultNamespace("granite_slab"));
        registry.accept(Identifier.withDefaultNamespace("diorite_slab"));
        registry.accept(Identifier.withDefaultNamespace("polished_andesite_slab"));
        registry.accept(Identifier.withDefaultNamespace("polished_granite_slab"));
        registry.accept(Identifier.withDefaultNamespace("polished_diorite_slab"));
        registry.accept(Identifier.withDefaultNamespace("sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("smooth_sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("red_sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("smooth_red_sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("cobblestone_slab"));
        registry.accept(Identifier.withDefaultNamespace("blackstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("stone_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("mud_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("nether_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("red_nether_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("quartz_slab"));
        registry.accept(Identifier.withDefaultNamespace("smooth_quartz_slab"));
        registry.accept(Identifier.withDefaultNamespace("cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("exposed_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("oxidized_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("weathered_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("waxed_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("waxed_exposed_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("waxed_oxidized_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("waxed_weathered_cut_copper_slab"));
        registry.accept(Identifier.withDefaultNamespace("red_sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("purpur_slab"));
        registry.accept(Identifier.withDefaultNamespace("end_stone_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("prismarine_slab"));
        registry.accept(Identifier.withDefaultNamespace("prismarine_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("dark_prismarine_slab"));
        registry.accept(Identifier.withDefaultNamespace("mossy_cobblestone_slab"));
        registry.accept(Identifier.withDefaultNamespace("mossy_stone_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("cut_sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("cut_red_sandstone_slab"));
        registry.accept(Identifier.withDefaultNamespace("bamboo_mosaic_slab"));
        registry.accept(Identifier.withDefaultNamespace("cobbled_deepslate_slab"));
        registry.accept(Identifier.withDefaultNamespace("polished_deepslate_slab"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_brick_slab"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_tile_slab"));
        // stair
        registry.accept(Identifier.withDefaultNamespace("stone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("cobblestone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("mossy_cobblestone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("stone_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("mossy_stone_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("granite_stairs"));
        registry.accept(Identifier.withDefaultNamespace("polished_granite_stairs"));
        registry.accept(Identifier.withDefaultNamespace("diorite_stairs"));
        registry.accept(Identifier.withDefaultNamespace("polished_diorite_stairs"));
        registry.accept(Identifier.withDefaultNamespace("andesite_stairs"));
        registry.accept(Identifier.withDefaultNamespace("polished_andesite_stairs"));
        registry.accept(Identifier.withDefaultNamespace("cobbled_deepslate_stairs"));
        registry.accept(Identifier.withDefaultNamespace("polished_deepslate_stairs"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_tile_stairs"));
        registry.accept(Identifier.withDefaultNamespace("brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("mud_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("sandstone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("smooth_sandstone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("red_sandstone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("smooth_red_sandstone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("prismarine_stairs"));
        registry.accept(Identifier.withDefaultNamespace("prismarine_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("dark_prismarine_stairs"));
        registry.accept(Identifier.withDefaultNamespace("nether_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("red_nether_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("blackstone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_stairs"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("end_stone_brick_stairs"));
        registry.accept(Identifier.withDefaultNamespace("purpur_stairs"));
        registry.accept(Identifier.withDefaultNamespace("quartz_stairs"));
        registry.accept(Identifier.withDefaultNamespace("smooth_quartz_stairs"));
        registry.accept(Identifier.withDefaultNamespace("cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("exposed_cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("weathered_cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("oxidized_cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("waxed_cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("waxed_exposed_cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("waxed_weathered_cut_copper_stairs"));
        registry.accept(Identifier.withDefaultNamespace("waxed_oxidized_cut_copper_stairs"));
        // wall
        registry.accept(Identifier.withDefaultNamespace("cobblestone_wall"));
        registry.accept(Identifier.withDefaultNamespace("mossy_cobblestone_wall"));
        registry.accept(Identifier.withDefaultNamespace("stone_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("mossy_stone_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("granite_wall"));
        registry.accept(Identifier.withDefaultNamespace("diorite_wall"));
        registry.accept(Identifier.withDefaultNamespace("andesite_wall"));
        registry.accept(Identifier.withDefaultNamespace("cobbled_deepslate_wall"));
        registry.accept(Identifier.withDefaultNamespace("polished_deepslate_wall"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("deepslate_tile_wall"));
        registry.accept(Identifier.withDefaultNamespace("brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("mud_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("sandstone_wall"));
        registry.accept(Identifier.withDefaultNamespace("red_sandstone_wall"));
        registry.accept(Identifier.withDefaultNamespace("prismarine_wall"));
        registry.accept(Identifier.withDefaultNamespace("nether_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("red_nether_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("blackstone_wall"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_wall"));
        registry.accept(Identifier.withDefaultNamespace("polished_blackstone_brick_wall"));
        registry.accept(Identifier.withDefaultNamespace("end_stone_brick_wall"));
    }
}
