package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SPacketSyncFluidVeins implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SPacketSyncFluidVeins> TYPE = new CustomPacketPayload.Type<>(
            GTCEu.id("sync_bedrock_fluid_veins"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSyncFluidVeins> CODEC = StreamCodec
            .ofMember(SPacketSyncFluidVeins::encode, SPacketSyncFluidVeins::decode);

    private final Map<ResourceLocation, BedrockFluidDefinition> veins;

    public SPacketSyncFluidVeins() {
        this.veins = new HashMap<>();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) BedrockFluidDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow();
            buf.writeNbt(tag);
        }
    }

    public static SPacketSyncFluidVeins decode(RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        var veins = Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readNbt();
            BedrockFluidDefinition def = BedrockFluidDefinition.FULL_CODEC.parse(ops, tag).getOrThrow();
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SPacketSyncFluidVeins(veins);
    }

    public static void execute(SPacketSyncFluidVeins packet, IPayloadContext handler) {
        ClientProxy.CLIENT_FLUID_VEINS.clear();
        ClientProxy.CLIENT_FLUID_VEINS.putAll(packet.veins);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
