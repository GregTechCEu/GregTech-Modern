package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SurfaceSystem.class)
public interface SurfaceSystemAccessor {
    @Accessor
    BlockState getDefaultBlock();
}
