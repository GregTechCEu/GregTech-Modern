package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public interface IMachineLife extends IMachineFeature {

    /**
     * Called when machine removed. {@link MetaMachineBlock#onRemove(BlockState, Level, BlockPos, BlockState, boolean)}
     * Only if block has changed will it be called. Ignore State changes.
     */
    default void onMachineRemoved() {}

    /**
     * Called when machine placed by (if exist) an entity with item.
     * it won't be called when machine added by {@link Level#setBlock(BlockPos, BlockState, int, int)}
     */
    default void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {}
}
