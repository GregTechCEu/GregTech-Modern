package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.common.data.GTRecipeCategories;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.block;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LARGE_METAL_SHEETS;
import static com.gregtechceu.gtceu.common.data.GTBlocks.METAL_SHEETS;
import static com.gregtechceu.gtceu.common.data.GTBlocks.STUDS;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_BATH_RECIPES;

public class DecorationRecipes {

    private DecorationRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        assemblerRecipes(provider);
        dyeRecipes(provider);
        copperOxidationRecipes(provider);
    }

    private static void assemblerRecipes(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("metal_sheet_white")
                .inputItems(block, Concrete, 5)
                .inputItems(plate, Iron, 2)
                .circuitMeta(8)
                .outputItems(METAL_SHEETS.get(DyeColor.WHITE), 32)
                .EUt(4).duration(20)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_metal_sheet_white")
                .inputItems(block, Concrete, 5)
                .inputItems(plate, Iron, 4)
                .circuitMeta(9)
                .outputItems(LARGE_METAL_SHEETS.get(DyeColor.WHITE), 32)
                .EUt(4).duration(20)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("studs_black")
                .inputItems(block, Concrete, 3)
                .inputItems(plate, Rubber, 3)
                .circuitMeta(8)
                .outputItems(STUDS.get(DyeColor.BLACK), 32)
                .EUt(4).duration(20)
                .addMaterialInfo(true).save(provider);
    }

    private static void dyeRecipes(Consumer<FinishedRecipe> provider) {
        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            var color = DyeColor.values()[i];
            var colorName = color.getName();
            CHEMICAL_BATH_RECIPES.recipeBuilder("metal_sheet_%s".formatted(colorName))
                    .inputItems(METAL_SHEETS.get(DyeColor.WHITE).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(METAL_SHEETS.get(color))
                    .EUt(2).duration(10)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("large_metal_sheet_%s".formatted(colorName))
                    .inputItems(LARGE_METAL_SHEETS.get(DyeColor.WHITE).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(LARGE_METAL_SHEETS.get(color))
                    .EUt(2).duration(10)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("studs_%s".formatted(colorName))
                    .inputItems(STUDS.get(DyeColor.BLACK).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(STUDS.get(color))
                    .EUt(2).duration(10)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);
        }
    }

    private static void copperOxidationRecipes(Consumer<FinishedRecipe> provider) {
        registerOxidationChain(provider, "copper_block", Items.COPPER_BLOCK, Items.EXPOSED_COPPER,
                Items.WEATHERED_COPPER, Items.OXIDIZED_COPPER);
        registerOxidationChain(provider, "cut_copper", Items.CUT_COPPER, Items.EXPOSED_CUT_COPPER,
                Items.WEATHERED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER);
        registerOxidationChain(provider, "cut_copper_stairs", Items.CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS,
                Items.WEATHERED_CUT_COPPER_STAIRS, Items.OXIDIZED_CUT_COPPER_STAIRS);
        registerOxidationChain(provider, "cut_copper_slab", Items.CUT_COPPER_SLAB, Items.EXPOSED_CUT_COPPER_SLAB,
                Items.WEATHERED_CUT_COPPER_SLAB, Items.OXIDIZED_CUT_COPPER_SLAB);

        // Waxing recipes
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_copper_block")
                .inputItems(Items.COPPER_BLOCK)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_COPPER_BLOCK)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_copper")
                .inputItems(Items.EXPOSED_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_EXPOSED_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_copper")
                .inputItems(Items.WEATHERED_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_WEATHERED_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_copper")
                .inputItems(Items.OXIDIZED_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_OXIDIZED_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_cut_copper")
                .inputItems(Items.CUT_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_CUT_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_cut_copper")
                .inputItems(Items.EXPOSED_CUT_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_EXPOSED_CUT_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_cut_copper")
                .inputItems(Items.WEATHERED_CUT_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_WEATHERED_CUT_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_cut_copper")
                .inputItems(Items.OXIDIZED_CUT_COPPER)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_OXIDIZED_CUT_COPPER)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_cut_copper_stairs")
                .inputItems(Items.CUT_COPPER_STAIRS)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_CUT_COPPER_STAIRS)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_cut_copper_stairs")
                .inputItems(Items.EXPOSED_CUT_COPPER_STAIRS)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_EXPOSED_CUT_COPPER_STAIRS)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_cut_copper_stairs")
                .inputItems(Items.WEATHERED_CUT_COPPER_STAIRS)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_WEATHERED_CUT_COPPER_STAIRS)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_cut_copper_stairs")
                .inputItems(Items.OXIDIZED_CUT_COPPER_STAIRS)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_cut_copper_slab")
                .inputItems(Items.CUT_COPPER_SLAB)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_CUT_COPPER_SLAB)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_cut_copper_slab")
                .inputItems(Items.EXPOSED_CUT_COPPER_SLAB)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_EXPOSED_CUT_COPPER_SLAB)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_cut_copper_slab")
                .inputItems(Items.WEATHERED_CUT_COPPER_SLAB)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_WEATHERED_CUT_COPPER_SLAB)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_cut_copper_slab")
                .inputItems(Items.OXIDIZED_CUT_COPPER_SLAB)
                .inputFluids(Wax, L / 2)
                .outputItems(Items.WAXED_OXIDIZED_CUT_COPPER_SLAB)
                .EUt(VA[ULV]).duration(10)
                .save(provider);
    }

    private static final String[] OXIDATION_STAGES = { "raw", "exposed", "weathered", "oxidized" };

    private static void registerOxidationChain(Consumer<FinishedRecipe> provider, String name, Item... items) {
        for (int i = 0; i < items.length - 1; i++) {
            CHEMICAL_BATH_RECIPES
                    .recipeBuilder(
                            "%s_to_%s_%s_oxidation".formatted(OXIDATION_STAGES[i], OXIDATION_STAGES[i + 1], name))
                    .inputItems(items[i])
                    .inputFluids(Oxygen, 100)
                    .outputItems(items[i + 1])
                    .EUt(VA[ULV]).duration(10)
                    .save(provider);
        }
    }
}
