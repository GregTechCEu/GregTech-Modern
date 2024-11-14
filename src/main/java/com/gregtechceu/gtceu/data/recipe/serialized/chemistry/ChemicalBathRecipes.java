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

        CHEMICAL_BATH_RECIPES.recipeBuilder("kanthal_cool_down")
                .inputItems(ingotHot, Kanthal)
                .inputFluids(Water.getFluid(100))
                .outputItems(ingot, Kanthal)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("kanthal_cool_down_distilled_water")
                .inputItems(ingotHot, Kanthal)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(ingot, Kanthal)
                .duration(250).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("silicon_cool_down")
                .inputItems(ingotHot, Silicon)
                .inputFluids(Water.getFluid(100))
                .outputItems(ingot, Silicon)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("silicon_cool_down_distilled_water")
                .inputItems(ingotHot, Silicon)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(ingot, Silicon)
                .duration(250).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("black_steel_cool_down")
                .inputItems(ingotHot, BlackSteel)
                .inputFluids(Water.getFluid(100))
                .outputItems(ingot, BlackSteel)
                .duration(200).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("black_steel_cool_down_distilled_water")
                .inputItems(ingotHot, BlackSteel)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(ingot, BlackSteel)
                .duration(125).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("red_steel_cool_down")
                .inputItems(ingotHot, RedSteel)
                .inputFluids(Water.getFluid(100))
                .outputItems(ingot, RedSteel)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("red_steel_cool_down_distilled_water")
                .inputItems(ingotHot, RedSteel)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(ingot, RedSteel)
                .duration(250).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("blue_steel_cool_down")
                .inputItems(ingotHot, BlueSteel)
                .inputFluids(Water.getFluid(100))
                .outputItems(ingot, BlueSteel)
                .duration(400).EUt(VA[MV]).save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("blue_steel_cool_down_distilled_water")
                .inputItems(ingotHot, BlueSteel)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(ingot, BlueSteel)
                .duration(250).EUt(VA[MV]).save(provider);
    }
}
