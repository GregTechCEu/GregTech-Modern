package com.gregtechceu.gtceu.data.recipe.chemistry;


import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.common.libs.GTMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.libs.GTItems.*;
import static com.gregtechceu.gtceu.common.libs.GTMaterials.*;
import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.libs.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.api.GTValues.*;

public class ReactorRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        CHEMICAL_RECIPES.recipeBuilder("dust_" + RawRubber.getName() + ".0")
                .circuitMeta(1)
                .inputFluids(Isoprene.getFluid(144))
                .inputFluids(Air.getFluid(2000))
                .outputItems(dust, RawRubber)
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + RawRubber.getName() + ".1")
                .circuitMeta(1)
                .inputFluids(Isoprene.getFluid(144))
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(dust, RawRubber, 3)
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Propene.getName())
                .circuitMeta(3)
                .inputFluids(Propene.getFluid(2000))
                .outputFluids(Methane.getFluid(1000))
                .outputFluids(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Carbon.getName())
                .circuitMeta(1)
                .inputItems(dust, Carbon)
                .inputFluids(Hydrogen.getFluid(4000))
                .outputFluids(Methane.getFluid(1000))
                .duration(3500).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Ethylene.getName() + "_" + Propene.getName())
                .inputFluids(Ethylene.getFluid(1000))
                .inputFluids(Propene.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(2000))
                .outputFluids(Isoprene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + SodiumSulfide.getName())
                .inputItems(dust, Sodium, 2)
                .inputItems(dust, Sulfur)
                .outputItems(dust, SodiumSulfide, 3)
                .duration(60).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Salt.getName() + ".0")
                .inputItems(dust, SodiumSulfide, 3)
                .inputFluids(Dichlorobenzene.getFluid(1000))
                .inputFluids(Air.getFluid(16000))
                .outputItems(dust, Salt, 4)
                .outputFluids(PolyphenyleneSulfide.getFluid(1000))
                .duration(240).EUt(360).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Salt.getName() + ".1")
                .inputItems(dust, SodiumSulfide, 3)
                .inputFluids(Dichlorobenzene.getFluid(1000))
                .inputFluids(Oxygen.getFluid(8000))
                .outputItems(dust, Salt, 4)
                .outputFluids(PolyphenyleneSulfide.getFluid(1500))
                .duration(240).EUt(360).save(provider);



        CHEMICAL_RECIPES.recipeBuilder(SiliconeRubber.getName())
                .inputItems(dust, Polydimethylsiloxane, 9)
                .inputItems(dust, Sulfur)
                .outputFluids(SiliconeRubber.getFluid(1296))
                .duration(600).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(CarbonMonoxide.getName() + "_" + TitaniumTetrachloride.getName())
                .inputItems(dust, Carbon, 2)
                .inputItems(dust, Rutile)
                .inputFluids(Chlorine.getFluid(4000))
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .outputFluids(TitaniumTetrachloride.getFluid(1000))
                .duration(400).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Polydimethylsiloxane.getName() + "_" + DilutedHydrochloricAcid.getName() + ".0")
                .inputFluids(Dimethyldichlorosilane.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .outputItems(dust, Polydimethylsiloxane, 3)
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .duration(240).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Polydimethylsiloxane.getName() + "_" + DilutedHydrochloricAcid.getName() + ".1")
                .inputItems(dust, Silicon)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .inputFluids(Methanol.getFluid(2000))
                .outputItems(dust, Polydimethylsiloxane, 3)
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .duration(480).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Polydimethylsiloxane.getName() + "_" + DilutedHydrochloricAcid.getName() + ".2")
                .circuitMeta(2)
                .inputItems(dust, Silicon)
                .inputFluids(Water.getFluid(1000))
                .inputFluids(Chlorine.getFluid(4000))
                .inputFluids(Methane.getFluid(2000))
                .outputItems(dust, Polydimethylsiloxane, 3)
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .duration(480).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(HydrochloricAcid.getName())
                .inputFluids(Chlorine.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        // NaCl + H2SO4 -> NaHSO4 + HCl
        CHEMICAL_RECIPES.recipeBuilder("dust_" + SodiumBisulfate.getName())
                .inputItems(dust, Salt, 2)
                .circuitMeta(1)
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, SodiumBisulfate, 7)
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(60).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Iron.getName() + "hcl")
                .inputItems(dust, Iron)
                .inputFluids(HydrochloricAcid.getFluid(3000))
                .circuitMeta(1)
                .outputFluids(Iron3Chloride.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(3000))
                .duration(400).EUt(VA[LV])
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Chlorine.getName() + "_" + Methane.getName())
                .circuitMeta(3)
                .inputFluids(Chlorine.getFluid(2000))
                .inputFluids(Methane.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .outputFluids(Chloromethane.getFluid(1000))
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Chlorine.getName() + "_" + Benzene.getName())
                .inputFluids(Chlorine.getFluid(4000))
                .inputFluids(Benzene.getFluid(1000))
                .circuitMeta(2)
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(Dichlorobenzene.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(NitrationMixture.getName() + "_" + Glycerol.getName())
                .inputFluids(NitrationMixture.getFluid(3000))
                .inputFluids(Glycerol.getFluid(1000))
                .outputFluids(GlycerylTrinitrate.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(3000))
                .duration(180).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(SulfuricAcid.getName() + "_" + AceticAcid.getName())
                .inputFluids(SulfuricAcid.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .outputFluids(Ethenone.getFluid(1000))
                .outputFluids(DilutedSulfuricAcid.getFluid(1000))
                .duration(160).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Calcite.getName() + "_" + AceticAcid.getName())
                .inputItems(dust, Calcite, 5)
                .inputFluids(AceticAcid.getFluid(2000))
                .outputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(200).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Quicklime.getName() + "_" + AceticAcid.getName())
                .inputItems(dust, Quicklime, 2)
                .inputFluids(AceticAcid.getFluid(2000))
                .circuitMeta(1)
                .outputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Calcium.getName() + "_" + AceticAcid.getName())
                .inputItems(dust, Calcium)
                .inputFluids(AceticAcid.getFluid(2000))
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(DissolvedCalciumAcetate.getFluid(1000))
                .duration(400).EUt(380).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Methanol.getName() + "_" + AceticAcid.getName())
                .inputFluids(Methanol.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(MethylAcetate.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .duration(240).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_" + Sulfur.getName() + "_" + Hydrogen.getName())
                .inputItems(dust, Sulfur)
                .inputFluids(Hydrogen.getFluid(2000))
                .outputFluids(HydrogenSulfide.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Air.getName() + "_" + VinylAcetate.getName() + ".0")
                .circuitMeta(1)
                .inputFluids(Air.getFluid(1000))
                .inputFluids(VinylAcetate.getFluid(144))
                .outputFluids(PolyvinylAcetate.getFluid(144))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Oxygen.getName() + "_" + VinylAcetate.getName() + ".0")
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(VinylAcetate.getFluid(144))
                .outputFluids(PolyvinylAcetate.getFluid(216))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Air.getName() + "_" + VinylAcetate.getName() + ".1")
                .circuitMeta(2)
                .inputFluids(Air.getFluid(7500))
                .inputFluids(VinylAcetate.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(PolyvinylAcetate.getFluid(3240))
                .duration(800).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Oxygen.getName() + "_" + VinylAcetate.getName() + ".1")
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(7500))
                .inputFluids(VinylAcetate.getFluid(2160))
                .inputFluids(TitaniumTetrachloride.getFluid(100))
                .outputFluids(PolyvinylAcetate.getFluid(4320))
                .duration(800).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Hydrogen.getName() + "_" + CarbonDioxide.getName() + ".0")
                .inputFluids(Hydrogen.getFluid(6000))
                .inputFluids(CarbonDioxide.getFluid(1000))
                .circuitMeta(2)
                .outputFluids(Water.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(120).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Hydrogen.getName() + "_" + CarbonMonoxide.getName() + ".0")
                .circuitMeta(1)
                .inputFluids(Hydrogen.getFluid(4000))
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(120).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s_%s".formatted(Carbon.getName(), Hydrogen.getName(), Oxygen.getName()))
                .circuitMeta(3)
                .inputItems(dust, Carbon)
                .inputFluids(Hydrogen.getFluid(4000))
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(320).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s_%s".formatted(Mercury.getName(), Water.getName(), Chlorine.getName()))
                .inputFluids(Mercury.getFluid(1000))
                .inputFluids(Water.getFluid(10000))
                .inputFluids(Chlorine.getFluid(10000))
                .outputFluids(HypochlorousAcid.getFluid(10000))
                .duration(600).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Water.getName(), Chlorine.getName()))
                .circuitMeta(1)
                .inputFluids(Water.getFluid(1000))
                .inputFluids(Chlorine.getFluid(2000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(1000))
                .outputFluids(HypochlorousAcid.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Dimethylamine.getName(), Monochloramine.getName()))
                .inputFluids(Dimethylamine.getFluid(1000))
                .inputFluids(Monochloramine.getFluid(1000))
                .outputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(1000))
                .duration(960).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s_%s".formatted(Methanol.getName(), Ammonia.getName(), HypochlorousAcid.getName()))
                .inputFluids(Methanol.getFluid(2000))
                .inputFluids(Ammonia.getFluid(2000))
                .inputFluids(HypochlorousAcid.getFluid(1000))
                .outputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(DilutedHydrochloricAcid.getFluid(2000))
                .duration(1040).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Hydrogen.getName(), Fluorine.getName()))
                .inputFluids(Hydrogen.getFluid(1000))
                .inputFluids(Fluorine.getFluid(1000))
                .outputFluids(HydrofluoricAcid.getFluid(1000))
                .duration(60).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s_%s".formatted(PhosphoricAcid.getName(), Benzene.getName(), Propene.getName()))
                .circuitMeta(1)
                .inputFluids(PhosphoricAcid.getFluid(1000))
                .inputFluids(Benzene.getFluid(8000))
                .inputFluids(Propene.getFluid(8000))
                .outputFluids(Cumene.getFluid(8000))
                .duration(1920).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Silicon.getName(), Chloromethane.getName()))
                .inputItems(dust, Silicon)
                .inputFluids(Chloromethane.getFluid(2000))
                .outputFluids(Dimethyldichlorosilane.getFluid(1000))
                .duration(240).EUt(96).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Oxygen.getName(), Ethylene.getName()))
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(2000))
                .inputFluids(Ethylene.getFluid(1000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(100).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(CarbonMonoxide.getName(), Methanol.getName()))
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .inputFluids(Methanol.getFluid(1000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(300).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Hydrogen.getName(), CarbonMonoxide.getName()))
                .circuitMeta(2)
                .inputFluids(Hydrogen.getFluid(4000))
                .inputFluids(CarbonMonoxide.getFluid(2000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(320).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s_%s".formatted(Carbon.getName(), Oxygen.getName(), Hydrogen.getName()))
                .circuitMeta(4)
                .inputItems(dust, Carbon, 2)
                .inputFluids(Oxygen.getFluid(2000))
                .inputFluids(Hydrogen.getFluid(4000))
                .outputFluids(AceticAcid.getFluid(1000))
                .duration(480).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Aluminium.getName(), IndiumConcentrate.getName()))
                .inputItems(dust, Aluminium, 4)
                .inputFluids(IndiumConcentrate.getFluid(1000))
                .outputItems(dustSmall, Indium)
                .outputItems(dust, AluminiumSulfite, 4)
                .outputFluids(LeadZincSolution.getFluid(1000))
                .duration(50).EUt(600).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s_%s".formatted(Oxygen.getName(), AceticAcid.getName(), Ethylene.getName()))
                .circuitMeta(3)
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(AceticAcid.getFluid(1000))
                .inputFluids(Ethylene.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(VinylAcetate.getFluid(1000))
                .duration(180).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s.0".formatted(Carbon.getName(), Oxygen.getName()))
                .circuitMeta(1)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(1000))
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("gem_%s_%s.0".formatted(Charcoal.getName(), Oxygen.getName()))
                .circuitMeta(1)
                .inputItems(gem, Charcoal)
                .inputFluids(Oxygen.getFluid(1000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("gem_%s_%s.0".formatted(Coal.getName(), Oxygen.getName()))
                .circuitMeta(1)
                .inputItems(gem, Coal)
                .inputFluids(Oxygen.getFluid(1000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s.0".formatted(Charcoal.getName(), Oxygen.getName()))
                .circuitMeta(1)
                .inputItems(dust, Charcoal)
                .inputFluids(Oxygen.getFluid(1000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s.0".formatted(Coal.getName(), Oxygen.getName()))
                .duration(80).EUt(VA[ULV])
                .inputItems(dust, Coal)
                .circuitMeta(1)
                .inputFluids(Oxygen.getFluid(1000))
                .outputItems(ChemicalHelper.get(dustTiny, Ash))
                .outputFluids(CarbonMonoxide.getFluid(1000))
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Carbon.getName(), CarbonDioxide.getName()))
                .inputItems(dust, Carbon)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputFluids(CarbonMonoxide.getFluid(2000))
                .duration(800).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(HypochlorousAcid.getName(), Ammonia.getName()))
                .inputFluids(HypochlorousAcid.getFluid(1000))
                .inputFluids(Ammonia.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .circuitMeta(1)
                .outputFluids(Monochloramine.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Ammonia.getName(), Methanol.getName()))
                .circuitMeta(2)
                .inputFluids(Ammonia.getFluid(1000))
                .inputFluids(Methanol.getFluid(2000))
                .outputFluids(Water.getFluid(2000))
                .outputFluids(Dimethylamine.getFluid(1000))
                .duration(240).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(HydrochloricAcid.getName(), Methanol.getName()))
                .circuitMeta(1)
                .inputFluids(HydrochloricAcid.getFluid(1000))
                .inputFluids(Methanol.getFluid(1000))
                .outputFluids(Water.getFluid(1000))
                .outputFluids(Chloromethane.getFluid(1000))
                .duration(160).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s.1".formatted(Carbon.getName(), Oxygen.getName()))
                .circuitMeta(2)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(2000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(40).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("gem_%s_%s.1".formatted(Charcoal.getName(), Oxygen.getName()))
                .circuitMeta(2)
                .inputItems(gem, Charcoal)
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("gem_%s_%s.1".formatted(Coal.getName(), Oxygen.getName()))
                .circuitMeta(2)
                .inputItems(gem, Coal)
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s.1".formatted(Charcoal.getName(), Oxygen.getName()))
                .circuitMeta(2)
                .inputItems(dust, Charcoal)
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s.1".formatted(Coal.getName(), Oxygen.getName()))
                .circuitMeta(2)
                .inputItems(dust, Coal)
                .inputFluids(Oxygen.getFluid(2000))
                .outputItems(dustTiny, Ash)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(80).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Water.getName(), Methane.getName()))
                .circuitMeta(1)
                .inputFluids(Water.getFluid(2000))
                .inputFluids(Methane.getFluid(1000))
                .outputFluids(Hydrogen.getFluid(8000))
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(150).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(MethylAcetate.getName(), NitricAcid.getName()))
                .inputFluids(MethylAcetate.getFluid(2000))
                .inputFluids(NitricAcid.getFluid(4000))
                .outputItems(dust, Carbon, 5)
                .outputFluids(Tetranitromethane.getFluid(1000))
                .outputFluids(Water.getFluid(8000))
                .duration(480).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(NitricAcid.getName(), Ethenone.getName()))
                .inputFluids(NitricAcid.getFluid(8000))
                .inputFluids(Ethenone.getFluid(1000))
                .outputFluids(Tetranitromethane.getFluid(2000))
                .outputFluids(Water.getFluid(5000))
                .duration(480).EUt(VA[MV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Oxygen.getName(), Ammonia.getName()))
                .circuitMeta(3)
                .inputFluids(Oxygen.getFluid(7000))
                .inputFluids(Ammonia.getFluid(2000))
                .outputFluids(DinitrogenTetroxide.getFluid(1000))
                .outputFluids(Water.getFluid(3000))
                .duration(480).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(NitrogenDioxide.getName(), DinitrogenTetroxide.getName()))
                .circuitMeta(2)
                .inputFluids(NitrogenDioxide.getFluid(2000))
                .outputFluids(DinitrogenTetroxide.getFluid(1000))
                .duration(640).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(SodiumHydroxide.getName(), SulfuricAcid.getName()))
                .inputItems(dust, SodiumHydroxide, 3)
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputItems(dust, SodiumBisulfate, 7)
                .outputFluids(Water.getFluid(1000))
                .duration(60).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sugar_dust_polythylene_toluene")
                .inputItems(new ItemStack(Items.SUGAR, 9))
                .inputItems(dust, Polyethylene)
                .inputFluids(Toluene.getFluid(1000))
                .outputItems(GELLED_TOLUENE.asStack(20))
                .duration(140).EUt(192).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_dust_%s_%s".formatted(Calcium.getName(), Carbon.getName(), Oxygen.getName()))
                .inputItems(dust, Calcium)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, Calcite, 5)
                .duration(500).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Quicklime.getName(), CarbonDioxide.getName()))
                .inputItems(dust, Quicklime, 2)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputItems(dust, Calcite, 5)
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Magnesia.getName(), CarbonDioxide.getName()))
                .inputItems(dust, Magnesia, 2)
                .inputFluids(CarbonDioxide.getFluid(1000))
                .outputItems(dust, Magnesite, 5)
                .duration(80).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_dust_%s".formatted(Calcite.getName(), Quicklime.getName()))
                .circuitMeta(1)
                .inputItems(dust, Calcite, 5)
                .outputItems(dust, Quicklime, 2)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_dust_%s".formatted(Magnesite.getName(), Magnesia.getName()))
                .inputItems(dust, Magnesite, 5)
                .outputItems(dust, Magnesia, 2)
                .outputFluids(CarbonDioxide.getFluid(1000))
                .duration(240).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_dust_%s".formatted(RawRubber.getName(), Sulfur.getName()))
                .inputItems(dust, RawRubber, 9)
                .inputItems(dust, Sulfur)
                .outputFluids(Rubber.getFluid(1296))
                .duration(600).EUt(16).save(provider);


        CHEMICAL_RECIPES.recipeBuilder(Items.CARROT.getDescriptionId())
                .inputItems(new ItemStack(Items.CARROT))
                .inputItems(nugget, Gold, 8)
                .outputItems(new ItemStack(Items.GOLDEN_CARROT))
                .duration(50).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Items.APPLE.getDescriptionId())
                .inputItems(new ItemStack(Items.APPLE))
                .inputItems(ingot, Gold, 8)
                .outputItems(new ItemStack(Items.GOLDEN_APPLE))
                .duration(50).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Items.MAGMA_CREAM.getDescriptionId())
                .inputItems(new ItemStack(Items.BLAZE_POWDER))
                .inputItems(new ItemStack(Items.SLIME_BALL))
                .outputItems(new ItemStack(Items.MAGMA_CREAM))
                .duration(50).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Blocks.TNT.getDescriptionId())
                .inputItems(GELLED_TOLUENE.asStack(4))
                .inputFluids(SulfuricAcid.getFluid(250))
                .outputItems(new ItemStack(Blocks.TNT))
                .duration(200).EUt(24).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(SodiumHydroxide.getName(), Dichlorobenzene.getName()))
                .inputItems(dust, SodiumHydroxide, 6)
                .inputFluids(Dichlorobenzene.getFluid(1000))
                .outputItems(dust, Salt, 4)
                .outputFluids(Phenol.getFluid(1000))
                .outputFluids(Oxygen.getFluid(1000))
                .duration(120).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(MethylAcetate.getName(), Water.getName()))
                .inputFluids(MethylAcetate.getFluid(1000))
                .inputFluids(Water.getFluid(1000))
                .notConsumable(ChemicalHelper.get(dust, SodiumHydroxide))
                .outputFluids(AceticAcid.getFluid(1000))
                .outputFluids(Methanol.getFluid(1000))
                .duration(264).EUt(60).save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("ingot_%s_dust_%s_%s".formatted(Plutonium239.getName(), Uranium238.getName(), Air.getName()))
                .inputItems(ingot, Plutonium239, 8)
                .inputItems(dust, Uranium238)
                .inputFluids(Air.getFluid(10000))
                .outputItems(dust, Plutonium239, 8)
                .outputFluids(Radon.getFluid(1000))
                .duration(4000).EUt(VA[HV]).save(provider);

        // TODO DYNAMITE
//        CHEMICAL_RECIPES.recipeBuilder()
//                .inputItems(Items.PAPER.getDefaultInstance())
//                .inputItems(Items.STRING.getDefaultInstance())
//                .inputFluids(GlycerylTrinitrate.getFluid(500))
//                .outputItems(DYNAMITE.asStack())
//                .duration(160).EUt(4).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Niobium.getName(), Nitrogen.getName()))
                .inputItems(dust, Niobium)
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(dust, NiobiumNitride, 2)
                .duration(200).EUt(VA[HV]).save(provider);

        // Dyes
        for (int i = 0; i < GTMaterials.CHEMICAL_DYES.length; i++) {
            CHEMICAL_RECIPES.recipeBuilder("chemical_dye_" + MarkerMaterials.Color.VALUES[i].getName().toLowerCase())
                    .inputItems(dye, MarkerMaterials.Color.VALUES[i])
                    .inputItems(dust, Salt, 2)
                    .inputFluids(SulfuricAcid.getFluid(250))
                    .outputFluids(GTMaterials.CHEMICAL_DYES[i].getFluid(288))
                    .duration(600).EUt(24).save(provider);
        }

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_dust_%s".formatted(Carbon.getName(), Sulfur.getName()))
                .inputItems(dust, Carbon)
                .inputItems(dust, Sulfur)
                .outputItems(dust, Blaze)
                .duration(200).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s_%s".formatted(Potassium.getName(), Oxygen.getName(), Nitrogen.getName()))
                .inputItems(dust, Potassium)
                .inputFluids(Oxygen.getFluid(3000))
                .inputFluids(Nitrogen.getFluid(1000))
                .outputItems(dust, Saltpeter, 5)
                .duration(180).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder(Items.GHAST_TEAR.getDescriptionId())
                .inputItems(new ItemStack(Items.GHAST_TEAR))
                .inputFluids(Water.getFluid(1000))
                .outputItems(dustTiny, Potassium)
                .outputItems(dustTiny, Lithium)
                .outputFluids(SaltWater.getFluid(1000))
                .duration(400).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_dust_%s".formatted(Sodium.getName(), Potassium.getName()))
                .inputItems(dust, Sodium)
                .inputItems(dust, Potassium)
                .outputFluids(SodiumPotassium.getFluid(1000))
                .duration(300).EUt(VA[LV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dust_%s_%s".formatted(Sodium.getName(), Chlorine.getName()))
                .inputItems(dust, Sodium)
                .inputFluids(Chlorine.getFluid(1000))
                .outputItems(dust, Salt, 2)
                .duration(200).EUt(VA[ULV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s_%s".formatted(Propene.getName(), Hydrogen.getName(), CarbonMonoxide.getName()))
                .inputFluids(Propene.getFluid(1000))
                .inputFluids(Hydrogen.getFluid(2000))
                .inputFluids(CarbonMonoxide.getFluid(1000))
                .outputFluids(Butyraldehyde.getFluid(1000))
                .duration(200).EUt(VA[HV]).save(provider);

        CHEMICAL_RECIPES.recipeBuilder("%s_%s".formatted(Butyraldehyde.getName(), PolyvinylAcetate.getName()))
                .inputFluids(Butyraldehyde.getFluid(250))
                .inputFluids(PolyvinylAcetate.getFluid(144))
                .outputFluids(PolyvinylButyral.getFluid(144))
                .duration(400).EUt(VA[HV]).save(provider);
    }
}
