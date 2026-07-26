package com.gregtechceu.gtceu.api.capability;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.level.block.entity.BlockEntity;

import brachy.modularui.api.drawable.IDrawable;
import org.jetbrains.annotations.Nullable;

public interface IMonitorComponent {

    default boolean isMonitor() {
        return false;
    }

    IDrawable getIcon();

    default BlockPos getComponentPos() {
        if (this instanceof BlockEntity be) {
            return be.getBlockPos();
        }
        throw new UnsupportedOperationException(
                "IMonitorComponent implementations that are not BlockEntities must override getComponentPos()");
    }

    default @Nullable IItemHandler getDataItems() {
        return null;
    }
}
