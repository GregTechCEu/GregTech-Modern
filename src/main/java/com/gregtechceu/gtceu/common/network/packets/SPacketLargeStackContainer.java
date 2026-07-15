package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

/** Synchronizes a modular menu without truncating ItemStack counts to a byte. */
public class SPacketLargeStackContainer implements GTNetwork.INetPacket {

    private final int containerId;
    private final int stateId;
    private final List<ItemStack> stacks;
    private final ItemStack carried;

    public SPacketLargeStackContainer(int containerId, int stateId, List<ItemStack> stacks, ItemStack carried) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.stacks = stacks;
        this.carried = carried;
    }

    public SPacketLargeStackContainer(FriendlyByteBuf buffer) {
        containerId = buffer.readVarInt();
        stateId = buffer.readVarInt();
        int size = buffer.readVarInt();
        stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(readStack(buffer));
        }
        carried = readStack(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(stateId);
        buffer.writeVarInt(stacks.size());
        stacks.forEach(stack -> writeStack(buffer, stack));
        writeStack(buffer, carried);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof ModularUIContainer &&
                player.containerMenu.containerId == containerId) {
            player.containerMenu.initializeContents(stateId, stacks, carried);
        }
    }

    private static void writeStack(FriendlyByteBuf buffer, ItemStack stack) {
        buffer.writeItem(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        buffer.writeVarInt(stack.getCount());
    }

    private static ItemStack readStack(FriendlyByteBuf buffer) {
        ItemStack stack = buffer.readItem();
        stack.setCount(buffer.readVarInt());
        return stack;
    }
}
