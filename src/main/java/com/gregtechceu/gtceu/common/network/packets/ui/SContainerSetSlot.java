package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SContainerSetSlot implements GTNetwork.INetPacket {

    private int inWorldId;
    private int stateId;
    private int slot;
    private ItemStack stack;

    public SContainerSetSlot(FriendlyByteBuf buf) {
        inWorldId = buf.readVarInt();
        stateId = buf.readVarInt();
        slot = buf.readVarInt();
        stack = buf.readItem();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(inWorldId);
        buffer.writeVarInt(stateId);
        buffer.writeVarInt(slot);
        buffer.writeItem(stack);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ModularContainerMenu menu = GuiManager.getClientInWorldMenu(inWorldId);
        if (menu != null) {
            if (slot == -1) {
                menu.setCarried(stack);
            } else menu.setItem(slot, stateId, stack);
        }
    }
}
