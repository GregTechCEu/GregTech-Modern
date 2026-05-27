package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class MultiblockStructureProvider extends MachineInfoProvider<MultiblockControllerMachine, ByteTag> {

    public MultiblockStructureProvider() {
        super(GTCEu.id("multiblock_structure"), MultiblockControllerMachine.class);
    }

    @Override
    protected ByteTag write(MultiblockControllerMachine machine) {
        return ByteTag.valueOf(machine.isFormed());
    }

    @Override
    protected void addTooltip(ByteTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (data.getAsByte() == 0) {
            tooltip.add(Component.translatable("gtceu.top.invalid_structure").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("gtceu.top.valid_structure").withStyle(ChatFormatting.GREEN));
        }
    }
}
