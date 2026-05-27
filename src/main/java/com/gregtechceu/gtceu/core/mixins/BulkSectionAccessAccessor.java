package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.BulkSectionAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BulkSectionAccess.class)
public interface BulkSectionAccessAccessor {

    @Accessor
    LevelAccessor getLevel();
}
