package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class ParallelProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("parallel");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        BlockEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
        if (blockEntity instanceof MetaMachineBlockEntity machineBlockEntity) {
            int parallel = 0;
            if (machineBlockEntity.getMetaMachine() instanceof IParallelHatch parallelHatch) {
                parallel = parallelHatch.getCurrentParallel();
            } else if (machineBlockEntity.getMetaMachine() instanceof IMultiController controller) {
                parallel = controller.getParallelHatch()
                        .map(IParallelHatch::getCurrentParallel)
                        .orElse(0);
            }
            if (parallel > 0) {
                iProbeInfo.text(Component.translatable(
                        "gtceu.multiblock.parallel",
                        Component.literal(parallel + "").withStyle(ChatFormatting.DARK_PURPLE)));
            }
        }
    }
}
