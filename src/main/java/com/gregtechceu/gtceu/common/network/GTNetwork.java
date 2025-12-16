package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.*;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncHazardZoneStrength;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockFluid;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockOre;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectOre;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Function;

public class GTNetwork {

    private static final String PROTOCOL_VERSION = "1.0.0";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(GTCEu.id("network"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static void protectedSend(PacketDistributor.PacketTarget target, INetPacket packet) {
        try {
            INSTANCE.send(target, packet);
        } catch (Exception e) {
            GTCEu.LOGGER.warn("Failed to send packet: {}", e.getLocalizedMessage());
        }
    }

    private static int nextPacketId = 0;

    public static void sendToServer(INetPacket packet) {
        try {
            INSTANCE.sendToServer(packet);
        } catch (Exception e) {
            GTCEu.LOGGER.warn("Failed to send packet: {}", e.getLocalizedMessage());
        }
    }

    public static void sendToPlayersInLevel(ResourceKey<Level> level, INetPacket packet) {
        protectedSend(PacketDistributor.DIMENSION.with(() -> level), packet);
    }

    public static void sendToPlayersNearPoint(PacketDistributor.TargetPoint point, INetPacket packet) {
        protectedSend(PacketDistributor.NEAR.with(() -> point), packet);
    }

    public static void sendToAllPlayersTrackingEntity(Entity entity, boolean includeSelf, INetPacket packet) {
        protectedSend(includeSelf ? PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity) :
                PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }

    public static void sendToAllPlayersTrackingChunk(LevelChunk chunk, INetPacket packet) {
        protectedSend(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
    }

    public static void sendToAll(INetPacket packet) {
        protectedSend(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToPlayer(ServerPlayer player, INetPacket packet) {
        protectedSend(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void reply(NetworkEvent.Context context, INetPacket packet) {
        INSTANCE.reply(packet, context);
    }

    public interface INetPacket {

        void encode(FriendlyByteBuf buffer);

        void execute(NetworkEvent.Context context);
    }

    public static <T extends INetPacket> void register(Class<T> cls, Function<FriendlyByteBuf, T> decode,
                                                       NetworkDirection direction) {
        INSTANCE.registerMessage(nextPacketId++, cls, INetPacket::encode, decode, (msg, ctx) -> {
            ctx.get().enqueueWork(() -> msg.execute(ctx.get()));
            ctx.get().setPacketHandled(true);
        }, Optional.ofNullable(direction));
    }

    public static void init() {
        register(SCPacketMonitorGroupNBTChange.class, SCPacketMonitorGroupNBTChange::new, null);
        register(CPacketImageRequest.class, CPacketImageRequest::new, NetworkDirection.PLAY_TO_SERVER);
        register(SPacketImageResponse.class, SPacketImageResponse::new, NetworkDirection.PLAY_TO_CLIENT);

        register(CPacketKeysPressed.class, CPacketKeysPressed::new, NetworkDirection.PLAY_TO_SERVER);
        register(CPacketKeyDown.class, CPacketKeyDown::new, NetworkDirection.PLAY_TO_SERVER);

        register(SPacketSyncOreVeins.class, SPacketSyncOreVeins::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketSyncFluidVeins.class, SPacketSyncFluidVeins::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketSyncBedrockOreVeins.class, SPacketSyncBedrockOreVeins::new, NetworkDirection.PLAY_TO_CLIENT);

        register(SPacketAddHazardZone.class, SPacketAddHazardZone::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketRemoveHazardZone.class, SPacketRemoveHazardZone::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketSyncHazardZoneStrength.class, SPacketSyncHazardZoneStrength::new,
                NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketSyncLevelHazards.class, SPacketSyncLevelHazards::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketProspectOre.class, SPacketProspectOre::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketProspectBedrockOre.class, SPacketProspectBedrockOre::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketProspectBedrockFluid.class, SPacketProspectBedrockFluid::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketSendWorldID.class, SPacketSendWorldID::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SPacketNotifyCapeChange.class, SPacketNotifyCapeChange::new, NetworkDirection.PLAY_TO_CLIENT);
        register(SCPacketShareProspection.class, SCPacketShareProspection::new, null);
    }
}
