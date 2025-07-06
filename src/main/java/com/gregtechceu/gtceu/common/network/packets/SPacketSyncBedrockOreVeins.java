package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SPacketSyncBedrockOreVeins implements GTNetwork.INetPacket {

    private final Map<ResourceLocation, BedrockOreDefinition> veins;

    @SuppressWarnings("unused")
    public SPacketSyncBedrockOreVeins() {
        this.veins = new HashMap<>();
    }

    public SPacketSyncBedrockOreVeins(FriendlyByteBuf buf) {
        this();
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readAnySizeNbt();
            BedrockOreDefinition def = BedrockOreDefinition.FULL_CODEC.parse(ops, tag).getOrThrow(false,
                    GTCEu.LOGGER::error);
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).forEach(entry -> veins.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) BedrockOreDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow(false, GTCEu.LOGGER::error);
            buf.writeNbt(tag);
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ClientProxy.CLIENT_BEDROCK_ORE_VEINS.clear();
        ClientProxy.CLIENT_BEDROCK_ORE_VEINS.putAll(veins);
    }
}
