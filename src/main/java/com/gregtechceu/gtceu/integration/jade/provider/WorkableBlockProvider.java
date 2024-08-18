package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class WorkableBlockProvider extends CapabilityBlockProvider<IWorkable> {

    public WorkableBlockProvider() {
        super(GTCEu.id("workable_provider"));
    }

    @Nullable
    @Override
    protected IWorkable getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getWorkable(level, pos, side);
    }

    @Override
    protected void write(CompoundTag data, IWorkable capability) {
        data.putBoolean("Active", capability.isActive());
        data.putInt("Progress", capability.getProgress());
        data.putInt("MaxProgress", capability.getMaxProgress());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.getBoolean("Active")) return;

        int currentProgress = capData.getInt("Progress");
        int maxProgress = capData.getInt("MaxProgress");
        Component text;

        if (block.getBlockEntity() instanceof IMachineBlockEntity mbe &&
                mbe.getMetaMachine() instanceof IRecipeLogicMachine rlm &&
                rlm.getRecipeLogic().getLastRecipe() != null &&
                rlm.getRecipeLogic().getLastRecipe().value().data.getBoolean("duration_is_total_cwu")) {
            // show as total computation instead
            int color = rlm.getRecipeLogic().isWorkingEnabled() ? 0xFF00D4CE : 0xFFBB1C28;
            tooltip.add(IElementHelper.get().progress(
                    currentProgress,
                    Component.translatable("gtceu.jade.progress_computation", currentProgress, maxProgress),
                    IElementHelper.get().progressStyle().color(color).textColor(-1),
                    Util.make(BoxStyle.GradientBorder.DEFAULT_NESTED_BOX,
                            style -> style.borderColor = new int[] { 0xFF555555, 0xFF555555, 0xFF555555, 0xFF555555 }),
                    true));
            return;
        }

        if (maxProgress < 20) {
            text = Component.translatable("gtceu.jade.progress_tick", currentProgress, maxProgress);
        } else {
            text = Component.translatable("gtceu.jade.progress_sec", Math.round(currentProgress / 20.0F),
                    Math.round(maxProgress / 20.0F));
        }

        if (maxProgress > 0) {
            int color = capData.getBoolean("WorkingEnabled") ? 0xFF4CBB17 : 0xFFBB1C28;
            tooltip.add(
                    IElementHelper.get().progress(
                            getProgress(currentProgress, maxProgress),
                            text,
                            IElementHelper.get().progressStyle().color(color).textColor(-1),
                            Util.make(BoxStyle.GradientBorder.DEFAULT_NESTED_BOX,
                                    style -> style.borderColor = new int[] { 0xFF555555, 0xFF555555, 0xFF555555,
                                            0xFF555555 }),
                            true));
        }
    }
}
