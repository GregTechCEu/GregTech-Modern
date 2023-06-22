package com.gregtechceu.gtceu.api.capability.fabric.compat;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class CompatApiProvider<A, B, C> implements BlockApiLookup.BlockApiProvider<A, C> {

    private final BlockApiLookup.BlockApiProvider<B, C> upvalue;

    public CompatApiProvider(BlockApiLookup.BlockApiProvider<B, C> upvalue) {
        this.upvalue = upvalue;
    }

    protected B findUpvalue(Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return upvalue.find(world, pos, state, blockEntity, context);
    }
}
