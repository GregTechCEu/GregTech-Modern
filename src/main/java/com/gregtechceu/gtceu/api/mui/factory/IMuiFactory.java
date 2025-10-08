package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.factory.BuilderMachineUIFactory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

@FunctionalInterface
public interface IMuiFactory extends IUIHolder<PosGuiData> {

    @Override
    ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings);

    default boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    default InteractionResult tryToOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.shouldOpenUI(player, hand, hit)) {
            if (player instanceof ServerPlayer serverPlayer) {
                BuilderMachineUIFactory.INSTANCE.open(serverPlayer, hit.getBlockPos(), this);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
}
