package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.core.Direction;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class FluidPipeData implements IAttachData {

    @Getter
    FluidPipeProperties properties;
    @Getter
    byte connections;

    public FluidPipeData(FluidPipeProperties properties, byte connections) {
        this.properties = properties;
        this.connections = connections;
    }

    @Override
    public boolean canAttachTo(Direction side) {
        return (connections & (1 << side.ordinal())) != 0;
    }

    @Override
    public boolean setAttached(Direction side, boolean attach) {
        var result = canAttachTo(side);
        if (result != attach) {
            if (attach) {
                connections |= (1 << side.ordinal());
            } else {
                connections &= ~(1 << side.ordinal());
            }
        }
        return result != attach;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluidPipeData cableData) {
            return cableData.properties.equals(properties) && connections == cableData.connections;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return GTMath.hashInts(properties.hashCode(), connections);
    }
}
