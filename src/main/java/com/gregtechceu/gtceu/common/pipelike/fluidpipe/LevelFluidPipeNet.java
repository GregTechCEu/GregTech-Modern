package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.material.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class LevelFluidPipeNet extends LevelPipeNet<FluidPipeProperties, FluidPipeNet> {

    public static LevelFluidPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(() -> new LevelFluidPipeNet(serverLevel),
                                (tag, provider) -> new LevelFluidPipeNet(serverLevel, tag, provider)),
                        "gtceu_fluid_pipe_net");
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
