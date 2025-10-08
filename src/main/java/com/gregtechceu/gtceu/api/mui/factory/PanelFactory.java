package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.base.IUIHolder;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.factory.MachineUIFactory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

@FunctionalInterface
public interface PanelFactory extends IUIHolder<PosGuiData> {

    @Override
    default ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var machine = MachineUIFactory.getMachine(data);
        return buildUIFunction(data, syncManager, settings, machine);
    };

    ModularPanel buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
                                 MetaMachine machine);

    default boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    default InteractionResult tryToOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.shouldOpenUI(player, hand, hit)) {
            if (player instanceof ServerPlayer serverPlayer) {
                MachineUIFactory.INSTANCE.open(serverPlayer, hit.getBlockPos());
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    default PanelFactory andThen(PanelEditor... edits) {
        return (data, syncManager, settings, machine) -> {
            var panel = this.buildUIFunction(data, syncManager, settings, machine);
            for (PanelEditor edit : edits) {
                edit.editUI(data, syncManager, settings, machine, panel);
            }
            return panel;
        };
    }
}
