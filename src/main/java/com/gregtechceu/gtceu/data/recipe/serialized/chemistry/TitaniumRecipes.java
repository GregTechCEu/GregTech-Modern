package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class TitaniumRecipes {

    private TitaniumRecipes() {}

    public static void init(Consumer<FinishedRecipe> provider) {
        titaniumProcess(provider);
        solvayProcess(provider);
        bauxiteProcess(provider);
        ilmeniteProcess(provider);
    }

    private static void titaniumProcess(Consumer<FinishedRecipe> provider) {
        // Rutile extraction from Ilmenite
        // FeTiO3 + C -> Fe + TiO2 + CO
        BLAST_RECIPES.recipeBuilder("rutile_from_ilmenite")
                .inputItems(dust, Ilmenite, 5)
                .inputItems(dust, Carbon)
                .outputItems(ingot, WroughtIron)
                .outputItems(dust, Rutile, 3)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .blastFurnaceTemp(1700)
                .duration(1600).EUt(VA[HV]).save(provider);

        // Chloride Process
        // TiO2 + 2C + 4Cl -> TiCl4 + 2CO
        CHEMICAL_RECIPES.recipeBuilder("titanium_tetrachloride")
                .inputItems(dust, Carbon, 2)
                .inputItems(dust, Rutile)
                .inputFluids(Chlorine.getFluid(4000))
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .outputFluids(TitaniumTetrachloride.getFluid(1000))
                .duration(400).EUt(VA[HV]).save(provider);

        // Kroll Process
        // TiCl4 + 2Mg -> Ti + 2MgCl2
        BLAST_RECIPES.recipeBuilder("titanium_from_tetrachloride")
                .inputItems(dust, Magnesium, 2)
                .inputFluids(TitaniumTetrachloride.getFluid(1000))
                .outputItems(ingotHot, Titanium)
                .outputItems(dust, MagnesiumChloride, 6)
                .blastFurnaceTemp(Titanium.getBlastTemperature() + 200)
                .duration(800).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("salt_from_magnesium_chloride")
                .inputItems(dust, MagnesiumChloride, 3)
                .inputItems(dust, Sodium, 2)
                .outputItems(dust, Magnesium, 1)
                .outputItems(dust, Salt, 4)
                .duration(200).EUt(VA[HV]).save(provider);
    }

    private static void solvayProcess(Consumer<FinishedRecipe> provider) {
        // CaCO3 -> CaO + CO2
        CHEMICAL_RECIPES.recipeBuilder("quicklime_from_calcite")
                .circuitMeta(1)
                .inputItems(dust, Calcite, 5)
                .outputItems(dust, Quicklime, 2)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(200).EUt(VA[LV]).save(provider);

        // NaCl(H2O) + CO2 + NH3 -> NH4Cl + NaHCO3
        CHEMICAL_RECIPES.recipeBuilder("sodium_bicarbonate_from_salt")
                .inputItems(dust, Salt, 4)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .inputFluids(Ammonia.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputItems(dust, AmmoniumChloride, 2)
                .outputItems(dust, SodiumBicarbonate, 6)
                .duration(400).EUt(VA[MV]).save(provider);

        // 2NaHCO3 -> Na2CO3 + CO2 + H2O
        ELECTROLYZER_RECIPES.recipeBuilder("soda_ash_from_bicarbonate")
                .inputItems(dust, SodiumBicarbonate, 12)
                .outputItems(dust, SodaAsh, 6)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(200).EUt(VA[MV]).save(provider);

        // 2NH4Cl + CaO -> CaCl2 + 2NH3 + H2O
        CHEMICAL_RECIPES.recipeBuilder("calcium_chloride_from_quicklime")
                .inputItems(dust, AmmoniumChloride, 4)
                .inputItems(dust, Quicklime, 2)
                .outputItems(dust, CalciumChloride, 3)
                .outputFluids(Ammonia.getFluid(2000))
                .outputFluids(Water.getFluid(1000))
                .duration(200).EUt(VA[MV]).save(provider);
    }

    private static void bauxiteProcess(Consumer<FinishedRecipe> provider) {
        // Bauxite (crushed) + Soda Ash + Calcium Chloride -> Bauxite Slurry
        MIXER_RECIPES.recipeBuilder("bauxite_slurry_from_crushed_bauxite")
                .inputItems(crushed, Bauxite, 32)
                .inputItems(dust, SodaAsh, 12)
                .inputItems(dust, CalciumChloride, 6)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(BauxiteSlurry.getFluid(4000))
                .duration(500).EUt(VA[HV]).save(provider);

        // Bauxite (washed) + Soda Ash + Calcium Chloride -> Bauxite Slurry
        MIXER_RECIPES.recipeBuilder("bauxite_slurry_from_washed_bauxite")
                .inputItems(crushedPurified, Bauxite, 32)
                .inputItems(dust, SodaAsh, 12)
                .inputItems(dust, CalciumChloride, 6)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(BauxiteSlurry.getFluid(4000))
                .duration(500).EUt(VA[HV]).save(provider);

        // Bauxite Slurry -> Cracked Bauxite Slurry
        CRACKING_RECIPES.recipeBuilder("cracked_bauxite_slurry")
                .circuitMeta(1)
                .inputFluids(BauxiteSlurry.getFluid(16000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(CrackedBauxiteSlurry.getFluid(16000))
                .duration(500).EUt(VA[HV]).save(provider);

        // Bauxite Slurry + Sulfuric -> Aluminium, Slag, Sludge, and SO3 (for looping back to Sulfuric Acid)
        LARGE_CHEMICAL_RECIPES.recipeBuilder("bauxite_sludge_from_slurry")
                .inputFluids(CrackedBauxiteSlurry.getFluid(4000))
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, Aluminium, 24)
                .outputItems(dust, BauxiteSlag, 8)
                .outputFluids(BauxiteSludge.getFluid(2500))
                .outputFluids(SulfurTrioxide.getFluid(1000))
                .duration(500).EUt(VA[HV]).save(provider);

        // Bauxite Slag -> Salt (looped) + Nd + Cr (byproducts)
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("bauxite_slag_separation")
                .inputItems(dust, BauxiteSlag)
                .outputItems(dust, Salt)
                .chancedOutput(dust, Neodymium, 2000, 0)
                .chancedOutput(dust, Chromium, 1000, 0)
                .duration(50).EUt(VA[MV]).save(provider);

        // Bauxite Sludge -> Calcite (looped) + Decalcified Bauxite Sludge
        DISTILLERY_RECIPES.recipeBuilder("bauxite_sludge_decalcification")
                .circuitMeta(1)
                .inputFluids(BauxiteSludge.getFluid(500))
                .outputItems(dust, Calcite, 2)
                .outputFluids(DecalcifiedBauxiteSludge.getFluid(500))
                .duration(100).EUt(VA[MV]).save(provider);

        // Decalcified Bauxite Sludge -> Rutile, Gallium, SiO2, Iron, Water
        CENTRIFUGE_RECIPES.recipeBuilder("bauxite_sludge_centrifuge")
                .inputFluids(DecalcifiedBauxiteSludge.getFluid(250))
                .outputItems(dust, Rutile, 2)
                .chancedOutput(dust, Gallium, 5000, 0)
                .chancedOutput(dust, Gallium, 3000, 0)
                .chancedOutput(dust, Gallium, 1000, 0)
                .chancedOutput(dust, SiliconDioxide, 9000, 0)
                .chancedOutput(dust, Iron, 8000, 0)
                .outputFluids(Water.getFluid(250))
                .duration(100).EUt(VA[MV]).save(provider);
    }

    private static void ilmeniteProcess(Consumer<FinishedRecipe> provider) {
        // Byproduct separation for Ilmenite
        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("ilmenite_separation")
                .inputItems(dust, IlmeniteSlag)
                .chancedOutput(dust, Iron, 8000, 0)
                .chancedOutput(dust, Tantalum, 2000, 0)
                .chancedOutput(dust, Niobium, 500, 0)
                .duration(50).EUt(VA[MV]).save(provider);
    }
}
