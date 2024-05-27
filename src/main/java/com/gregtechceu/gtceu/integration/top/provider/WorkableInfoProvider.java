package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
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
    protected void addProbeInfo(IWorkable capability, IProbeInfo probeInfo, Player player, BlockEntity blockEntity,
                                IProbeHitData data) {
        if (!capability.isActive()) return;

        int currentProgress = capability.getProgress();
        int maxProgress = capability.getMaxProgress();
        Component text;

        if (capability instanceof RecipeLogic logic &&
                logic.getLastRecipe() != null &&
                logic.getLastRecipe().data.getBoolean("duration_is_total_cwu")) {
            // show as total computation instead
            int color = capability.isWorkingEnabled() ? 0xFF00D4CE : 0xFFBB1C28;
            probeInfo.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                    .suffix(Component.translatable("gtceu.top.progress_computation", maxProgress))
                    .filledColor(color)
                    .alternateFilledColor(color)
                    .borderColor(0xFF555555));
            return;
        }

        if (maxProgress < 20) {
            text = Component.translatable("gtceu.top.progress_tick", maxProgress);
        } else {
            currentProgress = Math.round(currentProgress / 20.0F);
            maxProgress = Math.round(maxProgress / 20.0F);
            text = Component.translatable("gtceu.top.progress_sec", maxProgress);
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
