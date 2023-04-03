package com.gregtechceu.gtceu.data.recipe.misc;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public class AssemblyLineLoader {

    public static void init(Consumer<FinishedRecipe> provider) {

        // TODO Fusion reactor
/*
        ASSEMBLY_LINE_RECIPES.recipeBuilder("fusion_reactor_mk1")
                .inputItems(FUSION_CASING.getItemVariant(SUPERCONDUCTOR_COIL))
                .inputItems(circuit, Tier.ZPM, 4)
                .inputItems(plateDouble, Plutonium241)
                .inputItems(plateDouble, Osmiridium)
                .inputItems(FIELD_GENERATOR_IV, 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(wireGtSingle, IndiumTinBariumTitaniumCuprate, 32)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(NiobiumTitanium.getFluid(L * 8))
                .outputItems(FUSION_REACTOR[0].getStackForm())
                .duration(800).EUt(VA[LuV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("fusion_reactor_mk2")
                .inputItems(FUSION_CASING.getItemVariant(FUSION_COIL))
                .inputItems(circuit, Tier.UV, 4)
                .inputItems(plateDouble, Naquadria)
                .inputItems(plateDouble, Europium)
                .inputItems(FIELD_GENERATOR_LuV, 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItems(wireGtSingle, UraniumRhodiumDinaquadide, 32)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(VanadiumGallium.getFluid(L * 8))
                .outputItems(FUSION_REACTOR[1].getStackForm())
                .duration(1000).EUt(61440).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("fusion_reactor_mk3")
                .inputItems(FUSION_CASING.getItemVariant(FUSION_COIL))
                .inputItems(circuit, Tier.UHV, 4)
                .inputItems(QUANTUM_STAR)
                .inputItems(plateDouble, Americium)
                .inputItems(FIELD_GENERATOR_ZPM, 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(YttriumBariumCuprate.getFluid(L * 8))
                .outputItems(FUSION_REACTOR[2].getStackForm())
                .duration(1000).EUt(VA[ZPM]).save(provider);

 */
    }
}
