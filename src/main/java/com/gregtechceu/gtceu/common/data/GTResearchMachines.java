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
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredActiveMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ObjectHolderMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalComputationHatchMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.gregtechceu.gtceu.api.GTValues.ZPM;
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


    public static final MachineDefinition COMPUTATION_HATCH_TRANSMITTER = registerDataHatch(
        "computation_transmitter_hatch", "Computation Transmitter Hatch",
        ZPM, (holder) -> new OpticalComputationHatchMachine(holder, false),
        "optical_data_access_hatch", PartAbility.COMPUTATION_DATA_TRANSMISSION
    );

    public static final MachineDefinition COMPUTATION_HATCH_RECEIVER = registerDataHatch(
        "computation_receiver_hatch", "Computation Receiver Hatch",
        ZPM, (holder) -> new OpticalComputationHatchMachine(holder, false),
        "optical_data_access_hatch", PartAbility.COMPUTATION_DATA_RECEPTION
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


    public static void init() {

    }
}
