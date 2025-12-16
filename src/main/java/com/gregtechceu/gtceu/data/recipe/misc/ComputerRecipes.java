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
        ASSEMBLER_RECIPES.recipeBuilder("basic_data_access_hatch")
                .inputItems(ITEM_IMPORT_BUS[HV])
                .inputItems(TOOL_DATA_STICK, 4)
                .inputItems(CustomTags.HV_CIRCUITS, 4)
                .outputItems(BASIC_DATA_ACCESS_HATCH)
                .inputFluids(Polyethylene, L * 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[HV])
                .addMaterialInfo(true).save(provider);

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

        ASSEMBLER_RECIPES.recipeBuilder("wireless_transmitter_cover")
                .inputItems(plate, EnderPearl)
                .inputItems(foil, AnnealedCopper)
                .inputItems(EMITTER_MV)
                .inputItems(wireFine, Platinum)
                .inputFluids(SolderingAlloy, L)
                .outputItems(COVER_WIRELESS_TRANSMITTER)
                .duration(1000).EUt(VA[MV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("text_module")
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(foil, Steel, 4)
                .inputItems(wireFine, RedAlloy, 4)
                .inputItems(CustomTags.MV_CIRCUITS)
                .inputFluids(SolderingAlloy, L)
                .outputItems(TEXT_MODULE)
                .duration(1000).EUt(VA[MV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("image_module")
                .inputItems(PLASTIC_CIRCUIT_BOARD)
                .inputItems(foil, Electrum, 4)
                .inputItems(wireFine, Silver, 4)
                .inputItems(CustomTags.MV_CIRCUITS)
                .inputFluids(SolderingAlloy, L)
                .outputItems(IMAGE_MODULE)
                .duration(1000).EUt(VA[MV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("monitor_casing")
                .inputItems(HULL[MV])
                .inputItems(COVER_SCREEN)
                .inputItems(plate, Glass, 4)
                .inputItems(wireFine, RedAlloy, 4)
                .inputFluids(Glowstone, L)
                .outputItems(MONITOR, ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(1000).EUt(VA[MV])
                .addMaterialInfo(true).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("advanced_monitor_casing")
                .inputItems(MONITOR)
                .inputItems(CustomTags.HV_CIRCUITS)
                .inputItems(plate, StainlessSteel, 4)
                .inputFluids(SolderingAlloy, L)
                .outputItems(ADVANCED_MONITOR, 1)
                .duration(1000).EUt(VA[HV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("high_power_casing")
                .inputItems(frameGt, Iridium)
                .inputItems(plate, Iridium, 6)
                .inputItems(CustomTags.IV_CIRCUITS)
                .inputItems(wireFine, Cobalt, 16)
                .inputItems(wireFine, Copper, 16)
                .inputItems(wireGtSingle, NiobiumTitanium, 2)
                .outputItems(HIGH_POWER_CASING,
                        ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(100).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("computer_casing")
                .inputItems(frameGt, Iridium)
                .inputItems(plate, Iridium, 6)
                .inputItems(CustomTags.LuV_CIRCUITS)
                .inputItems(wireFine, Cobalt, 32)
                .inputItems(wireFine, Copper, 32)
                .inputItems(wireGtSingle, VanadiumGallium, 2)
                .outputItems(COMPUTER_CASING,
                        ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("advanced_computer_casing")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(CustomTags.ZPM_CIRCUITS)
                .inputItems(wireFine, Cobalt, 64)
                .inputItems(wireFine, Electrum, 64)
                .inputItems(wireGtSingle, IndiumTinBariumTitaniumCuprate, 4)
                .outputItems(ADVANCED_COMPUTER_CASING)
                .duration(200).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("computer_heat_vent")
                .inputItems(frameGt, StainlessSteel)
                .inputItems(ELECTRIC_MOTOR_IV, 2)
                .inputItems(rotor, StainlessSteel, 2)
                .inputItems(pipeTinyFluid, StainlessSteel, 16)
                .inputItems(plate, Copper, 16)
                .inputItems(wireGtSingle, SamariumIronArsenicOxide)
                .outputItems(COMPUTER_HEAT_VENT,
                        ConfigHolder.INSTANCE.recipes.casingsPerCraft)
                .duration(100).EUt(VA[EV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("optical_pipe")
                .inputItems(wireFine, BorosilicateGlass, 8)
                .inputItems(foil, Silver, 8)
                .inputFluids(Polytetrafluoroethylene, L)
                .cleanroom(CleanroomType.CLEANROOM)
                .outputItems(OPTICAL_PIPES[0])
                .duration(100).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLY_LINE_RECIPES.recipeBuilder("data_bank")
                .inputItems(COMPUTER_CASING.asStack())
                .inputItems(CustomTags.LuV_CIRCUITS, 8)
                .inputItems(TOOL_DATA_ORB)
                .inputItems(wireFine, Cobalt, 64)
                .inputItems(wireFine, Copper, 64)
                .inputItems(OPTICAL_PIPES[0].asStack(4))
                .inputItems(wireGtDouble, IndiumTinBariumTitaniumCuprate, 16)
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
                .inputItems(wireGtDouble, UraniumRhodiumDinaquadide, 32)
                .inputItems(foil, Trinium, 32)
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
                .inputItems(wireGtDouble, UraniumRhodiumDinaquadide, 16)
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
                .inputItems(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .inputItems(foil, Tritanium, 64)
                .inputItems(foil, Tritanium, 64)
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
                .inputItems(wireGtDouble, UraniumRhodiumDinaquadide, 64)
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
                .inputItems(plate, Aluminium, 32)
                .inputItems(screw, StainlessSteel, 8)
                .outputItems(HPCA_HEAT_SINK_COMPONENT)
                .inputFluids(PCBCoolant, 1000)
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(200).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hpca_active_cooler_component")
                .inputItems(ADVANCED_COMPUTER_CASING.asStack())
                .inputItems(plate, Aluminium, 16)
                .inputItems(pipeTinyFluid, StainlessSteel, 16)
                .inputItems(screw, StainlessSteel, 8)
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
                .inputItems(wireGtSingle, IndiumTinBariumTitaniumCuprate, 8)
                .inputItems(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 2)
                .inputFluids(PCBCoolant, 1000)
                .outputItems(GTMultiMachines.ACTIVE_TRANSFORMER)
                .duration(300).EUt(VA[LuV])
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("laser_cable")
                .inputItems(CASING_LAMINATED_GLASS.asStack(1))
                .inputItems(foil, Osmiridium, 2)
                .inputFluids(Polytetrafluoroethylene, L)
                .outputItems(LASER_PIPES[0])
                .cleanroom(CleanroomType.CLEANROOM)
                .duration(100).EUt(VA[IV])
                .addMaterialInfo(true).save(provider);
    }
}
