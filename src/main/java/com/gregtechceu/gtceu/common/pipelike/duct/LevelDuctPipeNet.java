package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LevelDuctPipeNet extends LevelPipeNet<DuctPipeProperties, DuctPipeNet> {

    private static final String DATA_ID = "gtceu_duct_pipe_net";
    private static final SavedDataType<LevelDuctPipeNet> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_ID),
            LevelDuctPipeNet::new,
            serverLevel -> CompoundTag.CODEC.xmap(
                    tag -> new LevelDuctPipeNet(serverLevel, tag, serverLevel.registryAccess()),
                    data -> data.save(new CompoundTag(), serverLevel.registryAccess())));

    public static LevelDuctPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
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
