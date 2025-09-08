package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ActivityDetectorCover extends DetectorCover {

    public ActivityDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() &&
                coverHolder.getEntity().getCapability(GTCapability.CAPABILITY_WORKABLE, attachedSide).isPresent();
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0) {
            return;
        }

        var workable = coverHolder.getEntity().getCapability(GTCapability.CAPABILITY_WORKABLE, attachedSide).resolve()
                .orElseThrow();

        boolean isCurrentlyWorking = workable.isActive() && workable.isWorkingEnabled();

        setRedstoneSignalOutput(isCurrentlyWorking != isInverted() ? 15 : 0);
    }
}
