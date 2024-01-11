package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import static mcjty.theoneprobe.api.TextStyleClass.WARNING;


public class ControllableInfoProvider extends CapabilityInfoProvider<IControllable> {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("controllable_provider");
    }

    @Nullable
    @Override
    protected IControllable getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getControllable(level, pos, side);
    }

    @Override
    protected void addProbeInfo(IControllable capability, IProbeInfo probeInfo, Player player, BlockEntity blockEntity, IProbeHitData data) {
        if (!capability.isWorkingEnabled()) probeInfo.text(CompoundText.create().text("gtceu.top.working_disabled").style(WARNING));
    }
}
