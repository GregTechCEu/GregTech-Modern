package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_BATH_RECIPES;

public class ChemicalBathRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_wood_dust")
                .inputItems(dust, Wood)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(200).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_paper_dust")
                .inputItems(dust, Paper)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_sugar_cane")
                .inputItems(Items.SUGAR_CANE)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_wood_dust_distilled")
                .inputItems(dust, Wood)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(200).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_paper_dust_distilled")
                .inputItems(dust, Paper)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper_from_sugar_cane_distilled")
                .inputItems(Items.SUGAR_CANE)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER)
                .duration(100).EUt(VA[ULV]).save(provider);

        // TODO Treated Wood
        //CHEMICAL_BATH_RECIPES.recipeBuilder("treated_planks")
        //        .inputItems("plankWood", 1)
        //        .inputFluids(Creosote.getFluid(100))
        //        .outputs(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK))
        //        .duration(100).EUt(VA[ULV]).save(provider);

        // TODO Concrete
        //CHEMICAL_BATH_RECIPES.recipeBuilder()
        //        .inputs(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.CONCRETE_LIGHT))
        //        .inputFluids(Water.getFluid(100))
        //        .outputs(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.CONCRETE_DARK))
        //        .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("tungstic_acid_from_scheelite")
                .inputItems(dust, Scheelite, 6)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(dust, TungsticAcid, 7)
                .outputItems(dust, CalciumChloride, 3)
                .duration(210).EUt(960).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("tungstic_acid_from_tungstate")
                .inputItems(dust, Tungstate, 7)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(dust, TungsticAcid, 7)
                .outputItems(dust, LithiumChloride, 4)
                .duration(210).EUt(960).save(provider);
    }
}
