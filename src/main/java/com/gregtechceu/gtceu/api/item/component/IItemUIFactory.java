package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IItemUIFactory extends IInteractionItem {

    Object createUI(GTHeldItemUIHolder holder, Player entityPlayer);

    @Override
    default InteractionResult use(ItemStack item, Level level, Player player,
                                  InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            GTHeldItemUIOpener.openUI(serverPlayer, usedHand);
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(item);
    }
}
