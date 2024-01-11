package com.gregtechceu.gtceu.integration.ae2;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.integration.ae2.machine.*;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.UHV;
import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

@SuppressWarnings("unused")
public class GTAEMachines {

    public final static MachineDefinition ITEM_IMPORT_BUS = REGISTRATE.machine("me_input_bus", MEInputBusPartMachine::new)
            .langValue("ME Stocking Input Bus")
            .tier(UHV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"))
            .compassNode("item_bus")
            .register();

    public final static MachineDefinition ITEM_EXPORT_BUS = REGISTRATE.machine("me_output_bus", MEOutputBusPartMachine::new)
            .langValue("ME Output Bus")
            .tier(UHV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.export")
            .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                    Component.translatable("gtceu.machine.me.item_export.tooltip"),
                    Component.translatable("gtceu.machine.me.export.tooltip"),
                    Component.translatable("gtceu.universal.enabled"))
            .compassNode("item_bus")
            .register();

    public final static MachineDefinition FLUID_IMPORT_HATCH = REGISTRATE.machine("me_input_hatch", MEInputHatchPartMachine::new)
            .langValue("ME Stocking Input Hatch")
            .tier(UHV)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tooltips(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip"))
            .compassNode("fluid_hatch")
            .register();

    public final static MachineDefinition FLUID_EXPORT_HATCH = REGISTRATE.machine("me_output_hatch", MEOutputHatchPartMachine::new)
            .langValue("ME Output Hatch")
            .tier(UHV)
            .rotationState(RotationState.ALL)
                    .abilities(PartAbility.EXPORT_FLUIDS)
                    .overlayTieredHullRenderer("me_fluid_hatch.export")
                    .tooltips(Component.translatable("gtceu.machine.fluid_hatch.export.tooltip"))
            .compassNode("fluid_hatch")
            .register();

    public static void init() {

    }
}
