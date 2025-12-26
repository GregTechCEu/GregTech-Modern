package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryUIFactory;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public interface IToolUIBehavior extends IToolBehavior, IUIHolder<PlayerInventoryGuiData<?>> {

    @Override
    default @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level level, @NotNull Player player,
                                                                         @NotNull InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (level.isClientSide && shouldOpenUI(player, hand)) {
            PlayerInventoryUIFactory.INSTANCE.openFromHandClient(hand);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    boolean shouldOpenUI(@NotNull Player player, @NotNull InteractionHand hand);
}
