package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static com.gregtechceu.gtceu.api.GTValues.M;

public class MaterialInfoLoader {

    public static void init() {
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_CUPRONICKEL.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Cupronickel, M * 8), // double wire
                        new MaterialStack(GTMaterials.Bronze, M * 2), // foil
                        new MaterialStack(GTMaterials.TinAlloy, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_KANTHAL.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Kanthal, M * 8), // double wire
                        new MaterialStack(GTMaterials.Aluminium, M * 2), // foil
                        new MaterialStack(GTMaterials.Copper, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_NICHROME.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Nichrome, M * 8), // double wire
                        new MaterialStack(GTMaterials.StainlessSteel, M * 2), // foil
                        new MaterialStack(GTMaterials.Aluminium, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_RTMALLOY.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.RTMAlloy, M * 8), // double wire
                        new MaterialStack(GTMaterials.VanadiumSteel, M * 2), // foil
                        new MaterialStack(GTMaterials.Nichrome, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_HSSG.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.HSSG, M * 8), // double wire
                        new MaterialStack(GTMaterials.TungstenCarbide, M * 2), // foil
                        new MaterialStack(GTMaterials.Tungsten, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_NAQUADAH.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Naquadah, M * 8), // double wire
                        new MaterialStack(GTMaterials.Osmium, M * 2), // foil
                        new MaterialStack(GTMaterials.TungstenSteel, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_TRINIUM.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Trinium, M * 8), // double wire
                        new MaterialStack(GTMaterials.NaquadahEnriched, M * 2), // foil
                        new MaterialStack(GTMaterials.Naquadah, M)) // ingot
        );
        ChemicalHelper.registerMaterialInfo(GTBlocks.COIL_TRITANIUM.get(),
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Tritanium, M * 8), // double wire
                        new MaterialStack(GTMaterials.Naquadria, M * 2), // foil
                        new MaterialStack(GTMaterials.Trinium, M)) // ingot
        );

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[0].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.WroughtIron, M * 8), // plate
                new MaterialStack(GTMaterials.RedAlloy, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[1].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Steel, M * 8), // plate
                new MaterialStack(GTMaterials.Tin, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[2].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Aluminium, M * 8), // plate
                new MaterialStack(GTMaterials.Copper, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[3].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.StainlessSteel, M * 8), // plate
                new MaterialStack(GTMaterials.Gold, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[4].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Titanium, M * 8), // plate
                new MaterialStack(GTMaterials.Aluminium, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[5].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.TungstenSteel, M * 8), // plate
                new MaterialStack(GTMaterials.Platinum, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[6].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.RhodiumPlatedPalladium, M * 8), // plate
                new MaterialStack(GTMaterials.NiobiumTitanium, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[7].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.NaquadahAlloy, M * 8), // plate
                new MaterialStack(GTMaterials.VanadiumGallium, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[8].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Darmstadtium, M * 8), // plate
                new MaterialStack(GTMaterials.YttriumBariumCuprate, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.HULL[9].getBlock(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Neutronium, M * 8), // plate
                new MaterialStack(GTMaterials.Europium, M), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 2))); // plate

        ChemicalHelper.registerMaterialInfo(GTMachines.ENERGY_INPUT_HATCH[3].getItem(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.StainlessSteel, M * 8), // plate
                new MaterialStack(GTMaterials.Gold, M * 2), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 4), // plate
                new MaterialStack(GTMaterials.BlackSteel, M * 2), // fine wire
                new MaterialStack(GTMaterials.SteelMagnetic, M / 2) // rod
        ));

        ChemicalHelper.registerMaterialInfo(GTMachines.ENERGY_INPUT_HATCH[4].getItem(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Titanium, M * 8), // plate
                new MaterialStack(GTMaterials.Aluminium, M * 2), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 4), // plate
                new MaterialStack(GTMaterials.TungstenSteel, M * 2), // fine wire
                new MaterialStack(GTMaterials.NeodymiumMagnetic, M / 2) // rod
        ));

        ChemicalHelper.registerMaterialInfo(GTMachines.ENERGY_INPUT_HATCH[5].getItem(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.TungstenSteel, M * 8), // plate
                new MaterialStack(GTMaterials.Tungsten, M * 2), // single cable
                new MaterialStack(GTMaterials.Rubber, M * 4), // plate
                new MaterialStack(GTMaterials.Iridium, M * 2), // fine wire
                new MaterialStack(GTMaterials.NeodymiumMagnetic, M / 2) // rod
        ));

        ChemicalHelper.registerMaterialInfo(GTMachines.ENERGY_OUTPUT_HATCH[3].getItem(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.StainlessSteel, M * 8), // plate
                new MaterialStack(GTMaterials.Gold, 3 * M), // single cable + spring
                new MaterialStack(GTMaterials.Rubber, M * 2), // plate
                new MaterialStack(GTMaterials.BlackSteel, M * 2), // fine wire
                new MaterialStack(GTMaterials.SteelMagnetic, M / 2) // rod
        ));

        ChemicalHelper.registerMaterialInfo(GTMachines.ENERGY_OUTPUT_HATCH[4].getItem(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Titanium, M * 8), // plate
                new MaterialStack(GTMaterials.Aluminium, 3 * M), // single cable + spring
                new MaterialStack(GTMaterials.Rubber, M * 2), // plate
                new MaterialStack(GTMaterials.TungstenSteel, M * 2), // fine wire
                new MaterialStack(GTMaterials.NeodymiumMagnetic, M / 2) // rod
        ));

        ChemicalHelper.registerMaterialInfo(GTMachines.ENERGY_OUTPUT_HATCH[5].getItem(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.TungstenSteel, M * 8), // plate
                new MaterialStack(GTMaterials.Tungsten, 3 * M), // single cable + spring
                new MaterialStack(GTMaterials.Rubber, M * 2), // plate
                new MaterialStack(GTMaterials.Iridium, M * 2), // fine wire
                new MaterialStack(GTMaterials.NeodymiumMagnetic, M / 2) // rod
        ));

        ChemicalHelper.registerMaterialInfo(GTBlocks.PLASTCRETE.get(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Steel, M), // frame / 2
                new MaterialStack(GTMaterials.Polyethylene, M * 3), // 6 sheets / 2
                new MaterialStack(GTMaterials.Concrete, M / 2) // 1 block / 2
        ));

        ChemicalHelper.registerMaterialInfo(GTBlocks.CLEANROOM_GLASS.get(), new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Steel, M), // frame / 2
                new MaterialStack(GTMaterials.Polyethylene, M * 3), // 6 sheets / 2
                new MaterialStack(GTMaterials.Glass, M / 2) // 1 block / 2
        ));

        ChemicalHelper.registerMaterialInfo(Blocks.TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.WHITE_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.ORANGE_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.MAGENTA_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.LIGHT_BLUE_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.YELLOW_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.LIME_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.PINK_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.GRAY_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.LIGHT_GRAY_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.CYAN_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.PURPLE_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.BLUE_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.BROWN_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.GREEN_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.RED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.BLACK_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));

        ChemicalHelper.registerMaterialInfo(Blocks.WHITE_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.ORANGE_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.MAGENTA_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.YELLOW_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.LIME_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.PINK_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.GRAY_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.CYAN_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.PURPLE_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.BLUE_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.BROWN_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.GREEN_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.RED_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.BLACK_GLAZED_TERRACOTTA,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Clay, M * 4)));

        ChemicalHelper.registerMaterialInfo(GTBlocks.CASING_PRIMITIVE_BRICKS.get(),
                ConfigHolder.INSTANCE.recipes.harderBrickRecipes ?
                        new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, M * 6),
                                new MaterialStack(GTMaterials.Gypsum, M * 2)) :
                        new ItemMaterialInfo(new MaterialStack(GTMaterials.Fireclay, M * 4)));

        if (ConfigHolder.INSTANCE.recipes.hardWoodRecipes) {
            ChemicalHelper.registerMaterialInfo(Items.ACACIA_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.BIRCH_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.JUNGLE_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.OAK_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.SPRUCE_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.DARK_OAK_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.MANGROVE_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.CRIMSON_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.WARPED_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.BAMBOO_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
            ChemicalHelper.registerMaterialInfo(Items.CHERRY_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 2), new MaterialStack(GTMaterials.Iron, M / 9))); // screw
        } else {
            ChemicalHelper.registerMaterialInfo(Items.ACACIA_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.BIRCH_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.JUNGLE_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.OAK_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.SPRUCE_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.DARK_OAK_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.MANGROVE_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.CRIMSON_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.WARPED_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.BAMBOO_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Items.CHERRY_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
        }

        ChemicalHelper.registerMaterialInfo(Blocks.ACACIA_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.BIRCH_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.JUNGLE_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.OAK_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.SPRUCE_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.DARK_OAK_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.MANGROVE_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.CRIMSON_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.WARPED_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.CHERRY_PLANKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));

        if (ConfigHolder.INSTANCE.recipes.hardAdvancedIronRecipes) {
            ChemicalHelper.registerMaterialInfo(Items.IRON_DOOR, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Iron, M * 4 + (M * 3 / 16)), // 4 iron plates + 1 iron bars
                    new MaterialStack(GTMaterials.Steel, M / 9))); // tiny steel dust
        } else {
            ChemicalHelper.registerMaterialInfo(Items.IRON_DOOR,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 2)));
        }

        ChemicalHelper.registerMaterialInfo(Blocks.OAK_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.BIRCH_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.SPRUCE_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.JUNGLE_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.DARK_OAK_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.ACACIA_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.MANGROVE_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.CRIMSON_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.WARPED_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.CHERRY_FENCE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M))); // dust

        ChemicalHelper.registerMaterialInfo(Blocks.OAK_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.BIRCH_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.SPRUCE_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.JUNGLE_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.DARK_OAK_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.ACACIA_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.MANGROVE_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.CRIMSON_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.WARPED_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.CHERRY_FENCE_GATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3))); // dust

        ChemicalHelper.registerMaterialInfo(Blocks.OAK_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.BIRCH_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.SPRUCE_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.JUNGLE_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.DARK_OAK_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.ACACIA_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.MANGROVE_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.CRIMSON_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.WARPED_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_MOSAIC_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.CHERRY_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, (3 * M) / 2))); // dust small

        ChemicalHelper.registerMaterialInfo(Items.OAK_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.BIRCH_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.SPRUCE_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.JUNGLE_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.DARK_OAK_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.ACACIA_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.MANGROVE_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.BAMBOO_RAFT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.CHERRY_BOAT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 5)));

        ChemicalHelper.registerMaterialInfo(Blocks.STONE_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.SANDSTONE_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.RED_SANDSTONE_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.STONE_BRICK_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, (3 * M) / 2))); // dust small
        ChemicalHelper.registerMaterialInfo(Blocks.QUARTZ_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.NetherQuartz, M * 4))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.BRICK_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Brick, M * 4))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.NETHER_BRICK_STAIRS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Netherrack, M * 4))); // dust

        ChemicalHelper.registerMaterialInfo(Blocks.STONE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.SANDSTONE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.RED_SANDSTONE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.COBBLESTONE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.BRICK_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Brick, M * 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.STONE_BRICK_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.NETHER_BRICK_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Netherrack, M * 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.QUARTZ_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.NetherQuartz, M * 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.SMOOTH_QUARTZ_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.NetherQuartz, M * 2)));

        ChemicalHelper.registerMaterialInfo(Blocks.OAK_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.BIRCH_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.SPRUCE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.JUNGLE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.DARK_OAK_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.ACACIA_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.MANGROVE_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.CRIMSON_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.WARPED_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_MOSAIC_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.CHERRY_SLAB,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 2)));

        ChemicalHelper.registerMaterialInfo(Blocks.LEVER, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M / 9), new MaterialStack(GTMaterials.Wood, 1814400L)));

        ChemicalHelper.registerMaterialInfo(Blocks.OAK_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.BIRCH_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.SPRUCE_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.JUNGLE_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.DARK_OAK_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.ACACIA_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.MANGROVE_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.CRIMSON_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.WARPED_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.BAMBOO_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.CHERRY_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 9)));

        ChemicalHelper.registerMaterialInfo(Blocks.STONE_BUTTON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M / 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.REDSTONE_TORCH, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Wood, M / 2), new MaterialStack(GTMaterials.Redstone, M)));

        ChemicalHelper.registerMaterialInfo(Blocks.RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 3 / 16)));
        ChemicalHelper.registerMaterialInfo(Blocks.POWERED_RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.DETECTOR_RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M / 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.ACTIVATOR_RAIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M / 2)));

        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) {
            // ChemicalHelper.registerMaterialInfo(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new
            // ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M), new MaterialStack(GTMaterials.Iron, M / 2)));
            ChemicalHelper.registerMaterialInfo(Blocks.STONE_PRESSURE_PLATE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Stone, M), new MaterialStack(GTMaterials.Iron, M * 6 / 8)));
            ChemicalHelper.registerMaterialInfo(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Gold, M), new MaterialStack(GTMaterials.Steel, M)));
            ChemicalHelper.registerMaterialInfo(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Iron, M), new MaterialStack(GTMaterials.Steel, M)));
        } else {
            // ChemicalHelper.registerMaterialInfo(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE, 1, W), new
            // ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
            ChemicalHelper.registerMaterialInfo(Blocks.STONE_PRESSURE_PLATE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M * 2)));
            ChemicalHelper.registerMaterialInfo(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 2)));
            ChemicalHelper.registerMaterialInfo(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 2)));
        }

        ChemicalHelper.registerMaterialInfo(Items.WHEAT, new ItemMaterialInfo(new MaterialStack(GTMaterials.Wheat, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.HAY_BLOCK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wheat, M * 9)));

        ChemicalHelper.registerMaterialInfo(Items.SNOWBALL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Water, M / 4)));
        ChemicalHelper.registerMaterialInfo(Blocks.SNOW, new ItemMaterialInfo(new MaterialStack(GTMaterials.Water, M)));

        ChemicalHelper.registerMaterialInfo(Blocks.ICE, new ItemMaterialInfo(new MaterialStack(GTMaterials.Ice, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.PACKED_ICE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Ice, M * 9)));
        ChemicalHelper.registerMaterialInfo(Blocks.BLUE_ICE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Ice, M * 81)));

        ChemicalHelper.registerMaterialInfo(Items.BOOK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Paper, M * 3)));
        ChemicalHelper.registerMaterialInfo(Items.WRITABLE_BOOK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Paper, M * 3)));
        ChemicalHelper.registerMaterialInfo(Items.ENCHANTED_BOOK,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Paper, M * 3)));
        ChemicalHelper.registerMaterialInfo(Blocks.BOOKSHELF, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Paper, M * 9), new MaterialStack(GTMaterials.Wood, M * 6)));
        ChemicalHelper.registerMaterialInfo(Items.ENCHANTED_GOLDEN_APPLE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 72))); // block
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_APPLE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 8))); // ingot

        ChemicalHelper.registerMaterialInfo(Items.MINECART,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.CHEST_MINECART, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 5), new MaterialStack(GTMaterials.Wood, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.FURNACE_MINECART, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 5), new MaterialStack(GTMaterials.Stone, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.TNT_MINECART,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.HOPPER_MINECART, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 10), new MaterialStack(GTMaterials.Wood, M * 8)));

        ChemicalHelper.registerMaterialInfo(Items.CAULDRON,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 7)));
        ChemicalHelper.registerMaterialInfo(Blocks.IRON_BARS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 3 / 16))); // todo is this accurate
        ChemicalHelper.registerMaterialInfo(Blocks.IRON_TRAPDOOR,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 4)));
        ChemicalHelper.registerMaterialInfo(Items.BUCKET,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 3)));

        ChemicalHelper.registerMaterialInfo(Blocks.ANVIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 31)));
        ChemicalHelper.registerMaterialInfo(Blocks.CHIPPED_ANVIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 22)));
        ChemicalHelper.registerMaterialInfo(Blocks.DAMAGED_ANVIL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 13)));
        ChemicalHelper.registerMaterialInfo(Blocks.HOPPER, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 5), new MaterialStack(GTMaterials.Wood, M * 8)));

        ChemicalHelper.registerMaterialInfo(Items.GLASS_BOTTLE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Glass, M)));
        // ChemicalHelper.registerMaterialInfo(new ItemStack(Blocks.STAINED_GLASS, 1, W), new ItemMaterialInfo(new
        // MaterialStack(GTMaterials.Glass, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.GLASS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Glass, M)));
        // ChemicalHelper.registerMaterialInfo(new ItemStack(Blocks.STAINED_GLASS_PANE, 1, W), new ItemMaterialInfo(new
        // MaterialStack(GTMaterials.Glass, M / 3))); // dust tiny
        ChemicalHelper.registerMaterialInfo(Blocks.GLASS_PANE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Glass, M / 3))); // dust tiny

        ChemicalHelper.registerMaterialInfo(Items.FLOWER_POT,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Brick, M * 3)));
        ChemicalHelper.registerMaterialInfo(Items.PAINTING,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
        ChemicalHelper.registerMaterialInfo(Items.ITEM_FRAME,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.COBBLESTONE_WALL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M)));
        ChemicalHelper.registerMaterialInfo(Items.END_CRYSTAL, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Glass, M * 7), new MaterialStack(GTMaterials.EnderEye, M)));

        if (ConfigHolder.INSTANCE.recipes.hardToolArmorRecipes) {
            ChemicalHelper.registerMaterialInfo(Items.CLOCK,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, (13 * M) / 8), // M + ring + 3 * bolt
                            new MaterialStack(GTMaterials.Redstone, M)));

            ChemicalHelper.registerMaterialInfo(Items.COMPASS, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Iron, (4 * M) / 3), // M + 3*screw
                    new MaterialStack(GTMaterials.RedAlloy, M / 8), // bolt
                    new MaterialStack(GTMaterials.Zinc, M / 4))); // ring
        } else {
            ChemicalHelper.registerMaterialInfo(Items.CLOCK, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Gold, M * 4), new MaterialStack(GTMaterials.Redstone, M)));
            ChemicalHelper.registerMaterialInfo(Items.COMPASS, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Iron, M * 4), new MaterialStack(GTMaterials.Redstone, M)));
        }

        if (ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            ChemicalHelper.registerMaterialInfo(Blocks.BEACON, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.NetherStar, (7 * M) / 4), // M + lens
                    new MaterialStack(GTMaterials.Obsidian, M * 3),
                    new MaterialStack(GTMaterials.Glass, M * 4)));

            ChemicalHelper.registerMaterialInfo(Blocks.ENCHANTING_TABLE, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Diamond, M * 4),
                    new MaterialStack(GTMaterials.Obsidian, M * 3),
                    new MaterialStack(GTMaterials.Paper, M * 9)));

            ChemicalHelper.registerMaterialInfo(Blocks.ENDER_CHEST, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 8), // chest
                    new MaterialStack(GTMaterials.Obsidian, M * 9 * 6), // 6 dense plates
                    new MaterialStack(GTMaterials.EnderEye, M)));
        } else {
            ChemicalHelper.registerMaterialInfo(Blocks.BEACON,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.NetherStar, M),
                            new MaterialStack(GTMaterials.Obsidian, M * 3),
                            new MaterialStack(GTMaterials.Glass, M * 5)));
            ChemicalHelper.registerMaterialInfo(Blocks.ENCHANTING_TABLE,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Diamond, M * 2),
                            new MaterialStack(GTMaterials.Obsidian, M * 4),
                            new MaterialStack(GTMaterials.Paper, M * 3)));
            ChemicalHelper.registerMaterialInfo(Blocks.ENDER_CHEST, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.EnderEye, M), new MaterialStack(GTMaterials.Obsidian, M * 8)));
        }

        ChemicalHelper.registerMaterialInfo(Blocks.ENDER_CHEST, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.EnderEye, M), new MaterialStack(GTMaterials.Obsidian, M * 8)));

        ChemicalHelper.registerMaterialInfo(Blocks.FURNACE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M * 8)));
        ChemicalHelper.registerMaterialInfo(Blocks.STONE_BRICKS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.COBBLESTONE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.MOSSY_COBBLESTONE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M)));
        ChemicalHelper.registerMaterialInfo(Blocks.LADDER,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M)));

        ChemicalHelper.registerMaterialInfo(Items.BOWL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M / 4)));
        // ChemicalHelper.registerMaterialInfo(new ItemStack(Items.SIGN, 1, W), new ItemMaterialInfo(new
        // MaterialStack(GTMaterials.Wood, M * 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.CHEST,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 8)));
        ChemicalHelper.registerMaterialInfo(Blocks.TRAPPED_CHEST, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Wood, M * 8), new MaterialStack(GTMaterials.Iron, M / 2))); // ring

        if (ConfigHolder.INSTANCE.recipes.hardMiscRecipes) {
            ChemicalHelper.registerMaterialInfo(Blocks.NOTE_BLOCK, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 8), new MaterialStack(GTMaterials.RedAlloy, M / 2))); // rod
            ChemicalHelper.registerMaterialInfo(Blocks.JUKEBOX, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Diamond, M / 8), // bolt
                    new MaterialStack(GTMaterials.Iron, (17 * M) / 4), // gear + ring
                    new MaterialStack(GTMaterials.RedAlloy, M)));
        } else {
            ChemicalHelper.registerMaterialInfo(Blocks.NOTE_BLOCK, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 8), new MaterialStack(GTMaterials.Redstone, M)));
            ChemicalHelper.registerMaterialInfo(Blocks.JUKEBOX, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Wood, M * 8), new MaterialStack(GTMaterials.Diamond, M)));
        }
        ChemicalHelper.registerMaterialInfo(Blocks.REDSTONE_LAMP, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Glowstone, M * 4), new MaterialStack(GTMaterials.Redstone, M * 4))); // dust
        ChemicalHelper.registerMaterialInfo(Blocks.CRAFTING_TABLE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2)));
        ChemicalHelper.registerMaterialInfo(Blocks.PISTON, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 4), new MaterialStack(GTMaterials.Wood, M * 3)));
        ChemicalHelper.registerMaterialInfo(Blocks.STICKY_PISTON, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 4), new MaterialStack(GTMaterials.Wood, M * 3)));
        if (ConfigHolder.INSTANCE.recipes.hardRedstoneRecipes) {
            ChemicalHelper.registerMaterialInfo(Blocks.DISPENSER,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M * 2),
                            new MaterialStack(GTMaterials.RedAlloy, M / 2),
                            new MaterialStack(GTMaterials.Iron, M * 4 + M / 4)));
            ChemicalHelper.registerMaterialInfo(Blocks.DROPPER,
                    new ItemMaterialInfo(new MaterialStack(GTMaterials.Stone, M * 2),
                            new MaterialStack(GTMaterials.RedAlloy, M / 2),
                            new MaterialStack(GTMaterials.Iron, M * 2 + M * 3 / 4)));
        } else {
            ChemicalHelper.registerMaterialInfo(Blocks.DISPENSER, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Redstone, M)));
            ChemicalHelper.registerMaterialInfo(Blocks.DROPPER, new ItemMaterialInfo(
                    new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Redstone, M)));
        }

        ChemicalHelper.registerMaterialInfo(Items.IRON_HELMET,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_CHESTPLATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_LEGGINGS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 7)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_BOOTS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 4)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_HORSE_ARMOR,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_SHOVEL, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_PICKAXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_AXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_SWORD, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.IRON_HOE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Iron, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));

        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_HELMET,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_CHESTPLATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_LEGGINGS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 7)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_BOOTS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 4)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_HORSE_ARMOR,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Gold, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_SHOVEL, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Gold, M), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_PICKAXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Gold, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_AXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Gold, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_SWORD, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Gold, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.GOLDEN_HOE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Gold, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));

        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_HELMET,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Diamond, M * 5)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_CHESTPLATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Diamond, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_LEGGINGS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Diamond, M * 7)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_BOOTS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Diamond, M * 4)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_HORSE_ARMOR,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Diamond, M * 8)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_SHOVEL, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Diamond, M), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_PICKAXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Diamond, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_AXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Diamond, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_SWORD, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Diamond, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.DIAMOND_HOE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Diamond, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));

        ChemicalHelper.registerMaterialInfo(Items.CHAINMAIL_HELMET,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 5 / 4)));
        ChemicalHelper.registerMaterialInfo(Items.CHAINMAIL_CHESTPLATE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 2)));
        ChemicalHelper.registerMaterialInfo(Items.CHAINMAIL_LEGGINGS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M * 7 / 4)));
        ChemicalHelper.registerMaterialInfo(Items.CHAINMAIL_BOOTS,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Iron, M)));

        ChemicalHelper.registerMaterialInfo(Items.WOODEN_SHOVEL,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M + M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.WOODEN_PICKAXE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3 + M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.WOODEN_AXE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 3 + M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.WOODEN_HOE,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2 + M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.WOODEN_SWORD,
                new ItemMaterialInfo(new MaterialStack(GTMaterials.Wood, M * 2 + M / 4)));

        ChemicalHelper.registerMaterialInfo(Items.STONE_SHOVEL, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.STONE_PICKAXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.STONE_AXE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 3), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.STONE_HOE, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Wood, M / 2)));
        ChemicalHelper.registerMaterialInfo(Items.STONE_SWORD, new ItemMaterialInfo(
                new MaterialStack(GTMaterials.Stone, M * 2), new MaterialStack(GTMaterials.Wood, M / 4)));
    }
}
