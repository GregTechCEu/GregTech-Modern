package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

public class CPacketKeyDown implements GTNetwork.INetPacket {

    private final Int2BooleanMap updateKeys;

    public CPacketKeyDown(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    public CPacketKeyDown(FriendlyByteBuf buf) {
        this.updateKeys = new Int2BooleanOpenHashMap();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            updateKeys.put(buf.readInt(), buf.readBoolean());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(updateKeys.size());
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            buf.writeInt(entry.getIntKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getSender() != null) {
            for (var entry : updateKeys.int2BooleanEntrySet()) {
                SyncedKeyMapping keyMapping = SyncedKeyMapping.getFromSyncId(entry.getIntKey());
                keyMapping.serverActivate(entry.getBooleanValue(), context.getSender());
            }
        }
    }
}
