package com.gregtechceu.gtceu.integration.create;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.integration.create.display.ComputerMonitorCoverDisplaySource;

import net.minecraft.core.registries.Registries;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.registrate.SimpleBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GTCreateDisplaySources {

    public static final RegistryEntry<ComputerMonitorCoverDisplaySource> COMPUTER_MONITOR_COVER = registerToAllMachines(
            "computer_monitor_cover", ComputerMonitorCoverDisplaySource::new);

    @SuppressWarnings("SameParameterValue")
    private static <T extends DisplaySource> RegistryEntry<T> registerToAllMachines(String name, Supplier<T> supplier) {
        SimpleBuilder<DisplaySource, T, GTRegistrate> builder = GTCreateIntegration
                .displaySource(GTRegistration.REGISTRATE, name, supplier);
        builder.onRegisterAfter(
                Registries.BLOCK_ENTITY_TYPE,
                source -> GTRegistries.MACHINES.entries().forEach(
                        (entry) -> DisplaySource.BY_BLOCK_ENTITY.add(
                                entry.getValue().getBlockEntityType(),
                                source)));
        return builder.register();
    }

    public static void init() {}
}
