package com.gregtechceu.gtceu.integration.top.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

import java.util.Locale;

public class StainedColorProvider implements IProbeInfoProvider {

    @Override
    public ResourceLocation getID() {
        return GTCEu.id("stained_color");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData iProbeHitData) {
        BlockEntity blockEntity = level.getBlockEntity(iProbeHitData.getPos());
        int paintingColor = -1;
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity) {
            paintingColor = machineBlockEntity.getMetaMachine().getPaintingColor();
        } else if (blockEntity instanceof PipeBlockEntity<?, ?> pipe) {
            paintingColor = pipe.getPaintingColor();
        }
        if (paintingColor != -1) {
            IProbeInfo horizontal = iProbeInfo
                    .horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontal.mcText(Component.translatable("gtceu.top.stained",
                    "#" + Integer.toHexString(paintingColor).toUpperCase(Locale.ROOT)));
        }
    }
}
