package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class CPacketKeyDown implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("key_down");
    public static final Type<CPacketKeyDown> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketKeyDown> CODEC = StreamCodec
            .ofMember(CPacketKeyDown::encode, CPacketKeyDown::new);

    private final Int2BooleanMap updateKeys;

    public CPacketKeyDown(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    public CPacketKeyDown(RegistryFriendlyByteBuf buf) {
        this.updateKeys = new Int2BooleanOpenHashMap();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            updateKeys.put(buf.readInt(), buf.readBoolean());
        }
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(updateKeys.size());
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            buf.writeInt(entry.getIntKey());
            buf.writeBoolean(entry.getBooleanValue());
        }
    }

    public void execute(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            for (var entry : updateKeys.int2BooleanEntrySet()) {
                SyncedKeyMapping keyMapping = SyncedKeyMapping.getFromSyncId(entry.getIntKey());
                keyMapping.serverActivate(entry.getBooleanValue(), player);
            }
        }
    }

    @Override
    public @NotNull Type<CPacketKeyDown> type() {
        return TYPE;
    }
}
