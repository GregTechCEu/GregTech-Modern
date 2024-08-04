package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import org.jetbrains.annotations.Nullable;

public class TransformerInfoProvider extends CapabilityInfoProvider<TransformerMachine> {

    @Override
    protected @Nullable TransformerMachine getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (MetaMachine.getMachine(level, pos) instanceof TransformerMachine transformer) {
            return transformer;
        }
        return null;
    }

    @Override
    protected void addProbeInfo(TransformerMachine capability, IProbeInfo probeInfo, Player player,
                                BlockEntity blockEntity, IProbeHitData data) {
        boolean transformUp = capability.isTransformUp();
        int voltage = capability.getTier();
        int amp = capability.getBaseAmp();
        int side = capability.getFrontFacing().get3DDataValue();

        IProbeInfo verticalPane = probeInfo.vertical(probeInfo.defaultLayoutStyle().spacing(0));
        if (transformUp) {
            verticalPane.text(Component.translatable("gtceu.top.transform_up",
                    (GTValues.VNF[voltage] + " §r(" + amp * 4 + "A) -> " + GTValues.VNF[voltage + 1] + " §r(" + amp +
                            "A)")));
        } else {
            verticalPane.text(Component.translatable("gtceu.top.transform_down",
                    (GTValues.VNF[voltage + 1] + " §r(" + amp + "A) -> " + GTValues.VNF[voltage] + " §r(" + amp * 4 +
                            "A)")));
        }

        if (data.getSideHit() == Direction.from3DDataValue(side)) {
            verticalPane.text(
                    Component.translatable((transformUp ? "gtceu.top.transform_output" : "gtceu.top.transform_input"),
                            (GTValues.VNF[voltage + 1] + " §r(" + amp + "A)")));
        } else {
            verticalPane.text(
                    Component.translatable((transformUp ? "gtceu.top.transform_input" : "gtceu.top.transform_output"),
                            (GTValues.VNF[voltage] + " §r(" + amp * 4 + "A)")));
        }
    }

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("transformer_provider");
    }
}
