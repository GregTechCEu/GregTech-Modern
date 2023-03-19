package com.gregtechceu.gtceu.data.recipe.chemistry;

import com.gregtechceu.gtceu.api.GTValues;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class ChemicalBathRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper.0")
                .inputItems(dust, Wood)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER.getDefaultInstance())
                .duration(200).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper.1")
                .inputItems(dust, Paper)
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER.getDefaultInstance())
                .duration(100).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper.2")
                .inputItems(Items.SUGAR_CANE.getDefaultInstance())
                .inputFluids(Water.getFluid(100))
                .outputItems(Items.PAPER.getDefaultInstance())
                .duration(100).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper.3")
                .inputItems(dust, Wood)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER.getDefaultInstance())
                .duration(200).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper.4")
                .inputItems(dust, Paper)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER.getDefaultInstance())
                .duration(100).EUt(4).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("paper.5")
                .inputItems(Items.SUGAR_CANE.getDefaultInstance())
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(Items.PAPER.getDefaultInstance())
                .duration(100).EUt(GTValues.VA[GTValues.ULV]).save(provider);

        // TODO PLANK
//        CHEMICAL_BATH_RECIPES.recipeBuilder()
//                .inputItems(Ingredient.of(ItemTags.PLANKS))
//                .inputFluids(Creosote.getFluid(100))
//                .outputs(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK))
//                .duration(100).EUt(VA[ULV]).save(provider);

        // TODO STONE
//        CHEMICAL_BATH_RECIPES.recipeBuilder()
//                .inputItems(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.CONCRETE_LIGHT))
//                .inputFluids(Water.getFluid(100))
//                .outputs(MetaBlocks.STONE_SMOOTH.getItemVariant(BlockStoneSmooth.BlockType.CONCRETE_DARK))
//                .duration(100).EUt(VA[ULV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("tungstic_acid_calcium_chloride")
                .inputItems(dust, Scheelite, 6)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(dust, TungsticAcid, 7)
                .outputItems(dust, CalciumChloride, 3)
                .duration(210).EUt(960).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("tungstic_acid_lithium_chloride")
                .inputItems(dust, Tungstate, 7)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(dust, TungsticAcid, 7)
                .outputItems(dust, LithiumChloride, 4)
                .duration(210).EUt(960).save(provider);
    }
}
