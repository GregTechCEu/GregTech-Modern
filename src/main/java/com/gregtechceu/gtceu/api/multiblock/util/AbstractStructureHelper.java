package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractStructureHelper {

    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP, Direction.DOWN };

    protected final HashBasedTable<PatternPredicate, BasePredicate, BlockInfo> blockPreferences = HashBasedTable
            .create();
    protected final HashBasedTable<PatternPredicate, BasePredicate, IntIntPair> minMaxPreferences = HashBasedTable
            .create();
    protected @Nullable Block controllerBlock;

    public static AbstractStructureHelper blockPattern(Int2IntMap sliceRepeats) {
        return new BlockPatternHelper(sliceRepeats);
    }

    public static AbstractStructureHelper expandable(IntList sliceRepeats) {
        return new ExpandablePatternHelper(sliceRepeats);
    }

    public Table<PatternPredicate, BasePredicate, BlockInfo> getBlockPreferences() {
        return this.blockPreferences;
    }

    public Table<PatternPredicate, BasePredicate, IntIntPair> getMinMaxPreferences() {
        return this.minMaxPreferences;
    }

    public void populate(Map<BlockPos, BlockInfo> resultStructure, IBlockPattern pattern,
                         @Nullable Long2ObjectMap<BlockInfo> userBlockPreferences,
                         Direction frontFacing, Direction upFacing, boolean isFlipped) {
        setup(pattern, frontFacing, upFacing, isFlipped);
        if (userBlockPreferences != null && !userBlockPreferences.isEmpty()) {
            populateWithUserBlockPreferences(resultStructure, pattern, userBlockPreferences, frontFacing, upFacing,
                    isFlipped);
        }
        populateFromPattern(resultStructure, pattern, frontFacing, upFacing, isFlipped);
        fixRotationsAndFacing(resultStructure, frontFacing, upFacing, this.controllerBlock);
    }

    protected void setup(IBlockPattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped) {}

    protected abstract void populateWithUserBlockPreferences(Map<BlockPos, BlockInfo> resultStructure,
                                                             IBlockPattern pattern,
                                                             Long2ObjectMap<BlockInfo> userBlockPreferences,
                                                             Direction frontFacing, Direction upFacing,
                                                             boolean isFlipped);

    protected abstract void populateFromPattern(Map<BlockPos, BlockInfo> resultStructure, IBlockPattern pattern,
                                                Direction frontFacing, Direction upFacing, boolean isFlipped);

    public abstract PatternPredicate getPredicateFromPos(IBlockPattern pattern, BlockPos pos,
                                                         Direction frontFacing, Direction upFacing, boolean isFlipped);

    protected int getMinCount(PatternPredicate predicate, BasePredicate basePredicate) {
        if (!minMaxPreferences.contains(predicate, basePredicate))
            return basePredicate.minCount;
        return minMaxPreferences.get(predicate, basePredicate).leftInt();
    }

    protected int getMaxCount(PatternPredicate predicate, BasePredicate basePredicate) {
        if (!minMaxPreferences.contains(predicate, basePredicate))
            return basePredicate.maxCount;
        return minMaxPreferences.get(predicate, basePredicate).rightInt();
    }

    protected static int countPopulatedGlobal(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate) {
        return (int) resultStructure.values().stream()
                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                .count();
    }

    protected static int countPopulatedInLayer(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate,
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
