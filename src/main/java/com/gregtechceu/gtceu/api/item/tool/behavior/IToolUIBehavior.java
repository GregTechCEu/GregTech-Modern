package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public interface IToolUIBehavior extends IToolBehavior {

    @Override
    default @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level level, @NotNull Player player,
                                                                         @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && openUI(serverPlayer, hand)) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, hand);
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    boolean openUI(@NotNull Player player, @NotNull InteractionHand hand);

    ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder holder);
}
