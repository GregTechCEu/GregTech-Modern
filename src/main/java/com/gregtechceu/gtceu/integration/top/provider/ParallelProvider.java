package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.utils.FormattingUtil;

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
            int batch = 0;
            int subtickParallel = 0;
            int totalRuns = 0;
            boolean exact = false;
            if (machineBlockEntity.getMetaMachine() instanceof IParallelHatch parallelHatch) {
                parallel = parallelHatch.getCurrentParallel();
            } else if (machineBlockEntity.getMetaMachine() instanceof IMultiController controller) {
                if (controller instanceof IRecipeLogicMachine rlm &&
                        rlm.getRecipeLogic().isActive() &&
                        rlm.getRecipeLogic().getLastRecipe() != null) {
                    parallel = rlm.getRecipeLogic().getLastRecipe().parallels;
                    batch = rlm.getRecipeLogic().getLastRecipe().batchParallels;
                    subtickParallel = rlm.getRecipeLogic().getLastRecipe().subtickParallels;
                    totalRuns = rlm.getRecipeLogic().getLastRecipe().getTotalRuns();
                    exact = true;
                } else {
                    parallel = controller.getParallelHatch()
                            .map(IParallelHatch::getCurrentParallel)
                            .orElse(0);
                }
            }

            if (!exact && parallel > 1) {
                Component parallels = Component.literal(FormattingUtil.formatNumbers(parallel))
                        .withStyle(ChatFormatting.DARK_PURPLE);
                String key = "gtceu.multiblock.parallel";
                iProbeInfo.text(Component.translatable(key, parallels));
            } else if (totalRuns > 1) {
                Component runs = Component.literal(FormattingUtil.formatNumbers(totalRuns))
                        .withStyle(ChatFormatting.DARK_PURPLE);
                String key = "gtceu.multiblock.total_runs";
                iProbeInfo.text(Component.translatable(key, runs));

                if (parallel > 1) {
                    Component parallels = Component.literal(FormattingUtil.formatNumbers(parallel))
                            .withStyle(ChatFormatting.DARK_PURPLE);
                    String keyParallel = "gtceu.multiblock.parallel.exact";
                    iProbeInfo.text(Component.translatable(keyParallel, parallels));
                }
                if (batch > 1) {
                    Component batches = Component.literal(FormattingUtil.formatNumbers(batch))
                            .withStyle(ChatFormatting.DARK_PURPLE);
                    String keyBatch = "gtceu.multiblock.batch_enabled";
                    iProbeInfo.text(Component.translatable(keyBatch, batches));
                }
                if (subtickParallel > 1) {
                    Component subticks = Component.literal(FormattingUtil.formatNumbers(subtickParallel))
                            .withStyle(ChatFormatting.DARK_PURPLE);
                    String keySubtick = "gtceu.multiblock.subtick_parallels";
                    iProbeInfo.text(Component.translatable(keySubtick, subticks));
                }
            }
        }
    }
}
