package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.utils.RedstoneUtil;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class EnergyDetectorCover extends DetectorCover {
    public EnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return getEnergyContainer() != null;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IEnergyContainer energyContainer = getEnergyContainer();
        if (energyContainer != null) {
            long storedEnergy = energyContainer.getEnergyStored();
            long energyCapacity = energyContainer.getEnergyCapacity();

            if (energyCapacity == 0)
                return;

            setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedEnergy, energyCapacity, isInverted()));
        }
    }

    @Nullable
    protected IEnergyContainer getEnergyContainer() {
        return GTCapabilityHelper.getEnergyContainer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }
}
