package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class StainedColorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("StainedColor")) {
            int paintingColor = blockAccessor.getServerData().getInt("StainedColor");
            if (paintingColor != IPaintable.UNPAINTED_COLOR) {
                iTooltip.add(Component.translatable("gtceu.top.stained", String.format("#%06X", paintingColor))
                        .withStyle(style -> style.withColor(paintingColor)));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof IPaintable paintable) {
            int paintingColor = paintable.isPainted() ? paintable.getPaintingColor() : IPaintable.UNPAINTED_COLOR;
            compoundTag.putInt("StainedColor", paintingColor);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("stained_color");
    }
}
