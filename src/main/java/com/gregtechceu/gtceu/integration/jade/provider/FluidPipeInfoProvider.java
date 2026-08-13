package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.block.FluidPipeBlock;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class FluidPipeInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
        BlockEntity be = blockAccessor.getBlockEntity();
        if (be != null) {
            CompoundTag data = blockAccessor.getServerData().getCompound(getUid().toString());
            if (data.contains("insulated") && data.getBoolean("insulated")) {
                tooltip.add(Component.translatable("gtceu.fluid_pipe.insulated"));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        CompoundTag data = compoundTag.getCompound(getUid().toString());
        if (blockAccessor.getBlock() instanceof FluidPipeBlock fluidPipeBlock) {
            FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) fluidPipeBlock.getPipeTile(
                    blockAccessor.getLevel(), blockAccessor.getPosition());
            if (pipe != null) {
                data.putBoolean("insulated", pipe.isInsulated());
            }
        }
        compoundTag.put(getUid().toString(), data);
    }

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("fluid_pipe_info");
    }
}
