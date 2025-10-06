package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.factory.MachineUIFactory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public interface IMuiMachine extends IUIHolder<PosGuiData>, IMachineFeature {

    @Override
    ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings);

    default boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    default InteractionResult tryToOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.shouldOpenUI(player, hand, hit)) {
            if (player instanceof ServerPlayer serverPlayer) {
                MachineUIFactory.INSTANCE.open(serverPlayer, this);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }
}
