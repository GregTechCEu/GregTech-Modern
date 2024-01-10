package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
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
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.getBoolean("Active")) return;

        int currentProgress = capData.getInt("Progress");
        int maxProgress = capData.getInt("MaxProgress");
        String text;

        if (maxProgress < 20) {
            text = currentProgress + " / " + maxProgress + " t";
        } else {
            text = Math.round(currentProgress / 20.0F) + " / " + Math.round(maxProgress / 20.0F) + " s";
        }

        if (maxProgress > 0) {
            int color = capData.getBoolean("WorkingEnabled") ? 0xFF4CBB17 : 0xFFBB1C28;
            tooltip.add(
                    tooltip.getElementHelper().progress(
                            getProgress(currentProgress, maxProgress),
                            Component.literal(text),
                            tooltip.getElementHelper().progressStyle().color(color).textColor(-1),
                            Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555),
                            true
                    )
            );
        }
    }
}
