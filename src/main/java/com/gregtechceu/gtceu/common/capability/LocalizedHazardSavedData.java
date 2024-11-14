package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.particle.HazardParticleOptions;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.BreadthFirstBlockSearch;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * localized, block-based environmental hazards. e.g. radiation.
 * do not use for large-scale effects, as this stores all blocks where an effect is, and thus will be too slow for that.
 */
public class LocalizedHazardSavedData extends SavedData {

    public static final int MIN_STRENGTH_FOR_SPREAD = 100;

    private final ServerLevel serverLevel;

    /**
     * Map of source position to a triple of (trigger, material).
     */
    @Getter
    private final Map<BlockPos, HazardZone> hazardZones = new HashMap<>();

    public static LocalizedHazardSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LocalizedHazardSavedData(serverLevel, tag),
                () -> new LocalizedHazardSavedData(serverLevel), "gtceu_localized_hazard_tracker");
    }

    public LocalizedHazardSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public LocalizedHazardSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        ListTag allHazardZones = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < allHazardZones.size(); ++i) {
            CompoundTag zoneTag = allHazardZones.getCompound(i);

            BlockPos source = BlockPos.of(zoneTag.getLong("pos"));
            HazardZone zone = HazardZone.deserializeNBT(zoneTag);

            this.hazardZones.put(source, zone);
        }
    }

    public void tick() {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return;
        }

        Object2IntMap<BlockPos> zonesToSpread = new Object2IntOpenHashMap<>();

        RandomSource random = serverLevel.random;
        for (final var entry : hazardZones.entrySet()) {
            HazardZone zone = entry.getValue();
            if (zone.strength() < MIN_STRENGTH_FOR_SPREAD / 5) {
                continue;
            }
            // try to spawn particles on every block in the zone if it's loaded and empty.
            for (BlockPos pos : zone.blocks()) {
                if (serverLevel.isLoaded(pos) &&
                        !serverLevel.getBlockState(pos).isCollisionShapeFullBlock(serverLevel, pos) &&
                        GTValues.RNG.nextInt(64000 / zone.strength()) == 0) {
                    serverLevel.sendParticles(
                            new HazardParticleOptions(zone.condition().color, zone.strength() / 250f),
                            pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                            pos.getZ() + random.nextDouble(),
                            1, 0, 0, 0, 0.1);
                }
            }

            Stream<ServerPlayer> playersInZone = serverLevel.players()
                    .stream()
                    .filter(player -> zone.blocks().contains(BlockPos.containing(player.getEyePosition())));
            tickPlayerHazards(zone, playersInZone);

            if (zone.canSpread() && zone.strength() > MIN_STRENGTH_FOR_SPREAD) {
                zonesToSpread.put(entry.getKey(), (zone.strength() - MIN_STRENGTH_FOR_SPREAD) / 500);
            }
        }

        zonesToSpread.forEach(this::expandHazard);
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
     * finds the hazard zone that's in the chunk the block pos is in and has the correct condition.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZoneByContainedPos(BlockPos containedPos) {
        for (HazardZone zone : hazardZones.values()) {
            if (zone.blocks().contains(containedPos)) {
                return zone;
            }
        }
        return null;
    }

    /**
     * finds all hazard zones that contain the given position and have the correct condition.
     *
     * @param containedPos the position to find zones for.
     * @return all zones that were found.
     */
    @Nullable
    public HazardZone getZoneByContainedPosAndCondition(BlockPos containedPos, MedicalCondition condition) {
        for (HazardZone zone : hazardZones.values()) {
            if (zone.condition() == condition && zone.blocks().contains(containedPos)) {
                return zone;
            }
        }
        return null;
    }

    public void removeZoneByPosition(BlockPos containedPos) {
        BlockPos toRemove = null;
        for (var entry : hazardZones.entrySet()) {
            if (entry.getValue().blocks().contains(containedPos)) {
                toRemove = entry.getKey();
            }
        }
        hazardZones.remove(toRemove);
    }

    public void removeZoneByPosition(BlockPos containedPos, MedicalCondition condition) {
        BlockPos toRemove = null;
        for (var entry : hazardZones.entrySet()) {
            if (entry.getValue().condition() == condition && entry.getValue().blocks().contains(containedPos)) {
                toRemove = entry.getKey();
            }
        }
        hazardZones.remove(toRemove);
    }

    public boolean expandHazard(BlockPos source, int blocksToAdd) {
        if (!ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return true;
        }

        if (blocksToAdd <= 0) {
            return false;
        }
        if (this.hazardZones.containsKey(source)) {
            final HazardZone zone = hazardZones.get(source);

            Set<BlockPos> allValidBlocks = BreadthFirstBlockSearch.search(blockPos -> !zone.blocks().contains(blockPos),
                    source, blocksToAdd);

            for (BlockPos found : allValidBlocks) {
                zone.blocks().add(found);
            }
            this.setDirty();
            return true;
        }
        return false;
    }

    public void addSphericalZone(BlockPos source, int sphereRadius, boolean canSpread,
                                 HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        if (expandHazard(source, (int) ((4.0 / 3) * Math.PI * Math.pow(sphereRadius, 3) / 50))) {
            return;
        }

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
        this.setDirty();
    }

    public void addCuboidZone(BlockPos source, int sizeX, int sizeY, int sizeZ, boolean canSpread,
                              HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        if (expandHazard(source, sizeX * sizeY * sizeZ / 100)) {
            return;
        }

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
        this.setDirty();
    }

    public void addCuboidZone(BlockPos source, int size, boolean canSpread,
                              HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        if (expandHazard(source, size * size * size / 100)) {
            return;
        }

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
        this.setDirty();
    }

    public void addCuboidZone(BlockPos source, BlockPos start, BlockPos end, boolean canSpread,
                              HazardProperty.HazardTrigger trigger, MedicalCondition condition) {
        int sizeX = start.getX() - end.getX();
        int sizeY = start.getY() - end.getY();
        int sizeZ = start.getZ() - end.getZ();
        if (expandHazard(source, Math.abs(sizeX * sizeY * sizeZ) / 100)) {
            return;
        }

        Set<BlockPos> blocks = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
            blocks.add(pos.immutable());
        }
        this.hazardZones.put(source, new HazardZone(blocks, canSpread, trigger, condition));
        this.setDirty();
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag hazardZonesTag = new ListTag();
        for (var entry : hazardZones.entrySet()) {
            CompoundTag zoneTag = new CompoundTag();

            zoneTag.putLong("pos", entry.getKey().asLong());
            entry.getValue().serializeNBT(zoneTag);

            hazardZonesTag.add(zoneTag);
        }
        compoundTag.put("zones", hazardZonesTag);
        return compoundTag;
    }

    public record HazardZone(Set<BlockPos> blocks, boolean canSpread,
                             HazardProperty.HazardTrigger trigger, MedicalCondition condition) {

        public int strength() {
            return blocks.size();
        }

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
            Set<BlockPos> blocks = zoneTag.getList("blocks", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(NbtUtils::readBlockPos)
                    .collect(Collectors.toSet());
            boolean canSpread = zoneTag.getBoolean("can_spread");
            HazardProperty.HazardTrigger trigger = HazardProperty.HazardTrigger.ALL_TRIGGERS
                    .get(zoneTag.getString("trigger"));
            MedicalCondition condition = MedicalCondition.CONDITIONS.get(zoneTag.getString("condition"));

            return new HazardZone(blocks, canSpread, trigger, condition);
        }
    }
}
