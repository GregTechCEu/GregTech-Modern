package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelOpticalPipeNet extends LevelPipeNet<OpticalPipeProperties, OpticalPipeNet> {

    private static final String DATA_ID = "gtceu_optical_pipe_net";

    public static LevelOpticalPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelOpticalPipeNet(serverLevel, tag),
                () -> new LevelOpticalPipeNet(serverLevel), DATA_ID);
    }

    public LevelOpticalPipeNet(ServerLevel level) {
        super(level);
    }

    public LevelOpticalPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected OpticalPipeNet createNetInstance() {
        return new OpticalPipeNet(this);
    }
}
