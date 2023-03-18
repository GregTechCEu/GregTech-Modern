package com.lowdragmc.gtceu.data.recipe.chemistry;

import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
import static com.lowdragmc.gtceu.common.libs.GTItems.*;

public class BrewingRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        for (Material material : new Material[]{Talc, Soapstone, Redstone}) {
            BREWING_RECIPES.recipeBuilder(material.getName() + "_oil_lubricant")
                    .inputItems(dust, material)
                    .inputFluids(Oil.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save(provider);

            BREWING_RECIPES.recipeBuilder(material.getName() + "_creosote_lubricant")
                    .inputItems(dust, material)
                    .inputFluids(Creosote.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save(provider);

            BREWING_RECIPES.recipeBuilder(material.getName() + "_seed_oil_lubricant")
                    .inputItems(dust, material)
                    .inputFluids(SeedOil.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save(provider);
        }

        // Biomass
        BREWING_RECIPES.recipeBuilder("biomass.0").duration(800).EUt(3).inputItems(Ingredient.of(ItemTags.SAPLINGS)).inputFluids(Water.getFluid(100)).outputFluids(Biomass.getFluid(100)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.1").duration(160).EUt(3).inputItems(new ItemStack(Items.POTATO)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.2").duration(160).EUt(3).inputItems(new ItemStack(Items.CARROT)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.3").duration(160).EUt(3).inputItems(new ItemStack(Blocks.CACTUS)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.4").duration(160).EUt(3).inputItems(new ItemStack(Items.SUGAR_CANE)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.5").duration(160).EUt(3).inputItems(new ItemStack(Blocks.BROWN_MUSHROOM)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.6").duration(160).EUt(3).inputItems(new ItemStack(Blocks.RED_MUSHROOM)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.7").duration(160).EUt(3).inputItems(new ItemStack(Items.BEETROOT)).inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save(provider);
        BREWING_RECIPES.recipeBuilder("biomass.8").EUt(4).duration(128).inputItems(BIO_CHAFF.asStack()).inputFluids(Water.getFluid(750)).outputFluids(Biomass.getFluid(750)).save(provider);
    }
}
