package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LevelDuctPipeNet extends LevelPipeNet<DuctPipeProperties, DuctPipeNet> {

    private static final String DATA_ID = "gtceu_duct_pipe_net";

    public static LevelDuctPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(() -> new LevelDuctPipeNet(serverLevel),
                        (tag, provider) -> new LevelDuctPipeNet(serverLevel, tag, provider)), DATA_ID);
    }

    public LevelDuctPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelDuctPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected DuctPipeNet createNetInstance() {
        return new DuctPipeNet(this);
    }
}
