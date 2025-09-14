package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.HPCAMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.NetworkSwitchMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalComputationHatchMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalDataHatchMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.*;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.CREATIVE_TOOLTIPS;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTResearchMachines {

    public static final MultiblockMachineDefinition RESEARCH_STATION = REGISTRATE
            .multiblock("research_station", ResearchStationMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.RESEARCH_STATION_RECIPES)
            .appearanceBlock(ADVANCED_COMPUTER_CASING)
            .tooltips(LangHandler.getMultiLang("gtceu.machine.research_station.tooltip"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "VVV", "PPP", "PPP", "PPP", "VVV", "XXX")
                    .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                    .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                    .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                    .aisle(" X ", "XAX", "---", "---", "---", "XAX", " X ")
                    .aisle(" X ", "XAX", "-A-", "-H-", "-A-", "XAX", " X ")
                    .aisle("   ", "XXX", "---", "---", "---", "XXX", "   ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(COMPUTER_CASING.get()))
                    .where(' ', any())
                    .where('-', air())
                    .where('V', blocks(COMPUTER_HEAT_VENT.get()))
                    .where('A', blocks(ADVANCED_COMPUTER_CASING.get()))
                    .where('P', blocks(COMPUTER_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2, 1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                            .or(autoAbilities(true, false, false)))
                    .where('H', abilities(PartAbility.OBJECT_HOLDER))
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle("---", "XXX", "---", "---", "---", "XXX", "---")
                    .aisle("-X-", "XAX", "-A-", "-H-", "-A-", "XAX", "-X-")
                    .aisle("-X-", "XAX", "---", "---", "---", "XAX", "-X-")
                    .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                    .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                    .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                    .aisle("XXX", "VVV", "POP", "PEP", "PMP", "VVV", "XXX")
                    .where('S', GTResearchMachines.RESEARCH_STATION, Direction.NORTH)
                    .where('X', COMPUTER_CASING.get())
                    .where('-', Blocks.AIR)
                    .where('V', COMPUTER_HEAT_VENT.get())
                    .where('A', ADVANCED_COMPUTER_CASING.get())
                    .where('P', COMPUTER_CASING.get())
                    .where('O', GTResearchMachines.COMPUTATION_HATCH_RECEIVER, Direction.SOUTH)
                    .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], Direction.SOUTH)
                    .where('M', ConfigHolder.INSTANCE.machines.enableMaintenance ?
                            GTMachines.MAINTENANCE_HATCH.getBlock().defaultBlockState().setValue(
                                    GTMachines.MAINTENANCE_HATCH.get().getRotationState().property, Direction.SOUTH) :
                            COMPUTER_CASING.getDefaultState())
                    .where('H', GTResearchMachines.OBJECT_HOLDER, Direction.SOUTH)
                    .build())
            .sidedWorkableCasingModel(GTCEu.id("block/casings/hpca/advanced_computer_casing"),
                    GTCEu.id("block/multiblock/research_station"))
            .register();

    public static final MachineDefinition OBJECT_HOLDER = REGISTRATE.machine("object_holder", ObjectHolderMachine::new)
            .langValue("Object Holder")
            .tier(ZPM)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.OBJECT_HOLDER)
            .modelProperty(IS_FORMED, false)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(createWorkableTieredHullMachineModel(GTCEu.id("block/machines/object_holder"))
                    .andThen((ctx, prov, model) -> {
                        model.addReplaceableTextures("bottom", "top", "side");
                    }))
            .register();

    public static final MachineDefinition DATA_BANK = REGISTRATE.multiblock("data_bank", DataBankMachine::new)
            .langValue("Data Bank")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(COMPUTER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("gtceu.machine.data_bank.tooltip.0"),
                    Component.translatable("gtceu.machine.data_bank.tooltip.1"),
                    Component.translatable("gtceu.machine.data_bank.tooltip.2"),
                    Component.translatable("gtceu.machine.data_bank.tooltip.3",
                            FormattingUtil.formatNumbers(DataBankMachine.EUT_PER_HATCH)),
                    Component.translatable("gtceu.machine.data_bank.tooltip.4",
                            FormattingUtil.formatNumbers(DataBankMachine.EUT_PER_HATCH_CHAINED)))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XDDDX", "XDDDX", "XDDDX")
                    .aisle("XDDDX", "XAAAX", "XDDDX")
                    .aisle("XCCCX", "XCSCX", "XCCCX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(COMPUTER_HEAT_VENT.get()))
                    .where('D', blocks(COMPUTER_CASING.get()).setMinGlobalLimited(3)
                            .or(abilities(PartAbility.DATA_ACCESS).setPreviewCount(3))
                            .or(abilities(PartAbility.OPTICAL_DATA_TRANSMISSION).setMinGlobalLimited(1, 1))
                            .or(abilities(PartAbility.OPTICAL_DATA_RECEPTION).setPreviewCount(1)))
                    .where('A', blocks(COMPUTER_CASING.get()))
                    .where('C', blocks(HIGH_POWER_CASING.get()).setMinGlobalLimited(4)
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2, 1))
                            .or(autoAbilities(true, false, false)))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/hpca/high_power_casing"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    public static final MachineDefinition NETWORK_SWITCH = REGISTRATE
            .multiblock("network_switch", NetworkSwitchMachine::new)
            .langValue("Network Switch")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(COMPUTER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(Component.translatable("gtceu.machine.network_switch.tooltip.0"),
                    Component.translatable("gtceu.machine.network_switch.tooltip.1"),
                    Component.translatable("gtceu.machine.network_switch.tooltip.2"),
                    Component.translatable("gtceu.machine.network_switch.tooltip.3",
                            FormattingUtil.formatNumbers(NetworkSwitchMachine.EUT_PER_HATCH)))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XAX", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('A', blocks(ADVANCED_COMPUTER_CASING.get()))
                    .where('X', blocks(COMPUTER_CASING.get()).setMinGlobalLimited(7)
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2, 1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION).setMinGlobalLimited(1, 1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setMinGlobalLimited(1, 2))
                            .or(autoAbilities(true, false, false)))
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle("XMX", "XSX", "XRX")
                    .aisle("XXX", "XAX", "XXX")
                    .aisle("XEX", "XXX", "TTT")
                    .where('S', GTResearchMachines.NETWORK_SWITCH, Direction.NORTH)
                    .where('X', COMPUTER_CASING)
                    .where('A', ADVANCED_COMPUTER_CASING)
                    .where('R', GTResearchMachines.COMPUTATION_HATCH_RECEIVER, Direction.NORTH)
                    .where('T', GTResearchMachines.COMPUTATION_HATCH_TRANSMITTER, Direction.SOUTH)
                    .where('M', GTMachines.MAINTENANCE_HATCH, Direction.NORTH)
                    .where('E', GTMachines.ENERGY_INPUT_HATCH[LuV], Direction.NORTH)
                    .build())
            .sidedWorkableCasingModel(GTCEu.id("block/casings/hpca/computer_casing"),
                    GTCEu.id("block/multiblock/network_switch"))
            .register();

    public static final MachineDefinition HIGH_PERFORMANCE_COMPUTING_ARRAY = REGISTRATE
            .multiblock("high_performance_computation_array", HPCAMachine::new)
            .langValue("High Performance Computation Array (HPCA)")
            .rotationState(RotationState.NON_Y_AXIS)
            // TODO : Make a controllerAppearanceBlock() so the controller CTM's to the correct casings - Also just a
            // good API addition for packdevs
            .appearanceBlock(COMPUTER_CASING)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(LangHandler.getMultiLang("gtceu.machine.high_performance_computation_array.tooltip"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AA", "CC", "CC", "CC", "AA")
                    .aisle("VA", "XV", "XV", "XV", "VA")
                    .aisle("VA", "XV", "XV", "XV", "VA")
                    .aisle("VA", "XV", "XV", "XV", "VA")
                    .aisle("SA", "CC", "CC", "CC", "AA")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('A', blocks(ADVANCED_COMPUTER_CASING.get()))
                    .where('V', blocks(COMPUTER_HEAT_VENT.get()))
                    .where('X', abilities(PartAbility.HPCA_COMPONENT))
                    .where('C', blocks(COMPUTER_CASING.get()).setMinGlobalLimited(5)
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2, 1))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                            .or(abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION).setExactLimit(1))
                            .or(autoAbilities(true, false, false)))
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("SA", "CC", "CC", "OC", "AA")
                        .aisle("VA", "8V", "5V", "2V", "VA")
                        .aisle("VA", "7V", "4V", "1V", "VA")
                        .aisle("VA", "6V", "3V", "0V", "VA")
                        .aisle("AA", "EC", "MC", "HC", "AA")
                        .where('S', GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY, Direction.NORTH)
                        .where('A', ADVANCED_COMPUTER_CASING)
                        .where('V', COMPUTER_HEAT_VENT)
                        .where('C', COMPUTER_CASING)
                        .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], Direction.SOUTH)
                        .where('H', GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], Direction.SOUTH)
                        .where('O', GTResearchMachines.COMPUTATION_HATCH_TRANSMITTER, Direction.NORTH)
                        .where('M', ConfigHolder.INSTANCE.machines.enableMaintenance ?
                                GTMachines.MAINTENANCE_HATCH.defaultBlockState().setValue(
                                        GTMachines.MAINTENANCE_HATCH.get().getRotationState().property,
                                        Direction.SOUTH) :
                                COMPUTER_CASING.getDefaultState());

                // a few example structures
                shapeInfo.add(builder.shallowCopy()
                        .where('0', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                        .where('1', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('2', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                        .where('3', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                        .where('4', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('5', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                        .where('6', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                        .where('7', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('8', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                        .build());

                shapeInfo.add(builder.shallowCopy()
                        .where('0', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('1', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('2', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('3', GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, Direction.WEST)
                        .where('4', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('5', GTResearchMachines.HPCA_BRIDGE_COMPONENT, Direction.WEST)
                        .where('6', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('7', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('8', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .build());

                shapeInfo.add(builder.shallowCopy()
                        .where('0', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('1', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('2', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('3', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('4', GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('5', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('6', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('7', GTResearchMachines.HPCA_BRIDGE_COMPONENT, Direction.WEST)
                        .where('8', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .build());

                shapeInfo.add(builder.shallowCopy()
                        .where('0', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('1', GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('2', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('3', GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, Direction.WEST)
                        .where('4', GTResearchMachines.HPCA_BRIDGE_COMPONENT, Direction.WEST)
                        .where('5', GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, Direction.WEST)
                        .where('6', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .where('7', GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, Direction.WEST)
                        .where('8', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                        .build());

                return shapeInfo;
            })
            .sidedWorkableCasingModel(GTCEu.id("block/casings/hpca/computer_casing"),
                    GTCEu.id("block/multiblock/hpca"))
            .register();

    ////////////////////////////////////////////
    // ******** MULTIBLOCK PARTS ********//
    ////////////////////////////////////////////

    public static final MachineDefinition COMPUTATION_HATCH_TRANSMITTER = registerDataHatch(
            "computation_transmitter_hatch", "Computation Data Transmission Hatch",
            ZPM, (holder) -> new OpticalComputationHatchMachine(holder, true),
            "computation_data_hatch", PartAbility.COMPUTATION_DATA_TRANSMISSION)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            .register();

    public static final MachineDefinition COMPUTATION_HATCH_RECEIVER = registerDataHatch(
            "computation_receiver_hatch", "Computation Data Reception Hatch",
            ZPM, (holder) -> new OpticalComputationHatchMachine(holder, false),
            "computation_data_hatch", PartAbility.COMPUTATION_DATA_RECEPTION)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            .register();

    public static final MachineDefinition DATA_HATCH_TRANSMITTER = registerDataHatch(
            "data_transmitter_hatch", "Optical Data Transmission Hatch",
            LuV, (holder) -> new OpticalDataHatchMachine(holder, true),
            "optical_data_hatch", PartAbility.OPTICAL_DATA_TRANSMISSION)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            .register();

    public static final MachineDefinition DATA_HATCH_RECEIVER = registerDataHatch(
            "data_receiver_hatch", "Optical Data Reception Hatch",
            LuV, (holder) -> new OpticalDataHatchMachine(holder, false),
            "optical_data_hatch", PartAbility.OPTICAL_DATA_RECEPTION)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            .register();

    public static final MachineDefinition BASIC_DATA_ACCESS_HATCH = REGISTRATE
            .machine("basic_data_access_hatch", (holder) -> new DataAccessHatchMachine(holder, HV, false))
            .langValue("Basic Data Access Hatch")
            .tier(HV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.DATA_ACCESS)
            .tooltips(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"),
                    Component.translatable("gtceu.machine.data_access_hatch.tooltip.1", 4),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .overlayTieredHullModel("data_access_hatch")
            .register();

    public static final MachineDefinition DATA_ACCESS_HATCH = REGISTRATE
            .machine("data_access_hatch", (holder) -> new DataAccessHatchMachine(holder, EV, false))
            .langValue("Data Access Hatch")
            .tier(EV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.DATA_ACCESS)
            .tooltips(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"),
                    Component.translatable("gtceu.machine.data_access_hatch.tooltip.1", 9),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .overlayTieredHullModel("data_access_hatch")
            .register();

    public static final MachineDefinition ADVANCED_DATA_ACCESS_HATCH = REGISTRATE
            .machine("advanced_data_access_hatch", (holder) -> new DataAccessHatchMachine(holder, LuV, false))
            .langValue("Advanced Data Access Hatch")
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.DATA_ACCESS)
            .tooltips(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"),
                    Component.translatable("gtceu.machine.data_access_hatch.tooltip.1", 16),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .overlayTieredHullModel("data_access_hatch")
            .register();

    public static final MachineDefinition CREATIVE_DATA_ACCESS_HATCH = REGISTRATE
            .machine("creative_data_access_hatch", (holder) -> new DataAccessHatchMachine(holder, MAX, true))
            .langValue("Creative Data Access Hatch")
            .tier(MAX)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.DATA_ACCESS)
            .modelProperty(IS_FORMED, false)
            .tooltipBuilder((s, list) -> {
                list.add(Component.translatable("gtceu.machine.data_access_hatch.tooltip.0"));
                CREATIVE_TOOLTIPS.accept(s, list);
                list.add(Component.translatable("gtceu.part_sharing.enabled"));
            })
            .overlayTieredHullModel("data_access_hatch_creative")
            .register();

    //////////////////////////////////////
    // *********** HPCA ***********//
    //////////////////////////////////////

    public static final BiConsumer<ItemStack, List<Component>> OVERHEAT_TOOLTIPS = (stack, components) -> components
            .add(Component.translatable("gtceu.machine.hpca.component_type.damaged")
                    .withStyle(style -> style.withColor(TooltipHelper.BLINKING_ORANGE.getCurrent())));

    public static final MachineDefinition HPCA_EMPTY_COMPONENT = registerHPCAPart(
            "hpca_empty_component", "Empty HPCA Component",
            HPCAEmptyPartMachine::new, "empty", false)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"))
            .register();
    public static final MachineDefinition HPCA_COMPUTATION_COMPONENT = registerHPCAPart(
            "hpca_computation_component", "HPCA Computation Component",
            holder -> new HPCAComputationPartMachine(holder, false), "computation", false)
            .tooltips(
                    Component.translatable("gtceu.machine.hpca.component_general.upkeep_eut", GTValues.VA[GTValues.EV]),
                    Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.LuV]),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cwut", 4),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cooling", 2),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .tooltipBuilder(OVERHEAT_TOOLTIPS)
            .register();
    public static final MachineDefinition HPCA_ADVANCED_COMPUTATION_COMPONENT = registerHPCAPart(
            "hpca_advanced_computation_component", "HPCA Advanced Computation Component",
            holder -> new HPCAComputationPartMachine(holder, true), "advanced_computation", true)
            .tooltips(
                    Component.translatable("gtceu.machine.hpca.component_general.upkeep_eut", GTValues.VA[GTValues.IV]),
                    Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.ZPM]),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cwut", 16),
                    Component.translatable("gtceu.machine.hpca.component_type.computation_cooling", 4),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .tooltipBuilder(OVERHEAT_TOOLTIPS)
            .register();
    public static final MachineDefinition HPCA_HEAT_SINK_COMPONENT = registerHPCAPart(
            "hpca_heat_sink_component", "HPCA Heat Sink Component",
            holder -> new HPCACoolerPartMachine(holder, false), "heat_sink", false)
            .tooltips(Component.translatable("gtceu.machine.hpca.component_type.cooler_passive"),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_cooling", 1),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .register();
    public static final MachineDefinition HPCA_ACTIVE_COOLER_COMPONENT = registerHPCAPart(
            "hpca_active_cooler_component", "HPCA Active Cooling Component",
            holder -> new HPCACoolerPartMachine(holder, true), "active_cooler", true)
            .tooltips(Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.IV]),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_active"),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_active_coolant",
                            8, GTMaterials.PCBCoolant.getLocalizedName()),
                    Component.translatable("gtceu.machine.hpca.component_type.cooler_cooling", 2),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .register();
    public static final MachineDefinition HPCA_BRIDGE_COMPONENT = registerHPCAPart(
            "hpca_bridge_component", "HPCA Bridge Component",
            HPCABridgePartMachine::new, "bridge", false)
            .tooltips(Component.translatable("gtceu.machine.hpca.component_type.bridge"),
                    Component.translatable("gtceu.machine.hpca.component_general.max_eut", GTValues.VA[GTValues.IV]),
                    Component.translatable("gtceu.part_sharing.disabled"))
            .register();

    @NotNull
    private static MachineBuilder<MachineDefinition> registerDataHatch(String name, String displayName, int tier,
                                                                       Function<IMachineBlockEntity, MetaMachine> constructor,
                                                                       String model, PartAbility... abilities) {
        return REGISTRATE.machine(name, constructor)
                .langValue(displayName)
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(abilities)
                .overlayTieredHullModel(model);
    }

    private static MachineBuilder<MachineDefinition> registerHPCAPart(String name, String displayName,
                                                                      Function<IMachineBlockEntity, MetaMachine> constructor,
                                                                      String texture, boolean isAdvanced) {
        return REGISTRATE.machine(name, constructor)
                .langValue(displayName)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.HPCA_COMPONENT)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.IS_HPCA_PART_DAMAGED, false)
                .modelProperty(GTMachineModelProperties.IS_ACTIVE, false)
                .model(createHPCAPartModel(isAdvanced,
                        GTCEu.id("block/overlay/machine/hpca/" + texture),
                        GTCEu.id("block/overlay/machine/hpca/damaged" + (isAdvanced ? "_advanced" : ""))));
    }

    public static void init() {}
}
