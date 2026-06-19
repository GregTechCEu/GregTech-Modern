package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class ExpandablePattern implements IBlockPattern {

    @FunctionalInterface
    public interface BoundsProvider {

        @Nullable
        IntList apply(Level level, BlockPos.MutableBlockPos pos, Direction front, Direction upwards);

        BoundsProvider EMPTY = (l, p, f, u) -> new IntArrayList(new int[] { 0, 0, 0, 0, 0, 0 });
    }

    @FunctionalInterface
    public interface BoundsConstraintProvider {

        List<IntIntPair> apply();
    }

    protected final BoundsProvider boundsProvider;
    @Getter
    @Setter
    protected @Nullable BoundsConstraintProvider boundsConstraints = null;
    @Getter
    protected final BiFunction<BlockPos.MutableBlockPos, List<Integer>, PatternPredicate> predicateProvider;
    @Getter
    protected final OriginOffset offset = new OriginOffset();

    @Getter
    protected final RelativeDirection[] directions;

    public ExpandablePattern(BoundsProvider boundsProvider,
                             BiFunction<BlockPos.MutableBlockPos, List<Integer>, PatternPredicate> predicateProvider,
                             RelativeDirection[] directions) {
        this.boundsProvider = boundsProvider;
        this.predicateProvider = predicateProvider;
        this.directions = directions;
    }

    @Override
    public void checkPatternFastAt(Level level, PatternState patternState, BlockPos centerPos, Direction frontFacing,
                                   Direction upwardsFacing, boolean allowsFlip) {
        if (!patternState.cache.isEmpty()) {
            boolean pass = true;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (var entry : patternState.cache.long2ObjectEntrySet()) {
                pos.set(entry.getLongKey());
                BlockState state = level.getBlockState(pos);

                if (state != entry.getValue().getBlockState()) {
                    pass = false;
                    break;
                }

                BlockEntity cachedBE = entry.getValue().getBlockEntity();
                if (cachedBE != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != cachedBE) {
                        pass = false;
                        break;
                    }
                }
            }
            if (pass) {
                if (patternState.hasErrors()) {
                    patternState.setState(PatternState.CheckState.INVALID_CACHED);
                } else {
                    patternState.setState(PatternState.CheckState.VALID_CACHED);
                }

                return;
            }
        }

        patternState.setFlipped(false);
        boolean valid = checkPatternAt(level, patternState, centerPos, frontFacing, upwardsFacing, false);
        if (valid) {
            patternState.setState(PatternState.CheckState.VALID_UNCACHED);
            return;
        }

        if (allowsFlip) {
            valid = checkPatternAt(level, patternState, centerPos, frontFacing, upwardsFacing, true);
        }
        if (!valid) {
            // maybe empty the block info part of the cache?
            patternState.setState(PatternState.CheckState.INVALID_UNCACHED);
            return;
        }

        patternState.setState(PatternState.CheckState.VALID_UNCACHED);
        patternState.setFlipped(true);
    }

    @Override
    public boolean checkPatternAt(Level level, PatternState patternState, BlockPos centerPos, Direction frontFacing,
                                  Direction upwardsFacing,
                                  boolean isFlipped) {
        List<Integer> bounds = boundsProvider.apply(level, centerPos.mutable(), frontFacing, upwardsFacing);
        if (bounds.isEmpty()) return false;

        patternState.globalCount.clear();

        BlockPos.MutableBlockPos negCorner = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos posCorner = new BlockPos.MutableBlockPos();

        Direction[] absolutes = new Direction[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = directions[i];

            absolutes[i] = selected.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

            if (i == 0) {
                negCorner.setX(-bounds.get(selected.oppositeOrdinal()));
                posCorner.setX(bounds.get(selected.ordinal()));
            } else if (i == 1) {
                negCorner.setY(-bounds.get(selected.oppositeOrdinal()));
                posCorner.setY(bounds.get(selected.ordinal()));
            } else {
                negCorner.setZ(-bounds.get(selected.oppositeOrdinal()));
                posCorner.setZ(bounds.get(selected.ordinal()));
            }
        }

        patternState.currentBlockInfo.setLevel(level);

        BlockPos.MutableBlockPos translation = centerPos.mutable();

        // SOUTH, UP, EAST means point is +z, line is +y, plane is +x. this basically means the x val of the iter is
        // aisle count, y is str count, and z is char count.
        for (var pos : BlockPos.betweenClosed(negCorner, posCorner)) {
            BlockPos.MutableBlockPos mPos = pos.mutable();
            PatternPredicate pred = predicateProvider.apply(mPos, bounds);

            // this basically reshuffles the coordinates into absolute form from relative form
            mPos.set(BlockPos.ZERO).move(absolutes[0], pos.getX()).move(absolutes[1], pos.getY()).move(absolutes[2],
                    pos.getZ());
            // translate from the origin to the center
            mPos = mPos.offset(translation).mutable();
            patternState.currentBlockInfo.setCurrentPos(mPos);

            if (!pred.equals(PatternPredicate.ANY)) {
                BlockState state = patternState.currentBlockInfo.retrieveCurrentBlockState();
                BlockEntity blockEntity = patternState.currentBlockInfo.retrieveCurrentBlockEntity();
                patternState.cache.put(mPos.asLong(), new BlockInfo(state, blockEntity));
            }

            List<PatternError> res = pred.test(patternState.currentBlockInfo, patternState.globalCount, null);
            if (!res.isEmpty()) {
                patternState.setErrors(res);
                return false;
            }
        }

        for (var entry : patternState.globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minCount) {
                patternState.setError(new SinglePredicateError(entry.getKey(),
                        SinglePredicateError.ErrorType.MIN_COUNT, entry.getIntValue()));
                return false;
            }
        }

        patternState.setError(null);
        return true;
    }
}
