package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelDuctPipeNet extends LevelPipeNet<DuctPipeProperties, DuctPipeNet> {

    private static final String DATA_ID = "gtceu_duct_pipe_net";

    public static LevelDuctPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelDuctPipeNet(serverLevel, tag),
                () -> new LevelDuctPipeNet(serverLevel), DATA_ID);
    }

    public LevelDuctPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelDuctPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected DuctPipeNet createNetInstance() {
        return new DuctPipeNet(this);
    }
}
