package com.gregtechceu.gtceu.common.pipelike.laser;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LevelLaserPipeNet extends LevelPipeNet<LaserPipeProperties, LaserPipeNet> {

    private static final String DATA_ID = "gtceu_laser_pipe_net";
    private static final SavedDataType<LevelLaserPipeNet> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_ID),
            LevelLaserPipeNet::new,
            serverLevel -> CompoundTag.CODEC.xmap(
                    tag -> new LevelLaserPipeNet(serverLevel, tag, serverLevel.registryAccess()),
                    data -> data.save(new CompoundTag(), serverLevel.registryAccess())));

    public static LevelLaserPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public LevelLaserPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelLaserPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected LaserPipeNet createNetInstance() {
        return new LaserPipeNet(this);
    }
}
