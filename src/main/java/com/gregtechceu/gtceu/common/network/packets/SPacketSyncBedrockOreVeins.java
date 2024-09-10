package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.client.ClientProxy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
public class SPacketSyncBedrockOreVeins implements CustomPacketPayload {

    public static final Type<SPacketSyncBedrockOreVeins> TYPE = new Type<>(GTCEu.id("sync_bedrock_ore_veins"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSyncBedrockOreVeins> CODEC = StreamCodec
            .ofMember(SPacketSyncBedrockOreVeins::encode, SPacketSyncBedrockOreVeins::decode);

    private final Map<ResourceLocation, BedrockOreDefinition> veins;

    public SPacketSyncBedrockOreVeins() {
        this.veins = new HashMap<>();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) BedrockOreDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow();
            buf.writeNbt(tag);
        }
    }

    public static SPacketSyncBedrockOreVeins decode(RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        var veins = Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readNbt();
            BedrockOreDefinition def = BedrockOreDefinition.FULL_CODEC.parse(ops, tag).getOrThrow();
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SPacketSyncBedrockOreVeins(veins);
    }

    public static void execute(SPacketSyncBedrockOreVeins packet, IPayloadContext handler) {
        ClientProxy.CLIENT_BEDROCK_ORE_VEINS.clear();
        ClientProxy.CLIENT_BEDROCK_ORE_VEINS.putAll(packet.veins);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
