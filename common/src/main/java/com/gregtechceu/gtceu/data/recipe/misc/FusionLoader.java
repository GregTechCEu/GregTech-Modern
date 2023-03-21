package com.gregtechceu.gtceu.data.recipe.misc;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class FusionLoader {

    // todo fusion reactor
    public static void init(Consumer<FinishedRecipe> provider) {
/*
        FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Deuterium.getFluid(125))
                .fluidInputs(Materials.Tritium.getFluid(125))
                .fluidOutputs(Materials.Helium.getPlasma(125))
                .duration(16)
                .EUt(4096)
                .EUToStart(40_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Carbon.getFluid(16))
                .fluidInputs(Materials.Helium3.getFluid(125))
                .fluidOutputs(Materials.Oxygen.getPlasma(125))
                .duration(32)
                .EUt(4096)
                .EUToStart(180_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Beryllium.getFluid(16))
                .fluidInputs(Materials.Deuterium.getFluid(375))
                .fluidOutputs(Materials.Nitrogen.getPlasma(125))
                .duration(16)
                .EUt(16384)
                .EUToStart(280_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Silicon.getFluid(16))
                .fluidInputs(Materials.Magnesium.getFluid(16))
                .fluidOutputs(Materials.Iron.getPlasma(16))
                .duration(32)
                .EUt(VA[IV])
                .EUToStart(360_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Potassium.getFluid(16))
                .fluidInputs(Materials.Fluorine.getFluid(125))
                .fluidOutputs(Materials.Nickel.getPlasma(16))
                .duration(16)
                .EUt(VA[LuV])
                .EUToStart(480_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Carbon.getFluid(16))
                .fluidInputs(Materials.Magnesium.getFluid(16))
                .fluidOutputs(Materials.Argon.getPlasma(125))
                .duration(32)
                .EUt(24576)
                .EUToStart(180_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Neodymium.getFluid(16))
                .fluidInputs(Materials.Hydrogen.getFluid(375))
                .fluidOutputs(Materials.Europium.getFluid(16))
                .duration(64)
                .EUt(24576)
                .EUToStart(150_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Lutetium.getFluid(32))
                .fluidInputs(Materials.Chrome.getFluid(32))
                .fluidOutputs(Materials.Americium.getFluid(32))
                .duration(64)
                .EUt(49152)
                .EUToStart(200_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Americium.getFluid(128))
                .fluidInputs(Materials.Naquadria.getFluid(128))
                .fluidOutputs(Materials.Neutronium.getFluid(32))
                .duration(200)
                .EUt(98304)
                .EUToStart(600_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Silver.getFluid(16))
                .fluidInputs(Materials.Copper.getFluid(16))
                .fluidOutputs(Materials.Osmium.getFluid(16))
                .duration(64)
                .EUt(24578)
                .EUToStart(150_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Mercury.getFluid(125))
                .fluidInputs(Materials.Magnesium.getFluid(16))
                .fluidOutputs(Materials.Uranium235.getFluid(16))
                .duration(128)
                .EUt(24576)
                .EUToStart(140_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Gold.getFluid(16))
                .fluidInputs(Materials.Aluminium.getFluid(16))
                .fluidOutputs(Materials.Uranium238.getFluid(16))
                .duration(128)
                .EUt(24576)
                .EUToStart(140_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Xenon.getFluid(125))
                .fluidInputs(Materials.Zinc.getFluid(16))
                .fluidOutputs(Materials.Plutonium239.getFluid(16))
                .duration(128)
                .EUt(49152)
                .EUToStart(120_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Krypton.getFluid(125))
                .fluidInputs(Materials.Cerium.getFluid(16))
                .fluidOutputs(Materials.Plutonium241.getFluid(16))
                .duration(128)
                .EUt(49152)
                .EUToStart(240_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Hydrogen.getFluid(125))
                .fluidInputs(Materials.Vanadium.getFluid(16))
                .fluidOutputs(Materials.Chrome.getFluid(16))
                .duration(64)
                .EUt(24576)
                .EUToStart(140_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Gallium.getFluid(16))
                .fluidInputs(Materials.Radon.getFluid(125))
                .fluidOutputs(Materials.Duranium.getFluid(16))
                .duration(64)
                .EUt(16384)
                .EUToStart(140_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Titanium.getFluid(32))
                .fluidInputs(Materials.Duranium.getFluid(32))
                .fluidOutputs(Materials.Tritanium.getFluid(16))
                .duration(64)
                .EUt(VA[LuV])
                .EUToStart(200_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Gold.getFluid(16))
                .fluidInputs(Materials.Mercury.getFluid(16))
                .fluidOutputs(Materials.Radon.getFluid(125))
                .duration(64)
                .EUt(VA[LuV])
                .EUToStart(200_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Silver.getFluid(144))
                .fluidInputs(Materials.Lithium.getFluid(144))
                .fluidOutputs(Materials.Indium.getFluid(144))
                .duration(16)
                .EUt(24576)
                .EUToStart(280_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.NaquadahEnriched.getFluid(16))
                .fluidInputs(Materials.Radon.getFluid(125))
                .fluidOutputs(Materials.Naquadria.getFluid(4))
                .duration(64)
                .EUt(49152)
                .EUToStart(400_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Lanthanum.getFluid(16))
                .fluidInputs(Materials.Silicon.getFluid(16))
                .fluidOutputs(Materials.Lutetium.getFluid(16))
                .duration(16)
                .EUt(VA[IV])
                .EUToStart(80_000_000)
                .buildAndRegister();

        RecipeMaps.FUSION_RECIPES.recipeBuilder()
                .fluidInputs(Materials.Arsenic.getFluid(32))
                .fluidInputs(Materials.Ruthenium.getFluid(16))
                .fluidOutputs(Materials.Darmstadtium.getFluid(16))
                .duration(32)
                .EUt(VA[LuV])
                .EUToStart(200_000_000)
                .buildAndRegister();
*/
    }
}
