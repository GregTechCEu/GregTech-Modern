package com.gregtechceu.gtceu.core.mixins.ldlib;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.network.LargeStackContainerSynchronizer;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ModularUIContainer.class)
public abstract class ModularUIContainerMixin extends AbstractContainerMenu {

    @Shadow(remap = false)
    @Final
    private ModularUI modularUI;

    protected ModularUIContainerMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Redirect(method = "mergeItemStack",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"),
              remap = false)
    private static int gtceu$getLargeStackLimit(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Redirect(method = "quickMoveStack",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
    private int gtceu$limitQuickMoveCount(ItemStack stack) {
        return Math.min(stack.getCount(), stack.getMaxStackSize());
    }

    @Override
    public void setSynchronizer(@NotNull ContainerSynchronizer synchronizer) {
        Player player = modularUI.entityPlayer;
        if (player instanceof ServerPlayer serverPlayer && modularUI.holder instanceof MetaMachine) {
            super.setSynchronizer(new LargeStackContainerSynchronizer(serverPlayer));
            return;
        }
        super.setSynchronizer(synchronizer);
    }
}
