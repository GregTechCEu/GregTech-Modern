package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class ElectricContainerBlockProvider extends CapabilityBlockProvider<IEnergyInfoProvider> {

    public ElectricContainerBlockProvider() {
        super(GTCEu.id("electric_container_provider"));
    }

    @Override
    protected @Nullable IEnergyInfoProvider getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getEnergyInfoProvider(level, pos, side);
    }

    @Override
    protected void write(CompoundTag data, IEnergyInfoProvider capability) {
        data.putByteArray("Energy", capability.getEnergyInfo().stored().toByteArray());
        data.putByteArray("MaxEnergy", capability.getEnergyInfo().capacity().toByteArray());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.contains("Energy") && !capData.contains("MaxEnergy")) return;

        var energy = new BigInteger(capData.getByteArray("Energy"));
        var maxEnergy = new BigInteger(capData.getByteArray("MaxEnergy"));
        if (maxEnergy.compareTo(BigInteger.ZERO) <= 0) return;
        var threshold = BigInteger.valueOf((long) 1e12);
        var energyStr = FormattingUtil.formatNumberOrSic(energy, threshold);
        var maxEnergyStr = FormattingUtil.formatNumberOrSic(maxEnergy, threshold);
        var progress = getProgress(energy, maxEnergy);

        var helper = tooltip.getElementHelper();

        tooltip.add(
                helper.progress(
                        progress,
                        Component.translatable("gtceu.jade.energy_stored", energyStr, maxEnergyStr),
                        helper.progressStyle().color(0xFFEEE600, 0xFFEEE600).textColor(-1),
                        Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555),
                        true));
    }

    @Override
    protected boolean allowDisplaying(IEnergyInfoProvider capability) {
        return !capability.isOneProbeHidden();
    }

    protected float getProgress(long progress, long maxProgress) {
        return maxProgress == 0 ? 0 : (float) ((double) progress / maxProgress);
    }

    protected float getProgress(BigInteger progress, BigInteger maxProgress) {
        if (maxProgress.compareTo(BigInteger.ZERO) <= 0) return 0;
        return new BigDecimal(progress).divide(new BigDecimal(maxProgress), MathContext.DECIMAL32).floatValue();
    }
}
