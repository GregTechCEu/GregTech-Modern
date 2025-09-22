package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
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
                "WMW", "RWC", 'C', new MaterialEntry(cableGtSingle, Tin), 'W',
                new MaterialEntry(wireGtSingle, Copper), 'R', new MaterialEntry(rod, Steel), 'M',
                new MaterialEntry(rod, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_lv_iron", ELECTRIC_MOTOR_LV.asStack(),
                "CWR", "WMW", "RWC", 'C', new MaterialEntry(cableGtSingle, Tin), 'W',
                new MaterialEntry(wireGtSingle, Copper), 'R', new MaterialEntry(rod, Iron), 'M',
                new MaterialEntry(rod, IronMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_mv", ELECTRIC_MOTOR_MV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(cableGtSingle, Copper), 'W',
                new MaterialEntry(wireGtDouble, Cupronickel), 'R', new MaterialEntry(rod, Aluminium), 'M',
                new MaterialEntry(rod, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_hv", ELECTRIC_MOTOR_HV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(cableGtDouble, Silver), 'W',
                new MaterialEntry(wireGtDouble, Electrum), 'R', new MaterialEntry(rod, StainlessSteel), 'M',
                new MaterialEntry(rod, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_ev", ELECTRIC_MOTOR_EV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(cableGtDouble, Aluminium), 'W',
                new MaterialEntry(wireGtDouble, Kanthal), 'R', new MaterialEntry(rod, Titanium), 'M',
                new MaterialEntry(rod, NeodymiumMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_motor_iv", ELECTRIC_MOTOR_IV.asStack(), "CWR",
                "WMW", "RWC", 'C', new MaterialEntry(cableGtDouble, Tungsten), 'W',
                new MaterialEntry(wireGtDouble, Graphene), 'R', new MaterialEntry(rod, TungstenSteel), 'M',
                new MaterialEntry(rod, NeodymiumMagnetic));

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_lv_iron")
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(rod, Iron, 2)
                .inputItems(rod, IronMagnetic)
                .inputItems(wireGtSingle, Copper, 4)
                .outputItems(ELECTRIC_MOTOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_lv_steel")
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(rod, Steel, 2)
                .inputItems(rod, SteelMagnetic)
                .inputItems(wireGtSingle, Copper, 4)
                .outputItems(ELECTRIC_MOTOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_mv")
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(rod, Aluminium, 2)
                .inputItems(rod, SteelMagnetic)
                .inputItems(wireGtDouble, Cupronickel, 4)
                .outputItems(ELECTRIC_MOTOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_hv")
                .inputItems(cableGtDouble, Silver, 2)
                .inputItems(rod, StainlessSteel, 2)
                .inputItems(rod, SteelMagnetic)
                .inputItems(wireGtDouble, Electrum, 4)
                .outputItems(ELECTRIC_MOTOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_ev")
                .inputItems(cableGtDouble, Aluminium, 2)
                .inputItems(rod, Titanium, 2)
                .inputItems(rod, NeodymiumMagnetic)
                .inputItems(wireGtDouble, Kanthal, 4)
                .outputItems(ELECTRIC_MOTOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_motor_iv")
                .inputItems(cableGtDouble, Tungsten, 2)
                .inputItems(rod, TungstenSteel, 2)
                .inputItems(rod, NeodymiumMagnetic)
                .inputItems(wireGtDouble, Graphene, 4)
                .outputItems(ELECTRIC_MOTOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_motor_luv")
                .inputItems(rodLong, SamariumMagnetic)
                .inputItems(rodLong, HSSS, 2)
                .inputItems(ring, HSSS, 2)
                .inputItems(round, HSSS, 4)
                .inputItems(wireFine, Ruridit, 64)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
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
                .inputItems(rodLong, SamariumMagnetic)
                .inputItems(rodLong, Osmiridium, 4)
                .inputItems(ring, Osmiridium, 4)
                .inputItems(round, Osmiridium, 8)
                .inputItems(wireFine, Europium, 64)
                .inputItems(wireFine, Europium, 32)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
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
                .inputItems(rodLong, SamariumMagnetic)
                .inputItems(rodLong, Tritanium, 4)
                .inputItems(ring, Tritanium, 4)
                .inputItems(round, Tritanium, 8)
                .inputItems(wireFine, Americium, 64)
                .inputItems(wireFine, Americium, 64)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
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
                    'R', new MaterialEntry(plate, material), 'C', new MaterialEntry(cableGtSingle, Tin), 'M',
                    ELECTRIC_MOTOR_LV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_mv_%s", name), CONVEYOR_MODULE_MV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(plate, material), 'C', new MaterialEntry(cableGtSingle, Copper), 'M',
                    ELECTRIC_MOTOR_MV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_hv_%s", name), CONVEYOR_MODULE_HV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(plate, material), 'C', new MaterialEntry(cableGtSingle, Gold), 'M',
                    ELECTRIC_MOTOR_HV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("conveyor_module_ev_%s", name), CONVEYOR_MODULE_EV.asStack(), "RRR", "MCM", "RRR",
                    'R', new MaterialEntry(plate, material), 'C', new MaterialEntry(cableGtSingle, Aluminium),
                    'M', ELECTRIC_MOTOR_EV.asStack());
            if (!materialEntry.getValue().equals(Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, material.equals(SiliconeRubber),
                        String.format("conveyor_module_iv_%s", materialEntry.getKey()), CONVEYOR_MODULE_IV.asStack(),
                        "RRR", "MCM", "RRR", 'R', new MaterialEntry(plate, material), 'C',
                        new MaterialEntry(cableGtSingle, Tungsten), 'M', ELECTRIC_MOTOR_IV.asStack());

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_lv_" + name)
                    .inputItems(cableGtSingle, Tin)
                    .inputItems(ELECTRIC_MOTOR_LV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_LV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_mv_" + name)
                    .inputItems(cableGtSingle, Copper)
                    .inputItems(ELECTRIC_MOTOR_MV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_MV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_hv_" + name)
                    .inputItems(cableGtSingle, Gold)
                    .inputItems(ELECTRIC_MOTOR_HV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_HV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_ev_" + name)
                    .inputItems(cableGtSingle, Aluminium)
                    .inputItems(ELECTRIC_MOTOR_EV, 2)
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_EV)
                    .duration(100).EUt(VA[LV]).save(provider);

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder("conveyor_module_iv_" + name)
                        .inputItems(cableGtSingle, Tungsten)
                        .inputItems(ELECTRIC_MOTOR_IV, 2)
                        .inputFluids(materialEntry.getValue().getFluid(L * 6))
                        .circuitMeta(1)
                        .outputItems(CONVEYOR_MODULE_IV)
                        .duration(100).EUt(VA[LV]).save(provider);

            // Pumps
            // Start---------------------------------------------------------------------------------------------------
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_lv_%s", name), ELECTRIC_PUMP_LV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(screw, Tin), 'X', new MaterialEntry(rotor, Tin), 'P',
                    new MaterialEntry(pipeNormalFluid, Bronze), 'R', new MaterialEntry(ring, material), 'C',
                    new MaterialEntry(cableGtSingle, Tin), 'M', ELECTRIC_MOTOR_LV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_mv_%s", name), ELECTRIC_PUMP_MV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(screw, Bronze), 'X', new MaterialEntry(rotor, Bronze), 'P',
                    new MaterialEntry(pipeNormalFluid, Steel), 'R', new MaterialEntry(ring, material), 'C',
                    new MaterialEntry(cableGtSingle, Copper), 'M', ELECTRIC_MOTOR_MV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_hv_%s", name), ELECTRIC_PUMP_HV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(screw, Steel), 'X', new MaterialEntry(rotor, Steel), 'P',
                    new MaterialEntry(pipeNormalFluid, StainlessSteel), 'R', new MaterialEntry(ring, material),
                    'C', new MaterialEntry(cableGtSingle, Gold), 'M', ELECTRIC_MOTOR_HV.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, material.equals(Rubber),
                    String.format("electric_pump_ev_%s", name), ELECTRIC_PUMP_EV.asStack(), "SXR", "dPw", "RMC", 'S',
                    new MaterialEntry(screw, StainlessSteel), 'X', new MaterialEntry(rotor, StainlessSteel), 'P',
                    new MaterialEntry(pipeNormalFluid, Titanium), 'R', new MaterialEntry(ring, material), 'C',
                    new MaterialEntry(cableGtSingle, Aluminium), 'M', ELECTRIC_MOTOR_EV.asStack());
            if (!material.equals(Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, material.equals(SiliconeRubber),
                        String.format("electric_pump_iv_%s", name), ELECTRIC_PUMP_IV.asStack(), "SXR", "dPw", "RMC",
                        'S', new MaterialEntry(screw, TungstenSteel), 'X',
                        new MaterialEntry(rotor, TungstenSteel), 'P',
                        new MaterialEntry(pipeNormalFluid, TungstenSteel), 'R', new MaterialEntry(ring, material),
                        'C', new MaterialEntry(cableGtSingle, Tungsten), 'M', ELECTRIC_MOTOR_IV.asStack());

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_lv_" + name)
                    .inputItems(cableGtSingle, Tin)
                    .inputItems(pipeNormalFluid, Bronze)
                    .inputItems(screw, Tin)
                    .inputItems(rotor, Tin)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_LV)
                    .outputItems(ELECTRIC_PUMP_LV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_mv_" + name)
                    .inputItems(cableGtSingle, Copper)
                    .inputItems(pipeNormalFluid, Steel)
                    .inputItems(screw, Bronze)
                    .inputItems(rotor, Bronze)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_MV)
                    .outputItems(ELECTRIC_PUMP_MV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_hv_" + name)
                    .inputItems(cableGtSingle, Gold)
                    .inputItems(pipeNormalFluid, StainlessSteel)
                    .inputItems(screw, Steel)
                    .inputItems(rotor, Steel)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_HV)
                    .outputItems(ELECTRIC_PUMP_HV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder("electric_pump_ev_" + name)
                    .inputItems(cableGtSingle, Aluminium)
                    .inputItems(pipeNormalFluid, Titanium)
                    .inputItems(screw, StainlessSteel)
                    .inputItems(rotor, StainlessSteel)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_EV)
                    .outputItems(ELECTRIC_PUMP_EV)
                    .duration(100).EUt(VA[LV]).save(provider);

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder("electric_pump_iv_" + name)
                        .inputItems(cableGtSingle, Tungsten)
                        .inputItems(pipeNormalFluid, TungstenSteel)
                        .inputItems(screw, TungstenSteel)
                        .inputItems(rotor, TungstenSteel)
                        .inputItems(ring, materialEntry.getValue(), 2)
                        .inputItems(ELECTRIC_MOTOR_IV)
                        .outputItems(ELECTRIC_PUMP_IV)
                        .duration(100).EUt(VA[LV]).save(provider);
        }

        ASSEMBLY_LINE_RECIPES.recipeBuilder("conveyor_module_luv")
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(plate, HSSS, 2)
                .inputItems(ring, HSSS, 4)
                .inputItems(round, HSSS, 16)
                .inputItems(screw, HSSS, 4)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
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
                .inputItems(plate, Osmiridium, 2)
                .inputItems(ring, Osmiridium, 4)
                .inputItems(round, Osmiridium, 16)
                .inputItems(screw, Osmiridium, 4)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
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
                .inputItems(plate, Tritanium, 2)
                .inputItems(ring, Tritanium, 4)
                .inputItems(round, Tritanium, 16)
                .inputItems(screw, Tritanium, 4)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
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
                .inputItems(pipeSmallFluid, NiobiumTitanium)
                .inputItems(plate, HSSS, 2)
                .inputItems(screw, HSSS, 8)
                .inputItems(ring, SiliconeRubber, 4)
                .inputItems(rotor, HSSS)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
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
                .inputItems(pipeNormalFluid, Polybenzimidazole)
                .inputItems(plate, Osmiridium, 2)
                .inputItems(screw, Osmiridium, 8)
                .inputItems(ring, SiliconeRubber, 8)
                .inputItems(rotor, Osmiridium)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
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
                .inputItems(pipeLargeFluid, Naquadah)
                .inputItems(plate, Tritanium, 2)
                .inputItems(screw, Tritanium, 8)
                .inputItems(ring, SiliconeRubber, 16)
                .inputItems(rotor, NaquadahAlloy)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
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
                "dPw", " E ", 'S', new MaterialEntry(screw, Steel), 'D', COVER_ITEM_DETECTOR.asStack(), 'P',
                new MaterialEntry(pipeNormalItem, Brass), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder("cover_item_voiding")
                .inputItems(screw, Steel, 2)
                .inputItems(COVER_ITEM_DETECTOR)
                .inputItems(pipeNormalItem, Brass)
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
                "dPw", " E ", 'S', new MaterialEntry(screw, Steel), 'D', COVER_FLUID_DETECTOR.asStack(), 'P',
                new MaterialEntry(pipeNormalFluid, Bronze), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder("cover_fluid_voiding")
                .inputItems(screw, Steel, 2)
                .inputItems(COVER_FLUID_DETECTOR)
                .inputItems(pipeNormalFluid, Bronze)
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
                "CRR", "CMG", 'P', new MaterialEntry(plate, Steel), 'C', new MaterialEntry(cableGtSingle, Tin),
                'R', new MaterialEntry(rod, Steel), 'G', new MaterialEntry(gearSmall, Steel), 'M',
                ELECTRIC_MOTOR_LV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_mv", ELECTRIC_PISTON_MV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(plate, Aluminium), 'C',
                new MaterialEntry(cableGtSingle, Copper), 'R', new MaterialEntry(rod, Aluminium), 'G',
                new MaterialEntry(gearSmall, Aluminium), 'M', ELECTRIC_MOTOR_MV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_hv", ELECTRIC_PISTON_HV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(plate, StainlessSteel), 'C',
                new MaterialEntry(cableGtSingle, Gold), 'R', new MaterialEntry(rod, StainlessSteel), 'G',
                new MaterialEntry(gearSmall, StainlessSteel), 'M', ELECTRIC_MOTOR_HV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_ev", ELECTRIC_PISTON_EV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(plate, Titanium), 'C',
                new MaterialEntry(cableGtSingle, Aluminium), 'R', new MaterialEntry(rod, Titanium), 'G',
                new MaterialEntry(gearSmall, Titanium), 'M', ELECTRIC_MOTOR_EV.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, true, "electric_piston_iv", ELECTRIC_PISTON_IV.asStack(), "PPP",
                "CRR", "CMG", 'P', new MaterialEntry(plate, TungstenSteel), 'C',
                new MaterialEntry(cableGtSingle, Tungsten), 'R', new MaterialEntry(rod, TungstenSteel), 'G',
                new MaterialEntry(gearSmall, TungstenSteel), 'M', ELECTRIC_MOTOR_IV.asStack());

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_lv")
                .inputItems(rod, Steel, 2)
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(plate, Steel, 3)
                .inputItems(gearSmall, Steel)
                .inputItems(ELECTRIC_MOTOR_LV)
                .outputItems(ELECTRIC_PISTON_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_mv")
                .inputItems(rod, Aluminium, 2)
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(plate, Aluminium, 3)
                .inputItems(gearSmall, Aluminium)
                .inputItems(ELECTRIC_MOTOR_MV)
                .outputItems(ELECTRIC_PISTON_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_hv")
                .inputItems(rod, StainlessSteel, 2)
                .inputItems(cableGtSingle, Gold, 2)
                .inputItems(plate, StainlessSteel, 3)
                .inputItems(gearSmall, StainlessSteel)
                .inputItems(ELECTRIC_MOTOR_HV)
                .outputItems(ELECTRIC_PISTON_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_ev")
                .inputItems(rod, Titanium, 2)
                .inputItems(cableGtSingle, Aluminium, 2)
                .inputItems(plate, Titanium, 3)
                .inputItems(gearSmall, Titanium)
                .inputItems(ELECTRIC_MOTOR_EV)
                .outputItems(ELECTRIC_PISTON_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("electric_piston_iv")
                .inputItems(rod, TungstenSteel, 2)
                .inputItems(cableGtSingle, Tungsten, 2)
                .inputItems(plate, TungstenSteel, 3)
                .inputItems(gearSmall, TungstenSteel)
                .inputItems(ELECTRIC_MOTOR_IV)
                .outputItems(ELECTRIC_PISTON_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("electric_piston_luv")
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(plate, HSSS, 4)
                .inputItems(ring, HSSS, 4)
                .inputItems(round, HSSS, 16)
                .inputItems(rod, HSSS, 4)
                .inputItems(gear, HSSS)
                .inputItems(gearSmall, HSSS, 2)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
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
                .inputItems(plate, Osmiridium, 4)
                .inputItems(ring, Osmiridium, 4)
                .inputItems(round, Osmiridium, 16)
                .inputItems(rod, Osmiridium, 4)
                .inputItems(gear, Osmiridium)
                .inputItems(gearSmall, Osmiridium, 2)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
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
                .inputItems(plate, Tritanium, 4)
                .inputItems(ring, Tritanium, 4)
                .inputItems(round, Tritanium, 16)
                .inputItems(rod, Tritanium, 4)
                .inputItems(gear, NaquadahAlloy)
                .inputItems(gearSmall, NaquadahAlloy, 2)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
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
                'C', new MaterialEntry(cableGtSingle, Tin), 'R', new MaterialEntry(rod, Steel), 'M',
                ELECTRIC_MOTOR_LV.asStack(), 'P', ELECTRIC_PISTON_LV.asStack(), 'X', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_mv", ROBOT_ARM_MV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(cableGtSingle, Copper), 'R', new MaterialEntry(rod, Aluminium), 'M',
                ELECTRIC_MOTOR_MV.asStack(), 'P', ELECTRIC_PISTON_MV.asStack(), 'X', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_hv", ROBOT_ARM_HV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(cableGtSingle, Gold), 'R', new MaterialEntry(rod, StainlessSteel), 'M',
                ELECTRIC_MOTOR_HV.asStack(), 'P', ELECTRIC_PISTON_HV.asStack(), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_ev", ROBOT_ARM_EV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(cableGtSingle, Aluminium), 'R', new MaterialEntry(rod, Titanium), 'M',
                ELECTRIC_MOTOR_EV.asStack(), 'P', ELECTRIC_PISTON_EV.asStack(), 'X', CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "robot_arm_iv", ROBOT_ARM_IV.asStack(), "CCC", "MRM", "PXR",
                'C', new MaterialEntry(cableGtSingle, Tungsten), 'R', new MaterialEntry(rod, TungstenSteel), 'M',
                ELECTRIC_MOTOR_IV.asStack(), 'P', ELECTRIC_PISTON_IV.asStack(), 'X', CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_lv")
                .inputItems(cableGtSingle, Tin, 3)
                .inputItems(rod, Steel, 2)
                .inputItems(ELECTRIC_MOTOR_LV, 2)
                .inputItems(ELECTRIC_PISTON_LV)
                .inputItems(CustomTags.LV_CIRCUITS)
                .outputItems(ROBOT_ARM_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_mv")
                .inputItems(cableGtSingle, Copper, 3)
                .inputItems(rod, Aluminium, 2)
                .inputItems(ELECTRIC_MOTOR_MV, 2)
                .inputItems(ELECTRIC_PISTON_MV)
                .inputItems(CustomTags.MV_CIRCUITS)
                .outputItems(ROBOT_ARM_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_hv")
                .inputItems(cableGtSingle, Gold, 3)
                .inputItems(rod, StainlessSteel, 2)
                .inputItems(ELECTRIC_MOTOR_HV, 2)
                .inputItems(ELECTRIC_PISTON_HV)
                .inputItems(CustomTags.HV_CIRCUITS)
                .outputItems(ROBOT_ARM_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_ev")
                .inputItems(cableGtSingle, Aluminium, 3)
                .inputItems(rod, Titanium, 2)
                .inputItems(ELECTRIC_MOTOR_EV, 2)
                .inputItems(ELECTRIC_PISTON_EV)
                .inputItems(CustomTags.EV_CIRCUITS)
                .outputItems(ROBOT_ARM_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("robot_arm_iv")
                .inputItems(cableGtSingle, Tungsten, 3)
                .inputItems(rod, TungstenSteel, 2)
                .inputItems(ELECTRIC_MOTOR_IV, 2)
                .inputItems(ELECTRIC_PISTON_IV)
                .inputItems(CustomTags.IV_CIRCUITS)
                .outputItems(ROBOT_ARM_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("robot_arm_luv")
                .inputItems(rodLong, HSSS, 4)
                .inputItems(gear, HSSS)
                .inputItems(gearSmall, HSSS, 3)
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(ELECTRIC_PISTON_LuV)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
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
                .inputItems(rodLong, Osmiridium, 4)
                .inputItems(gear, Osmiridium)
                .inputItems(gearSmall, Osmiridium, 3)
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(ELECTRIC_PISTON_ZPM)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 4)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
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
                .inputItems(rodLong, Tritanium, 4)
                .inputItems(gear, Tritanium)
                .inputItems(gearSmall, Tritanium, 3)
                .inputItems(ELECTRIC_MOTOR_UV, 2)
                .inputItems(ELECTRIC_PISTON_UV)
                .inputItems(CustomTags.UV_CIRCUITS)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
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
                "XGX", "WPW", 'W', new MaterialEntry(wireGtQuadruple, ManganesePhosphide), 'P',
                new MaterialEntry(plate, Steel), 'G', new MaterialEntry(gem, EnderPearl), 'X',
                CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_mv", FIELD_GENERATOR_MV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(wireGtQuadruple, MagnesiumDiboride), 'P',
                new MaterialEntry(plate, Aluminium), 'G', new MaterialEntry(gem, EnderEye), 'X',
                CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_hv", FIELD_GENERATOR_HV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(wireGtQuadruple, MercuryBariumCalciumCuprate), 'P',
                new MaterialEntry(plate, StainlessSteel), 'G', QUANTUM_EYE.asStack(), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_ev", FIELD_GENERATOR_EV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(wireGtQuadruple, UraniumTriplatinum), 'P',
                new MaterialEntry(plateDouble, Titanium), 'G', new MaterialEntry(gem, NetherStar), 'X',
                CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "field_generator_iv", FIELD_GENERATOR_IV.asStack(), "WPW",
                "XGX", "WPW", 'W', new MaterialEntry(wireGtQuadruple, SamariumIronArsenicOxide), 'P',
                new MaterialEntry(plateDouble, TungstenSteel), 'G', QUANTUM_STAR.asStack(), 'X',
                CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_lv")
                .inputItems(gem, EnderPearl)
                .inputItems(plate, Steel, 2)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, ManganesePhosphide, 4)
                .outputItems(FIELD_GENERATOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_mv")
                .inputItems(gem, EnderEye)
                .inputItems(plate, Aluminium, 2)
                .inputItems(CustomTags.MV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, MagnesiumDiboride, 4)
                .outputItems(FIELD_GENERATOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_hv")
                .inputItems(QUANTUM_EYE)
                .inputItems(plate, StainlessSteel, 2)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, MercuryBariumCalciumCuprate, 4)
                .outputItems(FIELD_GENERATOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_ev")
                .inputItems(gem, NetherStar)
                .inputItems(plateDouble, Titanium, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, UraniumTriplatinum, 4)
                .outputItems(FIELD_GENERATOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("field_generator_iv")
                .inputItems(QUANTUM_STAR)
                .inputItems(plateDouble, TungstenSteel, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(wireGtQuadruple, SamariumIronArsenicOxide, 4)
                .outputItems(FIELD_GENERATOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("field_generator_luv")
                .inputItems(frameGt, HSSS)
                .inputItems(plate, HSSS, 6)
                .inputItems(QUANTUM_STAR)
                .inputItems(EMITTER_LuV, 2)
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .outputItems(FIELD_GENERATOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(FIELD_GENERATOR_IV.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("field_generator_zpm")
                .inputItems(frameGt, NaquadahAlloy)
                .inputItems(plate, NaquadahAlloy, 6)
                .inputItems(QUANTUM_STAR)
                .inputItems(EMITTER_ZPM, 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(wireFine, UraniumRhodiumDinaquadide, 64)
                .inputItems(wireFine, UraniumRhodiumDinaquadide, 64)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 8)
                .outputItems(FIELD_GENERATOR_ZPM)
                .stationResearch(b -> b
                        .researchStack(FIELD_GENERATOR_LuV.asStack())
                        .CWUt(4))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("field_generator_uv")
                .inputItems(frameGt, Tritanium)
                .inputItems(plate, Tritanium, 6)
                .inputItems(GRAVI_STAR)
                .inputItems(EMITTER_UV, 2)
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
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
                new MaterialEntry(plate, Steel), 'R', new MaterialEntry(rod, Brass), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(LV), 'X', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_mv", SENSOR_MV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(plate, Aluminium), 'R', new MaterialEntry(rod, Electrum), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(MV), 'X', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_hv", SENSOR_HV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(plate, StainlessSteel), 'R', new MaterialEntry(rod, Chromium), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(HV), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_ev", SENSOR_EV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(plate, Titanium), 'R', new MaterialEntry(rod, Platinum), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(EV), 'X', CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "sensor_iv", SENSOR_IV.asStack(), "P G", "PR ", "XPP", 'P',
                new MaterialEntry(plate, TungstenSteel), 'R', new MaterialEntry(rod, Iridium), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(IV), 'X', CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_lv")
                .inputItems(rod, Brass)
                .inputItems(plate, Steel, 4)
                .inputItems(CustomTags.LV_CIRCUITS)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(LV))
                .outputItems(SENSOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_mv")
                .inputItems(rod, Electrum)
                .inputItems(plate, Aluminium, 4)
                .inputItems(CustomTags.MV_CIRCUITS)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(MV))
                .outputItems(SENSOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_hv")
                .inputItems(rod, Chromium)
                .inputItems(plate, StainlessSteel, 4)
                .inputItems(CustomTags.HV_CIRCUITS)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(HV))
                .outputItems(SENSOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_ev")
                .inputItems(rod, Platinum)
                .inputItems(plate, Titanium, 4)
                .inputItems(CustomTags.EV_CIRCUITS)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(EV))
                .outputItems(SENSOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("sensor_iv")
                .inputItems(rod, Iridium)
                .inputItems(plate, TungstenSteel, 4)
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(IV))
                .outputItems(SENSOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("sensor_luv")
                .inputItems(frameGt, HSSS)
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(plate, Ruridit, 4)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(LuV))
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(foil, Palladium, 64)
                .inputItems(foil, Palladium, 32)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 2)
                .outputItems(SENSOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(SENSOR_IV.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("sensor_zpm")
                .inputItems(frameGt, NaquadahAlloy)
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(plate, Osmiridium, 4)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(ZPM), 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(foil, Trinium, 64)
                .inputItems(foil, Trinium, 32)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .outputItems(SENSOR_ZPM)
                .stationResearch(b -> b
                        .researchStack(SENSOR_LuV.asStack())
                        .CWUt(4))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("sensor_uv")
                .inputItems(frameGt, Tritanium)
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(plate, Tritanium, 4)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(UV))
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(foil, Naquadria, 64)
                .inputItems(foil, Naquadria, 32)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
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
                'R', new MaterialEntry(rod, Brass), 'C', new MaterialEntry(cableGtSingle, Tin), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(LV), 'X', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_mv", EMITTER_MV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(rod, Electrum), 'C', new MaterialEntry(cableGtSingle, Copper), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(MV), 'X', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_hv", EMITTER_HV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(rod, Chromium), 'C', new MaterialEntry(cableGtSingle, Gold), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(HV), 'X', CustomTags.HV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_ev", EMITTER_EV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(rod, Platinum), 'C', new MaterialEntry(cableGtSingle, Aluminium), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(EV), 'X', CustomTags.EV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, "emitter_iv", EMITTER_IV.asStack(), "CRX", "RGR", "XRC",
                'R', new MaterialEntry(rod, Iridium), 'C', new MaterialEntry(cableGtSingle, Tungsten), 'G',
                GTCraftingComponents.SENSOR_EMITTER_GEM.get(IV), 'X', CustomTags.IV_CIRCUITS);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_lv")
                .inputItems(rod, Brass, 4)
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(LV))
                .circuitMeta(1)
                .outputItems(EMITTER_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_mv")
                .inputItems(rod, Electrum, 4)
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(CustomTags.MV_CIRCUITS, 2)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(MV))
                .circuitMeta(1)
                .outputItems(EMITTER_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_hv")
                .inputItems(rod, Chromium, 4)
                .inputItems(cableGtSingle, Gold, 2)
                .inputItems(CustomTags.HV_CIRCUITS, 2)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(HV))
                .circuitMeta(1)
                .outputItems(EMITTER_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_ev")
                .inputItems(rod, Platinum, 4)
                .inputItems(cableGtSingle, Aluminium, 2)
                .inputItems(CustomTags.EV_CIRCUITS, 2)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(EV))
                .circuitMeta(1)
                .outputItems(EMITTER_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("emitter_iv")
                .inputItems(rod, Iridium, 4)
                .inputItems(cableGtSingle, Tungsten, 2)
                .inputItems(CustomTags.IV_CIRCUITS, 2)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(IV))
                .circuitMeta(1)
                .outputItems(EMITTER_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("emitter_luv")
                .inputItems(frameGt, HSSS)
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(rodLong, Ruridit, 4)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(LuV))
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(foil, Palladium, 64)
                .inputItems(foil, Palladium, 32)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy, L * 2)
                .outputItems(EMITTER_LuV)
                .scannerResearch(b -> b
                        .researchStack(EMITTER_IV.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(600).EUt(6000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("emitter_zpm")
                .inputItems(frameGt, NaquadahAlloy)
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(rodLong, Osmiridium, 4)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(ZPM), 2)
                .inputItems(CustomTags.ZPM_CIRCUITS, 2)
                .inputItems(foil, Trinium, 64)
                .inputItems(foil, Trinium, 32)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy, L * 4)
                .outputItems(EMITTER_ZPM)
                .stationResearch(b -> b
                        .researchStack(EMITTER_LuV.asStack())
                        .CWUt(8))
                .duration(600).EUt(24000)
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("emitter_uv")
                .inputItems(frameGt, Tritanium)
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(rodLong, Tritanium, 4)
                .inputItems(GTCraftingComponents.SENSOR_EMITTER_GEM.get(UV))
                .inputItems(CustomTags.UV_CIRCUITS, 2)
                .inputItems(foil, Naquadria, 64)
                .inputItems(foil, Naquadria, 32)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
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
