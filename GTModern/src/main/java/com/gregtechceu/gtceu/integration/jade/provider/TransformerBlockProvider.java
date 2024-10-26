package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TransformerBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public ResourceLocation getUid() {
        return GTCEu.id("transformer");
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            MetaMachine machine = blockEntity.getMetaMachine();
            if (machine instanceof TransformerMachine transformer) {
                compoundTag.putInt("side", transformer.getFrontFacing().get3DDataValue());
                compoundTag.putBoolean("transformUp", transformer.isTransformUp());
                compoundTag.putInt("baseAmp", transformer.getBaseAmp());
                compoundTag.putInt("baseVoltage", transformer.getTier());
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            MetaMachine machine = blockEntity.getMetaMachine();
            if (machine instanceof TransformerMachine transformer) {
                boolean transformUp = blockAccessor.getServerData().getBoolean("transformUp");
                int voltage = blockAccessor.getServerData().getInt("baseVoltage");
                int amp = blockAccessor.getServerData().getInt("baseAmp");
                if (transformUp) {
                    tooltip.add(Component.translatable("gtceu.top.transform_up",
                            (GTValues.VNF[voltage] + " §r(" + amp * 4 + "A) -> " + GTValues.VNF[voltage + 1] + " §r(" +
                                    amp +
                                    "A)")));
                } else {
                    tooltip.add(Component.translatable("gtceu.top.transform_down",
                            (GTValues.VNF[voltage + 1] + " §r(" + amp + "A) -> " + GTValues.VNF[voltage] + " §r(" +
                                    amp * 4 +
                                    "A)")));
                }

                if (blockAccessor.getHitResult().getDirection() ==
                        Direction.from3DDataValue(blockAccessor.getServerData().getInt("side"))) {
                    tooltip.add(
                            Component.translatable(
                                    (transformUp ? "gtceu.top.transform_output" : "gtceu.top.transform_input"),
                                    (GTValues.VNF[voltage + 1] + " §r(" + amp + "A)")));
                } else {
                    tooltip.add(
                            Component.translatable(
                                    (transformUp ? "gtceu.top.transform_input" : "gtceu.top.transform_output"),
                                    (GTValues.VNF[voltage] + " §r(" + amp * 4 + "A)")));
                }
            }
        }
    }
}
