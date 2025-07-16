package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Level.class)
public interface LevelAccessor {

    @Accessor("thread")
    Thread gtceu$getThread();
}
