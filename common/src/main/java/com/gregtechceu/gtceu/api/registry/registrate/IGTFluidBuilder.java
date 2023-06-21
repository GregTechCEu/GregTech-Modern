package com.gregtechceu.gtceu.api.registry.registrate;


import com.tterrag.registrate.util.entry.FluidEntry;
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
    IGTFluidBuilder density(int density);
    IGTFluidBuilder luminance(int luminance);
    IGTFluidBuilder viscosity(int viscosity);
    IGTFluidBuilder hasBlock(boolean hasBlock);
    IGTFluidBuilder color(int color);
    IGTFluidBuilder onFluidRegister(Consumer<Fluid> fluidConsumer);
    Supplier<? extends Fluid> registerFluid();
    FluidEntry<? extends Fluid> register();
}
