package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LevelOpticalPipeNet extends LevelPipeNet<OpticalPipeProperties, OpticalPipeNet> {

    private static final String DATA_ID = "gtceu_optical_pipe_net";
    private static final SavedDataType<LevelOpticalPipeNet> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_ID),
            LevelOpticalPipeNet::new,
            serverLevel -> CompoundTag.CODEC.xmap(
                    tag -> new LevelOpticalPipeNet(serverLevel, tag, serverLevel.registryAccess()),
                    data -> data.save(new CompoundTag(), serverLevel.registryAccess())));

    public static LevelOpticalPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public LevelOpticalPipeNet(ServerLevel level) {
        super(level);
    }

    public LevelOpticalPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected OpticalPipeNet createNetInstance() {
        return new OpticalPipeNet(this);
    }
}
