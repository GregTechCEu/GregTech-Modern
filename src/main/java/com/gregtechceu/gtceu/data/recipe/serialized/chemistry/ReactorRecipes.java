package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.GELLED_TOLUENE;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class ReactorRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        CHEMICAL_RECIPES.recipeBuilder("raw_rubber_from_air")
                .circuitMeta(1)
                .inputFluids(Isoprene.getFluid(L))
                .inputFluids(Air.getFluid(2000))
                .outputItems(dust, RawRubber)
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("raw_rubber_from_oxygen")
                .circuitMeta(1)
                .inputFluids(Isoprene.getFluid(L))
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(dust, RawRubber, 3)
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("isoprene_from_methane")
                .circuitMeta(3)
                .inputFluids(Propene.getFluid(2000))
                .outputFluids(Methane.getFluid(1000))
                .outputFluids(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("methane_from_elements")
                .circuitMeta(1)
                .inputItems(dust, Carbon)
                .inputFluids(Hydrogen.getFluid(4000))
                .outputFluids(Methane.getFluid(1000))
                .duration(3500).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("isoprene_from_ethylene")
                .inputFluids(Ethylene.getFluid(1000))
                .inputFluids(Propene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sodium_sulfide")
                .inputItems(dust, Sodium, 2)
                .inputItems(dust, Sulfur)
                .outputItems(dust, SodiumSulfide, 3)
                .duration(60).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyphenylene_sulfide_from_air")
                .inputItems(dust, SodiumSulfide, 3)
                .inputFluids(Dichlorobenzene.getFluid(1000))
                .inputFluids(Air.getFluid(16000))
                .outputItems(dust, Salt, 4)
                .outputFluids(PolyphenyleneSulfide.getFluid(1000))
                .duration(240).EUt(360).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyphenylene_sulfide_from_oxygen")
                .inputItems(dust, SodiumSulfide, 3)
                .inputFluids(Dichlorobenzene.getFluid(1000))
                .inputFluids(Oxygen.getFluid(8000))
                .outputItems(dust, Salt, 4)
                .outputFluids(PolyphenyleneSulfide.getFluid(1500))
                .duration(240).EUt(360).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("silicone_rubber")
                .inputItems(dust, Polydimethylsiloxane, 9)
                .inputItems(dust, Sulfur)
                .outputFluids(SiliconeRubber.getFluid(1296))
                .duration(600).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polydimethylsiloxane_from_dimethyldichlorosilane")
                .inputFluids(Dimethyldichlorosilane.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputItems(dust, Polydimethylsiloxane, 3)
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .duration(240).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polydimethylsiloxane_from_silicon")
                .inputItems(dust, Silicon)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .inputFluids(Methanol.getFluid(2000))
                .outputItems(dust, Polydimethylsiloxane, 3)
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .duration(480).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polydimethylsiloxane_from_elements")
                .circuitMeta(2)
                .inputItems(dust, Silicon)
                .inputFluids(Water.getFluid(1000))
                .inputFluids(Chlorine.getFluid(4000))
                .inputFluids(Methane.getFluid(2000))
                .outputItems(dust, Polydimethylsiloxane, 3)
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .duration(480).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hydrochloric_acid")
                .inputFluids(Chlorine.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        // NaCl + H2SO4 -> NaHSO4 + HCl
        CHEMICAL_RECIPES.recipeBuilder("sodium_bisulfate_from_salt")
                .inputItems(dust, Salt, 2)
                .circuitMeta(1)
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, SodiumBisulfate, 7)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("iron_3_chloride")
                .inputItems(dust, Iron)
                .inputFluids(HydrochloricAcid.getFluid(3000))
                .circuitMeta(1)
                .outputFluids(Iron3Chloride.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(3000))
                .duration(400).EUt(VA[LV])
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("iron_2_chloride")
                .inputFluids(Iron3Chloride.getFluid(2000))
                .inputFluids(Chlorobenzene.getFluid(1000))
                .outputFluids(Iron2Chloride.getFluid(2000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(Dichlorobenzene.getFluid(1000))
                .duration(400).EUt(VA[MV])
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("chloromethane_from_methane")
                .circuitMeta(3)
                .inputFluids(Chlorine.getFluid(2000))
                .inputFluids(Methane.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(Chloromethane.getFluid(1000))
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dichlorobenzene")
                .inputFluids(Chlorine.getFluid(4000))
                .inputFluids(Benzene.getFluid(1000))
                .circuitMeta(2)
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(Dichlorobenzene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("glyceryl_trinitrate")
                .inputFluids(NitrationMixture.getFluid(3000))
                .inputFluids(Glycerol.getFluid(1000))
                .outputFluids(GlycerylTrinitrate.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(3000))
                .duration(180).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ethenone")
                .inputFluids(SulfuricAcid.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .outputFluids(Ethenone.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .duration(160).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dissolved_calcium_acetate_from_calcite")
                .inputItems(dust, Calcite, 5)
                .inputFluids(AceticAcid.getFluid(2000))
                .outputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(200).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dissolved_calcium_acetate_from_quicklime")
                .inputItems(dust, Quicklime, 2)
                .inputFluids(AceticAcid.getFluid(2000))
                .circuitMeta(1)
                .outputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dissolved_calcium_acetate_from_calcium")
                .inputItems(dust, Calcium)
                .inputFluids(AceticAcid.getFluid(2000))
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("methyl_acetate")
                .inputFluids(Methanol.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(MethylAcetate.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(240).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hydrogen_sulfide")
                .inputItems(dust, Sulfur)
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pva_from_air")
                .circuitMeta(1)
                .inputFluids(Air.getFluid(1000))
                .inputFluids(VinylAcetate.getFluid(L))
                .outputFluids(PolyvinylAcetate.getFluid(L))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("pva_from_oxygen")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(VinylAcetate.getFluid(L))
                .outputFluids(PolyvinylAcetate.getFluid(216))
                .duration(160).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("pva_from_tetrachloride_air")
                .circuitMeta(2)
                .inputFluids(Air.getFluid(7500))
                .inputFluids(VinylAcetate.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(PolyvinylAcetate.getFluid(3240))
                .duration(800).EUt(VA[LV]).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("pva_from_tetrachloride_oxygen")
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(7500))
                .inputFluids(VinylAcetate.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(PolyvinylAcetate.getFluid(4320))
                .duration(800).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("methanol_from_dioxide")
                .inputFluids(Hydrogen.getFluid(6000))
                .inputFluids(CarbonDioxide.getFluid(1000))
                .circuitMeta(2)
                .outputFluids(Water.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(120).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("methanol_from_monoxide")
                .circuitMeta(1)
                .inputFluids(Hydrogen.getFluid(4000))
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(120).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("methanol_from_carbon")
                .circuitMeta(3)
                .inputItems(dust, Carbon)
                .inputFluids(Hydrogen.getFluid(4000))
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(320).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hypochlorous_acid_mercury")
                .inputFluids(Mercury.getFluid(1000))
                .inputFluids(Water.getFluid(10000))
                .inputFluids(Chlorine.getFluid(10000))
                .outputFluids(HypochlorousAcid.getFluid(10000))
                .duration(600).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hypochlorous_acid")
                .circuitMeta(1)
                .inputFluids(Water.getFluid(1000))
                .inputFluids(Chlorine.getFluid(2000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .outputFluids(HypochlorousAcid.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dimethylhydrazine_from_dimethylamine")
                .inputFluids(Dimethylamine.getFluid(1000))
                .inputFluids(Monochloramine.getFluid(1000))
                .outputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(960).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dimethylhydrazine_from_methanol")
                .inputFluids(Methanol.getFluid(2000))
                .inputFluids(Ammonia.getFluid(2000))
                .inputFluids(HypochlorousAcid.getFluid(1000))
                .outputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .duration(1040).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hydrofluoric_acid_from_elements")
                .inputFluids(Hydrogen.getFluid(1000))
                .inputFluids(Fluorine.getFluid(1000))
                .outputFluids(HydrofluoricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("cumene_from_benzene")
                .circuitMeta(1)
                .inputFluids(PhosphoricAcid.getFluid(1000))
                .inputFluids(Benzene.getFluid(8000))
                .inputFluids(Propene.getFluid(8000))
                .outputFluids(Cumene.getFluid(8000))
                .duration(1920).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dimethylchlorosilane_from_chloromethane")
                .inputItems(dust, Silicon)
                .inputFluids(Chloromethane.getFluid(2000))
                .outputFluids(Dimethyldichlorosilane.getFluid(1000))
                .duration(240).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acetic_acid_from_ethylene")
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(2000))
                .inputFluids(Ethylene.getFluid(1000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acetic_acid_from_methanol")
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .inputFluids(Methanol.getFluid(1000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(300).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acetic_acid_from_monoxide")
                .circuitMeta(2)
                .inputFluids(Hydrogen.getFluid(4000))
                .inputFluids(CarbonMonoxide.getFluid(2000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acetic_acid_from_elements")
                .circuitMeta(4)
                .inputItems(dust, Carbon, 2)
                .inputFluids(Oxygen.getFluid(2000))
                .inputFluids(Hydrogen.getFluid(4000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(480).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("indium_concentrate_separation")
                .circuitMeta(1)
                .inputItems(dust, Aluminium, 4)
                .inputFluids(IndiumConcentrate.getFluid(1000))
                .outputItems(dustSmall, Indium)
                .outputItems(dust, AluminiumSulfite, 4)
                .outputFluids(LeadZincSolution.getFluid(1000))
                .duration(50).EUt(600).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("indium_concentrate_separation_4x")
                .circuitMeta(4)
                .inputItems(dust, Aluminium, 16)
                .inputFluids(IndiumConcentrate.getFluid(4000))
                .outputItems(dust, Indium)
                .outputItems(dust, AluminiumSulfite, 16)
                .outputFluids(LeadZincSolution.getFluid(4000))
                .duration(200).EUt(600).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("vinyl_acetate")
                .circuitMeta(3)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .inputFluids(Ethylene.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(VinylAcetate.getFluid(1000))
                .duration(180).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_monoxide_from_carbon")
                .circuitMeta(1)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_monoxide_from_charcoal_gem")
                .circuitMeta(1)
                .inputItems(gem, Charcoal)
                .inputFluids(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_monoxide_from_coal_gem")
                .circuitMeta(1)
                .inputItems(gem, Coal)
                .inputFluids(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_monoxide_from_charcoal_dust")
                .circuitMeta(1)
                .inputItems(dust, Charcoal)
                .inputFluids(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_monoxide_from_coal_dust")
                .duration(80).EUt(VA[ULV])
                .inputItems(dust, Coal)
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_monoxide_from_dioxide")
                .inputItems(dust, Carbon)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .duration(800).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("monochloramine")
                .inputFluids(HypochlorousAcid.getFluid(1000))
                .inputFluids(Ammonia.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(Monochloramine.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dimethylamine")
                .circuitMeta(2)
                .inputFluids(Ammonia.getFluid(1000))
                .inputFluids(Methanol.getFluid(2000))
                .outputFluids(Water.getFluid(2000))
                .outputFluids(Dimethylamine.getFluid(1000))
                .duration(240).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("chloromethane_from_methanol")
                .circuitMeta(1)
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .inputFluids(Methanol.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(Chloromethane.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_dioxide_from_carbon")
                .circuitMeta(2)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(2000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_dioxide_from_charcoal_gem")
                .circuitMeta(2)
                .inputItems(gem, Charcoal)
                .inputFluids(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_dioxide_from_coal_gem")
                .circuitMeta(2)
                .inputItems(gem, Coal)
                .inputFluids(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_dioxide_from_charcoal_dust")
                .circuitMeta(2)
                .inputItems(dust, Charcoal)
                .inputFluids(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_dioxide_from_coal_dust")
                .circuitMeta(2)
                .inputItems(dust, Coal)
                .inputFluids(Oxygen.getFluid(2000))
                .chancedOutput(dust, Ash, "1/9", 0)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("carbon_dioxide_from_methane")
                .circuitMeta(1)
                .inputFluids(Water.getFluid(2000))
                .inputFluids(Methane.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(8000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(150).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("tetranitromethane_from_methyl_acetate")
                .inputFluids(MethylAcetate.getFluid(2000))
                .inputFluids(NitricAcid.getFluid(4000))
                .outputItems(dust, Carbon, 5)
                .outputFluids(Tetranitromethane.getFluid(1000))
                .outputFluids(Water.getFluid(8000))
                .duration(480).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("tetranitromethane_from_ethenone")
                .inputFluids(NitricAcid.getFluid(8000))
                .inputFluids(Ethenone.getFluid(1000))
                .outputFluids(Tetranitromethane.getFluid(2000))
                .outputFluids(Water.getFluid(5000))
                .duration(480).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dinitrogen_tetroxide_from_ammonia")
                .circuitMeta(3)
                .inputFluids(Oxygen.getFluid(7000))
                .inputFluids(Ammonia.getFluid(2000))
                .outputFluids(DinitrogenTetroxide.getFluid(1000))
                .outputFluids(Water.getFluid(3000))
                .duration(480).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dinitrogen_tetroxide_from_dioxide")
                .circuitMeta(2)
                .inputFluids(NitrogenDioxide.getFluid(2000))
                .outputFluids(DinitrogenTetroxide.getFluid(1000))
                .duration(640).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sodium_bisulfate_from_hydroxide")
                .inputItems(dust, SodiumHydroxide, 3)
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, SodiumBisulfate, 7)
                .outputFluids(Water.getFluid(1000))
                .duration(60).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("gelled_toluene")
                .inputItems(Items.SUGAR, 9)
                .inputItems(dust, Polyethylene)
                .inputFluids(Toluene.getFluid(1000))
                .outputItems(GELLED_TOLUENE, 20)
                .duration(140).EUt(192).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("calcite_from_calcium")
                .inputItems(dust, Calcium)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, Calcite, 5)
                .duration(500).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("calcite_from_quicklime")
                .inputItems(dust, Quicklime, 2)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputItems(dust, Calcite, 5)
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("magnesite_from_magnesia")
                .inputItems(dust, Magnesia, 2)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputItems(dust, Magnesite, 5)
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("magnesia_from_magnesite")
                .inputItems(dust, Magnesite, 5)
                .outputItems(dust, Magnesia, 2)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("rubber")
                .inputItems(dust, RawRubber, 9)
                .inputItems(dust, Sulfur)
                .outputFluids(Rubber.getFluid(1296))
                .duration(600).EUt(16).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("glistening_melon_slice")
                .inputItems(Items.MELON_SLICE)
                .inputItems(nugget, Gold, 8)
                .outputItems(Items.GLISTERING_MELON_SLICE)
                .duration(50).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("golden_carrot")
                .inputItems(Items.CARROT)
                .inputItems(nugget, Gold, 8)
                .outputItems(Items.GOLDEN_CARROT)
                .duration(50).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("golden_apple")
                .inputItems(Items.APPLE)
                .inputItems(ingot, Gold, 8)
                .outputItems(Items.GOLDEN_APPLE)
                .duration(50).EUt(VA[LV])
                .addMaterialInfo(true).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("notch_apple")
                .inputItems(Items.APPLE)
                .inputItems(block, Gold, 8)
                .outputItems(Items.ENCHANTED_GOLDEN_APPLE)
                .duration(50).EUt(VA[LV])
                .addMaterialInfo(true).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("magma_cream")
                .inputItems(Items.BLAZE_POWDER)
                .inputItems(Items.SLIME_BALL)
                .outputItems(Items.MAGMA_CREAM)
                .duration(50).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("tnt_from_toluene")
                .inputItems(GELLED_TOLUENE, 4)
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(Blocks.TNT.asItem())
                .duration(200).EUt(24).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("itnt_from_toluene")
                .inputItems(GELLED_TOLUENE, 4)
                .inputFluids(NitrationMixture.getFluid(200))
                .outputItems(new ItemStack(GTBlocks.INDUSTRIAL_TNT))
                .outputFluids(DilutedSulfuricAcid.getFluid(150))
                .duration(80).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("phenol_from_dichlorobenzene")
                .inputItems(dust, SodiumHydroxide, 6)
                .inputFluids(Dichlorobenzene.getFluid(1000))
                .outputItems(dust, Salt, 4)
                .outputFluids(Phenol.getFluid(1000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("acetic_acid_from_methyl_acetate")
                .inputFluids(MethylAcetate.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .notConsumable(dust, SodiumHydroxide)
                .outputFluids(AceticAcid.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(264).EUt(60).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("radon_from_uranium_238")
                .inputItems(ingot, Plutonium239, 8)
                .inputItems(dust, Uranium238)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, Plutonium239, 8)
                .outputFluids(Radon.getFluid(1000))
                .duration(4000).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dynamite")
                .inputItems(Items.PAPER)
                .inputItems(Items.STRING)
                .inputFluids(GlycerylTrinitrate.getFluid(500))
                .outputItems(GTItems.DYNAMITE.get())
                .duration(160).EUt(4).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("niobium_nitride")
                .inputItems(dust, Niobium)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(dust, NiobiumNitride, 2)
                .duration(200).EUt(VA[HV]).save(provider);

        // Dyes
        for (int i = 0; i < GTMaterials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_RECIPES.recipeBuilder("chemical_dye_" + MarkerMaterials.Color.VALUES[i].getName())
                    .inputItems(dye, MarkerMaterials.Color.VALUES[i])
                    .inputItems(dust, Salt, 2)
                    .inputFluids(SulfuricAcid.getFluid(250))
                    .outputFluids(GTMaterials.CHEMICAL_DYES[i].getFluid(288))
                    .duration(600).EUt(24).save(provider);
        }

        CHEMICAL_RECIPES.recipeBuilder("blaze_powder")
                .inputItems(dust, Carbon)
                .inputItems(dust, Sulfur)
                .outputItems(dust, Blaze)
                .duration(200).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("saltpeter")
                .inputItems(dust, Potassium)
                .inputFluids(Oxygen.getFluid(3000))
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(dust, Saltpeter, 5)
                .duration(180).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("ghast_tear_separation")
                .inputItems(Items.GHAST_TEAR)
                .inputFluids(Water.getFluid(1000))
                .outputItems(dustTiny, Potassium)
                .outputItems(dustTiny, Lithium)
                .outputFluids(SaltWater.getFluid(1000))
                .duration(400).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sodium_potassium")
                .inputItems(dust, Sodium)
                .inputItems(dust, Potassium)
                .outputFluids(SodiumPotassium.getFluid(1000))
                .duration(300).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("salt")
                .inputItems(dust, Sodium)
                .inputFluids(Chlorine.getFluid(1000))
                .outputItems(dust, Salt, 2)
                .duration(200).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("butraldehyde")
                .inputFluids(Propene.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(2000))
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .outputFluids(Butyraldehyde.getFluid(1000))
                .duration(200).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polyvinyl_butyral")
                .inputFluids(Butyraldehyde.getFluid(250))
                .inputFluids(PolyvinylAcetate.getFluid(L))
                .outputFluids(PolyvinylButyral.getFluid(L))
                .duration(400).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("biphenyl_from_toluene")
                .inputFluids(Benzene.getFluid(1000))
                .inputFluids(Toluene.getFluid(1000))
                .outputItems(dust, Biphenyl, 2)
                .outputFluids(Methane.getFluid(1000))
                .duration(200).EUt(VH[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("biphenyl_from_oxygen")
                .inputFluids(Benzene.getFluid(2000))
                .inputFluids(Oxygen.getFluid(1000))
                .circuitMeta(1)
                .outputItems(dust, Biphenyl, 2)
                .outputFluids(Water.getFluid(1000))
                .duration(400).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("benzene_from_biphenyl")
                .inputItems(dust, Biphenyl, 2)
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Benzene.getFluid(2000))
                .duration(400).EUt(VA[EV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("polychlorinated_biphenyl")
                .inputItems(dust, Biphenyl, 2)
                .inputFluids(Chlorine.getFluid(4000))
                .outputFluids(PolychlorinatedBiphenyl.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .duration(200).EUt(VH[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("hydrogen_peroxide")
                .inputFluids(Hydrogen.getFluid(2000))
                .inputFluids(Oxygen.getFluid(2000))
                .circuitMeta(1)
                .outputFluids(HydrogenPeroxide.getFluid(1000))
                .duration(3000).EUt(VHA[HV]).save(provider);
    }
}
