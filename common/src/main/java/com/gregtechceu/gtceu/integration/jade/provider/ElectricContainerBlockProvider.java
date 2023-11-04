package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
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

public class ElectricContainerBlockProvider extends CapabilityBlockProvider<IEnergyContainer> {

    public ElectricContainerBlockProvider() {
        super(GTCEu.id("electric_container_provider"));
    }

    @Nullable
    @Override
    protected IEnergyContainer getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        return GTCapabilityHelper.getEnergyContainer(level, pos, side);
    }

    @Override
    protected void write(CompoundTag data, IEnergyContainer capability) {
        data.putLong("Energy", capability.getEnergyStored());
        data.putLong("MaxEnergy", capability.getEnergyCapacity());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        long maxStorage = capData.getLong("MaxEnergy");
        if (maxStorage == 0) return; // do not add empty max storage progress bar

        long stored = capData.getLong("Energy");
        var helper = tooltip.getElementHelper();

        tooltip.add(
                helper.progress(
                        getProgress(stored, maxStorage),
                        Component.literal(stored + " / " + maxStorage + " EU"),
                        helper.progressStyle().color(0xFFEEE600, 0xFFEEE600).textColor(-1),
                        Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555),
                        true
                )
        );
    }

    @Override
    protected boolean allowDisplaying(IEnergyContainer capability) {
        return !capability.isOneProbeHidden();
    }
}
