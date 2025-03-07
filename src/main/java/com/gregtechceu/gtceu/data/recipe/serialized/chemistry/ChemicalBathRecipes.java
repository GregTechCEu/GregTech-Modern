package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_BATH_RECIPES;

public class ChemicalBathRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_wood_dust")
                .inputItems(DUST, Wood)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(200).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_paper_dust")
                .inputItems(DUST, Paper)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_sugar_cane")
                .inputItems(Items.SUGAR_CANE)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_wood_dust_distilled")
                .inputItems(DUST, Wood)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(200).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_paper_dust_distilled")
                .inputItems(DUST, Paper)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_sugar_cane_distilled")
                .inputItems(Items.SUGAR_CANE)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("treated_planks")
                .inputItems(ItemTags.PLANKS)
                .inputFluids(Creosote.getFluid(100))
                .outputItems(GTBlocks.TREATED_WOOD_PLANK.asStack())
                .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("light_to_dark_concrete")
                .inputItems(GTBlocks.LIGHT_CONCRETE.asStack())
                .inputFluids(Water.getFluid(100))
                .outputItems(GTBlocks.DARK_CONCRETE.asStack())
                .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("tungstic_acid_from_scheelite")
                .inputItems(DUST, Scheelite, 6)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(DUST, TungsticAcid, 7)
                .outputItems(DUST, CalciumChloride, 3)
                .duration(210).EUt(960).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("tungstic_acid_from_tungstate")
                .inputItems(DUST, Tungstate, 7)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(DUST, TungsticAcid, 7)
                .outputItems(DUST, LithiumChloride, 4)
                .duration(210).EUt(960).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("kanthal_cool_down")
                .inputItems(INGOT_HOT, Kanthal)
                .inputFluids(Water.getFluid(100))
                .outputItems(INGOT, Kanthal)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("kanthal_cool_down_distilled_water")
                .inputItems(INGOT_HOT, Kanthal)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(INGOT, Kanthal)
                .duration(250).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("silicon_cool_down")
                .inputItems(INGOT_HOT, Silicon)
                .inputFluids(Water.getFluid(100))
                .outputItems(INGOT, Silicon)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("silicon_cool_down_distilled_water")
                .inputItems(INGOT_HOT, Silicon)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(INGOT, Silicon)
                .duration(250).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("black_steel_cool_down")
                .inputItems(INGOT_HOT, BlackSteel)
                .inputFluids(Water.getFluid(100))
                .outputItems(INGOT, BlackSteel)
                .duration(200).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("black_steel_cool_down_distilled_water")
                .inputItems(INGOT_HOT, BlackSteel)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(INGOT, BlackSteel)
                .duration(125).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("red_steel_cool_down")
                .inputItems(INGOT_HOT, RedSteel)
                .inputFluids(Water.getFluid(100))
                .outputItems(INGOT, RedSteel)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("red_steel_cool_down_distilled_water")
                .inputItems(INGOT_HOT, RedSteel)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(INGOT, RedSteel)
                .duration(250).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("blue_steel_cool_down")
                .inputItems(INGOT_HOT, BlueSteel)
                .inputFluids(Water.getFluid(100))
                .outputItems(INGOT, BlueSteel)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("blue_steel_cool_down_distilled_water")
                .inputItems(INGOT_HOT, BlueSteel)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(INGOT, BlueSteel)
                .duration(250).EUt(VA[MV]).save(provider);
    }
}
