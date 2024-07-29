package com.gregtechceu.gtceu.common.data.machines;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class GTAEMachines {

    public final static MachineDefinition ITEM_IMPORT_BUS_ME = REGISTRATE
            .machine("me_input_bus", MEInputBusPartMachine::new)
            .langValue("ME Input Bus")
            .tier(EV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"))
            .compassNode("item_bus")
            .register();

    public final static MachineDefinition STOCKING_IMPORT_BUS_ME = REGISTRATE
            .machine("me_stocking_input_bus", MEStockingBusPartMachine::new)
            .langValue("ME Stocking Input Bus")
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"))
            .compassNode("item_bus")
            .register();

    public final static MachineDefinition ITEM_EXPORT_BUS_ME = REGISTRATE
            .machine("me_output_bus", MEOutputBusPartMachine::new)
            .langValue("ME Output Bus")
            .tier(EV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.export")
            .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                    Component.translatable("gtceu.machine.me.item_export.tooltip"),
                    Component.translatable("gtceu.machine.me.export.tooltip"),
                    Component.translatable("gtceu.universal.enabled"))
            .compassNode("item_bus")
            .register();

    public final static MachineDefinition FLUID_IMPORT_HATCH_ME = REGISTRATE
            .machine("me_input_hatch", MEInputHatchPartMachine::new)
            .langValue("ME Input Hatch")
            .tier(EV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tooltips(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip"))
            .compassNode("fluid_hatch")
            .register();

    public final static MachineDefinition STOCKING_IMPORT_HATCH_ME = REGISTRATE
            .machine("me_stocking_input_hatch", MEStockingHatchPartMachine::new)
            .langValue("ME Stocking Input Hatch")
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tooltips(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip"))
            .compassNode("fluid_hatch")
            .register();

    public final static MachineDefinition FLUID_EXPORT_HATCH_ME = REGISTRATE
            .machine("me_output_hatch", MEOutputHatchPartMachine::new)
            .langValue("ME Output Hatch")
            .tier(EV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.export")
            .tooltips(Component.translatable("gtceu.machine.fluid_hatch.export.tooltip"))
            .compassNode("fluid_hatch")
            .register();

    public static void init() {}
}
