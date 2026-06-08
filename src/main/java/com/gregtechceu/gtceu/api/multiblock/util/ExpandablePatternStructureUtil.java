package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

public class ExpandablePatternStructureUtil {

    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP, Direction.DOWN };

    private Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences;
    private Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> minMaxPreferences;
    private List<Integer> userDimensions;

    public static Pair<Pair<BlockPos, BlockPos>, Direction[]> getCorners(List<Integer> bounds,
                                                                         ExpandablePattern pattern,
                                                                         Direction frontFacing, Direction upFacing,
                                                                         boolean isFlipped) {
        BlockPos.MutableBlockPos negCorner = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos posCorner = new BlockPos.MutableBlockPos();

        Direction[] absolutes = new Direction[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = pattern.getDirections()[i];

            absolutes[i] = selected.getRelativeFacing(frontFacing, upFacing, isFlipped);

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
        return Pair.of(Pair.of(posCorner, negCorner), absolutes);
    }

    public PatternPredicate getPredicateFromPos(ExpandablePattern pattern, BlockPos absPos,
                                                Direction frontFacing, Direction upFacing, boolean isFlipped) {
        Direction[] absolutes = getCorners(userDimensions, pattern, frontFacing, upFacing, isFlipped).right();
        // Reverse the absolute→relative transform (transpose of orthogonal rotation matrix)
        int relX = getOffsetFromDirection(absolutes[0], absPos);
        int relY = getOffsetFromDirection(absolutes[1], absPos);
        int relZ = getOffsetFromDirection(absolutes[2], absPos);
        return pattern.getPredicateFunc().apply(new BlockPos(relX, relY, relZ).mutable(), userDimensions);
    }

    public void populatePreferenceTables(Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences,
                                         Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> minMaxPreferences,
                                         List<Integer> userDimensions) {
        this.blockPreferences = blockPreferences;
        this.minMaxPreferences = minMaxPreferences;
        this.userDimensions = userDimensions;
    }

    public void populateWithUserBlockPreferences(Map<BlockPos, BlockInfo> resultStructure, ExpandablePattern pattern,
                                                 Map<Long, BlockInfo> userBlockPreferences, Direction frontFacing,
                                                 Direction upFacing, boolean isFlipped) {
        var cornerStuff = getCorners(userDimensions, pattern, frontFacing, upFacing, isFlipped);
        var corners = cornerStuff.left();
        var absolutes = cornerStuff.right();
        // contains is min<=x<max, inflate to make sure all pos are inside
        // kinda gross but it's the least invasive way I guess, maybe lookf or something better
        var bounds = new AABB(corners.left(), corners.right()).inflate(1.0);
        for (var entry : userBlockPreferences.entrySet()) {
            var pos = BlockPos.of(entry.getKey()); // absolute-space
            // Reverse-transform to relative/pattern space (transpose of orthogonal rotation) to check against bounds
            int relX = getOffsetFromDirection(absolutes[0], pos);
            int relY = getOffsetFromDirection(absolutes[1], pos);
            int relZ = getOffsetFromDirection(absolutes[2], pos);

            if (bounds.contains(relX, relY, relZ)) {
                resultStructure.put(pos, entry.getValue());
            }
        }
    }

    public void populateFromPattern(Map<BlockPos, BlockInfo> resultStructure, ExpandablePattern pattern,
                                    Direction frontFacing, Direction upFacing, boolean isFlipped) {
        BlockPos.MutableBlockPos translation = BlockPos.ZERO.mutable();
        var corners = getCorners(userDimensions, pattern, frontFacing, upFacing, isFlipped);
        var negCorner = corners.left().left();
        var posCorner = corners.left().right();
        var absolutes = corners.right();

        var predicateFunc = pattern.getPredicateFunc();
        // SOUTH, UP, EAST means point is +z, line is +y, plane is +x. this basically means the x val of the iter is
        // aisle count, y is str count, and z is char count.
        for (var pos : BlockPos.betweenClosed(negCorner, posCorner)) {
            BlockPos.MutableBlockPos mPos = pos.mutable();
            PatternPredicate predicate = predicateFunc.apply(mPos, userDimensions);

            // int[] arr = pos.getAll();
            // this basically reshuffles the coordinates into absolute form from relative form
            mPos.set(BlockPos.ZERO).move(absolutes[0], pos.getX()).move(absolutes[1], pos.getY()).move(absolutes[2],
                    pos.getZ());
            // translate from the origin to the center
            mPos = mPos.offset(translation).mutable();
            if (resultStructure.containsKey(mPos)) continue;

            // Attempts to first place the predicate if the min(layer)count isn't satisfied, then the
            // max(layer)count
            if (tryMinCount(resultStructure, predicate, mPos)) continue;
            if (tryMaxCount(resultStructure, predicate, mPos)) continue;
            // If we arrive here, there's nothing we can place that doesn't overflow a max count!
            throw new IllegalStateException("Could not place a block without breaking maxCount requirements");
        }
    }

    private boolean tryMinCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos) {
        for (BasePredicate basePredicate : predicate.predicateList) {
            int minCount = minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).left() :
                    basePredicate.minCount;
            if (minCount == 0) continue;
            int totalAlreadyPopulated = countGlobal(resultStructure, basePredicate);
            if (!(minCount > 0 && totalAlreadyPopulated < minCount)) continue;
            BlockInfo toInsert = null;
            if (blockPreferences.contains(predicate, basePredicate)) {
                toInsert = blockPreferences.get(predicate, basePredicate);
            } else {
                if (!basePredicate.getCandidates().isEmpty()) {
                    toInsert = basePredicate.getCandidates().get(0);
                }
            }
            if (toInsert != null) resultStructure.put(pos, toInsert);
            return true;
        }
        return false;
    }

    private boolean tryMaxCount(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate,
                                BlockPos pos) {
        for (BasePredicate basePredicate : predicate.predicateList) {
            int maxCount = minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).right() :
                    basePredicate.maxCount;
            if (maxCount == 0) continue;
            int totalAlreadyPopulated = countGlobal(resultStructure, basePredicate);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;
            BlockInfo toInsert = null;
            if (blockPreferences.contains(predicate, basePredicate)) {
                toInsert = blockPreferences.get(predicate, basePredicate);
            } else {
                if (!basePredicate.getCandidates().isEmpty()) {
                    toInsert = basePredicate.getCandidates().get(0);
                }
            }
            if (toInsert != null) resultStructure.put(pos, toInsert);
            return true;
        }
        return false;
    }

    private static int getOffsetFromDirection(Direction dir, BlockPos pos) {
        return dir.getAxis().choose(pos.getX(), pos.getY(), pos.getZ()) * dir.getAxisDirection().getStep();
    }

    public static void fixRotationsAndFacing(Map<BlockPos, BlockInfo> resultStructure, Direction frontFacing,
                                             Direction upFacing, Block controllerBlock) {
        Map<BlockPos, BlockState> toUpdate = new Object2ObjectOpenHashMap<>();
        for (var entry : resultStructure.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState currentState = entry.getValue().getBlockState();
            Direction valid = null;
            if (currentState.getBlock() instanceof MetaMachineBlock machineBlock) {
                if (!currentState.hasProperty(machineBlock.getRotationState().property)) continue;
                if (machineBlock.equals(controllerBlock)) {
                    var newState = currentState.setValue(machineBlock.getRotationState().property, frontFacing);
                    if (newState.hasProperty(GTBlockStateProperties.UPWARDS_FACING))
                        newState = newState.setValue(GTBlockStateProperties.UPWARDS_FACING, upFacing);
                    toUpdate.put(pos, newState);
                    continue;
                }
                for (var dir : DIRECTIONS_IN_ORDER) {
                    if (!machineBlock.getRotationState().test(valid)) continue;
                    if (!resultStructure.containsKey(pos.relative(dir))) {
                        valid = dir;
                        break;
                    }
                }
                if (valid != null) {
                    toUpdate.put(pos, currentState.setValue(machineBlock.getRotationState().property, valid));
                }
            }
        }
        for (var entry : toUpdate.entrySet()) {
            resultStructure.put(entry.getKey(), BlockInfo.fromBlockState(entry.getValue()));
        }
    };

    private static int countGlobal(Map<BlockPos, BlockInfo> resultStructure, BasePredicate basePredicate) {
        return (int) resultStructure.values()
                .stream()
                .filter(blockInfo -> basePredicate.getCandidates().contains(blockInfo))
                .count();
    }
}
