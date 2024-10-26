package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CHEMICAL_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.LARGE_CHEMICAL_RECIPES;

public class FuelRecipeChains {

    public static void init(Consumer<FinishedRecipe> provider) {
        // High Octane Gasoline
        LARGE_CHEMICAL_RECIPES.recipeBuilder("raw_gasoline").EUt(VA[HV]).duration(100)
                .inputFluids(Naphtha.getFluid(16000))
                .inputFluids(RefineryGas.getFluid(2000))
                .inputFluids(Methanol.getFluid(1000))
                .inputFluids(Acetone.getFluid(1000))
                .circuitMeta(24)
                .outputFluids(RawGasoline.getFluid(20000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("gasoline").EUt(VA[HV]).duration(10)
                .inputFluids(RawGasoline.getFluid(10000))
                .inputFluids(Toluene.getFluid(1000))
                .outputFluids(Gasoline.getFluid(11000))
                .save(provider);

        // Nitrous Oxide
        CHEMICAL_RECIPES.recipeBuilder("nitrous_oxide").EUt(VA[LV]).duration(100)
                .inputFluids(Nitrogen.getFluid(2000))
                .inputFluids(Oxygen.getFluid(1000))
                .circuitMeta(4)
                .outputFluids(NitrousOxide.getFluid(1000))
                .save(provider);

        // Ethyl Tert-Butyl Ether
        CHEMICAL_RECIPES.recipeBuilder("ethyl_tert_butyl_ether").EUt(VA[HV]).duration(400)
                .inputFluids(Butene.getFluid(1000))
                .inputFluids(Ethanol.getFluid(1000))
                .outputFluids(EthylTertButylEther.getFluid(1000))
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("high_octane_gasoline").EUt(VA[EV]).duration(50)
                .inputFluids(Gasoline.getFluid(20000))
                .inputFluids(Octane.getFluid(2000))
                .inputFluids(NitrousOxide.getFluid(2000))
                .inputFluids(Toluene.getFluid(1000))
                .inputFluids(EthylTertButylEther.getFluid(1000))
                .circuitMeta(24)
                .outputFluids(HighOctaneGasoline.getFluid(32000))
                .save(provider);

        // Nitrobenzene
        CHEMICAL_RECIPES.recipeBuilder("nitrobenzene").EUt(VA[HV]).duration(160)
                .inputFluids(Benzene.getFluid(5000))
                .inputFluids(NitrationMixture.getFluid(2000))
                .inputFluids(DistilledWater.getFluid(2000))
                .outputFluids(Nitrobenzene.getFluid(8000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .save(provider);
    }
}
