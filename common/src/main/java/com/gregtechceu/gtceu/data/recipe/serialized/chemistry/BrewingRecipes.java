package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTItems.BIO_CHAFF;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.BREWING_RECIPES;

public class BrewingRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        for (Material material : new Material[]{Talc, Soapstone, Redstone}) {
            BREWING_RECIPES.recipeBuilder("lubricant_from_oil_and_" + material.getName())
                    .inputItems(dust, material)
                    .inputFluids(Oil.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save(provider);

            BREWING_RECIPES.recipeBuilder("lubricant_from_creosote_and_" + material.getName())
                    .inputItems(dust, material)
                    .inputFluids(Creosote.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save(provider);

            BREWING_RECIPES.recipeBuilder("lubricant_from_seed_oil_and_" + material.getName())
                    .inputItems(dust, material)
                    .inputFluids(SeedOil.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save(provider);
        }

        // Biomass
        // todo tag?
        //BREWING_RECIPES.recipeBuilder("biomass_from_sapling").duration(800).EUt(3).inputItems("treeSapling", 1).inputFluids(Water.getFluid(100)).outputFluids(Biomass.getFluid(100)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_potato").duration(160).EUt(3).inputItems(Items.POTATO).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_carrot").duration(160).EUt(3).inputItems(Items.CARROT).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_cactus").duration(160).EUt(3).inputItems(Blocks.CACTUS.asItem()).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_sugar_cane").duration(160).EUt(3).inputItems(Items.SUGAR_CANE).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_brown_mushroom").duration(160).EUt(3).inputItems(Blocks.BROWN_MUSHROOM.asItem()).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_red_mushroom").duration(160).EUt(3).inputItems(Blocks.RED_MUSHROOM.asItem()).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_beetroot").duration(160).EUt(3).inputItems(Items.BEETROOT).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass_from_bio_chaff").EUt(4).duration(128).inputItems(BIO_CHAFF).inputFluids(Water.getFluid(750)).outputFluids(Biomass.getFluid(750)).save(provider);
        // TODO 1.13+ plants
    }
}
