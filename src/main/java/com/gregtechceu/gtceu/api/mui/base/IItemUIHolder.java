package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryGuiData;
import com.gregtechceu.gtceu.api.mui.factory.PlayerInventoryUIFactory;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public interface IItemUIHolder extends IUIHolder<PlayerInventoryGuiData<?>>, IInteractionItem {

    default boolean shouldOpenUI() {
        return true;
    }

    @Override
    default InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (!shouldOpenUI())
            return IInteractionItem.super.use(item, level, player, usedHand);
        if (level.isClientSide)
            PlayerInventoryUIFactory.INSTANCE.openFromHandClient(usedHand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    @Override
    default InteractionResult useOn(UseOnContext context) {
        if (!shouldOpenUI())
            return IInteractionItem.super.useOn(context);
        if (context.getLevel().isClientSide)
            PlayerInventoryUIFactory.INSTANCE.openFromHandClient(context.getHand());
        return InteractionResult.SUCCESS;
    }
}
