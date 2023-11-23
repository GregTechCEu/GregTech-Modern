package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.common.machine.trait.computation.ComputationRecipeLogic;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class WorkableInfoProvider extends CapabilityInfoProvider<IWorkable> {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("workable_provider");
    }

    @Nullable
    @Override
    protected IWorkable getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getWorkable(level, pos, side);
    }

    @Override
    protected void addProbeInfo(IWorkable capability, IProbeInfo probeInfo, Player player, BlockEntity blockEntity, IProbeHitData data) {
        if (!capability.isActive()) return;

        int currentProgress = capability.getProgress();
        int maxProgress = capability.getMaxProgress();
        String text;

        if (capability instanceof ComputationRecipeLogic logic && !logic.shouldShowDuration()) {
            // show as total computation instead
            int color = capability.isWorkingEnabled() ? 0xFF00D4CE : 0xFFBB1C28;
            probeInfo.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                    .suffix(" / " + maxProgress + " CWU")
                    .filledColor(color)
                    .alternateFilledColor(color)
                    .borderColor(0xFF555555));
            return;
        }

        if (maxProgress < 20) {
            text = " / " + maxProgress + " t";
        } else {
            currentProgress = Math.round(currentProgress / 20.0F);
            maxProgress = Math.round(maxProgress / 20.0F);
            text = " / " + maxProgress + " s";
        }

        if (maxProgress > 0) {
            int color = capability.isWorkingEnabled() ? 0xFF4CBB17 : 0xFFBB1C28;
            probeInfo.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                    .suffix(text)
                    .filledColor(color)
                    .alternateFilledColor(color)
                    .borderColor(0xFF555555));
        }
    }
}
