package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.material.material.properties.FluidPipeProperties;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote CableData
 */
@Accessors(fluent = true)
public class FluidPipeData {

    @Getter
    FluidPipeProperties properties;
    @Getter
    byte connections;

    public FluidPipeData(FluidPipeProperties properties, byte connections) {
        this.properties = properties;
        this.connections = connections;
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
        return Objects.hash(properties, connections);
    }
}
