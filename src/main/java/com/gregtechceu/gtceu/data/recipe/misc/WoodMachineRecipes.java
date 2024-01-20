package com.gregtechceu.gtceu.data.recipe.misc;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.BIO_CHAFF;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.PYROLYSE_RECIPES;

public class WoodMachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        registerPyrolyseOvenRecipes(provider);
    }

    // TODO Log stuff (complicated)
    /*
    public static void postInit() {
        processLogOreDictionary();
    }

    private static void processLogOreDictionary() {
        List<ItemStack> allWoodLogs = OreDictUnifier.getAllWithOreDictionaryName("logWood").stream()
                .flatMap(stack -> GTUtility.getAllSubItems(stack).stream())
                .collect(Collectors.toList());

        for (ItemStack stack : allWoodLogs) {
            Pair<IRecipe, ItemStack> outputPair = ModHandler.getRecipeoutputItems(null, stack);
            ItemStack plankStack = outputPair.getValue();
            int originalOutput = plankStack.getCount();
            if (plankStack.isEmpty()) {
                continue;
            }
            IRecipe outputRecipe = outputPair.getKey();

            //wood nerf
            if (ConfigHolder.INSTANCE.recipes.nerfWoodCrafting) {
                //remove the old recipe
                ModHandler.removeRecipeByName(outputRecipe.getRegistryName());

                // new wood recipes
                //noinspection ConstantConditions
                ModHandler.addShapelessRecipe(outputRecipe.getRegistryName().toString(),
                        GTUtility.copyAmount(Math.max(1, originalOutput / 2), plankStack), stack);

                ModHandler.addShapedRecipe(outputRecipe.getRegistryName().getPath() + "_saw",
                        GTUtility.copyAmount(originalOutput, plankStack), "s", "L", 'L', stack);
            } else {
                //noinspection ConstantConditions
                ModHandler.addShapedRecipe(outputRecipe.getRegistryName().getPath() + "_saw",
                        GTUtility.copyAmount((int) (originalOutput * 1.5), plankStack), "s", "L", 'L', stack);
            }


            CUTTER_RECIPES.recipeBuilder().inputItems(stack)
                    .inputFluids(Lubricant.getFluid(1))
                    .outputItems(GTUtility.copyAmount((int) (originalOutput * 1.5), plankStack), OreDictUnifier.get(dust, Wood, 2))
                    .duration(200).EUt(VA[ULV])
                    .save(provider;

            ItemStack doorStack = ModHandler.getRecipeoutputItems(DummyWorld.INSTANCE,
                    plankStack, plankStack, ItemStack.EMPTY,
                    plankStack, plankStack, ItemStack.EMPTY,
                    plankStack, plankStack, ItemStack.EMPTY).getRight();

            if (!doorStack.isEmpty()) {
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItems(GTUtility.copyAmount(6, plankStack))
                        .outputItems(doorStack)
                        .duration(600).EUt(4).circuitMeta(6)
                        .save(provider;
            }

            ItemStack slabStack = ModHandler.getRecipeoutputItems(DummyWorld.INSTANCE, plankStack, plankStack, plankStack).getRight();

            if (!slabStack.isEmpty()) {
                CUTTER_RECIPES.recipeBuilder()
                        .inputItems(GTUtility.copyAmount(1, plankStack))
                        .outputItems(GTUtility.copyAmount(2, slabStack))
                        .duration(200).EUt(VA[ULV])
                        .save(provider;

                ModHandler.addShapedRecipe(slabStack.getDisplayName() + "_saw", GTUtility.copyAmount(2, slabStack), "sS", 'S', GTUtility.copyAmount(1, plankStack));
            }

            if (ConfigHolder.INSTANCE.recipes.harderCharcoalRecipe) {
                ItemStack outputStack = FurnaceRecipes.instance().getSmeltingResult(stack);
                if (outputStack.getItem() == Items.COAL && outputStack.getItemDamage() == 1) {
                    ModHandler.removeFurnaceSmelting(stack);
                }
            }
        }
    }
     */

    private static void registerPyrolyseOvenRecipes(Consumer<FinishedRecipe> provider) {
        // Logs ================================================

        // Charcoal Byproducts
        PYROLYSE_RECIPES.recipeBuilder("log_to_charcoal_byproducts").circuitMeta(4)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(CharcoalByproducts.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);

        // Wood Tar
        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_tar").circuitMeta(9)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(WoodTar.getFluid(1500))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_tar_nitrogen").circuitMeta(10)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(WoodTar.getFluid(1500))
                .duration(320).EUt(96)
                .save(provider);

        // Wood Gas
        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_gas").circuitMeta(5)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(WoodGas.getFluid(1500))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_gas_nitrogen").circuitMeta(6)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(WoodGas.getFluid(1500))
                .duration(320).EUt(96)
                .save(provider);

        // Wood Vinegar
        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_vinegar").circuitMeta(7)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(WoodVinegar.getFluid(3000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_wood_vinegar_nitrogen").circuitMeta(8)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(WoodVinegar.getFluid(3000))
                .duration(320).EUt(96)
                .save(provider);

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder("log_to_creosote").circuitMeta(1)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(Creosote.getFluid(4000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("log_to_creosote_nitrogen").circuitMeta(2)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(Creosote.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);

        // Heavy Oil
        PYROLYSE_RECIPES.recipeBuilder("log_to_heavy_oil").circuitMeta(3)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .outputItems(dust, Ash, 4)
                .outputFluids(OilHeavy.getFluid(200))
                .duration(320).EUt(192)
                .save(provider);

        // Creosote
        PYROLYSE_RECIPES.recipeBuilder("coal_to_coke_creosote").circuitMeta(1)
                .inputItems(gem, Coal, 16)
                .outputItems(gem, Coke, 16)
                .outputFluids(Creosote.getFluid(8000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_to_coke_creosote_nitrogen").circuitMeta(2)
                .inputItems(gem, Coal, 16)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(gem, Coke, 16)
                .outputFluids(Creosote.getFluid(8000))
                .duration(320).EUt(96)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_block_to_coke_creosote").circuitMeta(1)
                .inputItems(block, Coal, 8)
                .outputItems(block, Coke, 8)
                .outputFluids(Creosote.getFluid(32000))
                .duration(2560).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_block_to_coke_creosote_nitrogen").circuitMeta(2)
                .inputItems(block, Coal, 8)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(block, Coke, 8)
                .outputFluids(Creosote.getFluid(32000))
                .duration(1280).EUt(96)
                .save(provider);

        // Biomass
        PYROLYSE_RECIPES.recipeBuilder("bio_chaff_to_fermented_biomass").EUt(10).duration(200)
                .inputItems(BIO_CHAFF)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(1500))
                .outputFluids(FermentedBiomass.getFluid(1500))
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("bio_chaff_to_biomass").EUt(10).duration(900)
                .inputItems(BIO_CHAFF, 4)
                .circuitMeta(1)
                .inputFluids(Water.getFluid(4000))
                .outputFluids(Biomass.getFluid(5000))
                .save(provider);

        // Sugar to Charcoal
        PYROLYSE_RECIPES.recipeBuilder("sugar_to_charcoal").circuitMeta(1)
                .inputItems(dust, Sugar, 23)
                .outputItems(dust, Charcoal, 12)
                .outputFluids(Water.getFluid(1500))
                .duration(320).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("sugar_to_charcoal_nitrogen").circuitMeta(2)
                .inputItems(dust, Sugar, 23)
                .inputFluids(Nitrogen.getFluid(500))
                .outputItems(dust, Charcoal, 12)
                .outputFluids(Water.getFluid(1500))
                .duration(160).EUt(96)
                .save(provider);

        // COAL GAS ============================================

        // From Log
        PYROLYSE_RECIPES.recipeBuilder("log_to_coal_gas").circuitMeta(20)
                .inputItems(ItemTags.LOGS_THAT_BURN, 16)
                .inputFluids(Steam.getFluid(1000))
                .outputItems(new ItemStack(Items.CHARCOAL, 20))
                .outputFluids(CoalGas.getFluid(2000))
                .duration(640).EUt(64)
                .save(provider);

        // From Coal
        PYROLYSE_RECIPES.recipeBuilder("coal_to_coal_gas").circuitMeta(22)
                .inputItems(gem, Coal, 16)
                .inputFluids(Steam.getFluid(1000))
                .outputItems(gem, Coke, 16)
                .outputFluids(CoalGas.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_block_to_coal_gas").circuitMeta(22)
                .inputItems(block, Coal, 8)
                .inputFluids(Steam.getFluid(4000))
                .outputItems(block, Coke, 8)
                .outputFluids(CoalGas.getFluid(16000))
                .duration(1280).EUt(96)
                .save(provider);

        // COAL TAR ============================================
        PYROLYSE_RECIPES.recipeBuilder("charcoal_to_coal_tar").circuitMeta(8)
                .inputItems(new ItemStack(Items.CHARCOAL, 32))
                .chancedOutput(dust, Ash, 5000, 0)
                .outputFluids(CoalTar.getFluid(1000))
                .duration(640).EUt(64)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coal_to_coal_tar").circuitMeta(8)
                .inputItems(new ItemStack(Items.COAL, 12))
                .chancedOutput(dust, DarkAsh, 5000, 0)
                .outputFluids(CoalTar.getFluid(3000))
                .duration(320).EUt(96)
                .save(provider);

        PYROLYSE_RECIPES.recipeBuilder("coke_to_coal_tar").circuitMeta(8)
                .inputItems(gem, Coke, 8)
                .chancedOutput(dust, Ash, 7500, 0)
                .outputFluids(CoalTar.getFluid(4000))
                .duration(320).EUt(96)
                .save(provider);
    }
}
