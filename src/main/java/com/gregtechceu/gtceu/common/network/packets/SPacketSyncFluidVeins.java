package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.worldgen.bedrockfluid.BedrockFluidDefinition;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncFluidVeins> CODEC = StreamCodec
            .ofMember(SPacketSyncFluidVeins::encode, SPacketSyncFluidVeins::decode);

    private final Map<ResourceLocation, BedrockFluidDefinition> veins;

    public SPacketSyncFluidVeins() {
        this.veins = new HashMap<>();
    }

    public void encode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) BedrockFluidDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow();
            buf.writeNbt(tag);
        }
    }

    public static SPacketSyncFluidVeins decode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry());
        var veins = Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readNbt();
            BedrockFluidDefinition def = BedrockFluidDefinition.FULL_CODEC.parse(ops, tag).getOrThrow();
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SPacketSyncFluidVeins(veins);
    }

    public static void execute(SPacketSyncFluidVeins packet, IPayloadContext handler) {
        if (GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.unfreeze();
        }
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.registry().clear();
        for (var entry : packet.veins.entrySet()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.registerOrOverride(entry.getKey(), entry.getValue());
        }
        if (!GTRegistries.BEDROCK_FLUID_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_FLUID_DEFINITIONS.freeze();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
