package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import net.minecraft.data.recipes.FinishedRecipe;

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
        registerLaserRecipes(provider);
        
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



        // Power Transformers

        ASSEMBLER_RECIPES.recipeBuilder("ulv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[ULV])
                .inputItems(ELECTRIC_PUMP_LV)
                .inputItems(cableGtOctal, Tin)
                .inputItems(cableGtHex, Lead, 2)
                .inputItems(springSmall, Lead)
                .inputItems(spring, Tin)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[ULV])
                .duration(200).EUt(VA[ULV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("lv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[LV])
                .inputItems(ELECTRIC_PUMP_LV)
                .inputItems(cableGtOctal, Copper)
                .inputItems(cableGtHex, Tin, 2)
                .inputItems(springSmall, Tin)
                .inputItems(spring, Copper)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[LV])
                .duration(200).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("mv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[MV])
                .inputItems(ELECTRIC_PUMP_MV)
                .inputItems(cableGtOctal, Gold)
                .inputItems(cableGtHex, Copper, 2)
                .inputItems(springSmall, Copper)
                .inputItems(spring, Gold)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[MV])
                .duration(200).EUt(VA[MV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[HV])
                .inputItems(ELECTRIC_PUMP_MV)
                .inputItems(cableGtOctal, Aluminium)
                .inputItems(cableGtHex, Gold, 2)
                .inputItems(springSmall, Gold)
                .inputItems(spring, Aluminium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[HV])
                .duration(200).EUt(VA[HV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("ev_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[EV])
                .inputItems(ELECTRIC_PUMP_HV)
                .inputItems(cableGtOctal, Tungsten)
                .inputItems(cableGtHex, Aluminium, 2)
                .inputItems(springSmall, Aluminium)
                .inputItems(spring, Tungsten)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[EV])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("iv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[IV])
                .inputItems(ELECTRIC_PUMP_HV)
                .inputItems(cableGtOctal, NiobiumTitanium)
                .inputItems(cableGtHex, Tungsten, 2)
                .inputItems(springSmall, Tungsten)
                .inputItems(spring, NiobiumTitanium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[IV])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[LuV])
                .inputItems(ELECTRIC_PUMP_EV)
                .inputItems(cableGtOctal, VanadiumGallium)
                .inputItems(cableGtHex, NiobiumTitanium, 2)
                .inputItems(springSmall, NiobiumTitanium)
                .inputItems(spring, VanadiumGallium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[LuV])
                .duration(200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[ZPM])
                .inputItems(ELECTRIC_PUMP_EV)
                .inputItems(cableGtOctal, YttriumBariumCuprate)
                .inputItems(cableGtHex, VanadiumGallium, 2)
                .inputItems(springSmall, VanadiumGallium)
                .inputItems(spring, YttriumBariumCuprate)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[ZPM])
                .duration(200).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_power_transformer")
                .inputItems(HI_AMP_TRANSFORMER_4A[UV])
                .inputItems(ELECTRIC_PUMP_IV)
                .inputItems(cableGtOctal, Europium)
                .inputItems(cableGtHex, YttriumBariumCuprate, 2)
                .inputItems(springSmall, YttriumBariumCuprate)
                .inputItems(spring, Europium)
                .inputFluids(Lubricant.getFluid(2000))
                .outputItems(POWER_TRANSFORMER[UV])
                .duration(200).EUt(VA[UV]).save(provider);

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

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_16a_ev")
                .inputItems(HI_AMP_TRANSFORMER_4A[IV])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[IV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_IV)
                .inputItems(wireGtOctal, Tungsten, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[EV])
                .duration(200).EUt(VA[EV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_16a_iv")
                .inputItems(HI_AMP_TRANSFORMER_4A[LuV])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[LuV])
                .inputItems(HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_LuV)
                .inputItems(wireGtOctal, NiobiumTitanium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[IV])
                .duration(200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_16a_luv")
                .inputItems(HI_AMP_TRANSFORMER_4A[ZPM])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[ZPM])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_ZPM)
                .inputItems(wireGtOctal, VanadiumGallium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[LuV])
                .duration(200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_16a_zpm")
                .inputItems(HI_AMP_TRANSFORMER_4A[UV])
                .inputItems(ENERGY_OUTPUT_HATCH_4A[UV])
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(VOLTAGE_COIL_UV)
                .inputItems(wireGtOctal, YttriumBariumCuprate, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[ZPM])
                .duration(200).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("dynamo_hatch_16a_uv")
                .inputItems(ENERGY_OUTPUT_HATCH_4A[UHV], 2)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputItems(wireGtDouble, RutheniumTriniumAmericiumNeutronate)
                .inputItems(wireGtOctal, Europium, 2)
                .outputItems(ENERGY_OUTPUT_HATCH_16A[UV])
                .duration(200).EUt(VA[UV]).save(provider);

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

    // TODO clean this up with a CraftingComponent rework
    private static void registerLaserRecipes(Consumer<FinishedRecipe> provider) {

        // 256A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder("iv_256a_laser_target_hatch")
                .inputItems(HULL[IV])
                .inputItems(lens, Diamond)
                .inputItems(EMITTER_IV)
                .inputItems(ELECTRIC_PUMP_IV)
                .inputItems(cableGtSingle, Platinum, 4)
                .circuitMeta(1)
                .outputItems(LASER_INPUT_HATCH_256[IV])
                .duration(300).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_256a_laser_target_hatch")
                .inputItems(HULL[LuV])
                .inputItems(lens, Diamond)
                .inputItems(EMITTER_LuV)
                .inputItems(ELECTRIC_PUMP_LuV)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .circuitMeta(1)
                .outputItems(LASER_INPUT_HATCH_256[LuV])
                .duration(300).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_256a_laser_target_hatch")
                .inputItems(HULL[ZPM])
                .inputItems(lens, Diamond)
                .inputItems(EMITTER_ZPM)
                .inputItems(ELECTRIC_PUMP_ZPM)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .circuitMeta(1)
                .outputItems(LASER_INPUT_HATCH_256[ZPM])
                .duration(300).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_256a_laser_target_hatch")
                .inputItems(HULL[UV])
                .inputItems(lens, Diamond)
                .inputItems(EMITTER_UV)
                .inputItems(ELECTRIC_PUMP_UV)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .circuitMeta(1)
                .outputItems(LASER_INPUT_HATCH_256[UV])
                .duration(300).EUt(VA[UV]).save(provider);

        // 256A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder("iv_256a_laser_source_hatch")
                .inputItems(HULL[IV])
                .inputItems(lens, Diamond)
                .inputItems(SENSOR_IV)
                .inputItems(ELECTRIC_PUMP_IV)
                .inputItems(cableGtSingle, Platinum, 4)
                .circuitMeta(1)
                .outputItems(LASER_OUTPUT_HATCH_256[IV])
                .duration(300).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_256a_laser_source_hatch")
                .inputItems(HULL[LuV])
                .inputItems(lens, Diamond)
                .inputItems(SENSOR_LuV)
                .inputItems(ELECTRIC_PUMP_LuV)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .circuitMeta(1)
                .outputItems(LASER_OUTPUT_HATCH_256[LuV])
                .duration(300).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_256a_laser_source_hatch")
                .inputItems(HULL[ZPM])
                .inputItems(lens, Diamond)
                .inputItems(SENSOR_ZPM)
                .inputItems(ELECTRIC_PUMP_ZPM)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .circuitMeta(1)
                .outputItems(LASER_OUTPUT_HATCH_256[ZPM])
                .duration(300).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_256a_laser_source_hatch")
                .inputItems(HULL[UV])
                .inputItems(lens, Diamond)
                .inputItems(SENSOR_UV)
                .inputItems(ELECTRIC_PUMP_UV)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .circuitMeta(1)
                .outputItems(LASER_OUTPUT_HATCH_256[UV])
                .duration(300).EUt(VA[UV]).save(provider);

        // 1024A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder("iv_1024a_laser_target_hatch")
                .inputItems(HULL[IV])
                .inputItems(lens, Diamond, 2)
                .inputItems(EMITTER_IV, 2)
                .inputItems(ELECTRIC_PUMP_IV, 2)
                .inputItems(cableGtDouble, Platinum, 4)
                .circuitMeta(2)
                .outputItems(LASER_INPUT_HATCH_1024[IV])
                .duration(600).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_1024a_laser_target_hatch")
                .inputItems(HULL[LuV])
                .inputItems(lens, Diamond, 2)
                .inputItems(EMITTER_LuV, 2)
                .inputItems(ELECTRIC_PUMP_LuV, 2)
                .inputItems(cableGtDouble, NiobiumTitanium, 4)
                .circuitMeta(2)
                .outputItems(LASER_INPUT_HATCH_1024[LuV])
                .duration(600).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_1024a_laser_target_hatch")
                .inputItems(HULL[ZPM])
                .inputItems(lens, Diamond, 2)
                .inputItems(EMITTER_ZPM, 2)
                .inputItems(ELECTRIC_PUMP_ZPM, 2)
                .inputItems(cableGtDouble, VanadiumGallium, 4)
                .circuitMeta(2)
                .outputItems(LASER_INPUT_HATCH_1024[ZPM])
                .duration(600).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_1024a_laser_target_hatch")
                .inputItems(HULL[UV])
                .inputItems(lens, Diamond, 2)
                .inputItems(EMITTER_UV, 2)
                .inputItems(ELECTRIC_PUMP_UV, 2)
                .inputItems(cableGtDouble, YttriumBariumCuprate, 4)
                .circuitMeta(2)
                .outputItems(LASER_INPUT_HATCH_1024[UV])
                .duration(600).EUt(VA[UV]).save(provider);

        // 1024A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder("iv_1024a_laser_source_hatch")
                .inputItems(HULL[IV])
                .inputItems(lens, Diamond, 2)
                .inputItems(SENSOR_IV, 2)
                .inputItems(ELECTRIC_PUMP_IV, 2)
                .inputItems(cableGtDouble, Platinum, 4)
                .circuitMeta(2)
                .outputItems(LASER_OUTPUT_HATCH_1024[IV])
                .duration(600).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_1024a_laser_source_hatch")
                .inputItems(HULL[LuV])
                .inputItems(lens, Diamond, 2)
                .inputItems(SENSOR_LuV, 2)
                .inputItems(ELECTRIC_PUMP_LuV, 2)
                .inputItems(cableGtDouble, NiobiumTitanium, 4)
                .circuitMeta(2)
                .outputItems(LASER_OUTPUT_HATCH_1024[LuV])
                .duration(600).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_1024a_laser_source_hatch")
                .inputItems(HULL[ZPM])
                .inputItems(lens, Diamond, 2)
                .inputItems(SENSOR_ZPM, 2)
                .inputItems(ELECTRIC_PUMP_ZPM, 2)
                .inputItems(cableGtDouble, VanadiumGallium, 4)
                .circuitMeta(2)
                .outputItems(LASER_OUTPUT_HATCH_1024[ZPM])
                .duration(600).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_1024a_laser_source_hatch")
                .inputItems(HULL[UV])
                .inputItems(lens, Diamond, 2)
                .inputItems(SENSOR_UV, 2)
                .inputItems(ELECTRIC_PUMP_UV, 2)
                .inputItems(cableGtDouble, YttriumBariumCuprate, 4)
                .circuitMeta(2)
                .outputItems(LASER_OUTPUT_HATCH_1024[UV])
                .duration(600).EUt(VA[UV]).save(provider);

        // 4096A Laser Target Hatches
        ASSEMBLER_RECIPES.recipeBuilder("iv_4096a_laser_target_hatch")
                .inputItems(HULL[IV])
                .inputItems(lens, Diamond, 4)
                .inputItems(EMITTER_IV, 4)
                .inputItems(ELECTRIC_PUMP_IV, 4)
                .inputItems(cableGtQuadruple, Platinum, 4)
                .circuitMeta(3)
                .outputItems(LASER_INPUT_HATCH_4096[IV])
                .duration(1200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_4096a_laser_target_hatch")
                .inputItems(HULL[LuV])
                .inputItems(lens, Diamond, 4)
                .inputItems(EMITTER_LuV, 4)
                .inputItems(ELECTRIC_PUMP_LuV, 4)
                .inputItems(cableGtQuadruple, NiobiumTitanium, 4)
                .circuitMeta(3)
                .outputItems(LASER_INPUT_HATCH_4096[LuV])
                .duration(1200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_4096a_laser_target_hatch")
                .inputItems(HULL[ZPM])
                .inputItems(lens, Diamond, 4)
                .inputItems(EMITTER_ZPM, 4)
                .inputItems(ELECTRIC_PUMP_ZPM, 4)
                .inputItems(cableGtQuadruple, VanadiumGallium, 4)
                .circuitMeta(3)
                .outputItems(LASER_INPUT_HATCH_4096[ZPM])
                .duration(1200).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_4096a_laser_target_hatch")
                .inputItems(HULL[UV])
                .inputItems(lens, Diamond, 4)
                .inputItems(EMITTER_UV, 4)
                .inputItems(ELECTRIC_PUMP_UV, 4)
                .inputItems(cableGtQuadruple, YttriumBariumCuprate, 4)
                .circuitMeta(3)
                .outputItems(LASER_INPUT_HATCH_4096[UV])
                .duration(1200).EUt(VA[UV]).save(provider);

        // 4096A Laser Source Hatches
        ASSEMBLER_RECIPES.recipeBuilder("iv_4096a_laser_source_hatch")
                .inputItems(HULL[IV])
                .inputItems(lens, Diamond, 4)
                .inputItems(SENSOR_IV, 4)
                .inputItems(ELECTRIC_PUMP_IV, 4)
                .inputItems(cableGtQuadruple, Platinum, 4)
                .circuitMeta(3)
                .outputItems(LASER_OUTPUT_HATCH_4096[IV])
                .duration(1200).EUt(VA[IV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("luv_4096a_laser_source_hatch")
                .inputItems(HULL[LuV])
                .inputItems(lens, Diamond, 4)
                .inputItems(SENSOR_LuV, 4)
                .inputItems(ELECTRIC_PUMP_LuV, 4)
                .inputItems(cableGtQuadruple, NiobiumTitanium, 4)
                .circuitMeta(3)
                .outputItems(LASER_OUTPUT_HATCH_4096[LuV])
                .duration(1200).EUt(VA[LuV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_4096a_laser_source_hatch")
                .inputItems(HULL[ZPM])
                .inputItems(lens, Diamond, 4)
                .inputItems(SENSOR_ZPM, 4)
                .inputItems(ELECTRIC_PUMP_ZPM, 4)
                .inputItems(cableGtQuadruple, VanadiumGallium, 4)
                .circuitMeta(3)
                .outputItems(LASER_OUTPUT_HATCH_4096[ZPM])
                .duration(1200).EUt(VA[ZPM]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("uv_4096a_laser_source_hatch")
                .inputItems(HULL[UV])
                .inputItems(lens, Diamond, 4)
                .inputItems(SENSOR_UV, 4)
                .inputItems(ELECTRIC_PUMP_UV, 4)
                .inputItems(cableGtQuadruple, YttriumBariumCuprate, 4)
                .circuitMeta(3)
                .outputItems(LASER_OUTPUT_HATCH_4096[UV])
                .duration(1200).EUt(VA[UV]).save(provider);
    }
}
