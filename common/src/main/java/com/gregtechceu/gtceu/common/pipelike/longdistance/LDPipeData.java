package com.gregtechceu.gtceu.common.pipelike.longdistance;

import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeData;
import lombok.Getter;
import net.minecraft.core.Direction;

import java.util.Objects;

public class LDPipeData implements IAttachData {

    @Getter
    LDPipeProperties properties;

    public LDPipeData(LDPipeProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean canAttachTo(Direction side) {
        return true;
    }

    @Override
    public boolean setAttached(Direction side, boolean attach) {
        return true;
    }

}
