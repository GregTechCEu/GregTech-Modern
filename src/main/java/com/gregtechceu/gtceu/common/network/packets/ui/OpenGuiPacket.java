package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.api.mui.base.UIFactory;
import com.gregtechceu.gtceu.api.mui.factory.GuiManager;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class OpenGuiPacket implements GTNetwork.INetPacket {

    private int windowId;
    private UIFactory<?> factory;
    private FriendlyByteBuf data;

    public OpenGuiPacket(FriendlyByteBuf buf) {
        this.windowId = buf.readVarInt();
        this.factory = GuiManager.getFactory(buf.readResourceLocation());
        this.data = NetworkUtils.readFriendlyByteBuf(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.windowId);
        buf.writeResourceLocation(this.factory.getFactoryName());
        NetworkUtils.writeByteBuf(buf, this.data);
    }

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            GuiManager.open(this.windowId, this.factory, this.data, Minecraft.getInstance().player);
        }
    }
}
