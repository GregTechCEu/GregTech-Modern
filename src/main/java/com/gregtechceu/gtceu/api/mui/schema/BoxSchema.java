package com.gregtechceu.gtceu.api.mui.schema;

import com.gregtechceu.gtceu.utils.BlockPosUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import lombok.Getter;

import java.util.function.BiPredicate;

public class BoxSchema extends PosListSchema {

    public static BoxSchema of(Level level, BlockPos center, int r) {
        return new BoxSchema(level, center.offset(-r, -r, -r), center.offset(r, r, r), (pos, state) -> true);
    }

    public static BoxSchema of(Level level, BlockPos center, int r, BiPredicate<BlockPos, BlockState> renderFilter) {
        return new BoxSchema(level, center.offset(-r, -r, -r), center.offset(r, r, r), renderFilter);
    }

    @Getter
    private final Level level;
    @Getter
    private final BlockPos min, max;
    private final Vec3 center;

    public BoxSchema(Level level, BlockPos min, BlockPos max, BiPredicate<BlockPos, BlockState> renderFilter) {
        super(level, BlockPosUtil.getAllInside(min, max, false), renderFilter);
        this.level = level;
        this.min = BlockPosUtil.getMin(min, max);
        this.max = BlockPosUtil.getMax(min, max);
        this.center = BlockPosUtil.getCenterD(min, max);
    }

    @Override
    public Vec3 getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return min;
    }
}
