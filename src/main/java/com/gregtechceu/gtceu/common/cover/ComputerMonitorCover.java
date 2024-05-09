package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.covers.CoverBehavior;
import com.gregtechceu.gtceu.api.covers.CoverDefinition;
import net.minecraft.core.Direction;

public class ComputerMonitorCover extends CoverBehavior {
    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    // No implementation here, this cover is just for decorative purposes
}
