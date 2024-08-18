package com.gregtechceu.gtceu.data.recipe.misc;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.data.block.GTBlocks.*;
import static com.gregtechceu.gtceu.data.material.GTMaterials.*;
import static com.gregtechceu.gtceu.data.recipe.GTRecipeTypes.*;

public class DecorationRecipes {

    private DecorationRecipes() {}

    public static void init(RecipeOutput provider) {
        assemblerRecipes(provider);
        dyeRecipes(provider);
    }

    private static void assemblerRecipes(RecipeOutput provider) {
        ASSEMBLER_RECIPES.recipeBuilder("metal_sheet_white")
                .inputItems(block, Concrete, 5)
                .inputItems(plate, Iron, 2)
                .circuitMeta(8)
                .outputItems(METAL_SHEETS.get(DyeColor.WHITE), 32)
                .EUt(4).duration(20)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_metal_sheet_white")
                .inputItems(block, Concrete, 5)
                .inputItems(plate, Iron, 4)
                .circuitMeta(9)
                .outputItems(LARGE_METAL_SHEETS.get(DyeColor.WHITE), 32)
                .EUt(4).duration(20)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("studs_black")
                .inputItems(block, Concrete, 3)
                .inputItems(plate, Rubber, 3)
                .circuitMeta(8)
                .outputItems(STUDS.get(DyeColor.BLACK), 32)
                .EUt(4).duration(20)
                .save(provider);
    }

    private static void dyeRecipes(RecipeOutput provider) {
        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            var color = DyeColor.values()[i];
            var colorName = color.getName();
            CHEMICAL_BATH_RECIPES.recipeBuilder("metal_sheet_%s".formatted(colorName))
                    .inputItems(METAL_SHEETS.get(DyeColor.WHITE).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(METAL_SHEETS.get(color))
                    .EUt(2).duration(10)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("large_metal_sheet_%s".formatted(colorName))
                    .inputItems(LARGE_METAL_SHEETS.get(DyeColor.WHITE).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(LARGE_METAL_SHEETS.get(color))
                    .EUt(2).duration(10)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("studs_%s".formatted(colorName))
                    .inputItems(STUDS.get(DyeColor.BLACK).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(STUDS.get(color))
                    .EUt(2).duration(10)
                    .save(provider);
        }
    }
}
