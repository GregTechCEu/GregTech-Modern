package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FluidStack.class, remap = false)
public interface FluidStackAccessor {

    @Accessor("fluid")
    @Nullable
    Holder<Fluid> getRawFluid();
}
