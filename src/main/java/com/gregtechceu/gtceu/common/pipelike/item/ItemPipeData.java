package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.core.Direction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@AllArgsConstructor
public class ItemPipeData implements IAttachData {

    @Getter
    ItemPipeProperties properties;
    @Getter
    byte connections;

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
        if (obj instanceof ItemPipeData cableData) {
            return cableData.properties.equals(properties) && connections == cableData.connections;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return GTMath.hashInts(properties.hashCode(), connections);
    }
}
