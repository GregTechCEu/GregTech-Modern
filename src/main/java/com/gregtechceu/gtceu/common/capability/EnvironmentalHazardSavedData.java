package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncHazardZoneStrength;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Full-chunk environmental hazards. e.g. pollution.
 */
public class EnvironmentalHazardSavedData extends SavedData {

    public static final float MIN_STRENGTH_FOR_SPREAD = 1000;

    private final ServerLevel serverLevel;

    /**
     * Map of source position to a triple of (trigger, material).
     */
    @Getter
    private final Map<ChunkPos, HazardZone> hazardZones = new HashMap<>();

    public static EnvironmentalHazardSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new EnvironmentalHazardSavedData(serverLevel, tag),
                () -> new EnvironmentalHazardSavedData(serverLevel), "gtceu_environmental_hazard_tracker");
    }

    public EnvironmentalHazardSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public EnvironmentalHazardSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        ListTag allHazardZones = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < allHazardZones.size(); ++i) {
            CompoundTag zoneTag = allHazardZones.getCompound(i);

            ChunkPos source = new ChunkPos(zoneTag.getLong("pos"));
            HazardZone zone = HazardZone.deserializeNBT(zoneTag);

            this.hazardZones.put(source, zone);
        }
    }

    public void tick() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        Set<ChunkPos> zonesToSpread = new HashSet<>();
        Set<ChunkPos> zonesToRemove = new HashSet<>();
        for (final var entry : hazardZones.entrySet()) {
            HazardZone zone = entry.getValue();
            Stream<ServerPlayer> playersInZone = serverLevel.players()
                    .stream()
                    .filter(player -> new ChunkPos(BlockPos.containing(player.getEyePosition()))
                            .equals(entry.getKey()));
            tickPlayerHazards(zone, playersInZone);

            zone = zone.removeStrength(ConfigHolder.INSTANCE.gameplay.environmentalHazardDecayRate);
            if (zone == null) {
                zonesToRemove.add(entry.getKey());
            } else if (zone.canSpread() && zone.strength() > MIN_STRENGTH_FOR_SPREAD) {
                zonesToSpread.add(entry.getKey());
            }
        }

        // remove empty zones
        for (ChunkPos pos : zonesToRemove) {
            hazardZones.remove(pos);
            if (this.serverLevel.hasChunk(pos.x, pos.z)) {
                LevelChunk chunk = this.serverLevel.getChunk(pos.x, pos.z);
                GTNetwork.sendToAllPlayersTrackingChunk(chunk, new SPacketRemoveHazardZone(pos));
            }
        }

        for (ChunkPos pos : zonesToSpread) {
            final HazardZone zone = hazardZones.get(pos);
            ChunkPos[] relativePositions = new ChunkPos[] {
                    new ChunkPos(pos.x, pos.z - 1),
                    new ChunkPos(pos.x, pos.z + 1),
                    new ChunkPos(pos.x - 1, pos.z),
                    new ChunkPos(pos.x + 1, pos.z)
            };
            float removedStrength = 0;
            for (ChunkPos relativePos : relativePositions) {
                hazardZones.compute(relativePos, (k, v) -> {
                    HazardZone newZone;
                    if (v != null && v.condition() == zone.condition() && v.trigger() == zone.trigger()) {
                        newZone = v.addStrength(20);
                        sendSyncZonePacket(k, newZone);
                    } else {
                        newZone = new HazardZone(k.getMiddleBlockPosition(zone.source().getY()), 20, true,
                                zone.trigger(), zone.condition());
                        sendAddZonePacket(k, newZone);
                    }
                    return newZone;
                });
                removedStrength += 20;
            }
            HazardZone newZone = zone.removeStrength(removedStrength);
            if (newZone == null) {
                hazardZones.remove(pos);
                if (this.serverLevel.hasChunk(pos.x, pos.z)) {
                    LevelChunk chunk = this.serverLevel.getChunk(pos.x, pos.z);
                    GTNetwork.sendToAllPlayersTrackingChunk(chunk, new SPacketRemoveHazardZone(pos));
                }
            }
            this.setDirty();
        }
    }

    public void tickPlayerHazards(final HazardZone zone, Stream<ServerPlayer> playerStream) {
        playerStream.forEach(player -> {
            if (zone.trigger().protectionType().isProtected(player)) {
                // entity has proper safety equipment, so damage it per material every 5 seconds.
                zone.trigger().protectionType().damageEquipment(player, 1);
                // don't progress this material condition if entity is protected
                return;
            }

            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            if (tracker == null) {
                return;
            }
            tracker.progressCondition(zone.condition(), zone.strength() / 1000f);
        });
    }

    /**
     * finds the hazard zone that's in the chunk pos.
     *
     * @param pos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZoneByPos(ChunkPos pos) {
        return hazardZones.get(pos);
    }

    /**
     * finds the hazard zone that's in the chunk the block pos is in.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZoneByContainedPos(BlockPos containedPos) {
        return getZoneByPos(new ChunkPos(containedPos));
    }

    /**
     * finds all hazard zones that contain the given position and have the correct condition.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZoneByContainedPosAndCondition(BlockPos containedPos, MedicalCondition condition) {
        HazardZone zone = hazardZones.get(new ChunkPos(containedPos));
        return zone != null && zone.condition == condition ? zone : null;
    }

    public void removeZone(BlockPos inChunkPos) {
        this.removeZone(new ChunkPos(inChunkPos));
    }

    public void removeZone(BlockPos inChunkPos, MedicalCondition condition) {
        ChunkPos chunkPos = new ChunkPos(inChunkPos);
        if (this.hazardZones.get(chunkPos).condition() == condition) {
            removeZone(chunkPos);
        }
    }

    public void removeZone(ChunkPos chunkPos) {
        this.hazardZones.remove(chunkPos);
        if (this.serverLevel.hasChunk(chunkPos.x, chunkPos.z)) {
            LevelChunk chunk = this.serverLevel.getChunk(chunkPos.x, chunkPos.z);
            GTNetwork.sendToAllPlayersTrackingChunk(chunk, new SPacketRemoveHazardZone(chunkPos));
        }
    }

    public void addZone(ChunkPos source, HazardZone zone) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        HazardZone existing = this.hazardZones.get(source);
        if (existing != null && existing.condition == zone.condition) {
            existing.addStrength(zone.strength());
            sendSyncZonePacket(source, existing);
        } else if (!this.hazardZones.containsKey(source)) {
            this.hazardZones.put(source, zone);
            sendAddZonePacket(source, zone);
        }
        this.setDirty();
    }

    public void addZone(BlockPos source, float strength, boolean canSpread,
                        HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        addZone(new ChunkPos(source), new HazardZone(source, strength, canSpread, trigger, condition));
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag hazardZonesTag = new ListTag();
        for (var entry : hazardZones.entrySet()) {
            CompoundTag zoneTag = new CompoundTag();

            zoneTag.putLong("pos", entry.getKey().toLong());
            entry.getValue().serializeNBT(zoneTag);

            hazardZonesTag.add(zoneTag);
        }
        compoundTag.put("zones", hazardZonesTag);
        return compoundTag;
    }

    @Accessors(fluent = true)
    @AllArgsConstructor
    public static class HazardZone {

        @Getter
        private final BlockPos source;
        @Getter
        @Setter
        private float strength;
        @Getter
        private final boolean canSpread;
        @Getter
        private final HazardProperty.HazardTrigger trigger;
        @Getter
        private final MedicalCondition condition;

        public HazardZone addStrength(float toAdd) {
            this.strength += toAdd;
            return this;
        }

        public HazardZone removeStrength(float toRemove) {
            this.strength -= toRemove;
            if (this.strength <= 0) {
                return null;
            }
            return this;
        }

        public CompoundTag serializeNBT(CompoundTag zoneTag) {
            zoneTag.put("source", NbtUtils.writeBlockPos(source));
            zoneTag.putFloat("strength", strength);
            zoneTag.putBoolean("can_spread", canSpread);
            zoneTag.putString("trigger", trigger.name());
            zoneTag.putString("condition", condition.name);

            return zoneTag;
        }

        public static HazardZone deserializeNBT(CompoundTag zoneTag) {
            BlockPos source = NbtUtils.readBlockPos(zoneTag.getCompound("source"));
            float strength = zoneTag.getFloat("strength");
            boolean canSpread = zoneTag.getBoolean("can_spread");
            HazardProperty.HazardTrigger trigger = HazardProperty.HazardTrigger.ALL_TRIGGERS
                    .get(zoneTag.getString("trigger"));
            MedicalCondition condition = com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition.CONDITIONS
                    .get(zoneTag.getString("condition"));

            return new HazardZone(source, strength, canSpread, trigger, condition);
        }

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeBlockPos(source);
            buf.writeFloat(strength);
            buf.writeBoolean(canSpread);
            buf.writeUtf(trigger.name());
            buf.writeUtf(condition.name);
        }

        public static HazardZone fromNetwork(FriendlyByteBuf buf) {
            BlockPos source = buf.readBlockPos();
            float strength = buf.readFloat();
            boolean canSpread = buf.readBoolean();
            HazardProperty.HazardTrigger trigger = HazardProperty.HazardTrigger.ALL_TRIGGERS.get(buf.readUtf());
            MedicalCondition condition = MedicalCondition.CONDITIONS.get(buf.readUtf());
            return new HazardZone(source, strength, canSpread, trigger, condition);
        }
    }

    public void sendAddZonePacket(ChunkPos pos, HazardZone zone) {
        if (this.serverLevel.hasChunk(pos.x, pos.z)) {
            LevelChunk chunk = this.serverLevel.getChunk(pos.x, pos.z);
            GTNetwork.sendToAllPlayersTrackingChunk(chunk, new SPacketAddHazardZone(pos, zone));
        }
    }

    public void sendSyncZonePacket(ChunkPos pos, HazardZone zone) {
        if (this.serverLevel.hasChunk(pos.x, pos.z)) {
            LevelChunk chunk = this.serverLevel.getChunk(pos.x, pos.z);
            GTNetwork.sendToAllPlayersTrackingChunk(chunk, new SPacketSyncHazardZoneStrength(pos, zone.strength()));
        }
    }
}
