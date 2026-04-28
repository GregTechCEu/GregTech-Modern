package com.gregtechceu.gtceu.client.color;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BlockColor {

    int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
}
