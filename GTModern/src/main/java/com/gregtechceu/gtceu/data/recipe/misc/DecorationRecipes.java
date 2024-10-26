package com.gregtechceu.gtceu.data.recipe.misc;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.DyeColor;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.block;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LARGE_METAL_SHEETS;
import static com.gregtechceu.gtceu.common.data.GTBlocks.METAL_SHEETS;
import static com.gregtechceu.gtceu.common.data.GTBlocks.STUDS;
import static com.gregtechceu.gtceu.common.data.GTMaterials.CHEMICAL_DYES;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Concrete;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Iron;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Rubber;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_BATH_RECIPES;

public class DecorationRecipes {

    private DecorationRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        assemblerRecipes(provider);
        dyeRecipes(provider);
    }

    private static void assemblerRecipes(Consumer<FinishedRecipe> provider) {
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

    private static void dyeRecipes(Consumer<FinishedRecipe> provider) {
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
