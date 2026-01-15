package com.gregtechceu.gtceu.integration.create;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.integration.create.display.ComputerMonitorCoverDisplayTarget;

import net.minecraft.core.registries.Registries;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

public class GTCreateDisplayTargets {

    public static final RegistryEntry<DisplayTarget, ComputerMonitorCoverDisplayTarget> COMPUTER_MONITOR_COVER = registerToAllMachines(
            "computer_monitor_cover", ComputerMonitorCoverDisplayTarget::new);

    @SuppressWarnings("SameParameterValue")
    private static <
            T extends DisplayTarget> RegistryEntry<DisplayTarget, T> registerToAllMachines(String name,
                                                                                           Supplier<T> supplier) {
        SimpleBuilder<DisplayTarget, T, GTRegistrate> builder = GTCreateIntegration
                .displayTarget(GTRegistration.REGISTRATE, name, supplier);
        builder.onRegisterAfter(Registries.BLOCK_ENTITY_TYPE,
                target -> GTRegistries.MACHINES.forEach(
                        (def) -> DisplayTarget.BY_BLOCK_ENTITY.register(def.getBlockEntityType(), target)));
        return builder.register();
    }

    public static void init() {}
}
