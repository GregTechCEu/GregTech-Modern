package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.machine.multiblock.fission.*;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_REACTOR;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_REACTOR_VESSEL;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTFissionMachines {

    // Capstone parts (top layer, passive)
    public static final MachineDefinition FISSION_HEAT_EXCHANGER = REGISTRATE
            .machine("fission_heat_exchanger", FissionCapstonePartMachine::new)
            .langValue("Fission Heat Exchanger")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_HEAT_EXCHANGER)
            .colorOverlayTieredHullModel("overlay_fission_heat_exchanger", null,
                    "overlay_fission_heat_exchanger_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_heat_exchanger.tooltip"))
            .register();

    public static final MachineDefinition FISSION_NEUTRON_REFLECTOR = REGISTRATE
            .machine("fission_neutron_reflector", FissionCapstonePartMachine::new)
            .langValue("Fission Neutron Reflector")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_NEUTRON_REFLECTOR)
            .colorOverlayTieredHullModel("overlay_fission_neutron_reflector", null,
                    "overlay_fission_neutron_reflector_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_neutron_reflector.tooltip"))
            .register();

    public static final MachineDefinition FISSION_MODERATOR = REGISTRATE
            .machine("fission_moderator", FissionCapstonePartMachine::new)
            .langValue("Fission Moderator Rod")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_MODERATOR)
            .colorOverlayTieredHullModel("overlay_fission_moderator", null,
                    "overlay_fission_moderator_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_moderator.tooltip"))
            .register();

    public static final MachineDefinition FISSION_CONTROL_ROD = REGISTRATE
            .machine("fission_control_rod", FissionCapstonePartMachine::new)
            .langValue("Fission Control Rod")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_CONTROL_ROD)
            .colorOverlayTieredHullModel("overlay_fission_control_rod", null,
                    "overlay_fission_control_rod_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_control_rod.tooltip"))
            .register();

    // Active Capstone parts (top layer)
    public static final MachineDefinition FISSION_FUEL_ROD_PORT = REGISTRATE
            .machine("fission_fuel_rod_port", FuelRodPortPartMachine::new)
            .langValue("Fission Fuel Rod Port")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_FUEL_PORT)
            .colorOverlayTieredHullModel("overlay_fission_fuel_rod_port", null,
                    "overlay_fission_fuel_rod_port_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_fuel_rod_port.tooltip"))
            .register();

    public static final MachineDefinition FISSION_COOLANT_OUTLET = REGISTRATE
            .machine("fission_coolant_outlet", CoolantOutletPartMachine::new)
            .langValue("Fission Coolant Outlet")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_COOLANT_OUTLET)
            .colorOverlayTieredHullModel("overlay_fission_coolant_outlet", null,
                    "overlay_fission_coolant_outlet_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_coolant_outlet.tooltip"))
            .register();

    // Active Capstone parts (bottom layer)
    public static final MachineDefinition FISSION_FUEL_ROD_DRAIN = REGISTRATE
            .machine("fission_fuel_rod_drain", FuelRodDrainPartMachine::new)
            .langValue("Fission Fuel Rod Drain")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_FUEL_DRAIN)
            .colorOverlayTieredHullModel("overlay_fission_fuel_rod_drain", null,
                    "overlay_fission_fuel_rod_drain_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_fuel_rod_drain.tooltip"))
            .register();

    public static final MachineDefinition FISSION_COOLANT_INLET = REGISTRATE
            .machine("fission_coolant_inlet", CoolantInletPartMachine::new)
            .langValue("Fission Coolant Inlet")
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.FISSION_COOLANT_INLET)
            .colorOverlayTieredHullModel("overlay_fission_coolant_inlet", null,
                    "overlay_fission_coolant_inlet_emissive")
            .tooltips(Component.translatable("gtceu.machine.fission_coolant_inlet.tooltip"))
            .register();

    // Spotless:off
    public static final MultiblockMachineDefinition SMALL_FISSION_REACTOR = REGISTRATE
            .multiblock("fission_reactor",
                    info -> new FissionReactorMachine(info, 10000))
            .langValue("Small Fission Reactor")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(CASING_REACTOR)
            .pattern(definition -> FactoryBlockPattern.start(
                    RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.DOWN)
                    .aisle("  VVV  ", " VTTTV ", "VTTTTTV", "VTTSTTV", "VTTTTTV", " VTTTV ", "  VVV  ")
                    .aisle("  VVV  ", " VFFFV ", "VFFFFFV", "VFFFFFV", "VFFFFFV", " VFFFV ", "  VVV  ").setRepeatable(1, 5)
                    .aisle("  VVV  ", " VBBBV ", "VBBBBBV", "VBBBBBV", "VBBBBBV", " VBBBV ", "  VVV  ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('V', blocks(CASING_REACTOR_VESSEL.get()))
                    .where('F', blocks(CASING_REACTOR.get()))
                    .where('T', blocks(CASING_REACTOR.get())
                            .or(abilities(PartAbility.FISSION_FUEL_PORT))
                            .or(abilities(PartAbility.FISSION_COOLANT_OUTLET))
                            .or(abilities(PartAbility.FISSION_HEAT_EXCHANGER))
                            .or(abilities(PartAbility.FISSION_NEUTRON_REFLECTOR))
                            .or(abilities(PartAbility.FISSION_MODERATOR))
                            .or(abilities(PartAbility.FISSION_CONTROL_ROD))
                            .or(air()))
                    .where('B', blocks(CASING_REACTOR.get())
                            .or(abilities(PartAbility.FISSION_FUEL_DRAIN))
                            .or(abilities(PartAbility.FISSION_COOLANT_INLET)))
                    .where(' ', any())
                    .build())

            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                        .where('S', definition, Direction.NORTH)
                        .where('V', CASING_REACTOR_VESSEL.get())
                        .where('R', CASING_REACTOR.get())
                        .where('P', FISSION_FUEL_ROD_PORT, Direction.UP)
                        .where('O', FISSION_COOLANT_OUTLET, Direction.UP)
                        .where('H', FISSION_HEAT_EXCHANGER, Direction.UP)
                        .where('N', FISSION_NEUTRON_REFLECTOR, Direction.UP)
                        .where('M', FISSION_MODERATOR, Direction.UP)
                        .where('C', FISSION_CONTROL_ROD, Direction.UP)
                        .where('D', FISSION_FUEL_ROD_DRAIN, Direction.DOWN)
                        .where('I', FISSION_COOLANT_INLET, Direction.DOWN);
                String[] bRow = { "  VVV  ", " VRRRV ", "VRDRRRV", "VRRIRRV", "VRDRRRV", " VRRRV ", "  VVV  " };
                String[] fRow = { "  VVV  ", " VRRRV ", "VRRRRRV", "VRRRRRV", "VRRRRRV", " VRRRV ", "  VVV  " };
                String[] tRow = { "  VVV  ", " VHOHV ", "VCPMPCV", "VOPSROV", "VNPMPCV", " VMOHV ", "  VVV  " };

                for (int repeats = 1; repeats <= 5; repeats++) {
                    List<String[]> yLayers = new ArrayList<>();
                    yLayers.add(bRow);
                    for (int i = 0; i < repeats; i++) {
                        yLayers.add(fRow);
                    }
                    yLayers.add(tRow);
                    var copy = builder.shallowCopy();
                    for (int z = 0; z < 7; z++) {
                        String[] yStrings = new String[yLayers.size()];
                        for (int y = 0; y < yLayers.size(); y++) {
                            yStrings[y] = yLayers.get(y)[z];
                        }
                        copy.aisle(yStrings);
                    }
                    shapeInfos.add(copy.build());
                }
                return shapeInfos;
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_reactor"),
                    GTCEu.id("block/multiblock/fission_reactor"))
            .appearanceBlock(CASING_REACTOR_VESSEL)
            .register();
    // Spotless:on
    public static void init() {}
}
