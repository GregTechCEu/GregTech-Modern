package com.gregtechceu.gtceu.common.pipelike.optical;

import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class WorldOpticalPipeNet extends LevelPipeNet<OpticalPipeData, OpticalPipeNet> {

    private static final String DATA_ID = "gtceu_optical_pipe_net";

    public static WorldOpticalPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new WorldOpticalPipeNet(serverLevel, tag), () -> new WorldOpticalPipeNet(serverLevel), DATA_ID);
    }

    public WorldOpticalPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public WorldOpticalPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected OpticalPipeNet createNetInstance() {
        return new OpticalPipeNet(this);
    }
}
