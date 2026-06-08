package com.gregtechceu.gtceu.api.multiblock.util;

import com.google.common.collect.Table;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import jdk.jfr.Experimental;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpandablePatternStructureUtil {

    public static final Direction[] DIRECTIONS_IN_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST, Direction.UP, Direction.DOWN };

    private Table<PatternPredicate, BasePredicate, BlockInfo> blockPreferences;
    private Table<PatternPredicate, BasePredicate, Pair<Integer, Integer>> minMaxPreferences;
    private List<Integer> userDimensions;


    public static Pair<Pair<BlockPos, BlockPos>, Direction[]> getCorners(List<Integer> bounds, ExpandablePattern pattern, Direction frontFacing, Direction upFacing, boolean isFlipped){
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
        int relX = absPos.getX() * absolutes[0].getStepX() + absPos.getY() * absolutes[0].getStepY() + absPos.getZ() * absolutes[0].getStepZ();
        int relY = absPos.getX() * absolutes[1].getStepX() + absPos.getY() * absolutes[1].getStepY() + absPos.getZ() * absolutes[1].getStepZ();
        int relZ = absPos.getX() * absolutes[2].getStepX() + absPos.getY() * absolutes[2].getStepY() + absPos.getZ() * absolutes[2].getStepZ();
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
                                                        Map<Long, BlockInfo> userBlockPreferences, Direction frontFacing, Direction upFacing, boolean isFlipped) {
        var cornerStuff = getCorners(userDimensions, pattern, frontFacing, upFacing, isFlipped);
        var corners = cornerStuff.left();
        var absolutes = cornerStuff.right();
        // contains is min<=x<max, inflate to make sure all pos are inside
        // kinda gross but it's the least invasive way I guess, maybe lookf or something better
        var bounds = new AABB(corners.left(), corners.right()).inflate(0.5);
        for (var entry : userBlockPreferences.entrySet()) {
            var pos = BlockPos.of(entry.getKey()); // absolute-space
            // Reverse-transform to relative/pattern space (transpose of orthogonal rotation) to check against bounds
            int relX = pos.getX() * absolutes[0].getStepX() + pos.getY() * absolutes[0].getStepY() + pos.getZ() * absolutes[0].getStepZ();
            int relY = pos.getX() * absolutes[1].getStepX() + pos.getY() * absolutes[1].getStepY() + pos.getZ() * absolutes[1].getStepZ();
            int relZ = pos.getX() * absolutes[2].getStepX() + pos.getY() * absolutes[2].getStepY() + pos.getZ() * absolutes[2].getStepZ();

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

    private boolean isValidCandidate(Map<BlockPos, BlockInfo> resultStructure, PatternPredicate predicate, BlockInfo newInfo) {
        // newInfo is valid if there's a basePredicate it qualifies for whose maxCount (global) and maxSliceCount
        // (this slice) wouldn't be exceeded by placing it here.
        for (BasePredicate basePredicate : predicate.predicateList) {
            if (!basePredicate.candidates.contains(newInfo)) continue;
            int maxCount = minMaxPreferences.contains(predicate, basePredicate) ?
                    minMaxPreferences.get(predicate, basePredicate).right() :
                    basePredicate.maxCount;
            if (maxCount == 0) continue;
            int totalAlreadyPopulated = countGlobal(resultStructure, basePredicate);
            if (maxCount != -1 && totalAlreadyPopulated >= maxCount) continue;
            return true;
        }
        return false;
    }

    private static int[] getDimensions(char[][][] charPattern) {
        int d0 = charPattern.length;
        int d1 = d0 > 0 ? charPattern[0].length : 0;
        int d2 = d1 > 0 ? charPattern[0][0].length : 0;
        return new int[] { d0, d1, d2 };
    }

    public static char[][][] rotateAndFlipCharPattern(char[][][] localFlattenedPattern,
                                                      RelativeDirection[] patternDirections,
                                                      Direction frontFacing, Direction upFacing, boolean isFlipped) {
        Direction absoluteDir0 = patternDirections[0].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteDir1 = patternDirections[1].getRelativeFacing(frontFacing, upFacing, isFlipped);
        Direction absoluteDir2 = patternDirections[2].getRelativeFacing(frontFacing, upFacing, isFlipped);

        var dimensions = getDimensions(localFlattenedPattern);
        if (dimensions[0] == 0 || dimensions[1] == 0 || dimensions[2] == 0) return new char[0][0][0];

        // Per-axis step vectors of each absolute direction.
        int[][] steps = {
                { absoluteDir0.getStepX(), absoluteDir0.getStepY(), absoluteDir0.getStepZ() },
                { absoluteDir1.getStepX(), absoluteDir1.getStepY(), absoluteDir1.getStepZ() },
                { absoluteDir2.getStepX(), absoluteDir2.getStepY(), absoluteDir2.getStepZ() },
        };
        // Max local index reached along each local axis.
        int[] extents = { dimensions[0] - 1, dimensions[1] - 1, dimensions[2] - 1 };

        // World-space bounding box. Each axis contributes monotonically, so the extremes are
        // reached at index 0 or at extents[axis] depending on the sign of the step.
        int[] min = new int[3];
        int[] max = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            for (int world = 0; world < 3; world++) {
                int contribution = steps[axis][world] * extents[axis];
                min[world] += Math.min(0, contribution);
                max[world] += Math.max(0, contribution);
            }
        }

        int sizeX = max[0] - min[0] + 1;
        int sizeY = max[1] - min[1] + 1;
        int sizeZ = max[2] - min[2] + 1;
        char[][][] result = new char[sizeX][sizeY][sizeZ];

        for (int s = 0; s < dimensions[0]; s++) {
            for (int t = 0; t < dimensions[1]; t++) {
                for (int c = 0; c < dimensions[2]; c++) {
                    int worldX = steps[0][0] * s + steps[1][0] * t + steps[2][0] * c;
                    int worldY = steps[0][1] * s + steps[1][1] * t + steps[2][1] * c;
                    int worldZ = steps[0][2] * s + steps[1][2] * t + steps[2][2] * c;
                    result[worldX - min[0]][worldY - min[1]][worldZ - min[2]] = localFlattenedPattern[s][t][c];
                }
            }
        }

        return result;
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

    private static int getCoordFromDir(BlockPos pos, Direction dir) {
        return dir.getAxis().choose(pos.getX(), pos.getY(), pos.getZ());
    }
}
