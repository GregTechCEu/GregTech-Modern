package com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor;

import net.minecraft.core.BlockPos;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonitorGroup {

    private final Set<BlockPos> relativeMonitorPositions = new HashSet<>();
    @Getter
    private final String name;

    public MonitorGroup(String name) {
        this.name = name;
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
}
