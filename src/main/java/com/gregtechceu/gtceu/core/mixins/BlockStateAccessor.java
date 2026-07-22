package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.BlockStateBase.class)
public interface BlockStateAccessor {

    @Accessor("mapColor")
    MapColor gtceu$getDefaultMapColor();
}
