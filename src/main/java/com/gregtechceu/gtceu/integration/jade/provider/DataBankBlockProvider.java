package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.LongTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class DataBankBlockProvider extends MachineInfoProvider<DataBankMachine, LongTag> {

    public DataBankBlockProvider() {
        super(GTCEu.id("data_bank"), DataBankMachine.class);
    }

    @Override
    protected LongTag write(DataBankMachine machine) {
        return LongTag.valueOf(machine.getEnergyUsage());
    }

    @Override
    protected void addTooltip(LongTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        long energyUsage = data.getAsLong();
        String energyFormatted = FormattingUtil.formatNumbers(energyUsage);
        // wrap in text component to keep it from being formatted
        Component voltageName = Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(energyUsage)]);
        Component text = Component.translatable(
                "gtceu.multiblock.energy_consumption",
                energyFormatted,
                voltageName);

        tooltip.add(text);
    }
}
