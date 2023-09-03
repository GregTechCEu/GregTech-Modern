package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.machine.multiblock.part.forge.GasHatchPartMachine;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.VNF;
import static com.gregtechceu.gtceu.common.data.GTMachines.ALL_TIERS;
import static com.gregtechceu.gtceu.common.data.GTMachines.registerTieredMachines;

public class GTMekanismMachines {

    public final static MachineDefinition[] GAS_IMPORT_HATCH = registerTieredMachines("gas_input_hatch",
            (holder, tier) -> new GasHatchPartMachine(holder, tier, IO.IN),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Gas Input Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.IMPORT_GASES)
                    .overlayTieredHullRenderer("gas_hatch.import")
                    .tooltips(Component.translatable("gtceu.machine.gas_hatch.import.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.gas_storage_capacity", (8 * FluidHelper.getBucket()) * (1L << Math.min(9, tier))))
                    .register(),
            ALL_TIERS);

    public final static MachineDefinition[] GAS_EXPORT_HATCH = registerTieredMachines("gas_output_hatch",
            (holder, tier) -> new GasHatchPartMachine(holder, tier, IO.OUT),
            (tier, builder) -> builder
                    .langValue(VNF[tier] + " Gas Output Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.EXPORT_GASES)
                    .overlayTieredHullRenderer("gas_hatch.export")
                    .tooltips(Component.translatable("gtceu.machine.gas_hatch.export.tooltip"),
                            Component.translatable("gtceu.universal.tooltip.gas_storage_capacity", (8 * FluidHelper.getBucket()) * (1L << Math.min(9, tier))))
                    .register(),
            ALL_TIERS);

    public static void init() {

    }
}
