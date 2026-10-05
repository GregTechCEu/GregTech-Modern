package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractStructureHelper {

    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP, Direction.DOWN };

    protected @Nullable Block controllerBlock;

    public static AbstractStructureHelper blockPattern(Int2IntMap sliceRepeats) {
        return new BlockPatternHelper(sliceRepeats);
    }

    public static AbstractStructureHelper expandable(IntList sliceRepeats) {
        return new ExpandablePatternHelper(sliceRepeats);
    }

    abstract protected Char2ObjectMap<MultiPredicate> getPredicatesFromPattern(IBlockPattern pattern);

    public void populate(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure, IBlockPattern pattern,
                         @Nullable Object2ObjectMap<BlockPos, BlockInfo> userBlockPreferences,
                         Direction frontFacing, Direction upFacing, boolean isFlipped) {
        setup(pattern, frontFacing, upFacing, isFlipped);
        if (userBlockPreferences != null && !userBlockPreferences.isEmpty()) {
            populateWithUserBlockPreferences(info, resultStructure, pattern, userBlockPreferences, frontFacing,
                    upFacing,
                    isFlipped);
        }
        Char2ObjectMap<MultiPredicate> sortedPredicates = sortPredicatesForPreferences(
                getPredicatesFromPattern(pattern), info.getBlockPreferences());
        populateFromPattern(info, resultStructure, pattern, sortedPredicates, frontFacing, upFacing, isFlipped);
        fixRotationsAndFacing(resultStructure, frontFacing, upFacing, this.controllerBlock);
    }

    protected void setup(IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped) {}

    protected abstract void populateWithUserBlockPreferences(MultiblockSchemaInfo info,
                                                             Map<BlockPos, BlockInfo> resultStructure,
                                                             IBlockPattern pattern,
                                                             Object2ObjectMap<BlockPos, BlockInfo> userBlockPreferences,
                                                             Direction frontFacing, Direction upFacing,
                                                             boolean isFlipped);

    protected abstract void populateFromPattern(MultiblockSchemaInfo info, Map<BlockPos, BlockInfo> resultStructure,
                                                IBlockPattern pattern, Char2ObjectMap<MultiPredicate> sortedPredicates,
                                                Direction frontFacing, Direction upFacing, boolean isFlipped);

    public abstract MultiPredicate getPredicateFromPos(IBlockPattern pattern, BlockPos pos,
                                                       Direction frontFacing, Direction upFacing, boolean isFlipped);

    private Pair<Boolean, MultiPredicate> sortPredicateRecursive(MultiPredicate predicate, BlockInfo preference) {
        List<BasePredicate> matchingPreds = new ArrayList<>();
        for (var basePred : predicate.predicates()) {
            if (basePred.getCandidates().contains(preference)) {
                matchingPreds.add(basePred);
            }
        }
        int i = 0;
        if (!matchingPreds.isEmpty()) {
            List<BasePredicate> sortedPredicates = new ArrayList<>(predicate.predicates());
            for (var matchingPred : matchingPreds) {
                sortedPredicates.remove(matchingPred);
                // Keep track of index so multiple preds that match keep their ordering
                sortedPredicates.add(i++, matchingPred);
            }
            return Pair.of(true,
                    predicate.getType().makePredicate(predicate.children(), sortedPredicates, predicate.hasAir()));
        }

        List<MultiPredicate> matchingChildren = new ArrayList<>();
        for (var child : predicate.children()) {
            var childMatches = sortPredicateRecursive(child, preference);
            if (childMatches.getFirst()) matchingChildren.add(childMatches.getSecond());
        }

        if (!matchingChildren.isEmpty()) {
            List<MultiPredicate> sortedChildren = new ArrayList<>(predicate.children());
            for (var matchingChild : matchingChildren) {
                sortedChildren.remove(matchingChild);
                // Keep track of index so multiple children that match keep their ordering
                sortedChildren.add(i++, matchingChild);
            }
            return Pair.of(true,
                    predicate.getType().makePredicate(sortedChildren, predicate.predicates(), predicate.hasAir()));
        }
        return Pair.of(false, predicate);
    }

    private Char2ObjectMap<MultiPredicate> sortPredicatesForPreferences(Char2ObjectMap<MultiPredicate> predicates,
                                                                        Char2ObjectMap<BlockInfo> preferences) {
        if (preferences.isEmpty()) return predicates;
        Char2ObjectMap<MultiPredicate> sortedMap = new Char2ObjectOpenHashMap<>(predicates.size());
        for (var entry : predicates.char2ObjectEntrySet()) {
            var charKey = entry.getCharKey();
            var predicate = entry.getValue().deepCopy();
            var preference = preferences.get(charKey);
            // noinspection ConstantConditions - preferences.get returns null when entry isn't present
            if (preference != null) {
                var sorted = sortPredicateRecursive(predicate, preference);
                if (sorted.getFirst()) {
                    predicate = sorted.getSecond();
                }
            }
            sortedMap.put(charKey, predicate);
        }
        return sortedMap;
    }

    // TODO backing map from predicate(base?) -> count
    protected static int countPopulatedGlobal(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate) {
        return (int) resultStructure.values().stream()
                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                .count();
    }

    // TODO backing map from predicate(base?) + layer offset -> count
    protected static int countPopulatedInSlice(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate,
                                               Direction dir, int offset) {
        return (int) resultStructure.entrySet().stream()
                .filter(e -> getCoordFromDir(e.getKey(), dir) == offset)
                .filter(e -> basePredicate.getCandidates().contains(e.getValue()))
                .count();
    }

    protected static int getCoordFromDir(BlockPos pos, Direction dir) {
        return dir.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
    }

    protected static void fixRotationsAndFacing(Map<BlockPos, BlockInfo> resultStructure, Direction frontFacing,
                                                Direction upFacing, @Nullable Block controllerBlock) {
        Map<BlockPos, BlockState> toUpdate = new Object2ObjectOpenHashMap<>();
        for (var entry : resultStructure.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState currentState = entry.getValue().getBlockState();
            if (!(currentState.getBlock() instanceof MetaMachineBlock machine)) {
                continue;
            }
            if (!currentState.hasProperty(machine.getRotationState().property)) continue;

            if (machine == controllerBlock) {
                BlockState newState = currentState.setValue(machine.getRotationState().property, frontFacing);
                if (newState.hasProperty(GTBlockStateProperties.UPWARDS_FACING)) {
                    newState = newState.setValue(GTBlockStateProperties.UPWARDS_FACING, upFacing);
                }
                toUpdate.put(pos, newState);
                continue;
            }

            Direction validFacing = null;
            for (Direction dir : DIRECTIONS_IN_ORDER) {
                // make sure the machine can face this way
                if (!machine.getRotationState().test(dir)) continue;
                // and that there won't be a block in front of it
                if (!resultStructure.containsKey(pos.relative(dir))) {
                    validFacing = dir;
                    break;
                }
            }
            if (validFacing != null) {
                toUpdate.put(pos, currentState.setValue(machine.getRotationState().property, validFacing));
            }
        }
        for (var entry : toUpdate.entrySet()) {
            resultStructure.put(entry.getKey(), BlockInfo.fromBlockState(entry.getValue()));
        }
    }
}
