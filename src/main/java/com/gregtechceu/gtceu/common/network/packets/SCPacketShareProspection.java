package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class SCPacketShareProspection implements IPacket {

    private UUID sender;
    private UUID receiver;
    private String cacheName;
    private String key;
    private boolean isDimCache;
    private ResourceKey<Level> dimension;
    private CompoundTag data;
    private boolean first;

    public SCPacketShareProspection() {}

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(sender);
        buf.writeUUID(receiver);
        buf.writeUtf(cacheName);
        buf.writeUtf(key);
        buf.writeBoolean(isDimCache);
        buf.writeResourceKey(dimension);
        buf.writeNbt(data);
        buf.writeBoolean(first);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        sender = buf.readUUID();
        receiver = buf.readUUID();
        cacheName = buf.readUtf();
        key = buf.readUtf();
        isDimCache = buf.readBoolean();
        dimension = buf.readResourceKey(Registries.DIMENSION);
        data = buf.readNbt();
        first = buf.readBoolean();
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (handler.isClient()) {
            if (first) {
                PlayerInfo senderInfo = Minecraft.getInstance().getConnection().getPlayerInfo(sender);
                if (senderInfo == null) {
                    return;
                }

                Component playerName = senderInfo.getTabListDisplayName() != null ? senderInfo.getTabListDisplayName() :
                        Component.literal(senderInfo.getProfile().getName());

                Minecraft.getInstance().player.sendSystemMessage(Component
                        .translatable("command.gtceu.share_prospection_data.notification", playerName));
            }
            ClientCacheManager.processProspectionShare(cacheName, key, isDimCache, dimension, data);
        } else {
            SCPacketShareProspection newPacket = new SCPacketShareProspection(sender, receiver,
                    cacheName, key,
                    isDimCache, dimension,
                    data, first);
            GTNetwork.NETWORK.sendToPlayer(newPacket,
                    GTCEu.getMinecraftServer().getPlayerList().getPlayer(receiver));
        }
    }
}
