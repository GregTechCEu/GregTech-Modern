package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface IDataStickInteractable extends IInteractedMachine {

    @Override
    default InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                    BlockHitResult hit) {
        var item = player.getItemInHand(hand);
        if (item.is(GTItems.TOOL_DATA_STICK.asItem())) {
            return onDataStickRightClick(player, item);
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    default boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        var item = player.getItemInHand(hand);
        if (item.is(GTItems.TOOL_DATA_STICK.asItem())) {
            return onDataStickLeftClick(player, item);
        }
        return IInteractedMachine.super.onLeftClick(player, world, hand, pos, direction);
    }

    InteractionResult onDataStickRightClick(Player player, ItemStack dataStick);

    boolean onDataStickLeftClick(Player player, ItemStack dataStick);
}
