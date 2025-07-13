package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class MultiblockStructureProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("multiblock_structure");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        if (level.getBlockEntity(iProbeHitData.getPos()) instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IMultiController controller) {
                if (!controller.isFormed()) {
                    iProbeInfo.text(CompoundText.create().error("gtceu.top.invalid_structure"));
                } else {
                    iProbeInfo.text(CompoundText.create().ok("gtceu.top.valid_structure"));
                }
            }
        }
    }
}
