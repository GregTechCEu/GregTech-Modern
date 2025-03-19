package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES;
import static com.gregtechceu.gtceu.common.data.machines.GTMultiMachines.FUSION_REACTOR;

public class AssemblyLineLoader {

    public static void init(Consumer<FinishedRecipe> provider) {
        ASSEMBLY_LINE_RECIPES.recipeBuilder("fusion_reactor_mk1")
                .inputItems(SUPERCONDUCTING_COIL.asStack())
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .inputItems(plateDouble, Plutonium241)
                .inputItems(plateDouble, Osmiridium)
                .inputItems(FIELD_GENERATOR_IV, 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(wireGtSingle, IndiumTinBariumTitaniumCuprate, 32)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(NiobiumTitanium.getFluid(L * 8))
                .outputItems(FUSION_REACTOR[LuV].asStack())
                .scannerResearch(b -> b
                        .researchStack(ChemicalHelper.get(wireGtSingle, IndiumTinBariumTitaniumCuprate))
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(800).EUt(VA[LuV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("fusion_reactor_mk2")
                .inputItems(FUSION_COIL.asStack())
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(plateDouble, Naquadria)
                .inputItems(plateDouble, Europium)
                .inputItems(FIELD_GENERATOR_LuV, 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItems(wireGtSingle, UraniumRhodiumDinaquadide, 32)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(VanadiumGallium.getFluid(L * 8))
                .outputItems(FUSION_REACTOR[ZPM].asStack())
                .stationResearch(b -> b
                        .researchStack(FUSION_REACTOR[LuV].asStack())
                        .CWUt(16)
                        .EUt(VA[ZPM]))
                .duration(1000).EUt(61440).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("fusion_reactor_mk3")
                .inputItems(FUSION_COIL.asStack())
                .inputItems(CustomTags.UHV_CIRCUITS, 4)
                .inputItems(QUANTUM_STAR)
                .inputItems(plateDouble, Americium)
                .inputItems(FIELD_GENERATOR_ZPM, 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
                .inputItems(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(YttriumBariumCuprate.getFluid(L * 8))
                .outputItems(FUSION_REACTOR[UV].asStack())
                .stationResearch(b -> b
                        .researchStack(FUSION_REACTOR[ZPM].asStack())
                        .CWUt(96)
                        .EUt(VA[UV]))
                .duration(1000).EUt(VA[ZPM]).save(provider);
    }
}
