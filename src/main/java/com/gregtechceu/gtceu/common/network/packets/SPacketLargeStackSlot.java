package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/** Sends one menu slot with an integer item count. */
public class SPacketLargeStackSlot implements GTNetwork.INetPacket {

    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack stack;

    public SPacketLargeStackSlot(int containerId, int stateId, int slot, ItemStack stack) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.stack = stack;
    }

    public SPacketLargeStackSlot(FriendlyByteBuf buffer) {
        containerId = buffer.readVarInt();
        stateId = buffer.readVarInt();
        slot = buffer.readVarInt();
        stack = buffer.readItem();
        stack.setCount(buffer.readVarInt());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(stateId);
        buffer.writeVarInt(slot);
        buffer.writeItem(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        buffer.writeVarInt(stack.getCount());
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof ModularUIContainer &&
                player.containerMenu.containerId == containerId) {
            if (slot < 0) {
                player.containerMenu.setCarried(stack);
            } else {
                player.containerMenu.setItem(slot, stateId, stack);
            }
        }
    }
}
