package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class MultiblockStructureProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("hasError")) {
            boolean hasError = blockAccessor.getServerData().getBoolean("hasError");
            if (hasError) {
                iTooltip.add(Component.translatable("gtceu.top.invalid_structure").withStyle(ChatFormatting.RED));
            } else {
                iTooltip.add(Component.translatable("gtceu.top.valid_structure").withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IMultiController controller) {
                compoundTag.putBoolean("hasError", !controller.isFormed());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("multiblock_structure");
    }
}
