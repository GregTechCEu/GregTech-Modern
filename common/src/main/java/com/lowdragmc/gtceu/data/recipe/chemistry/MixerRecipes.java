package com.lowdragmc.gtceu.data.recipe.chemistry;

import com.lowdragmc.gtceu.api.data.chemical.ChemicalHelper;
import com.lowdragmc.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.lowdragmc.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
import static com.lowdragmc.gtceu.api.GTValues.*;

public class MixerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        MIXER_RECIPES.recipeBuilder(NitrationMixture.getName())
                .inputFluids(NitricAcid.getFluid(1000))
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputFluids(NitrationMixture.getFluid(2000))
                .duration(500).EUt(2).save(provider);

        MIXER_RECIPES.recipeBuilder("glue.0")
                .inputFluids(PolyvinylAcetate.getFluid(1000))
                .inputFluids(Acetone.getFluid(1500))
                .outputFluids(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).save(provider);

        MIXER_RECIPES.recipeBuilder("glue.1")
                .inputFluids(PolyvinylAcetate.getFluid(1000))
                .inputFluids(MethylAcetate.getFluid(1500))
                .outputFluids(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).save(provider);

        MIXER_RECIPES.recipeBuilder(SaltWater.getName())
                .inputItems(dust, Salt, 2)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(SaltWater.getFluid(1000))
                .duration(40).EUt(VA[ULV]).save(provider);

        MIXER_RECIPES.recipeBuilder(CetaneBoostedDiesel.getName() + ".0")
                .inputFluids(BioDiesel.getFluid(1000))
                .inputFluids(Tetranitromethane.getFluid(40))
                .outputFluids(CetaneBoostedDiesel.getFluid(750))
                .duration(20).EUt(VA[HV]).save(provider);

        MIXER_RECIPES.recipeBuilder(CetaneBoostedDiesel.getName() + ".1")
                .inputFluids(Diesel.getFluid(1000))
                .inputFluids(Tetranitromethane.getFluid(20))
                .outputFluids(CetaneBoostedDiesel.getFluid(1000))
                .duration(20).EUt(VA[HV]).save(provider);

        MIXER_RECIPES.recipeBuilder(RocketFuel.getName() + ".0")
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(RocketFuel.getFluid(3000))
                .duration(60).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(RocketFuel.getName() + ".1")
                .inputFluids(DinitrogenTetroxide.getFluid(1000))
                .inputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(RocketFuel.getFluid(6000))
                .duration(60).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(Diesel.getName())
                .inputFluids(LightFuel.getFluid(5000))
                .inputFluids(HeavyFuel.getFluid(1000))
                .outputFluids(Diesel.getFluid(6000))
                .duration(16).EUt(VA[MV]).save(provider);

        MIXER_RECIPES.recipeBuilder(Concrete.getName() + ".0")
                .inputItems(dust, Clay)
                .inputItems(dust, Stone, 3)
                .inputFluids(Water.getFluid(500))
                .outputFluids(Concrete.getFluid(576))
                .duration(20).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(Concrete.getName() + ".1")
                .inputItems(dust, Stone, 2)
                .inputItems(dust, Calcite)
                .inputItems(dust, Gypsum)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Concrete.getFluid(1152))
                .duration(40).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(Concrete.getName() + ".2")
                .inputItems(dust, Stone, 2)
                .inputItems(dust, Marble)
                .inputItems(dust, Gypsum)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Concrete.getFluid(1152))
                .duration(40).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(ConstructionFoam.getName())
                .inputFluids(Concrete.getFluid(576))
                .inputItems(dust, RawRubber)
                .outputFluids(ConstructionFoam.getFluid(8000))
                .duration(20).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(DrillingFluid.getName())
                .inputItems(dust, Stone)
                .inputFluids(Lubricant.getFluid(20))
                .inputFluids(Water.getFluid(4980))
                .outputFluids(DrillingFluid.getFluid(5000))
                .duration(64).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder(EnderPearl.getName()).duration(160).EUt(VA[HV])
                .inputItems(dust, Beryllium)
                .inputItems(dust, Potassium, 4)
                .inputFluids(Nitrogen.getFluid(5000))
                .circuitMeta(1)
                .outputItems(dust, EnderPearl, 10)
                .save(provider);

        // Alloys
        VanillaRecipeHelper.addShapelessRecipe(provider, "dust_brass", ChemicalHelper.get(dust, Brass, 3),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Zinc));

        VanillaRecipeHelper.addShapelessRecipe(provider, "dust_bronze", ChemicalHelper.get(dust, Bronze, 3),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Copper),
                new UnificationEntry(dust, Tin));

        MIXER_RECIPES.recipeBuilder("dust_red_alloy").duration(100).EUt(VA[ULV])
                .inputItems(dust, Copper)
                .inputItems(dust, Redstone, 4)
                .circuitMeta(2)
                .outputItems(dust, RedAlloy)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_blue_alloy").duration(100).EUt(VA[ULV])
                .inputItems(dust, Silver)
                .inputItems(dust, Electrotine, 4)
                .circuitMeta(2)
                .outputItems(dust, BlueAlloy)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_electrotine").duration(200).EUt(VA[ULV])
                .inputItems(dust, Redstone)
                .inputItems(dust, Electrum)
                .circuitMeta(1)
                .outputItems(dust, Electrotine)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_soldering_alloy").duration(200).EUt(VA[ULV])
                .inputItems(dust, Tin, 6)
                .inputItems(dust, Lead, 3)
                .inputItems(dust, Antimony)
                .circuitMeta(3)
                .outputItems(dust, SolderingAlloy, 10)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + GalliumArsenide.getName()).duration(200).EUt(VA[ULV])
                .inputItems(dust, Gallium)
                .inputItems(dust, Arsenic)
                .circuitMeta(1)
                .outputItems(dust, GalliumArsenide, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Electrum.getName()).duration(200).EUt(VA[ULV])
                .inputItems(dust, Gold)
                .inputItems(dust, Silver)
                .circuitMeta(1)
                .outputItems(dust, Electrum, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + BorosilicateGlass.getName()).duration(200).EUt(VA[ULV])
                .circuitMeta(1)
                .inputItems(dust, Boron)
                .inputItems(dust, Glass, 7)
                .outputItems(dust, BorosilicateGlass, 8)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + IndiumGalliumPhosphide.getName()).duration(200).EUt(VA[ULV])
                .inputItems(dust, Indium)
                .inputItems(dust, Gallium)
                .inputItems(dust, Phosphorus)
                .circuitMeta(1)
                .outputItems(dust, IndiumGalliumPhosphide, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Invar.getName()).duration(300).EUt(VA[ULV])
                .inputItems(dust, Iron, 2)
                .inputItems(dust, Nickel)
                .circuitMeta(1)
                .outputItems(dust, Invar, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Bronze.getName()).duration(400).EUt(VA[ULV])
                .inputItems(dust, Copper, 3)
                .inputItems(dust, Tin)
                .circuitMeta(1)
                .outputItems(dust, Bronze, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Potin.getName()).duration(400).EUt(VA[ULV])
                .inputItems(dust, Copper, 6)
                .inputItems(dust, Tin, 2)
                .inputItems(dust, Lead)
                .circuitMeta(3)
                .outputItems(dust, Potin, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Brass.getName()).duration(400).EUt(VA[ULV])
                .inputItems(dust, Copper, 3)
                .inputItems(dust, Zinc)
                .circuitMeta(1)
                .outputItems(dust, Brass, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + BismuthBronze.getName()).duration(500).EUt(VA[ULV])
                .inputItems(dust, Bismuth)
                .inputItems(dust, Brass, 4)
                .circuitMeta(1)
                .outputItems(dust, BismuthBronze, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + BlackBronze.getName()).duration(500).EUt(VA[ULV])
                .inputItems(dust, Copper, 3)
                .inputItems(dust, Electrum, 2)
                .circuitMeta(1)
                .outputItems(dust, BlackBronze, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + BlackSteel.getName()).duration(500).EUt(VA[ULV])
                .inputItems(dust, BlackBronze)
                .inputItems(dust, Nickel)
                .inputItems(dust, Steel, 3)
                .circuitMeta(1)
                .outputItems(dust, BlackSteel, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Gunpowder.getName() + ".0").duration(600).EUt(VA[ULV])
                .inputItems(dust, Saltpeter, 2)
                .inputItems(dust, Sulfur)
                .inputItems(dust, Coal, 3)
                .circuitMeta(1)
                .outputItems(dust, Gunpowder, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Gunpowder.getName() + ".1").duration(600).EUt(VA[ULV])
                .inputItems(dust, Saltpeter, 2)
                .inputItems(dust, Sulfur)
                .inputItems(dust, Charcoal, 3)
                .circuitMeta(1)
                .outputItems(dust, Gunpowder, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Gunpowder.getName() + ".2").duration(400).EUt(VA[ULV])
                .inputItems(dust, Saltpeter, 2)
                .inputItems(dust, Sulfur)
                .inputItems(dust, Carbon, 3)
                .circuitMeta(1)
                .outputItems(dust, Gunpowder, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + BlueSteel.getName()).duration(800).EUt(VA[ULV])
                .inputItems(dust, RoseGold)
                .inputItems(dust, Brass)
                .inputItems(dust, BlackSteel, 4)
                .inputItems(dust, Steel, 2)
                .circuitMeta(1)
                .outputItems(dust, BlueSteel, 8)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + RedSteel.getName()).duration(800).EUt(VA[ULV])
                .inputItems(dust, SterlingSilver)
                .inputItems(dust, BismuthBronze)
                .inputItems(dust, BlackSteel, 4)
                .inputItems(dust, Steel, 2)
                .circuitMeta(1)
                .outputItems(dust, RedSteel, 8)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + CobaltBrass.getName()).duration(900).EUt(VA[ULV])
                .inputItems(dust, Brass, 7)
                .inputItems(dust, Aluminium)
                .inputItems(dust, Cobalt)
                .circuitMeta(1)
                .outputItems(dust, CobaltBrass, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Cupronickel.getName()).duration(200).EUt(24)
                .inputItems(dust, Copper)
                .inputItems(dust, Nickel)
                .circuitMeta(1)
                .outputItems(dust, Cupronickel, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + FerriteMixture.getName()).duration(200).EUt(VA[MV])
                .inputItems(dust, Nickel)
                .inputItems(dust, Zinc)
                .inputItems(dust, Iron, 4)
                .circuitMeta(2)
                .outputItems(dust, FerriteMixture, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Kanthal.getName()).duration(300).EUt(VA[MV])
                .inputItems(dust, Iron)
                .inputItems(dust, Aluminium)
                .inputItems(dust, Chrome)
                .circuitMeta(1)
                .outputItems(dust, Kanthal, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Nichrome.getName()).duration(500).EUt(VA[MV])
                .inputItems(dust, Nickel, 4)
                .inputItems(dust, Chrome)
                .circuitMeta(2)
                .outputItems(dust, Nichrome, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + RoseGold.getName()).duration(500).EUt(VA[MV])
                .inputItems(dust, Copper)
                .inputItems(dust, Gold, 4)
                .circuitMeta(3)
                .outputItems(dust, RoseGold, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + StainlessSteel.getName() + ".0").duration(600).EUt(VA[MV])
                .inputItems(dust, Iron, 4)
                .inputItems(dust, Invar, 3)
                .inputItems(dust, Manganese)
                .inputItems(dust, Chrome)
                .circuitMeta(1)
                .outputItems(dust, StainlessSteel, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + StainlessSteel.getName() + ".1").duration(600).EUt(VA[MV])
                .inputItems(dust, Iron, 6)
                .inputItems(dust, Nickel)
                .inputItems(dust, Manganese)
                .inputItems(dust, Chrome)
                .circuitMeta(3)
                .outputItems(dust, StainlessSteel, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Graphene.getName()).duration(100).EUt(VA[HV])
                .inputItems(dust, Graphite)
                .inputItems(dust, Silicon)
                .inputItems(dust, Carbon, 4)
                .circuitMeta(1)
                .outputItems(dust, Graphene)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + VanadiumSteel.getName()).duration(400).EUt(VA[MV])
                .inputItems(dust, Steel, 7)
                .inputItems(dust, Vanadium)
                .inputItems(dust, Chrome)
                .circuitMeta(1)
                .outputItems(dust, VanadiumSteel, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Ultimet.getName()).duration(900).EUt(VA[HV])
                .inputItems(dust, Cobalt, 5)
                .inputItems(dust, Chrome, 2)
                .inputItems(dust, Nickel)
                .inputItems(dust, Molybdenum)
                .circuitMeta(1)
                .outputItems(dust, Ultimet, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + TungstenCarbide.getName()).duration(200).EUt(VA[EV])
                .inputItems(dust, Tungsten)
                .inputItems(dust, Carbon)
                .circuitMeta(1)
                .outputItems(dust, TungstenCarbide, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + TungstenSteel.getName()).duration(200).EUt(VA[EV])
                .inputItems(dust, Tungsten)
                .inputItems(dust, Steel)
                .circuitMeta(1)
                .outputItems(dust, TungstenSteel, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + VanadiumGallium.getName()).duration(400).EUt(VA[EV])
                .inputItems(dust, Vanadium, 3)
                .inputItems(dust, Gallium)
                .circuitMeta(1)
                .outputItems(dust, VanadiumGallium, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + HSSG.getName() + ".0").duration(400).EUt(VA[EV])
                .inputItems(dust, TungstenSteel, 5)
                .inputItems(dust, Chrome)
                .inputItems(dust, Molybdenum, 2)
                .inputItems(dust, Vanadium)
                .circuitMeta(1)
                .outputItems(dust, HSSG, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + YttriumBariumCuprate.getName()).duration(600).EUt(VA[EV])
                .inputItems(dust, Yttrium)
                .inputItems(dust, Barium, 2)
                .inputItems(dust, Copper, 3)
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(7000))
                .outputItems(dust, YttriumBariumCuprate, 13)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + HSSE.getName() + ".1").duration(450).EUt(4046)
                .inputItems(dust, HSSG, 6)
                .inputItems(dust, Cobalt)
                .inputItems(dust, Manganese)
                .inputItems(dust, Silicon)
                .circuitMeta(1)
                .outputItems(dust, HSSE, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + NiobiumTitanium.getName()).duration(200).EUt(VA[IV])
                .inputItems(dust, Niobium)
                .inputItems(dust, Titanium)
                .circuitMeta(1)
                .outputItems(dust, NiobiumTitanium, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + HSSS.getName() + ".2").duration(500).EUt(VA[IV])
                .inputItems(dust, HSSG, 6)
                .inputItems(dust, Iridium, 2)
                .inputItems(dust, Osmium)
                .circuitMeta(2)
                .outputItems(dust, HSSS, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + NaquadahAlloy.getName()).duration(400).EUt(VA[IV])
                .inputItems(dust, Naquadah, 2)
                .inputItems(dust, Osmiridium)
                .inputItems(dust, Trinium)
                .circuitMeta(2)
                .outputItems(dust, NaquadahAlloy, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Osmiridium.getName()).duration(300).EUt(VA[LuV])
                .inputItems(dust, Osmium)
                .inputItems(dust, Iridium, 3)
                .circuitMeta(1)
                .outputItems(dust, Osmiridium, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + RhodiumPlatedPalladium.getName()).duration(400).EUt(VA[IV])
                .inputItems(dust, Palladium, 3)
                .inputItems(dust, Rhodium)
                .circuitMeta(1)
                .outputItems(dust, RhodiumPlatedPalladium, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Magnalium.getName()).duration(100).EUt(VA[ULV])
                .inputItems(dust, Aluminium, 2)
                .inputItems(dust, Magnesium)
                .circuitMeta(1)
                .outputItems(dust, Magnalium, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + SterlingSilver.getName()).duration(500).EUt(VA[MV])
                .inputItems(dust, Copper)
                .inputItems(dust, Silver, 4)
                .circuitMeta(1)
                .outputItems(dust, SterlingSilver, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + TinAlloy.getName()).duration(100).EUt(VA[ULV])
                .inputItems(dust, Tin)
                .inputItems(dust, Iron)
                .circuitMeta(1)
                .outputItems(dust, TinAlloy, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + BatteryAlloy.getName()).duration(100).EUt(VA[ULV])
                .inputItems(dust, Lead, 4)
                .inputItems(dust, Antimony)
                .circuitMeta(1)
                .outputItems(dust, BatteryAlloy, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + Ruridit.getName()).duration(350).EUt(4096)
                .inputItems(dust, Ruthenium, 2)
                .inputItems(dust, Iridium)
                .circuitMeta(1)
                .outputItems(dust, Ruridit, 3)
                .save(provider);

        // Superconductor Alloys
        MIXER_RECIPES.recipeBuilder("dust_" + ManganesePhosphide.getName()).duration(400).EUt(24)
                .inputItems(dust, Manganese)
                .inputItems(dust, Phosphorus)
                .circuitMeta(4)
                .outputItems(dust, ManganesePhosphide, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + MagnesiumDiboride.getName()).duration(600).EUt(VA[MV])
                .inputItems(dust, Magnesium)
                .inputItems(dust, Boron, 2)
                .circuitMeta(4)
                .outputItems(dust, MagnesiumDiboride, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + MercuryBariumCalciumCuprate.getName()).duration(400).EUt(VA[MV])
                .inputItems(dust, Barium, 2)
                .inputItems(dust, Calcium, 2)
                .inputItems(dust, Copper, 3)
                .inputFluids(Mercury.getFluid(1000))
                .inputFluids(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .outputItems(dust, MercuryBariumCalciumCuprate, 16)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + UraniumTriplatinum.getName()).duration(200).EUt(VA[EV])
                .inputItems(dust, Uranium238)
                .inputItems(dust, Platinum, 3)
                .circuitMeta(4)
                .outputItems(dust, UraniumTriplatinum, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + SamariumIronArsenicOxide.getName()).duration(100).EUt(VA[IV])
                .inputItems(dust, Samarium)
                .inputItems(dust, Iron)
                .inputItems(dust, Arsenic)
                .inputFluids(Oxygen.getFluid(1000))
                .circuitMeta(4)
                .outputItems(dust, SamariumIronArsenicOxide, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + IndiumTinBariumTitaniumCuprate.getName()).duration(600).EUt(VA[LuV])
                .inputItems(dust, Indium, 4)
                .inputItems(dust, Tin, 2)
                .inputItems(dust, Barium, 2)
                .inputItems(dust, Titanium)
                .inputItems(dust, Copper, 7)
                .inputFluids(Oxygen.getFluid(14000))
                .circuitMeta(4)
                .outputItems(dust, IndiumTinBariumTitaniumCuprate, 16)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + UraniumRhodiumDinaquadide.getName()).duration(150).EUt(VA[ZPM])
                .inputItems(dust, Uranium238)
                .inputItems(dust, Rhodium)
                .inputItems(dust, Naquadah, 2)
                .circuitMeta(4)
                .outputItems(dust, UraniumRhodiumDinaquadide, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + EnrichedNaquadahTriniumEuropiumDuranide.getName()).duration(175).EUt(VA[UV])
                .inputItems(dust, NaquadahEnriched, 4)
                .inputItems(dust, Trinium, 3)
                .inputItems(dust, Europium, 2)
                .inputItems(dust, Duranium)
                .circuitMeta(4)
                .outputItems(dust, EnrichedNaquadahTriniumEuropiumDuranide, 10)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("dust_" + RutheniumTriniumAmericiumNeutronate.getName()).duration(400).EUt(VA[UV])
                .inputItems(dust, Ruthenium)
                .inputItems(dust, Trinium, 2)
                .inputItems(dust, Americium)
                .inputItems(dust, Neutronium, 2)
                .inputFluids(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .outputItems(dust, RutheniumTriniumAmericiumNeutronate, 14)
                .save(provider);
    }
}
