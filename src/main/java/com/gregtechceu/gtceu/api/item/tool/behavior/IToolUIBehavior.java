package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.PlayerInventoryUIFactory;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import org.jetbrains.annotations.NotNull;

public interface IToolUIBehavior<T extends IToolUIBehavior<T>>
                                extends IToolBehavior<T>, IUIHolder<PlayerInventoryGuiData<?>> {

    @Override
    default @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level level, @NotNull Player player,
                                                                         @NotNull InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (level.isClientSide && shouldOpenUI(player, hand)) {
            PlayerInventoryUIFactory.INSTANCE.openFromHandClient(hand);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    default ModularScreen createScreen(PlayerInventoryGuiData<?> data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }

    boolean shouldOpenUI(@NotNull Player player, @NotNull InteractionHand hand);
}
