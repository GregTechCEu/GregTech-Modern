package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LiquidBlock.class)
public interface LiquidBlockAccessor {
    @Invoker
    public FluidState invokeGetFluidState(BlockState pState);
}
