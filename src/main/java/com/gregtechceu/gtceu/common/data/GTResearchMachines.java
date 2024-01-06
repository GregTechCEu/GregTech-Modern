package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.client.renderer.machine.HPCAPartRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredActiveMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalComputationHatchMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAComputationPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCACoolerPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAEmptyPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@net.minecraft.MethodsReturnNonnullByDefault
@javax.annotation.ParametersAreNonnullByDefault
public class GTResearchMachines {

    public static final MultiblockMachineDefinition RESEARCH_STATION = REGISTRATE.multiblock("research_station", ResearchStationMachine::new)
        .rotationState(RotationState.NON_Y_AXIS)
        .recipeType(GTRecipeTypes.RESEARCH_STATION_RECIPES)
        .appearanceBlock(COMPUTER_CASING)
        .tooltips(LangHandler.getMultiLang("gtceu.machine.research_station.tooltip").toArray(Component[]::new))
        .pattern(defintion -> FactoryBlockPattern.start()
            .aisle("XXX", "VVV", "PPP", "PPP", "PPP", "VVV", "XXX")
            .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
            .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
            .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
            .aisle(" X ", "XAX", "---", "---", "---", "XAX", " X ")
            .aisle(" X ", "XAX", "-A-", "-H-", "-A-", "XAX", " X ")
            .aisle("   ", "XXX", "---", "---", "---", "XXX", "   ")
            .where('S', controller(blocks(defintion.getBlock())))
            .where('X', blocks(COMPUTER_CASING.get()))
            .where(' ', any())
            .where('-', air())
            .where('V', blocks(COMPUTER_HEAT_VENT.get()))
            .where('A', blocks(ADVANCED_COMPUTER_CASING.get()))
            .where('P', blocks(COMPUTER_CASING.get())
                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1))
                .or(abilities(PartAbility.MAINTENANCE)
                    .setMinGlobalLimited(ConfigHolder.INSTANCE.machines.enableMaintenance ? 1 : 0).setMaxGlobalLimited(1))
                .or(abilities(PartAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1)))
            .where('H', abilities(PartAbility.OBJECT_HOLDER))
            .build())
        .shapeInfo(definition -> MultiblockShapeInfo.builder()
            .aisle("XXX", "VVV", "POP", "PEP", "PMP", "VVV", "XXX")
            .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
            .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
            .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
            .aisle("-X-", "XAX", "---", "---", "---", "XAX", "-X-")
            .aisle("-X-", "XAX", "-A-", "-H-", "-A-", "XAX", "-X-")
            .aisle("---", "XXX", "---", "---", "---", "XXX", "---")
            .where('S', GTResearchMachines.RESEARCH_STATION, Direction.SOUTH)
            .where('X', COMPUTER_CASING.get())
            .where('-', Blocks.AIR)
            .where('V', COMPUTER_HEAT_VENT.get())
            .where('A', ADVANCED_COMPUTER_CASING.get())
            .where('P', COMPUTER_CASING.get())
            .where('O', GTResearchMachines.COMPUTATION_HATCH_RECEIVER, Direction.NORTH)
            .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], Direction.NORTH)
            .where('M', ConfigHolder.INSTANCE.machines.enableMaintenance ? GTMachines.MAINTENANCE_HATCH.getBlock().defaultBlockState().setValue(GTMachines.MAINTENANCE_HATCH.get().getRotationState().property, Direction.NORTH) : COMPUTER_CASING.getDefaultState())
            .where('H', GTResearchMachines.OBJECT_HOLDER, Direction.NORTH)
            .build())
        .sidedWorkableCasingRenderer("block/casings/hpca/advanced_computer_casing",
            GTCEu.id("block/multiblock/research_station"), false)
        .register();

    public static final MachineDefinition OBJECT_HOLDER = REGISTRATE.machine("object_holder", ObjectHolderMachine::new)
        .langValue("Object Holder")
        .tier(ZPM)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.OBJECT_HOLDER)
        .renderer(() -> new OverlayTieredActiveMachineRenderer(ZPM, GTCEu.id("block/machine/part/object_holder"), GTCEu.id("block/machine/part/object_holder_active")))
        .register();


    ////////////////////////////////////////////
    //********    MULTIBLOCK PARTS    ********//
    ////////////////////////////////////////////

    public static final MachineDefinition COMPUTATION_HATCH_TRANSMITTER = registerDataHatch(
        "computation_transmitter_hatch", "Computation Data Transmission Hatch",
        ZPM, (holder) -> new OpticalComputationHatchMachine(holder, true),
        "computation_data_hatch", PartAbility.COMPUTATION_DATA_TRANSMISSION
    );

    public static final MachineDefinition COMPUTATION_HATCH_RECEIVER = registerDataHatch(
        "computation_receiver_hatch", "Computation Data Reception Hatch",
        ZPM, (holder) -> new OpticalComputationHatchMachine(holder, false),
        "computation_data_hatch", PartAbility.COMPUTATION_DATA_RECEPTION
    );


    public static final MachineDefinition DATA_HATCH_TRANSMITTER = registerDataHatch(
        "data_transmitter_hatch", "Optical Data Transmission Hatch",
        LuV, (holder) -> new OpticalComputationHatchMachine(holder, true),
        "optical_data_hatch", PartAbility.OPTICAL_DATA_TRANSMISSION
    );

    public static final MachineDefinition DATA_HATCH_RECEIVER = registerDataHatch(
        "data_receiver_hatch", "Optical Data Reception Hatch",
        LuV, (holder) -> new OpticalComputationHatchMachine(holder, false),
        "optical_data_hatch", PartAbility.OPTICAL_DATA_RECEPTION
    );


    public static final MachineDefinition DATA_ACCESS_HATCH = REGISTRATE.machine("data_access_hatch", DataAccessHatchMachine::new)
        .langValue("Data Access Hatch")
        .tier(EV)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.DATA_ACCESS)
        .overlayTieredHullRenderer("data_access_hatch")
        .register();

    public static final MachineDefinition ADVANCED_DATA_ACCESS_HATCH = REGISTRATE.machine("advanced_data_access_hatch", DataAccessHatchMachine::new)
        .langValue("Advanced Data Access Hatch")
        .tier(LuV)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.DATA_ACCESS)
        .overlayTieredHullRenderer("data_access_hatch")
        .register();

    public static final MachineDefinition CREATIVE_DATA_ACCESS_HATCH = REGISTRATE.machine("creative_data_access_hatch", DataAccessHatchMachine::new)
        .langValue("Creative Data Access Hatch")
        .tier(MAX)
        .rotationState(RotationState.ALL)
        .abilities(PartAbility.DATA_ACCESS)
        .overlayTieredHullRenderer("data_access_hatch_creative")
        .register();


    //////////////////////////////////////
    //***********    HPCA    ***********//
    //////////////////////////////////////

    public static final MachineDefinition HPCA_EMPTY_COMPONENT = registerHPCAPart(
        "hpca_empty_component", "Empty HPCA Component",
        HPCAEmptyPartMachine::new, "empty", false
    );
    public static final MachineDefinition HPCA_COMPUTATION_COMPONENT = registerHPCAPart(
        "hpca_computation_component", "HPCA Computation Component",
        holder -> new HPCAComputationPartMachine(holder, false), "computation", false
    );
    public static final MachineDefinition HPCA_ADVANCED_COMPUTATION_COMPONENT = registerHPCAPart(
        "hpca_advanced_computation_component", "Advanced HPCA Computation Component",
        holder -> new HPCAComputationPartMachine(holder, true), "advanced_computation", true
    );
    public static final MachineDefinition HPCA_HEAT_SINK_COMPONENT = registerHPCAPart(
        "hpca_heat_sink_component", "HPCA Heat Sink Component",
        holder -> new HPCACoolerPartMachine(holder, false), "heat_sink", false
    );
    public static final MachineDefinition HPCA_ACTIVE_COOLER_COMPONENT = registerHPCAPart(
        "hpca_active_cooler_component", "HPCA Active Cooler Component",
        holder -> new HPCACoolerPartMachine(holder, true), "active_cooler", true
    );
    public static final MachineDefinition HPCA_BRIDGE_COMPONENT = registerHPCAPart(
        "hpca_bridge_component", "HPCA Bridge Component",
        HPCAEmptyPartMachine::new, "bridge", false
    );


    @NotNull
    private static MachineDefinition registerDataHatch(String name, String displayName, int tier, Function<IMachineBlockEntity, MetaMachine> constructor, String model, PartAbility... abilities) {
        return REGISTRATE.machine(name, constructor)
            .langValue(displayName)
            .tier(tier)
            .rotationState(RotationState.ALL)
            .abilities(abilities)
            .overlayTieredHullRenderer(model)
            .register();
    }

    private static MachineDefinition registerHPCAPart(String name, String displayName, Function<IMachineBlockEntity, MetaMachine> constructor, String texture, boolean isAdvanced) {
        return REGISTRATE.machine(name, constructor)
            .langValue(displayName)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.HPCA_COMPONENT)
            .renderer(() -> new HPCAPartRenderer(
                GTCEu.id("textures/block/overlay/machine/hpca/" + texture),
                GTCEu.id("textures/block/overlay/machine/hpca/" + (isAdvanced ? "damaged_advanced" : "damaged"))
            ))
            .register();
    }

    public static void init() {

    }
}
