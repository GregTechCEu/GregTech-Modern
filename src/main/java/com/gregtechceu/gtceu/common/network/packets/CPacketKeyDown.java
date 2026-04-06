package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class CPacketKeyDown implements CustomPacketPayload {

    public static final ResourceLocation ID = GTCEu.id("key_down");
    public static final Type<CPacketKeyDown> TYPE = new Type<>(ID);
    public static final StreamCodec<ByteBuf, CPacketKeyDown> CODEC = ByteBufCodecs
            .map(size -> (Int2BooleanMap) new Int2BooleanOpenHashMap(size), ByteBufCodecs.VAR_INT, ByteBufCodecs.BOOL)
            .map(CPacketKeyDown::new, packet -> packet.updateKeys);

    private final Int2BooleanMap updateKeys;

    public CPacketKeyDown(Int2BooleanMap updateKeys) {
        this.updateKeys = updateKeys;
    }

    public void execute(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            SyncedKeyMapping keyMapping = SyncedKeyMapping.getFromSyncId(entry.getIntKey());
            keyMapping.serverActivate(entry.getBooleanValue(), player);
        }
    }

    @Override
    public @NotNull Type<CPacketKeyDown> type() {
        return TYPE;
    }
}
