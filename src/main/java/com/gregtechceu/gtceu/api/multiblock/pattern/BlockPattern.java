package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BlockPattern implements IBlockPattern {

    @Getter
    protected final RelativeDirection[] directions;

    // TODO: can this be removed? Multiblocks with repeatable isles can differ in sizes
    @Getter
    protected final int[] dimensions;
    @Getter
    protected final OriginOffset offset;

    protected final boolean hasStartOffset;
    @Getter
    protected final PatternSlice[] slices;
    @Getter
    protected final SliceStrategy sliceStrategy;
    @Getter
    protected final Char2ObjectMap<PatternPredicate> predicates;

    public BlockPattern(PatternSlice[] slices, SliceStrategy sliceStrategy,
                        int[] dimensions, RelativeDirection[] directions,
                        @Nullable OriginOffset offset, @Nullable OriginOffset anchorOffset,
                        Char2ObjectMap<PatternPredicate> predicates,
                        char centerChar) {
        this.slices = slices;
        this.sliceStrategy = sliceStrategy;
        this.dimensions = dimensions;
        this.directions = directions;
        this.predicates = predicates;
        hasStartOffset = offset != null;

        if (offset == null) {
            this.offset = new OriginOffset();
            legacyStartOffset(centerChar);
        } else {
            this.offset = offset;
        }

        if (anchorOffset != null) { // needs to be negative cause of double offsetting
            this.offset.move(RelativeDirection.FRONT, -anchorOffset.get(RelativeDirection.FRONT));
            this.offset.move(RelativeDirection.UP, -anchorOffset.get(RelativeDirection.UP));
            this.offset.move(RelativeDirection.LEFT, -anchorOffset.get(RelativeDirection.LEFT));
        }
    }

    private void legacyStartOffset(char center) {
        if (center == 0) return;
        for (int sliceI = 0; sliceI < dimensions[0]; sliceI++) {
            int[] res = slices[sliceI].firstInstanceOf(center);
            if (res != null) {
                moveOffset(directions[0], -sliceI);
                moveOffset(directions[1], -res[0]);
                moveOffset(directions[2], -res[1]);
                return;
            }
        }
        throw new IllegalStateException("Failed to find center symbol:  '" + center + "'");
    }

    @Override
    public void checkPatternFastAt(Level level, PatternState patternState, BlockPos centerPos, Direction frontFacing,
                                   Direction upwardsFacing, boolean allowsFlip) {
        if (!patternState.cache.isEmpty()) {
            boolean pass = true;
            BlockPos.MutableBlockPos mBlockPos = new BlockPos.MutableBlockPos();
            for (var entry : patternState.cache.long2ObjectEntrySet()) {
                BlockPos pos = mBlockPos.set(entry.getLongKey()).immutable();
                BlockState state = level.getBlockState(pos);

                if (state != entry.getValue().getBlockState()) {
                    pass = false;
                    break;
                }

                BlockEntity cachedBlockEntity = entry.getValue().getBlockEntity();

                if (cachedBlockEntity != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != cachedBlockEntity) {
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

        boolean valid = checkPatternAt(level, patternState, centerPos, frontFacing, upwardsFacing, false);
        if (valid) {
            // reaching here means the cache failed or was empty
            patternState.setState(PatternState.CheckState.VALID_UNCACHED);
            patternState.setFlipped(false);
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
        Objects.requireNonNull(patternState, "PatternState not set");

        patternState.globalCount.clear();
        patternState.layerCount.clear();
        // Make sure every prerdicate with a minvalue is checked
        for (PatternPredicate predicate : predicates.values()) {
            for (BasePredicate basePredicate : predicate.subPredicates) {
                if (basePredicate.minCount > 0) {
                    patternState.globalCount.putIfAbsent(basePredicate, 0);
                }
            }
        }
        // only try to clear the cache for structure checking mapping when checking the structure for unflipped
        // maybe switch to a multiblock state value instead?
        if (!isFlipped) {
            patternState.cache.clear();
        }

        patternState.currentBlockInfo.setLevel(level);

        BlockPos.MutableBlockPos controllerPos = centerPos.mutable();

        sliceStrategy.setPattern(this);
        sliceStrategy.start(controllerPos, frontFacing, upwardsFacing);
        if (!sliceStrategy.check(patternState, isFlipped)) return false;

        for (Object2IntMap.Entry<BasePredicate> entry : patternState.globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minCount) {
                patternState
                        .setError(new SinglePredicateError(entry.getKey(), SinglePredicateError.ErrorType.MIN_COUNT,
                                entry.getIntValue()));
                return false;
            }
        }

        patternState.setError(null);
        return true;
    }

    /**
     * Checks a specific slice for validity
     *
     * @param controllerPos The position of the controller
     * @param frontFacing   The front facing of the controller
     * @param upwardsFacing The up facing of the controller
     * @param sliceIndex    The index of the slice, this is where the pattern is gotten from, treats repeatable slices
     *                      as only 1
     * @param sliceOffset   The offset of the slice, how much offset in sliceDir to check the blocks in world, for
     *                      example, if the first slice is repeated 2 times, sliceIndex is 1 while this is 2
     * @param flip          Whether to flip or not
     * @return True if the check passed
     */
    public boolean checkSlice(BlockPos.MutableBlockPos controllerPos, PatternState patternState, Direction frontFacing,
                              Direction upwardsFacing,
                              int sliceIndex, int sliceOffset, boolean flip) {
        Direction absoluteSlice = directions[0].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction absoluteString = directions[1].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction absoluteChar = directions[2].getRelativeFacing(frontFacing, upwardsFacing, flip);

        BlockPos.MutableBlockPos sliceStart = startPos(controllerPos, frontFacing, upwardsFacing, flip)
                .move(absoluteSlice, sliceOffset);

        BlockPos.MutableBlockPos stringStart = sliceStart.mutable();
        BlockPos.MutableBlockPos charPos = sliceStart.mutable();
        PatternSlice slice = slices[sliceIndex];

        patternState.layerCount.clear();

        for (int stringIdx = 0; stringIdx < dimensions[1]; stringIdx++) {
            for (int charIdx = 0; charIdx < dimensions[2]; charIdx++) {
                patternState.currentBlockInfo.setCurrentPos(charPos);
                PatternPredicate pred = predicates.get(slice.charAt(stringIdx, charIdx));

                if (!pred.equals(PatternPredicate.ANY)) {
                    BlockEntity blockEntity = patternState.currentBlockInfo.retrieveCurrentBlockEntity();
                    BlockState state = patternState.currentBlockInfo.retrieveCurrentBlockState();
                    patternState.cache.put(charPos.asLong(), new BlockInfo(state, blockEntity));
                }

                List<PatternError> errors = pred.test(patternState.currentBlockInfo, patternState.globalCount,
                        patternState.layerCount);
                if (!errors.isEmpty()) {
                    patternState.setErrors(errors);
                    return false;
                }

                charPos.move(absoluteChar);
            }

            stringStart.move(absoluteString);
            charPos.set(stringStart);
        }

        for (Object2IntMap.Entry<BasePredicate> entry : patternState.layerCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minSliceCount) {
                patternState.setError(
                        new SinglePredicateError(entry.getKey(), SinglePredicateError.ErrorType.MIN_LAYER_COUNT,
                                entry.getIntValue()));
                return false;
            }
        }

        return true;
    }

    public int getRepetitionCount(int sliceIndex) {
        return slices[sliceIndex].actualRepeats;
    }

    private BlockPos.MutableBlockPos startPos(BlockPos.MutableBlockPos controllerPos, Direction frontFacing,
                                              Direction upwardsFacing, boolean flip) {
        BlockPos.MutableBlockPos start = controllerPos.mutable();
        offset.apply(start, frontFacing, upwardsFacing, flip);
        return start;
    }
}
