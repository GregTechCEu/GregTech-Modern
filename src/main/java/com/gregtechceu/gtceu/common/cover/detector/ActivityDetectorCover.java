package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;

import org.jspecify.annotations.NullMarked;
import net.minecraft.core.Direction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@NullMarked
public class ActivityDetectorCover extends DetectorCover {

    public ActivityDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() &&
                GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide) != null;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0) {
            return;
        }

        var workable = GTCapabilityHelper.getWorkable(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide);

        boolean isCurrentlyWorking = workable.isActive() &&
                (workable.isWorkingEnabled() || workable.isSuspendAfterFinish());

        setRedstoneSignalOutput(isCurrentlyWorking != isInverted() ? 15 : 0);
    }
}
