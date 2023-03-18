package com.lowdragmc.gtceu.data.recipe.misc;

import com.lowdragmc.gtceu.api.data.chemical.ChemicalHelper;
import com.lowdragmc.gtceu.api.data.chemical.material.MarkerMaterials;
import com.lowdragmc.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.lowdragmc.gtceu.api.machine.multiblock.CleanroomType;
import com.lowdragmc.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.GTValues.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
import static com.lowdragmc.gtceu.common.libs.GTItems.*;

public class BatteryRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        standardBatteries(provider);
        gemBatteries(provider);
    }

    private static void standardBatteries(Consumer<FinishedRecipe> provider) {

        // Tantalum Battery (since it doesn't fit elsewhere)
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_ULV_TANTALUM.getId())
                .inputItems(dust, Tantalum)
                .inputItems(foil, Manganese)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(BATTERY_ULV_TANTALUM.asStack(8))
                .duration(30).EUt(4).save(provider);

        // :trol:
        VanillaRecipeHelper.addShapedRecipe(provider, "tantalum_capacitor", BATTERY_ULV_TANTALUM.asStack(2),
                " F ", "FDF", "B B",
                'F', new UnificationEntry(foil, Manganese),
                'D', new UnificationEntry(dust, Tantalum),
                'B', new UnificationEntry(bolt, Iron));

        // Battery Hull Recipes

        // LV
        VanillaRecipeHelper.addShapedRecipe(provider, "battery_hull_lv", BATTERY_HULL_LV.asStack(),
                "C", "P", "P",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_LV.getId())
                .inputItems(cableGtSingle, Tin)
                .inputItems(plate, BatteryAlloy)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(BATTERY_HULL_LV.asStack())
                .duration(400).EUt(1).save(provider);

        // MV
        VanillaRecipeHelper.addShapedRecipe(provider, "battery_hull_mv", BATTERY_HULL_MV.asStack(),
                "C C", "PPP", "PPP",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', new UnificationEntry(plate, BatteryAlloy));

        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_MV.getId().getPath() + ".0")
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(plate, BatteryAlloy, 3)
                .inputFluids(Polyethylene.getFluid(L * 3))
                .outputItems(BATTERY_HULL_MV.asStack())
                .duration(200).EUt(2).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_MV.getId().getPath() + ".1")
                .inputItems(cableGtSingle, AnnealedCopper, 2)
                .inputItems(plate, BatteryAlloy, 3)
                .inputFluids(Polyethylene.getFluid(L * 3))
                .outputItems(BATTERY_HULL_MV.asStack())
                .duration(200).EUt(2).save(provider);

        // HV
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_HV.getId()).duration(300).EUt(4)
                .inputItems(cableGtSingle, Gold, 4)
                .inputItems(plate, BatteryAlloy, 9)
                .inputFluids(Polyethylene.getFluid(1296))
                .outputItems(BATTERY_HULL_HV.asStack())
                .save(provider);

        // EV
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_SMALL_VANADIUM.getId()).duration(100).EUt(VA[HV])
                .inputItems(cableGtSingle, Aluminium, 2)
                .inputItems(plate, BlueSteel, 2)
                .inputFluids(Polytetrafluoroethylene.getFluid(144))
                .outputItems(BATTERY_HULL_SMALL_VANADIUM.asStack())
                .save(provider);

        // IV
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_MEDIUM_VANADIUM.getId()).duration(200).EUt(VA[EV])
                .inputItems(cableGtSingle, Platinum, 2)
                .inputItems(plate, RoseGold, 6)
                .inputFluids(Polytetrafluoroethylene.getFluid(288))
                .outputItems(BATTERY_HULL_MEDIUM_VANADIUM.asStack())
                .save(provider);

        // LuV
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_LARGE_VANADIUM.getId()).duration(300).EUt(VA[IV])
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
                .inputItems(plate, RedSteel, 18)
                .inputFluids(Polybenzimidazole.getFluid(144))
                .outputItems(BATTERY_HULL_LARGE_VANADIUM.asStack())
                .save(provider);

        // ZPM
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_MEDIUM_NAQUADRIA.getId()).duration(200).EUt(VA[LuV])
                .inputItems(cableGtSingle, Naquadah, 2)
                .inputItems(plate, Europium, 6)
                .inputFluids(Polybenzimidazole.getFluid(288))
                .outputItems(BATTERY_HULL_MEDIUM_NAQUADRIA.asStack())
                .save(provider);

        // UV
        ASSEMBLER_RECIPES.recipeBuilder(BATTERY_HULL_LARGE_NAQUADRIA.getId()).duration(300).EUt(VA[ZPM])
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
                .inputItems(plate, Americium, 18)
                .inputFluids(Polybenzimidazole.getFluid(576))
                .outputItems(BATTERY_HULL_LARGE_NAQUADRIA.asStack())
                .save(provider);

        // Battery Filling Recipes

        // LV
        CANNER_RECIPES.recipeBuilder(BATTERY_LV_CADMIUM.getId()).duration(100).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_LV.get()))
                .inputItems(dust, Cadmium, 2)
                .outputItems(BATTERY_LV_CADMIUM.asStack())
                .save(provider);

        CANNER_RECIPES.recipeBuilder(BATTERY_LV_LITHIUM.getId()).duration(100).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_LV.get()))
                .inputItems(dust, Lithium, 2)
                .outputItems(BATTERY_LV_LITHIUM.asStack())
                .save(provider);

        CANNER_RECIPES.recipeBuilder(BATTERY_LV_SODIUM.getId()).duration(100).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_LV.get()))
                .inputItems(dust, Sodium, 2)
                .outputItems(BATTERY_LV_SODIUM.asStack())
                .save(provider);

        // MV
        CANNER_RECIPES.recipeBuilder(BATTERY_MV_CADMIUM.getId()).duration(400).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_MV.get()))
                .inputItems(dust, Cadmium, 8)
                .outputItems(BATTERY_MV_CADMIUM.asStack())
                .save(provider);

        CANNER_RECIPES.recipeBuilder(BATTERY_MV_LITHIUM.getId()).duration(400).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_MV.get()))
                .inputItems(dust, Lithium, 8)
                .outputItems(BATTERY_MV_LITHIUM.asStack())
                .save(provider);

        CANNER_RECIPES.recipeBuilder(BATTERY_MV_SODIUM.getId()).duration(400).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_MV.get()))
                .inputItems(dust, Sodium, 8)
                .outputItems(BATTERY_MV_SODIUM.asStack())
                .save(provider);

        // HV
        CANNER_RECIPES.recipeBuilder(BATTERY_HV_CADMIUM.getId()).duration(1600).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_HV.get()))
                .inputItems(dust, Cadmium, 16)
                .outputItems(BATTERY_HV_CADMIUM.asStack())
                .save(provider);

        CANNER_RECIPES.recipeBuilder(BATTERY_HV_LITHIUM.getId()).duration(1600).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_HV.get()))
                .inputItems(dust, Lithium, 16)
                .outputItems(BATTERY_HV_LITHIUM.asStack())
                .save(provider);

        CANNER_RECIPES.recipeBuilder(BATTERY_HV_SODIUM.getId()).duration(1600).EUt(2)
                .inputItems(Ingredient.of(BATTERY_HULL_HV.get()))
                .inputItems(dust, Sodium, 16)
                .outputItems(BATTERY_HV_SODIUM.asStack())
                .save(provider);

        // EV
        CANNER_RECIPES.recipeBuilder(BATTERY_EV_VANADIUM.getId()).duration(100).EUt(VA[HV])
                .inputItems(Ingredient.of(BATTERY_HULL_SMALL_VANADIUM.get()))
                .inputItems(dust, Vanadium, 2)
                .outputItems(BATTERY_EV_VANADIUM.asStack())
                .save(provider);

        // IV
        CANNER_RECIPES.recipeBuilder(BATTERY_IV_VANADIUM.getId()).duration(150).EUt(1024)
                .inputItems(Ingredient.of(BATTERY_HULL_MEDIUM_VANADIUM.get()))
                .inputItems(dust, Vanadium, 8)
                .outputItems(BATTERY_IV_VANADIUM.asStack())
                .save(provider);

        // LuV
        CANNER_RECIPES.recipeBuilder(BATTERY_LUV_VANADIUM.getId()).duration(200).EUt(VA[EV])
                .inputItems(Ingredient.of(BATTERY_HULL_LARGE_VANADIUM.get()))
                .inputItems(dust, Vanadium, 16)
                .outputItems(BATTERY_LUV_VANADIUM.asStack())
                .save(provider);

        // ZPM
        CANNER_RECIPES.recipeBuilder(BATTERY_ZPM_NAQUADRIA.getId()).duration(250).EUt(4096)
                .inputItems(Ingredient.of(BATTERY_HULL_MEDIUM_NAQUADRIA.get()))
                .inputItems(dust, Naquadria, 8)
                .outputItems(BATTERY_ZPM_NAQUADRIA.asStack())
                .save(provider);

        // UV
        CANNER_RECIPES.recipeBuilder(BATTERY_UV_NAQUADRIA.getId()).duration(300).EUt(VA[IV])
                .inputItems(Ingredient.of(BATTERY_HULL_LARGE_NAQUADRIA.get()))
                .inputItems(dust, Naquadria, 16)
                .outputItems(BATTERY_UV_NAQUADRIA.asStack())
                .save(provider);


        // Battery Recycling Recipes
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_LV.getId().getPath() + ".0").inputItems(Ingredient.of(BATTERY_LV_CADMIUM.get())).outputItems(BATTERY_HULL_LV.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_LV.getId().getPath() + ".1").inputItems(Ingredient.of(BATTERY_LV_LITHIUM.get())).outputItems(BATTERY_HULL_LV.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_LV.getId().getPath() + ".2").inputItems(Ingredient.of(BATTERY_LV_SODIUM.get())).outputItems(BATTERY_HULL_LV.asStack()).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_MV.getId().getPath() + ".0").inputItems(Ingredient.of(BATTERY_MV_CADMIUM.get())).outputItems(BATTERY_HULL_MV.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_MV.getId().getPath() + ".1").inputItems(Ingredient.of(BATTERY_MV_LITHIUM.get())).outputItems(BATTERY_HULL_MV.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_MV.getId().getPath() + ".2").inputItems(Ingredient.of(BATTERY_MV_SODIUM.get())).outputItems(BATTERY_HULL_MV.asStack()).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_HV.getId().getPath() + ".0").inputItems(Ingredient.of(BATTERY_HV_CADMIUM.get())).outputItems(BATTERY_HULL_HV.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_HV.getId().getPath() + ".1").inputItems(Ingredient.of(BATTERY_HV_LITHIUM.get())).outputItems(BATTERY_HULL_HV.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_HV.getId().getPath() + ".2").inputItems(Ingredient.of(BATTERY_HV_SODIUM.get())).outputItems(BATTERY_HULL_HV.asStack()).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_SMALL_VANADIUM.getId().getPath() + ".0").inputItems(Ingredient.of(BATTERY_EV_VANADIUM.get())).outputItems(BATTERY_HULL_SMALL_VANADIUM.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_SMALL_VANADIUM.getId().getPath() + ".1").inputItems(Ingredient.of(BATTERY_IV_VANADIUM.get())).outputItems(BATTERY_HULL_MEDIUM_VANADIUM.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_SMALL_VANADIUM.getId().getPath() + ".2").inputItems(Ingredient.of(BATTERY_LUV_VANADIUM.get())).outputItems(BATTERY_HULL_LARGE_VANADIUM.asStack()).save(provider);

        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_MEDIUM_NAQUADRIA.getId().getPath() + ".0").inputItems(Ingredient.of(BATTERY_ZPM_NAQUADRIA.get())).outputItems(BATTERY_HULL_MEDIUM_NAQUADRIA.asStack()).save(provider);
        EXTRACTOR_RECIPES.recipeBuilder(BATTERY_HULL_MEDIUM_NAQUADRIA.getId().getPath() + ".1").inputItems(Ingredient.of(BATTERY_UV_NAQUADRIA.get())).outputItems(BATTERY_HULL_LARGE_NAQUADRIA.asStack()).save(provider);
    }

    private static void gemBatteries(Consumer<FinishedRecipe> provider) {

        // Energy Crystal
        MIXER_RECIPES.recipeBuilder(ENERGIUM_DUST.getId()).duration(600).EUt(VA[MV])
                .inputItems(dust, Redstone, 5)
                .inputItems(dust, Ruby, 4)
                .circuitMeta(1)
                .outputItems(ENERGIUM_DUST.asStack(9))
                .save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(ENERGIUM_CRYSTAL.getId().getPath() + ".0")
                .inputItems(ENERGIUM_DUST.asStack(9))
                .inputFluids(Water.getFluid(1000))
                .outputItems(ENERGIUM_CRYSTAL.asStack())
                .duration(1800).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(ENERGIUM_CRYSTAL.getId().getPath() + ".1")
                .inputItems(ENERGIUM_DUST.asStack(9))
                .inputFluids(DistilledWater.getFluid(1000))
                .outputItems(ENERGIUM_CRYSTAL.asStack())
                .duration(1200).EUt(320).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(ENERGIUM_CRYSTAL.getId().getPath() + ".2")
                .inputItems(ENERGIUM_DUST.asStack(9))
                .inputFluids(BlackSteel.getFluid(L * 2))
                .outputItems(ENERGIUM_CRYSTAL.asStack())
                .duration(300).EUt(256).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder(ENERGIUM_CRYSTAL.getId().getPath() + ".3")
                .inputItems(ENERGIUM_DUST.asStack(9))
                .inputFluids(BlueSteel.getFluid(L / 2))
                .outputItems(ENERGIUM_CRYSTAL.asStack())
                .duration(150).EUt(192).save(provider);

        // Lapotron Crystal
        MIXER_RECIPES.recipeBuilder("gem_" + Lapotron.getName() + ".0")
                .inputItems(ENERGIUM_DUST.asStack(3))
                .inputItems(dust, Lapis, 2)
                .circuitMeta(2)
                .outputItems(dust, Lapotron, 5)
                .duration(200).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("gem_" + Lapotron.getName() + ".1")
                .inputItems(dust, Lapotron, 15)
                .inputFluids(Water.getFluid(1000))
                .outputItems(gem, Lapotron)
                .duration(1800).EUt(VA[HV]).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("gem_" + Lapotron.getName() + ".2")
                .inputItems(dust, Lapotron, 15)
                .inputFluids(DistilledWater.getFluid(1000))
                .outputItems(gem, Lapotron)
                .duration(1200).EUt(320).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("gem_" + Lapotron.getName() + ".3")
                .inputItems(dust, Lapotron, 15)
                .inputFluids(BlueSteel.getFluid(L * 2))
                .outputItems(gem, Lapotron)
                .duration(300).EUt(256).save(provider);

        AUTOCLAVE_RECIPES.recipeBuilder("gem_" + Lapotron.getName() + ".4")
                .inputItems(dust, Lapotron, 15)
                .inputFluids(RedSteel.getFluid(L / 2))
                .outputItems(gem, Lapotron)
                .duration(150).EUt(192).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(LAPOTRON_CRYSTAL.getId())
                .inputItems(gem, Lapotron)
                .inputItems(circuit, MarkerMaterials.Tier.HV, 2)
                .outputItems(LAPOTRON_CRYSTAL.asStack())
                .duration(600).EUt(VA[EV]).save(provider);

        // Lapotronic Energy Orb
        LASER_ENGRAVER_RECIPES.recipeBuilder(ENGRAVED_LAPOTRON_CHIP.getId())
                .inputItems(LAPOTRON_CRYSTAL.asStack())
                .notConsumable(ChemicalHelper.get(craftingLens, MarkerMaterials.Color.Blue))
                .outputItems(ENGRAVED_LAPOTRON_CHIP.asStack(3))
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(256).EUt(VA[HV]).save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(ENERGY_LAPOTRONIC_ORB.getId()).duration(512).EUt(1024)
                .inputItems(EXTREME_CIRCUIT_BOARD.asStack())
                .inputItems(POWER_INTEGRATED_CIRCUIT.asStack(4))
                .inputItems(ENGRAVED_LAPOTRON_CHIP.asStack(24))
                .inputItems(NANO_CENTRAL_PROCESSING_UNIT.asStack(2))
                .inputItems(wireFine, Platinum, 16)
                .inputItems(plate, Platinum, 8)
                .outputItems(ENERGY_LAPOTRONIC_ORB.asStack())
                .solderMultiplier(2)
                .cleanroom(CleanroomType.CLEANROOM)
                .save(provider);

        // Lapotronic Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder(ENERGY_LAPOTRONIC_ORB_CLUSTER.getId()).EUt(80000).duration(1000)
                .inputItems(EXTREME_CIRCUIT_BOARD.asStack())
                .inputItems(plate, Europium, 8)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 4)
                .inputItems(Ingredient.of(ENERGY_LAPOTRONIC_ORB.get()))
                .inputItems(Ingredient.of(FIELD_GENERATOR_IV.get()))
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT.asStack(16))
                .inputItems(ADVANCED_SMD_DIODE.asStack(8))
                .inputItems(ADVANCED_SMD_CAPACITOR.asStack(8))
                .inputItems(ADVANCED_SMD_RESISTOR.asStack(8))
                .inputItems(ADVANCED_SMD_TRANSISTOR.asStack(8))
                .inputItems(ADVANCED_SMD_INDUCTOR.asStack(8))
                .inputItems(wireFine, Platinum, 64)
                .inputItems(bolt, Naquadah, 16)
                .inputFluids(SolderingAlloy.getFluid(L * 5))
                .outputItems(ENERGY_LAPOTRONIC_ORB_CLUSTER.asStack())
                .save(provider);

        // Energy Module
        ASSEMBLY_LINE_RECIPES.recipeBuilder(ENERGY_MODULE.getId()).EUt(100000).duration(1200)
                .inputItems(Ingredient.of(ELITE_CIRCUIT_BOARD.get()))
                .inputItems(plateDouble, Europium, 8)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM, 4)
                .inputItems(Ingredient.of(ENERGY_LAPOTRONIC_ORB_CLUSTER.get()))
                .inputItems(Ingredient.of(FIELD_GENERATOR_LuV.get()))
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT.asStack(32))
                .inputItems(ADVANCED_SMD_DIODE.asStack(12))
                .inputItems(ADVANCED_SMD_CAPACITOR.asStack(12))
                .inputItems(ADVANCED_SMD_RESISTOR.asStack(12))
                .inputItems(ADVANCED_SMD_TRANSISTOR.asStack(12))
                .inputItems(ADVANCED_SMD_INDUCTOR.asStack(12))
                .inputItems(wireFine, Ruridit, 64)
                .inputItems(bolt, Trinium, 16)
                .inputFluids(SolderingAlloy.getFluid(L * 10))
                .outputItems(ENERGY_MODULE.asStack())
                .save(provider);

        // Energy Cluster
        ASSEMBLY_LINE_RECIPES.recipeBuilder(ENERGY_CLUSTER.getId()).EUt(200000).duration(1400)
                .inputItems(Ingredient.of(WETWARE_CIRCUIT_BOARD.get()))
                .inputItems(plate, Americium, 16)
                .inputItems(WETWARE_SUPER_COMPUTER_UV.asStack(4))
                .inputItems(Ingredient.of(ENERGY_MODULE.get()))
                .inputItems(Ingredient.of(FIELD_GENERATOR_ZPM.get()))
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.asStack(32))
                .inputItems(ADVANCED_SMD_DIODE.asStack(16))
                .inputItems(ADVANCED_SMD_CAPACITOR.asStack(16))
                .inputItems(ADVANCED_SMD_RESISTOR.asStack(16))
                .inputItems(ADVANCED_SMD_TRANSISTOR.asStack(16))
                .inputItems(ADVANCED_SMD_INDUCTOR.asStack(16))
                .inputItems(wireFine, Osmiridium, 64)
                .inputItems(bolt, Naquadria, 16)
                .inputFluids(SolderingAlloy.getFluid(L * 20))
                .inputFluids(Polybenzimidazole.getFluid(L * 4))
                .outputItems(ENERGY_CLUSTER.asStack())
                .save(provider);

        // TODO do we really need UHV+?
        // Ultimate Battery
//        ASSEMBLY_LINE_RECIPES.recipeBuilder(ULTIMATE_BATTERY.getId()).EUt(300000).duration(2000)
//                .inputItems(plateDouble, Darmstadtium, 16)
//                .inputItems(circuit, MarkerMaterials.Tier.UHV, 4)
//                .inputItems(ENERGY_CLUSTER.asStack(16))
//                .inputItems(FIELD_GENERATOR_UV.asStack(4))
//                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER.asStack(64))
//                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER.asStack(64))
//                .inputItems(ADVANCED_SMD_DIODE.asStack(64))
//                .inputItems(ADVANCED_SMD_CAPACITOR.asStack(64))
//                .inputItems(ADVANCED_SMD_RESISTOR.asStack(64))
//                .inputItems(ADVANCED_SMD_TRANSISTOR.asStack(64))
//                .inputItems(ADVANCED_SMD_INDUCTOR.asStack(64))
//                .inputItems(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 64)
//                .inputItems(bolt, Neutronium, 64)
//                .inputFluids(SolderingAlloy.getFluid(L * 40))
//                .inputFluids(Polybenzimidazole.getFluid(2304))
//                .inputFluids(Naquadria.getFluid(L * 18))
//                .outputItems(ULTIMATE_BATTERY.asStack())
//                .save(provider);
    }
}
