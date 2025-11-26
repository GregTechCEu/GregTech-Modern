package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.client.mui.screen.ModularContainerMenu;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SContainerSetData implements GTNetwork.INetPacket {

    private int inWorldId;
    private int id;
    private int value;

    public SContainerSetData(FriendlyByteBuf buf) {
        inWorldId = buf.readVarInt();
        id = buf.readVarInt();
        value = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(inWorldId);
        buffer.writeVarInt(id);
        buffer.writeVarInt(value);
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ModularContainerMenu menu = GuiManager.getClientInWorldMenu(inWorldId);
        if (menu != null) {
            menu.setData(id, value);
        }
    }
}
