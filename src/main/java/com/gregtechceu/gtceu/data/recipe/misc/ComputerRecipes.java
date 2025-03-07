package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.common.data.machines.GTResearchMachines.*;

public class ComputerRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        ASSEMBLER_RECIPES.recipeBuilder("data_access_hatch")
                .inputItems(ITEM_IMPORT_BUS[EV])
                .inputItems(TOOL_DATA_STICK, 4)
                .inputItems(CustomTags.EV_CIRCUITS, 4)
                .outputItems(DATA_ACCESS_HATCH)
                .inputFluids(Polytetrafluoroethylene, L * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[EV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("advanced_data_access_hatch")
                .inputItems(ITEM_IMPORT_BUS[LuV])
                .inputItems(TOOL_DATA_ORB, 4)
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .outputItems(ADVANCED_DATA_ACCESS_HATCH)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Polybenzimidazole, L * 4)
                .stationResearch(b -> b.researchStack(DATA_BANK.asStack()).CWUt(4))
                .duration(400).EUt(6000)
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("high_power_casing")
                .inputItems(FRAME_GT, Iridium)
                .inputItems(PLATE, Iridium, 6)
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(WIRE_FINE, Cobalt, 16)
                .inputItems(WIRE_FINE, Copper, 16)
                .inputItems(WIRE_GT_SINGLE, NiobiumTitanium, 2)
                .outputItems(HIGH_POWER_CASING,
                        ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(100).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("computer_casing")
                .inputItems(FRAME_GT, Iridium)
                .inputItems(PLATE, Iridium, 6)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(WIRE_FINE, Cobalt, 32)
                .inputItems(WIRE_FINE, Copper, 32)
                .inputItems(WIRE_GT_SINGLE, VanadiumGallium, 2)
                .outputItems(COMPUTER_CASING,
                        ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("advanced_computer_casing")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(WIRE_FINE, Cobalt, 64)
                .inputItems(WIRE_FINE, Electrum, 64)
                .inputItems(WIRE_GT_SINGLE, IndiumTinBariumTitaniumCuprate, 4)
                .outputItems(ADVANCED_COMPUTER_CASING)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("computer_heat_vent")
                .inputItems(FRAME_GT, StainlessSteel)
                .inputItems(ELECTRIC_MOTOR_IV, 2)
                .inputItems(ROTOR, StainlessSteel, 2)
                .inputItems(PIPE_TINY_FLUID, StainlessSteel, 16)
                .inputItems(PLATE, Copper, 16)
                .inputItems(WIRE_GT_SINGLE, SamariumIronArsenicOxide)
                .outputItems(COMPUTER_HEAT_VENT,
                        ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(100).EUt(VA[EV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("optical_pipe")
                .inputItems(WIRE_FINE, BorosilicateGlass, 8)
                .inputItems(FOIL, Silver, 8)
                .inputFluids(Polytetrafluoroethylene, L)
                .cleanroom(CleanroomType.CLEANROOM)
                .outputItems(OPTICAL_PIPES[0])
                .duration(100).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("data_bank")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(CustomTags.LuV_CIRCUITS, 8)
                .inputItems(TOOL_DATA_ORB)
                .inputItems(WIRE_FINE, Cobalt, 64)
                .inputItems(WIRE_FINE, Copper, 64)
                .inputItems(OPTICAL_PIPES[0].asStack(4))
                .inputItems(WIRE_GT_DOUBLE, IndiumTinBariumTitaniumCuprate, 16)
                .inputFluids(SolderingAlloy, L * 2)
                .inputFluids(Lubricant, 500)
                .outputItems(DATA_BANK)
                .scannerResearch(b -> b
                        .researchStack(DATA_ACCESS_HATCH.asStack())
                        .duration(2400)
                        .EUt(VA[EV]))
                .duration(1200).EUt(6000)
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("research_station")
                .inputItems(DATA_BANK)
                .inputItems(SENSOR_LuV, 8)
                .inputItems(CustomTags.ZPM_CIRCUITS, 8)
                .inputItems(FIELD_GENERATOR_LuV, 2)
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(WIRE_GT_DOUBLE, UraniumRhodiumDinaquadide, 32)
                .inputItems(FOIL, Trinium, 32)
                .inputItems(OPTICAL_PIPES[0].asStack(16))
                .inputFluids(SolderingAlloy, L * 8)
                .inputFluids(VanadiumGallium, L * 8)
                .outputItems(RESEARCH_STATION)
                .scannerResearch(b -> b
                        .researchStack(SCANNER[LuV].asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(100000)
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("object_holder")
                .inputItems(ITEM_IMPORT_BUS[ZPM])
                .inputItems(EMITTER_LuV, 8)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(ROBOT_ARM_ZPM, 2)
                .inputItems(ELECTRIC_MOTOR_ZPM, 2)
                .inputItems(WIRE_GT_DOUBLE, UraniumRhodiumDinaquadide, 16)
                .inputItems(OPTICAL_PIPES[0].asStack(2))
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Polybenzimidazole, L * 2)
                .outputItems(OBJECT_HOLDER)
                .scannerResearch(b -> b
                        .researchStack(ITEM_IMPORT_BUS[ZPM].asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(100000)
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("network_switch")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(EMITTER_ZPM, 4)
                .inputItems(SENSOR_ZPM, 4)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(WIRE_GT_DOUBLE, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .inputItems(FOIL, Tritanium, 64)
                .inputItems(FOIL, Tritanium, 64)
                .inputItems(OPTICAL_PIPES[0].asStack(8))
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Polybenzimidazole, L * 4)
                .outputItems(NETWORK_SWITCH)
                .stationResearch(b -> b
                        .researchStack(new ItemStack(OPTICAL_PIPES[0]))
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(1200).EUt(100000)
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("high_performance_computing_array")
                .inputItems(DATA_BANK)
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .inputItems(FIELD_GENERATOR_LuV, 8)
                .inputItems(TOOL_DATA_ORB)
                .inputItems(COVER_SCREEN)
                .inputItems(WIRE_GT_DOUBLE, UraniumRhodiumDinaquadide, 64)
                .inputItems(OPTICAL_PIPES[0].asStack(16))
                .inputFluids(SolderingAlloy, L * 8)
                .inputFluids(VanadiumGallium, L * 8)
                .inputFluids(PCBCoolant, 4000)
                .outputItems(HIGH_PERFORMANCE_COMPUTING_ARRAY)
                .scannerResearch(b -> b
                        .researchStack(COVER_SCREEN.asStack())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(100000)
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_empty_component")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(TOOL_DATA_STICK)
                .outputItems(HPCA_EMPTY_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_heat_sink_component")
                .inputItems(HPCA_EMPTY_COMPONENT)
                .inputItems(PLATE, Aluminium, 32)
                .inputItems(SCREW, StainlessSteel, 8)
                .outputItems(HPCA_HEAT_SINK_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_active_cooler_component")
                .inputItems(ADVANCED_COMPUTER_CASING.asStack())
                .inputItems(PLATE, Aluminium, 16)
                .inputItems(PIPE_TINY_FLUID, StainlessSteel, 16)
                .inputItems(SCREW, StainlessSteel, 8)
                .outputItems(HPCA_ACTIVE_COOLER_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_bridge_component")
                .inputItems(ADVANCED_COMPUTER_CASING.asStack())
                .inputItems(CustomTags.UV_CIRCUITS)
                .inputItems(EMITTER_ZPM)
                .inputItems(OPTICAL_PIPES[0].asStack(2))
                .outputItems(HPCA_BRIDGE_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_computation_component")
                .inputItems(HPCA_EMPTY_COMPONENT)
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .inputItems(FIELD_GENERATOR_LuV)
                .outputItems(HPCA_COMPUTATION_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_advanced_computation_component")
                .inputItems(HPCA_COMPUTATION_COMPONENT)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(FIELD_GENERATOR_ZPM)
                .outputItems(HPCA_ADVANCED_COMPUTATION_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[ZPM])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("data_receiver_hatch")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(ITEM_IMPORT_BUS[LuV])
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(SENSOR_IV)
                .inputItems(OPTICAL_PIPES[0].asStack(2))
                .inputFluids(Polybenzimidazole, L * 2)
                .outputItems(DATA_HATCH_RECEIVER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("data_transmitter_hatch")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(ITEM_EXPORT_BUS[LuV])
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(EMITTER_IV)
                .inputItems(OPTICAL_PIPES[0].asStack(2))
                .inputFluids(Polybenzimidazole, L * 2)
                .outputItems(DATA_HATCH_TRANSMITTER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("computation_receiver_hatch")
                .inputItems(DATA_HATCH_RECEIVER)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(SENSOR_LuV)
                .inputFluids(Polybenzimidazole, L * 2)
                .outputItems(COMPUTATION_HATCH_RECEIVER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("computation_transmitter_hatch")
                .inputItems(DATA_HATCH_TRANSMITTER)
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(EMITTER_LuV)
                .inputFluids(Polybenzimidazole, L * 2)
                .outputItems(COMPUTATION_HATCH_TRANSMITTER)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true, true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("active_transformer")
                .inputItems(POWER_TRANSFORMER[LuV])
                .inputItems(CustomTags.LuV_CIRCUITS, 2)
                .inputItems(WIRE_GT_SINGLE, IndiumTinBariumTitaniumCuprate, 8)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputFluids(PCBCoolant, 1000)
                .outputItems(GTMultiMachines.ACTIVE_TRANSFORMER)
                .duration(300).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("laser_cable")
                .inputItems(CASING_LAMINATED_GLASS.asStack(1))
                .inputItems(FOIL, Osmiridium, 2)
                .inputFluids(Polytetrafluoroethylene, L)
                .outputItems(LASER_PIPES[0])
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);
    }
}
