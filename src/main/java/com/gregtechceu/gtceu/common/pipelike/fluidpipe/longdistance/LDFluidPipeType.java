package com.gregtechceu.gtceu.common.pipelike.fluidpipe.longdistance;

import com.gregtechceu.gtceu.api.pipenet.longdistance.LongDistancePipeType;
import com.gregtechceu.gtceu.config.ConfigHolder;

public class LDFluidPipeType extends LongDistancePipeType {

    public static final LDFluidPipeType INSTANCE = new LDFluidPipeType();

    private LDFluidPipeType() {
        super("fluid");
    }

    @Override
    public int getMinLength() {
        return ConfigHolder.INSTANCE.machines.ldFluidPipeMinDistance;
    }
}
