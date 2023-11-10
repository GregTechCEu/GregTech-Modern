package com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance;

import com.gregtechceu.gtceu.api.pipenet.longdistance.ILDEndpoint;
import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistancePipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.world.level.block.state.BlockState;

public class LDFluidPipeType extends LongDistancePipeType {

    public static final LDFluidPipeType INSTANCE = new LDFluidPipeType();

    private LDFluidPipeType() {
        super("fluid");
    }

    @Override
    public boolean isValidBlock(BlockState blockState) {
        return GTBlocks.LD_FLUID_PIPE.is(blockState.getBlock());
    }

    @Override
    public boolean isValidEndpoint(ILDEndpoint endpoint) {
        return endpoint instanceof LDFluidEndpointMachine;
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldFluidPipeMinDistance;
    }
}