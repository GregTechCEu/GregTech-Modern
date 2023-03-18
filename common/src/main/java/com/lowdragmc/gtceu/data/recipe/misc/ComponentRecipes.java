package com.lowdragmc.gtceu.data.recipe.misc;

import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.lowdragmc.gtceu.data.recipe.VanillaRecipeHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.function.Consumer;

import static com.lowdragmc.gtceu.common.libs.GTMaterials.*;
import static com.lowdragmc.gtceu.api.GTValues.*;
import static com.lowdragmc.gtceu.api.tag.TagPrefix.*;
import static com.lowdragmc.gtceu.common.libs.GTRecipeTypes.*;
import static com.lowdragmc.gtceu.common.libs.GTItems.*;
import static com.lowdragmc.gtceu.api.data.chemical.material.MarkerMaterials.*;

public class ComponentRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        //Motors Start--------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_lv_steel", ELECTRIC_MOTOR_LV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtSingle, Tin), 'W', new UnificationEntry(wireGtSingle, Copper), 'R', new UnificationEntry(stick, Steel), 'M', new UnificationEntry(stick, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_lv_iron", ELECTRIC_MOTOR_LV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtSingle, Tin), 'W', new UnificationEntry(wireGtSingle, Copper), 'R', new UnificationEntry(stick, Iron), 'M', new UnificationEntry(stick, IronMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_mv", ELECTRIC_MOTOR_MV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtSingle, Copper), 'W', new UnificationEntry(wireGtDouble, Cupronickel), 'R', new UnificationEntry(stick, Aluminium), 'M', new UnificationEntry(stick, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_hv", ELECTRIC_MOTOR_HV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtDouble, Silver), 'W', new UnificationEntry(wireGtDouble, Electrum), 'R', new UnificationEntry(stick, StainlessSteel), 'M', new UnificationEntry(stick, SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_ev", ELECTRIC_MOTOR_EV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtDouble, Aluminium), 'W', new UnificationEntry(wireGtDouble, Kanthal), 'R', new UnificationEntry(stick, Titanium), 'M', new UnificationEntry(stick, NeodymiumMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_iv", ELECTRIC_MOTOR_IV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtDouble, Tungsten), 'W', new UnificationEntry(wireGtDouble, Graphene), 'R', new UnificationEntry(stick, TungstenSteel), 'M', new UnificationEntry(stick, NeodymiumMagnetic));

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_MOTOR_LV.getId() + ".0"))
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(stick, Iron, 2)
                .inputItems(stick, IronMagnetic)
                .inputItems(wireGtSingle, Copper, 4)
                .outputItems(ELECTRIC_MOTOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_MOTOR_LV.getId() + ".1"))
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(stick, Steel, 2)
                .inputItems(stick, SteelMagnetic)
                .inputItems(wireGtSingle, Copper, 4)
                .outputItems(ELECTRIC_MOTOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_MOTOR_MV.getId())
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(stick, Aluminium, 2)
                .inputItems(stick, SteelMagnetic)
                .inputItems(wireGtDouble, Cupronickel, 4)
                .outputItems(ELECTRIC_MOTOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_MOTOR_HV.getId())
                .inputItems(cableGtDouble, Silver, 2)
                .inputItems(stick, StainlessSteel, 2)
                .inputItems(stick, SteelMagnetic)
                .inputItems(wireGtDouble, Electrum, 4)
                .outputItems(ELECTRIC_MOTOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_MOTOR_EV.getId())
                .inputItems(cableGtDouble, Aluminium, 2)
                .inputItems(stick, Titanium, 2)
                .inputItems(stick, NeodymiumMagnetic)
                .inputItems(wireGtDouble, Kanthal, 4)
                .outputItems(ELECTRIC_MOTOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_MOTOR_IV.getId())
                .inputItems(cableGtDouble, Tungsten, 2)
                .inputItems(stick, TungstenSteel, 2)
                .inputItems(stick, NeodymiumMagnetic)
                .inputItems(wireGtDouble, Graphene, 4)
                .outputItems(ELECTRIC_MOTOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_MOTOR_LuV.getId())
                .inputItems(stickLong, SamariumMagnetic)
                .inputItems(stickLong, HSSS, 2)
                .inputItems(ring, HSSS, 2)
                .inputItems(round, HSSS, 4)
                .inputItems(wireFine, Ruridit, 64)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy.getFluid(L))
                .inputFluids(Lubricant.getFluid(250))
                .outputItems(ELECTRIC_MOTOR_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_MOTOR_ZPM.getId())
                .inputItems(stickLong, SamariumMagnetic)
                .inputItems(stickLong, Osmiridium, 4)
                .inputItems(ring, Osmiridium, 4)
                .inputItems(round, Osmiridium, 8)
                .inputItems(wireFine, Europium, 64)
                .inputItems(wireFine, Europium, 32)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .inputFluids(Lubricant.getFluid(500))
                .outputItems(ELECTRIC_MOTOR_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_MOTOR_UV.getId())
                .inputItems(stickLong, SamariumMagnetic)
                .inputItems(stickLong, Tritanium, 4)
                .inputItems(ring, Tritanium, 4)
                .inputItems(round, Tritanium, 8)
                .inputItems(wireFine, Americium, 64)
                .inputItems(wireFine, Americium, 64)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .inputFluids(Lubricant.getFluid(1000))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(ELECTRIC_MOTOR_UV)
                .duration(600).EUt(100000).save(provider);



        //Conveyors Start-----------------------------------------------------------------------------------------------
        final Map<String, Material> rubberMaterials = new Object2ObjectOpenHashMap<>();
        rubberMaterials.put("rubber", Rubber);
        rubberMaterials.put("silicone_rubber", SiliconeRubber);
        rubberMaterials.put("styrene_butadiene_rubber", StyreneButadieneRubber);

        for (Map.Entry<String, Material> materialEntry : rubberMaterials.entrySet()) {
            Material material = materialEntry.getValue();
            String name = materialEntry.getKey();

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_lv_%s", name), CONVEYOR_MODULE_LV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, Tin), 'M', ELECTRIC_MOTOR_LV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_mv_%s", name), CONVEYOR_MODULE_MV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, Copper), 'M', ELECTRIC_MOTOR_MV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_hv_%s", name), CONVEYOR_MODULE_HV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, Gold), 'M', ELECTRIC_MOTOR_HV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_ev_%s", name), CONVEYOR_MODULE_EV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, Aluminium), 'M', ELECTRIC_MOTOR_EV.get());
            if (!materialEntry.getValue().equals(Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_iv_%s", materialEntry.getKey()), CONVEYOR_MODULE_IV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'M', ELECTRIC_MOTOR_IV.get());

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CONVEYOR_MODULE_LV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Tin)
                    .inputItems(ELECTRIC_MOTOR_LV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_LV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CONVEYOR_MODULE_MV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Copper)
                    .inputItems(ELECTRIC_MOTOR_MV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_MV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CONVEYOR_MODULE_HV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Gold)
                    .inputItems(ELECTRIC_MOTOR_HV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_HV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CONVEYOR_MODULE_EV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Aluminium)
                    .inputItems(ELECTRIC_MOTOR_EV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputItems(CONVEYOR_MODULE_EV)
                    .duration(100).EUt(VA[LV]).save(provider);

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(CONVEYOR_MODULE_IV.getId() + "_" + name))
                        .inputItems(cableGtSingle, Tungsten)
                        .inputItems(ELECTRIC_MOTOR_IV.asStack(2))
                        .inputFluids(materialEntry.getValue().getFluid(L * 6))
                        .circuitMeta(1)
                        .outputItems(CONVEYOR_MODULE_IV)
                        .duration(100).EUt(VA[LV]).save(provider);


            //Pumps Start---------------------------------------------------------------------------------------------------
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_lv_%s", name), ELECTRIC_PUMP_LV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, Tin), 'X', new UnificationEntry(rotor, Tin), 'P', new UnificationEntry(pipeNormalFluid, Bronze), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Tin), 'M', ELECTRIC_MOTOR_LV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_mv_%s", name), ELECTRIC_PUMP_MV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, Bronze), 'X', new UnificationEntry(rotor, Bronze), 'P', new UnificationEntry(pipeNormalFluid, Steel), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Copper), 'M', ELECTRIC_MOTOR_MV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_hv_%s", name), ELECTRIC_PUMP_HV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, Steel), 'X', new UnificationEntry(rotor, Steel), 'P', new UnificationEntry(pipeNormalFluid, StainlessSteel), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Gold), 'M', ELECTRIC_MOTOR_HV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_ev_%s", name), ELECTRIC_PUMP_EV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, StainlessSteel), 'X', new UnificationEntry(rotor, StainlessSteel), 'P', new UnificationEntry(pipeNormalFluid, Titanium), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Aluminium), 'M', ELECTRIC_MOTOR_EV.get());
            if (!material.equals(Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_iv_%s", name), ELECTRIC_PUMP_IV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, TungstenSteel), 'X', new UnificationEntry(rotor, TungstenSteel), 'P', new UnificationEntry(pipeNormalFluid, TungstenSteel), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'M', ELECTRIC_MOTOR_IV.get());

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_PUMP_LV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Tin)
                    .inputItems(pipeNormalFluid, Bronze)
                    .inputItems(screw, Tin)
                    .inputItems(rotor, Tin)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_LV)
                    .outputItems(ELECTRIC_PUMP_LV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_PUMP_MV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Copper)
                    .inputItems(pipeNormalFluid, Steel)
                    .inputItems(screw, Bronze)
                    .inputItems(rotor, Bronze)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_MV)
                    .outputItems(ELECTRIC_PUMP_MV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_PUMP_HV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Gold)
                    .inputItems(pipeNormalFluid, StainlessSteel)
                    .inputItems(screw, Steel)
                    .inputItems(rotor, Steel)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_HV)
                    .outputItems(ELECTRIC_PUMP_HV)
                    .duration(100).EUt(VA[LV]).save(provider);

            ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_PUMP_EV.getId() + "_" + name))
                    .inputItems(cableGtSingle, Aluminium)
                    .inputItems(pipeNormalFluid, Titanium)
                    .inputItems(screw, StainlessSteel)
                    .inputItems(rotor, StainlessSteel)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(ELECTRIC_MOTOR_EV)
                    .outputItems(ELECTRIC_PUMP_EV)
                    .duration(100).EUt(VA[LV]).save(provider);

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(ELECTRIC_PUMP_IV.getId() + "_" + name))
                        .inputItems(cableGtSingle, Tungsten)
                        .inputItems(pipeNormalFluid, TungstenSteel)
                        .inputItems(screw, TungstenSteel)
                        .inputItems(rotor, TungstenSteel)
                        .inputItems(ring, materialEntry.getValue(), 2)
                        .inputItems(ELECTRIC_MOTOR_IV)
                        .outputItems(ELECTRIC_PUMP_IV)
                        .duration(100).EUt(VA[LV]).save(provider);
        }

        ASSEMBLY_LINE_RECIPES.recipeBuilder(CONVEYOR_MODULE_LuV.getId())
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(plate, HSSS, 2)
                .inputItems(ring, HSSS, 4)
                .inputItems(round, HSSS, 16)
                .inputItems(screw, HSSS, 4)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy.getFluid(L))
                .inputFluids(Lubricant.getFluid(250))
                .inputFluids(StyreneButadieneRubber.getFluid(L * 8))
                .outputItems(CONVEYOR_MODULE_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(CONVEYOR_MODULE_ZPM.getId())
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(plate, Osmiridium, 2)
                .inputItems(ring, Osmiridium, 4)
                .inputItems(round, Osmiridium, 16)
                .inputItems(screw, Osmiridium, 4)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .inputFluids(Lubricant.getFluid(500))
                .inputFluids(StyreneButadieneRubber.getFluid(L * 16))
                .outputItems(CONVEYOR_MODULE_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(CONVEYOR_MODULE_UV.getId())
                .inputItems(ELECTRIC_MOTOR_UV, 2)
                .inputItems(plate, Tritanium, 2)
                .inputItems(ring, Tritanium, 4)
                .inputItems(round, Tritanium, 16)
                .inputItems(screw, Tritanium, 4)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .inputFluids(Lubricant.getFluid(1000))
                .inputFluids(StyreneButadieneRubber.getFluid(L * 24))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(CONVEYOR_MODULE_UV)
                .duration(600).EUt(100000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_PUMP_LuV.getId())
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(pipeSmallFluid, NiobiumTitanium)
                .inputItems(plate, HSSS, 2)
                .inputItems(screw, HSSS, 8)
                .inputItems(ring, SiliconeRubber, 4)
                .inputItems(rotor, HSSS)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy.getFluid(L))
                .inputFluids(Lubricant.getFluid(250))
                .outputItems(ELECTRIC_PUMP_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_PUMP_ZPM.getId())
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(pipeNormalFluid, Polybenzimidazole)
                .inputItems(plate, Osmiridium, 2)
                .inputItems(screw, Osmiridium, 8)
                .inputItems(ring, SiliconeRubber, 8)
                .inputItems(rotor, Osmiridium)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .inputFluids(Lubricant.getFluid(500))
                .outputItems(ELECTRIC_PUMP_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_PUMP_UV.getId())
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(pipeLargeFluid, Naquadah)
                .inputItems(plate, Tritanium, 2)
                .inputItems(screw, Tritanium, 8)
                .inputItems(ring, SiliconeRubber, 16)
                .inputItems(rotor, NaquadahAlloy)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .inputFluids(Lubricant.getFluid(1000))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(ELECTRIC_PUMP_UV)
                .duration(600).EUt(100000).save(provider);

        //Fluid Regulators----------------------------------------------------------------------------------------------

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_LV.getId())
                .inputItems(ELECTRIC_PUMP_LV)
                .inputItems(circuit, Tier.LV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_LV)
                .EUt(VA[LV])
                .duration(400)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_MV.getId())
                .inputItems(ELECTRIC_PUMP_MV)
                .inputItems(circuit, Tier.MV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_MV)
                .EUt(VA[MV])
                .duration(350)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_HV.getId())
                .inputItems(ELECTRIC_PUMP_HV)
                .inputItems(circuit, Tier.HV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_HV)
                .EUt(VA[HV])
                .duration(300)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_EV.getId())
                .inputItems(ELECTRIC_PUMP_EV)
                .inputItems(circuit, Tier.EV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_EV)
                .EUt(VA[EV])
                .duration(250)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_IV.getId())
                .inputItems(ELECTRIC_PUMP_IV)
                .inputItems(circuit, Tier.IV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_IV)
                .EUt(VA[IV])
                .duration(200)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_LUV.getId())
                .inputItems(ELECTRIC_PUMP_LuV)
                .inputItems(circuit, Tier.LuV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_LUV)
                .EUt(VA[LuV])
                .duration(150)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_ZPM.getId())
                .inputItems(ELECTRIC_PUMP_ZPM)
                .inputItems(circuit, Tier.ZPM, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_ZPM)
                .EUt(VA[ZPM])
                .duration(100)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FLUID_REGULATOR_UV.getId())
                .inputItems(ELECTRIC_PUMP_UV)
                .inputItems(circuit, Tier.UV, 2)
                .circuitMeta(1)
                .outputItems(FLUID_REGULATOR_UV)
                .EUt(VA[UV])
                .duration(50)
                .save(provider);

        //Voiding Covers Start-----------------------------------------------------------------------------------------

        VanillaRecipeHelper.addShapedRecipe(provider, "cover_item_voiding", COVER_ITEM_VOIDING.asStack(), "SDS", "dPw", " E ", 'S', new UnificationEntry(screw, Steel), 'D', COVER_ITEM_DETECTOR.get(), 'P', new UnificationEntry(pipeNormalItem, Brass), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_ITEM_VOIDING.getId())
                .inputItems(screw, Steel, 2)
                .inputItems(COVER_ITEM_DETECTOR)
                .inputItems(pipeNormalItem, Brass)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(COVER_ITEM_VOIDING)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_ITEM_VOIDING_ADVANCED.getId())
                .inputItems(COVER_ITEM_VOIDING)
                .inputItems(circuit, Tier.MV, 1)
                .outputItems(COVER_ITEM_VOIDING_ADVANCED)
                .duration(100).EUt(VA[LV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "cover_fluid_voiding", COVER_FLUID_VOIDING.asStack(), "SDS", "dPw", " E ", 'S', new UnificationEntry(screw, Steel), 'D', COVER_FLUID_DETECTOR.get(), 'P', new UnificationEntry(pipeNormalFluid, Bronze), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_FLUID_VOIDING.getId())
                .inputItems(screw, Steel, 2)
                .inputItems(COVER_FLUID_DETECTOR)
                .inputItems(pipeNormalFluid, Bronze)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(COVER_FLUID_VOIDING)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(COVER_FLUID_VOIDING_ADVANCED.getId())
                .inputItems(COVER_FLUID_VOIDING)
                .inputItems(circuit, Tier.MV, 1)
                .outputItems(COVER_FLUID_VOIDING_ADVANCED)
                .duration(100).EUt(VA[LV]).save(provider);

        //Pistons Start-------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_lv", ELECTRIC_PISTON_LV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, Steel), 'C', new UnificationEntry(cableGtSingle, Tin), 'R', new UnificationEntry(stick, Steel), 'G', new UnificationEntry(gearSmall, Steel), 'M', ELECTRIC_MOTOR_LV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_mv", ELECTRIC_PISTON_MV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, Aluminium), 'C', new UnificationEntry(cableGtSingle, Copper), 'R', new UnificationEntry(stick, Aluminium), 'G', new UnificationEntry(gearSmall, Aluminium), 'M', ELECTRIC_MOTOR_MV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_hv", ELECTRIC_PISTON_HV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, StainlessSteel), 'C', new UnificationEntry(cableGtSingle, Gold), 'R', new UnificationEntry(stick, StainlessSteel), 'G', new UnificationEntry(gearSmall, StainlessSteel), 'M', ELECTRIC_MOTOR_HV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_ev", ELECTRIC_PISTON_EV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, Titanium), 'C', new UnificationEntry(cableGtSingle, Aluminium), 'R', new UnificationEntry(stick, Titanium), 'G', new UnificationEntry(gearSmall, Titanium), 'M', ELECTRIC_MOTOR_EV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_iv", ELECTRIC_PISTON_IV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, TungstenSteel), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'R', new UnificationEntry(stick, TungstenSteel), 'G', new UnificationEntry(gearSmall, TungstenSteel), 'M', ELECTRIC_MOTOR_IV.get());

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_PISTON_LV.getId())
                .inputItems(stick, Steel, 2)
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(plate, Steel, 3)
                .inputItems(gearSmall, Steel)
                .inputItems(ELECTRIC_MOTOR_LV)
                .outputItems(ELECTRIC_PISTON_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_PISTON_MV.getId())
                .inputItems(stick, Aluminium, 2)
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(plate, Aluminium, 3)
                .inputItems(gearSmall, Aluminium)
                .inputItems(ELECTRIC_MOTOR_MV)
                .outputItems(ELECTRIC_PISTON_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_PISTON_HV.getId())
                .inputItems(stick, StainlessSteel, 2)
                .inputItems(cableGtSingle, Gold, 2)
                .inputItems(plate, StainlessSteel, 3)
                .inputItems(gearSmall, StainlessSteel)
                .inputItems(ELECTRIC_MOTOR_HV)
                .outputItems(ELECTRIC_PISTON_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_PISTON_EV.getId())
                .inputItems(stick, Titanium, 2)
                .inputItems(cableGtSingle, Aluminium, 2)
                .inputItems(plate, Titanium, 3)
                .inputItems(gearSmall, Titanium)
                .inputItems(ELECTRIC_MOTOR_EV)
                .outputItems(ELECTRIC_PISTON_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ELECTRIC_PISTON_IV.getId())
                .inputItems(stick, TungstenSteel, 2)
                .inputItems(cableGtSingle, Tungsten, 2)
                .inputItems(plate, TungstenSteel, 3)
                .inputItems(gearSmall, TungstenSteel)
                .inputItems(ELECTRIC_MOTOR_IV)
                .outputItems(ELECTRIC_PISTON_IV)
                .duration(100).EUt(VA[LV]).save(provider);


        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_PISTON_LUV.getId())
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(plate, HSSS, 4)
                .inputItems(ring, HSSS, 4)
                .inputItems(round, HSSS, 16)
                .inputItems(stick, HSSS, 4)
                .inputItems(gear, HSSS)
                .inputItems(gearSmall, HSSS, 2)
                .inputItems(cableGtSingle, NiobiumTitanium, 2)
                .inputFluids(SolderingAlloy.getFluid(L))
                .inputFluids(Lubricant.getFluid(250))
                .outputItems(ELECTRIC_PISTON_LUV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_PISTON_ZPM.getId())
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(plate, Osmiridium, 4)
                .inputItems(ring, Osmiridium, 4)
                .inputItems(round, Osmiridium, 16)
                .inputItems(stick, Osmiridium, 4)
                .inputItems(gear, Osmiridium)
                .inputItems(gearSmall, Osmiridium, 2)
                .inputItems(cableGtSingle, VanadiumGallium, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .inputFluids(Lubricant.getFluid(500))
                .outputItems(ELECTRIC_PISTON_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ELECTRIC_PISTON_UV.getId())
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(plate, Tritanium, 4)
                .inputItems(ring, Tritanium, 4)
                .inputItems(round, Tritanium, 16)
                .inputItems(stick, Tritanium, 4)
                .inputItems(gear, NaquadahAlloy)
                .inputItems(gearSmall, NaquadahAlloy, 2)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 2)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .inputFluids(Lubricant.getFluid(1000))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(ELECTRIC_PISTON_UV)
                .duration(600).EUt(100000).save(provider);



        //Robot Arms Start ---------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_lv", ROBOT_ARM_LV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, Tin), 'R', new UnificationEntry(stick, Steel), 'M', ELECTRIC_MOTOR_LV.get(), 'P', ELECTRIC_PISTON_LV.get(), 'X', new UnificationEntry(circuit, Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_mv", ROBOT_ARM_MV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, Copper), 'R', new UnificationEntry(stick, Aluminium), 'M', ELECTRIC_MOTOR_MV.get(), 'P', ELECTRIC_PISTON_MV.get(), 'X', new UnificationEntry(circuit, Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_hv", ROBOT_ARM_HV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, Gold), 'R', new UnificationEntry(stick, StainlessSteel), 'M', ELECTRIC_MOTOR_HV.get(), 'P', ELECTRIC_PISTON_HV.get(), 'X', new UnificationEntry(circuit, Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_ev", ROBOT_ARM_EV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, Aluminium), 'R', new UnificationEntry(stick, Titanium), 'M', ELECTRIC_MOTOR_EV.get(), 'P', ELECTRIC_PISTON_EV.get(), 'X', new UnificationEntry(circuit, Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_iv", ROBOT_ARM_IV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, Tungsten), 'R', new UnificationEntry(stick, TungstenSteel), 'M', ELECTRIC_MOTOR_IV.get(), 'P', ELECTRIC_PISTON_IV.get(), 'X', new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder(ROBOT_ARM_LV.getId())
                .inputItems(cableGtSingle, Tin, 3)
                .inputItems(stick, Steel, 2)
                .inputItems(ELECTRIC_MOTOR_LV.asStack(2))
                .inputItems(ELECTRIC_PISTON_LV)
                .inputItems(circuit, Tier.LV)
                .outputItems(ROBOT_ARM_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ROBOT_ARM_MV.getId())
                .inputItems(cableGtSingle, Copper, 3)
                .inputItems(stick, Aluminium, 2)
                .inputItems(ELECTRIC_MOTOR_MV.asStack(2))
                .inputItems(ELECTRIC_PISTON_MV)
                .inputItems(circuit, Tier.MV)
                .outputItems(ROBOT_ARM_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ROBOT_ARM_HV.getId())
                .inputItems(cableGtSingle, Gold, 3)
                .inputItems(stick, StainlessSteel, 2)
                .inputItems(ELECTRIC_MOTOR_HV.asStack(2))
                .inputItems(ELECTRIC_PISTON_HV)
                .inputItems(circuit, Tier.HV)
                .outputItems(ROBOT_ARM_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ROBOT_ARM_EV.getId())
                .inputItems(cableGtSingle, Aluminium, 3)
                .inputItems(stick, Titanium, 2)
                .inputItems(ELECTRIC_MOTOR_EV.asStack(2))
                .inputItems(ELECTRIC_PISTON_EV)
                .inputItems(circuit, Tier.EV)
                .outputItems(ROBOT_ARM_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(ROBOT_ARM_IV.getId())
                .inputItems(cableGtSingle, Tungsten, 3)
                .inputItems(stick, TungstenSteel, 2)
                .inputItems(ELECTRIC_MOTOR_IV.asStack(2))
                .inputItems(ELECTRIC_PISTON_IV)
                .inputItems(circuit, Tier.IV)
                .outputItems(ROBOT_ARM_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ROBOT_ARM_LuV.getId())
                .inputItems(stickLong, HSSS, 4)
                .inputItems(gear, HSSS)
                .inputItems(gearSmall, HSSS, 3)
                .inputItems(ELECTRIC_MOTOR_LuV, 2)
                .inputItems(ELECTRIC_PISTON_LUV)
                .inputItems(circuit, Tier.LuV)
                .inputItems(circuit, Tier.IV, 2)
                .inputItems(circuit, Tier.EV, 4)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .inputFluids(Lubricant.getFluid(250))
                .outputItems(ROBOT_ARM_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ROBOT_ARM_ZPM.getId())
                .inputItems(stickLong, Osmiridium, 4)
                .inputItems(gear, Osmiridium)
                .inputItems(gearSmall, Osmiridium, 3)
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(ELECTRIC_PISTON_ZPM)
                .inputItems(circuit, Tier.ZPM)
                .inputItems(circuit, Tier.LuV, 2)
                .inputItems(circuit, Tier.IV, 4)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(Lubricant.getFluid(500))
                .outputItems(ROBOT_ARM_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(ROBOT_ARM_UV.getId())
                .inputItems(stickLong, Tritanium, 4)
                .inputItems(gear, Tritanium)
                .inputItems(gearSmall, Tritanium, 3)
                .inputItems(ELECTRIC_MOTOR_UV, 2)
                .inputItems(ELECTRIC_PISTON_UV)
                .inputItems(circuit, Tier.UV)
                .inputItems(circuit, Tier.ZPM, 2)
                .inputItems(circuit, Tier.LuV, 4)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 12))
                .inputFluids(Lubricant.getFluid(1000))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(ROBOT_ARM_UV)
                .duration(600).EUt(100000).save(provider);



        //Field Generators Start ---------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_lv", FIELD_GENERATOR_LV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, ManganesePhosphide), 'P', new UnificationEntry(plate, Steel), 'G', new UnificationEntry(gem, EnderPearl), 'X', new UnificationEntry(circuit, Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_mv", FIELD_GENERATOR_MV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, MagnesiumDiboride), 'P', new UnificationEntry(plate, Aluminium), 'G', new UnificationEntry(gem, EnderEye), 'X', new UnificationEntry(circuit, Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_hv", FIELD_GENERATOR_HV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, MercuryBariumCalciumCuprate), 'P', new UnificationEntry(plate, StainlessSteel), 'G', QUANTUM_EYE.get(), 'X', new UnificationEntry(circuit, Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_ev", FIELD_GENERATOR_EV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, UraniumTriplatinum), 'P', new UnificationEntry(plateDouble, Titanium), 'G', new UnificationEntry(gem, NetherStar), 'X', new UnificationEntry(circuit, Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_iv", FIELD_GENERATOR_IV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, SamariumIronArsenicOxide), 'P', new UnificationEntry(plateDouble, TungstenSteel), 'G', QUANTUM_STAR.get(), 'X', new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder(FIELD_GENERATOR_LV.getId())
                .inputItems(gem, EnderPearl)
                .inputItems(plate, Steel, 2)
                .inputItems(circuit, Tier.LV, 2)
                .inputItems(wireGtQuadruple, ManganesePhosphide, 4)
                .outputItems(FIELD_GENERATOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FIELD_GENERATOR_MV.getId())
                .inputItems(gem, EnderEye)
                .inputItems(plate, Aluminium, 2)
                .inputItems(circuit, Tier.MV, 2)
                .inputItems(wireGtQuadruple, MagnesiumDiboride, 4)
                .outputItems(FIELD_GENERATOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FIELD_GENERATOR_HV.getId())
                .inputItems(QUANTUM_EYE)
                .inputItems(plate, StainlessSteel, 2)
                .inputItems(circuit, Tier.HV, 2)
                .inputItems(wireGtQuadruple, MercuryBariumCalciumCuprate, 4)
                .outputItems(FIELD_GENERATOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FIELD_GENERATOR_EV.getId())
                .inputItems(gem, NetherStar)
                .inputItems(plateDouble, Titanium, 2)
                .inputItems(circuit, Tier.EV, 2)
                .inputItems(wireGtQuadruple, UraniumTriplatinum, 4)
                .outputItems(FIELD_GENERATOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(FIELD_GENERATOR_IV.getId())
                .inputItems(QUANTUM_STAR)
                .inputItems(plateDouble, TungstenSteel, 2)
                .inputItems(circuit, Tier.IV, 2)
                .inputItems(wireGtQuadruple, SamariumIronArsenicOxide, 4)
                .outputItems(FIELD_GENERATOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(FIELD_GENERATOR_LuV.get())
                .inputItems(frameGt, HSSS)
                .inputItems(plate, HSSS, 6)
                .inputItems(QUANTUM_STAR)
                .inputItems(EMITTER_LuV, 2)
                .inputItems(circuit, Tier.LuV, 2)
                .inputItems(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .outputItems(FIELD_GENERATOR_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(FIELD_GENERATOR_ZPM.getId())
                .inputItems(frameGt, NaquadahAlloy)
                .inputItems(plate, NaquadahAlloy, 6)
                .inputItems(QUANTUM_STAR)
                .inputItems(EMITTER_ZPM, 2)
                .inputItems(circuit, Tier.ZPM, 2)
                .inputItems(wireFine, UraniumRhodiumDinaquadide, 64)
                .inputItems(wireFine, UraniumRhodiumDinaquadide, 64)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .outputItems(FIELD_GENERATOR_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(FIELD_GENERATOR_UV.getId())
                .inputItems(frameGt, Tritanium)
                .inputItems(plate, Tritanium, 6)
                .inputItems(GRAVI_STAR)
                .inputItems(EMITTER_UV, 2)
                .inputItems(circuit, Tier.UV, 2)
                .inputItems(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 12))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(FIELD_GENERATOR_UV)
                .duration(600).EUt(100000).save(provider);



        //Sensors Start-------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_lv", SENSOR_LV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, Steel), 'R', new UnificationEntry(stick, Brass), 'G', new UnificationEntry(gem, Quartzite), 'X', new UnificationEntry(circuit, Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_mv", SENSOR_MV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, Aluminium), 'R', new UnificationEntry(stick, Electrum), 'G', new UnificationEntry(gemFlawless, Emerald), 'X', new UnificationEntry(circuit, Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_hv", SENSOR_HV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, StainlessSteel), 'R', new UnificationEntry(stick, Chrome), 'G', new UnificationEntry(gem, EnderEye), 'X', new UnificationEntry(circuit, Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_ev", SENSOR_EV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, Titanium), 'R', new UnificationEntry(stick, Platinum), 'G', QUANTUM_EYE.get(), 'X', new UnificationEntry(circuit, Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_iv", SENSOR_IV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, TungstenSteel), 'R', new UnificationEntry(stick, Iridium), 'G', QUANTUM_STAR.get(), 'X', new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder(SENSOR_LV.getId())
                .inputItems(stick, Brass)
                .inputItems(plate, Steel, 4)
                .inputItems(circuit, Tier.LV)
                .inputItems(gem, Quartzite)
                .outputItems(SENSOR_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(SENSOR_MV.getId())
                .inputItems(stick, Electrum)
                .inputItems(plate, Aluminium, 4)
                .inputItems(circuit, Tier.MV)
                .inputItems(gemFlawless, Emerald)
                .outputItems(SENSOR_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(SENSOR_HV.getId())
                .inputItems(stick, Chrome)
                .inputItems(plate, StainlessSteel, 4)
                .inputItems(circuit, Tier.HV)
                .inputItems(gem, EnderEye)
                .outputItems(SENSOR_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(SENSOR_EV.getId())
                .inputItems(stick, Platinum)
                .inputItems(plate, Titanium, 4)
                .inputItems(circuit, Tier.EV)
                .inputItems(QUANTUM_EYE)
                .outputItems(SENSOR_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(SENSOR_IV.getId())
                .inputItems(stick, Iridium)
                .inputItems(plate, TungstenSteel, 4)
                .inputItems(circuit, Tier.IV)
                .inputItems(QUANTUM_STAR)
                .outputItems(SENSOR_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(SENSOR_LuV.getId())
                .inputItems(frameGt, HSSS)
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(plate, Ruridit, 4)
                .inputItems(QUANTUM_STAR)
                .inputItems(circuit, Tier.LuV, 2)
                .inputItems(foil, Palladium, 64)
                .inputItems(foil, Palladium, 32)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .outputItems(SENSOR_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(SENSOR_ZPM.getId())
                .inputItems(frameGt, NaquadahAlloy)
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(plate, Osmiridium, 4)
                .inputItems(QUANTUM_STAR, 2)
                .inputItems(circuit, Tier.ZPM, 2)
                .inputItems(foil, Trinium, 64)
                .inputItems(foil, Trinium, 32)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .outputItems(SENSOR_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(SENSOR_UV.getId())
                .inputItems(frameGt, Tritanium)
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(plate, Tritanium, 4)
                .inputItems(GRAVI_STAR)
                .inputItems(circuit, Tier.UV, 2)
                .inputItems(foil, Naquadria, 64)
                .inputItems(foil, Naquadria, 32)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(SENSOR_UV)
                .duration(600).EUt(100000).save(provider);


        //Emitters Start------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_lv", EMITTER_LV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, Brass), 'C', new UnificationEntry(cableGtSingle, Tin), 'G', new UnificationEntry(gem, Quartzite), 'X', new UnificationEntry(circuit, Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_mv", EMITTER_MV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, Electrum), 'C', new UnificationEntry(cableGtSingle, Copper), 'G', new UnificationEntry(gemFlawless, Emerald), 'X', new UnificationEntry(circuit, Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_hv", EMITTER_HV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, Chrome), 'C', new UnificationEntry(cableGtSingle, Gold), 'G', new UnificationEntry(gem, EnderEye), 'X', new UnificationEntry(circuit, Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_ev", EMITTER_EV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, Platinum), 'C', new UnificationEntry(cableGtSingle, Aluminium), 'G', QUANTUM_EYE.get(), 'X', new UnificationEntry(circuit, Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_iv", EMITTER_IV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, Iridium), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'G', QUANTUM_STAR.get(), 'X', new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder(EMITTER_LV.getId())
                .inputItems(stick, Brass, 4)
                .inputItems(cableGtSingle, Tin, 2)
                .inputItems(circuit, Tier.LV, 2)
                .inputItems(gem, Quartzite)
                .circuitMeta(1)
                .outputItems(EMITTER_LV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(EMITTER_MV.getId())
                .inputItems(stick, Electrum, 4)
                .inputItems(cableGtSingle, Copper, 2)
                .inputItems(circuit, Tier.MV, 2)
                .inputItems(gemFlawless, Emerald)
                .circuitMeta(1)
                .outputItems(EMITTER_MV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(EMITTER_HV.getId())
                .inputItems(stick, Chrome, 4)
                .inputItems(cableGtSingle, Gold, 2)
                .inputItems(circuit, Tier.HV, 2)
                .inputItems(gem, EnderEye)
                .circuitMeta(1)
                .outputItems(EMITTER_HV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(EMITTER_EV.getId())
                .inputItems(stick, Platinum, 4)
                .inputItems(cableGtSingle, Aluminium, 2)
                .inputItems(circuit, Tier.EV, 2)
                .inputItems(QUANTUM_EYE)
                .circuitMeta(1)
                .outputItems(EMITTER_EV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder(EMITTER_IV.getId())
                .inputItems(stick, Iridium, 4)
                .inputItems(cableGtSingle, Tungsten, 2)
                .inputItems(circuit, Tier.IV, 2)
                .inputItems(QUANTUM_STAR)
                .circuitMeta(1)
                .outputItems(EMITTER_IV)
                .duration(100).EUt(VA[LV]).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(EMITTER_LuV.getId())
                .inputItems(frameGt, HSSS)
                .inputItems(ELECTRIC_MOTOR_LuV)
                .inputItems(stickLong, Ruridit, 4)
                .inputItems(QUANTUM_STAR)
                .inputItems(circuit, Tier.LuV, 2)
                .inputItems(foil, Palladium, 64)
                .inputItems(foil, Palladium, 32)
                .inputItems(cableGtSingle, NiobiumTitanium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 2))
                .outputItems(EMITTER_LuV)
                .duration(600).EUt(6000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(EMITTER_ZPM.getId())
                .inputItems(frameGt, NaquadahAlloy)
                .inputItems(ELECTRIC_MOTOR_ZPM)
                .inputItems(stickLong, Osmiridium, 4)
                .inputItems(QUANTUM_STAR, 2)
                .inputItems(circuit, Tier.ZPM, 2)
                .inputItems(foil, Trinium, 64)
                .inputItems(foil, Trinium, 32)
                .inputItems(cableGtSingle, VanadiumGallium, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 4))
                .outputItems(EMITTER_ZPM)
                .duration(600).EUt(24000).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder(EMITTER_UV.getId())
                .inputItems(frameGt, Tritanium)
                .inputItems(ELECTRIC_MOTOR_UV)
                .inputItems(stickLong, Tritanium, 4)
                .inputItems(GRAVI_STAR)
                .inputItems(circuit, Tier.UV, 2)
                .inputItems(foil, Naquadria, 64)
                .inputItems(foil, Naquadria, 32)
                .inputItems(cableGtSingle, YttriumBariumCuprate, 4)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(EMITTER_UV)
                .duration(600).EUt(100000).save(provider);
    }
}
