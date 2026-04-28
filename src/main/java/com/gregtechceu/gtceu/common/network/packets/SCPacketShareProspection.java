package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
public class SCPacketShareProspection implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("share_prospection");
    public static final Type<SCPacketShareProspection> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SCPacketShareProspection> CODEC = StreamCodec
            .ofMember(SCPacketShareProspection::encode, SCPacketShareProspection::new);

    private UUID sender;
    private UUID receiver;
    private String cacheName;
    private String key;
    private boolean isDimCache;
    private ResourceKey<Level> dimension;
    private CompoundTag data;
    private boolean first;

    public SCPacketShareProspection(FriendlyByteBuf buf) {
        sender = buf.readUUID();
        receiver = buf.readUUID();
        cacheName = buf.readUtf();
        key = buf.readUtf();
        isDimCache = buf.readBoolean();
        dimension = buf.readResourceKey(Registries.DIMENSION);
        data = buf.readNbt();
        first = buf.readBoolean();
    }

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

    public void execute(IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            if (first) {
                PlayerInfo senderInfo = Objects.requireNonNull(Minecraft.getInstance().getConnection())
                        .getPlayerInfo(sender);
                if (senderInfo == null) {
                    return;
                }

                Component playerName = senderInfo.getTabListDisplayName() != null ? senderInfo.getTabListDisplayName() :
                        Component.literal(senderInfo.getProfile().name());

                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.sendSystemMessage(Component
                        .translatable("command.gtceu.share_prospection_data.notification", playerName));
            }
            ClientCacheManager.processProspectionShare(cacheName, key, isDimCache, dimension, data,
                    context.player().registryAccess());
        } else {
            ServerPlayer receiverPlayer = GTCEu.getMinecraftServer().getPlayerList().getPlayer(receiver);
            if (receiverPlayer == null) {
                return;
            }
            SCPacketShareProspection newPacket = new SCPacketShareProspection(sender, receiver,
                    cacheName, key,
                    isDimCache, dimension,
                    data, first);
            PacketDistributor.sendToPlayer(receiverPlayer, newPacket);
        }
    }

    @Override
    public @NotNull Type<SCPacketShareProspection> type() {
        return TYPE;
    }
}
