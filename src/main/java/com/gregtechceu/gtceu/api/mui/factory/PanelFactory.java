package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

@FunctionalInterface
public interface PanelFactory extends IUIHolder<PosGuiData> {

    @Override
    default ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var machine = MachineUIFactory.getMachine(data);
        return buildUIFunction(data, syncManager, settings, machine);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    default ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }

    ModularPanel<?> buildUIFunction(PosGuiData data, PanelSyncManager syncManager, UISettings settings,
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
