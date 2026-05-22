package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;

import net.minecraft.nbt.ByteTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class EnergyConverterModeProvider extends MachineInfoProvider<ConverterMachine, ByteTag> {

    public EnergyConverterModeProvider() {
        super(GTCEu.id("energy_converter_provider"), ConverterMachine.class);
    }

    @Override
    protected ByteTag write(ConverterMachine machine) {
        return ByteTag.valueOf(machine.isFeToEu());
    }

    @Override
    protected void addTooltip(ByteTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (data.getAsByte() == 1) {
            tooltip.add(Component.translatable("gtceu.top.convert_fe"));
        } else {
            tooltip.add(Component.translatable("gtceu.top.convert_eu"));
        }
    }
}
