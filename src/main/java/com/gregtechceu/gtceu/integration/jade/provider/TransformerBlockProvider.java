package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TransformerBlockProvider extends MachineInfoProvider<TransformerMachine, CompoundTag> {

    public TransformerBlockProvider() {
        super(GTCEu.id("transformer"), TransformerMachine.class);
    }

    @Override
    protected CompoundTag write(TransformerMachine transformer) {
        var tag = new CompoundTag();
        tag.putInt("side", transformer.getFrontFacing().get3DDataValue());
        tag.putBoolean("transformUp", transformer.isTransformUp());
        tag.putInt("baseAmp", transformer.getBaseAmp());
        tag.putInt("baseVoltage", transformer.getTier());
        return tag;
    }

    @Override
    protected void addTooltip(CompoundTag data, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        boolean transformUp = data.getBoolean("transformUp");
        int voltage = data.getInt("baseVoltage");
        int amp = data.getInt("baseAmp");
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

        if (block.getHitResult().getDirection() ==
                Direction.from3DDataValue(data.getInt("side"))) {
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
