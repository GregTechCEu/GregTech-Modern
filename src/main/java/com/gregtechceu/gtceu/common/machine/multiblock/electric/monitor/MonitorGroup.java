package com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.BlockPos;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonitorGroup {

    private final Set<BlockPos> relativeMonitorPositions = new HashSet<>();
    @Getter
    private final String name;
    @Getter
    private final CustomItemStackHandler itemStackHandler;

    public MonitorGroup(String name) {
        this(name, new CustomItemStackHandler(1));
    }

    public MonitorGroup(String name, CustomItemStackHandler handler) {
        this.name = name;
        this.itemStackHandler = handler;
    }

    public void add(BlockPos pos) {
        relativeMonitorPositions.add(pos);
    }

    public void remove(BlockPos pos) {
        relativeMonitorPositions.remove(pos);
    }

    public BlockPos getRowLeft(int row) throws IndexOutOfBoundsException {
        List<Integer> yLevels = new ArrayList<>();
        Set<Integer> yLevelsSet = new HashSet<>();
        for (BlockPos pos : relativeMonitorPositions) {
            if (!yLevelsSet.contains(pos.getY())) {
                yLevelsSet.add(pos.getY());
                yLevels.add(pos.getY());
            }
        }
        int y = yLevels.stream().sorted().toList().get(row);
        BlockPos out = null;
        for (BlockPos pos : relativeMonitorPositions) {
            if (pos.getY() == y && (out == null || pos.getX() < out.getX())) {
                out = pos;
            }
        }
        if (out == null) throw new IndexOutOfBoundsException();
        return out;
    }

    public boolean contains(BlockPos pos) {
        return relativeMonitorPositions.contains(pos);
    }

    public boolean isEmpty() {
        return relativeMonitorPositions.isEmpty();
    }

    public Set<BlockPos> getRelativePositions() {
        return relativeMonitorPositions;
    }

    public @Nullable CoverBehavior getTarget() {
        return null;
    }
}
