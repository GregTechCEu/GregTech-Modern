package com.gregtechceu.gtceu.data.recipe.configurable;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.Locale;
import java.util.function.Consumer;

public class RecipeRemoval {

    public static void init(Consumer<ResourceLocation> registry) {
        generalRemovals(registry);
        if (ConfigHolder.INSTANCE.recipes.disableManualCompression) disableManualCompression(registry);
        if (ConfigHolder.INSTANCE.recipes.harderBrickRecipes) harderBrickRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) nerfWoodCrafting(registry);
        if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) hardWoodRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardIronRecipes) hardIronRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) hardRedstoneRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardToolArmorRecipes) hardToolArmorRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardMiscRecipes) hardMiscRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardGlassRecipes) hardGlassRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.nerfPaperCrafting) nerfPaperCrafting(registry);
        if (ConfigHolder.INSTANCE.recipes.hardAdvancedIronRecipes) hardAdvancedIronRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.hardDyeRecipes) hardDyeRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.harderCharcoalRecipe) harderCharcoalRecipes(registry);
        if (ConfigHolder.INSTANCE.recipes.flintAndSteelRequireSteel) flintAndSteelRequireSteel(registry);
        if (ConfigHolder.INSTANCE.recipes.removeVanillaBlockRecipes) removeVanillaBlockRecipes(registry);
    }

    private static void generalRemovals(Consumer<ResourceLocation> registry) {
        if (ConfigHolder.INSTANCE.recipes.removeVanillaTNTRecipe) registry.accept(new ResourceLocation("minecraft:tnt"));

        // todo
        /*
        // always remove these, GT ore processing changes their output
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.COAL_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.IRON_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.GOLD_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.DIAMOND_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.EMERALD_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.LAPIS_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.REDSTONE_ORE));
        ModHandler.removeFurnaceSmelting(new ItemStack(Blocks.QUARTZ_ORE));

        // Remove a bunch of processing recipes for tools and armor, since we have significantly better options
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HELMET, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_CHESTPLATE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_LEGGINGS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_BOOTS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HORSE_ARMOR, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_PICKAXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_SHOVEL, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_AXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_SWORD, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.IRON_HOE, 1, W));

        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HELMET, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_CHESTPLATE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_LEGGINGS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_BOOTS, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HORSE_ARMOR, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_PICKAXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_SHOVEL, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_AXE, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_SWORD, 1, W));
        ModHandler.removeFurnaceSmelting(new ItemStack(Items.GOLDEN_HOE, 1, W));*/
    }

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
    }

    private static void harderBrickRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:brick"));
        registry.accept(new ResourceLocation("minecraft:bricks"));
        registry.accept(new ResourceLocation("minecraft:nether_brick"));
    }

    private static void nerfWoodCrafting(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:stick"));
        registry.accept(new ResourceLocation("minecraft:oak_planks"));
        registry.accept(new ResourceLocation("minecraft:spruce_planks"));
        registry.accept(new ResourceLocation("minecraft:birch_planks"));
        registry.accept(new ResourceLocation("minecraft:jungle_planks"));
        registry.accept(new ResourceLocation("minecraft:acacia_planks"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_planks"));
        registry.accept(new ResourceLocation("minecraft:mangrove_planks"));
        registry.accept(new ResourceLocation("minecraft:crimson_planks"));
        registry.accept(new ResourceLocation("minecraft:warped_planks"));
    }

    private static void hardWoodRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:ladder"));
        registry.accept(new ResourceLocation("minecraft:oak_door"));
        registry.accept(new ResourceLocation("minecraft:spruce_door"));
        registry.accept(new ResourceLocation("minecraft:birch_door"));
        registry.accept(new ResourceLocation("minecraft:jungle_door"));
        registry.accept(new ResourceLocation("minecraft:acacia_door"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_door"));
        registry.accept(new ResourceLocation("minecraft:crimson_door"));
        registry.accept(new ResourceLocation("minecraft:warped_door"));
        registry.accept(new ResourceLocation("minecraft:mangrove_door"));
        registry.accept(new ResourceLocation("minecraft:bamboo_door"));
        registry.accept(new ResourceLocation("minecraft:cherry_door"));
        registry.accept(new ResourceLocation("minecraft:oak_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:birch_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:spruce_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:jungle_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:acacia_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:crimson_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:warped_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:mangrove_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:bamboo_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:cherry_trapdoor"));
        registry.accept(new ResourceLocation("minecraft:bowl"));
        registry.accept(new ResourceLocation("minecraft:chest"));
        registry.accept(new ResourceLocation("minecraft:oak_boat"));
        registry.accept(new ResourceLocation("minecraft:spruce_boat"));
        registry.accept(new ResourceLocation("minecraft:birch_boat"));
        registry.accept(new ResourceLocation("minecraft:jungle_boat"));
        registry.accept(new ResourceLocation("minecraft:acacia_boat"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_boat"));
        registry.accept(new ResourceLocation("minecraft:mangrove_boat"));
        registry.accept(new ResourceLocation("minecraft:bamboo_boat"));
        registry.accept(new ResourceLocation("minecraft:cherry_boat"));
        registry.accept(new ResourceLocation("minecraft:oak_fence"));
        registry.accept(new ResourceLocation("minecraft:spruce_fence"));
        registry.accept(new ResourceLocation("minecraft:birch_fence"));
        registry.accept(new ResourceLocation("minecraft:jungle_fence"));
        registry.accept(new ResourceLocation("minecraft:acacia_fence"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_fence"));
        registry.accept(new ResourceLocation("minecraft:crimson_fence"));
        registry.accept(new ResourceLocation("minecraft:warped_fence"));
        registry.accept(new ResourceLocation("minecraft:mangrove_fence"));
        registry.accept(new ResourceLocation("minecraft:bamboo_fence"));
        registry.accept(new ResourceLocation("minecraft:cherry_fence"));
        registry.accept(new ResourceLocation("minecraft:oak_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:spruce_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:birch_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:jungle_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:acacia_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:crimson_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:warped_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:mangrove_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:bamboo_fence_gate"));
        registry.accept(new ResourceLocation("minecraft:cherry_fence_gate"));
    }

    private static void hardIronRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:cauldron"));
        registry.accept(new ResourceLocation("minecraft:hopper"));
        registry.accept(new ResourceLocation("minecraft:iron_bars"));
        registry.accept(new ResourceLocation("minecraft:bucket"));
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
        registry.accept(new ResourceLocation("minecraft:oak_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:birch_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:spruce_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:jungle_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:acacia_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:crimson_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:warped_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:mangrove_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:heavy_weighted_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:light_weighted_pressure_plate"));
        registry.accept(new ResourceLocation("minecraft:stone_button"));
        registry.accept(new ResourceLocation("minecraft:oak_button"));
        registry.accept(new ResourceLocation("minecraft:birch_button"));
        registry.accept(new ResourceLocation("minecraft:spruce_button"));
        registry.accept(new ResourceLocation("minecraft:jungle_button"));
        registry.accept(new ResourceLocation("minecraft:acacia_button"));
        registry.accept(new ResourceLocation("minecraft:dark_oak_button"));
        registry.accept(new ResourceLocation("minecraft:crimson_button"));
        registry.accept(new ResourceLocation("minecraft:warped_button"));
        registry.accept(new ResourceLocation("minecraft:mangrove_button"));
    }

    private static void hardToolArmorRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:compass"));
        registry.accept(new ResourceLocation("minecraft:fishing_rod"));
        registry.accept(new ResourceLocation("minecraft:clock"));
        registry.accept(new ResourceLocation("minecraft:shears"));
        registry.accept(new ResourceLocation("minecraft:shield"));
        for (String type : new String[]{"iron", "golden", "diamond"}) {
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
        registry.accept(new ResourceLocation("minecraft:beacon"));
        registry.accept(new ResourceLocation("minecraft:jack_o_lantern"));
        registry.accept(new ResourceLocation("minecraft:golden_apple"));
        registry.accept(new ResourceLocation("minecraft:book"));
        registry.accept(new ResourceLocation("minecraft:brewing_stand"));
        registry.accept(new ResourceLocation("minecraft:ender_eye"));
        registry.accept(new ResourceLocation("minecraft:glistering_melon_slice"));
        registry.accept(new ResourceLocation("minecraft:golden_carrot"));
        registry.accept(new ResourceLocation("minecraft:magma_cream"));
        registry.accept(new ResourceLocation("minecraft:enchanting_table"));
        registry.accept(new ResourceLocation("minecraft:jukebox"));
        registry.accept(new ResourceLocation("minecraft:note_block"));
        registry.accept(new ResourceLocation("minecraft:furnace"));
        registry.accept(new ResourceLocation("minecraft:crafting_table"));
        registry.accept(new ResourceLocation("minecraft:polished_granite"));
        registry.accept(new ResourceLocation("minecraft:polished_diorite"));
        registry.accept(new ResourceLocation("minecraft:polished_andesite"));
        registry.accept(new ResourceLocation("minecraft:lead"));
        registry.accept(new ResourceLocation("minecraft:bow"));
        registry.accept(new ResourceLocation("minecraft:item_frame"));
        registry.accept(new ResourceLocation("minecraft:painting"));
        registry.accept(new ResourceLocation("minecraft:chest_minecart"));
        registry.accept(new ResourceLocation("minecraft:furnace_minecart"));
        registry.accept(new ResourceLocation("minecraft:tnt_minecart"));
        registry.accept(new ResourceLocation("minecraft:hopper_minecart"));
        registry.accept(new ResourceLocation("minecraft:flower_pot"));
        registry.accept(new ResourceLocation("minecraft:armor_stand"));
        registry.accept(new ResourceLocation("minecraft:trapped_chest"));
        registry.accept(new ResourceLocation("minecraft:ender_chest"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(new ResourceLocation(color.getName() + "_bed"));
        }
        registry.accept(new ResourceLocation("minecraft:fermented_spider_eye"));
        registry.accept(new ResourceLocation("minecraft:fire_charge"));
    }

    private static void hardGlassRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:glass"));
        registry.accept(new ResourceLocation("minecraft:glass_bottle"));
        registry.accept(new ResourceLocation("minecraft:glass_pane"));
        for (DyeColor color : DyeColor.values()) {
            registry.accept(new ResourceLocation(String.format("minecraft:%s_stained_glass_pane_from_glass_pane", color.name().toLowerCase(Locale.ROOT))));
            registry.accept(new ResourceLocation(String.format("minecraft:%s_stained_glass_pane", color.name().toLowerCase(Locale.ROOT))));
        }
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
            registry.accept(new ResourceLocation(String.format("minecraft:%s_concrete_powder", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:%s_terracotta", colorMaterial.getName())));
            registry.accept(new ResourceLocation(String.format("minecraft:%s_stained_glass", colorMaterial.getName())));
            if (colorMaterial != MarkerMaterials.Color.White) {
                registry.accept(new ResourceLocation(String.format("minecraft:%s_wool", colorMaterial.getName())));
            }
        }
        registry.accept(new ResourceLocation("minecraft:dark_prismarine"));
    }

    private static void harderCharcoalRecipes(Consumer<ResourceLocation> registry) {
    }

    private static void flintAndSteelRequireSteel(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:flint_and_steel"));
    }

    private static void removeVanillaBlockRecipes(Consumer<ResourceLocation> registry) {
        registry.accept(new ResourceLocation("minecraft:slime"));
        registry.accept(new ResourceLocation("minecraft:slime_ball"));
        registry.accept(new ResourceLocation("minecraft:melon_block"));
        registry.accept(new ResourceLocation("minecraft:hay_block"));
        registry.accept(new ResourceLocation("minecraft:wheat"));
        registry.accept(new ResourceLocation("minecraft:magma"));
        registry.accept(new ResourceLocation("minecraft:nether_wart_block"));
        registry.accept(new ResourceLocation("minecraft:bone_block"));
        registry.accept(new ResourceLocation("minecraft:bone_meal_from_block"));
        registry.accept(new ResourceLocation("minecraft:purpur_block"));
        registry.accept(new ResourceLocation("minecraft:prismarine_bricks"));
        registry.accept(new ResourceLocation("minecraft:prismarine"));
        registry.accept(new ResourceLocation("minecraft:snow"));
        registry.accept(new ResourceLocation("minecraft:sandstone"));
        registry.accept(new ResourceLocation("minecraft:polished_andesite"));
        registry.accept(new ResourceLocation("minecraft:polished_diorite"));
        registry.accept(new ResourceLocation("minecraft:polished_granite"));
        registry.accept(new ResourceLocation("minecraft:coarse_dirt"));
        registry.accept(new ResourceLocation("minecraft:smooth_sandstone"));
        registry.accept(new ResourceLocation("minecraft:chiseled_sandstone"));
        registry.accept(new ResourceLocation("minecraft:chiseled_quartz_block"));
        registry.accept(new ResourceLocation("minecraft:stone_bricks"));
        registry.accept(new ResourceLocation("minecraft:stone_bricks_from_stonecutting"));
        registry.accept(new ResourceLocation("minecraft:chiseled_stone_bricks"));
        registry.accept(new ResourceLocation("minecraft:purpur_pillar"));
        registry.accept(new ResourceLocation("minecraft:end_bricks"));
        registry.accept(new ResourceLocation("minecraft:red_nether_brick"));
        registry.accept(new ResourceLocation("minecraft:red_sandstone"));
        registry.accept(new ResourceLocation("minecraft:chiseled_red_sandstone"));
        registry.accept(new ResourceLocation("minecraft:smooth_red_sandstone"));
        registry.accept(new ResourceLocation("minecraft:bookshelf"));
        registry.accept(new ResourceLocation("minecraft:pillar_quartz_block"));
        registry.accept(new ResourceLocation("minecraft:sea_lantern"));

        // Slab replacement
        registry.accept(new ResourceLocation("minecraft:stone_slab"));;
        registry.accept(new ResourceLocation("minecraft:sandstone_slab"));;
        registry.accept(new ResourceLocation("minecraft:cobblestone_slab"));;
        registry.accept(new ResourceLocation("minecraft:brick_slab"));;
        registry.accept(new ResourceLocation("minecraft:stone_brick_slab"));;
        registry.accept(new ResourceLocation("minecraft:nether_brick_slab"));;
        registry.accept(new ResourceLocation("minecraft:quartz_slab"));;
        registry.accept(new ResourceLocation("minecraft:red_sandstone_slab"));;
        registry.accept(new ResourceLocation("minecraft:purpur_slab"));;
    }
}
