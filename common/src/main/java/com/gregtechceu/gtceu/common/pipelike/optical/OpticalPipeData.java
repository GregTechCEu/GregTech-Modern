package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.pipenet.IAttachData;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.Direction;

@Accessors(fluent = true)
public class OpticalPipeData implements IAttachData {

    @Getter
    byte connections;

    @Override
    public boolean canAttachTo(Direction side) {
        return (connections & (1 << side.ordinal())) != 0 && Integer.bitCount(connections) <= 2;
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
}
