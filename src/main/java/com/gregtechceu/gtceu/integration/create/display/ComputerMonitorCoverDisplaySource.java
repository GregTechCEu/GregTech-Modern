package com.gregtechceu.gtceu.integration.create.display;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.common.cover.ComputerMonitorCover;
import com.gregtechceu.gtceu.utils.GTStringUtils;

import net.minecraft.network.chat.MutableComponent;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import java.util.List;

public class ComputerMonitorCoverDisplaySource extends DisplaySource {

    private int refreshTicks = 100;

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        ICoverable coverable = GTCapabilityHelper.getCoverable(context.level(), context.getSourcePos(),
                context.blockEntity().getDirection().getOpposite());
        if (coverable != null) {
            if (coverable.getCoverAtSide(
                    context.blockEntity().getDirection().getOpposite()) instanceof ComputerMonitorCover cover) {
                refreshTicks = cover.getUpdateInterval();
                return cover.getText();
            }
        }
        return GTStringUtils.literalLine("No cover!");
    }

    @Override
    public int getPassiveRefreshTicks() {
        return refreshTicks;
    }
}
