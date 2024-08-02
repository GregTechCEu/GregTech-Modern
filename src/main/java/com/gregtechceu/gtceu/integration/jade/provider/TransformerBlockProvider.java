package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class TransformerBlockProvider extends CapabilityBlockProvider<TransformerMachine> {

    public TransformerBlockProvider() {
        super(GTCEu.id("transformer_provider"));
    }

    @Override
    protected @Nullable TransformerMachine getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (MetaMachine.getMachine(level, pos) instanceof TransformerMachine transformer) {
            return transformer;
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, TransformerMachine machine) {
        data.putInt("side", machine.getFrontFacing().get3DDataValue());
        data.putBoolean("transformUp", machine.isTransformUp());
        data.putInt("baseAmp", machine.getBaseAmp());
        data.putInt("baseVoltage", machine.getTier());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        boolean transformUp = capData.getBoolean("transformUp");
        int voltage = capData.getInt("baseVoltage");
        int amp = capData.getInt("baseAmp");
        if (transformUp) {
            tooltip.add(Component.translatable("gtceu.top.transform_up",
                    (GTValues.VNF[voltage] + " §r(" + amp * 4 + "A) -> " + GTValues.VNF[voltage + 1] + " §r(" + amp +
                            "A)")));
        } else {
            tooltip.add(Component.translatable("gtceu.top.transform_down",
                    (GTValues.VNF[voltage + 1] + " §r(" + amp + "A) -> " + GTValues.VNF[voltage] + " §r(" + amp * 4 +
                            "A)")));
        }

        /*
         * boolean higherAmpFace = block.getHitResult().getDirection() ==
         * Direction.from3DDataValue(capData.getInt("side"));
         * if(transformUp) {
         * tooltip.add(Component.translatable((higherAmpFace ? "gtceu.top.transform_output" :
         * "gtceu.top.transform_input"),
         * (GTValues.VNF[voltage] + "(" + amp * 4 + "A)")));
         * }
         */
        if (block.getHitResult().getDirection() == Direction.from3DDataValue(capData.getInt("side"))) {
            tooltip.add(
                    Component.translatable((transformUp ? "gtceu.top.transform_output" : "gtceu.top.transform_input"),
                            (GTValues.VNF[voltage + 1] + " §r(" + amp + "A)")));
        } else {
            tooltip.add(
                    Component.translatable((transformUp ? "gtceu.top.transform_input" : "gtceu.top.transform_output"),
                            (GTValues.VNF[voltage] + " §r(" + amp * 4 + "A)")));
        }
    }
}
