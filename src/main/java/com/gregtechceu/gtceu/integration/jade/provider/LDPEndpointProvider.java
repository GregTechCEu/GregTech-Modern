package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class LDPEndpointProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof LongDistanceEndpointMachine machine) {
            boolean isFormed = blockAccessor.getServerData().getBoolean("isFormed");
            String ioType = blockAccessor.getServerData().getString("ioType");
            String outputDirection = blockAccessor.getServerData().getString("outputDirection");

            iTooltip.add(Component.translatable(
                    isFormed ? "gtceu.top.ldp_endpoint.is_formed" : "gtceu.top.ldp_endpoint.not_formed"));
            iTooltip.add(Component.translatable("gtceu.top.ldp_endpoint.io_type", Component.translatable(ioType)
                    .withStyle(ioType.contains("import") ? ChatFormatting.GREEN : ChatFormatting.RED)));
            iTooltip.add(Component.translatable("gtceu.top.ldp_endpoint.output_direction",
                    FormattingUtil.toEnglishName((outputDirection))));
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof LongDistanceEndpointMachine ldpEndpoint) {
            compoundTag.putBoolean("isFormed", ldpEndpoint.getLink() == null ? false : true);
            compoundTag.putString("ioType", ldpEndpoint.getIoType().getTooltip());
            compoundTag.putString("outputDirection", ldpEndpoint.getOutputFacing().getName());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("ldp_endpoint");
    }
}
