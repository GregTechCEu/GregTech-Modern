package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public interface IToolUIBehavior<T extends IToolUIBehavior<T>> extends IToolBehavior<T> {

    @Override
    default @NotNull InteractionResult onItemRightClick(@NotNull Level level, @NotNull Player player,
                                                        @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && openUI(serverPlayer, hand)) {
            GTHeldItemUIOpener.openUI(serverPlayer, hand);
            return InteractionResult.SUCCESS.heldItemTransformedTo(heldItem);
        }
        return InteractionResult.PASS;
    }

    boolean openUI(@NotNull Player player, @NotNull InteractionHand hand);

    Object createUI(Player player, GTHeldItemUIHolder holder);
}
