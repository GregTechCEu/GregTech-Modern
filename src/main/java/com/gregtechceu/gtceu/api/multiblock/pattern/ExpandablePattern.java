package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.OriginOffset;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class ExpandablePattern implements IBlockPattern {

    @FunctionalInterface
    public interface BoundsProvider {

        @Nullable IntList apply(Level level, BlockPos.MutableBlockPos pos, Direction front, Direction upwards);

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

            // int[] arr = pos.getAll();
            // this basically reshuffles the coordinates into absolute form from relative form
            mPos.set(BlockPos.ZERO).move(absolutes[0], pos.getX()).move(absolutes[1], pos.getY()).move(absolutes[2],
                    pos.getZ());
            // translate from the origin to the center
            mPos = mPos.offset(translation).mutable();
            patternState.currentBlockInfo.setCurrentPos(mPos);

            if (!pred.equals(PatternPredicate.ANY)) {
                var bstate = patternState.currentBlockInfo.retrieveCurrentBlockState();
                BlockEntity be = patternState.currentBlockInfo.retrieveCurrentBlockEntity();
                patternState.cache.put(mPos.asLong(), new BlockInfo(bstate, be));
                // patternState.posCache.add(mPos.immutable());
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

    @Override
    public Long2ObjectSortedMap<@Nullable PatternPredicate> getDefaultShape(MultiblockControllerMachine src,
                                                                            CompoundTag tag) {
        Direction front = src.getFrontFacing();
        Direction up = src.getUpwardsFacing();

        IntList bounds = boundsProvider.apply(src.getLevel(), src.getBlockPos().mutable(), front, up);
        if (tag.isEmpty()) {
            bounds = new IntArrayList();
        }
        if (bounds == null || bounds.isEmpty()) return Long2ObjectSortedMaps.emptyMap();

        Long2ObjectSortedMap<PatternPredicate> predicates = new Long2ObjectRBTreeMap<>();

        BlockPos.MutableBlockPos negCorner = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos posCorner = new BlockPos.MutableBlockPos();

        Direction[] absolutes = new Direction[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = directions[i];

            absolutes[i] = selected.getRelativeFacing(front, up, false);

            if (i == 0) {
                negCorner.setX(-bounds.getInt(selected.oppositeOrdinal()));
                posCorner.setX(bounds.getInt(selected.ordinal()));
            } else if (i == 1) {
                negCorner.setY(-bounds.getInt(selected.oppositeOrdinal()));
                posCorner.setY(bounds.getInt(selected.ordinal()));
            } else {
                negCorner.setZ(-bounds.getInt(selected.oppositeOrdinal()));
                posCorner.setZ(bounds.getInt(selected.ordinal()));
            }
        }

        BlockPos.MutableBlockPos translation = src.getBlockPos().mutable();

        for (var pos : BlockPos.betweenClosed(negCorner, posCorner)) {
            BlockPos.MutableBlockPos mPos = pos.mutable();
            BlockPos.MutableBlockPos adjustPos = pos.mutable();
            PatternPredicate pred = predicateProvider.apply(mPos, bounds);

            // this basically reshuffles the coordinates into absolute form from relative form
            mPos.set(BlockPos.ZERO)
                    .move(absolutes[0], adjustPos.getX())
                    .move(absolutes[1], adjustPos.getY())
                    .move(absolutes[2], adjustPos.getZ())
                    .move(translation.getX(), translation.getY(), translation.getZ());

            if (!pred.equals(PatternPredicate.ANY) && !pred.equals(PatternPredicate.AIR)) {
                predicates.put(mPos.asLong(), pred);
            }
        }
        return predicates;
    }

    @Override
    public void autoBuild(Map<String, IBlockPattern> patterns, MultiblockControllerMachine controller,
                          CompoundTag tag, UseOnContext context) {
        var predicates = getDefaultShape(controller, new CompoundTag());

        var level = context.getLevel();

        Object2IntMap<PatternPredicate> predicateIndex = new Object2IntOpenHashMap<>();
        Object2IntMap<BasePredicate> globalCache = new Object2IntOpenHashMap<>();
        Map<BasePredicate, BlockInfo> cache = new HashMap<>();

        BiPredicate<Long, BlockInfo> placePredicate = (l, info) -> {
            BlockPos p = BlockPos.of(l);

            if (!level.isEmptyBlock(p)) {
                // cache the block?
                return true;
            }

            ItemStack removed = IBlockPattern.tryRemoveItem(context.getPlayer(), info.getItemStackForm());
            if (removed.isEmpty()) return false;

            level.setBlockAndUpdate(p, info.getBlockState());

            MetaMachine metaMachine = MetaMachine.getMachine(level, p);
            if (metaMachine == null) return false;

            // try to force the front face to an air block
            if (predicates.containsKey(p.relative(metaMachine.getFrontFacing()).asLong())) {
                Direction valid = null;
                for (var dir : GTUtil.HORIZONTALS) {
                    if (!predicates.containsKey(p.relative(dir).asLong())) {
                        valid = dir;
                        break;
                    }
                }
                if (valid != null) metaMachine.setFrontFacing(valid);
                else {
                    if (!predicates.containsKey(p.relative(Direction.UP).asLong())) {
                        metaMachine.setFrontFacing(Direction.UP);
                    } else if (!predicates.containsKey(p.relative(Direction.DOWN).asLong())) {
                        metaMachine.setFrontFacing(Direction.DOWN);
                    }
                }
            }
            return true;
        };

        for (var entry : predicates.long2ObjectEntrySet()) {
            PatternPredicate predicate = entry.getValue();
            if (predicate == null) continue;
            if (predicateIndex.getInt(predicate) >= predicate.subPredicates.size()) continue;

            int pointer = predicateIndex.getInt(predicate);
            BasePredicate simplePredicate = predicate.subPredicates.get(pointer);
            int count = globalCache.getInt(simplePredicate);

            try {
                while ((simplePredicate.previewCount == -1 || count == simplePredicate.previewCount) &&
                        (simplePredicate.minCount == -1 || count == simplePredicate.minCount)) {
                    pointer++;
                    simplePredicate = predicate.subPredicates.get(pointer);
                    count = globalCache.getInt(simplePredicate);
                }
                predicateIndex.put(predicate, pointer);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            globalCache.mergeInt(simplePredicate, 1, Integer::sum);
            if (simplePredicate.candidates.isEmpty()) continue;

            cache.computeIfAbsent(simplePredicate, pred -> pred.candidates.get(0));

            if (!placePredicate.test(entry.getLongKey(), cache.get(simplePredicate))) {
                return;
            }
            entry.setValue(null);
        }
        predicateIndex.clear();

        // FIXME why is this loop duplicated twice?
        //  also, why is this copy-pasted from BlockPattern?
        for (var entry : predicates.long2ObjectEntrySet()) {
            PatternPredicate predicate = entry.getValue();
            if (predicate == null || predicateIndex.getInt(predicate) >= predicate.subPredicates.size()) continue;

            BasePredicate simplePredicate = predicate.subPredicates.get(predicateIndex.getInt(predicate));
            int count = globalCache.getInt(simplePredicate);

            while (count == simplePredicate.previewCount || count == simplePredicate.maxCount) {
                int newIdx = predicateIndex.mergeInt(predicate, 1, Integer::sum);
                if (newIdx >= predicate.subPredicates.size()) {
                    GTCEu.LOGGER.warn("failed to generate default structure pattern");
                    return;
                }
                simplePredicate = predicate.subPredicates.get(newIdx);
                count = globalCache.getInt(simplePredicate);
            }
            globalCache.mergeInt(simplePredicate, 1, Integer::sum);
            if (simplePredicate.candidates.isEmpty()) continue;

            cache.computeIfAbsent(simplePredicate, pred -> pred.candidates.get(0));
            if (!placePredicate.test(entry.getLongKey(), cache.get(simplePredicate))) {
                return;
            }
        }
    }
}
