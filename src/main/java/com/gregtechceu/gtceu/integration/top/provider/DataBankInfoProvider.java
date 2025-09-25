package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class DataBankInfoProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("data_bank_provider");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        if (MetaMachine.getMachine(level, iProbeHitData.getPos()) instanceof DataBankMachine dataBank) {
            IProbeInfo verticalPane = iProbeInfo.vertical(iProbeInfo.defaultLayoutStyle().spacing(0));
            int energyUsage = dataBank.getEnergyUsage();
            String energyFormatted = FormattingUtil.formatNumbers(energyUsage);
            // wrap in text component to keep it from being formatted
            Component voltageName = Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(energyUsage)]);
            Component text = Component.translatable(
                    "gtceu.multiblock.energy_consumption",
                    energyFormatted,
                    voltageName);
            verticalPane.text(text);
        }
    }
}
