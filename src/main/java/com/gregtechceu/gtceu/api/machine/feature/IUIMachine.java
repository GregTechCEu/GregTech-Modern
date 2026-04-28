package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.gui.factory.GTMachineUI;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A machine that has a gui that can be opened via right click.
 */
public interface IUIMachine extends IUIHolder, IMachineFeature {

    @Override
    ModularUI createUI(Player entityPlayer);

    default boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    default InteractionResult tryToOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.shouldOpenUI(player, hand, hit)) {
            if (player instanceof ServerPlayer serverPlayer) {
                return GTMachineUI.open(self(), serverPlayer) ? InteractionResult.SUCCESS :
                        InteractionResult.TRY_WITH_EMPTY_HAND;
            }
        } else {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    default boolean isInvalid() {
        return self().isRemoved();
    }

    @Override
    default boolean isRemote() {
        return self().isRemote();
    }

    @Override
    default void markAsDirty() {};
}
