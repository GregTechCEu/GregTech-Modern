package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRenderer;
import com.gregtechceu.gtceu.client.renderer.pipe.cover.CoverRendererBuilder;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.utils.RedstoneUtil;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyDetectorCover extends DetectorCover {

    public EnergyDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    protected CoverRenderer buildRenderer() {
        return new CoverRendererBuilder(GTCEu.id("block/cover/overlay_energy_detector"), GTCEu.id("block/cover/overlay_energy_detector_emissive")).build();
    }

    @Override
    public boolean canAttach(@NotNull ICoverable coverable, @NotNull Direction side) {
        return coverable.getCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER).isPresent();
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        IEnergyInfoProvider energyInfoProvider = getEnergyInfoProvider();
        if (energyInfoProvider == null)
            return;

        var energyInfo = energyInfoProvider.getEnergyInfo();

        long storedEnergy = energyInfo.stored().longValue();
        long energyCapacity = energyInfo.capacity().longValue();

        if (energyCapacity == 0)
            return;

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneValue(storedEnergy, energyCapacity, isInverted()));
    }

    @Nullable
    protected IEnergyInfoProvider getEnergyInfoProvider() {
        return GTCapabilityHelper.getEnergyInfoProvider(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }
}
