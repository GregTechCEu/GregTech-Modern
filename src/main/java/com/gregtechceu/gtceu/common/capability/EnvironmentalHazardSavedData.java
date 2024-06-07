package com.gregtechceu.gtceu.common.capability;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnvironmentalHazardSavedData extends SavedData {

    private final ServerLevel serverLevel;

    /**
     * Map of source position to a pair of (all affected blocks, material).
     */
    @Getter
    private final Map<BlockPos, Pair<Set<BlockPos>, MedicalCondition>> hazardZones = new HashMap<>();

    public static EnvironmentalHazardSavedData getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new EnvironmentalHazardSavedData(serverLevel, tag),
                () -> new EnvironmentalHazardSavedData(serverLevel), "gtceu_environmental_hazard_tracker");
    }

    public EnvironmentalHazardSavedData(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public void tick() {
        for (var pair : hazardZones.values()) {
            for (BlockPos pos : pair.getFirst()) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(), 1.0f),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1,
                        0, 0.1, 0, 0.1);
            }

            Set<ServerPlayer> playersInZone = serverLevel.players()
                    .stream()
                    .filter(player -> pair.getFirst().contains(BlockPos.containing(player.getEyePosition())))
                    .collect(Collectors.toSet());
            if (playersInZone.isEmpty()) {
                continue;
            }
            for (ServerPlayer player : playersInZone) {
                IMedicalConditionTracker tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
                if (tracker == null) {
                    continue;
                }
                tracker.progressCondition(pair.getSecond(), pair.getFirst().size() / 10.0f);
            }
        }
    }

    public EnvironmentalHazardSavedData(ServerLevel serverLevel, CompoundTag tag) {
        this(serverLevel);
        ListTag allHazardZones = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < allHazardZones.size(); ++i) {
            CompoundTag zone = allHazardZones.getCompound(i);

            BlockPos source = NbtUtils.readBlockPos(zone.getCompound("source"));

            Set<BlockPos> allBlocks = zone.getList("blocks", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(NbtUtils::readBlockPos)
                    .collect(Collectors.toSet());

            MedicalCondition condition = MedicalCondition.CONDITIONS.get(zone.getString("condition"));

            this.hazardZones.put(source, Pair.of(allBlocks, condition));
        }
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag hazardZonesTag = new ListTag();
        for (var entry : hazardZones.entrySet()) {
            CompoundTag zoneTag = new CompoundTag();
            zoneTag.put("source", NbtUtils.writeBlockPos(entry.getKey()));

            ListTag blocksTag = new ListTag();
            entry.getValue().getFirst().stream()
                    .map(NbtUtils::writeBlockPos)
                    .forEach(blocksTag::add);
            zoneTag.put("blocks", blocksTag);

            zoneTag.putString("condition", entry.getValue().getSecond().name);

            hazardZonesTag.add(zoneTag);
        }
        compoundTag.put("zones", hazardZonesTag);
        return compoundTag;
    }
}
