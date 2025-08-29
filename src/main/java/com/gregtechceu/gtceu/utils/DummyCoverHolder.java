package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DummyCoverHolder implements ICoverable {

    @Getter
    private final Level level;
    @Getter
    private final BlockPos pos;
    private final Map<Direction, CoverBehavior> covers;

    public DummyCoverHolder(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
        this.covers = new HashMap<>();
    }

    @Override
    public long getOffsetTimer() {
        return 0;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isInValid() {
        return false;
    }

    @Override
    public void notifyBlockUpdate() {}

    @Override
    public void scheduleRenderUpdate() {}

    @Override
    public void scheduleNeighborShapeUpdate() {}

    @Override
    public boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side) {
        return true;
    }

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public Direction getFrontFacing() {
        return Direction.NORTH;
    }

    @Override
    public boolean shouldRenderBackSide() {
        return false;
    }

    @Override
    public IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    public void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side) {
        covers.put(side, coverBehavior);
    }

    @Override
    public @Nullable CoverBehavior getCoverAtSide(Direction side) {
        return covers.get(side);
    }

    @Override
    public @Nullable TickableSubscription subscribeServerTick(Runnable runnable) {
        return null;
    }

    @Override
    public void unsubscribe(@Nullable TickableSubscription current) {}
}
