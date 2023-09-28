package com.gregtechceu.gtceu.api.registry.registrate;


import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/14
 * @implNote GTFluidBuilder
 */
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
