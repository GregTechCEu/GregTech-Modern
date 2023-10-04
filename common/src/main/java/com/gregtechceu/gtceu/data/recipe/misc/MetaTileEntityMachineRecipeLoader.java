package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LD_FLUID_PIPE;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LD_ITEM_PIPE;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES;

public class MetaTileEntityMachineRecipeLoader {

    public static void init(Consumer<FinishedRecipe> provider) {

        // Energy Output Hatches

        VanillaRecipeHelper.addShapedRecipe(provider, true, "dynamo_hatch_ulv", ENERGY_OUTPUT_HATCH[ULV].asStack(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Lead),
                'V', VOLTAGE_COIL_ULV.asStack(),
                'H', HULL[ULV].asStack());

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_ulv")
                .inputItems(HULL[ULV])
                .inputItems(spring, Lead, 2)
                .inputItems(VOLTAGE_COIL_ULV)
                .outputItems(ENERGY_OUTPUT_HATCH[ULV])
                .duration(200).EUt(VA[ULV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "dynamo_hatch_lv", ENERGY_OUTPUT_HATCH[LV].asStack(),
                " V ", "SHS", "   ",
                'S', new UnificationEntry(spring, Tin),
                'V', VOLTAGE_COIL_LV.asStack(),
                'H', HULL[LV].asStack());

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_lv")
                .inputItems(HULL[LV])
                .inputItems(spring, Tin, 2)
                .inputItems(VOLTAGE_COIL_LV)
                .outputItems(ENERGY_OUTPUT_HATCH[LV])
                .duration(200).EUt(VA[LV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "dynamo_hatch_mv", ENERGY_OUTPUT_HATCH[MV].asStack(),
                " V ", "SHS", " P ",
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack(),
                'S', new UnificationEntry(spring, Copper),
                'V', VOLTAGE_COIL_MV.asStack(),
                'H', HULL[MV].asStack());

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_mv")
                .inputItems(HULL[MV])
                .inputItems(spring, Copper, 2)
                .inputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_MV)
                .outputItems(ENERGY_OUTPUT_HATCH[MV])
                .duration(200).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_hv")
                .inputItems(HULL[HV])
                .inputItems(spring, Gold, 2)
                .inputItems(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_HV)
                .inputFluids(SodiumPotassium.getFluid(1000))
                .outputItems(ENERGY_OUTPUT_HATCH[HV])
                .duration(200).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_ev")
                .inputItems(HULL[EV])
                .inputItems(spring, Aluminium, 2)
                .inputItems(POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_EV)
                .inputFluids(SodiumPotassium.getFluid(2000))
                .outputItems(ENERGY_OUTPUT_HATCH[EV])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_iv")
                .inputItems(HULL[IV])
                .inputItems(spring, Tungsten, 2)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_IV)
                .inputFluids(SodiumPotassium.getFluid(3000))
                .outputItems(ENERGY_OUTPUT_HATCH[IV])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("dynamo_hatch_luv")
                .inputItems(HULL[LuV])
                .inputItems(spring, NiobiumTitanium, 4)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(VOLTAGE_COIL_LuV, 2)
                .inputFluids(SodiumPotassium.getFluid(6000))
                .inputFluids(SolderingAlloy.getFluid(720))
                .outputItems(ENERGY_OUTPUT_HATCH[LuV])
                .duration(400).EUt(VA[LuV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("dynamo_hatch_zpm")
                .inputItems(HULL[ZPM])
                .inputItems(spring, VanadiumGallium, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(VOLTAGE_COIL_ZPM, 2)
                .inputFluids(SodiumPotassium.getFluid(8000))
                .inputFluids(SolderingAlloy.getFluid(1440))
                .outputItems(ENERGY_OUTPUT_HATCH[ZPM])
                .duration(600).EUt(VA[ZPM]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("dynamo_hatch_uv")
                .inputItems(HULL[UV])
                .inputItems(spring, YttriumBariumCuprate, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.UV_CIRCUITS)
                .inputItems(VOLTAGE_COIL_UV, 2)
                .inputFluids(SodiumPotassium.getFluid(10000))
                .inputFluids(SolderingAlloy.getFluid(2880))
                .outputItems(ENERGY_OUTPUT_HATCH[UV])
                .duration(800).EUt(VA[UV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("dynamo_hatch_uhv")
                .inputItems(HULL[UHV])
                .inputItems(spring, Europium, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.UHV_CIRCUITS)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .inputFluids(SodiumPotassium.getFluid(12000))
                .inputFluids(SolderingAlloy.getFluid(5760))
                .outputItems(ENERGY_OUTPUT_HATCH[UHV])
                .duration(1000).EUt(VA[UHV]).save(provider);

        // Energy Input Hatches

        VanillaRecipeHelper.addShapedRecipe(provider, true, "energy_hatch_ulv", ENERGY_INPUT_HATCH[ULV].asStack(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, RedAlloy),
                'V', VOLTAGE_COIL_ULV.asStack(),
                'H', HULL[ULV].asStack());

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_ulv")
                .inputItems(HULL[ULV])
                .inputItems(cableGtSingle, RedAlloy, 2)
                .inputItems(VOLTAGE_COIL_ULV)
                .outputItems(ENERGY_INPUT_HATCH[ULV])
                .duration(200).EUt(VA[ULV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "energy_hatch_lv", ENERGY_INPUT_HATCH[LV].asStack(),
                " V ", "CHC", "   ",
                'C', new UnificationEntry(cableGtSingle, Tin),
                'V', VOLTAGE_COIL_LV.asStack(),
                'H', HULL[LV].asStack());

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_lv")
                .inputItems(HULL[LV])
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(VOLTAGE_COIL_LV)
                .outputItems(ENERGY_INPUT_HATCH[LV])
                .duration(200).EUt(VA[LV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "energy_hatch_mv", ENERGY_INPUT_HATCH[MV].asStack(),
                " V ", "CHC", " P ",
                'C', new UnificationEntry(cableGtSingle, Copper),
                'P', ULTRA_LOW_POWER_INTEGRATED_CIRCUIT.asStack(),
                'V', VOLTAGE_COIL_MV.asStack(),
                'H', HULL[MV].asStack());

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_mv")
                .inputItems(HULL[MV])
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(ULTRA_LOW_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_MV)
                .outputItems(ENERGY_INPUT_HATCH[MV])
                .duration(200).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_hv")
                .inputItems(HULL[HV])
                .inputItems(cableGtSingle, Gold, 2)
                .inputItems(LOW_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_HV)
                .inputFluids(SodiumPotassium.getFluid(1000))
                .outputItems(ENERGY_INPUT_HATCH[HV])
                .duration(200).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_ev")
                .inputItems(HULL[EV])
                .inputItems(cableGtSingle, Aluminium, 2)
                .inputItems(POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_EV)
                .inputFluids(SodiumPotassium.getFluid(2000))
                .outputItems(ENERGY_INPUT_HATCH[EV])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_iv")
                .inputItems(HULL[IV])
                .inputItems(cableGtSingle, Tungsten, 2)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_IV)
                .inputFluids(SodiumPotassium.getFluid(3000))
                .outputItems(ENERGY_INPUT_HATCH[IV])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_hatch_luv")
                .inputItems(HULL[LuV])
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(VOLTAGE_COIL_LuV, 2)
                .inputFluids(SodiumPotassium.getFluid(6000))
                .inputFluids(SolderingAlloy.getFluid(720))
                .outputItems(ENERGY_INPUT_HATCH[LuV])
                .duration(400).EUt(VA[LuV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_hatch_zpm")
                .inputItems(HULL[ZPM])
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(VOLTAGE_COIL_ZPM, 2)
                .inputFluids(SodiumPotassium.getFluid(8000))
                .inputFluids(SolderingAlloy.getFluid(1440))
                .outputItems(ENERGY_INPUT_HATCH[ZPM])
                .duration(600).EUt(VA[ZPM]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_hatch_uv")
                .inputItems(HULL[UV])
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.UV_CIRCUITS)
                .inputItems(VOLTAGE_COIL_UV, 2)
                .inputFluids(SodiumPotassium.getFluid(10000))
                .inputFluids(SolderingAlloy.getFluid(2880))
                .outputItems(ENERGY_INPUT_HATCH[UV])
                .duration(800).EUt(VA[UV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("energy_hatch_uhv")
                .inputItems(HULL[UHV])
                .inputItems(cableGtSingle, Europium, 4)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(CustomTags.UHV_CIRCUITS)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate, 2)
                .inputFluids(SodiumPotassium.getFluid(12000))
                .inputFluids(SolderingAlloy.getFluid(5760))
                .outputItems(ENERGY_INPUT_HATCH[UHV])
                .duration(1000).EUt(VA[UHV]).save(provider);


        // Adjustable Transformers
        // TODO Adjustable transformers
/*
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[ULV])
                .inputItems(ELECTRIC_PUMP_LV)
                .inputItems(wireGtQuadruple, Tin)
                .inputItems(wireGtOctal, Lead)
                .inputItems(springSmall, Lead)
                .inputItems(spring, Tin)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[ULV])
                .duration(200).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[LV])
                .inputItems(ELECTRIC_PUMP_LV)
                .inputItems(wireGtQuadruple, Copper)
                .inputItems(wireGtOctal, Tin)
                .inputItems(springSmall, Tin)
                .inputItems(spring, Copper)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[LV])
                .duration(200).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[MV])
                .inputItems(ELECTRIC_PUMP_MV)
                .inputItems(wireGtQuadruple, Gold)
                .inputItems(wireGtOctal, Copper)
                .inputItems(springSmall, Copper)
                .inputItems(spring, Gold)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[MV])
                .duration(200).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[HV])
                .inputItems(ELECTRIC_PUMP_MV)
                .inputItems(wireGtQuadruple, Aluminium)
                .inputItems(wireGtOctal, Gold)
                .inputItems(springSmall, Gold)
                .inputItems(spring, Aluminium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[HV])
                .duration(200).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[EV])
                .inputItems(ELECTRIC_PUMP_HV)
                .inputItems(wireGtQuadruple, Tungsten)
                .inputItems(wireGtOctal, Aluminium)
                .inputItems(springSmall, Aluminium)
                .inputItems(spring, Tungsten)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[EV])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[IV])
                .inputItems(ELECTRIC_PUMP_HV)
                .inputItems(wireGtQuadruple, NiobiumTitanium)
                .inputItems(wireGtOctal, Tungsten)
                .inputItems(springSmall, Tungsten)
                .inputItems(spring, NiobiumTitanium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[IV])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[LuV])
                .inputItems(ELECTRIC_PUMP_EV)
                .inputItems(wireGtQuadruple, VanadiumGallium)
                .inputItems(wireGtOctal, NiobiumTitanium)
                .inputItems(springSmall, NiobiumTitanium)
                .inputItems(spring, VanadiumGallium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[LuV])
                .duration(200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[ZPM])
                .inputItems(ELECTRIC_PUMP_EV)
                .inputItems(wireGtQuadruple, YttriumBariumCuprate)
                .inputItems(wireGtOctal, VanadiumGallium)
                .inputItems(springSmall, VanadiumGallium)
                .inputItems(spring, YttriumBariumCuprate)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[ZPM])
                .duration(200).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(TRANSFORMER[UV])
                .inputItems(ELECTRIC_PUMP_IV)
                .inputItems(wireGtQuadruple, Europium)
                .inputItems(wireGtOctal, YttriumBariumCuprate)
                .inputItems(springSmall, YttriumBariumCuprate)
                .inputItems(spring, Europium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(ADJUSTABLE_TRANSFORMER[UV])
                .duration(200).EUt(VA[UV]).save(provider);
*/

        // 4A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_4a_ev")
                .inputItems(TRANSFORMER[EV])
                .inputItems(ENERGY_INPUT_HATCH[EV])
                .inputItems(POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_EV)
                .inputItems(wireGtQuadruple, Aluminium, 2)
                .outputItems(ENERGY_INPUT_HATCH_4A[EV])
                .duration(100).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_4a_iv")
                .inputItems(TRANSFORMER[IV])
                .inputItems(ENERGY_INPUT_HATCH[IV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_IV)
                .inputItems(wireGtQuadruple, Tungsten, 2)
                .outputItems(ENERGY_INPUT_HATCH_4A[IV])
                .duration(100).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_4a_luv")
                .inputItems(TRANSFORMER[LuV])
                .inputItems(ENERGY_INPUT_HATCH[LuV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_LuV)
                .inputItems(wireGtQuadruple, NiobiumTitanium, 2)
                .outputItems(ENERGY_INPUT_HATCH_4A[LuV])
                .duration(100).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_4a_zpm")
                .inputItems(TRANSFORMER[ZPM])
                .inputItems(ENERGY_INPUT_HATCH[ZPM])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_ZPM)
                .inputItems(wireGtQuadruple, VanadiumGallium, 2)
                .outputItems(ENERGY_INPUT_HATCH_4A[ZPM])
                .duration(100).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_4a_uv")
                .inputItems(TRANSFORMER[UV])
                .inputItems(ENERGY_INPUT_HATCH[UV])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_UV)
                .inputItems(wireGtQuadruple, YttriumBariumCuprate, 2)
                .outputItems(ENERGY_INPUT_HATCH_4A[UV])
                .duration(100).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_4a_uhv")
                .inputItems(ENERGY_INPUT_HATCH[UHV], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .inputItems(wireGtQuadruple, Europium, 2)
                .outputItems(ENERGY_INPUT_HATCH_4A[UHV])
                .duration(100).EUt(VA[UV]).save(provider);

        // 16A Energy Hatches

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_16a_ev")
                .inputItems(ENERGY_INPUT_HATCH_4A[EV], 2)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_IV)
                .inputItems(wireGtOctal, Tungsten, 2)
                .outputItems(ENERGY_INPUT_HATCH_16A[EV])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_16a_iv")
                .inputItems(ENERGY_INPUT_HATCH_4A[IV], 2)
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_LuV)
                .inputItems(wireGtOctal, NiobiumTitanium, 2)
                .outputItems(ENERGY_INPUT_HATCH_16A[IV])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_16a_luv")
                .inputItems(ENERGY_INPUT_HATCH_4A[LuV], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_ZPM)
                .inputItems(wireGtOctal, VanadiumGallium, 2)
                .outputItems(ENERGY_INPUT_HATCH_16A[LuV])
                .duration(200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_16a_zpm")
                .inputItems(ENERGY_INPUT_HATCH_4A[ZPM], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_UV)
                .inputItems(wireGtOctal, YttriumBariumCuprate, 2)
                .outputItems(ENERGY_INPUT_HATCH_16A[ZPM])
                .duration(200).EUt(VA[ZPM]).save(provider);


        ASSEMBLER_RECIPES.recipeBuilder("energy_hatch_16a_uv")
                .inputItems(ENERGY_INPUT_HATCH_4A[UV], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .inputItems(wireGtOctal, Europium, 2)
                .outputItems(ENERGY_INPUT_HATCH_16A[UV])
                .duration(200).EUt(VA[UV]).save(provider);

        // 4A Dynamo Hatches

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_4a_ev")
                .inputItems(TRANSFORMER[EV])
                .inputItems(ENERGY_OUTPUT_HATCH[EV])
                .inputItems(POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_EV)
                .inputItems(wireGtQuadruple, Aluminium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_4A[EV])
                .duration(100).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_4a_iv")
                .inputItems(TRANSFORMER[IV])
                .inputItems(ENERGY_OUTPUT_HATCH[IV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_IV)
                .inputItems(wireGtQuadruple, Tungsten, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_4A[IV])
                .duration(100).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_4a_luv")
                .inputItems(TRANSFORMER[LuV])
                .inputItems(ENERGY_OUTPUT_HATCH[LuV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_LuV)
                .inputItems(wireGtQuadruple, NiobiumTitanium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_4A[LuV])
                .duration(100).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_4a_zpm")
                .inputItems(TRANSFORMER[ZPM])
                .inputItems(ENERGY_OUTPUT_HATCH[ZPM])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_ZPM)
                .inputItems(wireGtQuadruple, VanadiumGallium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_4A[ZPM])
                .duration(100).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_4a_uv")
                .inputItems(TRANSFORMER[UV])
                .inputItems(ENERGY_OUTPUT_HATCH[UV])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(VOLTAGE_COIL_UV)
                .inputItems(wireGtQuadruple, YttriumBariumCuprate, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_4A[UV])
                .duration(100).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_4a_uhv")
                .inputItems(ENERGY_OUTPUT_HATCH[UHV], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .inputItems(wireGtQuadruple, Europium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_4A[UHV])
                .duration(100).EUt(VA[UV]).save(provider);

        // 16A Dynamo Hatches
        // TODO Adjustable transformers
/*
        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(ADJUSTABLE_TRANSFORMER[IV])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[1])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_IV)
                .inputItems(wireGtOctal, Tungsten, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[0])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(ADJUSTABLE_TRANSFORMER[LuV])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[2])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_LuV)
                .inputItems(wireGtOctal, NiobiumTitanium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[1])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(ADJUSTABLE_TRANSFORMER[ZPM])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[3])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_ZPM)
                .inputItems(wireGtOctal, VanadiumGallium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[2])
                .duration(200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(ADJUSTABLE_TRANSFORMER[UV])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[4])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_UV)
                .inputItems(wireGtOctal, YttriumBariumCuprate, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[3])
                .duration(200).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItems(ENERGY_OUTPUT_HATCH_4A[5], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .inputItems(wireGtOctal, Europium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[4])
                .duration(200).EUt(VA[UV]).save(provider);
 */

        // Maintenance Hatch

        ASSEMBLER_RECIPES.recipeBuilder("maintenance_hatch")
                .inputItems(HULL[LV])
                .circuitMeta(1)
                .outputItems(MAINTENANCE_HATCH)
                .duration(100).EUt(VA[LV]).save(provider);

        // Multiblock Miners

        ASSEMBLER_RECIPES.recipeBuilder("ev_large_miner")
                .inputItems(HULL[EV])
                .inputItems(frameGt, Titanium, 4)
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_EV, 4)
                .inputItems(ELECTRIC_PUMP_EV, 4)
                .inputItems(CONVEYOR_MODULE_EV, 4)
                .inputItems(gear, Tungsten, 4)
                .circuitMeta(2)
                .outputItems(LARGE_MINER[EV])
                .duration(400).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iv_large_miner")
                .inputItems(HULL[IV])
                .inputItems(frameGt, TungstenSteel, 4)
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_IV, 4)
                .inputItems(ELECTRIC_PUMP_IV, 4)
                .inputItems(CONVEYOR_MODULE_IV, 4)
                .inputItems(gear, Iridium, 4)
                .circuitMeta(2)
                .outputItems(LARGE_MINER[IV])
                .duration(400).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_large_miner")
                .inputItems(HULL[LuV])
                .inputItems(frameGt, HSSS, 4)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_LuV, 4)
                .inputItems(ELECTRIC_PUMP_LuV, 4)
                .inputItems(CONVEYOR_MODULE_LuV, 4)
                .inputItems(gear, Ruridit, 4)
                .circuitMeta(2)
                .outputItems(LARGE_MINER[LuV])
                .duration(400).EUt(VA[LuV]).save(provider);

        // Multiblock Fluid Drills
        // TODO Multiblock fluid rigs

        ASSEMBLER_RECIPES.recipeBuilder("mv_fluid_drilling_rig")
                .inputItems(HULL[MV])
                .inputItems(frameGt, Steel, 4)
                .inputItems(CustomTags.MV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_MV, 4)
                .inputItems(ELECTRIC_PUMP_MV, 4)
                .inputItems(gear, VanadiumSteel, 4)
                .circuitMeta(2)
                .outputItems(FLUID_DRILLING_RIG[MV])
                .duration(400).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hv_fluid_drilling_rig")
                .inputItems(HULL[EV])
                .inputItems(frameGt, Titanium, 4)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_EV, 4)
                .inputItems(ELECTRIC_PUMP_EV, 4)
                .inputItems(gear, TungstenCarbide, 4)
                .circuitMeta(2)
                .outputItems(FLUID_DRILLING_RIG[HV])
                .duration(400).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("ev_fluid_drilling_rig")
                .inputItems(HULL[LuV])
                .inputItems(frameGt, TungstenSteel, 4)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(ELECTRIC_MOTOR_LuV, 4)
                .inputItems(ELECTRIC_PUMP_LuV, 4)
                .inputItems(gear, Osmiridium, 4)
                .circuitMeta(2)
                .outputItems(FLUID_DRILLING_RIG[EV])
                .duration(400).EUt(VA[LuV]).save(provider);

        // Long Distance Pipes
        ASSEMBLER_RECIPES.recipeBuilder("long_distance_item_endpoint")
                .inputItems(pipeLargeItem, Tin, 2)
                .inputItems(plate, Steel, 8)
                .inputItems(gear, Steel, 2)
                .circuitMeta(1)
                .inputFluids(SolderingAlloy.getFluid(L / 2))
                .outputItems(LONG_DIST_ITEM_ENDPOINT, 2)
                .duration(400).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("long_distance_fluid_endpoint")
                .inputItems(pipeLargeFluid, Bronze, 2)
                .inputItems(plate, Steel, 8)
                .inputItems(gear, Steel, 2)
                .circuitMeta(1)
                .inputFluids(SolderingAlloy.getFluid(L / 2))
                .outputItems(LONG_DIST_FLUID_ENDPOINT, 2)
                .duration(400).EUt(16)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("long_distance_item_pipe")
                .inputItems(pipeLargeItem, Tin, 2)
                .inputItems(plate, Steel, 8)
                .circuitMeta(2)
                .inputFluids(SolderingAlloy.getFluid(L / 2))
                .outputItems(LD_ITEM_PIPE, 64)
                .duration(600).EUt(24)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("long_distance_fluid_pipe")
                .inputItems(pipeLargeFluid, Bronze, 2)
                .inputItems(plate, Steel, 8)
                .circuitMeta(2)
                .inputFluids(SolderingAlloy.getFluid(L / 2))
                .outputItems(LD_FLUID_PIPE, 64)
                .duration(600).EUt(24)
                .save(provider);

    }
}
