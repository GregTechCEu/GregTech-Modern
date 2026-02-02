package com.gregtechceu.gtceu.common.network.packets.ui;

import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.ModularNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CloseAllGuiPacket implements GTNetwork.INetPacket {

    public CloseAllGuiPacket(FriendlyByteBuf buffer) {}

    @Override
    public void encode(FriendlyByteBuf buffer) {}

    @Override
    public void execute(NetworkEvent.Context handler) {
        if (handler.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ModularNetwork.CLIENT.closeAll(MCHelper.getPlayer(), false);
        } else {
            ModularNetwork.SERVER.closeAll(handler.getSender(), false);
        }
    }
}
