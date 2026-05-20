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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class ExpandablePattern implements IBlockPattern {

    @FunctionalInterface
    public interface BoundsFunction {

        int[] apply(Level level, BlockPos.MutableBlockPos pos, Direction front, Direction upwards);
    }

    protected final BoundsFunction boundsFunc;
    protected final BiFunction<BlockPos.MutableBlockPos, int[], PatternPredicate> predicateFunc;
    @Getter
    protected final OriginOffset offset = new OriginOffset();

    protected final RelativeDirection[] directions;

    public ExpandablePattern(@NotNull BoundsFunction boundsFunc,
                             @NotNull BiFunction<BlockPos.MutableBlockPos, int[], PatternPredicate> predicateFunc,
                             @NotNull RelativeDirection[] directions) {
        this.boundsFunc = boundsFunc;
        this.predicateFunc = predicateFunc;
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
                if (patternState.hasError()) {
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

        // clearCache();
        patternState.getCache().clear();
        patternState.setState(PatternState.CheckState.INVALID_UNCACHED);
    }

    @Override
    public boolean checkPatternAt(Level level, PatternState patternState, BlockPos centerPos, Direction frontFacing,
                                  Direction upwardsFacing,
                                  boolean isFlipped) {
        int[] bounds = boundsFunc.apply(level, centerPos.mutable(), frontFacing, upwardsFacing);
        if (bounds == null) return false;

        patternState.globalCount.clear();

        BlockPos.MutableBlockPos negCorner = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos posCorner = new BlockPos.MutableBlockPos();

        Direction[] absolutes = new Direction[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = directions[i];

            absolutes[i] = selected.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

            if (i == 0) {
                negCorner.setX(-bounds[selected.oppositeOrdinal()]);
                posCorner.setX(bounds[selected.ordinal()]);
            } else if (i == 1) {
                negCorner.setY(-bounds[selected.oppositeOrdinal()]);
                posCorner.setY(bounds[selected.ordinal()]);
            } else {
                negCorner.setZ(-bounds[selected.oppositeOrdinal()]);
                posCorner.setZ(bounds[selected.ordinal()]);
            }
        }

        patternState.cbi.setLevel(level);

        BlockPos.MutableBlockPos translation = centerPos.mutable();

        // SOUTH, UP, EAST means point is +z, line is +y, plane is +x. this basically means the x val of the iter is
        // aisle count, y is str count, and z is char count.
        for (var pos : BlockPos.betweenClosed(negCorner, posCorner)) {
            BlockPos.MutableBlockPos mPos = pos.mutable();
            PatternPredicate pred = predicateFunc.apply(mPos, bounds);

            // int[] arr = pos.getAll();
            // this basically reshuffles the coordinates into absolute form from relative form
            mPos.set(BlockPos.ZERO).move(absolutes[0], pos.getX()).move(absolutes[1], pos.getY()).move(absolutes[2],
                    pos.getZ());
            // translate from the origin to the center
            mPos = mPos.offset(translation).mutable();
            patternState.cbi.setCurrentPos(mPos);

            if (!pred.equals(PatternPredicate.ANY)) {
                var bstate = patternState.cbi.retrieveCurrentBlockState();
                BlockEntity be = patternState.cbi.retrieveCurrentBlockEntity();
                patternState.cache.put(mPos.asLong(), new BlockInfo(bstate, be));
                // patternState.posCache.add(mPos.immutable());
            }

            PatternError res = pred.test(patternState.cbi, patternState.globalCount, null);
            if (res != null) {
                patternState.setError(res);
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
    public Long2ObjectSortedMap<PatternPredicate> getDefaultShape(MultiblockControllerMachine src,
                                                                  CompoundTag tag) {
        Direction front = src.getFrontFacing();
        Direction up = src.getUpwardsFacing();

        int[] bounds = boundsFunc.apply(src.getLevel(), src.getBlockPos().mutable(), front, up);
        if (tag.isEmpty()) {
            bounds = new int[] { 0, 4, 2, 2, 2, 2 };
        }
        if (bounds == null) return Long2ObjectSortedMaps.emptyMap();

        Long2ObjectSortedMap<PatternPredicate> predicates = new Long2ObjectRBTreeMap<>();

        BlockPos.MutableBlockPos negCorner = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos posCorner = new BlockPos.MutableBlockPos();

        Direction[] absolutes = new Direction[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = directions[i];

            absolutes[i] = selected.getRelativeFacing(front, up, false);

            if (i == 0) {
                negCorner.setX(-bounds[selected.oppositeOrdinal()]);
                posCorner.setX(bounds[selected.ordinal()]);
            } else if (i == 1) {
                negCorner.setY(-bounds[selected.oppositeOrdinal()]);
                posCorner.setY(bounds[selected.ordinal()]);
            } else {
                negCorner.setZ(-bounds[selected.oppositeOrdinal()]);
                posCorner.setZ(bounds[selected.ordinal()]);
            }
        }

        BlockPos.MutableBlockPos translation = src.getBlockPos().mutable();

        for (var pos : BlockPos.betweenClosed(negCorner, posCorner)) {
            BlockPos.MutableBlockPos mPos = pos.mutable();
            BlockPos.MutableBlockPos adjustPos = pos.mutable();
            PatternPredicate pred = predicateFunc.apply(mPos, bounds);

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
    public void autobuild(Object2ObjectMap<String, IBlockPattern> patterns, MultiblockControllerMachine controller,
                          CompoundTag tag, UseOnContext context) {
        var predicates = getDefaultShape(controller, null);

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

            var removed = IBlockPattern.tryRemoveItem(context.getPlayer(), info.getItemStackForm());
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
            var pred = entry.getValue();
            if (predicateIndex.getInt(pred) >= pred.predicateList.size()) continue;

            int pointer = predicateIndex.getInt(pred);
            BasePredicate simplePred = pred.predicateList.get(pointer);
            int count = globalCache.getInt(simplePred);

            try {
                while ((simplePred.previewCount == -1 || count == simplePred.previewCount) &&
                        (simplePred.minCount == -1 || count == simplePred.minCount)) {
                    pointer++;
                    simplePred = pred.predicateList.get(pointer);
                    count = globalCache.getInt(simplePred);
                }
                predicateIndex.put(pred, pointer);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            globalCache.mergeInt(simplePred, 1, Integer::sum);
            if (simplePred.candidates == null) continue;

            var finalSimple = simplePred;
            cache.computeIfAbsent(simplePred, k -> finalSimple.candidates.apply(tag)[0]);

            if (!placePredicate.test(entry.getLongKey(), cache.get(simplePred))) return;
            entry.setValue(null);
        }
        predicateIndex.clear();

        for (var entry : predicates.long2ObjectEntrySet()) {
            var pred = entry.getValue();
            if (pred == null || predicateIndex.getInt(pred) >= pred.predicateList.size()) continue;

            BasePredicate simplePred = pred.predicateList.get(predicateIndex.getInt(pred));
            int count = globalCache.getInt(simplePred);

            while (count == simplePred.previewCount || count == simplePred.maxCount) {
                int newIdx = predicateIndex.mergeInt(pred, 1, Integer::sum);
                if (newIdx >= pred.predicateList.size()) {
                    GTCEu.LOGGER.warn("failed to generate default structure pattern");
                    return;
                }
                simplePred = pred.predicateList.get(newIdx);
                count = globalCache.getInt(simplePred);
            }
            globalCache.mergeInt(simplePred, 1, Integer::sum);
            if (simplePred.candidates == null) continue;
            var finalSimple = simplePred;
            cache.computeIfAbsent(simplePred, k -> finalSimple.candidates.apply(tag)[0]);
            if (!placePredicate.test(entry.getLongKey(), cache.get(simplePred))) return;
        }
    }

    private static ItemStack tryRemoveItem(Player player, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (player.isCreative()) return stack.copy();

        for (var item : player.getInventory().items) {
            if (stack.is(stack.getItem()) && stack.getCount() <= item.getCount()) {
                item.setCount(item.getCount() - stack.getCount());
                return item.copy();
            }
        }
        return ItemStack.EMPTY;
    }
}
