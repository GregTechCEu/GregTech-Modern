package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class BatteryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        standardBatteries(provider);
        gemBatteries(provider);
        batteryBlocks(provider);
    }

    private static void standardBatteries(Consumer<FinishedRecipe> provider) {
        // Tantalum Battery (since it doesn't fit elsewhere)
        ASSEMBLER_RECIPES.recipeBuilder("tantalum_capacitor")
                .inputItems(DUST, Tantalum)
                .inputItems(FOIL, Manganese)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(BATTERY_ULV_TANTALUM, 8)
                .duration(30).EUt(4).save(provider);

        // :trol:
        VanillaRecipeHelper.addShapedRecipe(provider, "tantalum_capacitor", BATTERY_ULV_TANTALUM.asStack(2),
                " F ", "FDF", "B B",
                'F', new MaterialEntry(FOIL, Manganese),
                'D', new MaterialEntry(DUST, Tantalum),
                'B', new MaterialEntry(BOLT, Iron));

        // Battery Hull Recipes

        // LV
        VanillaRecipeHelper.addShapedRecipe(provider, "battery_hull_lv", BATTERY_HULL_LV.asStack(),
                "C", "P", "P",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Tin),
                'P', new MaterialEntry(PLATE, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_lv")
                .inputItems(CABLE_GT_SINGLE, Tin)
                .inputItems(PLATE, BatteryAlloy)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(BATTERY_HULL_LV)
                .duration(400).EUt(1).save(provider);

        // MV
        VanillaRecipeHelper.addShapedRecipe(provider, "battery_hull_mv", BATTERY_HULL_MV.asStack(),
                "C C", "PPP", "PPP",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Copper),
                'P', new MaterialEntry(PLATE, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_mv_copper")
                .inputItems(CABLE_GT_SINGLE, Copper, 2)
                .inputItems(PLATE, BatteryAlloy, 3)
                .inputFluids(Polyethylene.getFluid(L * 3))
                .outputItems(BATTERY_HULL_MV)
                .duration(200).EUt(2).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_mv_annealed")
                .inputItems(CABLE_GT_SINGLE, AnnealedCopper, 2)
                .inputItems(PLATE, BatteryAlloy, 3)
                .inputFluids(Polyethylene.getFluid(L * 3))
                .outputItems(BATTERY_HULL_MV)
                .duration(200).EUt(2).save(provider);

        // HV
        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_hv")
                .inputItems(CABLE_GT_SINGLE, Gold, 4)
                .inputItems(PLATE, BatteryAlloy, 9)
                .inputFluids(Polyethylene.getFluid(1296))
                .outputItems(BATTERY_HULL_HV)
                .duration(300).EUt(4).save(provider);

        // EV
        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_ev")
                .inputItems(CABLE_GT_SINGLE, Aluminium, 2)
                .inputItems(PLATE, RedSteel, 2)
                .inputFluids(Polytetrafluoroethylene.getFluid(L))
                .outputItems(BATTERY_HULL_SMALL_VANADIUM)
                .duration(100).EUt(VA[HV]).save(provider);

        // IV
        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_iv")
                .inputItems(CABLE_GT_SINGLE, Platinum, 2)
                .inputItems(PLATE, RoseGold, 6)
                .inputFluids(Polytetrafluoroethylene.getFluid(288))
                .outputItems(BATTERY_HULL_MEDIUM_VANADIUM)
                .duration(200).EUt(VA[EV]).save(provider);

        // LuV
        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_luv")
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 2)
                .inputItems(PLATE, BlueSteel, 18)
                .inputFluids(Polybenzimidazole.getFluid(L))
                .outputItems(BATTERY_HULL_LARGE_VANADIUM)
                .duration(300).EUt(VA[IV]).save(provider);

        // ZPM
        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_zpm")
                .inputItems(CABLE_GT_SINGLE, Naquadah, 2)
                .inputItems(PLATE, Europium, 6)
                .inputFluids(Polybenzimidazole.getFluid(288))
                .outputItems(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .duration(200).EUt(VA[LuV]).save(provider);

        // UV
        ASSEMBLER_RECIPES.recipeBuilder("battery_hull_uv")
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 2)
                .inputItems(PLATE, Americium, 18)
                .inputFluids(Polybenzimidazole.getFluid(576))
                .outputItems(BATTERY_HULL_LARGE_NAQUADRIA)
                .duration(300).EUt(VA[ZPM]).save(provider);

        // Battery Filling Recipes

        // LV
        CANNER_RECIPES.recipeBuilder("cadmium_battery_lv")
                .inputItems(BATTERY_HULL_LV)
                .inputItems(DUST, Cadmium, 2)
                .outputItems(BATTERY_LV_CADMIUM)
                .duration(100).EUt(2).save(provider);

        CANNER_RECIPES.recipeBuilder("lithium_battery_lv")
                .inputItems(BATTERY_HULL_LV)
                .inputItems(DUST, Lithium, 2)
                .outputItems(BATTERY_LV_LITHIUM)
                .duration(100).EUt(2).save(provider);

        CANNER_RECIPES.recipeBuilder("sodium_battery_lv")
                .inputItems(BATTERY_HULL_LV)
                .inputItems(DUST, Sodium, 2)
                .outputItems(BATTERY_LV_SODIUM)
                .duration(100).EUt(2).save(provider);

        // MV
        CANNER_RECIPES.recipeBuilder("cadmium_battery_mv")
                .inputItems(BATTERY_HULL_MV)
                .inputItems(DUST, Cadmium, 8)
                .outputItems(BATTERY_MV_CADMIUM)
                .duration(400).EUt(2).save(provider);

        CANNER_RECIPES.recipeBuilder("lithium_battery_mv")
                .inputItems(BATTERY_HULL_MV)
                .inputItems(DUST, Lithium, 8)
                .outputItems(BATTERY_MV_LITHIUM)
                .duration(400).EUt(2).save(provider);

        CANNER_RECIPES.recipeBuilder("sodium_battery_mv")
                .inputItems(BATTERY_HULL_MV)
                .inputItems(DUST, Sodium, 8)
                .outputItems(BATTERY_MV_SODIUM)
                .duration(400).EUt(2).save(provider);

        // HV
        CANNER_RECIPES.recipeBuilder("cadmium_battery_hv")
                .inputItems(BATTERY_HULL_HV)
                .inputItems(DUST, Cadmium, 16)
                .outputItems(BATTERY_HV_CADMIUM)
                .duration(1600).EUt(2).save(provider);

        CANNER_RECIPES.recipeBuilder("lithium_battery_hv")
                .inputItems(BATTERY_HULL_HV)
                .inputItems(DUST, Lithium, 16)
                .outputItems(BATTERY_HV_LITHIUM)
                .duration(1600).EUt(2).save(provider);

        CANNER_RECIPES.recipeBuilder("sodium_battery_hv")
                .inputItems(BATTERY_HULL_HV)
                .inputItems(DUST, Sodium, 16)
                .outputItems(BATTERY_HV_SODIUM)
                .duration(1600).EUt(2).save(provider);

        // EV
        CANNER_RECIPES.recipeBuilder("vanadium_battery_ev")
                .inputItems(BATTERY_HULL_SMALL_VANADIUM)
                .inputItems(DUST, Vanadium, 2)
                .outputItems(BATTERY_EV_VANADIUM)
                .duration(100).EUt(VA[HV]).save(provider);

        // IV
        CANNER_RECIPES.recipeBuilder("vanadium_battery_iv")
                .inputItems(BATTERY_HULL_MEDIUM_VANADIUM)
                .inputItems(DUST, Vanadium, 8)
                .outputItems(BATTERY_IV_VANADIUM)
                .duration(150).EUt(1024).save(provider);

        // LuV
        CANNER_RECIPES.recipeBuilder("vanadium_battery_luv")
                .inputItems(BATTERY_HULL_LARGE_VANADIUM)
                .inputItems(DUST, Vanadium, 16)
                .outputItems(BATTERY_LuV_VANADIUM)
                .duration(200).EUt(VA[EV]).save(provider);

        // ZPM
        CANNER_RECIPES.recipeBuilder("naquadria_battery_zpm")
                .inputItems(BATTERY_HULL_MEDIUM_NAQUADRIA)
                .inputItems(DUST, Naquadria, 8)
                .outputItems(BATTERY_ZPM_NAQUADRIA)
                .duration(250).EUt(4096).save(provider);

        // UV
        CANNER_RECIPES.recipeBuilder("naquadria_battery_uv")
                .inputItems(BATTERY_HULL_LARGE_NAQUADRIA)
                .inputItems(DUST, Naquadria, 16)
                .outputItems(BATTERY_UV_NAQUADRIA)
                .duration(300).EUt(VA[IV]).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("unpackage_lv_cadmium_battery").inputItems(BATTERY_LV_CADMIUM)
                .outputItems(BATTERY_HULL_LV).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_lv_lithium_battery").inputItems(BATTERY_LV_LITHIUM)
                .outputItems(BATTERY_HULL_LV).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_lv_sodium_battery").inputItems(BATTERY_LV_SODIUM)
                .outputItems(BATTERY_HULL_LV).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("unpackage_mv_cadmium_battery").inputItems(BATTERY_MV_CADMIUM)
                .outputItems(BATTERY_HULL_MV).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_mv_lithium_battery").inputItems(BATTERY_MV_LITHIUM)
                .outputItems(BATTERY_HULL_MV).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_mv_sodium_battery").inputItems(BATTERY_MV_SODIUM)
                .outputItems(BATTERY_HULL_MV).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("unpackage_hv_cadmium_battery").inputItems(BATTERY_HV_CADMIUM)
                .outputItems(BATTERY_HULL_HV).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_hv_lithium_battery").inputItems(BATTERY_HV_LITHIUM)
                .outputItems(BATTERY_HULL_HV).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_hv_sodium_battery").inputItems(BATTERY_HV_SODIUM)
                .outputItems(BATTERY_HULL_HV).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("unpackage_ev_vanadium_battery").inputItems(BATTERY_EV_VANADIUM)
                .outputItems(BATTERY_HULL_SMALL_VANADIUM).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_iv_vanadium_battery").inputItems(BATTERY_IV_VANADIUM)
                .outputItems(BATTERY_HULL_MEDIUM_VANADIUM).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_luv_vanadium_battery").inputItems(BATTERY_LuV_VANADIUM)
                .outputItems(BATTERY_HULL_LARGE_VANADIUM).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder("unpackage_zpm_naquadria_battery").inputItems(BATTERY_ZPM_NAQUADRIA)
                .outputItems(BATTERY_HULL_MEDIUM_NAQUADRIA).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder("unpackage_uv_naquadria_battery").inputItems(BATTERY_UV_NAQUADRIA)
                .outputItems(BATTERY_HULL_LARGE_NAQUADRIA).save(provider);
    }

    private static void gemBatteries(Consumer<FinishedRecipe> provider) {
        // Energy Crystal
        MIXER_RECIPES.recipeBuilder("energium_dust")
                .inputItems(DUST, Redstone, 5)
                .inputItems(DUST, Ruby, 4)
                .circuitMeta(1)
                .outputItems(ENERGIUM_DUST, 9)
                .duration(600).EUt(VA[MV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("energy_crystal_water")
                .inputItems(ENERGIUM_DUST, 9)
                .inputFluids(Water.getFluid(1000))
                .outputItems(ENERGIUM_CRYSTAL)
                .duration(1800).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("energy_crystal_distilled")
                .inputItems(ENERGIUM_DUST, 9)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputItems(ENERGIUM_CRYSTAL)
                .duration(1200).EUt(320).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("energy_crystal_black_steel")
                .inputItems(ENERGIUM_DUST, 9)
                .inputFluids(BlackSteel.getFluid(L * 2))
                .outputItems(ENERGIUM_CRYSTAL)
                .duration(300).EUt(256).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("energy_crystal_blue_steel")
                .inputItems(ENERGIUM_DUST, 9)
                .inputFluids(RedSteel.getFluid(L / 2))
                .outputItems(ENERGIUM_CRYSTAL)
                .duration(150).EUt(192).save(provider);

        // Lapotron Crystal
        MIXER_RECIPES.recipeBuilder("lapotron_dust")
                .inputItems(ENERGIUM_DUST, 3)
                .inputItems(DUST, Lapis, 2)
                .circuitMeta(2)
                .outputItems(DUST, Lapotron, 5)
                .duration(200).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("lapotron_gem_water")
                .inputItems(DUST, Lapotron, 15)
                .inputFluids(Water.getFluid(1000))
                .outputItems(GEM, Lapotron)
                .duration(1800).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("lapotron_gem_distilled")
                .inputItems(DUST, Lapotron, 15)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputItems(GEM, Lapotron)
                .duration(1200).EUt(320).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("lapotron_gem_blue_steel")
                .inputItems(DUST, Lapotron, 15)
                .inputFluids(RedSteel.getFluid(L * 2))
                .outputItems(GEM, Lapotron)
                .duration(300).EUt(256).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("lapotron_gem_red_steel")
                .inputItems(DUST, Lapotron, 15)
                .inputFluids(BlueSteel.getFluid(L / 2))
                .outputItems(GEM, Lapotron)
                .duration(150).EUt(192).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("lapotron_crystal")
                .inputItems(GEM, Lapotron)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .outputItems(LAPOTRON_CRYSTAL)
                .duration(600).EUt(VA[EV]).save(provider);

        // Lapotronic Energy Orb
        LASER_ENGRAVER_RECIPES.recipeBuilder("engraved_lapotron_chip")
                .inputItems(LAPOTRON_CRYSTAL)
                .notConsumable(LENS, Color.Blue)
                .outputItems(ENGRAVED_LAPOTRON_CHIP, 3)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(256).EUt(VA[HV]).save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder("lapotronic_energy_orb")
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(POWER_INTEGRATED_CIRCUIT, 4)
                .inputItems(ENGRAVED_LAPOTRON_CHIP, 24)
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT, 2)
                .inputItems(WIRE_FINE, Platinum, 16)
                .inputItems(PLATE, Platinum, 8)
                .outputItems(ENERGY_LAPOTRONIC_ORB)
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(512).EUt(1024).save(provider);

        // Lapotronic Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder("lapotronic_energy_orb_cluster")
                .inputItems(EXTREME_CIRCUIT_BOARD)
                .inputItems(PLATE, Europium, 8)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .inputItems(FIELD_GENERATOR_IV)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 16)
                .inputItems(ADVANCED_SMD_DIODE, 8)
                .inputItems(ADVANCED_SMD_CAPACITOR, 8)
                .inputItems(ADVANCED_SMD_RESISTOR, 8)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 8)
                .inputItems(ADVANCED_SMD_INDUCTOR, 8)
                .inputItems(WIRE_FINE, Platinum, 64)
                .inputItems(BOLT, Naquadah, 16)
                .inputFluids(SolderingAlloy.getFluid(L * 5))
                .outputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .scannerResearch(ENERGY_LAPOTRONIC_ORB.asStack())
                .EUt(80000).duration(1000).save(provider);

        // Energy Module
        ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_module")
                .inputItems(ELITE_CIRCUIT_BOARD)
                .inputItems(PLATE_DOUBLE, Europium, 8)
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .inputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .inputItems(FIELD_GENERATOR_LuV)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItems(ADVANCED_SMD_DIODE, 12)
                .inputItems(ADVANCED_SMD_CAPACITOR, 12)
                .inputItems(ADVANCED_SMD_RESISTOR, 12)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 12)
                .inputItems(ADVANCED_SMD_INDUCTOR, 12)
                .inputItems(WIRE_FINE, Ruridit, 64)
                .inputItems(BOLT, Trinium, 16)
                .inputFluids(SolderingAlloy.getFluid(L * 10))
                .outputItems(ENERGY_MODULE)
                .stationResearch(b -> b
                        .researchStack(ENERGY_LAPOTRONIC_ORB_CLUSTER.asStack())
                        .CWUt(16))
                .EUt(100000).duration(1200).save(provider);

        // Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_cluster")
                .inputItems(WETWARE_CIRCUIT_BOARD)
                .inputItems(PLATE, Americium, 16)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(ENERGY_MODULE)
                .inputItems(FIELD_GENERATOR_ZPM)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 32)
                .inputItems(ADVANCED_SMD_DIODE, 16)
                .inputItems(ADVANCED_SMD_CAPACITOR, 16)
                .inputItems(ADVANCED_SMD_RESISTOR, 16)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 16)
                .inputItems(ADVANCED_SMD_INDUCTOR, 16)
                .inputItems(WIRE_FINE, Osmiridium, 64)
                .inputItems(BOLT, Naquadria, 16)
                .inputFluids(SolderingAlloy.getFluid(L * 20))
                .inputFluids(Polybenzimidazole.getFluid(L * 4))
                .outputItems(ENERGY_CLUSTER)
                .stationResearch(b -> b
                        .researchStack(ENERGY_MODULE.asStack())
                        .CWUt(96)
                        .EUt(VA[ZPM]))
                .EUt(200000).duration(1400).save(provider);

        // Ultimate Battery
        ASSEMBLY_LINE_RECIPES.recipeBuilder("ultimate_battery")
                .inputItems(PLATE_DOUBLE, Darmstadtium, 16)
                .inputItems(CustomTags.UHV_CIRCUITS, 4)
                .inputItems(ENERGY_CLUSTER, 16)
                .inputItems(FIELD_GENERATOR_UV, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .inputItems(ADVANCED_SMD_DIODE, 64)
                .inputItems(ADVANCED_SMD_CAPACITOR, 64)
                .inputItems(ADVANCED_SMD_RESISTOR, 64)
                .inputItems(ADVANCED_SMD_TRANSISTOR, 64)
                .inputItems(ADVANCED_SMD_INDUCTOR, 64)
                .inputItems(WIRE_GT_SINGLE, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(BOLT, Neutronium, 64)
                .inputFluids(SolderingAlloy.getFluid(L * 40))
                .inputFluids(Polybenzimidazole.getFluid(2304))
                .inputFluids(Naquadria.getFluid(L * 18))
                .outputItems(ULTIMATE_BATTERY)
                .stationResearch(b -> b
                        .researchStack(ENERGY_CLUSTER.asStack())
                        .CWUt(144)
                        .EUt(VA[UHV]))
                .EUt(300000).duration(2000).save(provider);
    }

    private static void batteryBlocks(Consumer<FinishedRecipe> provider) {
        // Empty Tier I
        ASSEMBLER_RECIPES.recipeBuilder("empty_tier_1_battery")
                .inputItems(FRAME_GT, Ultimet)
                .inputItems(PLATE, Ultimet, 6)
                .inputItems(SCREW, Ultimet, 24)
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_I)
                .duration(400).EUt(VA[HV]).save(provider);

        // Lapotronic EV
        CANNER_RECIPES.recipeBuilder("ev_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_EMPTY_TIER_I.asStack(1))
                .inputItems(LAPOTRON_CRYSTAL)
                .outputItems(GTBlocks.BATTERY_LAPOTRONIC_EV)
                .duration(200).EUt(VA[HV]).save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_ev_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_LAPOTRONIC_EV.asStack(1))
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_I.asStack(1))
                .outputItems(LAPOTRON_CRYSTAL)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).save(provider);

        // Lapotronic IV
        CANNER_RECIPES.recipeBuilder("iv_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_EMPTY_TIER_I.asStack(1))
                .inputItems(ENERGY_LAPOTRONIC_ORB)
                .outputItems(GTBlocks.BATTERY_LAPOTRONIC_IV.asStack(1))
                .duration(400).EUt(VA[HV]).save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_iv_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_LAPOTRONIC_IV.asStack(1))
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_I)
                .outputItems(ENERGY_LAPOTRONIC_ORB)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).save(provider);

        // Empty Tier II
        ASSEMBLER_RECIPES.recipeBuilder("empty_tier_2_battery")
                .inputItems(FRAME_GT, Ruridit)
                .inputItems(PLATE, Ruridit, 6)
                .inputItems(SCREW, Ruridit, 24)
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_II)
                .duration(400).EUt(VA[IV]).save(provider);

        // Lapotronic LuV
        CANNER_RECIPES.recipeBuilder("luv_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_EMPTY_TIER_II.asStack(1))
                .inputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .outputItems(GTBlocks.BATTERY_LAPOTRONIC_LuV)
                .duration(200).EUt(VA[EV]).save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_luv_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_LAPOTRONIC_LuV.asStack(1))
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_II)
                .outputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).save(provider);

        // Lapotronic ZPM
        CANNER_RECIPES.recipeBuilder("zpm_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_EMPTY_TIER_II.asStack(1))
                .inputItems(ENERGY_MODULE)
                .outputItems(GTBlocks.BATTERY_LAPOTRONIC_ZPM)
                .duration(400).EUt(VA[EV]).save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_zpm_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_LAPOTRONIC_ZPM.asStack(1))
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_II)
                .outputItems(ENERGY_MODULE)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).save(provider);

        // Empty Tier III
        ASSEMBLER_RECIPES.recipeBuilder("empty_tier_3_battery")
                .inputItems(FRAME_GT, Neutronium)
                .inputItems(PLATE, Neutronium, 6)
                .inputItems(SCREW, Neutronium, 24)
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_III)
                .duration(400).EUt(VA[ZPM]).save(provider);

        // Lapotronic UV
        CANNER_RECIPES.recipeBuilder("uv_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_EMPTY_TIER_III.asStack(1))
                .inputItems(ENERGY_CLUSTER)
                .outputItems(GTBlocks.BATTERY_LAPOTRONIC_UV)
                .duration(200).EUt(VA[IV]).save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_uv_lapotronic_battery")
                .inputItems(GTBlocks.BATTERY_LAPOTRONIC_UV.asStack(1))
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_III)
                .outputItems(ENERGY_CLUSTER)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).save(provider);

        // Ultimate UHV
        CANNER_RECIPES.recipeBuilder("uhv_ultimate_battery")
                .inputItems(GTBlocks.BATTERY_EMPTY_TIER_III.asStack(1))
                .inputItems(ULTIMATE_BATTERY)
                .outputItems(GTBlocks.BATTERY_ULTIMATE_UHV)
                .duration(400).EUt(VA[IV]).save(provider);

        PACKER_RECIPES.recipeBuilder("unpackage_uhv_ultimate_battery")
                .inputItems(GTBlocks.BATTERY_ULTIMATE_UHV.asStack(1))
                .outputItems(GTBlocks.BATTERY_EMPTY_TIER_III)
                .outputItems(ULTIMATE_BATTERY)
                .circuitMeta(2)
                .duration(200).EUt(VA[LV]).save(provider);
    }
}
