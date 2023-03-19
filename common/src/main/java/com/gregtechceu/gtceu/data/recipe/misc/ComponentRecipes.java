package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.tag.TagPrefix.*;

public class ComponentRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {

        //Motors Start--------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_lv_steel", GTItems.ELECTRIC_MOTOR_LV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'W', new UnificationEntry(wireGtSingle, GTMaterials.Copper), 'R', new UnificationEntry(stick, GTMaterials.Steel), 'M', new UnificationEntry(stick, GTMaterials.SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_lv_iron", GTItems.ELECTRIC_MOTOR_LV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'W', new UnificationEntry(wireGtSingle, GTMaterials.Copper), 'R', new UnificationEntry(stick, GTMaterials.Iron), 'M', new UnificationEntry(stick, GTMaterials.IronMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_mv", GTItems.ELECTRIC_MOTOR_MV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Copper), 'W', new UnificationEntry(wireGtDouble, GTMaterials.Cupronickel), 'R', new UnificationEntry(stick, GTMaterials.Aluminium), 'M', new UnificationEntry(stick, GTMaterials.SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_hv", GTItems.ELECTRIC_MOTOR_HV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtDouble, GTMaterials.Silver), 'W', new UnificationEntry(wireGtDouble, GTMaterials.Electrum), 'R', new UnificationEntry(stick, GTMaterials.StainlessSteel), 'M', new UnificationEntry(stick, GTMaterials.SteelMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_ev", GTItems.ELECTRIC_MOTOR_EV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtDouble, GTMaterials.Aluminium), 'W', new UnificationEntry(wireGtDouble, GTMaterials.Kanthal), 'R', new UnificationEntry(stick, GTMaterials.Titanium), 'M', new UnificationEntry(stick, GTMaterials.NeodymiumMagnetic));
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_motor_iv", GTItems.ELECTRIC_MOTOR_IV.asStack(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(cableGtDouble, GTMaterials.Tungsten), 'W', new UnificationEntry(wireGtDouble, GTMaterials.Graphene), 'R', new UnificationEntry(stick, GTMaterials.TungstenSteel), 'M', new UnificationEntry(stick, GTMaterials.NeodymiumMagnetic));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_MOTOR_LV.getId() + ".0"))
                .inputItems(cableGtSingle, GTMaterials.Tin, 2)
                .inputItems(stick, GTMaterials.Iron, 2)
                .inputItems(stick, GTMaterials.IronMagnetic)
                .inputItems(wireGtSingle, GTMaterials.Copper, 4)
                .outputItems(GTItems.ELECTRIC_MOTOR_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_MOTOR_LV.getId() + ".1"))
                .inputItems(cableGtSingle, GTMaterials.Tin, 2)
                .inputItems(stick, GTMaterials.Steel, 2)
                .inputItems(stick, GTMaterials.SteelMagnetic)
                .inputItems(wireGtSingle, GTMaterials.Copper, 4)
                .outputItems(GTItems.ELECTRIC_MOTOR_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_MV.getId())
                .inputItems(cableGtSingle, GTMaterials.Copper, 2)
                .inputItems(stick, GTMaterials.Aluminium, 2)
                .inputItems(stick, GTMaterials.SteelMagnetic)
                .inputItems(wireGtDouble, GTMaterials.Cupronickel, 4)
                .outputItems(GTItems.ELECTRIC_MOTOR_MV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_HV.getId())
                .inputItems(cableGtDouble, GTMaterials.Silver, 2)
                .inputItems(stick, GTMaterials.StainlessSteel, 2)
                .inputItems(stick, GTMaterials.SteelMagnetic)
                .inputItems(wireGtDouble, GTMaterials.Electrum, 4)
                .outputItems(GTItems.ELECTRIC_MOTOR_HV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_EV.getId())
                .inputItems(cableGtDouble, GTMaterials.Aluminium, 2)
                .inputItems(stick, GTMaterials.Titanium, 2)
                .inputItems(stick, GTMaterials.NeodymiumMagnetic)
                .inputItems(wireGtDouble, GTMaterials.Kanthal, 4)
                .outputItems(GTItems.ELECTRIC_MOTOR_EV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_IV.getId())
                .inputItems(cableGtDouble, GTMaterials.Tungsten, 2)
                .inputItems(stick, GTMaterials.TungstenSteel, 2)
                .inputItems(stick, GTMaterials.NeodymiumMagnetic)
                .inputItems(wireGtDouble, GTMaterials.Graphene, 4)
                .outputItems(GTItems.ELECTRIC_MOTOR_IV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_LuV.getId())
                .inputItems(stickLong, GTMaterials.SamariumMagnetic)
                .inputItems(stickLong, GTMaterials.HSSS, 2)
                .inputItems(ring, GTMaterials.HSSS, 2)
                .inputItems(round, GTMaterials.HSSS, 4)
                .inputItems(wireFine, GTMaterials.Ruridit, 64)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .outputItems(GTItems.ELECTRIC_MOTOR_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_ZPM.getId())
                .inputItems(stickLong, GTMaterials.SamariumMagnetic)
                .inputItems(stickLong, GTMaterials.Osmiridium, 4)
                .inputItems(ring, GTMaterials.Osmiridium, 4)
                .inputItems(round, GTMaterials.Osmiridium, 8)
                .inputItems(wireFine, GTMaterials.Europium, 64)
                .inputItems(wireFine, GTMaterials.Europium, 32)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 2))
                .inputFluids(GTMaterials.Lubricant.getFluid(500))
                .outputItems(GTItems.ELECTRIC_MOTOR_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_MOTOR_UV.getId())
                .inputItems(stickLong, GTMaterials.SamariumMagnetic)
                .inputItems(stickLong, GTMaterials.Tritanium, 4)
                .inputItems(ring, GTMaterials.Tritanium, 4)
                .inputItems(round, GTMaterials.Tritanium, 8)
                .inputItems(wireFine, GTMaterials.Americium, 64)
                .inputItems(wireFine, GTMaterials.Americium, 64)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .inputFluids(GTMaterials.Lubricant.getFluid(1000))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.ELECTRIC_MOTOR_UV)
                .duration(600).EUt(100000).save(provider);



        //Conveyors Start-----------------------------------------------------------------------------------------------
        final Map<String, Material> rubberMaterials = new Object2ObjectOpenHashMap<>();
        rubberMaterials.put("rubber", GTMaterials.Rubber);
        rubberMaterials.put("silicone_rubber", GTMaterials.SiliconeRubber);
        rubberMaterials.put("styrene_butadiene_rubber", GTMaterials.StyreneButadieneRubber);

        for (Map.Entry<String, Material> materialEntry : rubberMaterials.entrySet()) {
            Material material = materialEntry.getValue();
            String name = materialEntry.getKey();

            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_lv_%s", name), GTItems.CONVEYOR_MODULE_LV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'M', GTItems.ELECTRIC_MOTOR_LV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_mv_%s", name), GTItems.CONVEYOR_MODULE_MV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Copper), 'M', GTItems.ELECTRIC_MOTOR_MV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_hv_%s", name), GTItems.CONVEYOR_MODULE_HV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Gold), 'M', GTItems.ELECTRIC_MOTOR_HV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_ev_%s", name), GTItems.CONVEYOR_MODULE_EV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Aluminium), 'M', GTItems.ELECTRIC_MOTOR_EV.get());
            if (!materialEntry.getValue().equals(GTMaterials.Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("conveyor_module_iv_%s", materialEntry.getKey()), GTItems.CONVEYOR_MODULE_IV.asStack(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tungsten), 'M', GTItems.ELECTRIC_MOTOR_IV.get());

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.CONVEYOR_MODULE_LV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Tin)
                    .inputItems(GTItems.ELECTRIC_MOTOR_LV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(GTValues.L * 6))
                    .circuitMeta(1)
                    .outputItems(GTItems.CONVEYOR_MODULE_LV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.CONVEYOR_MODULE_MV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Copper)
                    .inputItems(GTItems.ELECTRIC_MOTOR_MV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(GTValues.L * 6))
                    .circuitMeta(1)
                    .outputItems(GTItems.CONVEYOR_MODULE_MV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.CONVEYOR_MODULE_HV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Gold)
                    .inputItems(GTItems.ELECTRIC_MOTOR_HV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(GTValues.L * 6))
                    .circuitMeta(1)
                    .outputItems(GTItems.CONVEYOR_MODULE_HV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.CONVEYOR_MODULE_EV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Aluminium)
                    .inputItems(GTItems.ELECTRIC_MOTOR_EV.asStack(2))
                    .inputFluids(materialEntry.getValue().getFluid(GTValues.L * 6))
                    .circuitMeta(1)
                    .outputItems(GTItems.CONVEYOR_MODULE_EV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            if (!materialEntry.getValue().equals(GTMaterials.Rubber))
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.CONVEYOR_MODULE_IV.getId() + "_" + name))
                        .inputItems(cableGtSingle, GTMaterials.Tungsten)
                        .inputItems(GTItems.ELECTRIC_MOTOR_IV.asStack(2))
                        .inputFluids(materialEntry.getValue().getFluid(GTValues.L * 6))
                        .circuitMeta(1)
                        .outputItems(GTItems.CONVEYOR_MODULE_IV)
                        .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);


            //Pumps Start---------------------------------------------------------------------------------------------------
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_lv_%s", name), GTItems.ELECTRIC_PUMP_LV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, GTMaterials.Tin), 'X', new UnificationEntry(rotor, GTMaterials.Tin), 'P', new UnificationEntry(pipeNormalFluid, GTMaterials.Bronze), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'M', GTItems.ELECTRIC_MOTOR_LV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_mv_%s", name), GTItems.ELECTRIC_PUMP_MV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, GTMaterials.Bronze), 'X', new UnificationEntry(rotor, GTMaterials.Bronze), 'P', new UnificationEntry(pipeNormalFluid, GTMaterials.Steel), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Copper), 'M', GTItems.ELECTRIC_MOTOR_MV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_hv_%s", name), GTItems.ELECTRIC_PUMP_HV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, GTMaterials.Steel), 'X', new UnificationEntry(rotor, GTMaterials.Steel), 'P', new UnificationEntry(pipeNormalFluid, GTMaterials.StainlessSteel), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Gold), 'M', GTItems.ELECTRIC_MOTOR_HV.get());
            VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_ev_%s", name), GTItems.ELECTRIC_PUMP_EV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, GTMaterials.StainlessSteel), 'X', new UnificationEntry(rotor, GTMaterials.StainlessSteel), 'P', new UnificationEntry(pipeNormalFluid, GTMaterials.Titanium), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Aluminium), 'M', GTItems.ELECTRIC_MOTOR_EV.get());
            if (!material.equals(GTMaterials.Rubber))
                VanillaRecipeHelper.addShapedRecipe(provider, String.format("electric_pump_iv_%s", name), GTItems.ELECTRIC_PUMP_IV.asStack(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, GTMaterials.TungstenSteel), 'X', new UnificationEntry(rotor, GTMaterials.TungstenSteel), 'P', new UnificationEntry(pipeNormalFluid, GTMaterials.TungstenSteel), 'R', new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tungsten), 'M', GTItems.ELECTRIC_MOTOR_IV.get());

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_PUMP_LV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Tin)
                    .inputItems(pipeNormalFluid, GTMaterials.Bronze)
                    .inputItems(screw, GTMaterials.Tin)
                    .inputItems(rotor, GTMaterials.Tin)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(GTItems.ELECTRIC_MOTOR_LV)
                    .outputItems(GTItems.ELECTRIC_PUMP_LV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_PUMP_MV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Copper)
                    .inputItems(pipeNormalFluid, GTMaterials.Steel)
                    .inputItems(screw, GTMaterials.Bronze)
                    .inputItems(rotor, GTMaterials.Bronze)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(GTItems.ELECTRIC_MOTOR_MV)
                    .outputItems(GTItems.ELECTRIC_PUMP_MV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_PUMP_HV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Gold)
                    .inputItems(pipeNormalFluid, GTMaterials.StainlessSteel)
                    .inputItems(screw, GTMaterials.Steel)
                    .inputItems(rotor, GTMaterials.Steel)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(GTItems.ELECTRIC_MOTOR_HV)
                    .outputItems(GTItems.ELECTRIC_PUMP_HV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_PUMP_EV.getId() + "_" + name))
                    .inputItems(cableGtSingle, GTMaterials.Aluminium)
                    .inputItems(pipeNormalFluid, GTMaterials.Titanium)
                    .inputItems(screw, GTMaterials.StainlessSteel)
                    .inputItems(rotor, GTMaterials.StainlessSteel)
                    .inputItems(ring, materialEntry.getValue(), 2)
                    .inputItems(GTItems.ELECTRIC_MOTOR_EV)
                    .outputItems(GTItems.ELECTRIC_PUMP_EV)
                    .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

            if (!materialEntry.getValue().equals(GTMaterials.Rubber))
                GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(new ResourceLocation(GTItems.ELECTRIC_PUMP_IV.getId() + "_" + name))
                        .inputItems(cableGtSingle, GTMaterials.Tungsten)
                        .inputItems(pipeNormalFluid, GTMaterials.TungstenSteel)
                        .inputItems(screw, GTMaterials.TungstenSteel)
                        .inputItems(rotor, GTMaterials.TungstenSteel)
                        .inputItems(ring, materialEntry.getValue(), 2)
                        .inputItems(GTItems.ELECTRIC_MOTOR_IV)
                        .outputItems(GTItems.ELECTRIC_PUMP_IV)
                        .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);
        }

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.CONVEYOR_MODULE_LuV.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 2)
                .inputItems(plate, GTMaterials.HSSS, 2)
                .inputItems(ring, GTMaterials.HSSS, 4)
                .inputItems(round, GTMaterials.HSSS, 16)
                .inputItems(screw, GTMaterials.HSSS, 4)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .inputFluids(GTMaterials.StyreneButadieneRubber.getFluid(GTValues.L * 8))
                .outputItems(GTItems.CONVEYOR_MODULE_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.CONVEYOR_MODULE_ZPM.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(plate, GTMaterials.Osmiridium, 2)
                .inputItems(ring, GTMaterials.Osmiridium, 4)
                .inputItems(round, GTMaterials.Osmiridium, 16)
                .inputItems(screw, GTMaterials.Osmiridium, 4)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 2))
                .inputFluids(GTMaterials.Lubricant.getFluid(500))
                .inputFluids(GTMaterials.StyreneButadieneRubber.getFluid(GTValues.L * 16))
                .outputItems(GTItems.CONVEYOR_MODULE_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.CONVEYOR_MODULE_UV.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 2)
                .inputItems(plate, GTMaterials.Tritanium, 2)
                .inputItems(ring, GTMaterials.Tritanium, 4)
                .inputItems(round, GTMaterials.Tritanium, 16)
                .inputItems(screw, GTMaterials.Tritanium, 4)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .inputFluids(GTMaterials.Lubricant.getFluid(1000))
                .inputFluids(GTMaterials.StyreneButadieneRubber.getFluid(GTValues.L * 24))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.CONVEYOR_MODULE_UV)
                .duration(600).EUt(100000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_PUMP_LuV.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV)
                .inputItems(pipeSmallFluid, GTMaterials.NiobiumTitanium)
                .inputItems(plate, GTMaterials.HSSS, 2)
                .inputItems(screw, GTMaterials.HSSS, 8)
                .inputItems(ring, GTMaterials.SiliconeRubber, 4)
                .inputItems(rotor, GTMaterials.HSSS)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .outputItems(GTItems.ELECTRIC_PUMP_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_PUMP_ZPM.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM)
                .inputItems(pipeNormalFluid, GTMaterials.Polybenzimidazole)
                .inputItems(plate, GTMaterials.Osmiridium, 2)
                .inputItems(screw, GTMaterials.Osmiridium, 8)
                .inputItems(ring, GTMaterials.SiliconeRubber, 8)
                .inputItems(rotor, GTMaterials.Osmiridium)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 2))
                .inputFluids(GTMaterials.Lubricant.getFluid(500))
                .outputItems(GTItems.ELECTRIC_PUMP_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_PUMP_UV.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_UV)
                .inputItems(pipeLargeFluid, GTMaterials.Naquadah)
                .inputItems(plate, GTMaterials.Tritanium, 2)
                .inputItems(screw, GTMaterials.Tritanium, 8)
                .inputItems(ring, GTMaterials.SiliconeRubber, 16)
                .inputItems(rotor, GTMaterials.NaquadahAlloy)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .inputFluids(GTMaterials.Lubricant.getFluid(1000))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.ELECTRIC_PUMP_UV)
                .duration(600).EUt(100000).save(provider);

        //Fluid Regulators----------------------------------------------------------------------------------------------

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_LV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_LV)
                .inputItems(circuit, MarkerMaterials.Tier.LV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_LV)
                .EUt(GTValues.VA[GTValues.LV])
                .duration(400)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_MV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_MV)
                .inputItems(circuit, MarkerMaterials.Tier.MV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_MV)
                .EUt(GTValues.VA[GTValues.MV])
                .duration(350)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_HV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_HV)
                .inputItems(circuit, MarkerMaterials.Tier.HV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_HV)
                .EUt(GTValues.VA[GTValues.HV])
                .duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_EV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_EV)
                .inputItems(circuit, MarkerMaterials.Tier.EV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_EV)
                .EUt(GTValues.VA[GTValues.EV])
                .duration(250)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_IV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_IV)
                .inputItems(circuit, MarkerMaterials.Tier.IV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_IV)
                .EUt(GTValues.VA[GTValues.IV])
                .duration(200)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_LUV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_LuV)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_LUV)
                .EUt(GTValues.VA[GTValues.LuV])
                .duration(150)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_ZPM.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_ZPM)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_ZPM)
                .EUt(GTValues.VA[GTValues.ZPM])
                .duration(100)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FLUID_REGULATOR_UV.getId())
                .inputItems(GTItems.ELECTRIC_PUMP_UV)
                .inputItems(circuit, MarkerMaterials.Tier.UV, 2)
                .circuitMeta(1)
                .outputItems(GTItems.FLUID_REGULATOR_UV)
                .EUt(GTValues.VA[GTValues.UV])
                .duration(50)
                .save(provider);

        //Voiding Covers Start-----------------------------------------------------------------------------------------

        VanillaRecipeHelper.addShapedRecipe(provider, "cover_item_voiding", GTItems.COVER_ITEM_VOIDING.asStack(), "SDS", "dPw", " E ", 'S', new UnificationEntry(screw, GTMaterials.Steel), 'D', GTItems.COVER_ITEM_DETECTOR.get(), 'P', new UnificationEntry(pipeNormalItem, GTMaterials.Brass), 'E', Items.ENDER_PEARL);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.COVER_ITEM_VOIDING.getId())
                .inputItems(screw, GTMaterials.Steel, 2)
                .inputItems(GTItems.COVER_ITEM_DETECTOR)
                .inputItems(pipeNormalItem, GTMaterials.Brass)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(GTItems.COVER_ITEM_VOIDING)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.COVER_ITEM_VOIDING_ADVANCED.getId())
                .inputItems(GTItems.COVER_ITEM_VOIDING)
                .inputItems(circuit, MarkerMaterials.Tier.MV, 1)
                .outputItems(GTItems.COVER_ITEM_VOIDING_ADVANCED)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, "cover_fluid_voiding", GTItems.COVER_FLUID_VOIDING.asStack(), "SDS", "dPw", " E ", 'S', new UnificationEntry(screw, GTMaterials.Steel), 'D', GTItems.COVER_FLUID_DETECTOR.get(), 'P', new UnificationEntry(pipeNormalFluid, GTMaterials.Bronze), 'E', Items.ENDER_PEARL);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.COVER_FLUID_VOIDING.getId())
                .inputItems(screw, GTMaterials.Steel, 2)
                .inputItems(GTItems.COVER_FLUID_DETECTOR)
                .inputItems(pipeNormalFluid, GTMaterials.Bronze)
                .inputItems(Items.ENDER_PEARL)
                .outputItems(GTItems.COVER_FLUID_VOIDING)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.COVER_FLUID_VOIDING_ADVANCED.getId())
                .inputItems(GTItems.COVER_FLUID_VOIDING)
                .inputItems(circuit, MarkerMaterials.Tier.MV, 1)
                .outputItems(GTItems.COVER_FLUID_VOIDING_ADVANCED)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        //Pistons Start-------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_lv", GTItems.ELECTRIC_PISTON_LV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, GTMaterials.Steel), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'R', new UnificationEntry(stick, GTMaterials.Steel), 'G', new UnificationEntry(gearSmall, GTMaterials.Steel), 'M', GTItems.ELECTRIC_MOTOR_LV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_mv", GTItems.ELECTRIC_PISTON_MV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, GTMaterials.Aluminium), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Copper), 'R', new UnificationEntry(stick, GTMaterials.Aluminium), 'G', new UnificationEntry(gearSmall, GTMaterials.Aluminium), 'M', GTItems.ELECTRIC_MOTOR_MV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_hv", GTItems.ELECTRIC_PISTON_HV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, GTMaterials.StainlessSteel), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Gold), 'R', new UnificationEntry(stick, GTMaterials.StainlessSteel), 'G', new UnificationEntry(gearSmall, GTMaterials.StainlessSteel), 'M', GTItems.ELECTRIC_MOTOR_HV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_ev", GTItems.ELECTRIC_PISTON_EV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, GTMaterials.Titanium), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Aluminium), 'R', new UnificationEntry(stick, GTMaterials.Titanium), 'G', new UnificationEntry(gearSmall, GTMaterials.Titanium), 'M', GTItems.ELECTRIC_MOTOR_EV.get());
        VanillaRecipeHelper.addShapedRecipe(provider, "electric_piston_iv", GTItems.ELECTRIC_PISTON_IV.asStack(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(plate, GTMaterials.TungstenSteel), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tungsten), 'R', new UnificationEntry(stick, GTMaterials.TungstenSteel), 'G', new UnificationEntry(gearSmall, GTMaterials.TungstenSteel), 'M', GTItems.ELECTRIC_MOTOR_IV.get());

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_LV.getId())
                .inputItems(stick, GTMaterials.Steel, 2)
                .inputItems(cableGtSingle, GTMaterials.Tin, 2)
                .inputItems(plate, GTMaterials.Steel, 3)
                .inputItems(gearSmall, GTMaterials.Steel)
                .inputItems(GTItems.ELECTRIC_MOTOR_LV)
                .outputItems(GTItems.ELECTRIC_PISTON_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_MV.getId())
                .inputItems(stick, GTMaterials.Aluminium, 2)
                .inputItems(cableGtSingle, GTMaterials.Copper, 2)
                .inputItems(plate, GTMaterials.Aluminium, 3)
                .inputItems(gearSmall, GTMaterials.Aluminium)
                .inputItems(GTItems.ELECTRIC_MOTOR_MV)
                .outputItems(GTItems.ELECTRIC_PISTON_MV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_HV.getId())
                .inputItems(stick, GTMaterials.StainlessSteel, 2)
                .inputItems(cableGtSingle, GTMaterials.Gold, 2)
                .inputItems(plate, GTMaterials.StainlessSteel, 3)
                .inputItems(gearSmall, GTMaterials.StainlessSteel)
                .inputItems(GTItems.ELECTRIC_MOTOR_HV)
                .outputItems(GTItems.ELECTRIC_PISTON_HV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_EV.getId())
                .inputItems(stick, GTMaterials.Titanium, 2)
                .inputItems(cableGtSingle, GTMaterials.Aluminium, 2)
                .inputItems(plate, GTMaterials.Titanium, 3)
                .inputItems(gearSmall, GTMaterials.Titanium)
                .inputItems(GTItems.ELECTRIC_MOTOR_EV)
                .outputItems(GTItems.ELECTRIC_PISTON_EV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_IV.getId())
                .inputItems(stick, GTMaterials.TungstenSteel, 2)
                .inputItems(cableGtSingle, GTMaterials.Tungsten, 2)
                .inputItems(plate, GTMaterials.TungstenSteel, 3)
                .inputItems(gearSmall, GTMaterials.TungstenSteel)
                .inputItems(GTItems.ELECTRIC_MOTOR_IV)
                .outputItems(GTItems.ELECTRIC_PISTON_IV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);


        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_LUV.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV)
                .inputItems(plate, GTMaterials.HSSS, 4)
                .inputItems(ring, GTMaterials.HSSS, 4)
                .inputItems(round, GTMaterials.HSSS, 16)
                .inputItems(stick, GTMaterials.HSSS, 4)
                .inputItems(gear, GTMaterials.HSSS)
                .inputItems(gearSmall, GTMaterials.HSSS, 2)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .outputItems(GTItems.ELECTRIC_PISTON_LUV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_ZPM.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM)
                .inputItems(plate, GTMaterials.Osmiridium, 4)
                .inputItems(ring, GTMaterials.Osmiridium, 4)
                .inputItems(round, GTMaterials.Osmiridium, 16)
                .inputItems(stick, GTMaterials.Osmiridium, 4)
                .inputItems(gear, GTMaterials.Osmiridium)
                .inputItems(gearSmall, GTMaterials.Osmiridium, 2)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 2))
                .inputFluids(GTMaterials.Lubricant.getFluid(500))
                .outputItems(GTItems.ELECTRIC_PISTON_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ELECTRIC_PISTON_UV.getId())
                .inputItems(GTItems.ELECTRIC_MOTOR_UV)
                .inputItems(plate, GTMaterials.Tritanium, 4)
                .inputItems(ring, GTMaterials.Tritanium, 4)
                .inputItems(round, GTMaterials.Tritanium, 16)
                .inputItems(stick, GTMaterials.Tritanium, 4)
                .inputItems(gear, GTMaterials.NaquadahAlloy)
                .inputItems(gearSmall, GTMaterials.NaquadahAlloy, 2)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 2)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .inputFluids(GTMaterials.Lubricant.getFluid(1000))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.ELECTRIC_PISTON_UV)
                .duration(600).EUt(100000).save(provider);



        //Robot Arms Start ---------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_lv", GTItems.ROBOT_ARM_LV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'R', new UnificationEntry(stick, GTMaterials.Steel), 'M', GTItems.ELECTRIC_MOTOR_LV.get(), 'P', GTItems.ELECTRIC_PISTON_LV.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_mv", GTItems.ROBOT_ARM_MV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Copper), 'R', new UnificationEntry(stick, GTMaterials.Aluminium), 'M', GTItems.ELECTRIC_MOTOR_MV.get(), 'P', GTItems.ELECTRIC_PISTON_MV.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_hv", GTItems.ROBOT_ARM_HV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Gold), 'R', new UnificationEntry(stick, GTMaterials.StainlessSteel), 'M', GTItems.ELECTRIC_MOTOR_HV.get(), 'P', GTItems.ELECTRIC_PISTON_HV.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_ev", GTItems.ROBOT_ARM_EV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Aluminium), 'R', new UnificationEntry(stick, GTMaterials.Titanium), 'M', GTItems.ELECTRIC_MOTOR_EV.get(), 'P', GTItems.ELECTRIC_PISTON_EV.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "robot_arm_iv", GTItems.ROBOT_ARM_IV.asStack(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tungsten), 'R', new UnificationEntry(stick, GTMaterials.TungstenSteel), 'M', GTItems.ELECTRIC_MOTOR_IV.get(), 'P', GTItems.ELECTRIC_PISTON_IV.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.IV));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_LV.getId())
                .inputItems(cableGtSingle, GTMaterials.Tin, 3)
                .inputItems(stick, GTMaterials.Steel, 2)
                .inputItems(GTItems.ELECTRIC_MOTOR_LV.asStack(2))
                .inputItems(GTItems.ELECTRIC_PISTON_LV)
                .inputItems(circuit, MarkerMaterials.Tier.LV)
                .outputItems(GTItems.ROBOT_ARM_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_MV.getId())
                .inputItems(cableGtSingle, GTMaterials.Copper, 3)
                .inputItems(stick, GTMaterials.Aluminium, 2)
                .inputItems(GTItems.ELECTRIC_MOTOR_MV.asStack(2))
                .inputItems(GTItems.ELECTRIC_PISTON_MV)
                .inputItems(circuit, MarkerMaterials.Tier.MV)
                .outputItems(GTItems.ROBOT_ARM_MV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_HV.getId())
                .inputItems(cableGtSingle, GTMaterials.Gold, 3)
                .inputItems(stick, GTMaterials.StainlessSteel, 2)
                .inputItems(GTItems.ELECTRIC_MOTOR_HV.asStack(2))
                .inputItems(GTItems.ELECTRIC_PISTON_HV)
                .inputItems(circuit, MarkerMaterials.Tier.HV)
                .outputItems(GTItems.ROBOT_ARM_HV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_EV.getId())
                .inputItems(cableGtSingle, GTMaterials.Aluminium, 3)
                .inputItems(stick, GTMaterials.Titanium, 2)
                .inputItems(GTItems.ELECTRIC_MOTOR_EV.asStack(2))
                .inputItems(GTItems.ELECTRIC_PISTON_EV)
                .inputItems(circuit, MarkerMaterials.Tier.EV)
                .outputItems(GTItems.ROBOT_ARM_EV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_IV.getId())
                .inputItems(cableGtSingle, GTMaterials.Tungsten, 3)
                .inputItems(stick, GTMaterials.TungstenSteel, 2)
                .inputItems(GTItems.ELECTRIC_MOTOR_IV.asStack(2))
                .inputItems(GTItems.ELECTRIC_PISTON_IV)
                .inputItems(circuit, MarkerMaterials.Tier.IV)
                .outputItems(GTItems.ROBOT_ARM_IV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_LuV.getId())
                .inputItems(stickLong, GTMaterials.HSSS, 4)
                .inputItems(gear, GTMaterials.HSSS)
                .inputItems(gearSmall, GTMaterials.HSSS, 3)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV, 2)
                .inputItems(GTItems.ELECTRIC_PISTON_LUV)
                .inputItems(circuit, MarkerMaterials.Tier.LuV)
                .inputItems(circuit, MarkerMaterials.Tier.IV, 2)
                .inputItems(circuit, MarkerMaterials.Tier.EV, 4)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .inputFluids(GTMaterials.Lubricant.getFluid(250))
                .outputItems(GTItems.ROBOT_ARM_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_ZPM.getId())
                .inputItems(stickLong, GTMaterials.Osmiridium, 4)
                .inputItems(gear, GTMaterials.Osmiridium)
                .inputItems(gearSmall, GTMaterials.Osmiridium, 3)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(GTItems.ELECTRIC_PISTON_ZPM)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItems(circuit, MarkerMaterials.Tier.IV, 4)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 8))
                .inputFluids(GTMaterials.Lubricant.getFluid(500))
                .outputItems(GTItems.ROBOT_ARM_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.ROBOT_ARM_UV.getId())
                .inputItems(stickLong, GTMaterials.Tritanium, 4)
                .inputItems(gear, GTMaterials.Tritanium)
                .inputItems(gearSmall, GTMaterials.Tritanium, 3)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 2)
                .inputItems(GTItems.ELECTRIC_PISTON_UV)
                .inputItems(circuit, MarkerMaterials.Tier.UV)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM, 2)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 4)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 12))
                .inputFluids(GTMaterials.Lubricant.getFluid(1000))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.ROBOT_ARM_UV)
                .duration(600).EUt(100000).save(provider);



        //Field Generators Start ---------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_lv", GTItems.FIELD_GENERATOR_LV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, GTMaterials.ManganesePhosphide), 'P', new UnificationEntry(plate, GTMaterials.Steel), 'G', new UnificationEntry(gem, GTMaterials.EnderPearl), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_mv", GTItems.FIELD_GENERATOR_MV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, GTMaterials.MagnesiumDiboride), 'P', new UnificationEntry(plate, GTMaterials.Aluminium), 'G', new UnificationEntry(gem, GTMaterials.EnderEye), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_hv", GTItems.FIELD_GENERATOR_HV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, GTMaterials.MercuryBariumCalciumCuprate), 'P', new UnificationEntry(plate, GTMaterials.StainlessSteel), 'G', GTItems.QUANTUM_EYE.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_ev", GTItems.FIELD_GENERATOR_EV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, GTMaterials.UraniumTriplatinum), 'P', new UnificationEntry(plateDouble, GTMaterials.Titanium), 'G', new UnificationEntry(gem, GTMaterials.NetherStar), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "field_generator_iv", GTItems.FIELD_GENERATOR_IV.asStack(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(wireGtQuadruple, GTMaterials.SamariumIronArsenicOxide), 'P', new UnificationEntry(plateDouble, GTMaterials.TungstenSteel), 'G', GTItems.QUANTUM_STAR.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.IV));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_LV.getId())
                .inputItems(gem, GTMaterials.EnderPearl)
                .inputItems(plate, GTMaterials.Steel, 2)
                .inputItems(circuit, MarkerMaterials.Tier.LV, 2)
                .inputItems(wireGtQuadruple, GTMaterials.ManganesePhosphide, 4)
                .outputItems(GTItems.FIELD_GENERATOR_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_MV.getId())
                .inputItems(gem, GTMaterials.EnderEye)
                .inputItems(plate, GTMaterials.Aluminium, 2)
                .inputItems(circuit, MarkerMaterials.Tier.MV, 2)
                .inputItems(wireGtQuadruple, GTMaterials.MagnesiumDiboride, 4)
                .outputItems(GTItems.FIELD_GENERATOR_MV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_HV.getId())
                .inputItems(GTItems.QUANTUM_EYE)
                .inputItems(plate, GTMaterials.StainlessSteel, 2)
                .inputItems(circuit, MarkerMaterials.Tier.HV, 2)
                .inputItems(wireGtQuadruple, GTMaterials.MercuryBariumCalciumCuprate, 4)
                .outputItems(GTItems.FIELD_GENERATOR_HV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_EV.getId())
                .inputItems(gem, GTMaterials.NetherStar)
                .inputItems(plateDouble, GTMaterials.Titanium, 2)
                .inputItems(circuit, MarkerMaterials.Tier.EV, 2)
                .inputItems(wireGtQuadruple, GTMaterials.UraniumTriplatinum, 4)
                .outputItems(GTItems.FIELD_GENERATOR_EV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_IV.getId())
                .inputItems(GTItems.QUANTUM_STAR)
                .inputItems(plateDouble, GTMaterials.TungstenSteel, 2)
                .inputItems(circuit, MarkerMaterials.Tier.IV, 2)
                .inputItems(wireGtQuadruple, GTMaterials.SamariumIronArsenicOxide, 4)
                .outputItems(GTItems.FIELD_GENERATOR_IV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_LuV.get())
                .inputItems(frameGt, GTMaterials.HSSS)
                .inputItems(plate, GTMaterials.HSSS, 6)
                .inputItems(GTItems.QUANTUM_STAR)
                .inputItems(GTItems.EMITTER_LuV, 2)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItems(wireFine, GTMaterials.IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(wireFine, GTMaterials.IndiumTinBariumTitaniumCuprate, 64)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .outputItems(GTItems.FIELD_GENERATOR_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_ZPM.getId())
                .inputItems(frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(plate, GTMaterials.NaquadahAlloy, 6)
                .inputItems(GTItems.QUANTUM_STAR)
                .inputItems(GTItems.EMITTER_ZPM, 2)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM, 2)
                .inputItems(wireFine, GTMaterials.UraniumRhodiumDinaquadide, 64)
                .inputItems(wireFine, GTMaterials.UraniumRhodiumDinaquadide, 64)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 8))
                .outputItems(GTItems.FIELD_GENERATOR_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.FIELD_GENERATOR_UV.getId())
                .inputItems(frameGt, GTMaterials.Tritanium)
                .inputItems(plate, GTMaterials.Tritanium, 6)
                .inputItems(GTItems.GRAVI_STAR)
                .inputItems(GTItems.EMITTER_UV, 2)
                .inputItems(circuit, MarkerMaterials.Tier.UV, 2)
                .inputItems(wireFine, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(wireFine, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 12))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.FIELD_GENERATOR_UV)
                .duration(600).EUt(100000).save(provider);



        //Sensors Start-------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_lv", GTItems.SENSOR_LV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, GTMaterials.Steel), 'R', new UnificationEntry(stick, GTMaterials.Brass), 'G', new UnificationEntry(gem, GTMaterials.Quartzite), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_mv", GTItems.SENSOR_MV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, GTMaterials.Aluminium), 'R', new UnificationEntry(stick, GTMaterials.Electrum), 'G', new UnificationEntry(gemFlawless, GTMaterials.Emerald), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_hv", GTItems.SENSOR_HV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, GTMaterials.StainlessSteel), 'R', new UnificationEntry(stick, GTMaterials.Chrome), 'G', new UnificationEntry(gem, GTMaterials.EnderEye), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_ev", GTItems.SENSOR_EV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, GTMaterials.Titanium), 'R', new UnificationEntry(stick, GTMaterials.Platinum), 'G', GTItems.QUANTUM_EYE.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "sensor_iv", GTItems.SENSOR_IV.asStack(), "P G", "PR ", "XPP", 'P', new UnificationEntry(plate, GTMaterials.TungstenSteel), 'R', new UnificationEntry(stick, GTMaterials.Iridium), 'G', GTItems.QUANTUM_STAR.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.IV));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.SENSOR_LV.getId())
                .inputItems(stick, GTMaterials.Brass)
                .inputItems(plate, GTMaterials.Steel, 4)
                .inputItems(circuit, MarkerMaterials.Tier.LV)
                .inputItems(gem, GTMaterials.Quartzite)
                .outputItems(GTItems.SENSOR_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.SENSOR_MV.getId())
                .inputItems(stick, GTMaterials.Electrum)
                .inputItems(plate, GTMaterials.Aluminium, 4)
                .inputItems(circuit, MarkerMaterials.Tier.MV)
                .inputItems(gemFlawless, GTMaterials.Emerald)
                .outputItems(GTItems.SENSOR_MV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.SENSOR_HV.getId())
                .inputItems(stick, GTMaterials.Chrome)
                .inputItems(plate, GTMaterials.StainlessSteel, 4)
                .inputItems(circuit, MarkerMaterials.Tier.HV)
                .inputItems(gem, GTMaterials.EnderEye)
                .outputItems(GTItems.SENSOR_HV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.SENSOR_EV.getId())
                .inputItems(stick, GTMaterials.Platinum)
                .inputItems(plate, GTMaterials.Titanium, 4)
                .inputItems(circuit, MarkerMaterials.Tier.EV)
                .inputItems(GTItems.QUANTUM_EYE)
                .outputItems(GTItems.SENSOR_EV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.SENSOR_IV.getId())
                .inputItems(stick, GTMaterials.Iridium)
                .inputItems(plate, GTMaterials.TungstenSteel, 4)
                .inputItems(circuit, MarkerMaterials.Tier.IV)
                .inputItems(GTItems.QUANTUM_STAR)
                .outputItems(GTItems.SENSOR_IV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.SENSOR_LuV.getId())
                .inputItems(frameGt, GTMaterials.HSSS)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV)
                .inputItems(plate, GTMaterials.Ruridit, 4)
                .inputItems(GTItems.QUANTUM_STAR)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItems(foil, GTMaterials.Palladium, 64)
                .inputItems(foil, GTMaterials.Palladium, 32)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 2))
                .outputItems(GTItems.SENSOR_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.SENSOR_ZPM.getId())
                .inputItems(frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM)
                .inputItems(plate, GTMaterials.Osmiridium, 4)
                .inputItems(GTItems.QUANTUM_STAR, 2)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM, 2)
                .inputItems(foil, GTMaterials.Trinium, 64)
                .inputItems(foil, GTMaterials.Trinium, 32)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .outputItems(GTItems.SENSOR_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.SENSOR_UV.getId())
                .inputItems(frameGt, GTMaterials.Tritanium)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV)
                .inputItems(plate, GTMaterials.Tritanium, 4)
                .inputItems(GTItems.GRAVI_STAR)
                .inputItems(circuit, MarkerMaterials.Tier.UV, 2)
                .inputItems(foil, GTMaterials.Naquadria, 64)
                .inputItems(foil, GTMaterials.Naquadria, 32)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 8))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.SENSOR_UV)
                .duration(600).EUt(100000).save(provider);


        //Emitters Start------------------------------------------------------------------------------------------------
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_lv", GTItems.EMITTER_LV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, GTMaterials.Brass), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tin), 'G', new UnificationEntry(gem, GTMaterials.Quartzite), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.LV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_mv", GTItems.EMITTER_MV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, GTMaterials.Electrum), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Copper), 'G', new UnificationEntry(gemFlawless, GTMaterials.Emerald), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.MV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_hv", GTItems.EMITTER_HV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, GTMaterials.Chrome), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Gold), 'G', new UnificationEntry(gem, GTMaterials.EnderEye), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_ev", GTItems.EMITTER_EV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, GTMaterials.Platinum), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Aluminium), 'G', GTItems.QUANTUM_EYE.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));
        VanillaRecipeHelper.addShapedRecipe(provider, "emitter_iv", GTItems.EMITTER_IV.asStack(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(stick, GTMaterials.Iridium), 'C', new UnificationEntry(cableGtSingle, GTMaterials.Tungsten), 'G', GTItems.QUANTUM_STAR.get(), 'X', new UnificationEntry(circuit, MarkerMaterials.Tier.IV));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.EMITTER_LV.getId())
                .inputItems(stick, GTMaterials.Brass, 4)
                .inputItems(cableGtSingle, GTMaterials.Tin, 2)
                .inputItems(circuit, MarkerMaterials.Tier.LV, 2)
                .inputItems(gem, GTMaterials.Quartzite)
                .circuitMeta(1)
                .outputItems(GTItems.EMITTER_LV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.EMITTER_MV.getId())
                .inputItems(stick, GTMaterials.Electrum, 4)
                .inputItems(cableGtSingle, GTMaterials.Copper, 2)
                .inputItems(circuit, MarkerMaterials.Tier.MV, 2)
                .inputItems(gemFlawless, GTMaterials.Emerald)
                .circuitMeta(1)
                .outputItems(GTItems.EMITTER_MV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.EMITTER_HV.getId())
                .inputItems(stick, GTMaterials.Chrome, 4)
                .inputItems(cableGtSingle, GTMaterials.Gold, 2)
                .inputItems(circuit, MarkerMaterials.Tier.HV, 2)
                .inputItems(gem, GTMaterials.EnderEye)
                .circuitMeta(1)
                .outputItems(GTItems.EMITTER_HV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.EMITTER_EV.getId())
                .inputItems(stick, GTMaterials.Platinum, 4)
                .inputItems(cableGtSingle, GTMaterials.Aluminium, 2)
                .inputItems(circuit, MarkerMaterials.Tier.EV, 2)
                .inputItems(GTItems.QUANTUM_EYE)
                .circuitMeta(1)
                .outputItems(GTItems.EMITTER_EV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTItems.EMITTER_IV.getId())
                .inputItems(stick, GTMaterials.Iridium, 4)
                .inputItems(cableGtSingle, GTMaterials.Tungsten, 2)
                .inputItems(circuit, MarkerMaterials.Tier.IV, 2)
                .inputItems(GTItems.QUANTUM_STAR)
                .circuitMeta(1)
                .outputItems(GTItems.EMITTER_IV)
                .duration(100).EUt(GTValues.VA[GTValues.LV]).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.EMITTER_LuV.getId())
                .inputItems(frameGt, GTMaterials.HSSS)
                .inputItems(GTItems.ELECTRIC_MOTOR_LuV)
                .inputItems(stickLong, GTMaterials.Ruridit, 4)
                .inputItems(GTItems.QUANTUM_STAR)
                .inputItems(circuit, MarkerMaterials.Tier.LuV, 2)
                .inputItems(foil, GTMaterials.Palladium, 64)
                .inputItems(foil, GTMaterials.Palladium, 32)
                .inputItems(cableGtSingle, GTMaterials.NiobiumTitanium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 2))
                .outputItems(GTItems.EMITTER_LuV)
                .duration(600).EUt(6000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.EMITTER_ZPM.getId())
                .inputItems(frameGt, GTMaterials.NaquadahAlloy)
                .inputItems(GTItems.ELECTRIC_MOTOR_ZPM)
                .inputItems(stickLong, GTMaterials.Osmiridium, 4)
                .inputItems(GTItems.QUANTUM_STAR, 2)
                .inputItems(circuit, MarkerMaterials.Tier.ZPM, 2)
                .inputItems(foil, GTMaterials.Trinium, 64)
                .inputItems(foil, GTMaterials.Trinium, 32)
                .inputItems(cableGtSingle, GTMaterials.VanadiumGallium, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 4))
                .outputItems(GTItems.EMITTER_ZPM)
                .duration(600).EUt(24000).save(provider);

        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GTItems.EMITTER_UV.getId())
                .inputItems(frameGt, GTMaterials.Tritanium)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV)
                .inputItems(stickLong, GTMaterials.Tritanium, 4)
                .inputItems(GTItems.GRAVI_STAR)
                .inputItems(circuit, MarkerMaterials.Tier.UV, 2)
                .inputItems(foil, GTMaterials.Naquadria, 64)
                .inputItems(foil, GTMaterials.Naquadria, 32)
                .inputItems(cableGtSingle, GTMaterials.YttriumBariumCuprate, 4)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(GTValues.L * 8))
                .inputFluids(GTMaterials.Naquadria.getFluid(GTValues.L * 4))
                .outputItems(GTItems.EMITTER_UV)
                .duration(600).EUt(100000).save(provider);
    }
}
