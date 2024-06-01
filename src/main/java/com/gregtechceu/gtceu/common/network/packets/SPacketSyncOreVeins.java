package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.gregtechceu.gtceu.api.worldgen.GTOreDefinition;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.networking.IHandlerContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import lombok.RequiredArgsConstructor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SPacketSyncOreVeins implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SPacketSyncOreVeins> TYPE = new CustomPacketPayload.Type<>(GTCEu.id("sync_ore_veins"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketSyncOreVeins> CODEC =
            StreamCodec.ofMember(SPacketSyncOreVeins::encode, SPacketSyncOreVeins::decode);

    private final Map<ResourceLocation, GTOreDefinition> veins;

    public SPacketSyncOreVeins() {
        this.veins = new HashMap<>();
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        int size = veins.size();
        buf.writeVarInt(size);
        for (var entry : veins.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CompoundTag tag = (CompoundTag) GTOreDefinition.FULL_CODEC.encodeStart(ops, entry.getValue())
                    .getOrThrow();
            buf.writeNbt(tag);
        }
    }

    public static SPacketSyncOreVeins decode(RegistryFriendlyByteBuf buf) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        var veins = Stream.generate(() -> {
            ResourceLocation id = buf.readResourceLocation();
            CompoundTag tag = buf.readNbt();
            GTOreDefinition def = GTOreDefinition.FULL_CODEC.parse(ops, tag).getOrThrow();
            return Map.entry(id, def);
        }).limit(buf.readVarInt()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new SPacketSyncOreVeins(veins);
    }

    public static void execute(SPacketSyncOreVeins packet, IPayloadContext handler) {
        if (GTRegistries.ORE_VEINS.isFrozen()) {
            GTRegistries.ORE_VEINS.unfreeze();
        }
        GTRegistries.ORE_VEINS.registry().clear();
        for (var entry : packet.veins.entrySet()) {
            GTRegistries.ORE_VEINS.registerOrOverride(entry.getKey(), entry.getValue());
        }
        if (!GTRegistries.ORE_VEINS.isFrozen()) {
            GTRegistries.ORE_VEINS.freeze();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
