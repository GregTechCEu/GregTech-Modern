package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.gui.misc.MonitorComponentIcons;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class MonitorPartMachine extends MonitorComponentPartMachine {

    public MonitorPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public boolean isMonitor() {
        return true;
    }

    @Override
    public Object getComponentIcon() {
        return MonitorComponentIcons.monitorCover();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }
}
