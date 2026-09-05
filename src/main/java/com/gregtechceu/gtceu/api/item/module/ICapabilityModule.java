package com.gregtechceu.gtceu.api.item.module;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;

public interface ICapabilityModule {

    <T> @NotNull LazyOptional<T> getCapability(AppliedItemModule module, @NotNull Capability<T> cap);
}
