package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;

import net.minecraft.core.Direction;

public class ComputerMonitorCover extends CoverBehavior {

    public ComputerMonitorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    // No implementation here, this cover is just for decorative purposes

    @Override
    protected CoverRenderer buildRenderer() {
        return null;
    }
}
