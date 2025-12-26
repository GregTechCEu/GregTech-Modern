package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class SContainerSetContent implements GTNetwork.INetPacket {

    private int inWorldId;
    private int stateId;
    private List<ItemStack> items;
    private ItemStack carriedItem;

    public SContainerSetContent(FriendlyByteBuf buf) {
        this.inWorldId = buf.readVarInt();
        this.stateId = buf.readVarInt();
        this.items = buf.readList(FriendlyByteBuf::readItem);
        this.carriedItem = buf.readItem();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.inWorldId);
        buffer.writeVarInt(this.stateId);
        buffer.writeCollection(this.items, FriendlyByteBuf::writeItem);
        buffer.writeItem(carriedItem);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ModularContainerMenu menu = GuiManager.getClientInWorldMenu(this.inWorldId);
        if (menu != null) {
            menu.initializeContents(this.stateId, this.items, this.carriedItem);
        }
    }
}
