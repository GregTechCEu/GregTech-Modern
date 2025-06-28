package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketNotifyCapeChange implements IPacket {

    public UUID uuid;
    public ResourceLocation cape;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeBoolean(this.cape != null);
        if (this.cape != null) {
            buf.writeResourceLocation(this.cape);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.cape = buf.readBoolean() ? buf.readResourceLocation() : null;
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            CapeRegistry.giveRawCape(uuid, cape);
        }
    }
}
