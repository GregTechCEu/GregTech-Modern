package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.gregtechceu.gtceu.utils.FloodFiller3D;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnvironmentalHazardSavedData extends SavedData {

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

        // tick full-chunk zones
        for (final var entry : hazardZones.entrySet()) {
            HazardZone zone = entry.getValue();
            Stream<ServerPlayer> playersInZone = serverLevel.players()
                    .stream()
                    .filter(player -> new ChunkPos(BlockPos.containing(player.getEyePosition())).equals(entry.getKey()));
            tickPlayerHazards(zone, playersInZone);
        }
    }

    public void tickPlayerHazards(final HazardZone zone, Stream<ServerPlayer> playerStream) {
        playerStream.forEach(player -> {
            if (zone.trigger().protectionType().isProtected(player)) {
                // entity has proper safety equipment, so damage it per material every 5 seconds.
                if (player.level().getGameTime() % 100 == 0) {
                    for (ArmorItem.Type type : zone.trigger().protectionType().getEquipmentTypes()) {
                        player.getItemBySlot(type.getSlot()).hurtAndBreak(1, player,
                                p -> p.broadcastBreakEvent(type.getSlot()));
                    }
                }
                // don't progress this material condition if entity is protected
                return;
            }

            IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
            if (tracker == null) {
                return;
            }
            tracker.progressCondition(zone.condition(), zone.blocks().size() / 10.0f);
        });
    }

    /**
     * finds the hazard zone that's in the chunk the block pos is in and has the correct condition.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZonesByContainedPos(BlockPos containedPos) {
        return hazardZones.get(new ChunkPos(containedPos));
    }

    /**
     * finds all hazard zones that contain the given position and have the correct condition.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZonesByContainedPosAndCondition(BlockPos containedPos, MedicalCondition condition) {
        HazardZone zone = hazardZones.get(new ChunkPos(containedPos));
        return zone != null && zone.condition == condition ? zone : null;
    }

    public void removeZone(BlockPos source) {
        this.hazardZones.remove(new ChunkPos(source));
    }

    public void removeZone(ChunkPos chunkPos) {
        this.hazardZones.remove(chunkPos);
    }

    public void shrinkZone(BlockPos source, Direction side, int maxShrink) {
        Set<BlockPos> toRemove = FloodFiller3D.run(serverLevel, source, side, maxShrink);
        for (HazardZone zone : hazardZones.values()) {
            zone.blocks().removeAll(toRemove);
        }
    }

    public void addChunkZone(ChunkPos source, HazardZone zone) {
        this.hazardZones.put(source, zone);
    }

    public void addSphericalZone(BlockPos source, int sphereRadius, boolean canSpread,
                                 HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        Set<BlockPos> blocks = new HashSet<>();
        for (int x = -sphereRadius; x < sphereRadius; x++) {
            for (int y = -sphereRadius; y < sphereRadius; y++) {
                for (int z = -sphereRadius; z < sphereRadius; z++) {
                    float sizeFractionX = (float) x / sphereRadius;
                    float sizeFractionY = (float) y / sphereRadius;
                    float sizeFractionZ = (float) z / sphereRadius;
                    if ((sizeFractionX * sizeFractionX) +
                            (sizeFractionY * sizeFractionY) +
                            (sizeFractionZ * sizeFractionZ) <= 1) {
                        blocks.add(source.offset(x, y, z));
                    }
                }
            }
        }

        this.hazardZones.put(source, new HazardZone(blocks, canSpread, trigger, condition));
    }

    public void addCuboidZone(BlockPos source, int sizeX, int sizeY, int sizeZ, boolean canSpread,
                              HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        Set<BlockPos> blocks = new HashSet<>();
        sizeX = sizeX / 2;
        sizeY = sizeY / 2;
        sizeZ = sizeZ / 2;
        for (int x = -sizeX; x < sizeX; x++) {
            for (int y = -sizeY; y < sizeY; y++) {
                for (int z = -sizeZ; z < sizeZ; z++) {
                    blocks.add(source.offset(x, y, z));
                }
            }
        }

        this.hazardZones.put(source, new HazardZone(blocks, canSpread, trigger, condition));
    }

    public void addCuboidZone(BlockPos source, int size, boolean canSpread,
                              HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        Set<BlockPos> blocks = new HashSet<>();
        size = size / 2;
        for (int x = -size; x < size; x++) {
            for (int y = -size; y < size; y++) {
                for (int z = -size; z < size; z++) {
                    blocks.add(source.offset(x, y, z));
                }
            }
        }

        this.hazardZones.put(source, new HazardZone(blocks, canSpread, trigger, condition));
    }

    public void addFloodFilledZone(BlockPos source, Direction side, int maxSize, boolean canSpread,
                                   HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        Set<BlockPos> blocks = FloodFiller3D.run(serverLevel, source, side, maxSize);
        this.hazardZones.put(source, new HazardZone(blocks, canSpread, trigger, condition));
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag hazardZonesTag = new ListTag();
        for (var entry : hazardZones.entrySet()) {
            CompoundTag zoneTag = new CompoundTag();

            zoneTag.put("source", NbtUtils.writeBlockPos(entry.getKey()));
            entry.getValue().serializeNBT(zoneTag);

            hazardZonesTag.add(zoneTag);
        }
        compoundTag.put("zones", hazardZonesTag);
        return compoundTag;
    }

    public record HazardZone(Set<BlockPos> blocks, boolean canSpread, HazardProperty.HazardTrigger trigger, MedicalCondition condition) {

        public CompoundTag serializeNBT(CompoundTag zoneTag) {
            ListTag blocksTag = new ListTag();
            blocks.stream()
                    .map(NbtUtils::writeBlockPos)
                    .forEach(blocksTag::add);
            zoneTag.put("blocks", blocksTag);
            zoneTag.putBoolean("can_spread", canSpread);

            zoneTag.putString("trigger", trigger.name());
            zoneTag.putString("condition", condition.name);

            return zoneTag;
        }

        public static HazardZone deserializeNBT(CompoundTag zoneTag) {
            Set<BlockPos> allBlocks = zoneTag.getList("blocks", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(NbtUtils::readBlockPos)
                    .collect(Collectors.toSet());
            boolean canSpread = zoneTag.getBoolean("can_spread");

            HazardProperty.HazardTrigger trigger = HazardProperty.HazardTrigger.ALL_TRIGGERS
                    .get(zoneTag.getString("trigger"));
            MedicalCondition condition = com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition.CONDITIONS.get(zoneTag.getString("condition"));

            return new HazardZone(allBlocks, canSpread, trigger, condition);
        }
    }
}
