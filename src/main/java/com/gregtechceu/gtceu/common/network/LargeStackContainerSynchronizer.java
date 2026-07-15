package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.common.network.packets.SPacketLargeStackContainer;
import com.gregtechceu.gtceu.common.network.packets.SPacketLargeStackSlot;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;

/** Container synchronizer using GTM packets that retain full integer stack counts. */
public class LargeStackContainerSynchronizer implements ContainerSynchronizer {

    private final ServerPlayer player;

    public LargeStackContainerSynchronizer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> stacks, ItemStack carried,
                                int[] dataSlots) {
        GTNetwork.sendToPlayer(player,
                new SPacketLargeStackContainer(menu.containerId, menu.getStateId(), stacks, carried));
        for (int id = 0; id < dataSlots.length; id++) {
            sendDataChange(menu, id, dataSlots[id]);
        }
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu menu, int slot, ItemStack stack) {
        GTNetwork.sendToPlayer(player, new SPacketLargeStackSlot(menu.containerId, menu.getStateId(), slot, stack));
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu menu, ItemStack stack) {
        GTNetwork.sendToPlayer(player, new SPacketLargeStackSlot(menu.containerId, menu.getStateId(), -1, stack));
    }

    @Override
    public void sendDataChange(AbstractContainerMenu menu, int id, int value) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket(
                menu.containerId, id, value));
    }
}
