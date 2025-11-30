package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.packets.ui.SContainerSetContent;
import com.gregtechceu.gtceu.common.network.packets.ui.SContainerSetData;
import com.gregtechceu.gtceu.common.network.packets.ui.SContainerSetSlot;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class InWorldContainerSynchronizer implements ContainerSynchronizer {

    private final ServerPlayer player;

    public InWorldContainerSynchronizer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void sendInitialData(@NotNull AbstractContainerMenu container, @NotNull NonNullList<ItemStack> items,
                                @NotNull ItemStack carriedItem, int @NotNull [] initialData) {
        GTNetwork.sendToPlayer(player, new SContainerSetContent(getInWorldId(container),
                container.incrementStateId(), items, carriedItem));

        for (int i = 0; i < initialData.length; ++i) {
            this.broadcastDataValue(container, i, initialData[i]);
        }
    }

    @Override
    public void sendSlotChange(@NotNull AbstractContainerMenu container, int slot, @NotNull ItemStack itemStack) {
        GTNetwork.sendToPlayer(player,
                new SContainerSetSlot(getInWorldId(container), container.incrementStateId(), slot, itemStack));
    }

    @Override
    public void sendCarriedChange(@NotNull AbstractContainerMenu containerMenu, @NotNull ItemStack stack) {
        GTNetwork.sendToPlayer(player,
                new SContainerSetSlot(getInWorldId(containerMenu), containerMenu.incrementStateId(), -1, stack));
    }

    @Override
    public void sendDataChange(@NotNull AbstractContainerMenu container, int id, int value) {
        this.broadcastDataValue(container, id, value);
    }

    private void broadcastDataValue(AbstractContainerMenu container, int id, int value) {
        GTNetwork.sendToPlayer(player, new SContainerSetData(getInWorldId(container), id, value));
    }

    private int getInWorldId(AbstractContainerMenu container) {
        if (container instanceof ModularContainerMenu modularContainer) return modularContainer.inWorldID;
        return -1;
    }
}
