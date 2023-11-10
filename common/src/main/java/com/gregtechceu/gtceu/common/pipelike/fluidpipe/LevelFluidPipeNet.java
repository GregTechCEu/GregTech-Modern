package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelFluidPipeNet extends LevelPipeNet<FluidPipeData, FluidPipeNet> {

    public static LevelFluidPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelFluidPipeNet(serverLevel, tag), () -> new LevelFluidPipeNet(serverLevel), "gtcue_fluid_pipe_net");
    }

    public LevelFluidPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelFluidPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected FluidPipeNet createNetInstance() {
        return new FluidPipeNet(this);
    }

}
