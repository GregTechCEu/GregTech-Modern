package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import lombok.RequiredArgsConstructor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SPacketSyncBedrockOreVeins implements CustomPacketPayload {
    public static final Type<SPacketSyncBedrockOreVeins> TYPE = new Type<>(GTCEu.id("sync_bedrock_ore_veins"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncBedrockOreVeins> CODEC =
            StreamCodec.ofMember(SPacketSyncBedrockOreVeins::encode, SPacketSyncBedrockOreVeins::decode);

    private final Map<ResourceLocation, BedrockOreDefinition> veins;

    public SPacketSyncBedrockOreVeins() {
        this.veins = new HashMap<>();
    }

    public void encode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) BedrockOreDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow();
            buf.writeNbt(tag);
        }
    }

    public static SPacketSyncBedrockOreVeins decode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry());
        var veins = Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readNbt();
            BedrockOreDefinition def = BedrockOreDefinition.FULL_CODEC.parse(ops, tag).getOrThrow();
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SPacketSyncBedrockOreVeins(veins);
    }

    public static void execute(SPacketSyncBedrockOreVeins packet, IPayloadContext handler) {
        if (GTRegistries.BEDROCK_ORE_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.unfreeze();
        }
        GTRegistries.BEDROCK_ORE_DEFINITIONS.registry().clear();
        for (var entry : packet.veins.entrySet()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.registerOrOverride(entry.getKey(), entry.getValue());
        }
        if (!GTRegistries.BEDROCK_ORE_DEFINITIONS.isFrozen()) {
            GTRegistries.BEDROCK_ORE_DEFINITIONS.freeze();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
