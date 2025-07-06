package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.Direction;

public interface IAttachData {

    /**
     * is the node can attach to the side.
     */
    boolean canAttachTo(Direction side);

    /**
     * set it attach to a side.
     * 
     * @return whether the status is changed.
     */
    boolean setAttached(Direction side, boolean attach);
}
