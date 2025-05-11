package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static com.gregtechceu.gtceu.api.GTValues.M;

public class MaterialInfoLoader {

    public static void init() {
        ItemMaterialData.registerMaterialInfo(Blocks.TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        for (DyeColor color : DyeColor.values()) {
            String dye = color.getName();
            ItemMaterialData.registerMaterialInfo(
                    new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dye + "_terracotta"))).getItem(),
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
            ItemMaterialData.registerMaterialInfo(
                    new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(dye + "_glazed_terracotta")))
                            .getItem(),
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        }

        ItemMaterialData.registerMaterialInfo(Blocks.LEVER, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M / 9), new MaterialStack(GTMaterials.Wood, M / 2)));
        ItemMaterialData.registerMaterialInfo(Blocks.REDSTONE_TORCH, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Wood, M / 2), new MaterialStack(GTMaterials.Redstone, M)));

        ItemMaterialData.registerMaterialInfo(Blocks.RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 3 / 16)));
        ItemMaterialData.registerMaterialInfo(Blocks.POWERED_RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M / 2)));
        ItemMaterialData.registerMaterialInfo(Blocks.DETECTOR_RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M / 2)));
        ItemMaterialData.registerMaterialInfo(Blocks.ACTIVATOR_RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M / 2)));

        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) {
            // ChemicalHelper.registerMaterialInfo(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new
            // ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M), new MaterialStack(GTMaterials.Iron, M / 2)));
            ItemMaterialData.registerMaterialInfo(Blocks.STONE_PRESSURE_PLATE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Stone, M), new MaterialStack(GTMaterials.Iron, M * 6 / 8)));
            ItemMaterialData.registerMaterialInfo(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Gold, M), new MaterialStack(GTMaterials.Steel, M)));
            ItemMaterialData.registerMaterialInfo(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Iron, M), new MaterialStack(GTMaterials.Steel, M)));
        } else {
            // ChemicalHelper.registerMaterialInfo(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new
            // ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ItemMaterialData.registerMaterialInfo(Blocks.STONE_PRESSURE_PLATE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M * 2)));
            ItemMaterialData.registerMaterialInfo(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 2)));
            ItemMaterialData.registerMaterialInfo(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 2)));
        }

        ItemMaterialData.registerMaterialInfo(Items.WHEAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wheat, M)));
        ItemMaterialData.registerMaterialInfo(Blocks.HAY_BLOCK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wheat, M * 9)));

        ItemMaterialData.registerMaterialInfo(Items.SNOWBALL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Water, M / 4)));
        ItemMaterialData.registerMaterialInfo(Blocks.SNOW,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Water, M)));

        ItemMaterialData.registerMaterialInfo(Blocks.ICE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Ice, M)));

        ItemMaterialData.registerMaterialInfo(Items.BOOK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Paper, M * 3)));
        ItemMaterialData.registerMaterialInfo(Items.WRITABLE_BOOK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Paper, M * 3)));
        ItemMaterialData.registerMaterialInfo(Items.ENCHANTED_BOOK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Paper, M * 3)));

        ItemMaterialData.registerMaterialInfo(Items.MINECART,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 5)));
        ItemMaterialData.registerMaterialInfo(Items.CHEST_MINECART, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 5), new MaterialStack(GTMaterials.Wood, M * 8)));
        ItemMaterialData.registerMaterialInfo(Items.FURNACE_MINECART, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 5), new MaterialStack(GTMaterials.Stone, M * 8)));
        ItemMaterialData.registerMaterialInfo(Items.TNT_MINECART,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 5)));
        ItemMaterialData.registerMaterialInfo(Items.HOPPER_MINECART, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 10), new MaterialStack(GTMaterials.Wood, M * 8)));

        ItemMaterialData.registerMaterialInfo(Blocks.ANVIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 31)));
        ItemMaterialData.registerMaterialInfo(Blocks.CHIPPED_ANVIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 22)));
        ItemMaterialData.registerMaterialInfo(Blocks.DAMAGED_ANVIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 13)));

        if (!ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            ItemMaterialData.registerMaterialInfo(Blocks.BEACON,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.NetherStar, M),
                            new MaterialStack(GTMaterials.Obsidian, M * 3),
                            new MaterialStack(GTMaterials.Glass, M * 5)));
        }
        ItemMaterialData.registerMaterialInfo(Blocks.LADDER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));

        if (!ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            ItemMaterialData.registerMaterialInfo(Blocks.NOTE_BLOCK, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 8), new MaterialStack(GTMaterials.Redstone, M)));
            ItemMaterialData.registerMaterialInfo(Blocks.JUKEBOX, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 8), new MaterialStack(GTMaterials.Diamond, M)));
            ItemMaterialData.registerMaterialInfo(Blocks.REDSTONE_LAMP, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Glowstone, M * 4), new MaterialStack(GTMaterials.Redstone, M * 4))); // dust
            ItemMaterialData.registerMaterialInfo(Blocks.DISPENSER, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Redstone, M)));
            ItemMaterialData.registerMaterialInfo(Blocks.DROPPER, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Redstone, M)));
        }

        ItemMaterialData.registerMaterialInfo(Blocks.PISTON, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 4), new MaterialStack(GTMaterials.Wood, M * 3)));
        ItemMaterialData.registerMaterialInfo(Blocks.STICKY_PISTON, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 4), new MaterialStack(GTMaterials.Wood, M * 3)));

        ItemMaterialData.registerMaterialInfo(Items.STONE_SHOVEL, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M), new MaterialStack(GTMaterials.Wood, M / 2)));
        ItemMaterialData.registerMaterialInfo(Items.STONE_PICKAXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ItemMaterialData.registerMaterialInfo(Items.STONE_AXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ItemMaterialData.registerMaterialInfo(Items.STONE_HOE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));
        ItemMaterialData.registerMaterialInfo(Items.STONE_SWORD, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Wood, M / 4)));

        ItemMaterialData.registerMaterialInfo(Items.WEATHERED_COPPER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Copper, M * 9)));
        ItemMaterialData.registerMaterialInfo(Items.EXPOSED_COPPER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Copper, M * 9)));
        ItemMaterialData.registerMaterialInfo(Items.OXIDIZED_COPPER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Copper, M * 9)));
        ItemMaterialData.registerMaterialInfo(Items.WAXED_WEATHERED_COPPER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Copper, M * 9)));
        ItemMaterialData.registerMaterialInfo(Items.WAXED_EXPOSED_COPPER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Copper, M * 9)));
        ItemMaterialData.registerMaterialInfo(Items.WAXED_OXIDIZED_COPPER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Copper, M * 9)));
    }
}
