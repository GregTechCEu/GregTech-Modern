package com.gregtechceu.gtceu.api.registry.registrate;

import com.gregtechceu.gtceu.api.fluids.FluidState;

import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IGTFluidBuilder {

    IGTFluidBuilder temperature(int temperature);

    IGTFluidBuilder state(FluidState state);

    IGTFluidBuilder density(int density);

    IGTFluidBuilder luminance(int luminance);

    IGTFluidBuilder viscosity(int viscosity);

    IGTFluidBuilder burnTime(int burnTime);

    IGTFluidBuilder hasBlock(boolean hasBlock);

    IGTFluidBuilder hasBucket(boolean hasBucket);

    IGTFluidBuilder color(int color);

    IGTFluidBuilder onFluidRegister(Consumer<Fluid> fluidConsumer);

    Supplier<? extends Fluid> registerFluid();

    RegistryEntry<? extends Fluid> register();
}
