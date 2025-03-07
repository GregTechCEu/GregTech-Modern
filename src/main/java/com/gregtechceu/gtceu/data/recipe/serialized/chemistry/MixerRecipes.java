package com.gregtechceu.gtceu.data.recipe.serialized.chemistry;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.DUST;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MIXER_RECIPES;

public class MixerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        MIXER_RECIPES.recipeBuilder("nitration_mixture")
                .inputFluids(NitricAcid.getFluid(1000))
                .inputFluids(SulfuricAcid.getFluid(1000))
                .outputFluids(NitrationMixture.getFluid(2000))
                .duration(500).EUt(2).save(provider);

        MIXER_RECIPES.recipeBuilder("glue_from_acetone")
                .inputFluids(PolyvinylAcetate.getFluid(1000))
                .inputFluids(Acetone.getFluid(1500))
                .outputFluids(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).save(provider);

        MIXER_RECIPES.recipeBuilder("glue_from_methyl_acetate")
                .inputFluids(PolyvinylAcetate.getFluid(1000))
                .inputFluids(MethylAcetate.getFluid(1500))
                .outputFluids(Glue.getFluid(2500))
                .duration(50).EUt(VA[ULV]).save(provider);

        MIXER_RECIPES.recipeBuilder("salt_water")
                .inputItems(DUST, Salt, 2)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(SaltWater.getFluid(1000))
                .duration(40).EUt(VA[ULV]).save(provider);

        MIXER_RECIPES.recipeBuilder("cetane_diesel_from_biodiesel")
                .inputFluids(BioDiesel.getFluid(1000))
                .inputFluids(Tetranitromethane.getFluid(40))
                .outputFluids(CetaneBoostedDiesel.getFluid(750))
                .duration(20).EUt(VA[HV]).save(provider);

        MIXER_RECIPES.recipeBuilder("cetane_diesel_from_diesel")
                .inputFluids(Diesel.getFluid(1000))
                .inputFluids(Tetranitromethane.getFluid(20))
                .outputFluids(CetaneBoostedDiesel.getFluid(1000))
                .duration(20).EUt(VA[HV]).save(provider);

        MIXER_RECIPES.recipeBuilder("rocket_fuel_from_oxygen")
                .inputFluids(Oxygen.getFluid(1000))
                .inputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(RocketFuel.getFluid(3000))
                .duration(60).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("rocket_fuel_from_dinitrogen_tetroxide")
                .inputFluids(DinitrogenTetroxide.getFluid(1000))
                .inputFluids(Dimethylhydrazine.getFluid(1000))
                .outputFluids(RocketFuel.getFluid(6000))
                .duration(60).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("diesel")
                .inputFluids(LightFuel.getFluid(5000))
                .inputFluids(HeavyFuel.getFluid(1000))
                .outputFluids(Diesel.getFluid(6000))
                .duration(16).EUt(VA[MV]).save(provider);

        MIXER_RECIPES.recipeBuilder("concrete_from_clay")
                .inputItems(DUST, Clay)
                .inputItems(DUST, Stone, 3)
                .inputFluids(Water.getFluid(500))
                .outputFluids(Concrete.getFluid(576))
                .duration(20).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("concrete_from_calcite")
                .inputItems(DUST, Stone, 2)
                .inputItems(DUST, Calcite)
                .inputItems(DUST, Gypsum)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Concrete.getFluid(1152))
                .duration(40).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("concrete_from_marble")
                .inputItems(DUST, Stone, 2)
                .inputItems(DUST, Marble)
                .inputItems(DUST, Gypsum)
                .inputFluids(Water.getFluid(1000))
                .outputFluids(Concrete.getFluid(1152))
                .duration(40).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("construction_foam")
                .inputFluids(Concrete.getFluid(576))
                .inputItems(DUST, RawRubber)
                .outputFluids(ConstructionFoam.getFluid(8000))
                .duration(20).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("drilling_fluid")
                .inputItems(DUST, Stone)
                .inputFluids(Lubricant.getFluid(20))
                .inputFluids(Water.getFluid(4980))
                .outputFluids(DrillingFluid.getFluid(5000))
                .duration(64).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("drilling_fluid_distilled")
                .inputItems(DUST, Stone)
                .inputFluids(Lubricant.getFluid(20))
                .inputFluids(DistilledWater.getFluid(4980))
                .outputFluids(DrillingFluid.getFluid(5000))
                .duration(48).EUt(16).save(provider);

        MIXER_RECIPES.recipeBuilder("ender_pearl_dust").duration(160).EUt(VA[HV])
                .inputItems(DUST, Beryllium)
                .inputItems(DUST, Potassium, 4)
                .inputFluids(Nitrogen.getFluid(5000))
                .circuitMeta(1)
                .outputItems(DUST, EnderPearl, 10)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("pcb_coolant").duration(200).EUt(VA[HV])
                .inputFluids(PolychlorinatedBiphenyl.getFluid(750))
                .inputFluids(DistilledWater.getFluid(250))
                .outputFluids(PCBCoolant.getFluid(1000))
                .save(provider);

        // Alloys
        VanillaRecipeHelper.addShapelessRecipe(provider, "dust_brass", ChemicalHelper.get(DUST, Brass, 3),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Zinc));

        VanillaRecipeHelper.addShapelessRecipe(provider, "dust_bronze", ChemicalHelper.get(DUST, Bronze, 3),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Copper),
                new MaterialEntry(DUST, Tin));

        MIXER_RECIPES.recipeBuilder("red_alloy").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Copper)
                .inputItems(DUST, Redstone, 4)
                .circuitMeta(2)
                .outputItems(DUST, RedAlloy)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("blue_alloy").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Silver)
                .inputItems(DUST, Electrotine, 4)
                .circuitMeta(2)
                .outputItems(DUST, BlueAlloy)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("electrotine").duration(200).EUt(VA[ULV])
                .inputItems(DUST, Redstone)
                .inputItems(DUST, Electrum)
                .circuitMeta(1)
                .outputItems(DUST, Electrotine)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("soldering_alloy").duration(200).EUt(VA[ULV])
                .inputItems(DUST, Tin, 6)
                .inputItems(DUST, Lead, 3)
                .inputItems(DUST, Antimony)
                .circuitMeta(3)
                .outputItems(DUST, SolderingAlloy, 10)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("gallium_arsenide").duration(200).EUt(VA[ULV])
                .inputItems(DUST, Gallium)
                .inputItems(DUST, Arsenic)
                .circuitMeta(1)
                .outputItems(DUST, GalliumArsenide, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("electrum").duration(200).EUt(VA[ULV])
                .inputItems(DUST, Gold)
                .inputItems(DUST, Silver)
                .circuitMeta(1)
                .outputItems(DUST, Electrum, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("borosilicate_dust").duration(200).EUt(VA[ULV])
                .circuitMeta(1)
                .inputItems(DUST, Boron)
                .inputItems(DUST, Glass, 7)
                .outputItems(DUST, BorosilicateGlass, 8)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("indium_gallium_phosphide").duration(200).EUt(VA[ULV])
                .inputItems(DUST, Indium)
                .inputItems(DUST, Gallium)
                .inputItems(DUST, Phosphorus)
                .circuitMeta(1)
                .outputItems(DUST, IndiumGalliumPhosphide, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("invar").duration(300).EUt(VA[ULV])
                .inputItems(DUST, Iron, 2)
                .inputItems(DUST, Nickel)
                .circuitMeta(1)
                .outputItems(DUST, Invar, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("bronze").duration(400).EUt(VA[ULV])
                .inputItems(DUST, Copper, 3)
                .inputItems(DUST, Tin)
                .circuitMeta(1)
                .outputItems(DUST, Bronze, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("potin").duration(400).EUt(VA[ULV])
                .inputItems(DUST, Copper, 6)
                .inputItems(DUST, Tin, 2)
                .inputItems(DUST, Lead)
                .circuitMeta(3)
                .outputItems(DUST, Potin, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("brass").duration(400).EUt(VA[ULV])
                .inputItems(DUST, Copper, 3)
                .inputItems(DUST, Zinc)
                .circuitMeta(1)
                .outputItems(DUST, Brass, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("bismuth_bronze").duration(500).EUt(VA[ULV])
                .inputItems(DUST, Bismuth)
                .inputItems(DUST, Brass, 4)
                .circuitMeta(1)
                .outputItems(DUST, BismuthBronze, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("black_bronze").duration(500).EUt(VA[ULV])
                .inputItems(DUST, Copper, 3)
                .inputItems(DUST, Electrum, 2)
                .circuitMeta(1)
                .outputItems(DUST, BlackBronze, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("black_steel").duration(500).EUt(VA[ULV])
                .inputItems(DUST, BlackBronze)
                .inputItems(DUST, Nickel)
                .inputItems(DUST, Steel, 3)
                .circuitMeta(1)
                .outputItems(DUST, BlackSteel, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("gunpowder_from_coal").duration(600).EUt(VA[ULV])
                .inputItems(DUST, Saltpeter, 2)
                .inputItems(DUST, Sulfur)
                .inputItems(DUST, Coal, 3)
                .circuitMeta(1)
                .outputItems(DUST, Gunpowder, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("gunpowder_from_charcoal").duration(600).EUt(VA[ULV])
                .inputItems(DUST, Saltpeter, 2)
                .inputItems(DUST, Sulfur)
                .inputItems(DUST, Charcoal, 3)
                .circuitMeta(1)
                .outputItems(DUST, Gunpowder, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("gunpowder_from_carbon").duration(400).EUt(VA[ULV])
                .inputItems(DUST, Saltpeter, 2)
                .inputItems(DUST, Sulfur)
                .inputItems(DUST, Carbon, 3)
                .circuitMeta(1)
                .outputItems(DUST, Gunpowder, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("red_steel").duration(800).EUt(VA[ULV])
                .inputItems(DUST, RoseGold)
                .inputItems(DUST, Brass)
                .inputItems(DUST, BlackSteel, 4)
                .inputItems(DUST, Steel, 2)
                .circuitMeta(1)
                .outputItems(DUST, RedSteel, 8)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("blue_steel").duration(800).EUt(VA[ULV])
                .inputItems(DUST, SterlingSilver)
                .inputItems(DUST, BismuthBronze)
                .inputItems(DUST, BlackSteel, 4)
                .inputItems(DUST, Steel, 2)
                .circuitMeta(1)
                .outputItems(DUST, BlueSteel, 8)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("cobalt_brass").duration(900).EUt(VA[ULV])
                .inputItems(DUST, Brass, 7)
                .inputItems(DUST, Aluminium)
                .inputItems(DUST, Cobalt)
                .circuitMeta(1)
                .outputItems(DUST, CobaltBrass, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("cupronickel").duration(200).EUt(24)
                .inputItems(DUST, Copper)
                .inputItems(DUST, Nickel)
                .circuitMeta(1)
                .outputItems(DUST, Cupronickel, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("ferrite_mixture").duration(200).EUt(VA[MV])
                .inputItems(DUST, Nickel)
                .inputItems(DUST, Zinc)
                .inputItems(DUST, Iron, 4)
                .circuitMeta(2)
                .outputItems(DUST, FerriteMixture, 6)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("kanthal").duration(300).EUt(VA[MV])
                .inputItems(DUST, Iron)
                .inputItems(DUST, Aluminium)
                .inputItems(DUST, Chromium)
                .circuitMeta(1)
                .outputItems(DUST, Kanthal, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("nichrome").duration(500).EUt(VA[MV])
                .inputItems(DUST, Nickel, 4)
                .inputItems(DUST, Chromium)
                .circuitMeta(2)
                .outputItems(DUST, Nichrome, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("rose_gold").duration(500).EUt(VA[MV])
                .inputItems(DUST, Copper)
                .inputItems(DUST, Gold, 4)
                .circuitMeta(3)
                .outputItems(DUST, RoseGold, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("stainless_steel_from_invar").duration(600).EUt(VA[MV])
                .inputItems(DUST, Iron, 4)
                .inputItems(DUST, Invar, 3)
                .inputItems(DUST, Manganese)
                .inputItems(DUST, Chromium)
                .circuitMeta(1)
                .outputItems(DUST, StainlessSteel, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("stainless_steel_from_elements").duration(600).EUt(VA[MV])
                .inputItems(DUST, Iron, 6)
                .inputItems(DUST, Nickel)
                .inputItems(DUST, Manganese)
                .inputItems(DUST, Chromium)
                .circuitMeta(3)
                .outputItems(DUST, StainlessSteel, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("graphene").duration(100).EUt(VA[HV])
                .inputItems(DUST, Graphite)
                .inputItems(DUST, Silicon)
                .inputItems(DUST, Carbon, 4)
                .circuitMeta(1)
                .outputItems(DUST, Graphene)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("vanadiumsteel").duration(400).EUt(VA[MV])
                .inputItems(DUST, Steel, 7)
                .inputItems(DUST, Vanadium)
                .inputItems(DUST, Chromium)
                .circuitMeta(1)
                .outputItems(DUST, VanadiumSteel, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("ultimet").duration(900).EUt(VA[HV])
                .inputItems(DUST, Cobalt, 5)
                .inputItems(DUST, Chromium, 2)
                .inputItems(DUST, Nickel)
                .inputItems(DUST, Molybdenum)
                .circuitMeta(1)
                .outputItems(DUST, Ultimet, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("tungstencarbide").duration(200).EUt(VA[EV])
                .inputItems(DUST, Tungsten)
                .inputItems(DUST, Carbon)
                .circuitMeta(2)
                .outputItems(DUST, TungstenCarbide, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("tungstensteel").duration(200).EUt(VA[EV])
                .inputItems(DUST, Tungsten)
                .inputItems(DUST, Steel)
                .circuitMeta(1)
                .outputItems(DUST, TungstenSteel, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("vanadium_gallium").duration(400).EUt(VA[EV])
                .inputItems(DUST, Vanadium, 3)
                .inputItems(DUST, Gallium)
                .circuitMeta(1)
                .outputItems(DUST, VanadiumGallium, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hssg").duration(400).EUt(VA[EV])
                .inputItems(DUST, TungstenSteel, 5)
                .inputItems(DUST, Chromium)
                .inputItems(DUST, Molybdenum, 2)
                .inputItems(DUST, Vanadium)
                .circuitMeta(1)
                .outputItems(DUST, HSSG, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("yttrium_barium_cuprate").duration(600).EUt(VA[EV])
                .inputItems(DUST, Yttrium)
                .inputItems(DUST, Barium, 2)
                .inputItems(DUST, Copper, 3)
                .circuitMeta(2)
                .inputFluids(Oxygen.getFluid(7000))
                .outputItems(DUST, YttriumBariumCuprate, 13)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hsse").duration(450).EUt(4046)
                .inputItems(DUST, HSSG, 6)
                .inputItems(DUST, Cobalt)
                .inputItems(DUST, Manganese)
                .inputItems(DUST, Silicon)
                .circuitMeta(1)
                .outputItems(DUST, HSSE, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("niobium_titanium").duration(200).EUt(VA[IV])
                .inputItems(DUST, Niobium)
                .inputItems(DUST, Titanium)
                .circuitMeta(1)
                .outputItems(DUST, NiobiumTitanium, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("hsss").duration(500).EUt(VA[IV])
                .inputItems(DUST, HSSG, 6)
                .inputItems(DUST, Iridium, 2)
                .inputItems(DUST, Osmium)
                .circuitMeta(2)
                .outputItems(DUST, HSSS, 9)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("naquadah_alloy").duration(400).EUt(VA[IV])
                .inputItems(DUST, Naquadah, 2)
                .inputItems(DUST, Osmiridium)
                .inputItems(DUST, Trinium)
                .circuitMeta(2)
                .outputItems(DUST, NaquadahAlloy, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("osmiridium").duration(300).EUt(VA[LuV])
                .inputItems(DUST, Osmium)
                .inputItems(DUST, Iridium, 3)
                .circuitMeta(1)
                .outputItems(DUST, Osmiridium, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("rhodium_plated_palladium").duration(400).EUt(VA[IV])
                .inputItems(DUST, Palladium, 3)
                .inputItems(DUST, Rhodium)
                .circuitMeta(1)
                .outputItems(DUST, RhodiumPlatedPalladium, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("magnalium").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Aluminium, 2)
                .inputItems(DUST, Magnesium)
                .circuitMeta(1)
                .outputItems(DUST, Magnalium, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("sterling_silver").duration(500).EUt(VA[MV])
                .inputItems(DUST, Copper)
                .inputItems(DUST, Silver, 4)
                .circuitMeta(1)
                .outputItems(DUST, SterlingSilver, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("tin_alloy").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Tin)
                .inputItems(DUST, Iron)
                .circuitMeta(1)
                .outputItems(DUST, TinAlloy, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("battery_alloy").duration(100).EUt(VA[ULV])
                .inputItems(DUST, Lead, 4)
                .inputItems(DUST, Antimony)
                .circuitMeta(1)
                .outputItems(DUST, BatteryAlloy, 5)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("ruridit").duration(350).EUt(4096)
                .inputItems(DUST, Ruthenium, 2)
                .inputItems(DUST, Iridium)
                .circuitMeta(1)
                .outputItems(DUST, Ruridit, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("rtm_alloy").duration(300).EUt(VA[EV])
                .inputItems(DUST, Ruthenium, 4)
                .inputItems(DUST, Tungsten, 2)
                .inputItems(DUST, Molybdenum)
                .circuitMeta(1)
                .outputItems(DUST, RTMAlloy, 7)
                .save(provider);

        // Superconductor Alloys
        MIXER_RECIPES.recipeBuilder("manganese_phosphide").duration(400).EUt(24)
                .inputItems(DUST, Manganese)
                .inputItems(DUST, Phosphorus)
                .circuitMeta(4)
                .outputItems(DUST, ManganesePhosphide, 2)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("magnesium_diboride").duration(600).EUt(VA[MV])
                .inputItems(DUST, Magnesium)
                .inputItems(DUST, Boron, 2)
                .circuitMeta(4)
                .outputItems(DUST, MagnesiumDiboride, 3)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("mercury_barium_calcium_cuprate").duration(400).EUt(VA[MV])
                .inputItems(DUST, Barium, 2)
                .inputItems(DUST, Calcium, 2)
                .inputItems(DUST, Copper, 3)
                .inputFluids(Mercury.getFluid(1000))
                .inputFluids(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .outputItems(DUST, MercuryBariumCalciumCuprate, 16)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("uranium_triplatinum").duration(200).EUt(VA[EV])
                .inputItems(DUST, Uranium238)
                .inputItems(DUST, Platinum, 3)
                .circuitMeta(4)
                .outputItems(DUST, UraniumTriplatinum, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("samarium_iron_arsenic_oxide").duration(100).EUt(VA[IV])
                .inputItems(DUST, Samarium)
                .inputItems(DUST, Iron)
                .inputItems(DUST, Arsenic)
                .inputFluids(Oxygen.getFluid(1000))
                .circuitMeta(4)
                .outputItems(DUST, SamariumIronArsenicOxide, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("indium_tin_barium_titanium_cuprate").duration(600).EUt(VA[LuV])
                .inputItems(DUST, Indium, 4)
                .inputItems(DUST, Tin, 2)
                .inputItems(DUST, Barium, 2)
                .inputItems(DUST, Titanium)
                .inputItems(DUST, Copper, 7)
                .inputFluids(Oxygen.getFluid(14000))
                .circuitMeta(4)
                .outputItems(DUST, IndiumTinBariumTitaniumCuprate, 16)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("uranium_rhodium_dinaquadide").duration(150).EUt(VA[ZPM])
                .inputItems(DUST, Uranium238)
                .inputItems(DUST, Rhodium)
                .inputItems(DUST, Naquadah, 2)
                .circuitMeta(4)
                .outputItems(DUST, UraniumRhodiumDinaquadide, 4)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("enriched_naquadah_trinium_europium_duranide").duration(175).EUt(VA[UV])
                .inputItems(DUST, NaquadahEnriched, 4)
                .inputItems(DUST, Trinium, 3)
                .inputItems(DUST, Europium, 2)
                .inputItems(DUST, Duranium)
                .circuitMeta(4)
                .outputItems(DUST, EnrichedNaquadahTriniumEuropiumDuranide, 10)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("ruthenium_trinium_americium_neutronate").duration(400).EUt(VA[UV])
                .inputItems(DUST, Ruthenium)
                .inputItems(DUST, Trinium, 2)
                .inputItems(DUST, Americium)
                .inputItems(DUST, Neutronium, 2)
                .inputFluids(Oxygen.getFluid(8000))
                .circuitMeta(4)
                .outputItems(DUST, RutheniumTriniumAmericiumNeutronate, 14)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("rad_away")
                .inputItems(DUST, PotassiumIodide, 5)
                .inputItems(DUST, PrussianBlue, 3)
                .inputItems(DUST, DiethylenetriaminepentaaceticAcid, 10)
                .outputItems(DUST, RadAway, 48)
                .duration(60).EUt(VA[HV]).save(provider);
    }
}
