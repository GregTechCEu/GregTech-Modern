package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedDataType;

public class LevelFluidPipeNet extends LevelPipeNet<FluidPipeProperties, FluidPipeNet> {

    private static final String DATA_ID = "gtceu_fluid_pipe_net";
    private static final SavedDataType<LevelFluidPipeNet> TYPE = new SavedDataType<>(
            GTCEu.id(DATA_ID),
            LevelFluidPipeNet::new,
            serverLevel -> CompoundTag.CODEC.xmap(
                    tag -> new LevelFluidPipeNet(serverLevel, tag, serverLevel.registryAccess()),
                    data -> data.save(new CompoundTag(), serverLevel.registryAccess())));

    public static LevelFluidPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(TYPE);
    }

    public LevelFluidPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelFluidPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected FluidPipeNet createNetInstance() {
        return new FluidPipeNet(this);
    }
}
