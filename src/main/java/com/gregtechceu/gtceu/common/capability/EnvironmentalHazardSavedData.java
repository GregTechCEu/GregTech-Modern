package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.material.material.properties.HazardProperty;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;
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

    public static final int MIN_STRENGTH_FOR_SPREAD = 1000;

    private final ServerLevel serverLevel;

    /**
     * Map of source position to a triple of (trigger, material).
     */
    @Getter
    private final Map<ChunkPos, HazardZone> hazardZones = new HashMap<>();

    public static EnvironmentalHazardSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(() -> new EnvironmentalHazardSavedData(serverLevel),
                        (tag, provider) -> new EnvironmentalHazardSavedData(serverLevel, tag)),
                        "gtceu_environmental_hazard_tracker");
    }

    public EnvironmentalHazardSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public EnvironmentalHazardSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
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
        for (final var entry : hazardZones.entrySet()) {
            HazardZone zone = entry.getValue();
            if (zone.strength() >= MIN_STRENGTH_FOR_SPREAD / 5) {
                ChunkPos chunkPos = entry.getKey();
                BlockPos source = entry.getKey().getMiddleBlockPosition(zone.source().getY());
                for (BlockPos pos : BlockPos.betweenClosed(
                        chunkPos.getMinBlockX(), source.getY() - 8, chunkPos.getMinBlockZ(),
                        chunkPos.getMaxBlockX(), source.getY() + 8, chunkPos.getMaxBlockZ())) {
                    if (GTValues.RNG.nextInt(32000 / zone.strength()) == 0) {
                        serverLevel.sendParticles(
                                new DustParticleOptions(Vec3.fromRGB24(zone.condition.color).toVector3f(), 1),
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                1, 0, 0.1, 0, 0.1);
                    }
                }
            }

            Stream<ServerPlayer> playersInZone = serverLevel.players()
                    .stream()
                    .filter(player -> new ChunkPos(BlockPos.containing(player.getEyePosition()))
                            .equals(entry.getKey()));
            tickPlayerHazards(zone, playersInZone);

            if (zone.canSpread() && zone.strength() > MIN_STRENGTH_FOR_SPREAD) {
                zonesToSpread.add(entry.getKey());
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
            int removedStrength = 0;
            for (ChunkPos relativePos : relativePositions) {
                hazardZones.compute(relativePos, (k, v) -> {
                    if (v != null && v.condition() == zone.condition() && v.trigger() == zone.trigger()) {
                        return new HazardZone(v.source(), 20 + v.strength(), true,
                                v.trigger(), v.condition());
                    } else {
                        return new HazardZone(k.getMiddleBlockPosition(zone.source().getY()), 20, true,
                                zone.trigger(), zone.condition());
                    }
                });
                removedStrength += 20;
            }
            hazardZones.replace(pos, new HazardZone(zone.source(),
                    zone.strength - removedStrength, false, zone.trigger(), zone.condition()));
            this.setDirty();
        }
    }

    public void tickPlayerHazards(final HazardZone zone, Stream<ServerPlayer> playerStream) {
        playerStream.forEach(player -> {
            if (zone.trigger().protectionType().isProtected(player)) {
                // entity has proper safety equipment, so damage it per material every 5 seconds.
                if (player.level().getGameTime() % 100 == 0) {
                    for (ArmorItem.Type type : zone.trigger().protectionType().getEquipmentTypes()) {
                        player.getItemBySlot(type.getSlot()).hurtAndBreak(1, player, type.getSlot());
                    }
                }
                // don't progress this material condition if entity is protected
                return;
            }

            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            tracker.progressCondition(zone.condition(), zone.strength() / 1000f);
        });
    }

    /**
     * finds the hazard zone that's in the chunk the block pos is in and has the correct condition.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZoneByContainedPos(BlockPos containedPos) {
        return hazardZones.get(new ChunkPos(containedPos));
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
            this.hazardZones.remove(chunkPos);
        }
    }

    public void removeZone(ChunkPos chunkPos) {
        this.hazardZones.remove(chunkPos);
    }

    public void addZone(ChunkPos source, HazardZone zone) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }
        if (this.hazardZones.containsKey(source) && this.hazardZones.get(source).condition == zone.condition) {
            // noinspection DataFlowIssue
            this.hazardZones.compute(source, (k, oldZone) -> new HazardZone(oldZone.source(),
                    oldZone.strength() + zone.strength(), zone.canSpread(), zone.trigger(), zone.condition()));
        } else if (!this.hazardZones.containsKey(source)) {
            this.hazardZones.put(source, zone);
        }
        this.setDirty();
    }

    public void addZone(BlockPos source, int strength, boolean canSpread,
                        HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        addZone(new ChunkPos(source), new HazardZone(source, strength, canSpread, trigger, condition));
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.Provider provider) {
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

    public record HazardZone(BlockPos source, int strength, boolean canSpread,
                             HazardProperty.HazardTrigger trigger, MedicalCondition condition) {

        public CompoundTag serializeNBT(CompoundTag zoneTag) {
            zoneTag.put("source", NbtUtils.writeBlockPos(source));
            zoneTag.putInt("strength", strength);
            zoneTag.putBoolean("can_spread", canSpread);
            zoneTag.putString("trigger", trigger.name());
            zoneTag.putString("condition", condition.name);

            return zoneTag;
        }

        public static HazardZone deserializeNBT(CompoundTag zoneTag) {
            BlockPos source = NbtUtils.readBlockPos(zoneTag, "source").orElse(null);
            int strength = zoneTag.getInt("strength");
            boolean canSpread = zoneTag.getBoolean("can_spread");
            HazardProperty.HazardTrigger trigger = HazardProperty.HazardTrigger.ALL_TRIGGERS
                    .get(zoneTag.getString("trigger"));
            MedicalCondition condition = MedicalCondition.CONDITIONS.get(zoneTag.getString("condition"));

            return new HazardZone(source, strength, canSpread, trigger, condition);
        }
    }
}
