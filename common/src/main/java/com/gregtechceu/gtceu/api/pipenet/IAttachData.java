package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.Direction;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote IAttachData
 */
public interface IAttachData {

    boolean canAttachTo(Direction side);

    boolean setAttached(Direction side, boolean attach);

}
