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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

public class BlockPattern implements IBlockPattern {

    protected final RelativeDirection[] directions;

    @Getter
    protected final int[] dimensions;
    @Getter
    protected final OriginOffset offset;

    protected final boolean hasStartOffset;
    @Getter
    protected final PatternAisle[] aisles;
    @Getter
    protected final AisleStrategy aisleStrategy;
    @Getter
    protected final Char2ObjectMap<PatternPredicate> predicates;

    public BlockPattern(@NotNull PatternAisle @NotNull [] aisles, @NotNull AisleStrategy aisleStrategy,
                        int @NotNull [] dimensions, @NotNull RelativeDirection @NotNull [] directions,
                        @Nullable OriginOffset offset, @Nullable OriginOffset anchorOffset,
                        @NotNull Char2ObjectMap<@NotNull PatternPredicate> predicates,
                        char centerChar) {
        this.aisles = aisles;
        this.aisleStrategy = aisleStrategy;
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
        for (int aisleI = 0; aisleI < dimensions[0]; aisleI++) {
            int[] res = aisles[aisleI].firstInstanceOf(center);
            if (res != null) {
                moveOffset(directions[0], -aisleI);
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
                if (patternState.hasError()) {
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
        if (patternState == null) {
            throw new IllegalStateException("PatternState not set");
        }
        patternState.globalCount.clear();
        patternState.layerCount.clear();
        patternState.cache.clear();

        patternState.cbi.setLevel(level);

        BlockPos.MutableBlockPos controllerPos = centerPos.mutable();

        aisleStrategy.pattern = this;
        aisleStrategy.start(controllerPos, frontFacing, upwardsFacing);
        if (!aisleStrategy.check(patternState, isFlipped)) return false;

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
     * Checks a specific aisle for validity
     *
     * @param controllerPos The position of the controller
     * @param frontFacing   The front facing of the controller
     * @param upwardsFacing The up facing of the controller
     * @param aisleIndex    The index of the aisle, this is where the pattern is gotten from, treats repeatable aisles
     *                      as only 1
     * @param aisleOffset   The offset of the aisle, how much offset in aisleDir to check the blocks in world, for
     *                      example, if the first aisle is repeated 2 times, aisleIndex is 1 while this is 2
     * @param flip          Whether to flip or not
     * @return True if the check passed
     */
    public boolean checkAisle(BlockPos.MutableBlockPos controllerPos, PatternState patternState, Direction frontFacing,
                              Direction upwardsFacing,
                              int aisleIndex, int aisleOffset, boolean flip) {
        Direction absoluteAisle = directions[0].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction absoluteString = directions[1].getRelativeFacing(frontFacing, upwardsFacing, flip);
        Direction absoluteChar = directions[2].getRelativeFacing(frontFacing, upwardsFacing, flip);

        BlockPos.MutableBlockPos aisleStart = startPos(controllerPos, frontFacing, upwardsFacing, flip)
                .move(absoluteAisle, aisleOffset);

        BlockPos.MutableBlockPos stringStart = aisleStart.mutable();
        BlockPos.MutableBlockPos charPos = aisleStart.mutable();
        PatternAisle aisle = aisles[aisleIndex];

        patternState.layerCount.clear();

        for (int stringIdx = 0; stringIdx < dimensions[1]; stringIdx++) {
            for (int charIdx = 0; charIdx < dimensions[2]; charIdx++) {
                patternState.cbi.setCurrentPos(charPos);
                PatternPredicate pred = predicates.get(aisle.charAt(stringIdx, charIdx));

                if (!pred.equals(PatternPredicate.ANY)) {
                    BlockEntity be = patternState.cbi.retrieveCurrentBlockEntity();
                    BlockState state = patternState.cbi.retrieveCurrentBlockState();
                    patternState.cache.put(charPos.asLong(), new BlockInfo(state, be));
                }

                PatternError res = pred.test(patternState.cbi, patternState.globalCount, patternState.layerCount);
                if (res != null) {
                    patternState.setError(res);
                    return false;
                }

                charPos.move(absoluteChar);
            }

            stringStart.move(absoluteString);
            charPos.set(stringStart);
        }

        for (Object2IntMap.Entry<BasePredicate> entry : patternState.layerCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minLayerCount) {
                patternState.setError(
                        new SinglePredicateError(entry.getKey(), SinglePredicateError.ErrorType.MIN_LAYER_COUNT,
                                entry.getIntValue()));
                return false;
            }
        }

        return true;
    }

    public int getRepetitionCount(int aisleIndex) {
        return aisles[aisleIndex].actualRepeats;
    }

    @Override
    public Long2ObjectSortedMap<PatternPredicate> getDefaultShape(MultiblockControllerMachine src,
                                                                  CompoundTag tag) {
        Long2ObjectSortedMap<PatternPredicate> map = new Long2ObjectRBTreeMap<>();
        Direction absoluteAisle = directions[0].getRelativeFacing(src.getFrontFacing(), src.getUpwardsFacing());
        Direction absoluteString = directions[1].getRelativeFacing(src.getFrontFacing(), src.getUpwardsFacing());
        Direction absoluteChar = directions[2].getRelativeFacing(src.getFrontFacing(), src.getUpwardsFacing());

        BlockPos.MutableBlockPos pos = src.getBlockPos().mutable();
        BlockPos.MutableBlockPos start = startPos(pos, src.getFrontFacing(), src.getUpwardsFacing(), false);
        BlockPos.MutableBlockPos serial = start.mutable();

        int[] order = aisleStrategy.getDefaultAisles(tag);
        for (int i = 0; i < order.length; i++) {
            for (int j = 0; j < dimensions[1]; j++) {
                for (int k = 0; k < dimensions[2]; k++) {
                    PatternPredicate pred = predicates.get(aisles[order[i]].charAt(j, k));
                    if (!pred.equals(PatternPredicate.ANY))
                        map.put(serial.asLong(), pred);
                    serial.move(absoluteChar);
                }
                serial.move(absoluteString);
                serial.move(absoluteChar.getOpposite(), dimensions[2]);
            }
            serial.set(start);
            serial.move(absoluteAisle, i + 1);
        }

        return map;
    }

    private BlockPos.MutableBlockPos startPos(BlockPos.MutableBlockPos controllerPos, Direction frontFacing,
                                              Direction upwardsFacing, boolean flip) {
        BlockPos.MutableBlockPos start = controllerPos.mutable();
        offset.apply(start, frontFacing, upwardsFacing, flip);
        return start;
    }

    @Override
    public void autobuild(Reference2ObjectMap<String, IBlockPattern> patterns, MultiblockControllerMachine controller,
                          CompoundTag tag, UseOnContext context) {
        var predicates = getDefaultShape(controller, tag);

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

            var be = level.getBlockEntity(p);
            if (!(be instanceof MetaMachine metaMachine)) return true;

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

    public void retrievePatternInformation(String name, MultiblockControllerMachine controller, CompoundTag tag) {
        String controllerName = controller.getDefinition().getName();
        tag.putString("name", controllerName + "#" + name);

        for (var mapping : predicates.char2ObjectEntrySet()) {
            var predicate = mapping.getValue();
            if (predicate.equals(PatternPredicate.ANY)) continue;

            if (predicate.predicateList.size() == 1) {
                CompoundTag predicateTag = new CompoundTag();

                BasePredicate simplePred = predicate.predicateList.get(0);
                predicateTag.putInt("min", simplePred.minCount);
                predicateTag.putInt("max", simplePred.maxCount);
                predicateTag.putInt("minLayer", simplePred.minLayerCount);
                predicateTag.putInt("maxLayer", simplePred.maxLayerCount);

                tag.put(simplePred.getPredicateName(), predicateTag);
            }

            if (predicate.predicateList.size() > 1) {

            }
        }
    }
}
