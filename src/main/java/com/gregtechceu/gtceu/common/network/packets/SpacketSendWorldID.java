package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.capability.WorldIDSaveData;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@NoArgsConstructor
public class SpacketSendWorldID implements IPacket {

    private String worldId;

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(WorldIDSaveData.getWorldID());
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.worldId = buf.readUtf();
    }

    @Override
    public void execute(IHandlerContext handler) {
        ClientCacheManager.init(worldId);
    }
}
