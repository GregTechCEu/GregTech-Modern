package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.networking.IHandlerContext;
import com.lowdragmc.lowdraglib.networking.IPacket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SPacketSyncOreVeins implements IPacket {

    private final Map<ResourceLocation, GTOreDefinition> veins;

    public SPacketSyncOreVeins() {
        this.veins = new HashMap<>();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) GTOreDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow(false, GTCEu.LOGGER::error);
            buf.writeNbt(tag);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readAnySizeNbt();
            GTOreDefinition def = GTOreDefinition.FULL_CODEC.parse(ops, tag).getOrThrow(false, GTCEu.LOGGER::error);
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).forEach(entry -> veins.put(entry.getKey(), entry.getValue()));
    }

    @Override
    public void execute(IHandlerContext handler) {
        if (GTRegistries.ORE_VEINS.isFrozen()) {
            GTRegistries.ORE_VEINS.unfreeze();
        }
        GTRegistries.ORE_VEINS.registry().clear();
        for (var entry : veins.entrySet()) {
            if (!GTRegistries.ORE_VEINS.containKey(entry.getKey())) {
                GTRegistries.ORE_VEINS.register(entry.getKey(), entry.getValue());
            }
        }
        if (!GTRegistries.ORE_VEINS.isFrozen()) {
            GTRegistries.ORE_VEINS.freeze();
        }
    }
}
