package com.gregtechceu.gtceu.common.machine.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class AirScrubberMachine extends SimpleTieredMachine {

    public static final int CLEANING_PER_OPERATION = 10;

    public AirScrubberMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachines.defaultTankSizeFunction, args);
    }

    @Override
    public boolean onWorking() {
        if (getOffsetTimer() % 20 == 0) {
            ServerLevel serverLevel = (ServerLevel) getLevel();
            EnvironmentalHazardSavedData savedData = EnvironmentalHazardSavedData.getOrCreate(serverLevel);

            final ChunkPos pos = new ChunkPos(getPos());
            ChunkPos[] relativePositions = new ChunkPos[] {
                    pos,
                    new ChunkPos(pos.x, pos.z - 1),
                    new ChunkPos(pos.x, pos.z + 1),
                    new ChunkPos(pos.x - 1, pos.z),
                    new ChunkPos(pos.x + 1, pos.z)
            };
            for (ChunkPos rel : relativePositions) {
                savedData.getHazardZones().compute(rel, (k, v) -> {
                    if (v == null || v.strength() <= 0) {
                        return null;
                    }
                    EnvironmentalHazardSavedData.HazardZone zone;
                    if (k.equals(pos)) {
                        zone = new EnvironmentalHazardSavedData.HazardZone(
                                v.source(),
                                v.strength() - CLEANING_PER_OPERATION * 2 * getTier(),
                                v.canSpread(),
                                v.trigger(),
                                v.condition());
                    } else {
                        zone = new EnvironmentalHazardSavedData.HazardZone(
                                v.source(),
                                v.strength() - CLEANING_PER_OPERATION * getTier(),
                                v.canSpread(),
                                v.trigger(),
                                v.condition());
                    }
                    if (zone.strength() <= 0) return null;
                    else return zone;
                });
            }
        }
        return super.onWorking();
    }
}
