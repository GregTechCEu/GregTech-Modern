package com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public class MonitorGroup {

    private final Set<BlockPos> monitorPositions = new HashSet<>();
    @Getter
    private final String name;
    @Getter
    private final CustomItemStackHandler itemStackHandler;
    @Getter
    @Setter
    private @Nullable BlockPos target;
    @Getter
    @Setter
    private @Nullable Direction targetCoverSide;

    public MonitorGroup(String name) {
        this(name, new CustomItemStackHandler(1));
    }

    public MonitorGroup(String name, CustomItemStackHandler handler) {
        this.name = name;
        this.itemStackHandler = handler;
    }

    public void add(BlockPos pos) {
        monitorPositions.add(pos);
    }

    public void remove(BlockPos pos) {
        monitorPositions.remove(pos);
    }

    public List<BlockPos> getRow(int row, UnaryOperator<BlockPos> toRelative) throws IndexOutOfBoundsException {
        Set<Integer> yLevelsSet = new HashSet<>();
        for (BlockPos pos : monitorPositions) {
            yLevelsSet.add(toRelative.apply(pos).getY());
        }
        int y = yLevelsSet.stream().sorted().toList().get(row);
        List<BlockPos> rowPositions = new ArrayList<>();
        for (BlockPos pos : monitorPositions) {
            if (toRelative.apply(pos).getY() == y) {
                rowPositions.add(toRelative.apply(pos));
            }
        }
        rowPositions.sort(Comparator.comparingInt(Vec3i::getX));
        return rowPositions;
    }

    public boolean contains(BlockPos pos) {
        return monitorPositions.contains(pos);
    }

    public boolean isEmpty() {
        return monitorPositions.isEmpty();
    }

    public Set<BlockPos> getRelativePositions() {
        return monitorPositions;
    }

    public @Nullable CoverBehavior getTargetCover(Level level) {
        if (target != null && targetCoverSide != null) {
            ICoverable coverable = GTCapabilityHelper.getCoverable(level, target, targetCoverSide);
            if (coverable != null) return coverable.getCoverAtSide(targetCoverSide);
        }
        return null;
    }
}
