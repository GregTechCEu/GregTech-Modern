package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLY_LINE_RECIPES;

public class ComponentRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        // Motors
        // Start--------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_lv_steel", ELECTRIC_MOTOR_LV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(CABLE_GT_SINGLE, Tin), 'W',
                new MaterialEntry(WIRE_GT_SINGLE, Copper), 'R', new MaterialEntry(ROD, Steel), 'M',
                new MaterialEntry(ROD, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_lv_iron", ELECTRIC_MOTOR_LV.asStack(),
                "CWR", "WMW", "RWC", 'C', new MaterialEntry(CABLE_GT_SINGLE, Tin), 'W',
                new MaterialEntry(WIRE_GT_SINGLE, Copper), 'R', new MaterialEntry(ROD, Iron), 'M',
                new MaterialEntry(ROD, IronMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_mv", ELECTRIC_MOTOR_MV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(CABLE_GT_SINGLE, Copper), 'W',
                new MaterialEntry(WIRE_GT_DOUBLE, Cupronickel), 'R', new MaterialEntry(ROD, Aluminium), 'M',
                new MaterialEntry(ROD, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_hv", ELECTRIC_MOTOR_HV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(CABLE_GT_DOUBLE, Silver), 'W',
                new MaterialEntry(WIRE_GT_DOUBLE, Electrum), 'R', new MaterialEntry(ROD, StainlessSteel), 'M',
                new MaterialEntry(ROD, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_ev", ELECTRIC_MOTOR_EV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(CABLE_GT_DOUBLE, Aluminium), 'W',
                new MaterialEntry(WIRE_GT_DOUBLE, Kanthal), 'R', new MaterialEntry(ROD, Titanium), 'M',
                new MaterialEntry(ROD, NeodymiumMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_iv", ELECTRIC_MOTOR_IV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(CABLE_GT_DOUBLE, Tungsten), 'W',
                new MaterialEntry(WIRE_GT_DOUBLE, Graphene), 'R', new MaterialEntry(ROD, TungstenSteel), 'M',
                new MaterialEntry(ROD, NeodymiumMagnetic));

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_lv_iron")
                .inputItems(CABLE_GT_SINGLE, Tin, 2)
                .inputItems(ROD, Iron, 2)
                .inputItems(ROD, IronMagnetic)
                .inputItems(WIRE_GT_SINGLE, Copper, 4)
                .outputItems(ELECTRIC_MOTOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_lv_steel")
                .inputItems(CABLE_GT_SINGLE, Tin, 2)
                .inputItems(ROD, Steel, 2)
                .inputItems(ROD, SteelMagnetic)
                .inputItems(WIRE_GT_SINGLE, Copper, 4)
                .outputItems(ELECTRIC_MOTOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_mv")
                .inputItems(CABLE_GT_SINGLE, Copper, 2)
                .inputItems(ROD, Aluminium, 2)
                .inputItems(ROD, SteelMagnetic)
                .inputItems(WIRE_GT_DOUBLE, Cupronickel, 4)
                .outputItems(ELECTRIC_MOTOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_hv")
                .inputItems(CABLE_GT_DOUBLE, Silver, 2)
                .inputItems(ROD, StainlessSteel, 2)
                .inputItems(ROD, SteelMagnetic)
                .inputItems(WIRE_GT_DOUBLE, Electrum, 4)
                .outputItems(ELECTRIC_MOTOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_ev")
                .inputItems(CABLE_GT_DOUBLE, Aluminium, 2)
                .inputItems(ROD, Titanium, 2)
                .inputItems(ROD, NeodymiumMagnetic)
                .inputItems(WIRE_GT_DOUBLE, Kanthal, 4)
                .outputItems(ELECTRIC_MOTOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_iv")
                .inputItems(CABLE_GT_DOUBLE, Tungsten, 2)
                .inputItems(ROD, TungstenSteel, 2)
                .inputItems(ROD, NeodymiumMagnetic)
                .inputItems(WIRE_GT_DOUBLE, Graphene, 4)
                .outputItems(ELECTRIC_MOTOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_motor_luv")
                .inputItems(ROD_LONG, SamariumMagnetic)
                .inputItems(ROD_LONG, HSSS, 2)
                .inputItems(RING, HSSS, 2)
                .inputItems(ROUND, HSSS, 4)
                .inputItems(WIRE_FINE, Ruridit, 64)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy, L)
                .inputFluids(Lubricant, 250)
                .outputItems(ELECTRIC_MOTOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_MOTOR_IV.asStack())
                        .duration(900)
                        .EUt(VA[EV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_motor_zpm")
                .inputItems(ROD_LONG, SamariumMagnetic)
                .inputItems(ROD_LONG, Osmiridium, 4)
                .inputItems(RING, Osmiridium, 4)
                .inputItems(ROUND, Osmiridium, 8)
                .inputItems(WIRE_FINE, Europium, 64)
                .inputItems(WIRE_FINE, Europium, 32)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy, L * 2)
                .inputFluids(Lubricant, 500)
                .outputItems(ELECTRIC_MOTOR_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_MOTOR_LuV.asStack())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_motor_uv")
                .inputItems(ROD_LONG, SamariumMagnetic)
                .inputItems(ROD_LONG, Tritanium, 4)
                .inputItems(RING, Tritanium, 4)
                .inputItems(ROUND, Tritanium, 8)
                .inputItems(WIRE_FINE, Americium, 64)
                .inputItems(WIRE_FINE, Americium, 64)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Lubricant, 1000)
                .inputFluids(Naquadria, L * 4)
                .outputItems(ELECTRIC_MOTOR_UV)
                .stationResearch(b -> b
                        .researchStack(ELECTRIC_MOTOR_ZPM.asStack())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        // Conveyors
        // Start-----------------------------------------------------------------------------------------------
        final Map<String, Material> rubberMaterials = new Object2ObjectOpenHashMap<>();
        rubberMaterials.put("rubber", Rubber);
        rubberMaterials.put("silicone_rubber", SiliconeRubber);
        rubberMaterials.put("styrene_butadiene_rubber", StyreneButadieneRubber);

        for (Map.Entry<String, Material> materialEntry : rubberMaterials.entrySet()) {
            Material material = materialEntry.getValue();
            String name = materialEntry.getKey();

            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_lv_%s", name), CONVEYOR_MODULE_LV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(PLATE, material), 'C', new MaterialEntry(CABLE_GT_SINGLE, Tin), 'M',
                    ELECTRIC_MOTOR_LV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_mv_%s", name), CONVEYOR_MODULE_MV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(PLATE, material), 'C', new MaterialEntry(CABLE_GT_SINGLE, Copper), 'M',
                    ELECTRIC_MOTOR_MV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_hv_%s", name), CONVEYOR_MODULE_HV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(PLATE, material), 'C', new MaterialEntry(CABLE_GT_SINGLE, Gold), 'M',
                    ELECTRIC_MOTOR_HV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_ev_%s", name), CONVEYOR_MODULE_EV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(PLATE, material), 'C', new MaterialEntry(CABLE_GT_SINGLE, Aluminium),
                    'M', ELECTRIC_MOTOR_EV.asStack());
            if (!materialEntry.getValue().equals(Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, material.equals(SiliconeRubber),
                        String.format("conveyor_module_iv_%s", materialEntry.getKey()), CONVEYOR_MODULE_IV.asStack(),
                        "RRR", "MCM", "RRR", 'R', new MaterialEntry(PLATE, material), 'C',
                        new MaterialEntry(CABLE_GT_SINGLE, Tungsten), 'M', ELECTRIC_MOTOR_IV.asStack());

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_lv_" + name)
                    .inputItems(CABLE_GT_SINGLE, Tin)
                    .inputItems(ELECTRIC_MOTOR_LV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_LV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_mv_" + name)
                    .inputItems(CABLE_GT_SINGLE, Copper)
                    .inputItems(ELECTRIC_MOTOR_MV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_MV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_hv_" + name)
                    .inputItems(CABLE_GT_SINGLE, Gold)
                    .inputItems(ELECTRIC_MOTOR_HV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_HV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_ev_" + name)
                    .inputItems(CABLE_GT_SINGLE, Aluminium)
                    .inputItems(ELECTRIC_MOTOR_EV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_EV)
                    .duration(100).EUt(VA[LV]).save(provider);

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_iv_" + name)
                        .inputItems(CABLE_GT_SINGLE, Tungsten)
                        .inputItems(ELECTRIC_MOTOR_IV, 2)
                        .inputFluids(materialEntry.getValue().getFluid(L * 6))
                        .circuitMeta(1)
                        .outputItems(CONVEYOR_MODULE_IV)
                        .duration(100).EUt(VA[LV]).save(provider);

            // Pumps
            // Start---------------------------------------------------------------------------------------------------
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_lv_%s", name), ELECTRIC_PUMP_LV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(SCREW, Tin), 'X', new MaterialEntry(ROTOR, Tin), 'P',
                    new MaterialEntry(PIPE_NORMAL_FLUID, Bronze), 'R', new MaterialEntry(RING, material), 'C',
                    new MaterialEntry(CABLE_GT_SINGLE, Tin), 'M', ELECTRIC_MOTOR_LV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_mv_%s", name), ELECTRIC_PUMP_MV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(SCREW, Bronze), 'X', new MaterialEntry(ROTOR, Bronze), 'P',
                    new MaterialEntry(PIPE_NORMAL_FLUID, Steel), 'R', new MaterialEntry(RING, material), 'C',
                    new MaterialEntry(CABLE_GT_SINGLE, Copper), 'M', ELECTRIC_MOTOR_MV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_hv_%s", name), ELECTRIC_PUMP_HV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(SCREW, Steel), 'X', new MaterialEntry(ROTOR, Steel), 'P',
                    new MaterialEntry(PIPE_NORMAL_FLUID, StainlessSteel), 'R', new MaterialEntry(RING, material),
                    'C', new MaterialEntry(CABLE_GT_SINGLE, Gold), 'M', ELECTRIC_MOTOR_HV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_ev_%s", name), ELECTRIC_PUMP_EV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(SCREW, StainlessSteel), 'X', new MaterialEntry(ROTOR, StainlessSteel), 'P',
                    new MaterialEntry(PIPE_NORMAL_FLUID, Titanium), 'R', new MaterialEntry(RING, material), 'C',
                    new MaterialEntry(CABLE_GT_SINGLE, Aluminium), 'M', ELECTRIC_MOTOR_EV.asStack());
            if (!material.equals(Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, material.equals(SiliconeRubber),
                        String.format("electric_pump_iv_%s", name), ELECTRIC_PUMP_IV.asStack(), "SXR", "dPw", "RMC",
                        'S', new MaterialEntry(SCREW, TungstenSteel), 'X',
                        new MaterialEntry(ROTOR, TungstenSteel), 'P',
                        new MaterialEntry(PIPE_NORMAL_FLUID, TungstenSteel), 'R', new MaterialEntry(RING, material),
                        'C', new MaterialEntry(CABLE_GT_SINGLE, Tungsten), 'M', ELECTRIC_MOTOR_IV.asStack());

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_lv_" + name)
                    .inputItems(CABLE_GT_SINGLE, Tin)
                    .inputItems(PIPE_NORMAL_FLUID, Bronze)
                    .inputItems(SCREW, Tin)
                    .inputItems(ROTOR, Tin)
                    .inputItems(RING, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_LV)
                    .outputItems(ELECTRIC_PUMP_LV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_mv_" + name)
                    .inputItems(CABLE_GT_SINGLE, Copper)
                    .inputItems(PIPE_NORMAL_FLUID, Steel)
                    .inputItems(SCREW, Bronze)
                    .inputItems(ROTOR, Bronze)
                    .inputItems(RING, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_MV)
                    .outputItems(ELECTRIC_PUMP_MV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_hv_" + name)
                    .inputItems(CABLE_GT_SINGLE, Gold)
                    .inputItems(PIPE_NORMAL_FLUID, StainlessSteel)
                    .inputItems(SCREW, Steel)
                    .inputItems(ROTOR, Steel)
                    .inputItems(RING, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_HV)
                    .outputItems(ELECTRIC_PUMP_HV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_ev_" + name)
                    .inputItems(CABLE_GT_SINGLE, Aluminium)
                    .inputItems(PIPE_NORMAL_FLUID, Titanium)
                    .inputItems(SCREW, StainlessSteel)
                    .inputItems(ROTOR, StainlessSteel)
                    .inputItems(RING, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_EV)
                    .outputItems(ELECTRIC_PUMP_EV)
                    .duration(100).EUt(VA[LV]).save(provider);

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder("electric_pump_iv_" + name)
                        .inputItems(CABLE_GT_SINGLE, Tungsten)
                        .inputItems(PIPE_NORMAL_FLUID, TungstenSteel)
                        .inputItems(SCREW, TungstenSteel)
                        .inputItems(ROTOR, TungstenSteel)
                        .inputItems(RING, materialEntry.getValue(), 2)
                        .inputItems(ELECTRIC_MOTOR_IV)
                        .outputItems(ELECTRIC_PUMP_IV)
                        .duration(100).EUt(VA[LV]).save(provider);
        }

        ASSEMBLY_LINE_RECIPES.recipeBuilder("conveyor_module_luv")
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(PLATE, HSSS, 2)
                .inputItems(RING, HSSS, 4)
                .inputItems(ROUND, HSSS, 16)
                .inputItems(SCREW, HSSS, 4)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy, L)
                .inputFluids(Lubricant, 250)
                .inputFluids(StyreneButadieneRubber, L * 8)
                .outputItems(CONVEYOR_MODULE_LuV)
                .scannerResearch(b -> b
                        .researchStack(CONVEYOR_MODULE_IV.asStack())
                        .duration(900)
                        .EUt(VA[EV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("conveyor_module_zpm")
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(PLATE, Osmiridium, 2)
                .inputItems(RING, Osmiridium, 4)
                .inputItems(ROUND, Osmiridium, 16)
                .inputItems(SCREW, Osmiridium, 4)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy, L * 2)
                .inputFluids(Lubricant, 500)
                .inputFluids(StyreneButadieneRubber, L * 16)
                .outputItems(CONVEYOR_MODULE_ZPM)
                .scannerResearch(b -> b
                        .researchStack(CONVEYOR_MODULE_LuV.asStack())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("conveyor_module_uv")
                .inputItems(ELECTRIC_MOTOR_UV, 2)
                .inputItems(PLATE, Tritanium, 2)
                .inputItems(RING, Tritanium, 4)
                .inputItems(ROUND, Tritanium, 16)
                .inputItems(SCREW, Tritanium, 4)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Lubricant, 1000)
                .inputFluids(StyreneButadieneRubber, L * 24)
                .inputFluids(Naquadria, L * 4)
                .outputItems(CONVEYOR_MODULE_UV)
                .stationResearch(b -> b
                        .researchStack(CONVEYOR_MODULE_ZPM.asStack())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_pump_luv")
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(PIPE_SMALL_FLUID, NiobiumTitanium)
                .inputItems(PLATE, HSSS, 2)
                .inputItems(SCREW, HSSS, 8)
                .inputItems(RING, SiliconeRubber, 4)
                .inputItems(ROTOR, HSSS)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy, L)
                .inputFluids(Lubricant, 250)
                .outputItems(ELECTRIC_PUMP_LuV)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_PUMP_IV.asStack())
                        .duration(900)
                        .EUt(VA[EV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_pump_zpm")
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(PIPE_NORMAL_FLUID, Polybenzimidazole)
                .inputItems(PLATE, Osmiridium, 2)
                .inputItems(SCREW, Osmiridium, 8)
                .inputItems(RING, SiliconeRubber, 8)
                .inputItems(ROTOR, Osmiridium)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy, L * 2)
                .inputFluids(Lubricant, 500)
                .outputItems(ELECTRIC_PUMP_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_PUMP_LuV.asStack())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_pump_uv")
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(PIPE_LARGE_FLUID, Naquadah)
                .inputItems(PLATE, Tritanium, 2)
                .inputItems(SCREW, Tritanium, 8)
                .inputItems(RING, SiliconeRubber, 16)
                .inputItems(ROTOR, NaquadahAlloy)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Lubricant, 1000)
                .inputFluids(Naquadria, L * 4)
                .outputItems(ELECTRIC_PUMP_UV)
                .stationResearch(b -> b
                        .researchStack(ELECTRIC_PUMP_ZPM.asStack())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        // Fluid
        // Regulators----------------------------------------------------------------------------------------------

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_lv")
                .inputItems(ELECTRIC_PUMP_LV)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_LV)
                .EUt(VA[LV])
                .duration(400)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_mv")
                .inputItems(ELECTRIC_PUMP_MV)
                .inputItems(CustomTags.MV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_MV)
                .EUt(VA[MV])
                .duration(350)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_hv")
                .inputItems(ELECTRIC_PUMP_HV)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_HV)
                .EUt(VA[HV])
                .duration(300)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_ev")
                .inputItems(ELECTRIC_PUMP_EV)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_EV)
                .EUt(VA[EV])
                .duration(250)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_iv")
                .inputItems(ELECTRIC_PUMP_IV)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_IV)
                .EUt(VA[IV])
                .duration(200)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_luv")
                .inputItems(ELECTRIC_PUMP_LuV)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_LuV)
                .EUt(VA[LuV])
                .duration(150)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_zpm")
                .inputItems(ELECTRIC_PUMP_ZPM)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_ZPM)
                .EUt(VA[ZPM])
                .duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("fluid_regulator_uv")
                .inputItems(ELECTRIC_PUMP_UV)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_UV)
                .EUt(VA[UV])
                .duration(50)
                .save(provider);

        // Voiding Covers Start-----------------------------------------------------------------------------------------

        VanillaRecipeHelper.addShapedRecipe(provider, false, "cover_item_voiding", COVER_ITEM_VOIDING.asStack(), "SDS",
                "dPw", " E ", 'S', new MaterialEntry(SCREW, Steel), 'D', COVER_ITEM_DETECTOR.asStack(), 'P',
                new MaterialEntry(PIPE_NORMAL_ITEM, Brass), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder("cover_item_voiding")
                .inputItems(SCREW, Steel, 2)
                .inputItems(COVER_ITEM_DETECTOR)
                .inputItems(PIPE_NORMAL_ITEM, Brass)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(COVER_ITEM_VOIDING)
                .duration(100).EUt(VA[LV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_item_voiding_advanced")
                .inputItems(COVER_ITEM_VOIDING)
                .inputItems(CustomTags.MV_CIRCUITS, 1)
                .outputItems(COVER_ITEM_VOIDING_ADVANCED)
                .duration(100).EUt(VA[LV])
                .addMaterialInfo(true).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, false, "cover_fluid_voiding", COVER_FLUID_VOIDING.asStack(),
                "SDS",
                "dPw", " E ", 'S', new MaterialEntry(SCREW, Steel), 'D', COVER_FLUID_DETECTOR.asStack(), 'P',
                new MaterialEntry(PIPE_NORMAL_FLUID, Bronze), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder("cover_fluid_voiding")
                .inputItems(SCREW, Steel, 2)
                .inputItems(COVER_FLUID_DETECTOR)
                .inputItems(PIPE_NORMAL_FLUID, Bronze)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(COVER_FLUID_VOIDING)
                .duration(100).EUt(VA[LV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("cover_fluid_voiding_advanced")
                .inputItems(COVER_FLUID_VOIDING)
                .inputItems(CustomTags.MV_CIRCUITS, 1)
                .outputItems(COVER_FLUID_VOIDING_ADVANCED)
                .duration(100).EUt(VA[LV])
                .addMaterialInfo(true).save(provider);

        // Pistons
        // Start-------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_lv", ELECTRIC_PISTON_LV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(PLATE, Steel), 'C', new MaterialEntry(CABLE_GT_SINGLE, Tin),
                'R', new MaterialEntry(ROD, Steel), 'G', new MaterialEntry(GEAR_SMALL, Steel), 'M',
                ELECTRIC_MOTOR_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_mv", ELECTRIC_PISTON_MV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(PLATE, Aluminium), 'C',
                new MaterialEntry(CABLE_GT_SINGLE, Copper), 'R', new MaterialEntry(ROD, Aluminium), 'G',
                new MaterialEntry(GEAR_SMALL, Aluminium), 'M', ELECTRIC_MOTOR_MV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_hv", ELECTRIC_PISTON_HV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(PLATE, StainlessSteel), 'C',
                new MaterialEntry(CABLE_GT_SINGLE, Gold), 'R', new MaterialEntry(ROD, StainlessSteel), 'G',
                new MaterialEntry(GEAR_SMALL, StainlessSteel), 'M', ELECTRIC_MOTOR_HV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_ev", ELECTRIC_PISTON_EV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(PLATE, Titanium), 'C',
                new MaterialEntry(CABLE_GT_SINGLE, Aluminium), 'R', new MaterialEntry(ROD, Titanium), 'G',
                new MaterialEntry(GEAR_SMALL, Titanium), 'M', ELECTRIC_MOTOR_EV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_iv", ELECTRIC_PISTON_IV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(PLATE, TungstenSteel), 'C',
                new MaterialEntry(CABLE_GT_SINGLE, Tungsten), 'R', new MaterialEntry(ROD, TungstenSteel), 'G',
                new MaterialEntry(GEAR_SMALL, TungstenSteel), 'M', ELECTRIC_MOTOR_IV.asStack());

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_lv")
                .inputItems(ROD, Steel, 2)
                .inputItems(CABLE_GT_SINGLE, Tin, 2)
                .inputItems(PLATE, Steel, 3)
                .inputItems(GEAR_SMALL, Steel)
                .inputItems(ELECTRIC_MOTOR_LV)
                .outputItems(ELECTRIC_PISTON_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_mv")
                .inputItems(ROD, Aluminium, 2)
                .inputItems(CABLE_GT_SINGLE, Copper, 2)
                .inputItems(PLATE, Aluminium, 3)
                .inputItems(GEAR_SMALL, Aluminium)
                .inputItems(ELECTRIC_MOTOR_MV)
                .outputItems(ELECTRIC_PISTON_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_hv")
                .inputItems(ROD, StainlessSteel, 2)
                .inputItems(CABLE_GT_SINGLE, Gold, 2)
                .inputItems(PLATE, StainlessSteel, 3)
                .inputItems(GEAR_SMALL, StainlessSteel)
                .inputItems(ELECTRIC_MOTOR_HV)
                .outputItems(ELECTRIC_PISTON_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_ev")
                .inputItems(ROD, Titanium, 2)
                .inputItems(CABLE_GT_SINGLE, Aluminium, 2)
                .inputItems(PLATE, Titanium, 3)
                .inputItems(GEAR_SMALL, Titanium)
                .inputItems(ELECTRIC_MOTOR_EV)
                .outputItems(ELECTRIC_PISTON_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_iv")
                .inputItems(ROD, TungstenSteel, 2)
                .inputItems(CABLE_GT_SINGLE, Tungsten, 2)
                .inputItems(PLATE, TungstenSteel, 3)
                .inputItems(GEAR_SMALL, TungstenSteel)
                .inputItems(ELECTRIC_MOTOR_IV)
                .outputItems(ELECTRIC_PISTON_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_piston_luv")
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(PLATE, HSSS, 4)
                .inputItems(RING, HSSS, 4)
                .inputItems(ROUND, HSSS, 16)
                .inputItems(ROD, HSSS, 4)
                .inputItems(GEAR, HSSS)
                .inputItems(GEAR_SMALL, HSSS, 2)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy, L)
                .inputFluids(Lubricant, 250)
                .outputItems(ELECTRIC_PISTON_LuV)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_PISTON_IV.asStack())
                        .duration(900)
                        .EUt(VA[EV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_piston_zpm")
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(PLATE, Osmiridium, 4)
                .inputItems(RING, Osmiridium, 4)
                .inputItems(ROUND, Osmiridium, 16)
                .inputItems(ROD, Osmiridium, 4)
                .inputItems(GEAR, Osmiridium)
                .inputItems(GEAR_SMALL, Osmiridium, 2)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy, L * 2)
                .inputFluids(Lubricant, 500)
                .outputItems(ELECTRIC_PISTON_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_PISTON_LuV.asStack())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_piston_uv")
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(PLATE, Tritanium, 4)
                .inputItems(RING, Tritanium, 4)
                .inputItems(ROUND, Tritanium, 16)
                .inputItems(ROD, Tritanium, 4)
                .inputItems(GEAR, NaquadahAlloy)
                .inputItems(GEAR_SMALL, NaquadahAlloy, 2)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Lubricant, 1000)
                .inputFluids(Naquadria, L * 4)
                .outputItems(ELECTRIC_PISTON_UV)
                .stationResearch(b -> b
                        .researchStack(ELECTRIC_PISTON_ZPM.asStack())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        // Robot Arms Start
        // ---------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_lv", ROBOT_ARM_LV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Tin), 'R', new MaterialEntry(ROD, Steel), 'M',
                ELECTRIC_MOTOR_LV.asStack(), 'P', ELECTRIC_PISTON_LV.asStack(), 'X', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_mv", ROBOT_ARM_MV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Copper), 'R', new MaterialEntry(ROD, Aluminium), 'M',
                ELECTRIC_MOTOR_MV.asStack(), 'P', ELECTRIC_PISTON_MV.asStack(), 'X', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_hv", ROBOT_ARM_HV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Gold), 'R', new MaterialEntry(ROD, StainlessSteel), 'M',
                ELECTRIC_MOTOR_HV.asStack(), 'P', ELECTRIC_PISTON_HV.asStack(), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_ev", ROBOT_ARM_EV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Aluminium), 'R', new MaterialEntry(ROD, Titanium), 'M',
                ELECTRIC_MOTOR_EV.asStack(), 'P', ELECTRIC_PISTON_EV.asStack(), 'X', CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_iv", ROBOT_ARM_IV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(CABLE_GT_SINGLE, Tungsten), 'R', new MaterialEntry(ROD, TungstenSteel), 'M',
                ELECTRIC_MOTOR_IV.asStack(), 'P', ELECTRIC_PISTON_IV.asStack(), 'X', CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_lv")
                .inputItems(CABLE_GT_SINGLE, Tin, 3)
                .inputItems(ROD, Steel, 2)
                .inputItems(ELECTRIC_MOTOR_LV, 2)
                .inputItems(ELECTRIC_PISTON_LV)
                .inputItems(CustomTags.LV_CIRCUITS)
                .outputItems(ROBOT_ARM_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_mv")
                .inputItems(CABLE_GT_SINGLE, Copper, 3)
                .inputItems(ROD, Aluminium, 2)
                .inputItems(ELECTRIC_MOTOR_MV, 2)
                .inputItems(ELECTRIC_PISTON_MV)
                .inputItems(CustomTags.MV_CIRCUITS)
                .outputItems(ROBOT_ARM_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_hv")
                .inputItems(CABLE_GT_SINGLE, Gold, 3)
                .inputItems(ROD, StainlessSteel, 2)
                .inputItems(ELECTRIC_MOTOR_HV, 2)
                .inputItems(ELECTRIC_PISTON_HV)
                .inputItems(CustomTags.HV_CIRCUITS)
                .outputItems(ROBOT_ARM_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_ev")
                .inputItems(CABLE_GT_SINGLE, Aluminium, 3)
                .inputItems(ROD, Titanium, 2)
                .inputItems(ELECTRIC_MOTOR_EV, 2)
                .inputItems(ELECTRIC_PISTON_EV)
                .inputItems(CustomTags.EV_CIRCUITS)
                .outputItems(ROBOT_ARM_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_iv")
                .inputItems(CABLE_GT_SINGLE, Tungsten, 3)
                .inputItems(ROD, TungstenSteel, 2)
                .inputItems(ELECTRIC_MOTOR_IV, 2)
                .inputItems(ELECTRIC_PISTON_IV)
                .inputItems(CustomTags.IV_CIRCUITS)
                .outputItems(ROBOT_ARM_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("robot_arm_luv")
                .inputItems(ROD_LONG, HSSS, 4)
                .inputItems(GEAR, HSSS)
                .inputItems(GEAR_SMALL, HSSS, 3)
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(ELECTRIC_PISTON_LuV)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Lubricant, 250)
                .outputItems(ROBOT_ARM_LuV)
                .scannerResearch(b -> b
                        .researchStack(ROBOT_ARM_IV.asStack())
                        .duration(900)
                        .EUt(VA[EV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("robot_arm_zpm")
                .inputItems(ROD_LONG, Osmiridium, 4)
                .inputItems(GEAR, Osmiridium)
                .inputItems(GEAR_SMALL, Osmiridium, 3)
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(ELECTRIC_PISTON_ZPM)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 8)
                .inputFluids(Lubricant, 500)
                .outputItems(ROBOT_ARM_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ROBOT_ARM_LuV.asStack())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("robot_arm_uv")
                .inputItems(ROD_LONG, Tritanium, 4)
                .inputItems(GEAR, Tritanium)
                .inputItems(GEAR_SMALL, Tritanium, 3)
                .inputItems(ELECTRIC_MOTOR_UV, 2)
                .inputItems(ELECTRIC_PISTON_UV)
                .inputItems(CustomTags.UV_CIRCUITS)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy, L * 12)
                .inputFluids(Lubricant, 1000)
                .inputFluids(Naquadria, L * 4)
                .outputItems(ROBOT_ARM_UV)
                .stationResearch(b -> b
                        .researchStack(ROBOT_ARM_ZPM.asStack())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        // Field Generators Start
        // ---------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_lv", FIELD_GENERATOR_LV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(WIRE_GT_QUADRUPLE, ManganesePhosphide), 'P',
                new MaterialEntry(PLATE, Steel), 'G', new MaterialEntry(GEM, EnderPearl), 'X',
                CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_mv", FIELD_GENERATOR_MV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(WIRE_GT_QUADRUPLE, MagnesiumDiboride), 'P',
                new MaterialEntry(PLATE, Aluminium), 'G', new MaterialEntry(GEM, EnderEye), 'X',
                CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_hv", FIELD_GENERATOR_HV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(WIRE_GT_QUADRUPLE, MercuryBariumCalciumCuprate), 'P',
                new MaterialEntry(PLATE, StainlessSteel), 'G', QUANTUM_EYE.asStack(), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_ev", FIELD_GENERATOR_EV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(WIRE_GT_QUADRUPLE, UraniumTriplatinum), 'P',
                new MaterialEntry(PLATE_DOUBLE, Titanium), 'G', new MaterialEntry(GEM, NetherStar), 'X',
                CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_iv", FIELD_GENERATOR_IV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(WIRE_GT_QUADRUPLE, SamariumIronArsenicOxide), 'P',
                new MaterialEntry(PLATE_DOUBLE, TungstenSteel), 'G', QUANTUM_STAR.asStack(), 'X',
                CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_lv")
                .inputItems(GEM, EnderPearl)
                .inputItems(PLATE, Steel, 2)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, ManganesePhosphide, 4)
                .outputItems(FIELD_GENERATOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_mv")
                .inputItems(GEM, EnderEye)
                .inputItems(PLATE, Aluminium, 2)
                .inputItems(CustomTags.MV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, MagnesiumDiboride, 4)
                .outputItems(FIELD_GENERATOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_hv")
                .inputItems(QUANTUM_EYE)
                .inputItems(PLATE, StainlessSteel, 2)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, MercuryBariumCalciumCuprate, 4)
                .outputItems(FIELD_GENERATOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_ev")
                .inputItems(GEM, NetherStar)
                .inputItems(PLATE_DOUBLE, Titanium, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, UraniumTriplatinum, 4)
                .outputItems(FIELD_GENERATOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_iv")
                .inputItems(QUANTUM_STAR)
                .inputItems(PLATE_DOUBLE, TungstenSteel, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(WIRE_GT_QUADRUPLE, SamariumIronArsenicOxide, 4)
                .outputItems(FIELD_GENERATOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("field_generator_luv")
                .inputItems(FRAME_GT, HSSS)
                .inputItems(PLATE, HSSS, 6)
                .inputItems(QUANTUM_STAR)
                .inputItems(EMITTER_LuV, 2)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(WIRE_FINE, IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(WIRE_FINE, IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .outputItems(FIELD_GENERATOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(FIELD_GENERATOR_IV.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("field_generator_zpm")
                .inputItems(FRAME_GT, NaquadahAlloy)
                .inputItems(PLATE, NaquadahAlloy, 6)
                .inputItems(QUANTUM_STAR)
                .inputItems(EMITTER_ZPM, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(WIRE_FINE, UraniumRhodiumDinaquadide, 64)
                .inputItems(WIRE_FINE, UraniumRhodiumDinaquadide, 64)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 8)
                .outputItems(FIELD_GENERATOR_ZPM)
                .stationResearch(b -> b
                        .researchStack(FIELD_GENERATOR_LuV.asStack())
                        .CWUt(4))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("field_generator_uv")
                .inputItems(FRAME_GT, Tritanium)
                .inputItems(PLATE, Tritanium, 6)
                .inputItems(GRAVI_STAR)
                .inputItems(EMITTER_UV, 2)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(WIRE_FINE, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(WIRE_FINE, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy, L * 12)
                .inputFluids(Naquadria, L * 4)
                .outputItems(FIELD_GENERATOR_UV)
                .stationResearch(b -> b
                        .researchStack(FIELD_GENERATOR_ZPM.asStack())
                        .CWUt(48)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        // Sensors
        // Start-------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_lv", SENSOR_LV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(PLATE, Steel), 'R', new MaterialEntry(ROD, Brass), 'G',
                new MaterialEntry(GEM, Quartzite), 'X', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_mv", SENSOR_MV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(PLATE, Aluminium), 'R', new MaterialEntry(ROD, Electrum), 'G',
                new MaterialEntry(GEM_FLAWLESS, Emerald), 'X', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_hv", SENSOR_HV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(PLATE, StainlessSteel), 'R', new MaterialEntry(ROD, Chromium), 'G',
                new MaterialEntry(GEM, EnderEye), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_ev", SENSOR_EV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(PLATE, Titanium), 'R', new MaterialEntry(ROD, Platinum), 'G',
                QUANTUM_EYE.asStack(), 'X', CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_iv", SENSOR_IV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(PLATE, TungstenSteel), 'R', new MaterialEntry(ROD, Iridium), 'G',
                QUANTUM_STAR.asStack(), 'X', CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_lv")
                .inputItems(ROD, Brass)
                .inputItems(PLATE, Steel, 4)
                .inputItems(CustomTags.LV_CIRCUITS)
                .inputItems(GEM, Quartzite)
                .outputItems(SENSOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_mv")
                .inputItems(ROD, Electrum)
                .inputItems(PLATE, Aluminium, 4)
                .inputItems(CustomTags.MV_CIRCUITS)
                .inputItems(GEM_FLAWLESS, Emerald)
                .outputItems(SENSOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_hv")
                .inputItems(ROD, Chromium)
                .inputItems(PLATE, StainlessSteel, 4)
                .inputItems(CustomTags.HV_CIRCUITS)
                .inputItems(GEM, EnderEye)
                .outputItems(SENSOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_ev")
                .inputItems(ROD, Platinum)
                .inputItems(PLATE, Titanium, 4)
                .inputItems(CustomTags.EV_CIRCUITS)
                .inputItems(QUANTUM_EYE)
                .outputItems(SENSOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_iv")
                .inputItems(ROD, Iridium)
                .inputItems(PLATE, TungstenSteel, 4)
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(QUANTUM_STAR)
                .outputItems(SENSOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("sensor_luv")
                .inputItems(FRAME_GT, HSSS)
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(PLATE, Ruridit, 4)
                .inputItems(QUANTUM_STAR)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(FOIL, Palladium, 64)
                .inputItems(FOIL, Palladium, 32)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 2)
                .outputItems(SENSOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(SENSOR_IV.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("sensor_zpm")
                .inputItems(FRAME_GT, NaquadahAlloy)
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(PLATE, Osmiridium, 4)
                .inputItems(QUANTUM_STAR, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(FOIL, Trinium, 64)
                .inputItems(FOIL, Trinium, 32)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .outputItems(SENSOR_ZPM)
                .stationResearch(b -> b
                        .researchStack(SENSOR_LuV.asStack())
                        .CWUt(4))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("sensor_uv")
                .inputItems(FRAME_GT, Tritanium)
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(PLATE, Tritanium, 4)
                .inputItems(GRAVI_STAR)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(FOIL, Naquadria, 64)
                .inputItems(FOIL, Naquadria, 32)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy, L * 8)
                .inputFluids(Naquadria, L * 4)
                .outputItems(SENSOR_UV)
                .stationResearch(b -> b
                        .researchStack(SENSOR_ZPM.asStack())
                        .CWUt(48)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);

        // Emitters
        // Start------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_lv", EMITTER_LV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(ROD, Brass), 'C', new MaterialEntry(CABLE_GT_SINGLE, Tin), 'G',
                new MaterialEntry(GEM, Quartzite), 'X', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_mv", EMITTER_MV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(ROD, Electrum), 'C', new MaterialEntry(CABLE_GT_SINGLE, Copper), 'G',
                new MaterialEntry(GEM_FLAWLESS, Emerald), 'X', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_hv", EMITTER_HV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(ROD, Chromium), 'C', new MaterialEntry(CABLE_GT_SINGLE, Gold), 'G',
                new MaterialEntry(GEM, EnderEye), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_ev", EMITTER_EV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(ROD, Platinum), 'C', new MaterialEntry(CABLE_GT_SINGLE, Aluminium), 'G',
                QUANTUM_EYE.asStack(), 'X', CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_iv", EMITTER_IV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(ROD, Iridium), 'C', new MaterialEntry(CABLE_GT_SINGLE, Tungsten), 'G',
                QUANTUM_STAR.asStack(), 'X', CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_lv")
                .inputItems(ROD, Brass, 4)
                .inputItems(CABLE_GT_SINGLE, Tin, 2)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .inputItems(GEM, Quartzite)
                .circuitMeta(1)
                .outputItems(EMITTER_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_mv")
                .inputItems(ROD, Electrum, 4)
                .inputItems(CABLE_GT_SINGLE, Copper, 2)
                .inputItems(CustomTags.MV_CIRCUITS, 2)
                .inputItems(GEM_FLAWLESS, Emerald)
                .circuitMeta(1)
                .outputItems(EMITTER_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_hv")
                .inputItems(ROD, Chromium, 4)
                .inputItems(CABLE_GT_SINGLE, Gold, 2)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(GEM, EnderEye)
                .circuitMeta(1)
                .outputItems(EMITTER_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_ev")
                .inputItems(ROD, Platinum, 4)
                .inputItems(CABLE_GT_SINGLE, Aluminium, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(QUANTUM_EYE)
                .circuitMeta(1)
                .outputItems(EMITTER_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_iv")
                .inputItems(ROD, Iridium, 4)
                .inputItems(CABLE_GT_SINGLE, Tungsten, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(QUANTUM_STAR)
                .circuitMeta(1)
                .outputItems(EMITTER_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("emitter_luv")
                .inputItems(FRAME_GT, HSSS)
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(ROD_LONG, Ruridit, 4)
                .inputItems(QUANTUM_STAR)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(FOIL, Palladium, 64)
                .inputItems(FOIL, Palladium, 32)
                .inputItems(CABLE_GT_SINGLE, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 2)
                .outputItems(EMITTER_LuV)
                .scannerResearch(b -> b
                        .researchStack(EMITTER_IV.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("emitter_zpm")
                .inputItems(FRAME_GT, NaquadahAlloy)
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(ROD_LONG, Osmiridium, 4)
                .inputItems(QUANTUM_STAR, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(FOIL, Trinium, 64)
                .inputItems(FOIL, Trinium, 32)
                .inputItems(CABLE_GT_SINGLE, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .outputItems(EMITTER_ZPM)
                .stationResearch(b -> b
                        .researchStack(EMITTER_LuV.asStack())
                        .CWUt(8))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("emitter_uv")
                .inputItems(FRAME_GT, Tritanium)
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(ROD_LONG, Tritanium, 4)
                .inputItems(GRAVI_STAR)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(FOIL, Naquadria, 64)
                .inputItems(FOIL, Naquadria, 32)
                .inputItems(CABLE_GT_SINGLE, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy, L * 8)
                .inputFluids(Naquadria, L * 4)
                .outputItems(EMITTER_UV)
                .stationResearch(b -> b
                        .researchStack(EMITTER_ZPM.asStack())
                        .CWUt(48)
                        .EUt(VA[ZPM]))
                .duration(600).EUt(100000)
                .addMaterialInfo(true).save(provider);
    }
}
